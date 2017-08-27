package freefrogurt.scorecard;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;

public class SelectPlayerCountActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_player_count);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_select_player_count, menu);
        return true;
    }
    
    public void onClick4(View v) {
		Intent intent = new Intent(this, CardSetup.class);
		intent.putExtra(CardSetup.EXTRA_PLAYER_COUNT, 4);
		startActivity(intent);
    }
    
    public void onClick5(View v) {
		Intent intent = new Intent(this, CardSetup.class);
		intent.putExtra(CardSetup.EXTRA_PLAYER_COUNT, 5);
		startActivity(intent);
    }
    
    public void onClick6(View v) {
		Intent intent = new Intent(this, CardSetup.class);
		intent.putExtra(CardSetup.EXTRA_PLAYER_COUNT, 6);
		startActivity(intent);
    }
}
