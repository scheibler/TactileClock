package de.eric_scheibler.tactileclock.ui;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Switch;
import de.eric_scheibler.tactileclock.R;
import de.eric_scheibler.tactileclock.utils.Constants;

public class SettingsFragment extends Fragment {

    private SharedPreferences settings;

    // newInstance constructor for creating fragment with arguments
    public static SettingsFragment newInstance() {
        SettingsFragment settingsFragmentInstance = new SettingsFragment();
        return settingsFragmentInstance;
    }

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
    }

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

	@Override public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

        // enable service
        Switch buttonEnableService = (Switch) view.findViewById(R.id.buttonEnableService);
        buttonEnableService.setChecked(
                settings.getBoolean(Constants.SETTINGS_KEY.ENABLE_SERVICE, true));
        buttonEnableService.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Editor editor = settings.edit();
                editor.putBoolean(
                        Constants.SETTINGS_KEY.ENABLE_SERVICE, isChecked);
                editor.apply();
            }
        });

        // hour format radio group
        RadioGroup radioHourFormat = (RadioGroup) view.findViewById(R.id.radioHourFormat);
        Constants.HourFormat hourFormat = Constants.HourFormat.lookupByCode(
                settings.getString(Constants.SETTINGS_KEY.HOUR_FORMAT, null));
        if (Constants.HourFormat.TWELVE_HOURS == hourFormat) {
            radioHourFormat.check(R.id.button12Hours);
        } else {
            radioHourFormat.check(R.id.button24Hours);
        }
        radioHourFormat.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                Editor editor = settings.edit();
                switch (checkedId) {
                    case R.id.button12Hours:
                        editor.putString(
                                Constants.SETTINGS_KEY.HOUR_FORMAT,
                                Constants.HourFormat.TWELVE_HOURS.getCode());
                        break;
                    case R.id.button24Hours:
                        editor.putString(
                                Constants.SETTINGS_KEY.HOUR_FORMAT,
                                Constants.HourFormat.TWENTYFOUR_HOURS.getCode());
                        break;
                    default:
                        break;
                }
                editor.apply();
            }
        });

        // time component order: "hours minutes" or "minutes hours"
        RadioGroup radioTimeComponentOrder = (RadioGroup) view.findViewById(R.id.radioTimeComponentOrder);
        Constants.TimeComponentOrder timeComponentOrder = Constants.TimeComponentOrder.lookupByCode(
                settings.getString(Constants.SETTINGS_KEY.TIME_COMPONENT_ORDER, null));
        if (Constants.TimeComponentOrder.MINUTES_HOURS == timeComponentOrder) {
            radioTimeComponentOrder.check(R.id.buttonMinutesHours);
        } else {
            radioTimeComponentOrder.check(R.id.buttonHoursMinutes);
        }
        radioTimeComponentOrder.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                Editor editor = settings.edit();
                switch (checkedId) {
                    case R.id.buttonHoursMinutes:
                        editor.putString(
                                Constants.SETTINGS_KEY.TIME_COMPONENT_ORDER,
                                Constants.TimeComponentOrder.HOURS_MINUTES.getCode());
                        break;
                    case R.id.buttonMinutesHours:
                        editor.putString(
                                Constants.SETTINGS_KEY.TIME_COMPONENT_ORDER,
                                Constants.TimeComponentOrder.MINUTES_HOURS.getCode());
                        break;
                    default:
                        break;
                }
                editor.apply();
            }
        });

        // error vibration
        Switch buttonErrorVibration = (Switch) view.findViewById(R.id.buttonErrorVibration);
        buttonErrorVibration.setChecked(
                settings.getBoolean(Constants.SETTINGS_KEY.ERROR_VIBRATION, true));
        buttonErrorVibration.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Editor editor = settings.edit();
                editor.putBoolean(
                        Constants.SETTINGS_KEY.ERROR_VIBRATION, isChecked);
                editor.apply();
            }
        });
    }

}
