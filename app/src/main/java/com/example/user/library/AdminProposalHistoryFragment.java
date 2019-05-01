package com.example.user.library;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.app.Fragment;
import android.support.v4.util.ArrayMap;
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
import java.util.HashMap;
import java.util.Map;

public class AdminProposalHistoryFragment extends Fragment {
    public static final String USER_ID = "user_id";
    public static String user_id = "-1";
    public static Map<String, Proposal> bookIdForProposal;
    View rootView;
    //private ActiveProposalActivity.RequestResultReceiver requestResultReceiver;
    private RecyclerView recyclerView;
    private ProposalAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<Proposal> proposals;
    private ArrayMap<Integer, String> stasusDictionary = new ArrayMap<>();
    private Intent startIntent;
    private Intent startIntentBook;
    private AdminProposalHistoryFragment.RequestResultReceiver requestProposalReceiver;
    private AdminProposalHistoryFragment.RequestBookReceiver requestBookReceiver;
    String querySelectBook;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("GetArguments", String.valueOf(getArguments()));
        if (getArguments() != null) {
            user_id = getArguments().getString(USER_ID);
        }
        requestProposalReceiver = new AdminProposalHistoryFragment.RequestResultReceiver(new Handler());
        requestBookReceiver = new AdminProposalHistoryFragment.RequestBookReceiver(new Handler());
        startIntent = new Intent(getActivity(), DbService.class);
        startIntentBook = new Intent(getActivity(), DbService.class);
        fillStatusDictionary();
    }

    private void fillStatusDictionary(){
        stasusDictionary.put(0, "заявка отправлена");
        stasusDictionary.put(1, "заявка обрабатывается");
        stasusDictionary.put(2, "заявка отклонена");
        stasusDictionary.put(3, "заявка одобрена");
        stasusDictionary.put(4, "книга на руках");
        stasusDictionary.put(5, "заявка закрыта");
        stasusDictionary.put(6, "заявка расформирована");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.active_proposal, container, false);
        TextView userProposalText = rootView.findViewById(R.id.current_proposal);
        userProposalText.setText("Заявки читателя");
        recyclerView = (RecyclerView) rootView.findViewById(R.id.rViewProposal);
        layoutManager = new LinearLayoutManager(rootView.getContext());
        Log.d("User ID", String.valueOf(user_id));
        String querySelectProposals = "SELECT book1_id, bookstatus, issuedate FROM [proposal] WHERE fk_userreader = "+String.valueOf(user_id);
        //startIntent.putExtra("receiver", requestResultReceiver);
        startIntent.putExtra("request", querySelectProposals);
        startIntent.putExtra("receiver", requestProposalReceiver);
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
                            bookIdForProposal.put(bookId, new Proposal(bookId,0, stasusDictionary.get(bookStatus), row.getString("issuedate") ));
                        }
                        Log.d("After", bookIdForProposal.keySet().toString());
                        String querySelectBook = "SELECT book_id, bookname FROM [book] WHERE book_id IN ";
                        String booksId = bookIdForProposal.keySet().toString()
                                .replace("[","(")
                                .replace("]",")");
                        System.out.println(querySelectBook+booksId);
                        startIntentBook.putExtra("request", querySelectBook + booksId);
                        startIntentBook.putExtra("receiver", requestBookReceiver);
                        startIntentBook.putExtra("type", "select");
                        getActivity().startService(startIntentBook);
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
                        mAdapter = new ProposalAdapter(rootView.getContext(),proposals);
                        // use a linear layout manager
                        layoutManager = new LinearLayoutManager(rootView.getContext());
                        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                        recyclerView.setAdapter(mAdapter);
                    } catch (JSONException e) {
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