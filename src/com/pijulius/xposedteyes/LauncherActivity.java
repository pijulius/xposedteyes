package com.pijulius.xposedteyes;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class LauncherActivity extends Activity {
	SharedPreferences preferences;

	String musicApp = "";
	String navigationApp = "";
	String videosApp = "";

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);

		if ("AccessibilityService".equals(getIntent().getAction())) {
			SettingsActivity.activateAccessibilityService();

			finish();
			return;
		}

		SharedPreferences preferences = getSharedPreferences(getPackageName()+"_preferences", Context.MODE_PRIVATE);

		musicApp = preferences.getString("launcherMusicApp", "");
		navigationApp = preferences.getString("launcherNavigationApp", "");
		videosApp = preferences.getString("launcherVideosApp", "");

		if (musicApp.isEmpty() || navigationApp.isEmpty() || videosApp.isEmpty()) {
			setContentView(R.layout.launcher);

			final Context context = getBaseContext();
			Button settingsButton = this.findViewById(R.id.settingsButton);

			settingsButton.setOnClickListener(new View.OnClickListener() {
    	        @Override
        	    public void onClick(View view) {
            	    Intent i = new Intent(context, SettingsActivity.class);
		        	startActivity(i);

			        finish();
    	        }
			});
		}
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onStop() {
		super.onStop();
	}
}