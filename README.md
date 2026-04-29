# Health Assistance System

## Overview
JavaFX application for managing patients, doctors, appointments, health records with role-based access (Admin/Patient/Doctor). Uses MySQL (health_assist DB) via JDBC. Includes multithreading for reminders.

## Setup
1. **Database:** 
   ```
   mysql -u root -p
   CREATE DATABASE health_assist;
   USE health_assist;
   SOURCE init.sql;
   ```
   (Password: godeater164)

2. **Compile:**
   ```
   javac -cp "bin;lib/mysql-connector-j-8.4.0/mysql-connector-j-8.4.0.jar;%JAVA_HOME%/jmods/javafx.controls.jmod;%JAVA_HOME%/jmods/javafx.fxml.jmod;%JAVA_HOME%/jmods/javafx.graphics.jmod" -d bin src/**/*.java
   ```
   *Note: Requires JavaFX modules. Download JavaFX SDK if needed (https://gluonhq.com/products/javafx/). Adjust path.*

   Alternative (standalone JavaFX):
   ```
   javac --add-modules javafx.controls,javafx.fxml -cp "bin;lib/mysql-connector-j-8.4.0/mysql-connector-j-8.4.0.jar" -d bin src/**/*.java
   ```

3. **Run:**
   ```
   java --add-modules javafx.controls,javafx.fxml -cp "bin;lib/mysql-connector-j-8.4.0/mysql-connector-j-8.4.0.jar" HealthAppRunner
   ```

## Demo Credentials
- Admin: admin1 / adminpass
- Patient: patient1 / pass123
- Doctor: doctor1 / docpass

## Features
- **Login** with role-based dashboards.
- **Admin:** Add/list users (extend for patients/doctors).
- **Patient:** Book/view appts, view records.
- **Doctor:** View appts, add records.
- **DB:** Full CRUD, conflict prevention.
- **Multithreading:** Appointment reminders (check console).

## Compile Progress
Check TODO.md. Core app ready for testing.

**Test:** Login as admin1, add users; patient1 book appt; doctor1 add record.

