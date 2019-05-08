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

public class UserProposalsHistoryFragment extends Fragment {
    private ProgressBar spinner;
    public static int user_id = -1;
    public static Map<String, Proposal> bookIdForProposal;
    private ProposalAdapter.BookReturnClickListener bookReturnClickListener;
    View rootView;
    //private ActiveProposalActivity.RequebgfstResultReceiver requestResultReceiver;
    private RecyclerView recyclerView;
    private ProposalAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<Proposal> proposals;
    private Intent startIntent;
    private Intent startIntentBook;
    private UserProposalsHistoryFragment.RequestResultReceiver selectProposalReceiver;
    private UserProposalsHistoryFragment.RequestBookReceiver selectBookReceiver;
    String querySelectProposals = "SELECT book1_id, bookstatus, issuedate FROM [proposal] WHERE fk_userreader = %s";

    String querySelectBook;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("GetArguments", String.valueOf(getArguments()));
        if (getArguments() != null) {
            user_id = getArguments().getInt(Constants.USER_ID);
        }

        selectProposalReceiver = new UserProposalsHistoryFragment.RequestResultReceiver(new Handler());
        selectBookReceiver = new UserProposalsHistoryFragment.RequestBookReceiver(new Handler());
        startIntent = new Intent(getActivity(), DbService.class);
        startIntentBook = new Intent(getActivity(), DbService.class);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_proposals, container, false);
        TextView userProposalText = rootView.findViewById(R.id.current_proposal);
        userProposalText.setText("История заявок: ");
        spinner = rootView.findViewById(R.id.progressBar1);
        spinner.setVisibility(View.GONE);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.rViewProposal);
        layoutManager = new LinearLayoutManager(rootView.getContext());
        //запрос
        Log.d("User ID", String.valueOf(user_id));
        querySelectProposals = String.format(querySelectProposals, String.valueOf(user_id));//"SELECT book1_id, bookstatus, issuedate FROM [proposal] WHERE fk_userreader = "+String.valueOf(user_id) + " AND bookstatus IN (4,5)";
        //startIntent.putExtra("receiver", requestResultReceiver);
        startIntent.putExtra("request", querySelectProposals);
        startIntent.putExtra("receiver", selectProposalReceiver);
        startIntent.putExtra("type", "select");
        getActivity().startService(startIntent);
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
                        proposals = new ArrayList<>();
                        bookIdForProposal = new HashMap<>();

                        Log.d("Before", bookIdForProposal.keySet().toString());
                        for (int i = 0; i < resultSet.length(); ++i) {
                            JSONObject row = resultSet.getJSONObject(i);
                            bookId = row.getString("book1_id");
                            bookStatus = Integer.valueOf(row.getString("bookstatus"));
                            bookIdForProposal.put(bookId, new Proposal(bookId,0, Constants.stasusDictionary.get(bookStatus), row.getString("issuedate") ));
                        }
                        Log.d("After", bookIdForProposal.keySet().toString());
                        if (bookIdForProposal.keySet().size() > 0) {
                            String querySelectBook = "SELECT book_id, bookname FROM [book] WHERE book_id IN ";
                            String booksId = bookIdForProposal.keySet().toString()
                                    .replace("[", "(")
                                    .replace("]", ")");
                            System.out.println(querySelectBook + booksId);
                            startIntentBook.putExtra("request", querySelectBook + booksId);
                            startIntentBook.putExtra("receiver", selectBookReceiver);
                            startIntentBook.putExtra("type", "select");
                            getActivity().startService(startIntentBook);
                        }
                        else{
                            mAdapter = new ProposalAdapter(rootView.getContext(),proposals);
                            // use a linear layout manager
                            layoutManager = new LinearLayoutManager(rootView.getContext());
                            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                            recyclerView.setAdapter(mAdapter);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    //Log.d("data", resultData.getString("JSONString"));
                    break;
            }
            super.onReceiveResult(resultCode, resultData);
        }

    }
    private class RequestBookReceiver extends ResultReceiver {

        public RequestBookReceiver(Handler handler) {
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
                        String bookId, bookName;
                        JSONArray resultSet = new JSONArray(jsonString);
                        for (int i = 0; i < resultSet.length(); ++i) {
                            JSONObject rec = resultSet.getJSONObject(i);
                            bookId =  rec.getString("book_id");
                            bookName =  rec.getString("bookname");
                            bookIdForProposal.get(bookId).bookName = bookName;
                            Log.d("bookname", bookIdForProposal.get(bookId).bookName);
                        }

                        proposals = new ArrayList<>(bookIdForProposal.values());
                        Log.d("prpsize", String.valueOf(proposals.size()));
                        mAdapter = new ProposalAdapter(rootView.getContext(),proposals, bookReturnClickListener, "UserMyProposalsFragment");
                        // use a linear layout manager
                        layoutManager = new LinearLayoutManager(rootView.getContext());
                        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                        recyclerView.setAdapter(mAdapter);
                    } catch (JSONException e) {
                        Log.d("Error", e.toString());
                        e.printStackTrace();
                    }
                    Log.d("bookName2", bookIdForProposal.values().toString());
                    Log.d("data", resultData.getString("JSONString"));
                    break;
            }
            super.onReceiveResult(resultCode, resultData);
        }

    }


}
