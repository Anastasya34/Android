package com.example.user.library;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class RegistrationActivity extends AppCompatActivity { //implements LoaderManager.LoaderCallbacks<ArrayList<String>> {
    final static String MSSQL_DB = "jdbc:jtds:sqlserver://dertosh.ddns.net:49173;databaseName=LibraryNew;integratedSecurity=true";
    final static String MSSQL_LOGIN = "ReadingUser";
    final static String MSSQL_PASS = "Reading1234";
    private static final int LOADER_DORM = 734;
    private static final int LOADER_ROOM = 2;
    private static final int LOADER_REGISTRATION = 3;
    OnItemSelectedListener itemSelectedListenerDormitory;
    OnItemSelectedListener itemSelectedListenerRoom;
    Spinner dormList;
    //private ArrayAdapter<String> dormAdapter;
    private RequestResultReceiver roomRequestResultReceiver;
    Spinner roomList;
    String selectedDormitory;
    String selectedRoom;
    TextView errorMessage;

    private Intent startIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startIntent = new Intent(this, DbService.class);

        RequestResultReceiver requestResultReceiver = new RequestResultReceiver(new Handler());
        roomRequestResultReceiver = new RequestResultReceiver(new Handler());

        setContentView(R.layout.registration_activity);
        roomList = (Spinner) findViewById(R.id.rooms);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, new ArrayList<String>());
        // Определяем разметку для использования при выборе элемента
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Применяем адаптер к элементу spinner
        roomList.setAdapter(adapter);
        roomList.setOnItemSelectedListener(itemSelectedListenerRoom);

        roomRequestResultReceiver.setArgs(adapter);

        dormList = (Spinner) findViewById(R.id.dormitories);

        ArrayAdapter<String> dormAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<String>());
        // Определяем разметку для использования при выборе элемента
        dormAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Применяем адаптер к элементу spinner
        dormList.setAdapter(dormAdapter);
        dormList.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Получаем выбранный объект
                selectedDormitory = (String) parent.getItemAtPosition(position);
                Bundle loaderRoomBundleParams = new Bundle();
                loaderRoomBundleParams.putString("DormitoryName", selectedDormitory);

                startIntent.putExtra("receiver", roomRequestResultReceiver);
                startIntent.putExtra("request", "SELECT roomnumber, room_id FROM [room] WHERE fk_dorm = '" + selectedDormitory + "' ORDER BY roomnumber");
                startService(startIntent);

                Log.d("item", selectedDormitory);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        requestResultReceiver.setArgs(dormAdapter);

        startIntent.putExtra("receiver", requestResultReceiver);
        startIntent.putExtra("request", "SELECT namedorm_id FROM [dormitory]");
        startService(startIntent);

        errorMessage = findViewById(R.id.errorMessage);

        itemSelectedListenerRoom = new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Получаем выбранный объект
                selectedRoom = (String)parent.getItemAtPosition(position);
                Log.d("item", selectedDormitory);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };

    }
    // Обрабатываем нажатие кнопки "Зарегистрироваться":
    public void LoginReg(View view) {
        EditText fistName =  findViewById(R.id.firstName_value);
        EditText secondName =  findViewById(R.id.secondName_value);
        EditText surName =  findViewById(R.id.surname_value);
        EditText phone =  findViewById(R.id.phone_value);
        EditText email =  findViewById(R.id.email_value);
        EditText login =  findViewById(R.id.login_value);
        EditText password =  findViewById(R.id.password_value);

        ArrayList<EditText> editList = new ArrayList<>();
        editList.add(fistName);
        editList.add(secondName);
        editList.add(surName);
        editList.add(phone);
        editList.add(email);
        editList.add(login);
        editList.add(password);

        for (EditText editText : editList) {
            editText.setEnabled(false);
        }


        String fistNameString =  fistName.getText().toString();
        String secondNameString =  secondName.getText().toString();
        String surNameString =  surName.getText().toString();
        String phoneString =  phone.getText().toString();
        String emailString =  email.getText().toString();
        String loginString =  login.getText().toString();
        String passwordString =  password.getText().toString();
        if (fistNameString.isEmpty()
                || secondNameString.isEmpty()
                || surNameString.isEmpty()
                || phoneString.isEmpty()
                || emailString.isEmpty()
                || loginString.isEmpty()
                || passwordString.isEmpty()){

            errorMessage.setText("Все поля должны быть заполнены");
            errorMessage.setVisibility(View.VISIBLE);
        } else {

            String query = "INSERT INTO [userreader] (" +
                    "[userfirstname], " +
                    "[usersecondname], " +
                    "[usersurname], " +
                    "[fk_room], " +
                    "[phonenumber], " +
                    "[userlogin], " +
                    "[userpassword], " +
                    "[fk_dorm])" +
                    " VALUES ("
                    + "\'" + fistNameString + "\', "
                    + "\'" + secondNameString + "\', "
                    + "\'" + surNameString + "\', "
                    + selectedRoom + ", "
                    + "\'" + phoneString + "\', "
                    + "\'" + loginString + "\', "
                    + "\'" + passwordString + "\', "
                    + "\'" + selectedDormitory + "\')";
        }

        for (EditText editText : editList) {
            editText.setEnabled(true);
        }

    }

//    public Connection getConnection(){
//        try {
//            Class.forName("net.sourceforge.jtds.jdbc.Driver");
//            Connection connect = DriverManager.getConnection(MSSQL_DB, MSSQL_LOGIN, MSSQL_PASS);
//            return connect;
//        } catch (ClassNotFoundException e) {
//            Log.d("!ClassNotFoundException", e.toString());
//            e.printStackTrace();
//        } catch (SQLException e) {
//            Log.d("!ConnectionException", e.toString());
//            e.printStackTrace();
//        }
//        return null;
//    }
//    @Override
//    public Loader<ArrayList<String> > onCreateLoader(final int id, final Bundle args) {
//        return new AsyncTaskLoader<ArrayList<String> >(this) {
//            @Override
//            public void onStartLoading() {
//                Log.d("!StartLoader: ", String.valueOf(id));
//                if (args==null) return;
//                forceLoad();
//
//            }
//
//            @Override
//            public ArrayList<String>  loadInBackground() {
//                Log.d("!loadInBackground","kjhgf");
//                switch (id) {
//                    case LOADER_DORM: {
//                        ResultSet dormitories = null;
//                        ArrayList<String>  dormitoriesName = new ArrayList<>();
//                        //similar to doInBackground of AsyncTask
//                        Connection connect = getConnection();
//                        if (connect != null) {
//                            Log.d("!Success !", "connect");
//                            Statement st = null;
//                            try {
//                                st = connect.createStatement();
//                                dormitories = st.executeQuery("SELECT namedorm_id FROM [dormitory] ORDER BY number");
//                                int i=0;
//                                while (dormitories.next()) {
//                                    dormitoriesName.add( dormitories.getString("namedorm_id"));
//                                    Log.d("!namedorm"+String.valueOf(i), dormitories.getString("namedorm_id"));
//                                    i++;
//                                }
//                            } catch (SQLException e) {
//                                e.printStackTrace();
//                            }
//
//                            try {
//                                if (dormitories != null) dormitories.close();
//                                if (st != null) st.close();
//                                if (connect != null) connect.close();
//                            } catch (SQLException e) {
//                                throw new RuntimeException(e.getMessage());
//
//                            }
//                        }
//                        return dormitoriesName;
//                    }
//                    case LOADER_ROOM:{
//                        ResultSet rooms = null;
//                        ArrayList<String> roomsName = new ArrayList<>();
//
//                        Connection connect = getConnection();
//                        if (connect != null) {
//                            Log.d("!Success rooms!", "connect");
//                            Statement st = null;
//                            try {
//                                st = connect.createStatement();
//                                rooms = st.executeQuery("SELECT room_id, roomnumber FROM [room] WHERE fk_dorm = '" + String.valueOf(args.getString("DormitoryName") + "' ORDER BY roomnumber"));
//                             while (rooms.next()) {
//                                 roomsName.add(String.valueOf(rooms.getInt("roomnumber")));
//                             }
//                            } catch (SQLException e) {
//                                e.printStackTrace();
//                            }
//
//                            try {
//                                if (rooms != null) rooms.close();
//                                if (st != null) st.close();
//                                connect.close();
//                            } catch (SQLException e) {
//                                throw new RuntimeException(e.getMessage());
//                            }
//                        }
//                        return roomsName;
//                    }
//                    case LOADER_REGISTRATION: {
//                        String successResult = "success";
//                        ArrayList<String> success = new ArrayList<>();
//                        ResultSet rooms_id = null;
//                        Connection connect = getConnection();
//                        if (connect != null) {
//                            Statement st = null;
//                            try {
//                                st = connect.createStatement();
//                                String query = "SELECT userreader_id FROM [userreader] WHERE userlogin = '" + args.getString("login") + "'";
//                                ResultSet existUser =  st.executeQuery(query);
//                                if (existUser.next()) {
//                                    successResult = "alreadyExist";
//                                    success.add(successResult);
//                                    return success;
//                                }
//                                query = "SELECT room_id  FROM [room] WHERE fk_dorm = '" + args.getString("DormitoryName") + "' AND roomnumber=" + args.getString("RoomNumber");
//                                Log.d("!query", query);
//                                rooms_id = st.executeQuery(query);
//                                if (rooms_id.next()) {
//                                    st.executeUpdate("INSERT INTO [userreader] (" +
//                                            "[userfirstname], " +
//                                            "[usersecondname], " +
//                                            "[usersurname], " +
//                                            "[fk_room], " +
//                                            "[phonenumber], " +
//                                            "[userlogin], " +
//                                            "[userpassword], " +
//                                            "[fk_dorm])" +
//                                            " VALUES ("
//                                            +"'"+ args.getString("fistName") + "', "
//                                            +"'"+ args.getString("secondName") + "', "
//                                            +"'"+ args.getString("surName") + "', "
//                                            + String.valueOf(rooms_id.getInt("room_id")) + ", "
//                                            +"'"+ args.getString("phone") + "', "
//                                            +"'"+ args.getString("login") + "', "
//                                            + "'"+args.getString("password") + "', "
//                                            +"'"+ args.getString("DormitoryName") + "')"
//                                    );
//                                }
//                            } catch (SQLException e) {
//                                Log.d("!InsertException", e.toString());
//                                successResult = "errorInsert";
//                                e.printStackTrace();
//                            }
//                            success.add(successResult);
//                            return success;
//
//                        }
//                    }
//                }
//                return null;
//            }
//        };
//    }
//
//
//    @Override
//    public void onLoadFinished(Loader<ArrayList<String> > loader, ArrayList<String>  data) {
//        //Log.d("data ", data.toString());
//
//        switch (loader.getId()) {
//            case LOADER_DORM:{
//                ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, data);
//                // Определяем разметку для использования при выборе элемента
//                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//                // Применяем адаптер к элементу spinner
//                dormList.setAdapter(adapter);
//                dormList.setOnItemSelectedListener(itemSelectedListenerDormitory);
//                break;
//            }
//            case LOADER_ROOM:{
//                ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, data);
//                // Определяем разметку для использования при выборе элемента
//                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//                // Применяем адаптер к элементу spinner
//                roomList.setAdapter(adapter);
//                roomList.setOnItemSelectedListener(itemSelectedListenerRoom);
//                break;
//            }
//            case LOADER_REGISTRATION:{
//                // Выполняем переход на другой экран:
//                String result = data.get(0);
//                if (result == "alreadyExist"){
//                    errorMessage.setText("Извините, логин уже занят");
//                    errorMessage.setVisibility(View.VISIBLE);
//                    break;
//                }
//                if (result == "errorInsert"){
//                    errorMessage.setText("Что-то пошло не так, обратитесь к администратору");
//                    errorMessage.setVisibility(View.VISIBLE);
//                    break;
//                }
//
//                Intent intent = new Intent(RegistrationActivity.this, MenuLibrary.class);
//                startActivity(intent);
//                break;
//            }
//        }
//
//
//    }
//
//    @Override
//    public void onLoaderReset(Loader<ArrayList<String> > loader) {
//        Log.d("!onLoaderReset","kjhgf");
//
//    }

    private class RequestResultReceiver extends ResultReceiver {

        public RequestResultReceiver(Handler handler) {
            super(handler);
        }

        private ArrayAdapter adapter = null;

        public void setArgs(ArrayAdapter adapter) {
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


}
