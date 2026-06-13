# MEDIS — Data Flow

End-to-end traces showing exactly how data moves through the system for the three most important user actions.

> Architecture: **Approach 1 (MVC + DAO)** — View → Controller → DAO → Database

---

## Flow 1 — Login (Receptionist logs in)

```
1. User types  username="recep1", password="pass123"  in LoginFrame
   │
2. LoginFrame.loginButton click handler:
   │  calls  loginController.authenticate("recep1", "pass123")
   │
3. LoginController:
   │  - calls  userDAO.findByUsername("recep1")
   │  - DAO runs:  SELECT * FROM users WHERE username=?
   │  - DAO returns a Receptionist object (with hashed password)
   │  - controller calls  passwordHasher.verify("pass123", user.getHashedPassword())
   │  - if match → store user in systemController.setCurrentUser(user)
   │            → return Result.ok(user)
   │  - if no match → return Result.fail("Invalid credentials")
   │
4. LoginFrame receives Result:
   │  - if ok:    new DashboardFrame(systemController).setVisible(true)
   │             loginFrame.dispose()
   │  - if fail:  DialogHelper.showError("Invalid credentials")
   │
5. DashboardFrame opens, checks currentUser.getRole():
      → role = RECEPTIONIST → shows tabs [Patients, Appointments]
```

**Files touched:** `LoginFrame` → `LoginController` → `UserDAO` → `PasswordHasher` → `SystemController` → `DashboardFrame`

---

## Flow 2 — Book Appointment (the most complex flow)

```
1. Receptionist in AppointmentPanel selects:
       patient = "John Doe"   doctor = "Dr. Smith"   datetime = "2026-07-01 10:00"
   │   clicks  [Book Appointment]
   │
2. AppointmentPanel click handler:
   │  reads form → calls  appointmentController.book(patientId, doctorId, dateTime)
   │
3. AppointmentController.book(...):
   │
   │  ── Step 3a: VALIDATE ──
   │     - dateTime in the future?  (Validator.isFutureDate)
   │     - patient exists?          (patientDAO.findById)
   │     - doctor exists?           (doctorDAO.findById)
   │     - any check fails → return Result.fail("...")
   │
   │  ── Step 3b: DUPLICATE-SLOT CHECK ──
   │     - calls  appointmentDAO.existsByDoctorAndTime(doctorId, dateTime)
   │     - DAO runs:
   │         SELECT COUNT(*) FROM appointments
   │         WHERE doctor_id=? AND appointment_datetime=? AND status='SCHEDULED'
   │     - if count > 0 → return Result.fail("Time slot already taken")
   │
   │  ── Step 3c: INSERT ──
   │     - creates new Appointment(patient, doctor, dateTime, "SCHEDULED")
   │     - calls  appointmentDAO.insert(appt)
   │     - DAO runs:  INSERT INTO appointments(...) VALUES (?,?,?,?)
   │     - DB also enforces UNIQUE(doctor_id, appointment_datetime) as safety net
   │     - return Result.ok()
   │
4. AppointmentPanel receives Result:
      - ok:   refresh table by calling appointmentController.getAll()
              DialogHelper.showInfo("Appointment booked")
      - fail: DialogHelper.showError(result.message())
```

**Two-layer defense:** controller check + DB UNIQUE constraint. Either alone would work; both = bullet-proof against race conditions.

---

## Flow 3 — Generate Report (touches multiple DAOs)

```
1. Admin in ReportPanel clicks  [Generate Report]
   │
2. ReportPanel.generateButton click handler:
   │  calls  reportController.generate()
   │
3. ReportController.generate():
   │  - int totalPatients     = patientDAO.findAll().size()
   │  - int totalAppointments = appointmentDAO.findAll().size()
   │  - List<Doctor> doctors  = doctorDAO.findAll()
   │  - for each doctor:
   │       List<Appointment> appts = appointmentDAO.findByDoctor(d.getId())
   │       schedule.put(d, appts)
   │  - returns Report object containing all aggregated data
   │
4. ReportPanel receives Report:
      - updates labels:    "Total Patients: 25"   "Total Appointments: 47"
      - populates JTable with doctor schedules
```

**Files touched:** `ReportPanel` → `ReportController` → `PatientDAO` + `DoctorDAO` + `AppointmentDAO` → builds a `Report` object → back to UI

---

## Universal pattern

Every action in MEDIS follows this same shape:

```
   USER ACTION (click button)
        │
        ▼
   View captures input → calls Controller
        │
        ▼
   Controller validates → calls DAO(s)
        │
        ▼
   DAO talks to SQLite → returns data/result
        │
        ▼
   Controller returns Result to View
        │
        ▼
   View shows success/error → refreshes table
```

If you remember this **5-step shape**, every module is just a re-skin of the same pattern.
