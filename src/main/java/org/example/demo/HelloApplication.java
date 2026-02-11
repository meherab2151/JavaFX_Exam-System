package org.example.demo;

import java.util.ArrayList;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class HelloApplication extends Application {

    private static final String CSS_PATH = "/org/example/demo/style.css";
    private static ArrayList<Teacher> teacherList = new ArrayList<>();

    @Override
    public void start(Stage stage) {
        stage.setTitle("Online Exam System");
        stage.setScene(createMainScene(stage));
        stage.show();
    }

    // ================= MAIN ROLE SELECTION =================
    private Scene createMainScene(Stage stage) {

        Label lblWelcome = new Label("Welcome to the Online Exam System");
        lblWelcome.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Label lblPrompt = new Label("Please select your role to continue:");

        Button btnStudent = new Button("Student");
        Button btnTeacher = new Button("Teacher");

        btnStudent.getStyleClass().add("role-button");
        btnTeacher.getStyleClass().add("role-button");

        btnStudent.setPrefSize(120, 50);
        btnTeacher.setPrefSize(120, 50);

        btnStudent.setOnAction(e -> stage.setScene(createStudentLoginScene(stage)));
        btnTeacher.setOnAction(e -> stage.setScene(createTeacherLoginScene(stage)));

        HBox buttonContainer = new HBox(40, btnStudent, btnTeacher);
        buttonContainer.setAlignment(Pos.CENTER);

        VBox root = new VBox(40, lblWelcome, lblPrompt, buttonContainer);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #f4f4f4;");

        Scene scene = new Scene(root, 600, 400);
        scene.getStylesheets().add(getClass().getResource(CSS_PATH).toExternalForm());

        return scene;
    }

    // ================= TEACHER LOGIN =================
    private Scene createTeacherLoginScene(Stage stage) {

        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-padding: 40; -fx-background-color: #ffffff;");

        Label lblTitle = new Label("Teacher Login");
        lblTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        TextField txtUser = new TextField();
        txtUser.setPromptText("Username/Email");
        txtUser.setMaxWidth(250);

        PasswordField txtPass = new PasswordField();
        txtPass.setPromptText("Password");
        txtPass.setMaxWidth(250);

        Button btnLogin = new Button("Login");
        btnLogin.getStyleClass().add("role-button");
        btnLogin.setPrefWidth(250);
        btnLogin.setOnAction(e -> {
            String email = txtUser.getText();
            String pass = txtPass.getText();

            // Check if any teacher in our list matches the input
            boolean loginSuccess = teacherList.stream()
                    .anyMatch(t -> t.getEmail().equals(email) && t.getPassword().equals(pass));

            if (loginSuccess) {
                System.out.println("Login Successful! Welcome.");
                // stage.setScene(createTeacherDashboard(stage)); // This would be your next step!
            } else {
                System.out.println("Invalid email or password.");
            }
        });

        Hyperlink linkSignup = new Hyperlink("Don't have an account? Sign up here");
        linkSignup.setOnAction(e -> stage.setScene(createTeacherSignupScene(stage)));

        Button btnBack = new Button("← Back");
        btnBack.setOnAction(e -> stage.setScene(createMainScene(stage)));

        layout.getChildren().addAll(lblTitle, txtUser, txtPass, btnLogin, linkSignup, btnBack);

        Scene scene = new Scene(layout, 600, 400);
        scene.getStylesheets().add(getClass().getResource(CSS_PATH).toExternalForm());

        return scene;
    }

    // ================= TEACHER SIGNUP =================
    private Scene createTeacherSignupScene(Stage stage) {

        VBox layout = new VBox(15);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-padding: 40; -fx-background-color: #ffffff;");

        Label lblTitle = new Label("Teacher Registration");
        lblTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        TextField txtFullName = new TextField();
        txtFullName.setPromptText("Full Name");
        txtFullName.setMaxWidth(250);

        TextField txtEmail = new TextField();
        txtEmail.setPromptText("Email Address");
        txtEmail.setMaxWidth(250);

        TextField txtDept = new TextField();
        txtDept.setPromptText("Department");
        txtDept.setMaxWidth(250);

        PasswordField txtPass = new PasswordField();
        txtPass.setPromptText("Create Password");
        txtPass.setMaxWidth(250);

        PasswordField txtConfirmPass = new PasswordField();
        txtConfirmPass.setPromptText("Confirm Password");
        txtConfirmPass.setMaxWidth(250);

        Button btnRegister = new Button("Create Account");
        btnRegister.getStyleClass().add("role-button");
        btnRegister.setPrefWidth(250);
        btnRegister.setOnAction(e -> {
            if(txtPass.getText().equals(txtConfirmPass.getText())) {
                // Create and save the teacher
                Teacher newTeacher = new Teacher(txtFullName.getText(), txtEmail.getText(), txtPass.getText());
                teacherList.add(newTeacher);

                System.out.println("Registration Successful for: " + txtEmail.getText());
                stage.setScene(createTeacherLoginScene(stage)); // Take them to login
            } else {
                System.out.println("Passwords do not match!");
            }
        });

        Hyperlink linkLogin = new Hyperlink("Already have an account? Login");
        linkLogin.setOnAction(e -> stage.setScene(createTeacherLoginScene(stage)));

        Button btnBack = new Button("← Back");
        btnBack.setOnAction(e -> stage.setScene(createMainScene(stage)));

        layout.getChildren().addAll(lblTitle, txtFullName, txtEmail, txtDept,
                txtPass, txtConfirmPass, btnRegister, linkLogin, btnBack);

        Scene scene = new Scene(layout, 600, 450);
        scene.getStylesheets().add(getClass().getResource(CSS_PATH).toExternalForm());

        return scene;
    }

    // ================= STUDENT LOGIN =================
    private Scene createStudentLoginScene(Stage stage) {

        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);

        Label lblTitle = new Label("Student Login");
        lblTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        TextField txtID = new TextField();
        txtID.setPromptText("Student ID");
        txtID.setMaxWidth(250);

        PasswordField txtPass = new PasswordField();
        txtPass.setPromptText("Password");
        txtPass.setMaxWidth(250);

        Button btnLogin = new Button("Login");
        btnLogin.getStyleClass().add("role-button");
        btnLogin.setPrefWidth(250);

        Button btnBack = new Button("← Back");
        btnBack.setOnAction(e -> stage.setScene(createMainScene(stage)));

        layout.getChildren().addAll(lblTitle, txtID, txtPass, btnLogin, btnBack);

        Scene scene = new Scene(layout, 600, 400);
        scene.getStylesheets().add(getClass().getResource(CSS_PATH).toExternalForm());

        return scene;
    }
}
