package com.libraryproject.dao;

import com.libraryproject.model.Book;

import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BookDAOTest {
    //Attributes
    //Stock connection of my SQL
    private static Connection connection;
    //The Same object book DAO ( Data Access Object )
    private static BookDAO bookDAO;

    //Execute one time before all of the tests
    @BeforeAll
    static void setUpDatabase() throws SQLException {
        //Chaîne de connexion JDBC
        String url = "jdbc:mysql://192.168.101.155:3306/bibliotheque_test";
        //User & Password -> Droit pour accéder à la base "bibliotheque_test"
        String user = "javauser";
        String password = "Daumesnil504!";
        //Establish a connection
        connection = DriverManager.getConnection(url, user, password);
        //bookDAO use this connection in order to dialog with the database
        bookDAO = new BookDAO(connection);
    }

    @AfterAll
    static void tearDownDatabase() throws SQLException {
        connection.close();
    }

    @BeforeEach
    void cleanTable() throws SQLException {
        connection.prepareStatement("DELETE FROM borrows").executeUpdate();
        connection.prepareStatement("DELETE FROM books").executeUpdate();
    }

    @Test
    void testAddAndGetAllBooks() throws SQLException{
        Book book = new Book("JUnit Test Book", "Test", "TestAuth", "TestPub", "English", 9.99, "2024-01-01", "Testing DAO");

        bookDAO.addBook(book);
        List<Book> books = bookDAO.getAllBooks();

        assertEquals(1, books.size());
        Assertions.assertEquals("JUnit Test Book", books.get(0).getTitle());
    }

    @Test
    void testUpdateBook() throws SQLException{
        //Creation of the new Book
        Book book = new Book("Hunger Games", "Action", "Colins Suzins", "Someone", "English", 10.99, "2004-05-31", "Win or Lose");
        bookDAO.addBook(book);

        //Refactor
        Book changeTitle = bookDAO.getAllBooks().get(0);
        changeTitle.setTitle("Hunger Games Refactor");
        bookDAO.updateBook(changeTitle);

        //Test the update
        Book updated = bookDAO.getAllBooks().get(0);
        Assertions.assertEquals("Hunger Games Refactor", updated.getTitle());
    }

    @Test
    void deleteBook() throws SQLException{
        //Creation of a book
        Book catBook = new Book("CatCat", "Cat", "AuthorCat", "CatPublisher", "French", 5.0, "2020-02-20", "Miaou-Miaou");
        bookDAO.addBook(catBook);

        //retrieve the id in order to delete
        int retrieveId = bookDAO.getAllBooks().get(0).getId();
        bookDAO.deleteBook(retrieveId);
        Assertions.assertTrue(bookDAO.getAllBooks().isEmpty());
    }

    @Test
    void searchbyAuthor() throws SQLException{
        Book b1 = new Book("Le Petit Prince", "Children", "Antoine de Saint-Exupéry",
                "Gallimard", "French", 12.5, "1943-04-06", "Classic French tale");
        Book b2 = new Book("Hunger Games", "Adventure", "Suzanne Collins",
                "Scholastic", "English", 10.99, "2008-09-14", "Dystopian novel");

        bookDAO.addBook(b1);
        bookDAO.addBook(b2);

        List<Book> byTitle = bookDAO.findBookByTitle("Prince");
        for (Book book : byTitle) {
            System.out.println(book.getTitle());
        }

        List<Book> byAuthor = bookDAO.findBookByAuthor("Suzanne");
        for (Book book : byAuthor) {
            System.out.println(book.getAuthor());
        }
    }
}
