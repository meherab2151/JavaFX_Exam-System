package org.example.demo;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import java.util.ArrayList;

public class TeacherLoginManager {

    public static Scene createLoginScene(Stage stage, ArrayList<Teacher> teacherList, HelloApplication mainApp) {
        Pane root = new Pane();
        root.setStyle("-fx-background-color: #f8f9fa;"); // Consistent light background

        Label lblTitle = new Label("Teacher Login");
        lblTitle.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        lblTitle.setLayoutX(220); lblTitle.setLayoutY(40);

        TextField txtUser = new TextField();
        txtUser.setPromptText("Email or Username");
        txtUser.setLayoutX(175); txtUser.setLayoutY(110); txtUser.setPrefWidth(250);

        PasswordField txtPass = new PasswordField();
        txtPass.setPromptText("Password");
        txtPass.setLayoutX(175); txtPass.setLayoutY(160); txtPass.setPrefWidth(250);

        Button btnLogin = new Button("Login");
        btnLogin.setLayoutX(175); btnLogin.setLayoutY(210); btnLogin.setPrefSize(250, 40);

        // Fix: Pass the blue hover color
        UIUtils.applyButtonEffects(btnLogin, "#3498db");

        btnLogin.setOnAction(e -> {
            String input = txtUser.getText();
            String pass = txtPass.getText();
            boolean success = teacherList.stream()
                    .anyMatch(t -> (t.getEmail().equals(input) || t.getUser().equals(input)) && t.getPassword().equals(pass));
            if (success) {
                Teacher loggedInTeacher = teacherList.stream()
                        .filter(t -> (t.getEmail().equals(input) || t.getUser().equals(input)))
                        .findFirst().get();

                stage.setScene(TeacherDashboard.createDashboardScene(stage, loggedInTeacher, mainApp));
            } else {
                mainApp.showError("Login Error", "Invalid credentials!");
            }
        });

        Hyperlink linkSignup = new Hyperlink("Don't have an account? Sign up here");
        linkSignup.setLayoutX(180); linkSignup.setLayoutY(260);
        UIUtils.applyLinkEffects(linkSignup);
        linkSignup.setOnAction(e -> stage.setScene(createSignupScene(stage, teacherList, mainApp)));

        Button btnBack = new Button("← Back");
        btnBack.setLayoutX(10); btnBack.setLayoutY(10);
        // Fix: Use a neutral or red hover for back button
        UIUtils.applyButtonEffects(btnBack, "#7f8c8d");
        btnBack.setOnAction(e -> stage.setScene(mainApp.createMainScene(stage)));

        root.getChildren().addAll(lblTitle, txtUser, txtPass, btnLogin, linkSignup, btnBack);
        Scene scene = new Scene(root, 600, 400);
        UIUtils.applyStyle(scene);
        UIUtils.playTransition(root, true);
        return scene;
    }

    public static Scene createSignupScene(Stage stage, ArrayList<Teacher> teacherList, HelloApplication mainApp) {
        Pane root = new Pane();
        root.setStyle("-fx-background-color: #f8f9fa;");

        Label lblTitle = new Label("Teacher Registration");
        lblTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        lblTitle.setLayoutX(180); lblTitle.setLayoutY(30);

        TextField txtName = new TextField(); txtName.setPromptText("Full Name");
        txtName.setLayoutX(175); txtName.setLayoutY(90); txtName.setPrefWidth(250);

        TextField txtEmail = new TextField(); txtEmail.setPromptText("Email Address");
        txtEmail.setLayoutX(175); txtEmail.setLayoutY(140); txtEmail.setPrefWidth(250);

        PasswordField txtPass = new PasswordField(); txtPass.setPromptText("Create Password");
        txtPass.setLayoutX(175); txtPass.setLayoutY(190); txtPass.setPrefWidth(250);

        PasswordField txtConfirm = new PasswordField(); txtConfirm.setPromptText("Confirm Password");
        txtConfirm.setLayoutX(175); txtConfirm.setLayoutY(240); txtConfirm.setPrefWidth(250);

        Button btnRegister = new Button("Create Account");
        btnRegister.setLayoutX(175); btnRegister.setLayoutY(300); btnRegister.setPrefSize(250, 40);
        UIUtils.applyButtonEffects(btnRegister, "#9b59b6"); // Purple theme for signup

        btnRegister.setOnAction(e -> {
            if (txtPass.getText().equals(txtConfirm.getText()) && !txtName.getText().isEmpty()) {
                teacherList.add(new Teacher(txtName.getText(), txtEmail.getText(), txtPass.getText()));
                mainApp.showInfo("Success", "Account created!");
                stage.setScene(createLoginScene(stage, teacherList, mainApp));
            } else {
                mainApp.showError("Error", "Check your inputs!");
            }
        });

        Button btnBack = new Button("← Back");
        btnBack.setLayoutX(10); btnBack.setLayoutY(10);
        UIUtils.applyButtonEffects(btnBack, "#7f8c8d");
        btnBack.setOnAction(e -> stage.setScene(createLoginScene(stage, teacherList, mainApp)));

        root.getChildren().addAll(lblTitle, txtName, txtEmail, txtPass, txtConfirm, btnRegister, btnBack);
        Scene scene = new Scene(root, 600, 450);
        UIUtils.applyStyle(scene);
        UIUtils.playTransition(root, true);
        return scene;
    }
}