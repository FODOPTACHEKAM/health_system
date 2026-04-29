package models;

public class Doctor {
    private int id;
    private String name;
    private String specialization;
    private String schedule;  // e.g. "Mon-Fri 9-5"
    private int userId;

    public Doctor() {}

    public Doctor(String name, String specialization, String schedule, int userId) {
        this.name = name;
        this.specialization = specialization;
        this.schedule = schedule;
        this.userId = userId;
    }

    // Getters/Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public String getSchedule() { return schedule; }
    public void setSchedule(String schedule) { this.schedule = schedule; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
}

