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
    private UserClickListener userClickListener;
    List<User> users;

    public interface UserClickListener {
        void onItemClick(int position, View v);
    }

    public class UserViewHolder extends RecyclerView.ViewHolder {

            CardView userCardView;
            TextView userId;
            TextView username;
            TextView userlogin;

            UserViewHolder(View itemView) {
                super(itemView);
                userCardView = itemView.findViewById(R.id.userCard);
                userId = itemView.findViewById(R.id.user_id);
                Log.d("kjkk",String.valueOf(userId));
                username = itemView.findViewById(R.id.user_name);
                userlogin = itemView.findViewById(R.id.user_login);

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //User user = books.get(getLayoutPosition());
                        userClickListener.onItemClick(getAdapterPosition(), v);
                    }
                });

            }
        }


    UserListAdapter(List<User> users, UserClickListener userClickListener){
        this.users = users;
        this.userClickListener = userClickListener;
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
        }

        @Override
        public int getItemCount() {
            return users.size();
        }



}
