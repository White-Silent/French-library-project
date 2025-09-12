package com.libraryproject.service;

import com.libraryproject.dao.BookDAO;
import com.libraryproject.dao.BorrowDAO;
import com.libraryproject.dao.UserDAO;
import com.libraryproject.enums.BorrowStatus;
import com.libraryproject.enums.Role;
import com.libraryproject.model.Book;
import com.libraryproject.model.Borrow;
import com.libraryproject.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

@Service
public class BorrowService {
    //Attribute
    @Autowired
    private BorrowDAO borrowDAO;

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private BookDAO bookDAO;

    /*
    //Cosntructor
    public BorrowService(BorrowDAO borrowDAO) {
        this.borrowDAO = borrowDAO;
    }
     */
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

    //Get BorrowByUser ID
    public List<Borrow> getBorrowByUser(int userId) throws SQLException {
        return borrowDAO.getBorrowsByUser(userId);
    }

    // === GESTION DES EMPRUNTS ===

    public List<Borrow> getActiveBorrowsByUser(int userId) throws SQLException {
        return borrowDAO.getActiveBorrowsByUser(userId);
    }

    public List<Borrow> getAllBorrowsByUser(int userId) throws SQLException {
        return borrowDAO.getBorrowsByUser(userId);
    }

    /**
     * Emprunter un livre
     */
    public void borrowBook(int userId, int bookId) throws SQLException {
        // Vérifier que le livre est disponible
        if (!bookDAO.isBookAvailable(bookId)) {
            throw new RuntimeException("Ce livre n'est pas disponible");
        }

        // Créer l'emprunt
        User user = userDAO.getUserById(userId);
        Book book = bookDAO.getBookById(bookId);

        Borrow borrow = new Borrow(
                user,
                book,
                LocalDate.now(),
                LocalDate.now().plusWeeks(2), // 2 semaines d'emprunt
                BorrowStatus.BORROWED
        );

        borrowDAO.borrowBook(borrow);
    }
}
