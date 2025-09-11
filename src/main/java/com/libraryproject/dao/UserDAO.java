package com.libraryproject.dao;

import com.libraryproject.enums.Role;
import com.libraryproject.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Random;

@Repository
public class UserDAO {

    @Autowired
    private DataSource dataSource;

    // SUPPRIMEZ COMPLÈTEMENT L'ANCIEN CONSTRUCTEUR
    // public UserDAO(Connection connection) { ... }

    // Constructeur par défaut (Spring l'utilise)
    public UserDAO() {
        // Spring injecte automatiquement la DataSource
    }

    // Méthode pour obtenir une connexion
    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    //Générer un visa de manière aléatoire
    public static String generateVisa() {
        Random rand = new Random();
        char letter1 = (char) ('A' + rand.nextInt(26));
        char letter2 = (char) ('A' + rand.nextInt(26));
        int number = rand.nextInt(100);
        return String.format("%c%c%02d", letter1, letter2, number);
    }

    //Add a new User
    public void addUser(User user) throws SQLException {
        if (user.getVisa() == null || user.getVisa().isEmpty()){
            user.setVisa(generateVisa());
        }
        String sqlRequest = "INSERT INTO users (username, password, role, visa) VALUES (?, ?, ?, ?)";

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sqlRequest, Statement.RETURN_GENERATED_KEYS)){
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getRole().getDbValue());
            ps.setString(4, user.getVisa());
            ps.executeUpdate();

            //Retrieve the id
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    user.setId(rs.getInt(1));
                }
            }
        }
    }

    private User buildUserFromResultSet(ResultSet rs) throws SQLException {
        Role role = Role.fromDbValue(rs.getString("role"));
        User user = new User(
                rs.getString("username"),
                rs.getString("password"),
                role,
                rs.getString("visa")
        );
        user.setId(rs.getInt("id"));
        return user;
    }

    public User getUserById(int id) throws SQLException {
        String sqlRequest = "SELECT * FROM users WHERE id = ?";

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sqlRequest)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return buildUserFromResultSet(rs);
                }
            }
        }
        return null;
    }

    public User getUserByUsername(String username) throws SQLException {
        String sqlRequest = "SELECT * FROM users WHERE username = ?";

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sqlRequest)) {
            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return buildUserFromResultSet(rs);
                }
            }
        }
        return null;
    }

    //Verify the authentification
    public User authentificate(String username, String password, Role role, String visa) throws SQLException {
        String sqlRequest = "SELECT * FROM users WHERE username = ? AND password = ? AND role = ? AND visa = ?";

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sqlRequest)){
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, role.name());
            ps.setString(4, visa);

            try (ResultSet rs = ps.executeQuery()){
                if (rs.next()){
                    User user = new User(
                            rs.getString("username"),
                            rs.getString("password"),
                            Role.fromDbValue(rs.getString("role")),
                            rs.getString("visa")
                    );
                    user.setId(rs.getInt("id"));
                    return user;
                } else {
                    return null;
                }
            }
        }
    }
}