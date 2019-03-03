package com.example.user.library;

import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MainActivity extends  AppCompatActivity implements LoaderManager.LoaderCallbacks<String> {
    final static String MSSQL_DB = "jdbc:jtds:sqlserver://ASUS;databaseName=library;integratedSecurity=true";
    final static String MSSQL_LOGIN = "AllowUser";
    final static String MSSQL_PASS= "AllowUser";
    private static final int LOADER_ID = 734;
    // Объявляем об использовании следующих объектов:
    private EditText username;
    private EditText password;
    private Button login;
    private TextView loginLocked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Связываемся с элементами нашего интерфейса:
        username = (EditText) findViewById(R.id.edit_user);
        password = (EditText) findViewById(R.id.edit_password);
        login = (Button) findViewById(R.id.button_login);
        loginLocked = (TextView) findViewById(R.id.login_locked);
        Bundle asyncTaskLoaderParams = new Bundle();
        Log.i("onCreate!", "loader");
        LoaderManager loaderManager = getSupportLoaderManager();
        //Loader<String> loader = loaderManager.getLoader(LOADER_ID);
        loaderManager.initLoader(LOADER_ID, asyncTaskLoaderParams,this);

        }

    // Обрабатываем нажатие кнопки "Войти":
    public void Login(View view) {

        Bundle asyncTaskLoaderParams = new Bundle();
        asyncTaskLoaderParams.putString("Button", "Login");
        asyncTaskLoaderParams.putString("Password", password.getText().toString());
        asyncTaskLoaderParams.putString("UserName", username.getText().toString());

        LoaderManager loaderManager = getSupportLoaderManager();
        loaderManager.restartLoader(LOADER_ID, asyncTaskLoaderParams,this);
    }

    // Обрабатываем нажатие кнопки "Зарегистироваться":
    public void Registration(View view) {
        // Выполняем переход на другой экран:
        Intent intent = new Intent(MainActivity.this, RegistrationActivity.class);
        startActivity(intent);
    }

    @Override
    public Loader<String> onCreateLoader(int id,final Bundle args) {
        return new AsyncTaskLoader<String>(this) {
            @Override
            public void onStartLoading() {
                //similar to onPreExecute of AsyncTask
                Log.d("StartLoad!", "start");
                if (args==null) return;
                forceLoad();

            }

            @Override
            public String loadInBackground() {
                Log.d("!loadInBackground","kjhgf");

                //similar to doInBackground of AsyncTask
                JSONArray resultSet = new JSONArray();
                try {
                    Class.forName("net.sourceforge.jtds.jdbc.Driver");
                    Connection con = null;
                    Statement st = null;
                    ResultSet result1 = null;
                    try {
                        con = DriverManager.getConnection(MSSQL_DB, MSSQL_LOGIN, MSSQL_PASS);
                        if (con != null) {
                            Log.d("!Success!", "con");
                            st = con.createStatement();
                            String buttonType =  args.getString("Button");
                            if (buttonType == "Login"){
                                String username = args.getString("UserName");
                                String password = args.getString("Password");
                                result1 = st.executeQuery("SELECT * FROM [library].[dbo].[userreader] " +
                                                            "WHERE userlogin = '"+username+"' AND userpassword = '"+password+"'");
                                Log.d("result", String.valueOf(result1.next()));
                                //Log.d("result", String.valueOf(result1.first()));

                                if (result1.next()){
                                    Intent intent = new Intent(MainActivity.this, LibraryActivity.class);
                                    startActivity(intent);
                                }
                                else {
                                    result1 = st.executeQuery("SELECT * FROM [library].[dbo].[administration] " +
                                            "WHERE adminlogin = '"+username+"' AND adminpassword = '"+password+"'");

                                    if (result1.next()){
                                        Intent intent = new Intent(MainActivity.this, LibraryActivity.class);
                                        startActivity(intent);
                                    }
                                    else {
                                        return "Not Found";
                                        //loginLocked.setVisibility(View.VISIBLE);

                                    }
                                }

                            }
                        }
                    } catch (SQLException e) {
                        Log.d("!Error!", e.toString());

                        e.printStackTrace();
                    } finally {
                        try {
                            if (result1 != null) result1.close();
                            if (st != null) st.close();
                            if (con != null) con.close();
                        } catch (SQLException e) {
                            throw new RuntimeException(e.getMessage());
                        }
                    }
                } catch (ClassNotFoundException e) {
                    Log.d("!ClassNotFoundException", e.toString());

                    e.printStackTrace();
                }
                return "";
            }
        };
    }


    @Override
    public void onLoadFinished(Loader<String> loader, String data) {
        Log.d("data", data);
        if (data == "Not Found"){
            Log.d("data", data);
            loginLocked.setText("Неверный логин или пароль");
            loginLocked.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onLoaderReset(Loader<String> loader) {
        Log.d("!onLoaderReset","kjhgf");

    }

}

