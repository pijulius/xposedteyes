# XposedTeyes
Framework modifications for Teyes CC3 2k through Xposed

## System Changes

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

## Reverse Camera

1. Allow custom preview Width and Height
2. Possibility to set X and Y positions
3. Show warning message
4. Enhance preview so it doesn't look so pixelated on analog (original) car camera sources
5. Rounded corners around the preview so it matches android auto look

## CarLink

1. Custom video resolution support for Android Auto as default is very low and looks very pixelated.
2. Width and Height margin for the stream so you can move the streamed screen around (supports even negative numbers like -10 so that the small black borders are removed from the stream and the whole display is used)
3. Custom DPI so you can change the look of the Android Auto layout
4. Developer Mode (which is automatically turned on by this app) so Android Auto will support all unofficial apps.

# Install
Install instructions

1. Install Magisk on your Teyes Head Unit and set Magisk > Settings > enable Zygisk
2. Install LSPosed Zygisk module
3. Install latest XposedTeyes released apk
4. Activate XposedTeyes module in your LSPosed > Modules section
5, Select "Android Framework", "SystemUI", "CarLink" and "UI Server" to be used for XposedTeyes module

# Notes
Tested on Teyes CC3 2k and vanila Android 10 so think should work on other headunits too.
All changes except setting Launcher applications will require a restart of the device.

