package de.eric_scheibler.tactileclock.ui.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import de.eric_scheibler.tactileclock.utils.TactileClockService;
import de.eric_scheibler.tactileclock.R;

import android.view.ViewGroup.MarginLayoutParams;
import android.view.View;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowCompat;
import timber.log.Timber;
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
import androidx.core.graphics.Insets;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.activity.EdgeToEdge;


public abstract class AbstractActivity extends AppCompatActivity implements FragmentResultListener {
    private static String KEY_FIRST_APP_START_AND_HELP_DIALOG_IS_OPEN = "firstAppStartAndHelpDialogIsOpen";

	public SettingsManager settingsManagerInstance;
    private boolean firstAppStartAndHelpDialogIsOpen;

    public abstract int getLayoutResourceId();

    public int getRootViewResId() {
        return R.id.layoutRoot;
    }

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(getLayoutResourceId());
        settingsManagerInstance = SettingsManager.getInstance();
        getSupportFragmentManager()
            .setFragmentResultListener(
                    HelpDialog.REQUEST_DIALOG_CLOSED_AFTER_FIRST_APP_START, this, this);
        firstAppStartAndHelpDialogIsOpen = savedInstanceState != null
            ? savedInstanceState.getBoolean(KEY_FIRST_APP_START_AND_HELP_DIALOG_IS_OPEN)
            : false;

        // margin for system bars at the top and bottom of the screen
        View rootView = findViewById(getRootViewResId());
        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            Timber.d("insets: %1$s", insets);
            MarginLayoutParams mlp = (MarginLayoutParams) v.getLayoutParams();
            mlp.topMargin = insets.top;
            mlp.bottomMargin = insets.bottom;
            v.setLayoutParams(mlp);
            return WindowInsetsCompat.CONSUMED;
        });
    }

    @Override public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle bundle) {
        if (requestKey.equals(HelpDialog.REQUEST_DIALOG_CLOSED_AFTER_FIRST_APP_START)) {
            // firstAppStartAndHelpDialogIsOpen is temporary and required if i.e. the screen orientation changed while the HelpDialog is open
            firstAppStartAndHelpDialogIsOpen = false;
            // then save app firstStart = false permanently in settings
            settingsManagerInstance.setFirstStart(false);
            // and ask for foreground service notification
            askForNotificationPermission();
        }
    }

    @Override public void onPause() {
        super.onPause();
    }

    @Override public void onResume() {
        super.onResume();
        if (settingsManagerInstance.getFirstStart()) {
            if (! firstAppStartAndHelpDialogIsOpen) {
                HelpDialog.newInstance(true)
                    .show(getSupportFragmentManager(), "HelpDialog");
                firstAppStartAndHelpDialogIsOpen = true;
            }
        } else {
            askForNotificationPermission();
        }
    }

    @Override public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putBoolean(KEY_FIRST_APP_START_AND_HELP_DIALOG_IS_OPEN, firstAppStartAndHelpDialogIsOpen);
    }

    @SuppressLint("InlinedApi")     // TactileClockService.hasPostNotificationsPermission() does the api level check
    private void askForNotificationPermission() {
        if (! settingsManagerInstance.getAskedForNotificationPermission()
                && ! TactileClockService.hasPostNotificationsPermission()) {
            settingsManagerInstance.setAskedForNotificationPermission(true);
            requestNotificationPermissionLauncher
                .launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    }

    private final ActivityResultLauncher<String> requestNotificationPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    isGranted -> {
                        if (isGranted) {
                            // show notification
                            Intent updateNotificationIntent = new Intent(this, TactileClockService.class);
                            updateNotificationIntent.setAction(TactileClockService.ACTION_UPDATE_NOTIFICATION);
                            ContextCompat.startForegroundService(this, updateNotificationIntent);
                        }
                    });

}
