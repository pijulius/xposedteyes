# XposedTeyes
Framework modifications for Teyes CC3 2k through Xposed

1. Resizable Split Screen.
Will allow you to resize the splitscreen. You will need to activate module for Android Framework and also SystemUI.

2. Autolaunch Split Screen
Will launch applications in split screen mode automatically when head unit turns on (even on fresh or just resume wakeup)

3. Hide Divider.
Show or hide the split screen divider. If hidden a simple 1 pixel black line will be shown instead.

4. Screen Ratio.
Define your prefered ratio for the split screen, for e.g. 0.5 for half, 0.33 for 1third and so on.

5. Left Screen Position.
If checked the smaller split screen (in case for e.g. if you set screen ratio to 0.33) will be on the right side instead of the left.

6. Launcher.
Two separate shortcuts to launch your specified applications (in settings for e.g. "com.spotify.music" and "com.waze" and "video.player.videoplayer") in split screen mode. For e.g. one for Audio / Navigation and one for Navigation / Videos (in case long road and children bored).

7. Fixes.
Some fixes for apps like hide Waze floating car type button, color fixes for Audials and auto launch the now playing screen, possibility to define DPI for Mini Desktop launcher.

# Install
Install instructions

1. Install Magisk on your Teyes Head Unit and set Magisk > Settings > enable Zygisk
2. Install LSPosed Zygisk module
3. Install latest XposedTeyes released apk
4. Activate XposedTeyes module in your LSPosed > Modules section
5, Select "Android Framework" and "SystemUI" to be used for XposedTeyes module

# Notes
Tested on Teyes CC3 2k and vanila Android 10 so think should work on other headunits too.
All changes except setting Launcher applications will require a restart of the device.

