package com.libraryproject.model;

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

    //Method to String -> Easier to debug if there is some issue
    @Override
    public String toString() {
        return "Book [id=" + id
                + ", title=" + title
                + ", category=" + category
                + ", author=" + author
                + ", publisher=" + publisher
                + ", language=" + language
                + ", price=" + price
                + ", publicationDate="
                + publicationDate + ", description="
                + description + "]";
    }
}
