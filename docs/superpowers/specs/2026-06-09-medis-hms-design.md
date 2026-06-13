# MEDIS — Hospital Management System Design Spec

**Course:** CCP6224 Object-Oriented Analysis and Design
**Project:** Hospital Management System (HMS)
**Due:** 29 June 2026, 11.59 pm
**Marks:** 25% (scaled to 40%)
**Group size:** 4 members, same tutorial section
**Spec date:** 2026-06-09

---

## 1. Goal

Build a GUI-based Hospital Management System using **Java Swing** with **SQLite** persistence, demonstrating proper OOP design, UML modeling, and a clean layered architecture, suitable for the rubric's "Good (5)" tier across all five criteria.

---

## 2. Scope

### In scope (PDF Section 4 + lecturer additions)

| Module | Capability |
|--------|-----------|
| M1 — User Login | Username/password, role-based access (Admin, Doctor, Receptionist), input validation, **SHA-256 hashed passwords** |
| M2 — Appointment Booking | Create appointment, select patient/doctor/datetime, view list, **prevent duplicate slots** |
| M3 — Patient Records | Add, update, view medical history; store name/age/gender/history |
| M4 — Doctor Management | Add/view doctors, assign specialization, link to appointments |
| M5 — Basic Reporting | Total patients, total appointments, doctor schedules |

### Lecturer-mandated additions (not in PDF)
- **Database integration** (SQLite)
- **Use Case Diagram**

### Out of scope
- Patient self-service login
- Audit logs, login attempt limits
- CSV export, search/filter (deferred — can add if time permits)
- Multi-user concurrent access (single-user desktop app)

---

## 3. Locked Decisions

| Decision | Value | Why |
|----------|-------|-----|
| Database | **SQLite** | File-based, zero install for marker, ships inside ZIP |
| Build | **Manual + bundled `lib/`** | Marker only needs Java; no Maven/Gradle setup |
| Architecture | **Approach 1 — MVC + DAO** (4 layers) | Service layer would just forward calls; PDF doesn't require it |
| Polish level | **Solid coursework** | Hashing + validation + soft-delete + friendly dialogs |
| Diagrams | **6 total** | 1 Use Case + 1 Class + 4 Sequence (ERD on standby) |
| Workflow | **GitHub feature branches → PRs into `main`** | Avoids merge conflicts; 4-person collab |
| Java version | **Java 17 LTS** | Modern, stable, widely available |

---

## 4. Architecture

```
┌─────────────────────────────────────────────────────────┐
│  VIEW (Swing)                                            │
│  LoginFrame, DashboardFrame, PatientPanel, DoctorPanel,  │
│  AppointmentPanel, ReportPanel, DialogHelper             │
└────────────────────────────┬────────────────────────────┘
                             │ user events
                             ▼
┌─────────────────────────────────────────────────────────┐
│  CONTROLLER                                              │
│  SystemController + LoginController, PatientController,  │
│  DoctorController, AppointmentController, ReportController │
│  → validation + business rules                           │
└────────────────────────────┬────────────────────────────┘
                             │ method calls
                             ▼
┌─────────────────────────────────────────────────────────┐
│  DAO  (interface + JDBC impl)                            │
│  UserDAO, PatientDAO, DoctorDAO, AppointmentDAO          │
└────────────────────────────┬────────────────────────────┘
                             │ JDBC
                             ▼
┌─────────────────────────────────────────────────────────┐
│  SQLite — db/medis.db                                    │
│  Tables: users, patients, doctors, appointments          │
└─────────────────────────────────────────────────────────┘
```

### OOP concept mapping

| Concept | Where it lives |
|---------|----------------|
| Encapsulation | All model classes — private fields + getters/setters |
| Inheritance | `User` (abstract) → `Admin`, `Doctor`, `Receptionist` |
| Polymorphism | `User.login()` overridden per subclass; `User`-typed references |
| Abstraction | `DAO` interfaces hide JDBC details from controllers |

### Bootstrap (Main.java)

```
1. DatabaseConnection.getInstance() opens db/medis.db (runs schema.sql if missing)
2. Build DAOs:     new UserDAOImpl(conn), new PatientDAOImpl(conn), ...
3. Build Controllers, inject DAOs
4. Build SystemController, inject all sub-controllers
5. new LoginFrame(systemController).setVisible(true)
```

---

## 5. Components (file manifest)

Total: **~27 Java files**.

### Models — `src/com/medis/model/` (7 files)
`User.java` (abstract), `Admin.java`, `Doctor.java`, `Receptionist.java`, `Patient.java`, `Appointment.java`, `Result.java`

> `Doctor` doubles as User subclass and the doctor entity (avoids a separate "DoctorProfile" class).

### DAOs — `src/com/medis/dao/` (8 files)

| Interface | Impl | Key methods |
|-----------|------|-------------|
| `UserDAO` | `UserDAOImpl` | findByUsername, insert, findAll |
| `PatientDAO` | `PatientDAOImpl` | insert, findById, findAll, update, softDelete |
| `DoctorDAO` | `DoctorDAOImpl` | insert, findById, findAll, update |
| `AppointmentDAO` | `AppointmentDAOImpl` | insert, findAll, existsByDoctorAndTime, findByDoctor, updateStatus |

### Controllers — `src/com/medis/controller/` (6 files)
`SystemController.java`, `LoginController.java`, `PatientController.java`, `DoctorController.java`, `AppointmentController.java`, `ReportController.java`

### Views — `src/com/medis/view/` (7 files)
`LoginFrame.java`, `DashboardFrame.java`, `PatientPanel.java`, `DoctorPanel.java`, `AppointmentPanel.java`, `ReportPanel.java`, `DialogHelper.java`

### Other
- `src/com/medis/db/DatabaseConnection.java` (singleton)
- `src/com/medis/util/PasswordHasher.java`, `Validator.java`
- `src/com/medis/Main.java`

### Outside `src/`
- `lib/sqlite-jdbc-3.x.x.jar`
- `db/schema.sql`, `db/medis.db` (gitignored)
- `bin/` (gitignored)
- `README.md`

### Role-based access (DashboardFrame)

| Role | Visible tabs |
|------|--------------|
| Admin | Patients, Doctors, Appointments, Reports, Users |
| Doctor | Appointments (own), Patients (read-only) |
| Receptionist | Patients, Appointments |

---

## 6. Database Schema

```sql
CREATE TABLE users (
    user_id   INTEGER PRIMARY KEY AUTOINCREMENT,
    username  TEXT UNIQUE NOT NULL,
    password  TEXT NOT NULL,            -- SHA-256 hash
    role      TEXT NOT NULL CHECK(role IN ('ADMIN','DOCTOR','RECEPTIONIST'))
);

CREATE TABLE patients (
    patient_id      INTEGER PRIMARY KEY AUTOINCREMENT,
    name            TEXT NOT NULL,
    age             INTEGER,
    gender          TEXT,
    medical_history TEXT,
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE doctors (
    doctor_id      INTEGER PRIMARY KEY AUTOINCREMENT,
    name           TEXT NOT NULL,
    specialization TEXT NOT NULL,
    user_id        INTEGER REFERENCES users(user_id)
);

CREATE TABLE appointments (
    appointment_id       INTEGER PRIMARY KEY AUTOINCREMENT,
    patient_id           INTEGER NOT NULL REFERENCES patients(patient_id),
    doctor_id            INTEGER NOT NULL REFERENCES doctors(doctor_id),
    appointment_datetime DATETIME NOT NULL,
    status               TEXT NOT NULL DEFAULT 'SCHEDULED'
        CHECK(status IN ('SCHEDULED','COMPLETED','CANCELLED')),
    UNIQUE(doctor_id, appointment_datetime)
);
```

### Seed data
- 1 Admin: `admin / admin123`
- 1 Doctor user: `doctor1 / pass123`, linked to "Dr. Smith" (Cardiology)
- 1 Receptionist: `recep1 / pass123`
- 2 sample patients (Alice, Bob) with medical history
- 1 scheduled sample appointment

---

## 7. Data Flow (3 canonical examples)

> Full traces in `docs/data-flow.md`. Summary here.

### Login
`LoginFrame → LoginController → UserDAO → PasswordHasher → SystemController.setCurrentUser → DashboardFrame`

### Book Appointment
`AppointmentPanel → AppointmentController` →
1. Validate inputs
2. `appointmentDAO.existsByDoctorAndTime(...)` (duplicate-slot check)
3. `appointmentDAO.insert(...)` (DB UNIQUE constraint as safety net)
4. Return `Result.ok()` → refresh table

### Generate Report
`ReportPanel → ReportController` → `PatientDAO.findAll().size()` + `AppointmentDAO.findAll().size()` + per-doctor schedules → builds `Report` object → display

### Universal pattern
```
View → Controller (validate) → DAO → DB → Result → View (refresh/error)
```

---

## 8. Error Handling

> Full detail in `docs/error-handling.md`. Summary here.

**Convention:** Controllers always return a `Result` (`ok`, `fail(msg)`). No exceptions propagate to the View layer.

### Three layers of defense

1. **Layer 1 — Controller validation** — empty fields, age range, past dates, missing references
2. **Layer 2 — DAO** — wraps `SQLException` → `Result.fail("Database error")`
3. **Layer 3 — View** — uses `DialogHelper.showError/showInfo/confirm` (no raw `JOptionPane`)

### Special cases
- First-launch DB bootstrap (`schema.sql` auto-runs)
- JDBC driver missing → fatal-error dialog, exit
- Soft-delete (status flag), not hard-delete
- Concurrent edits — out of scope (single-user)

---

## 9. UML Diagrams (6 total)

| # | Diagram | Owner brief |
|---|---------|-------------|
| 1 | Use Case Diagram | 3 actors (Admin, Doctor, Receptionist) + use cases per actor + `<<include>>` Login + generalization from base User |
| 2 | Class Diagram | All ~20 classes (Models, Controllers, DAOs, Views) + inheritance + association + aggregation |
| 3 | Sequence — Login | LoginFrame → LoginController → UserDAO → PasswordHasher → DashboardFrame |
| 4 | Sequence — Book Appointment | AppointmentPanel → Controller → DAO (duplicate check + insert) |
| 5 | Sequence — Add Patient | PatientPanel → Controller (validate) → DAO.insert |
| 6 | Sequence — Generate Report | ReportPanel → ReportController → multi-DAO aggregation |

Tool: **draw.io / diagrams.net** (free, exports PNG + editable `.drawio`).

> ERD is **not** drawn unless lecturer requests it later (schema in §6 is sufficient).

---

## 10. Testing Strategy

> Full detail in `docs/testing-strategy.md`. Summary here.

- **Tier 1 (mandatory):** 15-step manual smoke test on a clean DB, run by all 4 members before video record day.
- **Tier 2 (optional):** JUnit tests for `Validator` + `PasswordHasher` (pure functions, no UI).
- **Seed data** in `schema.sql` guarantees a predictable starting state for the demo.

Pre-submit checklist: clean rebuild, all 15 steps pass, all 4 members run from ZIP, README accurate.

---

## 11. Work Distribution (4 members)

> Full detail in `docs/work-distribution.md`. Summary here.

| Member | Code | UML | Docs | Video |
|--------|------|-----|------|-------|
| A | M1 Login + `User` abstract class + `Admin` + `Receptionist` subclasses + DatabaseConnection | Use Case + Seq:Login | README, setup | §1 Intro |
| B | M3 Patient | **Class Diagram (lead)** + Seq:Add Patient | OOP mapping | §2 Class |
| C | M2 Appointment + `schema.sql` | Seq:Book Appointment | Module 2 + DB doc | §3 Sequence |
| D | M4 Doctor (incl. `Doctor` class extending User — coordinates field set with A) + M5 Reporting + Main + SystemController | Seq:Generate Report | Module 4/5 + video script | §4 Demo + §5 Code + editing |

> **Coordination point:** Member A defines the `User` abstract class **first** (Jun 13–14). Member D then writes `Doctor extends User`, adding doctor-specific fields (`doctorId`, `specialization`). PR review across A/D required before merge.

All 4 must appear on camera (PDF mandate).

---

## 12. Timeline (today 2026-06-09 → due 2026-06-29)

| Window | Goal |
|--------|------|
| Jun 9–10 | Repo skeleton, schema.sql, DatabaseConnection working |
| Jun 11–12 | First Class Diagram draft circulated |
| Jun 13–15 | All models + DAOs implemented, verified via small main() |
| Jun 16–18 | M1 + M3 integrated (login → patient panel works) |
| Jun 19–21 | M2 + M4 integrated (booking with duplicate-prevention works) |
| Jun 22–23 | M5 reports + UI polish pass |
| Jun 24 | All 4 sequence diagrams finalized |
| Jun 25–26 | 15-step smoke test, fix anything broken |
| Jun 27–28 | Record + edit video |
| Jun 29 | Submit (morning, NOT 11:59 pm) |

---

## 13. Risks

| Risk | Mitigation |
|------|------------|
| Lecturer rejects SQLite | Have MySQL connection-string variant ready as fallback |
| Lecturer adds ERD requirement | Schema in §6 → ERD is 30 mins in draw.io |
| Member can't compile / VS Code issues | Member D owns canonical run instructions in README |
| Merge conflicts in `Main.java` | Member D owns `Main.java`; others integrate through PRs |
| Video over 15 min | Script per video section; rehearse once before final take |
| Last-minute upload failure | Submit by Jun 28 evening at the latest |

---

## 14. Open Items (resolve before code starts)

- [ ] Confirm SQLite acceptable with lecturer in next class
- [ ] Confirm hashing is OK / not required as plaintext for marking
- [ ] Confirm ERD not needed
- [ ] Confirm Java version installed by all 4 members (default assumed: Java 17 LTS; minimum: Java 11)
- [ ] Assign real names to roles A/B/C/D
- [ ] Decide IDE (VS Code recommended; group should align)

---

## 15. Related documents

| Doc | Purpose |
|-----|---------|
| `docs/planning.md` | Project plan, requirements, timeline |
| `docs/diagrams.md` | Diagram-by-diagram content brief |
| `docs/architecture-approaches.md` | Approach 1 vs Approach 2 rationale |
| `docs/data-flow.md` | Step-by-step traces of 3 key flows |
| `docs/error-handling.md` | `Result` pattern + 3-layer defense |
| `docs/testing-strategy.md` | Smoke test script + pre-submit checklist |
| `docs/work-distribution.md` | Member-by-member assignments |
| `docs/question.md` | Q&A on architecture concepts |
