package com.libraryproject.service;

import com.libraryproject.dao.BookDAO;
import com.libraryproject.dao.BorrowDAO;
import com.libraryproject.dao.UserDAO;
import com.libraryproject.enums.BorrowStatus;
import com.libraryproject.enums.Role;
import com.libraryproject.model.Book;
import com.libraryproject.model.Borrow;
import com.libraryproject.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.time.LocalDate;
import java.util.List;

public class BorrowServiceTest {

    private BorrowService borrowService;
    private BorrowDAO borrowDAO;
    private UserDAO userDAO;
    private BookDAO bookDAO;
    private User reader;
    private User admin;
    private Book bookTest;
    private Connection connection;

    @BeforeEach
    void setup() throws Exception {
        connection = DriverManager.getConnection(
                "jdbc:mysql://192.168.101.214:3306/bibliotheque_test",
                "javauser",
                "Daumesnil504!"
        );

        userDAO = new UserDAO(connection);
        bookDAO = new BookDAO(connection);
        borrowDAO = new BorrowDAO(connection);
        borrowService = new BorrowService(borrowDAO);

        // Supprimer les anciennes données
        connection.prepareStatement("DELETE FROM borrows").executeUpdate();
        connection.prepareStatement("DELETE FROM books").executeUpdate();
        connection.prepareStatement("DELETE FROM users").executeUpdate();

        // Créer et insérer les users
        admin = new User("masterAdmin", "admin123", Role.ADMIN, null);
        reader = new User("reader", "reader123", Role.READER, null);
        userDAO.addUser(admin);
        userDAO.addUser(reader);

        // Récupérer les IDs générés
        admin = userDAO.getUserByUsername("masterAdmin");
        reader = userDAO.getUserByUsername("reader");

        // Créer et insérer le livre
        bookTest = new Book("TitleTest", "CategoryTest", "AuthorTest", "EditorTest",
                "French", 10.0, "2025-01-01", "DescriptionTest");
        bookDAO.addBook(bookTest);

        // Récupérer l'ID généré pour le livre
        bookTest = bookDAO.getBookByExactTitle("TitleTest");
    }

    @AfterEach
    void tearDown() throws Exception {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Test
    void testBorrowBook() throws Exception {
        // Emprunter le livre
        borrowService.borrowBook(reader, bookTest);

        // Récupérer les emprunts du reader
        List<Borrow> borrows = borrowService.getBorrowByUser(reader.getId());

        // Vérifications
        Assertions.assertEquals(1, borrows.size());
        Borrow borrow = borrows.get(0);
        Assertions.assertEquals(BorrowStatus.BORROWED, borrow.getStatus());
        Assertions.assertEquals(bookTest.getTitle(), borrow.getBook().getTitle());
    }

    @Test
    void testReturnBook() throws Exception {
        // 1️⃣ Emprunter le livre d'abord
        borrowService.borrowBook(reader, bookTest);

        // Récupérer l'emprunt du reader
        List<Borrow> borrowsBeforeReturn = borrowService.getBorrowByUser(reader.getId());
        Assertions.assertEquals(1, borrowsBeforeReturn.size());
        Borrow borrow = borrowsBeforeReturn.get(0);
        Assertions.assertEquals(BorrowStatus.BORROWED, borrow.getStatus());

        // 2️⃣ Retourner le livre
        borrowService.returnBook(borrow.getId());

        // 3️⃣ Vérifier que le statut a changé
        List<Borrow> borrowsAfterReturn = borrowService.getBorrowByUser(reader.getId());
        Borrow returnedBorrow = borrowsAfterReturn.get(0);
        Assertions.assertEquals(BorrowStatus.RETURNED, returnedBorrow.getStatus());

        // 4️⃣ Vérifier que la date de retour est bien aujourd'hui
        Assertions.assertEquals(LocalDate.now(), returnedBorrow.getReturnDate());
    }

}
