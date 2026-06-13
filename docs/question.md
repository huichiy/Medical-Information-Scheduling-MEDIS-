**_ For Model Folder _**

- do the model is something like user role?
- why the user only extends 3 role only: admin, doctor and receptionist?
- the patient is not a role? so basically it just let the main user role like doctor to record the name?

### Answers

**Q: Is the model something like a user role?**
No. A **Model** is a *blueprint for a data object*. It just holds data. A **role** is one *type* of user.

Think of it like this:
- `User.java` (model) = a blueprint that says "every user has a username and password"
- `Admin`, `Doctor`, `Receptionist` (models) = specific *kinds* of users — they inherit from `User`
- `Patient.java` (model) = a blueprint for patient data (name, age, gender, history) — **NOT a user**

So models include: `User`, `Admin`, `Doctor`, `Receptionist`, `Patient`, `Appointment`. Some are users (have roles), some aren't (Patient, Appointment).

**Q: Why only 3 roles — Admin, Doctor, Receptionist?**
Because the **PDF says so** (Section 4, Module 1):
> Role-based access: Admin / Doctor / Receptionist

We're following the PDF exactly. Adding more roles would be scope creep.

**Q: Is Patient a role? Does Doctor record the patient's name?**
Correct — **Patient is NOT a system user**. Patients don't log into MEDIS. The system users are Admin, Doctor, Receptionist. They are the ones who:

- **Receptionist** → adds new patients into the system (registers them)
- **Doctor** → views patient records, adds medical notes
- **Admin** → manages everything

A `Patient` object is just a **data record** stored in the database — like a row in a spreadsheet. No login, no password.

---

**_ For DAO Folder _**

- it is my first time hearing DAO, i think that is something decentralised, but for sure it is different, explain about it
- but from the file structure that you generate, i think it is sql command, but i mention that it is interface, so more bias is for main role user to search the patient name not the developer

### Answers

**Q: What is DAO?**
**DAO = Data Access Object.** Nothing to do with decentralization (you may be thinking of "DAO" in crypto/blockchain — totally different).

DAO is just a **design pattern** that says:
> *"All SQL code for one table lives in one class. Everywhere else in the app, you just call methods like `dao.insert(patient)` — never write SQL directly."*

Plain-English analogy:
- Without DAO → you'd write `INSERT INTO patients...` inside your button click handler. Messy.
- With DAO → you write `patientDAO.insert(patient)` in your button. The SQL lives somewhere else.

**Q: Interface vs SQL — who is it for?**
You're mixing two things. Let me untangle:

- **`PatientDAO.java` (interface)** → for **developers**. It says *"here are the methods you can call to access patient data."* No SQL inside.
- **`PatientDAOImpl.java` (implementation)** → for **developers** too. This is where the actual SQL `INSERT INTO patients...` lives.

```java
// PatientDAO.java — the INTERFACE (just method signatures, no SQL)
public interface PatientDAO {
    void insert(Patient p);
    Patient findById(int id);
    List<Patient> findAll();
}

// PatientDAOImpl.java — the IMPLEMENTATION (real SQL here)
public class PatientDAOImpl implements PatientDAO {
    public void insert(Patient p) {
        String sql = "INSERT INTO patients(name, age, gender) VALUES (?,?,?)";
        // ... JDBC code ...
    }
}
```

**End users (receptionist, doctor) never touch this code.** They click a button in the View → Controller runs → Controller calls `dao.insert(...)`. DAO is purely a developer concept.

**Why use an interface at all?** It demonstrates **Abstraction** — one of the OOP concepts the PDF requires (Section 5).

---

**_ For Controller Folder _**

- for mine perspective, it is logic for the "View folder"
- if wrong, fix me, and give the explanation

### Answers

**You're correct!** The Controller IS the logic behind the View.

To be precise:
- **View** = the buttons, forms, tables (what the user sees)
- **Controller** = "when the user clicks the button, what should happen?" (the brain)
- **DAO** = "how do I actually save/load data?" (the hands)
- **Model** = "what does the data look like?" (the blueprint)

Flow when user clicks "Add Patient":
```
View (button click) → Controller (validates input + decides what to do) → DAO (saves to DB)
```

Your mental model is right. ✅

---

**_ For View Folder _**

- all GUI for this java app with exception

### Answer

**Almost — let me clarify the "exception."**

Yes, the View folder = all GUI for the Java app, built with **Java Swing**:

| File | What it is |
|------|------------|
| `LoginFrame.java` | login window |
| `DashboardFrame.java` | main window after login |
| `PatientPanel.java` | patient management screen |
| `DoctorPanel.java` | doctor management screen |
| `AppointmentPanel.java` | appointment booking screen |
| `ReportPanel.java` | reports screen |
| `DialogHelper.java` | **the "exception"** — not a full screen, just a small helper for popup dialogs (`showError`, `showInfo`) used by all the other panels |

So `DialogHelper` isn't a screen, but it IS GUI-related — that's why it lives in the View folder. Everything in `view/` is about what the user sees.

---

**_ For Db Folder _**

- it is a database handling?
- store data

### Answers

Yes to both — but the **`db/` folder appears in TWO places**, which is confusing. Let me separate them:

**1. `src/com/medis/db/` (inside source code)**
Contains **Java code** that handles the database connection:
- `DatabaseConnection.java` → singleton class that opens the SQLite file and gives out connections to DAOs

**2. `db/` (outside src, at project root)**
Contains **data files** that aren't Java code:
- `schema.sql` → SQL statements that create the tables (run once on first launch)
- `medis.db` → the actual SQLite database file where data is stored

| Layer | What it does |
|-------|--------------|
| `src/com/medis/db/` (code) | "Open the connection to the DB file" |
| `db/` (data) | "The actual data sits here" |

Yes, "store data" is correct — that's exactly what `db/medis.db` does.

---

**_ For Util Folder _**

- PasswordHasher.java is for safety purpose? do it need in our assignment?
- logic of the database

### Answers

**Q: Is PasswordHasher for safety? Do we need it?**
Yes, it's for safety — it hashes passwords so they're not stored as plain text in the database.

**Do we need it?** The PDF doesn't explicitly require hashing. But:
- We chose **"Solid coursework" polish level (Option B)** earlier — which includes hashing
- The rubric criterion *"Design Coherence: Excellent, professional delivery"* benefits from showing security awareness
- It's only ~10 lines of code using Java's built-in `MessageDigest`

→ **Recommended yes, strictly required no.** If the group wants to drop it for time, we can.

**Q: "Logic of the database" — is that what Util does?**
**No, that's a misunderstanding.** Let me untangle:

| Folder | What it does |
|--------|--------------|
| `dao/` | Database **logic** — runs SQL, talks to DB |
| `db/` | Database **connection** — opens the SQLite file |
| `util/` | **General helpers** — reusable small tools that have nothing to do with the database |

Util contains:
- `PasswordHasher.java` → hashing (cryptography helper)
- `Validator.java` → input validation helpers (`isNotBlank`, `isAgeValid`, `isFutureDate`)

These are used by Controllers, not by the DB. Util = "miscellaneous toolbox."

---

**_ Medis Folder _**

- main execution, which is apply the encapsulation

### Answer

Half right — let me clarify:

**`com/medis/` is the root package** of the application. The full package path is `com.medis.model`, `com.medis.controller`, etc. So `medis/` is just the *folder name* that contains everything.

**`Main.java` lives directly in `com/medis/`** and yes — it's the **entry point** (where execution starts when you run the program):
```java
public class Main {
    public static void main(String[] args) {
        // bootstraps DB, creates DAOs, controllers, shows LoginFrame
    }
}
```

**About encapsulation:** encapsulation is applied **throughout the whole codebase**, not specifically in `Main.java`. Every model class uses encapsulation (private fields + getters/setters). `Main.java` itself doesn't really demonstrate encapsulation — it just wires things together.

→ **Correction:** `com/medis/` is the root package; `Main.java` is the entry point. Encapsulation is applied across all model classes, not concentrated here.

---

**_ src folder _**

- not the main code part

### Answer

**This is the opposite of correct — `src/` IS the main code.** ⚠️

In every Java project, the convention is:

| Folder | What it contains |
|--------|------------------|
| `src/` | **ALL the Java source code (`.java` files)** ← this is where you write code |
| `bin/` | Compiled `.class` files (auto-generated when you run `javac`) |
| `lib/` | External `.jar` files (like the SQLite driver) |
| `db/` | Database files (SQL scripts + the SQLite DB) |
| `docs/` | Documentation (like the files we're writing now) |

So `src/` literally stands for **"source code"** — it's THE main code part. Everything inside `src/com/medis/...` is the code you and your group will write.

→ **Correction:** `src/` is where all your Java code lives. It's the most important folder for the actual programming work.

---

## Summary table — what each folder actually does

| Folder | Role | Analogy |
|--------|------|---------|
| `src/com/medis/model/` | Data blueprints | "What does a Patient look like?" |
| `src/com/medis/view/` | GUI screens (Swing) | "What does the user see?" |
| `src/com/medis/controller/` | Logic behind buttons | "What happens when a button is clicked?" |
| `src/com/medis/dao/` | Database operations | "How do I save/load data?" |
| `src/com/medis/db/` | DB connection setup | "How do I open the database file?" |
| `src/com/medis/util/` | General helpers | "Reusable small tools" |
| `src/com/medis/Main.java` | Entry point | "Where the program starts" |
| `lib/` | External libraries | "Code other people wrote (SQLite driver)" |
| `db/` | Data files | "Where the data actually lives" |
| `docs/` | Documentation | "Notes for the group" |
