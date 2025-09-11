package com.libraryproject.service;

import com.libraryproject.dao.UserDAO;
import com.libraryproject.enums.Role;
import com.libraryproject.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;

@Service
public class UserService {

    @Autowired
    private UserDAO userDAO;

    //Method
    //Generate a Visa (pour la sécurité CSRF uniquement)
    public String generateVisa() {
        return UserDAO.generateVisa();
    }

    public void addUser(User user) throws SQLException {
        if (user.getUsername() == null || user.getUsername().isEmpty()){
            throw new RuntimeException("Username is required !");
        }
        userDAO.addUser(user);
    }

    public User getUserById(int id) throws SQLException {
        return userDAO.getUserById(id);
    }

    public User getUserByUsername(String username) throws SQLException {
        return userDAO.getUserByUsername(username);
    }

    public User authentificate(String username, String password, Role role) throws SQLException {
        // Utilisation directe de la méthode DAO qui fait la vérification complète
        return userDAO.authentificate(username, password, role);
    }
}