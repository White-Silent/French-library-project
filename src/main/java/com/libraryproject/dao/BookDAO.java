package com.libraryproject.dao;

import com.libraryproject.model.Book;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class BookDAO {

    @Autowired
    private DataSource dataSource;

    // Méthode pour obtenir une connexion
    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void addBook(Book book) throws SQLException {
        String sql = "INSERT INTO books (title, category, author, publisher, language, price , publicationDate, description) " + "VALUES (?,?,?,?,?,?,?,?)";

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, book.getTitle());
            ps.setString(2, book.getCategory());
            ps.setString(3, book.getAuthor());
            ps.setString(4, book.getPublisher());
            ps.setString(5, book.getLanguage());
            ps.setDouble(6, book.getPrice());
            ps.setString(7, book.getPublicationDate());
            ps.setString(8, book.getDescription());
            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()){
                if (generatedKeys.next()){
                    book.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    public Book getBookById(int id) throws SQLException {
        String sqlRequest = "SELECT * FROM books WHERE id = ?";

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sqlRequest)){
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToBook(rs);
                }
            }
            return null;
        }
    }

    public Book getBookByExactTitle(String title) throws SQLException {
        String sql = "SELECT * FROM books WHERE title = ?";

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, title);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToBook(rs);
                }
            }
        }
        return null;
    }

    public List<Book> getAllBooks() throws SQLException {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM books";

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                books.add(mapResultSetToBook(rs));
            }
        }
        return books;
    }

    public void updateBook(Book book) throws SQLException {
        String sql = "UPDATE books SET title = ?, category = ?, author = ?, publisher = ?, language = ?, " +
                "price = ?, publicationDate = ?, description = ? WHERE id = ?";

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, book.getTitle());
            ps.setString(2, book.getCategory());
            ps.setString(3, book.getAuthor());
            ps.setString(4, book.getPublisher());
            ps.setString(5, book.getLanguage());
            ps.setDouble(6, book.getPrice());
            ps.setString(7, book.getPublicationDate());
            ps.setString(8, book.getDescription());
            ps.setInt(9, book.getId());
            ps.executeUpdate();
        }
    }

    public void deleteBook(int id) throws SQLException {
        String sql = "DELETE FROM books where id = ?";

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Book mapResultSetToBook(ResultSet rs) throws SQLException {
        Book book = new Book(
                rs.getString("title"),
                rs.getString("category"),
                rs.getString("author"),
                rs.getString("publisher"),
                rs.getString("language"),
                rs.getDouble("price"),
                rs.getString("publicationDate"),
                rs.getString("description")
        );
        book.setId(rs.getInt("id"));
        return book;
    }

    public List<Book> findBookByTitle(String title) throws SQLException {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM books WHERE title LIKE ?";

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, "%" + title + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    books.add(mapResultSetToBook(rs));
                }
            }
        }
        return books;
    }

    public List<Book> findBookByAuthor(String author) throws SQLException {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM books WHERE author LIKE ?";

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, "%" + author + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    books.add(mapResultSetToBook(rs));
                }
            }
        }
        return books;
    }
}