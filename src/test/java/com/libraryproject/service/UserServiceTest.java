package com.libraryproject.service;

import com.libraryproject.dao.UserDAO;
import com.libraryproject.enums.Role;
import com.libraryproject.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {

    Connection connection;
    UserDAO userDAO;

    private String readerVisa;
    private String adminVisa;

    private UserService userService;

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
        //Instancier UserService
        userService = new UserService(userDAO);
        //Add admin test
        User adminUser = new User("admin", "adminpass", Role.ADMIN, null);
        userDAO.addUser(adminUser);
        adminVisa = adminUser.getVisa();
    }

    @Test
    void testAuthenticate() throws SQLException {
        // Utilisateur correct
        User user = userService.authentificate("raphael", "monmdp123", Role.READER);
        assertNotNull(user);

        // Mauvais mot de passe
        User wrongUser = userService.authentificate("raphael", "wrong", Role.READER);
        assertNull(wrongUser);
    }

    @Test
    void testRoleAuthentication() throws SQLException {
        // Directeur correct
        User director = userService.authentificate("admin", "adminpass", Role.ADMIN);
        assertNotNull(director);
        assertEquals(Role.ADMIN, director.getRole());

        // Lecteur correct
        User reader = userService.authentificate("raphael", "monmdp123", Role.READER);
        assertNotNull(reader);
        assertEquals(Role.READER, reader.getRole());

        // Tentative d'usurpation : lecteur avec rôle directeur
        User wrongRole = userService.authentificate("raphael", "monmdp123", Role.ADMIN);
        assertNull(wrongRole, "A reader should not authenticate as a director");
    }

    @Test
    void testAddUser() throws SQLException {
        // Crée un nouvel utilisateur
        User newUser = new User("testUser", "testPass", Role.READER, null);

        // Ajoute l'utilisateur via le service
        userService.addUser(newUser);

        // Vérifie que l'ID a été généré
        assertNotNull(newUser.getId(), "L'ID de l'utilisateur doit être généré après ajout");

        // Récupère l'utilisateur depuis la base pour vérifier
        User retrievedUser = userService.getUserByUsername("testUser");
        assertNotNull(retrievedUser, "L'utilisateur doit exister en base");
        assertEquals("testUser", retrievedUser.getUsername());
        assertEquals("testPass", retrievedUser.getPassword());
        assertEquals(Role.READER, retrievedUser.getRole());
        assertNotNull(retrievedUser.getVisa(), "Le visa doit être généré automatiquement si null");
    }

    @Test
    void testGetUserById() throws SQLException {
        // Crée un nouvel utilisateur
        User newUser = new User("idTestUser", "idTestPass", Role.READER, null);
        userService.addUser(newUser);

        // Récupère l'utilisateur depuis la base via son ID
        User retrievedUser = userService.getUserById(newUser.getId());

        // Vérifications
        assertNotNull(retrievedUser, "L'utilisateur doit exister en base");
        assertEquals(newUser.getId(), retrievedUser.getId(), "Les IDs doivent correspondre");
        assertEquals("idTestUser", retrievedUser.getUsername());
        assertEquals("idTestPass", retrievedUser.getPassword());
        assertEquals(Role.READER, retrievedUser.getRole());
        assertNotNull(retrievedUser.getVisa(), "Le visa doit être présent");
    }

    @AfterEach
    void closeConnection() throws SQLException {
        connection.close();
    }
}