# MEDIS — Testing Strategy

The PDF rubric doesn't grade tests — but the system still needs to be verified end-to-end before recording the demo video. Lightweight approach.

---

## Two-tier testing

### Tier 1 — Manual smoke test (mandatory)

A scripted walkthrough run by all 4 members **end-to-end on a clean DB** before the video record day. Treat it as a dress rehearsal for the demo.

**Test script (run in this exact order):**

| # | Action | Expected result |
|---|--------|-----------------|
| 1 | Run `Main.java` on a fresh checkout | Login window appears, `db/medis.db` is auto-created with seed data |
| 2 | Login as `admin / admin123` | Dashboard opens; all 5 tabs visible |
| 3 | **Add Doctor** "Dr. Lee", specialization "Cardiology" | Appears in doctor table |
| 4 | **Add Patient** "Alice", 30, "F", history "asthma" | Appears in patient table |
| 5 | Try **Add Patient** with empty name | Error dialog: "Name is required" |
| 6 | Try **Add Patient** with age = -1 | Error dialog: "Age must be 0–150" |
| 7 | **Book Appointment** Alice + Dr. Lee + 2026-07-01 10:00 | Success; appears in appointment table |
| 8 | Try **Book Appointment** same Dr. Lee + same time | Error: "Time slot already taken" |
| 9 | Try **Book Appointment** with date 2025-01-01 (past) | Error: "Date must be in the future" |
| 10 | Open **Reports** | Shows: Total Patients=1, Total Appts=1, Dr. Lee's schedule |
| 11 | Logout, log back in as `doctor1 / pass123` | Dashboard shows only Doctor-visible tabs |
| 12 | Logout, log in as `recep1 / pass123` | Dashboard shows only Receptionist-visible tabs |
| 13 | Try login with wrong password | Error: "Invalid username or password" |
| 14 | Cancel an appointment | Appointment marked CANCELLED (soft delete), still visible in table |
| 15 | Close app, reopen, verify data persisted | All data still there |

Mark each step done in a checklist before video record day. **All 15 must pass.**

### Tier 2 — Optional ad-hoc JUnit tests

If time permits, add small JUnit tests for **two** things that are easy to test without UI:

1. `Validator` methods — pure functions, trivial to test
2. `PasswordHasher` — verify `hash()` + `verify()` round-trip

```java
// Example — ValidatorTest.java
@Test
void isAgeValid_rejectsNegative()  { assertFalse(Validator.isAgeValid(-1)); }
@Test
void isAgeValid_rejectsTooHigh()   { assertFalse(Validator.isAgeValid(151)); }
@Test
void isAgeValid_acceptsNormal()    { assertTrue(Validator.isAgeValid(30)); }
```

Not required, but earns the group a sentence in the video: *"We added unit tests for our validator and password helper."*

---

## Test data (seed)

Comes from `db/schema.sql`. **Predictable** — same data every time the DB is rebuilt.

| Table | Seed rows |
|-------|-----------|
| `users` | `admin/admin123` (ADMIN), `doctor1/pass123` (DOCTOR), `recep1/pass123` (RECEPTIONIST) |
| `doctors` | 2 doctors linked to `doctor1` user (different specializations) |
| `patients` | 2 sample patients with medical history |
| `appointments` | 1 sample scheduled appointment |

→ This means the video demo always starts from the same state. No surprises.

---

## Before submitting

Final pre-flight checklist:

- [ ] Delete `db/medis.db`, run app → DB rebuilt from `schema.sql` cleanly
- [ ] All 15 manual smoke-test steps pass
- [ ] All 4 members can run the project on their own machine from the ZIP
- [ ] Compiles cleanly with `javac -d bin -cp "lib/*" src/com/medis/**/*.java`
- [ ] No `System.out.println` debug noise in production code paths
- [ ] `README.md` has accurate run instructions
