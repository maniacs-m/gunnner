package com.gunnner.ui.main;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.gunnner.R;
import com.gunnner.data.PlayerController;
import com.gunnner.data.Settings;
import com.gunnner.data.helpers.Utils;
import com.gunnner.data.models.Shot;
import com.gunnner.ui.BaseActivity;
import com.gunnner.ui.SettingsFragment;
import com.gunnner.ui.drawer.NavigationDrawerFragment;
import com.gunnner.ui.profile.ProfileFragment;
import com.gunnner.ui.shots.SearchController;
import com.gunnner.ui.shots.ShotsController;
import com.gunnner.ui.shots.ShotsFragment;
import com.gunnner.ui.widgets.InputDialog;

import java.util.ArrayList;

public class MainActivity extends BaseActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks,
        ShotsFragment.OnShotClickedListener, InputDialog.CustomDialogCallback {
    private NavigationDrawerFragment mNavigationDrawerFragment;

    private SearchController searchController;
    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Settings.restore(this);
        Crashlytics.start(this);
        setContentView(R.layout.activity_main);

        searchController = SearchController.getInstance();
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);

        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        switch (position) {
            case 0:  // main
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, new MainFragment())
                        .commit();
                break;
            case 1:  // my profile
                if (PlayerController.isLoggedIn(this)) {
                    setTitle("My profile");
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.container,
                                    ProfileFragment.newInstance(PlayerController.getName()))
                            .commit();
                } else {
                    showInputDialog(-1);
                }
                break;
            case 2:  // my shots
                if (PlayerController.isLoggedIn(this)) {
                    setTitle("My shots");
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.container,
                                    ShotsFragment.newInstance(ShotsFragment.MY_SHOTS))
                            .commit();
                } else {
                    showInputDialog(ShotsFragment.MY_SHOTS);
                }
                break;
            case 3:  // following
                if (PlayerController.isLoggedIn(this)) {
                    setTitle("Following");
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.container,
                                    ShotsFragment.newInstance(ShotsFragment.FOLLOWING))
                            .commit();
                } else {
                    showInputDialog(ShotsFragment.FOLLOWING);
                }
                break;
            case 4:  // likes
                if (PlayerController.isLoggedIn(this)) {
                    setTitle("Likes");
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.container,
                                    ShotsFragment.newInstance(ShotsFragment.LIKES))
                            .commit();
                } else {
                    showInputDialog(ShotsFragment.LIKES);
                }
                break;
            case 5:  // settings
                setTitle("Settings");
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, new SettingsFragment())
                        .commit();
                break;
        }
    }

    public void setTitle(String s) {
        getSupportActionBar().setTitle(s);
    }

    private void showInputDialog(int type) {
        dialog = new InputDialog(this, type, this);
        dialog.show();
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();

            final SearchView searchView = new SearchView(this);
            searchView.setOnSearchClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(MainActivity.this, "Search icon clicked", Toast.LENGTH_SHORT).show();
                }
            });
            searchView.setFocusable(false);

            MenuItem search = menu.findItem(R.id.action_search);
            search.setActionView(searchView);
            search.setIcon(R.drawable.ic_search);
            search.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS
                    | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);

            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    Utils.hideKeyboard(MainActivity.this);
                    // TODO: show a progress bar or something
                    searchController.search(query, new ShotsController.OnShotsLoadedListener() {
                        @Override
                        public void onShotsLoaded(ArrayList<Shot> shots) {
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.container,
                                            ShotsFragment.newInstance(shots))
                                    .addToBackStack("")
                                    .commit();
                        }

                        @Override
                        public void onShotsError() {

                        }
                    });
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String s) {
                    return false;
                }
            });

            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onShotClicked(Shot shot) {
        Utils.openShot(this, shot);
    }

    @Override
    public void onConfirm(String username, int type) {
        PlayerController.setName(MainActivity.this, username);
        Fragment fragment;
        if (type == -1) {
            setTitle("My profile");
            fragment = ProfileFragment.newInstance(username);
        } else {
            if (type == ShotsFragment.MY_SHOTS) {
                setTitle("My shots");
            } else if (type == ShotsFragment.FOLLOWING) {
                setTitle("Following");
            } else if (type == ShotsFragment.LIKES) {
                setTitle("Likes");
            }
            fragment = ShotsFragment.newInstance(type);
        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }

    @Override
    public void onCancel() {

    }
}
