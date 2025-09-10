// 1. Controller principal pour la gestion de l'authentification
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
public class AutoController {

    @Autowired
    private UserService userService;

    // Afficher la page de login
    @GetMapping("/login")
    public String showLoginForm(Model model, HttpSession session) {
        // Générer un nouveau visa à chaque affichage de la page
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

        // Vérifier d'abord le visa
        if (!visa.equals(generatedVisa)) {
            redirectAttributes.addFlashAttribute("error", "Visa incorrect. Nouveau code généré.");
            return "redirect:/login"; // Retour avec nouveau visa
        }

        try {
            // Authentification avec username, password, role
            User user = userService.authentificate(username, password, role);

            if (user != null) {
                // Stocker l'utilisateur en session
                session.setAttribute("user", user);
                return "redirect:/dashboard"; // Redirection vers tableau de bord
            } else {
                redirectAttributes.addFlashAttribute("error", "Identifiants ou rôle invalide.");
                return "redirect:/login";
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la connexion.");
            return "redirect:/login";
        }
    }

    // Dashboard après connexion réussie
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", user);
        return "dashboard";
    }

    // Gestion des emprunts
    @GetMapping("/emprunt")
    public String emprunts(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        // Logique pour récupérer les emprunts selon le rôle
        return "emprunt";
    }

    // Déconnexion
    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}

// 2. Template Thymeleaf pour la page de login (templates/login.html)
/*
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>Library Login</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body>
    <div class="container mt-5">
        <div class="row justify-content-center">
            <div class="col-md-6">
                <div class="card">
                    <div class="card-header">
                        <h3 class="text-center">Library Login</h3>
                    </div>
                    <div class="card-body">
                        <!-- Affichage des messages d'erreur -->
                        <div th:if="${error}" class="alert alert-danger" role="alert">
                            <span th:text="${error}"></span>
                        </div>

                        <form method="post" th:action="@{/login}">
                            <div class="mb-3">
                                <label for="username" class="form-label">Username:</label>
                                <input type="text" class="form-control" id="username" name="username" required>
                            </div>

                            <div class="mb-3">
                                <label for="password" class="form-label">Password:</label>
                                <input type="password" class="form-control" id="password" name="password" required>
                            </div>

                            <div class="mb-3">
                                <label for="role" class="form-label">Role:</label>
                                <select class="form-select" id="role" name="role" required>
                                    <option th:each="role : ${roles}"
                                            th:value="${role}"
                                            th:text="${role}"></option>
                                </select>
                            </div>

                            <div class="mb-3">
                                <label class="form-label">Visa: <strong th:text="${visa}"></strong></label>
                                <input type="text" class="form-control" name="visa"
                                       placeholder="Entrez le code visa" required>
                            </div>

                            <button type="submit" class="btn btn-primary w-100">Login</button>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>
</body>
</html>
*/

// 3. Configuration Spring Boot (application.properties)
/*
# Configuration base de données (adapter selon votre DB)
spring.datasource.url=jdbc:mysql://localhost:3306/library_db
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update

# Configuration Thymeleaf
spring.thymeleaf.prefix=classpath:/templates/
spring.thymeleaf.suffix=.html
spring.thymeleaf.mode=HTML
spring.thymeleaf.encoding=UTF-8

# Configuration serveur
server.port=8080
*/