package com.libraryproject.dao;

import com.libraryproject.model.Book;
import com.libraryproject.model.User;
import com.libraryproject.model.Borrow;
import com.libraryproject.enums.BorrowStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
public class BorrowDAO {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private UserDAO userDAO;  // Injection Spring

    @Autowired
    private BookDAO bookDAO;  // Injection Spring

    // Méthode pour obtenir une connexion
    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void borrowBook(Borrow borrow) throws SQLException {
        String sql = "INSERT INTO borrows (user_id, book_id, borrow_date, due_date, status) VALUES (?,?,?,?,?) ";

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)){
            ps.setInt(1, borrow.getUser().getId());
            ps.setInt(2, borrow.getBook().getId());
            ps.setDate(3, java.sql.Date.valueOf(borrow.getBorrowDate()));
            ps.setDate(4, java.sql.Date.valueOf(borrow.getDueDate()));
            ps.setString(5, borrow.getStatus().name());
            ps.executeUpdate();
        }
    }

    public void returnBook(int borrowId, LocalDate returnDate) throws SQLException{
        String sql = "UPDATE borrows SET return_date = ?, status = ? WHERE id = ?";

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)){
            ps.setDate(1, java.sql.Date.valueOf(returnDate));
            ps.setString(2, BorrowStatus.RETURNED.name());
            ps.setInt(3, borrowId);
            ps.executeUpdate();
        }
    }

    private Borrow mapResultSetToBorrow(ResultSet rs) throws SQLException {
        int userId = rs.getInt("user_id");
        int bookId = rs.getInt("book_id");

        // Utilise les DAO injectés par Spring au lieu de créer manuellement
        User user = userDAO.getUserById(userId);
        Book book = bookDAO.getBookById(bookId);

        Borrow borrow = new Borrow(user, book,
                rs.getDate("borrow_date").toLocalDate(),
                rs.getDate("due_date").toLocalDate(),
                BorrowStatus.BORROWED);
        borrow.setId(rs.getInt("id"));

        Date returnDate = rs.getDate("return_date");
        if (returnDate != null){
            borrow.setReturnDate(returnDate.toLocalDate());
        }
        borrow.setStatus(BorrowStatus.fromDbValue(rs.getString("status")));
        return borrow;
    }

    public List<Borrow> getBorrowsByUser(int userId) throws SQLException{
        List<Borrow> borrows = new ArrayList<>();
        String sqlRequest = "SELECT * FROM borrows WHERE user_id = ?";

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sqlRequest)){
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()){
                while (rs.next()){
                    borrows.add(mapResultSetToBorrow(rs));
                }
            }
        }
        return borrows;
    }
}