-- ============================================
-- Create Databases Script
-- ============================================
-- This script creates the auth_db and erp_db databases
-- if they do not already exist.
-- Default charset: utf8mb4 (supports full Unicode including emojis)
-- ============================================

-- Create auth_db for authentication and user management
CREATE DATABASE IF NOT EXISTS auth_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- Create erp_db for ERP business data
CREATE DATABASE IF NOT EXISTS erp_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- Display success message
SELECT 'Databases created successfully!' AS Status;

