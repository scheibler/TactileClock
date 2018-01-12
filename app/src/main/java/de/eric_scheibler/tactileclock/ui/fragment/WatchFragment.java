package de.eric_scheibler.tactileclock.ui.fragment;

import android.app.Activity;

import android.content.Context;

import android.os.Bundle;

import android.support.v4.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

import de.eric_scheibler.tactileclock.listener.FragmentCommunicator;
import de.eric_scheibler.tactileclock.listener.SelectIntegerDialogCloseListener;
import de.eric_scheibler.tactileclock.listener.SelectTimeDialogCloseListener;
import de.eric_scheibler.tactileclock.R;
import de.eric_scheibler.tactileclock.ui.activity.MainActivity;
import de.eric_scheibler.tactileclock.ui.dialog.SelectIntegerDialog;
import de.eric_scheibler.tactileclock.ui.dialog.SelectTimeDialog;
import de.eric_scheibler.tactileclock.utils.Constants;
import de.eric_scheibler.tactileclock.utils.SettingsManager;
import de.eric_scheibler.tactileclock.utils.TTSWrapper;

import java.util.Calendar;
import java.util.Locale;


public class WatchFragment extends Fragment
    implements FragmentCommunicator, SelectIntegerDialogCloseListener, SelectTimeDialogCloseListener {

	// Store instance variables
	private SettingsManager settingsManagerInstance;
    private TTSWrapper ttsWrapperInstance;
    private Button buttonStartWatch, buttonWatchInterval, buttonWatchAutoSwitchOff;
    private Switch buttonWatchOnlyVibrateMinutes, buttonWatchStartAtNextFullHour;

    // newInstance constructor for creating fragment with arguments
    public static WatchFragment newInstance() {
        WatchFragment watchFragmentInstance = new WatchFragment();
        return watchFragmentInstance;
    }

	@Override public void onAttach(Context context) {
		super.onAttach(context);
		Activity activity;
		if (context instanceof Activity) {
			activity = (Activity) context;
			// instanciate FragmentCommunicator interface to get data from MainActivity
			((MainActivity) activity).watchFragmentCommunicator = this;
		}
        // settings manager
		settingsManagerInstance = SettingsManager.getInstance(context);
        ttsWrapperInstance = TTSWrapper.getInstance(context);
	}

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_watch, container, false);
    }

	@Override public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

        buttonStartWatch = (Button) view.findViewById(R.id.buttonStartWatch);
        buttonStartWatch.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                onFragmentDisabled();
                if (! settingsManagerInstance.isWatchEnabled()) {
                    settingsManagerInstance.enableWatch();
                    ttsWrapperInstance.speak(
                            getResources().getString(R.string.messageWatchStarted), true, true);
                } else {
                    settingsManagerInstance.disableWatch();
                    ttsWrapperInstance.speak(
                            getResources().getString(R.string.messageWatchStopped), true, true);
                }
                onFragmentEnabled();
            }
        });

        buttonWatchInterval = (Button) view.findViewById(R.id.buttonWatchInterval);
        buttonWatchInterval.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                SelectIntegerDialog dialog = SelectIntegerDialog.newInstance(
                        SelectIntegerDialog.TOKEN_WATCH_INTERVAL,
                        settingsManagerInstance.getWatchVibrationIntervalInMinutes(),
                        SettingsManager.DEFAULT_WATCH_VIBRATION_INTERVAL);
                dialog.setTargetFragment(WatchFragment.this, 1);
                dialog.show(getActivity().getSupportFragmentManager(), "SelectIntegerDialog");
            }
        });

        buttonWatchAutoSwitchOff = (Button) view.findViewById(R.id.buttonWatchAutoSwitchOff);
        buttonWatchAutoSwitchOff.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                SelectTimeDialog dialog = SelectTimeDialog.newInstance();
                dialog.setTargetFragment(WatchFragment.this, 1);
                dialog.show(getActivity().getSupportFragmentManager(), "SelectTimeDialog");
            }
        });

        buttonWatchOnlyVibrateMinutes = (Switch) view.findViewById(R.id.buttonWatchOnlyVibrateMinutes);
        buttonWatchOnlyVibrateMinutes.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (settingsManagerInstance.getWatchOnlyVibrateMinutes() != isChecked) {
                    onFragmentDisabled();
                    settingsManagerInstance.setWatchOnlyVibrateMinutes(isChecked);
                    onFragmentEnabled();
                }
            }
        });

        buttonWatchStartAtNextFullHour = (Switch) view.findViewById(R.id.buttonWatchStartAtNextFullHour);
        buttonWatchStartAtNextFullHour.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (settingsManagerInstance.getWatchStartAtNextFullHour() != isChecked) {
                    onFragmentDisabled();
                    settingsManagerInstance.setWatchStartAtNextFullHour(isChecked);
                    onFragmentEnabled();
                }
            }
        });
    }

    @Override public void onFragmentEnabled() {
        if (! settingsManagerInstance.isWatchEnabled()) {
            buttonStartWatch.setText(
                    getResources().getString(R.string.buttonStartWatch));
        } else {
            buttonStartWatch.setText(
                    getResources().getString(R.string.buttonStopWatch));
        }

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

        if (settingsManagerInstance.getWatchAutoSwitchOffEnabled()) {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(settingsManagerInstance.getWatchAutoSwitchOffTime());
            buttonWatchAutoSwitchOff.setText(
                    String.format(
                        Locale.ROOT,
                        "%1$s: %2$02d:%3$02d",
                        getResources().getString(R.string.buttonWatchAutoSwitchOff),
                        c.get(Calendar.HOUR_OF_DAY),
                        c.get(Calendar.MINUTE))
                    );
        } else {
            buttonWatchAutoSwitchOff.setText(
                    String.format(
                        "%1$s: %2$s",
                        getResources().getString(R.string.buttonWatchAutoSwitchOff),
                        getResources().getString(R.string.dialogDisabled))
                    );
        }
        buttonWatchAutoSwitchOff.setClickable(! settingsManagerInstance.isWatchEnabled());

        buttonWatchOnlyVibrateMinutes.setChecked(
                settingsManagerInstance.getWatchOnlyVibrateMinutes());
        buttonWatchOnlyVibrateMinutes.setClickable(! settingsManagerInstance.isWatchEnabled());

        buttonWatchStartAtNextFullHour.setChecked(
                settingsManagerInstance.getWatchStartAtNextFullHour());
        buttonWatchStartAtNextFullHour.setClickable(! settingsManagerInstance.isWatchEnabled());
    }

	@Override public void onFragmentDisabled() {
    }

    @Override public void integerSelected(int token, int selectedInteger) {
        onFragmentDisabled();
        switch (token) {
            case SelectIntegerDialog.TOKEN_WATCH_INTERVAL:
                settingsManagerInstance.setWatchVibrationIntervalInMinutes(selectedInteger);
                break;
            default:
                break;
        }
        onFragmentEnabled();
    }

    @Override public void timeSelected(boolean enabled, long selectedTime) {
        onFragmentDisabled();
        settingsManagerInstance.setWatchAutoSwitchOffEnabled(enabled);
        settingsManagerInstance.setWatchAutoSwitchOffTime(selectedTime);
        onFragmentEnabled();
    }

}
