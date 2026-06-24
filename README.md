# MEDIS — Hospital Management System

CCP6224 Object-Oriented Analysis and Design — group coursework.
Java Swing + SQLite (no other frameworks).

## Requirements

- **Java 17+ JDK** (minimum Java 11)
- No other tools needed — `sqlite-jdbc.jar` is bundled in `lib/`

## Build & Run (terminal)

```bash
javac -d bin -cp "lib/*" $(find src -name "*.java")
java  -cp "lib/*:bin" Main
```

On Windows replace `:` with `;` in the classpath.

## Tests (optional)

JUnit tests for the utility classes and an end-to-end controller smoke test:

```bash
# compile sources + tests
javac -d bin -cp "lib/*" $(find src tests -name "*.java")

# unit tests (Validator, PasswordHasher) — 19 tests
java -jar lib/junit-platform-console-standalone-1.10.2.jar --class-path bin --scan-class-path

# end-to-end smoke test (login, CRUD, booking, duplicate-slot, report) — 18 checks
rm -f db/medis.db && java -cp "lib/*:bin" SmokeTest
```

## VS Code

1. Open this folder in VS Code (Java Extension Pack required).
2. Project Manager will detect `src/` and the JAR in `lib/`.
3. Open `src/Main.java` and press **F5**.

## Default logins (from `db/schema.sql` seed data)

| Role | Username | Password |
|------|----------|----------|
| Admin | `admin` | `admin123` |
| Doctor | `doctor1` | `pass123` |
| Receptionist | `recep1` | `pass123` |

The database file `db/medis.db` is auto-created from `db/schema.sql` on first launch.

## Project layout

```
src/
├── model/         data classes (User, Patient, Doctor, Appointment, ...)
├── view/          Swing GUI (LoginFrame, DashboardFrame, panels)
├── controller/    business logic + validation
├── dao/           data access (interfaces + JDBC implementations)
├── db/            DatabaseConnection (singleton)
├── util/          PasswordHasher, Validator
└── Main.java      entry point (default package)

lib/               sqlite-jdbc + JUnit standalone (bundled)
db/                schema.sql + medis.db (auto-created)
bin/               compiled .class files (gitignored)
docs/              UML diagrams + project documentation
tests/             JUnit tests for Validator + PasswordHasher
```

## Group

Course: CCP6224 OOAD · Tutorial section: [TBD] · Due: **2026-06-29**
