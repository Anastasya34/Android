package com.example.user.library;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class RegistrationActivity extends AppCompatActivity { //implements LoaderManager.LoaderCallbacks<ArrayList<String>> {

    private RequestResultRoomReceiver requestResultRoomReceiver;
    private RequestResultCheckReceiver requestResultCheckReceiver;
    private String selectedDormitory;
    //private String selectedRoom;
    private TextView errorMessage;
    private volatile int user_id = -1;
    private volatile String fk_room = "";
    private boolean sendFlag = false;
    private volatile boolean loginExist = true;
    //private volatile boolean emailExist = true;
    private volatile boolean phoneExist = true;
    private EditText login;
    private EditText email;
    private EditText roomValue;
    private EditText birthday;

    private Intent startIntent;
    private UpdateResultReceiver updateResultReceiver;
    private DatePickerDialog datePickerDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startIntent = new Intent(this, DbService.class);

        AdapterRequestResultReceiver adapterRequestResultReceiver = new AdapterRequestResultReceiver(new Handler());

        datePickerDialog = new DatePickerDialog(
                RegistrationActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                String date = String.valueOf(day) + "." + String.valueOf(month + 1) + "." + String.valueOf(year);
                birthday.setText(date);
            }
        }, 1990, 1, 1);





        updateResultReceiver = new UpdateResultReceiver(new Handler());
        requestResultRoomReceiver = new RequestResultRoomReceiver(new Handler());

        setContentView(R.layout.registration_activity);

        birthday = findViewById(R.id.birthday_value);

        birthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                datePickerDialog.show();
            }
        });

        birthday.setFocusable(false);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, new ArrayList<String>());
        // Определяем разметку для использования при выборе элемента
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Применяем адаптер к элементу spinner
        roomValue = findViewById(R.id.room_value);


        Spinner dormList = (Spinner) findViewById(R.id.dormitories);

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

                startIntent.putExtra("receiver", requestResultRoomReceiver);
                startIntent.putExtra("type", "select");
                startIntent.putExtra("request", "SELECT MAX([roomnumber]) AS maxRoom " +
                        " FROM [room] WHERE [fk_dorm] = '" + selectedDormitory + "';");
                startService(startIntent);

                Log.d("item", selectedDormitory);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        adapterRequestResultReceiver.setArgs(dormAdapter);

        startIntent.putExtra("receiver", adapterRequestResultReceiver);
        startIntent.putExtra("type", "select");
        startIntent.putExtra("request", "SELECT namedorm_id FROM [dormitory]");
        startService(startIntent);

        errorMessage = findViewById(R.id.errorMessage);

        login = findViewById(R.id.login_value);
        login.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                user_id = -1;
                errorMessage.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                startIntent.putExtra("receiver", new RequestResultIntReceiver(new Handler()));
                startIntent.putExtra("type", "select");
                startIntent.putExtra("request", "SELECT userreader_id FROM [userreader] WHERE userlogin = '" + editable + "'");
                startService(startIntent);
            }
        });

        requestResultCheckReceiver = new RequestResultCheckReceiver(new Handler());
        requestResultCheckReceiver.errorMSG = "Данный номер телефона уже используется!";
        requestResultCheckReceiver.type = 1;

        EditText phone = findViewById(R.id.phone_value);
        phone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                phoneExist = false;
                startIntent.putExtra("receiver", requestResultCheckReceiver);
                startIntent.putExtra("type", "select");
                startIntent.putExtra("request", "SELECT userreader_id FROM [userreader] WHERE [phonenumber] = '" + editable + "'");
                startService(startIntent);
            }
        });

        email = findViewById(R.id.email_value);
        email.addTextChangedListener(new TextWatcher() {
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

    }
    // Обрабатываем нажатие кнопки "Зарегистрироваться":
    public void LoginReg(View view) {

        EditText fistName =  findViewById(R.id.firstName_value);
        EditText secondName =  findViewById(R.id.secondName_value);
        EditText surName =  findViewById(R.id.surname_value);
        EditText phone =  findViewById(R.id.phone_value);
        EditText email =  findViewById(R.id.email_value);
        EditText password =  findViewById(R.id.password_value);
        EditText room = findViewById(R.id.room_value);

        ArrayList<EditText> editList = new ArrayList<>();
        editList.add(fistName);
        editList.add(secondName);
        editList.add(surName);
        editList.add(phone);
        editList.add(email);
        editList.add(login);
        editList.add(birthday);
        editList.add(password);
        editList.add(room);

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
        String selectedRoom = roomValue.getText().toString();
        String birthdayString = birthday.getText().toString();

        Spinner dormList = findViewById(R.id.dormitories);
        dormList.setEnabled(false);

        sendFlag = true;

        if (user_id > 0) {
            errorMessage.setTextColor(Color.RED);
            errorMessage.setText("Извините, логин уже занят");
            errorMessage.setVisibility(View.VISIBLE);
            sendFlag = false;
        }

        if (sendFlag && phoneExist) {
            errorMessage.setTextColor(Color.RED);
            errorMessage.setText("Данный номер телефона уже используется!");
            errorMessage.setVisibility(View.VISIBLE);
            sendFlag = false;
        }

        if (sendFlag && (
                fistNameString.isEmpty()
                || secondNameString.isEmpty()
                || surNameString.isEmpty()
                || phoneString.isEmpty()
                || emailString.isEmpty()
                || loginString.isEmpty()
                        || selectedRoom.isEmpty()
                        || birthdayString.isEmpty()
                        || passwordString.isEmpty())) {

            errorMessage.setTextColor(Color.RED);
            errorMessage.setText("Все поля должны быть заполнены");
            errorMessage.setVisibility(View.VISIBLE);
            sendFlag = false;
        }
        if (sendFlag) {
            startIntent.putExtra("receiver", new RequestResultIdRoomReceiver(new Handler()));
            startIntent.putExtra("type", "select");
            startIntent.putExtra("request", "SELECT room_id FROM [room] WHERE roomnumber = " + room.getText().toString() + " AND fk_dorm = \'" + selectedDormitory + "\';");
            startService(startIntent);

            new sendInsertTask().execute(fistNameString, secondNameString, surNameString, birthdayString, phoneString, emailString, loginString, passwordString, selectedDormitory);

        }

        for (EditText editText : editList) {
            editText.setEnabled(true);
        }

        dormList.setEnabled(true);
    }

    private class AdapterRequestResultReceiver extends ResultReceiver {

        AdapterRequestResultReceiver(Handler handler) {
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

    private class RequestResultIntReceiver extends ResultReceiver {

        public RequestResultIntReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

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
                            user_id = 0;
                            loginExist = false;

                        } else {
                            user_id = resultSet.getJSONObject(0).getInt("userreader_id");
                            loginExist = true;
                            Log.d("data", "user_id: " + user_id);
                        }

                        if (sendFlag) {
                            sendFlag = false;
                            Intent intent = new Intent(RegistrationActivity.this, MenuLibrary.class);
                            Log.d("reg", String.valueOf(user_id));
                            intent.putExtra("user_id", user_id);
                            startActivity(intent);
                            break;
                        }

                        if (user_id > 0) {
                            errorMessage.setTextColor(Color.RED);
                            errorMessage.setText("Извините, логин уже занят");
                            errorMessage.setVisibility(View.VISIBLE);
                        } else {
                            errorMessage.setTextColor(Color.GREEN);
                            errorMessage.setText("Логин свободен");
                            errorMessage.setVisibility(View.VISIBLE);
                        }

                        break;

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    break;
            }
            super.onReceiveResult(resultCode, resultData);
        }

    }

    private class UpdateResultReceiver extends ResultReceiver {

        private String login;

        UpdateResultReceiver(Handler handler) {
            super(handler);
        }

        public void setLogin(String login) {
            this.login = login;
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            switch (resultCode) {
                case DbService.REQUEST_ERROR:
                    Log.d("data", resultData.getString("SQLException"));
                    errorMessage.setTextColor(Color.RED);
                    errorMessage.setText("Ошибка! Профиль не обновлен!");
                    errorMessage.setVisibility(View.VISIBLE);
                    sendFlag = false;
                    //dbService.resetConnection();
                    break;

                case DbService.REQUEST_SUCCESS:
                    int n_strings = resultData.getInt("n_strings");
                    if (n_strings > 0) {
                        Log.d("data", "успех " + String.valueOf(n_strings));
                        errorMessage.setTextColor(Color.GREEN);
                        errorMessage.setText("Профиль обновлен!");
                        errorMessage.setVisibility(View.VISIBLE);

                        startIntent.putExtra("receiver", new RequestResultIntReceiver(new Handler()));
                        startIntent.putExtra("type", "select");
                        startIntent.putExtra("request", "SELECT userreader_id FROM [userreader] WHERE userlogin = '" + login + "'");
                        startService(startIntent);

                    } else {
                        Log.d("data", "ошибка " + String.valueOf(n_strings));
                        errorMessage.setTextColor(Color.RED);
                        errorMessage.setText("Ошибка! Профиль не обновлен!");
                        errorMessage.setVisibility(View.VISIBLE);
                        sendFlag = false;
                    }
                    break;
            }
            super.onReceiveResult(resultCode, resultData);
        }

    }

    private class RequestResultRoomReceiver extends ResultReceiver {

        public RequestResultRoomReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

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
                        } else {
                            int roomsMax = resultSet.getJSONObject(0).getInt("maxRoom");
                            Log.d("data", "user_id: " + roomsMax);
                            roomValue.setFilters(new InputFilter[]{new InputFilterMinMax(1, roomsMax)});
                            roomValue.setText(roomValue.getText());
                        }

                        break;

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    break;
            }
            super.onReceiveResult(resultCode, resultData);
        }

    }

    private class RequestResultIdRoomReceiver extends ResultReceiver {

        public RequestResultIdRoomReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            switch (resultCode) {
                case DbService.REQUEST_ERROR:
                    Log.d("data", resultData.getString("SQLException"));
                    fk_room = null;
                    break;

                case DbService.REQUEST_SUCCESS:
                    String jsonString = resultData.getString("JSONString");
                    try {
                        JSONArray resultSet = new JSONArray(jsonString);
                        if (resultSet.length() == 0) {
                            Log.d("data", "пусто");
                            fk_room = "";

                        } else {
                            fk_room = String.valueOf(resultSet.getJSONObject(0).getInt("room_id"));
                            Log.d("data", "room_id: " + fk_room);
                        }

                        break;

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    break;
            }
            super.onReceiveResult(resultCode, resultData);
        }

    }

    private class RequestResultCheckReceiver extends ResultReceiver {

        String successMSG;
        String errorMSG;
        int type;

        public RequestResultCheckReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            int userID = 0;

            switch (resultCode) {
                case DbService.REQUEST_ERROR:
                    Log.d("data", resultData.getString("SQLException"));
                    userID = 0;
                    break;

                case DbService.REQUEST_SUCCESS:
                    String jsonString = resultData.getString("JSONString");
                    try {
                        JSONArray resultSet = new JSONArray(jsonString);
                        if (resultSet.length() == 0) {
                            Log.d("data", "пусто");
                            userID = 0;

                        } else {
                            userID = resultSet.getJSONObject(0).getInt("userreader_id");
                            Log.d("data", "userID: " + userID);
                        }

                        if (userID > 0) {
                            errorMessage.setTextColor(Color.RED);
                            errorMessage.setText(errorMSG);
                            errorMessage.setVisibility(View.VISIBLE);
                            switch (type) {
                                case 1:
                                    phoneExist = true;
                                    break;
                            }
                        }//else{
                        //errorMessage.setTextColor(Color.GREEN);
                        //errorMessage.setText(successMSG);
                        //errorMessage.setVisibility(View.VISIBLE);
                        //}

                        break;

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    break;
            }
            super.onReceiveResult(resultCode, resultData);
        }

    }

    private class sendInsertTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {


            do {
                synchronized (fk_room) {
                    try {
                        fk_room.wait(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } while (fk_room.length() == 0);


            sendFlag = true;

            String query = "INSERT INTO [userreader] (" +
                    "[userfirstname], " +
                    "[usersecondname], " +
                    "[usersurname], " +
                    "[birthday], " +
                    "[fk_room], " +
                    "[phonenumber], " +
                    "[email], " +
                    "[userlogin], " +
                    "[userpassword], " +
                    "[fk_dorm])" +
                    " VALUES ("
                    + "\'" + strings[0] + "\', "
                    + "\'" + strings[1] + "\', "
                    + "\'" + strings[2] + "\', "
                    + "\'" + strings[3] + "\', "
                    + fk_room + ", "
                    + "\'" + strings[4] + "\', "
                    + "\'" + strings[5] + "\', "
                    + "\'" + strings[6] + "\', "
                    + "\'" + strings[7] + "\', "
                    + "\'" + strings[8] + "\')";

            Log.d("query", query);

            updateResultReceiver.setLogin(strings[6]);
            startIntent.putExtra("receiver", updateResultReceiver);
            startIntent.putExtra("type", "update");
            startIntent.putExtra("request", query);
            startService(startIntent);
            return null;
        }
    }


    public static class InputFilterMinMax implements InputFilter {

        private int min, max;

        InputFilterMinMax(int min, int max) {
            this.min = min;
            this.max = max;
        }

        public InputFilterMinMax(String min, String max) {
            this.min = Integer.parseInt(min);
            this.max = Integer.parseInt(max);
        }

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            try {
                // Remove the string out of destination that is to be replaced
                String newVal = dest.toString().substring(0, dstart) + dest.toString().substring(dend, dest.toString().length());
                // Add the new string in
                newVal = newVal.substring(0, dstart) + source.toString() + newVal.substring(dstart, newVal.length());
                int input = Integer.parseInt(newVal);
                if (isInRange(min, max, input))
                    return null;
            } catch (NumberFormatException ignored) {
            }
            return "";
        }

        private boolean isInRange(int a, int b, int c) {
            return b > a ? c >= a && c <= b : c >= b && c <= a;
        }
    }

}
