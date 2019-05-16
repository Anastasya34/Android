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
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class AdminMyProposals_ProposalInfoFragment extends Fragment {
    View rootView;
    private ArrayList<Book> books;
    private RecyclerView booksView;
    SelectUserBooKsReceiver selectUserBooksReceiver;
    User user;
    public static Integer admin_id = -1;
    public static String user_id = "-1";
    String bookName, proposalId;
    Integer countBook;
    TextView userId, username, userlogin;
    String querySelectUsers = "SELECT [userreader_id]\n" +
            "      ,[usersurname]\n" +
            "      ,[userfirstname]\n" +
            "      ,[usersecondname]\n" +
            "      ,[phonenumber]\n" +
            "      ,[userlogin]\n" +
            "      ,[email]\n" +
            "  FROM [dbo].[userreader] WHERE [userreader_id] = %s";
    SelectUserReceiver selectUserReceiver;
    UpdateProposalReceiver updateProposalReceiver;
    String querySelectUserBooks = "SELECT [book_id]\n" +
            "      ,[bookname]\n" +
            "  FROM [dbo].[book]\n" +
            "  WHERE [book_id] IN (SELECT \n" +
            "      [book1_id]\n" +
            "  FROM [dbo].[proposal]\n" +
            "  WHERE [fk_userreader] = '"+user_id+"' AND [bookstatus] = 5)";
    String queryUpdateApprovedProposal = "UPDATE [proposal] SET bookstatus = 5 WHERE proposal_id = %s;";
    String queryUpdateRejectedProposal = "UPDATE [proposal] SET bookstatus = 1 WHERE proposal_id = %s;";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("GetArguments", String.valueOf(getArguments()));
        if (getArguments() != null) {
            user_id = getArguments().getString(Constants.USER_ID);
            admin_id = getArguments().getInt(Constants.ADMIN_ID);
            bookName = getArguments().getString("BOOK_NAME");
            countBook = getArguments().getInt("COUNT_BOOK");
            proposalId = getArguments().getString("PROPOSAL_ID");
        }
        querySelectUsers = String.format(querySelectUsers, String.valueOf(user_id));
        selectUserReceiver = new SelectUserReceiver(new Handler());
        selectUserBooksReceiver = new SelectUserBooKsReceiver(new Handler());
        updateProposalReceiver = new UpdateProposalReceiver(new Handler());

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_proposalsinfo, container, false);
        TextView bookNameView = rootView.findViewById(R.id.book_name);
        booksView = rootView.findViewById(R.id.ViewBooks);
        LinearLayoutManager llm2 = new LinearLayoutManager(rootView.getContext());
        booksView.setLayoutManager(llm2);
        booksView.setHasFixedSize(true);
        bookNameView.setText("Название книги: "+bookName);
        TextView bookCount = rootView.findViewById(R.id.book_count);
        bookCount.setText("Количество экземпляров: "+String.valueOf(countBook));
        userId = rootView.findViewById(R.id.user_id);
        username = rootView.findViewById(R.id.user_name);
        userlogin = rootView.findViewById(R.id.user_login);
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
        TextView booksUserLink = rootView.findViewById(R.id.user_books_list);
        booksUserLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickBooksUserLink(v);
            }
        });
        Button approvedButton = rootView.findViewById(R.id.approved_button);
        approvedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                approvedButtonClick(v);
            }
        });
        Button rejectButton = rootView.findViewById(R.id.reject_button);
        rejectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //User user = books.get(getLayoutPosition());
                rejectButtonClick(v);
            }
        });
        LinearLayoutManager llm = new LinearLayoutManager(rootView.getContext());
        startIntent(querySelectUsers, selectUserReceiver, "select");
        startIntent(querySelectUserBooks, selectUserBooksReceiver, "select");
        return rootView;
    }

    public void approvedButtonClick(View v){
        queryUpdateApprovedProposal = String.format(queryUpdateApprovedProposal, proposalId);
        startIntent(queryUpdateApprovedProposal, updateProposalReceiver, "update");

        Bundle args = new Bundle();
        args.putInt(Constants.ADMIN_ID, admin_id);
        Class fragmentClass = AdminAllProposalsFragment.class;
        try {
            Fragment fragment = (Fragment) fragmentClass.newInstance();
            fragment.setArguments(args);
            // Вставляем фрагмент, заменяя текущий фрагмент
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.container, fragment).addToBackStack(this.getTag()).commit();
        } catch (java.lang.InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }


    }

    public void rejectButtonClick(View v){
        queryUpdateRejectedProposal = String.format(queryUpdateRejectedProposal, proposalId);
        startIntent(queryUpdateRejectedProposal, updateProposalReceiver, "update");

        Bundle args = new Bundle();
        args.putInt(Constants.ADMIN_ID, admin_id);
        Class fragmentClass = AdminMyProposals_Main.class;
        try {
            Fragment fragment = (Fragment) fragmentClass.newInstance();
            fragment.setArguments(args);
            // Вставляем фрагмент, заменяя текущий фрагмент
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.container, fragment).addToBackStack(this.getTag()).commit();
        } catch (java.lang.InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }


    }
    public void clickLinkProfile(View v) {
        Bundle args = new Bundle();
        args.putInt(Profile.ARG_USER_ID, Integer.valueOf(user_id));
        args.putBoolean(Profile.ARG_EDITABLE, false);

        android.support.v4.app.Fragment fragment = null;
        Class fragmentClass = Profile.class;

        if (fragmentClass != null) {

            try {
                fragment = (android.support.v4.app.Fragment) fragmentClass.newInstance();
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
                fragment = (android.support.v4.app.Fragment) fragmentClass.newInstance();
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
    public void clickBooksUserLink(View v){
        Bundle args = new Bundle();
        args.putInt(AdminProposalsUserHistoryFragment.USER_ID, Integer.valueOf(user_id));
        args.putString("admORuserId", "fk_userreader");
        Class fragmentClass = AdminMyProposals_UsersBooks.class;
        if (fragmentClass != null) {
            try {
                Fragment fragment = (Fragment) fragmentClass.newInstance();
                fragment.setArguments(args);
                // Вставляем фрагмент, заменяя текущий фрагмент
                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.container, fragment).addToBackStack(this.getTag()).commit();
            } catch (Exception e) {
                e.printStackTrace();
                Log.d("Error", e.getMessage());

            }

        }
    }
    private class SelectUserReceiver extends ResultReceiver {

        public SelectUserReceiver(Handler handler) {
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
                        for (int i = 0; i < resultSet.length(); ++i) {
                            JSONObject row = resultSet.getJSONObject(i);
                            user = new User(row.getString("userreader_id"),
                                    row.getString("usersurname"),
                                    row.getString("userfirstname"),
                                    row.getString("userlogin"),
                                    row.getString("phonenumber"),
                                    row.getString("email")
                            );
                        }
                       userId.setText("Номер читательского билета: "+ user.userId);
                       username.setText("Имя: "+ user.usersurname +" "+user.userfirstname);
                       userlogin.setText("Ник: " + user.userlogin);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    //Log.d("data", resultData.getString("JSONString"));
                    break;
            }
            super.onReceiveResult(resultCode, resultData);
        }

    }

    private class SelectUserBooKsReceiver extends ResultReceiver {

        public SelectUserBooKsReceiver(Handler handler) {
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
                        booksView.setAdapter(adapter);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                    Log.d("data", resultData.getString("JSONString"));
                    break;
            }
            super.onReceiveResult(resultCode, resultData);
        }


    }
    public void startIntent(String queryRequest, ResultReceiver startReceiver, String type){
        Intent startIntent = new Intent(rootView.getContext(), DbService.class);
        startIntent.putExtra("request", queryRequest);
        startIntent.putExtra("receiver", startReceiver);
        startIntent.putExtra("type", type);
        rootView.getContext().startService(startIntent);
    }
    private class UpdateProposalReceiver extends ResultReceiver {

        public UpdateProposalReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            switch (resultCode) {
                case DbService.REQUEST_ERROR:
                    Log.d("data", resultData.getString("SQLException"));
                    break;

                case DbService.REQUEST_SUCCESS:
                    Log.d("success","eeeeee");
                    break;
            }
            super.onReceiveResult(resultCode, resultData);
        }

    }
}
