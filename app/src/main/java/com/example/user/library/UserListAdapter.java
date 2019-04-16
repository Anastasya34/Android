package com.example.user.library;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import java.util.List;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.UserViewHolder> {

        public static class UserViewHolder extends RecyclerView.ViewHolder {

            CardView userCardView;
            TextView userId;
            TextView username;
            TextView userlogin;
            TextView email;
            TextView phonenumber;

            UserViewHolder(View itemView) {
                super(itemView);
                userCardView = itemView.findViewById(R.id.userCard);
                userId = itemView.findViewById(R.id.user_id);
                Log.d("kjkk",String.valueOf(userId));
                username = itemView.findViewById(R.id.user_name);
                userlogin = itemView.findViewById(R.id.user_login);
                email = itemView.findViewById(R.id.user_email);
                phonenumber = itemView.findViewById(R.id.user_phone);

            }
        }

        List<User> users;

        UserListAdapter(List<User> users){
            this.users = users;
        }

        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
        }

        @Override
        public com.example.user.library.UserListAdapter.UserViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.user_recycler_item, viewGroup, false);
            UserListAdapter.UserViewHolder bvh = new UserListAdapter.UserViewHolder(v);
            return bvh;
        }

        @Override
        public void onBindViewHolder(UserListAdapter.UserViewHolder userViewHolder, int i) {
            Log.d("USer", String.valueOf(users.get(i).userId));
            userViewHolder.userId.setText("Id: " + users.get(i).userId);
            userViewHolder.username.setText("ФИО: " + users.get(i).usersurname + " " + users.get(i).userfirstname );
            userViewHolder.userlogin.setText("Ник: " + users.get(i).userlogin);
            userViewHolder.email.setText("E-mail: " + users.get(i).email);
            userViewHolder.phonenumber.setText("Телефон: " + users.get(i).phonenumber);
        }

        @Override
        public int getItemCount() {
            return users.size();
        }



}
