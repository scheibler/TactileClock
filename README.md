Tactile Clock
=============

This Android app vibrates the current time when the display is locked and you double-click the power button of your device.

The app is available in the [Play Store](https://play.google.com/store/apps/details?id=de.eric_scheibler.tactileclock&hl=en)
and at [IzzyOnDroid](https://apt.izzysoft.de/packages/de.eric_scheibler.tactileclock).


## How it works

Double-click the power button.  This might take some practice to work reliably. Press slowly and deliberately. If you
double-click too fast the system only recognizes a single click and turns the screen on instead.

The time vibration is structured as follows:

A short vibration stands for the digit 1 and a long one for the digit 5. The 2
is represented by two consecutive short vibrations, the 6 by a long and a short one and so
on. The 0 constitutes an exception with two long vibrations.


### Examples:

```
01:18 =  .    .  -...
02:51 =  ..    -  .
10:11 =  .  --    .  .
19:06 =  .  -....    -.
```


### Explanation:

The time is processed digit by digit:

* . = short vibration
* \- = long vibration

Leading zeros in the hour and minute fields are omitted.


### Gaps

To simplify the recognition of the vibration pattern, there exist three kind of gaps with different durations:

* \[\]: A short gap between vibrations in the same digit
* \[&nbsp;&nbsp;\]: A medium gap for the separation of two digits within the hour and minute field
* \[&nbsp;&nbsp;&nbsp;&nbsp;\]: A long gap to split hours and minutes


## Additional features

* Keep informed about the current time. The app can vibrate the current time automatically every X minutes or every hour.
* Play the [Greenwich Time Signal](https://en.wikipedia.org/wiki/Greenwich_Time_Signal) at the start of each hour, similar to a radio station.
* [Tasker](https://play.google.com/store/apps/details?id=net.dinglisch.android.taskerm) integration: If your smartphone
  has an additional hardware button that is supported by Tasker, you can configure it to vibrate the time. You can find
  the corresponding action in Tasker under the "Plugin" category.
* Decide what to do if you double-click when the display is on. You can choose between a warning vibration and having
  the time vibrated as well.
* The app starts automatically after the system reboots.
* It runs on all devices with Android version >= 5.1 (API 21).

