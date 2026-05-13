-- =============================================
-- SmartTrip Database - COMPLETE FIX
-- Import in phpMyAdmin
-- =============================================
SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET FOREIGN_KEY_CHECKS = 0;
START TRANSACTION;
SET time_zone = "+00:00";

CREATE DATABASE IF NOT EXISTS `smarttrip_db`
  DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `smarttrip_db`;

-- =============================================
-- TABLE: users
-- Columns match UserDAO.java: username, password_hash, full_name, email, phone, avatar, role
-- =============================================
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(100) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `full_name` varchar(200) DEFAULT '',
  `email` varchar(255) DEFAULT '',
  `phone` varchar(20) DEFAULT '',
  `avatar` varchar(500) DEFAULT NULL,
  `role` varchar(50) DEFAULT 'CLIENT',
  `wallet_balance` double DEFAULT 0,
  `loyalty_points` int(11) DEFAULT 0,
  `est_bloque` tinyint(1) DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- ADMIN account: username=admin, password=admin123 (plain text, accepted for ADMIN role)
-- CLIENT account: username=user, password=password123 (BCrypt hashed)
-- =============================================
INSERT INTO `users` (`username`, `password_hash`, `full_name`, `email`, `phone`, `role`, `wallet_balance`, `loyalty_points`) VALUES
('admin', 'admin123', 'Admin SmartTrip', 'admin@smarttrip.tn', '+21600000000', 'ADMIN', 0, 0),
('user', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Louay User', 'louay@esprit.tn', '+21699999999', 'CLIENT', 500, 120);

-- =============================================
-- TABLE: destination
-- =============================================
DROP TABLE IF EXISTS `destination`;
CREATE TABLE `destination` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `nom` varchar(100) NOT NULL,
  `pays` varchar(100) NOT NULL,
  `code_iata` varchar(10) DEFAULT NULL,
  `description` longtext DEFAULT NULL,
  `image_url` longtext DEFAULT NULL,
  `video_url` varchar(500) DEFAULT NULL,
  `date_creation` datetime NOT NULL DEFAULT current_timestamp(),
  `order` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `destination` (`id`, `nom`, `pays`, `code_iata`, `description`, `image_url`) VALUES
(1, 'Paris', 'France', 'CDG', 'La ville lumiere', 'https://images.unsplash.com/photo-1502602898657-3e91760cbb34?w=800'),
(2, 'Barcelone', 'Espagne', 'BCN', 'Ville catalane vibrante', 'https://images.unsplash.com/photo-1583422409516-2895a77efded?w=800'),
(3, 'Marrakech', 'Maroc', 'RAK', 'Ville imperiale marocaine', 'https://images.unsplash.com/photo-1597212618440-806262de4f6b?w=800'),
(4, 'Santorin', 'Grece', 'JTR', 'Ile grecque magnifique', 'https://images.unsplash.com/photo-1613395877344-13d4a8e0d49e?w=800'),
(5, 'Dubai', 'Emirats Arabes Unis', 'DXB', 'Metropole futuriste du desert', 'https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=800'),
(6, 'Nabeul', 'Tunisie', 'TUN', 'Ville cotiere tunisienne', 'https://images.unsplash.com/photo-1572204292164-b35ba943fca7?w=800'),
(7, 'Djerba', 'Tunisie', 'DJE', 'Ile de reve tunisienne', 'https://images.unsplash.com/photo-1590523741831-ab7e8b8f9c7f?w=800'),
(8, 'Florence', 'Italie', 'FLR', 'Berceau de la Renaissance', 'https://images.unsplash.com/photo-1543429257-3eb0b65d9c58?w=800');

-- =============================================
-- TABLE: voyages (EXACT columns from VoyageServices.java)
-- =============================================
DROP TABLE IF EXISTS `voyages`;
CREATE TABLE `voyages` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `destination` varchar(255) DEFAULT NULL,
  `dateDebut` date DEFAULT NULL,
  `dateFin` date DEFAULT NULL,
  `prix` double DEFAULT 0,
  `imagePath` varchar(500) DEFAULT NULL,
  `description` longtext DEFAULT NULL,
  `destination_id` int(11) DEFAULT NULL,
  `pays_depart` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `voyages` (`destination`, `dateDebut`, `dateFin`, `prix`, `imagePath`, `description`, `destination_id`, `pays_depart`) VALUES
('Paris', '2026-06-01', '2026-06-08', 2500, 'https://images.unsplash.com/photo-1502602898657-3e91760cbb34?w=800', 'Decouvrez Paris, la ville lumiere. Tour Eiffel, Louvre, Champs-Elysees et gastronomie francaise.', 1, 'Tunisie'),
('Barcelone', '2026-06-15', '2026-06-22', 1800, 'https://images.unsplash.com/photo-1583422409516-2895a77efded?w=800', 'Explorez Barcelone, la Sagrada Familia, les plages et la cuisine catalane.', 2, 'Tunisie'),
('Marrakech', '2026-07-01', '2026-07-07', 1200, 'https://images.unsplash.com/photo-1597212618440-806262de4f6b?w=800', 'Vivez la magie de Marrakech, ses souks, la place Jemaa el-Fna et le jardin Majorelle.', 3, 'Tunisie'),
('Santorin', '2026-07-10', '2026-07-17', 3200, 'https://images.unsplash.com/photo-1613395877344-13d4a8e0d49e?w=800', 'Santorin, ile de reve avec ses maisons blanches et couchers de soleil legendaires.', 4, 'Tunisie'),
('Dubai', '2026-08-01', '2026-08-10', 4500, 'https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=800', 'Dubai, la ville du futur. Burj Khalifa, desert safari et shopping de luxe.', 5, 'Tunisie'),
('Djerba', '2026-06-20', '2026-06-27', 800, 'https://images.unsplash.com/photo-1590523741831-ab7e8b8f9c7f?w=800', 'Sejour balnéaire a Djerba. Plages paradisiaques et culture tunisienne.', 7, 'Tunisie'),
('Florence', '2026-09-01', '2026-09-07', 2800, 'https://images.unsplash.com/photo-1543429257-3eb0b65d9c58?w=800', 'Florence, berceau de la Renaissance. Uffizi, Duomo et cuisine toscane.', 8, 'Tunisie');

-- =============================================
-- TABLE: hotels
-- =============================================
DROP TABLE IF EXISTS `hotel_policies`;
DROP TABLE IF EXISTS `hotel_images`;
DROP TABLE IF EXISTS `hotel_amenities`;
DROP TABLE IF EXISTS `hotels`;
CREATE TABLE `hotels` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(200) NOT NULL,
  `location` varchar(200) NOT NULL,
  `city` varchar(100) NOT NULL,
  `country` varchar(100) NOT NULL,
  `description` text DEFAULT NULL,
  `price_per_night` decimal(10,2) NOT NULL,
  `price_per_week` decimal(10,2) DEFAULT NULL,
  `rating` decimal(2,1) DEFAULT 0.0,
  `review_count` int(11) DEFAULT 0,
  `check_in_time` varchar(10) DEFAULT NULL,
  `check_out_time` varchar(10) DEFAULT NULL,
  `contact_email` varchar(100) DEFAULT NULL,
  `contact_phone` varchar(20) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `hotels` (`id`, `name`, `location`, `city`, `country`, `description`, `price_per_night`, `price_per_week`, `rating`, `review_count`, `check_in_time`, `check_out_time`, `contact_email`, `contact_phone`) VALUES
(1, 'The Royal Plaza', 'Central Park West', 'New York', 'United States', 'Luxury at The Royal Plaza with breathtaking views and world-class dining.', 450.00, 2800.00, 4.8, 1247, '15:00', '11:00', 'reservations@royalplaza.com', '+1 (212) 555-0100'),
(2, 'Oceanview Resort & Spa', 'Malibu Beach', 'Malibu', 'United States', 'Tranquility along the pristine shores of Malibu.', 680.00, 4200.00, 4.9, 892, '16:00', '12:00', 'stay@oceanviewresort.com', '+1 (310) 555-0200'),
(3, 'Grand Hotel Paris', 'Champs-Elysees', 'Paris', 'France', 'Belle Epoque grandeur with contemporary luxury.', 520.00, 3200.00, 4.7, 2156, '15:00', '11:00', 'bonjour@grandhotelparis.fr', '+33 1 55 55 01 00'),
(4, 'Tokyo Skyline Hotel', 'Shibuya District', 'Tokyo', 'Japan', 'Japanese hospitality meets modern design.', 380.00, 2400.00, 4.6, 1534, '15:00', '11:00', 'info@tokyoskyline.jp', '+81 3-5555-0100'),
(5, 'Dubai Luxury Palace', 'Palm Jumeirah', 'Dubai', 'United Arab Emirates', 'The pinnacle of Arabian luxury.', 890.00, 5500.00, 4.9, 756, '15:00', '12:00', 'reservations@dubailuxurypalace.ae', '+971 4 555 0100');

-- =============================================
-- TABLE: hotel_amenities
-- =============================================
CREATE TABLE `hotel_amenities` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `hotel_id` int(11) NOT NULL,
  `amenity_name` varchar(100) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `hotel_id` (`hotel_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `hotel_amenities` (`hotel_id`, `amenity_name`) VALUES
(1,'Free WiFi'),(1,'Spa & Wellness'),(1,'Fitness Center'),(1,'Rooftop Pool'),(1,'Concierge'),
(2,'Private Beach'),(2,'Infinity Pool'),(2,'Full-Service Spa'),(2,'Free WiFi'),
(3,'Michelin Restaurant'),(3,'Rooftop Terrace'),(3,'Spa'),(3,'Free WiFi'),
(4,'Rooftop Bar'),(4,'Sushi Restaurant'),(4,'Fitness Center'),(4,'Free WiFi'),
(5,'Private Beach'),(5,'Water Park'),(5,'Spa'),(5,'Helipad'),(5,'Free WiFi');

-- =============================================
-- TABLE: hotel_images
-- =============================================
CREATE TABLE `hotel_images` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `hotel_id` int(11) NOT NULL,
  `image_url` varchar(500) NOT NULL,
  `display_order` int(11) DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `hotel_id` (`hotel_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `hotel_images` (`hotel_id`, `image_url`, `display_order`) VALUES
(1,'https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800&h=600&fit=crop',1),
(2,'https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?w=800&h=600&fit=crop',1),
(3,'https://images.unsplash.com/photo-1564501049412-61c2a3083791?w=800&h=600&fit=crop',1),
(4,'https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?w=800&h=600&fit=crop',1),
(5,'https://images.unsplash.com/photo-1578683010236-d716f9a3f461?w=800&h=600&fit=crop',1);

-- =============================================
-- TABLE: hotel_policies
-- =============================================
CREATE TABLE `hotel_policies` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `hotel_id` int(11) NOT NULL,
  `policy_text` varchar(500) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `hotel_id` (`hotel_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `hotel_policies` (`hotel_id`, `policy_text`) VALUES
(1,'No smoking'),(1,'No pets'),(1,'Check-in from 3 PM'),
(2,'No smoking'),(2,'Pet-friendly'),(2,'Check-in from 4 PM'),
(3,'No smoking'),(3,'No pets'),
(4,'No smoking'),(4,'Quiet hours 10 PM - 7 AM'),
(5,'No smoking'),(5,'Dress code applies');

-- =============================================
-- TABLE: avis
-- =============================================
DROP TABLE IF EXISTS `avis`;
CREATE TABLE `avis` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `nom_client` varchar(255) NOT NULL,
  `email` varchar(255) NOT NULL,
  `note` int(11) NOT NULL,
  `commentaire` longtext NOT NULL,
  `commentaires` longtext DEFAULT NULL,
  `date_avis` date NOT NULL,
  `voyage_id` int(11) NOT NULL,
  `destination` varchar(255) DEFAULT NULL,
  `latitude` double DEFAULT NULL,
  `longitude` double DEFAULT NULL,
  `weather_data` longtext DEFAULT NULL,
  `photos` longtext DEFAULT NULL,
  `main_photo` varchar(255) DEFAULT NULL,
  `status` varchar(20) NOT NULL DEFAULT 'pending',
  `sentiment_analysis` longtext DEFAULT NULL,
  `keywords` longtext DEFAULT NULL,
  `sentiment_score` double DEFAULT NULL,
  `predictive_analysis` longtext DEFAULT NULL,
  `satisfaction_score` double DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `avis` (`nom_client`, `email`, `note`, `commentaire`, `date_avis`, `voyage_id`, `destination`, `latitude`, `longitude`, `status`, `satisfaction_score`) VALUES
('zidi', 'wathekbelleh.zidi@esprit.tn', 5, 'good voyage', '2026-05-02', 1, 'Paris', 48.8534951, 2.3483915, 'approved', 67),
('farid', 'farid@esprit.tn', 5, 'Une experience absolument magique !', '2026-05-02', 2, 'Barcelone', 41.3851, 2.1734, 'approved', 81.5),
('Aziz', 'aziz@gmail.com', 4, 'Experience inoubliable a Marrakech', '2026-05-04', 3, 'Marrakech', 31.6295, -7.9811, 'pending', 62);

-- =============================================
-- TABLE: offre
-- =============================================
DROP TABLE IF EXISTS `offre`;
CREATE TABLE `offre` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `titre` varchar(255) NOT NULL,
  `description` longtext DEFAULT NULL,
  `prix` decimal(10,2) DEFAULT NULL,
  `date_debut` date DEFAULT NULL,
  `date_fin` date DEFAULT NULL,
  `destination_id` int(11) DEFAULT NULL,
  `remise` int(11) DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `offre` (`id`, `titre`, `description`, `prix`, `date_debut`, `date_fin`, `destination_id`, `remise`) VALUES
(28, 'Offre Paris Ete 2026', 'Sejour tout compris a Paris avec visite guidee', 2200.00, '2026-06-01', '2026-08-31', 1, 15),
(31, 'Promo Marrakech', 'Decouverte de Marrakech a prix reduit', 950.00, '2026-06-15', '2026-07-31', 3, 20),
(32, 'Escapade Barcelone', 'Week-end prolonge a Barcelone', 1500.00, '2026-07-01', '2026-09-30', 2, 10);

-- =============================================
-- TABLE: code_promo
-- =============================================
DROP TABLE IF EXISTS `code_promo`;
CREATE TABLE `code_promo` (
  `id_code` int(11) NOT NULL AUTO_INCREMENT,
  `code_texte` varchar(20) NOT NULL,
  `date_expiration` date NOT NULL,
  `id_offre` int(11) NOT NULL,
  PRIMARY KEY (`id_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `code_promo` (`code_texte`, `date_expiration`, `id_offre`) VALUES
('SMART-N8HYDTX0', '2026-06-03', 32),
('SMART-RNB3HE86', '2026-05-14', 32),
('SMART-L1SYH78B', '2026-06-03', 28),
('SMART-7SLCFI39', '2026-06-03', 28),
('SMART-ZM1PFB5H', '2026-06-04', 28);

-- =============================================
-- TABLE: paiement
-- =============================================
DROP TABLE IF EXISTS `paiement`;
CREATE TABLE `paiement` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) DEFAULT NULL,
  `montant` decimal(10,2) NOT NULL,
  `methode` varchar(50) DEFAULT NULL,
  `statut` varchar(50) DEFAULT 'pending',
  `date_paiement` datetime DEFAULT current_timestamp(),
  `stripe_payment_id` varchar(255) DEFAULT NULL,
  `description` text DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `paiement` (`user_id`, `montant`, `methode`, `statut`, `description`) VALUES
(2, 2500.00, 'stripe', 'completed', 'Paiement voyage Paris'),
(2, 1800.00, 'wallet', 'completed', 'Paiement voyage Barcelone'),
(3, 1200.00, 'stripe', 'pending', 'Paiement voyage Marrakech');

-- =============================================
-- TABLE: transport
-- =============================================
DROP TABLE IF EXISTS `transport`;
CREATE TABLE `transport` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `type` varchar(50) NOT NULL,
  `compagnie` varchar(100) DEFAULT NULL,
  `depart` varchar(255) DEFAULT NULL,
  `arrivee` varchar(255) DEFAULT NULL,
  `date_depart` datetime DEFAULT NULL,
  `date_arrivee` datetime DEFAULT NULL,
  `prix` decimal(10,2) DEFAULT NULL,
  `places_disponibles` int(11) DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `transport` (`type`, `compagnie`, `depart`, `arrivee`, `date_depart`, `date_arrivee`, `prix`, `places_disponibles`) VALUES
('Avion', 'Tunisair', 'Tunis', 'Paris', '2026-06-01 08:00:00', '2026-06-01 11:00:00', 450.00, 120),
('Avion', 'Transavia', 'Tunis', 'Barcelone', '2026-06-15 10:00:00', '2026-06-15 12:30:00', 320.00, 80),
('Bus', 'SNTRI', 'Tunis', 'Nabeul', '2026-06-20 07:00:00', '2026-06-20 08:30:00', 15.00, 45),
('Avion', 'Royal Air Maroc', 'Tunis', 'Marrakech', '2026-07-01 09:00:00', '2026-07-01 12:00:00', 380.00, 150),
('Avion', 'Emirates', 'Tunis', 'Dubai', '2026-08-01 22:00:00', '2026-08-02 06:00:00', 850.00, 200);

-- =============================================
-- TABLE: type_transport
-- =============================================
DROP TABLE IF EXISTS `type_transport`;
CREATE TABLE `type_transport` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `nom` varchar(100) NOT NULL,
  `description` text DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `type_transport` (`nom`, `description`) VALUES
('Avion', 'Transport aerien'),
('Bus', 'Transport terrestre par bus'),
('Train', 'Transport ferroviaire'),
('Bateau', 'Transport maritime');

-- =============================================
-- TABLE: vehicule
-- =============================================
DROP TABLE IF EXISTS `vehicule`;
CREATE TABLE `vehicule` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `marque` varchar(100) NOT NULL,
  `modele` varchar(100) DEFAULT NULL,
  `immatriculation` varchar(20) DEFAULT NULL,
  `capacite` int(11) DEFAULT 0,
  `type_id` int(11) DEFAULT NULL,
  `statut` varchar(50) DEFAULT 'disponible',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `vehicule` (`marque`, `modele`, `immatriculation`, `capacite`, `type_id`, `statut`) VALUES
('Airbus', 'A320', 'TS-IMA', 180, 1, 'disponible'),
('Boeing', '737-800', 'TS-ION', 162, 1, 'disponible'),
('Mercedes', 'Tourismo', 'TU-1234', 50, 2, 'disponible');

-- =============================================
-- TABLE: reservation
-- =============================================
DROP TABLE IF EXISTS `reservation`;
CREATE TABLE `reservation` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) DEFAULT NULL,
  `transport_id` int(11) DEFAULT NULL,
  `voyage_id` int(11) DEFAULT NULL,
  `hotel_id` int(11) DEFAULT NULL,
  `date_reservation` datetime DEFAULT current_timestamp(),
  `statut` varchar(50) DEFAULT 'confirmed',
  `nombre_places` int(11) DEFAULT 1,
  `prix_total` decimal(10,2) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `reservation` (`user_id`, `transport_id`, `voyage_id`, `hotel_id`, `statut`, `nombre_places`, `prix_total`) VALUES
(2, 1, 1, 3, 'confirmed', 2, 5000.00),
(3, 4, 3, NULL, 'confirmed', 1, 1200.00);

-- =============================================
-- TABLE: reclamation
-- =============================================
DROP TABLE IF EXISTS `reclamation`;
CREATE TABLE `reclamation` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) DEFAULT NULL,
  `sujet` varchar(255) NOT NULL,
  `message` longtext NOT NULL,
  `statut` varchar(50) DEFAULT 'ouverte',
  `date_creation` datetime DEFAULT current_timestamp(),
  `reponse` longtext DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- TABLE: declaration
-- =============================================
DROP TABLE IF EXISTS `declaration`;
CREATE TABLE `declaration` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) DEFAULT NULL,
  `type` varchar(100) DEFAULT NULL,
  `contenu` longtext DEFAULT NULL,
  `date_creation` datetime DEFAULT current_timestamp(),
  `statut` varchar(50) DEFAULT 'en_attente',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- Symfony tables (keep for compatibility)
-- =============================================
DROP TABLE IF EXISTS `doctrine_migration_versions`;
CREATE TABLE `doctrine_migration_versions` (
  `version` varchar(191) NOT NULL,
  `executed_at` datetime DEFAULT NULL,
  `execution_time` int(11) DEFAULT NULL,
  PRIMARY KEY (`version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS `messenger_messages`;
CREATE TABLE `messenger_messages` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `body` longtext NOT NULL,
  `headers` longtext NOT NULL,
  `queue_name` varchar(190) NOT NULL,
  `created_at` datetime NOT NULL,
  `available_at` datetime NOT NULL,
  `delivered_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `IDX_queue` (`queue_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET FOREIGN_KEY_CHECKS = 1;
COMMIT;
