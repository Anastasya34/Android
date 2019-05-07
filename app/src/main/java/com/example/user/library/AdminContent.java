package com.example.user.library;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class AdminContent extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout mDrawer;
    private int admin_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_left_panel);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Intent intent = getIntent();
        admin_id = intent.getIntExtra(Constants.ADMIN_ID, -1);
        Log.d("AdminContent admin_id", String.valueOf(admin_id));
        mDrawer = (DrawerLayout) findViewById(R.id.adm_drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.adm_nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        FragmentManager fragmentManager = getSupportFragmentManager();
        try {
            Bundle args = new Bundle();
            args.putInt(Constants.ADMIN_ID, admin_id);
            Fragment startFragment = (Fragment) AdminAllProposalsFragment.class.newInstance();
            startFragment.setArguments(args);
            fragmentManager.beginTransaction().replace(R.id.container, startFragment).commit();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        // getMenuInflater().inflate(R.menu.menu_library, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        /*if (id == R.id.action_settings) {
            return true;
        }*/

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        // Создадим новый фрагмент
        Fragment fragment = null;
        Class fragmentClass = null;
        // получим идентификатор выбранного пункта меню
        int id = item.getItemId();

        Bundle args = new Bundle();
        args.putInt(Constants.ADMIN_ID, admin_id);
        Log.d("args", args.toString());
        switch (id) {
            case R.id.all_proposals:
                Log.d("onOptionsItemSelected", String.valueOf(id));
                fragmentClass = AdminAllProposalsFragment.class;
                break;
            case R.id.my_proposals:
                Log.d("onOptionsItemSelected", String.valueOf(id));
                fragmentClass = AdminMyProposals_Main.class;
                break;
            case R.id.info_book:
                Log.d("onOptionsItemSelected", String.valueOf(id));
                // Выполняем переход на UserMyProposalsFragment:
                fragmentClass = AdminBooksFragment.class;
                break;
            case R.id.info_user:
                Log.d("onOptionsItemSelected", String.valueOf(id));
                fragmentClass = AdminUsersFragment.class;
                break;
            case R.id.proposals_history:
                Log.d("onOptionsItemSelected", String.valueOf(id));
                fragmentClass = AdminHistoryProposalsFragment.class;
                break;

        }


        if (fragmentClass != null) {

            try {
                fragment = (Fragment) fragmentClass.newInstance();
                fragment.setArguments(args);
            } catch (Exception e) {
                e.printStackTrace();
                Log.d("Error", e.getMessage());

            }

            // Вставляем фрагмент, заменяя текущий фрагмент
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.container, fragment).addToBackStack("adminMenu").commit();
            // Выделяем выбранный пункт меню в шторке
            item.setChecked(true);
            // Выводим выбранный пункт в заголовке

            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.adm_drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
        }


        return false;
    }
}
