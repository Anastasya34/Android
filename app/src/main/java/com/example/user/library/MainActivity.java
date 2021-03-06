package com.example.user.library;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
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
    private TextView loginLocked;

    private Intent startIntent;
    private ServiceConnection sConn;
    private DbService dbService;
    private RequestResultReceiver requestResultReceiver;
    private boolean bound = false;
    private boolean userLoginFlag = false;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences sharedPreferences = getSharedPreferences("Login", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        // Связываемся с элементами нашего интерфейса:
        username = (EditText) findViewById(R.id.edit_user);
        password = (EditText) findViewById(R.id.edit_password);
        loginLocked = (TextView) findViewById(R.id.login_locked);

        Log.i("onCreate!", "loader");

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

        int id = sharedPreferences.getInt("id", 0);
        if (id > 0) {
            if (sharedPreferences.getBoolean("user", true)) {
                Log.d("userreader_id", String.valueOf(id));
                Intent intent = new Intent(MainActivity.this, MenuLibrary.class);
                intent.putExtra("user_id", id);
                startActivity(intent);
            } else {
                Log.d("adminId", String.valueOf(id));
                Intent intent = new Intent(MainActivity.this, AdminContent.class);
                intent.putExtra(Constants.ADMIN_ID, id);
                startActivity(intent);
            }
        }
    }


    // Обрабатываем нажатие кнопки "Войти":
    public void Login(View view) {
        Log.d("Main", "Login");
        loginLocked.setVisibility(View.INVISIBLE);

        if (username.getText().toString().isEmpty() || password.getText().toString().isEmpty()) {
            loginLocked.setText("Все поля должны быть заполнены!");
            loginLocked.setTextColor(Color.RED);
            loginLocked.setVisibility(View.VISIBLE);
        } else {
            loginLocked.setText("Авторизация...");
            loginLocked.setTextColor(Color.BLACK);
            loginLocked.setVisibility(View.VISIBLE);

            userLoginFlag = false;

            startIntent.putExtra("receiver", requestResultReceiver);
            startIntent.putExtra("request", "SELECT * FROM [userreader] " +
                    "WHERE userlogin = '" + username.getText().toString() + "' AND userpassword = '" + password.getText().toString() + "'");
            startService(startIntent);
        }


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
                            if (userLoginFlag) {
                                Log.d("data", "Неверный логин или пароль");
                                loginLocked.setText("Неверный логин или пароль");
                                loginLocked.setVisibility(View.VISIBLE);
                            } else {
                                userLoginFlag = true;
                                startIntent.putExtra("receiver", requestResultReceiver);
                                startIntent.putExtra("request", "SELECT admin_id FROM [administration] " +
                                        "WHERE adminlogin = '" + username.getText().toString() + "' AND adminpassword = '" + password.getText().toString() + "'");
                                startService(startIntent);
                            }
                        } else {
                            loginLocked.setText("Авторизация успешна");
                            loginLocked.setTextColor(Color.GREEN);
                            loginLocked.setVisibility(View.VISIBLE);

                            JSONObject rec = resultSet.getJSONObject(0);
                            if (userLoginFlag) {
                                userLoginFlag = false;
                                int adminId = rec.getInt("admin_id");
                                editor.putBoolean("user", false);
                                editor.putInt("id", adminId);
                                editor.apply();
                                Log.d("adminId",String.valueOf(adminId));
                                Intent intent = new Intent(MainActivity.this, AdminContent.class);
                                intent.putExtra(Constants.ADMIN_ID, adminId);
                                startActivity(intent);
                            } else {
                                int userreader_id = rec.getInt("userreader_id");
                                editor.putBoolean("user", true);
                                editor.putInt("id", userreader_id);
                                editor.apply();
                                Intent intent = new Intent(MainActivity.this, MenuLibrary.class);
                                intent.putExtra("user_id", userreader_id);
                                startActivity(intent);
                                Log.d("data", resultData.getString("JSONString"));
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

