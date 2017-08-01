package com.example.aro_pc.myapplication.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.aro_pc.myapplication.BaseActivity;
import com.example.aro_pc.myapplication.Consts;
import com.example.aro_pc.myapplication.R;
import com.example.aro_pc.myapplication.fragment.AccountSettingsFragment;
import com.example.aro_pc.myapplication.fragment.ChooseUserFragment;
import com.example.aro_pc.myapplication.fragment.MainFragment;
import com.example.aro_pc.myapplication.helper.SharedPreferanceHelper;
import com.example.aro_pc.myapplication.services.BackgroundService;
import com.google.firebase.database.FirebaseDatabase;

import static com.example.aro_pc.myapplication.Consts.DATABASE_NAME;
import static com.example.aro_pc.myapplication.Consts.SHARED_PREFERANCES_UID;

public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {

    private FirebaseDatabase firebaseDatabase;
    private FragmentManager fragmentManager;
    private AccountSettingsFragment settingsFragment;
    private MainFragment mainFragment;
    private ChooseUserFragment chooseUserFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.GONE);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        fragmentManager = getSupportFragmentManager();
        mainFragment = new MainFragment();

        fragmentManager.beginTransaction().replace(R.id.container, mainFragment).commit();

        forFirebase();
        forServices();
    }

    private void forServices() {

        Intent intent = new Intent(this, BackgroundService.class);
        startService(intent);

    }

    private void forFirebase() {
        firebaseDatabase = FirebaseDatabase.getInstance();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            startActivity(intent);
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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id){
            case R.id.nav_account_settings:
                settingsFragment = new AccountSettingsFragment();
                fragmentManager.beginTransaction().replace(R.id.container, settingsFragment).commit();

                break;
            case R.id.nav_all_users:
                chooseUserFragment = new ChooseUserFragment();
                chooseUserFragment.setContext(getApplicationContext());
                fragmentManager.beginTransaction().replace(R.id.container,chooseUserFragment).commit();
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        String uId = SharedPreferanceHelper.getInstance().getString(SHARED_PREFERANCES_UID,"");
        FirebaseDatabase.getInstance().getReference(DATABASE_NAME).child(uId).child(Consts.USER_MODEL_ONLINE_STATUS).setValue(Consts.USER_MODEL_STATUS_VALUE_ONLINE);

    }

    @Override
    protected void onStop() {
        super.onStop();
        String uId = SharedPreferanceHelper.getInstance().getString(SHARED_PREFERANCES_UID,"");
        FirebaseDatabase.getInstance().getReference(DATABASE_NAME).child(uId).child(Consts.USER_MODEL_ONLINE_STATUS).setValue(Consts.USER_MODEL_STATUS_VALUE_OFFLINE);
    }
}
