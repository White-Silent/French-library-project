package com.libraryproject.controller;

import com.libraryproject.model.Book;
import com.libraryproject.model.User;
import com.libraryproject.service.BookService;
import com.libraryproject.enums.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/books")
public class BookController {

    @Autowired
    private BookService bookService;

    // Vérifier si l'utilisateur a les permissions d'administrateur
    private User checkAdminAccess(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != Role.ADMIN) {
            return null;
        }
        return user;
    }

    // Afficher la page de détails d’un livre
    @GetMapping("/{id}")
    public String showBookDetails(@PathVariable int id, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        try {
            System.out.println("DEBUG POUR LE SEARCH");
            Book book = bookService.getBookById(id);
            if (book == null) {
                model.addAttribute("error", "Livre non trouvé avec l'ID : " + id);
                return "books/search"; // ou une page d'erreur dédiée
            }

            model.addAttribute("book", book);
            model.addAttribute("user", user);
            return "books/search"; // Le template Thymeleaf pour afficher les détails du livre
        } catch (Exception e) {
            model.addAttribute("error", "Erreur lors de la récupération du livre : " + e.getMessage());
            return "books/search";
        }
    }
    // Afficher tous les livres (ADMIN)
    @GetMapping("/manage")
    public String manageBooks(HttpSession session, Model model) {
        User user = checkAdminAccess(session);
        if (user == null) return "redirect:/login";

        try {
            System.out.println("Nombre de livres récupérés pour l'utilisateur " + user.getUsername() );
            List<Book> booksAll = bookService.getAllBooks();
            System.out.println("Nombre de livres récupérés pour l'admin: " + booksAll.size());
            model.addAttribute("books", booksAll);
            model.addAttribute("availableCount", bookService.countAvailableBooks());
            model.addAttribute("borrowedCount", bookService.countBorrowedBooks());
            //model.addAttribute("books", bookService.getAllBooks(user));
            model.addAttribute("user", user);
            return "books/manage";
        } catch (Exception e) {
            System.out.println("ERREUR DE MANAGE !!!!!!!!");
            System.err.println("Erreur dans manageBooks: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Erreur lors du chargement: " + e.getMessage());
            return "dashboard";
        }
    }

    // Formulaire d'ajout de livre (ADMIN) - CORRECTION PRINCIPALE ICI
    @GetMapping("/add")
    public String showAddBookForm(HttpSession session, Model model) {
        User user = checkAdminAccess(session);
        if (user == null) {
            return "redirect:/login";
        }

        try {
            // CORRECTION: Utilisation du constructeur par défaut
            model.addAttribute("book", new Book());
            model.addAttribute("user", user);

            return "books/add";
        } catch (Exception e) {
            System.err.println("Erreur dans showAddBookForm: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Erreur lors du chargement du formulaire: " + e.getMessage());
            return "redirect:/books/manage";
        }
    }

    // Traitement de l'ajout de livre (ADMIN) - AMÉLIORÉ
    @PostMapping("/add")
    public String addBook(@ModelAttribute Book book,
                          HttpSession session,
                          RedirectAttributes redirectAttributes,
                          Model model) {
        User user = checkAdminAccess(session);
        if (user == null) {
            return "redirect:/login";
        }

        try {
            // Validation des champs obligatoires
            StringBuilder errors = new StringBuilder();

            if (book.getTitle() == null || book.getTitle().trim().isEmpty()) {
                errors.append("Le titre est obligatoire. ");
            }

            if (book.getAuthor() == null || book.getAuthor().trim().isEmpty()) {
                errors.append("L'auteur est obligatoire. ");
            }

            if (book.getCategory() == null || book.getCategory().trim().isEmpty()) {
                errors.append("La catégorie est obligatoire. ");
            }

            if (book.getLanguage() == null || book.getLanguage().trim().isEmpty()) {
                errors.append("La langue est obligatoire. ");
            }

            // Si il y a des erreurs, retourner au formulaire
            if (errors.length() > 0) {
                model.addAttribute("error", errors.toString().trim());
                model.addAttribute("book", book);
                model.addAttribute("user", user);
                return "books/add";
            }

            // Nettoyage des données
            book.setTitle(book.getTitle().trim());
            book.setAuthor(book.getAuthor().trim());
            book.setCategory(book.getCategory().trim());

            if (book.getPublisher() != null) {
                book.setPublisher(book.getPublisher().trim());
            }

            if (book.getDescription() != null) {
                book.setDescription(book.getDescription().trim());
            }

            // Gestion du prix
            if (book.getPrice() < 0) {
                book.setPrice(0.0);
            }

            System.out.println("Tentative d'ajout du livre: " + book.toString());

            bookService.addBook(book, user);
            redirectAttributes.addFlashAttribute("success", "Livre '" + book.getTitle() + "' ajouté avec succès !");
            return "redirect:/books/manage";

        } catch (Exception e) {
            System.err.println("Erreur dans addBook: " + e.getMessage());
            e.printStackTrace();

            model.addAttribute("error", "Erreur lors de l'ajout du livre: " + e.getMessage());
            model.addAttribute("book", book);
            model.addAttribute("user", user);
            return "books/add";
        }
    }

    // Remplacez ces méthodes dans votre BookController.java

    // Formulaire de modification de livre (ADMIN) - VERSION CORRIGÉE
    @GetMapping("/edit/{id}")
    public String showEditBookForm(@PathVariable int id,
                                   HttpSession session,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        User user = checkAdminAccess(session);
        if (user == null) {
            return "redirect:/login";
        }

        try {
            System.out.println("Tentative de récupération du livre avec ID: " + id);

            Book book = bookService.getBookById(id);
            if (book == null) {
                System.err.println("Livre non trouvé avec l'ID: " + id);
                redirectAttributes.addFlashAttribute("error", "Livre non trouvé avec l'ID: " + id);
                return "redirect:/books/manage";
            }

            System.out.println("Livre récupéré pour édition: " + book.toString());

            model.addAttribute("book", book);
            model.addAttribute("user", user);
            return "books/edit";

        } catch (Exception e) {
            System.err.println("Erreur dans showEditBookForm: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Erreur lors du chargement: " + e.getMessage());
            return "redirect:/books/manage";
        }
    }

    // Traitement de la modification de livre (ADMIN) - VERSION CORRIGÉE
    @PostMapping("/edit")
    public String updateBook(@ModelAttribute Book book,
                             HttpSession session,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        User user = checkAdminAccess(session);
        if (user == null) {
            return "redirect:/login";
        }

        try {
            System.out.println("Tentative de mise à jour du livre: " + book.toString());

            // Validation des champs obligatoires
            StringBuilder errors = new StringBuilder();

            if (book.getTitle() == null || book.getTitle().trim().isEmpty()) {
                errors.append("Le titre est obligatoire. ");
            }

            if (book.getAuthor() == null || book.getAuthor().trim().isEmpty()) {
                errors.append("L'auteur est obligatoire. ");
            }

            if (book.getCategory() == null || book.getCategory().trim().isEmpty()) {
                errors.append("La catégorie est obligatoire. ");
            }

            if (book.getLanguage() == null || book.getLanguage().trim().isEmpty()) {
                errors.append("La langue est obligatoire. ");
            }

            // Vérification que l'ID est présent
            if (book.getId() <= 0) {
                errors.append("ID du livre invalide. ");
            }

            // Si il y a des erreurs, retourner au formulaire
            if (errors.length() > 0) {
                model.addAttribute("error", errors.toString().trim());
                model.addAttribute("book", book);
                model.addAttribute("user", user);
                return "books/edit";
            }

            // Nettoyage des données
            book.setTitle(book.getTitle().trim());
            book.setAuthor(book.getAuthor().trim());
            book.setCategory(book.getCategory().trim());

            if (book.getPublisher() != null) {
                book.setPublisher(book.getPublisher().trim());
            }

            if (book.getDescription() != null) {
                book.setDescription(book.getDescription().trim());
            }

            // Gestion du prix
            if (book.getPrice() < 0) {
                book.setPrice(0.0);
            }

            // Appel du service pour mettre à jour
            bookService.updateBook(book, user);

            System.out.println("Livre mis à jour avec succès: " + book.toString());

            redirectAttributes.addFlashAttribute("success",
                    "Livre '" + book.getTitle() + "' modifié avec succès !");
            return "redirect:/books/manage";

        } catch (Exception e) {
            System.err.println("Erreur dans updateBook: " + e.getMessage());
            e.printStackTrace();

            // En cas d'erreur, retourner au formulaire d'édition avec le livre
            model.addAttribute("error", "Erreur lors de la modification: " + e.getMessage());
            model.addAttribute("book", book);
            model.addAttribute("user", user);
            return "books/edit";
        }
    }

    // Méthode utilitaire pour vérifier l'existence d'un livre - NOUVELLE
    @GetMapping("/check/{id}")
    @ResponseBody
    public ResponseEntity<String> checkBookExists(@PathVariable int id, HttpSession session) {
        User user = checkAdminAccess(session);
        if (user == null) {
            return ResponseEntity.status(401).body("Accès non autorisé");
        }

        try {
            Book book = bookService.getBookById(id);
            if (book != null) {
                return ResponseEntity.ok("Livre trouvé: " + book.getTitle());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erreur: " + e.getMessage());
        }
    }

    // Suppression de livre (ADMIN)
    @PostMapping("/delete/{id}")
    public String deleteBook(@PathVariable int id,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        User user = checkAdminAccess(session);
        if (user == null) return "redirect:/login";

        try {
            bookService.deleteBook(id, user);
            redirectAttributes.addFlashAttribute("success", "Livre supprimé avec succès !");
        } catch (Exception e) {
            System.err.println("Erreur dans deleteBook: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la suppression: " + e.getMessage());
        }
        return "redirect:/books/manage";
    }

    // Remplacez cette méthode dans votre BookController.java

    // Recherche de livres (accessible à tous) - VERSION AMÉLIORÉE
    /*
    @GetMapping("/search")
    public String searchBooks(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String language,
            HttpSession session,
            Model model) {

        User user = (User) session.getAttribute("user"); // facultatif si visiteurs autorisés

        try {
            // Appel au service qui gère tous les filtres dynamiques
            List<Book> books = bookService.searchBooks(title, author, category, language);

            model.addAttribute("books", books);
            model.addAttribute("searchType", "Recherche avancée");

            // Construire la chaîne de filtres pour affichage
            List<String> filters = new ArrayList<>();
            if (title != null && !title.isEmpty()) filters.add("Titre: " + title);
            if (author != null && !author.isEmpty()) filters.add("Auteur: " + author);
            if (category != null && !category.isEmpty()) filters.add("Catégorie: " + category);
            if (language != null && !language.isEmpty()) filters.add("Langue: " + language);

            model.addAttribute("searchTerm", String.join(", ", filters));
            return "books/search";

        }  catch (Exception e) {
            // Gestion de toutes les autres exceptions
            model.addAttribute("error", "Erreur inattendue : " + e.getMessage());
            model.addAttribute("books", new ArrayList<>());
            return "books/search";
        }
    }

     */
    // ✅ VERSION CORRIGÉE de la méthode searchBooks dans BookController

    @GetMapping("/search")
    public String searchBooks(@RequestParam(required = false) String title,
                              @RequestParam(required = false) String author,
                              @RequestParam(required = false) String showAll,
                              HttpSession session,
                              Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        try {
            System.out.println("===== DEBUG SEARCH =====");
            System.out.println("Title param: '" + title + "'");
            System.out.println("Author param: '" + author + "'");
            System.out.println("ShowAll param: '" + showAll + "'");

            List<Book> books = new ArrayList<>();

            // Vérifier si on doit afficher tous les livres
            if ("true".equals(showAll)) {
                books = bookService.getAllBooks();
                model.addAttribute("searchType", "Tous les livres");
            }
            // Vérifier si des critères de recherche sont fournis
            else if ((title != null && !title.trim().isEmpty()) ||
                    (author != null && !author.trim().isEmpty())) {

                books = bookService.searchBooks(title, author);

                // Construire le message de recherche
                List<String> criteria = new ArrayList<>();
                if (title != null && !title.trim().isEmpty()) {
                    criteria.add("Titre: " + title.trim());
                }
                if (author != null && !author.trim().isEmpty()) {
                    criteria.add("Auteur: " + author.trim());
                }

                model.addAttribute("searchType", "Résultats de recherche");
                model.addAttribute("searchTerm", String.join(", ", criteria));
            }
            // Si aucun critère, ne pas charger de livres (laisser books vide)
            else {
                books = null; // Pas de recherche effectuée
            }

            model.addAttribute("books", books);
            model.addAttribute("user", user);

            System.out.println("Nombre de livres trouvés: " + (books != null ? books.size() : "aucun"));

            return "books/search";

        } catch (Exception e) {
            System.err.println("ERREUR dans searchBooks: " + e.getMessage());
            e.printStackTrace();

            model.addAttribute("error", "Erreur lors de la recherche: " + e.getMessage());
            model.addAttribute("books", new ArrayList<>());
            model.addAttribute("user", user);

            return "books/search";
        }
    }
    /*
    @GetMapping("/search")
    public String searchBooks(@RequestParam(required = false) String title,
                              @RequestParam(required = false) String author,
                              HttpSession session,
                              Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        System.out.println("===== DEBUG SEARCH =====");
        System.out.println("Title param: " + title);
        System.out.println("Author param: " + author);
        try {
            List<Book> books = bookService.searchBooks(title, author);
            model.addAttribute("books", books);
            return "books/search";
            if (title != null && !title.isEmpty()) {
                model.addAttribute("books", bookService.findBookByTitle(title));
                model.addAttribute("searchType", "titre");
                model.addAttribute("searchTerm", title);
            } else if (author != null && !author.isEmpty()) {
                model.addAttribute("books", bookService.findBookByAuthor(author));
                model.addAttribute("searchType", "auteur");
                model.addAttribute("searchTerm", author);
            }
            else if (category != null && !category.isEmpty()) {
                List<String> categories = bookService.getAllCategories();
                model.addAttribute("categories", categories);


            }
            else {
                model.addAttribute("books", bookService.getAllBooks(user));
            }
            //Appeler la méthode qui fait cela

            model.addAttribute("user", user);
            return "books/search";

        } catch (Exception e) {
            //Pour chercher l'erreur
            e.printStackTrace();
            model.addAttribute("error", "Erreur lors de la recherche: " + e.getMessage());
            return "dashboard";
        }

    }
    */


    // Méthode utilitaire pour vérifier si une chaîne est vide ou null
    private boolean isEmptyOrNull(String str) {
        return str == null || str.trim().isEmpty();
    }
}