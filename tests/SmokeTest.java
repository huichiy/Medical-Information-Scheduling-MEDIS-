import controller.*;
import dao.*;
import model.Appointment;
import model.Result;
import model.User;
import java.time.LocalDateTime;

/** Throwaway end-to-end smoke test of the controller layer (what the GUI buttons call). */
public class SmokeTest {
    static int pass = 0, fail = 0;

    static void check(String label, boolean cond) {
        System.out.printf("%s %s%n", cond ? "[PASS]" : "[FAIL]", label);
        if (cond) pass++; else fail++;
    }

    public static void main(String[] args) {
        UserDAO userDAO = new UserDAOImpl();
        PatientDAO patientDAO = new PatientDAOImpl();
        DoctorDAO doctorDAO = new DoctorDAOImpl();
        AppointmentDAO apptDAO = new AppointmentDAOImpl();

        LoginController login = new LoginController(userDAO);
        PatientController patient = new PatientController(patientDAO);
        DoctorController doctor = new DoctorController(doctorDAO);
        AppointmentController appt = new AppointmentController(apptDAO, patientDAO, doctorDAO);
        ReportController report = new ReportController(patientDAO, doctorDAO, apptDAO);

        // 2. login admin
        Result r = login.authenticate("admin", "admin123");
        check("admin login succeeds", r.isOk() && ((User) r.getData()).getRole().equals("ADMIN"));

        // 13. wrong password
        check("wrong password rejected", !login.authenticate("admin", "nope").isOk());

        // 11/12. doctor + receptionist login (verifies pass123 hash fix)
        check("doctor1 login succeeds", login.authenticate("doctor1", "pass123").isOk());
        check("recep1 login succeeds",  login.authenticate("recep1", "pass123").isOk());

        // 3. add doctor
        int doctorsBefore = doctor.getAll().size();
        check("add doctor", doctor.add("Dr. Wong", "Neurology").isOk()
              && doctor.getAll().size() == doctorsBefore + 1);

        // 4. add patient
        int patientsBefore = patient.getAll().size();
        check("add patient", patient.add("Carol Ng", 28, "F", "none").isOk()
              && patient.getAll().size() == patientsBefore + 1);

        // 5. empty name rejected
        check("empty name rejected", !patient.add("", 20, "M", "").isOk());
        // 6. bad age rejected
        check("age -1 rejected",  !patient.add("X", -1, "M", "").isOk());
        check("age 200 rejected", !patient.add("X", 200, "M", "").isOk());

        // 7. book appointment (future, free slot) — patient 1, doctor 1 @ a new time
        LocalDateTime slot = LocalDateTime.of(2026, 8, 1, 9, 0);
        check("book future appointment", appt.book(1, 1, slot).isOk());

        // 8. duplicate slot rejected
        check("duplicate slot rejected", !appt.book(1, 1, slot).isOk());

        // 9. past date rejected
        check("past date rejected", !appt.book(1, 1, LocalDateTime.of(2020, 1, 1, 9, 0)).isOk());

        // 10. report totals
        check("report patient count > 0", report.totalPatients() > 0);
        check("report appointment count > 0", report.totalAppointments() > 0);
        check("doctor schedules non-empty", !report.doctorSchedules().isEmpty());

        // 14. cancel an appointment (soft delete -> status CANCELLED, row remains)
        int totalBefore = appt.getAll().size();
        int someId = appt.getAll().get(0).getAppointmentId();
        check("cancel appointment", appt.cancel(someId).isOk());
        boolean stillThere = appt.getAll().stream().anyMatch(a -> a.getAppointmentId() == someId);
        boolean nowCancelled = appt.getAll().stream()
            .anyMatch(a -> a.getAppointmentId() == someId && a.getStatus() == Appointment.Status.CANCELLED);
        check("cancelled row still present (soft delete)", stillThere && appt.getAll().size() == totalBefore);
        check("cancelled status applied", nowCancelled);

        System.out.printf("%n=== %d passed, %d failed ===%n", pass, fail);
        if (fail > 0) System.exit(1);
    }
}
