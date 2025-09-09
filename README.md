Library System Project
Description

Ce projet est un système de gestion de bibliothèque développé en Java, utilisant une base de données MySQL pour stocker les informations des utilisateurs, livres et emprunts. L’objectif est de créer une application complète permettant de gérer les opérations courantes d’une bibliothèque, telles que l’ajout, la modification et la suppression de livres et d’utilisateurs, ainsi que l’emprunt et le retour de livres.

Le projet suit une approche backend-first, avec une architecture en DAO/Service pour gérer les interactions avec la base de données et assurer la logique métier. Les tests unitaires sont intégrés pour garantir la fiabilité du code avant l’implémentation de l’interface utilisateur.

Fonctionnalités principales

Gestion des utilisateurs (lecteurs et administrateurs) avec authentification sécurisée.

Gestion des livres : ajout, mise à jour, suppression et recherche par titre ou auteur.

Gestion des emprunts : emprunt et retour de livres avec suivi des dates.

Vérification des rôles : seules certaines actions sont autorisées selon le rôle de l’utilisateur.

Interface simple pour interagir avec le système (console ou GUI).

Tests unitaires pour valider toutes les fonctionnalités.

Technologies utilisées

Java 17 pour la logique applicative.

JDBC / MySQL pour la gestion des données.

JUnit 5 pour les tests unitaires.

Swing (optionnel) pour une interface graphique simple.

Structure du projet

DAO : Classes pour interagir avec la base de données (UserDAO, BookDAO, BorrowDAO).

Service : Classes pour gérer la logique métier (UserService, BookService, BorrowService).

Model : Classes représentant les entités (User, Book, Borrow, Role, BorrowStatus).

Test : Tests unitaires pour toutes les fonctionnalités CRUD et l’authentification.

Plan de développement (à titre indicatif)

Le projet est développé sur 14 semaines, en commençant par l’installation des outils et la consolidation des bases en Java et SQL, puis la conception de la base de données, le développement CRUD, l’implémentation de l’interface et enfin les tests et améliorations.
