-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Mar 01, 2026 at 12:01 PM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.1.25

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `smarttrip_dp`
--

-- --------------------------------------------------------

--
-- Table structure for table `bookings`
--

CREATE TABLE `bookings` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `hotel_id` int(11) NOT NULL,
  `room_type_id` int(11) DEFAULT NULL,
  `check_in_date` date NOT NULL,
  `check_out_date` date NOT NULL,
  `guests` int(11) NOT NULL,
  `total_price` decimal(10,2) NOT NULL,
  `status` enum('PENDING','CONFIRMED','CANCELLED') DEFAULT 'PENDING',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `code_promo`
--

CREATE TABLE `code_promo` (
  `id_code` int(11) NOT NULL,
  `code_texte` varchar(20) NOT NULL,
  `date_expiration` date NOT NULL,
  `id_offre` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `code_promo`
--

INSERT INTO `code_promo` (`id_code`, `code_texte`, `date_expiration`, `id_offre`) VALUES
(6, 'SMART-CXXGZ4B4', '2026-02-26', 5),
(8, 'SMART-NDXCP9GK', '2026-03-10', 3),
(9, 'SMART-WMPIZY7U', '2026-03-27', 3),
(10, 'SMART-4DR91E98', '2026-03-27', 3);

-- --------------------------------------------------------

--
-- Table structure for table `hotels`
--

CREATE TABLE `hotels` (
  `id` int(11) NOT NULL,
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
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `hotels`
--

INSERT INTO `hotels` (`id`, `name`, `location`, `city`, `country`, `description`, `price_per_night`, `price_per_week`, `rating`, `review_count`, `check_in_time`, `check_out_time`, `contact_email`, `contact_phone`, `created_at`, `updated_at`) VALUES
(1, 'The Royal Plaza', 'Central Park West', 'New York', 'United States', 'Experience unparalleled luxury at The Royal Plaza, where timeless elegance meets modern sophistication. Located steps from Central Park, our five-star hotel offers breathtaking views, world-class dining, and impeccable service.', 450.00, 2800.00, 4.8, 1247, '15:00', '11:00', 'reservations@royalplaza.com', '+1 (212) 555-0100', '2026-02-14 19:50:55', '2026-02-14 19:50:55'),
(2, 'Oceanview Resort & Spa', 'Malibu Beach', 'Malibu', 'United States', 'Nestled along the pristine shores of Malibu, Oceanview Resort & Spa offers a sanctuary of tranquility. Indulge in award-winning spa treatments, savor coastal cuisine, and wake up to the sound of waves.', 680.00, 4200.00, 4.9, 892, '16:00', '12:00', 'stay@oceanviewresort.com', '+1 (310) 555-0200', '2026-02-14 19:50:55', '2026-02-14 19:50:55'),
(3, 'Grand Hotel Paris', 'Champs-Élysées', 'Paris', 'France', 'A Parisian icon on the world\'s most beautiful avenue. Grand Hotel Paris combines Belle Époque grandeur with contemporary luxury, offering an unforgettable stay in the heart of the City of Light.', 520.00, 3200.00, 4.7, 2156, '15:00', '11:00', 'bonjour@grandhotelparis.fr', '+33 1 55 55 01 00', '2026-02-14 19:50:55', '2026-02-14 19:50:55'),
(4, 'Tokyo Skyline Hotel', 'Shibuya District', 'Tokyo', 'Japan', 'Rising above the vibrant Shibuya crossing, Tokyo Skyline Hotel offers a perfect blend of Japanese hospitality and modern design. Experience Tokyo\'s energy from your sky-high sanctuary.', 380.00, 2400.00, 4.6, 1534, '15:00', '11:00', 'info@tokyoskyline.jp', '+81 3-5555-0100', '2026-02-14 19:50:55', '2026-02-14 19:50:55'),
(5, 'Dubai Luxury Palace', 'Palm Jumeirah', 'Dubai', 'United Arab Emirates', 'Experience the pinnacle of Arabian luxury at Dubai Luxury Palace. From private beach access to world-class dining, every moment is crafted for extraordinary experiences.', 890.00, 5500.00, 4.9, 756, '15:00', '12:00', 'reservations@dubailuxurypalace.ae', '+971 4 555 0100', '2026-02-14 19:50:55', '2026-02-14 19:50:55');

-- --------------------------------------------------------

--
-- Table structure for table `hotel_amenities`
--

CREATE TABLE `hotel_amenities` (
  `id` int(11) NOT NULL,
  `hotel_id` int(11) NOT NULL,
  `amenity_name` varchar(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `hotel_amenities`
--

INSERT INTO `hotel_amenities` (`id`, `hotel_id`, `amenity_name`) VALUES
(1, 1, 'Free WiFi'),
(2, 1, 'Spa & Wellness'),
(3, 1, 'Fitness Center'),
(4, 1, 'Rooftop Pool'),
(5, 1, 'Concierge'),
(6, 1, 'Valet Parking'),
(7, 1, 'Restaurant'),
(8, 1, 'Bar'),
(9, 1, 'Room Service'),
(10, 1, 'Business Center'),
(11, 2, 'Private Beach'),
(12, 2, 'Infinity Pool'),
(13, 2, 'Full-Service Spa'),
(14, 2, 'Yoga Studio'),
(15, 2, 'Beachfront Dining'),
(16, 2, 'Water Sports'),
(17, 2, 'Concierge'),
(18, 2, 'Free WiFi'),
(19, 2, 'Valet Parking'),
(20, 2, 'EV Charging'),
(21, 3, 'Michelin Restaurant'),
(22, 3, 'Rooftop Terrace'),
(23, 3, 'Spa'),
(24, 3, 'Fitness Center'),
(25, 3, 'Concierge'),
(26, 3, 'Limousine Service'),
(27, 3, 'Business Center'),
(28, 3, 'Free WiFi'),
(29, 3, 'Valet Parking'),
(30, 3, 'Champagne Bar'),
(31, 4, 'Rooftop Bar'),
(32, 4, 'Onsen-style Spa'),
(33, 4, 'Sushi Restaurant'),
(34, 4, 'Fitness Center'),
(35, 4, 'Concierge'),
(36, 4, 'Free WiFi'),
(37, 4, 'Business Center'),
(38, 4, 'Laundry Service'),
(39, 4, 'Currency Exchange'),
(40, 4, 'Airport Shuttle'),
(41, 5, 'Private Beach'),
(42, 5, 'Water Park'),
(43, 5, 'Spa'),
(44, 5, 'Helipad'),
(45, 5, 'Michelin Dining'),
(46, 5, 'Butler Service'),
(47, 5, 'Infinity Pools'),
(48, 5, 'Yacht Charter'),
(49, 5, 'Free WiFi'),
(50, 5, 'Luxury Shopping');

-- --------------------------------------------------------

--
-- Table structure for table `hotel_images`
--

CREATE TABLE `hotel_images` (
  `id` int(11) NOT NULL,
  `hotel_id` int(11) NOT NULL,
  `image_url` varchar(500) NOT NULL,
  `display_order` int(11) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `hotel_images`
--

INSERT INTO `hotel_images` (`id`, `hotel_id`, `image_url`, `display_order`) VALUES
(1, 1, 'https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800&h=600&fit=crop', 1),
(2, 1, 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=800&h=600&fit=crop', 2),
(3, 1, 'https://images.unsplash.com/photo-1590490360182-c33d57733427?w=800&h=600&fit=crop', 3),
(4, 2, 'https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?w=800&h=600&fit=crop', 1),
(5, 2, 'https://images.unsplash.com/photo-1571896349842-33c89424de2d?w=800&h=600&fit=crop', 2),
(6, 2, 'https://images.unsplash.com/photo-1584132967334-10e028bd69f7?w=800&h=600&fit=crop', 3),
(7, 3, 'https://images.unsplash.com/photo-1564501049412-61c2a3083791?w=800&h=600&fit=crop', 1),
(8, 3, 'https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?w=800&h=600&fit=crop', 2),
(9, 3, 'https://images.unsplash.com/photo-1445019980597-93fa8acb246c?w=800&h=600&fit=crop', 3),
(10, 4, 'https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?w=800&h=600&fit=crop', 1),
(11, 4, 'https://images.unsplash.com/photo-1555854877-bab0e564b8d5?w=800&h=600&fit=crop', 2),
(12, 4, 'https://images.unsplash.com/photo-1590073844006-33379778ae09?w=800&h=600&fit=crop', 3),
(13, 5, 'https://images.unsplash.com/photo-1578683010236-d716f9a3f461?w=800&h=600&fit=crop', 1),
(14, 5, 'https://images.unsplash.com/photo-1564501049412-61c2a3083791?w=800&h=600&fit=crop', 2),
(15, 5, 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=800&h=600&fit=crop', 3);

-- --------------------------------------------------------

--
-- Table structure for table `hotel_policies`
--

CREATE TABLE `hotel_policies` (
  `id` int(11) NOT NULL,
  `hotel_id` int(11) NOT NULL,
  `policy_text` varchar(500) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `hotel_policies`
--

INSERT INTO `hotel_policies` (`id`, `hotel_id`, `policy_text`) VALUES
(1, 1, 'No smoking'),
(2, 1, 'No pets'),
(3, 1, 'Check-in from 3 PM'),
(4, 1, 'Check-out by 11 AM'),
(5, 2, 'No smoking'),
(6, 2, 'Pet-friendly'),
(7, 2, 'Check-in from 4 PM'),
(8, 2, 'Check-out by 12 PM'),
(9, 3, 'No smoking'),
(10, 3, 'No pets'),
(11, 3, 'Check-in from 3 PM'),
(12, 3, 'Check-out by 11 AM'),
(13, 4, 'No smoking'),
(14, 4, 'No pets'),
(15, 4, 'Quiet hours 10 PM - 7 AM'),
(16, 5, 'No smoking'),
(17, 5, 'Dress code applies'),
(18, 5, 'Advance booking for restaurants');

-- --------------------------------------------------------

--
-- Table structure for table `notification`
--

CREATE TABLE `notification` (
  `id` int(11) NOT NULL,
  `user_id` int(11) DEFAULT NULL,
  `reservation_id` int(11) DEFAULT NULL,
  `message` text DEFAULT NULL,
  `date_sent` datetime DEFAULT current_timestamp(),
  `is_read` tinyint(1) DEFAULT 0,
  `type` varchar(50) DEFAULT 'CANCELLATION'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `notification`
--

INSERT INTO `notification` (`id`, `user_id`, `reservation_id`, `message`, `date_sent`, `is_read`, `type`) VALUES
(5, 14, 9, 'Réservation #9 (Voiture) annulée par maram', '2026-03-01 08:46:37', 0, 'CANCELLATION'),
(6, 14, 7, 'Réservation #7 (Taxi) annulée par maram', '2026-03-01 08:46:41', 0, 'CANCELLATION'),
(7, 14, 10, 'Réservation #10 (Voiture) annulée par maram', '2026-03-01 09:31:37', 0, 'CANCELLATION');

-- --------------------------------------------------------

--
-- Table structure for table `offre`
--

CREATE TABLE `offre` (
  `id_offre` int(11) NOT NULL,
  `titre` varchar(100) NOT NULL,
  `description` text DEFAULT NULL,
  `taux_remise` int(11) NOT NULL,
  `date_debut` date NOT NULL,
  `date_fin` date NOT NULL,
  `statut` enum('ACTIVE','EXPIREE') DEFAULT 'ACTIVE',
  `id_voyage` int(11) NOT NULL,
  `is_local_support` tinyint(1) DEFAULT 0,
  `image_url` varchar(255) DEFAULT 'default.jpg'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `offre`
--

INSERT INTO `offre` (`id_offre`, `titre`, `description`, `taux_remise`, `date_debut`, `date_fin`, `statut`, `id_voyage`, `is_local_support`, `image_url`) VALUES
(3, 'Promo Été', 'Réduction sur vols Rome', 20, '2026-06-01', '2026-08-31', 'ACTIVE', 1, 0, 'paris.jpg'),
(5, 'Promo d\'hiver', 'go ahead dont hesitate', 40, '2026-02-09', '2026-02-18', 'ACTIVE', 2, 1, 'rome.jpg');

-- --------------------------------------------------------

--
-- Table structure for table `plans`
--

CREATE TABLE `plans` (
  `id` int(11) NOT NULL,
  `name` varchar(200) NOT NULL,
  `description` text DEFAULT NULL,
  `price` decimal(10,2) NOT NULL,
  `duration_type` varchar(20) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `profession`
--

CREATE TABLE `profession` (
  `idProfession` int(11) NOT NULL,
  `titre` varchar(255) NOT NULL,
  `description` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `room_amenities`
--

CREATE TABLE `room_amenities` (
  `id` int(11) NOT NULL,
  `room_type_id` int(11) NOT NULL,
  `amenity_name` varchar(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `room_amenities`
--

INSERT INTO `room_amenities` (`id`, `room_type_id`, `amenity_name`) VALUES
(13, 3, 'Ocean View'),
(14, 3, 'Private Terrace'),
(15, 3, 'Outdoor Shower'),
(16, 3, 'Mini Bar'),
(17, 4, 'Panoramic View'),
(18, 4, 'King Bed'),
(19, 4, 'Spa Bath'),
(20, 4, 'Lounge Area'),
(21, 5, 'Queen Bed'),
(22, 5, 'City View'),
(23, 5, 'Marble Bathroom'),
(24, 5, 'Mini Bar'),
(25, 6, 'King Bed'),
(26, 6, 'Eiffel Tower View'),
(27, 6, 'Balcony'),
(28, 6, 'Butler Service'),
(29, 7, 'Queen Bed'),
(30, 7, 'City View'),
(31, 7, 'Smart TV'),
(32, 7, 'Rain Shower'),
(33, 8, 'King Bed'),
(34, 8, 'Tatami Area'),
(35, 8, 'City View'),
(36, 8, 'Deep Soaking Tub');

-- --------------------------------------------------------

--
-- Table structure for table `room_images`
--

CREATE TABLE `room_images` (
  `id` int(11) NOT NULL,
  `room_type_id` int(11) NOT NULL,
  `image_url` varchar(500) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `room_images`
--

INSERT INTO `room_images` (`id`, `room_type_id`, `image_url`) VALUES
(4, 3, 'https://images.unsplash.com/photo-1499793983690-e29da59ef1c2?w=600&h=400&fit=crop'),
(5, 4, 'https://images.unsplash.com/photo-1613395877344-13d4c79e4284?w=600&h=400&fit=crop'),
(6, 5, 'https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?w=600&h=400&fit=crop'),
(7, 6, 'https://images.unsplash.com/photo-1502602898657-3e91760cbb34?w=600&h=400&fit=crop'),
(8, 7, 'https://images.unsplash.com/photo-1590490360182-c33d57733427?w=600&h=400&fit=crop'),
(9, 8, 'https://images.unsplash.com/photo-1522771753035-a0a1f66cd459?w=600&h=400&fit=crop');

-- --------------------------------------------------------

--
-- Table structure for table `room_types`
--

CREATE TABLE `room_types` (
  `id` int(11) NOT NULL,
  `hotel_id` int(11) NOT NULL,
  `name` varchar(100) NOT NULL,
  `description` text DEFAULT NULL,
  `max_occupancy` int(11) NOT NULL DEFAULT 2,
  `price_per_night` decimal(10,2) NOT NULL,
  `is_available` tinyint(1) DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `room_types`
--

INSERT INTO `room_types` (`id`, `hotel_id`, `name`, `description`, `max_occupancy`, `price_per_night`, `is_available`) VALUES
(1, 1, 'Deluxe King Room', 'Spacious room with king bed and city views', 2, 450.00, 1),
(2, 1, 'Executive Suite', 'Luxury suite with separate living area and park views', 3, 750.00, 1),
(3, 1, 'Presidential Suite', 'Ultimate luxury with panoramic views and butler service', 4, 2500.00, 0),
(4, 2, 'Oceanfront Villa', 'Direct beach access with private terrace', 2, 680.00, 1),
(5, 2, 'Sunset Suite', 'Panoramic ocean views with floor-to-ceiling windows', 3, 950.00, 1),
(6, 3, 'Classic Room', 'Elegant room with Parisian décor', 2, 520.00, 1),
(7, 3, 'Eiffel Suite', 'Suite with direct Eiffel Tower views', 2, 1200.00, 1),
(8, 4, 'Sky Room', 'Modern room with city skyline views', 2, 380.00, 1),
(9, 4, 'Traditional Suite', 'Japanese-inspired suite with tatami area', 3, 650.00, 1),
(10, 5, 'Palace Room', 'Luxurious room with Arabian design elements', 2, 890.00, 1),
(11, 5, 'Royal Suite', 'Opulent suite with private pool', 4, 3500.00, 1);

-- --------------------------------------------------------

--
-- Table structure for table `transport`
--

CREATE TABLE `transport` (
  `id` int(11) NOT NULL,
  `name` varchar(200) NOT NULL,
  `type` enum('FLIGHT','TRAIN','BUS','CAR_RENTAL') NOT NULL,
  `origin` varchar(100) NOT NULL,
  `destination` varchar(100) NOT NULL,
  `price` decimal(10,2) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `transport_catalog`
--

CREATE TABLE `transport_catalog` (
  `idTransport` int(11) NOT NULL,
  `type` varchar(100) NOT NULL,
  `compagnie` varchar(150) NOT NULL,
  `numero` varchar(50) NOT NULL,
  `capacite` int(11) NOT NULL,
  `imageUrl` varchar(500) DEFAULT NULL,
  `description` text DEFAULT NULL,
  `prix` decimal(10,2) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `transport_catalog`
--

INSERT INTO `transport_catalog` (`idTransport`, `type`, `compagnie`, `numero`, `capacite`, `imageUrl`, `description`, `prix`) VALUES
(1, 'Bus', 'CityTransport', 'B45', 50, 'https://via.placeholder.com/300x180', 'Vehicule confortable', 25.00),
(2, 'Train', 'RailCorp', 'T12', 300, NULL, NULL, NULL),
(3, 'Plane', 'AirWays', 'A320', 180, NULL, NULL, NULL),
(6, 'scooter', 'scooter', '23456789', 100, NULL, NULL, 0.00),
(7, 'TAXI', 'citytaxi', '1234', 4, 'https://via.placeholder.com/300x180', 'Service rapide', 15.00);

-- --------------------------------------------------------

--
-- Table structure for table `transport_reservation`
--

CREATE TABLE `transport_reservation` (
  `idReservation` int(11) NOT NULL,
  `idUser` int(11) NOT NULL,
  `idTransport` int(11) DEFAULT NULL,
  `typeTransport` varchar(50) DEFAULT NULL,
  `idVehicule` int(11) DEFAULT NULL,
  `dateReservation` datetime DEFAULT current_timestamp(),
  `statut` varchar(20) DEFAULT 'CONFIRMED'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `transport_reservation`
--

INSERT INTO `transport_reservation` (`idReservation`, `idUser`, `idTransport`, `typeTransport`, `idVehicule`, `dateReservation`, `statut`) VALUES
(1, 13, NULL, 'Bus', 1, '2026-03-01 06:35:45', 'CONFIRMED'),
(6, 13, NULL, 'Bus', 2, '2026-03-01 08:43:05', 'CONFIRMED'),
(8, 14, NULL, 'Taxi', 17, '2026-03-01 08:46:13', 'CONFIRMED'),
(11, 14, NULL, 'Bus', 6, '2026-01-01 09:14:52', 'CONFIRMED');

-- --------------------------------------------------------

--
-- Table structure for table `transport_type`
--

CREATE TABLE `transport_type` (
  `idType` int(11) NOT NULL,
  `nom` varchar(50) NOT NULL,
  `prix_depart` double DEFAULT NULL,
  `image` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `transport_type`
--

INSERT INTO `transport_type` (`idType`, `nom`, `prix_depart`, `image`) VALUES
(5, 'Bus', 19, 'bus.png'),
(6, 'Taxi', 45, 'taxi.png'),
(7, 'Voiture', 190, 'voiture.png'),
(8, 'Scooter', 30, 'type_1772276097255.jpg');

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `id` int(11) NOT NULL,
  `username` varchar(50) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `full_name` varchar(100) NOT NULL,
  `email` varchar(100) NOT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `avatar` varchar(500) DEFAULT NULL,
  `role` enum('ADMIN','CLIENT') NOT NULL DEFAULT 'CLIENT',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `id_profession` int(11) DEFAULT 0,
  `telephone` varchar(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`id`, `username`, `password_hash`, `full_name`, `email`, `phone`, `avatar`, `role`, `created_at`, `updated_at`, `id_profession`, `telephone`) VALUES
(4, 'admin1', 'admin1', 'Administrator 2', 'admin1@smarttrip.com', '+1 (555) 000-0001', 'https://ui-avatars.com/api/?name=Admin1&background=random', 'ADMIN', '2026-02-14 19:56:17', '2026-02-14 19:56:17', 0, NULL),
(5, 'HamdiDridi', '$2a$10$6vnzzpWhGYrTZTqTFYO7K.KOimPxQ7WEuhdkLhHEh76htgXBT/TkW', 'Hamdi Dridi', 'hamdi.dridi.dev@gmail.com', NULL, 'https://ui-avatars.com/api/?name=Hamdi+Dridi&background=2563EB&color=fff', 'CLIENT', '2026-02-14 20:08:54', '2026-02-23 08:11:57', 0, NULL),
(6, 'AYAdridi', '$2a$10$74iUyMI3gymHOVBah.zclOpuFoKtRlbQUXcF1cRr2agV4phw8axKe', 'AYAdridi', 'AYAdridi@gmail.com', '456987123', 'https://ui-avatars.com/api/?name=AYAdridi&background=2563EB&color=fff', 'CLIENT', '2026-02-16 10:53:47', '2026-02-16 10:54:26', 0, NULL),
(7, 'bechir.hadiden', '$2a$10$h/wqtl21MDhFCyTXsRKnC.jrGHXUHdKXMF.iNf65uzJ0yXQ8t70OS', 'bechir hadiden', 'bechir.hadiden@gmail.com', NULL, 'https://ui-avatars.com/api/?name=bechir+hadiden&background=2563EB&color=fff', 'CLIENT', '2026-02-23 08:49:16', '2026-02-23 08:49:16', 0, NULL),
(8, 'marambousteni37', '$2a$10$s4lOdZreMS3boKTQaCk8beMG2DQILu8Ived1l495JWrA0798SaA2O', 'maram', 'marambousteni37@gmail.com', NULL, 'https://ui-avatars.com/api/?name=maram&background=2563EB&color=fff', 'CLIENT', '2026-03-01 04:53:40', '2026-03-01 04:53:40', 0, NULL),
(9, 'admin', 'admin123', 'admin', '', NULL, NULL, 'ADMIN', '2026-03-01 05:03:46', '2026-03-01 05:03:46', 0, NULL),
(13, 'louay', '$2a$10$JAaPLA4a6Uenc00tUjh6Sexqbaz/rjqVEJ3yFZ.HtjKF.j2Jpno.u', 'louay', 'louay@gmail.com', NULL, 'https://ui-avatars.com/api/?name=louay&background=2563EB&color=fff', 'CLIENT', '2026-03-01 05:20:48', '2026-03-01 05:20:48', 0, NULL),
(14, 'maram', '$2a$10$xzYRTbEuhZHCJ045Iy5QBOcHneYEXZATIrqv6soXsf6xxfEhJcfH.', 'maram', 'maram@gmail.com', NULL, 'https://ui-avatars.com/api/?name=maram&background=2563EB&color=fff', 'CLIENT', '2026-03-01 07:45:48', '2026-03-01 07:45:48', 0, NULL);

-- --------------------------------------------------------

--
-- Table structure for table `vehicule`
--

CREATE TABLE `vehicule` (
  `idVehicule` int(11) NOT NULL,
  `type` varchar(50) DEFAULT NULL,
  `compagnie` varchar(100) DEFAULT NULL,
  `numero` varchar(50) DEFAULT NULL,
  `capacite` int(11) DEFAULT NULL,
  `prix` double DEFAULT NULL,
  `disponible` tinyint(1) DEFAULT NULL,
  `image` varchar(255) DEFAULT NULL,
  `latitude` double DEFAULT NULL,
  `longitude` double DEFAULT NULL,
  `ville` varchar(100) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `vehicule`
--

INSERT INTO `vehicule` (`idVehicule`, `type`, `compagnie`, `numero`, `capacite`, `prix`, `disponible`, `image`, `latitude`, `longitude`, `ville`) VALUES
(1, 'Bus', 'SNTRI', '2345 SNT 71', 70, 15.5, 0, 'bus.png', 0, 0, 'Tunis'),
(2, 'Bus', 'SRT Monastir', '124 MON 71', 45, 4.2, 0, 'bus.png', 0, 0, 'Monastir'),
(3, 'Taxi', 'Taxi Individuel', 'T-TUN 9988', 4, 12, 1, 'taxi.png', NULL, NULL, 'Tunis'),
(4, 'Voiture', 'Avis Rent-a-car', 'VW Golf 8', 5, 125, 0, 'voiture.png', 0, 0, 'Aryanah'),
(5, 'Bus', 'SNTRI', 'BUS-101', 50, 15, 1, 'bus.png', 0, 0, 'Tunis'),
(6, 'Bus', 'SNTRI', 'BUS-102', 50, 16, 0, 'bus.png', 0, 0, 'Sousse'),
(8, 'Taxi', 'Allo Taxi', 'TAX-201', 4, 10, 1, 'taxi.png', NULL, NULL, 'Tunis'),
(9, 'Taxi', 'Allo Taxi', 'TAX-202', 4, 10, 0, 'taxi.png', 0, 0, 'Aryanah'),
(10, 'Taxi', 'Allo Taxi', 'TAX-203', 4, 10, 1, 'taxi.png', NULL, NULL, 'Ben Arous'),
(12, 'Voiture', 'Sixt', 'VOI-302', 5, 80, 1, 'voiture.png', 0, 0, 'aryanah'),
(13, 'Voiture', 'Hertz', 'VOI-303', 5, 90, 1, 'voiture.png', NULL, NULL, 'Monastir'),
(14, 'Scooter', 'Bolt', 'SCO-401', 1, 5, 1, 'veh_1772345595173.jpg', 0, 0, 'Aryanah'),
(15, 'Scooter', 'Bolt', 'SCO-402', 1, 5, 1, 'veh_1772345611953.jpg', 0, 0, 'La Marsa'),
(16, 'Bus', 'Express', 'BUS-104', 45, 12, 1, 'bus.png', 0, 0, 'Aryanah'),
(17, 'Taxi', 'Taxi Tour', 'TAX-204', 4, 12, 0, 'taxi.png', NULL, NULL, 'Sousse'),
(18, 'Train', 'Train', '1009', 200, 14, 1, 'veh_1772326582217.jpg', 36.8065, 10.1815, 'tunis'),
(19, 'Train', 'Train', '2300', 300, 10, 1, 'veh_1772326616003.jpg', 36.8065, 10.1815, 'Sousse'),
(20, 'Voiture', 'Tesla', '123', 5, 900, 1, NULL, 36.8065, 10.1815, 'tunis'),
(21, 'Voiture', 'PEUGEOT', '12345', 5, 120, 1, NULL, 36.8065, 10.1815, '');

-- --------------------------------------------------------

--
-- Table structure for table `vols`
--

CREATE TABLE `vols` (
  `id` bigint(20) NOT NULL,
  `compagnie` varchar(50) NOT NULL,
  `depart` varchar(10) NOT NULL,
  `arrivee` varchar(10) NOT NULL,
  `date_depart` varchar(20) NOT NULL,
  `date_arrivee` varchar(20) DEFAULT NULL,
  `heure_depart` varchar(10) DEFAULT NULL,
  `heure_arrivee` varchar(10) DEFAULT NULL,
  `prix` double NOT NULL,
  `devise` varchar(10) DEFAULT NULL,
  `escales` int(11) DEFAULT 0,
  `duree` varchar(50) DEFAULT NULL,
  `date_creation` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `voyage`
--

CREATE TABLE `voyage` (
  `id_voyage` int(11) NOT NULL,
  `destination` varchar(100) NOT NULL,
  `date_depart` date DEFAULT NULL,
  `date_retour` date DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `voyage`
--

INSERT INTO `voyage` (`id_voyage`, `destination`, `date_depart`, `date_retour`) VALUES
(1, 'Paris', '2026-02-04', '2026-03-05'),
(2, 'Rome', '2026-07-10', '2026-07-20');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `bookings`
--
ALTER TABLE `bookings`
  ADD PRIMARY KEY (`id`),
  ADD KEY `room_type_id` (`room_type_id`),
  ADD KEY `idx_user` (`user_id`),
  ADD KEY `idx_hotel` (`hotel_id`),
  ADD KEY `idx_status` (`status`);

--
-- Indexes for table `code_promo`
--
ALTER TABLE `code_promo`
  ADD PRIMARY KEY (`id_code`),
  ADD UNIQUE KEY `code_texte` (`code_texte`),
  ADD KEY `fk_code_offre` (`id_offre`);

--
-- Indexes for table `hotels`
--
ALTER TABLE `hotels`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_city` (`city`),
  ADD KEY `idx_country` (`country`),
  ADD KEY `idx_rating` (`rating`);
ALTER TABLE `hotels` ADD FULLTEXT KEY `idx_search` (`name`,`city`,`country`,`location`);

--
-- Indexes for table `hotel_amenities`
--
ALTER TABLE `hotel_amenities`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_hotel` (`hotel_id`);

--
-- Indexes for table `hotel_images`
--
ALTER TABLE `hotel_images`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_hotel` (`hotel_id`);

--
-- Indexes for table `hotel_policies`
--
ALTER TABLE `hotel_policies`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_hotel` (`hotel_id`);

--
-- Indexes for table `notification`
--
ALTER TABLE `notification`
  ADD PRIMARY KEY (`id`),
  ADD KEY `user_id` (`user_id`);

--
-- Indexes for table `offre`
--
ALTER TABLE `offre`
  ADD PRIMARY KEY (`id_offre`),
  ADD KEY `fk_offre_voyage` (`id_voyage`);

--
-- Indexes for table `plans`
--
ALTER TABLE `plans`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `profession`
--
ALTER TABLE `profession`
  ADD PRIMARY KEY (`idProfession`);

--
-- Indexes for table `room_amenities`
--
ALTER TABLE `room_amenities`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_room_type` (`room_type_id`);

--
-- Indexes for table `room_images`
--
ALTER TABLE `room_images`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_room_type` (`room_type_id`);

--
-- Indexes for table `room_types`
--
ALTER TABLE `room_types`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_hotel` (`hotel_id`);

--
-- Indexes for table `transport`
--
ALTER TABLE `transport`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `transport_catalog`
--
ALTER TABLE `transport_catalog`
  ADD PRIMARY KEY (`idTransport`);

--
-- Indexes for table `transport_reservation`
--
ALTER TABLE `transport_reservation`
  ADD PRIMARY KEY (`idReservation`),
  ADD KEY `idUser` (`idUser`);

--
-- Indexes for table `transport_type`
--
ALTER TABLE `transport_type`
  ADD PRIMARY KEY (`idType`),
  ADD UNIQUE KEY `nom` (`nom`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `username` (`username`),
  ADD UNIQUE KEY `email` (`email`),
  ADD KEY `idx_username` (`username`),
  ADD KEY `idx_email` (`email`),
  ADD KEY `idx_role` (`role`);

--
-- Indexes for table `vehicule`
--
ALTER TABLE `vehicule`
  ADD PRIMARY KEY (`idVehicule`);

--
-- Indexes for table `vols`
--
ALTER TABLE `vols`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_route` (`depart`,`arrivee`),
  ADD KEY `idx_date` (`date_depart`);

--
-- Indexes for table `voyage`
--
ALTER TABLE `voyage`
  ADD PRIMARY KEY (`id_voyage`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `bookings`
--
ALTER TABLE `bookings`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `code_promo`
--
ALTER TABLE `code_promo`
  MODIFY `id_code` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT for table `hotels`
--
ALTER TABLE `hotels`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- AUTO_INCREMENT for table `hotel_amenities`
--
ALTER TABLE `hotel_amenities`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=62;

--
-- AUTO_INCREMENT for table `hotel_images`
--
ALTER TABLE `hotel_images`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=16;

--
-- AUTO_INCREMENT for table `hotel_policies`
--
ALTER TABLE `hotel_policies`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=19;

--
-- AUTO_INCREMENT for table `notification`
--
ALTER TABLE `notification`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- AUTO_INCREMENT for table `offre`
--
ALTER TABLE `offre`
  MODIFY `id_offre` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT for table `plans`
--
ALTER TABLE `plans`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `profession`
--
ALTER TABLE `profession`
  MODIFY `idProfession` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `room_amenities`
--
ALTER TABLE `room_amenities`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=37;

--
-- AUTO_INCREMENT for table `room_images`
--
ALTER TABLE `room_images`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=10;

--
-- AUTO_INCREMENT for table `room_types`
--
ALTER TABLE `room_types`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=13;

--
-- AUTO_INCREMENT for table `transport`
--
ALTER TABLE `transport`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `transport_catalog`
--
ALTER TABLE `transport_catalog`
  MODIFY `idTransport` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- AUTO_INCREMENT for table `transport_reservation`
--
ALTER TABLE `transport_reservation`
  MODIFY `idReservation` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=12;

--
-- AUTO_INCREMENT for table `transport_type`
--
ALTER TABLE `transport_type`
  MODIFY `idType` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=12;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=15;

--
-- AUTO_INCREMENT for table `vehicule`
--
ALTER TABLE `vehicule`
  MODIFY `idVehicule` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=22;

--
-- AUTO_INCREMENT for table `vols`
--
ALTER TABLE `vols`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `voyage`
--
ALTER TABLE `voyage`
  MODIFY `id_voyage` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `bookings`
--
ALTER TABLE `bookings`
  ADD CONSTRAINT `bookings_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `bookings_ibfk_2` FOREIGN KEY (`hotel_id`) REFERENCES `hotels` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `bookings_ibfk_3` FOREIGN KEY (`room_type_id`) REFERENCES `room_types` (`id`) ON DELETE SET NULL;

--
-- Constraints for table `code_promo`
--
ALTER TABLE `code_promo`
  ADD CONSTRAINT `fk_code_offre` FOREIGN KEY (`id_offre`) REFERENCES `offre` (`id_offre`) ON DELETE CASCADE;

--
-- Constraints for table `hotel_amenities`
--
ALTER TABLE `hotel_amenities`
  ADD CONSTRAINT `hotel_amenities_ibfk_1` FOREIGN KEY (`hotel_id`) REFERENCES `hotels` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `hotel_images`
--
ALTER TABLE `hotel_images`
  ADD CONSTRAINT `hotel_images_ibfk_1` FOREIGN KEY (`hotel_id`) REFERENCES `hotels` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `hotel_policies`
--
ALTER TABLE `hotel_policies`
  ADD CONSTRAINT `hotel_policies_ibfk_1` FOREIGN KEY (`hotel_id`) REFERENCES `hotels` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `notification`
--
ALTER TABLE `notification`
  ADD CONSTRAINT `notification_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `room_amenities`
--
ALTER TABLE `room_amenities`
  ADD CONSTRAINT `room_amenities_ibfk_1` FOREIGN KEY (`room_type_id`) REFERENCES `room_types` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `room_images`
--
ALTER TABLE `room_images`
  ADD CONSTRAINT `room_images_ibfk_1` FOREIGN KEY (`room_type_id`) REFERENCES `room_types` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `room_types`
--
ALTER TABLE `room_types`
  ADD CONSTRAINT `room_types_ibfk_1` FOREIGN KEY (`hotel_id`) REFERENCES `hotels` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `transport_reservation`
--
ALTER TABLE `transport_reservation`
  ADD CONSTRAINT `transport_reservation_ibfk_1` FOREIGN KEY (`idUser`) REFERENCES `users` (`id`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
