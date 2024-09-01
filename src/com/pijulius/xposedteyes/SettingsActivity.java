package com.pijulius.xposedteyes;

import java.io.DataOutputStream;
import java.io.File;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

@SuppressWarnings("deprecation")
public class SettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	private String sharedPreferencesPath;

	public String getSharedPreferencesPath() {
		return null;
	}

	public void fixSharedPreferencesPermission() {
		if (sharedPreferencesPath == null)
			return;

		AsyncTask.execute(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(500);
				} catch (Throwable t) {}

				File sharedPrefsFile = new File(sharedPreferencesPath);

				if (sharedPrefsFile.exists())
					sharedPrefsFile.setReadable(true, false);
			}
		});
	}

	public static int activateAccessibilityService() {
		Process p = null;
		int result = 0;

		try {
			p = Runtime.getRuntime().exec("su");

			DataOutputStream os = new DataOutputStream(p.getOutputStream());
			os.writeBytes(
				"settings put secure enabled_accessibility_services " +
				"com.pijulius.xposedteyes/com.pijulius.xposedteyes.LauncherService\n");

			os.writeBytes("exit\n");
			os.flush();

			result = p.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	@Override
	public SharedPreferences getSharedPreferences(String name, int mode) {
		return super.getSharedPreferences(getPackageName()+"_preferences", 1);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		SharedPreferences preferences = getSharedPreferences(getPackageName()+"_preferences", 1);
		preferences.registerOnSharedPreferenceChangeListener(this);
		sharedPreferencesPath = getSharedPreferencesPath();

		addPreferencesFromResource(R.xml.settings);
		setContentView(R.layout.settings);

		Button marketButton = findViewById(R.id.activateLauncherServiceButton);

		marketButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
				Toast.makeText(
					getBaseContext(), getString(R.string.toast_accessibility_service_request), Toast.LENGTH_SHORT)
				.show();

				if (activateAccessibilityService() != 0) {
					Toast.makeText(
						getBaseContext(), getString(R.string.toast_accessibility_service_failed), Toast.LENGTH_SHORT)
					.show();

				} else {
					Toast.makeText(
						getBaseContext(), getString(R.string.toast_accessibility_service_succeeded), Toast.LENGTH_SHORT)
					.show();
				}
            }
        });
	}

	@Override
	protected void onPause() {
		super.onPause();
		fixSharedPreferencesPermission();

		finish();
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		fixSharedPreferencesPermission();
	}
}