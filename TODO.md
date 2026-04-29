# Health Assistance System Development Plan

## Approved Plan Summary:
- Build on existing JavaFX/JDBC template.
- New MySQL DB: `health_assist`.
- Entities: User(roles), Patient, Doctor, Appointment, HealthRecord.
- Role-based: Login -> Admin/Patient/Doctor dashboards with CRUD.
- Multithreading: Scheduled reminders.
- Reuses DBConnection, GUI patterns.

## TODO Steps:
1. **[COMPLETE]** Create `init.sql` for DB schema (`health_assist` DB/tables).
2. **[COMPLETE]** Update `src/database/DBConnection.java` (add health_assist connect method).
3. **[COMPLETE]** Create model classes in `src/models/`: User.java, Patient.java, Doctor.java, Appointment.java, HealthRecord.java.
4. **[COMPLETE]** Create `src/gui/LoginApp.java` (login screen with role check).
5. **[PENDING]** Create role dashboards:
   - `src/gui/AdminDashboard.java` (manage users/patients/doctors).
   - `src/gui/PatientDashboard.java` (appts/records).
   - `src/gui/DoctorDashboard.java` (appts/records/schedule).
6. **[PENDING]** Add multithreading (ScheduledExecutorService for reminders in dashboards).
7. **[PENDING]** Create `src/HealthAppRunner.java` (launch LoginApp).
8. **[PENDING]** Compile all: Update compile/run instructions.
9. **[PENDING]** Test: Run init.sql, login each role, CRUD, reminders.
10. **[COMPLETE]** Update README.md with instructions.

**Next:** User: Run MySQL: `CREATE DATABASE health_assist;`, then `source init.sql`. Confirm DB ready.

