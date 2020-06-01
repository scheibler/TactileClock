package de.eric_scheibler.tactileclock.ui.activity;

import com.google.common.primitives.Ints;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import de.eric_scheibler.tactileclock.ui.dialog.HelpDialog;


import android.content.Intent;

import android.os.Bundle;

import de.eric_scheibler.tactileclock.R;
import de.eric_scheibler.tactileclock.ui.activity.AbstractActivity;
import de.eric_scheibler.tactileclock.ui.fragment.PowerButtonFragment;
import de.eric_scheibler.tactileclock.ui.fragment.WatchFragment;
import de.eric_scheibler.tactileclock.utils.TactileClockService;


public class MainActivity extends AbstractActivity {

    // fragments
    public static final int TAB_POWER_BUTTON = 0;
    public static final int TAB_WATCH = 1;
    public final static int[] FragmentValueArray = { 
        TAB_POWER_BUTTON, TAB_WATCH
    };

	private DrawerLayout drawerLayout;
    private NavigationView navigationView;

	private ViewPager viewPager;
    private TabAdapter tabAdapter;
    private TabLayout tabLayout;
    private int selectedTabIndex;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // navigation drawer
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        navigationView = (NavigationView) findViewById(R.id.navigationView);
        navigationView.setItemIconTintList(null);

        // Setup click events on the Navigation View Items.
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override public boolean onNavigationItemSelected(MenuItem menuItem) {
                drawerLayout.closeDrawers();
                switch (menuItem.getItemId()) {
                    case R.id.menuItemSettings:
                        Intent intentStartSettingsActivity = new Intent(MainActivity.this, SettingsActivity.class);
                        startActivity(intentStartSettingsActivity);
                        break;
                    case R.id.menuItemInfo:
                        Intent intentStartInfoActivity = new Intent(MainActivity.this, InfoActivity.class);
                        startActivity(intentStartInfoActivity);
                        break;
                    case R.id.menuItemTutorial:
                        HelpDialog.newInstance()
                            .show(getSupportFragmentManager(), "HelpDialog");
                        break;
                    default:
                        break;
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

        tabAdapter = new TabAdapter(this);
		viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(tabAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override public void onPageSelected(int position) {
    			if (selectedTabIndex != position) {
                    // set toolbar title
                    setToolbarTitle(position);
                    // save active fragment
    				selectedTabIndex = position;
                }
            }
        });

		tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);

        // open selected tab
        int tabIndexFromBundle = -1;
        if (savedInstanceState != null) {
            tabIndexFromBundle = savedInstanceState.getInt("selectedTabIndex");
        }
        if (Ints.contains(FragmentValueArray, tabIndexFromBundle)) {
            selectedTabIndex = tabIndexFromBundle;
        } else {
            selectedTabIndex = TAB_POWER_BUTTON;
        }
        setToolbarTitle(selectedTabIndex);
        viewPager.setCurrentItem(selectedTabIndex);

        // start service
        Intent intent = new Intent(this, TactileClockService.class);
        startService(intent);
    }

    @Override public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt("selectedTabIndex", selectedTabIndex);
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
                tabAdapter.getPageTitle(tabIndex).toString());
    }


    /**
     * tab adapter
     */

	public class TabAdapter extends FragmentPagerAdapter {

		public TabAdapter(FragmentActivity activity) {
			super(activity.getSupportFragmentManager());
		}

        @Override public Fragment getItem(int position) {
            switch (position) {
                case TAB_POWER_BUTTON:
                    return PowerButtonFragment.newInstance();
                case TAB_WATCH:
                    return WatchFragment.newInstance();
                default:
                    return null;
            }
        }

		@Override public CharSequence getPageTitle(int position) {
            switch (position) {
                case TAB_POWER_BUTTON:
                    return getResources().getString(R.string.fragmentPowerButton);
                case TAB_WATCH:
                    return getResources().getString(R.string.fragmentWatch);
                default:
                    return "";
            }
        }

		@Override public int getCount() {
			return FragmentValueArray.length;
        }
    }

}
