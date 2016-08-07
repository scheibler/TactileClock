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
                settings.getBoolean(Constants.SETTINGS.ENABLE_SERVICE, true));
        buttonEnableService.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Editor editor = settings.edit();
                editor.putBoolean(
                        Constants.SETTINGS.ENABLE_SERVICE, isChecked);
                editor.apply();
            }
        });

        // time format radio group
        RadioGroup radioTimeFormat = (RadioGroup) view.findViewById(R.id.radioTimeFormat);
        Constants.TimeFormat timeFormat = Constants.TimeFormat.lookupByCode(
                settings.getString(Constants.SETTINGS.TIME_FORMAT, null));
        if (timeFormat == Constants.TimeFormat.TWENTYFOUR_HOURS) {
            radioTimeFormat.check(R.id.button24Hours);
        } else {
            radioTimeFormat.check(R.id.button12Hours);
        }
        radioTimeFormat.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                Editor editor = settings.edit();
                switch (checkedId) {
                    case R.id.button12Hours:
                        editor.putString(
                                Constants.SETTINGS.TIME_FORMAT,
                                Constants.TimeFormat.TWELVE_HOURS.getCode());
                        break;
                    case R.id.button24Hours:
                        editor.putString(
                                Constants.SETTINGS.TIME_FORMAT,
                                Constants.TimeFormat.TWENTYFOUR_HOURS.getCode());
                        break;
                    default:
                        break;
                }
                editor.apply();
            }
        });
    }

}
