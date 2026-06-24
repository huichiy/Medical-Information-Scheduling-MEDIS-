package model;

public class Patient {
    private int    patientId;
    private String name;
    private int    age;
    private String gender;
    private String medicalHistory;

    public Patient(int patientId, String name, int age, String gender, String medicalHistory) {
        this.patientId = patientId;
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.medicalHistory = medicalHistory;
    }

    /** Constructor for new patients before the DB assigns an ID. */
    public Patient(String name, int age, String gender, String medicalHistory) {
        this(0, name, age, gender, medicalHistory);
    }

    public int    getPatientId()      { return patientId; }
    public String getName()           { return name; }
    public int    getAge()            { return age; }
    public String getGender()         { return gender; }
    public String getMedicalHistory() { return medicalHistory; }

    public void setPatientId(int id)        { this.patientId = id; }
    public void setName(String n)           { this.name = n; }
    public void setAge(int a)               { this.age = a; }
    public void setGender(String g)         { this.gender = g; }
    public void setMedicalHistory(String h) { this.medicalHistory = h; }

    @Override public String toString() { return name + " (id=" + patientId + ")"; }
}
