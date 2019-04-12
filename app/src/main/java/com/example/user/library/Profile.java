package com.example.user.library;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TableRow;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    public static final String ARG_USER_ID = "user_id";
    public static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    //private int user_id;
    private String mParam2;

    private Intent startIntent;
    //private DbService dbService;
    private RequestResultReceiver requestResultReceiver;
    private UpdateResultReceiver updateResultReceiver;

    private OnFragmentInteractionListener mListener;

    private int mUser_id = -1;

    //интерфейс
    private Switch editModeSwitch;
    private EditText firstName;
    private EditText secondName;
    private EditText surName;
    private EditText birthday;
    private EditText phone;
    private EditText email;
    private EditText login;
    private EditText password;
    private TableRow password_row;
    private ArrayList<EditText> listProfile = new ArrayList<>();
    private String user_password;
    private TextView status;

    private Button saveEditButton;


    public Profile() {
        // Required empty public constructor
        requestResultReceiver = new RequestResultReceiver(new Handler());
        updateResultReceiver = new UpdateResultReceiver(new Handler());
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param user_id Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Profile.
     */
    // TODO: Rename and change types and number of parameters
    public static Profile newInstance(int user_id, String param2) {
        Profile fragment = new Profile();
        Bundle args = new Bundle();
        args.putInt(ARG_USER_ID, user_id);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        startIntent = new Intent(getActivity(), DbService.class);

        if (getArguments() != null) {
            mUser_id = getArguments().getInt(ARG_USER_ID);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        editModeSwitch = view.findViewById(R.id.editMode_switch);
        saveEditButton = view.findViewById(R.id.saveEdit_button);

        saveEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                setEditTextMasState(true);
                status.setTextColor(Color.BLACK);
                status.setText("Сохранение ...");
                status.setVisibility(View.VISIBLE);

                if (!password.getText().toString().equals(user_password)) {
                    status.setText("Неверный пароль ...");
                    status.setTextColor(Color.RED);
                    status.setVisibility(View.VISIBLE);
                    return;
                }

                startIntent.putExtra("receiver", updateResultReceiver);
                startIntent.putExtra("type", "update");
                startIntent.putExtra("request",
                        "UPDATE [library].[dbo].[userreader] SET " +
                                "[userfirstname]=\'" + firstName.getText() +
                                "\',[usersecondname]=\'" + secondName.getText() +
                                "\',[usersurname]=\'" + surName.getText() +
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
        phone = view.findViewById(R.id.phone_edit);
        listProfile.add(phone);
        email = view.findViewById(R.id.email_edit);
        listProfile.add(email);
        login = view.findViewById(R.id.login_edit);
        listProfile.add(login);

        status = view.findViewById(R.id.status_View);

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
                } else {

                    setEditTextMasState(false);
                    //password_row
                    password_row.setVisibility(View.INVISIBLE);
                    //save button
                    saveEditButton.setVisibility(View.INVISIBLE);
                    status.setVisibility(View.INVISIBLE);

                }
            }
        });

        startIntent.putExtra("receiver", requestResultReceiver);

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

        public RequestResultReceiver(Handler handler) {
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
                        birthday.setText(rec.getString("age")); ////TODO: поменять на дату
                        phone.setText(rec.getString("phonenumber"));
                        //email.setText(rec.getString("userfirstname")); //TODO: добавить в БД
                        login.setText(rec.getString("userlogin"));
                        user_password = rec.getString("userpassword"); //TODO: нормальную проверку

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    break;
            }
            super.onReceiveResult(resultCode, resultData);
        }

    }

    private class UpdateResultReceiver extends ResultReceiver {

        public UpdateResultReceiver(Handler handler) {
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