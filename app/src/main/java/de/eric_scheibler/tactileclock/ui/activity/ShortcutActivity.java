package de.eric_scheibler.tactileclock.ui.activity;

import android.annotation.TargetApi;
import android.content.Intent;


import android.os.Bundle;


import androidx.appcompat.app.AppCompatActivity;






import timber.log.Timber;
import de.eric_scheibler.tactileclock.utils.TactileClockService;
import androidx.core.content.ContextCompat;
import de.eric_scheibler.tactileclock.utils.ApplicationInstance;
import android.os.Build;
import android.content.BroadcastReceiver;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.content.IntentFilter;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;


public class ShortcutActivity extends AppCompatActivity {

    @Override public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    }

    @Override public void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter();
        filter.addAction(TactileClockService.VIBRATION_FINISHED);
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, filter);

        if (getIntent() != null
                && TactileClockService.ACTION_VIBRATE_TIME.equals(getIntent().getAction())) {
            new Handler(Looper.getMainLooper()).postDelayed(
                    new Runnable() {
                        @Override public void run() {
                            sendStartVibrationAction();
                        }
                    }, 750l);
        }
    }

    private void sendStartVibrationAction() {
        Intent vibrateTimeIntent = new Intent(this, TactileClockService.class);
        vibrateTimeIntent.setAction(TactileClockService.ACTION_VIBRATE_TIME);
        ContextCompat.startForegroundService(
                ApplicationInstance.getContext(), vibrateTimeIntent);
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(TactileClockService.VIBRATION_FINISHED)) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    finishAndRemoveTask();
                } else {
                    finish();
                }
            }
        }
    };

}
