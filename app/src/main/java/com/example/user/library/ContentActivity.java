package com.example.user.library;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;


public class ContentActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{


    private EditText searchRequest;
    private RecyclerView booksView;
    private List<Book> books;
    protected DrawerLayout mDrawer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.left_panel);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        booksView = findViewById(R.id.ViewBooks);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        booksView.setLayoutManager(llm);
        booksView.setHasFixedSize(true);

        searchRequest = findViewById(R.id.book_search);

        initializeData();
        initializeAdapter();

    }

    private void initializeData(){
        books = new ArrayList<>();
        books.add(new Book("Emma Wilson", "23 years old"));
        books.add(new Book("Lavery Maiss", "25 years old"));
        books.add(new Book("Lillie Watts", "35 years old"));
    }

    private void initializeAdapter(){
        BookListAdapter adapter = new BookListAdapter(books);
        booksView.setAdapter(adapter);
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // получим идентификатор выбранного пункта меню
        int id = item.getItemId();
        switch (id) {
            case R.id.my_proposal:
                Log.d("onOptionsItemSelected", String.valueOf(id));
                // Выполняем переход на ProposalActivity:
                Intent intent = new Intent(ContentActivity.this, ProposalActivity.class);
                startActivity(intent);
                return true;
        }

        return false;
    }
}
