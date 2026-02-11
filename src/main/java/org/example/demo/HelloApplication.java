package org.example.demo;

import java.util.ArrayList;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;

import javafx.animation.FadeTransition;
import javafx.util.Duration;

import javafx.animation.TranslateTransition;

public class HelloApplication extends Application {

    private static final String CSS_PATH = "/org/example/demo/style.css";
    private static ArrayList<Teacher> teacherList = new ArrayList<>();
    private static ArrayList<Student> studentList = new ArrayList<>();

    @Override
    public void start(Stage stage) {
        stage.setTitle("Online Exam System");
        stage.setScene(createMainScene(stage));
        stage.show();
    }

    // Helper to add CSS
    private void applyStyle(Scene scene) {
        if (getClass().getResource(CSS_PATH) != null) {
            scene.getStylesheets().add(getClass().getResource(CSS_PATH).toExternalForm());
        }
    }

    private void applyEffects(Button btn) {
        // 1. Drop Shadow (Constant)
        DropShadow ds = new DropShadow();
        ds.setOffsetY(3.0);
        ds.setColor(Color.color(0, 0, 0, 0.5));
        btn.setEffect(ds);

        // 2. Hover Glow & Scale
        btn.setOnMouseEntered(e -> {
            btn.setScaleX(1.1);
            btn.setScaleY(1.1);
            // Glow effect on hover
            ds.setRadius(15);
            ds.setColor(Color.color(0.2, 0.6, 1.0, 0.7));
        });
        btn.setOnMouseExited(e -> {
            btn.setScaleX(1.0);
            btn.setScaleY(1.0);
            // Reset to normal shadow
            ds.setRadius(10);
            ds.setColor(Color.color(0, 0, 0, 0.5));
        });
    }

    private void applyLinkEffects(Hyperlink link) {
        link.setStyle("-fx-text-fill: #2980b9; -fx-font-size: 13px; -fx-underline: false;");

        link.setOnMouseEntered(e -> {
            link.setScaleX(1.05); // Subtle grow
            link.setScaleY(1.05);
            link.setStyle("-fx-text-fill: #3498db; -fx-underline: true; -fx-font-weight: bold;");
        });

        link.setOnMouseExited(e -> {
            link.setScaleX(1.0); // Reset
            link.setScaleY(1.0);
            link.setStyle("-fx-text-fill: #2980b9; -fx-underline: false; -fx-font-weight: normal;");
        });
    }

    private void playTransition(Pane root, boolean movingForward) {
        // 1. Set up the Slide
        double startX = movingForward ? 100 : -100; // Move just a little bit (50px) instead of 600
        root.setTranslateX(startX);

        TranslateTransition slide = new TranslateTransition(Duration.millis(800), root);
        slide.setFromX(startX);
        slide.setToX(0);

        // 2. Set up the Fade
        FadeTransition fade = new FadeTransition(Duration.millis(800), root);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);

        // 3. Play both together
        slide.play();
        fade.play();
    }

    // ================= MAIN ROLE SELECTION (PANE) =================
    private Scene createMainScene(Stage stage) {
        Pane root = new Pane();
        root.setStyle("-fx-background-color: #f4f4f4;");

        Label lblWelcome = new Label("Welcome to the Online Exam System");
        lblWelcome.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");
        lblWelcome.setLayoutX(130); // Manual X
        lblWelcome.setLayoutY(80);  // Manual Y

        Button btnStudent = new Button("Student");
        Button btnTeacher = new Button("Teacher");

        btnStudent.getStyleClass().add("role-button");
        btnTeacher.getStyleClass().add("role-button");

        // Setting Exact Coordinates and Sizes
        btnStudent.setLayoutX(150);
        btnStudent.setLayoutY(180);
        btnStudent.setPrefSize(120, 50);

        btnTeacher.setLayoutX(330);
        btnTeacher.setLayoutY(180);
        btnTeacher.setPrefSize(120, 50);
        applyEffects(btnStudent);
        applyEffects(btnTeacher);

        btnStudent.setOnAction(e -> stage.setScene(createStudentLoginScene(stage)));
        btnTeacher.setOnAction(e -> stage.setScene(createTeacherLoginScene(stage)));

        root.getChildren().addAll(lblWelcome, btnStudent, btnTeacher);

        Scene scene = new Scene(root, 600, 400);
        applyStyle(scene);
        playTransition(root, false);
        return scene;
    }

    // ================= TEACHER LOGIN (PANE) =================
    private Scene createTeacherLoginScene(Stage stage) {
        Pane root = new Pane();
        root.setStyle("-fx-background-color: white;");

        Label lblTitle = new Label("Teacher Login");
        lblTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        lblTitle.setLayoutX(220);
        lblTitle.setLayoutY(40);

        TextField txtUser = new TextField();
        txtUser.setPromptText("Email or Username");
        txtUser.setLayoutX(175);
        txtUser.setLayoutY(110);
        txtUser.setPrefWidth(250);

        PasswordField txtPass = new PasswordField();
        txtPass.setPromptText("Password");
        txtPass.setLayoutX(175);
        txtPass.setLayoutY(160);
        txtPass.setPrefWidth(250);

        Button btnLogin = new Button("Login");
        btnLogin.getStyleClass().add("role-button");
        btnLogin.setLayoutX(175);
        btnLogin.setLayoutY(210);
        btnLogin.setPrefSize(250, 40);
        applyEffects(btnLogin);

        btnLogin.setOnAction(e -> {
            String input = txtUser.getText();
            String pass = txtPass.getText();
            boolean success = teacherList.stream()
                    .anyMatch(t -> (t.getEmail().equals(input) || t.getUser().equals(input)) && t.getPassword().equals(pass));

            if (success) {
                //stage.setScene(createTeacherDashboard(stage, input));
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Login Error");
                alert.setHeaderText(null);
                alert.setContentText("Invalid Email or Password. Please try again!");
                alert.showAndWait();
            }
        });

        Hyperlink linkSignup = new Hyperlink("Don't have an account? Sign up here");
        linkSignup.setLayoutX(180);
        linkSignup.setLayoutY(260);
        applyLinkEffects(linkSignup);
        linkSignup.setOnAction(e -> stage.setScene(createTeacherSignupScene(stage)));

        Button btnBack = new Button("← Back");
        //btnBack.getStyleClass().add("role-button");
        btnBack.setLayoutX(10);
        btnBack.setLayoutY(10);
        applyEffects(btnBack);
        btnBack.setOnAction(e -> stage.setScene(createMainScene(stage)));

        root.getChildren().addAll(lblTitle, txtUser, txtPass, btnLogin, linkSignup, btnBack);

        Scene scene = new Scene(root, 600, 400);
        applyStyle(scene);
        playTransition(root, true);
        return scene;
    }

    // ================= TEACHER SIGNUP (PANE) =================
    private Scene createTeacherSignupScene(Stage stage) {
        Pane root = new Pane();

        Label lblTitle = new Label("Teacher Registration");
        lblTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        lblTitle.setLayoutX(180);
        lblTitle.setLayoutY(30);

        // Positioning inputs vertically manually
        TextField txtName = new TextField(); txtName.setPromptText("Full Name");
        txtName.setLayoutX(175); txtName.setLayoutY(90); txtName.setPrefWidth(250);

        TextField txtEmail = new TextField(); txtEmail.setPromptText("Email Address");
        txtEmail.setLayoutX(175); txtEmail.setLayoutY(140); txtEmail.setPrefWidth(250);

        PasswordField txtPass = new PasswordField(); txtPass.setPromptText("Create Password");
        txtPass.setLayoutX(175); txtPass.setLayoutY(190); txtPass.setPrefWidth(250);

        PasswordField txtConfirm = new PasswordField(); txtConfirm.setPromptText("Confirm Password");
        txtConfirm.setLayoutX(175); txtConfirm.setLayoutY(240); txtConfirm.setPrefWidth(250);

        Button btnRegister = new Button("Create Account");
        btnRegister.getStyleClass().add("role-button");
        btnRegister.setLayoutX(175);
        btnRegister.setLayoutY(300);
        btnRegister.setPrefSize(250, 40);
        applyEffects(btnRegister);

        btnRegister.setOnAction(e -> {
            String pass = txtPass.getText();
            String confirm = txtConfirm.getText();
            String name = txtName.getText();
            String email = txtEmail.getText();

            if (name.isEmpty() || email.isEmpty()|| pass.isEmpty()) {
                showError("Error", "All fields are required!");
            }

            else if (pass.equals(confirm)) {
                // Success logic
                teacherList.add(new Teacher(txtName.getText(), txtEmail.getText(), pass));
                showInfo("Success", "Account created for " + name + " successfully!");
                stage.setScene(createTeacherLoginScene(stage));
            }
            else if (!pass.equals(confirm)) {
                showError("Error", "Passwords do not match!");
            }

        });

        Button btnBack = new Button("← Back");
        btnBack.setLayoutX(10);
        btnBack.setLayoutY(10);
        applyEffects(btnBack);
        btnBack.setOnAction(e -> stage.setScene(createMainScene(stage)));

        root.getChildren().addAll(lblTitle, txtName, txtEmail, txtPass, txtConfirm, btnRegister, btnBack);

        Scene scene = new Scene(root, 600, 450);
        applyStyle(scene);
        playTransition(root, true);
        return scene;
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private Scene createStudentLoginScene(Stage stage) {
        Pane root = new Pane();
        root.setStyle("-fx-background-color: #ffffff;");

        Label lblTitle = new Label("Student Login");
        lblTitle.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #34495e;");
        lblTitle.setLayoutX(215);
        lblTitle.setLayoutY(40);

        // This field accepts either ID or Email
        TextField txtLoginInput = new TextField();
        txtLoginInput.setPromptText("Student ID or Email");
        txtLoginInput.setLayoutX(175);
        txtLoginInput.setLayoutY(120);
        txtLoginInput.setPrefWidth(250);
        txtLoginInput.setPrefHeight(35);

        PasswordField txtPass = new PasswordField();
        txtPass.setPromptText("Password");
        txtPass.setLayoutX(175);
        txtPass.setLayoutY(175);
        txtPass.setPrefWidth(250);
        txtPass.setPrefHeight(35);

        Button btnLogin = new Button("Login");
        btnLogin.getStyleClass().add("role-button"); // Ensure this is in your CSS
        btnLogin.setLayoutX(175);
        btnLogin.setLayoutY(235);
        btnLogin.setPrefSize(250, 45);
        applyEffects(btnLogin);

        btnLogin.setOnAction(e -> {
            String input = txtLoginInput.getText();
            String pass = txtPass.getText();

            // The Logic: check if input matches EITHER ID or Email
            boolean success = studentList.stream()
                    .anyMatch(s -> (s.getID().equalsIgnoreCase(input) || s.getEmail().equalsIgnoreCase(input))
                            && s.getPassword().equals(pass));

            if (success) {
                // Find the specific student to welcome them by name
                Student loggedInStudent = studentList.stream()
                        .filter(s -> s.getID().equalsIgnoreCase(input) || s.getEmail().equalsIgnoreCase(input))
                        .findFirst().get();

                showInfo("Login Success", "Welcome back, " + loggedInStudent.getName() + "!");
                // stage.setScene(createStudentDashboard(stage, loggedInStudent));
            } else {
                showError("Login Failed", "Invalid ID/Email or Password.");
            }
        });

        Hyperlink linkSignup = new Hyperlink("Don't have an account? Sign up here");
        linkSignup.setLayoutX(180);
        linkSignup.setLayoutY(290);
        applyLinkEffects(linkSignup);
        linkSignup.setOnAction(e -> stage.setScene(createStudentSignupScene(stage)));

        Button btnBack = new Button("← Back");
        btnBack.setLayoutX(15);
        btnBack.setLayoutY(15);
        applyEffects(btnBack);
        btnBack.setOnAction(e -> stage.setScene(createMainScene(stage)));

        root.getChildren().addAll(lblTitle, txtLoginInput, txtPass, btnLogin, btnBack, linkSignup);

        Scene scene = new Scene(root, 600, 400);
        applyStyle(scene);
        playTransition(root, true);
        return scene;
    }

    private Scene createStudentSignupScene(Stage stage) {
        Pane root = new Pane();
        root.setStyle("-fx-background-color: #f9f9f9;");

        Label lblTitle = new Label("Student Registration");
        lblTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        lblTitle.setLayoutX(180);
        lblTitle.setLayoutY(30);

        // Form Fields
        TextField txtID = new TextField(); txtID.setPromptText("Student ID");
        txtID.setLayoutX(175); txtID.setLayoutY(90); txtID.setPrefWidth(250);

        TextField txtName = new TextField(); txtName.setPromptText("Full Name");
        txtName.setLayoutX(175); txtName.setLayoutY(140); txtName.setPrefWidth(250);

        TextField txtEmail = new TextField(); txtEmail.setPromptText("Email Address");
        txtEmail.setLayoutX(175); txtEmail.setLayoutY(190); txtEmail.setPrefWidth(250);

        PasswordField txtPass = new PasswordField(); txtPass.setPromptText("Password");
        txtPass.setLayoutX(175); txtPass.setLayoutY(240); txtPass.setPrefWidth(250);

        PasswordField txtConfirm = new PasswordField(); txtConfirm.setPromptText("Confirm Password");
        txtConfirm.setLayoutX(175); txtConfirm.setLayoutY(290); txtConfirm.setPrefWidth(250);

        Button btnRegister = new Button("Register");
        btnRegister.getStyleClass().add("role-button");
        btnRegister.setLayoutX(175);
        btnRegister.setLayoutY(350);
        btnRegister.setPrefSize(250, 40);
        applyEffects(btnRegister);

        btnRegister.setOnAction(e -> {
            String id = txtID.getText();
            String name = txtName.getText();
            String email = txtEmail.getText();
            String pass = txtPass.getText();
            String confirm = txtConfirm.getText();

            if (id.isEmpty() || name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                showError("Error", "All fields are required!");
            }
            else if (!pass.equals(confirm)) {
                showError("Error", "Passwords do not match!");
            }
            else {
                // Add to the initialized studentList
                studentList.add(new Student(id, name, email, pass));
                showInfo("Success", "Account created for " + name + " successfully!");
                stage.setScene(createStudentLoginScene(stage));
            }
        });

        Button btnBack = new Button("← Back");
        btnBack.setLayoutX(10);
        btnBack.setLayoutY(10);
        applyEffects(btnBack);
        btnBack.setOnAction(e -> stage.setScene(createStudentLoginScene(stage)));

        root.getChildren().addAll(lblTitle, txtID, txtName, txtEmail, txtPass, txtConfirm, btnRegister, btnBack);

        Scene scene = new Scene(root, 600, 480);
        applyStyle(scene);
        playTransition(root, true);
        return scene;
    }

}