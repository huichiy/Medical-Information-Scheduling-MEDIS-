package controller;

import model.User;

public class SystemController {
    private final LoginController       loginCtrl;
    private final PatientController     patientCtrl;
    private final DoctorController      doctorCtrl;
    private final AppointmentController appointmentCtrl;
    private final ReportController      reportCtrl;
    private User currentUser;

    public SystemController(LoginController lc, PatientController pc,
                            DoctorController dc, AppointmentController ac,
                            ReportController rc) {
        this.loginCtrl       = lc;
        this.patientCtrl     = pc;
        this.doctorCtrl      = dc;
        this.appointmentCtrl = ac;
        this.reportCtrl      = rc;
    }

    public LoginController       login()       { return loginCtrl; }
    public PatientController     patient()     { return patientCtrl; }
    public DoctorController      doctor()      { return doctorCtrl; }
    public AppointmentController appointment() { return appointmentCtrl; }
    public ReportController      report()      { return reportCtrl; }

    public User getCurrentUser()       { return currentUser; }
    public void setCurrentUser(User u) { this.currentUser = u; }
    public void logout()               { this.currentUser = null; }
}
