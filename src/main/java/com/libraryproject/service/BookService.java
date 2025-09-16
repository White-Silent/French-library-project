package com.libraryproject.service;

import com.libraryproject.dao.BookDAO;
import com.libraryproject.model.Book;
import com.libraryproject.model.User;
import com.libraryproject.enums.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;

@Service
public class BookService {

    @Autowired
    private BookDAO bookDAO;

    /**
     * Ajouter un livre (ADMIN uniquement)
     */
    public void addBook(Book book, User user) throws Exception {
        if (user.getRole() != Role.ADMIN) {
            throw new Exception("Seuls les administrateurs peuvent ajouter des livres");
        }

        try {
            // Vérifier si un livre avec le même titre existe déjà
            Book existingBook = bookDAO.getBookByExactTitle(book.getTitle());
            if (existingBook != null) {
                throw new Exception("Un livre avec ce titre existe déjà");
            }

            // Par défaut, un nouveau livre est disponible
            book.setAvailable(true);

            bookDAO.addBook(book);
            System.out.println("Livre ajouté avec succès: " + book.getTitle());
        } catch (SQLException e) {
            System.err.println("Erreur SQL lors de l'ajout: " + e.getMessage());
            throw new Exception("Erreur lors de l'ajout du livre: " + e.getMessage());
        }
    }

    /**
     * Récupérer un livre par ID
     */
    public Book getBookById(int id) throws Exception {
        try {
            return bookDAO.getBookById(id);
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération du livre: " + e.getMessage());
            throw new Exception("Erreur lors de la récupération du livre: " + e.getMessage());
        }
    }

    /**
     * Récupérer tous les livres (ADMIN) ou seulement les disponibles (READER)
     */
    public List<Book> getAllBooks(User user) throws Exception {
        try {
            if (user.getRole() == Role.ADMIN) {
                // Les admins voient tous les livres
                return bookDAO.getAllBooks();
            } else {
                // Les lecteurs ne voient que les livres disponibles
                return bookDAO.getAvailableBooks();
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des livres: " + e.getMessage());
            throw new Exception("Erreur lors de la récupération des livres: " + e.getMessage());
        }
    }

    /**
     * Récupérer tous les livres (pour les statistiques admin)
     */
    public List<Book> getAllBooks() throws Exception {
        try {
            return bookDAO.getAllBooks();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de tous les livres: " + e.getMessage());
            throw new Exception("Erreur lors de la récupération de tous les livres: " + e.getMessage());
        }
    }

    /**
     * Récupérer uniquement les livres disponibles
     */
    public List<Book> getAvailableBooks() throws Exception {
        try {
            return bookDAO.getAvailableBooks();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des livres disponibles: " + e.getMessage());
            throw new Exception("Erreur lors de la récupération des livres disponibles: " + e.getMessage());
        }
    }

    /**
     * Compter le nombre de livres disponibles
     */
    public int countAvailableBooks() throws Exception {
        try {
            return bookDAO.countAvailableBooks();
        } catch (SQLException e) {
            System.err.println("Erreur lors du comptage des livres disponibles: " + e.getMessage());
            throw new Exception("Erreur lors du comptage des livres disponibles: " + e.getMessage());
        }
    }

    /**
     * Mettre à jour un livre (ADMIN uniquement)
     */
    public void updateBook(Book book, User user) throws Exception {
        if (user.getRole() != Role.ADMIN) {
            throw new Exception("Seuls les administrateurs peuvent modifier des livres");
        }

        try {
            // Vérifier que le livre existe
            Book existingBook = bookDAO.getBookById(book.getId());
            if (existingBook == null) {
                throw new Exception("Livre non trouvé avec l'ID: " + book.getId());
            }

            bookDAO.updateBook(book);
            System.out.println("Livre mis à jour avec succès: " + book.getTitle());
        } catch (SQLException e) {
            System.err.println("Erreur SQL lors de la mise à jour: " + e.getMessage());
            throw new Exception("Erreur lors de la mise à jour du livre: " + e.getMessage());
        }
    }

    /**
     * Supprimer un livre (ADMIN uniquement)
     */
    public void deleteBook(int id, User user) throws Exception {
        if (user.getRole() != Role.ADMIN) {
            throw new Exception("Seuls les administrateurs peuvent supprimer des livres");
        }

        try {
            // Vérifier que le livre existe
            Book existingBook = bookDAO.getBookById(id);
            if (existingBook == null) {
                throw new Exception("Livre non trouvé avec l'ID: " + id);
            }

            // Vérifier que le livre n'est pas emprunté
            if (!existingBook.getAvailable()) {
                throw new Exception("Impossible de supprimer un livre actuellement emprunté");
            }

            bookDAO.deleteBook(id);
            System.out.println("Livre supprimé avec succès: " + existingBook.getTitle());
        } catch (SQLException e) {
            System.err.println("Erreur SQL lors de la suppression: " + e.getMessage());
            throw new Exception("Erreur lors de la suppression du livre: " + e.getMessage());
        }
    }

    /**
     * Rechercher des livres par titre
     */
    public List<Book> findBookByTitle(String title) throws Exception {
        try {
            return bookDAO.findBookByTitle(title);
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche par titre: " + e.getMessage());
            throw new Exception("Erreur lors de la recherche par titre: " + e.getMessage());
        }
    }

    /**
     * Rechercher des livres par auteur
     */
    public List<Book> findBookByAuthor(String author) throws Exception {
        try {
            return bookDAO.findBookByAuthor(author);
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche par auteur: " + e.getMessage());
            throw new Exception("Erreur lors de la recherche par auteur: " + e.getMessage());
        }
    }

    /**
     * Vérifier si un livre est disponible pour l'emprunt
     */
    public boolean isBookAvailable(int bookId) throws Exception {
        try {
            return bookDAO.isBookAvailable(bookId);
        } catch (SQLException e) {
            System.err.println("Erreur lors de la vérification de disponibilité: " + e.getMessage());
            throw new Exception("Erreur lors de la vérification de disponibilité: " + e.getMessage());
        }
    }

    /**
     * Mettre à jour la disponibilité d'un livre
     */
    public void updateBookAvailability(int bookId, boolean available) throws Exception {
        try {
            bookDAO.updateBookAvailability(bookId, available);
            System.out.println("Disponibilité du livre " + bookId + " mise à jour: " + available);
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour de disponibilité: " + e.getMessage());
            throw new Exception("Erreur lors de la mise à jour de disponibilité: " + e.getMessage());
        }
    }
}