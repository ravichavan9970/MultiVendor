-- MultiVendor Marketplace & Booking Platform Database Schema
-- Database: MySQL 8.0 / MariaDB / PostgreSQL compatible

CREATE DATABASE IF NOT EXISTS multivendor_db;
USE multivendor_db;

-- 1. Users Table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(120) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(30),
    role VARCHAR(20) NOT NULL DEFAULT 'CUSTOMER', -- 'CUSTOMER', 'VENDOR', 'ADMIN'
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 2. Vendor Profiles Table
CREATE TABLE IF NOT EXISTS vendor_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    business_name VARCHAR(150) NOT NULL,
    category VARCHAR(50) NOT NULL, -- 'TUTORING', 'FREELANCE', 'EVENT_SPACE', 'CONSULTING', 'FITNESS'
    bio TEXT,
    location VARCHAR(150),
    hourly_rate DECIMAL(10, 2) NOT NULL DEFAULT 50.00,
    rating DECIMAL(3, 2) DEFAULT 5.00,
    total_reviews INT DEFAULT 0,
    is_approved BOOLEAN NOT NULL DEFAULT TRUE,
    stripe_account_id VARCHAR(100),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 3. Service Items Table
CREATE TABLE IF NOT EXISTS service_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vendor_id BIGINT NOT NULL,
    title VARCHAR(150) NOT NULL,
    description TEXT,
    category VARCHAR(50) NOT NULL,
    duration_minutes INT NOT NULL DEFAULT 60,
    price DECIMAL(10, 2) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (vendor_id) REFERENCES vendor_profiles(id) ON DELETE CASCADE
);

-- 4. Availability Slots Table (Supports Pessimistic & Optimistic Concurrency Control)
CREATE TABLE IF NOT EXISTS availability_slots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    service_id BIGINT NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    status VARCHAR(25) NOT NULL DEFAULT 'AVAILABLE', -- 'AVAILABLE', 'HOLD', 'BOOKED'
    version BIGINT NOT NULL DEFAULT 0, -- Optimistic locking counter
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (service_id) REFERENCES service_items(id) ON DELETE CASCADE,
    INDEX idx_service_time (service_id, start_time, status)
);

-- 5. Bookings Table
CREATE TABLE IF NOT EXISTS bookings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_reference VARCHAR(36) NOT NULL UNIQUE,
    customer_id BIGINT NOT NULL,
    service_id BIGINT NOT NULL,
    slot_id BIGINT NOT NULL UNIQUE,
    status VARCHAR(30) NOT NULL DEFAULT 'HOLD_PENDING_PAYMENT', -- 'HOLD_PENDING_PAYMENT', 'CONFIRMED', 'CANCELLED', 'COMPLETED'
    total_amount DECIMAL(10, 2) NOT NULL,
    hold_expires_at DATETIME NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES users(id),
    FOREIGN KEY (service_id) REFERENCES service_items(id),
    FOREIGN KEY (slot_id) REFERENCES availability_slots(id)
);

-- 6. Payment Transactions Table (Stripe Integration Audit Trail)
CREATE TABLE IF NOT EXISTS payment_transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    stripe_session_id VARCHAR(150) NOT NULL UNIQUE,
    stripe_payment_intent VARCHAR(150),
    amount DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(10) NOT NULL DEFAULT 'usd',
    status VARCHAR(30) NOT NULL, -- 'PENDING', 'SUCCEEDED', 'FAILED', 'REFUNDED'
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE
);

-- Seed Initial Demo Data (BCrypt hashed password for 'password123': $2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd0P1R22C99fJ3C2)
INSERT INTO users (id, email, password_hash, full_name, phone_number, role, is_active, created_at, updated_at) VALUES
(1, 'admin@multivendor.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd0P1R22C99fJ3C2', 'System Administrator', '+1-555-0100', 'ADMIN', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'vendor.alex@multivendor.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd0P1R22C99fJ3C2', 'Alex Rivera', '+1-555-0101', 'VENDOR', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'vendor.sarah@multivendor.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd0P1R22C99fJ3C2', 'Sarah Chen', '+1-555-0102', 'VENDOR', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 'customer.john@gmail.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd0P1R22C99fJ3C2', 'John Doe', '+1-555-0201', 'CUSTOMER', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE password_hash = VALUES(password_hash);

-- Seed Vendors
INSERT INTO vendor_profiles (id, user_id, business_name, category, bio, location, hourly_rate, rating, total_reviews, is_approved) VALUES
(1, 2, 'Rivera Tech & Algorithms Academy', 'TUTORING', 'Senior Software Engineer with 8+ years experience coaching Java, Data Structures, and System Design.', 'San Francisco, CA (Remote)', 85.00, 4.95, 42, TRUE),
(2, 3, 'PixelCraft Digital Design Studio', 'FREELANCE', 'Award-winning UI/UX designer specializing in modern Web App interfaces, branding, and Figma design systems.', 'New York, NY (Remote)', 95.00, 4.90, 28, TRUE)
ON DUPLICATE KEY UPDATE business_name = VALUES(business_name);

-- Seed Services
INSERT INTO service_items (id, vendor_id, title, description, category, duration_minutes, price, is_active) VALUES
(1, 1, '1-on-1 Java & System Design Mock Interview', '60-minute intensive technical mock interview covering Spring Boot architecture, SQL locks, and live coding exercises with actionable feedback.', 'TUTORING', 60, 85.00, TRUE),
(2, 1, 'Data Structures & Algorithms Deep-Dive', '60-minute problem-solving breakdown targeting LeetCode medium/hard patterns and code optimization.', 'TUTORING', 60, 75.00, TRUE),
(3, 2, 'Complete UI/UX Audit & Figma Wireframing', 'Direct consultation to overhaul your website user interface, color system, and user flow.', 'FREELANCE', 60, 95.00, TRUE)
ON DUPLICATE KEY UPDATE title = VALUES(title);

-- Seed Availability Slots (Upcoming Dates)
INSERT INTO availability_slots (id, service_id, start_time, end_time, status) VALUES
(1, 1, DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 1 DAY), DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 25 HOUR), 'AVAILABLE'),
(2, 1, DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 2 DAY), DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 49 HOUR), 'AVAILABLE'),
(3, 2, DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 1 DAY), DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 25 HOUR), 'AVAILABLE'),
(4, 3, DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 3 DAY), DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 73 HOUR), 'AVAILABLE')
ON DUPLICATE KEY UPDATE status = VALUES(status);
