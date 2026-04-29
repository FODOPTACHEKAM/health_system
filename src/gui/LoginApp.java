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

public class LoginApp extends Application {
    private Stage primaryStage;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        Button loginBtn = new Button("Login");
        Label messageLabel = new Label();

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setVgap(10);
        grid.setHgap(10);
        grid.add(new Label("Username:"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(passwordField, 1, 1);
        grid.add(loginBtn, 1, 2);
        grid.add(messageLabel, 0, 3, 2, 1);

        VBox root = new VBox(20, grid);
        root.setPadding(new Insets(20));

        loginBtn.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText();

            if (authenticate(username, password)) {
                messageLabel.setText("Login successful!");
                messageLabel.setStyle("-fx-text-fill: green;");
                openDashboard(username);
            } else {
                messageLabel.setText("Invalid credentials!");
                messageLabel.setStyle("-fx-text-fill: red;");
            }
        });

        Scene scene = new Scene(root, 400, 250);
        stage.setTitle("Health Assistance System - Login");
        stage.setScene(scene);
        stage.show();
    }

    private boolean authenticate(String username, String password) {
        String sql = "SELECT role FROM users WHERE username = ? AND password_hash = ?";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);  // Demo: plain text; prod: hash
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                currentUserRole = rs.getString("role");
                currentUsername = username;
                return true;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    private String currentUserRole;
    private String currentUsername;

    private void openDashboard(String username) {
        Stage dashboardStage = new Stage();
        dashboardStage.setTitle("Health Dashboard - " + username);

        if ("admin".equals(currentUserRole)) {
            AdminDashboard dashboard = new AdminDashboard();
            dashboard.start(dashboardStage);
        } else if ("patient".equals(currentUserRole)) {
            PatientDashboard dashboard = new PatientDashboard();
            dashboard.start(dashboardStage);
        } else if ("doctor".equals(currentUserRole)) {
            DoctorDashboard dashboard = new DoctorDashboard();
            dashboard.start(dashboardStage);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}

