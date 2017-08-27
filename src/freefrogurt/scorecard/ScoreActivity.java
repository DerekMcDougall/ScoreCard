package freefrogurt.scorecard;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import freefrogurt.scorecard.Game.Suit;

public class ScoreActivity extends Activity implements OnClickListener {

	public static final String EXTRA_BIDDING_TEAM_ID = "freefrogurt.scorecard.biddingTeam";
	public static final String EXTRA_BID = "freefrogurt.scorecard.bid";
	public static final String EXTRA_PLAYER_LIST = "freefrogurt.scorecard.playerList";
	public static final String EXTRA_POINTS_PER_HAND = "freefrogurt.scorecard.totalPoints";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_score);

		// Populate trump spinner
		Spinner trumpSpinner = (Spinner) findViewById(R.id.trumpSelect);
		String[] trumpArray = new String[Suit.values().length];
		for (int i = 0; i < Suit.values().length; i++) {
			trumpArray[i] = getResources().getString(Suit.values()[i].getResourceId());
		}
		ArrayAdapter<String> trumpAdapter = new HybridArrayAdapter<String>(this,
				android.R.layout.simple_spinner_dropdown_item, trumpArray);
		trumpSpinner.setAdapter(trumpAdapter);

		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		boolean resume = extras.getBoolean(CardSetup.EXTRA_RESUME);
		if (savedInstanceState == null) {
			if (!resume) {
				((ScoreCardApplication) getApplication()).createGame();
				String[] playerArray = extras.getStringArray(CardSetup.EXTRA_PLAYERS);
				initializeTeams(playerArray);

				try {
					startHand();
				} catch (GameStateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				// Resume existing game
				// Check if we need a new hand
				if (getGame().getCurrentHand().isHandDone()) {
					try {
						startHand();
					} catch (GameStateException e) {
						e.printStackTrace();
					}
				}
			}
		}
		// Populate bidder spinner
		String[] playerArray = new String[getGame().getPlayers().size()];
		for (int i = 0; i < getGame().getPlayers().size(); i++) {
			playerArray[i] = getGame().getPlayers().get(i).getName();
		}
		Spinner bidderSpinner = (Spinner) findViewById(R.id.bidderSelect);
		ArrayAdapter<String> bidderAdapter = new HybridArrayAdapter<String>(this,
				android.R.layout.simple_spinner_dropdown_item, playerArray);
		bidderSpinner.setAdapter(bidderAdapter);
	}

	private void initializeTeams(String[] players) {
		// If there are an odd number of players, then everyone plays on their
		// own
		boolean playIndividually = (players.length % 2 != 0);
		int[] teamIds;
		if (playIndividually) {
			// Everyone plays on a "team" of 1
			teamIds = new int[players.length];
		} else {
			// Divide players into 2 teams
			teamIds = new int[2];
		}
		for (int i = 0; i < teamIds.length; i++) {
			teamIds[i] = getGame().addTeam(playIndividually ? players[i] : "Team " + (i + 1));
		}
		int teamIndex = 0;
		for (String name : players) {
			getGame().addPlayer(name, teamIds[teamIndex]);
			teamIndex = (teamIndex + 1) % teamIds.length;
		}
	}

	private Game getGame() {
		return ((ScoreCardApplication) getApplication()).getGame();
	}

	private void startHand() throws GameStateException {
		// Clear bid edit box in case this isn't the first hand
		((EditText) findViewById(R.id.editBid)).setText("");
		getGame().startNextHand();
	}

	@Override
	protected void onStart() {
		super.onStart();
		showScore();
		showDealer();
	}

	private void showDealer() {
		int dealerId = getGame().getCurrentHand().getDealerId();
		TextView dealerView = (TextView) findViewById(R.id.currentDealerView);
		dealerView.setText(getGame().getPlayerById(dealerId).getName());
	}

	private void showScore() {
		Score score = getGame().getCurrentScore();
		TableLayout teamScoreSection = (TableLayout) findViewById(R.id.teamScoresLayout);
		teamScoreSection.setStretchAllColumns(true);
		teamScoreSection.removeAllViews();
		List<Team> teams = getGame().getTeams();
		for (int i = 0; i < teams.size(); i++) {
			TableRow currentLine = new TableRow(this);
			TextView label = new TextView(this);
			label.setText(teams.get(i).getName() + ":");
			label.setTextAppearance(this, android.R.style.TextAppearance_Large);
			currentLine.addView(label);
			// TODO: put points in better position horizontally
			TextView points = new TextView(this);
			points.setText(Integer.toString(score.getScoreForTeam(i)));
			points.setTextAppearance(this, android.R.style.TextAppearance_Large);
			currentLine.addView(points);
			teamScoreSection.addView(currentLine);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_score, menu);
		return true;
	}

	public void onClick(View v) {
		// Save bid info to current hand
		int bidderId = getBidderId();
		// This requires that the position of players in the spinner is the same
		// as in the player list of the game object.
		Player bidder = getGame().getPlayers().get(bidderId);

		EditText bidEdit = (EditText) findViewById(R.id.editBid);
		String bidString = bidEdit.getText().toString();
		int bid;
		try {
			bid = Integer.parseInt(bidString);
		} catch (NumberFormatException ex) {
			bid = 0;
		}
		Hand hand = getGame().getCurrentHand();

		Spinner trumpSpinner = (Spinner) findViewById(R.id.trumpSelect);
		Suit trump = Suit.values()[trumpSpinner.getSelectedItemPosition()];

		hand.setBidResult(bidderId, bid, trump);

		// Now launch new activity to get change in score
		Intent intent = new Intent(this, ScoreUpdateActivity.class);
		intent.putExtra(EXTRA_BIDDING_TEAM_ID, getGame().getPlayerTeamId(bidder));
		intent.putExtra(EXTRA_BID, bid);
		intent.putExtra(EXTRA_POINTS_PER_HAND, getGame().getPointsPerHand());
		// Need to pass the player list for 5-player games to select partner
		if (getGame().getPlayers().size() % 2 == 1) {
			// player names must be passed in the same order that is returned by
			// getPlayers
			String[] playerNames = new String[getGame().getPlayers().size()];
			for (int i = 0; i < playerNames.length; i++) {
				playerNames[i] = getGame().getPlayers().get(i).getName();
			}
			intent.putExtra(EXTRA_PLAYER_LIST, playerNames);
		}
		startActivityForResult(intent, 0);
	}

	private int getBidderId() {
		Spinner bidderSpinner = (Spinner) findViewById(R.id.bidderSelect);
		return bidderSpinner.getSelectedItemPosition();
	}

	public void onClickTemp(View v) {
		// To do: make a different way to access this
		Intent intent = new Intent(this, HistoryGridActivity.class);
		startActivity(intent);
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_CANCELED) {
			return;
		}
		// Add the points to the current score
		Bundle extras = data.getExtras();
		int team1Change = extras.getInt(ScoreUpdateActivity.EXTRA_TEAM_1_CHANGE);
		int team2Change = extras.getInt(ScoreUpdateActivity.EXTRA_TEAM_2_CHANGE);
		int[] pointsDelta = new int[getGame().getTeams().size()];
		if (extras.containsKey(ScoreUpdateActivity.EXTRA_PARTNER_ID)) {
			// Each player is on its own team. "Team 1" in this case is the
			// offense, and "Team 2" the defense
			// Offense is the bidder and partner (if any)
			int bidderId = getBidderId();
			int partnerId = extras.getInt(ScoreUpdateActivity.EXTRA_PARTNER_ID);
			for (int i = 0; i < pointsDelta.length; i++) {
				if (i == bidderId) {
					pointsDelta[i] = team1Change;
				} else if (i == partnerId) {
					// Special case: partner does not get points if it would put
					// him at or above goal
					if (!getGame().getCanExceedGoalWithoutWinning()
							&& getGame().getCurrentScore().getScoreForTeam(i) + team1Change > getGame().getGoal()) {
						pointsDelta[i] = 0;
					} else {
						pointsDelta[i] = team1Change;
					}
				} else {
					// Defenders cannot score if it would put them up to goal.
					// "Bid-or-set" rule doesn't really work when there are no
					// teams.
					int currentScore = getGame().getCurrentScore().getScoreForTeam(i);
					if (currentScore + team2Change >= getGame().getGoal()) {
						pointsDelta[i] = 0;
					} else {
						pointsDelta[i] = team2Change;
					}
				}
			}
		} else {
			// Players are on permanent teams
			pointsDelta[0] = team1Change;
			pointsDelta[1] = team2Change;
			if (!getGame().getCanExceedGoalWithoutWinning()) {
				// Need to check if defense would reach the goal, and if this is
				// allowed in the game rules.
				int offenseId = getGame().getPlayerTeamId(getGame().getPlayerById(getBidderId()));
				int defenseId = (offenseId+1) % 2;
				int currentDefenseScore = getGame().getCurrentScore().getScoreForTeam(defenseId);
				if (currentDefenseScore + pointsDelta[defenseId] >= getGame().getGoal()) {
					// This will put defense at goal.  Do not allow if game has must-have-bid rule or 
					// if the offense was not set back (negative pointsDelta).
					if (getGame().getMustHaveBidToWin() || pointsDelta[offenseId] >= 0) {
						// Defense cannot count points this hand.
						pointsDelta[defenseId] = 0;
					}
				}
			}
		}
		try {
			getGame().getCurrentHand().setResultingScore(getGame().getCurrentScore().addPoints(pointsDelta));
			startHand();
		} catch (GameStateException e) {
			e.printStackTrace();
		}
	}
}
