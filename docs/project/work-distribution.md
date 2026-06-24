# MEDIS — Work Distribution (4 Members)

**Principle:** every member touches both **code** and **documentation/UML**. No one is "just docs" or "just code".

**Architecture:** Approach 1 — MVC + DAO (View → Controller → DAO → DB). No Service layer. See `docs/design/architecture-approaches.md`.

**Modules:** M1 Login · M2 Appointment · M3 Patient · M4 Doctor · M5 Reporting
**Diagrams:** 1 Use Case · 1 Class · 4 Sequence

---

## Quick Allocation Matrix

| Area | Member A | Member B | Member C | Member D |
|------|:--------:|:--------:|:--------:|:--------:|
| **Code Module** | M1 Login + `User`/`Admin`/`Receptionist` | M3 Patient | M2 Appointment | M4 Doctor + M5 Reporting |
| **UML — Use Case** | ✅ Lead | — | — | — |
| **UML — Class Diagram** | — | ✅ Lead | — | — |
| **Sequence Diagram** | Login | Add Patient | Book Appointment | Generate Report |
| **DB / Infra** | DatabaseConnection (JDBC) | — | `schema.sql` + seed data | Main.java + SystemController |
| **Docs** | README + setup guide | OOP concepts mapping | Module 2 functional doc | Module 4 & 5 doc + video script |
| **Video** | Section 1 (Intro) | Section 2 (Class Diagram) | Section 3 (Sequence) | Section 4 + 5 (Demo + Code) + editing |

> All 4 members must appear on camera (PDF requirement).

---

## Member A — Auth Foundation

### Code
- `User` abstract class + subclasses `Admin`, `Receptionist` (inheritance + polymorphism)
  > Note: `Doctor extends User` is owned by Member D — see coordination note below
- `LoginFrame` + `DashboardFrame` (Swing UI)
- `LoginController` (login logic, calls `PasswordHasher.verify(...)`)
- `UserDAO` interface + `UserDAOImpl` (JDBC: findByUsername, findAll)
- `DatabaseConnection` singleton (JDBC bootstrap, auto-runs `schema.sql` on first launch)
- `PasswordHasher` + `Validator` utilities (with JUnit tests — see plan Tasks 10–11)
- Role-based tab routing in `DashboardFrame`

### Diagrams
- **Use Case Diagram** (whole system — owns it)
- **Sequence Diagram — Login Process**

### Documentation
- `README.md` — how to compile, how to run, default credentials
- Setup section in `planning.md` if updates needed

### Video Section
- Section 1: Introduction (1 min) — introduce all members + system overview

---

## Member B — Patient Module + Class Diagram

### Code
- `Patient` model class (encapsulation: private fields + getters/setters)
- `PatientPanel` (Swing form + table view, includes medical history)
- `PatientController` (does validation **and** orchestration — no separate Service)
- `PatientDAO` interface + `PatientDAOImpl` (CRUD via JDBC)
- `Result` helper class (shared by all controllers)

### Diagrams
- **Class Diagram** (lead — collects input from A/C/D for their classes)
- **Sequence Diagram — Add Patient**

### Documentation
- OOP concepts mapping document — which file demonstrates encapsulation / inheritance / polymorphism / abstraction (referenced in video Section 5)

### Video Section
- Section 2: Class Diagram (3 min) — walk through structure, justify design

---

## Member C — Appointment Module + Database Design

### Code
- `Appointment` model class (with `Status` enum: SCHEDULED / COMPLETED / CANCELLED)
- `AppointmentPanel` (Swing UI: select patient, doctor, datetime; cancel button)
- `AppointmentController` — **owns the duplicate-slot prevention logic** (no Service layer)
- `AppointmentDAO` interface + `AppointmentDAOImpl` (with `existsByDoctorAndTime` + UNIQUE constraint as DB safety net)
- View appointment list + soft-cancel (status flag, no hard delete)

### Diagrams
- **Sequence Diagram — Book Appointment**

### Database / Infra
- `schema.sql` — all 4 tables (users, patients, doctors, appointments)
- UNIQUE constraint on `(doctor_id, appointment_datetime)`
- Seed data (1 admin, 2 doctors, 2 patients, 1 receptionist)

### Documentation
- Module 2 functional doc — booking flow, validation rules
- DB schema doc (table list + constraints)

### Video Section
- Section 3: Sequence Diagrams (3 min) — walk through Login, Appointment, Patient flows

---

## Member D — Doctor + Reporting + Integration

### Code
- `Doctor` model class — **extends `User`** (coordination point with Member A — see below). `Doctor` doubles as User subclass AND doctor entity (holds `doctorId`, `name`, `specialization` directly; no separate `Specialization` value object).
- `DoctorPanel` (Add/View, assign specialization)
- `DoctorController` + `DoctorDAO` interface + `DoctorDAOImpl`
- `ReportPanel` (totals + doctor schedules)
- `ReportController` (aggregates across Patient/Doctor/Appointment DAOs)
- `Main.java` — application entry point (wires up DAOs → Controllers → SystemController → LoginFrame)
- `SystemController` — top-level controller (holds sub-controllers + current logged-in user)
- `DialogHelper` (consistent JOptionPane wrapper used by all panels)

### Diagrams
- **Sequence Diagram — Generate Report**

### Documentation
- Module 4 & Module 5 functional docs
- **Video script / shot list** for the team
- Integration doc — how modules wire together

### Video Section
- Section 4: System Demonstration (5–6 min) — live demo
- Section 5: Code Explanation (2 min) — OOP concepts in code
- **Video editing + final cut**

---

## Coordination point: `Doctor extends User`

The `Doctor` class is **both** a `User` subclass (so it can log in) AND the doctor entity (with `doctorId`, `specialization`). This crosses two members:

| Step | Owner | What |
|------|-------|------|
| 1 | Member A | Writes `User` abstract class first (Jun 13–14) and merges to `main` |
| 2 | Member D | Pulls latest `main`, writes `Doctor extends User` with both User fields and doctor-entity fields |
| 3 | Members A + D | PR review before merge — make sure constructor / getters match what `UserDAO` and `DoctorDAO` expect |

`UserDAOImpl.findByUsername` joins `users` ↔ `doctors` on `user_id` and constructs the right subclass — Member A owns that JOIN query.

---

## Shared Responsibilities (everyone contributes)

| Item | Notes |
|------|-------|
| Class Diagram input | A/C/D each send their classes to Member B |
| Code reviews | Pair-review any cross-module touch (e.g. appointment ↔ doctor) |
| Final testing | All 4 run the system end-to-end before submission |
| ZIP packaging | Whoever submits — but content review by all |
| Video appearance | All 4 visible on camera at minimum during Intro |

---

## Workload Balance Check

| Member | Code Load | UML Load | Doc Load | Video Load |
|--------|:---------:|:--------:|:--------:|:----------:|
| A | Medium (Login + User hierarchy + DB connection) | Medium (Use Case + 1 Seq) | Medium (README) | Light (Intro) |
| B | Medium (Patient module) | **Heavy** (Class Diagram + 1 Seq) | Light | Medium (Class explanation) |
| C | Medium-Heavy (Appointment + DB schema) | Light (1 Seq) | Medium (DB + module doc) | Medium (Sequence explanation) |
| D | **Heavy** (Doctor + Reporting + Main + Controller) | Light (1 Seq) | Medium (script + integration) | **Heavy** (Demo + Code + editing) |

Rationale: B leads the Class Diagram (biggest UML deliverable) but has the lightest code load. D has the heaviest code load but the simplest UML. A and C sit in between.

---

## Timeline Sync Points (refer to `planning.md` §9)

| Date | Sync Goal |
|------|-----------|
| Jun 10 | A finishes DatabaseConnection + C finishes `schema.sql` → everyone can start DAOs |
| Jun 12 | B circulates first Class Diagram draft for review |
| Jun 18 | A's login + B's patient module integrated (login flow can reach patient panel) |
| Jun 21 | C's appointment + D's doctor integrated (booking works end-to-end) |
| Jun 23 | D's reporting reads real data |
| Jun 24 | All 4 sequence diagrams done |
| Jun 26 | Full system test, all 4 members present |
| Jun 27–28 | Record + edit video |
| Jun 29 | Submit (by morning, not 11:59 pm) |

---

## Decision Log

- [x] Architecture: **Approach 1 — MVC + DAO** (no Service layer)
- [x] Database: **SQLite** (bundled jar in `lib/`)
- [x] Build: manual `javac` + bundled `lib/` (no Maven/Gradle)
- [x] Polish level: **Solid coursework** (hashing + validation + soft-delete)
- [x] Git workflow: **GitHub feature branches → PRs into `main`**
- [ ] Member names assigned to A / B / C / D
- [ ] Confirm SQLite acceptable with lecturer (next class)
- [ ] Confirm hashing acceptable / not required as plaintext for marking
- [ ] Confirm whether ER Diagram is also lecturer-required (would shift to Member C if yes)
- [ ] Confirm all 4 members have Java 17 installed (minimum: Java 11)
