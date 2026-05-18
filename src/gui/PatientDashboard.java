package gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import dao.AppointmentDAO;
import dao.DoctorDAO;
import dao.PatientDAO;
import dao.HealthRecordDAO;
import models.Appointment;
import models.Doctor;
import models.HealthRecord;
import models.Patient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * PatientDashboard — book & manage appointments, view health records.
 * All data operations go through DAOs; patient ID resolved from logged-in user.
 */
public class PatientDashboard extends BaseDashboard {

    private Patient currentPatient;
    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("MMM d, yyyy  HH:mm");
    private static final DateTimeFormatter D_FMT  = DateTimeFormatter.ofPattern("MMM d, yyyy");

    public PatientDashboard(String role, String username) {
        super(role, username);
    }

    @Override
    public void start(javafx.stage.Stage s) {
        // Resolve current patient before building UI
        currentPatient = PatientDAO.findByUserId(currentUserId);
        super.start(s);
    }

    @Override
    protected Region createContent() {
        if (currentPatient == null) {
            return buildNoProfilePane();
        }

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab overviewTab = new Tab("🏠  Overview");
        overviewTab.setContent(buildOverviewTab());

        Tab bookTab = new Tab("📅  Book Appointment");
        bookTab.setContent(buildBookTab());

        Tab apptsTab = new Tab("📋  My Appointments");
        apptsTab.setContent(buildAppointmentsTab());

        Tab recordsTab = new Tab("📊  Health Records");
        recordsTab.setContent(buildRecordsTab());

        tabPane.getTabs().addAll(overviewTab, bookTab, apptsTab, recordsTab);
        return tabPane;
    }

    // ═══════════════════════════════════════════
    //  OVERVIEW TAB
    // ═══════════════════════════════════════════
    private Region buildOverviewTab() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(24));

        // Welcome card
        VBox welcomeCard = new VBox(8);
        welcomeCard.getStyleClass().add("card-accent");

        Label greet = new Label("Welcome back, " + currentPatient.getName() + "! 👋");
        greet.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #00695C;");

        Label sub = new Label("Stay on top of your health — your appointments and records are all here.");
        sub.getStyleClass().add("subtitle");

        welcomeCard.getChildren().addAll(greet, sub);

        // Stats row
        List<Appointment> myAppts = AppointmentDAO.listByPatient(currentPatient.getId());
        List<HealthRecord> myRecords = HealthRecordDAO.listByPatient(currentPatient.getId());
        long bookedCount = myAppts.stream().filter(a -> "booked".equals(a.getStatus())).count();
        long completedCount = myAppts.stream().filter(a -> "completed".equals(a.getStatus())).count();

        HBox stats = new HBox(16);
        stats.getChildren().addAll(
                miniStat("📅", String.valueOf(bookedCount), "Upcoming"),
                miniStat("✅", String.valueOf(completedCount), "Completed"),
                miniStat("📊", String.valueOf(myRecords.size()), "Health Records")
        );

        // Profile card
        VBox profileCard = createCard("👤  My Profile", buildProfilePane());

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

        addProfileRow(grid, 0, "Full Name", currentPatient.getName());
        addProfileRow(grid, 1, "Date of Birth",
                currentPatient.getDob() != null ? currentPatient.getDob().format(D_FMT) : "—");
        addProfileRow(grid, 2, "Contact", currentPatient.getContact());
        addProfileRow(grid, 3, "Patient ID", "#" + currentPatient.getId());

        return grid;
    }

    private void addProfileRow(GridPane grid, int row, String label, String value) {
        Label lbl = new Label(label + ":");
        lbl.getStyleClass().add("form-label");

        Label val = new Label(value);
        val.setStyle("-fx-font-size: 14px; -fx-text-fill: #1a2332;");

        grid.add(lbl, 0, row);
        grid.add(val, 1, row);
    }

    // ═══════════════════════════════════════════
    //  BOOK APPOINTMENT TAB
    // ═══════════════════════════════════════════
    private Region buildBookTab() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(24));

        VBox card = createCard("📅  Book a New Appointment", buildBookForm());
        root.getChildren().add(card);
        return wrapInScroll(root);
    }

    private Region buildBookForm() {
        GridPane grid = new GridPane();
        grid.setVgap(16); grid.setHgap(20);
        grid.setPadding(new Insets(16, 0, 8, 0));

        // Doctor selector
        ComboBox<String> doctorCombo = new ComboBox<>();
        doctorCombo.setPromptText("Select a doctor");
        doctorCombo.getStyleClass().add("combo-box");
        doctorCombo.setMaxWidth(Double.MAX_VALUE);

        List<Doctor> doctors = DoctorDAO.listAll();
        doctors.forEach(d -> doctorCombo.getItems()
                .add(d.getId() + " — Dr. " + d.getName() + "  (" + d.getSpecialization() + ")"));

        // Date / time
        DatePicker datePicker = new DatePicker(LocalDate.now().plusDays(1));
        datePicker.getStyleClass().add("date-picker");

        ComboBox<String> timeCombo = new ComboBox<>();
        timeCombo.getStyleClass().add("combo-box");
        timeCombo.setMaxWidth(Double.MAX_VALUE);
        for (int h = 8; h <= 17; h++) {
            timeCombo.getItems().add(String.format("%02d:00", h));
            timeCombo.getItems().add(String.format("%02d:30", h));
        }
        timeCombo.setValue("09:00");

        TextArea notesArea = new TextArea();
        notesArea.setPromptText("Optional notes for the doctor...");
        notesArea.setPrefRowCount(3);
        notesArea.getStyleClass().add("text-area");

        Button bookBtn = new Button("Book Appointment");
        bookBtn.getStyleClass().add("btn-elec");

        Label msg = new Label();
        msg.setWrapText(true);

        ColumnConstraints col0 = new ColumnConstraints(160);
        ColumnConstraints col1 = new ColumnConstraints(); col1.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(col0, col1);

        grid.add(formLabel("Doctor"), 0, 0);        grid.add(doctorCombo, 1, 0);
        grid.add(formLabel("Date"), 0, 1);          grid.add(datePicker, 1, 1);
        grid.add(formLabel("Time"), 0, 2);          grid.add(timeCombo, 1, 2);
        grid.add(formLabel("Notes (optional)"), 0, 3); grid.add(notesArea, 1, 3);
        grid.add(bookBtn, 1, 4);
        grid.add(msg, 1, 5);

        bookBtn.setOnAction(e -> {
            String doctorSel = doctorCombo.getValue();
            LocalDate date = datePicker.getValue();
            String time = timeCombo.getValue();

            if (doctorSel == null || date == null || time == null) {
                msg.getStyleClass().setAll("message-error");
                msg.setText("⚠ Please select a doctor, date, and time.");
                return;
            }

            if (date.isBefore(LocalDate.now())) {
                msg.getStyleClass().setAll("message-error");
                msg.setText("⚠ Appointment date must be in the future.");
                return;
            }

            int doctorId = Integer.parseInt(doctorSel.split(" — ")[0]);
            LocalTime lt = LocalTime.parse(time);
            LocalDateTime dateTime = LocalDateTime.of(date, lt);

            // Conflict check
            if (AppointmentDAO.hasConflict(currentPatient.getId(), doctorId, dateTime)) {
                msg.getStyleClass().setAll("message-error");
                msg.setText("✖  Scheduling conflict — choose a different time or doctor.");
                return;
            }

            Appointment appt = new Appointment(
                    currentPatient.getId(), doctorId, dateTime, "booked",
                    notesArea.getText().trim());

            if (AppointmentDAO.create(appt)) {
                msg.getStyleClass().setAll("message-success");
                msg.setText("✔  Appointment booked for " + dateTime.format(DT_FMT) + "!");
                notesArea.clear();
                doctorCombo.setValue(null);
                datePicker.setValue(LocalDate.now().plusDays(1));
                timeCombo.setValue("09:00");
            } else {
                msg.getStyleClass().setAll("message-error");
                msg.setText("✖  Booking failed. Please try again.");
            }
        });

        return grid;
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

        TableColumn<Appointment, Integer> docCol = new TableColumn<>("Doctor");
        docCol.setPrefWidth(200);
        docCol.setCellValueFactory(new PropertyValueFactory<>("doctorId"));
        docCol.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(Integer id, boolean empty) {
                super.updateItem(id, empty);
                if (empty || id == null) { setText(null); return; }
                Doctor d = DoctorDAO.findByUserId(id);
                setText(d != null ? "Dr. " + d.getName() : "Doctor #" + id);
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

        // Cancel action
        TableColumn<Appointment, Void> actionCol = new TableColumn<>("Action");
        actionCol.setPrefWidth(100);
        actionCol.setCellFactory(c -> new TableCell<>() {
            private final Button btn = new Button("Cancel");
            { btn.getStyleClass().add("btn-danger");
              btn.setStyle("-fx-font-size: 11px; -fx-padding: 4 10;"); }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty) { setGraphic(null); return; }
                Appointment a = getTableView().getItems().get(getIndex());
                btn.setDisable(!"booked".equals(a.getStatus()));
                btn.setOnAction(e -> {
                    if (AppointmentDAO.cancel(a.getId())) {
                        table.getItems().setAll(AppointmentDAO.listByPatient(currentPatient.getId()));
                    }
                });
                setGraphic(btn);
            }
        });

        table.getColumns().addAll(idCol, docCol, dtCol, statusCol, actionCol);
        table.getItems().setAll(AppointmentDAO.listByPatient(currentPatient.getId()));

        Button refreshBtn = new Button("↻  Refresh");
        refreshBtn.getStyleClass().add("btn-secondary");
        refreshBtn.setOnAction(e -> table.getItems()
                .setAll(AppointmentDAO.listByPatient(currentPatient.getId())));

        VBox card = createCard("📋  My Appointments",
                wrapTableInVBox(table, refreshBtn));
        root.getChildren().add(card);
        return wrapInScroll(root);
    }

    // ═══════════════════════════════════════════
    //  HEALTH RECORDS TAB
    // ═══════════════════════════════════════════
    private Region buildRecordsTab() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(24));

        TableView<HealthRecord> table = new TableView<>();
        table.getStyleClass().add("table-view");
        table.setPlaceholder(new Label("No health records found."));

        TableColumn<HealthRecord, Integer> idCol = col("ID", "id", 55);

        TableColumn<HealthRecord, Integer> docCol = new TableColumn<>("Doctor");
        docCol.setPrefWidth(200);
        docCol.setCellValueFactory(new PropertyValueFactory<>("doctorId"));
        docCol.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(Integer id, boolean empty) {
                super.updateItem(id, empty);
                if (empty || id == null) { setText(null); return; }
                Doctor d = DoctorDAO.findByUserId(id);
                setText(d != null ? "Dr. " + d.getName() : "Doctor #" + id);
            }
        });

        TableColumn<HealthRecord, LocalDate> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("recordDate"));
        dateCol.setPrefWidth(140);
        dateCol.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(LocalDate d, boolean empty) {
                super.updateItem(d, empty);
                setText(empty || d == null ? null : d.format(D_FMT));
            }
        });

        TableColumn<HealthRecord, String> notesCol = col("Notes", "notes", 300);

        table.getColumns().addAll(idCol, docCol, dateCol, notesCol);
        table.getItems().setAll(HealthRecordDAO.listByPatient(currentPatient.getId()));

        Button refreshBtn = new Button("↻  Refresh");
        refreshBtn.getStyleClass().add("btn-secondary");
        refreshBtn.setOnAction(e -> table.getItems()
                .setAll(HealthRecordDAO.listByPatient(currentPatient.getId())));

        VBox card = createCard("📊  Health Records", wrapTableInVBox(table, refreshBtn));
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

        Label msg = new Label("No patient profile linked to your account.");
        msg.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #495057;");

        Label sub = new Label("Please ask an administrator to register your patient profile.");
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

    private VBox wrapTableInVBox(TableView<?> table, Button refresh) {
        VBox v = new VBox(12);
        v.setPadding(new Insets(8, 0, 0, 0));
        v.getChildren().addAll(refresh, table);
        return v;
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
