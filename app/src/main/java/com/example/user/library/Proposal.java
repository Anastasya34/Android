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
    public String bookId;
    public String userId;
    public String userLogin;
    public String proposalId;

    public Proposal(String bookId, int imageId, String proposalStatus, String proposalcreateDate) {
        this.bookId = bookId;
        this.imageId = imageId;
        this.proposalStatus = proposalStatus;
        this.proposalcreateDate = proposalcreateDate;
    }
    public Proposal(String bookId, int imageId, String proposalStatus, String proposalcreateDate, String userId) {
        this.bookId = bookId;
        this.imageId = imageId;
        this.proposalStatus = proposalStatus;
        this.proposalcreateDate = proposalcreateDate;
        this.userId = userId;
    }
    public Proposal(String bookId, int imageId, String proposalStatus, String proposalcreateDate, String userId, String proposalId) {
        this.bookId = bookId;
        this.imageId = imageId;
        this.proposalStatus = proposalStatus;
        this.proposalcreateDate = proposalcreateDate;
        this.userId = userId;
        this.proposalId = proposalId;
    }
    /*public Proposal(String bookId, int imageId, String bookName, String proposalStatus, String proposalcreateDate) {
        this.bookId = bookId;
        this.imageId = imageId;
        this.bookName = bookName;
        this.proposalStatus = proposalStatus;
        this.proposalcreateDate = proposalcreateDate;
    }*/
}
