package com.example.user.library;

import android.app.IntentService;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class DbService extends IntentService {

    public static final int REQUEST_SUCCESS = 2;
    public static final int REQUEST_ERROR = 3;

    static final String SETTINGS_FILE = "settings.config";
    static String MSSQL_DB = "jdbc:jtds:sqlserver://dertosh.ddns.net:49173;databaseName=library";
    static String MSSQL_LOGIN = "ReadingUser";
    static String MSSQL_PASS = "Reading1234";

    final String LOG_TAG = "DbService";

    private volatile Connection con = null;
    private Statement st = null;

    private AsyncTask<String, Integer, Void> exec = null;

    public DbService() {
        super(DbService.class.getName());

    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "onCreate");

        readSettings();

        exec = new AsyncRequest().execute();

//        try {
//            new AsyncRequest().execute().get();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } catch (ExecutionException e) {
//            e.printStackTrace();
//        }

    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.d(LOG_TAG, "onHandleIntent");
        String query = intent != null ? intent.getStringExtra("request") : null;
        final ResultReceiver receiver = intent.getParcelableExtra("receiver");
        Bundle bundle = new Bundle();

        JSONArray resultSet = new JSONArray();
        ResultSet rs = null;
        try {
            try {
                exec.get(2, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                e.printStackTrace();
            }
//            if(con==null)
//            {
//                try {
//                    Thread.sleep(200);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
            if (con != null) {
                rs = st.executeQuery(query);
                if (rs != null) {
                    int columnCount = rs.getMetaData().getColumnCount();
                    // Сохранение данных в JSONArray
                    while (rs.next()) {
                        JSONObject rowObject = new JSONObject();
                        for (int i = 1; i <= columnCount; i++) {
                            rowObject.put(rs.getMetaData().getColumnName(i), (rs.getString(i) != null) ? rs.getString(i) : "");
                        }
                        resultSet.put(rowObject);
                    }
                    Log.d(LOG_TAG, "SQL: " + resultSet.toString());
                    bundle.putSerializable("JSONString", resultSet.toString());
                    receiver.send(REQUEST_SUCCESS, bundle);
                }
            } else {
                bundle.putSerializable("SQLException", "no connection");
                receiver.send(REQUEST_ERROR, bundle);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            bundle.putSerializable("SQLException", e.getLocalizedMessage());
            receiver.send(REQUEST_ERROR, bundle);
        } catch (JSONException e) {
            e.printStackTrace();
            receiver.send(REQUEST_ERROR, Bundle.EMPTY);
        } finally {
            try {
                if (rs != null) rs.close();
            } catch (SQLException e) {
                throw new RuntimeException(e.getMessage());
            }
        }
    }

    @Override
    public void onDestroy() {
        try {
            if (st != null) st.close();
            if (con != null) con.close();
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private void readSettings() {
        FileInputStream file = null;
        byte[] settings = new byte[100];
        try {
            file = openFileInput(SETTINGS_FILE);
            file.read(settings);

        } catch (FileNotFoundException e) {
            writeSettings();
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (file != null)
                    file.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Log.d(LOG_TAG, "readSettings: " + settings.toString());
    }

    private void writeSettings() {
        File file = new File(this.getApplicationContext().getFilesDir(), SETTINGS_FILE);
        FileWriter fileWriter = null;
        BufferedWriter bufferedWriter = null;
        String settings = "MSSQL_DB : jdbc:jtds:sqlserver://dertosh.ddns.net:49173;databaseName=library";
        try {
            fileWriter = new FileWriter(file);
            bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(settings);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    class AsyncRequest extends AsyncTask<String, Integer, Void> {

        @Override
        protected Void doInBackground(String... arg) {
            try {
                Class.forName("net.sourceforge.jtds.jdbc.Driver");
                try {
                    con = DriverManager.getConnection(MSSQL_DB, MSSQL_LOGIN, MSSQL_PASS);
                    if (con != null) {
                        st = con.createStatement();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            return null;
        }

    }
}
