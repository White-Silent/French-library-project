package com.libraryproject.controller;

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

@Controller
public class LoginController {

    @Autowired
    private UserService userService;

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
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        model.addAttribute("user", user);
        return "dashboard";
    }

    // Déconnexion
    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}