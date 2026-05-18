-- Health Assistance System Database Setup
-- Run: CREATE DATABASE health_assist; USE health_assist; SOURCE init.sql;

CREATE DATABASE IF NOT EXISTS health_assist;
USE health_assist;

-- Users table (for login/roles)
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,  -- Use hashed passwords (e.g. BCrypt)
    role ENUM('admin', 'patient', 'doctor') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Patients table
CREATE TABLE patients (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    dob DATE,
    contact VARCHAR(20),
    user_id INT UNIQUE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Doctors table
CREATE TABLE doctors (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    specialization VARCHAR(100),
    schedule TEXT,  -- JSON or serialized
    user_id INT UNIQUE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Appointments table
CREATE TABLE appointments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id INT NOT NULL,
    doctor_id INT NOT NULL,
    appointment_date_time DATETIME NOT NULL,
    status ENUM('booked', 'completed', 'cancelled') DEFAULT 'booked',
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(id),
    FOREIGN KEY (doctor_id) REFERENCES doctors(id),
    UNIQUE KEY unique_appointment (patient_id, doctor_id, appointment_date_time)
);

-- Health Records table
CREATE TABLE health_records (
    id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id INT NOT NULL,
    doctor_id INT NOT NULL,
    record_date DATE NOT NULL,
    notes TEXT NOT NULL,
    FOREIGN KEY (patient_id) REFERENCES patients(id),
    FOREIGN KEY (doctor_id) REFERENCES doctors(id)
);

-- Passwords: admin1=adminpass, patient1=pass123, doctor1=docpass
INSERT INTO users (username, password_hash, role) VALUES
('admin1',   SHA2('adminpass', 256), 'admin'),
('patient1', SHA2('pass123',   256), 'patient'),
('doctor1',  SHA2('docpass',   256), 'doctor');

INSERT INTO patients (name, dob, contact, user_id) VALUES ('John Doe', '1990-01-01', '1234567890', 2);

INSERT INTO doctors (name, specialization, schedule, user_id) VALUES ('Dr. Smith', 'Cardiology', 'Mon-Fri 9-5', 3);

COMMIT;

