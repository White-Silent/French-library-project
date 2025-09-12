package com.libraryproject.dao;

// Import des classes nécessaires
import com.libraryproject.model.Book;
import com.libraryproject.model.User;
import com.libraryproject.model.Borrow;
import com.libraryproject.enums.BorrowStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository // ← Indique à Spring que c'est un composant qui gère l'accès aux données
public class BorrowDAO {

    @Autowired // ← Demande à Spring d'injecter automatiquement la DataSource
    private DataSource dataSource; // ← Pool de connexions à la BDD

    @Autowired // ← Injection des dépendances nécessaires
    private UserDAO userDAO;  // Pour récupérer les utilisateurs

    @Autowired
    private BookDAO bookDAO;  // Pour récupérer les livres

    // Méthode utilitaire pour obtenir une connexion du pool
    private Connection getConnection() throws SQLException {
        return dataSource.getConnection(); // ← Emprunte une connexion existante
    }

    // Crée un nouvel emprunt dans la base de données
    public void borrowBook(Borrow borrow) throws SQLException {
        String sql = "INSERT INTO borrows (user_id, book_id, borrow_date, due_date, status) VALUES (?,?,?,?,?)";
        // ↑ Requête paramétrée pour éviter les injections SQL

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)){
            // Remplissage des paramètres
            ps.setInt(1, borrow.getUser().getId());
            ps.setInt(2, borrow.getBook().getId());
            ps.setDate(3, java.sql.Date.valueOf(borrow.getBorrowDate()));
            ps.setDate(4, java.sql.Date.valueOf(borrow.getDueDate()));
            ps.setString(5, borrow.getStatus().name());
            ps.executeUpdate(); // ← Exécution de l'INSERT
        } // ← Fermeture automatique des ressources
    }

    // Marque un livre comme retourné
    public void returnBook(int borrowId, LocalDate returnDate) throws SQLException{
        String sql = "UPDATE borrows SET return_date = ?, status = ? WHERE id = ?";

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)){
            ps.setDate(1, java.sql.Date.valueOf(returnDate));
            ps.setString(2, BorrowStatus.RETURNED.name());
            ps.setInt(3, borrowId);
            ps.executeUpdate(); // ← Exécution de la mise à jour
        }
    }

    // Transforme un ResultSet (résultat BDD) en objet Borrow
    private Borrow mapResultSetToBorrow(ResultSet rs) throws SQLException {
        // 1. Récupération des IDs
        int userId = rs.getInt("user_id");
        int bookId = rs.getInt("book_id");

        // 2. Récupération des objets complets via les DAO
        User user = userDAO.getUserById(userId);
        Book book = bookDAO.getBookById(bookId);

        // 3. Création de l'objet Borrow avec les données obligatoires
        Borrow borrow = new Borrow(user, book,
                rs.getDate("borrow_date").toLocalDate(),
                rs.getDate("due_date").toLocalDate(),
                BorrowStatus.BORROWED);
        borrow.setId(rs.getInt("id")); // Attribution de l'ID

        // 4. Gestion optionnelle de la date de retour
        Date returnDate = rs.getDate("return_date");
        if (returnDate != null){
            borrow.setReturnDate(returnDate.toLocalDate());
        }

        // 5. Mise à jour du statut réel
        borrow.setStatus(BorrowStatus.fromDbValue(rs.getString("status")));

        return borrow;
    }

    // Récupère tous les emprunts d'un utilisateur
    public List<Borrow> getBorrowsByUser(int userId) throws SQLException{
        List<Borrow> borrows = new ArrayList<>();
        String sqlRequest = "SELECT * FROM borrows WHERE user_id = ?";

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sqlRequest)){
            ps.setInt(1, userId); // ← Paramètre user_id

            try (ResultSet rs = ps.executeQuery()){ // ← Exécution requête
                while (rs.next()){ // ← Parcours de tous les résultats
                    borrows.add(mapResultSetToBorrow(rs)); // ← Transformation
                }
            }
        }
        return borrows;
    }

    /**
     * Récupère les emprunts actifs (BORROWED ou LATE) d'un utilisateur
     */
    public List<Borrow> getActiveBorrowsByUser(int userId) throws SQLException {
        List<Borrow> activeBorrows = new ArrayList<>();
        String sql = "SELECT * FROM borrows WHERE user_id = ? AND status IN ('BORROWED', 'LATE')";

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    activeBorrows.add(mapResultSetToBorrow(rs));
                }
            }
        }

        return activeBorrows;
    }

    /**
     * Récupère tous les emprunts actifs du système
     */
    public List<Borrow> getAllActiveBorrows() throws SQLException {
        List<Borrow> activeBorrows = new ArrayList<>();
        String sql = "SELECT * FROM borrows WHERE status IN ('BORROWED', 'LATE')";

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                activeBorrows.add(mapResultSetToBorrow(rs));
            }
        }

        return activeBorrows;
    }

    /**
     * Vérifie si un utilisateur peut emprunter un livre
     * (le livre doit être disponible ET l'utilisateur ne doit pas l'avoir déjà emprunté)
     */
    public boolean canUserBorrowBook(int userId, int bookId) throws SQLException {
        String sql = """
        SELECT COUNT(*) FROM borrows 
        WHERE book_id = ? AND status IN ('BORROWED', 'LATE')
        """;

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, bookId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) == 0; // Peut emprunter si pas d'emprunt actif
                }
            }
        }

        return false;
    }

    /**
     * Compte le nombre d'emprunts actifs d'un utilisateur
     */
    public int countActiveBorrowsByUser(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM borrows WHERE user_id = ? AND status IN ('BORROWED', 'LATE')";

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        return 0;
    }
}