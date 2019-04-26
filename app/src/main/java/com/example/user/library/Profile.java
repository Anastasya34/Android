package com.example.user.library;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.app.Fragment;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TableRow;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDate;
import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Profile.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Profile#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Profile extends Fragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    public static final String ARG_USER_ID = "user_id";
    public static final String ARG_EDITABLE = "editable";

    private DatePickerDialog datePickerDialog;

    private Intent startIntent;
    //private DbService dbService;
    private RequestResultReceiver requestResultReceiver;
    private UpdateResultReceiver updateResultReceiver;
    private RequestResultRoomReceiver requestResultRoomReceiver;

    private OnFragmentInteractionListener mListener;

    private int mUser_id = -1;
    private boolean mEditable = false;

    private EditText firstName;
    private EditText secondName;
    private EditText surName;
    private EditText birthday;
    private EditText room;
    private EditText phone;
    private EditText email;
    private EditText login;
    private EditText password;
    private TableRow password_row;
    private ArrayList<EditText> listProfile = new ArrayList<>();
    private String user_password;
    private TextView status;
    private Spinner dormList;
    private ArrayAdapter<String> dormAdapter;

    private Button saveEditButton;
    private String selectedDormitory;

    private TextView item;

    public Profile() {
        // Required empty public constructor
        requestResultReceiver = new RequestResultReceiver(new Handler());
        updateResultReceiver = new UpdateResultReceiver(new Handler());
        requestResultRoomReceiver = new RequestResultRoomReceiver(new Handler());
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @param user_id user id.
     * @param editable make profile editable
     * @return A new instance of fragment Profile.
     */
    // TODO: Rename and change types and number of parameters
    public static Profile newInstance(int user_id, boolean editable) {
        Profile fragment = new Profile();
        Bundle args = new Bundle();
        args.putInt(ARG_USER_ID, user_id);
        args.putBoolean(ARG_EDITABLE, editable);
        fragment.setArguments(args);
        return fragment;
    }

    public static Profile newInstance(int user_id) {
        return newInstance(user_id, true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        startIntent = new Intent(getActivity(), DbService.class);
        datePickerDialog = new DatePickerDialog(
                getContext(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                String date = day + "." + (month + 1) + "." + year;
                birthday.setText(date);
            }
        }, 1990, 1, 1);

        if (getArguments() != null) {
            mUser_id = getArguments().getInt(ARG_USER_ID);
            mEditable = getArguments().getBoolean(ARG_EDITABLE, true);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        //интерфейс
        Switch editModeSwitch = view.findViewById(R.id.editMode_switch);
        if (!mEditable) editModeSwitch.setVisibility(View.INVISIBLE);
        saveEditButton = view.findViewById(R.id.saveEdit_button);

        saveEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                setEditTextMasState(true);
                status.setTextColor(Color.BLACK);
                status.setText("Сохранение ...");
                status.setVisibility(View.VISIBLE);

                if (!password.getText().toString().equals(user_password)) {
                    status.setText("Неверный пароль!");
                    status.setTextColor(Color.RED);
                    status.setVisibility(View.VISIBLE);
                    return;
                }

                startIntent.putExtra("receiver", updateResultReceiver);
                startIntent.putExtra("type", "update");
                startIntent.putExtra("request",
                        "UPDATE [userreader] SET " +
                                "[userfirstname]=\'" + firstName.getText() +
                                "\',[usersecondname]=\'" + secondName.getText() +
                                "\',[usersurname]=\'" + surName.getText() +
                                "\',[birthday]=\'" + birthday.getText() +
                                "\',[email]=\'" + email.getText() +
                                "\',[phonenumber]=\'" + phone.getText() +
                                "\',[userlogin]=\'" + login.getText() +
                                "\'WHERE [userreader_id] = " + mUser_id + ";");
                getActivity().startService(startIntent);
            }
        });

        password = view.findViewById(R.id.password_edit);
        password_row = view.findViewById(R.id.password_Row);

        firstName = view.findViewById(R.id.firstName_edit);
        listProfile.add(firstName);
        secondName = view.findViewById(R.id.secondName_edit);
        listProfile.add(secondName);
        surName = view.findViewById(R.id.surName_edit);
        listProfile.add(surName);
        birthday = view.findViewById(R.id.birthday_edit);
        listProfile.add(birthday);
        room = view.findViewById(R.id.room_edit);
        listProfile.add(room);
        phone = view.findViewById(R.id.phone_edit);
        listProfile.add(phone);
        email = view.findViewById(R.id.email_edit);
        listProfile.add(email);
        login = view.findViewById(R.id.login_edit);
        listProfile.add(login);

        birthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                datePickerDialog.show();
            }
        });

        status = view.findViewById(R.id.status_View);

        dormList = view.findViewById(R.id.dormitories_spinner);
        dormList.setEnabled(false);
        AdapterRequestResultReceiver adapterRequestResultReceiver = new AdapterRequestResultReceiver(new Handler());

        dormAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, new ArrayList<String>());
        // Определяем разметку для использования при выборе элемента
        dormAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Применяем адаптер к элементу spinner
        dormList.setAdapter(dormAdapter);
        dormList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Получаем выбранный объект
                selectedDormitory = (String) parent.getItemAtPosition(position);

                startIntent.putExtra("receiver", requestResultRoomReceiver);
                startIntent.putExtra("type", "select");
                startIntent.putExtra("request", "SELECT MAX([roomnumber]) AS maxRoom " +
                        " FROM [room] WHERE [fk_dorm] = '" + selectedDormitory + "';");
                getActivity().startService(startIntent);

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
        getActivity().startService(startIntent);

        setEditTextMasState(false);


        editModeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {

                    setEditTextMasState(true);
                    //password_row
                    password_row.setVisibility(View.VISIBLE);
                    //save button
                    saveEditButton.setVisibility(View.VISIBLE);
                    dormList.setEnabled(true);
                    birthday.setFocusable(false);

                } else {
                    setEditTextMasState(false);
                    //password_row
                    password_row.setVisibility(View.INVISIBLE);
                    //save button
                    saveEditButton.setVisibility(View.INVISIBLE);
                    status.setVisibility(View.INVISIBLE);
                    dormList.setEnabled(false);
                    //TextView v = (TextView) dormList.getSelectedItem();
                    //v.setTextColor(Color.BLACK);
                }
            }
        });

        startIntent.putExtra("receiver", requestResultReceiver);
        startIntent.putExtra("type", "select");
        startIntent.putExtra("request", "SELECT * FROM [userreader] WHERE userreader_id = " + String.valueOf(mUser_id) + ";");
        getActivity().startService(startIntent);


        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private class RequestResultReceiver extends ResultReceiver {

        RequestResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            switch (resultCode) {
                case DbService.REQUEST_ERROR:
                    Log.d("data", resultData.getString("SQLException"));

                    //dbService.resetConnection();
                    break;

                case DbService.REQUEST_SUCCESS:
                    String jsonString = resultData.getString("JSONString");
                    try {
                        JSONArray resultSet = new JSONArray(jsonString);
                        if (resultSet.length() == 0) {
                            Log.d("data", "Неверный логин или пароль");
                            break;
                        }
                        JSONObject rec = resultSet.getJSONObject(0);
                        firstName.setText(rec.getString("userfirstname"));
                        secondName.setText(rec.getString("usersecondname"));
                        surName.setText(rec.getString("usersurname"));

                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            LocalDate date = LocalDate.parse(rec.getString("birthday"));
                            String dateString = String.valueOf(date.getDayOfMonth()) + "." + String.valueOf(date.getMonthValue() + 1) + "." + String.valueOf(date.getYear());
                            birthday.setText(dateString);
                            Log.d("data", String.valueOf(date.getYear()));
                            datePickerDialog.getDatePicker().updateDate(date.getYear(), date.getMonthValue(), date.getDayOfMonth());
                        }

                        phone.setText(rec.getString("phonenumber"));
                        email.setText(rec.getString("email"));
                        login.setText(rec.getString("userlogin"));
                        user_password = rec.getString("userpassword"); //TODO: нормальную проверку

                        dormList.setSelection(dormAdapter.getPosition(rec.getString("fk_dorm")));

                        startIntent.putExtra("receiver", new RequestResultIntReceiver(new Handler()));
                        startIntent.putExtra("type", "select");
                        startIntent.putExtra("request", "SELECT roomnumber FROM [room] WHERE room_id = " + rec.getString("fk_room"));
                        getActivity().startService(startIntent);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    break;
            }
            super.onReceiveResult(resultCode, resultData);
        }

    }

    private class UpdateResultReceiver extends ResultReceiver {

        UpdateResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            switch (resultCode) {
                case DbService.REQUEST_ERROR:
                    Log.d("data", resultData.getString("SQLException"));

                    //dbService.resetConnection();
                    break;

                case DbService.REQUEST_SUCCESS:
                    int n_strings = resultData.getInt("n_strings");
                    if (n_strings > 0) {
                        Log.d("data", "успех " + String.valueOf(n_strings));
                        status.setTextColor(Color.GREEN);
                        status.setText("Профиль обновлен!");
                        status.setVisibility(View.VISIBLE);
                    } else {
                        Log.d("data", "ошибка " + String.valueOf(n_strings));
                        status.setTextColor(Color.RED);
                        status.setText("Профиль не обновлен!");
                        status.setVisibility(View.VISIBLE);
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
                        } else {
                            room.setText(String.valueOf(resultSet.getJSONObject(0).getInt("roomnumber")));
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
                            room.setFilters(new InputFilter[]{new RegistrationActivity.InputFilterMinMax(1, roomsMax)});
                            room.setText(room.getText());
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


    private void setEditTextMasState(boolean state) {
        for (EditText editText : listProfile) {
            setEditTextState(editText, state);
        }
    }

    private void setEditTextState(EditText editText, boolean state) {
        editText.setEnabled(state);
        if (!state) editText.setTextColor(Color.BLACK);
    }

}