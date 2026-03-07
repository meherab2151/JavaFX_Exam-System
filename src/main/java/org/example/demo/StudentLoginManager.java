package org.example.demo;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import java.util.ArrayList;

public class StudentLoginManager {

    public static Scene createLoginScene(Stage stage, ArrayList<Student> studentList, HelloApplication mainApp) {
        Pane root = new Pane();
        root.setStyle("-fx-background-color: #f8f9fa;");

        Label lblTitle = new Label("Student Login");
        lblTitle.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        lblTitle.setLayoutX(215); lblTitle.setLayoutY(40);

        TextField txtInput = new TextField();
        txtInput.setPromptText("Student ID or Email");
        txtInput.setLayoutX(175); txtInput.setLayoutY(120); txtInput.setPrefWidth(250);

        PasswordField txtPass = new PasswordField();
        txtPass.setPromptText("Password");
        txtPass.setLayoutX(175); txtPass.setLayoutY(175); txtPass.setPrefWidth(250);

        Button btnLogin = new Button("Login");
        btnLogin.setLayoutX(175); btnLogin.setLayoutY(235); btnLogin.setPrefSize(250, 45);

        // Fix: Student login uses Green hover
        UIUtils.applyButtonEffects(btnLogin, "#2ecc71");

        btnLogin.setOnAction(e -> {
            String input = txtInput.getText();
            String pass = txtPass.getText();

            // 1. Find the student (Matching your Teacher search logic)
            Student foundStudent = studentList.stream()
                    .filter(s -> (s.getID().equals(input) || s.getEmail().equals(input))
                            && s.getPassword().equals(pass))
                    .findFirst()
                    .orElse(null);

            if (foundStudent != null) {
                // 2. Direct Scene Switch (Just like you did for the Teacher)
                stage.setScene(StudentDashboard.createStudentDashboard(stage, foundStudent, mainApp));
            } else {
                mainApp.showError("Login Failed", "Invalid Student ID/Email or Password.");
            }
        });

        Hyperlink linkSignup = new Hyperlink("Don't have an account? Sign up here");
        linkSignup.setLayoutX(180); linkSignup.setLayoutY(290);
        UIUtils.applyLinkEffects(linkSignup);
        linkSignup.setOnAction(e -> stage.setScene(createSignupScene(stage, studentList, mainApp)));

        Button btnBack = new Button("← Back");
        btnBack.setLayoutX(15); btnBack.setLayoutY(15);
        UIUtils.applyButtonEffects(btnBack, "#7f8c8d");
        btnBack.setOnAction(e -> stage.setScene(mainApp.createMainScene(stage)));

        root.getChildren().addAll(lblTitle, txtInput, txtPass, btnLogin, btnBack, linkSignup);
        Scene scene = new Scene(root, 600, 400);
        UIUtils.applyStyle(scene);
        UIUtils.playTransition(root, true);
        return scene;
    }

    public static Scene createSignupScene(Stage stage, ArrayList<Student> studentList, HelloApplication mainApp) {
        Pane root = new Pane();
        root.setStyle("-fx-background-color: #f8f9fa;");

        Label lblTitle = new Label("Student Registration");
        lblTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        lblTitle.setLayoutX(180); lblTitle.setLayoutY(30);

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
        btnRegister.setLayoutX(175); btnRegister.setLayoutY(350); btnRegister.setPrefSize(250, 40);
        UIUtils.applyButtonEffects(btnRegister, "#2ecc71");

        btnRegister.setOnAction(e -> {
            if (!txtPass.getText().equals(txtConfirm.getText())) mainApp.showError("Error", "Passwords mismatch!");
            else {
                studentList.add(new Student(txtID.getText(), txtName.getText(), txtEmail.getText(), txtPass.getText()));
                mainApp.showInfo("Success", "Registered!");
                stage.setScene(createLoginScene(stage, studentList, mainApp));
            }
        });

        Button btnBack = new Button("← Back");
        btnBack.setLayoutX(10); btnBack.setLayoutY(10);
        UIUtils.applyButtonEffects(btnBack, "#7f8c8d");
        btnBack.setOnAction(e -> stage.setScene(createLoginScene(stage, studentList, mainApp)));

        root.getChildren().addAll(lblTitle, txtID, txtName, txtEmail, txtPass, txtConfirm, btnRegister, btnBack);
        Scene scene = new Scene(root, 600, 480);
        UIUtils.applyStyle(scene);
        UIUtils.playTransition(root, true);
        return scene;
    }
}