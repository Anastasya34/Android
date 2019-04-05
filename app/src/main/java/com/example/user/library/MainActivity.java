package com.example.user.library;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    final String LOG_TAG = "Library";

    // Объявляем об использовании следующих объектов:
    private EditText username;
    private EditText password;
    private Button login;
    private TextView loginLocked;

    private Intent startIntent;
    private ServiceConnection sConn;
    private DbService dbService;
    private RequestResultReceiver requestResultReceiver;
    private boolean bound = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Связываемся с элементами нашего интерфейса:
        username = (EditText) findViewById(R.id.edit_user);
        password = (EditText) findViewById(R.id.edit_password);
        login = (Button) findViewById(R.id.button_login);
        loginLocked = (TextView) findViewById(R.id.login_locked);
        //Bundle asyncTaskLoaderParams = new Bundle();
        Log.i("onCreate!", "loader");
        //LoaderManager loaderManager = getSupportLoaderManager();
        //Loader<String> loader = loaderManager.getLoader(LOADER_ID);
        //loaderManager.initLoader(LOADER_ID, asyncTaskLoaderParams,this);
        requestResultReceiver = new RequestResultReceiver(new Handler());
        startIntent = new Intent(MainActivity.this, DbService.class);

        sConn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                Log.d(LOG_TAG, "MainActivity onServiceConnected");
                DbService.DbBinder binder = (DbService.DbBinder) iBinder;
                dbService = binder.getService();
                bound = true;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.d(LOG_TAG, "MainActivity onServiceDisconnected");
                bound = false;
            }
        };

        if (!bound) bindService(startIntent, sConn, BIND_AUTO_CREATE);
        }


    // Обрабатываем нажатие кнопки "Войти":
    public void Login(View view) {
        Log.d("Main", "Login");
//        Bundle asyncTaskLoaderParams = new Bundle();
//        asyncTaskLoaderParams.putString("Button", "Login");
//        asyncTaskLoaderParams.putString("Password", password.getText().toString());
//        asyncTaskLoaderParams.putString("UserName", username.getText().toString());

        //Intent startIntent = new Intent(MainActivity.this, DbService.class);

        loginLocked.setText("Авторизация...");
        loginLocked.setTextColor(Color.BLACK);
        loginLocked.setVisibility(View.VISIBLE);

        startIntent.putExtra("receiver", requestResultReceiver);
        startIntent.putExtra("request", "SELECT * FROM [library].[dbo].[userreader] " +
                "WHERE userlogin = '" + username.getText().toString() + "' AND userpassword = '" + password.getText().toString() + "'");
        startService(startIntent);

        //LoaderManager loaderManager = getSupportLoaderManager();
        //loaderManager.restartLoader(LOADER_ID, asyncTaskLoaderParams,this);
    }

    // Обрабатываем нажатие кнопки "Зарегистироваться":
    public void Registration(View view) {
        // Выполняем переход на другой экран:
        Intent intent = new Intent(MainActivity.this, RegistrationActivity.class);
        startActivity(intent);
    }


    private class RequestResultReceiver extends ResultReceiver {

        public RequestResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            loginLocked.setTextColor(Color.RED);
            switch (resultCode) {
                case DbService.REQUEST_ERROR:
                    Log.d("data", resultData.getString("SQLException"));
                    loginLocked.setText("Ошибка подключения");
                    loginLocked.setVisibility(View.VISIBLE);
                    dbService.resetConnection();
                    break;

                case DbService.REQUEST_SUCCESS:
                    String jsonString = resultData.getString("JSONString");
                    try {
                        JSONArray resultSet = new JSONArray(jsonString);
                        if (resultSet.length() == 0) {
                            Log.d("data", "Неверный логин или пароль");
                            loginLocked.setText("Неверный логин или пароль");
                            loginLocked.setVisibility(View.VISIBLE);
                        } else {
                            loginLocked.setText("Авторизация успешна");
                            loginLocked.setTextColor(Color.GREEN);
                            loginLocked.setVisibility(View.VISIBLE);

                            JSONObject rec = resultSet.getJSONObject(0);
                            int userreader_id = rec.getInt("userreader_id");

                            Intent intent = new Intent(MainActivity.this, MenuLibrary.class);
                            intent.putExtra("user_id", userreader_id);
                            startActivity(intent);
                            Log.d("data", resultData.getString("JSONString"));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    break;
            }
            super.onReceiveResult(resultCode, resultData);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bound) {
            unbindService(sConn);
            bound = false;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

}

