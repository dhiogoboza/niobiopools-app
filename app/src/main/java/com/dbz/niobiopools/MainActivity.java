package com.dbz.niobiopools;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.dbz.niobiopools.fragments.MiningFragment;
import com.dbz.niobiopools.fragments.NBFragment;
import com.dbz.niobiopools.fragments.PoolsFragment;
import com.dbz.niobiopools.fragments.SettingsFragment;
import com.dbz.niobiopools.managers.AccountManager;
import com.dbz.niobiopools.managers.PoolsManager;
import com.dbz.niobiopools.models.Account;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity";

    public static final String PREFERENCES_NAME = "NBStatsPreferences";
    public static final String PREFERENCES_ACCOUNT_PREFIX = "Account_";
    public static final String PREFERENCES_POOL_PREFIX = "Pool_";

    public static final int REQUEST_CODE_ACCOUNT = 2;
    public static final int REQUEST_CODE_POOL = 3;

    private int mCurrentSelection = R.id.nav_mining;
    private NBFragment mCurrentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        AccountManager.createInstance(this);
        PoolsManager.createInstance(this);

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                if (AccountManager.getInstance().getActiveAccount() == null) {
                    AccountManager.getInstance().getAll(true);
                    PoolsManager.getInstance().getAll(true);
                    AccountManager.getInstance().loadActiveAccount();
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void none) {
                if (AccountManager.getInstance().getActiveAccount() == null) {
                    Intent intent = new Intent(MainActivity.this, ManageModelActivity.class);
                    intent.putExtra(ManageModelActivity.EXTRA_FIRST_USE, true);
                    startActivityForResult(intent, REQUEST_CODE_ACCOUNT);

                    Snackbar.make(findViewById(android.R.id.content),
                                R.string.configure_account,
                                Snackbar.LENGTH_LONG)
                            .show();
                } else {
                    if (mCurrentSelection == R.id.nav_mining) {
                        updateCurrentFragment(mCurrentSelection);
                    }
                }
            }

        }.execute();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (AccountManager.getInstance().getActiveAccount() != null) {
            Log.d(TAG, "mCurrentSelection " + mCurrentSelection);
            if (mCurrentSelection == R.id.nav_mining) {
                updateCurrentFragment(mCurrentSelection);
            }

            if (mCurrentFragment != null) {
                mCurrentFragment.invalidateList();
            }
        }
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "home: " + (mCurrentSelection == R.id.nav_mining));

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (mCurrentSelection == R.id.nav_mining) {
            finish();
        } else {
            //TODO: update activity title
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_settings:
                updateCurrentFragment(R.id.nav_settings);
                return true;
            case R.id.action_pools:
                updateCurrentFragment(R.id.nav_pools);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        updateCurrentFragment(item.getItemId());

        return true;
    }

    public void updateCurrentFragment(int id) {
        Log.d(TAG, "current fragment " + id);

        NBFragment fragment;

        switch (id) {
            case R.id.nav_mining: {
                fragment = new MiningFragment();
                break;
            }
            case R.id.nav_pools: {
                fragment = new PoolsFragment();
                break;
            }
            case R.id.nav_settings: {
                fragment = new SettingsFragment();
                break;
            }
            case R.id.nav_share:
                try {
                    Intent i = new Intent(Intent.ACTION_SEND);
                    i.setType("text/plain");
                    i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
                    String sAux = "\n" + getString(R.string.recommenddation) + "\n\n";
                    sAux = sAux + "https://play.google.com/store/apps/details?id=" + getPackageName() + "\n\n";
                    i.putExtra(Intent.EXTRA_TEXT, sAux);
                    startActivity(Intent.createChooser(i, "Choose one"));
                } catch(Exception e) {
                    //e.toString();
                }

                return;
            case R.id.nav_about:
                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                alertDialog.setTitle(R.string.menu_about);
                alertDialog.setMessage(getString(R.string.about_text));
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
                return;

            default:

                return;
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.content_main, fragment)
                .addToBackStack(fragment.getFragmentTag())
                .commit();
        setTitle(fragment.getTitle());

        mCurrentSelection = id;
        mCurrentFragment = fragment;

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }

    public void updateTotal(final Account account, final long totalPaid, final long totalHashrate) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mCurrentFragment instanceof MiningFragment) {
                    ((MiningFragment) mCurrentFragment).updateTotal(account, totalPaid, totalHashrate);
                }
            }
        });
    }
}
