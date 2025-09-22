CREATE TABLE IF NOT EXISTS users (
                                     id INT AUTO_INCREMENT PRIMARY KEY,
                                     username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL
    );

CREATE TABLE IF NOT EXISTS books (
                                     id INT AUTO_INCREMENT PRIMARY KEY,
                                     title VARCHAR(255) NOT NULL,
    category VARCHAR(100),
    author VARCHAR(100),
    publisher VARCHAR(100),
    language VARCHAR(50),
    price DECIMAL(10,2),
    publication DATE,
    description TEXT,
    available BOOLEAN
    );
-- ----------------------------
-- Insertion des utilisateurs
-- ----------------------------
INSERT INTO users (username, password, role) VALUES
                                                 ('admin', 'admin123', 'ADMIN'),
                                                 ('user1', 'user123', 'READER'),
                                                 ('marie.dupont', 'password123', 'READER'),
                                                 ('jean.martin', 'password456', 'READER'),
                                                 ('sophie.leclerc', 'password789', 'READER');

-- ----------------------------
-- Insertion des livres
-- ----------------------------
INSERT INTO books (title, category, author, publisher, language, price, publication, description, available) VALUES
                                                                                                                      ('Le Petit Prince', 'Fiction', 'Antoine de Saint-Exupéry', 'Gallimard', 'Français', 12.99, '1943-04-06', 'Un conte poétique et philosophique sous l''apparence d''un conte pour enfants.', true),
                                                                                                                      ('1984', 'Science-Fiction', 'George Orwell', 'Secker & Warburg', 'Anglais', 15.50, '1949-06-08', 'Un roman dystopique qui dépeint une société totalitaire.', true),
                                                                                                                      ('L''Étranger', 'Philosophie', 'Albert Camus', 'Gallimard', 'Français', 14.20, '1942-01-01', 'Premier roman publié par Albert Camus en 1942.', true),
                                                                                                                      ('Harry Potter à l''école des sorciers', 'Fantasy', 'J.K. Rowling', 'Bloomsbury', 'Anglais', 18.99, '1997-06-26', 'Le premier tome de la saga Harry Potter.', true),
                                                                                                                      ('Les Misérables', 'Classique', 'Victor Hugo', 'A. Lacroix, Verboeckhoven & Cie', 'Français', 22.50, '1862-03-30', 'Roman historique, social et philosophique.', true),
                                                                                                                      ('Pride and Prejudice', 'Romance', 'Jane Austen', 'T. Egerton', 'Anglais', 13.75, '1813-01-28', 'Roman de mœurs de la société anglaise de la fin du XVIIIe siècle.', true),
                                                                                                                      ('Le Seigneur des Anneaux', 'Fantasy', 'J.R.R. Tolkien', 'George Allen & Unwin', 'Anglais', 25.99, '1954-07-29', 'Œuvre de fantasy épique en trois volumes.', true),
                                                                                                                      ('Guerre et Paix', 'Historique', 'Léon Tolstoï', 'The Russian Messenger', 'Russe', 28.00, '1869-01-01', 'Roman-fleuve qui dépeint la société russe à l''époque napoléonienne.', false),
                                                                                                                      ('Le Comte de Monte-Cristo', 'Aventure', 'Alexandre Dumas', 'Pétion', 'Français', 19.95, '1844-08-28', 'Roman d''aventures historique.', true),
                                                                                                                      ('Fahrenheit 451', 'Science-Fiction', 'Ray Bradbury', 'Ballantine Books', 'Anglais', 16.50, '1953-10-19', 'Roman dystopique sur la censure des livres.', true);
