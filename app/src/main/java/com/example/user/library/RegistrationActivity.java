package com.example.user.library;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.AdapterView;
import android.widget.TextView;

public class RegistrationActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<ArrayList<String>> {
    final static String MSSQL_DB = "jdbc:jtds:sqlserver://ASUS;databaseName=library;integratedSecurity=true";
    final static String MSSQL_LOGIN = "AllowUser";
    final static String MSSQL_PASS= "AllowUser";
    private static final int LOADER_DORM = 734;
    private static final int LOADER_ROOM = 2;
    private static final int LOADER_REGISTRATION = 3;
    OnItemSelectedListener itemSelectedListenerDormitory;
    OnItemSelectedListener itemSelectedListenerRoom;
    Spinner dormList;
    Spinner roomList;
    String selectedDormitory;
    String selectedRoom;
    TextView errorMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration_activity);
        dormList = (Spinner) findViewById(R.id.dormitories);
        roomList = (Spinner) findViewById(R.id.rooms);
        errorMessage = findViewById(R.id.errorMessage);
        itemSelectedListenerDormitory = new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Получаем выбранный объект
                selectedDormitory = (String)parent.getItemAtPosition(position);
                Bundle loaderRoomBundleParams = new Bundle();
                loaderRoomBundleParams.putString("DormitoryName", selectedDormitory);
                LoaderManager loaderRoomManager = getSupportLoaderManager();
                loaderRoomManager.restartLoader(LOADER_ROOM, loaderRoomBundleParams,RegistrationActivity.this);
                Log.d("item", selectedDormitory);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };

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

        Bundle asyncTaskLoaderParams = new Bundle();
        LoaderManager loaderManager = getSupportLoaderManager();
        loaderManager.initLoader(LOADER_DORM, asyncTaskLoaderParams,this);



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
            return;
        }
        Bundle loaderRegistrParams = new Bundle();
        loaderRegistrParams.putString("fistName", fistNameString);
        loaderRegistrParams.putString("secondName", secondNameString);
        loaderRegistrParams.putString("surName", surNameString);
        loaderRegistrParams.putString("phone", phoneString);
        loaderRegistrParams.putString("email", emailString);
        loaderRegistrParams.putString("login", loginString);
        loaderRegistrParams.putString("password", passwordString);
        loaderRegistrParams.putString("DormitoryName", selectedDormitory);
        loaderRegistrParams.putString("RoomNumber", selectedRoom);
        LoaderManager loaderManager = getSupportLoaderManager();
        loaderManager.restartLoader(LOADER_REGISTRATION, loaderRegistrParams,this);
}

    public Connection getConnection(){
        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
            Connection connect = DriverManager.getConnection(MSSQL_DB, MSSQL_LOGIN, MSSQL_PASS);
            return connect;
        } catch (ClassNotFoundException e) {
            Log.d("!ClassNotFoundException", e.toString());
            e.printStackTrace();
        } catch (SQLException e) {
            Log.d("!ConnectionException", e.toString());
            e.printStackTrace();
        }
        return null;
    }
    @Override
    public Loader<ArrayList<String> > onCreateLoader(final int id, final Bundle args) {
        return new AsyncTaskLoader<ArrayList<String> >(this) {
            @Override
            public void onStartLoading() {
                Log.d("!StartLoader: ", String.valueOf(id));
                if (args==null) return;
                forceLoad();

            }

            @Override
            public ArrayList<String>  loadInBackground() {
                Log.d("!loadInBackground","kjhgf");
                switch (id) {
                    case LOADER_DORM: {
                        ResultSet dormitories = null;
                        ArrayList<String>  dormitoriesName = new ArrayList<>();
                        //similar to doInBackground of AsyncTask
                        Connection connect = getConnection();
                        if (connect != null) {
                            Log.d("!Success !", "connect");
                            Statement st = null;
                            try {
                                st = connect.createStatement();
                                dormitories = st.executeQuery("SELECT namedorm_id FROM [library].[dbo].[dormitory] ORDER BY number");
                                int i=0;
                                while (dormitories.next()) {
                                    dormitoriesName.add( dormitories.getString("namedorm_id"));
                                    Log.d("!namedorm"+String.valueOf(i), dormitories.getString("namedorm_id"));
                                    i++;
                                }
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }

                            try {
                                if (dormitories != null) dormitories.close();
                                if (st != null) st.close();
                                if (connect != null) connect.close();
                            } catch (SQLException e) {
                                throw new RuntimeException(e.getMessage());

                            }
                        }
                        return dormitoriesName;
                    }
                    case LOADER_ROOM:{
                        ResultSet rooms = null;
                        ArrayList<String> roomsName = new ArrayList<>();

                        Connection connect = getConnection();
                        if (connect != null) {
                            Log.d("!Success rooms!", "connect");
                            Statement st = null;
                            try {
                                st = connect.createStatement();
                                rooms = st.executeQuery("SELECT room_id, roomnumber FROM [library].[dbo].[room] WHERE fk_dorm = '"+String.valueOf(args.getString("DormitoryName")+"' ORDER BY roomnumber"));
                             while (rooms.next()) {
                                 roomsName.add(String.valueOf(rooms.getInt("roomnumber")));
                             }
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }

                            try {
                                if (rooms != null) rooms.close();
                                if (st != null) st.close();
                                connect.close();
                            } catch (SQLException e) {
                                throw new RuntimeException(e.getMessage());
                            }
                        }
                        return roomsName;
                    }
                    case LOADER_REGISTRATION: {
                        String successResult = "success";
                        ArrayList<String> success = new ArrayList<>();
                        ResultSet rooms_id = null;
                        Connection connect = getConnection();
                        if (connect != null) {
                            Statement st = null;
                            try {
                                st = connect.createStatement();
                                String query = "SELECT userreader_id FROM [library].[dbo].[userreader] WHERE userlogin = '"+args.getString("login")+ "'";
                                ResultSet existUser =  st.executeQuery(query);
                                if (existUser.next()) {
                                    successResult = "alreadyExist";
                                    success.add(successResult);
                                    return success;
                                }
                                query = "SELECT room_id  FROM [library].[dbo].[room] WHERE fk_dorm = '"+args.getString("DormitoryName")+"' AND roomnumber="+args.getString("RoomNumber");
                                Log.d("!query", query);
                                rooms_id = st.executeQuery(query);
                                if (rooms_id.next()) {
                                    st.executeUpdate("INSERT INTO [library].[dbo].[userreader] (" +
                                            "[userfirstname], " +
                                            "[usersecondname], " +
                                            "[usersurname], " +
                                            "[fk_room], " +
                                            "[phonenumber], " +
                                            "[userlogin], " +
                                            "[userpassword], " +
                                            "[fk_dorm])" +
                                            " VALUES ("
                                            +"'"+ args.getString("fistName") + "', "
                                            +"'"+ args.getString("secondName") + "', "
                                            +"'"+ args.getString("surName") + "', "
                                            + String.valueOf(rooms_id.getInt("room_id")) + ", "
                                            +"'"+ args.getString("phone") + "', "
                                            +"'"+ args.getString("login") + "', "
                                            + "'"+args.getString("password") + "', "
                                            +"'"+ args.getString("DormitoryName") + "')"
                                    );
                                }
                            } catch (SQLException e) {
                                Log.d("!InsertException", e.toString());
                                successResult = "errorInsert";
                                e.printStackTrace();
                            }
                            success.add(successResult);
                            return success;

                        }
                    }
                }
                return null;
            }
        };
    }


    @Override
    public void onLoadFinished(Loader<ArrayList<String> > loader, ArrayList<String>  data) {
        //Log.d("data ", data.toString());

        switch (loader.getId()) {
            case LOADER_DORM:{
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, data);
                // Определяем разметку для использования при выборе элемента
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                // Применяем адаптер к элементу spinner
                dormList.setAdapter(adapter);
                dormList.setOnItemSelectedListener(itemSelectedListenerDormitory);
                break;
            }
            case LOADER_ROOM:{
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, data);
                // Определяем разметку для использования при выборе элемента
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                // Применяем адаптер к элементу spinner
                roomList.setAdapter(adapter);
                roomList.setOnItemSelectedListener(itemSelectedListenerRoom);
                break;
            }
            case LOADER_REGISTRATION:{
                // Выполняем переход на другой экран:
                String result = data.get(0);
                if (result == "alreadyExist"){
                    errorMessage.setText("Извините, логин уже занят");
                    errorMessage.setVisibility(View.VISIBLE);
                    break;
                }
                if (result == "errorInsert"){
                    errorMessage.setText("Что-то пошло не так, обратитесь к администратору");
                    errorMessage.setVisibility(View.VISIBLE);
                    break;
                }

                Intent intent = new Intent(RegistrationActivity.this, MenuLibrary.class);
                startActivity(intent);
                break;
            }
        }


    }

    @Override
    public void onLoaderReset(Loader<ArrayList<String> > loader) {
        Log.d("!onLoaderReset","kjhgf");

    }

}
