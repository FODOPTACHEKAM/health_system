package gui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import dao.UserDAO;
import gui.AdminDashboard;
import gui.DoctorDashboard;
import gui.PatientDashboard;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Modality;
import models.User;
import javafx.stage.Stage;

public class LoginApp extends Application {
    private Stage primaryStage;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;

        // 1. Root Container
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: #f0f2f5;");

        // 2. The Main Card (White box with shadow)
        BorderPane mainCard = new BorderPane();
        mainCard.setMaxSize(900, 550);
        mainCard.setStyle("-fx-background-color: white; " +
                         "-fx-background-radius: 24px; " +
                         "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 20, 0, 0, 10);");

        // 3. Grid Layout for the split
        GridPane splitLayout = new GridPane();
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(45); 
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(55); 
        splitLayout.getColumnConstraints().addAll(col1, col2);

        // --- THE CRITICAL FIX FOR THE CIRCLED ZONE ---
        // This forces the row to take up 100% of the available height in the mainCard.
        RowConstraints rowConstraints = new RowConstraints();
        rowConstraints.setPercentHeight(100);
        rowConstraints.setVgrow(Priority.ALWAYS);
        splitLayout.getRowConstraints().add(rowConstraints);

        VBox promoPanel = createPromoPanel();
        VBox formPanel = createFormPanel();

        splitLayout.add(promoPanel, 0, 0);
        splitLayout.add(formPanel, 1, 0);

        mainCard.setCenter(splitLayout);
        root.getChildren().add(mainCard);

        // Application Icon (Retained)
        try {
            stage.getIcons().add(new Image("file:../../logo.png"));
        } catch (Exception e) {
            System.out.println("App icon not found.");
        }

        Scene scene = new Scene(root, 1050, 650);
        try { scene.getStylesheets().add(getClass().getResource("/gui/styles.css").toExternalForm()); } catch (Exception ignored) {}
        stage.setTitle("HealthCare Pro - Secure Portal");
        stage.setScene(scene);
        stage.show();
    }

    private VBox createPromoPanel() {
        VBox promo = new VBox(25);
        promo.setAlignment(Pos.CENTER);
        promo.setPadding(new Insets(40));
        
        // Background radius set to 24 on the left side to match the main card perfectly.
        promo.setStyle("-fx-background-color: linear-gradient(to bottom right, #E0F7F2, #B2DFDB); " +
                      "-fx-background-radius: 24 0 0 24;"); 

        ImageView logoView = new ImageView();
        try {
            logoView.setImage(new Image("file:../../logo.png"));
            logoView.setFitWidth(110);
            logoView.setPreserveRatio(true);
        } catch (Exception e) {}

        Label brand = new Label("HealthCare Pro");
        brand.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #00695C;");

        promo.getChildren().addAll(logoView, brand, new Label("Your Health, Our Priority"));
        VBox.setVgrow(promo, Priority.ALWAYS); // Ensure internal stretching
        return promo;
    }

    private VBox createFormPanel() {
        VBox form = new VBox(15);
        form.setAlignment(Pos.CENTER_LEFT);
        form.setPadding(new Insets(60));

        Label title = new Label("Welcome Back");
        title.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: #333;");

        TextField userField = new TextField();
        userField.setPromptText("Username");
        userField.getStyleClass().add("text-field");

        // PASSWORD SECTION WITH VIEW BUTTON (VIEW PASSWORD LOGIC)
        PasswordField passHidden = new PasswordField();
        passHidden.setPromptText("Password");

        TextField passShown = new TextField();
        passShown.setPromptText("Password");
        passShown.setManaged(false);
        passShown.setVisible(false);

        passHidden.getStyleClass().add("text-field");
        passShown.getStyleClass().add("text-field");
        passHidden.setStyle("-fx-padding: 0 40 0 10;");
        passShown.setStyle("-fx-padding: 0 40 0 10;");

        Button viewBtn = new Button("👁");
        viewBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-text-fill: #888;");
        
        StackPane passStack = new StackPane(passHidden, passShown, viewBtn);
        StackPane.setAlignment(viewBtn, Pos.CENTER_RIGHT);
        StackPane.setMargin(viewBtn, new Insets(0, 10, 0, 0));

        viewBtn.setOnAction(e -> {
            if (passHidden.isVisible()) {
                passShown.setText(passHidden.getText());
                passShown.setVisible(true);
                passShown.setManaged(true);
                passHidden.setVisible(false);
                passHidden.setManaged(false);
                viewBtn.setText("🙈");
            } else {
                passHidden.setText(passShown.getText());
                passHidden.setVisible(true);
                passHidden.setManaged(true);
                passShown.setVisible(false);
                passShown.setManaged(false);
                viewBtn.setText("👁");
            }
        });

        Button loginBtn = new Button("SIGN IN SECURELY");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setPrefHeight(50);
        loginBtn.getStyleClass().add("btn-elec");
        
        // Backend login logic
        loginBtn.setOnAction(e -> {
            String username = userField.getText().trim();
            String password = passHidden.isVisible() ? passHidden.getText() : passShown.getText();
            
            if (username.isEmpty() || password.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Input Error", "Please enter username and password.");
                return;
            }
            
            User user = UserDAO.findByUsername(username);
            if (user == null || !UserDAO.hashPassword(password).equals(user.getPasswordHash())) {
                showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid username or password.");
                return;
            }

            String role = user.getRole();
            Stage dashStage = new Stage();
            dashStage.initModality(Modality.NONE);
            dashStage.setTitle("HealthCare Pro Dashboard");

            try {
                switch (role) {
                    case "admin"   -> new AdminDashboard(role, username).start(dashStage);
                    case "doctor"  -> new DoctorDashboard(role, username).start(dashStage);
                    case "patient" -> new PatientDashboard(role, username).start(dashStage);
                    default -> {
                        showAlert(Alert.AlertType.ERROR, "Role Error", "Unknown role: " + role);
                        return;
                    }
                }
                primaryStage.close();
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Launch Error", "Failed to open dashboard: " + ex.getMessage());
            }
        });

        form.getChildren().addAll(title, new Label("Sign in to continue"), new Label("Username"), userField, new Label("Password"), passStack, loginBtn);
        return form;
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
            alert.initOwner(primaryStage);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        }
    
    public static void main(String[] args) { launch(args); }
}
