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

    // SUPPERTIME le constructeur manuel
    // Spring s'occupe de l'injection automatiquement

    public void addBook(Book book, User user) throws SQLException {
        if (user.getRole() == Role.READER) {
            throw new RuntimeException("You don't have the permission to add a book !");
        }
        bookDAO.addBook(book);
    }

    public Book getBookById(int id) throws SQLException {
        return bookDAO.getBookById(id);
    }

    public void updateBook(Book book, User user) throws SQLException{
        if (user.getRole() == Role.READER){
            throw new RuntimeException("You don't have the permission to update a book !");
        }
        bookDAO.updateBook(book);
    }

    public void deleteBook(int id, User user) throws SQLException {
        if (user.getRole() == Role.READER){
            throw new RuntimeException("You don't have the permission to delete a book !");
        }
        bookDAO.deleteBook(id);
    }

    public List<Book> getAllBooks(User user) throws SQLException {
        return bookDAO.getAllBooks();
    }

    public List<Book> findBookByTitle(String title) throws SQLException{
        return bookDAO.findBookByTitle(title);
    }

    public List<Book> findBookByAuthor(String author) throws SQLException{
        return bookDAO.findBookByAuthor(author);
    }
}