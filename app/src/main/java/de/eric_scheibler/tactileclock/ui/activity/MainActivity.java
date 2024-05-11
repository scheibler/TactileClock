package de.eric_scheibler.tactileclock.ui.activity;

import androidx.core.content.ContextCompat;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;

import android.content.Intent;

import android.os.Bundle;

import android.view.MenuItem;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;

import androidx.core.view.GravityCompat;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;


import de.eric_scheibler.tactileclock.R;
import de.eric_scheibler.tactileclock.ui.activity.AbstractActivity;
import de.eric_scheibler.tactileclock.ui.dialog.HelpDialog;
import de.eric_scheibler.tactileclock.ui.fragment.PowerButtonFragment;
import de.eric_scheibler.tactileclock.ui.fragment.WatchFragment;
import de.eric_scheibler.tactileclock.utils.TactileClockService;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import timber.log.Timber;
import de.eric_scheibler.tactileclock.utils.ApplicationInstance;


public class MainActivity extends AbstractActivity {
    public static String EXTRA_NEW_TAB = "newTab";


	private DrawerLayout drawerLayout;
    private NavigationView navigationView;

	private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private Tab selectedTab;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Timber.d("onCreate");

        // start service
        Intent startServiceIntent = new Intent(this, TactileClockService.class);
        startServiceIntent.setAction(TactileClockService.ACTION_UPDATE_NOTIFICATION);
            ContextCompat.startForegroundService(
                    ApplicationInstance.getContext(), startServiceIntent);

        // navigation drawer
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        navigationView = (NavigationView) findViewById(R.id.navigationView);
        navigationView.setItemIconTintList(null);

        // Setup click events on the Navigation View Items.
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override public boolean onNavigationItemSelected(MenuItem menuItem) {
                drawerLayout.closeDrawers();
                if (menuItem.getItemId() == R.id.menuItemSettings) {
                    Intent intentStartSettingsActivity = new Intent(MainActivity.this, SettingsActivity.class);
                    startActivity(intentStartSettingsActivity);
                } else if (menuItem.getItemId() == R.id.menuItemInfo) {
                    Intent intentStartInfoActivity = new Intent(MainActivity.this, InfoActivity.class);
                    startActivity(intentStartInfoActivity);
                } else if (menuItem.getItemId() == R.id.menuItemTutorial) {
                    HelpDialog.newInstance()
                        .show(getSupportFragmentManager(), "HelpDialog");
                }
                return true;
            }
        });

        // toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.openNavigationDrawer, R.string.closeNavigationDrawer);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        // ViewPager2 and TabLayout

		viewPager = (ViewPager2) findViewById(R.id.pager);
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override public void onPageSelected(int position) {
                Timber.d("onPageSelected: %1$d", position);
                tabLayout.selectTab(tabLayout.getTabAt(position));
                setToolbarTitle(position);
                selectedTab = Tab.getTabAtPosition(position);
            }
        });

		tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) {
                loadFragment(tab.getPosition());
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {
            }
            @Override public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        // prepare tab adapter and load tab

        TabAdapter tabAdapter = new TabAdapter(MainActivity.this);
        for (int pos=0; pos<tabAdapter.getItemCount(); pos++) {
            TabLayout.Tab tab = tabLayout.newTab();
            tab.setText(tabAdapter.getFragmentName(pos));
            tabLayout.addTab(tab);
        }
        viewPager.setAdapter(tabAdapter);

        Tab tabToOpen = getTabFromIntent(getIntent());
        if (tabToOpen != null) {
            loadFragment(tabToOpen.position);
        } else {
            if (savedInstanceState != null) {
                tabToOpen = (Tab) savedInstanceState.getSerializable("selectedTab");
            }
            if (tabToOpen == null) {
                tabToOpen = Tab.CLOCK;
            }
            loadFragment(tabToOpen.position);
        }
    }

    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Tab tabToOpen = getTabFromIntent(intent);
        if (tabToOpen != null) {
            loadFragment(tabToOpen.position);
        }
    }

    private Tab getTabFromIntent(Intent intent) {
        if (intent != null && intent.getExtras() != null) {
            return (Tab) intent.getExtras().getSerializable(EXTRA_NEW_TAB);
        }
        return null;
    }

    @Override public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putSerializable("selectedTab", selectedTab);
    }

    @Override public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawers();
        } else {
            super.onBackPressed();
        }
    }

    private void setToolbarTitle(int tabIndex) {
        getSupportActionBar().setTitle(
                tabLayout.getTabAt(tabIndex).getText().toString());
    }


    /**
     * tabs
     */

    public enum Tab {
        CLOCK(0),
        SHORTCUT(1);

        public static Tab getTabAtPosition(int position) {
            for (Tab tab : Tab.values()) {
                if (tab.position == position) {
                    return tab;
                }
            }
            return null;
        }

        public int position;

        private Tab(int position) {
            this.position = position;
        }
    }


    private void loadFragment(int newTabIndex) {
        Timber.d("loadFragment: newTabIndex=%1$d", newTabIndex);
        viewPager.setCurrentItem(newTabIndex);
    }


	private class TabAdapter extends FragmentStateAdapter {

        public TabAdapter(FragmentActivity activity) {
            super(activity);
        }

        @Override public Fragment createFragment(int position) {
            Tab tab = Tab.getTabAtPosition(position);
            if (tab != null) {
                switch (tab) {
                    case CLOCK:
                        return WatchFragment.newInstance();
                    case SHORTCUT:
                        return PowerButtonFragment.newInstance();
                }
            }
            return null;
        }

		@Override public int getItemCount() {
            return Tab.values().length;
        }

        public String getFragmentName(int position) {
            Tab tab = Tab.getTabAtPosition(position);
            if (tab != null) {
                switch (tab) {
                    case CLOCK:
                        return getResources().getString(R.string.fragmentWatch);
                    case SHORTCUT:
                        return getResources().getString(R.string.fragmentPowerButton);
                }
            }
                return "";
        }
    }

}
