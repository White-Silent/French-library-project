package com.libraryproject.dao;

import com.libraryproject.model.Book;
import com.libraryproject.model.User;
import com.libraryproject.model.Borrow;
import com.libraryproject.enums.BorrowStatus;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BorrowDAO {

    //Attribute
    private Connection connection;

    //Constructor
    public BorrowDAO(Connection connection) {
        this.connection = connection;
    }

    //Register a book
    public void borrowBook(Borrow borrow) throws SQLException {
        String sql = "INSERT INTO borrows (user_id, book_id, borrow_date, due_date, status) VALUES (?,?,?,?,?) ";
        try (PreparedStatement ps = connection.prepareStatement(sql)){
            ps.setInt(1, borrow.getUser().getId());
            ps.setInt(2, borrow.getBook().getId());
            ps.setDate(3, java.sql.Date.valueOf(borrow.getBorrowDate()));
            ps.setDate(4, java.sql.Date.valueOf(borrow.getDueDate()));
            //Stocker exactement le nom de l'enum
            ps.setString(5, borrow.getStatus().name());
            ps.executeUpdate();
        }
    }

    //Return a book
    public void returnBook(int borrowId, LocalDate returnDate) throws  SQLException{
        String sql = "UPDATE borrows SET return_date = ?, status = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)){
            ps.setDate(1, java.sql.Date.valueOf(returnDate));
            ps.setString(2, BorrowStatus.RETURNED.name());
            ps.setInt(3, borrowId);
            ps.executeUpdate();
        }
    }


    //Helper method
    private Borrow mapResultSetToBorrow(ResultSet rs) throws SQLException {
        int userId = rs.getInt("user_id");
        int bookId = rs.getInt("book_id");

        //Créer les UserDao et BookDao
        UserDAO userDAO = new UserDAO(connection);
        BookDAO bookDAO = new BookDAO(connection);

        User user = userDAO.getUserById(userId);
        Book book = bookDAO.getBookById(bookId);

        Borrow borrow = new Borrow(user,book, rs.getDate("borrow_date").toLocalDate(), rs.getDate("due_date").toLocalDate(), BorrowStatus.BORROWED);
        borrow.setId(rs.getInt("id"));
        //Read the date to return the book
        Date returnDate = rs.getDate("return_date");
        if (returnDate != null){
            borrow.setReturnDate(returnDate.toLocalDate());
        }
        //status -> enum BorrowStatus
        borrow.setStatus(BorrowStatus.fromDbValue(rs.getString("status")));
        return borrow;
    }

    //Retrieve all of the borrow of an user
    public List<Borrow> getBorrowsByUser(int userId) throws SQLException{
        List<Borrow> borrows = new ArrayList<>();
        String sqlRequest = "SELECT * FROM borrows WHERE user_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sqlRequest)){
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
