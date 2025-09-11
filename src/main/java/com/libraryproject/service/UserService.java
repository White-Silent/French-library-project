package com.libraryproject.service;

import com.libraryproject.dao.UserDAO;
import com.libraryproject.enums.Role;
import com.libraryproject.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;

@Service  // ANNOTATION CRUCIALE !
public class UserService {

    @Autowired  // Spring injecte automatiquement UserDAO
    private UserDAO userDAO;

    // Supprimez le constructeur manuel, Spring s'en occupe

    //Method
    //Generate a Visa
    public String generateVisa() {
        return UserDAO.generateVisa();  // Méthode statique
    }

    public void addUser(User user) throws SQLException {
        if (user.getUsername() == null || user.getUsername().isEmpty()){
            throw new RuntimeException("Username is required !");
        }
        if (user.getVisa() == null || user.getVisa().isEmpty()){
            user.setVisa(generateVisa());
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
        User user = userDAO.getUserByUsername(username);
        if (user != null && user.getPassword().equals(password) && user.getRole() == role){
            return user;
        }
        return null;
    }
}