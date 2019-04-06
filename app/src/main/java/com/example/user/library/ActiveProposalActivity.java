package com.example.user.library;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public class ActiveProposalActivity extends Fragment {
    //private ActiveProposalActivity.RequestResultReceiver requestResultReceiver;
    private RecyclerView recyclerView;
    private ProposalAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<Proposal> proposals;
    private ArrayMap<Integer, String> stasusDictionary = new ArrayMap<>();
    private Intent startIntent;
    private void fillStatusDictionary(){
        stasusDictionary.put(0, "заявка отправлена");
        stasusDictionary.put(1, "заявка на рассмотрении");
        stasusDictionary.put(2, "заявка отклонена");
        stasusDictionary.put(3, "заявка одобрена");
        stasusDictionary.put(4, "заявка закрыта");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fillStatusDictionary();
        View rootView = inflater.inflate(R.layout.active_proposal, container, false);
        // specify an adapter (see also next example)
        Proposal test = new Proposal(0,"Название книги","Статус","01.01.19");
        Proposal test2 = new Proposal(0,"Название книги2","Статус","01.01.19");

        proposals = new ArrayList<>();
        proposals.add(test);
        proposals.add(test2);
        //запрос
        startIntent = new Intent(getActivity(), DbService.class);
        String querySelectUser = "SELECT userreader_id FROM [userreader]";
        String querySelectProposals = "SELECT book1_id, bookstatus, issuedate FROM [proposal] WHERE fk_userreader = ''";
        //startIntent.putExtra("receiver", requestResultReceiver);
        startIntent.putExtra("request", querySelectUser);
        startIntent.putExtra("permission", "user");

        mAdapter = new ProposalAdapter(rootView.getContext(), proposals);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.rViewProposal);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(rootView.getContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(mAdapter);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        //recyclerView.setHasFixedSize(true);
        return rootView;
    }

    /*private class RequestResultReceiver extends ResultReceiver{

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
                        proposals = new ArrayList<>();

                        for (int i = 0; i < resultSet.length(); ++i) {
                            JSONObject rec = resultSet.getJSONObject(i);
                            proposals.add(new Proposal(0, rec.getString("bookname"), rec.getString("bookstatus"), rec.getString("issuedate") ));
                        }
                        mAdapter = new ProposalAdapter(rootView.getContext(), proposals);
                        recyclerView.setAdapter(mAdapter);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                    Log.d("data", resultData.getString("JSONString"));
                    break;
            }
            super.onReceiveResult(resultCode, resultData);
        }

    }*/
}