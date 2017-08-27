package freefrogurt.scorecard;

import android.app.Application;

public class ScoreCardApplication extends Application {
	private Game currentGame;
	
	public Game getGame() {
		return currentGame;
	}
	
	public Game createGame() {
		return currentGame = new Game();
	}
}
