# MEDIS — Folder Structure Explained

What every folder and file in the project is for, in plain language.

---

## Top-level overview

```
Medical-Information-Scheduling-MEDIS-/
├── src/          ← all the Java code you write          (the app)
├── bin/          ← compiled .class files (auto-made)    (the running app)
├── lib/          ← external .jar libraries (bundled)    (the ingredients)
├── db/           ← database file + schema               (the data)
├── tests/        ← automated tests                      (the proof it works)
├── docs/         ← documentation                        (the notes)
├── README.md     ← quick start
└── CLAUDE.md     ← guidance for AI assistants
```

Simple mental model:

| Folder | Analogy |
|--------|---------|
| `src/` | the recipe (what you write) |
| `bin/` | the cooked meal (what gets run) |
| `lib/` | ingredients from the store (jars others made) |
| `db/` | the pantry (where data is stored) |
| `tests/` | the taste-test (checks it came out right) |

---

## `src/` — the source code (the actual application)

All the Java you write lives here, split by responsibility into flat packages
(MVC + DAO architecture). This is what gets submitted and graded.

```
src/
├── Main.java          entry point — starts the whole app
├── model/             data blueprints (no logic)
│   ├── User.java          abstract base for all accounts
│   ├── Admin.java         a User with ADMIN role
│   ├── Receptionist.java  a User with RECEPTIONIST role
│   ├── Doctor.java        a User with DOCTOR role + doctor details
│   ├── Patient.java       patient record
│   ├── Appointment.java   links patient + doctor + time + status
│   └── Result.java        success/failure wrapper passed between layers
├── view/              the Swing GUI (what the user sees)
│   ├── LoginFrame.java       login window
│   ├── DashboardFrame.java   main window, role-based tabs
│   ├── PatientPanel.java     patient screen
│   ├── DoctorPanel.java      doctor screen
│   ├── AppointmentPanel.java appointment screen
│   ├── ReportPanel.java      report screen
│   └── DialogHelper.java     consistent popup dialogs
├── controller/        the logic (validates + decides what to do)
│   ├── LoginController.java
│   ├── PatientController.java
│   ├── DoctorController.java
│   ├── AppointmentController.java
│   ├── ReportController.java
│   └── SystemController.java  top-level hub holding all the above
├── dao/               database access (SQL lives here only)
│   ├── UserDAO.java + UserDAOImpl.java
│   ├── PatientDAO.java + PatientDAOImpl.java
│   ├── DoctorDAO.java + DoctorDAOImpl.java
│   └── AppointmentDAO.java + AppointmentDAOImpl.java
├── db/
│   └── DatabaseConnection.java  opens the SQLite connection (singleton)
└── util/              small reusable helpers
    ├── Validator.java       input checks (name, age, date)
    └── PasswordHasher.java  SHA-256 password hashing
```

> See `CODE-EXPLANATION.md` for how these classes work together.

---

## `bin/` — compiled output (auto-generated)

Holds the `.class` files that `javac` produces from `src/`. The JVM runs these.

- **You never edit it.** `javac` recreates it on every build.
- **Gitignored** — not committed, not submitted. Anyone compiles their own.
- **Safe to delete** anytime: `rm -rf bin` → next build rebuilds it.

```
src/Main.java   ──[ javac -d bin ]──►   bin/Main.class
```

---

## `lib/` — bundled libraries (jars)

External code we depend on, shipped inside the repo so nothing needs installing.

| File | What it does |
|------|--------------|
| `sqlite-jdbc-3.42.0.0.jar` | lets Java talk to the SQLite database |
| `junit-platform-console-standalone-1.10.2.jar` | runs the JUnit tests |

Version 3.42.0.0 of sqlite-jdbc is used on purpose — newer 3.45.x needs an extra
SLF4J jar, this one runs standalone.

---

## `db/` — the database

```
db/
├── schema.sql    SQL that creates the 4 tables + inserts seed data
└── medis.db      the actual SQLite database file (auto-created, gitignored)
```

- On **first launch**, if `medis.db` doesn't exist, the app runs `schema.sql` to
  build and seed it (3 users, 2 doctors, 2 patients, 1 appointment).
- **To reset data to fresh:** `rm -f db/medis.db` → next launch rebuilds it.
- `schema.sql` IS committed/submitted; `medis.db` is NOT (it's generated).

---

## `tests/` — automated tests (optional to submit)

Code that verifies the app works. Not part of the running app, not required by the
brief, but demonstrates good engineering practice.

```
tests/
├── util/
│   ├── ValidatorTest.java       12 unit tests for input validation
│   └── PasswordHasherTest.java   7 unit tests for password hashing
└── SmokeTest.java               18 end-to-end checks of the controller layer
```

Run them after compiling `src` + `tests` together (see `HOW-TO-RUN.md` §6).

---

## `docs/` — documentation

```
docs/
├── README.md                 index of all docs
├── HOW-TO-RUN.md             build, run, login, test — step by step
├── CODE-EXPLANATION.md       class-by-class code walkthrough
├── FOLDER-STRUCTURE.md       this file
├── Lab Exercise 2026.pdf     the assignment brief
├── design/                   architecture & design docs
│   ├── architecture-approaches.md
│   ├── data-flow.md
│   ├── error-handling.md
│   └── diagrams.md           briefs for the 6 UML diagrams
├── project/                  planning & management docs
│   ├── planning.md
│   ├── work-distribution.md
│   ├── todolist.md
│   ├── testing-strategy.md
│   └── question.md
└── superpowers/              internal spec + plan (not for submission)
```

---

## Root files

| File | Purpose |
|------|---------|
| `README.md` | quick project intro + run commands |
| `CLAUDE.md` | guidance for AI coding assistants (architecture, conventions, requirements) |
| `.gitignore` | tells git to skip `bin/`, `db/medis.db`, IDE files; keeps `lib/*.jar` |

---

## What goes in the submission ZIP

✅ include: `src/`, `lib/`, `db/schema.sql`, `README.md`, UML diagrams, video
❌ skip: `bin/` (regenerated), `db/medis.db` (regenerated), `superpowers/` (internal)
