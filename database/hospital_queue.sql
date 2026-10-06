-- ====================================================================
-- Real-Time Hospital Appointment and Queue Management System
-- Database Schema & Sample Seed Data
-- Database: hospital_queue_db
-- ====================================================================

CREATE DATABASE IF NOT EXISTS hospital_queue_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE hospital_queue_db;

-- --------------------------------------------------------------------
-- Table 1: users
-- --------------------------------------------------------------------
DROP TABLE IF EXISTS queues;
DROP TABLE IF EXISTS queue_settings;
DROP TABLE IF EXISTS appointments;
DROP TABLE IF EXISTS doctor_schedules;
DROP TABLE IF EXISTS doctors;
DROP TABLE IF EXISTS rooms;
DROP TABLE IF EXISTS departments;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('PATIENT', 'DOCTOR', 'ADMIN') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- --------------------------------------------------------------------
-- Table 2: departments
-- --------------------------------------------------------------------
CREATE TABLE departments (
    department_id INT AUTO_INCREMENT PRIMARY KEY,
    department_name VARCHAR(100) NOT NULL,
    description TEXT,
    status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- --------------------------------------------------------------------
-- Table 3: rooms
-- --------------------------------------------------------------------
CREATE TABLE rooms (
    room_id INT AUTO_INCREMENT PRIMARY KEY,
    room_number VARCHAR(20) NOT NULL UNIQUE,
    department_id INT,
    status ENUM('AVAILABLE', 'OCCUPIED', 'MAINTENANCE') DEFAULT 'AVAILABLE',
    FOREIGN KEY (department_id) REFERENCES departments(department_id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- --------------------------------------------------------------------
-- Table 4: doctors
-- --------------------------------------------------------------------
CREATE TABLE doctors (
    doctor_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    department_id INT NOT NULL,
    room_id INT,
    specialization VARCHAR(100),
    status ENUM('AVAILABLE', 'BUSY', 'OFFLINE') DEFAULT 'AVAILABLE',
    average_consultation_minutes INT DEFAULT 5,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (department_id) REFERENCES departments(department_id) ON DELETE CASCADE,
    FOREIGN KEY (room_id) REFERENCES rooms(room_id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- --------------------------------------------------------------------
-- Table 5: appointments
-- --------------------------------------------------------------------
CREATE TABLE appointments (
    appointment_id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id INT NOT NULL,
    doctor_id INT NOT NULL,
    appointment_date DATE NOT NULL,
    appointment_time TIME DEFAULT '09:00:00',
    booking_source ENUM('ONLINE', 'WALK_IN') NOT NULL DEFAULT 'ONLINE',
    status ENUM('BOOKED', 'CHECKED_IN', 'CANCELLED', 'COMPLETED', 'ABSENT') DEFAULT 'BOOKED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (doctor_id) REFERENCES doctors(doctor_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- --------------------------------------------------------------------
-- Table 6: queues
-- --------------------------------------------------------------------
CREATE TABLE queues (
    queue_id INT AUTO_INCREMENT PRIMARY KEY,
    appointment_id INT,
    patient_id INT NOT NULL,
    doctor_id INT NOT NULL,
    token_number INT NOT NULL,
    queue_date DATE NOT NULL,
    status ENUM('BOOKED', 'CHECKED_IN', 'WAITING', 'CALLED', 'IN_CONSULTATION', 'COMPLETED', 'CANCELLED', 'ABSENT') DEFAULT 'BOOKED',
    booking_source ENUM('ONLINE', 'WALK_IN') NOT NULL DEFAULT 'ONLINE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    checked_in_at TIMESTAMP NULL DEFAULT NULL,
    called_at TIMESTAMP NULL DEFAULT NULL,
    consultation_started_at TIMESTAMP NULL DEFAULT NULL,
    completed_at TIMESTAMP NULL DEFAULT NULL,
    FOREIGN KEY (appointment_id) REFERENCES appointments(appointment_id) ON DELETE SET NULL,
    FOREIGN KEY (patient_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (doctor_id) REFERENCES doctors(doctor_id) ON DELETE CASCADE,
    UNIQUE KEY unique_doctor_date_token (doctor_id, queue_date, token_number)
) ENGINE=InnoDB;

-- --------------------------------------------------------------------
-- Table 7: queue_settings
-- --------------------------------------------------------------------
CREATE TABLE queue_settings (
    setting_id INT AUTO_INCREMENT PRIMARY KEY,
    doctor_id INT NOT NULL,
    queue_date DATE NOT NULL,
    starting_token INT DEFAULT 1,
    maximum_tokens INT DEFAULT 30,
    current_token INT DEFAULT 0,
    average_consultation_minutes INT DEFAULT 5,
    FOREIGN KEY (doctor_id) REFERENCES doctors(doctor_id) ON DELETE CASCADE,
    UNIQUE KEY unique_doctor_date_setting (doctor_id, queue_date)
) ENGINE=InnoDB;

-- --------------------------------------------------------------------
-- Table 8: doctor_schedules
-- --------------------------------------------------------------------
CREATE TABLE doctor_schedules (
    schedule_id INT AUTO_INCREMENT PRIMARY KEY,
    doctor_id INT NOT NULL,
    day_of_week ENUM('MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY') NOT NULL,
    start_time TIME NOT NULL DEFAULT '09:00:00',
    end_time TIME NOT NULL DEFAULT '17:00:00',
    maximum_patients INT DEFAULT 30,
    status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE',
    FOREIGN KEY (doctor_id) REFERENCES doctors(doctor_id) ON DELETE CASCADE
) ENGINE=InnoDB;


-- ====================================================================
-- SEED DATA
-- Passwords:
-- Admin:   Admin@123   -> NYHEOUNQ5H9dt6H/qM7/5Q==:voJSDBRO2/vl9leO8tT9LUnLaCZQ5JS30P5fm1uzk8E=
-- Doctor:  Doctor@123  -> lYbBuC2IdVLpvINO0/aJ8g==:V5Y2DNRKC5inQVTQ02zCfCMZ2IDkKwBdKLJ1dCYQjf4=
-- Patient: Patient@123 -> NUOaKXUp1lIkaRwVkGDsMw==:JAO2ndchT6z28Ueok7iDSWdL7ZYKKP1tuO9InrSu4tU=
-- ====================================================================

-- 1. Departments
INSERT INTO departments (department_id, department_name, description, status) VALUES
(1, 'General Medicine', 'Primary healthcare, internal medicine, fever and routine consultations', 'ACTIVE'),
(2, 'Cardiology', 'Heart health, cardiac checkups, hypertension and ECG services', 'ACTIVE'),
(3, 'Orthopedics', 'Bone, joint, spine care, fractures and orthopedic surgery consultations', 'ACTIVE'),
(4, 'Pediatrics', 'Comprehensive childcare, vaccinations and pediatric emergencies', 'ACTIVE');

-- 2. Rooms
INSERT INTO rooms (room_id, room_number, department_id, status) VALUES
(1, '101', 1, 'AVAILABLE'),
(2, '202', 2, 'AVAILABLE'),
(3, '305', 3, 'AVAILABLE'),
(4, '401', 4, 'AVAILABLE');

-- 3. Users (Admin, Doctors, Patients)
INSERT INTO users (user_id, full_name, email, phone, password_hash, role) VALUES
-- Admin
(1, 'Hospital Administrator', 'admin@hospital.com', '9876543210', 'NYHEOUNQ5H9dt6H/qM7/5Q==:voJSDBRO2/vl9leO8tT9LUnLaCZQ5JS30P5fm1uzk8E=', 'ADMIN'),

-- Doctors
(2, 'Dr. Kumar', 'kumar@hospital.com', '9876543211', 'lYbBuC2IdVLpvINO0/aJ8g==:V5Y2DNRKC5inQVTQ02zCfCMZ2IDkKwBdKLJ1dCYQjf4=', 'DOCTOR'),
(3, 'Dr. Priya', 'priya@hospital.com', '9876543212', 'lYbBuC2IdVLpvINO0/aJ8g==:V5Y2DNRKC5inQVTQ02zCfCMZ2IDkKwBdKLJ1dCYQjf4=', 'DOCTOR'),
(4, 'Dr. Arun', 'arun@hospital.com', '9876543213', 'lYbBuC2IdVLpvINO0/aJ8g==:V5Y2DNRKC5inQVTQ02zCfCMZ2IDkKwBdKLJ1dCYQjf4=', 'DOCTOR'),

-- Patients
(5, 'Arun', 'arun.patient@gmail.com', '9123456780', 'NUOaKXUp1lIkaRwVkGDsMw==:JAO2ndchT6z28Ueok7iDSWdL7ZYKKP1tuO9InrSu4tU=', 'PATIENT'),
(6, 'Meena Ramesh', 'meena.patient@gmail.com', '9123456781', 'NUOaKXUp1lIkaRwVkGDsMw==:JAO2ndchT6z28Ueok7iDSWdL7ZYKKP1tuO9InrSu4tU=', 'PATIENT'),
(7, 'Rahul Verma', 'rahul.patient@gmail.com', '9123456782', 'NUOaKXUp1lIkaRwVkGDsMw==:JAO2ndchT6z28Ueok7iDSWdL7ZYKKP1tuO9InrSu4tU=', 'PATIENT'),
(8, 'Sita Devi', 'sita.patient@gmail.com', '9123456783', 'NUOaKXUp1lIkaRwVkGDsMw==:JAO2ndchT6z28Ueok7iDSWdL7ZYKKP1tuO9InrSu4tU=', 'PATIENT'),
(9, 'Karthik Raja', 'karthik.patient@gmail.com', '9123456784', 'NUOaKXUp1lIkaRwVkGDsMw==:JAO2ndchT6z28Ueok7iDSWdL7ZYKKP1tuO9InrSu4tU=', 'PATIENT'),
(10, 'Ananya Sharma', 'ananya.patient@gmail.com', '9123456785', 'NUOaKXUp1lIkaRwVkGDsMw==:JAO2ndchT6z28Ueok7iDSWdL7ZYKKP1tuO9InrSu4tU=', 'PATIENT');

-- 4. Doctors profile mapping
INSERT INTO doctors (doctor_id, user_id, department_id, room_id, specialization, status, average_consultation_minutes) VALUES
(1, 2, 1, 1, 'General Physician & Diabetologist', 'AVAILABLE', 5),
(2, 3, 2, 2, 'Interventional Cardiologist', 'AVAILABLE', 8),
(3, 4, 3, 3, 'Joint Replacement & Spine Specialist', 'AVAILABLE', 7);

-- 5. Doctor Schedules (Every day of the week for full coverage)
INSERT INTO doctor_schedules (doctor_id, day_of_week, start_time, end_time, maximum_patients, status) VALUES
(1, 'MONDAY', '08:00:00', '20:00:00', 30, 'ACTIVE'),
(1, 'TUESDAY', '08:00:00', '20:00:00', 30, 'ACTIVE'),
(1, 'WEDNESDAY', '08:00:00', '20:00:00', 30, 'ACTIVE'),
(1, 'THURSDAY', '08:00:00', '20:00:00', 30, 'ACTIVE'),
(1, 'FRIDAY', '08:00:00', '20:00:00', 30, 'ACTIVE'),
(1, 'SATURDAY', '08:00:00', '20:00:00', 30, 'ACTIVE'),
(1, 'SUNDAY', '08:00:00', '20:00:00', 30, 'ACTIVE'),

(2, 'MONDAY', '09:00:00', '18:00:00', 30, 'ACTIVE'),
(2, 'TUESDAY', '09:00:00', '18:00:00', 30, 'ACTIVE'),
(2, 'WEDNESDAY', '09:00:00', '18:00:00', 30, 'ACTIVE'),
(2, 'THURSDAY', '09:00:00', '18:00:00', 30, 'ACTIVE'),
(2, 'FRIDAY', '09:00:00', '18:00:00', 30, 'ACTIVE'),
(2, 'SATURDAY', '09:00:00', '18:00:00', 30, 'ACTIVE'),
(2, 'SUNDAY', '09:00:00', '18:00:00', 30, 'ACTIVE'),

(3, 'MONDAY', '09:00:00', '17:00:00', 30, 'ACTIVE'),
(3, 'TUESDAY', '09:00:00', '17:00:00', 30, 'ACTIVE'),
(3, 'WEDNESDAY', '09:00:00', '17:00:00', 30, 'ACTIVE'),
(3, 'THURSDAY', '09:00:00', '17:00:00', 30, 'ACTIVE'),
(3, 'FRIDAY', '09:00:00', '17:00:00', 30, 'ACTIVE'),
(3, 'SATURDAY', '09:00:00', '17:00:00', 30, 'ACTIVE'),
(3, 'SUNDAY', '09:00:00', '17:00:00', 30, 'ACTIVE');

-- 6. Today's Queue Settings for Dr. Kumar (Doctor 1)
-- Starts with current_token = 20, tokens 21-25 waiting to mirror the exact problem prompt scenario!
INSERT INTO queue_settings (doctor_id, queue_date, starting_token, maximum_tokens, current_token, average_consultation_minutes) VALUES
(1, CURDATE(), 1, 30, 20, 5),
(2, CURDATE(), 1, 30, 0, 8),
(3, CURDATE(), 1, 30, 0, 7);

-- 7. Appointments & Active Queue for Dr. Kumar for CURDATE() matching Section 7 & 46 demonstration!
-- Token 20: Currently completed/served
-- Token 21: Meena (Online, CHECKED_IN/WAITING)
-- Token 22: Rahul (Walk-in, WAITING)
-- Token 23: Sita (Online, WAITING)
-- Token 24: Karthik (Online, WAITING)
-- Token 25: Arun (Online, WAITING) -> patient Arun!

INSERT INTO appointments (appointment_id, patient_id, doctor_id, appointment_date, appointment_time, booking_source, status) VALUES
(1, 6, 1, CURDATE(), '09:30:00', 'ONLINE', 'CHECKED_IN'),
(2, 7, 1, CURDATE(), '09:40:00', 'WALK_IN', 'CHECKED_IN'),
(3, 8, 1, CURDATE(), '09:50:00', 'ONLINE', 'CHECKED_IN'),
(4, 9, 1, CURDATE(), '10:00:00', 'ONLINE', 'CHECKED_IN'),
(5, 5, 1, CURDATE(), '10:10:00', 'ONLINE', 'CHECKED_IN');

INSERT INTO queues (queue_id, appointment_id, patient_id, doctor_id, token_number, queue_date, status, booking_source, created_at, checked_in_at) VALUES
(1, 1, 6, 1, 21, CURDATE(), 'WAITING', 'ONLINE', NOW() - INTERVAL 60 MINUTE, NOW() - INTERVAL 40 MINUTE),
(2, 2, 7, 1, 22, CURDATE(), 'WAITING', 'WALK_IN', NOW() - INTERVAL 50 MINUTE, NOW() - INTERVAL 35 MINUTE),
(3, 3, 8, 1, 23, CURDATE(), 'WAITING', 'ONLINE', NOW() - INTERVAL 45 MINUTE, NOW() - INTERVAL 30 MINUTE),
(4, 4, 9, 1, 24, CURDATE(), 'WAITING', 'ONLINE', NOW() - INTERVAL 40 MINUTE, NOW() - INTERVAL 25 MINUTE),
(5, 5, 5, 1, 25, CURDATE(), 'WAITING', 'ONLINE', NOW() - INTERVAL 30 MINUTE, NOW() - INTERVAL 20 MINUTE);
