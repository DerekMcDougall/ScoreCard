package freefrogurt.scorecard;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

public class Game implements Parcelable {
	public static enum Suit {
		HEARTS(R.string.hearts), SPADES(R.string.spades), CLUBS(R.string.clubs), DIAMONDS(
				R.string.diamonds), HIGH(R.string.high), LOW(R.string.low);

		private int resourceId;

		private Suit(int resourceId) {
			this.resourceId = resourceId;
		}

		public int getResourceId() {
			return resourceId;
		}
	};

	private List<Team> teams;
	private List<Player> players;
	private List<Hand> hands;

	// TODO: these should be customizable for other games, like King Pedro
	// For Bid Euchre, you win at 52 points
	private int goalScore = 52;
	// For Bid Euchre, there are 8 points per hand, unless bidder goes alone
	private int pointsPerHand = 8;
	// Some people play that you cannot win the game unless you win the bid for the current hand.
	// This allows some sore losers to prevent the end of the game by bidding way higher than they 
	// can possibly get.  Default to the more sane "bid-or-set" rule, where you can also win the game
	// if you reach the goal in a hand where you set the offense back (euchering them)
	private boolean mustHaveBidToWin = false;
	// Usually, the defense cannot count points when the offense wins a hand if doing so would bring them to
	// the goal score.  King Pedro is an exception, where the defense can exceed the goal but does not yet win.
	// Note that this allows for a scenario where the winning team has a lower score than the losing team!
	private boolean canExceedGoalWithoutWinning = false;
	
	public Game() {
		hands = new ArrayList<Hand>();
		teams = new ArrayList<Team>();
		players = new ArrayList<Player>();
	}

	public int getGoal() {
		return goalScore;
	}
	
	public int getPointsPerHand() {
		return pointsPerHand;
	}
	
	public boolean getMustHaveBidToWin() {
		return mustHaveBidToWin;
	}
	
	public boolean getCanExceedGoalWithoutWinning() {
		return canExceedGoalWithoutWinning;
	}
	
	public Player addPlayer(String name, int teamId) {
		Player newPlayer = new Player(name);
		players.add(newPlayer);
		teams.get(teamId).addPlayer(newPlayer);
		return newPlayer;
	}

	public int addTeam(String name) {
		teams.add(new Team(name));
		// return the index of the team
		return teams.size() - 1;
	}

	public List<Player> getPlayers() {
		// This should return an iterator, or something that is not modifiable
		return players;
	}

	public Player getPlayerById(int id) {
		return players.get(id);
	}

	public int getPlayerTeamId(Player player) {
		for (int teamId = 0; teamId < teams.size(); teamId++) {
			if (teams.get(teamId).hasPlayer(player)) {
				return teamId;
			}
		}
		return -1;
	}

	public List<Team> getTeams() {
		// This should return an iterator, or something that is not modifiable
		return teams;
	}

	public Hand getCurrentHand() {
		if (hands == null || hands.isEmpty()) {
			return null;
		}
		return hands.get(hands.size() - 1);
	}

	public Score getCurrentScore() {
		// Find most recent completed hand
		if (hands != null) {
			for (int i = hands.size() - 1; i >= 0; i--) {
				if (hands.get(i).isHandDone()) {
					return hands.get(i).getResultingScore();
				}
			}
		}
		// No completed hands yet. Return 0 for each team.
		if (teams == null || teams.isEmpty()) {
			return null;
		}
		int[] points = new int[teams.size()];
		for (int i = 0; i < points.length; i++) {
			points[i] = 0;
		}
		return new Score(points);
	}

	public List<Hand> getHands() {
		// This should be read-only...
		return hands;
	}

	public Hand startNextHand() throws GameStateException {
		Hand lastHand = getCurrentHand();
		int dealerId = 0; // default to first player
		if (lastHand != null) {
			if (!lastHand.isHandDone()) {
				throw new GameStateException(
						"Cannot start new hand until previous hand is done");
			}
			int lastDealerIndex = lastHand.getDealerId();
			dealerId = (lastDealerIndex + 1) % players.size();
		}
		Hand newHand = new Hand(dealerId);
		hands.add(newHand);
		return newHand;
	}

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel parcel, int flags) {
		// Note: all lists must be preserved in the proper order

		// Save team names. Position in the list is each team's ID.
		List<String> teamNames = new ArrayList<String>(teams.size());
		for (int teamId = 0; teamId < teams.size(); teamId++) {
			teamNames.add(teams.get(teamId).getName());
		}
		parcel.writeStringList(teamNames);

		// Save player names and mapping of each player to a team
		List<String> playerNames = new ArrayList<String>(players.size());
		int[] playerMemberships = new int[players.size()];
		for (int i = 0; i < players.size(); i++) {
			playerNames.add(players.get(i).getName());
			playerMemberships[i] = getPlayerTeamId(players.get(i));
		}
		parcel.writeStringList(playerNames);
		parcel.writeIntArray(playerMemberships);

		Hand[] handsArray = new Hand[hands.size()];
		handsArray = hands.toArray(handsArray);
		parcel.writeParcelableArray(handsArray, 0);
	}

	public static final Parcelable.Creator<Game> CREATOR = new Parcelable.Creator<Game>() {
		public Game createFromParcel(Parcel source) {
			return new Game(source);
		}

		public Game[] newArray(int size) {
			return new Game[size];
		}

	};

	private Game(Parcel in) {
		List<String> teamNames = new ArrayList<String>();
		in.readStringList(teamNames);
		List<String> playerNames = new ArrayList<String>();
		in.readStringList(playerNames);
		int[] playerMemberships = in.createIntArray();
		Parcelable[] handsArray = in.readParcelableArray(null);

		teams = new ArrayList<Team>(teamNames.size());
		for (String teamName : teamNames) {
			teams.add(new Team(teamName));
		}
		players = new ArrayList<Player>(playerNames.size());
		for (int i = 0; i < playerNames.size(); i++) {
			addPlayer(playerNames.get(i), playerMemberships[i]);
		}
		hands = new ArrayList<Hand>(handsArray.length);
		for (int i = 0; i < handsArray.length; i++) {
			hands.add((Hand) handsArray[i]);
		}
	}
}
