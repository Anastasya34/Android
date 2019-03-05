package com.example.user.library;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;


public class ContentActivity extends AppCompatActivity{
    final static String MSSQL_DB = "jdbc:jtds:sqlserver://ASUS\\SQLEXPRESS;databaseName=library";
    final static String MSSQL_LOGIN = "Asus\\Admin";
    final static String MSSQL_PASS = "";
    private static final int LOADER_ID = 734;

    private EditText searchRequest;
    private RecyclerView booksView;
    private List<Book> books;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);

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
}
