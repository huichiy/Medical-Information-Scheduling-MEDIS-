package controller;

import dao.AppointmentDAO;
import dao.DoctorDAO;
import dao.PatientDAO;
import model.Appointment;
import model.Doctor;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ReportController {
    private final PatientDAO     patientDAO;
    private final DoctorDAO      doctorDAO;
    private final AppointmentDAO apptDAO;

    public ReportController(PatientDAO p, DoctorDAO d, AppointmentDAO a) {
        this.patientDAO = p; this.doctorDAO = d; this.apptDAO = a;
    }

    public int totalPatients()     { return patientDAO.findAll().size(); }
    public int totalAppointments() { return apptDAO.findAll().size(); }

    /** Map of doctor -> their appointments, preserving doctor order. */
    public Map<Doctor, List<Appointment>> doctorSchedules() {
        Map<Doctor, List<Appointment>> map = new LinkedHashMap<>();
        for (Doctor d : doctorDAO.findAll()) {
            map.put(d, apptDAO.findByDoctor(d.getDoctorId()));
        }
        return map;
    }
}
