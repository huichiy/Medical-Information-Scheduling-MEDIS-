package hms.model;

/**
 * Patient entity (Module 3 fields: name, age, gender, medical history).
 *
 * INHERITANCE  : extends Person.
 * POLYMORPHISM : overrides getSummary().
 * ENCAPSULATION: private fields with getters/setters.
 */
public class Patient extends Person {

    private int age;
    private String gender;
    private String medicalHistory;

    public Patient(int id, String name, int age, String gender, String medicalHistory) {
        super(id, name);
        this.age = age;
        this.gender = gender;
        this.medicalHistory = medicalHistory;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getMedicalHistory() {
        return medicalHistory;
    }

    public void setMedicalHistory(String medicalHistory) {
        this.medicalHistory = medicalHistory;
    }

    @Override
    public String getSummary() {
        return getName() + " (" + age + ", " + gender + ")";
    }
}
