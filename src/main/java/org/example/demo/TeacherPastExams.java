package org.example.demo;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class TeacherPastExams {

    public static void renderPastExams(Pane contentArea, HelloApplication mainApp) {
        contentArea.getChildren().clear();

        // 1. Title
        Label lblTitle = new Label("Past Exam History");
        lblTitle.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #5758BB;");
        lblTitle.setLayoutX(20); lblTitle.setLayoutY(15);

        // 2. Header - Now aligned perfectly with ScrollPane width
        HBox header = createHeader();
        header.setLayoutX(20);
        header.setLayoutY(75);

        // 3. Scrollable List
        VBox historyList = new VBox(10);
        ScrollPane scroll = new ScrollPane(historyList);

        // Match width to header (780px)
        scroll.setPrefSize(780, 430);
        scroll.setLayoutX(20);
        scroll.setLayoutY(125);

        // Clean styling for the scroll pane
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: #dcdde1; -fx-border-radius: 0 0 5 5;");
        scroll.setFitToWidth(true);

        for (Exam e : ExamBank.allExams) {
            if (!e.isLive() && e.getScheduleDetails() != null && !e.getScheduleDetails().isEmpty()) {
                historyList.getChildren().add(createExamRow(e, contentArea, mainApp));
            }
        }

        contentArea.getChildren().addAll(lblTitle, header, scroll);
    }

    private static HBox createHeader() {
        HBox header = new HBox(0);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(12, 20, 12, 20));
        header.setStyle("-fx-background-color: #dfe6e9; -fx-background-radius: 5 5 0 0;");
        header.setPrefWidth(780);

        Label hDate = new Label("Date / Time"); hDate.setMinWidth(200);
        Label hSub = new Label("Subject");      hSub.setMinWidth(160);
        Label hCls = new Label("Class");        hCls.setMinWidth(80);
        Label hAct = new Label("Actions");      hAct.setMinWidth(240);

        header.getChildren().addAll(hDate, hSub, hCls, hAct);
        header.getChildren().forEach(n -> n.setStyle("-fx-text-fill: #2d3436; -fx-font-weight: bold; -fx-font-size: 14px;"));

        return header;
    }

    private static HBox createExamRow(Exam e, Pane contentArea, HelloApplication mainApp) {
        HBox row = new HBox(0);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(15, 20, 15, 20));
        row.setStyle("-fx-background-color: white; -fx-border-color: #dfe6e9; -fx-border-width: 0 0 1 0;");

        // 1. Date (Blue)
        Label lblDate = new Label(e.getScheduleDetails());
        lblDate.setMinWidth(200);
        lblDate.setStyle("-fx-font-family: 'Monospaced'; -fx-text-fill: #3498db; -fx-font-weight: bold;");

        // 2. Subject
        Label lblSub = new Label(e.getSubject());
        lblSub.setMinWidth(160);
        lblSub.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // 3. Class (Fixed: Now correctly shows "Grade 6")
        Label lblCls = new Label("Grade " + e.getGrade());
        lblCls.setMinWidth(80);
        lblCls.setStyle("-fx-text-fill: #7f8c8d;");

        // 4. Buttons (Analysis & View Questions)
        HBox btnGroup = new HBox(10);
        btnGroup.setMinWidth(240);
        btnGroup.setAlignment(Pos.CENTER_LEFT);

        Button btnAnalyze = new Button("Analysis");
        UIUtils.applyButtonEffects(btnAnalyze, "#f1c40f");
        btnAnalyze.setOnAction(ev -> renderAnalysis(e, contentArea, mainApp));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // 5. Three Dots (Far Right)
        MenuButton options = new MenuButton("...");
        options.setStyle("-fx-background-color: transparent; -fx-font-weight: bold;");
        MenuItem deleteItem = new MenuItem("Delete Record");
        deleteItem.setOnAction(ev -> {
            ExamBank.allExams.remove(e);
            renderPastExams(contentArea, mainApp);
        });
        options.getItems().add(deleteItem);

        btnGroup.getChildren().addAll(btnAnalyze, spacer, options);

        row.getChildren().addAll(lblDate, lblSub, lblCls, btnGroup);
        return row;
    }

    private static void renderAnalysis(Exam e, Pane contentArea, HelloApplication mainApp) {
        contentArea.getChildren().clear();

        Label lblTitle = new Label("Exam Details: " + e.getSubject());
        lblTitle.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        lblTitle.setLayoutX(30); lblTitle.setLayoutY(20);

        // Stats Section (Left)
        VBox statsBox = new VBox(15);
        statsBox.setPadding(new Insets(20));
        statsBox.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dcdde1; -fx-border-radius: 8;");
        statsBox.setLayoutX(30); statsBox.setLayoutY(80);
        statsBox.setPrefWidth(300);

        int qCount = (e.getQuestionsMap() != null) ? e.getQuestionsMap().size() : 0;
        statsBox.getChildren().addAll(
                createStatRow("Grade:", String.valueOf(e.getGrade())),
                createStatRow("Questions:", String.valueOf(qCount)),
                createStatRow("Total Marks:", e.getTotalMarks() + " pts")
        );

        // Question List (Right)
        Label lblHint = new Label("Click a question to view full details:");
        lblHint.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d; -fx-font-style: italic;");
        lblHint.setLayoutX(350); lblHint.setLayoutY(80);

        ListView<Question> qListView = new ListView<>();
        qListView.setPrefSize(420, 420);
        qListView.setLayoutX(350); qListView.setLayoutY(110);

        // Custom Cell Factory to show text but store the object
        qListView.setCellFactory(lv -> new ListCell<Question>() {
            @Override
            protected void updateItem(Question item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.getQuestionText());
            }
        });

        if (e.getQuestionsMap() != null) {
            qListView.getItems().addAll(e.getQuestionsMap().keySet());
        }

        // CLICK LOGIC: Open Popup
        qListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                showQuestionDetailPopup(newVal, e.getQuestionsMap().get(newVal));
            }
        });

        Button btnBack = new Button("← Back to History");
        btnBack.setLayoutX(30); btnBack.setLayoutY(540);
        UIUtils.applyButtonEffects(btnBack, "#34495e");
        btnBack.setOnAction(ev -> renderPastExams(contentArea, mainApp));

        contentArea.getChildren().addAll(lblTitle, statsBox, lblHint, qListView, btnBack);
    }

    private static void showQuestionDetailPopup(Question q, Double pts) {
        Stage stage = new Stage();
        stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        stage.setTitle("Question Detail View");

        VBox root = new VBox(15);
        root.setPadding(new Insets(25));
        root.setStyle("-fx-background-color: #ffffff;");

        // 1. Question Text Section
        Label lblQ = new Label("Question:");
        lblQ.setStyle("-fx-font-weight: bold; -fx-text-fill: #5758BB; -fx-font-size: 14px;");

        TextArea txtQuestion = new TextArea(q.getQuestionText());
        txtQuestion.setEditable(false);
        txtQuestion.setWrapText(true);
        txtQuestion.setPrefHeight(70);
        txtQuestion.setStyle("-fx-control-inner-background: #f1f2f6;");

        root.getChildren().addAll(lblQ, txtQuestion);

        // 2. Options Section (Only for MCQ)
        if (q instanceof MCQ) {
            Label lblOpt = new Label("Multiple Choice Options:");
            lblOpt.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");

            VBox optionsBox = new VBox(5);
            optionsBox.setPadding(new Insets(10));
            optionsBox.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dcdde1; -fx-border-radius: 5;");

            MCQ mcq = (MCQ) q;
            String[] options = mcq.getOptions();
            for (int i = 0; i < options.length; i++) {
                Label optLabel = new Label((i + 1) + ". " + options[i]);
                // Highlight the correct option in green
                if (i == mcq.getCorrectIndex()) {
                    optLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                    optLabel.setText(optLabel.getText() + " (Correct)");
                } else {
                    optLabel.setStyle("-fx-text-fill: #7f8c8d;");
                }
                optionsBox.getChildren().add(optLabel);
            }
            root.getChildren().addAll(lblOpt, optionsBox);
        }

        // 3. Correct Answer Summary
        Label lblCorrect = new Label("Correct Answer Summary:");
        lblCorrect.setStyle("-fx-font-weight: bold; -fx-text-fill: #27ae60;");

        String answerSummary = "";
        if (q instanceof MCQ) {
            MCQ m = (MCQ) q;
            answerSummary = "Option " + (m.getCorrectIndex() + 1) + ": " + m.getOptions()[m.getCorrectIndex()];
        } else if (q instanceof TextQuestion) {
            answerSummary = String.valueOf(((TextQuestion) q).getAnswer());
        } else if (q instanceof RangeQuestion) {
            RangeQuestion r = (RangeQuestion) q;
            answerSummary = r.getMin() + " to " + r.getMax();
        }

        TextField tfCorrect = new TextField(answerSummary);
        tfCorrect.setEditable(false);
        tfCorrect.setStyle("-fx-background-color: #e8f5e9; -fx-text-fill: #2e7d32; -fx-font-weight: bold;");

        // 4. Footer info
        Label lblPoints = new Label("Weight: " + pts + " Marks | Subject: " + q.getSubject());
        lblPoints.setStyle("-fx-font-style: italic; -fx-text-fill: #95a5a6;");

        Button btnClose = new Button("Close");
        btnClose.setPrefWidth(120);
        UIUtils.applyButtonEffects(btnClose, "#34495e");
        btnClose.setOnAction(ev -> stage.close());

        root.getChildren().addAll(lblCorrect, tfCorrect, lblPoints, btnClose);

        // Dynamic sizing based on content
        double height = (q instanceof MCQ) ? 550 : 400;
        stage.setScene(new javafx.scene.Scene(root, 450, height));
        stage.show();
    }

    private static HBox createStatRow(String label, String value) {
        HBox hb = new HBox(5);
        Label l = new Label(label); l.setStyle("-fx-font-weight: bold;");
        Label v = new Label(value); v.setStyle("-fx-text-fill: #2980b9;");
        hb.getChildren().addAll(l, v);
        return hb;
    }
}