package com.example.user.library;

import android.content.Intent;
import android.graphics.Color;
import android.icu.util.Calendar;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminAddBookFragment extends Fragment {
    View rootView;
    String selectedDormitory;
    volatile  Map<Author, String> authorsIds;
    volatile  Map<String, String> themesIds;
    private SelectDormitoryReceiver selectDormitoryReceiver;
    private SelectRoomReceiver selectRoomReceiver;
    private SelectBoardReceiver selectBoardReceiver;
    private SelectCupBoardReceiver selectCupBoardReceiver;

    private SelectBookReceiver selectBookReceiver;
    private InsertBookReceiver insertBookReceiver;

    private SelectAuthorReceiver selectAuthorReceiver;
    private InsertAuthorReceiver insertAuthorReceiver;
    private InsertBookAuthorReceiver insertBookAuthorReceiver;

    private SelectThemeReceiver selectThemeReceiver;
    private InsertThemeReceiver insertThemeReceiver;
    private InsertBookThemeReceiver insertBookThemeReceiver;
    volatile EditText publisment;
    volatile EditText bookObject;
    volatile String roomId = "";
    volatile String boardId = "";
    volatile String cupBoardId = "";
    volatile String authorId = "";
    volatile String bookId = "";
    volatile Boolean check = true;
    volatile EditText genre;
    String bookNameStr;
    TextView errorMessage;
    EditText bookName;
    EditText amountPage;
    EditText publishYear;
    EditText roomNumber, boardNumber, cupBoardNumber;
    Boolean endTask = false;
    private List<Author> newAuthors;
    private List<String> newThemes;
    private List<View> allEds;
    private List<View> allThemes;
    volatile ArrayList<Author> authors;
    volatile ArrayList<String> themes;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        allEds = new ArrayList<View>();
        allThemes = new ArrayList<>();
        authors = new ArrayList<>();
        themes = new ArrayList<>();
        authorsIds = new HashMap<>();
        themesIds = new HashMap<>();
        newAuthors = new ArrayList<>();
        newThemes = new ArrayList<>();
        selectDormitoryReceiver = new SelectDormitoryReceiver(new Handler());
        selectBoardReceiver = new SelectBoardReceiver(new Handler());
        selectRoomReceiver = new SelectRoomReceiver(new Handler());
        selectCupBoardReceiver = new SelectCupBoardReceiver(new Handler());

        selectBookReceiver = new SelectBookReceiver(new Handler());
        insertBookReceiver = new InsertBookReceiver(new Handler());

        selectAuthorReceiver = new SelectAuthorReceiver(new Handler());
        insertAuthorReceiver = new InsertAuthorReceiver(new Handler());
        insertBookAuthorReceiver = new InsertBookAuthorReceiver(new Handler());

        selectThemeReceiver = new SelectThemeReceiver(new Handler());
        insertThemeReceiver = new InsertThemeReceiver(new Handler());
        insertBookThemeReceiver = new InsertBookThemeReceiver(new Handler());

    }

        @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_add_new_book, container, false);
        addAuthor(rootView);
        addTheme(rootView);
        bookName =  (EditText)rootView.findViewById(R.id.book_name_value);
        genre = ((EditText)rootView.findViewById(R.id.genre_book_value));
        bookObject = rootView.findViewById(R.id.book_object_value);
        publisment = (EditText)rootView.findViewById(R.id.book_publishment_value);
            publishYear =  ((EditText)rootView.findViewById(R.id.publishyear_value));
            amountPage =  ((EditText)rootView.findViewById(R.id.amount_page_value));
            roomNumber = rootView.findViewById(R.id.room_value);
        boardNumber = rootView.findViewById(R.id.board_value);
        cupBoardNumber = rootView.findViewById(R.id.сupboard_value);
        errorMessage = rootView.findViewById(R.id.errorMessage);

            Spinner dormList = (Spinner) rootView.findViewById(R.id.dormitories);
        ArrayAdapter<String> dormAdapter = new ArrayAdapter<>(rootView.getContext(), android.R.layout.simple_spinner_item, new ArrayList<String>());
        // Определяем разметку для использования при выборе элемента
        dormAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Применяем адаптер к элементу spinner
        dormList.setAdapter(dormAdapter);
        dormList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    // Получаем выбранный объект
                    selectedDormitory = (String) parent.getItemAtPosition(position);
                    Log.d("item", selectedDormitory);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        selectDormitoryReceiver.setArgs(dormAdapter);
        startIntent("SELECT namedorm_id FROM [dormitory]", selectDormitoryReceiver, "select" );
            Button addBook = rootView.findViewById(R.id.add_new_book_button);
            addBook.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //User user = books.get(getLayoutPosition());
                   addBook(v);
                }
            });
            Button addAuthor = rootView.findViewById(R.id.add_author_button);
            addAuthor.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //User user = books.get(getLayoutPosition());
                    Log.d("addBooK", "addBooik");
                    addAuthor(v);
                }
            });
            Button addTheme = rootView.findViewById(R.id.add_theme_button);
            addTheme.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //User user = books.get(getLayoutPosition());
                    Log.d("addBooK", "addBooik");
                    addTheme(v);
                }
            });
        return rootView;
    }
  //=====================BOOK===============================================================
    public void addBook(View v) {
        if (!validFields()){
            authors.clear();
            themes.clear();
            return;
        }
        check = true;
        bookNameStr = bookName.getText().toString();
        String roomNumer_ = roomNumber.getText().toString();
        String selectRoomQuery = "SELECT [room_id]  FROM [dbo].[room] WHERE [roomnumber] = "+roomNumer_+" AND [fk_dorm] = '"+selectedDormitory+"'";
        startIntent(selectRoomQuery, selectRoomReceiver, "select");
        new SelectCupBoardIdTask().execute();
        new SelectBoardIdTask().execute();
        new CheckExistTask().execute(bookName.getText().toString(), amountPage.getText().toString());

    }
    private class CheckExistTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            do {
            synchronized (boardId) {
                try {
                    boardId.wait(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } while (boardId.length() == 0);

        String query = "SELECT [book_id] FROM [dbo].[book] WHERE" +
                "     bookname = '"+strings[0]+"' " +
                " AND fk_room = " + roomId +
                " AND fk_dorm = '"+selectedDormitory+"'" +
                " AND fk_board = " + boardId +
                " AND fk_cupboard = " + cupBoardId;

        startIntent(query, selectBookReceiver, "select");
            return null;
        }
    }

    private class SelectBookReceiver extends ResultReceiver {

        SelectBookReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            switch (resultCode) {
                case DbService.REQUEST_ERROR:
                    Log.d("SelectRoomReceiver", resultData.getString("SQLException"));
                    Toast toast = Toast.makeText(rootView.getContext(),"Что-то пошло не так, проверьте введенные данные", Toast.LENGTH_LONG);
                    toast.show();
                    break;

                case DbService.REQUEST_SUCCESS:
                    String pyblishyear_ = "%s-01-01";
                    String jsonString = resultData.getString("JSONString");
                    try {
                        JSONArray resultSet = new JSONArray(jsonString);
                        if (resultSet.length() == 0) {
                            String query = "\n" +
                                    "INSERT INTO [dbo].[book]\n" +
                                    "           ([fk_room]\n" +
                                    "           ,[fk_dorm]\n" +
                                    "           ,[fk_cupboard]\n" +
                                    "           ,[fk_board]\n" +
                                    "           ,[bookname]\n" +
                                    "           ,[bookavailability]\n" +
                                    "           ,[publishyear]\n" +
                                    "           ,[amountpage])" +
                                    "     VALUES( " + roomId +
                                    "           , '" + selectedDormitory +"'"+
                                    "           , " + cupBoardId+
                                    "           , " + boardId +
                                    "           , '" + bookName.getText().toString()+"'" +
                                    "           , 1" +
                                    "           , '" + String.format(pyblishyear_, publishYear.getText().toString()) +"'"+
                                    "           , " + amountPage.getText().toString() + ")" ;
                            Log.d("addBook", query);
                            Log.d("selectBoardQuery",query);
                            startIntent(query, insertBookReceiver, "update");
                            break;
                        }
                        JSONObject row = resultSet.getJSONObject(0);
                        bookId = row.getString("book_id");
                        Log.d("bookId", bookId);
                        if (check) {
                            Toast toast2 = Toast.makeText(rootView.getContext(),
                                    "Книга уже существует в базе данных", Toast.LENGTH_SHORT);
                            toast2.show();
                            check = false;
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
            }
            super.onReceiveResult(resultCode, resultData);
        }

    }

    private class InsertBookReceiver extends ResultReceiver {

        InsertBookReceiver(Handler handler) {
            super(handler);
        }
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            switch (resultCode) {
                case DbService.REQUEST_ERROR:
                    Log.d("data", resultData.getString("SQLException"));
                    Toast toast = Toast.makeText(rootView.getContext(),"Что-то пошло не так, проверьте введенные данные", Toast.LENGTH_LONG);
                    toast.show();
                    break;

                case DbService.REQUEST_SUCCESS:
                    Log.d("data", "пусто");
                    String query = "SELECT [book_id] FROM [dbo].[book] WHERE" +
                            "     bookname = '"+bookNameStr+"' " +
                            " AND fk_room = " + roomId +
                            " AND fk_dorm = '"+selectedDormitory+"'" +
                            " AND fk_board = " + boardId +
                            " AND fk_cupboard = " + cupBoardId;
                    check = false;
                    //get bookId
                    startIntent(query, selectBookReceiver, "select");

                    //add Author
                    String selectAuthors = "SELECT [author_id], [authorsurname],[authorfirstname], [authorsecondname]   FROM [dbo].[author] WHERE " + authors.get(0).toSelectString();
                    for (int i = 1; authors.size() > 1 && i < authors.size(); i++ ){
                        selectAuthors = selectAuthors + " OR " + authors.get(i).toSelectString();
                    }
                    Log.d("selectAuthors", selectAuthors);
                    startIntent(selectAuthors, selectAuthorReceiver, "select");
                    new InsertBookAuthorTask().execute();

                    //add Theme
                    String selectThemes = "SELECT [theme_id], [themename] FROM [dbo].[theme] WHERE [themename] IN ( ";
                    for (String theme : themes){
                        selectThemes = selectThemes + "'" +theme+"', ";
                    }
                    //не может быть ситауции, где тема не заполнена
                    selectThemes = selectThemes.substring(0, selectThemes.lastIndexOf(",")) + ")";
                    Log.d("selectThemes", selectThemes);
                    startIntent(selectThemes, selectThemeReceiver, "select");
                    new InsertThemeBookTask().execute();
                    break;
            }
            super.onReceiveResult(resultCode, resultData);
        }

    }
    //=====================theme=============================================
    public void addTheme(View view){
        LinearLayout themeLayout = rootView.findViewById(R.id.themeLinearLayout);
        EditText newTheme = new  EditText(rootView.getContext());
        newTheme.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        allThemes.add(newTheme);
        themeLayout.addView(newTheme);
    }

    private class SelectThemeReceiver extends ResultReceiver {

        SelectThemeReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            switch (resultCode) {
                case DbService.REQUEST_ERROR:
                    Log.d("SelectThemeReceiver", resultData.getString("SQLException"));
                    Toast toast = Toast.makeText(rootView.getContext(),"Что-то пошло не так, проверьте введенные данные", Toast.LENGTH_LONG);
                    toast.show();
                    break;

                case DbService.REQUEST_SUCCESS:
                    String jsonString = resultData.getString("JSONString");
                    String themeId, themeName;
                    try {
                        JSONArray resultSet = new JSONArray(jsonString);
                        if (resultSet.length() == 0) {
                            Log.d("SelectAuthorReceiver", "пусто");
                        }
                        for (int i = 0; i < resultSet.length(); i++) {
                            JSONObject row = resultSet.getJSONObject(i);
                            themeId = row.getString("theme_id");
                            themeName = row.getString("themename");
                            themesIds.put(themeName, themeId) ;
                        }
                        Log.d("themesIds", String.valueOf(themesIds.size()));
                        Log.d("themes", String.valueOf(themes.size()));

                        String insertThemes = "INSERT INTO [dbo].[theme] ([themename]) VALUES\n";
                        int oldLen = insertThemes.length();
                        for (String theme: themes){
                            if (themesIds.get(theme) == null){
                                insertThemes = insertThemes + " ( '"+theme+"' ), ";
                                newThemes.add(theme);
                            }
                        }
                        if (oldLen != insertThemes.length()){
                            insertThemes = insertThemes.substring(0, insertThemes.lastIndexOf(","));
                            Log.d("insertThemes", insertThemes);
                            startIntent(insertThemes, insertThemeReceiver, "update");
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    break;
            }
            super.onReceiveResult(resultCode, resultData);
        }

    }

    private class InsertThemeReceiver extends ResultReceiver {

        InsertThemeReceiver(Handler handler) {
            super(handler);
        }
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            switch (resultCode) {
                case DbService.REQUEST_ERROR:
                    Log.d("InsertThemeReceiver", resultData.getString("SQLException"));
                    Toast toast = Toast.makeText(rootView.getContext(),"Что-то пошло не так, проверьте введенные данные", Toast.LENGTH_LONG);
                    toast.show();
                    break;

                case DbService.REQUEST_SUCCESS:
                    String selectThemes = "SELECT [theme_id], [themename] FROM [dbo].[theme] WHERE [themename] IN ( ";
                    for (String theme : newThemes){
                        selectThemes = selectThemes + "'" +theme+"', ";
                    }
                    selectThemes = selectThemes.substring(0, selectThemes.lastIndexOf(",")) +")";
                    Log.d("selectThemes", selectThemes);
                    startIntent(selectThemes, selectThemeReceiver, "select");
                    break;
            }
            super.onReceiveResult(resultCode, resultData);
        }

    }

    public class InsertThemeBookTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            do {
                synchronized (themesIds) {
                    try {
                        themesIds.wait(100);
                    } catch (InterruptedException e) {
                        Log.d("error", e.toString());
                    }
                }

            } while (themesIds.size() != themes.size());
            String insertThemeBook = "INSERT INTO  [dbo].[themebook] ([theme_id],[book_id]) VALUES ";
            for (String fk_theme_id: themesIds.values()){
                insertThemeBook = insertThemeBook + "(" +  fk_theme_id  + ", " + bookId +"),";
            }
            insertThemeBook = insertThemeBook.substring(0, insertThemeBook.lastIndexOf(","));
            Log.d("insertBookAuthor",insertThemeBook);
            startIntent(insertThemeBook, insertBookThemeReceiver, "update");
            return null;
        }
    }
    private class InsertBookThemeReceiver extends ResultReceiver {
        InsertBookThemeReceiver(Handler handler) {
            super(handler);
        }
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            if (resultCode == DbService.REQUEST_ERROR){
                Log.d("InsertBookThemeReceiver", resultData.getString("SQLException"));
                Toast toast = Toast.makeText(rootView.getContext(),"Что-то пошло не так, проверьте введенные данные", Toast.LENGTH_LONG);
                toast.show();
            }
            else {
                Toast toast = Toast.makeText(rootView.getContext(),"Книга успешно добавлена", Toast.LENGTH_SHORT);
                toast.show();
                super.onReceiveResult(resultCode, resultData);
                Class fragmentClass = AdminAddBookFragment.class;
                Fragment fragment = null;
                try {
                    fragment = (Fragment) fragmentClass.newInstance();
                } catch (java.lang.InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                fragmentManager.beginTransaction().replace(R.id.container, fragment).addToBackStack("adminMenu").commit();
                //
            }




        }

    }

    //=====================author=============================================

    public void addAuthor(View view) {
        LinearLayout mainLayout = rootView.findViewById(R.id.mainLinearLayout);
        final View autorView = getLayoutInflater().inflate(R.layout.author_layout, null);
        allEds.add(view);
        mainLayout.addView(autorView);
    }
    private class SelectAuthorReceiver extends ResultReceiver {

        SelectAuthorReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            switch (resultCode) {
                case DbService.REQUEST_ERROR:
                    Log.d("SelectAuthorReceiver", resultData.getString("SQLException"));
                    Toast toast = Toast.makeText(rootView.getContext(),"Что-то пошло не так, проверьте введенные данные", Toast.LENGTH_LONG);
                    toast.show();
                    break;

                case DbService.REQUEST_SUCCESS:
                    String jsonString = resultData.getString("JSONString");
                    String authorId, authorName, authorSecondName, authorSurname;
                    try {
                        JSONArray resultSet = new JSONArray(jsonString);
                        if (resultSet.length() == 0) {
                            Log.d("SelectAuthorReceiver", "пусто");
                        }
                        for (int i = 0; i < resultSet.length(); i++) {
                            JSONObject row = resultSet.getJSONObject(i);
                            authorId = row.getString("author_id");
                            authorName = row.getString("authorfirstname");
                            authorSecondName = row.getString("authorsecondname");
                            authorSurname = row.getString("authorsurname");
                            authorsIds.put(new Author(authorName,authorSecondName,authorSurname), authorId) ;
                        }
                        Log.d("authorsIds", String.valueOf(authorsIds.size()));
                        Log.d("author", String.valueOf(authors.size()));

                        String insertAuthors = "INSERT INTO [dbo].[author] ([authorsurname],[authorfirstname],[authorsecondname]) VALUES\n";
                        int lenInsert = insertAuthors.length();
                        for (Author author: authors){
                            if (authorsIds.get(author) == null){
                                insertAuthors = insertAuthors + author.toInsertString()+", ";
                                newAuthors.add(author);
                            }
                        }
                        if (lenInsert != insertAuthors.length()){
                            insertAuthors = insertAuthors.substring(0, insertAuthors.lastIndexOf(","));
                            Log.d("insertAuthors", insertAuthors);
                            startIntent(insertAuthors, insertAuthorReceiver, "update");
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    break;
            }
            super.onReceiveResult(resultCode, resultData);
        }

    }


    private class InsertAuthorReceiver extends ResultReceiver {

        InsertAuthorReceiver(Handler handler) {
            super(handler);
        }
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            switch (resultCode) {
                case DbService.REQUEST_ERROR:
                    Log.d("InsertAuthorReceiver", resultData.getString("SQLException"));
                    Toast toast = Toast.makeText(rootView.getContext(),"Что-то пошло не так, проверьте введенные данные", Toast.LENGTH_LONG);
                    toast.show();
                    break;

                case DbService.REQUEST_SUCCESS:
                    String selectAuthors = "SELECT [author_id], [authorsurname],[authorfirstname], [authorsecondname]   FROM [dbo].[author] WHERE " + newAuthors.get(0).toSelectString();
                    for (int i = 1; newAuthors.size() > 1 && i < newAuthors.size(); i++ ){
                        selectAuthors = selectAuthors + " OR " + newAuthors.get(i).toSelectString();
                    }
                    Log.d("selectAuthors", selectAuthors);
                    startIntent(selectAuthors, selectAuthorReceiver, "select");
                    break;
            }
            super.onReceiveResult(resultCode, resultData);
        }

    }

    public class InsertBookAuthorTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            do {
                synchronized (authorsIds) {
                    try {
                        authorsIds.wait(100);
                    } catch (InterruptedException e) {
                        Log.d("error", e.toString());
                    }
                }

            } while (authorsIds.size() != authors.size());
            String insertBookAuthor = "INSERT INTO [dbo].[bookauthor]\n" +
                    "           ([fk_book_id]\n" +
                    "           ,[fk_author_id])\n" +
                    "     VALUES\n" ;
            for (String fk_author_id: authorsIds.values()){
                insertBookAuthor = insertBookAuthor + "(" + bookId + ", "+  fk_author_id +"),";
            }
            insertBookAuthor = insertBookAuthor.substring(0, insertBookAuthor.lastIndexOf(","));
            startIntent(insertBookAuthor, insertBookAuthorReceiver, "update");
            Log.d("insertBookAuthor",insertBookAuthor);
            return null;
        }
    }

    private class InsertBookAuthorReceiver extends ResultReceiver {
        InsertBookAuthorReceiver(Handler handler) {
            super(handler);
        }
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            if (resultCode == DbService.REQUEST_ERROR){
                    Log.d("InsertReceiver", resultData.getString("SQLException"));
            }
            authorsIds.clear();
            authors.clear();
            allEds.clear();
            super.onReceiveResult(resultCode, resultData);
        }

    }


    //=================================================================================
    private class SelectCupBoardIdTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            do {
                synchronized (roomId) {
                    try {
                        roomId.wait(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } while (roomId.length() == 0);
            String selectCupBoardQuery = "SELECT [cupboard_id]  FROM [dbo].[cupboard] WHERE [fk_room] = " + roomId;
            startIntent(selectCupBoardQuery, selectCupBoardReceiver, "select");
            Log.d("selectCupBoardQuery",selectCupBoardQuery);
            return null;
        }
    }
    private class SelectBoardIdTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {

            do {
                synchronized (cupBoardId) {
                    try {
                        cupBoardId.wait(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } while (cupBoardId.length() == 0);

            String selectBoardQuery = "SELECT [board_id]  FROM [dbo].[board] WHERE [fk_cupboard] = " + cupBoardId;
            startIntent(selectBoardQuery, selectBoardReceiver, "select");
            Log.d("selectBoardQuery",selectBoardQuery);
            return null;
        }
    }


    private class SelectCupBoardReceiver extends ResultReceiver {

        SelectCupBoardReceiver(Handler handler) {
            super(handler);
        }
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            switch (resultCode) {
                case DbService.REQUEST_ERROR:
                    Log.d("SelectRoomReceiver", resultData.getString("SQLException"));
                    Toast toast = Toast.makeText(rootView.getContext(),"Что-то пошло не так, проверьте введенные данные", Toast.LENGTH_LONG);
                    toast.show();
                    break;

                case DbService.REQUEST_SUCCESS:
                    String jsonString = resultData.getString("JSONString");
                    try {
                        JSONArray resultSet = new JSONArray(jsonString);
                        if (resultSet.length() == 0) {
                            Log.d("SelectRoomReceiver", "пусто");
                            break;
                        }
                        for (int i = 0; i < resultSet.length(); i++) {
                            JSONObject row = resultSet.getJSONObject(i);
                            cupBoardId = row.getString("cupboard_id");
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    break;
            }
            super.onReceiveResult(resultCode, resultData);
        }

    }

    private class SelectBoardReceiver extends ResultReceiver {

        SelectBoardReceiver(Handler handler) {
            super(handler);
        }
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            switch (resultCode) {
                case DbService.REQUEST_ERROR:
                    Log.d("SelectBoardReceiver", resultData.getString("SQLException"));
                    Toast toast = Toast.makeText(rootView.getContext(),"Что-то пошло не так, проверьте введенные данные", Toast.LENGTH_LONG);
                    toast.show();
                    break;

                case DbService.REQUEST_SUCCESS:
                    String jsonString = resultData.getString("JSONString");
                    try {
                        JSONArray resultSet = new JSONArray(jsonString);
                        if (resultSet.length() == 0) {
                            Log.d("SelectBoardReceiver", "пусто");
                            break;
                        }
                        for (int i = 0; i < resultSet.length(); i++) {
                            JSONObject row = resultSet.getJSONObject(i);
                            boardId = row.getString("board_id");
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    break;
            }
            super.onReceiveResult(resultCode, resultData);
        }

    }

    private class SelectRoomReceiver extends ResultReceiver {

        SelectRoomReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            switch (resultCode) {
                case DbService.REQUEST_ERROR:
                    Log.d("SelectRoomReceiver", resultData.getString("SQLException"));
                    Toast toast = Toast.makeText(rootView.getContext(),"Что-то пошло не так, проверьте введенные данные", Toast.LENGTH_LONG);
                    toast.show();
                    break;

                case DbService.REQUEST_SUCCESS:
                    String jsonString = resultData.getString("JSONString");
                    try {
                        JSONArray resultSet = new JSONArray(jsonString);
                        if (resultSet.length() == 0) {
                            Log.d("SelectRoomReceiver", "пусто");
                            break;
                        }
                        for (int i = 0; i < resultSet.length(); i++) {
                            JSONObject row = resultSet.getJSONObject(i);
                            roomId = row.getString("room_id");
                        }
                        endTask = true;

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    break;
            }
            super.onReceiveResult(resultCode, resultData);
        }

    }


    private class SelectDormitoryReceiver extends ResultReceiver {

        SelectDormitoryReceiver(Handler handler) {
            super(handler);
        }

        private ArrayAdapter adapter = null;

        void setArgs(ArrayAdapter adapter) {
            this.adapter = adapter;
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            ArrayList<String> list = new ArrayList<>();

            switch (resultCode) {
                case DbService.REQUEST_ERROR:
                    Log.d("data", resultData.getString("SQLException"));
                    Toast toast = Toast.makeText(rootView.getContext(),"Что-то пошло не так, проверьте введенные данные", Toast.LENGTH_LONG);
                    toast.show();
                    break;

                case DbService.REQUEST_SUCCESS:
                    String jsonString = resultData.getString("JSONString");
                    try {
                        JSONArray resultSet = new JSONArray(jsonString);
                        if (resultSet.length() == 0) {
                            Log.d("data", "пусто");
                            break;
                        }

                        for (int i = 0; i < resultSet.length(); i++) {
                            JSONObject o = resultSet.getJSONObject(i);
                            list.add(o.getString(o.keys().next()));
                        }

                        if (adapter != null) {
                            adapter.clear();

                            for (String s : list) {
                                Log.d("List", s);
                                adapter.add(s);
                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    break;
            }
            super.onReceiveResult(resultCode, resultData);
        }

    }

    public void startIntent(String queryRequest, ResultReceiver startReceiver, String type){
        Intent startIntent = new Intent(rootView.getContext(), DbService.class);
        startIntent.putExtra("request", queryRequest);
        startIntent.putExtra("receiver", startReceiver);
        startIntent.putExtra("type", type);
        rootView.getContext().startService(startIntent);
    }

    private Boolean validFields(){
        if (bookName.getText().toString().isEmpty()
                || genre.getText().toString().isEmpty()
                || bookObject.getText().toString().isEmpty()
                || publisment.getText().toString().isEmpty()
                || publishYear.getText().toString().isEmpty()
                || amountPage.getText().toString().isEmpty()
                || roomNumber.getText().toString().isEmpty()
                || boardNumber.getText().toString().isEmpty()
                || cupBoardNumber.getText().toString().isEmpty()) {

            errorMessage.setTextColor(Color.RED);
            errorMessage.setText("Все поля должны быть заполнены");
            errorMessage.setVisibility(View.VISIBLE);
            return false;
        }
        for (View view : allThemes){
            EditText theme = (EditText) view;
            if (!theme.getText().toString().isEmpty()) {
                themes.add(theme.getText().toString());
            }
            else{
                errorMessage.setTextColor(Color.RED);
                errorMessage.setText("Все поля должны быть заполнены");
                errorMessage.setVisibility(View.VISIBLE);
                return false;
            }
        }
        for (View view : allEds){
            String surname = ((EditText) view.findViewById(R.id.authorsurname_value)).getText().toString();
            String firstname = ((EditText) view.findViewById(R.id.authorname_value)).getText().toString();
            String secondname = ((EditText)view.findViewById(R.id.authorsecondname_value)).getText().toString();
            if (!firstname.isEmpty() && !secondname.isEmpty() && !surname.isEmpty()) {
                authors.add(new Author(firstname, secondname, surname));
            }
            else{
                errorMessage.setTextColor(Color.RED);
                errorMessage.setText("Все поля должны быть заполнены");
                errorMessage.setVisibility(View.VISIBLE);
                authors.clear();
                return false;
            }
        }
        try {
            if (publishYear.getText().toString().length() != 4){
                errorMessage.setTextColor(Color.RED);
                errorMessage.setText("Неверно введен год издательства");
                errorMessage.setVisibility(View.VISIBLE);
                return false;
            }
            int publishmentYear = Integer.valueOf(publishYear.getText().toString());
            int currentYear = Calendar.getInstance().get(Calendar.YEAR);
            if (publishmentYear < 1800 ||  currentYear < publishmentYear){
                errorMessage.setTextColor(Color.RED);
                errorMessage.setText("Неверно введен год издательства");
                errorMessage.setVisibility(View.VISIBLE);
                return false;
            }
        }

        catch (NumberFormatException ex){
            errorMessage.setTextColor(Color.RED);
            errorMessage.setText("Неверно введен год издательства");
            errorMessage.setVisibility(View.VISIBLE);
            return false;
        }
        try {
            int amountpage = Integer.valueOf(amountPage.getText().toString());
        }
        catch (NumberFormatException ex){
            errorMessage.setTextColor(Color.RED);
            errorMessage.setText("Неверно введено количество страниц");
            errorMessage.setVisibility(View.VISIBLE);
            return false;
        }
        try {
            int amountpage = Integer.valueOf(roomNumber.getText().toString());
        }
        catch (NumberFormatException ex){
            errorMessage.setTextColor(Color.RED);
            errorMessage.setText("Неверно введен номер комнаты");
            errorMessage.setVisibility(View.VISIBLE);
            return false;
        }
        try {
            int amountpage = Integer.valueOf(boardNumber.getText().toString());
        }
        catch (NumberFormatException ex){
            errorMessage.setTextColor(Color.RED);
            errorMessage.setText("Неверно введен номер шкафа");
            errorMessage.setVisibility(View.VISIBLE);
            return false;
        }
        try {
            int amountpage = Integer.valueOf(cupBoardNumber.getText().toString());
        }
        catch (NumberFormatException ex){
            errorMessage.setTextColor(Color.RED);
            errorMessage.setText("Неверно введен номер полки");
            errorMessage.setVisibility(View.VISIBLE);
            return false;
        }
        return true;
    }
}
