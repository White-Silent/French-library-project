import java.sql.*;
import java.util.Random;

public class UserDAO {
    //Attribut
    private Connection connection;

    //Constructor
    public UserDAO(Connection connection){
        this.connection = connection;
    }

    //Générer un visa de manière aléatoire
    public static String generateVisa() {
        Random rand = new Random();
        //le rand.nextInt est un int mais du coup on le cast en char
        char letter1 = (char) ('A' + rand.nextInt(26));
        char letter2 = (char) ('A' + rand.nextInt(26));
        int number = rand.nextInt(100);
        return String.format("%c%c%02d", letter1, letter2, number);
    }
    //Add a new User
    //INSERT (Java -> SQL)
    public void addUser(User user) throws SQLException {
        if (user.getVisa() == null || user.getVisa().isEmpty()){
            user.setVisa(generateVisa());
        }
        String sqlRequest = "INSERT INTO users (username, password, role, visa) VALUES (?, ?, ?, ?)";
        //Retourner les clés générés demandés de manière explicites
        //Jdbc sait maintenant qu'il doit retourner la clé générée
        try (PreparedStatement ps = connection.prepareStatement(sqlRequest, Statement.RETURN_GENERATED_KEYS)){
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            //Conversion Role -> String
            ps.setString(3, user.getRole().toString());
            ps.setString(4, user.getVisa());
            ps.executeUpdate();

            //Retrieve the id
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    user.setId(rs.getInt(1));
                }
            }
        }
    }

    //Get les id's
    //SELECT (SQL -> Java)

    private User buildUserFromResultSet(ResultSet rs) throws SQLException {
        Role role = Role.fromDbValue(rs.getString("role"));
        User user = new User(
                rs.getString("username"),
                rs.getString("password"),
                role,
                rs.getString("visa")
        );
        user.setId(rs.getInt("id"));
        return user;
    }

    public User getUserById(int id) throws SQLException {
        String sqlRequest = "SELECT * FROM users WHERE id = ?";
        //Crée un object PreparedStatement grâce à la connexion SQL
        //try -> vérifier que ps soit fermé automatiquement
        try (PreparedStatement ps = connection.prepareStatement(sqlRequest)) {
            //Le 1 correspond à la position où l'on change son id par le ?
            //Cela permet de laisse le compilateur comprendre que l'on remplace le ? à la position 1 ici
            //cela correspond à l'id
            ps.setInt(1, id);
            //Exécution de la requête SQL puis on stocke le résultat dans un ResultSet
            //try -> pour garantir que rs soit bien fermé
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    //Build your object User with a ROle enum and not a string
                    //Conversion String->Role pour correspondre à ce qu'attend à la construction du User
                    /*
                    Role role = Role.fromDbValue(rs.getString("role"));
                    User user = new User(
                            rs.getString("username"),
                            rs.getString("password"),
                            role,
                            rs.getString("visa")
                    );
                    user.setId(rs.getInt("id"));
                    return user;
                    */
                    return buildUserFromResultSet(rs);
                }
            }
        }
        return null;
    }

    public User getUserByUsername(String username) throws SQLException {
        String sqlRequest = "SELECT * FROM users WHERE username = ?";
        try (PreparedStatement ps = connection.prepareStatement(sqlRequest)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return buildUserFromResultSet(rs);
                }
            }
        }
        return null;
    }

    //Verify the authentification
    public User authentificate(String username, String password, Role role, String visa) throws SQLException {
        String sqlRequest = "SELECT * FROM users WHERE username = ? AND password = ? AND role = ? AND visa = ?";
        try (PreparedStatement ps = connection.prepareStatement(sqlRequest)){
            // Replace all of the ? by the corresponding String that you enter
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, role.name());
            ps.setString(4, visa);
            //ResultSet temporary table to retrieve the results
            try (ResultSet rs = ps.executeQuery()){
                //Check la première ligne du résultat si c'est bon
                if (rs.next()){
                    //Create the objet Java to mainpulate his infos - garde juste en mémoire
                    User user = new User(rs.getString("username"), rs.getString("password"), Role.fromDbValue(rs.getString("role")), rs.getString("visa"));
                    user.setId(rs.getInt("id"));
                    return user;
                }
                else {
                    return null;
                }
            }
        }
    }
}
