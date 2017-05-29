package ir.mhdr.bmi;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ScrollingTabContainerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

import ir.mhdr.bmi.bl.UserBL;
import ir.mhdr.bmi.lib.CustomViewPager;
import ir.mhdr.bmi.lib.MainViewPagerAdapter;
import ir.mhdr.bmi.model.User;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity {

    DrawerLayout drawerLayout;
    Toolbar toolbar;
    ActionBarDrawerToggle actionBarDrawerToggle;
    BottomNavigationView bottomNavigationView;
    CustomViewPager viewPagerMain;
    NavigationView navigationView;
    boolean firstRun = false;
    Spinner spinnerProfile;
    ArrayAdapter<String> spinnerAdapter;

    String[] profiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        UserBL userBL = new UserBL(MainActivity.this);

        if (userBL.countUsers() == 0) {
            firstRun = true;
        }

        if (firstRun) {
            Intent intent = new Intent(MainActivity.this, FirstRunActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish(); // to close current activity
        }

        navigationView = (NavigationView) findViewById(R.id.navigationView);
        ViewCompat.setLayoutDirection(navigationView, ViewCompat.LAYOUT_DIRECTION_RTL);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ViewCompat.setLayoutDirection(toolbar, ViewCompat.LAYOUT_DIRECTION_RTL);
        getSupportActionBar().setTitle(R.string.app_name_fa);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottomNavigationView);

        viewPagerMain = (CustomViewPager) findViewById(R.id.viewPagerMain);
        viewPagerMain.setPagingEnabled(false);

        MainViewPagerAdapter adapter = new MainViewPagerAdapter(getSupportFragmentManager());
        viewPagerMain.setAdapter(adapter);

        // set selected item
        bottomNavigationView.getMenu().getItem(1).setChecked(true);
        viewPagerMain.setCurrentItem(1);

        // bind bottomNavigationView and viewPager
        bottomNavigationView.setOnNavigationItemSelectedListener(bottomNavigationView_OnNavigationItemSelectedListener);
        viewPagerMain.addOnPageChangeListener(viewPagerMain_OnPageChangeListener);

        navigationView.setNavigationItemSelectedListener(navigationView_OnNavigationItemSelectedListener);


        View headerView = navigationView.getHeaderView(0);
        spinnerProfile = (Spinner) headerView.findViewById(R.id.spinnerProfile);
        generateSpinnerProfile();
        spinnerProfile.setOnItemSelectedListener(spinnerProfile_OnItemSelectedListener);
    }

    AdapterView.OnItemSelectedListener spinnerProfile_OnItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    private void generateSpinnerProfile() {
        UserBL userBL = new UserBL(MainActivity.this);
        List<User> userList = userBL.getUsers();
        List<String> profileStrList = new ArrayList<>();

        for (User u : userList) {
            profileStrList.add(u.getName());
        }

        profiles = profileStrList.toArray(new String[0]);
        spinnerAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_dropdown_item,
                profiles);

        spinnerProfile.setAdapter(spinnerAdapter);
    }

    NavigationView.OnNavigationItemSelectedListener navigationView_OnNavigationItemSelectedListener = new NavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            // uncheck other menus and sub menus
            int menuSize = navigationView.getMenu().size();

            for (int i = 0; i < menuSize; i++) {
                MenuItem menuItem = navigationView.getMenu().getItem(i);

                if (menuItem.hasSubMenu()) {
                    int submenuSize = menuItem.getSubMenu().size();

                    for (int j = 0; j < submenuSize; j++) {
                        MenuItem subItem = menuItem.getSubMenu().getItem(j);
                        subItem.setChecked(false);
                    }
                } else {
                    menuItem.setChecked(false);
                }
            }

            item.setChecked(true);

            if (item.getItemId() == R.id.itemMenuExit) {

                finish();
                System.exit(0);
                return true;
            } else if (item.getItemId() == R.id.itemMenuProfile) {
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);
            }

            return false;
        }
    };

    BottomNavigationView.OnNavigationItemSelectedListener bottomNavigationView_OnNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            switch (item.getItemId()) {
                case R.id.item_bn_graph:
                    viewPagerMain.setCurrentItem(0);
                    break;
                case R.id.item_bn_bmi:
                    viewPagerMain.setCurrentItem(1);
                    break;
                case R.id.item_bn_table:
                    viewPagerMain.setCurrentItem(2);
                    break;
            }

            return true;
        }
    };

    ViewPager.OnPageChangeListener viewPagerMain_OnPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            switch (position) {
                case 0:
                    bottomNavigationView.getMenu().getItem(0).setChecked(true);
                    break;
                case 1:
                    bottomNavigationView.getMenu().getItem(1).setChecked(true);
                    break;
                case 2:
                    bottomNavigationView.getMenu().getItem(2).setChecked(true);
                    break;
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        actionBarDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        actionBarDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (drawerLayout.isDrawerOpen(Gravity.END)) {
                drawerLayout.closeDrawer(Gravity.END);
            } else {
                drawerLayout.openDrawer(Gravity.END);
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
