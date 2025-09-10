package com.libraryproject.service;

import com.libraryproject.dao.BookDAO;
import com.libraryproject.dao.UserDAO;
import com.libraryproject.enums.Role;
import com.libraryproject.model.Book;
import com.libraryproject.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BookServiceTest {
    //Attribute
    private BookService bookService;
    private User admin;
    private User reader;
    private BookDAO bookDAO;

    private Connection connection;
    @BeforeEach
    void setup() throws Exception {
        connection = DriverManager.getConnection(
                "jdbc:mysql://192.168.101.214:3306/bibliotheque_test",
                "javauser",
                "Daumesnil504!"
        );
        bookDAO = new BookDAO(connection);
        bookService = new BookService(bookDAO);

        //Vider la table avant
        connection.prepareStatement("DELETE FROM books").executeUpdate();

        //Create a new user
        admin = new User("adminTest", "password", Role.ADMIN, null);
        reader = new User("readerTest", "password", Role.READER, null);
    }


    @AfterEach
    void tearDown() throws Exception {
        if (connection != null && !connection.isClosed()){
            connection.close();
        }
    }

    @Test
    void testAddWithDirector() throws Exception {
        Book book = new Book("TitleTest", "CategoryTest", "AuthorTest", "EditorTest", "French", 10.0, "2025-01-01", "DescriptionTest");
        assertDoesNotThrow(() -> bookService.addBook(book, admin));
    }

    @Test
    void testAddWithReaderFailing() throws Exception {
        Book book = new Book("Failure", "Fail", "M.Failer", "Miss Fail", "English", 10.0, "2025-01-01", "FailureDescription");
        Exception exception = assertThrows(RuntimeException.class, () -> bookService.addBook(book,reader));
        assertTrue(exception.getMessage().contains("You don't have the permission to add a book !"));
    }

    @Test
    void testUpdateBook() throws Exception {
        //Add le book
        Book book = new Book("TitleTest", "CategoryTest", "AuthorTest", "EditorTest", "French", 10.0, "2025-01-01", "DescriptionTest");
        //Rajoutes dans la DB Genre c'est quoi la DB déjà ?
        bookService.addBook(book, admin);
        // ça j'ai compris on change le nom du nouveau book donc le titre à présent c'est "New Title"
        book.setTitle("New Title");
        //le bookService il appelle la méthode updateBook qui vient de bookDAO qui exécute la fonction de devoir update le livre
        bookService.updateBook(book, admin);
        //Ici j'imagine, on crée un nouveau book et on update les résultats à partir de l'ID que l'on trouve je ne sais où
        Book updatedBook = bookService.getBookById(book.getId());
        //Et là juste on compare avec un assert
        assertEquals("New Title", updatedBook.getTitle());
    }

    @Test
    void testDeleteBook() throws Exception {
        //Création d'un book
        Book book = new Book("DeleteBook", "DeleteCat", "M.Deleter", "Del", "English", 10.0, "2025-01-01", "Deleting a book");
        //On vérifie que c'est bien un admin et qu'il a droit d'ajouter un livre dans ce cas là on exécute cette commande
        bookService.addBook(book, admin);
        //Juste pour vérifier que l'id est pas nul
        System.out.println("Before deleting the Book id: " + book.getId());
        System.out.println("Title of the book " + book.getTitle());
        //On delete le book dans la DB cette fois - ci
        bookService.deleteBook(book.getId(), admin);
        // On crée un deleteBookID qui récupère un id null normalement car l'id vient d'être supprimé donc le livre n'existe plus normalement
        Book deletedBookId = bookDAO.getBookById(book.getId());
        if (deletedBookId != null){
            System.out.println("Title of the deletingbook " + deletedBookId.getTitle());
        }
        else {
            System.out.println("The book has been deleted");
        }
        if (deletedBookId != null) {
            System.out.println("After deleting the Book id: " + deletedBookId.getId());
        }
        else {
            System.out.println("deletedBookID is null ! ");
        }
        assertNull(deletedBookId, "The book should have been deleted by th DataBase");
    }

    @Test
    void testDeleteBookByReaderFailing() throws Exception {
        Book book = new Book("ReaderBook", "ReaderCat", "M.Reader", "ReadPub", "English", 10.0, "2025-01-01", "ReaderDescription");
        Exception exception = assertThrows(RuntimeException.class, () -> bookService.addBook(book, reader));
        assertTrue(exception.getMessage().contains("You don't have the permission to add a book !"));
    }

    @Test
    void getAllBooks() throws Exception {
        Book book1 = new Book("Galactic Odyssey", "Science Fiction", "A. Nova", "Stellar Press", "English", 15.99, "2012-07-14", "A journey through the stars to find humanity's new home.");
        Book book2 = new Book("Chronicles of Andromeda", "Science Fiction", "L. Vega", "Cosmos Editions", "English", 18.50, "2018-03-22", "An epic saga of survival in the Andromeda galaxy.");
        Book book3 = new Book("Neon Skies", "Science Fiction", "R. Solaris", "FutureBound", "English", 12.99, "2020-11-05", "In a cyberpunk city, one hacker discovers the truth about AI control.");
        bookService.addBook(book1, admin);
        bookService.addBook(book2, admin);
        bookService.addBook(book3, admin);
        bookService.getAllBooks(admin);
        assertEquals(3, bookService.getAllBooks(admin).size());
    }

    @Test
    void findBookByAuthor() throws Exception {
        Book book1 = new Book("Galactic Odyssey", "Science Fiction", "A. Nova", "Stellar Press", "English", 15.99, "2012-07-14", "A journey through the stars to find humanity's new home.");
        Book book2 = new Book("Chronicles of Andromeda", "Science Fiction", "L. Vega", "Cosmos Editions", "English", 18.50, "2018-03-22", "An epic saga of survival in the Andromeda galaxy.");
        Book book3 = new Book("Neon Skies", "Science Fiction", "R. Solaris", "FutureBound", "English", 12.99, "2020-11-05", "In a cyberpunk city, one hacker discovers the truth about AI control");
        bookService.addBook(book1, admin);
        bookService.addBook(book2, admin);
        bookService.addBook(book3, admin);
        List<Book> byAuthor = bookService.findBookByAuthor("Nova");
        for (Book book : byAuthor){
            System.out.println("Les livres correspondants : " + book);
        }
    }

    @Test
    void findBookByTitle() throws Exception {
        Book book1 = new Book("Galactic Odyssey", "Science Fiction", "A. Nova", "Stellar Press", "English", 15.99, "2012-07-14", "A journey through the stars to find humanity's new home.");
        Book book2 = new Book("Chronicles of Andromeda", "Science Fiction", "L. Vega", "Cosmos Editions", "English", 18.50, "2018-03-22", "An epic saga of survival in the Andromeda galaxy.");
        Book book3 = new Book("Neon Skies", "Science Fiction", "R. Solaris", "FutureBound", "English", 12.99, "2020-11-05", "In a cyberpunk city, one hacker discovers the truth about AI con");
        bookService.addBook(book1, admin);
        bookService.addBook(book2, admin);
        bookService.addBook(book3, admin);
        List<Book> byTitle = bookService.findBookByTitle("Galactic");
        for (Book book : byTitle){
            System.out.println(" Les livres correspondants avec ce titre sont : " + book);
        }
    }
}
