package com.libraryproject.service;

import com.libraryproject.dao.UserDAO;
import com.libraryproject.enums.Role;
import com.libraryproject.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UserService {
    //Attribute
    private UserDAO userDAO;

    //Constructor
    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    //Method
    //Generate a Visa
    public String generateVisa() {
        return userDAO.generateVisa();
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

    //Creation of the User
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
