package com.pijulius.xposedteyes;

import java.io.File;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.View;
import android.widget.Button;
import android.net.Uri;

@SuppressWarnings("deprecation")
public class SettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	private String sharedPreferencesPath;

	public String getSharedPreferencesPath() {
		return null;
	}

	public void fixSharedPreferencesPermission() {
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

		Button marketButton = findViewById(R.id.marketButton);

		marketButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.fb.splitscreenlauncher"));
		        startActivity(i);
            }
        });
	}

	@Override
	protected void onPause() {
		super.onPause();
		fixSharedPreferencesPermission();
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		fixSharedPreferencesPermission();
	}
}