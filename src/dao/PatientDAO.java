package dao;

import model.Patient;
import model.Result;
import java.util.List;
import java.util.Optional;

public interface PatientDAO {
    Result            insert(Patient p);
    Optional<Patient> findById(int id);
    List<Patient>     findAll();
    Result            update(Patient p);
}
