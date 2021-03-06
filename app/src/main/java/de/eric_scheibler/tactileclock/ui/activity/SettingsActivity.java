package de.eric_scheibler.tactileclock.ui.activity;

import android.os.Bundle;

import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RadioGroup;

import androidx.appcompat.widget.Toolbar;

import de.eric_scheibler.tactileclock.R;
import de.eric_scheibler.tactileclock.data.HourFormat;
import de.eric_scheibler.tactileclock.data.TimeComponentOrder;
import de.eric_scheibler.tactileclock.ui.activity.AbstractActivity;


public class SettingsActivity extends AbstractActivity {

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

        // hour format
        radioHourFormat = (RadioGroup) findViewById(R.id.radioHourFormat);
        radioHourFormat.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.button12Hours:
                        settingsManagerInstance.setHourFormat(HourFormat.TWELVE_HOURS);
                        break;
                    case R.id.button24Hours:
                        settingsManagerInstance.setHourFormat(HourFormat.TWENTYFOUR_HOURS);
                        break;
                    default:
                        break;
                }
            }
        });

        // time component order: "hours minutes" or "minutes hours"
        radioTimeComponentOrder = (RadioGroup) findViewById(R.id.radioTimeComponentOrder);
        radioTimeComponentOrder.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.buttonHoursMinutes:
                        settingsManagerInstance.setTimeComponentOrder(TimeComponentOrder.HOURS_MINUTES);
                        break;
                    case R.id.buttonMinutesHours:
                        settingsManagerInstance.setTimeComponentOrder(TimeComponentOrder.MINUTES_HOURS);
                        break;
                    default:
                        break;
                }
            }
        });
    }

	@Override public void onResume() {
		super.onResume();
        updateUI();
    }

    private void updateUI() {
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
