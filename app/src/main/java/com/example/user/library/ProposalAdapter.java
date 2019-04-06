package com.example.user.library;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;

public class ProposalAdapter extends RecyclerView.Adapter<ProposalAdapter.ProposalViewHolder> {
    private Context context;
    List<Proposal> proposals;

    public ProposalAdapter(Context context, List<Proposal> proposals) {
        this.context = context;
        this.proposals = proposals;
    }

    public class ProposalViewHolder extends RecyclerView.ViewHolder {
        private ImageView bookCover;
        private TextView bookName;
        private TextView proposalStatus;
        private TextView proposalcreateDate;

        public ProposalViewHolder(View itemView) {
            super(itemView);
            bookCover = itemView.findViewById(R.id.book_cover);
            bookName = itemView.findViewById(R.id.book_name);
            proposalStatus = itemView.findViewById(R.id.proposal_status);
            proposalcreateDate = itemView.findViewById(R.id.proposal_date);
        }

        public void setData(String name, String status, String date) {
            bookName.setText(name);
            proposalStatus.setText(status);
            proposalcreateDate.setText(date);
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
