package com.example.user.library;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
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

public class UserBookPlace extends Fragment {
    private View rootView;
    public static final String BOOK_ID = "book_id";
    public String bookId;
    private SelectBookReceiver selectBookReceiver;
    private SelectRoomReceiver selectRoomReceiver;
    TextView dormitory;
    TextView room;
    TextView board;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selectBookReceiver = new SelectBookReceiver(new Handler());
        selectRoomReceiver = new SelectRoomReceiver(new Handler());
        if (getArguments() != null) {
            bookId = getArguments().getString(BOOK_ID);
            Log.d("bookid", getArguments().getString(BOOK_ID));
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_bookplace, container, false);
        dormitory = rootView.findViewById(R.id.dormitory);
        room = rootView.findViewById(R.id.room);
        board = rootView.findViewById(R.id.board);
        String selectBook = "SELECT [book_id]\n" +
                "      ,[fk_room]\n" +
                "      ,[fk_dorm]\n" +
                "      ,[fk_board] FROM [dbo].[book] WHERE [book_id] = " + bookId;
        startIntent(selectBook, selectBookReceiver, "select");
        return rootView;
    }
    private class SelectBookReceiver extends ResultReceiver {

        public SelectBookReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {


            switch (resultCode) {
                case DbService.REQUEST_ERROR:
                    Log.d("data", resultData.getString("SQLException"));
                    break;

                case DbService.REQUEST_SUCCESS:
                    String dormitoryString = "Общежитие", roomId = "1";
                    String boardString = "Шкаф ";
                    String jsonString = resultData.getString("JSONString");
                    try {
                        JSONArray resultSet = new JSONArray(jsonString);
                        for (int i = 0; i < resultSet.length(); ++i) {
                            JSONObject row = resultSet.getJSONObject(i);
                            dormitoryString = row.getString("fk_dorm");
                            roomId = row.getString("fk_room");
                            boardString = boardString + row.getString("fk_board");
                             }
                        dormitory.setText(dormitoryString);
                        board.setText(boardString);
                        String selectRoom = "SELECT [roomnumber] FROM [dbo].[room] WHERE [room_id] = " + roomId;
                        startIntent(selectRoom, selectRoomReceiver, "select");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
            }
            super.onReceiveResult(resultCode, resultData);
        }

    }
    private class SelectRoomReceiver extends ResultReceiver {

        public SelectRoomReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {


            switch (resultCode) {
                case DbService.REQUEST_ERROR:
                    Log.d("data", resultData.getString("SQLException"));
                    break;

                case DbService.REQUEST_SUCCESS:
                    String roomString = "Комната ";
                    String jsonString = resultData.getString("JSONString");
                    try {
                        JSONArray resultSet = new JSONArray(jsonString);
                        for (int i = 0; i < resultSet.length(); ++i) {
                            JSONObject row = resultSet.getJSONObject(i);
                            roomString = roomString + row.getString("roomnumber");
                        }
                        room.setText(roomString);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
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
