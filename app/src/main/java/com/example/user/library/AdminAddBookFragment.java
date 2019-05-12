package com.example.user.library;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class AdminAddBookFragment extends Fragment {
    View rootView;
    String selectedDormitory;
    private SelectDormitoryReceiver selectDormitoryReceiver;
    private InsertBookReceiver insertBookReceiver;
    private SelectRoomReceiver selectRoomReceiver;
    private SelectBoardReceiver selectBoardReceiver;
    private SelectCupBoardReceiver selectCupBoardReceiver;
    volatile String roomId;
    volatile String boardId;
    volatile String cupBoardId;
    EditText bookName;
    EditText amountPage;
    EditText publishYear;
    EditText roomNumber;
    Boolean endTask = false;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selectDormitoryReceiver = new SelectDormitoryReceiver(new Handler());
        selectBoardReceiver = new SelectBoardReceiver(new Handler());
        selectRoomReceiver = new SelectRoomReceiver(new Handler());
        selectCupBoardReceiver = new SelectCupBoardReceiver(new Handler());
    }

        @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_add_new_book, container, false);
        bookName =  (EditText)rootView.findViewById(R.id.book_name_value);
        roomNumber = rootView.findViewById(R.id.room_value);

        roomNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
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
        return rootView;
    }

    // Обрабатываем нажатие кнопки "Зарегистрироваться":
    public void addBook(View view) {
        String roomNumer_ = roomNumber.getText().toString();
        String selectRoomQuery = "SELECT [room_id]  FROM [dbo].[room] WHERE [roomnumber] = "+roomNumer_+" AND [fk_dorm] = '"+selectedDormitory+"'";
        startIntent(selectRoomQuery, selectRoomReceiver, "select");
        new sendInsertTask().execute(selectedDormitory);
        new sendInsertTask2().execute(selectedDormitory);


        // startIntent(query, insertBookReceiver, "insert");


    }
    private class sendInsertTask extends AsyncTask<String, Void, String> {

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
    private class sendInsertTask2 extends AsyncTask<String, Void, String> {

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
    private class sendInsertTask3 extends AsyncTask<String, Void, String> {

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

            String query = "\n" +
                    "INSERT INTO [dbo].[book]\n" +
                    "           ([fk_room]\n" +
                    "           ,[fk_dorm]\n" +
                    "           ,[fk_cupboard]\n" +
                    "           ,[fk_board]\n" +
                    "           ,[bookname]\n" +
                    "           ,[bookavailability]\n" +
                    "           ,[amountpage]\n" +
                    "           ,[amountinstances]\n" +
                    "     VALUES\n" +
                    "           (" + roomId +
                    "           , " + selectedDormitory +
                    "           , " + cupBoardId+
                    "           , " + boardId +
                    "           , 'fgfh'" +
                    "           , 1" +
                    "           , 123" +
                    "           , 11)" ;
            Log.d("addBook", query);
            Log.d("selectBoardQuery",query);
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
    public void startIntent(String queryRequest, ResultReceiver startReceiver, String type){
        Intent startIntent = new Intent(rootView.getContext(), DbService.class);
        startIntent.putExtra("request", queryRequest);
        startIntent.putExtra("receiver", startReceiver);
        startIntent.putExtra("type", type);
        rootView.getContext().startService(startIntent);
    }
}
