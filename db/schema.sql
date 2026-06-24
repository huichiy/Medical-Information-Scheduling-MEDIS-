CREATE TABLE IF NOT EXISTS users (
    user_id   INTEGER PRIMARY KEY AUTOINCREMENT,
    username  TEXT UNIQUE NOT NULL,
    password  TEXT NOT NULL,
    role      TEXT NOT NULL CHECK(role IN ('ADMIN','DOCTOR','RECEPTIONIST'))
);

CREATE TABLE IF NOT EXISTS patients (
    patient_id      INTEGER PRIMARY KEY AUTOINCREMENT,
    name            TEXT NOT NULL,
    age             INTEGER,
    gender          TEXT,
    medical_history TEXT,
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS doctors (
    doctor_id      INTEGER PRIMARY KEY AUTOINCREMENT,
    name           TEXT NOT NULL,
    specialization TEXT NOT NULL,
    user_id        INTEGER REFERENCES users(user_id)
);

CREATE TABLE IF NOT EXISTS appointments (
    appointment_id       INTEGER PRIMARY KEY AUTOINCREMENT,
    patient_id           INTEGER NOT NULL REFERENCES patients(patient_id),
    doctor_id            INTEGER NOT NULL REFERENCES doctors(doctor_id),
    appointment_datetime DATETIME NOT NULL,
    status               TEXT NOT NULL DEFAULT 'SCHEDULED'
        CHECK(status IN ('SCHEDULED','COMPLETED','CANCELLED')),
    UNIQUE(doctor_id, appointment_datetime)
);

-- Seed data. Password hashes are SHA-256 of the plaintext shown in README:
--   admin123  -> 240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9
--   pass123   -> 9b8769a4a742959a2d0298c36fb70623f2dfacda8436237df08d8dfd5b37374c
INSERT OR IGNORE INTO users (user_id, username, password, role) VALUES
    (1, 'admin',   '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'ADMIN'),
    (2, 'doctor1', '9b8769a4a742959a2d0298c36fb70623f2dfacda8436237df08d8dfd5b37374c', 'DOCTOR'),
    (3, 'recep1',  '9b8769a4a742959a2d0298c36fb70623f2dfacda8436237df08d8dfd5b37374c', 'RECEPTIONIST');

INSERT OR IGNORE INTO doctors (doctor_id, name, specialization, user_id) VALUES
    (1, 'Dr. Smith', 'Cardiology', 2),
    (2, 'Dr. Lee',   'Pediatrics', NULL);

INSERT OR IGNORE INTO patients (patient_id, name, age, gender, medical_history) VALUES
    (1, 'Alice Tan', 30, 'F', 'Mild asthma'),
    (2, 'Bob Lim',   45, 'M', 'Hypertension');

INSERT OR IGNORE INTO appointments (appointment_id, patient_id, doctor_id, appointment_datetime, status) VALUES
    (1, 1, 1, '2026-07-01 10:00:00', 'SCHEDULED');
