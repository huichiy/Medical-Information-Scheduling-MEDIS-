package hms.model;

/**
 * Doctor entity (Module 4: specialization).
 *
 * INHERITANCE  : extends Person.
 * POLYMORPHISM : overrides getSummary() differently from Patient.
 */
public class Doctor extends Person {

    private String specialization;

    public Doctor(int id, String name, String specialization) {
        super(id, name);
        this.specialization = specialization;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    @Override
    public String getSummary() {
        return getName() + " \u2014 " + specialization; // e.g. "Dr. Lee Wei Ming — Cardiology"
    }
}
