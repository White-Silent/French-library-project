package com.libraryproject.model;

import com.libraryproject.enums.Role;
import jakarta.persistence.*;

@Entity // ← AJOUTÉ : Dit à Spring que c'est une entité de base de données
@Table(name = "users") // ← AJOUTÉ : Nom de la table
public class User {

    @Id // ← AJOUTÉ : Clé primaire
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ← AJOUTÉ : Auto-increment
    private int id;

    @Column(nullable = false, unique = true) // ← AJOUTÉ : Username unique et obligatoire
    private String username;

    @Column(nullable = false) // ← AJOUTÉ : Mot de passe obligatoire
    private String password;

    @Enumerated(EnumType.STRING) // ← AJOUTÉ : Pour sauvegarder l'enum comme texte
    @Column(nullable = false)
    private Role role;

    // Constructeur par défaut - OBLIGATOIRE pour JPA
    public User() {
        // Constructeur vide nécessaire pour Spring Boot/JPA
    }

    // Constructor avec paramètres
    public User(String username, String password, Role role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    // Getters et Setters
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

    @Override
    public String toString() {
        return "User [ id = " + this.id + " username = " + this.username +
                " role = " + this.role + " ]";
    }
}