package com.libraryproject.dao;

import com.libraryproject.enums.Role;
import com.libraryproject.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

//Indique à Spring que cela permet d'accéder aux données
@Repository
public class UserDAO {

    @Autowired
    private DataSource dataSource;

    // Constructeur par défaut (Spring l'utilise)
    public UserDAO() {
        // Spring injecte automatiquement la DataSource
    }

    // Méthode pour obtenir une connexion
    //Au lieu de créer manuellement la connexion, on gère tout le processus de manière optimisée
    //avec dataSource.getConnection()
    //Le pooling permet d'améliorer considérablement les performances de l'application
    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    //Générer un visa de manière aléatoire (pour la sécurité CSRF)
    public static String generateVisa() {
        Random rand = new Random();
        char letter1 = (char) ('A' + rand.nextInt(26));
        char letter2 = (char) ('A' + rand.nextInt(26));
        int number = rand.nextInt(100);
        return String.format("%c%c%02d", letter1, letter2, number);
    }

    public List<User> getAllUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users";

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String username = rs.getString("username");
                String password = rs.getString("password"); // si besoin
                String role = rs.getString("role");
                Role roleEnum = Role.valueOf(role);
                users.add(new User( username, password, roleEnum)); // adapte selon ton constructeur
            }
        }

        return users;
    }

    //Add a new User
    public void addUser(User user) throws SQLException {
        String sqlRequest = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sqlRequest, Statement.RETURN_GENERATED_KEYS)){
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getRole().getDbValue());
            ps.executeUpdate(); // -> exécution de l'insert

            //Retrieve the id
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    user.setId(rs.getInt(1)); //Met à jour l'objet user, avec son nouvel ID
                }
            }
        } // Connexion automatiquement fermée
    }

    private User buildUserFromResultSet(ResultSet rs) throws SQLException {
        Role role = Role.fromDbValue(rs.getString("role")); //Conversion String role en enum
        User user = new User(
                rs.getString("username"),
                rs.getString("password"),
                role
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
                    return buildUserFromResultSet(rs); // -> Construction de l'objet
                }
            }
        }
        return null; // -> aucun utilisateur trouvé
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



    //Verify the authentification (plus de visa en base)
    public User authentificate(String username, String password, Role role) throws SQLException {
        String sqlRequest = "SELECT * FROM users WHERE username = ? AND password = ? AND role = ?";
        //Vérification des 3 critères en simulatnés

        try (Connection connection = getConnection(); // Connexion au pool
             PreparedStatement ps = connection.prepareStatement(sqlRequest)){ // Préparation
            ps.setString(1, username); // Paramétrage
            ps.setString(2, password);
            ps.setString(3, role.getDbValue()); // Utiliser getDbValue() pour la cohérence

            try (ResultSet rs = ps.executeQuery()){ //Exécution + Récupération
                if (rs.next()){
                    return buildUserFromResultSet(rs); //Construction de l'objet
                } else {
                    return null;
                }
            }
        }
    }
}