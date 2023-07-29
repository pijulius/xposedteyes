package com.pijulius.xposedteyes;

import static de.robv.android.xposed.XposedHelpers.findClass;

import java.util.ArrayList;

import android.content.res.XResources;
import android.graphics.Rect;
import android.util.TypedValue;
import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class XposedTeyes implements IXposedHookInitPackageResources, IXposedHookLoadPackage, IXposedHookZygoteInit {
	@Override
	public void initZygote(StartupParam startupParam) throws Throwable {
	}

	@Override
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
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
		}

		if (!settings.getBoolean("resizableSplitScreen", true))
			return;

		final float screenRatio = Float.valueOf(settings.getString("screenRatio", "0.35"));
		final boolean leftScreenPosition = settings.getBoolean("leftScreenPosition", false);

		final Class<?> hookClassAlgo = findClass("com.android.internal.policy.DividerSnapAlgorithm",
				lpparam.classLoader);

		final Class<?> hookClassTarget = findClass("com.android.internal.policy.DividerSnapAlgorithm.SnapTarget",
				lpparam.classLoader);

		XposedBridge.hookAllMethods(hookClassAlgo, "calculateTargets", new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				Rect insets = (Rect)XposedHelpers.getObjectField(param.thisObject, "mInsets");
				int displayHeight = (Integer)XposedHelpers.getObjectField(param.thisObject, "mDisplayHeight");
				int displayWidth = (Integer)XposedHelpers.getObjectField(param.thisObject, "mDisplayWidth");
				int dividerSize = (Integer)XposedHelpers.getObjectField(param.thisObject, "mDividerSize");

				int dividerMax = (Boolean)param.args[0]
                	? displayHeight
	                : displayWidth;

				@SuppressWarnings("unchecked")
				ArrayList<Object> targets = (ArrayList<Object>)
					XposedHelpers.getObjectField(param.thisObject, "mTargets");

				targets.clear();
				targets.add(XposedHelpers.newInstance(hookClassTarget, 0, 0, 1, 1f));

				int start = (Boolean)param.args[0] ? insets.top : insets.left;
				int end = (Boolean)param.args[0]
					? displayHeight - insets.bottom
					: displayWidth - insets.right;
				int size = (int) (screenRatio * (end - start)) - dividerSize / 2;
				int topPosition = start + size;
				int bottomPosition = end - size - dividerSize;

				targets.add(XposedHelpers.newInstance(hookClassTarget, topPosition, topPosition, 0));

				if (leftScreenPosition) {
					targets.add(XposedHelpers.newInstance(hookClassTarget, bottomPosition, bottomPosition, 0));
				} else {
					targets.add(XposedHelpers.newInstance(hookClassTarget, topPosition, topPosition, 0));
				}

				targets.add(XposedHelpers.newInstance(hookClassTarget, bottomPosition, bottomPosition, 0));

				targets.add(XposedHelpers.newInstance(hookClassTarget, dividerMax, dividerMax, 2, 1f));

				param.setResult(null);

				//XposedBridge.log("snap calc end: "+Arrays.toString(param.args));
			}
		});
	}

	@Override
	public void handleInitPackageResources(InitPackageResourcesParam resparam) throws Throwable {
		final XSharedPreferences settings = new XSharedPreferences("com.pijulius.xposedteyes");

		if (!settings.getBoolean("hideDivider", true))
			return;

		XResources.setSystemWideReplacement("android", "dimen", "docked_stack_divider_thickness", new XResources.DimensionReplacement(1, TypedValue.COMPLEX_UNIT_DIP));
		XResources.setSystemWideReplacement("android", "dimen", "docked_stack_divider_insets", new XResources.DimensionReplacement(0, TypedValue.COMPLEX_UNIT_DIP));
	}
}
