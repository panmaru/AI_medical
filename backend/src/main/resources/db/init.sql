-- AI Medical Platform - Database Initialization Script
CREATE DATABASE IF NOT EXISTS ai_medical DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE ai_medical;

SET FOREIGN_KEY_CHECKS = 0;

-- User Table
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    real_name VARCHAR(50),
    phone VARCHAR(20),
    email VARCHAR(100),
    role INT DEFAULT 2 COMMENT '0: Admin, 1: Doctor, 2: User',
    status INT DEFAULT 1 COMMENT '0: Disabled, 1: Enabled',
    dept_id BIGINT,
    title VARCHAR(50),
    specialty VARCHAR(200),
    avatar VARCHAR(500),
    deleted INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_real_name (real_name),
    INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Patient Table
DROP TABLE IF EXISTS patient;
CREATE TABLE patient (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_no VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(50) NOT NULL,
    gender INT COMMENT '0: Female, 1: Male',
    age INT,
    birthday DATE,
    phone VARCHAR(20),
    id_card VARCHAR(18),
    address VARCHAR(500),
    allergy_history TEXT,
    past_history TEXT,
    family_history TEXT,
    remark TEXT,
    create_by BIGINT,
    deleted INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_patient_no (patient_no),
    INDEX idx_name (name),
    INDEX idx_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Diagnosis Record Table
DROP TABLE IF EXISTS diagnosis_record;
CREATE TABLE diagnosis_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    record_no VARCHAR(50) UNIQUE NOT NULL,
    patient_id BIGINT NOT NULL,
    patient_name VARCHAR(50),
    doctor_id BIGINT,
    doctor_name VARCHAR(50),
    chief_complaint TEXT,
    present_illness TEXT,
    symptoms TEXT,
    ai_diagnosis TEXT,
    ai_suggestion TEXT,
    doctor_diagnosis TEXT,
    treatment_plan TEXT,
    prescription TEXT,
    diagnosis_type INT DEFAULT 0 COMMENT '0: AI, 1: Manual',
    match_rate DECIMAL(5,2),
    status INT DEFAULT 0 COMMENT '0: Pending, 1: Confirmed, 2: Completed',
    deleted INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_record_no (record_no),
    INDEX idx_patient_id (patient_id),
    INDEX idx_doctor_id (doctor_id),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Medical Knowledge Table
DROP TABLE IF EXISTS medical_knowledge;
CREATE TABLE medical_knowledge (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    disease_name VARCHAR(100),
    category VARCHAR(100),
    symptoms TEXT,
    etiology TEXT,
    diagnosis_methods TEXT,
    treatment TEXT,
    medication_advice TEXT,
    prevention TEXT,
    precautions TEXT,
    `references` TEXT,
    tags VARCHAR(500),
    source INT DEFAULT 0 COMMENT '0: System, 1: Manual',
    audit_status INT DEFAULT 0 COMMENT '0: Pending, 1: Approved',
    auditor_id BIGINT,
    audit_time DATETIME,
    create_by BIGINT,
    deleted INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_disease_name (disease_name),
    INDEX idx_category (category),
    INDEX idx_tags (tags),
    INDEX idx_audit_status (audit_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Insert Default Admin (password: admin123)
INSERT INTO sys_user (username, password, real_name, role, status)
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'System Admin', 0, 1);

-- Insert Test Doctor (password: doctor123)
INSERT INTO sys_user (username, password, real_name, role, status, title, specialty)
VALUES ('doctor', '$2a$10$VBIlJ5oZi3vRH3Pj4GyGXePJHlS4Cq0VHEQf2vZXA7LqP8ZxZzZ5m', 'Dr. Zhang', 1, 1, 'Attending Physician', 'Internal Medicine, Respiratory Medicine');

-- Insert Test Patients
INSERT INTO patient (patient_no, name, gender, age, birthday, phone, address) VALUES
('P20241201001', 'Zhang San', 1, 35, '1989-05-15', '13800138001', 'Beijing Chaoyang District'),
('P20241201002', 'Li Si', 0, 28, '1996-08-20', '13800138002', 'Beijing Haidian District'),
('P20241201003', 'Wang Wu', 1, 42, '1982-03-10', '13800138003', 'Beijing Xicheng District');

-- Insert Test Medical Knowledge
INSERT INTO medical_knowledge (title, disease_name, category, symptoms, etiology, diagnosis_methods, treatment, medication_advice, precautions, source, audit_status) VALUES
('Acute Upper Respiratory Infection', 'Acute Upper Respiratory Infection', 'Respiratory System', 'Nasal congestion, runny nose, cough, fever, headache, malaise', 'Mainly viral infection, some bacterial infections', 'Blood routine, chest X-ray', 'Rest, drink plenty of water, symptomatic treatment', 'Antipyretics, antihistamines, decongestants', 'Avoid cold, rest, drink plenty of water', 0, 1),
('Acute Gastritis', 'Acute Gastritis', 'Digestive System', 'Epigastric pain, nausea, vomiting, loss of appetite', 'Improper diet, drug irritation, stress response', 'Gastroscopy, blood routine', 'Fasting, fluid replacement, symptomatic treatment', 'Acid suppressants, gastric mucosal protectants', 'Regular diet, avoid irritating food', 0, 1);

SET FOREIGN_KEY_CHECKS = 1;
