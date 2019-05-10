package com.example.user.library;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AdminMyProposals_UsersBooks extends Fragment {
    private ProgressBar spinner;
    public static int admin_id = -1;
    public static int user_id = -1;
    public static Map<String, Book> bookIdForBook;
    View rootView;
    private RecyclerView recyclerView;
    private ProposalAdapter mAdapter;
    private ArrayList<Proposal> proposals;
    private SelectProposalsReceiver selectProposalReceiver;
    private SelectBookReceiver selectBookReceiver;
    String admORuserId = "admORuserId";
    public static Map<String, User> userIdForBook;
    private SelectUsersReceiver selectUsersReceiver;
    String querySelectUsers =  "SELECT [userreader_id], [usersurname],[userfirstname],[usersecondname], [userlogin],[email] FROM [dbo].[userreader] WHERE [userreader_id] IN %s";

    String querySelectProposals = "SELECT proposal_id, book1_id, bookstatus, fk_userreader, issuedate FROM [proposal] WHERE %s = %s AND bookstatus = 5";
    String querySelectBook = "SELECT book_id, bookname FROM [book] WHERE book_id IN ";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Integer id = -1;
        Log.d("GetArguments", String.valueOf(getArguments()));
        if (getArguments() != null) {
            admORuserId = getArguments().getString(admORuserId);
            if (admORuserId.equals("fk_admin")) {
                id = getArguments().getInt(Constants.ADMIN_ID);
            }
            else {
                id = getArguments().getInt(Constants.USER_ID);
            }

        }
        querySelectProposals = String.format(querySelectProposals, admORuserId, String.valueOf(id));
        Log.d("querySelectProposals", querySelectProposals);
        selectProposalReceiver = new SelectProposalsReceiver(new Handler());
        selectBookReceiver = new SelectBookReceiver(new Handler());
        selectUsersReceiver = new SelectUsersReceiver(new Handler());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_proposals, container, false);
        TextView textView = rootView.findViewById(R.id.current_proposal);
        textView.setText(R.string.user_books_list);
        if (admORuserId.equals("fk_admin")){
            textView.setVisibility(View.GONE);
        }
        recyclerView = (RecyclerView) rootView.findViewById(R.id.rViewProposal);
        spinner = rootView.findViewById(R.id.progressBar1);
        spinner.setVisibility(View.GONE);
        startIntent(querySelectProposals, selectProposalReceiver, "select");
        return rootView;
    }

    void setAdapter(){
        mAdapter = new ProposalAdapter(rootView.getContext(), proposals);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(mAdapter);
    }
    private class SelectProposalsReceiver extends ResultReceiver {

        public SelectProposalsReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            int bookStatus;
            String  proposalId, bookId, userId;
            proposals = new ArrayList<>();
            bookIdForBook = new HashMap<>();
            userIdForBook = new HashMap<>();
            proposals = new ArrayList<>();
            switch (resultCode) {
                case DbService.REQUEST_SUCCESS:
                    String jsonString = resultData.getString("JSONString");
                    Log.d("Response", jsonString);
                    try {
                        JSONArray resultSet = new JSONArray(jsonString);
                        for (int i = 0; i < resultSet.length(); ++i) {
                            JSONObject row = resultSet.getJSONObject(i);
                            bookId = row.getString("book1_id");
                            userId = row.getString("fk_userreader");
                            proposalId = row.getString("proposal_id");
                            bookStatus = Integer.valueOf(row.getString("bookstatus"));
                            proposals.add(new Proposal(bookId,0, Constants.stasusDictionary.get(bookStatus), row.getString("issuedate"), userId, proposalId ));
                            bookIdForBook.put(bookId, new Book(bookId));
                            userIdForBook.put(userId, new User(userId));
                        }
                        Log.d("After", bookIdForBook.keySet().toString());
                        if (!bookIdForBook.keySet().isEmpty()) {
                            String booksId = bookIdForBook.keySet().toString()
                                    .replace("[", "(")
                                    .replace("]", ")");
                            System.out.println(querySelectBook + booksId);
                            startIntent(querySelectBook + booksId, selectBookReceiver, "select");
                            if (!userIdForBook.keySet().isEmpty()) {
                                String usersId = userIdForBook.keySet().toString()
                                        .replace("[", "(")
                                        .replace("]", ")");
                                System.out.println(querySelectUsers + usersId);
                                startIntent(String.format(querySelectUsers, usersId), selectUsersReceiver, "select");
                            }
                        }
                        else {
                            setAdapter();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case DbService.REQUEST_ERROR:
                    Log.d("data", resultData.getString("SQLException"));
                    break;

            }
            super.onReceiveResult(resultCode, resultData);
        }

    }
    private class SelectBookReceiver extends ResultReceiver {

        public SelectBookReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            String bookId, bookName;

            switch (resultCode) {
                case DbService.REQUEST_ERROR:
                    Log.d("data", resultData.getString("SQLException"));
                    break;

                case DbService.REQUEST_SUCCESS:
                    String jsonString = resultData.getString("JSONString");
                    Log.d("Response", jsonString);
                    try {
                        JSONArray resultSet = new JSONArray(jsonString);
                        for (int i = 0; i < resultSet.length(); ++i) {
                            JSONObject rec = resultSet.getJSONObject(i);
                            bookId =  rec.getString("book_id");
                            bookName =  rec.getString("bookname");
                            bookIdForBook.get(bookId).name = bookName;
                        }
                        for (Proposal proposal: proposals){
                            proposal.bookName = bookIdForBook.get(proposal.bookId).name;
                        }
                        setAdapter();

                    } catch (JSONException e) {
                        Log.d("Error", e.toString());
                        e.printStackTrace();
                    }
                    Log.d("data", resultData.getString("JSONString"));
                    break;
            }
            super.onReceiveResult(resultCode, resultData);
        }

    }
    private class SelectUsersReceiver extends ResultReceiver {

        public SelectUsersReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            switch (resultCode) {
                case DbService.REQUEST_ERROR:
                    Log.d("data", resultData.getString("SQLException"));
                    break;

                case DbService.REQUEST_SUCCESS:
                    String userId, userLogin;
                    String jsonString = resultData.getString("JSONString");
                    Log.d("Response", jsonString);
                    try {
                        JSONArray resultSet = new JSONArray(jsonString);
                        for (int i = 0; i < resultSet.length(); ++i) {
                            JSONObject rec = resultSet.getJSONObject(i);
                            userId =  rec.getString("userreader_id");
                            userLogin =  rec.getString("userlogin");
                            userIdForBook.get(userId).userlogin = userLogin;
                        }
                        for (Proposal proposal: proposals){
                            proposal.userLogin = userIdForBook.get(proposal.userId).userlogin;
                        }
                        setAdapter();
                    } catch (JSONException e) {
                        Log.d("Error", e.toString());
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
}
