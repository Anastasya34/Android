package com.example.user.library;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;

public class ProposalAdapter extends RecyclerView.Adapter<ProposalAdapter.ProposalViewHolder> {
    private Context context;
    List<Proposal> proposals;
    private BookReturnClickListener bookReturnClickListener;
    private String type;
    public interface BookReturnClickListener {
        void onBookReturnButtonClick(int position, View v);
    }
    public ProposalAdapter(Context context, List<Proposal> proposals) {
        this.context = context;
        this.proposals = proposals;
    }
    public ProposalAdapter(Context context, List<Proposal> proposals,BookReturnClickListener bookReturnClickListener, String type) {
        this.context = context;
        this.proposals = proposals;
        this.bookReturnClickListener = bookReturnClickListener;
        this.type = type;
    }

    public class ProposalViewHolder extends RecyclerView.ViewHolder {
        private ImageView bookCover;
        private TextView bookName;
        private TextView proposalStatus;
        private TextView proposalcreateDate;
        private Button returnButton;

        public ProposalViewHolder(View itemView) {
            super(itemView);
            bookCover = itemView.findViewById(R.id.book_cover);
            bookName = itemView.findViewById(R.id.book_name);
            proposalStatus = itemView.findViewById(R.id.proposal_status);
            proposalcreateDate = itemView.findViewById(R.id.proposal_date);
            returnButton = itemView.findViewById(R.id.return_book_button);
            returnButton.setVisibility(View.GONE);
            returnButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //User user = books.get(getLayoutPosition());
                    bookReturnClickListener.onBookReturnButtonClick(getAdapterPosition(), v);
                }
            });
        }

        public void setData(String name, String status, String date) {
            bookName.setText(name);
            proposalStatus.setText("Статус: "+status);
            proposalcreateDate.setText("Дата создания заявки: "+date);
            if (status.equals(Constants.stasusDictionary.get(0)) && type.equals("UserMyProposalsFragment")){
                returnButton.setVisibility(View.VISIBLE);
                returnButton.setText(R.string.cancel_proposal);
            }
            if (status.equals(Constants.stasusDictionary.get(5))&& type.equals("UserMyBooksFragment")){
                returnButton.setVisibility(View.VISIBLE);
                returnButton.setText(R.string.confirm_return);
            }
        }


    }
    @Override
    public ProposalAdapter.ProposalViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d("onCreateViewHolder", "test");
        View proposalCard = LayoutInflater.from(this.context).inflate(R.layout.proposal_card, parent, false);
        final ProposalAdapter.ProposalViewHolder vh =  new ProposalAdapter.ProposalViewHolder(proposalCard);
        return vh;
    }

    //заполняет объект данными
    @Override
    public void onBindViewHolder(ProposalAdapter.ProposalViewHolder holder, int position) {
        Log.d("onBindViewHolder",String.valueOf(position));
        Proposal proposal = proposals.get(position);
        holder.setData(proposal.bookName, proposal.proposalStatus, proposal.proposalcreateDate);
    }

    @Override
    public int getItemCount() {
        return proposals.size();
    }
}
