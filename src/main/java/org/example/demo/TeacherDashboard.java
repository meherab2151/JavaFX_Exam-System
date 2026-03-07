package org.example.demo;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class TeacherDashboard {

    public static Scene createDashboardScene(Stage stage, Teacher teacher, HelloApplication mainApp) {
        Pane root = new Pane();
        root.setStyle("-fx-background-color: #f8f9fa;");

        // Sidebar logic remains same...
        Pane sidebar = new Pane();
        sidebar.setPrefSize(180, 600);
        sidebar.setStyle("-fx-background-color: #ffffff; -fx-border-color: #dcdde1; -fx-border-width: 0 5 0 0;");
        Circle avatar = new Circle(35, Color.web("#3498db"));
        avatar.setCenterX(90); avatar.setCenterY(85);
        Label lblName = new Label(teacher.getUser());
        lblName.setPrefWidth(180); lblName.setAlignment(Pos.CENTER);
        lblName.setStyle("-fx-text-fill: #2c3e50; -fx-font-size: 16px; -fx-font-weight: 900;");
        lblName.setLayoutY(130);
        Line separator = new Line(20, 160, 160, 160);
        separator.setStroke(Color.web("#dcdde1"));

        Pane contentArea = new Pane();
        contentArea.setLayoutX(180);
        contentArea.setPrefSize(820, 600);

        String[] menuItems = {"Dashboard", "Create Exam", "Analysis", "Add Question", "Past Exams", "Thread"};
        String[] hoverColors = {"#3498db", "#9b59b6", "#2ecc71", "#f1c40f", "#5758BB", "#e67e22"};
        double startY = 180;

        for (int i = 0; i < menuItems.length; i++) {
            String item = menuItems[i];
            Button btn = new Button(item);
            btn.setPrefSize(150, 42); btn.setLayoutX(15); btn.setLayoutY(startY);
            UIUtils.applyButtonEffects(btn, hoverColors[i]);
            btn.setOnAction(e -> {
                contentArea.getChildren().clear();
                if (item.equals("Dashboard")) renderDashboardHome(contentArea, mainApp);
                else if (item.equals("Create Exam")) TeacherCreateExam.showCreateExamForm(contentArea, mainApp);
                else if (item.equals("Add Question")) TeacherAddQuestion.showAddQuestionForm(contentArea, mainApp, null, null);
                else if (item.equals("Past Exams")) TeacherPastExams.renderPastExams(contentArea, mainApp);
                UIUtils.playTransition(contentArea, true);
            });
            sidebar.getChildren().add(btn);
            startY += 55;
        }

        Button btnLogout = new Button("Log Out");
        btnLogout.setPrefSize(140, 40); btnLogout.setLayoutX(20); btnLogout.setLayoutY(540);
        UIUtils.applyButtonEffects(btnLogout, "#c0392b");
        btnLogout.setOnAction(e -> stage.setScene(mainApp.createMainScene(stage)));

        sidebar.getChildren().addAll(avatar, lblName, separator, btnLogout);
        root.getChildren().addAll(sidebar, contentArea);

        renderDashboardHome(contentArea, mainApp);
        return new Scene(root, 1000, 600);
    }

    public static void renderDashboardHome(Pane contentArea, HelloApplication mainApp) {
        contentArea.getChildren().clear();
        // --- ONGOING EXAMS SECTION ---
        Label lblOngoing = new Label("Ongoing Exams");
        lblOngoing.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        lblOngoing.setLayoutX(280); lblOngoing.setLayoutY(15);

        HBox ongoingHeader = new HBox(15);
        ongoingHeader.setAlignment(Pos.CENTER_LEFT);
        ongoingHeader.setPadding(new Insets(10, 20, 10, 20));
        ongoingHeader.setStyle("-fx-background-color: #dfe6e9; -fx-background-radius: 5 5 0 0;");
        ongoingHeader.setPrefWidth(780);
        ongoingHeader.setLayoutX(20);
        ongoingHeader.setLayoutY(55);

        Label ohSub = new Label("Subject"); ohSub.setMinWidth(140);
        Label ohCls = new Label("Class");   ohCls.setMinWidth(90);
        Label ohMrk = new Label("Marks");   ohMrk.setMinWidth(120);
        Label ohAtt = new Label("Attendance"); ohAtt.setMinWidth(110);
        Label ohHigh = new Label("Highest"); ohHigh.setMinWidth(100);
        Label ohCode = new Label("Code"); ohCode.setMinWidth(100);
        ongoingHeader.getChildren().addAll(ohSub, ohCls, ohMrk, ohCode, ohAtt, ohHigh);

        ongoingHeader.setStyle(ongoingHeader.getStyle() + "-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");

        VBox ongoingList = new VBox(10);
        ScrollPane ongoingScroll = new ScrollPane(ongoingList);
        ongoingScroll.setFitToWidth(true);
        ongoingScroll.setPrefSize(780, 160);
        ongoingScroll.setLayoutX(20);
        ongoingScroll.setLayoutY(95);
        ongoingScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: #dcdde1;");

        boolean hasLive = false;
        for (Exam e : ExamBank.allExams) {
            if (e.isLive()) {
                HBox row = new HBox(15);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(10, 20, 10, 20));
                row.setStyle("-fx-background-color: white; -fx-border-color: #27ae60; -fx-border-radius: 8;");
                e.generateCode();

                Label lblSub = new Label(e.getSubject());
                lblSub.setMinWidth(140);
                lblSub.setStyle("-fx-font-size: 20px; -fx-text-fill: #2c3e50;");

                Label lblCls = new Label("Grade " + e.getGrade());
                lblCls.setMinWidth(90);
                lblCls.setStyle("-fx-font-size: 18px; -fx-text-fill: #7f8c8d;");

                Label lblMrk = new Label(e.getTotalMarks() + " Marks");
                lblMrk.setMinWidth(120);
                lblMrk.setStyle("-fx-font-size: 18px; -fx-text-fill: #7f8c8d;");

                Label lblCode = new Label(e.getExamCode());
                lblCode.setMinWidth(100);
                lblCode.setStyle("-fx-text-fill: #8e44ad; -fx-font-weight: bold; -fx-cursor: hand;");

                lblCode.setOnMouseClicked(event -> {
                    javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
                    javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
                    content.putString(e.getExamCode());
                    clipboard.setContent(content);
                    mainApp.showInfo("Copied!", "Exam code " + e.getExamCode() + " copied to clipboard.");
                });

                Label lblAtt = new Label("0%"); lblAtt.setMinWidth(110);
                lblAtt.setStyle("-fx-text-fill: #2980b9; -fx-font-weight: bold;");

                Label lblHigh = new Label("0 / " + e.getTotalMarks()); lblHigh.setMinWidth(100);
                lblHigh.setStyle("-fx-text-fill: #c0392b; -fx-font-weight: bold;");

                Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
                MenuButton options = new MenuButton("...");
                MenuItem mEdit = new MenuItem("Edit Questions");
                MenuItem mStop = new MenuItem("Stop Exam");
                MenuItem mDel = new MenuItem("Delete Exam");
                options.getItems().addAll(mEdit, mStop, mDel);

                mEdit.setOnAction(ev -> {
                    TeacherCreateExam.loadExamForEditing(e, contentArea, mainApp);
                });

                mStop.setOnAction(ev -> {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Stop this exam? It will be moved to 'Past Exams'.", ButtonType.YES, ButtonType.NO);
                    alert.showAndWait().ifPresent(res -> {
                        if (res == ButtonType.YES) {
                            e.setLive(false);

                            // Ensure it has a timestamp so it qualifies for "Past Exams"
                            if (e.getScheduleDetails() == null) {
                                java.time.LocalDateTime now = java.time.LocalDateTime.now();
                                java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                                e.setScheduleDetails("Ended: " + now.format(dtf));
                            }

                            TeacherPastExams.renderPastExams(contentArea, mainApp);
                        }
                    });
                });

                mDel.setOnAction(ev -> {
                    ExamBank.allExams.remove(e);
                    renderDashboardHome(contentArea, mainApp);
                });

                row.getChildren().addAll(lblSub, lblCls, lblMrk, lblCode, lblAtt, lblHigh, spacer, options);
                ongoingList.getChildren().add(row);
                hasLive = true;
            }
        }
        if (!hasLive) {
            Label empty = new Label("No exams are currently live.");
            empty.setPadding(new Insets(20));
            ongoingList.getChildren().add(empty);
        }

        // --- SAVED EXAMS SECTION ---
        Label lblSaved = new Label("Saved Exams");
        lblSaved.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        lblSaved.setLayoutX(300);
        lblSaved.setLayoutY(250);

        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(10, 20, 10, 20));
        header.setStyle("-fx-background-color: #dfe6e9; -fx-background-radius: 5;");
        header.setPrefWidth(780);
        header.setLayoutX(20);
        header.setLayoutY(310);

        Label hSub = new Label("Subject"); hSub.setMinWidth(180);
        Label hCls = new Label("Class");   hCls.setMinWidth(100);
        Label hMrk = new Label("Marks");   hMrk.setMinWidth(120);
        header.getChildren().addAll(hSub, hCls, hMrk);
        header.setStyle(header.getStyle() + "-fx-font-size: 18px; -fx-font-weight: bold;");

        VBox savedList = new VBox(15);
        ScrollPane savedScroll = new ScrollPane(savedList);
        savedScroll.setFitToWidth(true);
        savedScroll.setPrefSize(780, 250);
        savedScroll.setLayoutX(20);
        savedScroll.setLayoutY(355);
        savedScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        for (Exam exam : ExamBank.allExams) {
            if (!exam.isLive() && (exam.getScheduleDetails() == null || exam.getScheduleDetails().isEmpty())) {
                HBox row = new HBox(20);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(15, 20, 15, 20));
                row.setStyle("-fx-background-color: white; -fx-border-color: #dfe6e9; -fx-border-radius: 10;");

                // DATA LABELS: Using setMinWidth is the ONLY way to stop them from disappearing
                Label lblSub = new Label(exam.getSubject());
                lblSub.setMinWidth(180);
                lblSub.setStyle("-fx-font-size: 20px; -fx-text-fill: #2c3e50;");

                Label lblCls = new Label("Grade " + exam.getGrade());
                lblCls.setMinWidth(100);
                lblCls.setStyle("-fx-font-size: 18px; -fx-text-fill: #7f8c8d;");

                Label lblMrk = new Label(exam.getTotalMarks() + " Marks");
                lblMrk.setMinWidth(120);
                lblMrk.setStyle("-fx-font-size: 18px; -fx-text-fill: #7f8c8d;");

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                // BUTTONS: Using your UIUtils
                Button btnLive = new Button("Live");
                btnLive.setPrefSize(90, 45);
                UIUtils.applyButtonEffects(btnLive, "#2ecc71"); // Hover Green

                Button btnSchedule = new Button("Schedule");
                btnSchedule.setPrefSize(110, 45);
                UIUtils.applyButtonEffects(btnSchedule, "#3498db"); // Hover Blue

                MenuButton options = new MenuButton("...");
                options.setPrefSize(30, 25);
                options.setStyle("-fx-font-size: 12px;");

                MenuItem mEdit = new MenuItem("Edit Exam");
                MenuItem mDel = new MenuItem("Delete");
                options.getItems().addAll(mEdit, mDel);

                // Button Logic
                btnLive.setOnAction(e -> showLivePopup(exam, contentArea, mainApp));
                btnSchedule.setOnAction(e -> showSchedulePopup(exam, contentArea, mainApp));
                mEdit.setOnAction(e -> {
                    TeacherCreateExam.loadExamForEditing(exam, contentArea, mainApp);
                });
                mDel.setOnAction(e -> {
                    ExamBank.allExams.remove(exam);
                    renderDashboardHome(contentArea, mainApp);
                });

                row.getChildren().addAll(lblSub, lblCls, lblMrk, spacer, btnLive, btnSchedule, options);
                savedList.getChildren().add(row);
            }
        }

        contentArea.getChildren().addAll( lblOngoing, ongoingHeader, ongoingScroll, lblSaved, header, savedScroll
        );
    }

    private static void showLivePopup(Exam exam, Pane contentArea, HelloApplication mainApp) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Set Availability Window");

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #ffffff;");

        Label lbl = new Label("Set Exam Availability Window:");
        lbl.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #2c3e50;");

        // Helpful hint so the teacher knows the minimum requirement
        Label lblMinReq = new Label("(Min required: " + exam.getDuration() + " mins)");
        lblMinReq.setStyle("-fx-font-size: 11px; -fx-text-fill: #e67e22;");

        HBox inputs = new HBox(10);
        inputs.setAlignment(Pos.CENTER);
        TextField hField = new TextField(); hField.setPromptText("0"); hField.setPrefWidth(50);
        TextField mField = new TextField(); mField.setPromptText("30"); mField.setPrefWidth(50);
        inputs.getChildren().addAll(hField, new Label("hr"), mField, new Label("min"));

        Button btnLaunch = new Button("Launch Exam Now");
        UIUtils.applyButtonEffects(btnLaunch, "#2ecc71");
        btnLaunch.setPrefWidth(180);

        btnLaunch.setOnAction(e -> {
            try {
                int inputHours = hField.getText().isEmpty() ? 0 : Integer.parseInt(hField.getText());
                int inputMins = mField.getText().isEmpty() ? 0 : Integer.parseInt(mField.getText());
                int totalInputMinutes = (inputHours * 60) + inputMins;

                int requiredMins = Integer.parseInt(exam.getDuration());

                if (totalInputMinutes < requiredMins) {
                    mainApp.showError("Time Error", "Availability window cannot be less than exam duration.");
                    return;
                }

                // --- EXISTING LOGIC ---
                exam.setLive(true);
                exam.generateCode();
                exam.setLiveWindow(inputHours + "h " + inputMins + "m");
                java.time.LocalDateTime now = java.time.LocalDateTime.now();
                exam.setScheduleDetails(now.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));

                // --- NEW: AUTO-EXPIRY TIMER ---
                javafx.animation.PauseTransition expiryTimer = new javafx.animation.PauseTransition(javafx.util.Duration.minutes(totalInputMinutes));
                expiryTimer.setOnFinished(event -> {
                    if (exam.isLive()) { // Check if it hasn't been stopped manually already
                        exam.setLive(false);
                        exam.setScheduleDetails("Ended: " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));

                        // Refresh UI if the teacher is currently looking at the dashboard
                        renderDashboardHome(contentArea, mainApp);

                        mainApp.showInfo("Exam Expired", "The exam '" + exam.getSubject() + "' has reached its time limit and moved to Past Exams.");
                    }
                });
                expiryTimer.play();

                stage.close();
                renderDashboardHome(contentArea, mainApp);

            } catch (NumberFormatException ex) {
                mainApp.showError("Invalid Input", "Please enter valid numbers.");
            }
        });

        root.getChildren().addAll(lbl, lblMinReq, inputs, btnLaunch);
        stage.setScene(new Scene(root, 320, 240)); // Increased height slightly for the hint label
        stage.show();
    }



    private static void showSchedulePopup(Exam exam, Pane contentArea, HelloApplication mainApp) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Schedule Exam");

        VBox root = new VBox(15);
        root.setPadding(new Insets(25));
        root.setStyle("-fx-background-color: #f4f7f6;");
        root.setAlignment(Pos.CENTER_LEFT);

        Label lblTitle = new Label("Schedule Exam");
        lblTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        DatePicker dp = new DatePicker();
        dp.setPrefWidth(250);
        dp.setStyle("-fx-font-size: 14px;");

        // --- START TIME ROW ---
        Label lblStart = new Label("Start Time (HH:MM):");
        lblStart.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        HBox startBox = new HBox(10);
        startBox.setAlignment(Pos.CENTER_LEFT);
        TextField startH = new TextField(); startH.setPromptText("HH"); startH.setPrefWidth(60); startH.setStyle("-fx-font-size: 14px;");
        TextField startM = new TextField(); startM.setPromptText("MM"); startM.setPrefWidth(60); startM.setStyle("-fx-font-size: 14px;");
        startBox.getChildren().addAll(startH, new Label(":"), startM);

        // --- END TIME ROW ---
        Label lblEnd = new Label("End Time (HH:MM):");
        lblEnd.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        HBox endBox = new HBox(10);
        endBox.setAlignment(Pos.CENTER_LEFT);
        TextField endH = new TextField(); endH.setPromptText("HH"); endH.setPrefWidth(60); endH.setStyle("-fx-font-size: 14px;");
        TextField endM = new TextField(); endM.setPromptText("MM"); endM.setPrefWidth(60); endM.setStyle("-fx-font-size: 14px;");
        endBox.getChildren().addAll(endH, new Label(":"), endM);

        // --- SAVE BUTTON ---
        Button btnSave = new Button("Confirm Schedule");
        btnSave.setPrefSize(250, 50);
        UIUtils.applyButtonEffects(btnSave, "#9b59b6");

        btnSave.setOnAction(e -> {
            try {
                if (dp.getValue() == null || startH.getText().isEmpty() || endH.getText().isEmpty()) {
                    mainApp.showError("Missing Fields", "Please select a date and enter hours.");
                    return;
                }

                // Parse hours and minutes
                int sh = Integer.parseInt(startH.getText());
                int sm = Integer.parseInt(startM.getText().isEmpty() ? "0" : startM.getText());
                int eh = Integer.parseInt(endH.getText());
                int em = Integer.parseInt(endM.getText().isEmpty() ? "0" : endM.getText());

                // Validate 24h format ranges
                if (sh < 0 || sh > 23 || eh < 0 || eh > 23 || sm < 0 || sm > 59 || em < 0 || em > 59) {
                    mainApp.showError("Invalid Time", "Hours must be 0-23 and Minutes must be 0-59.");
                    return;
                }

                // Convert to total minutes for comparison
                int startTotalMinutes = (sh * 60) + sm;
                int endTotalMinutes = (eh * 60) + em;

                if (startTotalMinutes >= endTotalMinutes) {
                    mainApp.showError("Time Mismatch", "Start time must be strictly earlier than end time.");
                    return;
                }

                String schedule = String.format("%s [%02d:%02d to %02d:%02d]",
                        dp.getValue().toString(), sh, sm, eh, em);

                exam.setScheduleDetails(schedule);
                stage.close();
                renderDashboardHome(contentArea, mainApp);

            } catch (NumberFormatException ex) {
                mainApp.showError("Input Error", "Please enter valid numbers for time.");
            }
        });

        root.getChildren().addAll(lblTitle, new Label("Date:"), dp, lblStart, startBox, lblEnd, endBox, btnSave);
        stage.setScene(new Scene(root, 350, 480));
        stage.show();
    }

}



