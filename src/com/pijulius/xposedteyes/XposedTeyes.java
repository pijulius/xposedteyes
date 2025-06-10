package com.pijulius.xposedteyes;

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
import android.content.res.XModuleResources;
import android.content.res.XResources;
import android.graphics.Color;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.display.DisplayManager;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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
	private static String modulePath = null;
	private static Object carlinkLogoFragment = null;
	private static final float[] negativeColorMatrix = {
		-1.0f,     0,     0,    0, 255, // red
			0, -1.0f,     0,    0, 255, // green
			0,     0, -1.0f,    0, 255, // blue
			0,     0,     0, 1.0f,   0  // alpha
		};

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
		modulePath = startupParam.modulePath;
	}

	@Override
	public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
		final XSharedPreferences settings = new XSharedPreferences("com.pijulius.xposedteyes");

		if (lpparam.packageName.equals("com.pijulius.xposedteyes")) {
			final Class<?> hookClassSelf = XposedHelpers.findClass(
					"com.pijulius.xposedteyes.SettingsActivity", lpparam.classLoader);

			XposedBridge.hookAllMethods(hookClassSelf, "getSharedPreferencesPath", new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					param.setResult(settings.getFile().toString());
				}
			});

			return;
		}

		if (lpparam.packageName.equals("com.audials")) {
			final Class<?> hookClassPlaybackFooter = XposedHelpers.findClass(
					"com.audials.controls.PlaybackFooterInfo", lpparam.classLoader);

			XposedBridge.hookAllMethods(hookClassPlaybackFooter, "onPlayingTrackChanged", new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					XposedHelpers.callMethod(param.thisObject, "onExpandItem");
				}
			});

			final Class<?> baseActivity = XposedHelpers.findClass(
					"com.audials.main.SplashScreenActivity", lpparam.classLoader);

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

		if (lpparam.packageName.equals("com.spotify.music") || lpparam.packageName.equals("com.spotify.lite")) {
			final Class<?> hookClassSelf = XposedHelpers.findClass(
					"com.spotify.connectivity.productstate.ProductStateUtil", lpparam.classLoader);

			XposedBridge.hookAllMethods(hookClassSelf, "isPremium", new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					param.setResult(true);
				}
			});

			return;
		}

		if (lpparam.packageName.equals("video.player.videoplayer")) {
			final Class<?> hookClassSelf = XposedHelpers.findClass(
					"android.app.SharedPreferencesImpl", lpparam.classLoader);

			XposedBridge.hookAllMethods(hookClassSelf, "getBoolean", new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					if (param.args[0] == "adRemoved" || param.args[0] == "hasRated" ||
						param.args[0] == "SB08XyFV" || param.args[0] == "aeAfFfti")
					{
						param.setResult(true);
					}
				}
			});

			return;
		}

		if (lpparam.packageName.equals("tunein.player")) {
			final Class<?> hookClassTunein = XposedHelpers.findClass(
					"tunein.settings.SubscriptionSettings", lpparam.classLoader);

			XposedBridge.hookAllMethods(hookClassTunein, "isSubscribed", new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					param.setResult(true);
				}
			});

			final Class<?> hookClassNowPlaying = XposedHelpers.findClass(
					"tunein.nowplayinglite.NowPlayingDelegate", lpparam.classLoader);

			XposedBridge.hookAllMethods(hookClassNowPlaying, "updateToolbarColor", new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					float[] hsv = new float[3];
					Color.colorToHSV((Integer)param.args[0], hsv);
					hsv[2] = 0.3f;
					param.args[0] = Color.HSVToColor(hsv);
				}
			});

			return;
		}

		if (lpparam.packageName.equals("com.syu.carlink")) {
			final Class<?> hookClassSelf = XposedHelpers.findClass(
					"com.syu.carlink.auto.AutoFragment", lpparam.classLoader);

			//param.args[0] = 2000; //width
			//param.args[1] = 1200; //height
			//param.args[2] = 210; //dpi
			//param.args[3] = 170; //real dpi
			//param.args[4] = 30; //fps
			//param.args[5] = 0; //additional depth
			XposedBridge.hookAllMethods(hookClassSelf, "c", new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					String resolution = settings.getString("carlinkResolution", "");
					Integer resolutionWidthMargin = Integer.valueOf(settings.getString("carlinkResolutionWidthMargin", "0"));
					Integer resolutionHeightMargin = Integer.valueOf(settings.getString("carlinkResolutionHeightMargin", "0"));
					Integer dpi = Integer.valueOf(settings.getString("carlinkDPI", "0"));

					if (resolution.isEmpty() && dpi <= 0)
						return;

					Class<?> configurationClass = XposedHelpers.findClass(
							"com.google.android.projection.proto.Protos$VideoConfiguration", lpparam.classLoader);

					Object configuration = XposedHelpers.callStaticMethod(configurationClass, "newBuilder", param.getResult());

					if (!resolution.isEmpty()) {
						Class<?> codecResolutionType = XposedHelpers.findClass(
								"com.google.android.projection.proto.Protos$VideoCodecResolutionType", lpparam.classLoader);

						XposedHelpers.callMethod(configuration, "setCodecResolution",
							XposedHelpers.getStaticObjectField(codecResolutionType, "VIDEO_"+resolution));
						XposedHelpers.callMethod(configuration, "setWidthMargin", resolutionWidthMargin); //80
						XposedHelpers.callMethod(configuration, "setHeightMargin", resolutionHeightMargin); //-30
					}

					if (dpi > 0)
						XposedHelpers.callMethod(configuration, "setDensity", dpi);

					param.setResult(XposedHelpers.callMethod(configuration, "build"));
				}
			});

			final Class<?> hookClassDrivingStatus = XposedHelpers.findClass(
					"com.google.android.projection.sink.sensors.LocationSensors", lpparam.classLoader);

			//DRIVE_STATUS_UNRESTRICTED
			XposedBridge.hookAllMethods(hookClassDrivingStatus, "reportKeyboardLockout", new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					XposedHelpers.callMethod(param.args[1], "reportDrivingStatusData", 0);
					param.setResult(null);
				}
			});

			final Class<?> hookClassGalReceiverCarInfo = XposedHelpers.findClass(
					"com.google.android.projection.protocol.GalReceiver$CarInfo", lpparam.classLoader);

			// Activating Developer Mode
			XposedBridge.hookAllConstructors(hookClassGalReceiverCarInfo, new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					param.args[1] = "Desktop Head Unit";
					param.args[7] = "Google";
					param.args[8] = "Desktop Head Unit";
				}
			});

			final Class<?> hookClassLogoFragment = XposedHelpers.findClass(
					"com.syu.carlink.fragment.LogoFragment", lpparam.classLoader);

			XposedBridge.hookAllConstructors(hookClassLogoFragment, new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					carlinkLogoFragment = param.thisObject;
				}
			});

			return;
		}

		if (lpparam.packageName.equals("com.syu.us")) {
			final Class<?> hookClassPageBack = XposedHelpers.findClass(
					"com.syu.page.PageBack", lpparam.classLoader);

			int cameraWidth = Integer.valueOf(settings.getString("cameraWidth", "1600"));
			int cameraHeight = Integer.valueOf(settings.getString("cameraHeight", "1000"));
			int cameraX = Integer.valueOf(settings.getString("cameraX", "400"));
			int cameraY = Integer.valueOf(settings.getString("cameraY", "100"));

			boolean cameraRound = settings.getBoolean("cameraRound", true);
			boolean cameraEnhance = settings.getBoolean("cameraEnhance", true);
			boolean cameraWarning = settings.getBoolean("cameraWarning", true);

			if (cameraWidth != 0 || cameraHeight != 0 || cameraX != 0 || cameraY != 0) {
				if (cameraX != 0) {
					XposedBridge.hookAllMethods(hookClassPageBack, "setTrackPosX", new XC_MethodHook() {
						@Override
						protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
							param.setResult(null);
						}
					});
				}

				if (cameraY != 0) {
					XposedBridge.hookAllMethods(hookClassPageBack, "setTrackPosY", new XC_MethodHook() {
						@Override
						protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
							param.setResult(null);
						}
					});
				}

				XposedBridge.hookAllMethods(hookClassPageBack, "show", new XC_MethodHook() {
					@Override
					protected void afterHookedMethod(MethodHookParam param) throws Throwable {
						XposedBridge.log("show");
						View texture = (View)XposedHelpers.getObjectField(param.thisObject, "mTexture");

						if (texture == null)
							return;

						RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)texture.getLayoutParams();

						if (cameraWidth != 0)
							layoutParams.width = cameraWidth;
						if (cameraHeight != 0)
							layoutParams.height = cameraHeight-(cameraRound?50:0);

						if (cameraX != 0)
							texture.setX(cameraX);
						if (cameraY != 0 || cameraRound)
							texture.setY(cameraY+(cameraRound?25:0));

						texture.setLayoutParams(layoutParams);

						if (cameraRound) {
							View backTrackLine = (View)XposedHelpers.getObjectField(param.thisObject, "mBackTrackLine");
							RelativeLayout.LayoutParams backTrackLineParams = (RelativeLayout.LayoutParams)backTrackLine.getLayoutParams();

							if (cameraWidth != 0)
								backTrackLineParams.width = cameraWidth;
							if (cameraHeight != 0)
								backTrackLineParams.height = cameraHeight;

							if (cameraX != 0)
								backTrackLine.setX(cameraX);
							if (cameraY != 0)
								backTrackLine.setY(cameraY);

							backTrackLine.setLayoutParams(backTrackLineParams);
							backTrackLine.setVisibility(View.VISIBLE);
						}
					}
				});
			}

			if (cameraEnhance) {
				XposedBridge.hookAllMethods(hookClassPageBack, "initBaseView", new XC_MethodHook() {
					@Override
					protected void afterHookedMethod(MethodHookParam param) throws Throwable {
						XposedBridge.log("initBaseView");
						View texture = (View)XposedHelpers.getObjectField(param.thisObject, "mTexture");

						if (texture == null)
							return;

						Paint paint = new Paint();
						paint.setAntiAlias(true);
						paint.setFilterBitmap(true);
						paint.setDither(true);

						texture.setLayerPaint(paint);
					}
				});
			}

			if (cameraWarning) {
				final Class<?> hookClassPlatform = XposedHelpers.findClass(
						"com.syu.util.PlatForm", lpparam.classLoader);

				XposedBridge.hookAllMethods(hookClassPlatform, "isExistImagePrompt", new XC_MethodHook() {
					@Override
					protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
						param.setResult(true);
					}
				});
			}

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

					int dpi = Integer.valueOf(settings.getString("miniDesktopDPI", "450"));
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
				final Class<?> hookClassHome = XposedHelpers.findClass(
						"com.android.systemui.statusbar.policy.KeyButtonView", lpparam.classLoader);
				final Class<?> hookClassDock = XposedHelpers.findClass(
						"com.android.systemui.stackdivider.WindowManagerProxy", lpparam.classLoader);

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

			if (settings.getBoolean("launchOnStartup", false)) {
				final Class<?> hookSystemUIApp = XposedHelpers.findClass(
						"com.android.systemui.SystemUIApplication", lpparam.classLoader);

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
			final Class<?> hookClassRecents = XposedHelpers.findClass(
					"com.android.systemui.recents.Recents", lpparam.classLoader);

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

		final Class<?> hookClassDivider = XposedHelpers.findClass(
				"com.android.internal.policy.DockedDividerUtils", lpparam.classLoader);

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

		final Class<?> hookClassAlgo = XposedHelpers.findClass(
				"com.android.internal.policy.DividerSnapAlgorithm", lpparam.classLoader);
		final Class<?> hookClassTarget = XposedHelpers.findClass(
				"com.android.internal.policy.DividerSnapAlgorithm.SnapTarget", lpparam.classLoader);

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

		if (resparam.packageName.equals("com.syu.carlink")) {
			resparam.res.setReplacement("com.syu.carlink", "drawable", "teyes", new XResources.DrawableLoader() {
				@Override
				public Drawable newDrawable(XResources res, int id) throws Throwable {
					return new ColorDrawable(Color.TRANSPARENT);
				}
			});

			resparam.res.setReplacement("com.syu.carlink", "drawable", "connection_msg_bg_tzy", new XResources.DrawableLoader() {
				@Override
				public Drawable newDrawable(XResources res, int id) throws Throwable {
					return new ColorDrawable(Color.TRANSPARENT);
				}
			});

			resparam.res.hookLayout("com.syu.carlink", "layout", "logo_fragment_tzy2", new XC_LayoutInflated() {
				@Override
				public void handleLayoutInflated(XC_LayoutInflated.LayoutInflatedParam liparam) throws Throwable {
					View view = (View)liparam.view.findViewById(liparam.res.getIdentifier(
						"connecting_iv", "id", "com.syu.carlink"));
					LayoutParams params = view.getLayoutParams();
					params.height = params.height * 3;
					view.setLayoutParams(params);
					view.setAlpha(0);

					ImageView imageView = (ImageView)liparam.view.findViewById(liparam.res.getIdentifier(
						"setting_iv", "id", "com.syu.carlink"));
					imageView.setColorFilter(new ColorMatrixColorFilter(negativeColorMatrix));

					imageView = (ImageView)liparam.view.findViewById(liparam.res.getIdentifier(
						"help_iv", "id", "com.syu.carlink"));
					imageView.setColorFilter(new ColorMatrixColorFilter(negativeColorMatrix));

					imageView = (ImageView)liparam.view.findViewById(liparam.res.getIdentifier(
						"home_iv", "id", "com.syu.carlink"));
					imageView.setColorFilter(new ColorMatrixColorFilter(negativeColorMatrix));

					imageView.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							if (carlinkLogoFragment == null)
								return;

							Activity activity = (Activity)XposedHelpers.callMethod(carlinkLogoFragment, "getActivity");
							if (activity != null)
								activity.finish();
						}
					});
				}
			});

			XModuleResources modRes = XModuleResources.createInstance(modulePath, resparam.res);
			resparam.res.setReplacement("com.syu.carlink", "drawable", "bg_tzy", modRes.fwd(R.drawable.bg_tzy));
			resparam.res.setReplacement("com.syu.carlink", "drawable", "icon_carlink", modRes.fwd(R.drawable.icon_carlink));
			resparam.res.setReplacement("com.syu.carlink", "string", "app_name", "Android Auto");

			return;
		}

		if (resparam.packageName.equals("com.syu.us")) {
			XModuleResources modRes = XModuleResources.createInstance(modulePath, resparam.res);
			resparam.res.setReplacement("com.syu.us", "drawable", "car_modle", modRes.fwd(R.drawable.car_modle));
			resparam.res.setReplacement("com.syu.us", "drawable", "car_modle_1920", modRes.fwd(R.drawable.car_modle_1920));

			resparam.res.setReplacement("com.syu.us", "drawable", "car_modle_h", modRes.fwd(R.drawable.car_modle_h));
			resparam.res.setReplacement("com.syu.us", "drawable", "car_modle_h_1280", modRes.fwd(R.drawable.car_modle_h_1280));

			if (settings.getBoolean("cameraRound", true))
				resparam.res.setReplacement("com.syu.us", "drawable", "img_track", modRes.fwd(R.drawable.rounded_corners));

			if (settings.getBoolean("cameraWarning", true))
				resparam.res.setReplacement("com.syu.us", "drawable", "bg_warning_en", modRes.fwd(R.drawable.bg_warning_en));

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
