package de.eric_scheibler.tactileclock.ui.fragment;

import android.app.AlarmManager;
import android.content.Context;

import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

import de.eric_scheibler.tactileclock.R;
import de.eric_scheibler.tactileclock.ui.dialog.SelectIntegerDialog.IntegerSelector;
import de.eric_scheibler.tactileclock.ui.dialog.SelectIntegerDialog.Token;
import de.eric_scheibler.tactileclock.ui.dialog.SelectIntegerDialog;
import de.eric_scheibler.tactileclock.utils.SettingsManager;
import androidx.fragment.app.Fragment;
import android.annotation.TargetApi;
import android.os.Build;
import android.content.Intent;
import android.provider.Settings;
import de.eric_scheibler.tactileclock.utils.ApplicationInstance;
import timber.log.Timber;



public class WatchFragment extends Fragment implements IntegerSelector {

	// Store instance variables
	private SettingsManager settingsManagerInstance;

    private Switch buttonStartWatch;
    private Button buttonWatchInterval;
    private Switch buttonWatchOnlyVibrateMinutes, buttonWatchStartAtNextFullHour, buttonWatchAnnouncementVibration;

    // newInstance constructor for creating fragment with arguments
    public static WatchFragment newInstance() {
        WatchFragment watchFragmentInstance = new WatchFragment();
        return watchFragmentInstance;
    }

	@Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settingsManagerInstance = new SettingsManager();
	}

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_watch, container, false);
    }

	@Override public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

        buttonStartWatch = (Switch) view.findViewById(R.id.buttonStartWatch);
        buttonStartWatch.setOnCheckedChangeListener(null);

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

        buttonWatchOnlyVibrateMinutes = (Switch) view.findViewById(R.id.buttonWatchOnlyVibrateMinutes);
        buttonWatchOnlyVibrateMinutes.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (settingsManagerInstance.getWatchOnlyVibrateMinutes() != isChecked) {
                    settingsManagerInstance.setWatchOnlyVibrateMinutes(isChecked);
                }
            }
        });

        buttonWatchStartAtNextFullHour = (Switch) view.findViewById(R.id.buttonWatchStartAtNextFullHour);
        buttonWatchStartAtNextFullHour.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (settingsManagerInstance.getWatchStartAtNextFullHour() != isChecked) {
                    settingsManagerInstance.setWatchStartAtNextFullHour(isChecked);
                }
            }
        });

        buttonWatchAnnouncementVibration = (Switch) view.findViewById(R.id.buttonWatchAnnouncementVibration);
        buttonWatchAnnouncementVibration.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (settingsManagerInstance.getWatchAnnouncementVibration() != isChecked) {
                    settingsManagerInstance.setWatchAnnouncementVibration(isChecked);
                }
            }
        });
    }

    @Override public void onPause() {
        super.onPause();
    }

    @Override public void onResume() {
        super.onResume();
        if (settingsManagerInstance.isWatchEnabled()
                && ! ApplicationInstance.canScheduleExactAlarms()) {
            settingsManagerInstance.disableWatch();
        }

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

    private void updateUI() {
        buttonStartWatch.setChecked(
                settingsManagerInstance.isWatchEnabled());
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

        buttonWatchOnlyVibrateMinutes.setChecked(
                settingsManagerInstance.getWatchOnlyVibrateMinutes());
        buttonWatchOnlyVibrateMinutes.setClickable(! settingsManagerInstance.isWatchEnabled());

        buttonWatchStartAtNextFullHour.setChecked(
                settingsManagerInstance.getWatchStartAtNextFullHour());
        buttonWatchStartAtNextFullHour.setClickable(! settingsManagerInstance.isWatchEnabled());

        buttonWatchAnnouncementVibration.setChecked(
                settingsManagerInstance.getWatchAnnouncementVibration());
        buttonWatchAnnouncementVibration.setClickable(! settingsManagerInstance.isWatchEnabled());
    }

    @TargetApi(Build.VERSION_CODES.S)
    private void tryToEnableWatch() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            if (! ApplicationInstance.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
                return;
            }
        }
        settingsManagerInstance.enableWatch();
    }

}
