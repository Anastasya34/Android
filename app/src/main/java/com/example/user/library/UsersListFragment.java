package com.example.user.library;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class UsersListFragment extends Fragment {
    private View rootView;
    private RecyclerView usersView;
    public static final String BOOK_ID = "book_id";
    public static String book_id;
    UsersListFragment.RequestResultReceiver requestResultReceiver;
    private ArrayList<User> users;
    private UserListAdapter.UserClickListener userClickListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            book_id = getArguments().getString(BOOK_ID);
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_users_list, container, false);
        usersView = rootView.findViewById(R.id.ViewUsers);
        LinearLayoutManager llm = new LinearLayoutManager(rootView.getContext());
        usersView.setLayoutManager(llm);
        usersView.setHasFixedSize(true);
        String query = "SELECT [userreader_id]\n" +
                "      ,[usersurname]\n" +
                "      ,[userfirstname]\n" +
                "      ,[usersecondname]\n" +
                "      ,[phonenumber]\n" +
                "      ,[userlogin]\n" +
                "      ,[email]\n" +
                "  FROM [dbo].[userreader]\n" +
                "  WHERE [userreader_id] IN (SELECT \n" +
                "      [fk_userreader]\n" +
                "  FROM [dbo].[proposal]\n" +
                "  WHERE [book1_id] = '"+book_id+"' AND [bookstatus] = 5)";
        Intent startIntent = new Intent(rootView.getContext(), DbService.class);


        requestResultReceiver = new UsersListFragment.RequestResultReceiver(new Handler());
        startIntent.putExtra("receiver", requestResultReceiver);
        startIntent.putExtra("request", query);
        rootView.getContext().startService(startIntent);
        userClickListener = new UserListAdapter.UserClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                Bundle args = new Bundle();
                args.putInt(Profile.ARG_USER_ID, Integer.valueOf(users.get(position).userId));
                args.putBoolean(Profile.ARG_EDITABLE, false);

                Fragment fragment = null;
                Class fragmentClass = Profile.class;

                if (fragmentClass != null) {

                    try {
                        fragment = (Fragment) fragmentClass.newInstance();
                        fragment.setArguments(args);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.d("Error", e.getMessage());

                    }
                    // Вставляем фрагмент, заменяя текущий фрагмент
                    FragmentManager fragmentManager = getFragmentManager();
                    fragmentManager.beginTransaction().replace(R.id.container, fragment).addToBackStack(this.getClass().getName()).commit();

                }
            }
        };


        return rootView;
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
                    break;

                case DbService.REQUEST_SUCCESS:
                    String jsonString = resultData.getString("JSONString");
                    Log.d("Response", jsonString);
                    try {
                        int bookStatus;
                        String  bookId;
                        JSONArray resultSet = new JSONArray(jsonString);
                        users = new ArrayList<>();
                        for (int i = 0; i < resultSet.length(); ++i) {
                            JSONObject row = resultSet.getJSONObject(i);
                            users.add(new User(row.getString("userreader_id"),
                                    row.getString("usersurname"),
                                    row.getString("userfirstname"),
                                    row.getString("userlogin"),
                                    row.getString("phonenumber"),
                                    row.getString("email")
                                    ));
                        }
                       UserListAdapter adapter = new UserListAdapter(users, userClickListener);
                        usersView.setAdapter(adapter);
                    } catch (JSONException e) {
                            e.printStackTrace();
                    }
                    //Log.d("data", resultData.getString("JSONString"));
                    break;
            }
            super.onReceiveResult(resultCode, resultData);
        }

    }
}
