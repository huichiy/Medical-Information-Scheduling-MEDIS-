package controller;

import dao.DoctorDAO;
import model.Doctor;
import model.Result;
import util.Validator;
import java.util.List;

public class DoctorController {
    private final DoctorDAO dao;

    public DoctorController(DoctorDAO dao) { this.dao = dao; }

    public Result add(String name, String specialization) {
        if (!Validator.isNotBlank(name))           return Result.fail("Name is required");
        if (!Validator.isNotBlank(specialization)) return Result.fail("Specialization is required");
        return dao.insert(new Doctor(0, name, specialization));
    }

    public List<Doctor> getAll() { return dao.findAll(); }
}
