import java.sql.SQLException;
import java.util.List;

public class BookService {

    //Attribute
    private BookDAO bookDAO;

    //Constructor
    public BookService(BookDAO bookDAO) {
        this.bookDAO = bookDAO;
    }

    //Add book (only the director has the perm)
    public void addBook(Book book, User user) throws SQLException {
        if (user.getRole() == Role.READER) {
            throw new RuntimeException("You don't have the permission to add a book !");
        }
        bookDAO.addBook(book);
    }

    //Update a book ( Only for the director too)
    public void updateBook(Book book, User user) throws SQLException{
        if (user.getRole() == Role.READER){
            throw new RuntimeException("You don't have the permission to update a book !");
        }
        bookDAO.updateBook(book);
    }

    //Delete a book ( Only the director can )
    public void deleteBook(int id, User user) throws SQLException {
        if (user.getRole() == Role.READER){
            throw new RuntimeException("You don't have the permission to delete a book !");
        }
        bookDAO.deleteBook(id);
    }

    //Get all the books (reader and director) can do that
    public List<Book> getAllBooks(User user) throws SQLException {
        return bookDAO.getAllBooks();
    }

    //Search book by title - it may throw an exception - bookDAO are checking exceptions
    public List<Book> findBookByTitle(String title, User user) throws  SQLException{
        return bookDAO.findBookByTitle(title);
    }

    //Search book by author - it may throw an exception
    public List<Book> findBookByAuthor(String author, User user) throws SQLException{
        return bookDAO.findBookByAuthor(author);
    }
}
