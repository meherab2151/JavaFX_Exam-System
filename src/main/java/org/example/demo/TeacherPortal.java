package org.example.demo;

import javafx.animation.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.stage.*;
import javafx.util.Duration;
import java.util.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// ═══════════════════════════════════════════════════════════════════
//  TeacherPortal.java — EduExam Instructor Experience
//  Dashboard: 4 rows — KPI / Live / Scheduled / All Exams
//  Exam state machine: Draft → Scheduled|Live → Draft (reusable)
//  Code generated only when scheduling or going live
// ═══════════════════════════════════════════════════════════════════
public class TeacherPortal {

    static class Toast {
        static void success(Pane p, String msg) { UIUtils.Toast.success(p, msg); }
        static void error(Pane p, String msg)   { UIUtils.Toast.error(p, msg);   }
        static void info(Pane p, String msg)    { UIUtils.Toast.info(p, msg);    }
    }

    static int activeNavIndex = 0;

    // ══════════════════════════════════════════════════════════════
    //  SHARED: auth panel
    // ══════════════════════════════════════════════════════════════
    private static VBox buildAuthPanel(String title, String sub) {
        VBox v = new VBox(0);
        v.setPrefWidth(320);
        v.setAlignment(Pos.CENTER_LEFT);
        v.setPadding(new Insets(0, 0, 0, 44));
        v.setStyle("-fx-background-color:#111722;");

        Region rule = new Region(); rule.setPrefSize(32, 3);
        rule.setStyle("-fx-background-color:#0f7d74;-fx-background-radius:99;");
        VBox.setMargin(rule, new Insets(0, 0, 20, 0));

        StackPane iconBox = new StackPane(UIUtils.icon(UIUtils.ICO_TEACHER, "#0f7d74", 24));
        iconBox.setPrefSize(52, 52);
        iconBox.setStyle("-fx-background-color:rgba(15,125,116,0.14);-fx-background-radius:10;");
        VBox.setMargin(iconBox, new Insets(0, 0, 20, 0));

        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-font-size:22px;-fx-font-weight:700;-fx-text-fill:#e8eaf2;-fx-letter-spacing:-0.5px;");
        VBox.setMargin(titleLbl, new Insets(0, 0, 8, 0));

        Label subLbl = new Label(sub);
        subLbl.setStyle("-fx-font-size:13px;-fx-text-fill:#4a566e;");
        subLbl.setWrapText(true); subLbl.setMaxWidth(240);

        v.getChildren().addAll(rule, iconBox, titleLbl, subLbl);
        return v;
    }

    // ══════════════════════════════════════════════════════════════
    //  1. LOGIN
    // ══════════════════════════════════════════════════════════════
    public static Scene createLoginScene(Stage stage, ArrayList<Teacher> list, HelloApplication app) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color:" + UIUtils.bgContent() + ";");
        root.setLeft(buildAuthPanel("Instructor Portal", "Sign in to manage examinations and review student performance."));

        VBox form = new VBox(16);
        form.setAlignment(Pos.CENTER);
        form.setPadding(new Insets(60, 68, 60, 68));
        form.setMaxWidth(420);

        Label title = new Label("Sign In");
        title.setStyle("-fx-font-size:24px;-fx-font-weight:700;-fx-text-fill:" + UIUtils.textDark() + ";-fx-letter-spacing:-0.3px;");
        Label sub = new Label("Enter your credentials to continue");
        sub.setStyle("-fx-font-size:13px;-fx-text-fill:" + UIUtils.textMid() + ";");

        TextField     txtUser = UIUtils.styledField("Email or Username");
        PasswordField txtPass = UIUtils.styledPassword("Password");

        Button btnLogin = UIUtils.primaryBtn("", "Sign In", UIUtils.ACCENT_BLUE);
        btnLogin.setPrefWidth(Double.MAX_VALUE); btnLogin.setPrefHeight(42);
        Hyperlink linkSignup = new Hyperlink("No account? Register here");
        UIUtils.applyLinkEffects(linkSignup);
        Button btnBack = UIUtils.ghostBtn("", "Back", UIUtils.TEXT_MID);

        btnLogin.setOnAction(e -> {
            String in = txtUser.getText().trim(), pw = txtPass.getText();
            if (in.isEmpty() || pw.isEmpty()) { app.showError("Missing Fields", "Please enter your email and password."); return; }
            Teacher found = UserDAO.loginTeacher(in, pw);
            if (found != null) {
                if (list.stream().noneMatch(t -> t.getEmail().equals(found.getEmail()))) list.add(found);
                stage.setScene(createDashboardScene(stage, found, app));
            } else { app.showError("Login Failed", "Invalid credentials. Please try again."); }
        });
        txtPass.setOnAction(e -> btnLogin.fire());
        linkSignup.setOnAction(e -> stage.setScene(createSignupScene(stage, list, app)));
        btnBack.setOnAction(e -> {
            stage.setScene(app.createMainScene(stage));
            stage.setWidth(1000);
            stage.setHeight(580);
            stage.centerOnScreen();
        });

        String lblS = "-fx-font-size:10px;-fx-font-weight:700;-fx-text-fill:" + UIUtils.textSubtle() + ";-fx-letter-spacing:1.2px;";
        form.getChildren().addAll(
            title, sub, UIUtils.divider(),
            new Label("EMAIL / USERNAME") {{ setStyle(lblS); }}, txtUser,
            new Label("PASSWORD") {{ setStyle(lblS); }}, txtPass,
            btnLogin, linkSignup, btnBack
        );

        ScrollPane sp = new ScrollPane(form);
        sp.setFitToWidth(true); sp.setStyle("-fx-background:transparent;-fx-background-color:transparent;");
        root.setCenter(sp);
        Scene scene = new Scene(root);
        UIUtils.applyStyle(scene);
        javafx.application.Platform.runLater(() -> { stage.setWidth(820); stage.setHeight(560); stage.centerOnScreen(); });
        UIUtils.slideIn(form, true);
        return scene;
    }

    // ══════════════════════════════════════════════════════════════
    //  2. SIGN-UP
    // ══════════════════════════════════════════════════════════════
    public static Scene createSignupScene(Stage stage, ArrayList<Teacher> list, HelloApplication app) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color:" + UIUtils.bgContent() + ";");
        root.setLeft(buildAuthPanel("Create Account", "Register to access EduExam instructor tools."));

        VBox form = new VBox(14);
        form.setAlignment(Pos.CENTER);
        form.setPadding(new Insets(50, 68, 50, 68));
        form.setMaxWidth(420);

        Label title = new Label("Instructor Registration");
        title.setStyle("-fx-font-size:22px;-fx-font-weight:700;-fx-text-fill:" + UIUtils.textDark() + ";-fx-letter-spacing:-0.3px;");

        TextField     txtName    = UIUtils.styledField("Full Name");
        TextField     txtEmail   = UIUtils.styledField("Email Address");
        PasswordField txtPass    = UIUtils.styledPassword("Create Password");
        PasswordField txtConfirm = UIUtils.styledPassword("Confirm Password");

        Button btnReg  = UIUtils.primaryBtn("", "Create Account", UIUtils.ACCENT_BLUE);
        btnReg.setPrefWidth(Double.MAX_VALUE); btnReg.setPrefHeight(42);
        Button btnBack = UIUtils.ghostBtn("", "Back to Sign In", UIUtils.TEXT_MID);

        btnReg.setOnAction(e -> {
            String name=txtName.getText().trim(), email=txtEmail.getText().trim(), pass=txtPass.getText();
            if (!pass.equals(txtConfirm.getText())) { app.showError("Mismatch", "Passwords do not match."); return; }
            if (name.isEmpty()||email.isEmpty()||pass.isEmpty()) { app.showError("Missing Info", "Please fill in all fields."); return; }
            if (UserDAO.teacherEmailExists(email)) { app.showError("Email Taken", "An instructor with that email already exists."); return; }
            if (UserDAO.registerTeacher(name, email, pass)) {
                list.add(new Teacher(name, email, pass));
                app.showInfo("Account Created", "You may now sign in.");
                stage.setScene(createLoginScene(stage, list, app));
            } else { app.showError("Error", "Registration failed. Please try again."); }
        });
        btnBack.setOnAction(e -> stage.setScene(createLoginScene(stage, list, app)));

        String lblS = "-fx-font-size:10px;-fx-font-weight:700;-fx-text-fill:" + UIUtils.textSubtle() + ";-fx-letter-spacing:1.2px;";
        form.getChildren().addAll(
            title, UIUtils.divider(),
            new Label("FULL NAME")  {{ setStyle(lblS); }}, txtName,
            new Label("EMAIL")      {{ setStyle(lblS); }}, txtEmail,
            new Label("PASSWORD")   {{ setStyle(lblS); }}, txtPass,
            new Label("CONFIRM")    {{ setStyle(lblS); }}, txtConfirm,
            btnReg, btnBack
        );

        ScrollPane sp = new ScrollPane(form);
        sp.setFitToWidth(true); sp.setStyle("-fx-background:transparent;-fx-background-color:transparent;");
        root.setCenter(sp);
        Scene scene = new Scene(root);
        UIUtils.applyStyle(scene);
        javafx.application.Platform.runLater(() -> { stage.setWidth(820); stage.setHeight(640); stage.centerOnScreen(); });
        UIUtils.slideIn(form, true);
        return scene;
    }

    // ══════════════════════════════════════════════════════════════
    //  3. DASHBOARD SHELL
    //  Nav: Dashboard | Create Exam | Add Question | Question Bank | Past Exams | Leaderboard | Analytics
    // ══════════════════════════════════════════════════════════════
    static Scene createDashboardScene(Stage stage, Teacher teacher, HelloApplication app) {
        return createDashboardScene(stage, teacher, app, activeNavIndex);
    }

    static Scene createDashboardScene(Stage stage, Teacher teacher, HelloApplication app, int startPage) {
        BorderPane root = new BorderPane();

        VBox sidebar = new VBox(0);
        sidebar.setPrefWidth(210);
        sidebar.setStyle("-fx-background-color:#111722;");

        StackPane themeSwitch = UIUtils.themeToggleSwitch(() ->
            stage.setScene(createDashboardScene(stage, teacher, app, activeNavIndex))
        );
        HBox switchRow = new HBox(themeSwitch);
        switchRow.setAlignment(Pos.CENTER_LEFT);
        switchRow.setPadding(new Insets(14, 0, 0, 14));

        VBox avatarBlock = new VBox(6);
        avatarBlock.setAlignment(Pos.CENTER_LEFT);
        avatarBlock.setPadding(new Insets(18, 14, 16, 16));

        String initials = teacher.getUser().substring(0, 1).toUpperCase();
        Circle avCircle = new Circle(22, Color.web("#0f7d74", 0.18));
        avCircle.setStroke(Color.web("#0f7d74", 0.4)); avCircle.setStrokeWidth(1.5);
        Label avLbl = new Label(initials);
        avLbl.setStyle("-fx-font-size:16px;-fx-font-weight:700;-fx-text-fill:#0f7d74;");
        StackPane av = new StackPane(avCircle, avLbl); av.setPrefSize(44, 44);

        Label nameL = new Label(teacher.getUser());
        nameL.setStyle("-fx-font-size:13px;-fx-font-weight:700;-fx-text-fill:#bdc6d6;-fx-letter-spacing:0.1px;");
        Label roleBadge = new Label("INSTRUCTOR");
        roleBadge.setStyle("-fx-font-size:9.5px;-fx-font-weight:700;-fx-text-fill:#0f7d74;-fx-background-color:rgba(15,125,116,0.16);-fx-padding:2 7;-fx-background-radius:4;-fx-letter-spacing:0.8px;");

        HBox avRow = new HBox(10, av, new VBox(3, nameL, roleBadge) {{ setAlignment(Pos.CENTER_LEFT); }});
        avRow.setAlignment(Pos.CENTER_LEFT);
        avatarBlock.getChildren().add(avRow);

        Region topSep = new Region(); topSep.setPrefHeight(1);
        topSep.setStyle("-fx-background-color:#1e2a3a;");
        VBox.setMargin(topSep, new Insets(0, 14, 8, 14));

        javafx.scene.layout.AnchorPane contentArea = new javafx.scene.layout.AnchorPane();
        contentArea.setPrefSize(790, 600);
        contentArea.setStyle("-fx-background-color:" + UIUtils.bgContent() + ";");

        // Nav: Dashboard | Create Exam | Add Question | Question Bank | Leaderboard | Analytics
        String[][] navDefs = {
            {UIUtils.ICO_DASHBOARD, "Dashboard",     UIUtils.ACCENT_TEAL},
            {UIUtils.ICO_EXAM,      "Create Exam",   UIUtils.ACCENT_PURP},
            {UIUtils.ICO_PLUS,      "Add Question",  UIUtils.ACCENT_YELL},
            {UIUtils.ICO_BANK,      "Question Bank", UIUtils.ACCENT_GREEN},
            {UIUtils.ICO_TROPHY,    "Leaderboard",   "#b45309"},
            {UIUtils.ICO_ANALYTICS, "Analytics",     UIUtils.ACCENT_TEAL},
        };

        StackPane[] navBtns = new StackPane[navDefs.length];
        VBox navBox = new VBox(5);
        navBox.setPadding(new Insets(0, 10, 8, 10));

        for (int i = 0; i < navDefs.length; i++) {
            final int idx = i;
            navBtns[i] = UIUtils.modernSidebarBtn(navDefs[i][0], navDefs[i][1], navDefs[i][2]);
            navBtns[i].setOnMouseClicked(e -> {
                activeNavIndex = idx;
                for (StackPane nb : navBtns) UIUtils.modernSidebarSetInactive(nb);
                UIUtils.modernSidebarSetActive(navBtns[idx]);
                contentArea.getChildren().clear();
                if (idx != 1) { ExamEditor.clearState(); ExamEditor.editing = null; }
                dispatchPage(idx, contentArea, stage, teacher, app);
                UIUtils.slideIn(contentArea, true);
            });
            navBox.getChildren().add(navBtns[i]);
        }

        ScrollPane navScroll = new ScrollPane(navBox);
        navScroll.setFitToWidth(true);
        navScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        navScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        navScroll.setStyle("-fx-background:transparent;-fx-background-color:transparent;-fx-border-color:transparent;");
        VBox.setVgrow(navScroll, Priority.ALWAYS);

        VBox logoutBox = new VBox();
        logoutBox.setPadding(new Insets(8, 10, 14, 10));
        logoutBox.setStyle("-fx-background-color:#111722;-fx-border-color:#1e2a3a;-fx-border-width:1 0 0 0;");
        Button btnLogout = UIUtils.primaryBtn("", "Sign Out", UIUtils.ACCENT_RED);
        btnLogout.setPrefWidth(190);
        btnLogout.setOnAction(e -> {
            Alert c = new Alert(Alert.AlertType.CONFIRMATION);
            c.setTitle("Sign Out"); c.setHeaderText(null);
            c.setContentText("Sign out and return to the home screen?");
            c.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
            c.showAndWait().ifPresent(r -> {
                if (r == ButtonType.YES) { ExamEditor.clearState(); ExamEditor.editing = null; stage.setScene(app.createMainScene(stage)); }
            });
        });
        logoutBox.getChildren().add(btnLogout);

        sidebar.getChildren().addAll(switchRow, avatarBlock, topSep, navScroll, logoutBox);
        sidebar.setPrefHeight(Double.MAX_VALUE);
        root.setLeft(sidebar);
        root.setCenter(contentArea);

        UIUtils.modernSidebarSetActive(navBtns[startPage]);
        contentArea.getChildren().clear();
        dispatchPage(startPage, contentArea, stage, teacher, app);

        Scene dashScene = new Scene(root, 1100, 660);
        javafx.application.Platform.runLater(() -> { stage.setWidth(1100); stage.setHeight(660); stage.centerOnScreen(); });
        return dashScene;
    }

    private static void dispatchPage(int idx, Pane ca, Stage stage, Teacher teacher, HelloApplication app) {
        switch (idx) {
            case 0 -> renderDashboardHome(ca, stage, teacher, app);
            case 1 -> ExamEditor.show(ca, stage, teacher, app);
            case 2 -> QuestionEditor.show(ca, stage, teacher, app, null, null);
            case 3 -> QuestionBankBrowser.render(ca, stage, teacher, app);
            case 4 -> renderLeaderboard(ca, stage, teacher, app);
            case 5 -> renderAnalytics(ca, stage, teacher, app);
        }
    }

    // ── Back button helper ─────────────────────────────────────────
    private static Button backBtn(Pane ca, Stage stage, Teacher teacher, HelloApplication app) {
        Button btn = new Button();
        HBox inner = new HBox(6, UIUtils.icon(UIUtils.ICO_BACK, UIUtils.ACCENT_TEAL, 13), new Label("Back") {{ setStyle("-fx-font-size:12.5px;-fx-font-weight:600;-fx-text-fill:" + UIUtils.ACCENT_TEAL + ";"); }});
        inner.setAlignment(Pos.CENTER_LEFT);
        btn.setGraphic(inner);
        btn.setStyle("-fx-background-color:" + UIUtils.ACCENT_TEAL + "14;-fx-background-radius:6;-fx-border-color:" + UIUtils.ACCENT_TEAL + "40;-fx-border-radius:6;-fx-border-width:1;-fx-padding:6 14;-fx-cursor:hand;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color:" + UIUtils.ACCENT_TEAL + "26;-fx-background-radius:6;-fx-border-color:" + UIUtils.ACCENT_TEAL + "70;-fx-border-radius:6;-fx-border-width:1;-fx-padding:6 14;-fx-cursor:hand;"));
        btn.setOnMouseExited(e  -> btn.setStyle("-fx-background-color:" + UIUtils.ACCENT_TEAL + "14;-fx-background-radius:6;-fx-border-color:" + UIUtils.ACCENT_TEAL + "40;-fx-border-radius:6;-fx-border-width:1;-fx-padding:6 14;-fx-cursor:hand;"));
        btn.setOnAction(e -> {
            activeNavIndex = 0;
            ca.getChildren().clear();
            renderDashboardHome(ca, stage, teacher, app);
            UIUtils.slideIn(ca, false);
        });
        return btn;
    }

    // ══════════════════════════════════════════════════════════════
    //  4. DASHBOARD HOME — 4 ROWS
    //  Row 1: Greeting + KPI stats
    //  Row 2: Live Examinations (full width, always expanded)
    //  Row 3: Scheduled Examinations (full width, always expanded)
    //  Row 4: All Examinations (draft / reusable exams)
    // ══════════════════════════════════════════════════════════════
    public static void renderDashboardHome(Pane contentArea, Stage stage, Teacher teacher, HelloApplication app) {
        contentArea.getChildren().clear();

        ScrollPane scroll = new ScrollPane();
        scroll.prefWidthProperty().bind(contentArea.widthProperty());
        scroll.prefHeightProperty().bind(contentArea.heightProperty());
        scroll.setStyle("-fx-background:transparent;-fx-background-color:transparent;");
        scroll.setFitToWidth(true);

        VBox page = new VBox(0);
        page.setStyle("-fx-background-color:" + UIUtils.bgContent() + ";");

        // ── ROW 1: Greeting + KPI ─────────────────────────────────
        VBox row1 = new VBox(14);
        row1.setPadding(new Insets(28, 32, 20, 32));
        row1.setStyle("-fx-background-color:" + UIUtils.bgContent() + ";");

        int hour = java.time.LocalTime.now().getHour();
        String greet = hour < 12 ? "Good morning" : hour < 17 ? "Good afternoon" : "Good evening";
        String dateStr = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy"));
        Label greetL = new Label(greet + ", " + teacher.getUser().split(" ")[0]);
        greetL.setStyle("-fx-font-size:20px;-fx-font-weight:700;-fx-text-fill:" + UIUtils.textDark() + ";-fx-letter-spacing:-0.3px;");
        Label dateL  = new Label(dateStr);
        dateL.setStyle("-fx-font-size:12.5px;-fx-text-fill:" + UIUtils.textSubtle() + ";");

        long liveCount  = ExamBank.getLiveExams().size();
        long schedCount = ExamBank.getScheduledExams().size();
        long draftCount = ExamBank.getDraftExams().size();
        long totalQ     = QuestionBank.allQuestions.size();
        long studentCnt = UserDAO.loadAllStudents().size();
        long subsCnt    = ResultDAO.loadAll().size();

        HBox stats = new HBox(10);
        stats.getChildren().addAll(
            UIUtils.statCard(UIUtils.ICO_LIVE,      String.valueOf(liveCount),  "Live",        UIUtils.ACCENT_GREEN),
            UIUtils.statCard(UIUtils.ICO_SCHEDULE,  String.valueOf(schedCount), "Scheduled",   UIUtils.ACCENT_PURP),
            UIUtils.statCard(UIUtils.ICO_EXAM,      String.valueOf(draftCount), "All Exams",   UIUtils.ACCENT_TEAL),
            UIUtils.statCard(UIUtils.ICO_QUESTION,  String.valueOf(totalQ),     "Questions",   UIUtils.ACCENT_ORG),
            UIUtils.statCard(UIUtils.ICO_USERS,     String.valueOf(studentCnt), "Students",    UIUtils.ACCENT_RED),
            UIUtils.statCard(UIUtils.ICO_ANALYTICS, String.valueOf(subsCnt),    "Submissions", UIUtils.ACCENT_YELL)
        );

        row1.getChildren().addAll(new VBox(2, greetL, dateL), stats);

        // ── ROW 2: LIVE EXAMINATIONS ──────────────────────────────
        VBox row2 = new VBox(12);
        row2.setPadding(new Insets(20, 32, 20, 32));
        row2.setStyle("-fx-background-color:" + UIUtils.bgContent() + ";-fx-border-color:" + UIUtils.border() + ";-fx-border-width:1 0 0 0;");

        // Section header — dot + label (same style for all sections)
        HBox liveHdr = buildSectionHeader("Live Examinations", liveCount, "#0e7a56", true);

        VBox liveBody = new VBox(8);
        liveBody.setMaxWidth(Double.MAX_VALUE);
        if (ExamBank.getLiveExams().isEmpty()) {
            liveBody.getChildren().add(buildEmptyRow("No live examinations at the moment. Go live from All Examinations below."));
        } else {
            for (Exam e : ExamBank.getLiveExams())
                liveBody.getChildren().add(buildLiveRow(e, contentArea, stage, teacher, app));
        }

        row2.getChildren().addAll(liveHdr, liveBody);

        // ── ROW 3: SCHEDULED EXAMINATIONS ────────────────────────
        VBox row3 = new VBox(12);
        row3.setPadding(new Insets(20, 32, 20, 32));
        row3.setStyle("-fx-background-color:" + UIUtils.bgContent() + ";-fx-border-color:" + UIUtils.border() + ";-fx-border-width:1 0 0 0;");

        HBox schedHdr = buildSectionHeader("Scheduled Examinations", schedCount, UIUtils.ACCENT_PURP, false);

        VBox schedBody = new VBox(8);
        schedBody.setMaxWidth(Double.MAX_VALUE);
        if (ExamBank.getScheduledExams().isEmpty()) {
            schedBody.getChildren().add(buildEmptyRow("No scheduled examinations. Schedule an exam from All Examinations below."));
        } else {
            for (Exam e : ExamBank.getScheduledExams())
                schedBody.getChildren().add(buildScheduledRow(e, contentArea, stage, teacher, app));
        }

        row3.getChildren().addAll(schedHdr, schedBody);

        // ── ROW 4: ALL EXAMINATIONS (draft / reusable) ────────────
        VBox row4 = new VBox(12);
        row4.setPadding(new Insets(20, 32, 28, 32));
        row4.setStyle("-fx-background-color:" + UIUtils.bgContent() + ";-fx-border-color:" + UIUtils.border() + ";-fx-border-width:1 0 0 0;");

        HBox allHdr = buildSectionHeader("All Examinations", draftCount, UIUtils.ACCENT_TEAL, false);
        Button btnNewExam = UIUtils.primaryBtn("", "New Exam", UIUtils.ACCENT_PURP);
        btnNewExam.setPrefHeight(34);
        btnNewExam.setOnAction(e -> {
            activeNavIndex = 1;
            contentArea.getChildren().clear();
            ExamEditor.show(contentArea, stage, teacher, app);
            UIUtils.slideIn(contentArea, true);
        });
        HBox allHdrRow = new HBox(12, allHdr, new Region() {{ HBox.setHgrow(this, Priority.ALWAYS); }}, btnNewExam);
        allHdrRow.setAlignment(Pos.CENTER_LEFT);

        VBox allBody = new VBox(8);
        allBody.setMaxWidth(Double.MAX_VALUE);
        List<Exam> drafts = ExamBank.getDraftExams();
        if (drafts.isEmpty()) {
            allBody.getChildren().add(buildEmptyRow("No examinations created yet. Click 'New Exam' to create your first examination."));
        } else {
            for (Exam e : drafts)
                allBody.getChildren().add(buildAllExamRow(e, contentArea, stage, teacher, app));
        }

        row4.getChildren().addAll(allHdrRow, allBody);

        page.getChildren().addAll(row1, row2, row3, row4);
        scroll.setContent(page);
        contentArea.getChildren().add(scroll);
        if (contentArea instanceof javafx.scene.layout.AnchorPane ap) {
            javafx.scene.layout.AnchorPane.setTopAnchor(scroll, 0.0);
            javafx.scene.layout.AnchorPane.setBottomAnchor(scroll, 0.0);
            javafx.scene.layout.AnchorPane.setLeftAnchor(scroll, 0.0);
            javafx.scene.layout.AnchorPane.setRightAnchor(scroll, 0.0);
        }
        UIUtils.slideIn(page, true);
    }

    // ── Consistent section header: dot + label + count badge ──────
    private static HBox buildSectionHeader(String title, long count, String color, boolean animated) {
        HBox hdr = new HBox(10); hdr.setAlignment(Pos.CENTER_LEFT);

        if (animated) {
            Circle dot    = new Circle(4, Color.web(color));
            Circle ripple = new Circle(4, Color.web(color)); ripple.setOpacity(0);
            ripple.setMouseTransparent(true);
            Timeline sonar = new Timeline(
                new KeyFrame(Duration.ZERO,     new KeyValue(ripple.radiusProperty(), 4.0),  new KeyValue(ripple.opacityProperty(), 0.55)),
                new KeyFrame(Duration.millis(900), new KeyValue(ripple.radiusProperty(), 10.0), new KeyValue(ripple.opacityProperty(), 0.0))
            );
            sonar.setCycleCount(Timeline.INDEFINITE); sonar.play();
            StackPane dotStack = new StackPane(ripple, dot);
            dotStack.setPrefSize(16, 16); dotStack.setMinSize(16, 16); dotStack.setMaxSize(16, 16);
            dotStack.setClip(new Circle(8, 8, 8));
            dotStack.setMouseTransparent(true);
            dotStack.sceneProperty().addListener((obs, o, n) -> { if (n == null) sonar.stop(); });
            hdr.getChildren().add(dotStack);
        } else {
            Circle dot = new Circle(4, Color.web(color));
            StackPane dotStack = new StackPane(dot);
            dotStack.setPrefSize(16, 16); dotStack.setMinSize(16, 16); dotStack.setMaxSize(16, 16);
            hdr.getChildren().add(dotStack);
        }

        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-font-size:14px;-fx-font-weight:700;-fx-text-fill:" + UIUtils.textDark() + ";-fx-letter-spacing:-0.1px;");

        Label badge = UIUtils.badge(String.valueOf(count), color);

        hdr.getChildren().addAll(titleLbl, badge);
        return hdr;
    }

    // ── Empty state row (full width) ──────────────────────────────
    private static HBox buildEmptyRow(String msg) {
        HBox row = new HBox();
        row.setMaxWidth(Double.MAX_VALUE);
        row.setPadding(new Insets(16, 20, 16, 20));
        row.setStyle(
            "-fx-background-color:" + UIUtils.bgCard() + ";" +
            "-fx-background-radius:9;-fx-border-color:" + UIUtils.border() + ";" +
            "-fx-border-radius:9;-fx-border-width:1;" +
            "-fx-border-style:dashed;"
        );
        Label lbl = new Label(msg);
        lbl.setStyle("-fx-font-size:12.5px;-fx-text-fill:" + UIUtils.textSubtle() + ";");
        row.getChildren().add(lbl);
        return row;
    }

    // ══════════════════════════════════════════════════════════════
    //  END EXAM HELPER — returns exam to draft
    // ══════════════════════════════════════════════════════════════
    private static void endExam(Exam e, boolean termination, Pane ca, Stage stage, Teacher teacher, HelloApplication app) {
        e.resetToDraft();
        ExamDAO.save(e);
        renderDashboardHome(ca, stage, teacher, app);
        String displayTitle = (e.getTitle() != null && !e.getTitle().isEmpty()) ? e.getTitle() : e.getSubject();
        UIUtils.Toast.info(ca, displayTitle + (termination ? " terminated" : " ended") + " — returned to All Examinations");
    }

    // ══════════════════════════════════════════════════════════════
    //  LIVE EXAM ROW — radiating dot only, no text vibration, no shadow
    //  Buttons inline: Stop | Edit | Delete
    // ══════════════════════════════════════════════════════════════
    private static VBox buildLiveRow(Exam e, Pane ca, Stage stage, Teacher teacher, HelloApplication app) {
        VBox wrapper = new VBox();

        HBox row = new HBox(12); row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(13, 18, 13, 18));
        row.setMaxWidth(Double.MAX_VALUE);
        row.setStyle(
            "-fx-background-color:" + UIUtils.bgCard() + ";" +
            "-fx-background-radius:9;" +
            "-fx-border-color:rgba(14,122,86,0.35);" +
            "-fx-border-radius:9;-fx-border-width:1.5;"
        );
        // No shadow — removed per request

        // Radiating sonar dot — fixed-size clip so ripple never shifts layout
        Circle dot = new Circle(4, Color.web("#0e7a56"));
        Circle ripple = new Circle(4, Color.web("#0e7a56")); ripple.setOpacity(0);
        ripple.setMouseTransparent(true);
        Timeline sonar = new Timeline(
            new KeyFrame(Duration.ZERO,    new KeyValue(ripple.radiusProperty(), 4.0),  new KeyValue(ripple.opacityProperty(), 0.55)),
            new KeyFrame(Duration.millis(900), new KeyValue(ripple.radiusProperty(), 11.0), new KeyValue(ripple.opacityProperty(), 0.0))
        );
        sonar.setCycleCount(Timeline.INDEFINITE); sonar.play();
        // Use a fixed-size StackPane with clip so expanding ripple never causes row layout shift
        StackPane dotStack = new StackPane(ripple, dot);
        dotStack.setPrefSize(22, 22);
        dotStack.setMinSize(22, 22);
        dotStack.setMaxSize(22, 22);
        dotStack.setClip(new Circle(11, 11, 11)); // clip to its own bounds
        dotStack.setMouseTransparent(true);

        Label liveBadge = new Label("LIVE");
        liveBadge.setStyle("-fx-font-size:9.5px;-fx-font-weight:700;-fx-text-fill:#0e7a56;-fx-background-color:#d1f0e8;-fx-padding:2 8;-fx-background-radius:4;-fx-letter-spacing:1px;");
        HBox livePill = new HBox(6, dotStack, liveBadge); livePill.setAlignment(Pos.CENTER_LEFT);

        String displayTitle = (e.getTitle()!=null&&!e.getTitle().isEmpty()) ? e.getTitle() : e.getSubject();
        // Static labels — NO animation on text
        Label subL = new Label(displayTitle);
        subL.setStyle("-fx-font-size:14px;-fx-font-weight:700;-fx-text-fill:" + UIUtils.textDark() + ";-fx-letter-spacing:-0.1px;");
        Label metaL = new Label(e.getSubject() + "  ·  Grade " + e.getGrade() + "  ·  " + e.getDuration() + " min  ·  " + (int)e.getTotalMarks() + " marks");
        metaL.setStyle("-fx-font-size:11px;-fx-text-fill:" + UIUtils.textSubtle() + ";");

        // Countdown timer — no blink on text
        Label countdown = new Label(e.getRemainingFormatted());
        countdown.setStyle("-fx-font-family:Monospaced;-fx-font-size:13px;-fx-font-weight:700;-fx-text-fill:#0e7a56;-fx-background-color:rgba(14,122,86,0.10);-fx-background-radius:5;-fx-padding:3 10;");
        final Timeline[] tlRef = { null };
        Timeline tl = new Timeline(new KeyFrame(Duration.seconds(1), ev -> {
            countdown.setText(e.getRemainingFormatted());
            // No opacity blink — text stays still
            if (e.isExpired()) {
                if (tlRef[0]!=null) tlRef[0].stop();
                sonar.stop();
                endExam(e, false, ca, stage, teacher, app);
            }
        }));
        tlRef[0] = tl; tl.setCycleCount(Timeline.INDEFINITE); tl.play();
        row.sceneProperty().addListener((obs,o,n) -> { if (n==null) { tl.stop(); sonar.stop(); }});

        // Code label — large and legible for demo visibility
        Label codeLbl = new Label(e.getExamCode());
        codeLbl.setStyle(
            "-fx-font-family:Monospaced;" +
            "-fx-font-size:22px;" +
            "-fx-font-weight:700;" +
            "-fx-text-fill:#5046a0;" +
            "-fx-letter-spacing:8px;" +
            "-fx-background-color:rgba(80,70,160,0.12);" +
            "-fx-background-radius:8;" +
            "-fx-padding:6 18;" +
            "-fx-cursor:hand;"
        );
        Tooltip.install(codeLbl, new Tooltip("Click to copy"));
        codeLbl.setOnMouseClicked(ev -> {
            javafx.scene.input.Clipboard cb2 = javafx.scene.input.Clipboard.getSystemClipboard();
            javafx.scene.input.ClipboardContent cc = new javafx.scene.input.ClipboardContent();
            cc.putString(e.getExamCode()); cb2.setContent(cc);
            codeLbl.setText(e.getExamCode() + "  Copied");
            PauseTransition revert = new PauseTransition(Duration.seconds(1.5));
            revert.setOnFinished(ev2 -> codeLbl.setText(e.getExamCode()));
            revert.play();
            UIUtils.Toast.success(ca, "Code " + e.getExamCode() + " copied");
        });

        Label codeCaption = new Label("EXAM CODE");
        codeCaption.setStyle("-fx-font-size:9px;-fx-font-weight:700;-fx-text-fill:#9aa1b0;-fx-letter-spacing:1.4px;");
        VBox codeBlock = new VBox(2, codeCaption, codeLbl);
        codeBlock.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        VBox info = new VBox(5, subL, metaL, new HBox(16, countdown, codeBlock) {{ setAlignment(javafx.geometry.Pos.CENTER_LEFT); }});

        Region sp1 = new Region(); HBox.setHgrow(sp1, Priority.ALWAYS);

        // Inline action buttons: Stop | Edit | Delete (no menu)
        Button btnStop = inlineBtn("Stop", "#b45309", "#b4530914");
        btnStop.setOnAction(ev -> {
            Alert a = new Alert(Alert.AlertType.CONFIRMATION, "Terminate this examination early?", ButtonType.YES, ButtonType.NO);
            a.setHeaderText(null); a.showAndWait().ifPresent(r -> { if (r == ButtonType.YES) { tl.stop(); sonar.stop(); endExam(e, true, ca, stage, teacher, app); }});
        });
        Button btnEdit = inlineBtn("Edit", "#0f7d74", "#0f7d7414");
        btnEdit.setOnAction(ev -> ExamEditor.loadForEditing(e, ca, stage, teacher, app));
        Button btnDel = inlineBtn("Delete", UIUtils.ACCENT_RED, UIUtils.ACCENT_RED + "14");
        btnDel.setOnAction(ev -> {
            Alert a = new Alert(Alert.AlertType.CONFIRMATION, "Delete this examination?", ButtonType.YES, ButtonType.NO);
            a.setHeaderText(null); a.showAndWait().ifPresent(r -> { if (r==ButtonType.YES) { ExamDAO.delete(e); ExamBank.allExams.remove(e); renderDashboardHome(ca, stage, teacher, app); }});
        });

        row.getChildren().addAll(livePill, info, sp1, btnStop, btnEdit, btnDel);
        wrapper.getChildren().add(row);
        wrapper.setMaxWidth(Double.MAX_VALUE);
        return wrapper;
    }

    // ══════════════════════════════════════════════════════════════
    //  SCHEDULED EXAM ROW — no shadow, inline buttons: Live | Edit | Delete
    // ══════════════════════════════════════════════════════════════
    private static HBox buildScheduledRow(Exam e, Pane ca, Stage stage, Teacher teacher, HelloApplication app) {
        HBox row = new HBox(12); row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(13, 18, 13, 18));
        row.setMaxWidth(Double.MAX_VALUE);
        row.setStyle("-fx-background-color:" + UIUtils.bgCard() + ";-fx-background-radius:9;-fx-border-color:" + UIUtils.border() + ";-fx-border-radius:9;-fx-border-width:1;");
        // No shadow

        String displayTitle = (e.getTitle()!=null&&!e.getTitle().isEmpty()) ? e.getTitle() : e.getSubject();
        VBox info = new VBox(3);
        Label subL = new Label(displayTitle);
        subL.setStyle("-fx-font-size:14px;-fx-font-weight:700;-fx-text-fill:" + UIUtils.textDark() + ";-fx-letter-spacing:-0.1px;");
        Label metaL = new Label(e.getSubject() + "  ·  Grade " + e.getGrade() + "  ·  " + e.getDuration() + " min  ·  " + (int)e.getTotalMarks() + " marks");
        metaL.setStyle("-fx-font-size:11px;-fx-text-fill:" + UIUtils.textSubtle() + ";");
        info.getChildren().addAll(subL, metaL);

        if (e.getScheduledStartMillis() > 0 && e.getScheduledStartMillis() > System.currentTimeMillis()) {
            Label countdown = new Label("Starts in  " + e.getStartCountdownFormatted());
            countdown.setStyle("-fx-font-family:Monospaced;-fx-font-size:11.5px;-fx-font-weight:700;-fx-text-fill:#5046a0;-fx-background-color:rgba(80,70,160,0.10);-fx-background-radius:4;-fx-padding:3 9;");
            Timeline autoTl = new Timeline(new KeyFrame(Duration.seconds(1), ev -> {
                long n = System.currentTimeMillis();
                if (n >= e.getScheduledStartMillis() && !e.isLive()) {
                    e.setLive(true);
                    long wMs = e.getScheduledEndMillis() - e.getScheduledStartMillis();
                    long wMin = wMs / 60_000L;
                    e.setLiveWindow((wMin/60) + "h " + (wMin%60) + "m");
                    e.setLiveEndMillis(e.getScheduledEndMillis());
                    ExamDAO.save(e);
                    renderDashboardHome(ca, stage, teacher, app);
                    UIUtils.Toast.success(ca, displayTitle + " went live automatically");
                } else countdown.setText("Starts in  " + e.getStartCountdownFormatted());
            }));
            autoTl.setCycleCount(Timeline.INDEFINITE); autoTl.play();
            countdown.sceneProperty().addListener((obs,o,n)-> { if(n==null) autoTl.stop(); });

            java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("dd MMM  ·  HH:mm");
            String startStr = java.time.LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(e.getScheduledStartMillis()), java.time.ZoneId.systemDefault()).format(fmt);
            String endStr   = java.time.LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(e.getScheduledEndMillis()),   java.time.ZoneId.systemDefault()).format(fmt);
            Label window = new Label(startStr + "  →  " + endStr);
            window.setStyle("-fx-font-size:11px;-fx-text-fill:" + UIUtils.textSubtle() + ";");
            info.getChildren().addAll(window, countdown);
        }

        // Access code
        if (e.getExamCode() != null && !e.getExamCode().isBlank()) {
            Label codeL = new Label("Code: " + e.getExamCode());
            codeL.setStyle("-fx-font-family:Monospaced;-fx-font-size:11px;-fx-font-weight:700;-fx-text-fill:#5046a0;-fx-cursor:hand;");
            codeL.setOnMouseClicked(ev -> {
                javafx.scene.input.Clipboard cb3 = javafx.scene.input.Clipboard.getSystemClipboard();
                javafx.scene.input.ClipboardContent cc3 = new javafx.scene.input.ClipboardContent();
                cc3.putString(e.getExamCode()); cb3.setContent(cc3);
                UIUtils.Toast.success(ca, "Code " + e.getExamCode() + " copied");
            });
            info.getChildren().add(codeL);
        }

        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);

        // Inline buttons: Live (green) | Edit | Delete (red)
        Button btnLive = UIUtils.primaryBtn("", "Live", UIUtils.ACCENT_GREEN);
        btnLive.setPrefHeight(34);
        btnLive.setOnAction(ev -> showLaunchPopup(e, ca, stage, teacher, app));

        Button btnEdit = inlineBtn("Edit", "#0f7d74", "#0f7d7414");
        btnEdit.setOnAction(ev -> ExamEditor.loadForEditing(e, ca, stage, teacher, app));

        Button btnDel = inlineBtn("Delete", UIUtils.ACCENT_RED, UIUtils.ACCENT_RED + "14");
        btnDel.setOnAction(ev -> {
            Alert a = new Alert(Alert.AlertType.CONFIRMATION, "Delete this examination?", ButtonType.YES, ButtonType.NO);
            a.setHeaderText(null); a.showAndWait().ifPresent(r -> { if(r==ButtonType.YES) { ExamDAO.delete(e); ExamBank.allExams.remove(e); renderDashboardHome(ca, stage, teacher, app); }});
        });

        row.getChildren().addAll(info, sp, btnLive, btnEdit, btnDel);
        return row;
    }

    // ══════════════════════════════════════════════════════════════
    //  ALL EXAM ROW — no shadow, inline: Live (green) | Schedule | Edit | Delete (red)
    // ══════════════════════════════════════════════════════════════
    private static HBox buildAllExamRow(Exam e, Pane ca, Stage stage, Teacher teacher, HelloApplication app) {
        HBox row = new HBox(10); row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(13, 18, 13, 18));
        row.setMaxWidth(Double.MAX_VALUE);
        row.setStyle("-fx-background-color:" + UIUtils.bgCard() + ";-fx-background-radius:9;-fx-border-color:" + UIUtils.border() + ";-fx-border-radius:9;-fx-border-width:1;");
        // No shadow

        String displayTitle = (e.getTitle()!=null&&!e.getTitle().isEmpty()) ? e.getTitle() : e.getSubject();
        VBox info = new VBox(3);
        Label subL = new Label(displayTitle);
        subL.setStyle("-fx-font-size:14px;-fx-font-weight:700;-fx-text-fill:" + UIUtils.textDark() + ";-fx-letter-spacing:-0.1px;");
        Label metaL = new Label(e.getSubject() + "  ·  Grade " + e.getGrade() + "  ·  " + e.getDuration() + " min  ·  " + (int)e.getTotalMarks() + " marks  ·  " + e.getQuestionsMap().size() + " questions");
        metaL.setStyle("-fx-font-size:11px;-fx-text-fill:" + UIUtils.textSubtle() + ";");
        info.getChildren().addAll(subL, metaL);

        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);

        // Live button (green)
        Button btnLive = UIUtils.primaryBtn("", "Live", UIUtils.ACCENT_GREEN);
        btnLive.setPrefHeight(34);
        btnLive.setOnAction(ev -> showLaunchPopup(e, ca, stage, teacher, app));

        // Schedule button (purple/teal)
        Button btnSched = new Button("Schedule");
        btnSched.setStyle("-fx-background-color:rgba(80,70,160,0.10);-fx-text-fill:#5046a0;-fx-font-weight:700;-fx-font-size:12px;-fx-background-radius:6;-fx-padding:7 14;-fx-cursor:hand;-fx-border-color:#5046a040;-fx-border-radius:6;-fx-border-width:1;");
        btnSched.setOnMouseEntered(ev -> btnSched.setStyle("-fx-background-color:rgba(80,70,160,0.18);-fx-text-fill:#5046a0;-fx-font-weight:700;-fx-font-size:12px;-fx-background-radius:6;-fx-padding:7 14;-fx-cursor:hand;-fx-border-color:#5046a060;-fx-border-radius:6;-fx-border-width:1;"));
        btnSched.setOnMouseExited(ev  -> btnSched.setStyle("-fx-background-color:rgba(80,70,160,0.10);-fx-text-fill:#5046a0;-fx-font-weight:700;-fx-font-size:12px;-fx-background-radius:6;-fx-padding:7 14;-fx-cursor:hand;-fx-border-color:#5046a040;-fx-border-radius:6;-fx-border-width:1;"));
        btnSched.setOnAction(ev -> showSchedulePopup(e, ca, stage, teacher, app));

        // Edit button
        Button btnEdit = inlineBtn("Edit", "#0f7d74", "#0f7d7414");
        btnEdit.setOnAction(ev -> ExamEditor.loadForEditing(e, ca, stage, teacher, app));

        // Delete button (red)
        Button btnDel = inlineBtn("Delete", UIUtils.ACCENT_RED, UIUtils.ACCENT_RED + "14");
        btnDel.setOnAction(ev -> {
            Alert a = new Alert(Alert.AlertType.CONFIRMATION, "Delete this examination?", ButtonType.YES, ButtonType.NO);
            a.setHeaderText(null); a.showAndWait().ifPresent(r -> { if(r==ButtonType.YES) { ExamDAO.delete(e); ExamBank.allExams.remove(e); renderDashboardHome(ca, stage, teacher, app); }});
        });

        row.getChildren().addAll(info, sp, btnLive, btnSched, btnEdit, btnDel);
        return row;
    }

    // ══════════════════════════════════════════════════════════════
    //  LEADERBOARD
    // ══════════════════════════════════════════════════════════════
    private static void renderLeaderboard(Pane contentArea, Stage stage, Teacher teacher, HelloApplication app) {
        contentArea.getChildren().clear();
        ScrollPane scroll = new ScrollPane(); scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background:transparent;-fx-background-color:transparent;");

        VBox page = new VBox(20); page.setPadding(new Insets(28, 32, 28, 32));
        page.setStyle("-fx-background-color:" + UIUtils.bgContent() + ";");

        HBox titleRow = new HBox(14); titleRow.setAlignment(Pos.CENTER_LEFT);
        titleRow.getChildren().addAll(backBtn(contentArea, stage, teacher, app), UIUtils.heading("Leaderboard"));
        page.getChildren().addAll(titleRow, UIUtils.subheading("Top submissions per examination"), UIUtils.divider());

        List<ExamResult> all = ResultDAO.loadAll();
        if (all.isEmpty()) {
            page.getChildren().add(emptyStateCard(UIUtils.ICO_TROPHY, "No Results Yet", "Students have not submitted any examinations yet.", null, null, null));
        } else {
            Map<Integer, List<ExamResult>> byExam = new java.util.LinkedHashMap<>();
            for (ExamResult r : all) byExam.computeIfAbsent(r.examId, k->new ArrayList<>()).add(r);

            for (Map.Entry<Integer, List<ExamResult>> entry : byExam.entrySet()) {
                List<ExamResult> top = entry.getValue().stream().sorted((a,b)->Double.compare(b.score,a.score)).collect(Collectors.toList());
                ExamResult first = top.get(0);
                String examLabel = (first.examTitle!=null&&!first.examTitle.isBlank()) ? first.examTitle : first.examSubject;

                VBox card = UIUtils.card(760); card.setMaxWidth(Double.MAX_VALUE);
                card.setPadding(new Insets(16, 20, 16, 20)); card.setSpacing(8);

                HBox hdr = new HBox(10); hdr.setAlignment(Pos.CENTER_LEFT);
                Region trophyIco = UIUtils.icon(UIUtils.ICO_TROPHY, UIUtils.ACCENT_ORG, 14);
                Label titleLbl = new Label(examLabel);
                titleLbl.setStyle("-fx-font-size:14px;-fx-font-weight:700;-fx-text-fill:" + UIUtils.textDark() + ";");
                Label subBadge = UIUtils.badge(first.examSubject + "  Grade " + first.examGrade, UIUtils.ACCENT_TEAL);
                Label cntBadge = UIUtils.badge(top.size() + " submissions", UIUtils.ACCENT_GREEN);
                hdr.getChildren().addAll(trophyIco, titleLbl, subBadge, cntBadge);
                card.getChildren().addAll(hdr, UIUtils.divider());

                Map<String,String> nameCache = new java.util.HashMap<>();
                for (Student s : UserDAO.loadAllStudents()) nameCache.put(s.getID(), s.getName());

                for (int rank = 0; rank < top.size(); rank++) {
                    ExamResult r = top.get(rank);
                    String gc = r.pct()>=65?UIUtils.ACCENT_GREEN:r.pct()>=50?UIUtils.ACCENT_TEAL:UIUtils.ACCENT_RED;
                    HBox rowH = new HBox(12); rowH.setAlignment(Pos.CENTER_LEFT);
                    rowH.setPadding(new Insets(6, 10, 6, 10));
                    if (rank%2==0) rowH.setStyle("-fx-background-color:" + UIUtils.bgMuted() + ";-fx-background-radius:5;");

                    String rankStr = rank==0?"1st":rank==1?"2nd":rank==2?"3rd":"#"+(rank+1);
                    Label rankLbl = new Label(rankStr);
                    rankLbl.setStyle("-fx-font-size:11.5px;-fx-font-weight:700;-fx-text-fill:" + (rank==0?UIUtils.ACCENT_ORG:rank==1?UIUtils.textMid():UIUtils.textSubtle()) + ";");
                    rankLbl.setMinWidth(36);

                    String name = nameCache.getOrDefault(r.studentId, r.studentId);
                    Label nameLbl = new Label(name);
                    nameLbl.setStyle("-fx-font-size:13px;-fx-font-weight:700;-fx-text-fill:" + UIUtils.textDark() + ";"); nameLbl.setMinWidth(158);
                    Label idLbl = new Label("(" + r.studentId + ")");
                    idLbl.setStyle("-fx-font-size:11px;-fx-text-fill:" + UIUtils.textSubtle() + ";");
                    Region sp2 = new Region(); HBox.setHgrow(sp2, Priority.ALWAYS);

                    javafx.scene.control.ProgressBar pb = new javafx.scene.control.ProgressBar(r.pct()/100);
                    pb.setPrefWidth(120); pb.setPrefHeight(5);
                    pb.setStyle("-fx-accent:"+gc+";-fx-background-color:"+UIUtils.border()+";-fx-background-radius:99;");
                    Label scoreLbl = new Label(String.format("%.0f / %.0f  (%.1f%%)", r.score, r.totalMarks, r.pct()));
                    scoreLbl.setStyle("-fx-font-size:12px;-fx-font-weight:600;-fx-text-fill:"+gc+";"); scoreLbl.setMinWidth(150);
                    Label dateLbl = new Label(r.dateStr());
                    dateLbl.setStyle("-fx-font-size:10px;-fx-text-fill:"+UIUtils.textSubtle()+";");

                    rowH.getChildren().addAll(rankLbl, nameLbl, idLbl, sp2, pb, scoreLbl, dateLbl);
                    card.getChildren().add(rowH);
                }
                page.getChildren().add(card);
            }
        }
        wrapInScroll(contentArea, scroll, page);
        UIUtils.slideIn(page, true);
    }

    // ══════════════════════════════════════════════════════════════
    //  ANALYTICS
    // ══════════════════════════════════════════════════════════════
    private static void renderAnalytics(Pane contentArea, Stage stage, Teacher teacher, HelloApplication app) {
        contentArea.getChildren().clear();
        ScrollPane scroll = new ScrollPane(); scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background:transparent;-fx-background-color:transparent;");

        VBox page = new VBox(20); page.setPadding(new Insets(28, 32, 28, 32));
        page.setStyle("-fx-background-color:" + UIUtils.bgContent() + ";");

        HBox titleRow = new HBox(14); titleRow.setAlignment(Pos.CENTER_LEFT);
        titleRow.getChildren().addAll(backBtn(contentArea, stage, teacher, app), UIUtils.heading("Analytics"));
        page.getChildren().addAll(titleRow, UIUtils.subheading("Platform-wide performance insights"), UIUtils.divider());

        List<ExamResult> all = ResultDAO.loadAll();
        long studentCnt = UserDAO.loadAllStudents().size();
        long examCnt = ExamBank.allExams.size();

        // KPI
        double avgPct = all.isEmpty() ? 0 : all.stream().mapToDouble(ExamResult::pct).average().orElse(0);
        long passed = all.stream().filter(r -> r.pct() >= 50).count();

        HBox kpiRow = new HBox(12);
        kpiRow.getChildren().addAll(
            UIUtils.statCard(UIUtils.ICO_USERS,     String.valueOf(studentCnt), "Students",        UIUtils.ACCENT_TEAL),
            UIUtils.statCard(UIUtils.ICO_EXAM,      String.valueOf(examCnt),    "Total Exams",     UIUtils.ACCENT_PURP),
            UIUtils.statCard(UIUtils.ICO_ANALYTICS, String.valueOf(all.size()), "Submissions",     UIUtils.ACCENT_GREEN),
            UIUtils.statCard(UIUtils.ICO_CHECK,     String.valueOf(passed),     "Passed",          UIUtils.ACCENT_ORG),
            UIUtils.statCard(UIUtils.ICO_TROPHY,    String.format("%.1f%%", avgPct), "Avg Score", UIUtils.ACCENT_YELL)
        );
        page.getChildren().add(kpiRow);

        if (all.isEmpty()) {
            page.getChildren().add(emptyStateCard(UIUtils.ICO_ANALYTICS, "No Data Yet", "No submissions have been made yet.", null, null, null));
            wrapInScroll(contentArea, scroll, page);
            return;
        }

        // Score distribution chart
        VBox chartCard = UIUtils.card(760); chartCard.setMaxWidth(Double.MAX_VALUE);
        chartCard.setPadding(new Insets(18)); chartCard.setSpacing(10);
        Label chartHdr = new Label("Score Distribution by Exam");
        chartHdr.setStyle("-fx-font-size:13px;-fx-font-weight:700;-fx-text-fill:"+UIUtils.textDark()+";");
        chartCard.getChildren().add(chartHdr);

        Map<Integer, List<ExamResult>> byExam = new java.util.LinkedHashMap<>();
        for (ExamResult r : all) byExam.computeIfAbsent(r.examId, k->new ArrayList<>()).add(r);

        double BAR_MAX = 400;
        String[] colors = {UIUtils.ACCENT_TEAL, UIUtils.ACCENT_GREEN, UIUtils.ACCENT_PURP, UIUtils.ACCENT_ORG, UIUtils.ACCENT_RED};
        int ci = 0;
        for (Map.Entry<Integer, List<ExamResult>> entry : byExam.entrySet()) {
            List<ExamResult> results = entry.getValue();
            double avg = results.stream().mapToDouble(ExamResult::pct).average().orElse(0);
            ExamResult first = results.get(0);
            String lb = (first.examTitle!=null&&!first.examTitle.isBlank()) ? first.examTitle : first.examSubject;
            if (lb.length() > 24) lb = lb.substring(0,22)+"…";
            String c = colors[ci++ % colors.length];

            Label nameLbl = new Label(lb); nameLbl.setMinWidth(170);
            nameLbl.setStyle("-fx-font-size:11.5px;-fx-text-fill:"+UIUtils.textMid()+";");
            double barW = BAR_MAX * (avg/100);
            Region bar = new Region(); bar.setPrefWidth(Math.max(barW, 4)); bar.setPrefHeight(18);
            bar.setStyle("-fx-background-color:"+c+";-fx-background-radius:3;");
            Label valLbl = new Label(String.format("%.1f%%  (%d subs)", avg, results.size()));
            valLbl.setStyle("-fx-font-size:11px;-fx-font-weight:700;-fx-text-fill:"+c+";");
            HBox barRow = new HBox(8, nameLbl, bar, valLbl); barRow.setAlignment(Pos.CENTER_LEFT);
            chartCard.getChildren().add(barRow);
        }

        // Pass/fail by exam
        VBox pfCard = UIUtils.card(760); pfCard.setMaxWidth(Double.MAX_VALUE);
        pfCard.setPadding(new Insets(18)); pfCard.setSpacing(9);
        Label pfHdr = new Label("Pass / Fail Rate");
        pfHdr.setStyle("-fx-font-size:13px;-fx-font-weight:700;-fx-text-fill:"+UIUtils.textDark()+";");
        pfCard.getChildren().add(pfHdr);

        for (Map.Entry<Integer, List<ExamResult>> entry : byExam.entrySet()) {
            List<ExamResult> results = entry.getValue();
            ExamResult first = results.get(0);
            String lb = (first.examTitle!=null&&!first.examTitle.isBlank()) ? first.examTitle : first.examSubject;
            long passCount = results.stream().filter(r->r.pct()>=50).count();
            long failCount = results.size() - passCount;
            double passRate = results.isEmpty() ? 0 : (double)passCount/results.size()*100;

            HBox pfRow = new HBox(12); pfRow.setAlignment(Pos.CENTER_LEFT);
            Label examLbl = new Label(lb); examLbl.setMinWidth(170);
            examLbl.setStyle("-fx-font-size:12px;-fx-font-weight:700;-fx-text-fill:"+UIUtils.textDark()+";");
            Label passLbl = UIUtils.badge(passCount + " passed", UIUtils.ACCENT_GREEN);
            Label failLbl = UIUtils.badge(failCount + " failed", UIUtils.ACCENT_RED);
            javafx.scene.control.ProgressBar pb = new javafx.scene.control.ProgressBar(passRate/100);
            pb.setPrefWidth(160); pb.setPrefHeight(7);
            pb.setStyle("-fx-accent:"+UIUtils.ACCENT_GREEN+";-fx-background-color:"+UIUtils.ACCENT_RED+"40;-fx-background-radius:99;");
            Label rateLbl = new Label(String.format("%.0f%% pass", passRate));
            rateLbl.setStyle("-fx-font-size:11px;-fx-text-fill:"+UIUtils.textSubtle()+";");
            pfRow.getChildren().addAll(examLbl, pb, rateLbl, passLbl, failLbl);
            pfCard.getChildren().add(pfRow);
        }

        page.getChildren().addAll(chartCard, pfCard);
        wrapInScroll(contentArea, scroll, page);
        UIUtils.slideIn(page, true);
    }

    // ── Empty state card ──────────────────────────────────────────
    private static VBox emptyStateCard(String svgIcon, String heading, String sub,
                                        String btnLabel, String btnColor, Runnable btnAction) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(32, 24, 32, 24));
        card.setMaxWidth(520);
        card.setStyle("-fx-background-color:" + UIUtils.bgCard() + ";-fx-background-radius:9;-fx-border-color:" + UIUtils.border() + ";-fx-border-radius:9;-fx-border-width:1;");
        DropShadow ds = new DropShadow(); ds.setColor(Color.color(0,0,0,0.04)); ds.setRadius(8); ds.setOffsetY(2); card.setEffect(ds);

        StackPane ico = new StackPane(UIUtils.icon(svgIcon, UIUtils.textSubtle(), 22));
        ico.setPrefSize(48, 48);
        ico.setStyle("-fx-background-color:" + UIUtils.bgMuted() + ";-fx-background-radius:9;");

        Label hdr = new Label(heading);
        hdr.setStyle("-fx-font-size:14px;-fx-font-weight:700;-fx-text-fill:" + UIUtils.textDark() + ";");
        Label subL = new Label(sub);
        subL.setStyle("-fx-font-size:12px;-fx-text-fill:" + UIUtils.textMid() + ";"); subL.setWrapText(true); subL.setMaxWidth(380);

        card.getChildren().addAll(ico, hdr, subL);
        if (btnLabel != null) {
            Button btn = UIUtils.primaryBtn("", btnLabel, btnColor);
            btn.setOnAction(e -> btnAction.run());
            card.getChildren().add(btn);
        }
        return card;
    }

    // ══════════════════════════════════════════════════════════════
    //  LAUNCH POPUP — Go Live
    // ══════════════════════════════════════════════════════════════
    private static void showLaunchPopup(Exam exam, Pane ca, Stage stage, Teacher teacher, HelloApplication app) {
        Stage st = new Stage(); st.initModality(Modality.APPLICATION_MODAL); st.setTitle("Launch Examination");
        VBox root = new VBox(14); root.setPadding(new Insets(26)); root.setStyle("-fx-background-color:" + UIUtils.bgCard() + ";");

        Label lbl = new Label("Set Live Window Duration");
        lbl.setStyle("-fx-font-size:17px;-fx-font-weight:700;-fx-text-fill:" + UIUtils.textDark() + ";");
        Label hint = new Label("Examination duration: " + exam.getDuration() + " min");
        hint.setStyle("-fx-font-size:12px;-fx-text-fill:" + UIUtils.ACCENT_ORG + ";");

        TextField hF = UIUtils.styledField("0"); hF.setPrefWidth(68);
        TextField mF = UIUtils.styledField("30"); mF.setPrefWidth(68);
        HBox timeRow = new HBox(9); timeRow.setAlignment(Pos.CENTER_LEFT);
        Label hL = new Label("hours"); hL.setStyle("-fx-font-size:12.5px;-fx-text-fill:#6b7585;");
        Label mL = new Label("minutes"); mL.setStyle("-fx-font-size:12.5px;-fx-text-fill:#6b7585;");
        timeRow.getChildren().addAll(hF, hL, mF, mL);

        Button btnGo = UIUtils.primaryBtn("", "Go Live Now", UIUtils.ACCENT_GREEN);
        btnGo.setPrefWidth(200); btnGo.setPrefHeight(42);
        Button btnCancel2 = UIUtils.ghostBtn("", "Cancel", UIUtils.TEXT_MID);
        btnCancel2.setPrefHeight(42);
        HBox btnRow = new HBox(12, btnGo, btnCancel2); btnRow.setAlignment(Pos.CENTER_LEFT);

        btnGo.setOnAction(e -> {
            try {
                int h = hF.getText().trim().isEmpty() ? 0 : Integer.parseInt(hF.getText().trim());
                int m = mF.getText().trim().isEmpty() ? 0 : Integer.parseInt(mF.getText().trim());
                int total = h*60+m;
                if (total < Integer.parseInt(exam.getDuration())) { UIUtils.Toast.error(ca, "Live window must be at least " + exam.getDuration() + " min"); return; }
                exam.setLive(true); exam.generateCode();
                exam.setLiveWindow(h+"h "+m+"m");
                exam.setLiveEndMillis(System.currentTimeMillis() + total*60_000L);
                ExamDAO.save(exam);
                st.close();
                renderDashboardHome(ca, stage, teacher, app);
                UIUtils.Toast.success(ca, exam.getSubject() + " is now live  ·  Code: " + exam.getExamCode());
            } catch (NumberFormatException ex) { UIUtils.Toast.error(ca, "Enter valid whole numbers for hours and minutes"); }
        });
        btnCancel2.setOnAction(e -> st.close());

        Label windowLbl = UIUtils.sectionLabel("LIVE WINDOW");
        root.getChildren().addAll(lbl, hint, windowLbl, timeRow, UIUtils.divider(), btnRow);
        Scene sc = new Scene(root, 360, 260);
        UIUtils.applyStyle(sc); st.setScene(sc); st.show();
    }

    // ══════════════════════════════════════════════════════════════
    //  SCHEDULE POPUP — FIXED with proper buttons
    // ══════════════════════════════════════════════════════════════
    private static void showSchedulePopup(Exam exam, Pane ca, Stage stage, Teacher teacher, HelloApplication app) {
        Stage st = new Stage(); st.initModality(Modality.APPLICATION_MODAL); st.setTitle("Schedule Examination"); st.setResizable(false);
        String displayTitle = (exam.getTitle()!=null&&!exam.getTitle().isEmpty()) ? exam.getTitle() : exam.getSubject();

        VBox root = new VBox(0);
        root.setStyle("-fx-background-color:" + UIUtils.bgCard() + ";");
        root.setPrefWidth(480);

        // Header
        VBox header = new VBox(3); header.setPadding(new Insets(18, 22, 16, 22));
        header.setStyle("-fx-background-color:#111722;-fx-background-radius:0;");
        Label headerTitle = new Label("Schedule Examination");
        headerTitle.setStyle("-fx-font-size:16px;-fx-font-weight:700;-fx-text-fill:#e8eaf2;");
        Label headerSub = new Label(displayTitle + "  ·  " + exam.getDuration() + " min");
        headerSub.setStyle("-fx-font-size:11px;-fx-text-fill:#4a566e;");
        header.getChildren().addAll(headerTitle, headerSub);

        // Body
        VBox body = new VBox(13); body.setPadding(new Insets(18, 22, 18, 22));

        // Warning
        HBox warn = new HBox(7); warn.setAlignment(Pos.CENTER_LEFT);
        warn.setPadding(new Insets(8, 12, 8, 12));
        warn.setStyle("-fx-background-color:#fffbeb;-fx-background-radius:7;-fx-border-color:#f59e0b44;-fx-border-radius:7;-fx-border-width:1;");
        warn.getChildren().addAll(UIUtils.icon(UIUtils.ICO_WARN, UIUtils.ACCENT_ORG, 13),
            new Label("Exam auto-starts and auto-ends at the chosen times.") {{ setStyle("-fx-font-size:11px;-fx-text-fill:#b45309;-fx-font-weight:600;"); }});

        // Start pickers
        DatePicker startDate = buildDatePicker(java.time.LocalDate.now().plusDays(1));
        TextField startHH = buildTimeField("09"), startMM = buildTimeField("00");
        ToggleButton startAP = buildAmPmToggle();

        // End pickers
        DatePicker endDate = buildDatePicker(java.time.LocalDate.now().plusDays(1));
        TextField endHH = buildTimeField("10"), endMM = buildTimeField("00");
        ToggleButton endAP = buildAmPmToggle();

        Label startCaption = new Label("START");
        startCaption.setStyle("-fx-font-size:9.5px;-fx-font-weight:700;-fx-text-fill:#0e7a56;-fx-letter-spacing:1.2px;");
        Label endCaption = new Label("END");
        endCaption.setStyle("-fx-font-size:9.5px;-fx-font-weight:700;-fx-text-fill:#c0392b;-fx-letter-spacing:1.2px;");

        VBox startCard = buildPickerCard("#e8f5f4", "#d5eeed", startCaption, buildTimeInputRow(startDate, startHH, startMM, startAP));
        VBox endCard   = buildPickerCard("#fde8e8", "#f5c6c6", endCaption,   buildTimeInputRow(endDate,   endHH,   endMM,   endAP));

        Label durationPreview = new Label("  ");
        durationPreview.setStyle("-fx-font-size:11px;-fx-font-weight:600;-fx-padding:6 12;-fx-background-radius:20;");
        Runnable updatePreview = () -> {
            try {
                java.time.LocalDateTime s2 = buildLDT(startDate, startHH, startMM, startAP);
                java.time.LocalDateTime e2 = buildLDT(endDate, endHH, endMM, endAP);
                long mins = java.time.Duration.between(s2, e2).toMinutes();
                int examMins = Integer.parseInt(exam.getDuration());
                if (mins<=0) { durationPreview.setText("End must be after Start"); durationPreview.setStyle("-fx-font-size:11px;-fx-font-weight:600;-fx-padding:5 11;-fx-background-color:#fde8e8;-fx-text-fill:#c0392b;-fx-background-radius:5;"); }
                else if (mins<examMins) { durationPreview.setText("Window " + mins + " min  <  " + examMins + " min needed"); durationPreview.setStyle("-fx-font-size:11px;-fx-font-weight:600;-fx-padding:5 11;-fx-background-color:#fde8e8;-fx-text-fill:#c0392b;-fx-background-radius:5;"); }
                else { durationPreview.setText("Window: " + mins + " min  (need " + examMins + " min)"); durationPreview.setStyle("-fx-font-size:11px;-fx-font-weight:600;-fx-padding:5 11;-fx-background-color:#d1f0e8;-fx-text-fill:#0e7a56;-fx-background-radius:5;"); }
            } catch (Exception ex) { durationPreview.setText("  "); }
        };
        javafx.beans.value.ChangeListener<Object> pl = (o,ov,nv)->updatePreview.run();
        startDate.valueProperty().addListener(pl); startHH.textProperty().addListener(pl); startMM.textProperty().addListener(pl); startAP.selectedProperty().addListener(pl);
        endDate.valueProperty().addListener(pl); endHH.textProperty().addListener(pl); endMM.textProperty().addListener(pl); endAP.selectedProperty().addListener(pl);
        updatePreview.run();

        // Pre-fill if already scheduled
        if (exam.getScheduledStartMillis() > 0) {
            java.time.LocalDateTime es = java.time.LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(exam.getScheduledStartMillis()), java.time.ZoneId.systemDefault());
            java.time.LocalDateTime ee = java.time.LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(exam.getScheduledEndMillis()), java.time.ZoneId.systemDefault());
            startDate.setValue(es.toLocalDate()); int sh=es.getHour(); startAP.setSelected(sh>=12); startHH.setText(String.format("%02d",sh%12==0?12:sh%12)); startMM.setText(String.format("%02d",es.getMinute()));
            endDate.setValue(ee.toLocalDate()); int eh=ee.getHour(); endAP.setSelected(eh>=12); endHH.setText(String.format("%02d",eh%12==0?12:eh%12)); endMM.setText(String.format("%02d",ee.getMinute()));
            updatePreview.run();
        }

        body.getChildren().addAll(warn, startCard, endCard, durationPreview);
        root.getChildren().addAll(header, body);

        // ── FIXED BUTTON ROW — teal Schedule + Cancel ─────────────
        HBox btnBar = new HBox(12);
        btnBar.setAlignment(Pos.CENTER);
        btnBar.setPadding(new Insets(14, 22, 18, 22));
        btnBar.setStyle("-fx-background-color:" + UIUtils.bgCard() + ";-fx-border-color:" + UIUtils.border() + ";-fx-border-width:1 0 0 0;");

        Button btnSchedule = UIUtils.primaryBtn("", "Schedule", UIUtils.ACCENT_TEAL);
        btnSchedule.setPrefWidth(160); btnSchedule.setPrefHeight(42);
        Button btnCancel = UIUtils.ghostBtn("", "Cancel", UIUtils.TEXT_MID);
        btnCancel.setPrefWidth(100); btnCancel.setPrefHeight(42);

        btnSchedule.setOnAction(ev -> {
            try {
                java.time.LocalDateTime sLDT = buildLDT(startDate, startHH, startMM, startAP);
                java.time.LocalDateTime eLDT = buildLDT(endDate, endHH, endMM, endAP);
                if (!eLDT.isAfter(sLDT)) { UIUtils.Toast.error(ca, "End time must be after start time"); return; }
                if (java.time.Duration.between(sLDT, eLDT).toMinutes() < Integer.parseInt(exam.getDuration())) { UIUtils.Toast.error(ca, "Window is shorter than the exam duration"); return; }
                if (!sLDT.isAfter(java.time.LocalDateTime.now())) { UIUtils.Toast.error(ca, "Start time must be in the future"); return; }
                java.time.ZoneId zone = java.time.ZoneId.systemDefault();
                exam.setScheduledStartMillis(sLDT.atZone(zone).toInstant().toEpochMilli());
                exam.setScheduledEndMillis(eLDT.atZone(zone).toInstant().toEpochMilli());
                exam.generateCode();
                ExamDAO.save(exam);
                st.close();
                renderDashboardHome(ca, stage, teacher, app);
                UIUtils.Toast.success(ca, displayTitle + " scheduled for " + sLDT.format(java.time.format.DateTimeFormatter.ofPattern("dd MMM, HH:mm")));
            } catch (Exception ex) { UIUtils.Toast.error(ca, "Please enter valid time values"); }
        });
        btnCancel.setOnAction(ev -> st.close());

        btnBar.getChildren().addAll(btnSchedule, btnCancel);
        root.getChildren().add(btnBar);

        Scene sc = new Scene(root, 480, 460);
        UIUtils.applyStyle(sc); st.setScene(sc); st.show();
    }

    private static VBox buildPickerCard(String bgColor, String borderColor, Label caption, HBox inputRow) {
        VBox card = new VBox(7); card.setPadding(new Insets(10, 13, 10, 13));
        card.setStyle("-fx-background-color:" + bgColor + ";-fx-background-radius:8;-fx-border-color:" + borderColor + ";-fx-border-radius:8;-fx-border-width:1;");
        card.getChildren().addAll(caption, inputRow);
        return card;
    }

    // ══════════════════════════════════════════════════════════════
    //  EXAM EDITOR inner class
    // ══════════════════════════════════════════════════════════════
    static class ExamEditor {
        private static String  sSub   = null;
        private static Integer sGrd   = null;
        private static String  sMrk   = "";
        private static String  sDur   = "";
        private static String  sTitle = "";
        private static String  sDesc  = "";
        private static double  selTotal = 0;
        private static Label   markLbl;
        private static VBox    qList;
        private static HashMap<Question, Double> sel = new HashMap<>();
        static Exam editing = null;

        static void show(Pane ca, Stage stage, Teacher teacher, HelloApplication app) {
            ca.getChildren().clear();
            ScrollPane sp = new ScrollPane();
            sp.prefWidthProperty().bind(ca.widthProperty());
            sp.prefHeightProperty().bind(ca.heightProperty());
            sp.setStyle("-fx-background:transparent;-fx-background-color:transparent;");
            sp.setFitToWidth(true);

            VBox page = new VBox(16); page.setPadding(new Insets(26, 30, 28, 30));
            page.setStyle("-fx-background-color:" + UIUtils.bgContent() + ";");

            HBox titleRow = new HBox(14); titleRow.setAlignment(Pos.CENTER_LEFT);
            Button back = new Button();
            HBox backInner = new HBox(6, UIUtils.icon(UIUtils.ICO_BACK, UIUtils.ACCENT_TEAL, 13), new Label("Back") {{ setStyle("-fx-font-size:12.5px;-fx-font-weight:600;-fx-text-fill:" + UIUtils.ACCENT_TEAL + ";"); }});
            backInner.setAlignment(Pos.CENTER_LEFT);
            back.setGraphic(backInner);
            back.setStyle("-fx-background-color:" + UIUtils.ACCENT_TEAL + "14;-fx-background-radius:6;-fx-border-color:" + UIUtils.ACCENT_TEAL + "40;-fx-border-radius:6;-fx-border-width:1;-fx-padding:6 14;-fx-cursor:hand;");
            back.setOnAction(e -> { clearState(); editing = null; ca.getChildren().clear(); renderDashboardHome(ca, stage, teacher, app); UIUtils.slideIn(ca, false); });
            titleRow.getChildren().addAll(back, UIUtils.heading(editing == null ? "Create Examination" : "Edit Examination"));
            Label subHead = UIUtils.subheading("Configure the examination and select questions from the bank");

            Label tlbl = sectionLabel("Examination Title");
            TextField fTitle = UIUtils.styledField("e.g. Mid-Term Physics Assessment");
            fTitle.setText(sTitle); fTitle.setOnKeyReleased(e -> { sTitle = fTitle.getText(); });

            Label dlbl = sectionLabel("Description (optional)");
            TextArea fDesc = new TextArea(sDesc); fDesc.setPromptText("Brief description..."); fDesc.setPrefHeight(60); fDesc.setWrapText(true);
            fDesc.setStyle("-fx-font-size:13px;-fx-background-radius:6;-fx-border-color:" + UIUtils.border() + ";-fx-background-color:" + UIUtils.bgInput() + ";-fx-text-fill:" + UIUtils.textDark() + ";-fx-control-inner-background:" + UIUtils.bgInput() + ";");
            fDesc.setOnKeyReleased(e -> sDesc = fDesc.getText());

            Label cfglbl = sectionLabel("Configuration");
            FlowPane cfg = new FlowPane(12, 9); cfg.setAlignment(Pos.CENTER_LEFT);

            StackPane wrapSub = UIUtils.styledCombo("Subject", "Select…");
            ComboBox<String> cbSub = UIUtils.getCombo(wrapSub);
            cbSub.getItems().addAll("Physics","Chemistry","Math","Biology","English");
            wrapSub.setPrefWidth(176);
            if (sSub!=null) cbSub.setValue(sSub);

            StackPane wrapGrd = UIUtils.styledCombo("Class", "Select…");
            ComboBox<Integer> cbGrd = UIUtils.getCombo(wrapGrd);
            for (int i=6;i<=12;i++) cbGrd.getItems().add(i);
            wrapGrd.setPrefWidth(152);
            if (sGrd!=null) cbGrd.setValue(sGrd);

            TextField fMark = UIUtils.styledField("Total Marks"); fMark.setPrefWidth(126);
            if (!sMrk.isEmpty()) fMark.setText(sMrk);
            TextField fDur  = UIUtils.styledField("Duration (min)"); fDur.setPrefWidth(142);
            if (!sDur.isEmpty()) fDur.setText(sDur);

            fMark.setOnKeyReleased(e -> { sMrk = fMark.getText(); updateMark(); });
            fDur.setOnKeyReleased(e  -> { sDur = fDur.getText(); });
            cfg.getChildren().addAll(wrapSub, wrapGrd, fMark, fDur);

            markLbl = new Label("Selected: 0 / 0 marks");
            markLbl.setStyle("-fx-font-weight:700;-fx-text-fill:" + UIUtils.ACCENT_ORG + ";-fx-font-size:12.5px;");

            Label qHdr = sectionLabel("Available Questions");
            qList = new VBox(9);
            if (sSub!=null&&sGrd!=null) refreshList(sSub, sGrd, stage, teacher, app);

            cbSub.setOnAction(e -> { sSub=cbSub.getValue(); UIUtils.comboClear(wrapSub); sel.clear(); refreshList(sSub,sGrd,stage,teacher,app); });
            cbGrd.setOnAction(e -> { sGrd=cbGrd.getValue(); UIUtils.comboClear(wrapGrd); sel.clear(); refreshList(sSub,sGrd,stage,teacher,app); });

            FlowPane actions = new FlowPane(12, 9);
            Button btnMore   = UIUtils.ghostBtn("", "Add More Questions", UIUtils.ACCENT_TEAL);
            Button btnCreate = UIUtils.primaryBtn("", editing==null?"Create Examination":"Save Changes", UIUtils.ACCENT_GREEN);
            Button btnCancel = UIUtils.ghostBtn("", "Cancel", UIUtils.TEXT_MID);

            btnMore.setOnAction(e -> QuestionEditor.show(ca, stage, teacher, app, cbSub.getValue(), cbGrd.getValue()));
            btnCreate.setOnAction(e -> handleCreate(app, ca, stage, teacher, wrapSub, wrapGrd, fMark, fDur));
            btnCancel.setOnAction(e -> { clearState(); editing=null; ca.getChildren().clear(); renderDashboardHome(ca, stage, teacher, app); });
            actions.getChildren().addAll(btnMore, btnCreate, btnCancel);

            page.getChildren().addAll(titleRow, subHead, UIUtils.divider(), tlbl, fTitle, dlbl, fDesc, cfglbl, cfg, UIUtils.divider(), markLbl, qHdr, qList, UIUtils.divider(), actions);
            sp.setContent(page); ca.getChildren().add(sp);
            if (ca instanceof javafx.scene.layout.AnchorPane ap) {
                javafx.scene.layout.AnchorPane.setTopAnchor(sp,0.0); javafx.scene.layout.AnchorPane.setBottomAnchor(sp,0.0);
                javafx.scene.layout.AnchorPane.setLeftAnchor(sp,0.0); javafx.scene.layout.AnchorPane.setRightAnchor(sp,0.0);
            }
            updateMark(); UIUtils.slideIn(page, true);
        }

        static void loadForEditing(Exam exam, Pane ca, Stage stage, Teacher teacher, HelloApplication app) {
            editing=exam; sSub=exam.getSubject(); sGrd=exam.getGrade();
            sMrk=String.valueOf(exam.getTotalMarks()); sDur=exam.getDuration();
            sTitle=exam.getTitle()!=null?exam.getTitle():""; sDesc=exam.getDescription()!=null?exam.getDescription():"";
            sel.clear();
            for (var entry : exam.getQuestionsMap().entrySet()) {
                Question dbQ=entry.getKey(); double marks=entry.getValue();
                Question live = QuestionBank.allQuestions.stream().filter(bq->bq.getDbId()>0&&bq.getDbId()==dbQ.getDbId()).findFirst().orElse(dbQ);
                sel.put(live, marks);
            }
            show(ca, stage, teacher, app);
        }

        private static Label sectionLabel(String text) {
            Label l = new Label(text.toUpperCase());
            l.setStyle("-fx-font-size:10px;-fx-font-weight:700;-fx-text-fill:" + UIUtils.textSubtle() + ";-fx-letter-spacing:1.2px;");
            return l;
        }

        private static void refreshList(String sub, Integer cls, Stage stage, Teacher teacher, HelloApplication app) {
            if (qList==null) return;
            qList.getChildren().clear();
            if (sub==null||cls==null) return;

            for (Question q : QuestionBank.allQuestions) {
                if (!q.getSubject().equals(sub)||q.getGrade()!=cls) continue;

                VBox card = UIUtils.card(720); card.setPadding(new Insets(13));
                HBox top = new HBox(11); top.setAlignment(Pos.CENTER_LEFT);
                CheckBox cb = new CheckBox();
                Label qText = new Label(q.getQuestionText());
                qText.setStyle("-fx-font-size:13.5px;-fx-font-weight:600;-fx-text-fill:" + UIUtils.textDark() + ";");
                qText.setWrapText(true); qText.setMaxWidth(380);

                TextField fMk = UIUtils.styledField("Marks"); fMk.setPrefWidth(78);
                if (sel.containsKey(q)) { cb.setSelected(true); fMk.setText(String.valueOf(sel.get(q))); }
                else if (q==QuestionEditor.lastAdded) { cb.setSelected(true); sel.put(q,0.0); QuestionEditor.lastAdded=null; }
                else fMk.setDisable(true);

                String type   = (q instanceof MCQ)?"MCQ":(q instanceof TextQuestion)?"Text":"Range";
                String tColor = (q instanceof MCQ)?UIUtils.ACCENT_TEAL:(q instanceof TextQuestion)?UIUtils.ACCENT_GREEN:UIUtils.ACCENT_PURP;
                Label badge = UIUtils.badge(type, tColor);

                MenuButton qMenu = new MenuButton("···");
                qMenu.setStyle("-fx-background-color:transparent;-fx-text-fill:" + UIUtils.textMid() + ";-fx-font-size:13px;-fx-font-weight:700;-fx-background-radius:6;-fx-border-color:" + UIUtils.border() + ";-fx-border-radius:6;-fx-border-width:1;-fx-padding:3 8;-fx-cursor:hand;");
                qMenu.setMinWidth(MenuButton.USE_PREF_SIZE);
                MenuItem miEdit = UIUtils.modernMenuItem(UIUtils.ICO_EDIT,   "Edit",   "#0f7d74", false);
                miEdit.setOnAction(e -> showEditPopup(q, app, stage, teacher));
                MenuItem miDel  = UIUtils.modernMenuItem(UIUtils.ICO_DELETE, "Remove", UIUtils.ACCENT_RED, true);
                miDel.setOnAction(e -> showDeleteConfirm(q, app, sub, cls, stage, teacher));
                qMenu.getItems().addAll(miEdit, new SeparatorMenuItem(), miDel);
                top.getChildren().addAll(cb, badge, qText, fMk, qMenu);

                VBox ansBox = new VBox(3); ansBox.setPadding(new Insets(5,0,0,24));
                renderAnswerPreview(q, ansBox);
                card.getChildren().addAll(top, ansBox);
                qList.getChildren().add(card);

                cb.setOnAction(e -> { fMk.setDisable(!cb.isSelected()); if(!cb.isSelected()) sel.remove(q); else sel.putIfAbsent(q,0.0); updateMark(); });
                fMk.setOnKeyReleased(e -> { try { sel.put(q, fMk.getText().isEmpty()?0:Double.parseDouble(fMk.getText())); } catch(Exception ex) { sel.put(q,0.0); } updateMark(); });
            }
            updateMark();
        }

        private static void renderAnswerPreview(Question q, VBox c) {
            c.getChildren().clear();
            if (q instanceof MCQ m) {
                for (int i=0;i<m.getOptions().length;i++) {
                    Label l = new Label((i+1)+".  "+m.getOptions()[i]);
                    l.setStyle(i==m.getCorrectIndex()
                        ? "-fx-text-fill:#0e7a56;-fx-font-weight:700;-fx-font-size:11.5px;"
                        : "-fx-text-fill:" + UIUtils.textSubtle() + ";-fx-font-size:11.5px;");
                    c.getChildren().add(l);
                }
            } else if (q instanceof TextQuestion tq) {
                Label l = new Label("Correct answer: " + tq.getAnswer());
                l.setStyle("-fx-text-fill:#0e7a56;-fx-font-size:11.5px;-fx-font-weight:600;"); c.getChildren().add(l);
            } else if (q instanceof RangeQuestion rq) {
                Label l = new Label("Accepted range: " + rq.getMin() + " – " + rq.getMax());
                l.setStyle("-fx-text-fill:#0e7a56;-fx-font-size:11.5px;-fx-font-weight:600;"); c.getChildren().add(l);
            }
        }

        static void showEditPopupPublic(Question q, HelloApplication app, Stage stage, Teacher teacher) { showEditPopupImpl(q, app, () -> refreshList(sSub, sGrd, stage, teacher, app)); }
        private static void showEditPopup(Question q, HelloApplication app, Stage stage, Teacher teacher) { showEditPopupImpl(q, app, () -> refreshList(sSub, sGrd, stage, teacher, app)); }

        private static void showEditPopupImpl(Question q, HelloApplication app, Runnable onSave) {
            Stage st = new Stage(); st.initModality(Modality.APPLICATION_MODAL); st.setTitle("Edit Question");
            VBox layout = new VBox(13); layout.setPadding(new Insets(22)); layout.setStyle("-fx-background-color:" + UIUtils.bgCard() + ";");
            Label lbl = new Label("Edit Question");
            lbl.setStyle("-fx-font-size:17px;-fx-font-weight:700;-fx-text-fill:" + UIUtils.textDark() + ";");

            TextArea txt = new TextArea(q.getQuestionText()); txt.setWrapText(true); txt.setPrefHeight(78);
            txt.setStyle("-fx-font-size:13.5px;-fx-background-radius:6;-fx-border-color:" + UIUtils.border() + ";-fx-background-color:" + UIUtils.bgInput() + ";-fx-text-fill:" + UIUtils.textDark() + ";-fx-control-inner-background:" + UIUtils.bgInput() + ";");

            TextField[] mcqF = new TextField[4]; ToggleGroup tg = new ToggleGroup(); RadioButton[] rbs = new RadioButton[4];
            TextField tAns=UIUtils.styledField("Answer"); tAns.setPrefWidth(200);
            TextField tMin=UIUtils.styledField("Min"); tMin.setPrefWidth(96);
            TextField tMax=UIUtils.styledField("Max"); tMax.setPrefWidth(96);

            String capStyle = "-fx-font-size:10px;-fx-font-weight:700;-fx-text-fill:#9aa1b0;-fx-letter-spacing:1.2px;";
            layout.getChildren().addAll(lbl, new Label("QUESTION TEXT") {{ setStyle(capStyle); }}, txt);

            if (q instanceof MCQ m) {
                layout.getChildren().add(new Label("OPTIONS") {{ setStyle(capStyle); }});
                for (int i=0;i<4;i++) {
                    HBox r = new HBox(9); r.setAlignment(Pos.CENTER_LEFT);
                    rbs[i]=new RadioButton(); rbs[i].setToggleGroup(tg);
                    mcqF[i]=UIUtils.styledField("Option "+(i+1)); mcqF[i].setPrefWidth(240);
                    mcqF[i].setText(m.getOptions()[i]);
                    if (i==m.getCorrectIndex()) rbs[i].setSelected(true);
                    r.getChildren().addAll(rbs[i], mcqF[i]); layout.getChildren().add(r);
                }
            } else if (q instanceof TextQuestion tq) {
                tAns.setText(String.valueOf(tq.getAnswer()));
                layout.getChildren().addAll(new Label("ANSWER") {{ setStyle(capStyle); }}, tAns);
            } else if (q instanceof RangeQuestion rq) {
                tMin.setText(String.valueOf(rq.getMin())); tMax.setText(String.valueOf(rq.getMax()));
                HBox rng = new HBox(10, tMin, new Label("to") {{ setStyle("-fx-font-size:12px;-fx-text-fill:#6b7585;"); }}, tMax);
                layout.getChildren().addAll(new Label("ACCEPTED RANGE") {{ setStyle(capStyle); }}, rng);
            }

            Button btnSave = UIUtils.primaryBtn("", "Save Changes", UIUtils.ACCENT_GREEN);
            btnSave.setOnAction(e -> {
                try {
                    q.setQuestionText(txt.getText().trim());
                    if (q instanceof MCQ m) { String[] opts=new String[4]; for(int i=0;i<4;i++) opts[i]=mcqF[i].getText(); m.setOptions(opts); m.setCorrectIndex(tg.getToggles().indexOf(tg.getSelectedToggle())); }
                    else if (q instanceof TextQuestion tq) { tq.setAnswer(Double.parseDouble(tAns.getText())); }
                    else if (q instanceof RangeQuestion rq) {
                        double mn=Double.parseDouble(tMin.getText()), mx=Double.parseDouble(tMax.getText());
                        if (mn>=mx) { app.showError("Invalid Range","Min must be less than Max."); return; }
                        rq.setMin(mn); rq.setMax(mx);
                    }
                    QuestionDAO.update(q); st.close(); if (onSave!=null) onSave.run();
                } catch(Exception ex) { app.showError("Error","Invalid values."); }
            });
            layout.getChildren().add(btnSave);
            ScrollPane spEdit = new ScrollPane(layout); spEdit.setFitToWidth(true);
            spEdit.setStyle("-fx-background:transparent;-fx-background-color:transparent;");
            Scene sc = new Scene(spEdit, 420, 500);
            UIUtils.applyStyle(sc); st.setScene(sc); st.showAndWait();
        }

        private static void showDeleteConfirm(Question q, HelloApplication app, String sub, Integer cls, Stage stage, Teacher teacher) {
            Stage st = new Stage(); st.initModality(Modality.APPLICATION_MODAL); st.setTitle("Remove Question");
            VBox layout = new VBox(14); layout.setPadding(new Insets(26)); layout.setAlignment(Pos.CENTER);
            layout.setStyle("-fx-background-color:" + UIUtils.bgCard() + ";");
            Label warn = new Label("Remove this question?");
            warn.setStyle("-fx-font-size:15px;-fx-font-weight:700;-fx-text-fill:" + UIUtils.ACCENT_RED + ";");
            Label prev = new Label("\"" + q.getQuestionText() + "\"");
            prev.setWrapText(true); prev.setMaxWidth(300);
            prev.setStyle("-fx-font-style:italic;-fx-text-fill:" + UIUtils.textMid() + ";-fx-font-size:12.5px;");
            HBox btns = new HBox(12); btns.setAlignment(Pos.CENTER);
            Button yes = UIUtils.primaryBtn("","Remove",UIUtils.ACCENT_RED);
            Button no  = UIUtils.ghostBtn("","Cancel",UIUtils.TEXT_MID);
            yes.setOnAction(e -> { sel.remove(q); updateMark(); st.close(); refreshList(sub,cls,stage,teacher,app); });
            no.setOnAction(e  -> st.close());
            btns.getChildren().addAll(yes, no);
            layout.getChildren().addAll(warn, prev, btns);
            Scene scDel = new Scene(layout, 400, 200);
            UIUtils.applyStyle(scDel); st.setScene(scDel); st.showAndWait();
        }

        private static void handleCreate(HelloApplication app, Pane ca, Stage stage, Teacher teacher,
                                          StackPane wrapSub, StackPane wrapGrd, TextField fMark, TextField fDur) {
            boolean valid = true;
            if (sSub==null)     { UIUtils.comboError(wrapSub); valid=false; }
            if (sGrd==null)     { UIUtils.comboError(wrapGrd); valid=false; }
            if (sMrk.isEmpty()) { valid=false; }
            if (sDur.isEmpty()) { valid=false; }
            if (!valid) { UIUtils.Toast.error(ca, "Please fill in all required fields"); return; }
            if (sel.isEmpty()) { UIUtils.Toast.error(ca, "Select at least one question before saving"); return; }
            try {
                double target = Double.parseDouble(sMrk);
                if (Math.abs(selTotal-target)>0.01) { UIUtils.Toast.error(ca, "Assigned marks (" + selTotal + ") do not match total marks (" + target + ")"); return; }

                Exam updated = new Exam(sSub, sGrd, target, sDur, new HashMap<>(sel));
                updated.setTitle(sTitle); updated.setDescription(sDesc);

                if (editing!=null) {
                    // Preserve live/schedule state when editing
                    updated.setLive(editing.isLive());
                    updated.setScheduleDetails(editing.getScheduleDetails());
                    updated.setLiveWindow(editing.getLiveWindow());
                    updated.setLiveEndMillis(editing.getLiveEndMillis());
                    updated.setExamCode(editing.getExamCode());
                    updated.setScheduledStartMillis(editing.getScheduledStartMillis());
                    updated.setScheduledEndMillis(editing.getScheduledEndMillis());
                    updated.setDbId(editing.getDbId());
                    ExamDAO.save(updated);
                    int idx = ExamBank.allExams.indexOf(editing);
                    if (idx!=-1) ExamBank.allExams.set(idx, updated); else ExamBank.allExams.add(updated);
                    editing=null; clearState();
                    ca.getChildren().clear();
                    renderDashboardHome(ca, stage, teacher, app);
                    UIUtils.Toast.success(ca, "Examination updated successfully");
                } else {
                    ExamBank.allExams.add(0, updated); ExamDAO.save(updated);
                    clearState();
                    ca.getChildren().clear();
                    renderDashboardHome(ca, stage, teacher, app);
                    UIUtils.Toast.success(ca, "Examination created — ready in All Examinations");
                }
            } catch (Exception ex) { UIUtils.Toast.error(ca, "Total marks must be a valid number"); }
        }

        private static void updateMark() {
            selTotal = sel.values().stream().mapToDouble(Double::doubleValue).sum();
            if (markLbl!=null) markLbl.setText("Selected: " + selTotal + " / " + (sMrk.isEmpty()?"?":sMrk) + " marks");
        }

        static void clearState() {
            sSub=null; sGrd=null; sMrk=""; sDur=""; sTitle=""; sDesc=""; sel.clear(); selTotal=0;
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  QUESTION EDITOR inner class
    // ══════════════════════════════════════════════════════════════
    static class QuestionEditor {
        static Question lastAdded = null;
        private static TextArea    qArea;
        private static TextField[] mcqOpts = new TextField[4];
        private static ToggleGroup mcqTG;
        private static TextField   exactF, minF, maxF;
        private static ComboBox<String> cbAnsType;
        private static boolean returnToExam = false;

        static void show(Pane ca, Stage stage, Teacher teacher, HelloApplication app, String initSub, Integer initGrd) {
            ca.getChildren().clear();
            returnToExam = (initSub!=null);

            ScrollPane sp = new ScrollPane();
            sp.prefWidthProperty().bind(ca.widthProperty());
            sp.prefHeightProperty().bind(ca.heightProperty());
            sp.setStyle("-fx-background:transparent;-fx-background-color:transparent;");
            sp.setFitToWidth(true);

            VBox page = new VBox(16); page.setPadding(new Insets(26, 30, 28, 30));
            page.setStyle("-fx-background-color:" + UIUtils.bgContent() + ";");

            HBox titleRow2 = new HBox(14); titleRow2.setAlignment(Pos.CENTER_LEFT);
            Button back2 = new Button();
            HBox bi2 = new HBox(6, UIUtils.icon(UIUtils.ICO_BACK, UIUtils.ACCENT_TEAL, 13), new Label("Back") {{ setStyle("-fx-font-size:12.5px;-fx-font-weight:600;-fx-text-fill:" + UIUtils.ACCENT_TEAL + ";"); }});
            bi2.setAlignment(Pos.CENTER_LEFT);
            back2.setGraphic(bi2);
            back2.setStyle("-fx-background-color:" + UIUtils.ACCENT_TEAL + "14;-fx-background-radius:6;-fx-border-color:" + UIUtils.ACCENT_TEAL + "40;-fx-border-radius:6;-fx-border-width:1;-fx-padding:6 14;-fx-cursor:hand;");
            back2.setOnAction(e -> { ca.getChildren().clear(); renderDashboardHome(ca, stage, teacher, app); UIUtils.slideIn(ca, false); });
            titleRow2.getChildren().addAll(back2, UIUtils.heading("Add Question"));

            Label subHead = UIUtils.subheading("Create MCQ, text, or range questions for the question bank");

            HBox cfg = new HBox(12); cfg.setAlignment(Pos.CENTER_LEFT);
            StackPane wrapSub = UIUtils.styledCombo("Subject", "Select…"); ComboBox<String> cbSub = UIUtils.getCombo(wrapSub);
            cbSub.getItems().addAll("Physics","Chemistry","Math","Biology","English"); wrapSub.setPrefWidth(176);
            StackPane wrapGrd = UIUtils.styledCombo("Class", "Select…"); ComboBox<Integer> cbGrd = UIUtils.getCombo(wrapGrd);
            for (int i=6;i<=12;i++) cbGrd.getItems().add(i); wrapGrd.setPrefWidth(152);
            if (initSub!=null) { cbSub.setValue(initSub); cbGrd.setValue(initGrd); }

            ToggleGroup tg = new ToggleGroup();
            RadioButton rbMcq  = new RadioButton("MCQ");           rbMcq.setToggleGroup(tg);  rbMcq.setSelected(true);
            RadioButton rbText = new RadioButton("Text / Numeric"); rbText.setToggleGroup(tg);
            String pillSel  = "-fx-background-color:%s;-fx-text-fill:white;-fx-font-size:12.5px;-fx-font-weight:700;-fx-background-radius:6;-fx-padding:7 16;-fx-cursor:hand;";
            String pillRest = "-fx-background-color:%s18;-fx-text-fill:%s;-fx-font-size:12.5px;-fx-font-weight:600;-fx-background-radius:6;-fx-padding:7 16;-fx-cursor:hand;";
            rbMcq.setStyle(String.format(pillSel, UIUtils.ACCENT_TEAL));
            rbText.setStyle(String.format(pillRest, UIUtils.ACCENT_PURP, UIUtils.ACCENT_PURP));
            tg.selectedToggleProperty().addListener((obs,ov,nv) -> {
                rbMcq.setStyle(nv==rbMcq ? String.format(pillSel, UIUtils.ACCENT_TEAL) : String.format(pillRest, UIUtils.ACCENT_TEAL, UIUtils.ACCENT_TEAL));
                rbText.setStyle(nv==rbText ? String.format(pillSel, UIUtils.ACCENT_PURP) : String.format(pillRest, UIUtils.ACCENT_PURP, UIUtils.ACCENT_PURP));
            });

            HBox typeRow = new HBox(10, new Label("Type:") {{ setStyle("-fx-font-size:13px;-fx-font-weight:700;-fx-text-fill:" + UIUtils.textMid() + ";"); }}, rbMcq, rbText);
            typeRow.setAlignment(Pos.CENTER_LEFT);
            cfg.getChildren().addAll(wrapSub, wrapGrd);

            VBox dynForm = new VBox();
            renderMcqForm(dynForm);
            rbMcq.setOnAction(e  -> renderMcqForm(dynForm));
            rbText.setOnAction(e -> renderTextForm(dynForm));

            Button btnSave = UIUtils.primaryBtn("", "Save to Question Bank", UIUtils.ACCENT_GREEN);
            btnSave.setPrefHeight(42);
            btnSave.setOnAction(e -> {
                boolean valid=true;
                if (cbSub.getValue()==null) { UIUtils.comboError(wrapSub); valid=false; }
                if (cbGrd.getValue()==null) { UIUtils.comboError(wrapGrd); valid=false; }
                if (qArea.getText().trim().isEmpty()) { valid=false; }
                if (!valid) { UIUtils.Toast.error(ca, "Please fill in all required fields"); return; }
                if (rbMcq.isSelected()&&mcqTG.getSelectedToggle()==null) { UIUtils.Toast.error(ca, "Select the correct MCQ answer before saving"); return; }
                try {
                    String s=cbSub.getValue(); int g=cbGrd.getValue(); String txt=qArea.getText().trim();
                    Question nq;
                    if (rbMcq.isSelected()) { String[] opts=new String[4]; for(int i=0;i<4;i++) opts[i]=mcqOpts[i].getText(); nq=new MCQ(s,g,txt,opts,mcqTG.getToggles().indexOf(mcqTG.getSelectedToggle())); }
                    else if ("Exact Answer".equals(cbAnsType.getValue())) nq=new TextQuestion(s,g,txt,Double.parseDouble(exactF.getText()));
                    else { double mn=Double.parseDouble(minF.getText()), mx=Double.parseDouble(maxF.getText()); if(mn>=mx){UIUtils.Toast.error(ca,"Min must be less than Max");return;} nq=new RangeQuestion(s,g,txt,mn,mx); }
                    QuestionBank.allQuestions.addFirst(nq); QuestionDAO.save(nq); lastAdded=nq;
                    UIUtils.Toast.success(ca, "Question saved to the bank");
                    if (returnToExam) ExamEditor.show(ca, stage, teacher, app); else qArea.clear();
                } catch(Exception ex) { UIUtils.Toast.error(ca, "Enter valid numeric values for answer/range"); }
            });

            page.getChildren().addAll(titleRow2, subHead, UIUtils.divider(), cfg, typeRow, dynForm, btnSave);
            sp.setContent(page);
            ca.getChildren().add(sp);
            if (ca instanceof javafx.scene.layout.AnchorPane ap) {
                javafx.scene.layout.AnchorPane.setTopAnchor(sp,0.0); javafx.scene.layout.AnchorPane.setBottomAnchor(sp,0.0);
                javafx.scene.layout.AnchorPane.setLeftAnchor(sp,0.0); javafx.scene.layout.AnchorPane.setRightAnchor(sp,0.0);
            }
            UIUtils.slideIn(page, true);
        }

        private static void renderMcqForm(Pane c) {
            c.getChildren().clear();
            VBox form = new VBox(11); form.prefWidthProperty().bind(c.widthProperty());
            qArea = new TextArea(); qArea.setPromptText("Enter your MCQ question here..."); qArea.setPrefHeight(78); qArea.setWrapText(true);
            qArea.setStyle("-fx-font-size:13.5px;-fx-background-radius:6;-fx-border-color:" + UIUtils.border() + ";-fx-background-color:" + UIUtils.bgInput() + ";-fx-text-fill:" + UIUtils.textDark() + ";-fx-control-inner-background:" + UIUtils.bgInput() + ";");
            Label optHdr = new Label("OPTIONS — SELECT THE CORRECT ANSWER");
            optHdr.setStyle("-fx-font-size:10px;-fx-font-weight:700;-fx-text-fill:#9aa1b0;-fx-letter-spacing:1.2px;");
            mcqTG = new ToggleGroup();
            GridPane grid = new GridPane(); grid.setHgap(11); grid.setVgap(9);
            for (int i=0;i<4;i++) {
                mcqOpts[i] = UIUtils.styledField("Option "+(i+1)); mcqOpts[i].setPrefWidth(260);
                RadioButton rb = new RadioButton(); rb.setToggleGroup(mcqTG);
                HBox cell = new HBox(7, rb, mcqOpts[i]); cell.setAlignment(Pos.CENTER_LEFT);
                grid.add(cell, i%2, i/2);
            }
            form.getChildren().addAll(qArea, optHdr, grid);
            c.getChildren().add(form);
        }

        private static void renderTextForm(Pane c) {
            c.getChildren().clear();
            VBox form = new VBox(11); form.prefWidthProperty().bind(c.widthProperty());
            qArea = new TextArea(); qArea.setPromptText("Enter your text/numeric question here..."); qArea.setPrefHeight(78); qArea.setWrapText(true);
            qArea.setStyle("-fx-font-size:13.5px;-fx-background-radius:6;-fx-border-color:" + UIUtils.border() + ";-fx-background-color:" + UIUtils.bgInput() + ";-fx-text-fill:" + UIUtils.textDark() + ";-fx-control-inner-background:" + UIUtils.bgInput() + ";");
            Label ansHdr = new Label("ANSWER TYPE");
            ansHdr.setStyle("-fx-font-size:10px;-fx-font-weight:700;-fx-text-fill:#9aa1b0;-fx-letter-spacing:1.2px;");
            StackPane wrapAns = UIUtils.styledCombo("Answer Type", "Select…");
            cbAnsType = UIUtils.getCombo(wrapAns); cbAnsType.getItems().addAll("Exact Answer","Allow Range"); cbAnsType.setValue("Exact Answer"); wrapAns.setPrefWidth(230);
            exactF=UIUtils.styledField("Exact numeric answer"); exactF.setPrefWidth(210);
            minF=UIUtils.styledField("Min"); minF.setPrefWidth(150);
            maxF=UIUtils.styledField("Max"); maxF.setPrefWidth(150);
            HBox rangeRow = new HBox(11, minF, new Label("to") {{ setStyle("-fx-font-size:12px;-fx-text-fill:#6b7585;"); }}, maxF); rangeRow.setAlignment(Pos.CENTER_LEFT);
            VBox ansSlot = new VBox(exactF);
            cbAnsType.setOnAction(e -> { ansSlot.getChildren().clear(); ansSlot.getChildren().add("Exact Answer".equals(cbAnsType.getValue())?exactF:rangeRow); });
            form.getChildren().addAll(qArea, ansHdr, wrapAns, ansSlot);
            c.getChildren().add(form);
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  QUESTION BANK BROWSER inner class
    // ══════════════════════════════════════════════════════════════
    static class QuestionBankBrowser {
        static void render(Pane ca, Stage stage, Teacher teacher, HelloApplication app) {
            ca.getChildren().clear();
            ScrollPane sp = new ScrollPane(); sp.prefWidthProperty().bind(ca.widthProperty()); sp.prefHeightProperty().bind(ca.heightProperty());
            sp.setStyle("-fx-background:transparent;-fx-background-color:transparent;"); sp.setFitToWidth(true);

            VBox page = new VBox(16); page.setPadding(new Insets(26, 30, 28, 30));
            page.setStyle("-fx-background-color:" + UIUtils.bgContent() + ";");

            HBox titleRow = new HBox(14); titleRow.setAlignment(Pos.CENTER_LEFT);
            Button back = new Button();
            HBox bi = new HBox(6, UIUtils.icon(UIUtils.ICO_BACK, UIUtils.ACCENT_TEAL, 13), new Label("Back") {{ setStyle("-fx-font-size:12.5px;-fx-font-weight:600;-fx-text-fill:" + UIUtils.ACCENT_TEAL + ";"); }});
            bi.setAlignment(Pos.CENTER_LEFT);
            back.setGraphic(bi);
            back.setStyle("-fx-background-color:" + UIUtils.ACCENT_TEAL + "14;-fx-background-radius:6;-fx-border-color:" + UIUtils.ACCENT_TEAL + "40;-fx-border-radius:6;-fx-border-width:1;-fx-padding:6 14;-fx-cursor:hand;");
            back.setOnAction(e -> { ca.getChildren().clear(); renderDashboardHome(ca, stage, teacher, app); UIUtils.slideIn(ca, false); });
            titleRow.getChildren().addAll(back, UIUtils.heading("Question Bank"));

            Label subHead = UIUtils.subheading("Browse, search and manage all saved questions");

            HBox filterRow = new HBox(11); filterRow.setAlignment(Pos.CENTER_LEFT);
            filterRow.setPadding(new Insets(13));
            filterRow.setStyle("-fx-background-color:" + UIUtils.bgCard() + ";-fx-background-radius:9;-fx-border-color:" + UIUtils.border() + ";-fx-border-radius:9;-fx-border-width:1;");

            TextField searchField = UIUtils.styledField("Search questions...");
            searchField.setPrefWidth(240);
            Region ico = UIUtils.icon(UIUtils.ICO_SEARCH, UIUtils.textSubtle(), 14);

            StackPane wrapSubF = UIUtils.styledCombo("Subject","All Subjects"); ComboBox<String> cbSubF=UIUtils.getCombo(wrapSubF);
            cbSubF.getItems().addAll("All Subjects","Physics","Chemistry","Math","Biology","English"); cbSubF.setValue("All Subjects"); wrapSubF.setPrefWidth(164);
            StackPane wrapGrdF = UIUtils.styledCombo("Grade","All Grades"); ComboBox<String> cbGrdF=UIUtils.getCombo(wrapGrdF);
            cbGrdF.getItems().add("All Grades"); for(int i=6;i<=12;i++) cbGrdF.getItems().add("Grade "+i); cbGrdF.setValue("All Grades"); wrapGrdF.setPrefWidth(148);
            StackPane wrapTypF = UIUtils.styledCombo("Type","All Types"); ComboBox<String> cbTypF=UIUtils.getCombo(wrapTypF);
            cbTypF.getItems().addAll("All Types","MCQ","Text","Range"); cbTypF.setValue("All Types"); wrapTypF.setPrefWidth(136);

            Label totalLbl = new Label(); totalLbl.setStyle("-fx-font-size:12px;-fx-text-fill:" + UIUtils.textMid() + ";");
            Region fsp = new Region(); HBox.setHgrow(fsp, Priority.ALWAYS);
            filterRow.getChildren().addAll(ico, searchField, wrapSubF, wrapGrdF, wrapTypF, fsp, totalLbl);

            VBox results = new VBox(9);
            Runnable refresh = () -> rebuildResults(results, ca, stage, teacher, app, searchField.getText(), cbSubF.getValue(), cbGrdF.getValue(), cbTypF.getValue(), totalLbl);
            searchField.setOnKeyReleased(e -> refresh.run());
            cbSubF.setOnAction(e->refresh.run()); cbGrdF.setOnAction(e->refresh.run()); cbTypF.setOnAction(e->refresh.run());
            refresh.run();

            page.getChildren().addAll(titleRow, subHead, UIUtils.divider(), filterRow, results);
            sp.setContent(page); ca.getChildren().add(sp);
            if (ca instanceof javafx.scene.layout.AnchorPane ap) {
                javafx.scene.layout.AnchorPane.setTopAnchor(sp,0.0); javafx.scene.layout.AnchorPane.setBottomAnchor(sp,0.0);
                javafx.scene.layout.AnchorPane.setLeftAnchor(sp,0.0); javafx.scene.layout.AnchorPane.setRightAnchor(sp,0.0);
            }
            UIUtils.slideIn(page, true);
        }

        private static void rebuildResults(VBox results, Pane ca, Stage stage, Teacher teacher, HelloApplication app,
                                            String search, String subF, String grdF, String typF, Label totalLbl) {
            results.getChildren().clear();
            String sl = search==null?"":search.toLowerCase().trim();
            List<Question> filtered = QuestionBank.allQuestions.stream().filter(q -> {
                if (!"All Subjects".equals(subF)&&!q.getSubject().equals(subF)) return false;
                if (!"All Grades".equals(grdF)) { int g=Integer.parseInt(grdF.replace("Grade ","")); if(q.getGrade()!=g) return false; }
                if (!"All Types".equals(typF)) { String qt=(q instanceof MCQ)?"MCQ":(q instanceof TextQuestion)?"Text":"Range"; if(!qt.equals(typF)) return false; }
                if (!sl.isEmpty()&&!q.getQuestionText().toLowerCase().contains(sl)) return false;
                return true;
            }).collect(Collectors.toList());

            totalLbl.setText(filtered.size() + " question" + (filtered.size()==1?"":"s"));
            if (filtered.isEmpty()) {
                VBox emp = new VBox(9); emp.setAlignment(Pos.CENTER); emp.setPadding(new Insets(36));
                emp.getChildren().addAll(new Label("No questions match your filters") {{ setStyle("-fx-font-size:14px;-fx-font-weight:700;-fx-text-fill:" + UIUtils.textMid() + ";"); }});
                results.getChildren().add(emp); return;
            }

            for (Question q : filtered) {
                String type   = (q instanceof MCQ)?"MCQ":(q instanceof TextQuestion)?"Text":"Range";
                String tColor = (q instanceof MCQ)?UIUtils.ACCENT_TEAL:(q instanceof TextQuestion)?UIUtils.ACCENT_GREEN:UIUtils.ACCENT_PURP;

                HBox row = new HBox(11); row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(13, 16, 13, 16));
                row.setStyle("-fx-background-color:" + UIUtils.bgCard() + ";-fx-background-radius:9;-fx-border-color:" + UIUtils.border() + ";-fx-border-radius:9;-fx-border-width:1;");
                DropShadow ds = new DropShadow(); ds.setColor(Color.color(0,0,0,0.04)); ds.setRadius(6); ds.setOffsetY(1); row.setEffect(ds);

                Label typeB = UIUtils.badge(type, tColor);
                Label metaB = UIUtils.badge(q.getSubject()+"  G"+q.getGrade(), UIUtils.ACCENT_BLUE);
                Label qText = new Label(q.getQuestionText()); qText.setStyle("-fx-font-size:13.5px;-fx-font-weight:600;-fx-text-fill:" + UIUtils.textDark() + ";"); qText.setWrapText(true); qText.setMaxWidth(360);
                Region rowSp = new Region(); HBox.setHgrow(rowSp, Priority.ALWAYS);

                String ansPreview = "";
                if (q instanceof MCQ m)            ansPreview = m.getOptions()[m.getCorrectIndex()];
                else if (q instanceof TextQuestion tq) ansPreview = String.valueOf(tq.getAnswer());
                else if (q instanceof RangeQuestion rq) ansPreview = rq.getMin()+"–"+rq.getMax();
                Label ansL = new Label(ansPreview); ansL.setStyle("-fx-font-size:12px;-fx-font-weight:600;-fx-text-fill:#0e7a56;"); ansL.setMaxWidth(150); ansL.setWrapText(true);

                MenuButton actMenu = menuBtn();
                MenuItem miEdit = UIUtils.modernMenuItem(UIUtils.ICO_EDIT,   "Edit",   "#0f7d74", false);
                miEdit.setOnAction(ev -> ExamEditor.showEditPopupPublic(q, app, stage, teacher));
                MenuItem miDel  = UIUtils.modernMenuItem(UIUtils.ICO_DELETE, "Delete", UIUtils.ACCENT_RED, true);
                miDel.setOnAction(ev -> {
                    Alert a = new Alert(Alert.AlertType.CONFIRMATION,"Delete this question permanently?",ButtonType.YES,ButtonType.NO); a.setHeaderText(null);
                    a.showAndWait().ifPresent(r -> { if(r==ButtonType.YES) {
                        QuestionBank.allQuestions.remove(q); QuestionDAO.delete(q); results.getChildren().remove(row);
                        UIUtils.Toast.success(ca, "Question deleted");
                    }});
                });
                actMenu.getItems().addAll(miEdit, new SeparatorMenuItem(), miDel);
                row.getChildren().addAll(typeB, metaB, qText, rowSp, ansL, actMenu);
                results.getChildren().add(row);
            }
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  SHARED HELPERS
    // ══════════════════════════════════════════════════════════════

    /** Three-dot options menu button */
    private static MenuButton menuBtn() {
        MenuButton mb = new MenuButton("···");
        mb.setStyle(
            "-fx-background-color:transparent;" +
            "-fx-text-fill:" + UIUtils.textMid() + ";" +
            "-fx-font-size:13.5px;-fx-font-weight:700;" +
            "-fx-background-radius:6;" +
            "-fx-border-color:" + UIUtils.border() + ";" +
            "-fx-border-radius:6;-fx-border-width:1;" +
            "-fx-padding:5 10;-fx-cursor:hand;"
        );
        mb.setMinWidth(MenuButton.USE_PREF_SIZE);
        return mb;
    }

    /** Small flat inline button used in exam rows */
    private static Button inlineBtn(String text, String textColor, String bgColor) {
        Button b = new Button(text);
        String base = "-fx-background-color:" + bgColor + ";-fx-text-fill:" + textColor + ";-fx-font-weight:700;-fx-font-size:12px;-fx-background-radius:6;-fx-padding:7 14;-fx-cursor:hand;-fx-border-color:" + textColor + "40;-fx-border-radius:6;-fx-border-width:1;";
        String hov  = "-fx-background-color:" + textColor + "26;-fx-text-fill:" + textColor + ";-fx-font-weight:700;-fx-font-size:12px;-fx-background-radius:6;-fx-padding:7 14;-fx-cursor:hand;-fx-border-color:" + textColor + "70;-fx-border-radius:6;-fx-border-width:1;";
        b.setStyle(base);
        b.setOnMouseEntered(e -> b.setStyle(hov));
        b.setOnMouseExited(e  -> b.setStyle(base));
        return b;
    }

    private static void wrapInScroll(Pane contentArea, ScrollPane scroll, VBox page) {
        scroll.setContent(page);
        contentArea.getChildren().add(scroll);
        if (contentArea instanceof javafx.scene.layout.AnchorPane ap) {
            javafx.scene.layout.AnchorPane.setTopAnchor(scroll, 0.0);
            javafx.scene.layout.AnchorPane.setBottomAnchor(scroll, 0.0);
            javafx.scene.layout.AnchorPane.setLeftAnchor(scroll, 0.0);
            javafx.scene.layout.AnchorPane.setRightAnchor(scroll, 0.0);
        }
    }

    private static DatePicker buildDatePicker(java.time.LocalDate initial) {
        DatePicker dp = new DatePicker(initial);
        dp.setStyle("-fx-font-size:12px;-fx-pref-height:36px;"); dp.setPrefWidth(144);
        return dp;
    }
    private static TextField buildTimeField(String initial) {
        TextField tf = new TextField(initial); tf.setPrefWidth(44); tf.setPrefHeight(36);
        tf.setStyle("-fx-font-family:Monospaced;-fx-font-size:14px;-fx-font-weight:700;-fx-alignment:center;" +
            "-fx-background-color:" + UIUtils.bgInput() + ";-fx-border-color:" + UIUtils.border() + ";" +
            "-fx-border-radius:6;-fx-background-radius:6;-fx-padding:3 1;");
        tf.textProperty().addListener((obs,ov,nv) -> { if(!nv.matches("\\d{0,2}")) tf.setText(ov); });
        return tf;
    }
    private static ToggleButton buildAmPmToggle() {
        ToggleButton tb = new ToggleButton("AM"); tb.setPrefWidth(64); tb.setPrefHeight(36);
        Runnable applyStyle = () -> {
            boolean pm = tb.isSelected();
            tb.setText(pm?"PM":"AM");
            tb.setStyle(pm
                ? "-fx-background-color:#5046a0;-fx-text-fill:white;-fx-font-weight:700;-fx-font-size:12px;-fx-background-radius:6;-fx-cursor:hand;"
                : "-fx-background-color:#0f7d74;-fx-text-fill:white;-fx-font-weight:700;-fx-font-size:12px;-fx-background-radius:6;-fx-cursor:hand;"
            );
        };
        applyStyle.run();
        tb.selectedProperty().addListener((obs,o,n) -> applyStyle.run());
        return tb;
    }
    private static HBox buildTimeInputRow(DatePicker dp, TextField hh, TextField mm, ToggleButton amPm) {
        HBox row = new HBox(5); row.setAlignment(Pos.CENTER_LEFT);
        Label colon = new Label(":"); colon.setStyle("-fx-font-size:16px;-fx-font-weight:700;-fx-text-fill:" + UIUtils.textDark() + ";");
        row.getChildren().addAll(dp, hh, colon, mm, amPm);
        return row;
    }
    private static java.time.LocalDateTime buildLDT(DatePicker dp, TextField hhF, TextField mmF, ToggleButton amPm) {
        int h = Integer.parseInt(hhF.getText().trim());
        int m = Integer.parseInt(mmF.getText().trim());
        if (h<1||h>12) throw new IllegalArgumentException("Hour out of range");
        if (m<0||m>59) throw new IllegalArgumentException("Minute out of range");
        boolean pm = amPm.isSelected();
        if (pm&&h!=12) h+=12; if (!pm&&h==12) h=0;
        return java.time.LocalDateTime.of(dp.getValue(), java.time.LocalTime.of(h,m));
    }

    private static Label sectionLabel(String text) {
        Label l = new Label(text.toUpperCase());
        l.setStyle("-fx-font-size:10px;-fx-font-weight:700;-fx-text-fill:" + UIUtils.textSubtle() + ";-fx-letter-spacing:1.2px;");
        return l;
    }
}
