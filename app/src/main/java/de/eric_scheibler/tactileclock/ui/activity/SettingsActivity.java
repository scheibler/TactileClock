package de.eric_scheibler.tactileclock.ui.activity;

import android.os.Bundle;

import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RadioGroup;

import androidx.appcompat.widget.Toolbar;

import de.eric_scheibler.tactileclock.R;
import de.eric_scheibler.tactileclock.data.HourFormat;
import de.eric_scheibler.tactileclock.data.TimeComponentOrder;
import de.eric_scheibler.tactileclock.ui.activity.AbstractActivity;
import androidx.appcompat.widget.SwitchCompat;
import android.widget.CompoundButton;


public class SettingsActivity extends AbstractActivity {

    private SwitchCompat switchMaxStrengthVibrations;
    private RadioGroup radioHourFormat, radioTimeComponentOrder;

	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);

        // toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(
                getResources().getString(R.string.settingsActivityTitle));

        switchMaxStrengthVibrations = (SwitchCompat) findViewById(R.id.switchMaxStrengthVibrations);
        switchMaxStrengthVibrations.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton view, boolean isChecked) {
                if (isChecked != settingsManagerInstance.getMaxStrengthVibrationsEnabled()) {
                    settingsManagerInstance.setMaxStrengthVibrationsEnabled(isChecked);
                }
            }
        });

        // hour format
        radioHourFormat = (RadioGroup) findViewById(R.id.radioHourFormat);
        radioHourFormat.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.button12Hours) {
                    settingsManagerInstance.setHourFormat(HourFormat.TWELVE_HOURS);
                } else if (checkedId == R.id.button24Hours) {
                    settingsManagerInstance.setHourFormat(HourFormat.TWENTYFOUR_HOURS);
                }
            }
        });

        // time component order: "hours minutes" or "minutes hours"
        radioTimeComponentOrder = (RadioGroup) findViewById(R.id.radioTimeComponentOrder);
        radioTimeComponentOrder.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.buttonHoursMinutes) {
                    settingsManagerInstance.setTimeComponentOrder(TimeComponentOrder.HOURS_MINUTES);
                } else if (checkedId == R.id.buttonMinutesHours) {
                    settingsManagerInstance.setTimeComponentOrder(TimeComponentOrder.MINUTES_HOURS);
                }
            }
        });
    }

	@Override public void onResume() {
		super.onResume();
        updateUI();
    }

    private void updateUI() {
        switchMaxStrengthVibrations.setChecked(settingsManagerInstance.getMaxStrengthVibrationsEnabled());
        if (settingsManagerInstance.getHourFormat() == HourFormat.TWELVE_HOURS) {
            radioHourFormat.check(R.id.button12Hours);
        } else {
            radioHourFormat.check(R.id.button24Hours);
        }
        if (settingsManagerInstance.getTimeComponentOrder() == TimeComponentOrder.MINUTES_HOURS) {
            radioTimeComponentOrder.check(R.id.buttonMinutesHours);
        } else {
            radioTimeComponentOrder.check(R.id.buttonHoursMinutes);
        }
    }

}
