package freefrogurt.scorecard;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class ScoreUpdateActivity extends Activity {

	public static String RESULT_ACTION = "freefrogurt.scorecard.ScoreUpdateActivity.RESULT_ACTION";
	public static String EXTRA_TEAM_1_CHANGE = "freefrogurt.scorecard.team1Change";
	public static String EXTRA_TEAM_2_CHANGE = "freefrogurt.scorecard.team2Change";
	public static String EXTRA_PARTNER_ID = "freefrogurt.scorecard.partnerId";

	private int bid;
	private int pointsPerHand;
	private int biddingTeamId = -1;
	private boolean selectPartner;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_score_update);
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		bid = extras.getInt(ScoreActivity.EXTRA_BID);
		pointsPerHand = extras.getInt(ScoreActivity.EXTRA_POINTS_PER_HAND);
		biddingTeamId = extras.getInt(ScoreActivity.EXTRA_BIDDING_TEAM_ID);

		selectPartner = extras.containsKey(ScoreActivity.EXTRA_PLAYER_LIST);
		if (selectPartner) {
			// This game has no fixed teams, so we have to select the partner
			findViewById(R.id.partner_label).setVisibility(LinearLayout.VISIBLE);
			Spinner partnerSpinner = (Spinner) findViewById(R.id.partner_spinner);
			String[] playerList = extras.getStringArray(ScoreActivity.EXTRA_PLAYER_LIST);
			// Partner choice consists of [no partner] and all players other
			// than the bidder
			String[] partnerArray = new String[playerList.length];
			partnerArray[0] = getResources().getString(R.string.no_partner);
			for (int sourceIndex = 0, destIndex = 1; destIndex < partnerArray.length && sourceIndex < playerList.length; sourceIndex++) {
				// Skip the current bidder
				if (sourceIndex != biddingTeamId) {
					partnerArray[destIndex++] = playerList[sourceIndex];
				}
			}
			partnerSpinner.setAdapter(new HybridArrayAdapter<String>(this,
					android.R.layout.simple_spinner_dropdown_item, partnerArray));
			partnerSpinner.setVisibility(LinearLayout.VISIBLE);

			// Since there are no teams, change the labels to be offense and
			// defense
			((TextView) findViewById(R.id.top_team_label)).setText(R.string.offense_points_label);
			((TextView) findViewById(R.id.bottom_team_label)).setText(R.string.defense_points_label);
		}

		if (bid > 0 && biddingTeamId > -1) {
			// for 5-player games, the top box is the offense, so it's the one
			// that can be euchered
			int eucheredId = (selectPartner || biddingTeamId == 0) ? R.id.team1GoDownButton : R.id.team2GoDownButton;
			Button eucheredButton = (Button) findViewById(eucheredId);
			eucheredButton.setText(Integer.toString(-1 * bid));
			eucheredButton.setVisibility(View.VISIBLE);
		}

		// Pair the complementary edit controls
		ComplementaryEditText team1Edit = (ComplementaryEditText) findViewById(R.id.team1PointsEdit);
		ComplementaryEditText team2Edit = (ComplementaryEditText) findViewById(R.id.team2PointsEdit);
		if (selectPartner || biddingTeamId == 0) {
			ComplementaryEditText.Pair(team1Edit, bid, team2Edit, pointsPerHand);
		} else {
			ComplementaryEditText.Pair(team2Edit, bid, team1Edit, pointsPerHand);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_score_update, menu);
		return true;
	}

	public void onClickEuchered(View v) {
		EditText loserPointsEdit = (EditText) findViewById((selectPartner || biddingTeamId == 0) ? R.id.team1PointsEdit
				: R.id.team2PointsEdit);
		loserPointsEdit.setText(Integer.toString(-1 * bid));
	}

	public void onClickUpdate(View v) {
		try {
			int team1Change = Integer.parseInt(((EditText) findViewById(R.id.team1PointsEdit)).getText().toString());
			int team2Change = Integer.parseInt(((EditText) findViewById(R.id.team2PointsEdit)).getText().toString());
			Intent result = new Intent(RESULT_ACTION);
			result.putExtra(EXTRA_TEAM_1_CHANGE, team1Change);
			result.putExtra(EXTRA_TEAM_2_CHANGE, team2Change);
			if (selectPartner) {
				Spinner partnerSpinner = (Spinner) findViewById(R.id.partner_spinner);
				int partnerPosition = partnerSpinner.getSelectedItemPosition();
				// To convert the spinner position to a player ID:
				// Players appear in the order of their player ID, except the
				// first position is no partner,
				// and the current bidder does not appear in the list.
				// Use -1 for no partner.
				int partnerId = partnerPosition - 1;
				if (partnerId >= this.biddingTeamId) {
					// ID is offset for IDs higher than the current bidder
					// because current bidder isn't in the list
					partnerId++;
				}
				result.putExtra(EXTRA_PARTNER_ID, partnerId);
			}
			setResult(RESULT_OK, result);
			finish();
		} catch (NumberFormatException ex) {
			Toast alert = Toast.makeText(this, getResources().getString(R.string.fill_all_fields), Toast.LENGTH_SHORT);
			alert.show();
		}
	}
}
