package dao;

import model.Doctor;
import model.Result;
import java.util.List;
import java.util.Optional;

public interface DoctorDAO {
    Result           insert(Doctor d);
    Optional<Doctor> findById(int id);
    List<Doctor>     findAll();
    Result           update(Doctor d);
}
