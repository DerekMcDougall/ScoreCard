package freefrogurt.scorecard;

import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CardSetup extends Activity {

	public static final String EXTRA_PLAYERS = "freefrogurt.scorecard.players";
	public static final String EXTRA_RESUME = "freefrogurt.scorecard.resume";
	public static final String EXTRA_PLAYER_COUNT = "freefrogurt.scorecard.player_count";

	private class ClickHandler implements OnClickListener {

		private CardSetup parent;
		private boolean resume;

		public ClickHandler(CardSetup parent, boolean resume) {
			this.parent = parent;
			this.resume = resume;
		}

		public void onClick(View v) {
			List<String> names = parent.GetNames();

			Intent intent = new Intent(parent, ScoreActivity.class);
			intent.putExtra(EXTRA_RESUME, resume);
			if (!resume) {
				intent.putExtra(EXTRA_PLAYERS, names.toArray(new String[0]));
			}
			startActivity(intent);
		}
	}

	private final int EDIT_TEXT_ID_BASE = 1977;
	private int numPlayers = 4;
	private LinearLayout root;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_card_setup);
		
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if (extras != null && extras.containsKey(EXTRA_PLAYER_COUNT))
		{
			numPlayers = extras.getInt(EXTRA_PLAYER_COUNT);
		}
		root = (LinearLayout)findViewById(R.id.player_list_form);

		LinearLayout inProgressSection = new LinearLayout(this);
		inProgressSection.setId(R.id.game_in_progress_section_id);
		inProgressSection.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT));
		inProgressSection.setOrientation(LinearLayout.VERTICAL);
		inProgressSection.setVisibility(LinearLayout.GONE);
		root.addView(inProgressSection);

		TextView inProgressAlert = new TextView(this);
		inProgressAlert.setText(getResources().getString(R.string.game_in_progress));
		inProgressSection.addView(inProgressAlert);
		
		TextView inProgressDescription = new TextView(this);
		inProgressDescription.setId(R.id.game_in_progress_description_id);
		inProgressSection.addView(inProgressDescription);
		
		Button resumeButton = new Button(this);
		resumeButton.setId(R.id.resume_button_id);
		resumeButton.setText(R.string.resume_button);
		resumeButton.setOnClickListener(new ClickHandler(this, true));
		inProgressSection.addView(resumeButton);

		for (int i = 0; i < numPlayers; i++) {
			LinearLayout layout = new LinearLayout(this);
			layout.setOrientation(LinearLayout.HORIZONTAL);
			TextView label = new TextView(this);
			label.setText("Player " + (i + 1));
			layout.addView(label);
			EditText field = new EditText(this);
			field.setId(EDIT_TEXT_ID_BASE + i);
			field.setSingleLine();
			layout.addView(field, new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.MATCH_PARENT,
					LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
			root.addView(layout, new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.MATCH_PARENT,
					LinearLayout.LayoutParams.WRAP_CONTENT));
		}

		Button startButton = new Button(this);
		startButton.setText(R.string.start_button);
		startButton.setOnClickListener(new ClickHandler(this, false));
		root.addView(startButton);

	}

	@Override
	protected void onStart() {
		super.onStart();
		// Allow user to resume game if a game already exists
		Game existingGame = ((ScoreCardApplication) getApplication()).getGame();
		if (existingGame != null) {
			findViewById(R.id.game_in_progress_section_id).setVisibility(View.VISIBLE);
			TextView description = (TextView)findViewById(R.id.game_in_progress_description_id);
			description.setText(getResources().getString(R.string.game_in_progress_score) + " " + existingGame.getCurrentScore().toShortString());
		} else {
			findViewById(R.id.game_in_progress_section_id).setVisibility(View.GONE);
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_card_setup, menu);
		return true;
	}

	private List<String> GetNames() {
		List<String> names = new ArrayList<String>();
		for (int i = 0; i < numPlayers; i++) {
			EditText editView = (EditText) findViewById(EDIT_TEXT_ID_BASE + i);
			names.add(editView.getText().toString());
		}
		return names;
	}
}
