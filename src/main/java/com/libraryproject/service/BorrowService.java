package com.libraryproject.service;

import com.libraryproject.dao.BookDAO;
import com.libraryproject.dao.BorrowDAO;
import com.libraryproject.model.Book;
import com.libraryproject.model.Borrow;
import com.libraryproject.model.User;
import com.libraryproject.enums.BorrowStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

@Service
public class BorrowService {

    @Autowired
    private BorrowDAO borrowDAO;

    @Autowired
    private BookDAO bookDAO;

    private static final int BORROW_DURATION_DAYS = 14;
    private static final int MAX_BORROWS_PER_USER = 5; // Limite d'emprunts par utilisateur

    /**
     * Emprunter un livre
     */
    @Transactional
    public void borrowBook(User user, Book book) throws Exception {
        try {
            // Vérifications préliminaires
            if (user == null) {
                throw new Exception("Utilisateur non trouvé");
            }

            if (book == null) {
                throw new Exception("Livre non trouvé");
            }

            // 1. Vérifier si le livre est disponible
            if (!bookDAO.isBookAvailable(book.getId())) {
                throw new Exception("Ce livre n'est pas disponible pour l'emprunt");
            }

            // 2. Vérifier si l'utilisateur n'a pas déjà emprunté ce livre
            if (!borrowDAO.canUserBorrowBook(user.getId(), book.getId())) {
                throw new Exception("Vous avez déjà emprunté ce livre");
            }

            // 3. Vérifier le nombre d'emprunts actifs de l'utilisateur
            int activeBorrows = borrowDAO.countActiveBorrowsByUser(user.getId());
            if (activeBorrows >= MAX_BORROWS_PER_USER) {
                throw new Exception("Vous avez atteint la limite de " + MAX_BORROWS_PER_USER + " emprunts simultanés");
            }

            // 4. Créer l'emprunt
            LocalDate borrowDate = LocalDate.now();
            LocalDate dueDate = borrowDate.plusDays(BORROW_DURATION_DAYS);

            Borrow borrow = new Borrow(user, book, borrowDate, dueDate, BorrowStatus.BORROWED);

            // 5. Enregistrer l'emprunt (cela met automatiquement le livre en indisponible)
            borrowDAO.borrowBook(borrow);

            System.out.println("Emprunt créé avec succès pour le livre: " + book.getTitle() + " par l'utilisateur: " + user.getUsername());

        } catch (SQLException e) {
            System.err.println("Erreur SQL lors de l'emprunt: " + e.getMessage());
            throw new Exception("Erreur technique lors de l'emprunt: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Erreur lors de l'emprunt: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Retourner un livre
     */
    @Transactional
    public void returnBook(int borrowId) throws Exception {
        try {
            // 1. Récupérer l'emprunt
            Borrow borrow = borrowDAO.getBorrowById(borrowId);
            if (borrow == null) {
                throw new Exception("Emprunt non trouvé");
            }

            // 2. Vérifier que le livre n'est pas déjà retourné
            if (borrow.getReturnDate() != null) {
                throw new Exception("Ce livre a déjà été retourné");
            }

            // 3. Marquer le livre comme retourné
            LocalDate returnDate = LocalDate.now();
            borrowDAO.returnBook(borrowId, returnDate);

            // 4. Remettre le livre en disponible
            bookDAO.updateBookAvailability(borrow.getBook().getId(), true);

            System.out.println("Livre retourné avec succès: " + borrow.getBook().getTitle());

        } catch (SQLException e) {
            System.err.println("Erreur SQL lors du retour: " + e.getMessage());
            throw new Exception("Erreur technique lors du retour: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Erreur lors du retour: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Renouveler un emprunt
     */
    @Transactional
    public void renewBook(int borrowId) throws Exception {
        try {
            // 1. Récupérer l'emprunt
            Borrow borrow = borrowDAO.getBorrowById(borrowId);
            if (borrow == null) {
                throw new Exception("Emprunt non trouvé");
            }

            // 2. Vérifier que le livre n'est pas retourné
            if (borrow.getReturnDate() != null) {
                throw new Exception("Impossible de renouveler un livre déjà retourné");
            }

            // 3. Calculer la nouvelle date d'échéance (14 jours supplémentaires)
            LocalDate newDueDate = borrow.getDueDate().plusDays(14);;

            // 4. Mettre à jour la date d'échéance
            borrowDAO.renewBook(borrowId, newDueDate);

            System.out.println("Emprunt renouvelé jusqu'au: " + newDueDate);

        } catch (SQLException e) {
            System.err.println("Erreur SQL lors du renouvellement: " + e.getMessage());
            throw new Exception("Erreur technique lors du renouvellement: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Erreur lors du renouvellement: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Récupérer tous les emprunts d'un utilisateur
     */
    public List<Borrow> getAllBorrowsByUser(int userId) throws Exception {
        try {
            return borrowDAO.getBorrowsByUser(userId);
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des emprunts: " + e.getMessage());
            throw new Exception("Erreur lors de la récupération des emprunts: " + e.getMessage());
        }
    }

    /**
     * Récupérer les emprunts actifs d'un utilisateur
     */
    public List<Borrow> getActiveBorrowsByUser(int userId) throws Exception {
        try {
            return borrowDAO.getActiveBorrowsByUser(userId);
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des emprunts actifs: " + e.getMessage());
            throw new Exception("Erreur lors de la récupération des emprunts actifs: " + e.getMessage());
        }
    }

    /**
     * Vérifier si un utilisateur peut emprunter un livre
     */
    public boolean canUserBorrowBook(int userId, int bookId) throws Exception {
        try {
            return borrowDAO.canUserBorrowBook(userId, bookId) && bookDAO.isBookAvailable(bookId);
        } catch (SQLException e) {
            System.err.println("Erreur lors de la vérification: " + e.getMessage());
            throw new Exception("Erreur lors de la vérification: " + e.getMessage());
        }
    }

    /**
     * Compter les emprunts actifs d'un utilisateur
     */
    public int countActiveBorrowsByUser(int userId) throws Exception {
        try {
            return borrowDAO.countActiveBorrowsByUser(userId);
        } catch (SQLException e) {
            System.err.println("Erreur lors du comptage: " + e.getMessage());
            throw new Exception("Erreur lors du comptage: " + e.getMessage());
        }
    }
}