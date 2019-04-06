package com.example.user.library;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class Proposal{
    public int imageId;
    public String bookName;
    public String proposalStatus;
    public String proposalcreateDate;


    public Proposal(int imageId, String bookName, String proposalStatus, String proposalcreateDate) {
        this.imageId = imageId;
        this.bookName = bookName;
        this.proposalStatus = proposalStatus;
        this.proposalcreateDate = proposalcreateDate;
    }
}
