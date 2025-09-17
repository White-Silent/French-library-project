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

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void addBook(Book book) throws SQLException {
        String sql = "INSERT INTO books (title, category, author, publisher, language, price, publicationDate, description, available) VALUES (?,?,?,?,?,?,?,?,?)";

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
            ps.setBoolean(9, book.getAvailable());
            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    book.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    public Book getBookById(int id) throws SQLException {
        String sqlRequest = "SELECT * FROM books WHERE id = ?";

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sqlRequest)) {
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
        System.out.println("DEBUG DAO - getAllBooks: " + books.size() + " livres récupérés");
        return books;
    }

    public void updateBook(Book book) throws SQLException {
        String sql = "UPDATE books SET title = ?, category = ?, author = ?, publisher = ?, language = ?, " +
                "price = ?, publicationDate = ?, description = ?, available = ? WHERE id = ?";

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
            ps.setBoolean(9, book.getAvailable());
            ps.setInt(10, book.getId());
            ps.executeUpdate();
        }
    }

    public void deleteBook(int id) throws SQLException {
        Connection connection = getConnection();

        try {
            connection.setAutoCommit(false);

            // Vérifier si le livre est actuellement emprunté
            String checkCurrentBorrows = """
            SELECT COUNT(*) as count FROM borrows 
            WHERE book_id = ? AND status IN ('BORROWED', 'LATE')
            """;

            try (PreparedStatement ps = connection.prepareStatement(checkCurrentBorrows)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt("count") > 0) {
                        throw new SQLException("Impossible de supprimer : livre actuellement emprunté");
                    }
                }
            }

            // Suppression physique directe
            // Grâce à ON DELETE CASCADE, les enregistrements dans borrows seront automatiquement supprimés
            String physicalDelete = "DELETE FROM books WHERE id = ?";
            try (PreparedStatement ps = connection.prepareStatement(physicalDelete)) {
                ps.setInt(1, id);
                int affected = ps.executeUpdate();
                if (affected == 0) {
                    throw new SQLException("Aucun livre trouvé avec l'ID: " + id);
                }
            }

            connection.commit();

        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
            connection.close();
        }
    }
    /*
    // ✅ CORRECTION MAJEURE : Gestion intelligente de la suppression
    public void deleteBook(int id) throws SQLException {
        Connection connection = getConnection();

        try {
            connection.setAutoCommit(false);

            // 1. Vérifier si le livre est actuellement emprunté
            String checkCurrentBorrows = """
                SELECT COUNT(*) as count FROM borrows 
                WHERE book_id = ? AND status IN ('BORROWED', 'LATE')
                """;

            try (PreparedStatement ps = connection.prepareStatement(checkCurrentBorrows)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt("count") > 0) {
                        throw new SQLException("Impossible de supprimer : livre actuellement emprunté");
                    }
                }
            }

            // 2. Vérifier s'il y a un historique d'emprunts
            String checkHistory = "SELECT COUNT(*) as count FROM borrows WHERE book_id = ?";
            boolean hasHistory = false;

            try (PreparedStatement ps = connection.prepareStatement(checkHistory)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        hasHistory = rs.getInt("count") > 0;
                    }
                }
            }

            if (hasHistory) {
                // Si il y a un historique, faire une suppression logique
                String logicalDelete = "UPDATE books SET available = false, title = CONCAT('[SUPPRIMÉ] ', title) WHERE id = ?";
                try (PreparedStatement ps = connection.prepareStatement(logicalDelete)) {
                    ps.setInt(1, id);
                    int affected = ps.executeUpdate();
                    if (affected == 0) {
                        throw new SQLException("Aucun livre trouvé avec l'ID: " + id);
                    }
                }
            } else {
                // Pas d'historique, suppression physique possible
                String physicalDelete = "DELETE FROM books WHERE id = ?";
                try (PreparedStatement ps = connection.prepareStatement(physicalDelete)) {
                    ps.setInt(1, id);
                    int affected = ps.executeUpdate();
                    if (affected == 0) {
                        throw new SQLException("Aucun livre trouvé avec l'ID: " + id);
                    }
                }
            }

            connection.commit();

        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
            connection.close();
        }
    }

     */

    // ✅ CORRECTION CRITIQUE : mapResultSetToBook corrigé
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
        // 🔥 CORRECTION PRINCIPALE : Récupération de la disponibilité
        book.setAvailable(rs.getBoolean("available"));
        return book;
    }

    // ✅ NOUVELLE MÉTHODE : Vérifier les emprunts en cours
    public boolean hasCurrentBorrows(int bookId) throws SQLException {
        String sql = """
            SELECT COUNT(*) as count FROM borrows 
            WHERE book_id = ? AND status IN ('BORROWED', 'LATE')
            """;

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count") > 0;
                }
            }
        }
        return false;
    }

    // ✅ NOUVELLE MÉTHODE : Obtenir le nombre d'emprunts en cours
    public int getCurrentBorrowCount(int bookId) throws SQLException {
        String sql = """
            SELECT COUNT(*) as count FROM borrows 
            WHERE book_id = ? AND status IN ('BORROWED', 'LATE')
            """;

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count");
                }
            }
        }
        return 0;
    }



    // ✅ NOUVELLE MÉTHODE : Obtenir le nombre total d'emprunts
    public int getTotalBorrowCount(int bookId) throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM borrows WHERE book_id = ?";

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count");
                }
            }
        }
        return 0;
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

    public List<Book> getAvailableBooks() throws SQLException {
        List<Book> availableBooks = new ArrayList<>();
        String sqlRequest = "SELECT * FROM books WHERE available = TRUE";

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sqlRequest);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                availableBooks.add(mapResultSetToBook(rs));
            }
        }
        return availableBooks;
    }

    public List<Book> getBorrowedBooks() throws SQLException {
        List<Book> borrowedBooks = new ArrayList<>();
        String sql = "SELECT * FROM books WHERE available = FALSE";

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                borrowedBooks.add(mapResultSetToBook(rs));
            }
        }
        return borrowedBooks;
    }

    public int countAvailableBooks() throws SQLException {
        String sql = "SELECT COUNT(*) FROM books WHERE available = TRUE";

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    public int countBorrowedBooks() throws SQLException {
        String sql = "SELECT COUNT(*) FROM books WHERE available = FALSE";
        try (Connection connection = getConnection();
        PreparedStatement ps = connection.prepareStatement(sql);
        ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    public Integer getCurrentBorrowId(int bookId) throws SQLException {
        String sql = """
                SELECT id FROM borrows 
                WHERE book_id = ? AND status IN ('BORROWED', 'LATE')
                ORDER BY borrow_date DESC
                LIMIT 1
                """;

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }
        return null;
    }

    public void updateBookAvailability(int bookId, boolean available) throws SQLException {
        String sql = "UPDATE books SET available = ? WHERE id = ?";

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setBoolean(1, available);
            ps.setInt(2, bookId);
            ps.executeUpdate();
        }
    }

    public boolean isBookAvailable(int bookId) throws SQLException {
        String sql = "SELECT available FROM books WHERE id = ?";

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Livre introuvable pour l'id " + bookId);
                }
                return rs.getBoolean("available");
            }
        }
    }


}