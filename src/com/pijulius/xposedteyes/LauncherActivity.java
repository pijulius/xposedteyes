package com.pijulius.xposedteyes;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

public class LauncherActivity extends Activity {
	SharedPreferences preferences;

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);

		SharedPreferences preferences = getSharedPreferences("com.pijulius.xposedteyes_preferences", Context.MODE_PRIVATE);

		String firstApp = preferences.getString("launcherFirstApp", "");
		String secondApp = preferences.getString("launcherSecondApp", "");
		Intent intent = getPackageManager().getLaunchIntentForPackage("com.fb.splitscreenlauncher");

		if (intent != null && !firstApp.isEmpty() && !secondApp.isEmpty()) {
			intent.setAction(Intent.ACTION_MAIN);
        	intent.setClassName("com.fb.splitscreenlauncher", "com.fb.splitscreenlauncher.ui.shortcut.ShortcutActivity");
			intent.putExtra("first", getPackageManager().getLaunchIntentForPackage(firstApp).toUri(0));
			intent.putExtra("second", getPackageManager().getLaunchIntentForPackage(secondApp).toUri(0));
			startActivity(intent);

			finish();
			return;
		}

		intent = new Intent(this, SettingsActivity.class);
		startActivity(intent);

		finish();
		return;
	}
}