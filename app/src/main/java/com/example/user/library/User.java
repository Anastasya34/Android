package com.example.user.library;

public class User {
    public String userId;
    public String usersurname;
    public String userfirstname;
    public String userlogin;
    public String phonenumber;
    public String email;

    public User(String userId) {
        this.userId = userId;
    }

    public User(String userId, String usersurname, String userfirstname,String userlogin, String phonenumber, String email) {
        this.userId = userId;
        this.usersurname = usersurname;
        this.userfirstname = userfirstname;
        this.userlogin = userlogin;
        this.phonenumber = phonenumber;
        this.email = email;
    }
}
