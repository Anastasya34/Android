package com.example.user.library;

public class Book{
    String bookId;
    String name;
    String description;
    String fk1_dorm;
    String fk1_room;
    String fk1_board;
    String fk1_cupboard;
    Boolean already_get = false;
    Integer countBooks;
    int imageId;
    Book(String bookId){
        this.bookId = bookId;
    }
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
    public Book(String bookId, Integer countBooks) {
        this.bookId = bookId;
        this.countBooks = countBooks;
    }

    public Book(String bookId, String name, String fk1_dorm, String fk1_room, String fk1_board, String fk1_cupboard, Boolean already_get) {
        this.bookId = bookId;
        this.name = name;
        this.fk1_dorm = fk1_dorm;
        this.fk1_room = fk1_room;
        this.fk1_board = fk1_board;
        this.fk1_cupboard = fk1_cupboard;
        this.already_get = already_get;
        this.imageId = 0;
    }
}
