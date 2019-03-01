package com.example.user.library;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.AdapterView;

public class RegistrationActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Map<Integer,String>> {
    final static String MSSQL_DB = "jdbc:jtds:sqlserver://ASUS;databaseName=library;integratedSecurity=true";
    final static String MSSQL_LOGIN = "AllowUser";
    final static String MSSQL_PASS= "AllowUser";
    private static final int LOADER_DORM = 734;
    private static final int LOADER_ROOM = 2;
    OnItemSelectedListener itemSelectedListener;
    Spinner dormList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration_activity);
        dormList = (Spinner) findViewById(R.id.dormitories);
        Spinner roomList = (Spinner) findViewById(R.id.rooms);
        Bundle asyncTaskLoaderParams = new Bundle();
        Log.i("onCreate!", "loader");
        LoaderManager loaderManager = getSupportLoaderManager();
        loaderManager.initLoader(LOADER_DORM, asyncTaskLoaderParams,this);
        itemSelectedListener = new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Получаем выбранный объект
                String dormitory = (String)parent.getItemAtPosition(position);
                Bundle loaderRoomBundleParams = new Bundle();
                loaderRoomBundleParams.putString("DormitoryName", dormitory);
                LoaderManager loaderRoomManager = getSupportLoaderManager();
                loaderRoomManager.restartLoader(LOADER_ROOM, loaderRoomBundleParams,RegistrationActivity.this);
                Log.d("item", dormitory);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };


    }
    // Обрабатываем нажатие кнопки "Войти":
    public void LoginReg(View view) {
        // Выполняем переход на другой экран:
        Intent intent = new Intent(RegistrationActivity.this, LibraryActivity.class);
        startActivity(intent);
    }
    @Override
    public Loader<Map<Integer,String>> onCreateLoader(final int id, final Bundle args) {
        return new AsyncTaskLoader<Map<Integer,String>>(this) {
            @Override
            public void onStartLoading() {
                Log.d("!StartLoader: ", String.valueOf(id));
                if (args==null) return;
                forceLoad();

            }

            @Override
            public Map<Integer,String> loadInBackground() {
                Log.d("!loadInBackground","kjhgf");
                switch (id) {
                    case LOADER_DORM: {
                        ResultSet dormitories = null;
                        Map<Integer, String> dormitoriesName = new HashMap<Integer, String>();
                        //similar to doInBackground of AsyncTask
                        try {
                            Class.forName("net.sourceforge.jtds.jdbc.Driver");
                            Connection connect = null;
                            Statement st = null;
                            ResultSet rooms = null;
                            try {
                                connect = DriverManager.getConnection(MSSQL_DB, MSSQL_LOGIN, MSSQL_PASS);
                                if (connect != null) {
                                    Log.d("!Success !", "connect");
                                    st = connect.createStatement();
                                    dormitories = st.executeQuery("SELECT dorm_id, namedorm FROM [library].[dbo].[dormitory] ");
                                    while (dormitories.next()) {
                                        dormitoriesName.put(dormitories.getInt("dorm_id"), dormitories.getString("namedorm"));
                                    }

                                }
                            } catch (SQLException e) {
                                Log.d("!Error!", e.toString());

                                e.printStackTrace();
                            } finally {
                                try {
                                    if (dormitories != null) dormitories.close();
                                    if (st != null) st.close();
                                    if (connect != null) connect.close();
                                } catch (SQLException e) {
                                    throw new RuntimeException(e.getMessage());
                                }
                            }
                        } catch (ClassNotFoundException e) {
                            Log.d("!ClassNotFoundException", e.toString());
                            e.printStackTrace();
                        }
                        return dormitoriesName;
                    }
                    case LOADER_ROOM:{
                        ResultSet rooms = null;
                        Map<Integer, String> roomsName = new HashMap<Integer, String>();
                        try {
                            Class.forName("net.sourceforge.jtds.jdbc.Driver");
                            Connection connect = null;
                            Statement st = null;
                            try {
                                connect = DriverManager.getConnection(MSSQL_DB, MSSQL_LOGIN, MSSQL_PASS);
                                if (connect != null) {
                                    Log.d("!Success rooms!", "connect");
                                    st = connect.createStatement();
                                    /*rooms = st.executeQuery("SELECT roomnumber FROM [library].[dbo].[room] WHERE fk_dorm = "+String.valueOf());
                                    while (rooms.next()) {
                                        dormitoriesName.put(dormitories.getInt("dorm_id"), dormitories.getString("namedorm"));
                                    }*/

                                }
                            } catch (SQLException e) {
                                Log.d("!Error!", e.toString());

                                e.printStackTrace();
                            } finally {
                                try {
                                    if (rooms != null) rooms.close();
                                    if (st != null) st.close();
                                    if (connect != null) connect.close();
                                } catch (SQLException e) {
                                    throw new RuntimeException(e.getMessage());
                                }
                            }
                        } catch (ClassNotFoundException e) {
                            Log.d("!ClassNotFoundException", e.toString());
                            e.printStackTrace();
                        }
                        return roomsName;
                    }
                }
                return null;
            }
        };
    }


    @Override
    public void onLoadFinished(Loader<Map<Integer,String>> loader, Map<Integer,String> data) {
        Log.d("data ", data.values().toString());
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, new ArrayList<String>(data.values()));
        // Определяем разметку для использования при выборе элемента
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Применяем адаптер к элементу spinner
        switch (loader.getId()) {
            case LOADER_DORM:{
                dormList.setAdapter(adapter);
                dormList.setOnItemSelectedListener(itemSelectedListener);
                break;
            }
        }


    }

    @Override
    public void onLoaderReset(Loader<Map<Integer,String>> loader) {
        Log.d("!onLoaderReset","kjhgf");

    }

}
