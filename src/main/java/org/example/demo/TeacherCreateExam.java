package org.example.demo;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.util.HashMap;

public class TeacherCreateExam {
    private static String savedSubject = null;
    private static Integer savedGrade = null;
    private static String savedTotalMark = "";
    private static String savedDuration = "";
    private static double currentSelectedMarks = 0;
    private static Label lblMarkStatus;
    private static VBox questionListContainer;
    private static HashMap<Question, Double> selectedWithMarks = new HashMap<>();

    public static void showCreateExamForm(Pane contentArea, HelloApplication mainApp) {
        contentArea.getChildren().clear();

        Label lblTitle = new Label("Create Exam Configuration");
        lblTitle.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        lblTitle.setLayoutX(30); lblTitle.setLayoutY(15);

        ComboBox<String> cbSubject = new ComboBox<>();
        cbSubject.getItems().addAll("Physics", "Chemistry", "Math");
        cbSubject.setPromptText("Subject");
        cbSubject.setLayoutX(30); cbSubject.setLayoutY(55);
        if (savedSubject != null) cbSubject.setValue(savedSubject);

        ComboBox<Integer> cbClass = new ComboBox<>();
        for (int i = 6; i <= 12; i++) cbClass.getItems().add(i);
        cbClass.setPromptText("Class");
        cbClass.setLayoutX(150); cbClass.setLayoutY(55);
        if (savedGrade != null) cbClass.setValue(savedGrade);

        TextField txtTotalMark = new TextField(savedTotalMark);
        txtTotalMark.setPromptText("Total Marks");
        txtTotalMark.setPrefWidth(100); txtTotalMark.setLayoutX(270); txtTotalMark.setLayoutY(55);

        TextField txtDuration = new TextField(savedDuration);
        txtDuration.setPromptText("Duration (Mins)");
        txtDuration.setPrefWidth(120); txtDuration.setLayoutX(380); txtDuration.setLayoutY(55);
        if (!savedDuration.isEmpty()) txtDuration.setText(savedDuration);

        lblMarkStatus = new Label("Selected: 0 / 0 Marks");
        lblMarkStatus.setStyle("-fx-font-weight: bold; -fx-text-fill: #e67e22;");
        lblMarkStatus.setLayoutX(30); lblMarkStatus.setLayoutY(95);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setPrefSize(760, 340);
        scrollPane.setLayoutX(30); scrollPane.setLayoutY(125);

        questionListContainer = new VBox(15);
        questionListContainer.setPadding(new Insets(10));
        scrollPane.setContent(questionListContainer);

        // State Listeners
        cbSubject.setOnAction(e -> { savedSubject = cbSubject.getValue(); refreshList(savedSubject, savedGrade, mainApp); });
        cbClass.setOnAction(e -> { savedGrade = cbClass.getValue(); refreshList(savedSubject, savedGrade, mainApp); });
        txtTotalMark.setOnKeyReleased(e -> { savedTotalMark = txtTotalMark.getText(); updateMarkLabel(); });
        txtDuration.setOnKeyReleased(e -> savedDuration = txtDuration.getText());

        if (savedSubject != null && savedGrade != null) refreshList(savedSubject, savedGrade, mainApp);

        Button btnAddMore = new Button("+ Add More Questions");
        btnAddMore.setLayoutX(30); btnAddMore.setLayoutY(480);
        UIUtils.applyButtonEffects(btnAddMore, "#3498db");
        btnAddMore.setOnAction(e -> TeacherAddQuestion.showAddQuestionForm(contentArea, mainApp, cbSubject.getValue(), cbClass.getValue()));

        Button btnCreate = new Button("CREATE EXAM");
        btnCreate.setPrefSize(180, 45); btnCreate.setLayoutX(610); btnCreate.setLayoutY(475);
        UIUtils.applyButtonEffects(btnCreate, "#27ae60");
        btnCreate.setOnAction(e -> handleCreateExam(mainApp, contentArea));

        contentArea.getChildren().addAll(lblTitle, cbSubject, cbClass, txtTotalMark, txtDuration, lblMarkStatus, scrollPane, btnAddMore, btnCreate);
        updateMarkLabel();
    }

    private static void refreshList(String sub, Integer cls, HelloApplication mainApp) {
        questionListContainer.getChildren().clear();
        if (sub == null || cls == null) return;

        for (Question q : QuestionBank.allQuestions) {
            if (q.getSubject().equals(sub) && q.getGrade() == cls) {
                VBox card = new VBox(10);
                card.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-border-color: #dcdde1; -fx-border-radius: 8;");

                HBox topRow = new HBox(15);
                topRow.setAlignment(Pos.CENTER_LEFT);

                CheckBox cb = new CheckBox();
                Label lblQuestion = new Label(q.getQuestionText());
                lblQuestion.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
                lblQuestion.setPrefWidth(450);

                TextField txtMark = new TextField();
                txtMark.setPromptText("Marks");
                txtMark.setPrefWidth(60);

                if (selectedWithMarks.containsKey(q)) {
                    cb.setSelected(true);
                    txtMark.setText(String.valueOf(selectedWithMarks.get(q)));
                } else if (q == TeacherAddQuestion.lastAddedQuestion) {
                    cb.setSelected(true);
                    selectedWithMarks.put(q, 0.0);
                    TeacherAddQuestion.lastAddedQuestion = null;
                } else {
                    txtMark.setDisable(true);
                }

                Button btnEdit = new Button("Edit");
                UIUtils.applyButtonEffects(btnEdit, "#f39c12");
                btnEdit.setOnAction(e -> showEditPopup(q, mainApp));

                topRow.getChildren().addAll(cb, lblQuestion, txtMark, btnEdit);

                VBox answerInfo = new VBox(5);
                answerInfo.setPadding(new Insets(0, 0, 0, 35));
                renderAnswerDetails(q, answerInfo);

                cb.setOnAction(e -> {
                    txtMark.setDisable(!cb.isSelected());
                    if (!cb.isSelected()) selectedWithMarks.remove(q);
                    else selectedWithMarks.putIfAbsent(q, 0.0);
                    updateMarkLabel();
                });

                txtMark.setOnKeyReleased(e -> {
                    try {
                        String val = txtMark.getText().trim();
                        selectedWithMarks.put(q, val.isEmpty() ? 0.0 : Double.parseDouble(val));
                        updateMarkLabel();
                    } catch (Exception ex) {
                        selectedWithMarks.put(q, 0.0);
                        updateMarkLabel();
                    }
                });

                card.getChildren().addAll(topRow, answerInfo);
                questionListContainer.getChildren().add(card);
            }
        }
        updateMarkLabel();
    }

    private static void renderAnswerDetails(Question q, VBox container) {
        container.getChildren().clear();
        if (q instanceof MCQ) {
            MCQ mcq = (MCQ) q;
            for (int i = 0; i < 4; i++) {
                Label opt = new Label((i + 1) + ". " + mcq.getOptions()[i]);
                if (i == mcq.getCorrectIndex()) opt.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                container.getChildren().add(opt);
            }
        } else if (q instanceof TextQuestion) {
            container.getChildren().add(new Label("Correct Answer: " + ((TextQuestion) q).getAnswer()));
        } else if (q instanceof RangeQuestion) {
            RangeQuestion rq = (RangeQuestion) q;
            container.getChildren().add(new Label("Correct Range: " + rq.getMin() + " to " + rq.getMax()));
        }
    }

    private static void showEditPopup(Question q, HelloApplication mainApp) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Edit Question Details");

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: #f4f7f6;");

        // Common Text
        TextArea areaText = new TextArea(q.getQuestionText());
        areaText.setPrefHeight(60);
        layout.getChildren().addAll(new Label("Question Text:"), areaText);

        // Type Specific Fields
        TextField[] mcqFields = new TextField[4];
        ToggleGroup tg = new ToggleGroup();
        RadioButton[] rbs = new RadioButton[4];
        TextField txtAns = new TextField();
        TextField txtMin = new TextField(), txtMax = new TextField();

        if (q instanceof MCQ) {
            MCQ mcq = (MCQ) q;
            layout.getChildren().add(new Label("Options (Select the correct one):"));
            for (int i = 0; i < 4; i++) {
                HBox hb = new HBox(10);
                rbs[i] = new RadioButton();
                rbs[i].setToggleGroup(tg);
                mcqFields[i] = new TextField(mcq.getOptions()[i]);
                if (i == mcq.getCorrectIndex()) rbs[i].setSelected(true);
                hb.getChildren().addAll(rbs[i], mcqFields[i]);
                layout.getChildren().add(hb);
            }
        } else if (q instanceof TextQuestion) {
            txtAns.setText(String.valueOf(((TextQuestion) q).getAnswer()));
            layout.getChildren().addAll(new Label("Correct Answer:"), txtAns);
        } else if (q instanceof RangeQuestion) {
            RangeQuestion rq = (RangeQuestion) q;
            txtMin.setText(String.valueOf(rq.getMin()));
            txtMax.setText(String.valueOf(rq.getMax()));
            layout.getChildren().addAll(new Label("Min Value:"), txtMin, new Label("Max Value:"), txtMax);
        }

        Button btnUpdate = new Button("Apply Changes");
        UIUtils.applyButtonEffects(btnUpdate, "#27ae60");
        btnUpdate.setOnAction(e -> {
            try {
                q.setQuestionText(areaText.getText());
                if (q instanceof MCQ) {
                    MCQ mcq = (MCQ) q;
                    String[] newOpts = new String[4];
                    for (int i = 0; i < 4; i++) newOpts[i] = mcqFields[i].getText();
                    mcq.setOptions(newOpts);
                    mcq.setCorrectIndex(tg.getToggles().indexOf(tg.getSelectedToggle()));
                } else if (q instanceof TextQuestion) {
                    ((TextQuestion) q).setAnswer(Double.parseDouble(txtAns.getText()));
                } else if (q instanceof RangeQuestion) {
                    ((RangeQuestion) q).setMin(Double.parseDouble(txtMin.getText()));
                    ((RangeQuestion) q).setMax(Double.parseDouble(txtMax.getText()));
                }
                stage.close();
                refreshList(savedSubject, savedGrade, mainApp);
            } catch (Exception ex) {
                mainApp.showError("Edit Error", "Invalid numeric values provided.");
            }
        });

        layout.getChildren().add(btnUpdate);
        stage.setScene(new Scene(layout, 400, 500));
        stage.showAndWait();
    }

    private static void handleCreateExam(HelloApplication mainApp, Pane contentArea) {
        // Validation for ALL configuration fields
        if (savedSubject == null || savedGrade == null || savedTotalMark.isEmpty() || savedDuration.isEmpty()) {
            mainApp.showError("Missing Fields", "Please fill in all details (Subject, Class, Marks, and Duration).");
            return;
        }

        if (selectedWithMarks.isEmpty()) {
            mainApp.showError("No Questions", "Please select at least one question for the exam.");
            return;
        }

        try {
            double target = Double.parseDouble(savedTotalMark);
            if (currentSelectedMarks != target) {
                mainApp.showError("Mark Mismatch", "Total marks assigned (" + currentSelectedMarks + ") must exactly match target (" + target + ").");
                return;
            }
            mainApp.showInfo("Success", "Exam Created Successfully!");
            clearState();
            showCreateExamForm(contentArea, mainApp);
        } catch (Exception ex) {
            mainApp.showError("Input Error", "Check total marks input format.");
        }
    }

    private static void updateMarkLabel() {
        currentSelectedMarks = selectedWithMarks.values().stream().mapToDouble(Double::doubleValue).sum();
        String goal = savedTotalMark.isEmpty() ? "0" : savedTotalMark;
        if (lblMarkStatus != null) lblMarkStatus.setText("Selected: " + currentSelectedMarks + " / " + goal + " Marks");
    }

    private static void clearState() {
        savedSubject = null; savedGrade = null; savedTotalMark = ""; savedDuration = "";
        selectedWithMarks.clear(); currentSelectedMarks = 0;
    }
}