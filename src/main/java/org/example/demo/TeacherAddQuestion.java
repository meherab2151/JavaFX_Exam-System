package org.example.demo;

import javafx.scene.control.*;
import javafx.scene.layout.Pane;

public class TeacherAddQuestion {

    private static TextArea globalQuestionArea;
    private static TextField[] mcqOptions = new TextField[4];
    private static ToggleGroup mcqToggleGroup;
    private static TextField exactAnsField;
    private static TextField minField, maxField;
    private static ComboBox<String> cbAnsType;

    private static boolean returnToExam = false;
    public static Question lastAddedQuestion = null;

    private static void applyInteractiveStyle(Control control) {
        control.setStyle("-fx-background-color: white; -fx-border-color: #dcdde1; -fx-border-radius: 5;");
    }

    private static void renderMcqForm(Pane container) {
        container.getChildren().clear();
        globalQuestionArea = new TextArea();
        globalQuestionArea.setPromptText("Enter MCQ Question...");
        globalQuestionArea.setPrefSize(600, 80); globalQuestionArea.setLayoutX(30);
        applyInteractiveStyle(globalQuestionArea);

        mcqToggleGroup = new ToggleGroup();
        double[] xCoords = {60, 360, 60, 360};
        double[] yCoords = {100, 100, 150, 150};
        double[] rbX = {30, 330, 30, 330};

        for (int i = 0; i < 4; i++) {
            mcqOptions[i] = new TextField();
            mcqOptions[i].setPromptText("Option " + (i + 1));
            mcqOptions[i].setPrefWidth(250);
            mcqOptions[i].setLayoutX(xCoords[i]); mcqOptions[i].setLayoutY(yCoords[i]);
            applyInteractiveStyle(mcqOptions[i]);

            RadioButton rb = new RadioButton();
            rb.setToggleGroup(mcqToggleGroup);
            rb.setLayoutX(rbX[i]); rb.setLayoutY(yCoords[i] + 5);

            container.getChildren().addAll(mcqOptions[i], rb);
        }
        container.getChildren().add(globalQuestionArea);
    }

    private static void renderTextForm(Pane container) {
        container.getChildren().clear();
        globalQuestionArea = new TextArea();
        globalQuestionArea.setPromptText("Enter Text Question...");
        globalQuestionArea.setPrefSize(600, 80); globalQuestionArea.setLayoutX(30);
        applyInteractiveStyle(globalQuestionArea);

        cbAnsType = new ComboBox<>();
        cbAnsType.getItems().addAll("Exact Answer", "Allow Deviate (Range)");
        cbAnsType.setValue("Exact Answer");
        cbAnsType.setLayoutX(30); cbAnsType.setLayoutY(100);

        Pane ansInputPane = new Pane();
        ansInputPane.setLayoutY(140);

        exactAnsField = new TextField(); exactAnsField.setPromptText("Correct Answer");
        exactAnsField.setLayoutX(30); applyInteractiveStyle(exactAnsField);

        minField = new TextField(); minField.setPromptText("Min Value"); minField.setPrefWidth(120);
        maxField = new TextField(); maxField.setPromptText("Max Value"); maxField.setPrefWidth(120);
        minField.setLayoutX(30); maxField.setLayoutX(170);
        applyInteractiveStyle(minField); applyInteractiveStyle(maxField);

        ansInputPane.getChildren().add(exactAnsField);

        cbAnsType.setOnAction(e -> {
            ansInputPane.getChildren().clear();
            if (cbAnsType.getValue().equals("Exact Answer")) {
                ansInputPane.getChildren().add(exactAnsField);
            } else {
                ansInputPane.getChildren().addAll(minField, maxField);
            }
        });

        container.getChildren().addAll(globalQuestionArea, cbAnsType, ansInputPane);
    }

    public static void showAddQuestionForm(Pane contentArea, HelloApplication mainApp, String initialSubject, Integer initialGrade) {
        contentArea.getChildren().clear();
        returnToExam = (initialSubject != null);

        Label lblTitle = new Label("Add New Question");
        lblTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        lblTitle.setLayoutX(30); lblTitle.setLayoutY(20);

        ComboBox<String> cbSubject = new ComboBox<>();
        cbSubject.getItems().addAll("Physics", "Chemistry", "Math");
        cbSubject.setPromptText("Subject");
        cbSubject.setLayoutX(30); cbSubject.setLayoutY(75);

        ComboBox<Integer> cbClass = new ComboBox<>();
        for (int i = 6; i <= 12; i++) cbClass.getItems().add(i);
        cbClass.setPromptText("Class");
        cbClass.setLayoutX(160); cbClass.setLayoutY(75);

        if (returnToExam) {
            cbSubject.setValue(initialSubject);
            cbClass.setValue(initialGrade);
        }

        ToggleGroup typeGroup = new ToggleGroup();
        RadioButton rbMcq = new RadioButton("MCQ");
        rbMcq.setToggleGroup(typeGroup); rbMcq.setSelected(true);
        rbMcq.setLayoutX(450); rbMcq.setLayoutY(80);

        RadioButton rbText = new RadioButton("Text");
        rbText.setToggleGroup(typeGroup);
        rbText.setLayoutX(520); rbText.setLayoutY(80);

        Pane dynamicForm = new Pane();
        dynamicForm.setLayoutY(140);
        dynamicForm.setPrefSize(800, 350);

        renderMcqForm(dynamicForm);
        rbMcq.setOnAction(e -> renderMcqForm(dynamicForm));
        rbText.setOnAction(e -> renderTextForm(dynamicForm));

        Button btnSave = new Button("Save to Question Bank");
        btnSave.setPrefSize(220, 45); btnSave.setLayoutX(30); btnSave.setLayoutY(520);
        UIUtils.applyButtonEffects(btnSave, "#f22c99");

        btnSave.setOnAction(e -> {
            // Validation
            if (cbSubject.getValue() == null || cbClass.getValue() == null || globalQuestionArea.getText().trim().isEmpty()) {
                mainApp.showError("Missing Info", "Please complete all fields.");
                return;
            }

            // MCQ Specific Validation: Check if answer selected
            if (rbMcq.isSelected() && mcqToggleGroup.getSelectedToggle() == null) {
                mainApp.showError("Missing Answer", "Please select the correct answer for the MCQ.");
                return;
            }

            try {
                String sub = cbSubject.getValue();
                int gr = cbClass.getValue();
                String text = globalQuestionArea.getText().trim();
                Question newQ = null;

                if (rbMcq.isSelected()) {
                    String[] opts = new String[4];
                    for (int i = 0; i < 4; i++) opts[i] = mcqOptions[i].getText();
                    int correctIdx = mcqToggleGroup.getToggles().indexOf(mcqToggleGroup.getSelectedToggle());
                    newQ = new MCQ(sub, gr, text, opts, correctIdx);
                } else {
                    if (cbAnsType.getValue().equals("Exact Answer")) {
                        newQ = new TextQuestion(sub, gr, text, Double.parseDouble(exactAnsField.getText()));
                    } else {
                        newQ = new RangeQuestion(sub, gr, text, Double.parseDouble(minField.getText()), Double.parseDouble(maxField.getText()));
                    }
                }

                QuestionBank.allQuestions.add(newQ);
                lastAddedQuestion = newQ;
                mainApp.showInfo("Success", "Question added to bank.");

                if (returnToExam) {
                    TeacherCreateExam.showCreateExamForm(contentArea, mainApp);
                } else {
                    // Only clear the question text field
                    globalQuestionArea.clear();
                }
            } catch (Exception ex) {
                mainApp.showError("Input Error", "Please enter valid numeric answers.");
            }
        });

        contentArea.getChildren().addAll(lblTitle, cbSubject, cbClass, rbMcq, rbText, dynamicForm, btnSave);
    }
}