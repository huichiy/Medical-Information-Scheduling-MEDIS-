# MEDIS — Presentation Video Script

**Project:** MEDIS — Medical Information & Scheduling System (Hospital Management System)
**Course:** CCP6224 OOAD
**Stack:** Java Swing + SQLite, MVC + DAO architecture (no external frameworks)
**Target length:** 10–15 min (this script is budgeted at ~15:00)

> **Marking note:** The rubric says *"Must follow EXACT sequence for easy marking."*
> Do **not** reorder sections. Every member must appear on camera (rubric requirement).
> Read the **[NARRATION]** lines aloud; do the **[SCREEN]** actions live.

---

## Timing budget

| # | Section | Time | Speaker |
|---|---------|------|---------|
| 1 | Introduction | 1:00 | _Member A_ |
| 2 | Class Diagram | 3:00 | _Member B_ |
| 3 | Sequence Diagrams | 3:00 | _Member C_ |
| 4 | System Demonstration | 5:30 | _Member D_ |
| 5 | Code Explanation | 2:00 | _Member A/B_ |
| | **Total** | **~14:30** | |

**Before recording — checklist**
- [ ] Reset DB to clean seed: `rm -f db/medis.db`
- [ ] Build: `javac -d bin -cp "lib/*" $(find src -name "*.java")`
- [ ] Open the 6 diagrams (PNG/Mermaid) ready to screen-share for Sections 2–3
- [ ] Have logins ready: `admin/admin123`, `doctor1/pass123`, `recep1/pass123`
- [ ] Run once to warm up: `java -cp "lib/*:bin" Main`
- [ ] Fill in every _Member X_ / [Names], [Tutorial], [Lecturer] placeholder below

---

## SECTION 1 — Introduction (1:00)

**Speaker:** _Member A_ · **On screen:** webcam / title slide

**[SCREEN]** Title slide: "MEDIS — Medical Information & Scheduling System", group members' names + IDs, tutorial section, lecturer name.

**[NARRATION]**
> "Good [morning/afternoon]. We are Group [X] from tutorial section [____].
> Our members are [Name 1], [Name 2], [Name 3], and [Name 4].
>
> Our project is **MEDIS — a Medical Information and Scheduling System**, a desktop
> Hospital Management System built with **Java Swing** for the interface and **SQLite**
> for storage.
>
> MEDIS supports **three user roles** — Admin, Doctor, and Receptionist — and covers
> five core modules: **secure login with role-based access, patient records,
> doctor management, appointment scheduling, and reporting.**
>
> It is built on a clean **layered MVC + DAO architecture**, which we'll walk through
> in the class diagram next. We'll then show three sequence diagrams, give a live demo
> of the working system, and finish by explaining the OOP concepts in our code."

**[TRANSITION]** "Let's start with the class diagram." → hand to _Member B_.

---

## SECTION 2 — Class Diagram (3:00)

**Speaker:** _Member B_ · **On screen:** `class.png` (from `yap diagram/`)

### 2a. Explain the design (~1:00)

**[NARRATION]**
> "MEDIS uses a strict **four-layer architecture**. Data flows **down** through the
> layers and results flow back **up**:
>
> **View → Controller → DAO → Database.**
>
> - The **view** layer is pure Java Swing — frames and panels. It never talks to the
>   database directly.
> - The **controller** layer holds validation and orchestration. `SystemController` is
>   the central hub that wires every sub-controller together.
> - The **DAO** layer — Data Access Object — is the only layer that runs SQL.
> - **Model** objects like `Patient`, `Doctor`, `Appointment`, and `User` carry data
>   across every layer; they hold no business logic.
>
> We deliberately kept it to these four layers — no extra Service layer — because the
> system is small enough that an extra layer would add complexity without benefit."

### 2b. Show the relationships (~1:15)

**[SCREEN]** Point to each relationship on the diagram as you say it.

**[NARRATION]**
> "Now the relationships the rubric asks for:
>
> **Inheritance** — `User` is an **abstract** base class. `Admin`, `Receptionist`, and
> `Doctor` all **extend** `User`. This is shown by the hollow-triangle arrows.
>
> **`Doctor` is special** — it extends `User` *and* doubles as the doctor entity,
> holding `doctorId` and `specialization`. It has two constructors: one for a doctor
> with a login account, one for a doctor record without one.
>
> **Association** — `SystemController` *has-a* reference to each controller
> (`LoginController`, `PatientController`, `DoctorController`, `AppointmentController`,
> `ReportController`), and it holds the currently logged-in `User`.
>
> **Aggregation / Composition** — each controller holds its DAO **interface**, and an
> `Appointment` references a `Patient` and a `Doctor` by their IDs.
>
> **Abstraction via interfaces** — every DAO is an **interface + implementation pair**,
> for example `PatientDAO` and `PatientDAOImpl`. Controllers depend only on the
> interface, never on the concrete class."

### 2c. Justify the structure (~0:45)

**[NARRATION]**
> "Why this structure? Three reasons.
>
> First, **separation of concerns** — if we change the GUI, the controllers and DAOs
> don't change; if we swap SQLite for another database, only the DAO implementations
> change because controllers depend on the interfaces.
>
> Second, it cleanly demonstrates all **four OOP pillars** — encapsulation in the
> models, inheritance in the `User` hierarchy, polymorphism when we load the right
> user subclass, and abstraction through the DAO interfaces.
>
> Third, **consistency** — every controller method returns a `Result` object, so the
> view always handles success and failure the same way."

**[TRANSITION]** "Now let's trace how these classes interact at runtime." → _Member C_.

---

## SECTION 3 — Sequence Diagrams (3:00)

**Speaker:** _Member C_ · **On screen:** swap between `seq_login.png`, `seq_book.png`, `seq_addpatient.png`

> The rubric lists three flows: **Login**, **Appointment**, **Record management**.
> (We also have a Report sequence diagram — mention it exists, show if time allows.)

### 3a. Login flow (~1:00)

**[SCREEN]** Show `seq_login.png`.

**[NARRATION]**
> "First, **Login**. The actor enters a username and password in `LoginFrame` and
> clicks Login.
>
> `LoginFrame` calls `LoginController.authenticate(user, pass)`. The controller first
> runs `Validator.isNotBlank` to reject empty input.
>
> It then calls `UserDAO.findByUsername`, which runs a SQL `SELECT` with a
> **`LEFT JOIN` on the doctors table**. Based on the `role` column, the DAO
> **instantiates the correct subclass** — `Admin`, `Doctor`, or `Receptionist`. This
> is our **polymorphism** in action.
>
> The controller verifies the password with `PasswordHasher.verify` — passwords are
> stored as **SHA-256 hashes**, never plaintext. On success it returns
> `Result.ok(user)` and `DashboardFrame` opens with role-based tabs. On failure it
> returns `Result.fail` and a dialog shows the error."

### 3b. Book Appointment flow (~1:00)

**[SCREEN]** Show `seq_book.png`.

**[NARRATION]**
> "Second, **Book Appointment**. In `AppointmentPanel`, the user picks a patient, a
> doctor, and a date-time, then clicks Book.
>
> `AppointmentController.book` validates in order: the date must be in the **future**
> (`Validator.isFutureDate`), the patient and doctor must exist, and crucially the slot
> must be free — it calls `existsByDoctorAndTime` to **prevent double-booking**.
>
> This is **defended twice**: the controller checks for a clash, *and* the database has
> a `UNIQUE(doctor_id, appointment_datetime)` constraint. Only then does the DAO
> `INSERT` the appointment, and a `Result.ok` flows back to refresh the table."

### 3c. Record management — Add Patient (~1:00)

**[SCREEN]** Show `seq_addpatient.png`.

**[NARRATION]**
> "Third, **Record management — adding a patient**. In `PatientPanel`, the user enters
> name, age, gender, and medical history, then clicks Add Patient.
>
> `PatientController.add` validates: the name must not be blank, the age must be
> between 0 and 150, and the gender is required. If valid, it builds a `Patient` model
> and calls `PatientDAO.insert`, which runs a parameterised SQL `INSERT`.
>
> The same controller also supports **update** — selecting a patient row loads it into
> the form, and `Update Selected` calls `PatientController.update` to save changes.
> Every path returns a `Result`, so the view shows a success or error dialog uniformly."

**[TRANSITION]** "Let's see all of this running live." → _Member D_.

---

## SECTION 4 — System Demonstration (5:30)

**Speaker:** _Member D_ · **On screen:** the running application (`java -cp "lib/*:bin" Main`)

> **Tip:** Make the window large enough that the **Book** button isn't clipped.
> Narrate every click. Keep mouse movements slow and deliberate.

### 4a. Login + role-based access (~1:15)

**[SCREEN]** App launches at `LoginFrame`.

**[NARRATION + ACTIONS]**
> "Here is the login screen.
> - First I'll show validation — I click **Login** with empty fields → notice the error
>   dialog: *username and password are required.*"
>
> **[Type wrong password]** "Now a wrong password → *invalid credentials.* The password
> is checked against a stored SHA-256 hash."
>
> **[Login as `admin` / `admin123`]** "Now I log in as **Admin**. Notice the Admin sees
> **all four modules** — Patients, Doctors, Appointments, and Reports — because access
> is **role-based.** A Doctor or Receptionist would see a reduced set of tabs."

### 4b. Add patient (~1:15)

**[SCREEN]** Patients tab.

**[NARRATION + ACTIONS]**
> "In the **Patients** tab I'll add a new patient.
> Name: *Sarah Lim*, Age: *29*, Gender: *F*, History: *mild asthma.*
> I click **Add Patient** → success dialog, and the new record appears in the table below.
>
> To show **update**, I click Sarah's row — her details load into the form. I change the
> history to *asthma, stable*, click **Update Selected** → the table updates.
> The **Clear** button resets the form for the next entry."

### 4c. Book appointment + duplicate prevention (~1:30)

**[SCREEN]** Appointments tab.

**[NARRATION + ACTIONS]**
> "Now the **Appointments** tab. I select patient *Sarah Lim*, doctor *Dr. Smith*,
> and a future date-time, then click **Book** → the appointment appears in the list.
>
> Now the key feature — **duplicate-slot prevention.** I try to book the **same doctor
> at the same time** again → the system blocks it: *that slot is already taken.*
>
> I'll also **cancel** an appointment — I select the row and click **Cancel Selected.**
> Notice it disappears from the list. Internally this is a **soft delete** — the row is
> kept with status CANCELLED, and that time slot becomes bookable again."

### 4d. View report (~1:00)

**[SCREEN]** Reports tab.

**[NARRATION + ACTIONS]**
> "Finally the **Reports** tab. I click **Refresh Report.**
> It shows **total patients** and **total appointments**, and a **per-doctor schedule**
> — each doctor with their booked appointments. These numbers are computed live from
> the database through the `ReportController`."

**[TRANSITION]** "Let's look at the code behind this." → _Member A/B_.

---

## SECTION 5 — Code Explanation (2:00)

**Speaker:** _Member A or B_ · **On screen:** IDE / editor showing the named files

### 5a. OOP concepts used (~1:10)

**[SCREEN]** Open `model/User.java`, then `dao/PatientDAO.java` + `PatientDAOImpl.java`.

**[NARRATION]**
> "Our code demonstrates all **four OOP pillars**:
>
> **1. Encapsulation** — every model field is `private` with getters; for example
> `Patient` exposes `getName()`, `getAge()`. Data is never accessed directly.
>
> **2. Inheritance** — `User` is `abstract`; `Admin`, `Receptionist`, and `Doctor`
> extend it and reuse its shared fields like `username` and `role`.
>
> **3. Polymorphism** — in `UserDAOImpl.findByUsername`, we read the `role` column and
> return the correct subclass as a `User`. The caller treats them all as `User` but the
> right concrete type is created at runtime.
>
> **4. Abstraction** — every DAO is an **interface**, like `PatientDAO`, with a separate
> `PatientDAOImpl`. Controllers depend on the interface, so the implementation can change
> without touching the controllers."

### 5b. Key classes (~0:50)

**[SCREEN]** Briefly show `controller/SystemController.java`, `model/Result.java`, `db/DatabaseConnection.java`.

**[NARRATION]**
> "A few key classes that hold the system together:
>
> - **`SystemController`** is the single wiring hub — it owns every sub-controller and the
>   logged-in user, and `Main` builds the whole object graph through it.
> - **`Result`** is our return-value pattern: `Result.ok()`, `Result.ok(data)`, and
>   `Result.fail(message)`. Controllers **never throw** to the view — the view just checks
>   `isOk()` and shows the message. DAOs catch `SQLException` and convert it to a failed
>   `Result`.
> - **`DatabaseConnection`** is a **singleton** holding one shared SQLite connection; on
>   first launch it runs `schema.sql` to create and seed the tables.
> - **`Validator`** and **`PasswordHasher`** are reusable utilities for input validation
>   and SHA-256 hashing.
>
> Together these give us a clean, layered, and maintainable system where each concern
> lives in exactly one place.
>
> That concludes our presentation of MEDIS. Thank you."

---

## Appendix — quick reference for the demo

**Seed logins**

| Role | Username | Password |
|------|----------|----------|
| Admin | `admin` | `admin123` |
| Doctor | `doctor1` | `pass123` |
| Receptionist | `recep1` | `pass123` |

**Role → visible tabs**

| Role | Tabs |
|------|------|
| Admin | Patients · Doctors · Appointments · Reports |
| Doctor | Appointments · Patients |
| Receptionist | Patients · Appointments |

**Commands**

```bash
# Reset DB to clean seed
rm -f db/medis.db
# Build
javac -d bin -cp "lib/*" $(find src -name "*.java")
# Run
java -cp "lib/*:bin" Main
```

**Datetime format for booking:** `yyyy-MM-dd HH:mm` (e.g. `2026-07-01 14:00`)

**Diagrams to screen-share:** `class.png`, `seq_login.png`, `seq_book.png`,
`seq_addpatient.png` (and `seq_report.png`, `usecase.png` if asked) — all in `yap diagram/`.
