package com.libraryproject.dao;

import com.libraryproject.model.Book;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

//DAO -> Data Access Object
public class BookDAO {

    //Attribut of Connection
    private Connection connection;

    //Constructor avec connexion
    public BookDAO(Connection connection) {
        this.connection = connection;
    }

    //Méthods
    //Create (insert) a book into the database
    // It's doesn't check if the book is already in the database
    //Insert (Java -> SQL)
    public void addBook(Book book) throws SQLException {
        String sql = "INSERT INTO books (title, category, author, publisher, language, price , publicationDate, description) " + "VALUES (?,?,?,?,?,?,?,?)";
        //Expliquer de manière explicite de générer la clé
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
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
                    book.setId(generatedKeys.getInt(1)); // <- test utilise book.getId()
                }
            }
        }
    }

    // SQL -> Java
    public Book getBookById(int id) throws SQLException {
        String sqlRequest = "SELECT * FROM books WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sqlRequest)){
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToBook(rs);
                }
            }
            return null;
        }
    }

    //Read (Get all books)
    //Retrieve all books currenty stored in the database
    //It doesn't create anything, it just reads existing data
    public List<Book> getAllBooks() throws SQLException {
        //List of Book
        List<Book> books = new ArrayList<>();
        //Get all the colums from the table "books"
        String sql = "SELECT * FROM books";
        try ( PreparedStatement ps = connection.prepareStatement(sql)){
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
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
                books.add(book);
            }
        }
        return books;
    }

    //Update the Book (if need to be modify)
    public void updateBook(Book book) throws SQLException {
        String sql = "UPDATE books SET title = ?, category = ?, author = ?, publisher = ?, language = ?, " +
                "price = ?, publicationDate = ?, description = ? WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, book.getTitle());
            ps.setString(2, book.getCategory());
            ps.setString(3, book.getAuthor());
            ps.setString(4, book.getPublisher());
            ps.setString(5, book.getLanguage());
            ps.setDouble(6, book.getPrice());
            ps.setString(7, book.getPublicationDate());
            ps.setString(8, book.getDescription());
            ps.setInt(9, book.getId()); //Which book to update
            ps.executeUpdate(); //Executes the update
        }
    }

    //Delete book by using the ID
    public void deleteBook(int id) throws SQLException {
        String sql = "DELETE FROM books where id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id); //Specify which book to delete
            ps.executeUpdate(); //Execute the deletion
        }
    }

    //Helper method : map ResultSet to Book object
    //Convert a single row from the SQL Result set into a Book object
    //and to get the values from the current row
    //A ResultSet is a table of data in memory that Java gets when you execute a SELECT query in SQL.
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
        //It returns a fully constructed book
        return book;
    }

    //Find book by title
    public List<Book> findBookByTitle(String title) throws SQLException {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM books WHERE title LIKE ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, "%" + title + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    books.add(mapResultSetToBook(rs));
                }
            }
        }
        return books;
    }

    //Find book by author
    public List<Book> findBookByAuthor(String author) throws SQLException {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM books WHERE author LIKE ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
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
