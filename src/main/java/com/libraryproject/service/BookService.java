package com.libraryproject.service;

import com.libraryproject.dao.BookDAO;
import com.libraryproject.enums.Role;
import com.libraryproject.model.Book;
import com.libraryproject.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;

@Service
public class BookService {

    @Autowired
    private BookDAO bookDAO;

    public void addBook(Book book, User user) throws SQLException {
        System.out.println("Service: Tentative d'ajout de livre par l'utilisateur: " + user.getUsername());

        if (user.getRole() == Role.READER) {
            throw new RuntimeException("You don't have the permission to add a book !");
        }

        // Validation supplémentaire
        if (book == null) {
            throw new IllegalArgumentException("Le livre ne peut pas être null");
        }

        if (book.getTitle() == null || book.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Le titre du livre est obligatoire");
        }

        if (book.getAuthor() == null || book.getAuthor().trim().isEmpty()) {
            throw new IllegalArgumentException("L'auteur du livre est obligatoire");
        }

        System.out.println("Service: Ajout du livre: " + book.toString());
        bookDAO.addBook(book);
        System.out.println("Service: Livre ajouté avec succès");
    }

    public Book getBookById(int id) throws SQLException {
        System.out.println("Service: Recherche du livre avec ID: " + id);

        if (id <= 0) {
            System.err.println("Service: ID invalide: " + id);
            throw new IllegalArgumentException("ID invalide: " + id);
        }

        Book book = bookDAO.getBookById(id);

        if (book == null) {
            System.err.println("Service: Aucun livre trouvé avec l'ID: " + id);
        } else {
            System.out.println("Service: Livre trouvé: " + book.toString());
        }

        return book;
    }

    public void updateBook(Book book, User user) throws SQLException {
        System.out.println("Service: Tentative de mise à jour de livre par l'utilisateur: " + user.getUsername());

        if (user.getRole() == Role.READER) {
            throw new RuntimeException("You don't have the permission to update a book !");
        }

        // Validations renforcées
        if (book == null) {
            throw new IllegalArgumentException("Le livre ne peut pas être null");
        }

        if (book.getId() <= 0) {
            throw new IllegalArgumentException("ID du livre invalide: " + book.getId());
        }

        if (book.getTitle() == null || book.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Le titre du livre est obligatoire");
        }

        if (book.getAuthor() == null || book.getAuthor().trim().isEmpty()) {
            throw new IllegalArgumentException("L'auteur du livre est obligatoire");
        }

        // Vérifier que le livre existe avant de le modifier
        Book existingBook = getBookById(book.getId());
        if (existingBook == null) {
            throw new RuntimeException("Impossible de modifier: livre non trouvé avec l'ID " + book.getId());
        }

        System.out.println("Service: Mise à jour du livre: " + book.toString());

        try {
            bookDAO.updateBook(book);
            System.out.println("Service: Livre mis à jour avec succès");
        } catch (SQLException e) {
            System.err.println("Service: Erreur lors de la mise à jour: " + e.getMessage());
            throw new SQLException("Erreur lors de la mise à jour du livre: " + e.getMessage(), e);
        }
    }

    public void deleteBook(int id, User user) throws SQLException {
        System.out.println("Service: Tentative de suppression du livre ID: " + id + " par l'utilisateur: " + user.getUsername());

        if (user.getRole() == Role.READER) {
            throw new RuntimeException("You don't have the permission to delete a book !");
        }

        if (id <= 0) {
            throw new IllegalArgumentException("ID invalide: " + id);
        }

        // Vérifier que le livre existe avant de le supprimer
        Book existingBook = getBookById(id);
        if (existingBook == null) {
            throw new RuntimeException("Impossible de supprimer: livre non trouvé avec l'ID " + id);
        }

        System.out.println("Service: Suppression du livre: " + existingBook.getTitle());
        bookDAO.deleteBook(id);
        System.out.println("Service: Livre supprimé avec succès");
    }

    public List<Book> getAllBooks(User user) throws SQLException {
        System.out.println("Service: Récupération de tous les livres pour l'utilisateur: " + user.getUsername());

        List<Book> books = bookDAO.getAllBooks();
        System.out.println("Service: " + (books != null ? books.size() : 0) + " livre(s) trouvé(s)");

        return books;
    }

    public List<Book> findBookByTitle(String title) throws SQLException {
        System.out.println("Service: Recherche de livres par titre: " + title);

        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Le titre de recherche ne peut pas être vide");
        }

        List<Book> books = bookDAO.findBookByTitle(title.trim());
        System.out.println("Service: " + (books != null ? books.size() : 0) + " livre(s) trouvé(s) pour le titre: " + title);

        return books;
    }

    public List<Book> findBookByAuthor(String author) throws SQLException {
        System.out.println("Service: Recherche de livres par auteur: " + author);

        if (author == null || author.trim().isEmpty()) {
            throw new IllegalArgumentException("L'auteur de recherche ne peut pas être vide");
        }

        List<Book> books = bookDAO.findBookByAuthor(author.trim());
        System.out.println("Service: " + (books != null ? books.size() : 0) + " livre(s) trouvé(s) pour l'auteur: " + author);

        return books;
    }
}