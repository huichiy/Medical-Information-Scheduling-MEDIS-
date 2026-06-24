package controller;

import dao.AppointmentDAO;
import dao.DoctorDAO;
import dao.PatientDAO;
import model.Appointment;
import model.Doctor;
import model.Patient;
import model.Result;
import util.Validator;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class AppointmentController {
    private final AppointmentDAO apptDAO;
    private final PatientDAO     patientDAO;
    private final DoctorDAO      doctorDAO;

    public AppointmentController(AppointmentDAO a, PatientDAO p, DoctorDAO d) {
        this.apptDAO = a; this.patientDAO = p; this.doctorDAO = d;
    }

    public Result book(int patientId, int doctorId, LocalDateTime dt) {
        if (!Validator.isFutureDate(dt))
            return Result.fail("Date must be in the future");

        Optional<Patient> p = patientDAO.findById(patientId);
        if (p.isEmpty()) return Result.fail("Please select a patient");

        Optional<Doctor> d = doctorDAO.findById(doctorId);
        if (d.isEmpty()) return Result.fail("Please select a doctor");

        if (apptDAO.existsByDoctorAndTime(doctorId, dt))
            return Result.fail("Time slot already taken");

        return apptDAO.insert(new Appointment(p.get(), d.get(), dt));
    }

    public Result cancel(int appointmentId) {
        return apptDAO.updateStatus(appointmentId, Appointment.Status.CANCELLED);
    }

    public List<Appointment> getAll() { return apptDAO.findAll(); }
}
