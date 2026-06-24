# MEDIS — Error Handling

How errors are caught and shown to the user. **Three layers of defense, all using the `Result` helper class.**

---

## The `Result` class — single error-passing convention

```java
public class Result {
    private final boolean ok;
    private final String message;
    private final Object data;   // optional payload

    public static Result ok()                    { return new Result(true, null, null); }
    public static Result ok(Object data)         { return new Result(true, null, data); }
    public static Result fail(String message)    { return new Result(false, message, null); }

    public boolean isOk()        { return ok; }
    public String  getMessage()  { return message; }
    public Object  getData()     { return data; }
}
```

→ Controllers **always return a `Result`**. Views always check `.isOk()`. **No exceptions thrown to the view layer.**

---

## Layer 1 — Input validation (Controller)

Catches predictable user mistakes **before** touching the database.

| What can go wrong | Controller check | User sees |
|-------------------|------------------|-----------|
| Empty name field | `Validator.isNotBlank(name)` | "Name is required" |
| Age = -5 or 200 | `Validator.isAgeValid(age)` | "Age must be 0–150" |
| Past appointment date | `Validator.isFutureDate(dt)` | "Date must be in the future" |
| Patient not selected | null check | "Please select a patient" |
| Doctor not selected | null check | "Please select a doctor" |
| Booking a taken slot | `appointmentDAO.existsByDoctorAndTime(...)` | "Time slot already taken" |
| Wrong login credentials | `passwordHasher.verify(...)` | "Invalid username or password" |

```java
// PatientController.add(...)
if (!Validator.isNotBlank(name)) return Result.fail("Name is required");
if (!Validator.isAgeValid(age))  return Result.fail("Age must be 0–150");
```

---

## Layer 2 — Database errors (DAO)

Catches **system-level** problems. DAO wraps JDBC's `SQLException` so the controller never sees raw exceptions.

```java
// PatientDAOImpl.java
@Override
public Result insert(Patient p) {
    try (PreparedStatement ps = conn.prepareStatement(SQL)) {
        ps.setString(1, p.getName());
        ps.setInt(2, p.getAge());
        // ...
        ps.executeUpdate();
        return Result.ok();
    } catch (SQLException e) {
        return Result.fail("Database error: " + e.getMessage());
    }
}
```

What this catches:

| Problem | User sees |
|---------|-----------|
| DB file missing / locked | "Database error: ..." |
| UNIQUE constraint violation | "Time slot already taken" (caught by Layer 1 too) |
| Disk full | "Database error: ..." |

---

## Layer 3 — View display (DialogHelper)

The View **only** uses `DialogHelper` — never raw `JOptionPane.showMessageDialog`. Keeps message style consistent.

```java
// DialogHelper.java
public class DialogHelper {
    public static void showError(String msg)   { JOptionPane.showMessageDialog(null, msg, "Error",   JOptionPane.ERROR_MESSAGE); }
    public static void showInfo(String msg)    { JOptionPane.showMessageDialog(null, msg, "Info",    JOptionPane.INFORMATION_MESSAGE); }
    public static boolean confirm(String msg)  { return JOptionPane.showConfirmDialog(null, msg, "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION; }
}

// Used in panels like this:
Result r = patientController.add(name, age, gender, history);
if (r.isOk()) {
    DialogHelper.showInfo("Patient added");
    refreshTable();
} else {
    DialogHelper.showError(r.getMessage());
}
```

---

## Special cases

| Scenario | Handling |
|----------|----------|
| **DB file missing on first launch** | `DatabaseConnection` runs `schema.sql` automatically. Logs a `Loading schema...` message at startup. |
| **JDBC driver missing** | `Main.java` catches `ClassNotFoundException` at startup, shows fatal error dialog, exits. |
| **Soft-delete instead of hard-delete** | Cancelling an appointment sets `status='CANCELLED'`. No row is deleted. Avoids foreign-key issues. |
| **Concurrent-edit race condition** | Single-user app on one machine — not addressed (out of scope for coursework). DB UNIQUE constraint covers the only realistic case. |

---

## Logging (light-touch)

Print to stderr in DAO catch blocks for debugging during development:

```java
catch (SQLException e) {
    System.err.println("[PatientDAO.insert] " + e.getMessage());
    return Result.fail("Database error");
}
```

Not a full logging framework — that would be over-engineering for coursework.
