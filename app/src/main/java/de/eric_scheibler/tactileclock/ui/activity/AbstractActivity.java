package de.eric_scheibler.tactileclock.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import de.eric_scheibler.tactileclock.ui.dialog.HelpDialog;
import de.eric_scheibler.tactileclock.utils.SettingsManager;
import android.support.v4.content.LocalBroadcastManager;
import android.content.Context;
import de.eric_scheibler.tactileclock.utils.Constants;


public abstract class AbstractActivity extends AppCompatActivity {

	public SettingsManager settingsManagerInstance;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		settingsManagerInstance = SettingsManager.getInstance(this);
    }


    @Override public void onPause() {
        super.onPause();
        // unregister broadcast receiver
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    }

    @Override public void onResume() {
        super.onResume();
        if (settingsManagerInstance.getFirstStart()) {
            HelpDialog.newInstance().show(
                    getSupportFragmentManager(), "HelpDialog");
        }
        // register broadcast receiver to listen to messages from dtPlayer
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter(Constants.CustomAction.RELOAD_UI));
    }


    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            if (Constants.CustomAction.RELOAD_UI.equals(intent.getAction())) {
                AbstractActivity.this.recreate();
            }
        }
    };

}
