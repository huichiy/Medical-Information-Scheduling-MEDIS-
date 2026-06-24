package dao;

import model.Appointment;
import model.Result;
import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentDAO {
    Result            insert(Appointment a);
    List<Appointment> findAll();
    List<Appointment> findByDoctor(int doctorId);
    boolean           existsByDoctorAndTime(int doctorId, LocalDateTime dt);
    Result            updateStatus(int appointmentId, Appointment.Status status);
}
