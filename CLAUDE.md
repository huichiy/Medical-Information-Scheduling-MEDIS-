# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

MEDIS is a **Hospital Management System** built for the CCP6224 (OOAD) coursework.
Java Swing GUI + SQLite, no external frameworks. Graded on OOP design, UML, and a
working GUI — so changes must preserve the clean layer separation and the four OOP
pillars the rubric checks (encapsulation, inheritance, polymorphism, abstraction).

## Commands

All commands run from the repo root. The SQLite driver and JUnit runner are bundled
in `lib/` — no Maven/Gradle, no network needed.

```bash
# Build (compile all sources into bin/)
javac -d bin -cp "lib/*" $(find src -name "*.java")

# Run the app
java -cp "lib/*:bin" Main

# Build sources + tests together (needed before running any test)
javac -d bin -cp "lib/*" $(find src tests -name "*.java")

# Run all JUnit tests (Validator + PasswordHasher)
java -jar lib/junit-platform-console-standalone-1.10.2.jar --class-path bin --scan-class-path

# Run a single JUnit test class
java -jar lib/junit-platform-console-standalone-1.10.2.jar --class-path bin --select-class util.ValidatorTest

# End-to-end smoke test (drives the controller layer like the GUI does)
rm -f db/medis.db && java -cp "lib/*:bin" SmokeTest

# Reset the database to a clean seeded state
rm -f db/medis.db   # next launch rebuilds from db/schema.sql
```

On Windows, use `;` instead of `:` in the classpath.

## Architecture

Strict 4-layer **MVC + DAO** (deliberately *no* Service layer — see
`docs/design/architecture-approaches.md` for why). Data flows one direction down,
results flow back up:

```
view/  →  controller/  →  dao/  →  db/  →  SQLite (db/medis.db)
         (validation +    (SQL only,
          orchestration)   interface + Impl)
model/ objects carry data across every layer (no logic in models)
```

**The non-obvious rules that hold this together:**

- **Controllers never throw to the view.** Every controller method returns a
  `model.Result` (`Result.ok()`, `Result.ok(data)`, `Result.fail(msg)`). Views check
  `r.isOk()` and show `r.getMessage()` via `view.DialogHelper`. DAOs catch
  `SQLException` and convert it to `Result.fail(...)`. Keep this convention.

- **DAOs are interface + Impl pairs** (`PatientDAO` / `PatientDAOImpl`). Controllers
  depend on the interface — this is the "abstraction" the rubric wants and the seam
  that would let SQLite be swapped. Don't make controllers touch JDBC directly.

- **`User` is abstract; role subclasses are `Admin`, `Receptionist`, `Doctor`.**
  `Doctor` is special — it extends `User` *and* doubles as the doctor entity (holds
  `doctorId`, `specialization`), with two constructors (with/without a login account).
  `UserDAOImpl.findByUsername` does a `users LEFT JOIN doctors` and instantiates the
  correct subclass from the `role` column — this is the project's polymorphism story.

- **`SystemController` is the single wiring hub.** It holds every sub-controller plus
  the current logged-in `User`. `Main` builds the object graph (DAOs → controllers →
  SystemController) and hands `SystemController` to each Swing frame. Add new features
  by going through it, not by `new`-ing DAOs inside views.

- **`db.DatabaseConnection` is a singleton** holding one shared `Connection`. On first
  launch (when `db/medis.db` is absent) it runs `db/schema.sql` to create tables and
  seed data. To re-seed, delete `db/medis.db`.

- **Appointment datetimes use one canonical format**: `AppointmentDAOImpl.FMT`
  (`yyyy-MM-dd HH:mm`). Storage, the duplicate-slot lookup, the views, and the seed
  data in `schema.sql` must all use it, or `existsByDoctorAndTime` comparisons silently
  fail to match. Reuse `AppointmentDAOImpl.FMT` rather than introducing another format.

- **Duplicate-slot prevention is defended twice**: `AppointmentController.book` checks
  `existsByDoctorAndTime`, AND `schema.sql` has `UNIQUE(doctor_id, appointment_datetime)`.

- **Cancelling is a soft delete** — `updateStatus(..., CANCELLED)`, the row stays.

## Package layout

Flat single-word packages (not `com.medis.*` — a deliberate simplification):
`model`, `view`, `controller`, `dao`, `db`, `util`, and `Main` in the default package.
Tests mirror this under `tests/` (`util.ValidatorTest`, default-package `SmokeTest`).

## Conventions specific to this repo

- **Passwords are SHA-256 hashed** via `util.PasswordHasher`. `schema.sql` stores
  hashes, not plaintext. If you change a seed password, the stored hash must match
  `PasswordHasher.hash(plain)` — `PasswordHasherTest` pins the `admin123`/`pass123`
  hashes precisely to catch this (a wrong seed hash silently blocks login).
- **Input validation lives in `util.Validator`** (`isNotBlank`, `isAgeValid` 0–150,
  `isFutureDate`) and is called from controllers, not views.
- Seed logins: `admin`/`admin123`, `doctor1`/`pass123`, `recep1`/`pass123`.
- `bin/` and `db/medis.db` are gitignored; `lib/*.jar` is force-included.

## Requirements coverage (assignment + rubric)

Source of truth is `docs/Lab Exercise 2026.pdf`. The "lecturer verbal additions"
(database + Use Case diagram) are not in the PDF but are required.

**Functional modules (all implemented in code):**

| Module | Requirement | Where |
|--------|-------------|-------|
| M1 Login | Username/password, role-based access (Admin/Doctor/Receptionist), input validation | `LoginController`, `LoginFrame`, role tabs in `DashboardFrame` |
| M2 Appointment | Create, select patient/doctor/datetime, view list, **prevent duplicate slots** | `AppointmentController.book`, `AppointmentPanel` |
| M3 Patient records | Add, update, view history; store name/age/gender/history | `PatientController`, `PatientPanel`, `patients` table |
| M4 Doctor mgmt | Add/view doctors, assign specialization, link to appointments | `DoctorController`, `DoctorPanel`, `doctors` table |
| M5 Reporting | Total patients, total appointments, doctor schedules | `ReportController`, `ReportPanel` |

**Technical requirements:** Java Swing only ✅ · OOP pillars (see Architecture) ✅ ·
event handling via `ActionListener` lambdas on every button ✅ · clean layered
architecture ✅ · database (SQLite, lecturer-added) ✅.

**Deliverables — submit ONE `StudentID-Name.zip`:**

| Deliverable | Status |
|-------------|--------|
| Java Swing source, organized into packages | ✅ done (`src/`) |
| Database integration (lecturer-added) | ✅ done (`db/schema.sql`) |
| Working system, 5 modules | ✅ done & verified (19 JUnit + 18 smoke) |
| **Use Case diagram** (lecturer-added) | ⬜ not started |
| **Class diagram** — must include Patient, Doctor, Appointment, User, SystemController + relationships | ⬜ not started |
| **4 Sequence diagrams** — Login, Book Appointment, Add Patient, Generate Report | ⬜ not started |
| **Presentation video** 10–15 min, all members on camera, rubric order | ⬜ not started |

Video must follow this exact order (rubric): 1) Intro · 2) Class Diagram ·
3) Sequence Diagrams · 4) System Demo · 5) Code Explanation. Rubric criteria:
Class Diagram, Sequence Diagram, Implementation, Design Coherence, Presentation.

> **6 UML diagrams total** (1 Use Case + 1 Class + 4 Sequence) are the main
> remaining work alongside the video. Briefs in `docs/design/diagrams.md`.

## Where to read more

`docs/` is organized into `design/` (architecture, data-flow, error-handling, diagram
briefs), `project/` (planning, work split, testing strategy), plus top-level
`HOW-TO-RUN.md` and `CODE-EXPLANATION.md`. `docs/design/diagrams.md` specifies the 6
UML diagrams the submission needs.
