package com.libraryproject.model;

import com.libraryproject.enums.BorrowStatus;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity // ← AJOUTÉ : Dit à Spring que c'est une entité de base de données
@Table(name = "borrows") // ← AJOUTÉ : Nom de la table
public class Borrow {

    @Id // ← AJOUTÉ : Clé primaire
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ← AJOUTÉ : Auto-increment
    private int id;

    @ManyToOne(fetch = FetchType.LAZY) // ← AJOUTÉ : Relation avec User
    @JoinColumn(name = "user_id", nullable = false) // ← AJOUTÉ : Colonne de jointure
    private User user;

    @ManyToOne(fetch = FetchType.LAZY) // ← AJOUTÉ : Relation avec Book
    @JoinColumn(name = "book_id", nullable = false) // ← AJOUTÉ : Colonne de jointure
    private Book book;

    @Column(name = "borrow_date", nullable = false) // ← AJOUTÉ : Nom de colonne
    private LocalDate borrowDate;

    @Column(name = "due_date", nullable = false) // ← AJOUTÉ : Date limite obligatoire
    private LocalDate dueDate;

    @Column(name = "return_date") // ← AJOUTÉ : Date de retour (peut être null)
    private LocalDate returnDate;

    @Enumerated(EnumType.STRING) // ← AJOUTÉ : Pour sauvegarder l'enum comme texte
    @Column(nullable = false)
    private BorrowStatus status;

    // Constructeur par défaut - OBLIGATOIRE pour JPA
    public Borrow() {
        // Constructeur vide nécessaire pour Spring Boot/JPA
    }

    // Constructor avec paramètres
    public Borrow(User user, Book book, LocalDate borrowDate, LocalDate dueDate, BorrowStatus status) {
        this.user = user;
        this.book = book;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.status = status;
    }

    // Méthode utilitaire pour vérifier si un livre est disponible
    // Note: Cette méthode devrait plutôt être dans une classe Service
    public boolean isBookAvailable() {
        // Cette logique sera mieux dans un Service, mais gardée pour compatibilité
        return book != null && book.getAvailable();
    }

    // Méthode pour vérifier si l'emprunt est en retard
    public boolean isLate() {
        if (returnDate != null) {
            return false; // Déjà rendu
        }
        return LocalDate.now().isAfter(dueDate);
    }

    // Méthode pour calculer les jours de retard
    public int getDaysLate() {
        if (!isLate()) return 0;
        return (int) java.time.temporal.ChronoUnit.DAYS.between(dueDate, LocalDate.now());
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public LocalDate getBorrowDate() {
        return borrowDate;
    }

    public void setBorrowDate(LocalDate borrowDate) {
        this.borrowDate = borrowDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public BorrowStatus getStatus() {
        return status;
    }

    public void setStatus(BorrowStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Borrow{" +
                "id=" + id +
                ", user=" + (user != null ? user.getUsername() : "null") +
                ", book=" + (book != null ? book.getTitle() : "null") +
                ", borrowDate=" + borrowDate +
                ", dueDate=" + dueDate +
                ", returnDate=" + returnDate +
                ", status=" + status +
                '}';
    }
}