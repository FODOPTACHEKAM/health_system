package gui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import database.DBConnection;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PatientDashboard extends Application {
    @Override
    public void start(Stage stage) {
        VBox root = new VBox(10);
        root.setPadding(new Insets(15));

        Label title = new Label("Patient Dashboard");
        Button bookApptBtn = new Button("Book Appointment");
        Button viewApptsBtn = new Button("View Appointments");
        Button viewRecordsBtn = new Button("View Records");
        TextArea displayArea = new TextArea();
        displayArea.setEditable(false);
        Label message = new Label();

        root.getChildren().addAll(title, bookApptBtn, viewApptsBtn, viewRecordsBtn, message, displayArea);

        bookApptBtn.setOnAction(e -> bookAppointment(displayArea, message));
        viewApptsBtn.setOnAction(e -> viewAppointments(displayArea, message));
        viewRecordsBtn.setOnAction(e -> viewRecords(displayArea, message));

        Scene scene = new Scene(root, 500, 400);
        stage.setTitle("Patient Dashboard");
        stage.setScene(scene);
        stage.show();
    }

    private void bookAppointment(TextArea displayArea, Label message) {
        // Simple demo: book with doctor1
        String sql = "INSERT INTO appointments (patient_id, doctor_id, appointment_date_time, status) VALUES (1, 1, NOW() + INTERVAL 1 DAY, 'booked')";
        try (Connection conn = DBConnection.connect(); Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            message.setText("Appointment booked!");
        } catch (SQLException ex) {
            message.setText("Error booking (check conflicts): " + ex.getMessage());
        }
    }

    private void viewAppointments(TextArea displayArea, Label message) {
        displayArea.clear();
        String sql = "SELECT a.id, d.name as doctor, a.appointment_date_time, a.status FROM appointments a JOIN doctors d ON a.doctor_id = d.id WHERE patient_id = 1";  // Demo patient1
        try (Connection conn = DBConnection.connect(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                displayArea.appendText(rs.getInt("id") + " | Dr. " + rs.getString("doctor") + " | " + rs.getString("appointment_date_time") + " | " + rs.getString("status") + "\n");
            }
        } catch (SQLException ex) {
            message.setText("Error: " + ex.getMessage());
        }
    }

    private void viewRecords(TextArea displayArea, Label message) {
        displayArea.clear();
        String sql = "SELECT h.id, d.name as doctor, h.record_date, h.notes FROM health_records h JOIN doctors d ON h.doctor_id = d.id WHERE patient_id = 1";  // Demo patient1
        try (Connection conn = DBConnection.connect(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                displayArea.appendText(rs.getInt("id") + " | Dr. " + rs.getString("doctor") + " | " + rs.getString("record_date") + " | " + rs.getString("notes") + "\n");
            }
        } catch (SQLException ex) {
            message.setText("Error: " + ex.getMessage());
        }
    }
}

