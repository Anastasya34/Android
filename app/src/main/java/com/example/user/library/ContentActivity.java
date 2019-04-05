package com.example.user.library;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;


public class ContentActivity extends Fragment {
    private RecyclerView booksView;
    private List<Book> books;
    private EditText searchRequest;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.content_main, container, false);
        booksView = rootView.findViewById(R.id.ViewBooks);
        searchRequest = rootView.findViewById(R.id.book_search);
        booksView.setLayoutManager(new LinearLayoutManager(getActivity()));
       /* RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int visibleItemCount = layoutManager.getChildCount();//смотрим сколько элементов на экране
                int totalItemCount = layoutManager.getItemCount();//сколько всего элементов
                int firstVisibleItems = layoutManager.findFirstVisibleItemPosition();//какая позиция первого элемента

                if (!isLoading) {//проверяем, грузим мы что-то или нет, эта переменная должна быть вне класса  OnScrollListener
                    if ( (visibleItemCount+firstVisibleItems) >= totalItemCount) {
                        isLoading = true;//ставим флаг что мы попросили еще элемены
                        if(loadingListener != null){
                            loadingListener.loadMoreItems(totalItemCount);//тут я использовал калбэк который просто говорит наружу что нужно еще элементов и с какой позиции начинать загрузку
                        }
                    }
                }
            }
        };*/
        initializeData();
        initializeAdapter();
        return rootView;

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
