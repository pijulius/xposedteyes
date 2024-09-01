package com.pijulius.xposedteyes;

import static de.robv.android.xposed.XposedHelpers.findClass;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.XResources;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.display.DisplayManager;
import android.os.Handler;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;
import de.robv.android.xposed.callbacks.XC_LayoutInflated;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class XposedTeyes implements IXposedHookInitPackageResources, IXposedHookLoadPackage, IXposedHookZygoteInit {
	private int calculateMiddlePosition(boolean horizontal, Rect insets, int displayWidth, int displayHeight, int dividerSize) {
		final XSharedPreferences settings = new XSharedPreferences("com.pijulius.xposedteyes");

		int start = horizontal ? insets.top : insets.left;
		int end = horizontal
			? displayHeight - insets.bottom
			: displayWidth - insets.right;

		float screenRatio = Float.valueOf(settings.getString("screenRatio", "0.33"));
		boolean leftScreenPosition = settings.getBoolean("leftScreenPosition", false);

		return (int)(start + (end - start) * (leftScreenPosition?1-screenRatio:screenRatio) - dividerSize / 2);
	}

	@Override
	public void initZygote(StartupParam startupParam) throws Throwable {
	}

	@Override
	public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
		final XSharedPreferences settings = new XSharedPreferences("com.pijulius.xposedteyes");

		if (lpparam.packageName.equals("com.pijulius.xposedteyes")) {
			final Class<?> hookClassSelf = findClass("com.pijulius.xposedteyes.SettingsActivity",
					lpparam.classLoader);
			XposedBridge.hookAllMethods(hookClassSelf, "getSharedPreferencesPath", new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					param.setResult(settings.getFile().toString());
				}
			});

			return;
		}

		if (lpparam.packageName.equals("com.audials")) {
			final Class<?> hookClassPlaybackFooter = findClass("com.audials.controls.PlaybackFooterInfo",
					lpparam.classLoader);
			XposedBridge.hookAllMethods(hookClassPlaybackFooter, "onPlayingTrackChanged", new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					XposedHelpers.callMethod(param.thisObject, "onExpandItem");
				}
			});

			final Class<?> baseActivity = findClass("com.audials.main.SplashScreenActivity",
					lpparam.classLoader);
			XposedBridge.hookAllMethods(baseActivity, "onPause", new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					final Activity activity = (Activity)param.thisObject;
					new android.os.Handler().postDelayed(
						new Runnable() {
							@Override
							public void run() {
								Intent intent = new Intent();
								intent.setClassName("com.audials", "com.audials.playback.PlaybackActivity");
								activity.startActivity(intent);
							}
						}, 1000);
				}
			});

			return;
		}

		XposedBridge.hookAllMethods(ContextWrapper.class, "attachBaseContext", new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				if (lpparam.packageName.equals("com.atomicadd.tinylauncher") &&
					param.args[0] != null && (param.args[0] instanceof Context))
				{
					Context context = (Context) param.args[0];
					Resources res = context.getResources();
					Configuration config = new Configuration(res.getConfiguration());

					DisplayMetrics runningMetrics = res.getDisplayMetrics();
					DisplayMetrics newMetrics;
					if (runningMetrics != null) {
						newMetrics = new DisplayMetrics();
						newMetrics.setTo(runningMetrics);
					} else {
						newMetrics = res.getDisplayMetrics();
					}

					int dpi = Integer.valueOf(settings.getString("miniDesktopDPI", "500"));
					newMetrics.density = dpi / 160f;
					newMetrics.densityDpi = dpi;
					XposedHelpers.setIntField(config, "densityDpi", dpi);

					context = context.createConfigurationContext(config);
					param.args[0] = context;
				}
			}
		});

		if (lpparam.packageName.equals("com.android.systemui")) {
			if (settings.getBoolean("dontExitSplitScreen", true)) {
				final Class<?> hookClassHome = findClass("com.android.systemui.statusbar.policy.KeyButtonView",
						lpparam.classLoader);
				final Class<?> hookClassDock = findClass("com.android.systemui.stackdivider.WindowManagerProxy",
						lpparam.classLoader);
				XposedBridge.hookAllMethods(hookClassHome, "onTouchEvent", new XC_MethodHook() {
					@Override
					protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
						int action = ((MotionEvent)param.args[0]).getAction();
						int key = (Integer)XposedHelpers.getObjectField(param.thisObject, "mCode");
						if (action != 1 || key != 3)
							return;

						XposedHelpers.setAdditionalInstanceField(hookClassDock, "ignoreUndock", true);
					}

					@Override
					protected void afterHookedMethod(MethodHookParam param) throws Throwable {
						int action = ((MotionEvent)param.args[0]).getAction();
						int key = (Integer)XposedHelpers.getObjectField(param.thisObject, "mCode");
						if (action != 1 || key != 3)
							return;

						XposedHelpers.setAdditionalInstanceField(hookClassDock, "ignoreUndock", false);
					}
				});
				XposedBridge.hookAllMethods(hookClassDock, "getDockSide", new XC_MethodHook() {
					@Override
					protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
						try {
							if ((Boolean)XposedHelpers.getAdditionalInstanceField(hookClassDock, "ignoreUndock")) {
								param.setResult(-1);
							}
						} catch(Exception e) {}
					}
				});
			}

			if (settings.getBoolean("launchOnStartup", true)) {
				final Class<?> hookSystemUIApp = XposedHelpers.findClass("com.android.systemui.SystemUIApplication",
						lpparam.classLoader);
				XposedBridge.hookAllMethods(hookSystemUIApp, "onCreate", new XC_MethodHook() {
					@Override
					protected void afterHookedMethod(MethodHookParam param) throws Throwable {
						final Application application = (Application)param.thisObject;
						final Handler handler = new android.os.Handler();

						final Intent launcherIntent = new Intent();
						launcherIntent.setClassName(
							"com.pijulius.xposedteyes", "com.pijulius.xposedteyes.LauncherActivity");
						launcherIntent.setAction("AccessibilityService");
						launcherIntent.addCategory(Intent.CATEGORY_LAUNCHER);
						launcherIntent.setFlags(
							Intent.FLAG_ACTIVITY_NEW_TASK |
							Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

						final Intent activityIntent = new Intent();
						activityIntent.setClassName(
							"com.pijulius.xposedteyes", "com.pijulius.xposedteyes.MusicNavigationActivity");
						activityIntent.addCategory(Intent.CATEGORY_LAUNCHER);
						activityIntent.setFlags(
							Intent.FLAG_ACTIVITY_NEW_TASK |
							Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

						final Runnable runnable = new Runnable() {
								@Override
								public void run() {
									application.startActivity(activityIntent);
								}
							};

						IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BOOT_COMPLETED);
						intentFilter.addAction("com.fyt.boot.ACCON"); //ACCOFF
						intentFilter.addAction("com.glsx.boot.ACCON"); //ACCOFF
						application.registerReceiver(new BroadcastReceiver() {
							@Override
							public void onReceive(Context context, Intent intent) {
								application.startActivity(launcherIntent);

								handler.removeCallbacks(runnable);
								handler.postDelayed(runnable, 3000);
							}
						}, intentFilter);
					}
				});
			}
		}

		if (!settings.getBoolean("resizableSplitScreen", true))
			return;

		if (lpparam.packageName.equals("com.android.systemui")) {
			final Class<?> hookClassRecents = findClass("com.android.systemui.recents.Recents",
					lpparam.classLoader);
			XposedBridge.hookAllMethods(hookClassRecents, "splitPrimaryTask", new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					Point realSize = new Point();
					Context context = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
					context.getSystemService(DisplayManager.class).getDisplay(Display.DEFAULT_DISPLAY)
						.getRealSize(realSize);

					float screenRatio = Float.valueOf(settings.getString("screenRatio", "0.33"));
					boolean leftScreenPosition = settings.getBoolean("leftScreenPosition", false);

					if (realSize.x < realSize.y)
						realSize.y = (int)(realSize.y * (leftScreenPosition?1-screenRatio:screenRatio));
					else
						realSize.x = (int)(realSize.x * (leftScreenPosition?1-screenRatio:screenRatio));

					param.args[1] = new Rect(0, 0, realSize.x, realSize.y);
				}
			});
		}

		final Class<?> hookClassDivider = findClass("com.android.internal.policy.DockedDividerUtils",
				lpparam.classLoader);
		XposedBridge.hookAllMethods(hookClassDivider, "calculateMiddlePosition", new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				boolean horizontal = (Boolean)param.args[0];
				Rect insets = (Rect)param.args[1];
				int displayWidth = (Integer)param.args[2];
				int displayHeight = (Integer)param.args[3];
				int dividerSize = (Integer)param.args[4];

				int position = calculateMiddlePosition(horizontal, insets, displayWidth, displayHeight, dividerSize);
				param.setResult(position);
			}
		});

		final Class<?> hookClassAlgo = findClass("com.android.internal.policy.DividerSnapAlgorithm",
				lpparam.classLoader);
		final Class<?> hookClassTarget = findClass("com.android.internal.policy.DividerSnapAlgorithm.SnapTarget",
				lpparam.classLoader);
		XposedBridge.hookAllMethods(hookClassAlgo, "addMiddleTarget", new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				boolean horizontal = (Boolean)param.args[0];

				@SuppressWarnings("unchecked")
				ArrayList<Object> targets = (ArrayList<Object>)
					XposedHelpers.getObjectField(param.thisObject, "mTargets");

				Rect insets = (Rect)XposedHelpers.getObjectField(param.thisObject, "mInsets");
				int dividerSize = (Integer)XposedHelpers.getObjectField(param.thisObject, "mDividerSize");
				int displayHeight = (Integer)XposedHelpers.getObjectField(param.thisObject, "mDisplayHeight");
				int displayWidth = (Integer)XposedHelpers.getObjectField(param.thisObject, "mDisplayWidth");

				int position = calculateMiddlePosition(horizontal, insets, displayWidth, displayHeight, dividerSize);
				targets.add(XposedHelpers.newInstance(hookClassTarget, position, position, 0));

				param.setResult(null);
			}
		});
		XposedBridge.hookAllMethods(hookClassAlgo, "calculateTargets", new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				@SuppressWarnings("unchecked")
				ArrayList<Object> targets = (ArrayList<Object>)
					XposedHelpers.getObjectField(param.thisObject, "mTargets");
				targets.clear();

				boolean horizontal = (Boolean)param.args[0];
				int dockedSide = (Integer)param.args[1];

				Rect insets = (Rect)XposedHelpers.getObjectField(param.thisObject, "mInsets");
				int dividerSize = (Integer)XposedHelpers.getObjectField(param.thisObject, "mDividerSize");
				int snapMode = (Integer)XposedHelpers.getObjectField(param.thisObject, "mSnapMode");
				int displayHeight = (Integer)XposedHelpers.getObjectField(param.thisObject, "mDisplayHeight");
				int displayWidth = (Integer)XposedHelpers.getObjectField(param.thisObject, "mDisplayWidth");

				int dividerMax = horizontal
					? displayHeight
					: displayWidth;
				int navBarSize = horizontal ? insets.bottom : insets.right;
				int startPos = -dividerSize;
				if (dockedSide == 3) {
					startPos += insets.left;
				}

				targets.add(XposedHelpers.newInstance(hookClassTarget, startPos, startPos, 1,
					0.35f));
				switch (snapMode) {
					case 0:
					case 1:
					case 2:
						boolean leftScreenPosition = settings.getBoolean("leftScreenPosition", false);

						int position = calculateMiddlePosition(horizontal, insets, displayWidth, displayHeight, dividerSize);
						targets.add(XposedHelpers.newInstance(hookClassTarget, position, position, 0));

						if (!leftScreenPosition) {
							targets.add(XposedHelpers.newInstance(hookClassTarget, position, position, 0));
						} else {
							targets.add(XposedHelpers.newInstance(hookClassTarget, (horizontal?displayHeight:displayWidth)-position, (horizontal?displayHeight:displayWidth)-position, 0));
						}

						targets.add(XposedHelpers.newInstance(hookClassTarget, (horizontal?displayHeight:displayWidth)-position, (horizontal?displayHeight:displayWidth)-position, 0));
						break;
					case 3:
						XposedHelpers.callMethod(param.thisObject, "addMinimizedTarget", horizontal, dockedSide);
						break;
				}
				targets.add(XposedHelpers.newInstance(hookClassTarget, dividerMax - navBarSize, dividerMax, 2, 0.35f));

				param.setResult(null);
				//XposedBridge.log("snap calc end: "+Arrays.toString(param.args));
			}
		});
	}

	@Override
	public void handleInitPackageResources(InitPackageResourcesParam resparam) throws Throwable {
		final XSharedPreferences settings = new XSharedPreferences("com.pijulius.xposedteyes");

		if (resparam.packageName.equals("com.audials")) {
			resparam.res.setReplacement("com.audials", "color", "bottom_navbar_background_color_dark", 0x90000000);
			resparam.res.setReplacement("com.audials", "color", "playback_footer_background_gradient0_color_dark", 0x90000000);
			resparam.res.setReplacement("com.audials", "color", "playback_footer_background_gradient1_color_dark", 0x10000000);
			resparam.res.setReplacement("com.audials", "color", "streamview_header_background_gradient0_color_dark", 0x90000000);
			resparam.res.setReplacement("com.audials", "color", "streamview_header_background_gradient1_color_dark", 0x10000000);
			resparam.res.setReplacement("com.audials", "color", "top_appbar_background_color_dark", 0x90000000);

			return;
		}

		if (resparam.packageName.equals("com.waze")) {
			resparam.res.hookLayout("com.waze", "layout", "floating_buttons_view", new XC_LayoutInflated() {
				@Override
				public void handleLayoutInflated(XC_LayoutInflated.LayoutInflatedParam liparam) throws Throwable {
					View view = (View)liparam.view.findViewById(liparam.res.getIdentifier(
						"vehicleTypeView", "id", "com.waze"));
					view.setAlpha(0);
				}
			});

			return;
		}

		if (resparam.packageName.equals("com.atomicadd.tinylauncher")) {
			resparam.res.setReplacement("com.atomicadd.tinylauncher", "color", "actionbar_bg", Color.TRANSPARENT);

			resparam.res.hookLayout("com.atomicadd.tinylauncher", "layout", "app_item_label", new XC_LayoutInflated() {
				@Override
				public void handleLayoutInflated(XC_LayoutInflated.LayoutInflatedParam liparam) throws Throwable {
					TextView view = (TextView)liparam.view.findViewById(liparam.res.getIdentifier(
						"label", "id", "com.atomicadd.tinylauncher"));
					view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 7);
				}
			});

			resparam.res.hookLayout("com.atomicadd.tinylauncher", "layout", "activity_main_top_bar", new XC_LayoutInflated() {
				@Override
				public void handleLayoutInflated(XC_LayoutInflated.LayoutInflatedParam liparam) throws Throwable {
					TextView view = (TextView)liparam.view.findViewById(liparam.res.getIdentifier(
						"sort", "id", "com.atomicadd.tinylauncher"));
					view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 9);
				}
			});

			return;
		}

		int dividerWidth = Integer.valueOf(settings.getString("dividerWidth", "22"));
		int dividerInsetPosition = Integer.valueOf(settings.getString("dividerInsetPosition", "10"));

		if (settings.getBoolean("hideDivider", false)) {
			dividerWidth = 1;
			dividerInsetPosition = 0;
		}

		XResources.setSystemWideReplacement("android", "integer", "config_dockedStackDividerSnapMode", 1);

		if (dividerWidth > 0) {
			XResources.setSystemWideReplacement("android", "dimen", "docked_stack_divider_thickness", new XResources.DimensionReplacement(dividerWidth, TypedValue.COMPLEX_UNIT_DIP));
			XResources.setSystemWideReplacement("android", "dimen", "docked_stack_divider_insets", new XResources.DimensionReplacement(dividerInsetPosition, TypedValue.COMPLEX_UNIT_DIP));
		}
	}
}
