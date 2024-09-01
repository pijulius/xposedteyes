package com.pijulius.xposedteyes;

import android.content.Intent;
import android.os.Bundle;

public class MusicNavigationActivity extends LauncherActivity {
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);

		if (!musicApp.isEmpty() && !navigationApp.isEmpty()) {
			Intent intent = new Intent(this, LauncherService.class);
			intent.setAction(getPackageName()+"."+this.getClass().getSimpleName());
			startService(intent);

			finish();
			return;
		}
	}
}