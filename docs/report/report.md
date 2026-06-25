# MEDIS — Hospital Management System
### Object-Oriented Analysis and Design Report

---

## Cover Page

**Course:** CCP6224 — Object-Oriented Analysis and Design
**Assignment:** Lab Exercise (Group Project) — Hospital Management System (HMS)
**System Name:** MEDIS (Medical Information & Scheduling)
**Technology:** Java Swing + SQLite (JDBC)

**Tutorial Section:** _[Tutorial Section]_
**Lecturer / Tutor:** _[Lecturer / Tutor Name]_
**Submission Date:** _[Submission Date]_ (Due: 29 June 2026)

**Group Members:**

| No. | Student Name | Student ID | Role / Contribution |
|-----|--------------|------------|---------------------|
| 1 | _[Student Name 1]_ | _[Student ID 1]_ | _[Contribution]_ |
| 2 | _[Student Name 2]_ | _[Student ID 2]_ | _[Contribution]_ |
| 3 | _[Student Name 3]_ | _[Student ID 3]_ | _[Contribution]_ |
| 4 | _[Student Name 4]_ | _[Student ID 4]_ | _[Contribution]_ |

> _Declaration: This is original group work produced solely by the members listed above, in accordance with the assignment integrity rules._

---

## Table of Contents

1. Introduction
2. Project Background
3. System Overview
4. Functional Requirements
5. Object-Oriented Design
6. Use Case Diagram
7. Class Diagram
8. Sequence Diagrams
   - 8.1 Login Process
   - 8.2 Book Appointment
   - 8.3 Add Patient
   - 8.4 Generate Report
9. System Implementation
10. OOP Concepts Applied
11. System Features
12. Conclusion
13. References
14. Appendices

---

## 1. Introduction

This report documents the analysis, design, and implementation of **MEDIS**, a desktop
**Hospital Management System (HMS)** developed for the CCP6224 Object-Oriented Analysis
and Design lab exercise. The system is built entirely with **Java Swing** for the
graphical user interface and **SQLite** (accessed through JDBC) for persistent storage,
in line with the assignment's instruction to use Java Swing only and avoid external
application frameworks.

The objective of the assignment is to extend prior single-feature Swing exercises into a
cohesive, multi-module application that demonstrates **strong object-oriented design**,
**proper UML modelling**, and a **functional GUI**. MEDIS satisfies these goals through
five integrated modules — User Login, Appointment Booking, Patient Record Management,
Doctor Management, and Basic Reporting — organised under a clean, layered architecture.

This report explains the system's requirements, presents the supporting UML models (one
Use Case diagram, one Class diagram, and four Sequence diagrams), maps each
object-oriented principle directly to the implemented source code, and summarises the
delivered features. All diagrams are derived from the actual submitted source code to
guarantee design–code coherence, which is an explicit criterion in the marking rubric.

## 2. Project Background

Hospitals coordinate large volumes of information — patient records, doctor
specialisations, and appointment schedules — that must remain consistent and accessible
to staff in different roles. A manual or paper-based process is error-prone: double-booked
time slots, lost patient histories, and uncontrolled access to records are common
problems.

MEDIS addresses a simplified version of this domain. It provides three categories of
staff — **Admin**, **Doctor**, and **Receptionist** — with role-appropriate access to the
system. Receptionists register patients and book appointments, doctors review their
schedules and patient histories, and administrators oversee all modules including
reporting. The system enforces core business rules such as **input validation**,
**secure password storage**, and **prevention of duplicate appointment slots**.

The project deliberately models a *real-world* scenario rather than a toy example, in
keeping with the assignment's emphasis on "real-world OO modelling" and the expansion
"from single-feature apps to a multi-module system."

## 3. System Overview

MEDIS is a single-user desktop application launched from the `Main` class. On first
launch it auto-creates and seeds a local SQLite database (`db/medis.db`) from
`db/schema.sql`. The user authenticates through a login window; upon success a role-based
dashboard presents only the tabs the user is permitted to use.

The system follows a strict **four-layer architecture** — a Model–View–Controller (MVC)
arrangement augmented with a **Data Access Object (DAO)** layer for persistence. Control
flows downward (View → Controller → DAO → Database) and results flow back upward, with
plain model objects carrying data across every layer.

```
  view/  ──▶  controller/  ──▶  dao/  ──▶  db/  ──▶  SQLite (db/medis.db)
 (Swing GUI)  (validation +     (interface   (singleton
              orchestration)     + JDBC impl)  connection)

  model/  — Patient, Doctor, Appointment, User, Result — flows across all layers
  util/   — Validator, PasswordHasher — cross-cutting helpers
```

A central `SystemController` acts as the single wiring hub: it holds every sub-controller
and the currently logged-in `User`, and is the only object handed to the Swing frames.
This keeps the views free of any direct database or DAO instantiation.

> **Design note.** Unlike the early planning brief in `docs/design/diagrams.md`, the
> final implementation contains **no separate "Service" layer**. Business logic
> (validation, duplicate-slot checks, orchestration) lives in the controllers, and all
> SQL lives in the DAO implementations. The UML models in this report reflect the
> **actual delivered code**, ensuring full design–code consistency.

## 4. Functional Requirements

The assignment specifies five mandatory modules. The table below maps each requirement to
its implementing classes in the submitted source.

| Module | Required Functions | Implementation |
|--------|--------------------|----------------|
| **M1 — User Login** | Username/password login; role-based access (Admin/Doctor/Receptionist); input validation | `LoginController.authenticate()`, `LoginFrame`, `DashboardFrame` (role-based tabs), `Validator`, `PasswordHasher` |
| **M2 — Appointment Booking** | Create appointment; select patient/doctor/date-time; view list; prevent duplicate slots | `AppointmentController.book()` / `cancel()` / `getAll()`, `AppointmentPanel`, `AppointmentDAOImpl` |
| **M3 — Patient Records** | Add patient; update details; view history; store name/age/gender/history | `PatientController.add()` / `update()` / `getAll()`, `PatientPanel`, `PatientDAOImpl` |
| **M4 — Doctor Management** | Add/view doctors; assign specialisation; link to appointments | `DoctorController.add()` / `getAll()`, `DoctorPanel`, `DoctorDAOImpl` |
| **M5 — Basic Reporting** | Total patients; total appointments; doctor schedules | `ReportController.totalPatients()` / `totalAppointments()` / `doctorSchedules()`, `ReportPanel` |

**Non-functional / technical requirements satisfied:**

- **Java Swing only** — the entire GUI uses `JFrame`, `JPanel`, `JTable`, `JComboBox`,
  `JOptionPane`, etc.; no external UI framework is used.
- **Event handling** — every button is wired with an `ActionListener` lambda (e.g.
  `book.addActionListener(e -> doBook())` in `AppointmentPanel`).
- **Input validation** — centralised in `util.Validator` (blank checks, age 0–150,
  future-date check) and invoked by the controllers.
- **Security** — passwords are stored as **SHA-256 hashes** (`util.PasswordHasher`); the
  database seed stores hashes, never plaintext.
- **Clean architecture** — strict layer separation with DAO interfaces decoupling
  controllers from JDBC.

## 5. Object-Oriented Design

The design organises responsibilities into cohesive packages, each with a single concern:

| Package | Responsibility | Key Classes |
|---------|----------------|-------------|
| `model` | Plain data carriers (entities) and the `Result` envelope | `User` (abstract), `Admin`, `Doctor`, `Receptionist`, `Patient`, `Appointment`, `Result` |
| `view` | Swing GUI and dialogs | `LoginFrame`, `DashboardFrame`, `PatientPanel`, `DoctorPanel`, `AppointmentPanel`, `ReportPanel`, `DialogHelper` |
| `controller` | Validation, business rules, orchestration | `SystemController`, `LoginController`, `PatientController`, `DoctorController`, `AppointmentController`, `ReportController` |
| `dao` | Data access (interface + JDBC implementation pairs) | `UserDAO`/`Impl`, `PatientDAO`/`Impl`, `DoctorDAO`/`Impl`, `AppointmentDAO`/`Impl` |
| `db` | Database connection management | `DatabaseConnection` (singleton) |
| `util` | Cross-cutting helpers | `Validator`, `PasswordHasher` |

**Key design decisions and their rationale:**

1. **Controllers never throw exceptions to the view.** Every controller method returns a
   `model.Result` object (`Result.ok()`, `Result.ok(data)`, or `Result.fail(message)`).
   Views simply check `r.isOk()` and display `r.getMessage()`. This produces a uniform,
   predictable error-handling contract across the whole system.

2. **DAOs are interface + implementation pairs.** Controllers depend on the *interface*
   (e.g. `PatientDAO`), not the JDBC implementation. This is the abstraction seam that
   would allow the SQLite backend to be replaced without touching controller code.

3. **`SystemController` is the composition root.** `Main` builds the object graph
   (DAOs → controllers → `SystemController`) once, and passes the single `SystemController`
   to each frame, avoiding scattered object creation inside the GUI.

4. **A single canonical date-time format.** `AppointmentDAOImpl.FMT` (`yyyy-MM-dd HH:mm`)
   is reused by storage, the duplicate-slot lookup, and the views, guaranteeing that slot
   comparisons match exactly.

## 6. Use Case Diagram

**Figure 1** presents the use case diagram for MEDIS. It captures the three actors and the
functions each may perform within the system boundary.

![Use Case Diagram](assets/usecase.png)

**Figure 1 — MEDIS Use Case Diagram**

**Actors:**

- **Admin** — the privileged staff member. Has access to all four functional areas: managing
  patients, managing doctors, booking/viewing appointments, and viewing reports. In the
  implementation this corresponds to the `Admin` role, whose dashboard shows all four tabs.
- **Receptionist** — front-desk staff. Registers and updates patients and books/views
  appointments, but does not access reports. Maps to the `Receptionist` role (Patients +
  Appointments tabs).
- **Doctor** — clinical staff. Reviews their appointment list and patient records. Maps to
  the `Doctor` role (Appointments + Patients tabs).

**Use cases and rationale:**

| Use Case | Description | Actors |
|----------|-------------|--------|
| Login | Authenticate with username/password; role determines available functions | All |
| Logout | End the session and return to the login screen | All |
| Add / Update Patient | Create or modify a patient record (name, age, gender, history) | Admin, Receptionist |
| View Patient Records | Browse the patient table / history | Admin, Doctor, Receptionist |
| Add / View Doctors | Register a doctor and assign a specialisation | Admin |
| Book Appointment | Select patient, doctor and date-time; system prevents duplicates | Admin, Receptionist |
| View Appointment List | View all appointments in a table | Admin, Doctor, Receptionist |
| Cancel Appointment | Soft-cancel a selected appointment | Admin, Receptionist |
| View Reports | Display totals and doctor schedules | Admin |

**Relationships:**

- **`<<include>>`** — *Add/Update Patient*, *Book Appointment*, and *View Reports* all
  *include* **Login**, because authentication is a mandatory precondition for every
  protected action (enforced in code: the dashboard is only reachable after
  `LoginController.authenticate()` succeeds).
- **`<<extend>>`** — *Cancel Appointment* *extends* *View Appointment List*, because
  cancelling is an optional action taken on a row already shown in the list (in code,
  `AppointmentPanel.doCancel()` operates on the selected table row).

## 7. Class Diagram

**Figure 2** shows the class diagram of the implemented system, grouped by architectural
layer (model = blue, controller = green, DAO interface = red, DAO implementation = pink,
infrastructure/util = light blue).

![Class Diagram](assets/class.png)

**Figure 2 — MEDIS Class Diagram**

**Required classes (per the assignment) and how they appear:**

- **User** *(abstract)* — base class holding `userId`, `username`, `passwordHash`, `role`,
  with the abstract operation `dashboardTitle()`.
- **Admin**, **Receptionist**, **Doctor** — concrete subclasses of `User`.
- **Patient** — entity with `patientId`, `name`, `age`, `gender`, `medicalHistory`.
- **Appointment** — entity holding references to a `Patient` and a `Doctor`, plus
  `dateTime` and a `Status` enumeration.
- **SystemController** — the central controller aggregating all sub-controllers and the
  current `User`.

**Relationships and design rationale:**

| Relationship | Type | Meaning |
|--------------|------|---------|
| `User → Admin / Receptionist / Doctor` | **Inheritance (generalization)** | The three roles specialise the abstract `User`; each overrides `dashboardTitle()`. |
| `Appointment → Patient`, `Appointment → Doctor` | **Association (1)** | Each appointment references exactly one patient and one doctor (object references held as fields). |
| `Appointment ◆— Status` | **Composition** | The `Status` enumeration is owned by, and only meaningful within, an `Appointment`. |
| `SystemController ◇— sub-controllers` | **Aggregation** | `SystemController` holds the five controllers; they are independent objects assembled at startup. |
| `SystemController → User` | **Association** | Holds the current logged-in user (`currentUser`). |
| `*DAOImpl ..▷ *DAO` | **Realization** | Each implementation realises its DAO interface (the abstraction seam). |
| `Controller ⇢ DAO` | **Dependency** | Controllers depend on DAO interfaces; `AppointmentController` depends on three DAOs. |
| `*DAOImpl ⇢ DatabaseConnection` | **Dependency** | Implementations obtain the shared JDBC connection from the singleton. |

A notable design point is that **`Doctor` extends `User` while also serving as the doctor
entity** (it carries `doctorId`, `name`, `specialization`). It provides two constructors —
one for a doctor *with* a login account and one for a doctor record *without* one — which
is why `UserDAOImpl` performs a `users LEFT JOIN doctors` query and instantiates the
correct subtype based on the `role` column.

## 8. Sequence Diagrams

The four sequence diagrams below correspond exactly to the four flows required by the
assignment (Login, Book Appointment, Add Patient, Generate Report). Each diagram traces
the real method calls in the submitted code, preserving the View → Controller → DAO →
Database direction and the `Result` return contract.

### 8.1 Login Process

![Login Sequence Diagram](assets/seq_login.png)

**Figure 3 — Sequence Diagram: Login Process**

When the user submits credentials, `LoginFrame.doLogin()` calls
`LoginController.authenticate()`. The controller first validates that the fields are not
blank (`Validator.isNotBlank`), then asks `UserDAO.findByUsername()` to load the matching
record. `UserDAOImpl` runs a `SELECT ... LEFT JOIN doctors` query and **instantiates the
correct `User` subclass** (`Admin`, `Doctor`, or `Receptionist`) according to the `role`
column — the system's primary example of polymorphism. The controller then verifies the
password against the stored hash via `PasswordHasher.verify()`. On success it returns
`Result.ok(user)`; `LoginFrame` stores the user in `SystemController` and opens the
`DashboardFrame`. On failure (`Result.fail`), `DialogHelper.showError` is shown and the
user stays on the login screen.

### 8.2 Book Appointment

![Book Appointment Sequence Diagram](assets/seq_book.png)

**Figure 4 — Sequence Diagram: Book Appointment**

`AppointmentPanel.doBook()` parses the date-time string using the canonical `FMT` format
and calls `AppointmentController.book()`. The controller enforces the business rules in
order: the date must be in the future (`Validator.isFutureDate`), the patient and doctor
must exist (`findById`), and the slot must be free (`existsByDoctorAndTime`). Only then is
`AppointmentDAO.insert()` invoked. **Duplicate slots are defended twice** — by the
controller's `existsByDoctorAndTime` check *and* by a `UNIQUE(doctor_id,
appointment_datetime)` constraint in the schema, which causes the insert to return
`Result.fail("Time slot already taken")`. Any rule violation short-circuits with a
`Result.fail`, displayed via `DialogHelper.showError`.

### 8.3 Add Patient

![Add Patient Sequence Diagram](assets/seq_addpatient.png)

**Figure 5 — Sequence Diagram: Add Patient**

`PatientPanel.doAdd()` parses the age field (rejecting non-numeric input) and calls
`PatientController.add()`. The controller validates the name, age range (0–150) and
gender, then calls `PatientDAO.insert()`, which performs an `INSERT` with
`RETURN_GENERATED_KEYS` and writes the database-assigned `patient_id` back onto the
`Patient` object. A successful `Result.ok(patient)` triggers an information dialog and a
table refresh; a validation failure returns `Result.fail` and shows an error.

### 8.4 Generate Report

![Generate Report Sequence Diagram](assets/seq_report.png)

**Figure 6 — Sequence Diagram: Generate Report**

`ReportPanel.refresh()` queries `ReportController` for three figures:
`totalPatients()` and `totalAppointments()` (each derived from the corresponding
`findAll().size()`), and `doctorSchedules()`, which iterates over every doctor and collects
their appointments via `AppointmentDAO.findByDoctor()` into a `Map<Doctor,
List<Appointment>>`. The panel then renders the totals into labels and the schedule into a
`JTable`.

## 9. System Implementation

**Technology stack.** Java (Swing for UI, JDBC for data access) with an embedded SQLite
database. The SQLite driver and JUnit runner are bundled in `lib/`, so the project builds
and runs with only a JDK installed — no Maven, Gradle, or network access.

**Persistence.** `db.DatabaseConnection` is a **singleton** holding one shared
`Connection`. On first launch (when `db/medis.db` is absent) it executes `db/schema.sql` to
create the `users`, `patients`, `doctors`, and `appointments` tables and seed demonstration
data. Foreign keys are enabled (`PRAGMA foreign_keys = ON`).

**Application startup (`Main`).** `Main` registers the JDBC driver, applies the system
look-and-feel, constructs the DAOs, injects them into the controllers, assembles the
`SystemController`, and shows the `LoginFrame` on the Swing event-dispatch thread.

**Representative code — duplicate-slot business rule (`AppointmentController.book`):**

```java
public Result book(int patientId, int doctorId, LocalDateTime dt) {
    if (!Validator.isFutureDate(dt))
        return Result.fail("Date must be in the future");
    Optional<Patient> p = patientDAO.findById(patientId);
    if (p.isEmpty()) return Result.fail("Please select a patient");
    Optional<Doctor> d = doctorDAO.findById(doctorId);
    if (d.isEmpty()) return Result.fail("Please select a doctor");
    if (apptDAO.existsByDoctorAndTime(doctorId, dt))
        return Result.fail("Time slot already taken");
    return apptDAO.insert(new Appointment(p.get(), d.get(), dt));
}
```

**Representative code — polymorphic user construction (`UserDAOImpl.mapUser`):**

```java
switch (role) {
    case "ADMIN":        return new Admin(id, un, hash);
    case "RECEPTIONIST": return new Receptionist(id, un, hash);
    case "DOCTOR":       return new Doctor(id, un, hash, doctorId, name, specialization);
    default: throw new SQLException("Unknown role: " + role);
}
```

**Testing.** The project includes JUnit tests for `Validator` and `PasswordHasher`, and an
end-to-end `SmokeTest` that drives the controller layer exactly as the GUI does — covering
login, role authorisation, CRUD, future-date and duplicate-slot rejection, soft-cancel,
and report totals.

## 10. OOP Concepts Applied

Each of the four required OOP pillars is demonstrated directly in the MEDIS source code.

### 10.1 Encapsulation

All entity fields are declared `private` (or `protected` in `User`) and exposed only
through accessor/mutator methods. For example, `Patient` keeps `patientId`, `name`, `age`,
`gender`, and `medicalHistory` private and provides `getName()`, `setAge()`, etc. The
`Result` class goes further: its fields are `private final` and instances can only be
created through the static factory methods `ok()` and `fail()`, making it immutable. The
`DatabaseConnection` singleton hides its `Connection` and constructor, exposing only
`getInstance()` and `get()`.

### 10.2 Inheritance

`User` is an **abstract base class** providing shared state (`userId`, `username`,
`passwordHash`, `role`) and behaviour. `Admin`, `Receptionist`, and `Doctor` each
`extends User`, inheriting that state and supplying role-specific behaviour through their
constructors and overrides. This eliminates duplication across the three roles.

```java
public abstract class User {
    protected int userId; protected String username, passwordHash, role;
    public abstract String dashboardTitle();
}
public class Admin extends User {
    public Admin(int id, String u, String h) { super(id, u, h, "ADMIN"); }
    @Override public String dashboardTitle() { return "Admin Dashboard"; }
}
```

### 10.3 Polymorphism

Polymorphism appears in two concrete forms:

1. **Subtype polymorphism via the abstract method.** `dashboardTitle()` is declared
   abstract in `User` and overridden by each subclass. `DashboardFrame` calls
   `system.getCurrentUser().dashboardTitle()` without knowing the concrete type — the
   correct title is resolved at runtime.
2. **Runtime object substitution in the DAO.** `UserDAOImpl.findByUsername()` returns an
   `Optional<User>` whose actual content may be an `Admin`, `Doctor`, or `Receptionist`,
   selected at runtime from the `role` column (see §10.2 / §8.1). Callers treat all three
   uniformly through the `User` reference.

Method overriding of `toString()` in `Doctor` and `Patient` (used to render combo-box
items) is a further example.

### 10.4 Abstraction

Abstraction is realised through **interfaces** in the DAO layer. `PatientDAO`,
`DoctorDAO`, `AppointmentDAO`, and `UserDAO` declare *what* data operations exist
(`insert`, `findById`, `findAll`, `update`, …) while the `…Impl` classes encapsulate *how*
they are done with JDBC. Controllers program against the interface only:

```java
public class PatientController {
    private final PatientDAO dao;            // depends on the abstraction
    public PatientController(PatientDAO dao) { this.dao = dao; }
}
```

This decouples business logic from the persistence technology and is the seam that would
allow SQLite to be swapped for another store without changing the controllers. The
abstract `User` class is a second form of abstraction, modelling the general concept of a
system user independently of any specific role.

## 11. System Features

| Feature | Description |
|---------|-------------|
| **Role-based access control** | After login, `DashboardFrame` shows only the tabs permitted for the user's role (Admin: all four; Doctor: Appointments + Patients; Receptionist: Patients + Appointments). |
| **Secure authentication** | Passwords are SHA-256 hashed; only hashes are stored and compared. |
| **Input validation** | Blank-field, age-range (0–150), numeric-age, future-date, and date-format checks reject bad input with clear messages. |
| **Duplicate-slot prevention** | Enforced both in the controller (`existsByDoctorAndTime`) and by a database `UNIQUE` constraint. |
| **Soft cancellation** | Cancelling an appointment sets its status to `CANCELLED`; the record is retained for auditability rather than deleted. |
| **Persistent storage** | All data is stored in SQLite and survives application restarts; the database is auto-created and seeded on first run. |
| **Consistent error handling** | The uniform `Result` contract drives every success/error dialog through `DialogHelper`. |
| **Reporting** | Live totals for patients and appointments plus a per-doctor schedule table. |
| **Event-driven UI** | All actions are wired through `ActionListener` lambdas. |

## 12. Conclusion

MEDIS fulfils every functional and technical requirement of the CCP6224 lab exercise. The
five mandated modules are implemented and verified, the GUI is built entirely in Java
Swing with event-driven controls, and persistence is provided by an embedded SQLite
database. The system demonstrates all four object-oriented pillars in code that is
directly traceable to the UML models presented here — abstract `User` and DAO interfaces
(abstraction), the role hierarchy (inheritance), runtime role resolution and overridden
operations (polymorphism), and private state with controlled access (encapsulation).

The deliberately clean, layered architecture (View → Controller → DAO → Database) with a
uniform `Result` contract and a single composition root makes the system coherent,
testable, and extensible. Because every diagram in this report was produced from the
submitted source, the design and implementation remain fully consistent — satisfying the
"Design Coherence" criterion of the marking rubric.

Possible future enhancements include an in-application user-management screen for admins,
finer-grained appointment statuses (e.g. automatic completion), and exportable PDF
reports.

## 13. References

1. Oracle. *Java Platform, Standard Edition Documentation — Java Swing (javax.swing).*
   https://docs.oracle.com/en/java/
2. SQLite Consortium. *SQLite Documentation.* https://www.sqlite.org/docs.html
3. Xerial. *sqlite-jdbc Driver.* https://github.com/xerial/sqlite-jdbc
4. Object Management Group. *Unified Modeling Language (UML) Specification, v2.5.1.*
   https://www.omg.org/spec/UML/
5. Gamma, E., Helm, R., Johnson, R., & Vlissides, J. (1994). *Design Patterns: Elements
   of Reusable Object-Oriented Software.* Addison-Wesley. (Singleton, DAO patterns.)
6. CCP6224 Object-Oriented Analysis and Design — *Lab Exercise 2026* assignment brief and
   marking rubric.

## 14. Appendices

**Appendix A — Build & Run Instructions**

```bash
# Compile all sources into bin/
javac -d bin -cp "lib/*" $(find src -name "*.java")
# Run the application
java -cp "lib/*:bin" Main
```
(On Windows, use `;` instead of `:` in the classpath.)

**Appendix B — Default Login Credentials (seed data)**

| Role | Username | Password |
|------|----------|----------|
| Admin | `admin` | `admin123` |
| Doctor | `doctor1` | `pass123` |
| Receptionist | `recep1` | `pass123` |

**Appendix C — Database Schema (tables)**

- `users(user_id, username, password, role)`
- `patients(patient_id, name, age, gender, medical_history, created_at)`
- `doctors(doctor_id, name, specialization, user_id)`
- `appointments(appointment_id, patient_id, doctor_id, appointment_datetime, status, UNIQUE(doctor_id, appointment_datetime))`

**Appendix D — Automated Tests**

- JUnit unit tests: `util.ValidatorTest`, `util.PasswordHasherTest`.
- End-to-end controller smoke test: `SmokeTest` (login, RBAC, CRUD, duplicate-slot &
  future-date rejection, soft-cancel, report totals).
