package org.example.demo;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.stage.Stage;

public class TeacherDashboard {

    public static Scene createDashboardScene(Stage stage, Teacher teacher, HelloApplication mainApp) {
        Pane root = new Pane();
        root.setStyle("-fx-background-color: #f8f9fa;");

        Pane sidebar = new Pane();
        sidebar.setPrefSize(180, 600);
        sidebar.setStyle("-fx-background-color: #ffffff; -fx-border-color: #dcdde1; -fx-border-width: 0 5 0 0;");

        // Avatar & Info
        Circle avatar = new Circle(35, Color.web("#3498db"));
        avatar.setCenterX(90); avatar.setCenterY(85);

        Label lblName = new Label(teacher.getUser());
        lblName.setPrefWidth(180); lblName.setAlignment(Pos.CENTER);
        // Ultra-bold style for the name
        lblName.setStyle("-fx-text-fill: #2c3e50; -fx-font-size: 16px; -fx-font-weight: 900;");
        lblName.setLayoutY(130);

        Line separator = new Line(20, 160, 160, 160);
        separator.setStroke(Color.web("#dcdde1"));

        Pane contentArea = new Pane();
        contentArea.setLayoutX(180);
        contentArea.setPrefSize(820, 600);

        // Sidebar Navigation
        String[] menuItems = {"Dashboard", "Create Exam", "Analysis", "Add Question", "Thread"};
        String[] hoverColors = {"#3498db", "#9b59b6", "#2ecc71", "#f1c40f", "#e67e22"};
        double startY = 180;

        for (int i = 0; i < menuItems.length; i++) {
            String item = menuItems[i];
            String hoverColor = hoverColors[i];

            Button btn = new Button(item);
            btn.setPrefSize(150, 42);
            btn.setLayoutX(15);
            btn.setLayoutY(startY);

            UIUtils.applyButtonEffects(btn, hoverColor);

            btn.setOnAction(e -> {
                contentArea.getChildren().clear();
                if (item.equals("Add Question")) {
                    TeacherAddQuestion.showAddQuestionForm(contentArea, mainApp, null, null);
                }

                else if (item.equals("Create Exam")) {
                    // This links your new class to the dashboard
                    TeacherCreateExam.showCreateExamForm(contentArea, mainApp);
                }

                else {
                    Label placeholder = new Label(item + " View Coming Soon...");
                    placeholder.setLayoutX(50); placeholder.setLayoutY(50);
                    placeholder.setStyle("-fx-font-size: 22px; -fx-text-fill: #bdc3c7; -fx-font-weight: bold;");
                    contentArea.getChildren().add(placeholder);
                }
                UIUtils.playTransition(contentArea, true);
            });

            startY += 55;
            sidebar.getChildren().add(btn);
        }

        Button btnLogout = new Button("Log Out");
        btnLogout.setPrefSize(140, 40);
        btnLogout.setLayoutX(20);
        btnLogout.setLayoutY(540);

        UIUtils.applyButtonEffects(btnLogout, "#c0392b");

        btnLogout.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #2c3e50; -fx-font-size: 16px; -fx-font-weight: 900; -fx-background-radius: 8;");

        btnLogout.setOnAction(e -> stage.setScene(mainApp.createMainScene(stage)));

        sidebar.getChildren().addAll(avatar, lblName, separator, btnLogout);
        root.getChildren().addAll(sidebar, contentArea);

        Scene scene = new Scene(root, 1000, 600);
        UIUtils.applyStyle(scene);

        UIUtils.playTransition(root, true);

        return scene;
    }
}