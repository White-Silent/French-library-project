package com.libraryproject.model;

import com.libraryproject.enums.BorrowStatus;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;

import java.util.ArrayList;
import java.util.List;

//Database book
public class Book {
    //Attributes
    private int id;
    private String title;
    private String category;
    private String author;
    private String publisher;
    private String language;
    private double price;
    private String publicationDate;
    private String description;
    private boolean available;

    // CONSTRUCTEUR PAR DÉFAUT - OBLIGATOIRE pour Spring Boot
    /*
    public Book() {
        // Constructeur vide nécessaire pour Spring Boot et Thymeleaf
        // Initialisation avec des valeurs par défaut si nécessaire
        this.title = "";
        this.category = "";
        this.author = "";
        this.publisher = "";
        this.language = "";
        this.price = 0.0;
        this.publicationDate = "";
        this.description = "";
        this.available = true;
    }
    */

    private Integer currentBorrowCount; // Nombre d'emprunts en cours
    private Integer totalBorrowCount;   // Nombre total d'emprunts historiques

    // Constructeurs
    public Book() {
        this.available = true; // Par défaut disponible
        this.currentBorrowCount = 0;
        this.totalBorrowCount = 0;
    }

    //Constructor of the Book - for the creation of the Object
    public Book(String title, String category, String author, String publisher, String language, double price, String publicationDate, String description) {
        this.title = title;
        this.category = category;
        this.author = author;
        this.publisher = publisher;
        this.language = language;
        this.price = price;
        this.publicationDate = publicationDate;
        this.description = description;
        this.available = true;
    }

    //Add Getter and Setter - for all of these attributes
    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id = id;
    }

    public String getTitle(){
        return title;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public String getCategory(){
        return category;
    }

    public void setCategory(String category){
        this.category = category;
    }

    public String getAuthor(){
        return author;
    }

    public void setAuthor(String author){
        this.author = author;
    }

    public String getPublisher(){
        return publisher;
    }

    public void setPublisher(String publisher){
        this.publisher = publisher;
    }

    public String getLanguage(){
        return language;
    }

    public void setLanguage(String language){
        this.language = language;
    }

    public double getPrice(){
        return price;
    }

    public void setPrice(double price){
        this.price = price;
    }

    public String getPublicationDate(){
        return publicationDate;
    }

    public void setPublicationDate(String publicationDate){
        this.publicationDate = publicationDate;
    }

    public String getDescription(){
        return description;
    }

    public void setDescription(String description){
        this.description = description;
    }

    public boolean getAvailable(){
        return available;
    }

    public void setAvailable(boolean available){
        this.available = available;
    }

    // ✅ NOUVEAUX GETTERS ET SETTERS pour les statistiques
    public Integer getCurrentBorrowCount() { return currentBorrowCount; }
    public void setCurrentBorrowCount(Integer currentBorrowCount) {
        this.currentBorrowCount = currentBorrowCount;
    }

    public Integer getTotalBorrowCount() { return totalBorrowCount; }
    public void setTotalBorrowCount(Integer totalBorrowCount) {
        this.totalBorrowCount = totalBorrowCount;
    }

    // ✅ MÉTHODES UTILITAIRES
    /**
     * Vérifie si le livre peut être supprimé (pas d'emprunts en cours)
     */
    public boolean canBeDeleted() {
        return currentBorrowCount == null || currentBorrowCount == 0;
    }

    /**
     * Vérifie si le livre a un historique d'emprunts
     */
    public boolean hasHistory() {
        return totalBorrowCount != null && totalBorrowCount > 0;
    }

    /**
     * Retourne le statut du livre sous forme de texte
     */
    public String getStatusText() {
        if (currentBorrowCount != null && currentBorrowCount > 0) {
            return "Emprunté (" + currentBorrowCount + " emprunt(s) en cours)";
        } else if (available) {
            return "Disponible";
        } else {
            return "Non disponible";
        }
    }

    @Override
    public String toString() {
        return "Book{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", category='" + category + '\'' +
                ", available=" + available +
                ", currentBorrows=" + currentBorrowCount +
                ", totalBorrows=" + totalBorrowCount +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Book book = (Book) o;
        return id == book.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}