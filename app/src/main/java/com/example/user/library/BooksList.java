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
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 */
public class BooksList extends Fragment {
    private BookListAdapter.ButtonClickListener issuePropoasalClickListener;
    public static int user_id = -1;
    private EditText searchRequest;
    private RecyclerView booksView;
    private View rootView;
    private List<Book> books;
    private Intent startIntent;
    private RequestResultReceiver requestResultReceiver;
    EditText themeSearch;
    TextView authorLabel;
    TableRow rowAuthor;
    Button searchButton;
    LinearLayout.LayoutParams textParam;
    LinearLayout.LayoutParams textParam1;

    private InsertProposalReceiver insertProposalReceiver;
    private SelectUserBooksReceiver selectUserBooksReceiver;
    private List<String> userBooks;
    public BooksList() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("GetArguments", String.valueOf(getArguments()));
        if (getArguments() != null) {
            user_id = getArguments().getInt(Constants.USER_ID);
        }
        requestResultReceiver = new RequestResultReceiver(new Handler());
        insertProposalReceiver = new BooksList.InsertProposalReceiver(new Handler());
        selectUserBooksReceiver = new SelectUserBooksReceiver(new Handler());
        userBooks = new ArrayList<>();
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
        //узнаем какие книги уже есть у пользователя, чтобы скрыть на них кнопку "оформить заявку"
        String selectUserBooksQuery = "SELECT book1_id FROM [proposal] WHERE fk_userreader = "+String.valueOf(user_id)+" AND bookstatus IN (0,2,4,5,6)";
        Intent selectUserBooks =  new Intent(rootView.getContext(), DbService.class);
        selectUserBooks.putExtra("receiver", selectUserBooksReceiver);
        selectUserBooks.putExtra("request", selectUserBooksQuery);
        rootView.getContext().startService(selectUserBooks);

        requestResultReceiver = new RequestResultReceiver(new Handler());
        startIntent = new Intent(rootView.getContext(), DbService.class);
        startIntent.putExtra("receiver", requestResultReceiver);
        startIntent.putExtra("request", "SELECT book_id, bookname, fk_dorm, fk_room, fk_board, fk_cupboard FROM book WHERE book.bookname LIKE '%" + searchRequest.getText() + "%';");
        rootView.getContext().startService(startIntent);

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
        issuePropoasalClickListener = new BookListAdapter.ButtonClickListener(){
            @Override
            public void onButtonClick(int position, View v) {
                onClickIssuePropoasalButton(position, v);
            }
        };
        return rootView;
    }
    public void onClickIssuePropoasalButton(int position, View v){
        GregorianCalendar currentDate = new GregorianCalendar();
        java.sql.Date date = new java.sql.Date(currentDate.getTimeInMillis());
        java.sql.Time time = new java.sql.Time(currentDate.getTimeInMillis());

        GregorianCalendar returnBookDate = new GregorianCalendar();
        returnBookDate.add(Calendar.MONTH, 1);
        java.sql.Date returnBookDate_ = new java.sql.Date(returnBookDate.getTimeInMillis());
        java.sql.Time returnBookTime = new java.sql.Time(returnBookDate.getTimeInMillis());
        String query = "INSERT INTO [dbo].[proposal]" +
                " ([bookstatus]" +
                ",[issuedate]" +
                ",[returndate]" +
                ",[fk_userreader]" +
                ",[fk1_dorm]" +
                ",[fk1_room]" +
                ",[fk1_board]" +
                " ,[fk1_cupboard]" +
                ",[book1_id])" +
                " VALUES(" +
                "0" +
                ",'" + date.toString() + "T"+time.toString() +"'"+
                ",'" + returnBookDate_.toString() + "T"+returnBookTime.toString() +"'"+
                "," + String.valueOf(user_id)+
                ",'" + books.get(position).fk1_dorm +"'"+
                "," + books.get(position).fk1_room +
                "," + books.get(position).fk1_board +
                "," + books.get(position).fk1_cupboard +
                "," + books.get(position).bookId +")";
        Log.d("Query Insert", query);
        Intent startIntentInsert = new Intent(rootView.getContext(), DbService.class);
        startIntentInsert.putExtra("receiver", insertProposalReceiver);
        startIntentInsert.putExtra("type", "update");
        startIntentInsert.putExtra("request", query);
        rootView.getContext().startService(startIntentInsert);
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
                        books = new ArrayList<>();
                        if (resultSet.length() == 0) {
                            Log.d("data", "пустой");

                            break;
                        }

                        for (int i = 0; i < resultSet.length(); ++i) {
                            JSONObject rec = resultSet.getJSONObject(i);
                            books.add(new Book(rec.getString("bookname"), rec.getString("book_id")));
                            //Boolean already_get = userBooks.indexOf(rec.getString("book_id")) != -1;
                            books.add(new Book(rec.getString("book_id"),
                                    rec.getString("bookname"),
                                    rec.getString("fk_dorm"),
                                    rec.getString("fk_board"),
                                    rec.getString("fk_room"),
                                    rec.getString("fk_cupboard"),
                                    already_get ));
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
            BookListAdapter adapter = new BookListAdapter(books, issuePropoasalClickListener);
            booksView.setAdapter(adapter);
            super.onReceiveResult(resultCode, resultData);
        }

    }
    private class SelectUserBooksReceiver extends ResultReceiver {

        SelectUserBooksReceiver(Handler handler) {
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
                            userBooks.add(rec.getString("book1_id"));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    Log.d("data", resultData.getString("JSONString"));
                    break;

            }
            super.onReceiveResult(resultCode, resultData);
        }

    }
    private class InsertProposalReceiver extends ResultReceiver {

        InsertProposalReceiver(Handler handler) {
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

                    Log.d("data", "fffffffffffffffffff");
                    break;
            }
            BookListAdapter adapter = new BookListAdapter(books);
            booksView.setAdapter(adapter);
            super.onReceiveResult(resultCode, resultData);
        }

    }

}
