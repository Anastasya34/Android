package com.example.user.library;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class ProposalActivity extends ContentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.proposal_activity, null, false);
        mDrawer.addView(contentView, 0);

    }

    /*@Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // получим идентификатор выбранного пункта меню
        int id = item.getItemId();
        switch (id) {
            case R.id.general:
                Log.d("onOptionsItemSelected", String.valueOf(id));
                // Выполняем переход на ProposalActivity:
                Intent intent = new Intent(ProposalActivity.this, ContentActivity.class);
                startActivity(intent);
                return true;
        }

        return false;
    }*/
}
