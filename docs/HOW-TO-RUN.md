# MEDIS — How to Run & Test

Complete step-by-step guide to building, running, and testing the Hospital
Management System. No prior setup beyond a JDK is required — the SQLite driver
and JUnit runner are bundled in `lib/`.

---

## 1. Prerequisites

| Need | Detail | Check it |
|------|--------|----------|
| **JDK 17+** (min Java 11) | Java compiler + runtime | `java -version` and `javac -version` |
| Nothing else | `sqlite-jdbc` + JUnit jars ship inside `lib/` | — |

> No internet, no Maven, no MySQL install. Everything needed is in the repo.

---

## 2. Project layout (what you're running)

```
src/                          all Java source (flat packages)
├── Main.java                 ← entry point
├── model/  view/  controller/  dao/  db/  util/
lib/                          bundled jars (sqlite-jdbc, junit)
db/
├── schema.sql                tables + seed data (runs on first launch)
└── medis.db                  the SQLite database (auto-created, gitignored)
bin/                          compiled .class files (gitignored)
tests/                        JUnit tests + end-to-end smoke test
```

---

## 3. Run the application

### Option A — Terminal (macOS / Linux)

```bash
# 1. compile everything in src/ into bin/
javac -d bin -cp "lib/*" $(find src -name "*.java")

# 2. run
java -cp "lib/*:bin" Main
```

### Option A — Terminal (Windows)

Windows uses `;` instead of `:` in the classpath, and a different file glob:

```cmd
javac -d bin -cp "lib/*" src\*.java src\model\*.java src\view\*.java src\controller\*.java src\dao\*.java src\db\*.java src\util\*.java
java -cp "lib/*;bin" Main
```

### Option B — VS Code

1. Open the project folder in VS Code (install the **Extension Pack for Java**).
2. The extension auto-detects `src/` as the source root and the jar in `lib/`
   as a referenced library.
3. Open `src/Main.java` and press **F5** (or click **Run** above `main`).

### What you should see

- A **Login** window appears.
- On first launch, `db/medis.db` is created automatically and seeded from
  `db/schema.sql` (you'll have 3 users, 2 doctors, 2 patients, 1 appointment).

---

## 4. Login credentials (seed data)

| Role | Username | Password | After login sees tabs |
|------|----------|----------|------------------------|
| **Admin** | `admin` | `admin123` | Patients, Doctors, Appointments, Reports |
| **Doctor** | `doctor1` | `pass123` | Appointments, Patients |
| **Receptionist** | `recep1` | `pass123` | Patients, Appointments |

Passwords are stored as **SHA-256 hashes**, never plain text.

---

## 5. Manual test walkthrough (the demo script)

Run these in order on a **fresh database** (delete `db/medis.db` first). All 15
should behave exactly as the "Expected" column says. This is also the order to
follow when recording the demo video.

| # | Action | Expected result |
|---|--------|-----------------|
| 1 | Launch app on a fresh DB | Login window appears; `db/medis.db` created |
| 2 | Login `admin` / `admin123` | Dashboard opens with all 4 tabs |
| 3 | Doctors tab → add "Dr. Lee", "Cardiology" | Row appears in doctor table |
| 4 | Patients tab → add "Alice", 30, F, "asthma" | Row appears in patient table |
| 5 | Add patient with **empty name** | Error: "Name is required" |
| 6 | Add patient with **age -1** | Error: "Age must be 0-150" |
| 7 | Appointments → book Alice + Dr. Lee + future date | "Appointment booked"; row appears |
| 8 | Book the **same doctor + same time** again | Error: "Time slot already taken" |
| 9 | Book with a **past date** | Error: "Date must be in the future" |
| 10 | Reports tab | Shows total patients, total appointments, doctor schedule |
| 11 | Logout → login `doctor1` / `pass123` | Dashboard shows only Doctor tabs |
| 12 | Logout → login `recep1` / `pass123` | Dashboard shows only Receptionist tabs |
| 13 | Login with **wrong password** | Error: "Invalid username or password" |
| 14 | Appointments → select a row → Cancel | Status becomes CANCELLED (row stays — soft delete) |
| 15 | Close app, reopen | All data persisted |

**Reset to a clean state any time:**
```bash
rm -f db/medis.db        # next launch rebuilds from schema.sql
```

---

## 6. Automated tests

### Unit tests (JUnit 5) — 19 tests

Cover the `Validator` (input rules) and `PasswordHasher` (SHA-256) classes.

```bash
# compile sources + tests together
javac -d bin -cp "lib/*" $(find src tests -name "*.java")

# run the JUnit console runner
java -jar lib/junit-platform-console-standalone-1.10.2.jar \
     --class-path bin --scan-class-path
```

Expected tail:
```
[        19 tests successful      ]
[         0 tests failed          ]
```

### End-to-end smoke test — 18 checks

`tests/SmokeTest.java` drives the **controller layer** (the same methods the GUI
buttons call) against a real database: login, role auth, CRUD, future-date +
duplicate-slot rejection, soft-cancel, and report totals — no clicking required.

```bash
javac -d bin -cp "lib/*" $(find src tests -name "*.java")
rm -f db/medis.db
java -cp "lib/*:bin" SmokeTest
```

Expected tail:
```
=== 18 passed, 0 failed ===
```

---

## 7. Troubleshooting

| Symptom | Cause | Fix |
|---------|-------|-----|
| `No suitable driver found for jdbc:sqlite` | jar not on classpath | Use the full command with `-cp "lib/*"` (or `lib/*;bin` on Windows) |
| `NoClassDefFoundError: org/slf4j/...` | wrong sqlite-jdbc version | We bundle **3.42.0.0** which needs no SLF4J — make sure that jar is in `lib/` |
| `cannot find symbol` on compile | stale `bin/` | `rm -rf bin && mkdir bin`, then recompile |
| Login fails for everyone | DB seeded with bad hashes | `rm -f db/medis.db` and relaunch to re-seed |
| App data looks wrong/old | leftover `medis.db` | Delete it to start clean |
| `WARNING: ... restricted method ... System::load` | harmless JDK 17+ native-access notice from sqlite-jdbc | Ignore; the app runs fine |

---

## 8. One-shot verification (copy-paste)

Compiles, runs both test suites, and confirms a clean build from scratch:

```bash
rm -rf bin db/medis.db && mkdir bin
javac -d bin -cp "lib/*" $(find src tests -name "*.java") && echo "COMPILE OK"
java -jar lib/junit-platform-console-standalone-1.10.2.jar --class-path bin --scan-class-path | grep -E "tests (successful|failed)"
rm -f db/medis.db && java -cp "lib/*:bin" SmokeTest | tail -1
```
