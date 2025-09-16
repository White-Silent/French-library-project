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

    @Autowired
    private BorrowDAO borrowDAO;

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private BookDAO bookDAO;

    /**
     * Emprunter un livre
     */
    public void borrowBook(User user, Book book) throws SQLException {
        if (user.getRole() != Role.READER) {
            throw new RuntimeException("Seuls les lecteurs peuvent emprunter des livres !");
        }

        // Vérifier si l'utilisateur peut emprunter ce livre
        if (!borrowDAO.canUserBorrowBook(user.getId(), book.getId())) {
            throw new RuntimeException("Ce livre n'est pas disponible ou vous l'avez déjà emprunté");
        }

        // Vérifier la limite d'emprunts (par exemple, max 5 livres)
        int activeBorrows = borrowDAO.countActiveBorrowsByUser(user.getId());
        if (activeBorrows >= 5) {
            throw new RuntimeException("Vous avez atteint la limite de 5 emprunts simultanés");
        }

        LocalDate today = LocalDate.now();
        LocalDate dueDate = today.plusDays(14);

        Borrow borrow = new Borrow(user, book, today, dueDate, BorrowStatus.BORROWED);
        borrowDAO.borrowBook(borrow);

        // Marquer le livre comme indisponible
        bookDAO.updateBookAvailability(book.getId(), false);
    }

    /**
     * Retourner un livre
     */
    public void returnBook(int borrowId) throws SQLException {
        // D'abord, récupérer l'emprunt pour obtenir les informations du livre
        Borrow borrow = getBorrowById(borrowId);
        if (borrow == null) {
            throw new RuntimeException("Emprunt non trouvé");
        }

        if (borrow.getStatus() == BorrowStatus.RETURNED) {
            throw new RuntimeException("Ce livre a déjà été retourné");
        }

        // Retourner le livre
        LocalDate today = LocalDate.now();
        borrowDAO.returnBook(borrowId, today);

        // Rendre le livre disponible
        bookDAO.updateBookAvailability(borrow.getBook().getId(), true);
    }

    /**
     * Renouveler un emprunt
     */
    public void renewBook(int borrowId) throws SQLException {
        Borrow borrow = getBorrowById(borrowId);
        if (borrow == null) {
            throw new RuntimeException("Emprunt non trouvé");
        }

        if (borrow.getStatus() != BorrowStatus.BORROWED) {
            throw new RuntimeException("Seuls les emprunts actifs peuvent être renouvelés");
        }

        // Vérifier si le livre n'est pas en retard
        if (LocalDate.now().isAfter(borrow.getDueDate())) {
            throw new RuntimeException("Impossible de renouveler un livre en retard");
        }

        // Prolonger de 14 jours
        LocalDate newDueDate = borrow.getDueDate().plusDays(14);
        borrowDAO.renewBook(borrowId, newDueDate);
    }

    /**
     * Récupérer un emprunt par son ID
     */
    public Borrow getBorrowById(int borrowId) throws SQLException {
        // Cette méthode doit être implémentée dans BorrowDAO
        return borrowDAO.getBorrowById(borrowId);
    }

    /**
     * Récupérer tous les emprunts d'un utilisateur
     */
    public List<Borrow> getAllBorrowsByUser(int userId) throws SQLException {
        return borrowDAO.getBorrowsByUser(userId);
    }

    /**
     * Récupérer les emprunts actifs d'un utilisateur
     */
    public List<Borrow> getActiveBorrowsByUser(int userId) throws SQLException {
        return borrowDAO.getActiveBorrowsByUser(userId);
    }

    /**
     * Emprunter un livre (méthode alternative avec IDs)
     */
    public void borrowBook(int userId, int bookId) throws SQLException {
        User user = userDAO.getUserById(userId);
        Book book = bookDAO.getBookById(bookId);

        if (user == null) {
            throw new RuntimeException("Utilisateur non trouvé");
        }

        if (book == null) {
            throw new RuntimeException("Livre non trouvé");
        }

        borrowBook(user, book);
    }
}