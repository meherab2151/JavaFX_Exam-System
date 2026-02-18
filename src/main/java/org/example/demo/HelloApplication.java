package org.example.demo;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import java.util.ArrayList;

public class HelloApplication extends Application {

    private static ArrayList<Teacher> teacherList = new ArrayList<>();
    private static ArrayList<Student> studentList = new ArrayList<>();

    @Override
    public void start(Stage stage) {
        // Test account
        teacherList.add(new Teacher("A", "a", "a"));

        stage.setTitle("Online Exam System");
        stage.setScene(createMainScene(stage));
        stage.show();
    }

    public Scene createMainScene(Stage stage) {
        Pane root = new Pane();
        root.setStyle("-fx-background-color: #f8f9fa;"); // Matches dashboard bg

        Label lblWelcome = new Label("Online Exam System");
        lblWelcome.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        lblWelcome.setLayoutX(170); lblWelcome.setLayoutY(80);

        Button btnStudent = new Button("Student Portal");
        Button btnTeacher = new Button("Teacher Portal");

        btnStudent.setLayoutX(120); btnStudent.setLayoutY(180); btnStudent.setPrefSize(160, 50);
        btnTeacher.setLayoutX(320); btnTeacher.setLayoutY(180); btnTeacher.setPrefSize(160, 50);

        UIUtils.applyButtonEffects(btnStudent, "#3498db");
        UIUtils.applyButtonEffects(btnTeacher, "#9b59b6");

        btnStudent.setOnAction(e -> stage.setScene(StudentLoginManager.createLoginScene(stage, studentList, this)));
        btnTeacher.setOnAction(e -> stage.setScene(TeacherLoginManager.createLoginScene(stage, teacherList, this)));

        root.getChildren().addAll(lblWelcome, btnStudent, btnTeacher);

        Scene scene = new Scene(root, 600, 400);
        UIUtils.applyStyle(scene);
        UIUtils.playTransition(root, false);
        return scene;
    }

    public void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}