package de.eric_scheibler.tactileclock;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;


public class MainActivity extends Activity {

    private ActionBar actionbar;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ActionBar
        actionbar = getActionBar();
        actionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionbar.setDisplayShowTitleEnabled(true);
        actionbar.setDisplayShowHomeEnabled(false);

        Tab startTab = actionbar.newTab()
            .setText(getResources().getString(R.string.fragmentStart))
            .setTabListener(
                    new MyTabListener<StartFragment>(
                        this,
                        getResources().getString(R.string.fragmentStart),
                        StartFragment.class)
                    );
        actionbar.addTab(startTab);

        Tab settingsTab = actionbar.newTab()
            .setText(getResources().getString(R.string.fragmentSettings))
            .setTabListener(
                    new MyTabListener<SettingsFragment>(
                        this,
                        getResources().getString(R.string.fragmentSettings),
                        SettingsFragment.class)
                    );
        actionbar.addTab(settingsTab);

        Tab infoTab = actionbar.newTab()
            .setText(getResources().getString(R.string.fragmentInfo))
            .setTabListener(
                    new MyTabListener<InfoFragment>(
                        this,
                        getResources().getString(R.string.fragmentInfo),
                        InfoFragment.class)
                    );
        actionbar.addTab(infoTab);

        // select tab
        if (savedInstanceState != null) {
            actionbar.setSelectedNavigationItem(
                    savedInstanceState.getInt("tab", 0));
        } else {
            actionbar.setSelectedNavigationItem(0);
        }

        // set 24 hour format option on first application start based on user selection
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        if (! settings.contains(TactileClockService.TWENTY_FOUR_HOUR_FORMAT_KEY)) {
            Editor editor = settings.edit();
            editor.putBoolean(
                    TactileClockService.TWENTY_FOUR_HOUR_FORMAT_KEY,
                    DateFormat.is24HourFormat(this));
            editor.commit();
        }

        // start service
        Intent intent = new Intent(this, TactileClockService.class);
        startService(intent);
    }

    @Override protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("tab", getActionBar().getSelectedNavigationIndex());
    }


    public static class MyTabListener<T extends Fragment> implements ActionBar.TabListener {
        private Fragment mFragment;
        private final Activity mActivity;
        private final String mTag;
        private final Class<T> mClass;

        /** Constructor used each time a new tab is created.
         * @param activity  The host Activity, used to instantiate the fragment
         * @param tag  The identifier tag for the fragment
         * @param clz  The fragment's Class, used to instantiate the fragment
         */
        public MyTabListener(Activity activity, String tag, Class<T> clz) {
            mActivity = activity;
            mTag = tag;
            mClass = clz;
        }

        /* The following are each of the ActionBar.TabListener callbacks */
        public void onTabSelected(Tab tab, FragmentTransaction ft) {
            // Check if the fragment is already initialized
            mFragment = (Fragment) mActivity.getFragmentManager().findFragmentByTag(mTag);
            if (mFragment != null) {
                // If it exists, simply attach it in order to show it
                ft.attach(mFragment);
            } else {
                // If not, instantiate and add it to the activity
                mFragment = Fragment.instantiate(mActivity, mClass.getName());
                ft.add(android.R.id.content, mFragment, mTag);
            }
        }

        public void onTabUnselected(Tab tab, FragmentTransaction ft) {
            if (mFragment != null) {
                // Detach the fragment, because another one is being attached
                ft.detach(mFragment);
            }
        }

        public void onTabReselected(Tab tab, FragmentTransaction ft) {
            // User selected the already selected tab. Usually do nothing.
        }

    }

}
