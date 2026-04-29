package gui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import database.DBConnection;
import java.sql.*;

public class DoctorDashboard extends Application {
    @Override
    public void start(Stage stage) {
        VBox root = new VBox(10);
        root.setPadding(new Insets(15));

        Label title = new Label("Doctor Dashboard");
        Button viewApptsBtn = new Button("View Appointments");
        Button addRecordBtn = new Button("Add Health Record");
        TextField patientIdField = new TextField();
        TextArea notesArea = new TextArea();
        notesArea.setPromptText("Enter health notes");
        TextArea displayArea = new TextArea();
        displayArea.setEditable(false);
        Label message = new Label();

        GridPane inputGrid = new GridPane();
        inputGrid.setVgap(8);
        inputGrid.setHgap(10);
        inputGrid.setPadding(new Insets(10));
        inputGrid.add(new Label("Patient ID:"), 0, 0);
        inputGrid.add(patientIdField, 1, 0);
        inputGrid.add(new Label("Notes:"), 0, 1);
        inputGrid.add(notesArea, 1, 1);
        inputGrid.add(addRecordBtn, 0, 2);

        root.getChildren().addAll(title, viewApptsBtn, inputGrid, message, displayArea);

        viewApptsBtn.setOnAction(e -> viewAppointments(displayArea, message));
        addRecordBtn.setOnAction(e -> addRecord(patientIdField, notesArea, message, displayArea));

        Scene scene = new Scene(root, 500, 450);
        stage.setTitle("Doctor Dashboard");
        stage.setScene(scene);
        stage.show();
    }

    private void viewAppointments(TextArea displayArea, Label message) {
        displayArea.clear();
        String sql = "SELECT a.id, p.name as patient, a.appointment_date_time, a.status FROM appointments a JOIN patients p ON a.patient_id = p.id WHERE doctor_id = 1";  // Demo doctor1
        try (Connection conn = DBConnection.connect(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                displayArea.appendText(rs.getInt("id") + " | " + rs.getString("patient") + " | " + rs.getString("appointment_date_time") + " | " + rs.getString("status") + "\n");
            }
        } catch (SQLException ex) {
            message.setText("Error: " + ex.getMessage());
        }
    }

    private void addRecord(TextField patientIdField, TextArea notesArea, Label message, TextArea displayArea) {
        int patientId;
        try {
            patientId = Integer.parseInt(patientIdField.getText());
        } catch (NumberFormatException ex) {
            message.setText("Invalid patient ID!");
            return;
        }
        String notes = notesArea.getText().trim();
        if (notes.isEmpty()) {
            message.setText("Notes required!");
            return;
        }

        String sql = "INSERT INTO health_records (patient_id, doctor_id, record_date, notes) VALUES (?, 1, CURDATE(), ?)";
        try (Connection conn = DBConnection.connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            ps.setString(2, notes);
            ps.executeUpdate();
            message.setText("Record added for patient " + patientId + "!");
            notesArea.clear();
            patientIdField.clear();
            viewAppointments(displayArea, message);  // Refresh
        } catch (SQLException ex) {
            message.setText("Error: " + ex.getMessage());
        }
    }
}

