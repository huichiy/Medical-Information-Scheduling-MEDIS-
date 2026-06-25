#!/usr/bin/env python3
"""Render the four MEDIS sequence diagrams as PNGs (matplotlib)."""
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
from matplotlib.patches import FancyArrowPatch, Rectangle

HEAD = "#1e293b"; CALL = "#1d4ed8"; RET = "#15803d"; NOTE = "#fde68a"

def render(title, lifelines, steps, outfile):
    n = len(lifelines)
    xs = {name: i * 2.6 for i, name in enumerate(lifelines)}
    n_rows = sum(1 for s in steps if s[0] != "note") + sum(0.6 for s in steps if s[0] == "note")
    height = 1.6 + n_rows * 0.62
    width = 1.2 + (n - 1) * 2.6 + 2.2
    fig, ax = plt.subplots(figsize=(width, height))
    top = n_rows * 0.62 + 0.2

    # lifeline headers + dashed lines
    for name in lifelines:
        x = xs[name]
        ax.add_patch(Rectangle((x - 1.05, top + 0.15), 2.1, 0.62,
                     facecolor="#e2e8f0", edgecolor=HEAD, lw=1.2, zorder=3))
        ax.text(x, top + 0.46, name, ha="center", va="center",
                fontsize=8.5, fontweight="bold", color=HEAD, zorder=4)
        ax.plot([x, x], [-0.1, top + 0.15], color="#94a3b8", ls=(0, (4, 4)), lw=1, zorder=1)

    y = top - 0.25
    for s in steps:
        kind = s[0]
        if kind == "note":
            text = s[1]; targets = s[2]
            lo = min(xs[t] for t in targets) - 1.0
            hi = max(xs[t] for t in targets) + 1.0
            ax.add_patch(Rectangle((lo, y - 0.18), hi - lo, 0.42,
                         facecolor=NOTE, edgecolor="#b45309", lw=0.8, zorder=5))
            ax.text((lo + hi) / 2, y + 0.03, text, ha="center", va="center",
                    fontsize=7.2, style="italic", color="#92400e", zorder=6)
            y -= 0.62 * 1.0 + 0.05
            continue
        frm, to, label = s[1], s[2], s[3]
        color = RET if kind == "return" else CALL
        ls = "--" if kind == "return" else "-"
        x1, x2 = xs[frm], xs[to]
        if kind == "self" or frm == to:
            ax.annotate("", xy=(x1 + 0.05, y - 0.28), xytext=(x1, y),
                        arrowprops=dict(arrowstyle="-", color=color, lw=1.2))
            ax.add_patch(FancyArrowPatch((x1 + 0.9, y), (x1 + 0.9, y - 0.28),
                         arrowstyle="-", color=color, lw=1.2))
            ax.plot([x1, x1 + 0.9], [y, y], color=color, lw=1.2)
            ax.plot([x1, x1 + 0.9], [y - 0.28, y - 0.28], color=color, lw=1.2)
            ax.annotate("", xy=(x1, y - 0.28), xytext=(x1 + 0.45, y - 0.28),
                        arrowprops=dict(arrowstyle="-|>", color=color, lw=1.2))
            ax.text(x1 + 1.0, y - 0.13, label, ha="left", va="center",
                    fontsize=7.2, color="#334155")
            y -= 0.62
            continue
        style = "-|>" if kind != "return" else "-|>"
        ax.annotate("", xy=(x2, y), xytext=(x1, y),
                    arrowprops=dict(arrowstyle=style, color=color, lw=1.3, ls=ls))
        mid = (x1 + x2) / 2
        ax.text(mid, y + 0.08, label, ha="center", va="bottom",
                fontsize=7.3, color="#334155")
        y -= 0.62

    ax.set_xlim(-1.6, (n - 1) * 2.6 + 1.6)
    ax.set_ylim(-0.4, top + 1.1)
    ax.set_title(title, fontsize=12, fontweight="bold", color=HEAD, pad=10)
    ax.axis("off")
    fig.tight_layout()
    fig.savefig(outfile, dpi=160, bbox_inches="tight")
    plt.close(fig)
    print("wrote", outfile)


# ---------------- 1. LOGIN ----------------
render("Sequence Diagram 1 — Login Process",
    ["Actor", "LoginFrame", "LoginController", "UserDAO", "Database", "DashboardFrame"],
    [
        ("call",   "Actor", "LoginFrame", "enter username/password, click Login"),
        ("call",   "LoginFrame", "LoginController", "authenticate(user, pass)"),
        ("self",   "LoginController", "LoginController", "Validator.isNotBlank(...)"),
        ("call",   "LoginController", "UserDAO", "findByUsername(user)"),
        ("call",   "UserDAO", "Database", "SELECT ... LEFT JOIN doctors"),
        ("return", "Database", "UserDAO", "row(s)"),
        ("note",   "instantiate Admin / Doctor / Receptionist by role", ["UserDAO"]),
        ("return", "UserDAO", "LoginController", "Optional<User>"),
        ("self",   "LoginController", "LoginController", "PasswordHasher.verify(pass, hash)"),
        ("return", "LoginController", "LoginFrame", "Result.ok(user)"),
        ("call",   "LoginFrame", "DashboardFrame", "setCurrentUser(user); open"),
        ("note",   "alt: invalid -> Result.fail -> DialogHelper.showError", ["LoginFrame", "LoginController"]),
    ],
    "docs/report-assets/seq_login.png")

# ---------------- 2. BOOK APPOINTMENT ----------------
render("Sequence Diagram 2 — Book Appointment",
    ["Receptionist", "AppointmentPanel", "AppointmentController", "Patient/DoctorDAO", "AppointmentDAO", "Database"],
    [
        ("call",   "Receptionist", "AppointmentPanel", "select patient, doctor, datetime; click Book"),
        ("self",   "AppointmentPanel", "AppointmentPanel", "parse datetime (FMT)"),
        ("call",   "AppointmentPanel", "AppointmentController", "book(patientId, doctorId, dt)"),
        ("self",   "AppointmentController", "AppointmentController", "Validator.isFutureDate(dt)"),
        ("call",   "AppointmentController", "Patient/DoctorDAO", "findById(...)"),
        ("return", "Patient/DoctorDAO", "AppointmentController", "Optional<Patient/Doctor>"),
        ("call",   "AppointmentController", "AppointmentDAO", "existsByDoctorAndTime(doctorId, dt)"),
        ("call",   "AppointmentDAO", "Database", "SELECT COUNT(*) ..."),
        ("return", "Database", "AppointmentController", "false (slot free)"),
        ("call",   "AppointmentController", "AppointmentDAO", "insert(appointment)"),
        ("call",   "AppointmentDAO", "Database", "INSERT (UNIQUE doctor+time)"),
        ("return", "AppointmentDAO", "AppointmentController", "Result.ok(appt)"),
        ("return", "AppointmentController", "AppointmentPanel", "Result -> showInfo + refresh()"),
        ("note",   "alt: past date / slot taken -> Result.fail -> showError", ["AppointmentController", "AppointmentDAO"]),
    ],
    "docs/report-assets/seq_book.png")

# ---------------- 3. ADD PATIENT ----------------
render("Sequence Diagram 3 — Add Patient",
    ["Receptionist", "PatientPanel", "PatientController", "PatientDAO", "Database"],
    [
        ("call",   "Receptionist", "PatientPanel", "fill name/age/gender/history; click Add"),
        ("self",   "PatientPanel", "PatientPanel", "parse age (NumberFormat)"),
        ("call",   "PatientPanel", "PatientController", "add(name, age, gender, history)"),
        ("self",   "PatientController", "PatientController", "Validator.isNotBlank / isAgeValid"),
        ("call",   "PatientController", "PatientDAO", "insert(new Patient(...))"),
        ("call",   "PatientDAO", "Database", "INSERT ... RETURN_GENERATED_KEYS"),
        ("return", "Database", "PatientDAO", "generated patient_id"),
        ("return", "PatientDAO", "PatientController", "Result.ok(patient)"),
        ("return", "PatientController", "PatientPanel", "Result -> showInfo + refresh()"),
        ("note",   "alt: validation fail -> Result.fail -> showError", ["PatientController", "PatientPanel"]),
    ],
    "docs/report-assets/seq_addpatient.png")

# ---------------- 4. GENERATE REPORT ----------------
render("Sequence Diagram 4 — Generate Report",
    ["Admin", "ReportPanel", "ReportController", "DAO layer", "Database"],
    [
        ("call",   "Admin", "ReportPanel", "click Refresh Report"),
        ("call",   "ReportPanel", "ReportController", "totalPatients()"),
        ("call",   "ReportController", "DAO layer", "PatientDAO.findAll()"),
        ("call",   "DAO layer", "Database", "SELECT * FROM patients"),
        ("return", "DAO layer", "ReportController", "List<Patient> -> .size()"),
        ("call",   "ReportPanel", "ReportController", "totalAppointments()"),
        ("call",   "ReportController", "DAO layer", "AppointmentDAO.findAll()"),
        ("return", "DAO layer", "ReportController", "List<Appointment> -> .size()"),
        ("call",   "ReportPanel", "ReportController", "doctorSchedules()"),
        ("self",   "ReportController", "ReportController", "for each doctor: findByDoctor(id)"),
        ("call",   "ReportController", "Database", "SELECT ... per doctor"),
        ("return", "ReportController", "ReportPanel", "Map<Doctor, List<Appointment>>"),
        ("note",   "ReportPanel updates labels + schedule table", ["ReportPanel"]),
    ],
    "docs/report-assets/seq_report.png")
