package freefrogurt.scorecard;

import java.util.ArrayList;
import java.util.List;

public class Team {
	private String name;
	private List<Player> members;
	
	public Team(String name)
	{
		this.name = name;
		members = new ArrayList<Player>();
	}
	
	public String getName() {
		return name;
	}
	
	public void addPlayer(Player player) {
		members.add(player);
	}
	
	public boolean hasPlayer(Player player) {
		// This checks if the same instance exists in the members list.
		// This assumes that only one copy of each player exists.
		return members.contains(player);
	}
}
