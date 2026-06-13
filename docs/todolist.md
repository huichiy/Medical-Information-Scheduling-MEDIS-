# MEDIS — Task Progress Tracker

Source of truth for what's done / in progress / pending. Update as you go.

**Today:** 2026-06-13 · **Due:** 2026-06-29 · **16 days left**

Legend: ✅ done · 🟡 in progress · ⬜ pending · ⛔ blocked

---

## Branch state

| Branch | Status | Notes |
|--------|--------|-------|
| `main` | 7 commits ahead of `origin/main` | foundation merged in (unpushed) |
| `member-a-auth` | 1 commit ahead of `main` | User hierarchy committed |
| `notes` | local only | internal planning docs |
| `yap` | pushed | personal branch |

---

## Phase 1 — Foundation (shared)

| # | Task | Owner | Status | Commit |
|---|------|-------|--------|--------|
| 1 | Project skeleton + .gitignore + README | shared | ✅ | `e9e189d` |
| 2 | Bundle SQLite JDBC driver (3.42.0.0) | shared | ✅ | `616836e` + `d09752f` |
| 3 | `db/schema.sql` + seed data | C | ✅ | `0d48d98` |
| 4 | `DatabaseConnection` singleton | A | ✅ | `77c4808` |
| 5 | `Result` helper class | shared | ✅ | `ad1cc71` |

> All 5 foundation tasks **complete**. Merged to `main` as `4e7eea6`.

---

## Phase 2 — Models

| # | Task | Owner | Status | Commit |
|---|------|-------|--------|--------|
| 6 | `User` abstract + `Admin` + `Receptionist` | **A** | ✅ | `35fff21` |
| 7 | `Doctor extends User` (entity + login) | **D** | ⬜ | — |
| 8 | `Patient` model | **B** | ⬜ | — |
| 9 | `Appointment` model + `Status` enum | **C** | ⬜ | — |

---

## Phase 3 — Utilities (TDD)

| # | Task | Owner | Status | Commit |
|---|------|-------|--------|--------|
| 10 | `Validator` + JUnit tests | **A** | ⬜ | — |
| 11 | `PasswordHasher` + JUnit tests | **A** | ⬜ | — |

---

## Phase 4 — DAOs

| # | Task | Owner | Status | Commit | Depends on |
|---|------|-------|--------|--------|-----------|
| 12 | `UserDAO` + `UserDAOImpl` | **A** | ⬜ | — | Task 6 + 7 |
| 13 | `PatientDAO` + impl | **B** | ⬜ | — | Task 8 |
| 14 | `DoctorDAO` + impl | **D** | ⬜ | — | Task 7 |
| 15 | `AppointmentDAO` + impl (with `existsByDoctorAndTime`) | **C** | ⬜ | — | Tasks 13+14 |

---

## Phase 5 — Controllers

| # | Task | Owner | Status | Commit | Depends on |
|---|------|-------|--------|--------|-----------|
| 16 | `LoginController` | **A** | ⬜ | — | Tasks 11, 12 |
| 17 | `PatientController` | **B** | ⬜ | — | Tasks 10, 13 |
| 18 | `DoctorController` | **D** | ⬜ | — | Tasks 10, 14 |
| 19 | `AppointmentController` (duplicate-slot logic) | **C** | ⬜ | — | Tasks 10, 13, 14, 15 |
| 20 | `ReportController` | **D** | ⬜ | — | Tasks 13, 14, 15 |
| 21 | `SystemController` (top-level) | **D** | ⬜ | — | Tasks 16–20 |

---

## Phase 6 — Views

| # | Task | Owner | Status | Commit | Depends on |
|---|------|-------|--------|--------|-----------|
| 22 | `DialogHelper` | D | ⬜ | — | — |
| 23 | `LoginFrame` | **A** | ⬜ | — | Tasks 16, 22, 24 |
| 24 | `DashboardFrame` (role-based tabs) | **A** | ⬜ | — | Task 21 |
| 25 | `PatientPanel` | **B** | ⬜ | — | Task 17 |
| 26 | `DoctorPanel` | **D** | ⬜ | — | Task 18 |
| 27 | `AppointmentPanel` | **C** | ⬜ | — | Task 19 |
| 28 | `ReportPanel` | **D** | ⬜ | — | Task 20 |

---

## Phase 7 — Integration

| # | Task | Owner | Status | Commit |
|---|------|-------|--------|--------|
| 29 | `Main.java` (wires everything together) | **D** | ⬜ | — |
| 30 | Run 15-step smoke test | all 4 | ⬜ | — |
| 31 | README polish + ZIP dry-run | all 4 | ⬜ | — |

---

## Phase 8 — UML, Video, Submission (not in plan as code tasks)

| Item | Owner | Status |
|------|-------|--------|
| Use Case Diagram | **A** | ⬜ |
| Class Diagram (lead) | **B** | ⬜ |
| Sequence — Login | **A** | ⬜ |
| Sequence — Add Patient | **B** | ⬜ |
| Sequence — Book Appointment | **C** | ⬜ |
| Sequence — Generate Report | **D** | ⬜ |
| Video Section 1 — Intro | **A** | ⬜ |
| Video Section 2 — Class Diagram | **B** | ⬜ |
| Video Section 3 — Sequence | **C** | ⬜ |
| Video Section 4+5 — Demo + Code | **D** | ⬜ |
| Video editing & final cut | **D** | ⬜ |
| Final ZIP (`StudentID-Name.zip`) | shared | ⬜ |
| **SUBMIT before 2026-06-29 11:59 pm** | shared | ⬜ |

---

## Open decisions (Decision Log)

- [x] Architecture: Approach 1 — MVC + DAO
- [x] Database: SQLite
- [x] Build: manual `javac` + `lib/`
- [x] Polish level: Solid coursework
- [x] Git workflow: feature branches → PRs into `main`
- [ ] Assign real names to A / B / C / D
- [ ] Confirm SQLite acceptable with lecturer
- [ ] Confirm hashing acceptable
- [ ] Confirm ER Diagram not required
- [ ] Confirm all 4 members have Java 17 (min Java 11)

---

## Per-member checklist

### Member A (you, currently)
- [x] Task 1 — Project skeleton (shared)
- [x] Task 2 — JDBC jar (shared)
- [x] Task 3 — schema.sql (covered for C; C reviews)
- [x] Task 4 — `DatabaseConnection`
- [x] Task 5 — `Result` (shared)
- [x] Task 6 — `User` + `Admin` + `Receptionist`
- [ ] Task 10 — `Validator` + JUnit
- [ ] Task 11 — `PasswordHasher` + JUnit
- [ ] Task 12 — `UserDAO`
- [ ] Task 16 — `LoginController`
- [ ] Task 23 — `LoginFrame`
- [ ] Task 24 — `DashboardFrame`
- [ ] Use Case Diagram
- [ ] Sequence — Login
- [ ] Video Section 1

### Member B
- [ ] Task 8 — `Patient` model
- [ ] Task 13 — `PatientDAO`
- [ ] Task 17 — `PatientController`
- [ ] Task 25 — `PatientPanel`
- [ ] Class Diagram (lead)
- [ ] Sequence — Add Patient
- [ ] OOP concepts mapping doc
- [ ] Video Section 2

### Member C
- [x] Task 3 — schema.sql (already merged; please review)
- [ ] Task 9 — `Appointment` model
- [ ] Task 15 — `AppointmentDAO`
- [ ] Task 19 — `AppointmentController`
- [ ] Task 27 — `AppointmentPanel`
- [ ] Sequence — Book Appointment
- [ ] Video Section 3

### Member D
- [ ] Task 7 — `Doctor extends User` (needs Member A's `User` — ✅ available)
- [ ] Task 14 — `DoctorDAO`
- [ ] Task 18 — `DoctorController`
- [ ] Task 20 — `ReportController`
- [ ] Task 21 — `SystemController`
- [ ] Task 22 — `DialogHelper`
- [ ] Task 26 — `DoctorPanel`
- [ ] Task 28 — `ReportPanel`
- [ ] Task 29 — `Main.java`
- [ ] Sequence — Generate Report
- [ ] Video Section 4 + 5
- [ ] Video editing

---

## Timeline checkpoints

| Date | Goal | Status |
|------|------|--------|
| 2026-06-10 | Foundation ready | ✅ done (Jun 13) — *3 days late* |
| 2026-06-12 | Class Diagram draft | 🟡 not started |
| 2026-06-18 | Login + Patient module integrated | ⬜ |
| 2026-06-21 | Appointment + Doctor integrated | ⬜ |
| 2026-06-23 | Reporting reads real data | ⬜ |
| 2026-06-24 | All 4 sequence diagrams done | ⬜ |
| 2026-06-26 | Full system test, all 4 members present | ⬜ |
| 2026-06-27 → 28 | Record + edit video | ⬜ |
| 2026-06-29 | **SUBMIT (morning)** | ⬜ |
