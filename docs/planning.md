# MEDIS — Hospital Management System Planning

**Course:** CCP6224 Object-Oriented Analysis and Design
**Due:** 29 June 2026, 11.59 pm (no extensions)
**Marks:** 25% (scaled to 40%)
**Type:** Group work (3–4 students, same tutorial section)

---

## 1. Requirements Source Note

| Source | Requirement |
|--------|-------------|
| PDF (official) | Java Swing GUI, OOP principles, UML diagrams (Class + Sequence), 5 modules, presentation video |
| **Lecturer (verbal addition)** | **Database integration is REQUIRED** (not stated in PDF) |
| **Lecturer (verbal addition)** | **Use Case Diagram is REQUIRED** (not stated in PDF) |

> Database is treated as a hard requirement from this point onward.

---

## 2. System Overview

A **Hospital Management System (HMS)** providing:

1. User authentication with role-based access (Admin, Doctor, Receptionist)
2. Appointment booking with duplicate-slot prevention
3. Patient record management (CRUD + medical history)
4. Doctor management (CRUD + specialization)
5. Basic reporting (totals + schedules)

---

## 3. Technology Stack

| Layer | Choice | Justification |
|-------|--------|---------------|
| UI | **Java Swing** (mandatory, no other framework) | PDF requirement |
| Language | Java 17 LTS | Modern Java, stable |
| Database | **SQLite** (recommended) or MySQL | SQLite = zero-config, file-based, easy to ship in ZIP; MySQL if lecturer prefers server-based |
| JDBC Driver | `sqlite-jdbc` or `mysql-connector-j` | Standard DB access |
| Build | Manual (`javac`) or Maven/Gradle | Maven preferred for dependency management of JDBC driver |
| IDE | IntelliJ IDEA / VS Code / NetBeans | Free choice |

**Decision needed from group:** SQLite vs MySQL. Recommendation = **SQLite** (no install, fits inside the submission ZIP, runs anywhere).

---

## 4. Architecture — Clean / Layered

```
┌──────────────────────────────────────────┐
│  View (Swing JFrames / JPanels)          │  ← user interaction
├──────────────────────────────────────────┤
│  Controller (SystemController, etc.)     │  ← orchestrates flow
├──────────────────────────────────────────┤
│  Service (business logic)                │  ← validation, rules
├──────────────────────────────────────────┤
│  DAO (Data Access Objects)               │  ← JDBC calls
├──────────────────────────────────────────┤
│  Model (Patient, Doctor, Appointment...) │  ← domain entities
└──────────────────────────────────────────┘
              ↓
          Database (SQLite)
```

### Package Structure

```
src/
└── com.medis/
    ├── Main.java
    ├── model/          (Patient, Doctor, Appointment, User, Admin, Receptionist)
    ├── view/           (LoginFrame, DashboardFrame, AppointmentPanel, etc.)
    ├── controller/     (SystemController, LoginController, AppointmentController)
    ├── service/        (AuthService, AppointmentService, PatientService, ReportService)
    ├── dao/            (UserDAO, PatientDAO, DoctorDAO, AppointmentDAO)
    ├── db/             (DatabaseConnection.java — singleton JDBC connection)
    └── util/           (Validator, DateUtil, etc.)
```

---

## 5. Database Design

### Tables

**`users`**
| Column | Type | Notes |
|--------|------|-------|
| user_id | INTEGER PK AUTOINCREMENT | |
| username | TEXT UNIQUE NOT NULL | |
| password | TEXT NOT NULL | hashed (SHA-256) |
| role | TEXT NOT NULL | ADMIN / DOCTOR / RECEPTIONIST |

**`patients`**
| Column | Type | Notes |
|--------|------|-------|
| patient_id | INTEGER PK AUTOINCREMENT | |
| name | TEXT NOT NULL | |
| age | INTEGER | |
| gender | TEXT | M / F / Other |
| medical_history | TEXT | free text |
| created_at | DATETIME | |

**`doctors`**
| Column | Type | Notes |
|--------|------|-------|
| doctor_id | INTEGER PK AUTOINCREMENT | |
| name | TEXT NOT NULL | |
| specialization | TEXT NOT NULL | |
| user_id | INTEGER FK → users | nullable, links to login |

**`appointments`**
| Column | Type | Notes |
|--------|------|-------|
| appointment_id | INTEGER PK AUTOINCREMENT | |
| patient_id | INTEGER FK NOT NULL | |
| doctor_id | INTEGER FK NOT NULL | |
| appointment_datetime | DATETIME NOT NULL | UNIQUE(doctor_id, appointment_datetime) — prevents duplicate slots |
| status | TEXT | SCHEDULED / COMPLETED / CANCELLED |

### Constraints
- Unique index on `(doctor_id, appointment_datetime)` → enforces no duplicate slots
- Foreign keys with `ON DELETE RESTRICT` for safety

### Seed Data
- 1 admin user (`admin / admin123`)
- 2 sample doctors, 2 sample patients, 1 receptionist — for demo purposes

---

## 6. UML Plan

### 6.0 Use Case Diagram (lecturer-mandated, not in PDF)

**Actors:** Admin, Doctor, Receptionist, (optional: Patient if self-service is added later)

**Use Cases by actor:**

| Actor | Use Cases |
|-------|-----------|
| Admin | Login, Manage Users, Manage Doctors, View Reports |
| Doctor | Login, View Appointments, View Patient History, Update Medical Notes |
| Receptionist | Login, Add/Update Patient, Book Appointment, View Appointment List |

**Relationships to show:**
- `<<include>>` — every use case includes **Login** (or place Login as a separate base use case)
- `<<extend>>` — *Cancel Appointment* extends *View Appointment List* (optional)
- Generalization — Admin / Doctor / Receptionist can generalize from a base `User` actor

### 6.1 Class Diagram (required classes + extensions)

**Required by PDF:** Patient, Doctor, Appointment, User, SystemController

**Adding:**
- `Admin`, `Receptionist` (extend `User`) — demonstrates **inheritance**
- DAO classes — separation of concerns
- Service classes — business logic
- `DatabaseConnection` (singleton)

### 6.2 Key Relationships
- `User` ← inheritance — `Admin`, `Doctor`, `Receptionist` (polymorphism via `login()` / role checks)
- `Appointment` → association → `Patient` (1..1)
- `Appointment` → association → `Doctor` (1..1)
- `SystemController` → aggregation → `Patient[]`, `Doctor[]`, `Appointment[]`
- `Doctor` — composition — `Specialization` (optional, if value object)

### 6.3 Sequence Diagrams (4 required)
1. **Login Process** — LoginFrame → AuthService → UserDAO → DB → role-based dashboard
2. **Book Appointment** — AppointmentPanel → AppointmentController → AppointmentService (check duplicate) → AppointmentDAO → DB
3. **Add Patient** — PatientPanel → PatientController → PatientService → PatientDAO → DB
4. **Generate Report** — ReportPanel → ReportService → multiple DAOs → aggregated result → display

### 6.4 Tooling
- **Recommended:** draw.io / diagrams.net (free, exports PNG + .drawio source)
- Alternative: Lucidchart, StarUML, Visual Paradigm Community

---

## 7. OOP Concepts — Where Each Lives

| Concept | Implementation site |
|---------|---------------------|
| **Encapsulation** | All model classes — private fields + getters/setters |
| **Inheritance** | `User` (abstract) → `Admin`, `Doctor`, `Receptionist` |
| **Polymorphism** | `User.getDashboard()` overridden per role; collections of `User` |
| **Abstraction** | `User` abstract class; DAO interfaces (`PatientDAO`, etc.) |

Plus: **ActionListener** for buttons, **MouseListener** where needed (table row clicks for editing).

---

## 8. Module Breakdown & Task Assignment Suggestion

| Module | Tasks | Suggested Owner |
|--------|-------|-----------------|
| M1: Login | LoginFrame, AuthService, UserDAO, password hashing, role routing | Member A |
| M2: Appointment | AppointmentPanel, duplicate-slot logic, table view | Member B |
| M3: Patient | PatientPanel (CRUD), medical history view | Member C |
| M4: Doctor | DoctorPanel (CRUD), specialization, link to appointments | Member D (or shared) |
| M5: Reporting | ReportPanel, aggregate queries | Shared / rotating |
| **Cross-cutting** | DB setup, schema, Main.java, UML diagrams, video | Whole group |

---

## 9. Timeline (today → 29 June 2026)

Today is **2026-06-09** → **20 days** to deadline.

| Phase | Days | Window | Deliverable |
|-------|------|--------|-------------|
| Setup | 2 | Jun 9–10 | Repo, package skeleton, DB schema, JDBC connection working |
| UML — Class Diagram | 2 | Jun 11–12 | Draft class diagram, group review |
| Core models + DAO + DB | 3 | Jun 13–15 | All entities + CRUD via DAO, verified |
| M1 Login + M3 Patient | 3 | Jun 16–18 | Login flow + patient CRUD GUI functional |
| M4 Doctor + M2 Appointment | 3 | Jun 19–21 | Booking with duplicate prevention works |
| M5 Reports + polish | 2 | Jun 22–23 | Reports + UI consistency pass |
| Sequence diagrams | 1 | Jun 24 | All 4 diagrams finalized |
| Testing + bug fix | 2 | Jun 25–26 | End-to-end test, fix edge cases |
| Video recording + ZIP | 2 | Jun 27–28 | 10–15 min video, packaged ZIP |
| Buffer / submit | 1 | Jun 29 | Submit early — NOT at 11:59 pm |

---

## 10. Video Presentation Plan (10–15 min, exact rubric order)

| Section | Time | Content |
|---------|------|---------|
| 1. Introduction | 1 min | Names of all members on camera, system overview |
| 2. Class Diagram | 3 min | Walk through classes, relationships, justify design |
| 3. Sequence Diagrams | 3 min | Login + Appointment + Record mgmt flows |
| 4. System Demo | 5–6 min | Live demo: login → add patient → book appt → view report |
| 5. Code Explanation | 2 min | Show OOP concepts in code + key classes |

**All members must appear on camera.**

---

## 11. Submission Checklist

ZIP filename: `StudentID-Name.zip`

- [ ] `/uml/` — Class diagram (PNG + source), 4 sequence diagrams
- [ ] `/src/` — `.java` files organized into packages
- [ ] `/db/` — SQLite file with seed data + `schema.sql`
- [ ] `/lib/` — JDBC driver `.jar` (so it runs out of the box)
- [ ] `README.md` — how to run, default login credentials
- [ ] `presentation.mp4` — 10–15 min, all members visible
- [ ] Verified the ZIP extracts and runs on a clean machine

---

## 12. Risk Register

| Risk | Mitigation |
|------|------------|
| Group member drops out | Distribute knowledge, no single owner of critical code |
| DB choice rejected by lecturer | Confirm SQLite is acceptable in next class; have MySQL fallback config ready |
| Last-minute upload failure | Submit by Jun 28 latest, not Jun 29 |
| UML and code diverge | Re-sync diagrams after each module is done, not at the end |
| Video over 15 min | Script + rehearse once; cut ruthlessly |
| Forgetting to demo a module | Use a demo script ticked off during recording |

---

## 13. Open Questions for Lecturer / Tutor

1. Is **SQLite** acceptable, or must the database be **MySQL/server-based**?
2. Should the JDBC driver `.jar` be bundled in the submission ZIP?
3. Are passwords required to be hashed, or is plaintext acceptable for the lab scope?
4. Is a `README.md` with run instructions expected in the ZIP?

---

## 14. Next Immediate Steps

1. Confirm group members and assign module ownership.
2. Confirm DB choice with lecturer (SQLite recommended).
3. Initialize Java project with package skeleton from Section 4.
4. Create `schema.sql` and `DatabaseConnection` singleton.
5. Start class diagram draft in draw.io.
