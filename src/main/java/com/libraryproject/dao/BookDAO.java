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

    //Récupérer tous les livre disponibles non empruntées actuellement
    public List<Book> getAvailableBooks() throws SQLException {
        List<Book> availableBooks = new ArrayList<>();
        String sqlRequest = """
                    SELECT DISTINCT b.* FROM books b
                    WHERE b.id NOT IN (
                    SELECT br.book_id FROM borrows br
                    WHERE br.status IN ('BORROWED', 'LATE')
                """;
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sqlRequest);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                availableBooks.add(mapResultSetToBook(rs));
            }
        }
        return availableBooks;
    }

    //Vérifier si un livre spécifique est disponible
    public boolean isBookAvailable(int bookId) throws SQLException {
        String sql = """
                   SELECT COUNT(*) FROM borrows
                   WHERE book_id = ? AND status IN ('BORROWED', 'LATE')
                """;
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)){
            ps.setInt(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) == 0; //Disponible si aucun emprunt
                }
            }
        }
        return false;
    }

    //Récupère tous les livres actuellement empruntées
    public List<Book> getBorrowedBooks() throws SQLException {
        List<Book> borrowedBooks = new ArrayList<>();
        String sql = """
                SELECT DISTINCT b.* FROM books b
                INNER JOIN borrows br on b.id = br.book_id
                WHERE br.status IN ('BORROWED', 'LATE')  
                """;
        try (Connection connection = getConnection();
            PreparedStatement ps = connection.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    borrowedBooks.add(mapResultSetToBook(rs));
                }
        }
        return borrowedBooks;
    }

    //Count the number of books available
    public int countAvailableBooks() throws SQLException {
        String sql = """
                SELECT COUNT(DISTINCT b.id) FROM books b
                WHERE b.id NOT IN (
                    SELECT br.book_id FROM borrows br
                    WHERE br.status IN ('BORROWED', 'LATE'))
                """;
        try (Connection connection = getConnection();
        PreparedStatement ps = connection.prepareStatement(sql);
        ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    //Récupérer l'emprunt actuel d'un livre (s'il existe)
    public Integer getCurrentBorrowId(int bookId) throws SQLException{
        String sql = """
                SELECT id FROM borrows 
                WHERE book_id = ? and   status IN ('BORROWED', 'LATE')
                order by borrow_date DESC
                LIMIT 1
                """;
        // -> ouvre la connexion à la BDD
        //Le try évite les fuites de mémoire
        try (Connection connection = getConnection();
             // -> Prépare la requête SQL ils sont fermé dans le try
        PreparedStatement ps = connection.prepareStatement(sql)){
            ps.setInt(1, bookId); // Remplace le ? par l'id en argument
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }
        return null;
    }


}