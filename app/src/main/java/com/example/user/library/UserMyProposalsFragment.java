package com.example.user.library;

import android.content.Intent;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UserMyProposalsFragment extends Fragment {
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
    private UserMyProposalsFragment.RequestResultReceiver selectProposalReceiver;
    private UserMyProposalsFragment.RequestBookReceiver selectBookReceiver;
    private UserMyProposalsFragment.UpdateProposalReceiver updateProposalReceiver;
    private UpdateBookAvialbilityReceiver updateBookAvialbilityReceiver;
    String querySelectProposals = "SELECT book1_id, bookstatus, issuedate, bookplace_id FROM [proposal] WHERE fk_userreader = %s AND bookstatus IN (0,2)";
    String queryUpdateProposal = "UPDATE [proposal] SET bookstatus = %s WHERE fk_userreader = %s AND book1_id = %s ;";
    String updateBookAvialbility = "UPDATE [dbo].[bookplace] SET [bookavailability] = 1 WHERE [id_bookplace] = %s";

    String querySelectBook;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("GetArguments", String.valueOf(getArguments()));
        if (getArguments() != null) {
            user_id = getArguments().getInt(Constants.USER_ID);
        }

        selectProposalReceiver = new UserMyProposalsFragment.RequestResultReceiver(new Handler());
        selectBookReceiver = new UserMyProposalsFragment.RequestBookReceiver(new Handler());
        updateProposalReceiver = new UserMyProposalsFragment.UpdateProposalReceiver(new Handler());
        updateBookAvialbilityReceiver = new UpdateBookAvialbilityReceiver(new Handler());
        startIntent = new Intent(getActivity(), DbService.class);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_proposals, container, false);
        spinner = rootView.findViewById(R.id.progressBar1);
        spinner.setVisibility(View.GONE);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.rViewProposal);
        layoutManager = new LinearLayoutManager(rootView.getContext());
        //запрос
        Log.d("User ID", String.valueOf(user_id));
        querySelectProposals = String.format(querySelectProposals, String.valueOf(user_id));//"SELECT book1_id, bookstatus, issuedate FROM [proposal] WHERE fk_userreader = "+String.valueOf(user_id) + " AND bookstatus IN (4,5)";
        startIntent(querySelectProposals, selectProposalReceiver, "select");

        bookReturnClickListener = new ProposalAdapter.BookReturnClickListener(){
            @Override
            public void onBookReturnButtonClick(int position, View v) {
                onClickCancelButton(position, v);
            }
        };
        return rootView;
    }

    public void onClickCancelButton(int position, View v){
        spinner.setVisibility(View.VISIBLE);
        String bookId = proposals.get(position).bookId;
        String bookplace_id = proposals.get(position).bookplace_id;
        Log.d("onClickReturnBook", bookId);

        String queryUpdate = String.format(queryUpdateProposal, String.valueOf(1), String.valueOf(user_id), bookId);
        startIntent(queryUpdate, updateProposalReceiver, "update");

        String queryUpdateAvialBook = String.format(updateBookAvialbility, bookplace_id);
        startIntent(queryUpdateAvialBook, updateBookAvialbilityReceiver, "update");
        Log.d("queryUpdateAvialBook", queryUpdateAvialBook);

        bookReturnClickListener = new ProposalAdapter.BookReturnClickListener(){
            @Override
            public void onBookReturnButtonClick(int position, View v) {
                onClickCancelButton(position, v);
            }
        };
        startIntent(querySelectProposals, selectProposalReceiver, "select");
        spinner.setVisibility(View.GONE);
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
                        String  bookId, bookplaceId;
                        JSONArray resultSet = new JSONArray(jsonString);
                        proposals = new ArrayList<>();
                        bookIdForProposal = new HashMap<>();

                        Log.d("Before", bookIdForProposal.keySet().toString());
                        for (int i = 0; i < resultSet.length(); ++i) {
                            JSONObject row = resultSet.getJSONObject(i);
                            bookId = row.getString("book1_id");
                            bookplaceId =  row.getString("bookplace_id");
                            bookStatus = Integer.valueOf(row.getString("bookstatus"));
                            bookIdForProposal.put(bookId, new Proposal(bookId,Constants.stasusDictionary.get(bookStatus), row.getString("issuedate"), bookplaceId,0 ));
                        }
                        Log.d("After", bookIdForProposal.keySet().toString());
                        if (bookIdForProposal.keySet().size() > 0) {
                            String querySelectBook = "SELECT book_id, bookname FROM [book] WHERE book_id IN ";
                            String booksId = bookIdForProposal.keySet().toString()
                                    .replace("[", "(")
                                    .replace("]", ")");
                            System.out.println(querySelectBook + booksId);
                            startIntent(querySelectBook + booksId, selectBookReceiver,"select");

                        }
                        else{
                            mAdapter = new ProposalAdapter(rootView.getContext(),proposals, bookReturnClickListener, "UserMyProposalsFragment");
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
    private class UpdateBookAvialbilityReceiver extends ResultReceiver {

        int position = -1;
        UpdateBookAvialbilityReceiver(Handler handler) {
            super(handler);
        }

        void setPosition(int position) {
            this.position = position;
        }
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            switch (resultCode) {
                case DbService.REQUEST_ERROR:
                    Log.d("data", resultData.getString("SQLException"));

                    break;

                case DbService.REQUEST_SUCCESS:
                    // возвращает количество вставленных или измененных строк
                    int n_strings = resultData.getInt("n_strings");
                    Log.e("data", String.valueOf(n_strings));
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
