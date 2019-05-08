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
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class AdminUsersFragment extends Fragment {
    private View rootView;
    private RecyclerView usersView;
    private AdminUsersFragment.RequestResultReceiver requestResultReceiver;
    private Intent startIntent;
    private ArrayList<User> users;
    private UserListAdapter.UserClickListener userClickListener;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_users_list, container, false);
        usersView = rootView.findViewById(R.id.ViewUsers);
        TextView infoText = rootView.findViewById(R.id.info_text);
        infoText.setText("Выберите пользователя: ");
        LinearLayoutManager llm = new LinearLayoutManager(rootView.getContext());
        usersView.setLayoutManager(llm);
        usersView.setHasFixedSize(true);
        requestResultReceiver = new AdminUsersFragment.RequestResultReceiver(new Handler());
        startIntent = new Intent(rootView.getContext(), DbService.class);
        String query = "SELECT [userreader_id]\n" +
                "      ,[usersurname]\n" +
                "      ,[userfirstname]\n" +
                "      ,[usersecondname]\n" +
                "      ,[phonenumber]\n" +
                "      ,[userlogin]\n" +
                "      ,[email]\n" +
                "  FROM [dbo].[userreader]";
        Intent startIntent = new Intent(rootView.getContext(), DbService.class);
        requestResultReceiver = new AdminUsersFragment.RequestResultReceiver(new Handler());
        startIntent.putExtra("receiver", requestResultReceiver);
        startIntent.putExtra("request", query);
        rootView.getContext().startService(startIntent);
        userClickListener = new UserListAdapter.UserClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                // Создадим новый фрагмент
                Fragment fragment = null;
                Class fragmentClass = null;
                Bundle args = new Bundle();
                fragmentClass = AdminUserInfoFragment.class;
                User curUser = users.get(position);
                Log.d("userId_", curUser.userId );
                args.putString(AdminUserInfoFragment.USER_ID, curUser.userId);
                args.putString(AdminUserInfoFragment.USER_NAME, curUser.usersurname+" "+curUser.userfirstname);
                args.putString(AdminUserInfoFragment.USER_LOGIN, curUser.userlogin);

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
                // Выделяем выбранный пункт меню в шторке

                Log.d("!position", String.valueOf(position));
                //Log.d("!id", String.valueOf(id));*/
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
