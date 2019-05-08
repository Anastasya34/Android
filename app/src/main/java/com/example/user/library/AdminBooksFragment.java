package com.example.user.library;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ToggleButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class AdminBooksFragment extends Fragment {
    AdminBookListAdapter.BookClickListener adminBookClickListener;
    private EditText searchRequest;
    private RecyclerView booksView;
    private List<Book> books;
    private View rootView;
    private Intent startIntent;
    private RequestResultReceiver requestResultReceiver;

    public AdminBooksFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        rootView = inflater.inflate(R.layout.fragment_books_list, container, false);
        ToggleButton searchingState = rootView.findViewById(R.id.toggleButton_searching);
        searchingState.setVisibility(View.GONE);

        booksView = rootView.findViewById(R.id.ViewBooks);
        LinearLayoutManager llm = new LinearLayoutManager(rootView.getContext());
        booksView.setLayoutManager(llm);
        booksView.setHasFixedSize(true);
        searchRequest = rootView.findViewById(R.id.book_search);

        requestResultReceiver = new RequestResultReceiver(new Handler());
        startIntent = new Intent(rootView.getContext(), DbService.class);
        startIntent.putExtra("receiver", requestResultReceiver);
        startIntent.putExtra("request", "SELECT book_id, bookname, fk_dorm, fk_room, fk_board, fk_cupboard FROM book WHERE book.bookname LIKE '%" + searchRequest.getText() + "%';");
        rootView.getContext().startService(startIntent);
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
        adminBookClickListener = new AdminBookListAdapter.BookClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                // Создадим новый фрагмент
                Fragment fragment = null;
                Class fragmentClass = null;
                Bundle args = new Bundle();
                fragmentClass = UsersListFragment.class;
                args.putString(UsersListFragment.BOOK_ID, books.get(position).description);
                try {
                    fragment = (Fragment) fragmentClass.newInstance();
                    fragment.setArguments(args);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d("Error", e.getMessage());

                }

                // Вставляем фрагмент, заменяя текущий фрагмент
                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.container, fragment).addToBackStack("adminBooks").commit();
                // Выделяем выбранный пункт меню в шторке

                Log.d("!position", String.valueOf(position));
                //Log.d("!id", String.valueOf(id));
            }
        };

        //initializeData();
        //initializeAdapter();

        return rootView;
    }

    private class RequestResultReceiver extends ResultReceiver {

        public RequestResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            switch (resultCode) {
                case DbService.REQUEST_ERROR:
                    Log.d("data", resultData.getString("SQLException"));

                    break;

                case DbService.REQUEST_SUCCESS:
                    String jsonString = resultData.getString("JSONString");
                    try {
                        JSONArray resultSet = new JSONArray(jsonString);
                        books = new ArrayList<>();
                        if (resultSet.length() == 0) {
                            Log.d("data", "пустой");

                            break;
                        }

                        for (int i = 0; i < resultSet.length(); ++i) {
                            JSONObject rec = resultSet.getJSONObject(i);
                            books.add(new Book(rec.getString("bookname"), rec.getString("book_id")));
                        }
                        AdminBookListAdapter adapter = new AdminBookListAdapter(books, adminBookClickListener);
                        booksView.setAdapter(adapter);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                    Log.d("data", resultData.getString("JSONString"));
                    break;
            }
            super.onReceiveResult(resultCode, resultData);
        }

    }

}
