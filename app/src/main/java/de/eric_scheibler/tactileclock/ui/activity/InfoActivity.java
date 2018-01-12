package de.eric_scheibler.tactileclock.ui.activity;

import android.content.Context;

import android.os.Bundle;

import android.support.v7.widget.Toolbar;

import android.view.Menu;

import android.widget.TextView;

import de.eric_scheibler.tactileclock.R;
import de.eric_scheibler.tactileclock.ui.activity.AbstractActivity;


public class InfoActivity extends AbstractActivity {

    private TextView labelApplicationVersion;

	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_info);

        // toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(
                getResources().getString(R.string.infoActivityTitle));

        labelApplicationVersion = (TextView) findViewById(R.id.labelApplicationVersion);
    }

    @Override public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.menuItemTutorial).setVisible(false);
        menu.findItem(R.id.menuItemSettings).setVisible(false);
        menu.findItem(R.id.menuItemInfo).setVisible(false);
        return true;
    }

	@Override public void onResume() {
		super.onResume();
        updateUI();
    }

    private void updateUI() {
        labelApplicationVersion.setText(
                String.format(
                    "%1$s%2$s",
                    getResources().getString(R.string.labelApplicationVersion),
                    settingsManagerInstance.getApplicationVersion())
                );
    }

}
