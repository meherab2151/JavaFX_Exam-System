package org.example.demo;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.HashMap;

public class HelloApplication extends Application {

    private static ArrayList<Teacher> teacherList = new ArrayList<>();
    private static ArrayList<Student> studentList = new ArrayList<>();

    @Override
    public void start(Stage stage) {
        // Test account
        teacherList.add(new Teacher("A", "a", "a"));

        QuestionBank.allQuestions.add(new MCQ(
                "Physics", 6, "What is the unit of Force?",
                new String[]{"Newton", "Joule", "Watt", "Pascal"}, 0
        ));
        QuestionBank.allQuestions.add(new TextQuestion(
                "Physics", 6, "What is the square root of 144?", 12.0
        ));
        QuestionBank.allQuestions.add(new RangeQuestion(
                "Physics", 6, "What is the boiling point of pure water in Celsius at 1 atm?",
                99.5, 100.5
        ));

        HashMap<Question, Double> physicsExamQuestions = new HashMap<>();
        for (Question q : QuestionBank.allQuestions) {
            if (q.getSubject().equals("Physics") && q.getGrade() == 6) {
                physicsExamQuestions.put(q, 5.0);
            }
        }
        Exam physicsMidterm = new Exam("Physics", 6, 15.0, "20", physicsExamQuestions);
        ExamBank.allExams.add(physicsMidterm);

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