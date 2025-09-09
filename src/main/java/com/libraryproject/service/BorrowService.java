package com.libraryproject.service;

import com.libraryproject.dao.BorrowDAO;
import com.libraryproject.enums.BorrowStatus;
import com.libraryproject.enums.Role;
import com.libraryproject.model.Book;
import com.libraryproject.model.Borrow;
import com.libraryproject.model.User;

import java.sql.SQLException;
import java.time.LocalDate;

public class BorrowService {
    //Attribute
    private BorrowDAO borrowDAO;

    //Cosntructor
    public BorrowService(BorrowDAO borrowDAO) {
        this.borrowDAO = borrowDAO;
    }

    //Method
    //Borrow a book
    public void borrowBook(User user, Book book) throws SQLException {
        if (user.getRole() == Role.READER){
            LocalDate today = LocalDate.now();
            LocalDate due = today.plusDays(14);
            //Objet crée dans la mémoire mais rien dans la DB
            Borrow borrow = new Borrow(user, book, today, due, BorrowStatus.BORROWED);
            //A présent il est inscrit dans la DB
            borrowDAO.borrowBook(borrow);
        }
        else {
            throw new RuntimeException("Only Readers car borrow books !");
        }
    }

    //Return a book
    public void returnBook(int borrowId) throws SQLException {
        LocalDate today = LocalDate.now();
        borrowDAO.returnBook(borrowId, today);
    }
}
