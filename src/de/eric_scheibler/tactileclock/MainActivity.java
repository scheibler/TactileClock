package de.eric_scheibler.tactileclock;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.widget.CompoundButton;
import android.widget.Switch;

public class MainActivity extends Activity {

	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

        // set 24 hour format option on first application start based on user selection
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        if (! settings.contains(TactileClockService.TWENTY_FOUR_HOUR_FORMAT_KEY)) {
            store24HourFormatOption(DateFormat.is24HourFormat(this));
        }

        // toggle button to switch between 12 and 24 hour format
        Switch buttonTimeFormat = (Switch) findViewById(R.id.buttonTimeFormat);
        buttonTimeFormat.setChecked(
                settings.getBoolean(TactileClockService.TWENTY_FOUR_HOUR_FORMAT_KEY, true));
        buttonTimeFormat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                store24HourFormatOption(isChecked);
            }
        });

        // start service
        Intent intent = new Intent(this, TactileClockService.class);
        startService(intent);
	}

    private void store24HourFormatOption(boolean checked) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        Editor editor = settings.edit();
        editor.putBoolean(TactileClockService.TWENTY_FOUR_HOUR_FORMAT_KEY, checked);
        editor.commit();
    }

}
