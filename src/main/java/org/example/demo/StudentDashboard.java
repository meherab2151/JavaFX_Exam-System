package org.example.demo;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

public class StudentDashboard {

    public static Scene createStudentDashboard(Stage stage, Student student, HelloApplication mainApp) {
        VBox root = new VBox(30);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(50));
        root.setStyle("-fx-background-color: #f8f9fa;");

        // --- TOP HEADER ---
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER);
        Label lblWelcome = new Label("Welcome, " + student.getName());
        lblWelcome.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Circle userIcon = new Circle(20, Color.web("#2ecc71")); // Student Green
        header.getChildren().addAll(userIcon, lblWelcome);

        // --- JOIN EXAM BOX ---
        VBox joinBox = new VBox(20);
        joinBox.setAlignment(Pos.CENTER);
        joinBox.setPadding(new Insets(30));
        joinBox.setStyle("-fx-background-color: white; -fx-border-color: #dcdde1; -fx-border-radius: 15; -fx-background-radius: 15;");
        joinBox.setMaxWidth(400);

        Label lblJoin = new Label("Enter Exam Code");
        lblJoin.setStyle("-fx-font-size: 18px; -fx-text-fill: #7f8c8d;");

        TextField txtCode = new TextField();
        txtCode.setPromptText("e.g. 5A2B9C");
        txtCode.setPrefHeight(50);
        txtCode.setStyle("-fx-font-size: 20px; -fx-alignment: center; -fx-font-family: 'Monospaced';");

        Button btnStart = new Button("Start Exam");
        btnStart.setPrefSize(200, 45);
        UIUtils.applyButtonEffects(btnStart, "#2ecc71");

        btnStart.setOnAction(e -> {
            String code = txtCode.getText().trim().toUpperCase();

            // Search for the live exam with this code
            Exam targetExam = ExamBank.allExams.stream()
                    .filter(ex -> ex.isLive() && ex.getExamCode().equals(code))
                    .findFirst()
                    .orElse(null);

            if (targetExam != null) {
                mainApp.showInfo("Joining Exam", "Entering " + targetExam.getSubject() + "...");
                // NEXT: stage.setScene(StudentExamView.createExamScene(stage, student, targetExam, mainApp));
            } else {
                mainApp.showError("Invalid Code", "No live exam found with that code. Check with your teacher!");
            }
        });

        joinBox.getChildren().addAll(lblJoin, txtCode, btnStart);

        // --- LOGOUT ---
        Button btnLogout = new Button("Log Out");
        UIUtils.applyButtonEffects(btnLogout, "#c0392b");
        btnLogout.setOnAction(e -> stage.setScene(mainApp.createMainScene(stage)));

        root.getChildren().addAll(header, joinBox, btnLogout);

        Scene scene = new Scene(root, 1000, 600);
        UIUtils.playTransition(root, true);
        return scene;
    }
}