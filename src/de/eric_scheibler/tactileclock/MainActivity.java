package de.eric_scheibler.tactileclock;

import java.lang.ref.WeakReference;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import de.eric_scheibler.tactileclock.ui.InfoFragment;
import de.eric_scheibler.tactileclock.ui.SettingsFragment;
import de.eric_scheibler.tactileclock.ui.StartFragment;
import de.eric_scheibler.tactileclock.utils.Constants;
import de.eric_scheibler.tactileclock.utils.TactileClockService;


public class MainActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private AppSectionsPagerAdapter mAppSectionsPagerAdapter;
    private ViewPager mViewPager;
    private int recentOpenTab;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // set default settings
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);

        // service should be enabled by default
        if (! settings.contains(Constants.SETTINGS_KEY.ENABLE_SERVICE)) {
            Editor editor = settings.edit();
            editor.putBoolean(
                    Constants.SETTINGS_KEY.ENABLE_SERVICE, true);
            editor.apply();
        }

        // set hour format option on first application start based on user selection
        if (! settings.contains(Constants.SETTINGS_KEY.HOUR_FORMAT)) {
            Editor editor = settings.edit();
            if (DateFormat.is24HourFormat(this)) {
                editor.putString(
                        Constants.SETTINGS_KEY.HOUR_FORMAT,
                        Constants.HourFormat.TWENTYFOUR_HOURS.getCode());
            } else {
                editor.putString(
                        Constants.SETTINGS_KEY.HOUR_FORMAT,
                        Constants.HourFormat.TWELVE_HOURS.getCode());
            }
            editor.apply();
        }

        // set time component order
        if (! settings.contains(Constants.SETTINGS_KEY.TIME_COMPONENT_ORDER)) {
            Editor editor = settings.edit();
            editor.putString(
                    Constants.SETTINGS_KEY.TIME_COMPONENT_ORDER,
                    Constants.TimeComponentOrder.HOURS_MINUTES.getCode());
            editor.apply();
        }

        // error vibration should be enabled by default
        if (! settings.contains(Constants.SETTINGS_KEY.ERROR_VIBRATION)) {
            Editor editor = settings.edit();
            editor.putBoolean(
                    Constants.SETTINGS_KEY.ERROR_VIBRATION, true);
            editor.apply();
        }

        // delete deprecated keys from preferences
        if (settings.contains("24HourFormat")) {
            Editor editor = settings.edit();
            editor.remove("24HourFormat");
            editor.apply();
        }
        if (settings.contains("timeFormat")) {
            Editor editor = settings.edit();
            editor.remove("timeFormat");
            editor.apply();
        }

        // toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.app_name));

        // Create the adapter that will return a fragment for each of the three primary sections
        // of the app.
        mAppSectionsPagerAdapter = new AppSectionsPagerAdapter(this);

        // Set up the ViewPager, attaching the adapter and setting up a listener for when the
        // user swipes between sections.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mAppSectionsPagerAdapter);

        // tab layout
        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(mViewPager);

        // add bug free onPageChangeListener
        mViewPager.addOnPageChangeListener(new TabLayoutOnPageChangeListenerBugFree(tabLayout));

        // open the recent tab again
        recentOpenTab = 0;
        if (savedInstanceState != null)
            recentOpenTab = savedInstanceState.getInt("tab", 0);
        mViewPager.setCurrentItem(recentOpenTab);

        // start service
        Intent intent = new Intent(this, TactileClockService.class);
        startService(intent);
    }

    @Override protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("tab", recentOpenTab);
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the primary
     * sections of the app.
     */
    public class AppSectionsPagerAdapter extends FragmentStatePagerAdapter {

        private String[] tabTitles = new String[] {
                getResources().getString(R.string.fragmentStart),
                getResources().getString(R.string.fragmentSettings),
                getResources().getString(R.string.fragmentInfo)
        };

        public AppSectionsPagerAdapter(FragmentActivity activity) {
            super(activity.getSupportFragmentManager());
        }

        @Override public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return StartFragment.newInstance();
                case 1:
                    return SettingsFragment.newInstance();
                case 2:
                    return InfoFragment.newInstance();
                default:
                    return null;
            }
        }

        @Override public int getCount() {
            return tabTitles.length;
        }

        @Override public CharSequence getPageTitle(int position) {
            return tabTitles[position];
        }
    }

    /**
     * A custom Page Change Listener which fixes the bug described here:
     * https://code.google.com/p/android/issues/detail?id=183123
     **/
    private class TabLayoutOnPageChangeListenerBugFree implements ViewPager.OnPageChangeListener {

        private final WeakReference<TabLayout> mTabLayoutRef;
        private int mPreviousScrollState;
        private int mScrollState;

        public TabLayoutOnPageChangeListenerBugFree(TabLayout tabLayout) {
            mTabLayoutRef = new WeakReference<TabLayout>(tabLayout);
        }

        @Override public void onPageScrollStateChanged(int state) {
            mPreviousScrollState = mScrollState;
            mScrollState = state;
        }

        @Override public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            final TabLayout tabLayout = mTabLayoutRef.get();
            if (tabLayout != null) {
                final boolean updateText = (mScrollState == ViewPager.SCROLL_STATE_DRAGGING)
                    || (mScrollState == ViewPager.SCROLL_STATE_SETTLING
                            && mPreviousScrollState == ViewPager.SCROLL_STATE_DRAGGING);
                tabLayout.setScrollPosition(position, positionOffset, updateText);
            }
        }

        @Override public void onPageSelected(int position) {
            if (recentOpenTab != position) {
                final TabLayout tabLayout = mTabLayoutRef.get();
                if (tabLayout != null)
                    tabLayout.getTabAt(position).select();
                recentOpenTab = position;
            }
        }
    }


}
