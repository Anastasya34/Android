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
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.ToggleButton;

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
    EditText themeSearch;
    TextView authorLabel;
    TableRow rowAuthor;
    Button searchButton;
    LinearLayout.LayoutParams textParam;
    LinearLayout.LayoutParams textParam1;

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

        textParam = new LinearLayout.LayoutParams
                (LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        textParam1 = new LinearLayout.LayoutParams
                (LinearLayout.LayoutParams.WRAP_CONTENT, 0);

        authorLabel = rootView.findViewById(R.id.view_author);
        rowAuthor = rootView.findViewById(R.id.row_author);
        themeSearch = rootView.findViewById(R.id.themeSearch);
        searchButton = rootView.findViewById(R.id.searchButton);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String str = String.valueOf(themeSearch.getText());
                startIntent.putExtra("receiver", requestResultReceiver);
                startIntent.putExtra("request", "select book.book_id, bookname from book, themebook, theme where theme.themename like \'%" + themeSearch.getText() + "%\' and themebook.theme_id = theme.theme_id and themebook.book_id = book.book_id;");
                rootView.getContext().startService(startIntent);
            }
        });

        ToggleButton searchingState = rootView.findViewById(R.id.toggleButton_searching);
        searchingState.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    Log.d("books", "textParam");
                    //rowAuthor.setLayoutParams(textParam);
                    authorLabel.setLayoutParams(textParam);
                    //rowAuthor.getVirtualChildAt(1).setLayoutParams(new LinearLayout.LayoutParams
                    //        (LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT,1f));

                } else {
                    Log.d("books", "setHeight");
                    //rowAuthor.setLayoutParams(textParam1);
                    //rowAuthor.getVirtualChildAt(0).setLayoutParams(textParam1);
                    //rowAuthor.getVirtualChildAt(1).setLayoutParams(textParam1);

                }
            }
        });

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

    private class RequestAdvSearchResultReceiver extends ResultReceiver {

        RequestAdvSearchResultReceiver(Handler handler) {
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
