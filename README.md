# XposedTeyes
Framework modifications for Teyes CC3 2k through Xposed

1. Resizable Split Screen.
Will allow you to resize the splitscreen. You will need to activate module for Android Framework and also SystemUI.

2. Hide Divider.
Show or hide the split screen divider. If hidden a simple 1 pixel black line will be shown instead.

3. Screen Ratio.
Define your prefered ratio for the split screen, for e.g. 0.5 for half, 0.33 for 1third and so on.

4. Left Screen Position.
If checked the smaller split screen (in case for e.g. if you set screen ratio to 0.33) will be on the right side instead of the left.

5. Launcher.
Define two applications and then you can set Xposed Teyes as your startup application to launch those two applications in a split screen as soon as your car starts. You will need to install [Split Screen Launcher](https://github.com/paprikanotfound/split-screen-launcher) from \"paprikanotfound\" for this to work.

# Install
Install instructions

1. Install Magisk on your Teyes Head Unit and set Magisk > Settings > enable Zygisk
2. Install LSPosed Zygisk module
3. Install latest XposedTeyes released apk
4. Activate XposedTeyes module in your LSPosed > Modules section
5, Select "Android Framework" and "SystemUI" to be used for XposedTeyes module

# Notes
Tested on Teyes CC3 2k and vanila Android 10 so think should work on other headunits too.

