package com.example.user.library;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class BookListAdapter extends RecyclerView.Adapter<BookListAdapter.BookViewHolder> {
    public interface ButtonClickListener {
        void onButtonClick(int position, View v);
    }
    private ButtonClickListener buttonClickListener;

    public class BookViewHolder extends RecyclerView.ViewHolder {

        CardView bookCardView;
        TextView bookName;
        TextView bookDescription;
        ImageView bookImage;
        private Button ussueProposalButton;

        BookViewHolder(View itemView) {
            super(itemView);
            bookCardView = itemView.findViewById(R.id.bookCard);
            bookName = itemView.findViewById(R.id.book_name);
            bookDescription = itemView.findViewById(R.id.book_info);
            bookImage = itemView.findViewById(R.id.book_icon);
            ussueProposalButton = itemView.findViewById(R.id.issue_proposal_button);
            ussueProposalButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //User user = books.get(getLayoutPosition());
                    buttonClickListener.onButtonClick(getAdapterPosition(), v);
                }
            });
        }
    }

    List<Book> books;

    BookListAdapter(List<Book> books){
        this.books = books;
    }

    BookListAdapter(List<Book> books, ButtonClickListener buttonClickListener){
        this.books = books;
        this.buttonClickListener = buttonClickListener;
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
        if (books.get(i).already_get) {
            Log.d("already_get", books.get(i).bookId);
            bookViewHolder.ussueProposalButton.setVisibility(View.GONE);
        }
        bookViewHolder.bookName.setText(books.get(i).name);
        bookViewHolder.bookDescription.setText(books.get(i).description);
        //personViewHolder.bookImage.setImageResource(books.get(i).imageId);
    }

    @Override
    public int getItemCount() {
        return books.size();
    }

}
