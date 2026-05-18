-- ============================================
-- Seed Data Script
-- ============================================
-- This script inserts sample data into both auth_db and erp_db.
-- 
-- IMPORTANT: Before running this script, you MUST replace the
-- password hash placeholders with real BCrypt hashes generated
-- using the HashPassword.java utility.
-- 
-- To generate a hash:
--   mvn -q compile exec:java -Dexec.mainClass="edu.univ.erp.util.HashPassword" -Dexec.args="yourpassword"
-- 
-- Then replace <BCRYPT-HASH-HERE> placeholders in the users_auth
-- INSERT statements below with the generated hash.
-- ============================================

USE auth_db;

-- ============================================
-- Insert users into users_auth
-- ============================================
-- Using explicit user_id values (1001-1004) for clarity and
-- to ensure consistent references in ERP tables.
-- Replace <BCRYPT-HASH-HERE> placeholders with real hashes!
-- ============================================
INSERT INTO users_auth (user_id, username, role, password_hash, status) VALUES
(1001, 'admin1', 'admin', '$2a$12$LGQc9Jf7qZuf9wjlNZUS.eaKPcHZu3pCelOgBQiegZC3g8qQkpxa2', 'active'),
(1002, 'inst1', 'instructor', '$2a$12$Vd6Sc/Py.n4zAxJaOEIXhOBIZM/3trBj6QGAmBPAGlXwaozsremdm', 'active'),
(1003, 'stu1', 'student', '$2a$12$Z5iOczaFGAfvFK1fKz6fKeWShEiMZSIgY9Dnkcb72efx1nkkPzZga', 'active'),            
(1004, 'stu2', 'student', '$2a$12$2Tf9PEvsld/IZJPe3d19m.luQhZJNl8c8lj/yFk7f/MsH1Br9vese', 'active');

-- ============================================
-- ERP Database Seed Data
-- ============================================

USE erp_db;

-- Insert student profiles
-- user_id 1003 = stu1, user_id 1004 = stu2
INSERT INTO students (user_id, roll_no, program, year) VALUES
(1003, 'MT25056', 'CSE', 2),
(1004, 'MT25055', 'CSAM', 2);

-- Insert instructor profile
-- user_id 1002 = inst1
INSERT INTO instructors (user_id, department) VALUES
(1002, 'CSE');

-- Insert sample course
INSERT INTO courses (course_id, code, title, credits) VALUES
(1, 'CSE101', 'Intro to Programming', 4);

-- Insert sample section
-- course_id 1 = CS101, instructor_id 1002 = inst1
INSERT INTO sections (section_id, course_id, instructor_id, day_time, room, capacity, semester, year) VALUES
(1, 1, 1002, 'Mon/Wed 10:00-11:30', 'Room 101', 30, '1', 2024);

-- Insert enrollment
-- student_id 1004 = stu2, section_id 1 = CS101 section
INSERT INTO enrollments (enrollment_id, student_id, section_id, status) VALUES
(1, 1004, 1, 'enrolled');

-- Insert settings
INSERT INTO settings (k, v) VALUES
('maintenance', 'false')
ON DUPLICATE KEY UPDATE v='false';

SELECT 'Seed data inserted successfully!' AS Status;
SELECT 'NOTE: Remember to replace password hash placeholders with real BCrypt hashes!' AS Warning;

