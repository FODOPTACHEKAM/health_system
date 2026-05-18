package gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import dao.UserDAO;
import dao.DoctorDAO;
import dao.PatientDAO;
import models.User;
import models.Doctor;
import models.Patient;

import java.time.LocalDate;
import java.util.List;

/**
 * AdminDashboard — full user, patient, and doctor management.
 * Uses DAO layer exclusively (no raw SQL in GUI).
 */
public class AdminDashboard extends BaseDashboard {

    public AdminDashboard(String role, String username) {
        super(role, username);
    }

    @Override
    protected Region createContent() {
        // TabPane for the three management sections
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab usersTab = new Tab("👥  User Management");
        usersTab.setContent(buildUsersTab());

        Tab patientsTab = new Tab("👤  Patients");
        patientsTab.setContent(buildPatientsTab());

        Tab doctorsTab = new Tab("👨‍⚕️  Doctors");
        doctorsTab.setContent(buildDoctorsTab());

        tabPane.getTabs().addAll(usersTab, patientsTab, doctorsTab);

        return tabPane;
    }

    // ═══════════════════════════════════════════
    //  USERS TAB
    // ═══════════════════════════════════════════
    private Region buildUsersTab() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(24));

        // ── Summary row ──
        HBox stats = buildUserStats();

        // ── Add User card ──
        VBox addCard = createCard("➕  Add New User", buildAddUserForm());

        // ── User list card ──
        VBox listCard = createCard("📋  All Users", buildUserListPane());

        root.getChildren().addAll(stats, addCard, listCard);
        return wrapInScroll(root);
    }

    private HBox buildUserStats() {
        List<User> users = UserDAO.listAll();
        long admins = users.stream().filter(u -> "admin".equals(u.getRole())).count();
        long patients = users.stream().filter(u -> "patient".equals(u.getRole())).count();
        long doctors = users.stream().filter(u -> "doctor".equals(u.getRole())).count();

        HBox row = new HBox(16);
        row.getChildren().addAll(
                statCard("👥", String.valueOf(users.size()), "Total Users"),
                statCard("👤", String.valueOf(patients), "Patients"),
                statCard("👨‍⚕️", String.valueOf(doctors), "Doctors"),
                statCard("🔑", String.valueOf(admins), "Admins")
        );
        return row;
    }

    private VBox statCard(String icon, String number, String label) {
        VBox card = new VBox(4);
        card.getStyleClass().add("stat-card");
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(150);

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 24px;");

        Label numLabel = new Label(number);
        numLabel.getStyleClass().add("stat-number");

        Label lbl = new Label(label);
        lbl.getStyleClass().add("stat-label");

        card.getChildren().addAll(iconLabel, numLabel, lbl);
        return card;
    }

    private Region buildAddUserForm() {
        GridPane grid = new GridPane();
        grid.setVgap(14);
        grid.setHgap(20);
        grid.setPadding(new Insets(16, 0, 8, 0));

        // ── Fields ──
        TextField usernameField = styledField("Enter username");
        PasswordField passField = new PasswordField();
        passField.setPromptText("Enter password");
        passField.getStyleClass().add("text-field");

        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll("admin", "patient", "doctor");
        roleCombo.setPromptText("Select role");
        roleCombo.getStyleClass().add("combo-box");
        roleCombo.setMaxWidth(Double.MAX_VALUE);

        Button addBtn = new Button("Create User");
        addBtn.getStyleClass().add("btn-elec");

        Label msg = new Label();
        msg.setWrapText(true);

        // Column constraints
        ColumnConstraints col0 = new ColumnConstraints(120);
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(col0, col1);

        grid.add(formLabel("Username"), 0, 0); grid.add(usernameField, 1, 0);
        grid.add(formLabel("Password"), 0, 1); grid.add(passField, 1, 1);
        grid.add(formLabel("Role"), 0, 2);     grid.add(roleCombo, 1, 2);
        grid.add(addBtn, 1, 3);
        grid.add(msg, 1, 4);

        addBtn.setOnAction(e -> {
            String uname = usernameField.getText().trim();
            String pass = passField.getText();
            String role = roleCombo.getValue();

            if (uname.isEmpty() || pass.isEmpty() || role == null) {
                msg.getStyleClass().setAll("message-error");
                msg.setText("⚠ Please fill in all fields.");
                return;
            }

            User user = new User(uname, UserDAO.hashPassword(pass), role);
            if (UserDAO.create(user)) {
                msg.getStyleClass().setAll("message-success");
                msg.setText("✔  User \"" + uname + "\" created successfully.");
                usernameField.clear();
                passField.clear();
                roleCombo.setValue(null);
            } else {
                msg.getStyleClass().setAll("message-error");
                msg.setText("✖  Failed — username may already exist.");
            }
        });

        return grid;
    }

    private Region buildUserListPane() {
        VBox pane = new VBox(12);
        pane.setPadding(new Insets(8, 0, 0, 0));

        TableView<User> table = new TableView<>();
        table.getStyleClass().add("table-view");
        table.setPrefHeight(350);
        table.setPlaceholder(new Label("No users found."));

        TableColumn<User, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(60);

        TableColumn<User, String> nameCol = new TableColumn<>("Username");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        nameCol.setPrefWidth(220);

        TableColumn<User, String> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));
        roleCol.setPrefWidth(120);
        roleCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); setText(null); return; }
                Label badge = new Label(capitalize(item));
                String style = "patient".equals(item) ? "badge-booked"
                        : "doctor".equals(item) ? "badge-completed" : "badge-cancelled";
                badge.getStyleClass().add(style);
                setGraphic(badge); setText(null);
            }
        });

        // Delete action column
        TableColumn<User, Void> actionCol = new TableColumn<>("Action");
        actionCol.setPrefWidth(90);
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button del = new Button("🗑 Delete");
            { del.getStyleClass().add("btn-danger");
              del.setStyle("-fx-font-size: 11px; -fx-padding: 4 8;"); }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                del.setOnAction(e -> {
                    User u = getTableView().getItems().get(getIndex());
                    if (u.getUsername().equals(username)) {
                        showAlert(Alert.AlertType.WARNING, "Warning", "Cannot delete your own account.");
                        return;
                    }
                    // Soft approach: just remove from list display for now (no delete in UserDAO by default)
                    showAlert(Alert.AlertType.INFORMATION, "Delete User",
                            "Delete functionality can be enabled via UserDAO.delete(id).");
                });
                setGraphic(del);
            }
        });

        table.getColumns().addAll(idCol, nameCol, roleCol, actionCol);

        Button refreshBtn = new Button("↻  Refresh List");
        refreshBtn.getStyleClass().add("btn-secondary");
        refreshBtn.setOnAction(e -> table.getItems().setAll(UserDAO.listAll()));

        // Load immediately
        table.getItems().setAll(UserDAO.listAll());

        pane.getChildren().addAll(refreshBtn, table);
        return pane;
    }

    // ═══════════════════════════════════════════
    //  PATIENTS TAB
    // ═══════════════════════════════════════════
    private Region buildPatientsTab() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(24));

        VBox addCard = createCard("➕  Register Patient", buildAddPatientForm());
        VBox listCard = createCard("📋  All Patients", buildPatientListPane());

        root.getChildren().addAll(addCard, listCard);
        return wrapInScroll(root);
    }

    private Region buildAddPatientForm() {
        GridPane grid = new GridPane();
        grid.setVgap(14); grid.setHgap(20);
        grid.setPadding(new Insets(16, 0, 8, 0));

        TextField nameField = styledField("Full name");
        DatePicker dobPicker = new DatePicker();
        dobPicker.getStyleClass().add("date-picker");
        dobPicker.setPromptText("Date of birth");
        TextField contactField = styledField("Phone number");

        ComboBox<String> userCombo = new ComboBox<>();
        userCombo.setPromptText("Link to user account");
        userCombo.getStyleClass().add("combo-box");
        userCombo.setMaxWidth(Double.MAX_VALUE);
        // Populate with patient-role users
        UserDAO.listAll().stream()
                .filter(u -> "patient".equals(u.getRole()))
                .forEach(u -> userCombo.getItems().add(u.getId() + " — " + u.getUsername()));

        Button addBtn = new Button("Register Patient");
        addBtn.getStyleClass().add("btn-elec");

        Label msg = new Label();
        msg.setWrapText(true);

        ColumnConstraints col0 = new ColumnConstraints(130);
        ColumnConstraints col1 = new ColumnConstraints(); col1.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(col0, col1);

        grid.add(formLabel("Full Name"), 0, 0);  grid.add(nameField, 1, 0);
        grid.add(formLabel("Date of Birth"), 0, 1); grid.add(dobPicker, 1, 1);
        grid.add(formLabel("Contact"), 0, 2);    grid.add(contactField, 1, 2);
        grid.add(formLabel("User Account"), 0, 3); grid.add(userCombo, 1, 3);
        grid.add(addBtn, 1, 4);
        grid.add(msg, 1, 5);

        addBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            LocalDate dob = dobPicker.getValue();
            String contact = contactField.getText().trim();
            String sel = userCombo.getValue();

            if (name.isEmpty() || dob == null || contact.isEmpty() || sel == null) {
                msg.getStyleClass().setAll("message-error");
                msg.setText("⚠ Please complete all fields.");
                return;
            }

            int userId = Integer.parseInt(sel.split(" — ")[0]);
            Patient patient = new Patient(name, dob, contact, userId);
            if (PatientDAO.create(patient)) {
                msg.getStyleClass().setAll("message-success");
                msg.setText("✔  Patient registered successfully (ID: " + patient.getId() + ").");
                nameField.clear(); dobPicker.setValue(null); contactField.clear(); userCombo.setValue(null);
            } else {
                msg.getStyleClass().setAll("message-error");
                msg.setText("✖  Registration failed — user may already be linked to a patient.");
            }
        });

        return grid;
    }

    private Region buildPatientListPane() {
        VBox pane = new VBox(12);
        pane.setPadding(new Insets(8, 0, 0, 0));

        TableView<Patient> table = new TableView<>();
        table.getStyleClass().add("table-view");
        table.setPrefHeight(350);
        table.setPlaceholder(new Label("No patients registered."));

        TableColumn<Patient, Integer> idCol = col("ID", "id", 60);
        TableColumn<Patient, String>  nameCol = col("Name", "name", 200);
        TableColumn<Patient, String>  contactCol = col("Contact", "contact", 150);
        TableColumn<Patient, LocalDate> dobCol = col("Date of Birth", "dob", 130);

        table.getColumns().addAll(idCol, nameCol, dobCol, contactCol);

        Button refreshBtn = new Button("↻  Refresh");
        refreshBtn.getStyleClass().add("btn-secondary");
        refreshBtn.setOnAction(e -> table.getItems().setAll(PatientDAO.listAll()));
        table.getItems().setAll(PatientDAO.listAll());

        pane.getChildren().addAll(refreshBtn, table);
        return pane;
    }

    // ═══════════════════════════════════════════
    //  DOCTORS TAB
    // ═══════════════════════════════════════════
    private Region buildDoctorsTab() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(24));

        VBox addCard = createCard("➕  Register Doctor", buildAddDoctorForm());
        VBox listCard = createCard("📋  All Doctors", buildDoctorListPane());

        root.getChildren().addAll(addCard, listCard);
        return wrapInScroll(root);
    }

    private Region buildAddDoctorForm() {
        GridPane grid = new GridPane();
        grid.setVgap(14); grid.setHgap(20);
        grid.setPadding(new Insets(16, 0, 8, 0));

        TextField nameField = styledField("Dr. Full Name");
        TextField specField = styledField("e.g. Cardiology");
        TextField schedField = styledField("e.g. Mon-Fri 9am-5pm");

        ComboBox<String> userCombo = new ComboBox<>();
        userCombo.setPromptText("Link to user account");
        userCombo.getStyleClass().add("combo-box");
        userCombo.setMaxWidth(Double.MAX_VALUE);
        UserDAO.listAll().stream()
                .filter(u -> "doctor".equals(u.getRole()))
                .forEach(u -> userCombo.getItems().add(u.getId() + " — " + u.getUsername()));

        Button addBtn = new Button("Register Doctor");
        addBtn.getStyleClass().add("btn-elec");

        Label msg = new Label();
        msg.setWrapText(true);

        ColumnConstraints col0 = new ColumnConstraints(130);
        ColumnConstraints col1 = new ColumnConstraints(); col1.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(col0, col1);

        grid.add(formLabel("Full Name"), 0, 0);      grid.add(nameField, 1, 0);
        grid.add(formLabel("Specialization"), 0, 1); grid.add(specField, 1, 1);
        grid.add(formLabel("Schedule"), 0, 2);       grid.add(schedField, 1, 2);
        grid.add(formLabel("User Account"), 0, 3);   grid.add(userCombo, 1, 3);
        grid.add(addBtn, 1, 4);
        grid.add(msg, 1, 5);

        addBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            String spec = specField.getText().trim();
            String sched = schedField.getText().trim();
            String sel = userCombo.getValue();

            if (name.isEmpty() || spec.isEmpty() || sched.isEmpty() || sel == null) {
                msg.getStyleClass().setAll("message-error");
                msg.setText("⚠ Please complete all fields.");
                return;
            }

            int userId = Integer.parseInt(sel.split(" — ")[0]);
            Doctor doctor = new Doctor(name, spec, sched, userId);
            if (DoctorDAO.create(doctor)) {
                msg.getStyleClass().setAll("message-success");
                msg.setText("✔  Doctor registered successfully (ID: " + doctor.getId() + ").");
                nameField.clear(); specField.clear(); schedField.clear(); userCombo.setValue(null);
            } else {
                msg.getStyleClass().setAll("message-error");
                msg.setText("✖  Registration failed.");
            }
        });

        return grid;
    }

    private Region buildDoctorListPane() {
        VBox pane = new VBox(12);
        pane.setPadding(new Insets(8, 0, 0, 0));

        TableView<Doctor> table = new TableView<>();
        table.getStyleClass().add("table-view");
        table.setPrefHeight(350);
        table.setPlaceholder(new Label("No doctors registered."));

        TableColumn<Doctor, Integer> idCol = col("ID", "id", 60);
        TableColumn<Doctor, String> nameCol = col("Name", "name", 200);
        TableColumn<Doctor, String> specCol = col("Specialization", "specialization", 180);
        TableColumn<Doctor, String> schedCol = col("Schedule", "schedule", 180);

        table.getColumns().addAll(idCol, nameCol, specCol, schedCol);

        Button refreshBtn = new Button("↻  Refresh");
        refreshBtn.getStyleClass().add("btn-secondary");
        refreshBtn.setOnAction(e -> table.getItems().setAll(DoctorDAO.listAll()));
        table.getItems().setAll(DoctorDAO.listAll());

        pane.getChildren().addAll(refreshBtn, table);
        return pane;
    }

    // ═══════════════════════════════════════════
    //  HELPERS
    // ═══════════════════════════════════════════
    private ScrollPane wrapInScroll(VBox content) {
        ScrollPane sp = new ScrollPane(content);
        sp.setFitToWidth(true);
        sp.getStyleClass().add("scroll-pane");
        sp.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        return sp;
    }

    private TextField styledField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.getStyleClass().add("text-field");
        return tf;
    }

    private Label formLabel(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("form-label");
        l.setAlignment(Pos.CENTER_RIGHT);
        l.setMaxWidth(Double.MAX_VALUE);
        return l;
    }

    @SuppressWarnings("unchecked")
    private <S, T> TableColumn<S, T> col(String title, String prop, double width) {
        TableColumn<S, T> col = new TableColumn<>(title);
        col.setCellValueFactory(new PropertyValueFactory<>(prop));
        col.setPrefWidth(width);
        return col;
    }
}
