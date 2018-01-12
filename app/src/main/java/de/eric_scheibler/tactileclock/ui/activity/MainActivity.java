package de.eric_scheibler.tactileclock.ui.activity;

import java.lang.ref.WeakReference;

import android.content.Intent;

import android.os.Bundle;
import android.os.Handler;

import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import de.eric_scheibler.tactileclock.R;
import de.eric_scheibler.tactileclock.ui.activity.AbstractActivity;
import de.eric_scheibler.tactileclock.ui.fragment.PowerButtonFragment;
import de.eric_scheibler.tactileclock.ui.fragment.WatchFragment;
import de.eric_scheibler.tactileclock.utils.Constants;
import de.eric_scheibler.tactileclock.utils.TactileClockService;
import de.eric_scheibler.tactileclock.listener.FragmentCommunicator;


public class MainActivity extends AbstractActivity {

    // communicate with attached fragments
    public FragmentCommunicator powerButtonFragmentCommunicator;
    public FragmentCommunicator watchFragmentCommunicator;

    private TabLayout tabLayout;
    private AppSectionsPagerAdapter mAppSectionsPagerAdapter;
    private ViewPager mViewPager;

    // fragment handler
    private Handler onFragmentDisabledHandler;
    private Handler onFragmentEnabledHandler;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Create the adapter that will return a fragment for each of the three primary sections
        // of the app.
        mAppSectionsPagerAdapter = new AppSectionsPagerAdapter(this);

        // Set up the ViewPager, attaching the adapter and setting up a listener for when the
        // user swipes between sections.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mAppSectionsPagerAdapter);

        // initialize handlers for disabling and enabling fragments
        onFragmentDisabledHandler = new Handler();
        onFragmentEnabledHandler = new Handler();

        // tab layout
        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(mViewPager);

        // add bug free onPageChangeListener
        mViewPager.addOnPageChangeListener(new TabLayoutOnPageChangeListenerBugFree(tabLayout));

        // open the recent tab again
        mViewPager.setCurrentItem(
                settingsManagerInstance.getRecentOpenTab());
        getSupportActionBar().setTitle(
        		mAppSectionsPagerAdapter.getPageTitle(
                    settingsManagerInstance.getRecentOpenTab())
                .toString());

        // start service
        Intent intent = new Intent(this, TactileClockService.class);
        startService(intent);
    }

    @Override public void onPause() {
        super.onPause();
        leaveActiveFragment();
    }

    @Override public void onResume() {
        super.onResume();
        enterActiveFragment();
    }


    /**
     * fragment management
     */

    private void leaveActiveFragment() {
        onFragmentDisabledHandler.postDelayed(
                new OnFragmentDisabledUpdater(settingsManagerInstance.getRecentOpenTab()), 0);
    }

    private class OnFragmentDisabledUpdater implements Runnable {
        private static final int NUMBER_OF_RETRIES = 5;
        private int counter;
        private int currentFragment;
        public OnFragmentDisabledUpdater(int currentFragment) {
            this.counter = 0;
            this.currentFragment = currentFragment;
        }
        @Override public void run() {
            switch (currentFragment) {
                case 0:
                    if(powerButtonFragmentCommunicator != null) {
                        powerButtonFragmentCommunicator.onFragmentDisabled();
                        return;
                    }
                    break;
                case 1:
                    if(watchFragmentCommunicator != null) {
                        watchFragmentCommunicator.onFragmentDisabled();
                        return;
                    }
                    break;
                default:
                    return;
            }
            if (counter < NUMBER_OF_RETRIES) {
                counter += 1;
                onFragmentDisabledHandler.postDelayed(this, 100);
            }
        }
    }

    private void enterActiveFragment() {
        onFragmentEnabledHandler.postDelayed(
                new OnFragmentEnabledUpdater(settingsManagerInstance.getRecentOpenTab()), 0);
    }

    private class OnFragmentEnabledUpdater implements Runnable {
        private static final int NUMBER_OF_RETRIES = 5;
        private int counter;
        private int currentFragment;
        public OnFragmentEnabledUpdater(int currentFragment) {
            this.counter = 0;
            this.currentFragment = currentFragment;
        }
        @Override public void run() {
            switch (currentFragment) {
                case 0:
                    if(powerButtonFragmentCommunicator != null) {
                        powerButtonFragmentCommunicator.onFragmentEnabled();
                        return;
                    }
                    break;
                case 1:
                    if(watchFragmentCommunicator != null) {
                        watchFragmentCommunicator.onFragmentEnabled();
                        return;
                    }
                    break;
                default:
                    return;
            }
            if (counter < NUMBER_OF_RETRIES) {
                counter += 1;
                onFragmentEnabledHandler.postDelayed(this, 100);
            }
        }
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the primary
     * sections of the app.
     */
    public class AppSectionsPagerAdapter extends FragmentStatePagerAdapter {

        private String[] tabTitles = new String[] {
                getResources().getString(R.string.fragmentPowerButton),
                getResources().getString(R.string.fragmentWatch)
        };

        public AppSectionsPagerAdapter(FragmentActivity activity) {
            super(activity.getSupportFragmentManager());
        }

        @Override public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return PowerButtonFragment.newInstance();
                case 1:
                    return WatchFragment.newInstance();
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
            if (settingsManagerInstance.getRecentOpenTab() != position) {
                leaveActiveFragment();
                settingsManagerInstance.setRecentOpenTab(position);
                final TabLayout tabLayout = mTabLayoutRef.get();
                if (tabLayout != null) {
                    tabLayout.getTabAt(position).select();
                    getSupportActionBar().setTitle(
                    		mAppSectionsPagerAdapter.getPageTitle(
                                settingsManagerInstance.getRecentOpenTab())
                            .toString());
                }
                enterActiveFragment();
            }
        }
    }


}
