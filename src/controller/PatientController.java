package controller;

import dao.PatientDAO;
import model.Patient;
import model.Result;
import util.Validator;
import java.util.List;

public class PatientController {
    private final PatientDAO dao;

    public PatientController(PatientDAO dao) { this.dao = dao; }

    public Result add(String name, int age, String gender, String history) {
        if (!Validator.isNotBlank(name))   return Result.fail("Name is required");
        if (!Validator.isAgeValid(age))    return Result.fail("Age must be 0-150");
        if (!Validator.isNotBlank(gender)) return Result.fail("Gender is required");
        return dao.insert(new Patient(name, age, gender, history == null ? "" : history));
    }

    public Result update(int id, String name, int age, String gender, String history) {
        if (!Validator.isNotBlank(name))   return Result.fail("Name is required");
        if (!Validator.isAgeValid(age))    return Result.fail("Age must be 0-150");
        if (!Validator.isNotBlank(gender)) return Result.fail("Gender is required");
        return dao.update(new Patient(id, name, age, gender, history == null ? "" : history));
    }

    public List<Patient> getAll() { return dao.findAll(); }
}
