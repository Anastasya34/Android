package com.example.user.library;

import android.content.Context;
import android.content.Intent;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


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
    private DbService dbService;
    private RequestResultReceiver requestResultReceiver;

    private OnFragmentInteractionListener mListener;

    private int mUser_id = -1;

    private Switch editModeSwitch;
    private EditText firstName;
    private Button saveEditButton;


    public Profile() {
        // Required empty public constructor
        requestResultReceiver = new RequestResultReceiver(new Handler());
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

        firstName = view.findViewById(R.id.firstName_edit);
        firstName.setFocusable(false);
        firstName.setFocusableInTouchMode(false);
        firstName.setClickable(false);

        editModeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    firstName.setFocusable(true);
                    firstName.setFocusableInTouchMode(true);
                    firstName.setClickable(true);
                    saveEditButton.setVisibility(View.VISIBLE);
                } else {
                    firstName.setFocusable(false);
                    firstName.setFocusableInTouchMode(false);
                    firstName.setClickable(false);
                    saveEditButton.setVisibility(View.INVISIBLE);
                }
            }
        });

        startIntent.putExtra("receiver", requestResultReceiver);
        startIntent.putExtra("request", "SELECT * FROM [library].[dbo].[userreader] WHERE userreader_id = " + String.valueOf(mUser_id) + ";");
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

                    dbService.resetConnection();
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
                        String firstName_str = rec.getString("userfirstname");
                        firstName.setText(firstName_str);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    break;
            }
            super.onReceiveResult(resultCode, resultData);
        }

    }

}
