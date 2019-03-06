package com.example.user.library;

public class Book {
    String name;
    String description;
    int imageId;

    Book(String name, String description, int imageId){
        this.name = name;
        this.description = description;
        this.imageId = imageId;
    }

    Book(String name, String description){
        this.name = name;
        this.description = description;
        this.imageId = 0;
    }


}
