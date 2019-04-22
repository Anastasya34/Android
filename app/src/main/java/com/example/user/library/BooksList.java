package com.example.user.library;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class BooksList extends Fragment {

    private EditText searchRequest;
    private RecyclerView booksView;
    private View rootView;

    private Intent startIntent;
    private RequestResultReceiver requestResultReceiver;

    public BooksList() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        rootView = inflater.inflate(R.layout.fragment_books_list, container, false);

        booksView = rootView.findViewById(R.id.ViewBooks);
        LinearLayoutManager llm = new LinearLayoutManager(rootView.getContext());
        booksView.setLayoutManager(llm);
        booksView.setHasFixedSize(true);

        requestResultReceiver = new RequestResultReceiver(new Handler());
        startIntent = new Intent(rootView.getContext(), DbService.class);

        searchRequest = rootView.findViewById(R.id.book_search);
        searchRequest.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String str = String.valueOf(searchRequest.getText());
                startIntent.putExtra("receiver", requestResultReceiver);
                startIntent.putExtra("request", "SELECT book_id, bookname FROM book WHERE book.bookname LIKE '%" + str + "%';");
                rootView.getContext().startService(startIntent);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        startIntent.putExtra("receiver", requestResultReceiver);
        startIntent.putExtra("request", "SELECT book_id, bookname FROM book WHERE book.bookname LIKE '%" + searchRequest.getText() + "%';");
        rootView.getContext().startService(startIntent);

        return rootView;
    }

    private class RequestResultReceiver extends ResultReceiver {

        RequestResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            List<Book> books = new ArrayList<>();
            switch (resultCode) {
                case DbService.REQUEST_ERROR:
                    Log.d("data", resultData.getString("SQLException"));

                    break;

                case DbService.REQUEST_SUCCESS:
                    String jsonString = resultData.getString("JSONString");
                    try {
                        JSONArray resultSet = new JSONArray(jsonString);

                        if (resultSet.length() == 0) {
                            Log.d("data", "пустой");
                            break;
                        }

                        for (int i = 0; i < resultSet.length(); ++i) {
                            JSONObject rec = resultSet.getJSONObject(i);
                            books.add(new Book(rec.getString("bookname"), rec.getString("book_id")));
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    Log.d("data", resultData.getString("JSONString"));
                    break;
            }
            BookListAdapter adapter = new BookListAdapter(books);
            booksView.setAdapter(adapter);
            super.onReceiveResult(resultCode, resultData);
        }

    }

}
