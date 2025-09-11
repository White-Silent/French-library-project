package com.libraryproject.model;

import com.libraryproject.enums.Role;

public class User {
    //Attributes
    private int id;
    private String username;
    private String password;
    private Role role;

    //Constructor User
    public User( String username, String password, Role role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    //Getter and Setter

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    //Method
    @Override
    public String toString(){
        return "User [ id = " + this.id + " password = " + this.password + " role = " + this.role + " ]";
    }
}
