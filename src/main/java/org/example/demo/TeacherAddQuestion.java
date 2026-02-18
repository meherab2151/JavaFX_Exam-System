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

    private static void applyInteractiveStyle(Control control) {
        control.setStyle("-fx-background-color: white; -fx-border-color: #dcdde1; -fx-border-radius: 5;");
        control.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                control.setStyle("-fx-background-color: white; -fx-border-color: #3498db; -fx-border-width: 2; -fx-border-radius: 5;");
            } else {
                control.setStyle("-fx-background-color: white; -fx-border-color: #dcdde1; -fx-border-width: 1; -fx-border-radius: 5;");
            }
        });
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

    public static void showAddQuestionForm(Pane contentArea, HelloApplication mainApp) {
        contentArea.getChildren().clear();

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

        // Save Button with UIUtils effects
        Button btnSave = new Button("Save to Question Bank");
        btnSave.setPrefSize(220, 45);
        btnSave.setLayoutX(30);
        btnSave.setLayoutY(520);

        UIUtils.applyButtonEffects(btnSave, "#f22c99");

        btnSave.setOnAction(e -> {
            // 1. Basic Validation: Ensure Subject, Class, and Question Text are present
            if (cbSubject.getValue() == null || cbClass.getValue() == null) {
                mainApp.showError("Missing Info", "Please select Subject and Class.");
                return;
            }
            if (globalQuestionArea.getText().trim().isEmpty()) {
                mainApp.showError("Missing Info", "Please enter the question text.");
                return;
            }

            try {
                String subject = cbSubject.getValue();
                int grade = cbClass.getValue();
                String questionText = globalQuestionArea.getText().trim();

                if (rbMcq.isSelected()) {
                    String[] options = new String[4];
                    for (int i = 0; i < 4; i++) {
                        String optText = mcqOptions[i].getText().trim();
                        if (optText.isEmpty()) {
                            mainApp.showError("Missing Info", "All four MCQ options must be filled.");
                            return;
                        }
                        options[i] = optText;
                    }

                    if (mcqToggleGroup.getSelectedToggle() == null) {
                        mainApp.showError("Missing Info", "Please select the correct MCQ answer.");
                        return;
                    }

                    int correctIdx = mcqToggleGroup.getToggles().indexOf(mcqToggleGroup.getSelectedToggle());

                    QuestionBank.allQuestions.add(new MCQ(subject, grade, questionText, options, correctIdx));
                }

                else {
                    if (cbAnsType.getValue().equals("Exact Answer")) {
                        if (exactAnsField.getText().trim().isEmpty()) {
                            mainApp.showError("Missing Info", "Please provide the exact answer.");
                            return;
                        }
                        double ans = Double.parseDouble(exactAnsField.getText().trim());
                        QuestionBank.allQuestions.add(new TextQuestion(subject, grade, questionText, ans));
                    }
                    else {
                        if (minField.getText().trim().isEmpty() || maxField.getText().trim().isEmpty()) {
                            mainApp.showError("Missing Info", "Please provide both Min and Max values.");
                            return;
                        }

                        double minVal = Double.parseDouble(minField.getText().trim());
                        double maxVal = Double.parseDouble(maxField.getText().trim());

                        if (minVal >= maxVal) {
                            mainApp.showError("Logic Error", "Min value must be less than Max value.");
                            return;
                        }
                        QuestionBank.allQuestions.add(new RangeQuestion(subject, grade, questionText, minVal, maxVal));
                    }
                }

                mainApp.showInfo("Question Bank Updated", "Question has been saved successfully!");

                globalQuestionArea.clear();
                if (rbMcq.isSelected()) {
                    for (TextField opt : mcqOptions) opt.clear();
                    mcqToggleGroup.selectToggle(null);
                } else {
                    exactAnsField.clear();
                    minField.clear();
                    maxField.clear();
                }

            } catch (NumberFormatException ex) {
                mainApp.showError("Input Error", "Please enter valid numbers for the answers (e.g., 10.5).");
            }
        });

        contentArea.getChildren().addAll(lblTitle, cbSubject, cbClass, rbMcq, rbText, dynamicForm, btnSave);
    }
}