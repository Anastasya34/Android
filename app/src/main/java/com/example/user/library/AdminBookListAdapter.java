package com.example.user.library;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class AdminBookListAdapter extends RecyclerView.Adapter<AdminBookListAdapter.BookViewHolder> {
    List<Book> books;
    private ClickListener adminClickListener;

    public class BookViewHolder extends RecyclerView.ViewHolder {

        CardView bookCardView;
        TextView bookName;
        TextView bookDescription;
        ImageView bookImage;

        BookViewHolder(View itemView) {
            super(itemView);
            bookCardView = itemView.findViewById(R.id.bookCard);
            bookName = itemView.findViewById(R.id.book_name);
            bookDescription = itemView.findViewById(R.id.book_info);
            bookImage = itemView.findViewById(R.id.book_icon);


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //User user = books.get(getLayoutPosition());
                    adminClickListener.onItemClick(getAdapterPosition(), v);
                }
            });
        }
    }

    public interface ClickListener {
        void onItemClick(int position, View v);
    }

    AdminBookListAdapter(List<Book> books, ClickListener adminClickListener){
        this.books = books;
        this.adminClickListener = adminClickListener;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public BookViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.book_card, viewGroup, false);
        BookViewHolder bvh = new BookViewHolder(v);

        return bvh;
    }

    @Override
    public void onBindViewHolder(BookViewHolder bookViewHolder, int i) {
        bookViewHolder.bookName.setText(books.get(i).name);
        bookViewHolder.bookDescription.setText(books.get(i).description);
        //personViewHolder.bookImage.setImageResource(books.get(i).imageId);
    }

    @Override
    public int getItemCount() {
        return books.size();
    }

}
