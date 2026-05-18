-- ============================================
-- Auth Database Schema
-- ============================================
-- This script creates the authentication and user management tables
-- in the auth_db database.
-- ============================================

USE auth_db;

-- ============================================
-- Table: users_auth
-- ============================================
-- Stores user authentication credentials and basic profile information.
-- user_id: Primary key, auto-incremented
-- username: Unique identifier for login
-- role: User role (student, instructor, or admin)
-- password_hash: BCrypt hashed password (never store plaintext)
-- status: Account status (active, inactive, suspended, etc.)
-- last_login: Timestamp of last successful login
-- ============================================
CREATE TABLE IF NOT EXISTS users_auth (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(64) UNIQUE NOT NULL,
    role ENUM('student', 'instructor', 'admin') NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    status VARCHAR(20) DEFAULT 'active',
    last_login DATETIME NULL,
    INDEX idx_username (username),
    INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Table: auth_failed_logins (Optional)
-- ============================================
-- Tracks failed login attempts for security monitoring.
-- username: Username that failed login
-- failed_count: Number of consecutive failed attempts
-- last_failed: Timestamp of last failed attempt
-- ============================================
CREATE TABLE IF NOT EXISTS auth_failed_logins (
    username VARCHAR(64) PRIMARY KEY,
    failed_count INT DEFAULT 0,
    last_failed DATETIME NULL,
    INDEX idx_last_failed (last_failed)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SELECT 'Auth schema created successfully!' AS Status;

