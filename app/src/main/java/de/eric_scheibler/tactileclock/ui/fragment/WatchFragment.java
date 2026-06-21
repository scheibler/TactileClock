package de.eric_scheibler.tactileclock.ui.fragment;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.media.AudioManager;
import androidx.annotation.RequiresApi;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import de.eric_scheibler.tactileclock.R;
import de.eric_scheibler.tactileclock.ui.dialog.SelectIntegerDialog.IntegerSelector;
import de.eric_scheibler.tactileclock.ui.dialog.SelectIntegerDialog.Token;
import de.eric_scheibler.tactileclock.ui.dialog.SelectIntegerDialog;
import de.eric_scheibler.tactileclock.utils.SettingsManager;
import androidx.fragment.app.Fragment;
import android.os.Build;
import android.content.Intent;
import android.provider.Settings;
import de.eric_scheibler.tactileclock.utils.ApplicationInstance;
import androidx.appcompat.widget.SwitchCompat;
import android.app.NotificationManager;
import android.widget.Toast;
import android.content.Context;
import de.eric_scheibler.tactileclock.utils.TactileClockService;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;


public class WatchFragment extends Fragment implements IntegerSelector {

	// Store instance variables
	private SettingsManager settingsManagerInstance;

    private SwitchCompat buttonStartWatch, buttonEnableVibration, buttonPlayGTS;
    private LinearLayout containerVibrationOptions, containerGTSOptions;
    private Button buttonWatchInterval;
    private SwitchCompat buttonWatchOnlyVibrateMinutes, buttonWatchStartAtNextFullHour, buttonWatchAnnouncementVibration, buttonWatchPlayGTSWhileMusic;

    // newInstance constructor for creating fragment with arguments
    public static WatchFragment newInstance() {
        WatchFragment watchFragmentInstance = new WatchFragment();
        return watchFragmentInstance;
    }

	@Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settingsManagerInstance = SettingsManager.getInstance();
	}

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_watch, container, false);
    }

	@Override public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

        buttonStartWatch = (SwitchCompat) view.findViewById(R.id.buttonStartWatch);
        buttonStartWatch.setOnCheckedChangeListener(null);

        buttonEnableVibration = (SwitchCompat) view.findViewById(R.id.switchEnableVibration);
        buttonEnableVibration.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (settingsManagerInstance.getWatchEnableVibration() != isChecked) {
                    settingsManagerInstance.setWatchEnableVibration(isChecked);
                    updateUI();
                }
            }
        });

        containerVibrationOptions = (LinearLayout) view.findViewById(R.id.containerVibrationOptions);

        buttonWatchInterval = (Button) view.findViewById(R.id.buttonWatchInterval);
        buttonWatchInterval.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                SelectIntegerDialog dialog = SelectIntegerDialog.newInstance(
                        Token.WATCH_INTERVAL,
                        settingsManagerInstance.getWatchVibrationIntervalInMinutes(),
                        SettingsManager.DEFAULT_WATCH_VIBRATION_INTERVAL);
                dialog.setTargetFragment(WatchFragment.this, 1);
                dialog.show(getActivity().getSupportFragmentManager(), "SelectIntegerDialog");
            }
        });

        buttonWatchOnlyVibrateMinutes = (SwitchCompat) view.findViewById(R.id.buttonWatchOnlyVibrateMinutes);
        buttonWatchOnlyVibrateMinutes.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (settingsManagerInstance.getWatchOnlyVibrateMinutes() != isChecked) {
                    settingsManagerInstance.setWatchOnlyVibrateMinutes(isChecked);
                }
            }
        });

        buttonWatchStartAtNextFullHour = (SwitchCompat) view.findViewById(R.id.buttonWatchStartAtNextFullHour);
        buttonWatchStartAtNextFullHour.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (settingsManagerInstance.getWatchStartAtNextFullHour() != isChecked) {
                    settingsManagerInstance.setWatchStartAtNextFullHour(isChecked);
                }
            }
        });

        buttonWatchAnnouncementVibration = (SwitchCompat) view.findViewById(R.id.buttonWatchAnnouncementVibration);
        buttonWatchAnnouncementVibration.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (settingsManagerInstance.getWatchAnnouncementVibration() != isChecked) {
                    settingsManagerInstance.setWatchAnnouncementVibration(isChecked);
                }
            }
        });

        buttonPlayGTS = (SwitchCompat) view.findViewById(R.id.buttonPlayGTS);
        buttonPlayGTS.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (settingsManagerInstance.getPlayGTS() != isChecked) {
                    settingsManagerInstance.setPlayGTS(isChecked);
                    updateUI();
                }
            }
        });

        containerGTSOptions = (LinearLayout) view.findViewById(R.id.containerGTSOptions);

        buttonWatchPlayGTSWhileMusic = (SwitchCompat) view.findViewById(R.id.buttonWatchPlayGTSWhileMusic);
        buttonWatchPlayGTSWhileMusic.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (settingsManagerInstance.getPlayGTSWhileMusic() != isChecked) {
                    settingsManagerInstance.setPlayGTSWhileMusic(isChecked);
                }
            }
        });
    }

    @Override public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mMessageReceiver);
    }

    @Override public void onResume() {
        super.onResume();
        if (settingsManagerInstance.isWatchEnabled()
                && ! ApplicationInstance.canScheduleExactAlarms()) {
            settingsManagerInstance.disableWatch();
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(TactileClockService.UPDATE_WATCH_UI);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mMessageReceiver, filter);

        updateUI();
    }

    @Override public void integerSelected(Token token, Integer newInteger) {
        if (newInteger != null) {
            switch (token) {
                case WATCH_INTERVAL:
                    settingsManagerInstance.setWatchVibrationIntervalInMinutes(newInteger);
                    updateUI();
                    break;
                default:
                    break;
            }
        }
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(TactileClockService.UPDATE_WATCH_UI)) {
                updateUI();
            }
        }
    };


    private void updateUI() {
        buttonStartWatch.setChecked(settingsManagerInstance.isWatchEnabled());
        buttonStartWatch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (settingsManagerInstance.isWatchEnabled() != isChecked) {
                    if (! settingsManagerInstance.isWatchEnabled()) {
                        tryToEnableWatch();
                    } else {
                        settingsManagerInstance.disableWatch();
                    }
                    updateUI();
                }
            }
        });

        // Semi transparent elements when watch is disabled
        float alpha = settingsManagerInstance.isWatchEnabled() ? 0.4f : 1f;

        // Vibration
        buttonEnableVibration.setChecked(settingsManagerInstance.getWatchEnableVibration());
        buttonEnableVibration.setClickable(! settingsManagerInstance.isWatchEnabled());
        buttonEnableVibration.setAlpha(alpha);

        containerVibrationOptions.setVisibility(buttonEnableVibration.isChecked() ? View.VISIBLE : View.GONE);

        buttonWatchInterval.setText(
                String.format(
                    "%1$s: %2$s",
                    getResources().getString(R.string.buttonWatchInterval),
                    getResources().getQuantityString(
                        R.plurals.minutes,
                        settingsManagerInstance.getWatchVibrationIntervalInMinutes(),
                        settingsManagerInstance.getWatchVibrationIntervalInMinutes()))
                );
        buttonWatchInterval.setClickable(! settingsManagerInstance.isWatchEnabled());
        buttonWatchInterval.setAlpha(alpha);

        buttonWatchOnlyVibrateMinutes.setChecked(settingsManagerInstance.getWatchOnlyVibrateMinutes());
        buttonWatchOnlyVibrateMinutes.setClickable(! settingsManagerInstance.isWatchEnabled());
        buttonWatchOnlyVibrateMinutes.setAlpha(alpha);

        buttonWatchStartAtNextFullHour.setChecked(settingsManagerInstance.getWatchStartAtNextFullHour());
        buttonWatchStartAtNextFullHour.setClickable(! settingsManagerInstance.isWatchEnabled());
        buttonWatchStartAtNextFullHour.setAlpha(alpha);

        buttonWatchAnnouncementVibration.setChecked(settingsManagerInstance.getWatchAnnouncementVibration());
        buttonWatchAnnouncementVibration.setClickable(! settingsManagerInstance.isWatchEnabled());
        buttonWatchAnnouncementVibration.setAlpha(alpha);

        // GTS
        buttonPlayGTS.setChecked(settingsManagerInstance.getPlayGTS());
        buttonPlayGTS.setClickable(! settingsManagerInstance.isWatchEnabled());
        buttonPlayGTS.setAlpha(alpha);

        containerGTSOptions.setVisibility(buttonPlayGTS.isChecked() ? View.VISIBLE : View.GONE);

        buttonWatchPlayGTSWhileMusic.setChecked(settingsManagerInstance.getPlayGTSWhileMusic());
        buttonWatchPlayGTSWhileMusic.setClickable(! settingsManagerInstance.isWatchEnabled());
        buttonWatchPlayGTSWhileMusic.setAlpha(alpha);
    }

    private void tryToEnableWatch() {
        if (ApplicationInstance.canScheduleExactAlarms()) {
            settingsManagerInstance.enableWatch();

            // show some warnings
            AudioManager audioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
            NotificationManager notificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);

            if (TactileClockService.isDoNotDisturbEnabled(notificationManager)) {
                Toast.makeText(
                        getContext(), R.string.warningDndActive, Toast.LENGTH_LONG)
                    .show();

            } else if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT) {
                Toast.makeText(
                        getContext(), R.string.warningRingerModeSilent, Toast.LENGTH_LONG)
                    .show();
            }

        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                requestScheduleExactAlarmPermissionForSAndNewer();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    private void requestScheduleExactAlarmPermissionForSAndNewer() {
        // only relevant for version code S
        // because for TIRAMISU and newer the permission USE_EXACT_ALARM is requested and that one is granted implicetly
        // therefore for later Android versions the function canScheduleExactAlarms() from above never returns false (see apps manifest for details)
        Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
        startActivity(intent);
    }

}
