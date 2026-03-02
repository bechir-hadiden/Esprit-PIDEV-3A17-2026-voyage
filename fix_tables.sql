SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
USE `smarttrip_db`;

-- 1. Create Vols Table
CREATE TABLE IF NOT EXISTS `vols` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `compagnie` VARCHAR(50) NOT NULL,
    `depart` VARCHAR(10) NOT NULL,
    `arrivee` VARCHAR(10) NOT NULL,
    `date_depart` VARCHAR(20) NOT NULL,
    `date_arrivee` VARCHAR(20),
    `heure_depart` VARCHAR(10),
    `heure_arrivee` VARCHAR(10),
    `prix` DOUBLE NOT NULL,
    `devise` VARCHAR(10),
    `escales` INT DEFAULT 0,
    `duree` VARCHAR(50),
    `image_url` VARCHAR(500),
    `date_creation` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_route (depart, arrivee),
    INDEX idx_date (date_depart)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. Create Voyages Table (Plural - for VoyageServices)
CREATE TABLE IF NOT EXISTS `voyages` (
    `id` int(11) NOT NULL AUTO_INCREMENT,
    `destination` varchar(255) NOT NULL,
    `dateDebut` date NOT NULL,
    `dateFin` date NOT NULL,
    `prix` double NOT NULL,
    `imagePath` varchar(255) DEFAULT NULL,
    `description` text DEFAULT NULL,
    `destination_id` int(11) DEFAULT NULL,
    `pays_depart` varchar(50) DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. Create Voyage Table (Singular - for OffreService)
CREATE TABLE IF NOT EXISTS `voyage` (
    `id_voyage` int(11) NOT NULL AUTO_INCREMENT,
    `destination` varchar(255) NOT NULL,
    PRIMARY KEY (`id_voyage`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. Create Offre Table
CREATE TABLE IF NOT EXISTS `offre` (
    `id_offre` int(11) NOT NULL AUTO_INCREMENT,
    `titre` varchar(255) NOT NULL,
    `description` text DEFAULT NULL,
    `taux_remise` int(11) NOT NULL,
    `date_debut` date NOT NULL,
    `date_fin` date NOT NULL,
    `statut` varchar(50) NOT NULL,
    `id_voyage` int(11) DEFAULT NULL,
    `id_hotel` int(11) DEFAULT NULL,
    `id_vol` bigint(20) DEFAULT NULL,
    `id_vehicule` int(11) DEFAULT NULL,
    `category` varchar(50) NOT NULL,
    `is_local_support` tinyint(1) NOT NULL DEFAULT 0,
    `image_url` varchar(255) DEFAULT NULL,
    PRIMARY KEY (`id_offre`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. Create Destination Table
CREATE TABLE IF NOT EXISTS `destination` (
    `id` int(11) NOT NULL AUTO_INCREMENT,
    `nom` varchar(255) NOT NULL,
    `pays` varchar(255) NOT NULL,
    `image_url` varchar(255) DEFAULT NULL,
    `code_iata` varchar(10) DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

COMMIT;
