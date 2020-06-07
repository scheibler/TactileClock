package de.eric_scheibler.tactileclock.ui.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import de.eric_scheibler.tactileclock.ui.dialog.HelpDialog;
import de.eric_scheibler.tactileclock.utils.SettingsManager;


public abstract class AbstractActivity extends AppCompatActivity {

	public SettingsManager settingsManagerInstance;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settingsManagerInstance = new SettingsManager();
    }


    @Override public void onPause() {
        super.onPause();
    }

    @Override public void onResume() {
        super.onResume();
        if (settingsManagerInstance.getFirstStart()) {
            HelpDialog.newInstance().show(
                    getSupportFragmentManager(), "HelpDialog");
        }
    }

}
