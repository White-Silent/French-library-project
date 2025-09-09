import java.sql.SQLException;

public class UserService {
    //Attribute
    private UserDAO userDAO;

    //Constructor
    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    //Method
    //Generate a Visa
    public String generateVisa() {
        return userDAO.generateVisa();
    }

    public void addUser(User user) throws SQLException {
        if (user.getUsername() == null || user.getUsername().isEmpty()){
            throw new RuntimeException("Username is required !");
        }
        if (user.getVisa() == null || user.getVisa().isEmpty()){
            user.setVisa(generateVisa());
        }
        userDAO.addUser(user);
    }

    public User getUserById(int id) throws SQLException {
        return userDAO.getUserById(id);
    }

    public User authenticate(String username, String password, Role role, String visa) throws SQLException {
        User user = userDAO.getUserById()
    }
}
