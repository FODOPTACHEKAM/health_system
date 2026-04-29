package models;

import java.time.LocalDate;

public class Patient {
    private int id;
    private String name;
    private LocalDate dob;
    private String contact;
    private int userId;

    public Patient() {}

    public Patient(String name, LocalDate dob, String contact, int userId) {
        this.name = name;
        this.dob = dob;
        this.contact = contact;
        this.userId = userId;
    }

    // Getters/Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LocalDate getDob() { return dob; }
    public void setDob(LocalDate dob) { this.dob = dob; }

    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
}

