# Architecture: Approach 1 vs Approach 2 — Made Simple

## The ONLY difference in one sentence

> **Approach 2 adds one extra class (Service) between Controller and DAO. Approach 1 does not.**

That's it. Same UI, same database, same models. The only question is: **do you want a Service layer in the middle?**

---

## Visual

```
                  USER CLICKS "ADD PATIENT" BUTTON
                              │
                              ▼
┌─────────────────────────────────────────────────────────┐
│                      APPROACH 1                          │
│                                                          │
│   PatientPanel ──► PatientController ──► PatientDAO ──► DB
│      (View)          (validates +            (SQL)        │
│                       talks to DB)                        │
└─────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────┐
│                      APPROACH 2                          │
│                                                          │
│   PatientPanel ──► PatientController ──► PatientService ──► PatientDAO ──► DB
│      (View)          (just passes         (validates +         (SQL)        │
│                       it on)               talks to DAO)                    │
└─────────────────────────────────────────────────────────┘
                                            ▲
                                            │
                                       extra step
```

---

## Same scenario, two implementations

**Scenario:** Receptionist types `"John Doe", 30, "M"` and clicks **Add Patient**.

---

### APPROACH 1 — Controller does the work

**3 files involved:**

```java
// 1) PatientPanel.java  (View)
addButton.addActionListener(e -> {
    String name = nameField.getText();
    int age = Integer.parseInt(ageField.getText());
    String gender = genderBox.getSelectedItem().toString();

    Result r = controller.addPatient(name, age, gender);

    if (r.isOk()) showSuccess("Patient added");
    else          showError(r.message());
});
```

```java
// 2) PatientController.java  (validates + saves)
public class PatientController {
    private final PatientDAO dao;

    public Result addPatient(String name, int age, String gender) {
        // ── validation ──
        if (name.isBlank())       return Result.fail("Name required");
        if (age < 0 || age > 150) return Result.fail("Invalid age");

        // ── save ──
        Patient p = new Patient(name, age, gender);
        dao.insert(p);
        return Result.ok();
    }
}
```

```java
// 3) PatientDAO.java  (SQL only)
public class PatientDAO {
    public void insert(Patient p) {
        String sql = "INSERT INTO patients(name, age, gender) VALUES (?, ?, ?)";
        // ... PreparedStatement code ...
    }
}
```

### Flow (Approach 1)
```
User clicks button
   ↓
Panel reads form fields
   ↓
Panel calls   controller.addPatient("John", 30, "M")
   ↓
Controller VALIDATES name and age
   ↓
Controller creates new Patient object
   ↓
Controller calls   dao.insert(patient)
   ↓
DAO runs SQL INSERT
   ↓
Returns Result.ok() → Panel shows "Patient added"
```

**Total hops:** Panel → Controller → DAO → DB. **3 layers between user and DB.**

---

### APPROACH 2 — Service does the work, Controller just passes the call

**4 files involved (one extra):**

```java
// 1) PatientPanel.java  (View) — IDENTICAL to Approach 1
addButton.addActionListener(e -> {
    Result r = controller.addPatient(name, age, gender);
    if (r.isOk()) showSuccess("Patient added");
    else          showError(r.message());
});
```

```java
// 2) PatientController.java  (THIN — just forwards)
public class PatientController {
    private final PatientService service;

    public Result addPatient(String name, int age, String gender) {
        return service.addPatient(name, age, gender);   // ← that's it
    }
}
```

```java
// 3) PatientService.java  (validates + saves)
public class PatientService {
    private final PatientDAO dao;

    public Result addPatient(String name, int age, String gender) {
        // ── validation ──
        if (name.isBlank())       return Result.fail("Name required");
        if (age < 0 || age > 150) return Result.fail("Invalid age");

        // ── save ──
        Patient p = new Patient(name, age, gender);
        dao.insert(p);
        return Result.ok();
    }
}
```

```java
// 4) PatientDAO.java  (SQL only) — IDENTICAL to Approach 1
public class PatientDAO {
    public void insert(Patient p) { /* SQL INSERT */ }
}
```

### Flow (Approach 2)
```
User clicks button
   ↓
Panel reads form fields
   ↓
Panel calls   controller.addPatient("John", 30, "M")
   ↓
Controller calls   service.addPatient(...)          ← extra hop
   ↓
Service VALIDATES name and age
   ↓
Service creates new Patient object
   ↓
Service calls   dao.insert(patient)
   ↓
DAO runs SQL INSERT
   ↓
Returns Result.ok() → bubbles back up through Service → Controller → Panel
```

**Total hops:** Panel → Controller → Service → DAO → DB. **4 layers between user and DB.**

---

## Spot the difference

| | Approach 1 | Approach 2 |
|---|---|---|
| **Where validation lives** | `PatientController` | `PatientService` |
| **Where SQL lives** | `PatientDAO` | `PatientDAO` (same) |
| **What the Controller does** | Validates + calls DAO | Just forwards to Service |
| **Files per module** | 3 (Panel + Controller + DAO) | 4 (Panel + Controller + Service + DAO) |
| **Lines of code (approx)** | ~120 lines × 5 modules = ~600 | ~150 lines × 5 modules = ~750 |

---

## When would Approach 2 actually be worth it?

Only when business logic needs **multiple DAOs**. Example:

> *"When booking an appointment, check the doctor's schedule AND check the patient exists AND check the time isn't in the past AND log an audit entry."*

In Approach 2, the Service does all four checks cleanly. In Approach 1, the Controller would do all four — which makes the Controller bigger.

**For MEDIS:** the most complex check is just *"is this slot taken?"* — one DAO call. Service layer is overkill for that.

---

## TL;DR

- **Same UI, same models, same DAOs, same database.**
- **Approach 2 = Approach 1 + one extra class per module.**
- That extra class only earns its keep if you have complex multi-DAO logic. **MEDIS doesn't.**

---

## Decision

- [ ] Approach 1 — Controller does validation + DAO calls *(simpler, recommended for this scope)*
- [ ] Approach 2 — Service layer in between *(textbook cleaner, but more files)*
