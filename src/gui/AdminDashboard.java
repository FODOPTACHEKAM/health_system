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

public class AdminDashboard extends Application {
    @Override
    public void start(Stage stage) {
        VBox root = new VBox(10);
        root.setPadding(new Insets(15));

        TabPane tabPane = new TabPane();

        // Users Tab
        Tab usersTab = new Tab("Manage Users", createUsersGrid());
        tabPane.getTabs().add(usersTab);

        // Patients Tab
        Tab patientsTab = new Tab("Manage Patients", createPatientsGrid());
        tabPane.getTabs().add(patientsTab);

        // Doctors Tab
        Tab doctorsTab = new Tab("Manage Doctors", createDoctorsGrid());
        tabPane.getTabs().add(doctorsTab);

        root.getChildren().addAll(new Label("Admin Dashboard"), tabPane);

        Scene scene = new Scene(root, 600, 500);
        stage.setTitle("Admin Dashboard");
        stage.setScene(scene);
        stage.show();
    }

    private GridPane createUsersGrid() {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setVgap(8);
        grid.setHgap(10);

        TextField usernameField = new TextField();
        PasswordField passField = new PasswordField();
        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll("admin", "patient", "doctor");

        Button addBtn = new Button("Add User");
        Button listBtn = new Button("List Users");
        TextArea displayArea = new TextArea();
        displayArea.setEditable(false);
        Label message = new Label();

        grid.add(new Label("Username:"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(passField, 1, 1);
        grid.add(new Label("Role:"), 0, 2);
        grid.add(roleCombo, 1, 2);
        grid.add(addBtn, 0, 3);
        grid.add(listBtn, 1, 3);
        grid.add(message, 0, 4, 2, 1);
        grid.add(displayArea, 0, 5, 2, 1);

        addBtn.setOnAction(e -> addUser(usernameField, passField, roleCombo, message));
        listBtn.setOnAction(e -> listUsers(displayArea, message));

        return grid;
    }

    private void addUser(TextField usernameField, PasswordField passField, ComboBox<String> roleCombo, Label message) {
        String username = usernameField.getText().trim();
        String pass = passField.getText();
        String role = roleCombo.getValue();

        if (username.isEmpty() || pass.isEmpty() || role == null) {
            message.setText("Fill all fields!");
            return;
        }

        String sql = "INSERT INTO users (username, password_hash, role) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, pass);  // Plain for demo
            ps.setString(3, role);
            ps.executeUpdate();
            message.setText("User added!");
        } catch (SQLException ex) {
            message.setText("Error: " + ex.getMessage());
        }
        usernameField.clear();
        passField.clear();
        roleCombo.setValue(null);
    }

    private void listUsers(TextArea displayArea, Label message) {
        displayArea.clear();
        String sql = "SELECT * FROM users";
        try (Connection conn = DBConnection.connect(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                displayArea.appendText(String.format("ID: %d, User: %s, Role: %s\n", rs.getInt("id"), rs.getString("username"), rs.getString("role")));
            }
        } catch (SQLException ex) {
            message.setText("Error: " + ex.getMessage());
        }
    }

    private GridPane createPatientsGrid() {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setVgap(8);
        grid.setHgap(10);
        grid.add(new Label("Patient Management - Implement CRUD similar to Users"), 0, 0);
        return grid;
    }

    private GridPane createDoctorsGrid() {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setVgap(8);
        grid.setHgap(10);
        grid.add(new Label("Doctor Management - Implement CRUD similar to Users"), 0, 0);
        return grid;
    }
}

