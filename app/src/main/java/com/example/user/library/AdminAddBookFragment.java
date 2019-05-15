package com.example.user.library;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.app.Fragment;
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
    private SelectDormitoryReceiver selectDormitoryReceiver;
    private SelectRoomReceiver selectRoomReceiver;
    private SelectBoardReceiver selectBoardReceiver;
    private SelectCupBoardReceiver selectCupBoardReceiver;
    private SelectAuthorReceiver selectAuthorReceiver;
    private SelectBookReceiver selectBookReceiver;
    private CheckInstanceBookReceiver checkInstanceBookReceiver;
    private InsertAuthorReceiver insertAuthorReceiver;
    private InsertBookAuthorReceiver insertBookAuthorReceiver;
    private InsertBookReceiver insertBookReceiver;
    volatile String roomId = "";
    volatile String boardId = "";
    volatile String cupBoardId = "";
    volatile String authorId = "";
    volatile String bookId = "";
    volatile Boolean check = true;
    String bookNameStr;
    EditText bookName;
    EditText amountPage;
    EditText publishYear;
    EditText roomNumber;
    Boolean endTask = false;
    private List<Author> newAuthors;
    private List<View> allEds;
    volatile ArrayList<Author> authors;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        allEds = new ArrayList<View>();
        authors = new ArrayList<>();
        authorsIds = new HashMap<>();
        newAuthors = new ArrayList<>();
        selectDormitoryReceiver = new SelectDormitoryReceiver(new Handler());
        selectBoardReceiver = new SelectBoardReceiver(new Handler());
        selectRoomReceiver = new SelectRoomReceiver(new Handler());
        selectCupBoardReceiver = new SelectCupBoardReceiver(new Handler());
        insertBookReceiver = new InsertBookReceiver(new Handler());
        selectAuthorReceiver = new SelectAuthorReceiver(new Handler());
        selectBookReceiver = new SelectBookReceiver(new Handler());
        checkInstanceBookReceiver = new CheckInstanceBookReceiver(new Handler());
        insertAuthorReceiver = new InsertAuthorReceiver(new Handler());
        insertBookAuthorReceiver = new InsertBookAuthorReceiver(new Handler());
    }

        @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_add_new_book, container, false);
        addAuthor(rootView);
        bookName =  (EditText)rootView.findViewById(R.id.book_name_value);
        roomNumber = rootView.findViewById(R.id.room_value);
        //author = rootView.findViewById(R.id.book_author_value);
        EditText boardNumber = rootView.findViewById(R.id.board_value);

        amountPage =  ((EditText)rootView.findViewById(R.id.amount_page_value));
        publishYear =  ((EditText)rootView.findViewById(R.id.publishyear_value));
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
        return rootView;
    }
    public void addAuthor(View view) {
        LinearLayout mainLayout = rootView.findViewById(R.id.mainLinearLayout);
        final View autorView = getLayoutInflater().inflate(R.layout.author_layout, null);
        allEds.add(view);
        mainLayout.addView(autorView);
    }
    public void addBook(View v) {
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

    private class CheckInstanceBookReceiver extends ResultReceiver {

        CheckInstanceBookReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            switch (resultCode) {
                case DbService.REQUEST_ERROR:
                    Log.d("SelectRoomReceiver", resultData.getString("SQLException"));
                    break;

                case DbService.REQUEST_SUCCESS:
                    String jsonString = resultData.getString("JSONString");
                    try {
                        JSONArray resultSet = new JSONArray(jsonString);
                        if (resultSet.length() == 0) {
                            //new InsertBookTask().execute(bookName.getText().toString(), amountPage.getText().toString());
                            //amountinstances = +1
                            break;
                        }
                        //new InsertBookTask().execute(bookName.getText().toString(), amountPage.getText().toString());
                        //amountinstances = 1

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    break;
            }
            super.onReceiveResult(resultCode, resultData);
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
                    break;

                case DbService.REQUEST_SUCCESS:
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
                                    "           ,[amountpage])" +
                                    "     VALUES( " + roomId +
                                    "           , '" + selectedDormitory +"'"+
                                    "           , " + cupBoardId+
                                    "           , " + boardId +
                                    "           , '" + bookName.getText().toString()+"'" +
                                    "           , 1" +
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
                            Toast toast = Toast.makeText(rootView.getContext(),
                                    "Книга уже существует в базе данных", Toast.LENGTH_SHORT);
                            toast.show();
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
    private class InsertBookAuthorReceiver extends ResultReceiver {
        InsertBookAuthorReceiver(Handler handler) {
            super(handler);
        }
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            if (resultCode == DbService.REQUEST_ERROR){
                    Log.d("InsertBookAuthorReceiver", resultData.getString("SQLException"));
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
    private class InsertAuthorReceiver extends ResultReceiver {

        InsertAuthorReceiver(Handler handler) {
            super(handler);
        }
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            switch (resultCode) {
                case DbService.REQUEST_ERROR:
                    Log.d("InsertAuthorReceiver", resultData.getString("SQLException"));
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
    private class SelectAuthorReceiver extends ResultReceiver {

        SelectAuthorReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            switch (resultCode) {
                case DbService.REQUEST_ERROR:
                    Log.d("SelectAuthorReceiver", resultData.getString("SQLException"));
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
    private class SelectCupBoardReceiver extends ResultReceiver {

        SelectCupBoardReceiver(Handler handler) {
            super(handler);
        }
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            switch (resultCode) {
                case DbService.REQUEST_ERROR:
                    Log.d("SelectRoomReceiver", resultData.getString("SQLException"));
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

    private class InsertBookReceiver extends ResultReceiver {

        InsertBookReceiver(Handler handler) {
            super(handler);
        }
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            switch (resultCode) {
                case DbService.REQUEST_ERROR:
                    Log.d("data", resultData.getString("SQLException"));
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
                            startIntent(query, selectBookReceiver, "select");
                            for (View view : allEds){
                                EditText surname = view.findViewById(R.id.authorsurname_value);
                                EditText firstname = view.findViewById(R.id.authorname_value);
                                EditText secondname = view.findViewById(R.id.authorsecondname_value);
                                authors.add(new Author(firstname.getText().toString(),
                                        secondname.getText().toString(),
                                        surname.getText().toString()));
                            }
                            String selectAuthors = "SELECT [author_id], [authorsurname],[authorfirstname], [authorsecondname]   FROM [dbo].[author] WHERE " + authors.get(0).toSelectString();
                            for (int i = 1; authors.size() > 1 && i < authors.size(); i++ ){
                                selectAuthors = selectAuthors + " OR " + authors.get(i).toSelectString();
                            }
                            Log.d("selectAuthors", selectAuthors);
                            startIntent(selectAuthors, selectAuthorReceiver, "select");
                            new InsertBookAuthorTask().execute();
                            //insert bookauthor task                          //   и др fk
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
}
