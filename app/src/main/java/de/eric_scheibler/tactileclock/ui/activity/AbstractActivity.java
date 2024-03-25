package de.eric_scheibler.tactileclock.ui.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import de.eric_scheibler.tactileclock.ui.dialog.HelpDialog;
import de.eric_scheibler.tactileclock.utils.SettingsManager;
import androidx.activity.result.ActivityResultLauncher;
import android.os.Build;
import android.Manifest;
import de.eric_scheibler.tactileclock.utils.ApplicationInstance;
import android.content.pm.PackageManager;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentResultListener;
import androidx.annotation.NonNull;


public abstract class AbstractActivity extends AppCompatActivity implements FragmentResultListener {

	public SettingsManager settingsManagerInstance;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settingsManagerInstance = new SettingsManager();
        getSupportFragmentManager()
            .setFragmentResultListener(
                    HelpDialog.REQUEST_DIALOG_CLOSED, this, this);
    }


    @Override public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle bundle) {
        if (requestKey.equals(HelpDialog.REQUEST_DIALOG_CLOSED)) {
            if (settingsManagerInstance.getFirstStart()) {
                settingsManagerInstance.setFirstStart(false);
                askForNotificationPermission();
            }
        }
    }

    @Override public void onPause() {
        super.onPause();
    }

    @Override public void onResume() {
        super.onResume();
        if (settingsManagerInstance.getFirstStart()) {
            HelpDialog.newInstance()
                .show(getSupportFragmentManager(), "HelpDialog");
        } else {
            askForNotificationPermission();
        }
    }

    private void askForNotificationPermission() {
        if (! settingsManagerInstance.getAskedForNotificationPermission()
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(ApplicationInstance.getContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            settingsManagerInstance.setAskedForNotificationPermission(true);
            requestNotificationPermissionLauncher
                .launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    }

    private final ActivityResultLauncher<String> requestNotificationPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    isGranted -> {
                    });

}
