-- RideEase SQL Relational Schema Initialization (MySQL 8.0+)
-- Auto-generated and aligned with Spring Data JPA & Hibernate Entities

CREATE DATABASE IF NOT EXISTS `rideease_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `rideease_db`;

-- 1. Users Table
CREATE TABLE IF NOT EXISTS `users` (
    `id` VARCHAR(64) NOT NULL,
    `name` VARCHAR(255) NULL,
    `email` VARCHAR(255) NOT NULL UNIQUE,
    `phone` VARCHAR(255) NOT NULL UNIQUE,
    `password` VARCHAR(255) NULL,
    `role` VARCHAR(32) NULL,
    `profile_photo_url` VARCHAR(500) NULL,
    `active` BOOLEAN NOT NULL DEFAULT TRUE,
    `wallet_balance` DOUBLE NOT NULL DEFAULT 1500.0,
    `created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_user_email` (`email`),
    INDEX `idx_user_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. Drivers Table
CREATE TABLE IF NOT EXISTS `drivers` (
    `id` VARCHAR(64) NOT NULL,
    `user_id` VARCHAR(64) NOT NULL UNIQUE,
    `license_number` VARCHAR(255) NULL,
    `license_photo_url` VARCHAR(500) NULL,
    `driver_photo_url` VARCHAR(500) NULL,
    `rc_document_url` VARCHAR(500) NULL,
    `insurance_document_url` VARCHAR(500) NULL,
    `permit_document_url` VARCHAR(500) NULL,
    `verification_notes` VARCHAR(500) NULL,
    `verification_status` VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    `online` BOOLEAN NOT NULL DEFAULT FALSE,
    `available` BOOLEAN NOT NULL DEFAULT TRUE,
    `current_latitude` DOUBLE NULL,
    `current_longitude` DOUBLE NULL,
    `rating` DOUBLE NOT NULL DEFAULT 5.0,
    `total_trips` INT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_driver_user_id` (`user_id`),
    INDEX `idx_driver_status` (`verification_status`),
    INDEX `idx_driver_location` (`current_latitude`, `current_longitude`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. Vehicles Table
CREATE TABLE IF NOT EXISTS `vehicles` (
    `id` VARCHAR(64) NOT NULL,
    `driver_id` VARCHAR(64) NOT NULL UNIQUE,
    `category` VARCHAR(32) NULL,
    `make` VARCHAR(255) NULL,
    `model` VARCHAR(255) NULL,
    `color` VARCHAR(255) NULL,
    `manufacturing_year` INT NULL,
    `registration_number` VARCHAR(255) NOT NULL UNIQUE,
    `vehicle_photo_url` VARCHAR(500) NULL,
    `rc_book_url` VARCHAR(500) NULL,
    `insurance_policy_url` VARCHAR(500) NULL,
    `fitness_certificate_url` VARCHAR(500) NULL,
    `verification_status` VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    `rejection_reason` VARCHAR(500) NULL,
    PRIMARY KEY (`id`),
    INDEX `idx_vehicle_driver_id` (`driver_id`),
    INDEX `idx_vehicle_reg_num` (`registration_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. Bookings Table
CREATE TABLE IF NOT EXISTS `bookings` (
    `id` VARCHAR(64) NOT NULL,
    `passenger_id` VARCHAR(64) NULL,
    `driver_id` VARCHAR(64) NULL,
    `vehicle_category` VARCHAR(32) NULL,
    `pickup_address` VARCHAR(500) NULL,
    `pickup_latitude` DOUBLE NULL,
    `pickup_longitude` DOUBLE NULL,
    `dropoff_address` VARCHAR(500) NULL,
    `dropoff_latitude` DOUBLE NULL,
    `dropoff_longitude` DOUBLE NULL,
    `distance_km` DOUBLE NOT NULL DEFAULT 0.0,
    `estimated_duration_minutes` INT NOT NULL DEFAULT 0,
    `estimated_fare` DOUBLE NOT NULL DEFAULT 0.0,
    `final_fare` DOUBLE NULL,
    `status` VARCHAR(32) NOT NULL DEFAULT 'REQUESTED',
    `otp` VARCHAR(8) NULL,
    `otp_attempts` INT NOT NULL DEFAULT 0,
    `scheduled` BOOLEAN NOT NULL DEFAULT FALSE,
    `scheduled_time` TIMESTAMP NULL,
    `payment_status` VARCHAR(32) NULL,
    `payment_method` VARCHAR(32) NULL,
    `payment_transaction_id` VARCHAR(64) NULL,
    `paid_at` TIMESTAMP NULL,
    `created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_booking_passenger` (`passenger_id`),
    INDEX `idx_booking_driver` (`driver_id`),
    INDEX `idx_booking_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. Reviews Table
CREATE TABLE IF NOT EXISTS `reviews` (
    `id` VARCHAR(64) NOT NULL,
    `booking_id` VARCHAR(64) NULL,
    `passenger_id` VARCHAR(64) NULL,
    `driver_id` VARCHAR(64) NULL,
    `rating` INT NOT NULL DEFAULT 5,
    `comment` VARCHAR(1000) NULL,
    `created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_review_booking` (`booking_id`),
    INDEX `idx_review_driver` (`driver_id`),
    INDEX `idx_review_passenger` (`passenger_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6. Safety Contacts Table
CREATE TABLE IF NOT EXISTS `safety_contacts` (
    `id` VARCHAR(64) NOT NULL,
    `user_id` VARCHAR(64) NOT NULL,
    `name` VARCHAR(255) NULL,
    `phone` VARCHAR(255) NULL,
    `relationship` VARCHAR(255) NULL,
    `created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_safety_contact_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
