package freefrogurt.scorecard;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Menu;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class HistoryGridActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_history_grid);

		Game game = ((ScoreCardApplication) getApplication()).getGame();
		TableLayout table = (TableLayout) findViewById(R.id.score_history_grid);
		table.setStretchAllColumns(true);
		TableRow headerRow = new TableRow(this);
		addHeader(headerRow, getString(R.string.bidder_column));
		addHeader(headerRow, getString(R.string.bid_column));
		addHeader(headerRow, getString(R.string.trump_column));
		for (Team team: game.getTeams())
		{
			addHeader(headerRow, team.getName());
		}
		table.addView(headerRow, new ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT));

		// Get the list of hands played so far
		for (Hand hand : game.getHands()) {
			if (!hand.isBiddingDone()) {
				continue;
			}
			TableRow currentRow = new TableRow(this);
			addCell(currentRow, game.getPlayerById(hand.getBidderId())
					.getName());
			addCell(currentRow, Integer.toString(hand.getBid()));
			addCell(currentRow, getResources().getString(hand.getTrump().getResourceId()));
			if (hand.isHandDone()) {
				Score currentScore = hand.getResultingScore();
				for (int i = 0; i < game.getTeams().size(); i++)
				{
					addCell(currentRow, Integer.toString(currentScore.getScoreForTeam(i)));
				}
			} else {
				// No score to show for this hand yet.
				addCell(currentRow, "");
				addCell(currentRow, "");
			}
			table.addView(currentRow, new ViewGroup.LayoutParams(
					ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT));
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_history_grid, menu);
		return true;
	}

	private void addHeader(TableRow row, String text) {
		addCell(row, text, true);
	}

	private void addCell(TableRow row, String text) {
		addCell(row, text, false);
	}

	private void addCell(TableRow row, String text, boolean header) {
		TextView view = new TextView(this);
		if (header) {
			view.setTypeface(null, Typeface.BOLD);
		}
		view.setText(text);
		// TODO: don't use raw pixels for spacing
		view.setPadding(0,0,5,0);
		row.addView(view);
	}
}
