package com.example.user.library;

import android.app.IntentService;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

    final static String SETTINGS_FILE = "settings.json";
    private static String MSSQL_DB = "jdbc:jtds:sqlserver://ASUS;databaseName=library;integratedSecurity=true";
    private static String MSSQL_LOGIN = "AllowUser";
    private static String MSSQL_PASS = "AllowUser";

    final String LOG_TAG = "DbService";

    private volatile Connection con = null;
    private Statement st = null;

    private AsyncTask<String, Void, Connection> exec = null;

    private final IBinder mBinder = new DbBinder();

    public DbService() {
        super(DbService.class.getName());

    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "onCreate");

        readSettings();

        exec = new AsyncRequest().execute();

    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.d(LOG_TAG, "onHandleIntent");
        if (intent == null)
            return;
        try {
            if ((con == null) || con.isClosed()) resetConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            resetConnection();
        }

        String query = intent.getStringExtra("request");
        String type = intent.getStringExtra("type");
        final ResultReceiver receiver = intent.getParcelableExtra("receiver");
        Bundle bundle = new Bundle();

        JSONArray resultSet = new JSONArray();
        ResultSet rs = null;
        try {
            try {
                con = exec.get(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
                resetConnection();
            } catch (ExecutionException e) {
                e.printStackTrace();
                resetConnection();
            } catch (TimeoutException e) {
                e.printStackTrace();
                resetConnection();
            }
            if (con != null) {
                st = con.createStatement();
                if (type != null && type.equals("update")) {
                    int n_strings = st.executeUpdate(query);
                    Log.d(LOG_TAG, "SQL: number of strings:" + n_strings);
                    bundle.putSerializable("n_strings", n_strings);
                    receiver.send(REQUEST_SUCCESS, bundle);

                } else {
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
                }
            } else {
                bundle.putSerializable("SQLException", "no connection");
                receiver.send(REQUEST_ERROR, bundle);
                resetConnection();
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
                e.printStackTrace();
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

        String filePath = getExternalFilesDir(null) + "/" + SETTINGS_FILE;
        File getFilePath = new File(filePath);
        FileInputStream file = null;
        byte[] settings = new byte[200];
        try {
            file = new FileInputStream(getFilePath);
            file.read(settings);
            String text = new String(settings);
            JSONObject obj = new JSONObject(text);

            MSSQL_DB = obj.get("MSSQL_DB") + ";Connect Timeout=3";
            MSSQL_LOGIN = (String) obj.get("MSSQL_LOGIN");
            MSSQL_PASS = (String) obj.get("MSSQL_PASS");
            Log.d(LOG_TAG, "readSettings: " + "MSSQL_DB: " + MSSQL_DB);

        } catch (FileNotFoundException e) {
            writeSettings();
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (file != null) {
                    file.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void writeSettings() {
        File file = new File(this.getApplicationContext().getExternalFilesDir(null), SETTINGS_FILE);


        JSONObject object = new JSONObject();
        try {
            object.put("MSSQL_DB", MSSQL_DB);
            object.put("MSSQL_LOGIN", MSSQL_LOGIN);
            object.put("MSSQL_PASS", MSSQL_PASS);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        FileWriter fileWriter = null;

        try {

            fileWriter = new FileWriter(file);
            fileWriter.write(object.toString());

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileWriter != null) {
                    fileWriter.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    static class AsyncRequest extends AsyncTask<String, Void, Connection> {

        @Override
        protected Connection doInBackground(String... arg) {
            Connection con = null;
            //do {
            try {
                Class.forName("net.sourceforge.jtds.jdbc.Driver");
                try {
                    DriverManager.setLoginTimeout(3);
                    con = DriverManager.getConnection(MSSQL_DB, MSSQL_LOGIN, MSSQL_PASS);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            //}while (con == null);
            return con;
        }

    }

    public void resetConnection() {
        if (exec.getStatus() == AsyncTask.Status.FINISHED) exec = new AsyncRequest().execute();
    }

    public class DbBinder extends Binder {
        DbService getService() {
            return DbService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
}
