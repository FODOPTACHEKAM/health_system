package gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import dao.UserDAO;
import dao.AppointmentDAO;
import models.Appointment;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * BaseDashboard — shared shell for Admin, Doctor, and Patient dashboards.
 * Provides: sidebar navigation, top header, content area, and background reminder thread.
 */
public abstract class BaseDashboard extends Application {

    protected Stage stage;
    protected String userRole;
    protected String username;
    protected int currentUserId;

    protected BorderPane mainLayout;
    protected VBox sidebar;
    protected HBox header;
    protected Region contentArea;

    // Background reminder thread
    private Timer reminderTimer;
    private Label reminderLabel;
    private HBox reminderBanner;

    public BaseDashboard(String role, String user) {
        this.userRole = role;
        this.username = user;
        this.currentUserId = UserDAO.getIdByUsername(user);
    }

    @Override
    public void start(Stage primaryStage) {
        this.stage = primaryStage;
        mainLayout = new BorderPane();
        mainLayout.setStyle("-fx-background-color: #f0f2f5;");

        sidebar = buildSidebar();
        header = buildHeader();
        contentArea = createContent();

        // Load stylesheet — resolve relative to classpath root
        String css = getClass().getResource("/gui/styles.css") != null
                ? getClass().getResource("/gui/styles.css").toExternalForm()
                : "styles.css";

        VBox centerStack = new VBox(0, header, buildReminderBanner(), contentArea);
        VBox.setVgrow(contentArea, Priority.ALWAYS);

        mainLayout.setLeft(sidebar);
        mainLayout.setCenter(centerStack);

        Scene scene = new Scene(mainLayout, 1100, 720);
        try { scene.getStylesheets().add(css); } catch (Exception ignored) {}

        stage.setTitle("HealthCare Pro — " + capitalize(userRole) + " | " + username);
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.setScene(scene);
        stage.show();

        // Kick off background reminder thread (multithreading requirement)
        startReminderThread();
    }

    // ─────────────────────────────────────────────
    //  ABSTRACT — subclasses provide the main pane
    // ─────────────────────────────────────────────
    protected abstract Region createContent();

    // ─────────────────────────────────────────────
    //  SIDEBAR
    // ─────────────────────────────────────────────
    private VBox buildSidebar() {
        VBox sb = new VBox(0);
        sb.getStyleClass().add("sidebar");

        // ── Logo block ──
        VBox logoSection = new VBox(4);
        logoSection.getStyleClass().add("sidebar-logo-section");
        logoSection.setPadding(new Insets(28, 20, 20, 20));

        Label appName = new Label("HealthCare Pro");
        appName.getStyleClass().add("logo");

        Label tagline = new Label("Your Health, Our Priority");
        tagline.getStyleClass().add("logo-tagline");

        logoSection.getChildren().addAll(appName, tagline);

        // ── Nav section ──
        VBox navSection = new VBox(2);
        navSection.setPadding(new Insets(16, 0, 0, 0));

        Label navLabel = new Label("NAVIGATION");
        navLabel.getStyleClass().add("nav-section-label");

        navSection.getChildren().add(navLabel);
        navSection.getChildren().addAll(buildNavButtons());

        // ── Spacer + logout ──
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button logoutBtn = new Button("🚪  Logout");
        logoutBtn.getStyleClass().add("nav-btn-logout");
        logoutBtn.setMaxWidth(Double.MAX_VALUE);
        logoutBtn.setOnAction(e -> handleLogout());

        VBox bottomSection = new VBox(0);
        bottomSection.setPadding(new Insets(0, 0, 16, 0));
        bottomSection.getChildren().add(logoutBtn);

        // ── User info at bottom ──
        VBox userInfo = new VBox(2);
        userInfo.setPadding(new Insets(12, 16, 16, 16));
        userInfo.setStyle("-fx-background-color: rgba(0,0,0,0.15); -fx-background-radius: 10;");
        Insets userInfoMargin = new Insets(0, 12, 0, 12);

        Label userNameLabel = new Label("👤  " + username);
        userNameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px;");

        Label roleLabel = new Label(capitalize(userRole));
        roleLabel.setStyle("-fx-text-fill: #00D4AA; -fx-font-size: 11px; -fx-padding: 0 0 0 24;");

        userInfo.getChildren().addAll(userNameLabel, roleLabel);
        VBox.setMargin(userInfo, userInfoMargin);

        sb.getChildren().addAll(logoSection, navSection, spacer, bottomSection, userInfo,
                new Region() {{ setMinHeight(12); }});
        return sb;
    }

    /** Subclasses can override to add extra nav buttons before logout. Default buttons for all. */
    protected java.util.List<Button> buildNavButtons() {
        Button homeBtn = navButton("🏠  Dashboard", true);
        homeBtn.setOnAction(e -> {
            contentArea = createContent();
            ((VBox) mainLayout.getCenter()).getChildren().set(2, contentArea);
        });
        return java.util.List.of(homeBtn);
    }

    protected Button navButton(String text, boolean active) {
        Button btn = new Button(text);
        btn.getStyleClass().add(active ? "nav-btn-active" : "nav-btn");
        btn.setMaxWidth(Double.MAX_VALUE);
        return btn;
    }

    // ─────────────────────────────────────────────
    //  HEADER
    // ─────────────────────────────────────────────
    private HBox buildHeader() {
        HBox hdr = new HBox(16);
        hdr.getStyleClass().add("header");
        hdr.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(capitalize(userRole) + " Dashboard");
        titleLabel.getStyleClass().add("header-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label badge = new Label(capitalize(userRole));
        badge.getStyleClass().add("header-badge");

        Label timeLabel = new Label();
        timeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d;");

        // Update time every second (another use of threading)
        javafx.animation.Timeline clock = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.seconds(1), ev -> {
                    timeLabel.setText(LocalDateTime.now()
                            .format(DateTimeFormatter.ofPattern("EEE, MMM d  •  HH:mm:ss")));
                }));
        clock.setCycleCount(javafx.animation.Animation.INDEFINITE);
        clock.play();

        hdr.getChildren().addAll(titleLabel, spacer, timeLabel, badge);
        return hdr;
    }

    // ─────────────────────────────────────────────
    //  REMINDER BANNER (hidden by default)
    // ─────────────────────────────────────────────
    private HBox buildReminderBanner() {
        reminderBanner = new HBox(10);
        reminderBanner.getStyleClass().add("reminder-banner");
        reminderBanner.setAlignment(Pos.CENTER_LEFT);
        reminderBanner.setVisible(false);
        reminderBanner.setManaged(false);

        reminderLabel = new Label();
        reminderLabel.getStyleClass().add("reminder-text");

        Button dismiss = new Button("✕");
        dismiss.getStyleClass().add("btn-icon");
        dismiss.setStyle("-fx-font-size: 11px; -fx-text-fill: #856404;");
        dismiss.setOnAction(e -> {
            reminderBanner.setVisible(false);
            reminderBanner.setManaged(false);
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        reminderBanner.getChildren().addAll(new Label("⏰"), reminderLabel, spacer, dismiss);
        return reminderBanner;
    }

    // ─────────────────────────────────────────────
    //  BACKGROUND REMINDER THREAD
    // ─────────────────────────────────────────────
    private void startReminderThread() {
        reminderTimer = new Timer("ReminderThread-" + username, true /* daemon */);
        reminderTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                checkUpcomingAppointments();
            }
        }, 3_000L, 60_000L); // First check after 3s, then every minute
    }

    private void checkUpcomingAppointments() {
        try {
            // Fetch appointments for current user (works for both patient and doctor)
            List<Appointment> upcoming;
            if ("patient".equals(userRole)) {
                int patientId = dao.PatientDAO.findByUserId(currentUserId) != null
                        ? dao.PatientDAO.findByUserId(currentUserId).getId() : -1;
                upcoming = (patientId > 0) ? AppointmentDAO.listByPatient(patientId)
                        : java.util.Collections.emptyList();
            } else if ("doctor".equals(userRole)) {
                int doctorId = dao.DoctorDAO.findByUserId(currentUserId) != null
                        ? dao.DoctorDAO.findByUserId(currentUserId).getId() : -1;
                upcoming = (doctorId > 0) ? AppointmentDAO.listByDoctor(doctorId)
                        : java.util.Collections.emptyList();
            } else {
                upcoming = java.util.Collections.emptyList();
            }

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime threshold = now.plusHours(24);

            long count = upcoming.stream()
                    .filter(a -> "booked".equals(a.getStatus()))
                    .filter(a -> a.getDateTime() != null
                            && a.getDateTime().isAfter(now)
                            && a.getDateTime().isBefore(threshold))
                    .count();

            if (count > 0) {
                final String msg = count == 1
                        ? "You have 1 appointment within the next 24 hours!"
                        : "You have " + count + " appointments within the next 24 hours!";
                Platform.runLater(() -> showReminder(msg));
            }
        } catch (Exception e) {
            System.err.println("[ReminderThread] " + e.getMessage());
        }
    }

    private void showReminder(String message) {
        if (reminderLabel != null) {
            reminderLabel.setText(message);
            reminderBanner.setVisible(true);
            reminderBanner.setManaged(true);
        }
    }

    // ─────────────────────────────────────────────
    //  UTILITIES
    // ─────────────────────────────────────────────
    protected VBox createCard(String title, Region content) {
        VBox card = new VBox(0);
        card.getStyleClass().add("card");

        Label cardTitle = new Label(title);
        cardTitle.getStyleClass().add("card-title");

        card.getChildren().addAll(cardTitle, content);
        VBox.setVgrow(content, Priority.ALWAYS);
        return card;
    }

    protected void handleLogout() {
        if (reminderTimer != null) reminderTimer.cancel();
        stage.close();
        Platform.runLater(() -> new LoginApp().start(new Stage()));
    }

    protected static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase();
    }

    /** Show a styled alert dialog. */
    protected void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.initOwner(stage);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
