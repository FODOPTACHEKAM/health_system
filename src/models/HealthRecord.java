package models;

import java.time.LocalDate;

public class HealthRecord {
    private int id;
    private int patientId;
    private int doctorId;
    private LocalDate recordDate;
    private String notes;

    public HealthRecord() {}

    public HealthRecord(int patientId, int doctorId, LocalDate recordDate, String notes) {
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.recordDate = recordDate;
        this.notes = notes;
    }

    // Getters/Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }

    public int getDoctorId() { return doctorId; }
    public void setDoctorId(int doctorId) { this.doctorId = doctorId; }

    public LocalDate getRecordDate() { return recordDate; }
    public void setRecordDate(LocalDate recordDate) { this.recordDate = recordDate; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}

