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
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

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
    TableLayout advSearch;
    ToggleButton searchingState;
    EditText themeSearch;
    EditText authorsSearch;
    EditText nameSearch;
    TextView authorLabel;
    TableRow rowAuthor;
    Button searchButton;
    Button simpleSearch;
    LinearLayout simpleSearchLiner;
    LinearLayout.LayoutParams textParam;
    LinearLayout.LayoutParams textParam1;
    String bookplace_id = "";
    String book_id = "";
    BookListAdapter bookAdapter;
    private SelectAvailableBookReciver selectAvailableBookReciver;
    private InsertProposalReceiver insertProposalReceiver;
    private SelectUserBooksReceiver selectUserBooksReceiver;
    private UpdateBookAvialbilityReceiver updateBookAvialbilityReceiver;
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
        selectAvailableBookReciver = new SelectAvailableBookReciver(new Handler());
        updateBookAvialbilityReceiver = new UpdateBookAvialbilityReceiver(new Handler());
        userBooks = new ArrayList<>();
        books = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        rootView = inflater.inflate(R.layout.fragment_books_list, container, false);

        //узнаем какие книги уже есть у пользователя, чтобы скрыть на них кнопку "оформить заявку"
        String selectUserBooksQuery = "with booksProp as (\n" +
                "SELECT book1_id as book_id, bookplace_id FROM proposal WHERE fk_userreader = " + user_id + " AND bookstatus IN (0,2,4,5,6)\n" +
                ")\n" +
                "select book_id from bookplace where bookavailability = 1 and not(book_id in (select book_id from booksProp)) group by book_id";
        Intent selectUserBooks = new Intent(rootView.getContext(), DbService.class);
        selectUserBooks.putExtra("receiver", selectUserBooksReceiver);
        selectUserBooks.putExtra("request", selectUserBooksQuery);
        rootView.getContext().startService(selectUserBooks);

        advSearch = rootView.findViewById(R.id.tableSearch);
        simpleSearchLiner = rootView.findViewById(R.id.simpleSearchLinear);
        simpleSearch = rootView.findViewById(R.id.simpleSearchButton);
        simpleSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchingState.setChecked(false);
            }
        });

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
        authorsSearch = rootView.findViewById(R.id.author_search);
        searchButton = rootView.findViewById(R.id.searchButton);
        nameSearch = rootView.findViewById(R.id.name_search);

        issuePropoasalClickListener = new BookListAdapter.ButtonClickListener() {
            @Override
            public void onButtonClick(int position, View v) {
                onClickIssuePropoasalButton(position, v);
            }
        };

        bookAdapter = new BookListAdapter(books, issuePropoasalClickListener);
        booksView.setAdapter(bookAdapter);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                String authorsString = String.valueOf(authorsSearch.getText());
                String nameString = String.valueOf(nameSearch.getText());
                String themeString = String.valueOf(themeSearch.getText());

                if (authorsString.isEmpty() && nameString.isEmpty() && themeString.isEmpty())
                    return;

                boolean authorsFlag = authorsString.matches(".*\\w+.*");
                boolean nameFlag = nameString.matches(".*\\w+.*");
                boolean themeFlag = themeString.matches(".*\\w+.*");

                StringBuilder request = new StringBuilder();
                request.append("with ");
                String tableRequest = "";
                if (authorsFlag) {
                    request.append(authorsRequest(authorsString));
                    tableRequest = "booksT";
                }
                if (nameFlag) {
                    if (authorsFlag) request.append(", ");
                    request.append(nameRequest(nameString, authorsFlag));
                    tableRequest = "booksTS";
                }
                if (themeFlag) {
                    if (authorsFlag || nameFlag) request.append(", ");
                    request.append(themeRequest(themeString, authorsFlag, nameFlag));
                    tableRequest = "booksTT";
                }
                request.append("select book.book_id, bookname, fk_dorm, fk_room, fk_board, fk_cupboard\n" + "from ").append(tableRequest).append(", book\n").append("where ").append(tableRequest).append(".book_id = book.book_id\n").append("order by ");
                if (nameFlag || themeFlag)
                    request.append("Rank DESC ");
                if (authorsFlag) {
                    if (nameFlag || themeFlag)
                        request.append(", ");
                    request.append("match DESC ");
                }

                Log.d("ADVSearch", request.toString());
                startIntent.putExtra("receiver", requestResultReceiver);
                startIntent.putExtra("request", request.toString());//"select book.book_id, bookname, fk_dorm, fk_room, fk_board, fk_cupboard from book, themebook, theme where theme.themename like \'%" + themeSearch.getText() + "%\' and themebook.theme_id = theme.theme_id and themebook.book_id = book.book_id;");
                rootView.getContext().startService(startIntent);
            }
        });

        searchingState = rootView.findViewById(R.id.toggleButton_searching);
        searchingState.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    Log.d("books", "textParam");
                    advSearch.setVisibility(View.VISIBLE);
                    simpleSearchLiner.setVisibility(View.GONE);

                } else {
                    Log.d("books", "setHeight");
                    advSearch.setVisibility(View.GONE);
                    simpleSearchLiner.setVisibility(View.VISIBLE);
                }
            }
        });
        searchRequest = rootView.findViewById(R.id.book_search);


        requestResultReceiver = new RequestResultReceiver(new Handler());
        startIntent = new Intent(rootView.getContext(), DbService.class);
        String searchString = String.valueOf(searchRequest.getText());
        String searchRequestStr = "SELECT book_id, bookname FROM book";
        if (searchString.isEmpty()) {
            searchRequestStr += ";";
        } else {
            searchRequestStr += " WHERE book.bookname LIKE \'%" + searchRequest.getText() + "%\';";
        }
        startIntent.putExtra("receiver", requestResultReceiver);
        startIntent.putExtra("request", searchRequestStr);
        rootView.getContext().startService(startIntent);


        searchRequest.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!searchingState.isChecked()) {
                    String str = String.valueOf(searchRequest.getText());
                    String searchRequestStr = "SELECT book_id, bookname FROM book";
                    if (str.isEmpty()) {
                        searchRequestStr += ";";
                    } else {
                        searchRequestStr += " WHERE book.bookname LIKE \'%" + searchRequest.getText() + "%\';";
                    }

                    startIntent.putExtra("receiver", requestResultReceiver);
                    startIntent.putExtra("request", searchRequestStr);
                    rootView.getContext().startService(startIntent);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        return rootView;
    }
    public void onClickIssuePropoasalButton(int position, View v){
        book_id = books.get(position).bookId;
        //SELECT найти доступную книгу
        String selectAvailableBookPlace = "SELECT  TOP 1 [id_bookplace]\n" +
                "      ,[book_id]\n" +
                "      ,[room_id]\n" +
                "      ,[dorm_id]\n" +
                "      ,[cupboard_id]\n" +
                "      ,[board_id]\n" +
                "      ,[bookavailability]\n" +
                "  FROM [dbo].[bookplace] WHERE ([bookavailability] = 1 AND [book_id] = " + book_id + ")";
        selectAvailableBookReciver.setPosition(position);
        startIntent(selectAvailableBookPlace, selectAvailableBookReciver, "select");

    }
    private class SelectAvailableBookReciver extends ResultReceiver {
        int position = -1;

        SelectAvailableBookReciver(Handler handler) {
            super(handler);
        }

        void setPosition(int position) {
            this.position = position;
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            switch (resultCode) {
                case DbService.REQUEST_ERROR:
                    Log.d("SelectAvailableBookReciver", resultData.getString("SQLException"));
                    break;

                case DbService.REQUEST_SUCCESS:
                    String jsonString = resultData.getString("JSONString");
                    try {
                        JSONArray resultSet = new JSONArray(jsonString);

                        GregorianCalendar currentDate = new GregorianCalendar();
                        java.sql.Date date = new java.sql.Date(currentDate.getTimeInMillis());
                        java.sql.Time time = new java.sql.Time(currentDate.getTimeInMillis());

                        GregorianCalendar returnBookDate = new GregorianCalendar();
                        returnBookDate.add(Calendar.MONTH, 1);
                        java.sql.Date returnBookDate_ = new java.sql.Date(returnBookDate.getTimeInMillis());
                        java.sql.Time returnBookTime = new java.sql.Time(returnBookDate.getTimeInMillis());
                        if (resultSet.length() == 0) {
                            String query = "INSERT INTO [dbo].[proposal]" +
                                    " ([bookstatus]" +
                                    ",[issuedate]" +
                                    ",[returndate]" +
                                    ",[fk_userreader]" +
                                    ",[book1_id])" +
                                    " VALUES(" +
                                    "0" +
                                    ",'" + date.toString() + "T"+time.toString() +"'"+
                                    ",'" + returnBookDate_.toString() + "T"+returnBookTime.toString() +"'"+
                                    "," + String.valueOf(user_id)+
                                    "," + book_id +")";
                            Log.d("Query Insert", query);
                            insertProposalReceiver.setPosition(position);
                            startIntent(query, insertProposalReceiver,"update" );

                            break;
                        }


                        for (int i = 0; i < resultSet.length(); ++i) {
                            JSONObject rec = resultSet.getJSONObject(i);
                            bookplace_id = rec.getString("id_bookplace");
                        }
                        Log.d("bookplace_id", bookplace_id);

                        String query = "INSERT INTO [dbo].[proposal]" +
                                " ([bookstatus]" +
                                ",[issuedate]" +
                                ",[returndate]" +
                                ",[fk_userreader]" +
                                ",[bookplace_id]" +
                                ",[book1_id])" +
                                " VALUES(" +
                                "0" +
                                ",'" + date.toString() + "T"+time.toString() +"'"+
                                ",'" + returnBookDate_.toString() + "T"+returnBookTime.toString() +"'"+
                                "," + String.valueOf(user_id)+
                                "," + bookplace_id +
                                "," + book_id +")";
                        Log.d("Query Insert", query);
                        insertProposalReceiver.setPosition(position);
                        startIntent(query, insertProposalReceiver,"update" );

                        String updateBookAvialbilityBookAvialbility = "UPDATE [dbo].[bookplace] SET [bookavailability] = 0 WHERE [id_bookplace] = " + bookplace_id;
                        startIntent(updateBookAvialbilityBookAvialbility, updateBookAvialbilityReceiver,"update" );

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    Log.d("data", resultData.getString("JSONString"));
                    break;
            }

            super.onReceiveResult(resultCode, resultData);
        }

    }
    private class UpdateBookAvialbilityReceiver extends ResultReceiver {

        int position = -1;
        UpdateBookAvialbilityReceiver(Handler handler) {
            super(handler);
        }

        void setPosition(int position) {
            this.position = position;
        }
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            switch (resultCode) {
                case DbService.REQUEST_ERROR:
                    Log.d("data", resultData.getString("SQLException"));

                    break;

                case DbService.REQUEST_SUCCESS:
                    // возвращает количество вставленных или измененных строк
                    int n_strings = resultData.getInt("n_strings");
                    Log.e("data", String.valueOf(n_strings));
                    break;
            }
            super.onReceiveResult(resultCode, resultData);
        }

    }
    private class InsertProposalReceiver extends ResultReceiver {

        int position = -1;
        InsertProposalReceiver(Handler handler) {
            super(handler);
        }

        void setPosition(int position) {
            this.position = position;
        }
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            switch (resultCode) {
                case DbService.REQUEST_ERROR:
                    Log.d("data", resultData.getString("SQLException"));

                    break;

                case DbService.REQUEST_SUCCESS:
                    // возвращает количество вставленных или измененных строк
                    int n_strings = resultData.getInt("n_strings");
                    if (n_strings > 0) {
                        Log.d("data", "заявка оформлена");
                        Toast.makeText(rootView.getContext(),
                                "Заявка оформлена",
                                Toast.LENGTH_LONG).show();
                        books.get(position).already_get = true;
                        bookAdapter.notifyDataSetChanged();
                    } else {
                        Log.e("data", "заявка не оформлена");
                    }
                    break;
            }
            super.onReceiveResult(resultCode, resultData);
        }

    }
    private class RequestResultReceiver extends ResultReceiver {

        RequestResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            switch (resultCode) {
                case DbService.REQUEST_ERROR:
                    Log.d("RequestResultReceiver", resultData.getString("SQLException"));
                    break;

                case DbService.REQUEST_SUCCESS:
                    String jsonString = resultData.getString("JSONString");
                    try {
                        JSONArray resultSet = new JSONArray(jsonString);
                        books.clear();
                        if (resultSet.length() == 0) {
                            Log.d("data", "пустой");

                            break;
                        }

                        for (int i = 0; i < resultSet.length(); ++i) {
                            JSONObject rec = resultSet.getJSONObject(i);
                            //books.add(new Book(rec.getString("bookname"), rec.getString("book_id")));
                            Boolean already_get = userBooks.indexOf(rec.getString("book_id")) == -1;
                            books.add(new Book(rec.getString("book_id"),
                                    rec.getString("bookname"),
                                    already_get ));
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    Log.d("data", resultData.getString("JSONString"));
                    break;
            }
            bookAdapter.notifyDataSetChanged();
            super.onReceiveResult(resultCode, resultData);
        }

    }

    private class SelectUserBooksReceiver extends ResultReceiver {

        SelectUserBooksReceiver(Handler handler) {
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

                        if (resultSet.length() == 0) {
                            Log.d("data", "пустой");
                            break;
                        }

                        userBooks.clear();

                        for (int i = 0; i < resultSet.length(); ++i) {
                            JSONObject rec = resultSet.getJSONObject(i);
                            userBooks.add(rec.getString("book_id"));
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


    static String authorsRequest(String authors) {
        //деление строки на авторов и получение их ФИО
        String[] authorsMas = authors.split(",(\\W|\\b|$)");
        StringBuilder authorsRequest = new StringBuilder();
        authorsRequest.append("authors (author_id) " +
                "as ( " +
                "select author_id " +
                "from author " +
                "where ");
        boolean firstAuthors = true;
        for (String author : authorsMas) {
            if (!firstAuthors)
                authorsRequest.append(" or ");
            boolean firstAuthor = true;

            authorsRequest.append("(");
            for (String s : author.split("\\W(\\b|\\s|$)")) {
                if (!firstAuthor)
                    authorsRequest.append("and ");

                authorsRequest.append("CONCAT(\' \',authorfirstname,\' \',authorsecondname,\' \',authorsurname) like \'% ").append(s).append("%\' ");
                firstAuthor = false;
            }

            authorsRequest.append(")");
            firstAuthors = false;

        }
        authorsRequest.append(
                "), " +
                        "booksT as " +
                        "(" +
                        "select fk_book_id as book_id, count(fk_book_id) as match " +
                        "from bookauthor " +
                        "where fk_author_id IN (Select authors.author_id from authors) " +
                        "group by fk_book_id " +
                        ") "
        );
        Log.d("Author", authorsRequest.toString());
        return String.valueOf(authorsRequest);
    }

    static String nameRequest(String name, boolean authorsFlag) {
        StringBuilder request = new StringBuilder();
        request.append(
                "booksTS as (\n" +
                        "select ");
        if (!authorsFlag) request.append("KEY_TBL.[KEY] as ");
        request.append("book_id, ");

        if (authorsFlag) request.append("match, ");
        request.append("RANK\n from ");
        if (authorsFlag) request.append("booksT, ");
        request.append("FREETEXTTABLE(book, bookname, \'").append(name).append("\') AS KEY_TBL\n");
        if (authorsFlag) request.append("where book_id = KEY_TBL.[KEY]\n");
        request.append(")\n");
        return request.toString();
    }

    static String themeRequest(String themes, boolean authorsFlag, boolean nameFlag) {
        String table = "";
        if (authorsFlag)
            table = "booksT";
        if (nameFlag)
            table = "booksTS";
        StringBuilder request = new StringBuilder();
        request.append(
                "booksTT as(\n" +
                        "select themebook.book_id, ");
        if (authorsFlag)
            request.append("max(match) as match, ");
        request.append("sum(");
        if (nameFlag)
            request.append("booksTS.Rank+");
        request.append("KEY_T.RANK) as rank\n" +
                "from ");
        if (nameFlag || authorsFlag)
            request.append(table).append(", ");
        request.append("themebook \n" +
                "INNER JOIN FREETEXTTABLE(theme, themename, \'").append(themes).append("\') AS KEY_T\n" +
                "ON themebook.theme_id = KEY_T.[KEY]\n");
        if (nameFlag || authorsFlag)
            request.append("where themebook.book_id = ").append(table).append(".book_id\n");
        request.append("group by themebook.book_id\n" +
                ")\n");
        return request.toString();
    }

    public void startIntent(String queryRequest, ResultReceiver startReceiver, String type){
        Intent startIntent = new Intent(rootView.getContext(), DbService.class);
        startIntent.putExtra("request", queryRequest);
        startIntent.putExtra("receiver", startReceiver);
        startIntent.putExtra("type", type);
        rootView.getContext().startService(startIntent);
    }

}
