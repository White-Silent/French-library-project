package com.libraryproject.dao;

import static org.junit.jupiter.api.Assertions.*;

import com.libraryproject.dao.UserDAO;
import com.libraryproject.enums.Role;
import com.libraryproject.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

class UserDAOTest {

    //Déclarer connection comme attribut de classe pour pouvoir l'utiliser dans toutes les méthodes
    private Connection connection;
    private UserDAO userDAO;


    //Sotck les visa pour les tests
    private String readerVisa;
    private String adminVisa;

    @BeforeEach
    void setupUser() throws SQLException {
        //Setup de base
        connection = DriverManager.getConnection(
                "jdbc:mysql://192.168.101.214:3306/bibliotheque_test",
                "javauser",
                "Daumesnil504!"
        );
        userDAO = new UserDAO(connection);

        //Supprime tous les anciens utilisateurs pour éviter de potentiels conflits
        connection.prepareStatement("DELETE FROM borrows").executeUpdate();
        connection.prepareStatement("DELETE FROM users").executeUpdate();

        //Add the reader test
        User testUser = new User("raphael", "monmdp123", Role.READER, null);
        userDAO.addUser(testUser);
        readerVisa = testUser.getVisa();

        //Add admin test
        User adminUser = new User("admin", "adminpass", Role.ADMIN, null);
        userDAO.addUser(adminUser);
        adminVisa = adminUser.getVisa();
    }

    @Test
    void testAuthenticate() throws SQLException {
        // Utilisateur correct
        User user = userDAO.authentificate("raphael", "monmdp123", Role.READER, readerVisa);
        assertNotNull(user);

        // Mauvais mot de passe
        User wrongUser = userDAO.authentificate("raphael", "wrong", Role.READER, readerVisa);
        assertNull(wrongUser);
    }

    @Test
    void testRoleAuthentication() throws SQLException {
        // Directeur correct
        User director = userDAO.authentificate("admin", "adminpass", Role.ADMIN, adminVisa);
        assertNotNull(director);
        Assertions.assertEquals(Role.ADMIN, director.getRole());

        // Lecteur correct
        User reader = userDAO.authentificate("raphael", "monmdp123", Role.READER, readerVisa);
        assertNotNull(reader);
        Assertions.assertEquals(Role.READER, reader.getRole());

        // Tentative d'usurpation : lecteur avec rôle directeur
        User wrongRole = userDAO.authentificate("raphael", "monmdp123", Role.ADMIN, readerVisa);
        assertNull(wrongRole, "A reader should not authenticate as a director");
    }

    @AfterEach
    void closeConnection() throws SQLException {
        connection.close();
    }
}
