package com.libraryproject.model;

import com.libraryproject.enums.BorrowStatus;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Borrow {

    //Attributes
    private  int id;
    private User user;
    private Book book;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private BorrowStatus status;

    //Constructor
    public Borrow(User user, Book book, LocalDate borrowDate, LocalDate dueDate, BorrowStatus status) {
        this.user = user;
        this.book = book;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.status = status;
    }


    @OneToMany(mappedBy = "book", fetch = FetchType.LAZY)
    private List<Borrow> borrows = new ArrayList<>();

    //Lire dispo s'il n'a pas d'emprunt actif
    public boolean isAvailable(){
        return borrows.stream().noneMatch(borrow ->
                borrow.getStatus() == BorrowStatus.BORROWED ||
                        borrow.getStatus() == BorrowStatus.LATE
        );
    }

    //Getter & Setter
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public LocalDate getBorrowDate() {
        return borrowDate;
    }

    public void setBorrowDate(LocalDate borrowDate) {
        this.borrowDate = borrowDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public BorrowStatus getStatus() {
        return status;
    }

    public void setStatus(BorrowStatus status) {
        this.status = status;
    }
}
