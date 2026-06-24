# UML Diagrams — Content Brief

Short brief of what each diagram must contain for the MEDIS Hospital Management System.

**Total: 6 diagrams**
- 1 Use Case Diagram *(lecturer-mandated, not in PDF)*
- 1 Class Diagram *(PDF requirement)*
- 4 Sequence Diagrams *(PDF requirement)*

---

## 1. Use Case Diagram

**Actors:** Admin, Doctor, Receptionist

| Actor | Key Use Cases |
|-------|---------------|
| Admin | Login, Manage Users, Manage Doctors, View Reports |
| Doctor | Login, View Appointments, View Patient History, Update Notes |
| Receptionist | Login, Add/Update Patient, Book Appointment, View Appointment List |

**Relationships:**
- `<<include>>` → all use cases include **Login**
- `<<extend>>` → *Cancel Appointment* extends *View Appointment List*
- Generalization → Admin/Doctor/Receptionist generalize from base **User** actor

---

## 2. Class Diagram

**Classes (minimum required):** Patient, Doctor, Appointment, User, SystemController

| Class | Key Attributes | Key Methods |
|-------|----------------|-------------|
| User *(abstract)* | userId, username, password, role | login(), logout() |
| Admin / Doctor / Receptionist | inherits User + role-specific fields | role-specific actions |
| Patient | patientId, name, age, gender, medicalHistory | getHistory(), updateInfo() |
| Doctor | doctorId, name, specialization | viewSchedule(), assignAppointment() |
| Appointment | apptId, dateTime, status | create(), cancel(), checkSlot() |
| SystemController | lists of users/patients/doctors/appts | manage*(), route() |

**Relationships:**
- Inheritance → User → Admin / Doctor / Receptionist
- Association → Appointment ↔ Patient (1..1), Appointment ↔ Doctor (1..1)
- Aggregation → SystemController ◇— Patient/Doctor/Appointment lists
- Composition → Doctor ◆— Specialization (if used as value object)

---

## 3. Sequence Diagram — Login Process

**Lifelines:** Actor (User) → LoginFrame → AuthService → UserDAO → Database → DashboardFrame

| Step | Message |
|------|---------|
| 1 | User enters username + password in LoginFrame |
| 2 | LoginFrame → AuthService.authenticate(u, p) |
| 3 | AuthService → UserDAO.findByUsername(u) |
| 4 | UserDAO → Database query → returns User record |
| 5 | AuthService verifies password (hash check) |
| 6 | Return role → LoginFrame routes to correct Dashboard |
| Alt | Invalid → show error dialog, stay on LoginFrame |

---

## 4. Sequence Diagram — Book Appointment

**Lifelines:** Receptionist → AppointmentPanel → AppointmentController → AppointmentService → AppointmentDAO → Database

| Step | Message |
|------|---------|
| 1 | Receptionist selects Patient, Doctor, DateTime |
| 2 | AppointmentPanel → Controller.bookAppointment(...) |
| 3 | Controller → Service.checkDuplicateSlot(doctor, dateTime) |
| 4 | Service → DAO.findByDoctorAndTime(...) → Database |
| 5 | If free → DAO.insert(appointment) → Database |
| 6 | Return success → refresh appointment list |
| Alt | Slot taken → show error, do not insert |

---

## 5. Sequence Diagram — Add Patient

**Lifelines:** Receptionist → PatientPanel → PatientController → PatientService → PatientDAO → Database

| Step | Message |
|------|---------|
| 1 | Receptionist fills patient form (name, age, gender, history) |
| 2 | PatientPanel → Controller.addPatient(data) |
| 3 | Controller → Service.validate(data) |
| 4 | Service → DAO.insert(patient) → Database |
| 5 | Database returns generated patient_id |
| 6 | Return success → refresh patient list / show confirmation |
| Alt | Validation fail → show error on PatientPanel |

---

## 6. Sequence Diagram — Generate Report

**Lifelines:** Admin → ReportPanel → ReportService → PatientDAO + DoctorDAO + AppointmentDAO → Database

| Step | Message |
|------|---------|
| 1 | Admin clicks "Generate Report" in ReportPanel |
| 2 | ReportPanel → ReportService.generate() |
| 3 | Service → PatientDAO.countAll() → Database |
| 4 | Service → AppointmentDAO.countAll() → Database |
| 5 | Service → DoctorDAO.getSchedules() → Database |
| 6 | Service aggregates results into Report object |
| 7 | Return report → ReportPanel displays totals + schedules |

---

## Tooling Recommendation

- **draw.io / diagrams.net** — free, exports PNG + editable `.drawio` source
- Alternatives: Lucidchart, StarUML, Visual Paradigm Community
