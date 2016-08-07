package de.eric_scheibler.tactileclock;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

public class SettingsFragment extends Fragment {

    private SharedPreferences settings;

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
                settings.getBoolean(TactileClockService.ENABLE_SERVICE_KEY, true));
        buttonEnableService.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Editor editor = settings.edit();
                editor.putBoolean(
                        TactileClockService.ENABLE_SERVICE_KEY, isChecked);
                editor.commit();
            }
        });

        // toggle button to switch between 12 and 24 hour format
        Switch buttonTimeFormat = (Switch) view.findViewById(R.id.buttonTimeFormat);
        buttonTimeFormat.setChecked(
                settings.getBoolean(TactileClockService.TWENTY_FOUR_HOUR_FORMAT_KEY, true));
        buttonTimeFormat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Editor editor = settings.edit();
                editor.putBoolean(
                        TactileClockService.TWENTY_FOUR_HOUR_FORMAT_KEY,
                        isChecked);
                editor.commit();
            }
        });
    }

}
