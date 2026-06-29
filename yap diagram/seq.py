#!/usr/bin/env python3
"""Render the four MEDIS sequence diagrams as PNGs (matplotlib).

Output goes next to this script (the "yap diagram" folder), so it is safe to
run from any working directory:  python3 "yap diagram/seq.py"

Supports UML 2.x `alt` combined fragments via these pseudo-steps:
    ("frag", "alt", "[guard for operand 1]")   # open the fragment
    ("else", "[guard for operand 2]")           # divider to the next operand
    ("end",)                                     # close the fragment
"""
import os
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
from matplotlib.patches import FancyArrowPatch, Rectangle, Polygon

OUT = os.path.dirname(os.path.abspath(__file__))

HEAD = "#1e293b"; CALL = "#1d4ed8"; RET = "#15803d"; NOTE = "#fde68a"
FRAME = "#475569"

DY = 0.62  # vertical spacing for a normal message row


def step_dy(kind):
    return {"note": 0.67, "frag": 0.50, "else": 0.50, "end": 0.30}.get(kind, DY)


def render(title, lifelines, steps, outfile):
    n = len(lifelines)
    xs = {name: i * 2.6 for i, name in enumerate(lifelines)}
    total = sum(step_dy(s[0]) for s in steps)
    height = 1.6 + total
    width = 1.2 + (n - 1) * 2.6 + 2.2
    fig, ax = plt.subplots(figsize=(width, height))
    top = total + 0.2

    # lifeline headers + dashed lines
    for name in lifelines:
        x = xs[name]
        ax.add_patch(Rectangle((x - 1.05, top + 0.15), 2.1, 0.62,
                     facecolor="#e2e8f0", edgecolor=HEAD, lw=1.2, zorder=3))
        ax.text(x, top + 0.46, name, ha="center", va="center",
                fontsize=8.5, fontweight="bold", color=HEAD, zorder=4)
        ax.plot([x, x], [-0.1, top + 0.15], color="#94a3b8", ls=(0, (4, 4)), lw=1, zorder=1)

    y = top - 0.25
    frag_stack = []   # open fragments
    frags_done = []   # completed fragments to draw last

    def touch(*names):
        """Expand the x-extent of every open fragment to include these lifelines."""
        for f in frag_stack:
            for nm in names:
                f["xlo"] = min(f["xlo"], xs[nm])
                f["xhi"] = max(f["xhi"], xs[nm])

    for s in steps:
        kind = s[0]

        if kind == "frag":
            frag_stack.append({"label": s[1], "guard": s[2], "top": y + 0.20,
                               "xlo": float("inf"), "xhi": float("-inf"),
                               "dividers": []})
            y -= step_dy(kind)
            continue
        if kind == "else":
            frag_stack[-1]["dividers"].append((y + 0.20, s[1]))
            y -= step_dy(kind)
            continue
        if kind == "end":
            f = frag_stack.pop()
            f["bottom"] = y + 0.30
            frags_done.append(f)
            y -= step_dy(kind)
            continue

        if kind == "note":
            text = s[1]; targets = s[2]
            lo = min(xs[t] for t in targets) - 1.0
            hi = max(xs[t] for t in targets) + 1.0
            ax.add_patch(Rectangle((lo, y - 0.18), hi - lo, 0.42,
                         facecolor=NOTE, edgecolor="#b45309", lw=0.8, zorder=5))
            ax.text((lo + hi) / 2, y + 0.03, text, ha="center", va="center",
                    fontsize=7.2, style="italic", color="#92400e", zorder=6)
            touch(*targets)
            y -= step_dy(kind)
            continue

        frm, to, label = s[1], s[2], s[3]
        touch(frm, to)
        color = RET if kind == "return" else CALL
        ls = "--" if kind == "return" else "-"
        x1, x2 = xs[frm], xs[to]
        if kind == "self" or frm == to:
            ax.add_patch(FancyArrowPatch((x1 + 0.9, y), (x1 + 0.9, y - 0.28),
                         arrowstyle="-", color=color, lw=1.2))
            ax.plot([x1, x1 + 0.9], [y, y], color=color, lw=1.2)
            ax.plot([x1, x1 + 0.9], [y - 0.28, y - 0.28], color=color, lw=1.2)
            ax.annotate("", xy=(x1, y - 0.28), xytext=(x1 + 0.45, y - 0.28),
                        arrowprops=dict(arrowstyle="-|>", color=color, lw=1.2))
            ax.text(x1 + 1.0, y - 0.13, label, ha="left", va="center",
                    fontsize=7.2, color="#334155")
            y -= DY
            continue
        ax.annotate("", xy=(x2, y), xytext=(x1, y),
                    arrowprops=dict(arrowstyle="-|>", color=color, lw=1.3, ls=ls))
        mid = (x1 + x2) / 2
        ax.text(mid, y + 0.08, label, ha="center", va="bottom",
                fontsize=7.3, color="#334155")
        y -= DY

    # ---- draw completed alt fragments (frame + tab + guards) ----
    for f in frags_done:
        xlo = f["xlo"] - 1.15
        xhi = f["xhi"] + 1.15
        ytop = f["top"]; ybot = f["bottom"]
        ax.add_patch(Rectangle((xlo, ybot), xhi - xlo, ytop - ybot,
                     fill=False, edgecolor=FRAME, lw=1.2, zorder=1.4))
        # operator tab (pentagon with folded bottom-right corner)
        tw, th = 0.95, 0.32
        ax.add_patch(Polygon([(xlo, ytop), (xlo + tw, ytop),
                              (xlo + tw, ytop - th + 0.10),
                              (xlo + tw - 0.12, ytop - th), (xlo, ytop - th)],
                     closed=True, facecolor="#e2e8f0", edgecolor=FRAME,
                     lw=1.0, zorder=6))
        ax.text(xlo + tw / 2 - 0.05, ytop - th / 2, f["label"], ha="center",
                va="center", fontsize=7.8, fontweight="bold", color=HEAD, zorder=7)
        ax.text(xlo + tw + 0.18, ytop - th / 2, f["guard"], ha="left",
                va="center", fontsize=7.0, style="italic", color="#334155", zorder=7)
        for dy_y, guard in f["dividers"]:
            ax.plot([xlo, xhi], [dy_y, dy_y], color=FRAME, ls=(0, (5, 3)),
                    lw=0.9, zorder=1.6)
            ax.text(xlo + 0.18, dy_y - 0.13, guard, ha="left", va="center",
                    fontsize=7.0, style="italic", color="#334155", zorder=7)

    ax.set_xlim(-1.6, (n - 1) * 2.6 + 1.6)
    ax.set_ylim(-0.5, top + 1.1)
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
        ("self",   "LoginController", "LoginController", "Validator.isNotBlank(user, pass)"),
        ("call",   "LoginController", "UserDAO", "findByUsername(user)"),
        ("call",   "UserDAO", "Database", "SELECT ... LEFT JOIN doctors"),
        ("return", "Database", "UserDAO", "row(s)"),
        ("note",   "instantiate Admin / Doctor / Receptionist by role", ["UserDAO"]),
        ("return", "UserDAO", "LoginController", "Optional<User>"),
        ("self",   "LoginController", "LoginController", "PasswordHasher.verify(pass, hash)"),
        ("frag",   "alt", "[user found AND password matches]"),
        ("return", "LoginController", "LoginFrame", "Result.ok(user)"),
        ("call",   "LoginFrame", "DashboardFrame", "setCurrentUser(user); open"),
        ("else",   "[blank / invalid credentials]"),
        ("return", "LoginController", "LoginFrame", "Result.fail(msg)"),
        ("self",   "LoginFrame", "LoginFrame", "DialogHelper.showError(msg)"),
        ("end",),
    ],
    os.path.join(OUT, "seq_login.png"))

# ---------------- 2. BOOK APPOINTMENT ----------------
render("Sequence Diagram 2 — Book Appointment",
    ["Receptionist", "AppointmentPanel", "AppointmentController", "Patient/DoctorDAO", "AppointmentDAO", "Database"],
    [
        ("call",   "Receptionist", "AppointmentPanel", "select patient, doctor, datetime; click Book"),
        ("self",   "AppointmentPanel", "AppointmentPanel", "parse datetime (FMT)"),
        ("call",   "AppointmentPanel", "AppointmentController", "book(patientId, doctorId, dt)"),
        ("self",   "AppointmentController", "AppointmentController", "Validator.isFutureDate(dt)"),
        ("call",   "AppointmentController", "Patient/DoctorDAO", "findById(patientId) / findById(doctorId)"),
        ("return", "Patient/DoctorDAO", "AppointmentController", "Optional<Patient> / Optional<Doctor>"),
        ("call",   "AppointmentController", "AppointmentDAO", "existsByDoctorAndTime(doctorId, dt)"),
        ("call",   "AppointmentDAO", "Database", "SELECT COUNT(*) ..."),
        ("return", "Database", "AppointmentController", "false (slot free)"),
        ("frag",   "alt", "[future date AND patient/doctor found AND slot free]"),
        ("call",   "AppointmentController", "AppointmentDAO", "insert(new Appointment(p, d, dt))"),
        ("call",   "AppointmentDAO", "Database", "INSERT (UNIQUE doctor+time)"),
        ("return", "AppointmentDAO", "AppointmentController", "Result.ok(appt)"),
        ("return", "AppointmentController", "AppointmentPanel", "showInfo + refresh()"),
        ("else",   "[past date / missing selection / slot taken]"),
        ("return", "AppointmentController", "AppointmentPanel", "Result.fail(msg)"),
        ("self",   "AppointmentPanel", "AppointmentPanel", "DialogHelper.showError(msg)"),
        ("end",),
    ],
    os.path.join(OUT, "seq_book.png"))

# ---------------- 3. ADD PATIENT ----------------
render("Sequence Diagram 3 — Add Patient",
    ["Receptionist", "PatientPanel", "PatientController", "PatientDAO", "Database"],
    [
        ("call",   "Receptionist", "PatientPanel", "fill name/age/gender/history; click Add"),
        ("self",   "PatientPanel", "PatientPanel", "parse age (NumberFormat)"),
        ("call",   "PatientPanel", "PatientController", "add(name, age, gender, history)"),
        ("self",   "PatientController", "PatientController", "Validator.isNotBlank / isAgeValid"),
        ("frag",   "alt", "[name & gender not blank AND age 0-150]"),
        ("call",   "PatientController", "PatientDAO", "insert(new Patient(...))"),
        ("call",   "PatientDAO", "Database", "INSERT ... RETURN_GENERATED_KEYS"),
        ("return", "Database", "PatientDAO", "generated patient_id"),
        ("return", "PatientDAO", "PatientController", "Result.ok(patient)"),
        ("return", "PatientController", "PatientPanel", "showInfo + refresh()"),
        ("else",   "[blank field / age out of range]"),
        ("return", "PatientController", "PatientPanel", "Result.fail(msg)"),
        ("self",   "PatientPanel", "PatientPanel", "DialogHelper.showError(msg)"),
        ("end",),
    ],
    os.path.join(OUT, "seq_addpatient.png"))

# ---------------- 4. GENERATE REPORT (no error path -> no alt) ----------------
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
    os.path.join(OUT, "seq_report.png"))
