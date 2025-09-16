package com.libraryproject.controller;

import com.libraryproject.dao.BorrowDAO;
import com.libraryproject.model.Book;
import com.libraryproject.model.Borrow;
import com.libraryproject.model.User;
import com.libraryproject.service.BookService;
import com.libraryproject.service.BorrowService;
import com.libraryproject.enums.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/borrows")
public class BorrowController {

    @Autowired
    private BorrowService borrowService;

    @Autowired
    private BookService bookService;
    @Autowired
    private BorrowDAO borrowDAO;

    // Vérifier si l'utilisateur est connecté
    private User checkUserAccess(HttpSession session) {
        return (User) session.getAttribute("user");
    }

    // Mes emprunts (READER)
    @GetMapping("/my")
    public String myBorrows(HttpSession session, Model model) {
        User user = checkUserAccess(session);
        if (user == null) return "redirect:/login";

        try {
            List<Borrow> borrowsUser = borrowService.getAllBorrowsByUser(user.getId());
            model.addAttribute("borrows", borrowsUser);

            //Create a variable to use in Html
            List<Borrow> userBorrow = borrowDAO.getAllActiveBorrows();

            model.addAttribute("userBorrow", userBorrow);
            //model.addAttribute("canBorrow", bookService.isBookAvailable(borrowsUser.get(0).getBook().getId()));
            System.out.println("borrowUser size : " + borrowsUser.size());
            model.addAttribute("user", user);
            return "borrows/my-borrows";
        } catch (Exception e) {
            model.addAttribute("error", "Erreur lors du chargement: " + e.getMessage());
            return "dashboard";
        }
    }

    // Catalogue des livres disponibles (READER)
    @GetMapping("/catalog")
    public String showCatalog(HttpSession session, Model model) {
        User user = checkUserAccess(session);
        if (user == null) return "redirect:/login";

        try {
            model.addAttribute("books", bookService.getAllBooks(user));
            model.addAttribute("user", user);
            model.addAttribute("canBorrow", user.getRole() == Role.READER);
            return "borrows/catalog";
        } catch (Exception e) {
            model.addAttribute("error", "Erreur lors du chargement: " + e.getMessage());
            return "dashboard";
        }
    }

    // Emprunter un livre (READER uniquement)
    @PostMapping("/borrow/{bookId}")
    public String borrowBook(@PathVariable int bookId,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        User user = checkUserAccess(session);
        if (user == null) return "redirect:/login";

        if (user.getRole() != Role.READER) {
            redirectAttributes.addFlashAttribute("error", "Seuls les lecteurs peuvent emprunter des livres.");
            return "redirect:/borrows/catalog";
        }

        try {
            Book book = bookService.getBookById(bookId);
            if (book == null) {
                redirectAttributes.addFlashAttribute("error", "Livre non trouvé.");
                return "redirect:/borrows/catalog";
            }

            borrowService.borrowBook(user, book);
            redirectAttributes.addFlashAttribute("success",
                    "Livre '" + book.getTitle() + "' emprunté avec succès ! Retour prévu dans 14 jours.");
            return "redirect:/borrows/my";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de l'emprunt: " + e.getMessage());
            return "redirect:/borrows/catalog";
        }
    }


    // Retourner un livre
    @PostMapping("/return/{borrowId}")
    public String returnBook(@PathVariable int borrowId,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        User user = checkUserAccess(session);
        if (user == null) return "redirect:/login";

        try {
            borrowService.returnBook(borrowId);
            redirectAttributes.addFlashAttribute("success", "Livre retourné avec succès !");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors du retour: " + e.getMessage());
        }
        return "redirect:/borrows/my";
    }

    /*
    @PostMapping("/return/{id}")
    public String returnBook(@PathVariable int id, RedirectAttributes redirectAttributes) {
        try {
            borrowService.returnBook(id);
            redirectAttributes.addFlashAttribute("success", "Livre retourné avec succès !");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors du retour : " + e.getMessage());
        }
        return "redirect:/borrows/my";
    }
    */
    /*
    @PostMapping("/renew/{id}")
    public String renewBook(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            borrowService.renewBook(id);
            redirectAttributes.addFlashAttribute("success", "Emprunt renouvelé pour 2 semaines !");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Impossible de renouveler : " + e.getMessage());
        }
        return "redirect:/borrows/my";
    }
     */

    // Détails d'un emprunt
    @GetMapping("/details/{borrowId}")
    public String borrowDetails(@PathVariable int borrowId,
                                HttpSession session,
                                Model model) {
        User user = checkUserAccess(session);
        if (user == null) return "redirect:/login";

        // Cette fonctionnalité nécessiterait une méthode getBorrowById dans BorrowService
        // Pour l'instant, redirection vers mes emprunts
        return "redirect:/borrows";
    }

    private Map<String, Object> calculateBorrowStatus(Borrow borrow) {
        Map<String, Object> statusInfo = new HashMap<>();
        LocalDate now = LocalDate.now();
        LocalDate dueDate = borrow.getDueDate();

        // Vérifier si en retard
        boolean isOverdue = now.isAfter(dueDate) && borrow.getReturnDate() == null;

        // Vérifier si à rendre bientôt (dans les 3 prochains jours)
        boolean isDueSoon = false;
        long daysUntilDue = 0;

        if (!isOverdue && borrow.getReturnDate() == null) {
            daysUntilDue = ChronoUnit.DAYS.between(now, dueDate);
            isDueSoon = daysUntilDue <= 3 && daysUntilDue >= 0;
        }

        statusInfo.put("isOverdue", isOverdue);
        statusInfo.put("isDueSoon", isDueSoon);
        statusInfo.put("daysUntilDue", daysUntilDue);
        statusInfo.put("isReturned", borrow.getReturnDate() != null);

        return statusInfo;
    }

    // Renouveler un emprunt
    @PostMapping("/renew/{id}")
    public String renewBook(@PathVariable int id,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        User user = checkUserAccess(session);
        if (user == null) return "redirect:/login";

        try {
            borrowService.renewBook(id);
            redirectAttributes.addFlashAttribute("success", "Emprunt renouvelé pour 2 semaines !");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Impossible de renouveler : " + e.getMessage());
        }
        return "redirect:/borrows/my";
    }
}