-- Use the target database
USE `smarttrip_db`;

-- 1. Create Profession Table
CREATE TABLE IF NOT EXISTS `profession` (
  `idProfession` int(11) NOT NULL AUTO_INCREMENT,
  `titre` varchar(255) NOT NULL,
  `description` text DEFAULT NULL,
  PRIMARY KEY (`idProfession`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 2. Update Users Table
-- Adding id_profession and telephone to handle transport-related user data
-- Note: Errors are ignored if columns already exist
ALTER TABLE `users` ADD COLUMN IF NOT EXISTS `id_profession` int(11) DEFAULT 0;
ALTER TABLE `users` ADD COLUMN IF NOT EXISTS `telephone` varchar(20) DEFAULT NULL;

-- 3. Create Transport Type Table
CREATE TABLE IF NOT EXISTS `transport_type` (
  `idType` int(11) NOT NULL AUTO_INCREMENT,
  `nom` varchar(50) NOT NULL,
  `prix_depart` double DEFAULT NULL,
  `image` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`idType`),
  UNIQUE KEY `nom` (`nom`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 4. Create Vehicle Table
CREATE TABLE IF NOT EXISTS `vehicule` (
  `idVehicule` int(11) NOT NULL AUTO_INCREMENT,
  `type` varchar(50) DEFAULT NULL,
  `compagnie` varchar(100) DEFAULT NULL,
  `numero` varchar(50) DEFAULT NULL,
  `capacite` int(11) DEFAULT NULL,
  `prix` double DEFAULT NULL,
  `disponible` tinyint(1) DEFAULT NULL,
  `image` varchar(255) DEFAULT NULL,
  `latitude` double DEFAULT NULL,
  `longitude` double DEFAULT NULL,
  `ville` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`idVehicule`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 5. Create Transport Catalog (renamed from transport)
-- Renamed to avoid name conflict with the existing 'transport' table in smarttrip_db
CREATE TABLE IF NOT EXISTS `transport_catalog` (
  `idTransport` int(11) NOT NULL AUTO_INCREMENT,
  `type` varchar(100) NOT NULL,
  `compagnie` varchar(150) NOT NULL,
  `numero` varchar(50) NOT NULL,
  `capacite` int(11) NOT NULL,
  `imageUrl` varchar(500) DEFAULT NULL,
  `description` text DEFAULT NULL,
  `prix` decimal(10,2) DEFAULT NULL,
  PRIMARY KEY (`idTransport`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 6. Create Transport Reservation Table (renamed from reservation)
-- Pointed to the shared 'users' table (using 'id' as FK)
CREATE TABLE IF NOT EXISTS `transport_reservation` (
  `idReservation` int(11) NOT NULL AUTO_INCREMENT,
  `idUser` int(11) NOT NULL,
  `idTransport` int(11) DEFAULT NULL,
  `typeTransport` varchar(50) DEFAULT NULL,
  `idVehicule` int(11) DEFAULT NULL,
  `dateReservation` datetime DEFAULT current_timestamp(),
  `statut` varchar(20) DEFAULT 'CONFIRMED',
  PRIMARY KEY (`idReservation`),
  KEY `idUser` (`idUser`),
  CONSTRAINT `transport_reservation_ibfk_1` FOREIGN KEY (`idUser`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 7. Create Notification Table
CREATE TABLE IF NOT EXISTS `notification` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) DEFAULT NULL,
  `reservation_id` int(11) DEFAULT NULL,
  `message` text DEFAULT NULL,
  `date_sent` datetime DEFAULT current_timestamp(),
  `is_read` tinyint(1) DEFAULT 0,
  `type` varchar(50) DEFAULT 'CANCELLATION',
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `notification_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
