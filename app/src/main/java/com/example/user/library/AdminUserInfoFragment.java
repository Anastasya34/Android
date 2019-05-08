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
import java.util.List;

public class AdminUserInfoFragment  extends Fragment {
    private View rootView;
    public static final String USER_ID = "user_id";
    public static final String USER_NAME = "user_name";
    public static final String USER_LOGIN = "user_login";
    public static String user_id;
    public static String user_name;
    public static String user_login;
    private RecyclerView usersBookView;
    private AdminUserInfoFragment.RequestResultReceiver requestResultReceiver;
    private List<Book> books;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Log.d("userId_", getArguments().getString(USER_ID));
            user_id = getArguments().getString(USER_ID);
            user_name = getArguments().getString(USER_NAME);
            user_login = getArguments().getString(USER_LOGIN);
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_user_info, container, false);
        TextView userId = rootView.findViewById(R.id.user_id);
        TextView username = rootView.findViewById(R.id.user_name);
        TextView userlogin = rootView.findViewById(R.id.user_login);
        TextView profileLink = rootView.findViewById(R.id.link_user_profile);
        profileLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickLinkProfile(v);
            }
        });
        TextView proposalHistoryLink = rootView.findViewById(R.id.link_user_proposal_history);
        proposalHistoryLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickProposalHistory(v);
            }
        });
        userId.setText("Номер читательского билета: " + user_id);
        username.setText("ФИО: " + user_name);
        userlogin.setText("Ник: " + user_login);

        usersBookView =  rootView.findViewById(R.id.ViewUsers);
        LinearLayoutManager llm = new LinearLayoutManager(rootView.getContext());
        usersBookView.setLayoutManager(llm);
        usersBookView.setHasFixedSize(true);

        String query = "SELECT [book_id]\n" +
                "      ,[bookname]\n" +
                "  FROM [dbo].[book]\n" +
                "  WHERE [book_id] IN (SELECT \n" +
                "      [book1_id]\n" +
                "  FROM [dbo].[proposal]\n" +
                "  WHERE [fk_userreader] = '"+user_id+"' AND [bookstatus] = 4)";
        Intent startIntent = new Intent(rootView.getContext(), DbService.class);


        requestResultReceiver = new AdminUserInfoFragment.RequestResultReceiver(new Handler());
        startIntent.putExtra("receiver", requestResultReceiver);
        startIntent.putExtra("request", query);
        rootView.getContext().startService(startIntent);
        return rootView;
    }
    public void clickLinkProfile(View v) {
        Bundle args = new Bundle();
        args.putInt(Profile.ARG_USER_ID, Integer.valueOf(user_id));
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
            fragmentManager.beginTransaction().replace(R.id.container, fragment).addToBackStack(this.getTag()).commit();

        }
    }

    public void clickProposalHistory(View v) {
        Bundle args = new Bundle();
        args.putString(AdminProposalsUserHistoryFragment.USER_ID, user_id);

        Fragment fragment = null;
        Class fragmentClass = AdminProposalsUserHistoryFragment.class;

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
            fragmentManager.beginTransaction().replace(R.id.container, fragment).addToBackStack(this.getTag()).commit();


        }
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
                    try {
                        JSONArray resultSet = new JSONArray(jsonString);
                        books = new ArrayList<>();
                        if (resultSet.length() == 0) {
                            Log.d("data", "пустой");

                            break;
                        }

                        for (int i = 0; i < resultSet.length(); ++i) {
                            JSONObject rec = resultSet.getJSONObject(i);
                            books.add(new Book(rec.getString("bookname"), rec.getString("book_id")));
                        }
                        AdminBookListAdapter adapter = new AdminBookListAdapter(books);
                        usersBookView.setAdapter(adapter);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                    Log.d("data", resultData.getString("JSONString"));
                    break;
            }
            super.onReceiveResult(resultCode, resultData);
        }


    }
}
