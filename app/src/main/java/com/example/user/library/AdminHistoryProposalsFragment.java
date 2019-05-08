package com.example.user.library;

import android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
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

public class AdminHistoryProposalsFragment extends Fragment {
    private ProgressBar spinner;
    public static int admin_id = -1;
    public static Map<String, Proposal> bookIdForProposal;
    private ProposalAdapter.BookReturnClickListener processClickListener;
    View rootView;
    private RecyclerView recyclerView;
    private ProposalAdapter mAdapter;
    private ArrayList<Proposal> proposals;
    private SelectProposalsReceiver selectProposalReceiver;
    private SelecttBookReceiver selectBookReceiver;

    String querySelectProposals = "SELECT book1_id, bookstatus, fk_userreader, issuedate FROM [proposal] WHERE fk_admin = %s";
    String querySelectBook = "SELECT book_id, bookname FROM [book] WHERE book_id IN ";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("GetArguments", String.valueOf(getArguments()));
        if (getArguments() != null) {
            admin_id = getArguments().getInt(Constants.ADMIN_ID);
        }
        querySelectProposals = String.format(querySelectProposals, String.valueOf(admin_id));
        selectProposalReceiver = new SelectProposalsReceiver(new Handler());
        selectBookReceiver = new SelecttBookReceiver(new Handler());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_proposals, container, false);
        TextView textView = rootView.findViewById(R.id.current_proposal);
        textView.setVisibility(View.GONE);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.rViewProposal);
        spinner = rootView.findViewById(R.id.progressBar1);
        spinner.setVisibility(View.GONE);
        startIntent(querySelectProposals, selectProposalReceiver, "select");
        return rootView;
    }

    public void startIntent(String queryRequest, ResultReceiver startReceiver, String type){
        Intent startIntent = new Intent(rootView.getContext(), DbService.class);
        startIntent.putExtra("request", queryRequest);
        startIntent.putExtra("receiver", startReceiver);
        startIntent.putExtra("type", type);
        rootView.getContext().startService(startIntent);
    }


    private class SelectProposalsReceiver extends ResultReceiver {

        public SelectProposalsReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            int bookStatus;
            String  bookId, userId;
            proposals = new ArrayList<>();
            bookIdForProposal = new HashMap<>();

            switch (resultCode) {
                case DbService.REQUEST_ERROR:
                    Log.d("data", resultData.getString("SQLException"));
                    break;

                case DbService.REQUEST_SUCCESS:
                    String jsonString = resultData.getString("JSONString");
                    Log.d("Response", jsonString);
                    Log.d("Before", bookIdForProposal.keySet().toString());
                    try {
                        JSONArray resultSet = new JSONArray(jsonString);
                        for (int i = 0; i < resultSet.length(); ++i) {
                            JSONObject row = resultSet.getJSONObject(i);
                            bookId = row.getString("book1_id");
                            userId = row.getString("fk_userreader");
                            bookStatus = Integer.valueOf(row.getString("bookstatus"));
                            bookIdForProposal.put(bookId, new Proposal(bookId,0, Constants.stasusDictionary.get(bookStatus), row.getString("issuedate"), userId ));
                        }
                        Log.d("After", bookIdForProposal.keySet().toString());
                        if (bookIdForProposal.keySet().size() > 0) {
                            String booksId = bookIdForProposal.keySet().toString()
                                    .replace("[", "(")
                                    .replace("]", ")");
                            System.out.println(querySelectBook + booksId);
                            startIntent(querySelectBook + booksId, selectBookReceiver, "select");
                        }
                        else{
                            mAdapter = new ProposalAdapter(rootView.getContext(),proposals, processClickListener, "AdminHistoryProposalsFragment");
                            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                            recyclerView.setAdapter(mAdapter);
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
                            bookIdForProposal.get(bookId).bookName = bookName;
                            Log.d("bookname", bookIdForProposal.get(bookId).bookName);
                        }

                        proposals = new ArrayList<>(bookIdForProposal.values());
                        Log.d("prpsize", String.valueOf(proposals.size()));
                        mAdapter = new ProposalAdapter(rootView.getContext(),proposals, processClickListener, "AdminHistoryProposalsFragment");
                        // use a linear layout manager
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
