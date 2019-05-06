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
    private String type = "default";
    public interface BookReturnClickListener {
        void onBookReturnButtonClick(int position, View v);
    }
    public ProposalAdapter(Context context, List<Proposal> proposals) {
        this.context = context;
        this.proposals = proposals;
    }
    public ProposalAdapter(Context context, List<Proposal> proposals,BookReturnClickListener bookReturnClickListener, String type, String userLogin) {
        this.context = context;
        this.proposals = proposals;
        this.bookReturnClickListener = bookReturnClickListener;
        this.type = type;
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
        private TextView userLogin;
        private Button button;

        public ProposalViewHolder(View itemView) {
            super(itemView);
            bookCover = itemView.findViewById(R.id.book_cover);
            bookName = itemView.findViewById(R.id.book_name);
            userLogin = itemView.findViewById(R.id.user_name);
            userLogin.setVisibility(View.GONE);
            proposalStatus = itemView.findViewById(R.id.proposal_status);
            proposalcreateDate = itemView.findViewById(R.id.proposal_date);
            button = itemView.findViewById(R.id.return_book_button);
            button.setVisibility(View.GONE);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //User user = books.get(getLayoutPosition());
                    bookReturnClickListener.onBookReturnButtonClick(getAdapterPosition(), v);
                }
            });
        }

        public void setData(Proposal proposal) {
            String status = proposal.proposalStatus;
            bookName.setText(proposal.bookName);
            if (proposal.userLogin != null){
                userLogin.setVisibility(View.VISIBLE);
                userLogin.setText("Ник: " + proposal.userLogin);
            }
            proposalStatus.setText("Статус: "+status);
            proposalcreateDate.setText("Дата создания заявки: "+ proposal.proposalcreateDate);
            Log.d("AdminAllProposalsFragm", type);
            //ToDo переделать на switch
            if (status.equals(Constants.stasusDictionary.get(0)) && type.equals("UserMyProposalsFragment")){
                button.setVisibility(View.VISIBLE);
                button.setText(R.string.cancel_proposal);
            }
            if (status.equals(Constants.stasusDictionary.get(5))&& type.equals("UserMyBooksFragment")){
                button.setVisibility(View.VISIBLE);
                button.setText(R.string.confirm_return);
            }
            if (type.equals("AdminAllProposalsFragment")){
                Log.d("AdminAllProposalsFragm", "tyt");
                button.setVisibility(View.VISIBLE);
                button.setText(R.string.process_button);
            }
            if (type.equals("AdminMyProposals_ApprovedProposalsFragment")){
                button.setVisibility(View.VISIBLE);
                button.setText(R.string.disband_button);
            }
            if (type.equals("AdminMyProposals_ReturnProposalsFragment")){
                button.setVisibility(View.VISIBLE);
                button.setText(R.string.approve_return);
            }
            if (type.equals("AdminMyProposals_NewProposalsFragment")){
                button.setVisibility(View.VISIBLE);
                button.setText(R.string.consider_button);
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
        holder.setData(proposals.get(position));
    }

    @Override
    public int getItemCount() {
        return proposals.size();
    }
}
