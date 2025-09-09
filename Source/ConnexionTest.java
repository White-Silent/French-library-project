import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

public class ConnexionTest {
    public static void main(String[] args) {
        String url = "jdbc:mysql://192.168.101.134:3306/bibliotheque_test";
        String user = "javauser";
        String password = "Daumesnil504!";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            System.out.println("Connexion réussie !");

            BookDAO bookDAO = new BookDAO(conn);

            // --- 1. Add books ---
            Book b1 = new Book("Le Petit Prince", "Children", "Antoine de Saint-Exupéry",
                    "Gallimard", "French", 12.5, "1943-04-06", "Classic French tale");
            Book b2 = new Book("Hunger Games", "Adventure", "Suzanne Collins",
                    "Scholastic", "English", 10.99, "2008-09-14", "Dystopian novel");
            Book b3 = new Book("The One Thing", "Adult", "Gary",
                    "Gallimard", "English", 25.0, "2022-04-06", "Deep Thinking");

            bookDAO.addBook(b1);
            bookDAO.addBook(b2);
            bookDAO.addBook(b3);
            System.out.println("Books added!");

            // --- 2. List all books ---
            List<Book> books = bookDAO.getAllBooks();
            System.out.println("Listing all books:");
            for (Book b : books) {
                System.out.println(b);
            }

            // --- 3. Update a book ---
            Book bookToUpdate = books.get(0);
            bookToUpdate.setPrice(15.0);
            bookToUpdate.setTitle("Le Petit Prince - Revised");
            bookDAO.updateBook(bookToUpdate);
            System.out.println("Updated first book!");

            // --- 4. Delete a book ---
            int idToDelete = books.get(1).getId();
            bookDAO.deleteBook(idToDelete);
            System.out.println("Deleted second book!");

            // --- 5. List books again ---
            System.out.println("Listing all books after update & delete:");
            List<Book> updatedBooks = bookDAO.getAllBooks();
            for (Book b : updatedBooks) {
                System.out.println(b);
            }

            // --- 6. Search books by title ---
            System.out.println("Searching books by title 'Petit':");
            List<Book> foundByTitle = bookDAO.findBookByTitle("Petit");
            for (Book b : foundByTitle) {
                System.out.println(b);
            }

            // --- 7. Search books by author ---
            System.out.println("Searching books by author 'Collins':");
            List<Book> foundByAuthor = bookDAO.findBookByAuthor("Collins");
            for (Book b : foundByAuthor) {
                System.out.println(b);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Erreur de connexion !");
        }
    }
}
