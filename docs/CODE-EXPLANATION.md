# MEDIS — Code Explanation

A complete walkthrough of the codebase: what every class does, how the layers
talk to each other, and where each OOP concept lives. Use this as the script for
**video Section 5 (Code Explanation)**.

---

## 1. The big picture — 4-layer MVC + DAO

```
   USER clicks a button
        │
        ▼
   VIEW (Swing)          ── what the user sees ─────────────────────
        │  calls
        ▼
   CONTROLLER            ── validates input + decides what to do ───
        │  calls
        ▼
   DAO                   ── reads/writes the database (SQL) ────────
        │  JDBC
        ▼
   SQLite (db/medis.db)  ── where data actually lives ──────────────

   MODEL objects (Patient, Doctor, ...) flow up and down all layers —
   they are plain data carriers and hold no logic.
```

**Why this split?** Each layer has one job. You can change the database (DAO)
without touching the screens (View). You can redesign a screen without touching
business rules (Controller). This is the "clean architecture" the brief asks for.

Every action follows the **same 5-step shape**:

```
View reads input → Controller validates → DAO runs SQL → returns Result → View shows outcome
```

---

## 2. Package map

| Package | Responsibility | Classes |
|---------|----------------|---------|
| `model` | data blueprints (no logic) | `User`, `Admin`, `Receptionist`, `Doctor`, `Patient`, `Appointment`, `Result` |
| `dao` | database access (SQL only) | `UserDAO(+Impl)`, `PatientDAO(+Impl)`, `DoctorDAO(+Impl)`, `AppointmentDAO(+Impl)` |
| `controller` | validation + orchestration | `LoginController`, `PatientController`, `DoctorController`, `AppointmentController`, `ReportController`, `SystemController` |
| `view` | Swing GUI | `LoginFrame`, `DashboardFrame`, `PatientPanel`, `DoctorPanel`, `AppointmentPanel`, `ReportPanel`, `DialogHelper` |
| `db` | one shared DB connection | `DatabaseConnection` |
| `util` | reusable helpers | `Validator`, `PasswordHasher` |
| *(default)* | startup | `Main` |

---

## 3. The four OOP pillars — where to point in the video

| Concept | Where | How |
|---------|-------|-----|
| **Encapsulation** | every `model` class | private fields + public getters/setters; nobody touches data directly |
| **Inheritance** | `User` → `Admin`, `Receptionist`, `Doctor` | subclasses reuse `User`'s fields + constructor via `super(...)` |
| **Polymorphism** | `User.dashboardTitle()` | each subclass overrides it; `DashboardFrame` calls it on a `User` reference without knowing the exact type |
| **Abstraction** | `User` (abstract class) + DAO **interfaces** | callers depend on the contract (`PatientDAO`), not the SQL implementation (`PatientDAOImpl`) |

---

## 4. Model layer (data)

### `User` (abstract)
The base for every account. Holds `userId`, `username`, `passwordHash`, `role`
(all `protected`). Declares one **abstract** method:

```java
public abstract String dashboardTitle();
```

`abstract` means you cannot do `new User(...)` — you must create a concrete role.
This is **abstraction** (you can't instantiate an incomplete concept) and sets up
**polymorphism** (each role answers `dashboardTitle()` differently).

### `Admin`, `Receptionist`
Tiny subclasses. They pass their role string up to `User` and return their own
title:

```java
public class Admin extends User {
    public Admin(int id, String username, String hash) {
        super(id, username, hash, "ADMIN");   // inheritance: reuse parent constructor
    }
    @Override public String dashboardTitle() { return "Admin Dashboard"; }
}
```

### `Doctor` (extends `User`)
Special: a doctor is **both** a login account **and** a medical entity, so it adds
`doctorId`, `name`, `specialization`. It has **two constructors**:
- one for when loaded as a logged-in user (has username + hash + doctor fields),
- one for when loaded as a plain entity (just the doctor fields, no login).

This avoids a separate "DoctorProfile" class and keeps the inheritance clean.

### `Patient`
Plain entity: `patientId`, `name`, `age`, `gender`, `medicalHistory`. Two
constructors — one with an id (loaded from DB), one without (new patient before
the DB assigns an id). All fields private → **encapsulation**.

### `Appointment`
Links a `Patient` + `Doctor` + `LocalDateTime` + a `Status` **enum**
(`SCHEDULED`, `COMPLETED`, `CANCELLED`). The enum makes invalid statuses
impossible at compile time.

### `Result`
The glue for error handling. Instead of throwing exceptions to the GUI,
controllers return a `Result`:

```java
Result.ok()              // success
Result.ok(someData)      // success + payload
Result.fail("message")   // failure with a user-friendly reason
```

The constructor is `private` — you build one only via these factory methods. All
fields `final` → immutable and safe to pass around.

---

## 5. DAO layer (database access)

Each table has an **interface** + an **implementation**:

```
PatientDAO        (interface — the contract: insert, findById, findAll, update)
PatientDAOImpl    (implementation — the actual SQL via JDBC)
```

**Why the interface?** That's **abstraction**. Controllers hold a `PatientDAO`
and call `dao.insert(p)`. They never see SQL. If we swapped SQLite for MySQL, we'd
write a new `PatientDAOImpl` and nothing in the controllers would change.

### How a DAO method looks

```java
@Override
public Result insert(Patient p) {
    String sql = "INSERT INTO patients(name, age, gender, medical_history) VALUES (?,?,?,?)";
    try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
        ps.setString(1, p.getName());      // PreparedStatement = safe from SQL injection
        ps.setInt(2, p.getAge());
        ps.setString(3, p.getGender());
        ps.setString(4, p.getMedicalHistory());
        ps.executeUpdate();
        // read back the auto-generated id and put it on the object
        try (ResultSet keys = ps.getGeneratedKeys()) {
            if (keys.next()) p.setPatientId(keys.getInt(1));
        }
        return Result.ok(p);
    } catch (SQLException e) {
        return Result.fail("Database error: " + e.getMessage());
    }
}
```

Key points:
- **`PreparedStatement` with `?`** placeholders prevents SQL injection.
- DAO **catches `SQLException`** and converts it to a `Result` — the upper layers
  never deal with raw exceptions.

### `UserDAOImpl` — polymorphism on load
`findByUsername` runs one `LEFT JOIN` of `users` and `doctors`, then builds the
**right subclass** based on the role column:

```java
switch (role) {
    case "ADMIN":        return new Admin(...);
    case "RECEPTIONIST": return new Receptionist(...);
    case "DOCTOR":       return new Doctor(...);   // includes doctor_id, name, specialization
}
```

The method's return type is `User`, but the real object is the correct subclass —
**polymorphism** in action.

### `AppointmentDAOImpl` — the duplicate-slot guard
Two important details:
1. A shared formatter `FMT = "yyyy-MM-dd HH:mm"` is used for **storing and looking
   up** datetimes, so comparisons always match.
2. `existsByDoctorAndTime(doctorId, dt)` runs
   `SELECT COUNT(*) ... WHERE doctor_id=? AND appointment_datetime=? AND status='SCHEDULED'`.
   If it returns > 0, the slot is taken.

There's also a **database-level safety net**: `schema.sql` declares
`UNIQUE(doctor_id, appointment_datetime)`. So even if the code check were bypassed,
the DB itself rejects a double booking.

---

## 6. Controller layer (logic)

Controllers are the "brain": they **validate** then **call the DAO**. (We chose
Approach 1 — MVC + DAO — so there is no separate Service layer; the controller
does both.)

### `LoginController`
```java
public Result authenticate(String username, String password) {
    if (!Validator.isNotBlank(username)) return Result.fail("Username is required");
    if (!Validator.isNotBlank(password)) return Result.fail("Password is required");

    Optional<User> u = userDAO.findByUsername(username);
    if (u.isEmpty()) return Result.fail("Invalid username or password");

    if (!PasswordHasher.verify(password, u.get().getPasswordHash()))
        return Result.fail("Invalid username or password");

    return Result.ok(u.get());
}
```
Note: same generic message for "user not found" and "wrong password" — you don't
leak which usernames exist.

### `PatientController` / `DoctorController`
Validate fields (non-blank name, age 0–150, gender, specialization) using
`Validator`, then delegate to the DAO. Return whatever `Result` the DAO gives.

### `AppointmentController` — the most logic
```java
public Result book(int patientId, int doctorId, LocalDateTime dt) {
    if (!Validator.isFutureDate(dt))            return Result.fail("Date must be in the future");
    if (patientDAO.findById(patientId).isEmpty()) return Result.fail("Please select a patient");
    if (doctorDAO.findById(doctorId).isEmpty())   return Result.fail("Please select a doctor");
    if (apptDAO.existsByDoctorAndTime(doctorId, dt)) return Result.fail("Time slot already taken");
    return apptDAO.insert(new Appointment(...));
}
```
`cancel()` does a **soft delete** — it sets status to `CANCELLED` instead of
removing the row, so history is preserved.

### `ReportController`
Aggregates across three DAOs: counts patients, counts appointments, and builds a
`Map<Doctor, List<Appointment>>` for the per-doctor schedule.

### `SystemController` — the top-level hub
Holds references to all the sub-controllers plus the **current logged-in user**.
The whole app is wired through this one object, which gets passed to each Swing
frame:

```java
system.login()        // LoginController
system.patient()      // PatientController
system.appointment()  // AppointmentController
system.getCurrentUser() / setCurrentUser() / logout()
```

---

## 7. View layer (Swing GUI)

### `LoginFrame`
Username + password fields and a Login button. On click it calls
`system.login().authenticate(...)`. On success it stores the user in
`SystemController` and opens `DashboardFrame`; on failure it shows an error dialog.

### `DashboardFrame` — role-based access
Reads `currentUser.getRole()` and only adds the tabs that role is allowed to see:

| Role | Tabs |
|------|------|
| Admin | Patients, Doctors, Appointments, Reports |
| Doctor | Appointments, Patients |
| Receptionist | Patients, Appointments |

This is **role-based access control** done in the UI.

### `PatientPanel`, `DoctorPanel`
A form on top (text fields + dropdown) and a `JTable` below. The Add button reads
the form, calls the controller, shows a dialog, and refreshes the table. The table
is read-only (`isCellEditable` returns `false`).

### `AppointmentPanel`
Two `JComboBox` dropdowns (patients, doctors) + a datetime text field. Book and
Cancel buttons call the controller. Uses the same `FMT` datetime format as the DAO
so what you type matches how it's stored.

### `ReportPanel`
Shows total patients / total appointments in bold labels, plus a table of every
doctor's schedule.

### `DialogHelper`
One small utility so every popup looks consistent:
`showError`, `showInfo`, `confirm`. The panels never call `JOptionPane` directly.

---

## 8. Supporting classes

### `DatabaseConnection` (Singleton)
Exactly **one** database connection for the whole app:

```java
DatabaseConnection.getInstance().get()   // always the same Connection
```

- Constructor is `private` → nobody can make a second one.
- On first run (when `db/medis.db` doesn't exist) it executes `schema.sql` to
  create the tables and seed data.
- Turns on `PRAGMA foreign_keys = ON` so foreign keys are enforced.

### `Validator`
Three pure functions, fully unit-tested: `isNotBlank`, `isAgeValid` (0–150),
`isFutureDate`. Pure = no side effects, easy to test.

### `PasswordHasher`
SHA-256 hashing. `hash(plain)` returns a 64-char hex string; `verify(plain, hash)`
re-hashes and compares. Passwords are **never** stored in plain text — `schema.sql`
holds hashes, and login hashes the typed password before comparing.

### `Main` (entry point)
Wires the whole object graph together, then shows the login window:

```
check JDBC driver is present (fail fast if not)
set native look-and-feel
build DAOs  →  build Controllers (inject DAOs)  →  build SystemController
SwingUtilities.invokeLater( show LoginFrame )
```

`SwingUtilities.invokeLater` makes sure the GUI is built on the Swing event thread
(the correct, thread-safe way to start a Swing app).

---

## 9. Event handling (what the brief asks to demonstrate)

Every button uses an `ActionListener`, attached with a lambda:

```java
JButton addBtn = new JButton("Add Patient");
addBtn.addActionListener(e -> doAdd());   // lambda = the ActionListener
```

When clicked, `doAdd()` runs: read form → call controller → show dialog → refresh
table. That is the View → Controller → DAO → Result → View loop from §1, triggered
by a real Swing event.

---

## 10. End-to-end example: booking an appointment

```
1. Receptionist picks Alice + Dr. Lee + "2026-08-01 09:00", clicks Book
        AppointmentPanel.doBook()
2. Panel parses the datetime, calls
        system.appointment().book(patientId, doctorId, dt)
3. AppointmentController:
        - Validator.isFutureDate(dt)?            ok
        - patientDAO.findById(patientId)?        exists
        - doctorDAO.findById(doctorId)?          exists
        - apptDAO.existsByDoctorAndTime(...)?    not taken
        - apptDAO.insert(new Appointment(...))
4. AppointmentDAOImpl runs INSERT (PreparedStatement); DB UNIQUE constraint is the
   final guard against double-booking. Returns Result.ok().
5. Panel sees Result.isOk(), shows "Appointment booked", refreshes the table.
```

If the slot were taken, step 3 returns `Result.fail("Time slot already taken")`
and step 5 shows that as an error dialog instead. Same flow, different branch.
