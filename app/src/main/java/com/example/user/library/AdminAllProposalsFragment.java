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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AdminAllProposalsFragment extends Fragment {
    private ProgressBar spinner;
    public static int admin_id = -1;
    public static Map<String, Book> bookIdForBook;
    public static Map<String, User> userIdForBook;
    private SelectUsersReceiver selectUsersReceiver;
    private ProposalAdapter.BookReturnClickListener processClickListener;
    View rootView;
    private RecyclerView recyclerView;
    private ProposalAdapter mAdapter;
    private ArrayList<Proposal> proposals;
    private SelectProposalsReceiver selectProposalReceiver;
    private SelecttBookReceiver selectBookReceiver;
    private UpdateProposalReceiver updateProposalReceiver;
    private String proposalType = "AdminAllProposalsFragment";
    String querySelectUsers =  "SELECT [userreader_id], [usersurname],[userfirstname],[usersecondname], [userlogin],[email] FROM [dbo].[userreader] WHERE [userreader_id] IN %s";
    String querySelectProposals = "SELECT proposal_id, book1_id, bookstatus, fk_userreader, issuedate FROM [proposal] WHERE fk_admin IS NULL";
    String querySelectBook = "SELECT book_id, bookname FROM [book] WHERE book_id IN ";
    String queryUpdateProposal = "UPDATE [proposal] SET bookstatus = 2, fk_admin = '%s' WHERE fk_userreader = %s AND book1_id = %s;";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("GetArguments", String.valueOf(getArguments()));
        if (getArguments() != null) {
            admin_id = getArguments().getInt(Constants.ADMIN_ID);
        }

        selectProposalReceiver = new AdminAllProposalsFragment.SelectProposalsReceiver(new Handler());
        selectBookReceiver = new AdminAllProposalsFragment.SelecttBookReceiver(new Handler());
        updateProposalReceiver = new AdminAllProposalsFragment.UpdateProposalReceiver(new Handler());
        selectUsersReceiver = new SelectUsersReceiver(new Handler());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_proposals, container, false);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.rViewProposal);
        spinner = rootView.findViewById(R.id.progressBar1);
        spinner.setVisibility(View.GONE);

        startIntent(querySelectProposals, selectProposalReceiver, "select");
        processClickListener = new ProposalAdapter.BookReturnClickListener(){
            @Override
            public void onBookReturnButtonClick(int position, View v) {
                onClickProcessButton(position, v);
            }
        };
        return rootView;
    }

    public void startIntent(String queryRequest,ResultReceiver startReceiver, String type){
        Intent startIntent = new Intent(rootView.getContext(), DbService.class);
        startIntent.putExtra("request", queryRequest);
        startIntent.putExtra("receiver", startReceiver);
        startIntent.putExtra("type", type);
        rootView.getContext().startService(startIntent);
    }

    public void onClickProcessButton(int position, View v){
        Proposal currentProposal = proposals.get(position);
        Log.d("admin_id", String.valueOf(admin_id));
        String queryUpdate = String.format(queryUpdateProposal, String.valueOf(admin_id), currentProposal.userId, currentProposal.bookId);
        startIntent(queryUpdate, updateProposalReceiver, "update");
        startIntent(querySelectProposals, selectProposalReceiver, "select");
    }
    void setAdapter(){
        mAdapter = new ProposalAdapter(rootView.getContext(), proposals, processClickListener, proposalType);
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
            String  bookId, userId, proposalId;
            proposals = new ArrayList<>();
            bookIdForBook = new HashMap<>();
            userIdForBook = new HashMap<>();
            switch (resultCode) {
                case DbService.REQUEST_ERROR:
                    Log.d("data", resultData.getString("SQLException"));
                    break;

                case DbService.REQUEST_SUCCESS:
                    String jsonString = resultData.getString("JSONString");
                    Log.d("Response", jsonString);
                    Log.d("Before", bookIdForBook.keySet().toString());
                    try {
                        JSONArray resultSet = new JSONArray(jsonString);
                        for (int i = 0; i < resultSet.length(); ++i) {
                            JSONObject row = resultSet.getJSONObject(i);
                            bookId = row.getString("book1_id");
                            userId = row.getString("fk_userreader");
                            bookStatus = Integer.valueOf(row.getString("bookstatus"));
                            proposalId = row.getString("proposal_id");
                            proposals.add(new Proposal(bookId,0, Constants.stasusDictionary.get(bookStatus), row.getString("issuedate"), userId, proposalId ));
                            bookIdForBook.put(bookId, new Book(bookId));
                            userIdForBook.put(userId, new User(userId));
                        }
                        Log.d("After", bookIdForBook.keySet().toString());
                        if (bookIdForBook.keySet().size() > 0) {
                            String booksId = bookIdForBook.keySet().toString()
                                    .replace("[", "(")
                                    .replace("]", ")");
                            System.out.println(querySelectBook + booksId);
                            startIntent(querySelectBook + booksId, selectBookReceiver, "select");
                            if (userIdForBook.keySet().size() > 0) {
                                String usersId = userIdForBook.keySet().toString()
                                        .replace("[", "(")
                                        .replace("]", ")");
                                System.out.println(querySelectUsers + usersId);
                                startIntent(String.format(querySelectUsers, usersId), selectUsersReceiver, "select");
                            }
                        }
                        else{
                            setAdapter();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
            }
            super.onReceiveResult(resultCode, resultData);
        }

    }
    private class SelecttBookReceiver extends ResultReceiver {

        public SelecttBookReceiver(Handler handler) {
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
