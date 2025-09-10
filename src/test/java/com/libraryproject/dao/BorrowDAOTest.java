package com.libraryproject.dao;

import com.libraryproject.enums.BorrowStatus;
import com.libraryproject.enums.Role;
import com.libraryproject.model.Book;
import com.libraryproject.model.Borrow;
import com.libraryproject.model.User;

import org.junit.jupiter.api.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class BorrowDAOTest {

    private static Connection connection;
    private static BorrowDAO borrowDAO;
    private static UserDAO userDAO;
    private static BookDAO bookDAO;

    private User testUser;
    private Book testBook;

    @BeforeAll
    static void setUpDatabase() throws SQLException {
        String url = "jdbc:mysql://192.168.101.214:3306/bibliotheque_test";
        String user = "javauser";
        String password = "Daumesnil504!";
        connection = DriverManager.getConnection(url, user, password);

        borrowDAO = new BorrowDAO(connection);
        userDAO = new UserDAO(connection);
        bookDAO = new BookDAO(connection);
    }

    @AfterAll
    static void tearDownDatabase() throws SQLException {
        connection.close();
    }

    @BeforeEach
    void cleanTables() throws SQLException {
        connection.prepareStatement("DELETE FROM borrows").executeUpdate();
        connection.prepareStatement("DELETE FROM books").executeUpdate();
        connection.prepareStatement("DELETE FROM users").executeUpdate();

        // Create a test user
        testUser = new User("reader1", "pass123", Role.READER, "visa123");
        userDAO.addUser(testUser);
        testUser.setId(userDAO.authentificate("reader1", "pass123", Role.READER, "visa123").getId());

        // Create a test book
        testBook = new Book("JUnit Borrow Test", "Test", "TestAuthor", "TestPub", "EN", 9.99, "2025-01-01", "Testing BorrowDAO");
        bookDAO.addBook(testBook);
        testBook.setId(bookDAO.getAllBooks().get(0).getId());
    }

    @Test
    void testBorrowBookAndRetrieve() throws SQLException {
        LocalDate today = LocalDate.now();
        LocalDate due = today.plusDays(14);

        Borrow borrow = new Borrow(testUser, testBook, today, due, BorrowStatus.BORROWED);
        borrowDAO.borrowBook(borrow);

        List<Borrow> borrows = borrowDAO.getBorrowsByUser(testUser.getId());
        assertEquals(1, borrows.size());
        Assertions.assertEquals(BorrowStatus.BORROWED, borrows.get(0).getStatus());
        Assertions.assertEquals(testBook.getTitle(), borrows.get(0).getBook().getTitle());
    }

    @Test
    void testReturnBook() throws SQLException {
        LocalDate today = LocalDate.now();
        LocalDate due = today.plusDays(14);

        Borrow borrow = new Borrow(testUser, testBook, today, due, BorrowStatus.BORROWED);
        borrowDAO.borrowBook(borrow);

        int borrowId = borrowDAO.getBorrowsByUser(testUser.getId()).get(0).getId();
        borrowDAO.returnBook(borrowId, today);

        Borrow returned = borrowDAO.getBorrowsByUser(testUser.getId()).get(0);
        Assertions.assertEquals(BorrowStatus.RETURNED, returned.getStatus());
        Assertions.assertEquals(today, returned.getReturnDate());
    }
}
