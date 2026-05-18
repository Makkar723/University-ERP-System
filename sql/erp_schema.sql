-- ============================================
-- ERP Database Schema
-- ============================================
-- This script creates the ERP business data tables
-- in the erp_db database.
-- Note: Foreign keys are defined but may not be enforced
-- depending on MySQL configuration (foreign_key_checks setting).
-- ============================================

USE erp_db;

-- ============================================
-- Table: students
-- ============================================
-- Student profile information.
-- user_id: References auth_db.users_auth.user_id (logical FK)
-- roll_no: Student roll number/registration number
-- program: Academic program (e.g., "Computer Science", "Engineering")
-- year: Current academic year (1, 2, 3, 4, etc.)
-- ============================================
CREATE TABLE IF NOT EXISTS students (
    user_id INT PRIMARY KEY,
    roll_no VARCHAR(30),
    program VARCHAR(100),
    year INT,
    INDEX idx_roll_no (roll_no),
    INDEX idx_program (program)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Table: instructors
-- ============================================
-- Instructor profile information.
-- user_id: References auth_db.users_auth.user_id (logical FK)
-- department: Department name (e.g., "Computer Science", "Mathematics")
-- ============================================
CREATE TABLE IF NOT EXISTS instructors (
    user_id INT PRIMARY KEY,
    department VARCHAR(100),
    INDEX idx_department (department)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Table: courses
-- ============================================
-- Course catalog information.
-- course_id: Primary key, auto-incremented
-- code: Course code (e.g., "CS101", "MATH201") - must be unique
-- title: Full course title
-- credits: Number of credit hours
-- ============================================
CREATE TABLE IF NOT EXISTS courses (
    course_id INT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(20) UNIQUE NOT NULL,
    title VARCHAR(255) NOT NULL,
    credits INT NOT NULL,
    INDEX idx_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Table: sections
-- ============================================
-- Course section/section information.
-- section_id: Primary key, auto-incremented
-- course_id: References courses.course_id (logical FK)
-- instructor_id: References instructors.user_id (logical FK), NULL if unassigned
-- day_time: Schedule information (e.g., "Mon/Wed 10:00-11:30")
-- room: Classroom location
-- capacity: Maximum enrollment capacity (must be >= 0)
-- semester: Semester name (e.g., "Fall", "Spring", "Summer")
-- year: Academic year (e.g., 2024)
-- ============================================
CREATE TABLE IF NOT EXISTS sections (
    section_id INT PRIMARY KEY AUTO_INCREMENT,
    course_id INT NOT NULL,
    instructor_id INT NULL,
    day_time VARCHAR(100),
    room VARCHAR(50),
    capacity INT NOT NULL DEFAULT 0 CHECK (capacity >= 0),
    semester VARCHAR(20),
    year INT,
    INDEX idx_course_id (course_id),
    INDEX idx_instructor_id (instructor_id),
    INDEX idx_semester_year (semester, year)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Table: enrollments
-- ============================================
-- Student enrollment in course sections.
-- enrollment_id: Primary key, auto-incremented
-- student_id: References students.user_id (logical FK)
-- section_id: References sections.section_id (logical FK)
-- status: Enrollment status (e.g., "enrolled", "dropped", "completed")
-- UNIQUE constraint prevents duplicate enrollments (same student in same section)
-- ============================================
CREATE TABLE IF NOT EXISTS enrollments (
    enrollment_id INT PRIMARY KEY AUTO_INCREMENT,
    student_id INT NOT NULL,
    section_id INT NOT NULL,
    status VARCHAR(20) DEFAULT 'enrolled',
    UNIQUE KEY unique_student_section (student_id, section_id),
    INDEX idx_student_id (student_id),
    INDEX idx_section_id (section_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Table: grades
-- ============================================
-- Grade records for enrolled students.
-- grade_id: Primary key, auto-incremented
-- enrollment_id: References enrollments.enrollment_id (logical FK)
-- component: Grade component name (e.g., "quiz", "midterm", "final", "assignment")
-- score: Numeric score (DECIMAL for precision)
-- final_grade: Final letter grade (e.g., "A", "B+", "C", "F")
-- ============================================
CREATE TABLE IF NOT EXISTS grades (
    grade_id INT PRIMARY KEY AUTO_INCREMENT,
    enrollment_id INT NOT NULL,
    component VARCHAR(50) NOT NULL,
    score DECIMAL(7,2) NULL,
    final_grade VARCHAR(10) NULL,
    INDEX idx_enrollment_id (enrollment_id),
    INDEX idx_component (component)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Table: settings
-- ============================================
-- Application settings key-value store.
-- k: Setting key name (e.g., "maintenance", "registration_open")
-- v: Setting value (stored as string, e.g., "true", "false", "2024-01-01")
-- ============================================
CREATE TABLE IF NOT EXISTS settings (
    k VARCHAR(100) PRIMARY KEY,
    v VARCHAR(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SELECT 'ERP schema created successfully!' AS Status;

