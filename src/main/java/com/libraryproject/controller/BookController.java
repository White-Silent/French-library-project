package com.libraryproject.controller;

import com.libraryproject.model.Book;
import com.libraryproject.model.User;
import com.libraryproject.service.BookService;
import com.libraryproject.enums.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;

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

    // Afficher tous les livres (ADMIN)
    @GetMapping("/manage")
    public String manageBooks(HttpSession session, Model model) {
        User user = checkAdminAccess(session);
        if (user == null) return "redirect:/login";

        try {
            model.addAttribute("books", bookService.getAllBooks(user));
            model.addAttribute("user", user);
            return "books/manage";
        } catch (Exception e) {
            model.addAttribute("error", "Erreur lors du chargement: " + e.getMessage());
            return "dashboard";
        }
    }

    // Formulaire d'ajout de livre (ADMIN)
    @GetMapping("/add")
    public String showAddBookForm(HttpSession session, Model model) {
        User user = checkAdminAccess(session);
        if (user == null) return "redirect:/login";

        model.addAttribute("book", new Book("", "", "", "", "", 0.0, "", ""));
        return "books/add";
    }

    // Traitement de l'ajout de livre (ADMIN)
    @PostMapping("/add")
    public String addBook(@ModelAttribute Book book,
                          HttpSession session,
                          RedirectAttributes redirectAttributes) {
        User user = checkAdminAccess(session);
        if (user == null) return "redirect:/login";

        try {
            bookService.addBook(book, user);
            redirectAttributes.addFlashAttribute("success", "Livre ajouté avec succès !");
            return "redirect:/books/manage";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de l'ajout: " + e.getMessage());
            return "redirect:/books/add";
        }
    }

    // Formulaire de modification de livre (ADMIN)
    @GetMapping("/edit/{id}")
    public String showEditBookForm(@PathVariable int id,
                                   HttpSession session,
                                   Model model) {
        User user = checkAdminAccess(session);
        if (user == null) return "redirect:/login";

        try {
            Book book = bookService.getBookById(id);
            if (book == null) {
                model.addAttribute("error", "Livre non trouvé");
                return "redirect:/books/manage";
            }
            model.addAttribute("book", book);
            return "books/edit";
        } catch (Exception e) {
            model.addAttribute("error", "Erreur: " + e.getMessage());
            return "redirect:/books/manage";
        }
    }

    // Traitement de la modification de livre (ADMIN)
    @PostMapping("/edit")
    public String updateBook(@ModelAttribute Book book,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        User user = checkAdminAccess(session);
        if (user == null) return "redirect:/login";

        try {
            bookService.updateBook(book, user);
            redirectAttributes.addFlashAttribute("success", "Livre modifié avec succès !");
            return "redirect:/books/manage";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la modification: " + e.getMessage());
            return "redirect:/books/edit/" + book.getId();
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
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la suppression: " + e.getMessage());
        }
        return "redirect:/books/manage";
    }

    // Recherche de livres (accessible à tous)
    @GetMapping("/search")
    public String searchBooks(@RequestParam(required = false) String title,
                              @RequestParam(required = false) String author,
                              HttpSession session,
                              Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        try {
            if (title != null && !title.isEmpty()) {
                model.addAttribute("books", bookService.findBookByTitle(title));
                model.addAttribute("searchType", "titre");
                model.addAttribute("searchTerm", title);
            } else if (author != null && !author.isEmpty()) {
                model.addAttribute("books", bookService.findBookByAuthor(author));
                model.addAttribute("searchType", "auteur");
                model.addAttribute("searchTerm", author);
            } else {
                model.addAttribute("books", bookService.getAllBooks(user));
            }

            model.addAttribute("user", user);
            return "books/search";
        } catch (Exception e) {
            model.addAttribute("error", "Erreur lors de la recherche: " + e.getMessage());
            return "dashboard";
        }
    }
}