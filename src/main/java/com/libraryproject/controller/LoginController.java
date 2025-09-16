package com.libraryproject.controller;

import com.libraryproject.dao.BookDAO;
import com.libraryproject.dao.BorrowDAO;
import com.libraryproject.model.Book;
import com.libraryproject.model.Borrow;
import com.libraryproject.service.BookService;
import com.libraryproject.service.BorrowService;
import com.libraryproject.service.UserService;
import com.libraryproject.dao.UserDAO;
import com.libraryproject.model.User;
import com.libraryproject.enums.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static java.sql.DriverManager.getConnection;

//Indique que cette classe gère les requêtes HTTP
@Controller
public class LoginController {

    //Onjection du service
    @Autowired
    private UserService userService;

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private BookDAO bookDAO;

    @Autowired
    private BorrowDAO borrowDAO;

    @Autowired
    private BookService bookService;

    @Autowired
    private BorrowService borrowService;

    // Route racine - redirection vers login
    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }

    // Afficher la page de login
    @GetMapping("/login")
    public String showLoginForm(Model model, HttpSession session) {
        String visa = UserDAO.generateVisa();
        session.setAttribute("generatedVisa", visa);

        model.addAttribute("visa", visa);
        model.addAttribute("roles", Role.values());
        try {
            List<User> users = userDAO.getAllUsers();
            System.out.println("Nombre d'utilisateurs : " + users.size());
        }
        catch (SQLException e) {
            e.printStackTrace();
        }


        return "login"; // correspond à login.html dans templates/
    }

    // Traiter la soumission du formulaire de login
    @PostMapping("/login")
    public String processLogin(@RequestParam String username,
                               @RequestParam String password,
                               @RequestParam Role role,
                               @RequestParam String visa,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {

        String generatedVisa = (String) session.getAttribute("generatedVisa");

        if (!visa.equals(generatedVisa)) {
            redirectAttributes.addFlashAttribute("error", "Visa incorrect. Nouveau code généré.");
            return "redirect:/login";
        }

        try {
            User user = userService.authentificate(username, password, role);

            if (user != null) {
                session.setAttribute("user", user);
                return "redirect:/dashboard";
            } else {
                redirectAttributes.addFlashAttribute("error", "Identifiants ou rôle invalide.");
                return "redirect:/login";
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la connexion: " + e.getMessage());
            return "redirect:/login";
        }
    }

    // Dashboard après connexion réussie
    /*
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        model.addAttribute("user", user);

        return "dashboard";
    }
     */

    // Ajoutez cette méthode dans votre LoginController existant

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        try {
            model.addAttribute("user", user);

            if (user.getRole() == Role.ADMIN) {
                // Dashboard ADMIN
                setupAdminDashboard(user, model);
            } else if (user.getRole() == Role.READER) {
                // Dashboard READER
                setupReaderDashboard(user, model);
            }

            return "dashboard";

        } catch (Exception e) {
            System.err.println("Erreur dans le dashboard: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Erreur lors du chargement du dashboard: " + e.getMessage());
            return "dashboard";
        }
    }

    private void setupAdminDashboard(User user, Model model) throws Exception {
        try {
            // Récupérer tous les livres pour l'admin
            List<Book> allBooks = bookService.getAllBooks();
            model.addAttribute("allBooks", allBooks);

            // Statistiques pour l'admin
            int totalBooks = allBooks.size();
            int availableBooks = bookService.countAvailableBooks();
            int borrowedBooks = totalBooks - availableBooks;

            model.addAttribute("totalBooks", totalBooks);
            model.addAttribute("availableBooks", availableBooks);
            model.addAttribute("borrowedBooks", borrowedBooks);

            System.out.println("Admin Dashboard - Total: " + totalBooks + ", Disponibles: " + availableBooks + ", Empruntés: " + borrowedBooks);

        } catch (Exception e) {
            System.err.println("Erreur dans setupAdminDashboard: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Configuration du dashboard pour les lecteurs
     */
    private void setupReaderDashboard(User user, Model model) throws Exception {
        try {
            // Récupérer les emprunts de l'utilisateur
            List<Borrow> myBorrows = borrowService.getActiveBorrowsByUser(user.getId());
            model.addAttribute("myBorrows", myBorrows);

            // Récupérer les livres disponibles pour l'emprunt
            List<Book> availableBooks = bookService.getAvailableBooks();
            model.addAttribute("books", availableBooks);

            // Statistiques pour le lecteur
            int activeBorrowsCount = myBorrows.size();
            int availableBooksCount = availableBooks.size();

            model.addAttribute("activeBorrowsCount", activeBorrowsCount);
            model.addAttribute("availableBooksCount", availableBooksCount);

            System.out.println("Reader Dashboard - Emprunts actifs: " + activeBorrowsCount + ", Livres disponibles: " + availableBooksCount);

        } catch (Exception e) {
            System.err.println("Erreur dans setupReaderDashboard: " + e.getMessage());
            throw e;
        }
    }

    // Déconnexion
    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}