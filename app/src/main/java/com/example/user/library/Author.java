package com.example.user.library;

public class Author {
    String firstName;
    String secondName;
    String surName;

    public Author(String firstName, String secondName, String surName) {
        this.firstName = firstName;
        this.secondName = secondName;
        this.surName = surName;
    }
    @Override
    public int hashCode() {
        String str = surName+secondName+firstName;
        return str.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        Author otherAuthor = (Author) obj;
        if (this.firstName.equals(otherAuthor.firstName)
                && this.secondName.equals(otherAuthor.secondName)
                && this.surName.equals(otherAuthor.surName)) {
            return true;
        }
        return true;
    }

    String toInsertString(){
        return "('" + this.surName + "', '" + this.firstName +"', '" + this.secondName+"')";
    }
    String toSelectString(){
        return "([authorsurname] = '" + this.surName + "'" +
                " AND [authorfirstname] = '" + this.firstName + "'" +
                " AND [authorsecondname] = '"+this.secondName +"')";
    }
}
