package gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import dao.AppointmentDAO;
import dao.DoctorDAO;
import dao.HealthRecordDAO;
import dao.PatientDAO;
import models.Appointment;
import models.Doctor;
import models.HealthRecord;
import models.Patient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * DoctorDashboard — view appointments, manage records, view patient info.
 * Doctor ID is resolved from the logged-in user via DoctorDAO.
 */
public class DoctorDashboard extends BaseDashboard {

    private Doctor currentDoctor;
    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("MMM d, yyyy  HH:mm");
    private static final DateTimeFormatter D_FMT  = DateTimeFormatter.ofPattern("MMM d, yyyy");

    public DoctorDashboard(String role, String username) {
        super(role, username);
    }

    @Override
    public void start(javafx.stage.Stage s) {
        currentDoctor = DoctorDAO.findByUserId(currentUserId);
        super.start(s);
    }

    @Override
    protected Region createContent() {
        if (currentDoctor == null) {
            return buildNoProfilePane();
        }

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab overviewTab = new Tab("🏠  Overview");
        overviewTab.setContent(buildOverviewTab());

        Tab apptsTab = new Tab("📋  Appointments");
        apptsTab.setContent(buildAppointmentsTab());

        Tab addRecordTab = new Tab("➕  Add Record");
        addRecordTab.setContent(buildAddRecordTab());

        Tab recordsTab = new Tab("📊  Patient Records");
        recordsTab.setContent(buildRecordsTab());

        tabPane.getTabs().addAll(overviewTab, apptsTab, addRecordTab, recordsTab);
        return tabPane;
    }

    // ═══════════════════════════════════════════
    //  OVERVIEW TAB
    // ═══════════════════════════════════════════
    private Region buildOverviewTab() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(24));

        // Welcome banner
        VBox welcomeCard = new VBox(8);
        welcomeCard.getStyleClass().add("card-accent");

        Label greet = new Label("Good day, Dr. " + currentDoctor.getName() + " 👨‍⚕️");
        greet.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #00695C;");

        Label sub = new Label(currentDoctor.getSpecialization() + "  •  " + currentDoctor.getSchedule());
        sub.getStyleClass().add("subtitle");

        welcomeCard.getChildren().addAll(greet, sub);

        // Stats
        List<Appointment> myAppts = AppointmentDAO.listByDoctor(currentDoctor.getId());
        long todayCount = myAppts.stream()
                .filter(a -> "booked".equals(a.getStatus()))
                .filter(a -> a.getDateTime() != null
                        && a.getDateTime().toLocalDate().equals(LocalDate.now()))
                .count();
        long upcomingCount = myAppts.stream()
                .filter(a -> "booked".equals(a.getStatus()))
                .filter(a -> a.getDateTime() != null
                        && a.getDateTime().isAfter(LocalDateTime.now()))
                .count();
        long recordsCount = HealthRecordDAO.listByDoctor(currentDoctor.getId()).size();

        HBox stats = new HBox(16);
        stats.getChildren().addAll(
                miniStat("📅", String.valueOf(todayCount), "Today"),
                miniStat("🕒", String.valueOf(upcomingCount), "Upcoming"),
                miniStat("📊", String.valueOf(recordsCount), "Records Written")
        );

        // Profile card
        VBox profileCard = createCard("👨‍⚕️  My Profile", buildProfilePane());

        root.getChildren().addAll(welcomeCard, stats, profileCard);
        return wrapInScroll(root);
    }

    private VBox miniStat(String icon, String value, String label) {
        VBox card = new VBox(4);
        card.getStyleClass().add("stat-card");
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(160);

        Label ico = new Label(icon);
        ico.setStyle("-fx-font-size: 24px;");

        Label val = new Label(value);
        val.getStyleClass().add("stat-number");

        Label lbl = new Label(label);
        lbl.getStyleClass().add("stat-label");

        card.getChildren().addAll(ico, val, lbl);
        return card;
    }

    private Region buildProfilePane() {
        GridPane grid = new GridPane();
        grid.setVgap(10); grid.setHgap(20);
        grid.setPadding(new Insets(12, 0, 0, 0));

        addRow(grid, 0, "Doctor ID", "#" + currentDoctor.getId());
        addRow(grid, 1, "Full Name", "Dr. " + currentDoctor.getName());
        addRow(grid, 2, "Specialization", currentDoctor.getSpecialization());
        addRow(grid, 3, "Schedule", currentDoctor.getSchedule());

        return grid;
    }

    private void addRow(GridPane grid, int row, String label, String value) {
        Label lbl = new Label(label + ":");
        lbl.getStyleClass().add("form-label");
        Label val = new Label(value);
        val.setStyle("-fx-font-size: 14px; -fx-text-fill: #1a2332;");
        grid.add(lbl, 0, row);
        grid.add(val, 1, row);
    }

    // ═══════════════════════════════════════════
    //  APPOINTMENTS TAB
    // ═══════════════════════════════════════════
    private Region buildAppointmentsTab() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(24));

        TableView<Appointment> table = new TableView<>();
        table.getStyleClass().add("table-view");
        table.setPlaceholder(new Label("No appointments found."));

        TableColumn<Appointment, Integer> idCol = col("ID", "id", 55);

        TableColumn<Appointment, Integer> patientCol = new TableColumn<>("Patient");
        patientCol.setPrefWidth(200);
        patientCol.setCellValueFactory(new PropertyValueFactory<>("patientId"));
        patientCol.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(Integer id, boolean empty) {
                super.updateItem(id, empty);
                if (empty || id == null) { setText(null); return; }
                Patient p = PatientDAO.findByUserId(id);
                // findByUserId resolves via user_id; for patient objects use the patient id directly
                setText(p != null ? p.getName() : "Patient #" + id);
            }
        });

        TableColumn<Appointment, LocalDateTime> dtCol = new TableColumn<>("Date & Time");
        dtCol.setCellValueFactory(new PropertyValueFactory<>("dateTime"));
        dtCol.setPrefWidth(180);
        dtCol.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(LocalDateTime dt, boolean empty) {
                super.updateItem(dt, empty);
                setText(empty || dt == null ? null : dt.format(DT_FMT));
            }
        });

        TableColumn<Appointment, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(110);
        statusCol.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) { setGraphic(null); setText(null); return; }
                Label badge = new Label(capitalize(s));
                badge.getStyleClass().add(
                        "booked".equals(s) ? "badge-booked"
                        : "completed".equals(s) ? "badge-completed" : "badge-cancelled");
                setGraphic(badge); setText(null);
            }
        });

        TableColumn<Appointment, String> notesCol = col("Notes", "notes", 220);

        // Mark complete / cancel
        TableColumn<Appointment, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setPrefWidth(150);
        actionCol.setCellFactory(c -> new TableCell<>() {
            private final Button completeBtn = new Button("Complete");
            private final Button cancelBtn = new Button("Cancel");
            private final HBox box = new HBox(6, completeBtn, cancelBtn);
            {
                completeBtn.setStyle("-fx-background-color: #E0F7F2; -fx-text-fill: #00695C; " +
                        "-fx-background-radius: 6; -fx-font-size: 11px; -fx-padding: 4 8; -fx-cursor: hand;");
                cancelBtn.getStyleClass().add("btn-danger");
                cancelBtn.setStyle("-fx-font-size: 11px; -fx-padding: 4 8;");
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty) { setGraphic(null); return; }
                Appointment a = getTableView().getItems().get(getIndex());
                boolean isBooked = "booked".equals(a.getStatus());
                completeBtn.setDisable(!isBooked);
                cancelBtn.setDisable(!isBooked);

                completeBtn.setOnAction(e -> {
                    // Update status via DAO (reuse cancel logic pattern)
                    database.DBConnection.connect(); // ensure connected
                    updateStatus(a.getId(), "completed");
                    table.getItems().setAll(AppointmentDAO.listByDoctor(currentDoctor.getId()));
                });
                cancelBtn.setOnAction(e -> {
                    AppointmentDAO.cancel(a.getId());
                    table.getItems().setAll(AppointmentDAO.listByDoctor(currentDoctor.getId()));
                });
                setGraphic(box);
            }
        });

        table.getColumns().addAll(idCol, patientCol, dtCol, statusCol, notesCol, actionCol);
        table.getItems().setAll(AppointmentDAO.listByDoctor(currentDoctor.getId()));

        Button refreshBtn = new Button("↻  Refresh");
        refreshBtn.getStyleClass().add("btn-secondary");
        refreshBtn.setOnAction(e -> table.getItems()
                .setAll(AppointmentDAO.listByDoctor(currentDoctor.getId())));

        VBox card = createCard("📋  My Appointments",
                wrap(new VBox(12) {{ setPadding(new Insets(8, 0, 0, 0)); getChildren().addAll(refreshBtn, table); }}));
        root.getChildren().add(card);
        return wrapInScroll(root);
    }

    private void updateStatus(int appointmentId, String status) {
        String sql = "UPDATE appointments SET status = ? WHERE id = ?";
        try (java.sql.Connection conn = database.DBConnection.connect();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, appointmentId);
            ps.executeUpdate();
        } catch (java.sql.SQLException e) {
            System.err.println("Status update failed: " + e.getMessage());
        }
    }

    // ═══════════════════════════════════════════
    //  ADD HEALTH RECORD TAB
    // ═══════════════════════════════════════════
    private Region buildAddRecordTab() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(24));

        VBox card = createCard("➕  Write Health Record", buildRecordForm());
        root.getChildren().add(card);
        return wrapInScroll(root);
    }

    private Region buildRecordForm() {
        GridPane grid = new GridPane();
        grid.setVgap(16); grid.setHgap(20);
        grid.setPadding(new Insets(16, 0, 8, 0));

        // Patient selector (from doctor's appointments)
        ComboBox<String> patientCombo = new ComboBox<>();
        patientCombo.setPromptText("Select patient");
        patientCombo.getStyleClass().add("combo-box");
        patientCombo.setMaxWidth(Double.MAX_VALUE);

        // Populate from appointments
        AppointmentDAO.listByDoctor(currentDoctor.getId()).stream()
                .filter(a -> !"cancelled".equals(a.getStatus()))
                .mapToInt(Appointment::getPatientId)
                .distinct()
                .forEach(pid -> {
                    // Try to find patient by their id (not user_id)
                    patientCombo.getItems().add("Patient #" + pid);
                });

        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.getStyleClass().add("date-picker");

        TextArea notesArea = new TextArea();
        notesArea.setPromptText("Diagnosis, treatment, prescriptions, observations...");
        notesArea.setPrefRowCount(5);
        notesArea.getStyleClass().add("text-area");

        Button saveBtn = new Button("Save Health Record");
        saveBtn.getStyleClass().add("btn-elec");

        Label msg = new Label();
        msg.setWrapText(true);

        ColumnConstraints col0 = new ColumnConstraints(160);
        ColumnConstraints col1 = new ColumnConstraints(); col1.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(col0, col1);

        grid.add(formLabel("Patient"), 0, 0);        grid.add(patientCombo, 1, 0);
        grid.add(formLabel("Record Date"), 0, 1);    grid.add(datePicker, 1, 1);
        grid.add(formLabel("Clinical Notes"), 0, 2); grid.add(notesArea, 1, 2);
        grid.add(saveBtn, 1, 3);
        grid.add(msg, 1, 4);

        saveBtn.setOnAction(e -> {
            String sel = patientCombo.getValue();
            LocalDate date = datePicker.getValue();
            String notes = notesArea.getText().trim();

            if (sel == null || date == null || notes.isEmpty()) {
                msg.getStyleClass().setAll("message-error");
                msg.setText("⚠ Please complete all fields.");
                return;
            }

            // Extract numeric patient ID from "Patient #N"
            int patientId;
            try {
                patientId = Integer.parseInt(sel.replace("Patient #", "").trim());
            } catch (NumberFormatException ex) {
                msg.getStyleClass().setAll("message-error");
                msg.setText("⚠ Invalid patient selection.");
                return;
            }

            HealthRecord record = new HealthRecord(
                    patientId, currentDoctor.getId(), date, notes);

            if (HealthRecordDAO.create(record)) {
                msg.getStyleClass().setAll("message-success");
                msg.setText("✔  Health record saved successfully.");
                notesArea.clear();
                patientCombo.setValue(null);
                datePicker.setValue(LocalDate.now());
            } else {
                msg.getStyleClass().setAll("message-error");
                msg.setText("✖  Failed to save record. Please try again.");
            }
        });

        return grid;
    }

    // ═══════════════════════════════════════════
    //  RECORDS TAB
    // ═══════════════════════════════════════════
    private Region buildRecordsTab() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(24));

        TableView<HealthRecord> table = new TableView<>();
        table.getStyleClass().add("table-view");
        table.setPlaceholder(new Label("No health records found."));

        TableColumn<HealthRecord, Integer> idCol = col("ID", "id", 55);
        TableColumn<HealthRecord, Integer> patientCol = new TableColumn<>("Patient ID");
        patientCol.setCellValueFactory(new PropertyValueFactory<>("patientId"));
        patientCol.setPrefWidth(100);

        TableColumn<HealthRecord, LocalDate> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("recordDate"));
        dateCol.setPrefWidth(140);
        dateCol.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(LocalDate d, boolean empty) {
                super.updateItem(d, empty);
                setText(empty || d == null ? null : d.format(D_FMT));
            }
        });

        TableColumn<HealthRecord, String> notesCol = col("Notes", "notes", 380);

        table.getColumns().addAll(idCol, patientCol, dateCol, notesCol);
        table.getItems().setAll(HealthRecordDAO.listByDoctor(currentDoctor.getId()));

        Button refreshBtn = new Button("↻  Refresh");
        refreshBtn.getStyleClass().add("btn-secondary");
        refreshBtn.setOnAction(e -> table.getItems()
                .setAll(HealthRecordDAO.listByDoctor(currentDoctor.getId())));

        VBox card = createCard("📊  Health Records I've Written",
                wrap(new VBox(12) {{ setPadding(new Insets(8, 0, 0, 0)); getChildren().addAll(refreshBtn, table); }}));
        root.getChildren().add(card);
        return wrapInScroll(root);
    }

    // ═══════════════════════════════════════════
    //  NO PROFILE PANE
    // ═══════════════════════════════════════════
    private Region buildNoProfilePane() {
        VBox pane = new VBox(16);
        pane.setAlignment(Pos.CENTER);
        pane.setPadding(new Insets(60));

        Label icon = new Label("⚠");
        icon.setStyle("-fx-font-size: 48px;");

        Label msg = new Label("No doctor profile linked to your account.");
        msg.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #495057;");

        Label sub = new Label("Please ask an administrator to register your doctor profile.");
        sub.getStyleClass().add("subtitle");

        pane.getChildren().addAll(icon, msg, sub);
        return pane;
    }

    // ─────────────────────────────────────────────
    //  HELPERS
    // ─────────────────────────────────────────────
    private ScrollPane wrapInScroll(VBox content) {
        ScrollPane sp = new ScrollPane(content);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        sp.getStyleClass().add("scroll-pane");
        return sp;
    }

    private VBox wrap(VBox v) { return v; }

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
