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
import java.util.stream.Collectors;

// ═══════════════════════════════════════════════════════════
//  TeacherPortal.java
// ═══════════════════════════════════════════════════════════
public class TeacherPortal {

    // ╔══════════════════════════════════════════════════════╗
    //  TOAST NOTIFICATION SYSTEM  (#7)
    //  Non-blocking slide-in banners replacing modal alerts
    // ╚══════════════════════════════════════════════════════╝
    static class Toast {
        enum Type { SUCCESS, ERROR, INFO }

        /** Show a toast anchored to the top-right of the given Pane. */
        static void show(Pane root, String message, Type type) {
            javafx.application.Platform.runLater(() -> {
                String bg, icon;
                switch (type) {
                    case SUCCESS -> { bg = UIUtils.ACCENT_GREEN; icon = "✅"; }
                    case ERROR   -> { bg = UIUtils.ACCENT_RED;   icon = "❌"; }
                    default      -> { bg = UIUtils.ACCENT_BLUE;  icon = "ℹ"; }
                }

                HBox toast = new HBox(10);
                toast.setAlignment(Pos.CENTER_LEFT);
                toast.setPadding(new Insets(12, 20, 12, 16));
                toast.setMaxWidth(340);
                toast.setStyle(
                        "-fx-background-color:" + bg + ";" +
                                "-fx-background-radius:12;" +
                                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.28), 16, 0, 0, 4);"
                );

                Label iconL = new Label(icon);
                iconL.setStyle("-fx-font-size:16px;-fx-text-fill:white;");

                Label msgL = new Label(message);
                msgL.setStyle("-fx-text-fill:white;-fx-font-size:13px;-fx-font-weight:bold;");
                msgL.setWrapText(true);
                msgL.setMaxWidth(270);

                toast.getChildren().addAll(iconL, msgL);

                // Position top-right, stacked below any existing toasts
                long existing = root.getChildren().stream()
                        .filter(n -> n instanceof HBox && n.getId() != null && n.getId().equals("toast"))
                        .count();
                toast.setId("toast");
                toast.setLayoutX(root.getWidth() - 360);
                toast.setLayoutY(16 + existing * 64);
                toast.setOpacity(0);
                toast.setTranslateX(40);

                root.getChildren().add(toast);

                // Reposition when root width is known
                root.widthProperty().addListener((obs, o, n) ->
                        toast.setLayoutX(n.doubleValue() - 360)
                );

                // Animate in
                FadeTransition fi = new FadeTransition(Duration.millis(220), toast);
                fi.setFromValue(0); fi.setToValue(1);
                TranslateTransition ti = new TranslateTransition(Duration.millis(220), toast);
                ti.setFromX(40); ti.setToX(0);
                ti.setInterpolator(Interpolator.EASE_OUT);
                new ParallelTransition(fi, ti).play();

                // Auto-dismiss after 3 s
                PauseTransition pause = new PauseTransition(Duration.seconds(3));
                pause.setOnFinished(ev -> {
                    FadeTransition fo = new FadeTransition(Duration.millis(280), toast);
                    fo.setFromValue(1); fo.setToValue(0);
                    TranslateTransition to2 = new TranslateTransition(Duration.millis(280), toast);
                    to2.setFromX(0); to2.setToX(40);
                    ParallelTransition out = new ParallelTransition(fo, to2);
                    out.setOnFinished(e2 -> root.getChildren().remove(toast));
                    out.play();
                });
                pause.play();
            });
        }

        static void success(Pane root, String msg) { show(root, msg, Type.SUCCESS); }
        static void error(Pane root, String msg)   { show(root, msg, Type.ERROR); }
        static void info(Pane root, String msg)    { show(root, msg, Type.INFO); }
    }

    // ╔══════════════════════════════════════════════════════╗
    //  1. LOGIN & SIGN-UP
    // ╚══════════════════════════════════════════════════════╝
    public static Scene createLoginScene(Stage stage, ArrayList<Teacher> list, HelloApplication app) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color:" + UIUtils.bgContent() + ";");

        VBox accent = new VBox(0);
        accent.setPrefWidth(340); accent.setAlignment(Pos.CENTER); accent.setPadding(new Insets(50, 36, 50, 36));
        accent.setStyle("-fx-background-color:" + UIUtils.BG_DARK + ";");
        Label logo  = new Label("📚"); logo.setStyle("-fx-font-size:52px;-fx-text-fill:white;");
        Label brand = new Label("EduExam"); brand.setStyle("-fx-font-size:28px;-fx-font-weight:bold;-fx-text-fill:white;");
        Label tag   = new Label("Smart Assessment Platform");
        tag.setStyle("-fx-font-size:13px;-fx-text-fill:#64748b;");
        VBox.setMargin(brand, new Insets(10, 0, 4, 0));
        accent.getChildren().addAll(logo, brand, tag);
        root.setLeft(accent);

        VBox form = new VBox(18);
        form.setAlignment(Pos.CENTER); form.setPadding(new Insets(60, 70, 60, 70)); form.setMaxWidth(400);

        Label title = new Label("Teacher Login");
        title.setStyle("-fx-font-size:28px;-fx-font-weight:bold;-fx-text-fill:" + UIUtils.textDark() + ";");
        Label sub = new Label("Welcome back, educator 👋");
        sub.setStyle("-fx-font-size:14px;-fx-text-fill:" + UIUtils.textMid() + ";");

        TextField     txtUser = UIUtils.styledField("Email or Username");
        PasswordField txtPass = UIUtils.styledPassword("Password");

        Button btnLogin = UIUtils.primaryBtn("🔑", "Sign In", UIUtils.ACCENT_BLUE);
        btnLogin.setPrefWidth(Double.MAX_VALUE); btnLogin.setPrefHeight(46);
        Hyperlink linkSignup = new Hyperlink("Don't have an account? Register here");
        UIUtils.applyLinkEffects(linkSignup);
        Button btnBack = UIUtils.ghostBtn("←", "Back", UIUtils.TEXT_MID);

        btnLogin.setOnAction(e -> {
            String in = txtUser.getText().trim(), pw = txtPass.getText();
            if (in.isEmpty() || pw.isEmpty()) {
                app.showError("Missing Fields", "Please enter your email and password."); return;
            }
            // DB login
            Teacher found = UserDAO.loginTeacher(in, pw);
            if (found != null) {
                if (list.stream().noneMatch(t -> t.getEmail().equals(found.getEmail()))) list.add(found);
                stage.setScene(createDashboardScene(stage, found, app));
            } else {
                app.showError("Login Failed", "Invalid credentials. Please try again.");
            }
        });
        linkSignup.setOnAction(e -> stage.setScene(createSignupScene(stage, list, app)));
        btnBack.setOnAction(e -> stage.setScene(app.createMainScene(stage)));

        String lbl = "-fx-font-size:11px;-fx-font-weight:bold;-fx-text-fill:" + UIUtils.TEXT_MID
                + ";-fx-letter-spacing:0.5px;";
        form.getChildren().addAll(title, sub, UIUtils.divider(),
                new Label("EMAIL / USERNAME") {{ setStyle(lbl); }}, txtUser,
                new Label("PASSWORD")         {{ setStyle(lbl); }}, txtPass,
                btnLogin, linkSignup, btnBack);

        ScrollPane sp = new ScrollPane(form);
        sp.setFitToWidth(true); sp.setStyle("-fx-background:transparent;-fx-background-color:transparent;");
        root.setCenter(sp);

        Scene scene = new Scene(root, 1000, 600);
        UIUtils.applyStyle(scene); UIUtils.slideIn(form, true);
        return scene;
    }

    public static Scene createSignupScene(Stage stage, ArrayList<Teacher> list, HelloApplication app) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color:" + UIUtils.bgContent() + ";");

        VBox accent = new VBox(0);
        accent.setPrefWidth(340); accent.setAlignment(Pos.CENTER); accent.setPadding(new Insets(50, 36, 50, 36));
        accent.setStyle("-fx-background-color:" + UIUtils.BG_DARK + ";");
        Label sLogo  = new Label("🎓"); sLogo.setStyle("-fx-font-size:52px;-fx-text-fill:white;");
        Label sBrand = new Label("Join EduExam"); sBrand.setStyle("-fx-font-size:28px;-fx-font-weight:bold;-fx-text-fill:white;");
        Label sTag   = new Label("Create your teacher account");
        sTag.setStyle("-fx-font-size:13px;-fx-text-fill:#64748b;");
        VBox.setMargin(sBrand, new Insets(10, 0, 4, 0));
        accent.getChildren().addAll(sLogo, sBrand, sTag);
        root.setLeft(accent);

        VBox form = new VBox(14);
        form.setAlignment(Pos.CENTER); form.setPadding(new Insets(50, 70, 50, 70)); form.setMaxWidth(400);

        TextField     txtName    = UIUtils.styledField("Full Name");
        TextField     txtEmail   = UIUtils.styledField("Email Address");
        PasswordField txtPass    = UIUtils.styledPassword("Create Password");
        PasswordField txtConfirm = UIUtils.styledPassword("Confirm Password");

        Button btnReg  = UIUtils.primaryBtn("✅", "Create Account", UIUtils.ACCENT_PURP);
        btnReg.setPrefWidth(Double.MAX_VALUE); btnReg.setPrefHeight(46);
        Button btnBack = UIUtils.ghostBtn("←", "Back to Login", UIUtils.TEXT_MID);

        btnReg.setOnAction(e -> {
            String name  = txtName.getText().trim();
            String email = txtEmail.getText().trim();
            String pass  = txtPass.getText();
            if (!pass.equals(txtConfirm.getText())) { app.showError("Mismatch", "Passwords do not match!"); return; }
            if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) { app.showError("Missing Info", "Please fill in all fields."); return; }
            if (UserDAO.teacherEmailExists(email)) { app.showError("Email Taken", "A teacher with that email already exists."); return; }
            boolean ok = UserDAO.registerTeacher(name, email, pass);
            if (ok) {
                list.add(new Teacher(name, email, pass));
                app.showInfo("✅ Success", "Account created! You can now log in.");
                stage.setScene(createLoginScene(stage, list, app));
            } else {
                app.showError("Error", "Registration failed. Please try again.");
            }
        });
        btnBack.setOnAction(e -> stage.setScene(createLoginScene(stage, list, app)));

        String lblStyle = "-fx-font-size:11px;-fx-font-weight:bold;-fx-text-fill:" + UIUtils.TEXT_MID
                + ";-fx-letter-spacing:0.5px;";
        form.getChildren().addAll(
                new Label("Teacher Registration") {{ setStyle("-fx-font-size:24px;-fx-font-weight:bold;-fx-text-fill:"+UIUtils.TEXT_DARK+";"); }},
                new Label("FULL NAME")   {{ setStyle(lblStyle); }}, txtName,
                new Label("EMAIL")       {{ setStyle(lblStyle); }}, txtEmail,
                new Label("PASSWORD")    {{ setStyle(lblStyle); }}, txtPass,
                new Label("CONFIRM")     {{ setStyle(lblStyle); }}, txtConfirm,
                btnReg, btnBack);

        ScrollPane sp = new ScrollPane(form);
        sp.setFitToWidth(true); sp.setStyle("-fx-background:transparent;-fx-background-color:transparent;");
        root.setCenter(sp);

        Scene scene = new Scene(root, 1000, 600);
        UIUtils.applyStyle(scene); UIUtils.slideIn(form, true);
        return scene;
    }

    // Remembers which nav tab is active so theme toggle returns to same page
    static int activeNavIndex = 0;

    // ╔══════════════════════════════════════════════════════╗
    //  2. DASHBOARD SHELL
    // ╚══════════════════════════════════════════════════════╝
    static Scene createDashboardScene(Stage stage, Teacher teacher, HelloApplication app) {
        return createDashboardScene(stage, teacher, app, activeNavIndex);
    }

    static Scene createDashboardScene(Stage stage, Teacher teacher, HelloApplication app, int startPage) {
        BorderPane root = new BorderPane();

        VBox sidebar = new VBox(8);
        sidebar.setPrefWidth(210);
        sidebar.setPadding(new Insets(0, 10, 20, 10));
        sidebar.setStyle("-fx-background-color:" + UIUtils.BG_DARK + ";");

        VBox avatarBox = new VBox(8);
        avatarBox.setAlignment(Pos.CENTER);
        avatarBox.setPadding(new Insets(30, 10, 20, 10));
        Circle av = new Circle(36);
        av.setFill(Color.web(UIUtils.ACCENT_BLUE));
        Label initials = new Label(teacher.getUser().substring(0, 1).toUpperCase());
        initials.setStyle("-fx-font-size:22px;-fx-font-weight:bold;-fx-text-fill:white;");
        StackPane avStack = new StackPane(av, initials);
        Label nameL = new Label(teacher.getUser());
        nameL.setStyle("-fx-font-size:14px;-fx-font-weight:bold;-fx-text-fill:white;");
        Label roleL = UIUtils.badge("Teacher", UIUtils.ACCENT_BLUE);
        VBox.setMargin(roleL, new Insets(2, 0, 0, 0));
        avatarBox.getChildren().addAll(avStack, nameL, roleL);

        // ── Theme switch — top of sidebar, above avatar ───────
        StackPane themeSwitch = UIUtils.themeToggleSwitch(() ->
                stage.setScene(createDashboardScene(stage, teacher, app, activeNavIndex))
        );
        HBox switchRow = new HBox(themeSwitch);
        switchRow.setAlignment(Pos.CENTER_LEFT);
        switchRow.setPadding(new Insets(14, 10, 0, 14));

        sidebar.getChildren().addAll(switchRow, avatarBox);

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color:#1e293b;");
        sep.setPadding(new Insets(4, 0, 12, 0));
        sidebar.getChildren().add(sep);

        javafx.scene.layout.AnchorPane contentArea = new javafx.scene.layout.AnchorPane();
        contentArea.setPrefSize(790, 600);
        contentArea.setStyle("-fx-background-color:" + UIUtils.bgContent() + ";");

        String[][] nav = {
                {"🏠", "Dashboard",      UIUtils.ACCENT_BLUE},
                {"📝", "Create Exam",    UIUtils.ACCENT_PURP},
                {"➕", "Add Question",   UIUtils.ACCENT_YELL},
                {"📚", "Question Bank",  UIUtils.ACCENT_GREEN},
                {"📂", "Past Exams",     UIUtils.ACCENT_ORG},
        };
        Button[] navBtns = new Button[nav.length];
        for (int i = 0; i < nav.length; i++) {
            final int idx = i;
            final String color = nav[i][2];
            navBtns[i] = UIUtils.sidebarBtn(nav[i][0], nav[i][1], color);
            navBtns[i].setOnAction(e -> {
                activeNavIndex = idx;
                for (int j = 0; j < navBtns.length; j++) {
                    UIUtils.sidebarBtn(navBtns[j], nav[j][0], nav[j][1], nav[j][2]);
                }
                UIUtils.setSidebarBtnActive(navBtns[idx], color);
                contentArea.getChildren().clear();
                // Clear any in-progress exam editing when navigating away
                if (idx != 1) { ExamEditor.clearState(); ExamEditor.editing = null; }
                switch (idx) {
                    case 0 -> renderDashboardHome(contentArea, app);
                    case 1 -> ExamEditor.show(contentArea, app);
                    case 2 -> QuestionEditor.show(contentArea, app, null, null);
                    case 3 -> QuestionBankBrowser.render(contentArea, app);
                    case 4 -> PastExams.render(contentArea, app);
                }
                UIUtils.slideIn(contentArea, true);
            });
            sidebar.getChildren().add(navBtns[i]);
        }

        Region spacer = new Region(); VBox.setVgrow(spacer, Priority.ALWAYS);
        Button btnLogout = UIUtils.primaryBtn("🚪", "Log Out", UIUtils.ACCENT_RED);
        btnLogout.setPrefWidth(190);
        btnLogout.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Log Out");
            confirm.setHeaderText("Are you sure you want to log out?");
            confirm.setContentText("Any unsaved changes will be lost.");
            confirm.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
            confirm.showAndWait().ifPresent(r -> {
                if (r == ButtonType.YES) {
                    ExamEditor.clearState();
                    ExamEditor.editing = null;
                    stage.setScene(app.createMainScene(stage));
                }
            });
        });
        sidebar.getChildren().addAll(spacer, btnLogout);

        root.setLeft(sidebar);
        root.setCenter(contentArea);

        // Restore the active page (important after theme toggle)
        UIUtils.setSidebarBtnActive(navBtns[startPage], nav[startPage][2]);
        contentArea.getChildren().clear();
        switch (startPage) {
            case 0 -> renderDashboardHome(contentArea, app);
            case 1 -> ExamEditor.show(contentArea, app);
            case 2 -> QuestionEditor.show(contentArea, app, null, null);
            case 3 -> QuestionBankBrowser.render(contentArea, app);
            case 4 -> PastExams.render(contentArea, app);
        }

        return new Scene(root, 1000, 600);
    }

    // ╔══════════════════════════════════════════════════════╗
    //  3. DASHBOARD HOME  (Scheduled + Live tabs)
    // ╚══════════════════════════════════════════════════════╝
    public static void renderDashboardHome(Pane contentArea, HelloApplication app) {
        contentArea.getChildren().clear();

        ScrollPane scroll = new ScrollPane();
        scroll.prefWidthProperty().bind(contentArea.widthProperty());
        scroll.prefHeightProperty().bind(contentArea.heightProperty());
        scroll.setStyle("-fx-background:transparent;-fx-background-color:transparent;");
        scroll.setFitToWidth(true);

        VBox page = new VBox(24);
        page.setPadding(new Insets(30));

        // ── Greeting header ───────────────────────────────────
        String hour = String.valueOf(java.time.LocalTime.now().getHour());
        String timeGreet = Integer.parseInt(hour) < 12 ? "Good morning" :
                Integer.parseInt(hour) < 17 ? "Good afternoon" : "Good evening";
        String dateStr = java.time.LocalDate.now().format(
                java.time.format.DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy"));
        HBox greetRow = new HBox();
        greetRow.setAlignment(Pos.CENTER_LEFT);
        VBox greetBox = new VBox(3);
        Label greetL = new Label(timeGreet + " 👋");
        greetL.setStyle("-fx-font-size:22px;-fx-font-weight:bold;-fx-text-fill:" + UIUtils.textDark() + ";");
        Label dateL  = new Label(dateStr);
        dateL.setStyle("-fx-font-size:13px;-fx-text-fill:" + UIUtils.textMid() + ";");
        greetBox.getChildren().addAll(greetL, dateL);
        greetRow.getChildren().add(greetBox);
        long liveCount  = ExamBank.allExams.stream().filter(Exam::isLive).count();
        long totalQ     = QuestionBank.allQuestions.size();
        long schedCount = ExamBank.allExams.stream()
                .filter(e -> !e.isLive() && e.getScheduleDetails() != null
                        && !e.getScheduleDetails().isEmpty()
                        && !e.getScheduleDetails().startsWith("Ended")).count();
        long pastCount  = ExamBank.allExams.stream()
                .filter(e -> !e.isLive() && e.getScheduleDetails() != null
                        && e.getScheduleDetails().startsWith("Ended")).count();

        HBox stats = new HBox(16);
        stats.setMaxWidth(760);
        stats.getChildren().addAll(
                UIUtils.statCard("📡", String.valueOf(liveCount),  "Live Exams",  UIUtils.ACCENT_GREEN),
                UIUtils.statCard("📋", String.valueOf(totalQ),     "Questions",   UIUtils.ACCENT_BLUE),
                UIUtils.statCard("📅", String.valueOf(schedCount), "Scheduled",   UIUtils.ACCENT_PURP),
                UIUtils.statCard("📂", String.valueOf(pastCount),  "Past Exams",  UIUtils.ACCENT_ORG)
        );

        // ── LIVE EXAMS section ────────────────────────────────
        HBox liveHdrRow = new HBox(10); liveHdrRow.setAlignment(Pos.CENTER_LEFT);
        Region liveDot = new Region(); liveDot.setPrefSize(10, 10);
        liveDot.setStyle("-fx-background-color:" + UIUtils.ACCENT_GREEN + ";-fx-background-radius:99;");
        Label liveHdr = UIUtils.heading("Live Exams");
        Label liveCnt = UIUtils.badge(String.valueOf(liveCount), UIUtils.ACCENT_GREEN);
        liveHdrRow.getChildren().addAll(liveDot, liveHdr, liveCnt);
        VBox liveBox = new VBox(10);
        if (ExamBank.getLiveExams().isEmpty()) {
            liveBox.getChildren().add(emptyStateCard(
                    "📡", "No Live Exams Right Now",
                    "Launch a scheduled exam to make it live. Students can join using the exam code.",
                    null, null, null
            ));
        } else {
            for (Exam e : ExamBank.getLiveExams()) {
                liveBox.getChildren().add(buildLiveRow(e, contentArea, app));
            }
        }

        // ── SCHEDULED EXAMS section ───────────────────────────
        HBox schedHdrRow = new HBox(10); schedHdrRow.setAlignment(Pos.CENTER_LEFT);
        Region schedDot = new Region(); schedDot.setPrefSize(10, 10);
        schedDot.setStyle("-fx-background-color:" + UIUtils.ACCENT_PURP + ";-fx-background-radius:99;");
        Label schedHdr = UIUtils.heading("Scheduled Exams");
        Label schedCnt = UIUtils.badge(String.valueOf(schedCount), UIUtils.ACCENT_PURP);
        schedHdrRow.getChildren().addAll(schedDot, schedHdr, schedCnt);
        VBox schedBox = new VBox(10);
        boolean hasScheduled = false;
        for (Exam e : ExamBank.allExams) {
            if (!e.isLive() && e.getScheduleDetails() != null
                    && !e.getScheduleDetails().isEmpty()
                    && !e.getScheduleDetails().startsWith("Ended")) {
                schedBox.getChildren().add(buildScheduledRow(e, contentArea, app));
                hasScheduled = true;
            }
        }
        if (!hasScheduled) schedBox.getChildren().add(emptyStateCard(
                "📝", "No Scheduled Exams Yet",
                "Create your first exam to get started. Once created, you can launch it live for students.",
                "Create Exam", UIUtils.ACCENT_PURP,
                () -> ExamEditor.show(contentArea, app)
        ));

        page.getChildren().addAll(
                greetRow,
                stats,
                UIUtils.divider(), liveHdrRow, liveBox,
                UIUtils.divider(), schedHdrRow, schedBox
        );

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

    private static Label emptyLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill:" + UIUtils.textMid() + ";-fx-font-size:14px;-fx-padding:16;");
        return l;
    }

    // ── Rich empty state card  (#2) ───────────────────────────
    private static VBox emptyStateCard(String icon, String heading, String sub, String btnLabel,
                                       String btnColor, Runnable btnAction) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(36, 24, 36, 24));
        card.setMaxWidth(520);
        card.setStyle("-fx-background-color:" + UIUtils.bgCard() + ";-fx-background-radius:16;"
                + "-fx-border-color:" + UIUtils.border() + ";-fx-border-radius:16;");
        DropShadow ds = new DropShadow();
        ds.setColor(Color.color(0,0,0,0.05)); ds.setRadius(10); ds.setOffsetY(3);
        card.setEffect(ds);

        Label ico = new Label(icon);
        ico.setStyle("-fx-font-size:42px;-fx-text-fill:" + UIUtils.textDark() + ";");

        Label hdr = new Label(heading);
        hdr.setStyle("-fx-font-size:16px;-fx-font-weight:bold;-fx-text-fill:" + UIUtils.textDark() + ";");

        Label subL = new Label(sub);
        subL.setStyle("-fx-font-size:13px;-fx-text-fill:" + UIUtils.textMid() + ";");
        subL.setWrapText(true);
        subL.setMaxWidth(380);
        subL.setAlignment(Pos.CENTER);

        if (btnLabel != null) {
            Button btn = UIUtils.primaryBtn("", btnLabel, btnColor);
            btn.setOnAction(e -> btnAction.run());
            card.getChildren().addAll(ico, hdr, subL, btn);
        } else {
            card.getChildren().addAll(ico, hdr, subL);
        }
        return card;
    }

    // ── Live exam row ─────────────────────────────────────────
    private static VBox buildLiveRow(Exam e, Pane ca, HelloApplication app) {
        VBox wrapper = new VBox();

        VBox card = new VBox(10);
        card.setPadding(new Insets(14, 18, 14, 18));
        card.setStyle("-fx-background-color:" + UIUtils.bgCard() + ";-fx-background-radius:12;"
                + "-fx-border-color:#22c55e55;-fx-border-radius:12;-fx-border-width:1.5;");
        DropShadow ds = new DropShadow(); ds.setColor(Color.color(0,0,0,0.06)); ds.setRadius(8); ds.setOffsetY(2);
        card.setEffect(ds);

        // ── Top row: dot + title + badges + options ──────────
        HBox topRow = new HBox(12);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label dot = new Label("🟢"); dot.setStyle("-fx-font-size:16px;-fx-text-fill:" + UIUtils.ACCENT_GREEN + ";");
        VBox info = new VBox(2);
        String displayTitle = (e.getTitle() != null && !e.getTitle().isEmpty()) ? e.getTitle() : e.getSubject();
        Label subL = new Label(displayTitle);
        subL.setStyle("-fx-font-size:15px;-fx-font-weight:bold;-fx-text-fill:" + UIUtils.textDark() + ";");
        Label metaL = new Label(e.getSubject() + "  ·  Grade " + e.getGrade() + "  ·  " + e.getDuration() + " min exam  ·  " + e.getTotalMarks() + " marks");
        metaL.setStyle("-fx-font-size:12px;-fx-text-fill:" + UIUtils.textMid() + ";");
        info.getChildren().addAll(subL, metaL);

        Region sp1 = new Region(); HBox.setHgrow(sp1, Priority.ALWAYS);

        // Options menu
        MenuButton opts = new MenuButton("⚙");
        opts.setStyle("-fx-background-color:" + UIUtils.bgSurface() + ";-fx-text-fill:" + UIUtils.textDark() + ";-fx-font-weight:bold;-fx-background-radius:8;-fx-font-size:14px;");

        MenuItem edit = new MenuItem("✏  Edit Exam");
        edit.setOnAction(ev -> ExamEditor.loadForEditing(e, ca, app));

        MenuItem stop = new MenuItem("⏹  Stop Exam");
        stop.setOnAction(ev -> {
            Alert a = new Alert(Alert.AlertType.CONFIRMATION,
                    "Stop this exam? It will move to Past Exams.", ButtonType.YES, ButtonType.NO);
            a.showAndWait().ifPresent(r -> {
                if (r == ButtonType.YES) {
                    e.setLive(false);
                    e.setScheduleDetails("Ended: " + nowStr());
                    renderDashboardHome(ca, app);
                }
            });
        });

        MenuItem del = new MenuItem("🗑  Delete");
        del.setStyle("-fx-text-fill:" + UIUtils.ACCENT_RED + ";-fx-font-size:13px;");
        del.setOnAction(ev -> {
            ExamBank.allExams.remove(e);
            renderDashboardHome(ca, app);
        });

        opts.getItems().addAll(edit, stop, del);
        topRow.getChildren().addAll(dot, info, sp1, opts);

        // ── Bottom row: countdown + code ─────────────────────
        HBox bottomRow = new HBox(16);
        bottomRow.setAlignment(Pos.CENTER_LEFT);

        // Countdown label (updates via timeline)
        Label countdown = new Label("⏱ " + e.getRemainingFormatted());
        countdown.setStyle("-fx-font-family:Monospaced;-fx-font-size:15px;-fx-font-weight:bold;"
                + "-fx-text-fill:" + UIUtils.ACCENT_GREEN
                + ";-fx-background-color:" + (UIUtils.darkMode ? "#14532d" : "#dcfce7") + ";-fx-background-radius:8;-fx-padding:5 14;");

        Timeline tl = new Timeline(new KeyFrame(Duration.seconds(1), ev -> {
            countdown.setText("⏱ " + e.getRemainingFormatted());
            if (e.getRemainingMillis() == 0 && e.isLive()) {
                e.setLive(false);
                if (e.getScheduleDetails() == null || !e.getScheduleDetails().startsWith("Ended"))
                    e.setScheduleDetails("Ended: " + nowStr());
                renderDashboardHome(ca, app);
                Toast.info(ca, e.getSubject() + " exam ended — moved to Past Exams");
            }
        }));
        tl.setCycleCount(Timeline.INDEFINITE);
        tl.play();
        countdown.sceneProperty().addListener((obs, o, n) -> { if (n == null) tl.stop(); });

        // Exam code – click to copy
        e.generateCode();
        Label codeL = new Label("🔑 " + e.getExamCode());
        codeL.setStyle("-fx-font-family:Monospaced;-fx-font-size:15px;-fx-font-weight:bold;"
                + "-fx-text-fill:" + UIUtils.ACCENT_PURP + ";-fx-cursor:hand;"
                + "-fx-background-color:#f3e8ff;-fx-background-radius:8;-fx-padding:5 14;");
        Tooltip.install(codeL, new Tooltip("Click to copy exam code"));
        codeL.setOnMouseClicked(ev -> {
            javafx.scene.input.Clipboard cb2 = javafx.scene.input.Clipboard.getSystemClipboard();
            javafx.scene.input.ClipboardContent cc = new javafx.scene.input.ClipboardContent();
            cc.putString(e.getExamCode()); cb2.setContent(cc);
            codeL.setText("🔑 " + e.getExamCode() + "  ✅ Copied!");
            PauseTransition revert = new PauseTransition(Duration.seconds(1.5));
            revert.setOnFinished(ev2 -> codeL.setText("🔑 " + e.getExamCode()));
            revert.play();
            Toast.success(ca, "Code " + e.getExamCode() + " copied to clipboard");
        });

        bottomRow.getChildren().addAll(countdown, codeL);

        card.getChildren().addAll(topRow, bottomRow);
        wrapper.getChildren().add(card);
        return wrapper;
    }

    // ── Scheduled exam row ────────────────────────────────────
    private static HBox buildScheduledRow(Exam e, Pane ca, HelloApplication app) {
        HBox row = new HBox(14);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(14, 18, 14, 18));
        row.setStyle("-fx-background-color:" + UIUtils.bgCard() + ";-fx-background-radius:12;"
                + "-fx-border-color:" + UIUtils.border() + ";-fx-border-radius:12;");
        DropShadow ds = new DropShadow(); ds.setColor(Color.color(0,0,0,0.05)); ds.setRadius(6); ds.setOffsetY(1);
        row.setEffect(ds);

        VBox info = new VBox(4);
        String displayTitle = (e.getTitle() != null && !e.getTitle().isEmpty()) ? e.getTitle() : e.getSubject();
        Label subL = new Label("📅 " + displayTitle);
        subL.setStyle("-fx-font-size:15px;-fx-font-weight:bold;-fx-text-fill:" + UIUtils.textDark() + ";");
        Label metaL = new Label(e.getSubject() + " · Grade " + e.getGrade() + " · " + e.getDuration() + " min · " + e.getTotalMarks() + " marks");
        metaL.setStyle("-fx-font-size:12px;-fx-text-fill:" + UIUtils.textMid() + ";");
        info.getChildren().addAll(subL, metaL);

        // If auto-scheduled: show countdown-to-start label that ticks every second
        if (e.getScheduledStartMillis() > 0 && e.getScheduledStartMillis() > System.currentTimeMillis()) {
            Label countdownLbl = new Label("⏰ Starts in " + e.getStartCountdownFormatted());
            countdownLbl.setStyle("-fx-font-family:Monospaced;-fx-font-size:12px;-fx-font-weight:bold;"
                    + "-fx-text-fill:" + UIUtils.ACCENT_PURP
                    + ";-fx-background-color:" + (UIUtils.darkMode ? "#3b1f6e" : "#f3e8ff") + ";-fx-background-radius:6;-fx-padding:3 10;");

            // Tick every second; auto-launch when start time arrives
            Timeline autoTl = new Timeline(new KeyFrame(Duration.seconds(1), ev -> {
                long now = System.currentTimeMillis();
                if (now >= e.getScheduledStartMillis() && !e.isLive()) {
                    // Auto-launch
                    e.setLive(true);
                    e.generateCode();
                    e.setLiveEndMillis(e.getScheduledEndMillis());
                    e.setScheduleDetails(nowStr());
                    renderDashboardHome(ca, app);
                    Toast.success(ca, "📡 " + displayTitle + " went live automatically!");
                } else {
                    countdownLbl.setText("⏰ Starts in " + e.getStartCountdownFormatted());
                }
            }));
            autoTl.setCycleCount(Timeline.INDEFINITE);
            autoTl.play();
            countdownLbl.sceneProperty().addListener((obs, o, n) -> { if (n == null) autoTl.stop(); });

            // Show scheduled window time
            java.time.format.DateTimeFormatter fmt =
                    java.time.format.DateTimeFormatter.ofPattern("dd MMM · hh:mm a");
            String startStr = java.time.LocalDateTime
                    .ofInstant(java.time.Instant.ofEpochMilli(e.getScheduledStartMillis()),
                            java.time.ZoneId.systemDefault()).format(fmt);
            String endStr = java.time.LocalDateTime
                    .ofInstant(java.time.Instant.ofEpochMilli(e.getScheduledEndMillis()),
                            java.time.ZoneId.systemDefault()).format(fmt);
            Label windowLbl = new Label("🕐 " + startStr + "  →  " + endStr);
            windowLbl.setStyle("-fx-font-size:11px;-fx-text-fill:" + UIUtils.textMid() + ";");
            info.getChildren().addAll(windowLbl, countdownLbl);
        } else if (e.getScheduledStartMillis() > 0) {
            // Start time passed but exam may have gone live already — should not show here
            Label pastLbl = new Label("⚠ Schedule time has passed — launch manually");
            pastLbl.setStyle("-fx-font-size:11px;-fx-text-fill:" + UIUtils.ACCENT_ORG + ";-fx-font-weight:bold;");
            info.getChildren().add(pastLbl);
        }

        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);

        // Primary: Go Live Now
        Button btnLaunch = UIUtils.primaryBtn("📡", "Go Live Now", UIUtils.ACCENT_GREEN);
        btnLaunch.setOnAction(ev -> showLaunchPopup(e, ca, app));

        // Secondary actions in a clean options menu
        MenuButton opts = new MenuButton("⋯  Options");
        opts.setStyle(
                "-fx-background-color:" + UIUtils.bgCard() + ";" +
                        "-fx-text-fill:" + UIUtils.textDark() + ";" +
                        "-fx-font-weight:bold;-fx-font-size:13px;" +
                        "-fx-background-radius:10;" +
                        "-fx-border-color:" + UIUtils.border() + ";" +
                        "-fx-border-radius:10;" +
                        "-fx-padding:9 16;" +
                        "-fx-cursor:hand;"
        );
        opts.setOnMouseEntered(ev -> opts.setStyle(opts.getStyle().replace("-fx-background-color:" + UIUtils.bgCard() + ";", "-fx-background-color:" + UIUtils.bgContent() + ";")));
        opts.setOnMouseExited(ev  -> opts.setStyle(opts.getStyle().replace("-fx-background-color:" + UIUtils.bgContent() + ";", "-fx-background-color:" + UIUtils.bgCard() + ";")));

        MenuItem miSchedule = new MenuItem("📅  Set Auto-Schedule");
        miSchedule.setOnAction(ev -> showSchedulePopup(e, ca, app));

        MenuItem miDetails = new MenuItem("📋  View Details");
        miDetails.setOnAction(ev -> showExamDetailsPopup(e, ca, app));

        MenuItem miEdit = new MenuItem("✏  Edit Exam");
        miEdit.setOnAction(ev -> ExamEditor.loadForEditing(e, ca, app));

        MenuItem miDel = new MenuItem("🗑  Delete");
        miDel.setStyle("-fx-text-fill:" + UIUtils.ACCENT_RED + ";-fx-font-size:13px;");
        miDel.setOnAction(ev -> {
            Alert a = new Alert(Alert.AlertType.CONFIRMATION, "Delete this exam?", ButtonType.YES, ButtonType.NO);
            a.showAndWait().ifPresent(r -> { if (r == ButtonType.YES) { ExamBank.allExams.remove(e); renderDashboardHome(ca, app); } });
        });

        opts.getItems().addAll(miSchedule, miDetails, miEdit, new SeparatorMenuItem(), miDel);

        row.getChildren().addAll(info, sp, btnLaunch, opts);
        return row;
    }

    // ── Exam details popup (for scheduled exams) ──────────────
    private static void showExamDetailsPopup(Exam e, Pane ca, HelloApplication app) {
        Stage st = new Stage();
        st.initModality(Modality.APPLICATION_MODAL);
        st.setTitle("Exam Details");

        VBox root = new VBox(16);
        root.setPadding(new Insets(28));
        root.setStyle("-fx-background-color:" + UIUtils.bgCard() + ";");

        Label title = new Label("📋  " + (e.getTitle() == null || e.getTitle().isEmpty() ? e.getSubject() : e.getTitle()));
        title.setStyle("-fx-font-size:20px;-fx-font-weight:bold;-fx-text-fill:" + UIUtils.textDark() + ";");

        // Meta info grid
        GridPane grid = new GridPane();
        grid.setHgap(16); grid.setVgap(10);
        grid.setPadding(new Insets(12));
        grid.setStyle("-fx-background-color:" + UIUtils.bgContent() + ";-fx-background-radius:10;-fx-border-color:" + UIUtils.border() + ";-fx-border-radius:10;");

        addGridRow(grid, 0, "Subject:", e.getSubject());
        addGridRow(grid, 1, "Grade:", "Grade " + e.getGrade());
        addGridRow(grid, 2, "Total Marks:", String.valueOf(e.getTotalMarks()));
        addGridRow(grid, 3, "Duration:", e.getDuration() + " minutes");
        addGridRow(grid, 4, "Questions:", String.valueOf(e.getQuestionsMap() != null ? e.getQuestionsMap().size() : 0));

        // Description
        if (e.getDescription() != null && !e.getDescription().isEmpty()) {
            Label descHdr = new Label("Description:");
            descHdr.setStyle("-fx-font-weight:bold;-fx-text-fill:" + UIUtils.textMid() + ";");
            TextArea descArea = new TextArea(e.getDescription());
            descArea.setEditable(false); descArea.setWrapText(true); descArea.setPrefHeight(70);
            descArea.setStyle("-fx-font-size:13px;-fx-background-radius:8;-fx-background-color:" + UIUtils.bgSurface() + ";-fx-text-fill:" + UIUtils.textDark() + ";-fx-control-inner-background:" + UIUtils.bgSurface() + ";");
            root.getChildren().addAll(descHdr, descArea);
        }

        // Question list preview
        Label qlHdr = new Label("Question List:");
        qlHdr.setStyle("-fx-font-weight:bold;-fx-text-fill:" + UIUtils.textMid() + ";");
        VBox qlBox = new VBox(6);
        qlBox.setPadding(new Insets(8));
        qlBox.setStyle("-fx-background-color:" + UIUtils.bgContent() + ";-fx-background-radius:8;-fx-border-color:" + UIUtils.border() + ";-fx-border-radius:8;");
        if (e.getQuestionsMap() != null && !e.getQuestionsMap().isEmpty()) {
            int idx = 1;
            for (Question q : e.getQuestionsMap().keySet()) {
                String type = (q instanceof MCQ) ? "MCQ" : (q instanceof TextQuestion) ? "Text" : "Range";
                Label ql = new Label(idx++ + ". [" + type + "] " + q.getQuestionText());
                ql.setStyle("-fx-font-size:13px;-fx-text-fill:" + UIUtils.textDark() + ";");
                ql.setWrapText(true);
                qlBox.getChildren().add(ql);
            }
        } else {
            qlBox.getChildren().add(new Label("No questions added yet.") {{ setStyle("-fx-text-fill:" + UIUtils.textMid() + ";"); }});
        }

        HBox btnRow = new HBox(12);
        Button btnEdit = UIUtils.primaryBtn("✏", "Edit Exam", UIUtils.ACCENT_ORG);
        btnEdit.setOnAction(ev -> { st.close(); ExamEditor.loadForEditing(e, ca, app); });
        Button btnClose = UIUtils.ghostBtn("✕", "Close", UIUtils.TEXT_MID);
        btnClose.setOnAction(ev -> st.close());
        btnRow.getChildren().addAll(btnEdit, btnClose);

        root.getChildren().addAll(0, List.of(title, grid, qlHdr, qlBox));
        root.getChildren().addAll(btnRow);

        ScrollPane sp = new ScrollPane(root);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background:white;");
        st.setScene(new Scene(sp, 520, 580));
        st.show();
    }

    private static void addGridRow(GridPane g, int row, String lbl, String val) {
        Label l = new Label(lbl); l.setStyle("-fx-font-weight:bold;-fx-text-fill:" + UIUtils.textMid() + ";-fx-font-size:13px;");
        Label v = new Label(val); v.setStyle("-fx-text-fill:" + UIUtils.textDark() + ";-fx-font-size:13px;");
        g.add(l, 0, row); g.add(v, 1, row);
    }

    // ╔══════════════════════════════════════════════════════╗
    //  4. LAUNCH POPUP
    // ╚══════════════════════════════════════════════════════╝
    private static void showLaunchPopup(Exam exam, Pane ca, HelloApplication app) {
        Stage st = new Stage();
        st.initModality(Modality.APPLICATION_MODAL);
        st.setTitle("Go Live");

        VBox root = new VBox(16);
        root.setPadding(new Insets(28));
        root.setStyle("-fx-background-color:" + UIUtils.bgCard() + ";");

        Label lbl = new Label("📡  Set Live Window Duration");
        lbl.setStyle("-fx-font-size:18px;-fx-font-weight:bold;-fx-text-fill:" + UIUtils.textDark() + ";");
        Label hint = new Label("Exam duration: " + exam.getDuration() + " min");
        hint.setStyle("-fx-font-size:12px;-fx-text-fill:" + UIUtils.ACCENT_ORG + ";");

        Label info = new Label("Students will have " + exam.getDuration() + " min to complete the exam.");
        info.setStyle("-fx-font-size:12px;-fx-text-fill:" + UIUtils.textMid() + ";");
        info.setWrapText(true);

        TextField hF = UIUtils.styledField("0"); hF.setPrefWidth(70);
        TextField mF = UIUtils.styledField("30"); mF.setPrefWidth(70);
        HBox timeRow = new HBox(10);
        timeRow.setAlignment(Pos.CENTER_LEFT);
        Label hLbl = new Label("hours"); hLbl.setStyle("-fx-font-size:13px;-fx-text-fill:" + UIUtils.textMid() + ";");
        Label mLbl = new Label("minutes"); mLbl.setStyle("-fx-font-size:13px;-fx-text-fill:" + UIUtils.textMid() + ";");
        timeRow.getChildren().addAll(hF, hLbl, mF, mLbl);

        Button btnGo = UIUtils.primaryBtn("🚀", "Launch Now", UIUtils.ACCENT_GREEN);
        btnGo.setPrefWidth(220); btnGo.setPrefHeight(46);

        btnGo.setOnAction(e -> {
            try {
                int h = hF.getText().trim().isEmpty() ? 0 : Integer.parseInt(hF.getText().trim());
                int m = mF.getText().trim().isEmpty() ? 0 : Integer.parseInt(mF.getText().trim());
                int totalMins = h * 60 + m;
                if (totalMins < Integer.parseInt(exam.getDuration())) {
                    Toast.error(ca, "Live window must be ≥ exam duration (" + exam.getDuration() + " min)"); return;
                }
                exam.setLive(true);
                exam.generateCode();
                exam.setLiveWindow(h + "h " + m + "m");
                exam.setScheduleDetails(nowStr());
                long endMs = System.currentTimeMillis() + totalMins * 60_000L;
                exam.setLiveEndMillis(endMs);

                st.close();
                renderDashboardHome(ca, app);
                Toast.success(ca, exam.getSubject() + " is now live! Code: " + exam.getExamCode());
            } catch (NumberFormatException ex) {
                Toast.error(ca, "Enter valid whole numbers for hours/minutes");
            }
        });

        root.getChildren().addAll(lbl, hint, info, new Label("Live window:") {{ setStyle("-fx-font-weight:bold;"); }}, timeRow, btnGo);
        st.setScene(new Scene(root, 360, 280));
        st.show();
    }

    private static String nowStr() {
        return java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    // ╔══════════════════════════════════════════════════════╗
    //  SCHEDULE POPUP  — pick start date/time + end time
    //  Exam auto-launches when the start time arrives.
    //  Uses editable text fields for hour/minute input.
    // ╚══════════════════════════════════════════════════════╝
    private static void showSchedulePopup(Exam exam, Pane ca, HelloApplication app) {
        Stage st = new Stage();
        st.initModality(Modality.APPLICATION_MODAL);
        st.setTitle("Set Exam Schedule");

        VBox root = new VBox(0);
        root.setStyle("-fx-background-color:" + UIUtils.bgCard() + ";");
        root.setPrefWidth(460);

        // ── Coloured header band ──────────────────────────
        VBox header = new VBox(4);
        header.setPadding(new Insets(20, 24, 20, 24));
        header.setStyle("-fx-background-color:" + UIUtils.BG_DARK + ";");
        Label titleL = new Label("📅  Schedule Exam");
        titleL.setStyle("-fx-font-size:18px;-fx-font-weight:bold;-fx-text-fill:white;");
        String displayTitle = (exam.getTitle() != null && !exam.getTitle().isEmpty()) ? exam.getTitle() : exam.getSubject();
        Label subL = new Label(displayTitle + "  ·  " + exam.getDuration() + " min");
        subL.setStyle("-fx-font-size:13px;-fx-text-fill:#64748b;");
        header.getChildren().addAll(titleL, subL);

        // ── Body ──────────────────────────────────────────
        VBox body = new VBox(16);
        body.setPadding(new Insets(22, 24, 24, 24));

        // Warning banner
        Label ruleL = new Label("⚠ Exam auto-starts at the chosen time.");
        ruleL.setStyle("-fx-font-size:12px;-fx-text-fill:" + UIUtils.ACCENT_ORG
                + ";-fx-background-color:" + (UIUtils.darkMode ? "#431407" : "#fff7ed") + ";-fx-background-radius:8;-fx-padding:8 12;");
        ruleL.setWrapText(true);

        // ── Helper: build a date + time input row ─────────
        // Time row: DatePicker | [HH] : [MM] | AM/PM toggle
        DatePicker startDate = buildDatePicker(java.time.LocalDate.now().plusDays(1));
        TextField  startHH   = buildTimeField("09");
        TextField  startMM   = buildTimeField("00");
        ToggleButton startAmPm = buildAmPmToggle();

        DatePicker endDate = buildDatePicker(java.time.LocalDate.now().plusDays(1));
        TextField  endHH   = buildTimeField("10");
        TextField  endMM   = buildTimeField("00");
        ToggleButton endAmPm = buildAmPmToggle();

        Label startHdr = schedSectionLabel("🟢  Start");
        Label endHdr   = schedSectionLabel("🔴  End");

        HBox startRow = buildTimeInputRow(startDate, startHH, startMM, startAmPm);
        HBox endRow   = buildTimeInputRow(endDate,   endHH,   endMM,   endAmPm);

        // ── Duration preview ──────────────────────────────
        Label durationPreview = new Label();
        durationPreview.setStyle("-fx-font-size:12px;-fx-font-weight:bold;");

        Runnable updatePreview = () -> {
            try {
                java.time.LocalDateTime s  = buildLDT(startDate, startHH, startMM, startAmPm);
                java.time.LocalDateTime e2 = buildLDT(endDate,   endHH,   endMM,   endAmPm);
                long mins = java.time.Duration.between(s, e2).toMinutes();
                int examMins = Integer.parseInt(exam.getDuration());
                if (mins <= 0) {
                    durationPreview.setText("❌  End must be after Start");
                    durationPreview.setStyle("-fx-font-size:12px;-fx-font-weight:bold;-fx-text-fill:" + UIUtils.ACCENT_RED + ";");
                } else if (mins < examMins) {
                    durationPreview.setText("❌  Window is " + mins + " min — needs ≥ " + examMins + " min");
                    durationPreview.setStyle("-fx-font-size:12px;-fx-font-weight:bold;-fx-text-fill:" + UIUtils.ACCENT_RED + ";");
                } else {
                    durationPreview.setText("✅  Window: " + mins + " min  (exam needs " + examMins + " min)");
                    durationPreview.setStyle("-fx-font-size:12px;-fx-font-weight:bold;-fx-text-fill:#16a34a;");
                }
            } catch (Exception ex) { durationPreview.setText(""); }
        };

        // Wire all inputs to update preview live
        javafx.beans.value.ChangeListener<Object> previewListener = (o, ov, nv) -> updatePreview.run();
        startDate.valueProperty().addListener(previewListener);
        startHH.textProperty().addListener(previewListener);
        startMM.textProperty().addListener(previewListener);
        startAmPm.selectedProperty().addListener(previewListener);
        endDate.valueProperty().addListener(previewListener);
        endHH.textProperty().addListener(previewListener);
        endMM.textProperty().addListener(previewListener);
        endAmPm.selectedProperty().addListener(previewListener);
        updatePreview.run();

        // Pre-fill if already scheduled
        if (exam.getScheduledStartMillis() > 0) {
            java.time.LocalDateTime existStart = java.time.LocalDateTime.ofInstant(
                    java.time.Instant.ofEpochMilli(exam.getScheduledStartMillis()), java.time.ZoneId.systemDefault());
            java.time.LocalDateTime existEnd = java.time.LocalDateTime.ofInstant(
                    java.time.Instant.ofEpochMilli(exam.getScheduledEndMillis()), java.time.ZoneId.systemDefault());
            startDate.setValue(existStart.toLocalDate());
            int sh = existStart.getHour();
            startAmPm.setSelected(sh >= 12);
            startHH.setText(String.format("%02d", sh % 12 == 0 ? 12 : sh % 12));
            startMM.setText(String.format("%02d", existStart.getMinute()));
            endDate.setValue(existEnd.toLocalDate());
            int eh = existEnd.getHour();
            endAmPm.setSelected(eh >= 12);
            endHH.setText(String.format("%02d", eh % 12 == 0 ? 12 : eh % 12));
            endMM.setText(String.format("%02d", existEnd.getMinute()));
            updatePreview.run();
        }

        // ── Buttons ───────────────────────────────────────
        Button btnConfirm = UIUtils.primaryBtn("📅", "Confirm Schedule", UIUtils.ACCENT_PURP);
        btnConfirm.setPrefWidth(Double.MAX_VALUE);
        btnConfirm.setPrefHeight(44);
        btnConfirm.setOnAction(ev -> {
            try {
                java.time.LocalDateTime startLDT = buildLDT(startDate, startHH, startMM, startAmPm);
                java.time.LocalDateTime endLDT   = buildLDT(endDate,   endHH,   endMM,   endAmPm);
                if (!endLDT.isAfter(startLDT)) { Toast.error(ca, "End time must be after start time"); return; }
                long windowMins = java.time.Duration.between(startLDT, endLDT).toMinutes();
                int examMins = Integer.parseInt(exam.getDuration());
                if (windowMins < examMins) { Toast.error(ca, "Window (" + windowMins + " min) must be ≥ exam duration (" + examMins + " min)"); return; }
                if (!startLDT.isAfter(java.time.LocalDateTime.now())) { Toast.error(ca, "Start time must be in the future"); return; }
                java.time.ZoneId zone = java.time.ZoneId.systemDefault();
                exam.setScheduledStartMillis(startLDT.atZone(zone).toInstant().toEpochMilli());
                exam.setScheduledEndMillis(endLDT.atZone(zone).toInstant().toEpochMilli());
                exam.generateCode();
                st.close();
                renderDashboardHome(ca, app);
                java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("dd MMM, hh:mm a");
                Toast.success(ca, "📅 " + displayTitle + " scheduled for " + startLDT.format(fmt));
            } catch (Exception ex) { Toast.error(ca, "Please enter valid time values (HH: 1–12, MM: 0–59)"); }
        });

        HBox bottomRow = new HBox(10);
        if (exam.getScheduledStartMillis() > 0) {
            Button btnClear = UIUtils.ghostBtn("✕", "Clear Schedule", UIUtils.ACCENT_RED);
            btnClear.setOnAction(ev -> {
                exam.setScheduledStartMillis(0); exam.setScheduledEndMillis(0);
                st.close(); renderDashboardHome(ca, app);
                Toast.info(ca, "Schedule cleared from " + displayTitle);
            });
            bottomRow.getChildren().addAll(btnConfirm, btnClear);
        } else {
            bottomRow.getChildren().add(btnConfirm);
        }
        HBox.setHgrow(btnConfirm, Priority.ALWAYS);

        body.getChildren().addAll(
                ruleL,
                startHdr, startRow,
                endHdr, endRow,
                durationPreview,
                UIUtils.divider(),
                bottomRow
        );

        root.getChildren().addAll(header, body);
        st.setScene(new Scene(root, 460, 400));
        UIUtils.applyStyle(st.getScene());
        st.show();
    }

    /** Styled DatePicker for the schedule popup. */
    private static DatePicker buildDatePicker(java.time.LocalDate initial) {
        DatePicker dp = new DatePicker(initial);
        dp.setStyle("-fx-font-size:13px;-fx-pref-height:40px;");
        dp.setPrefWidth(160);
        return dp;
    }

    /** Editable 2-digit time field (HH or MM). */
    private static TextField buildTimeField(String initial) {
        TextField tf = new TextField(initial);
        tf.setPrefWidth(52);
        tf.setPrefHeight(40);
        tf.setStyle(
                "-fx-font-family:Monospaced;-fx-font-size:16px;-fx-font-weight:bold;"
                        + "-fx-alignment:center;-fx-background-color:" + UIUtils.bgSurface() + ";"
                        + "-fx-border-color:" + UIUtils.border() + ";-fx-border-radius:8;-fx-background-radius:8;"
                        + "-fx-padding:6 4;"
        );
        // Auto-format on focus lost: clamp and zero-pad
        tf.focusedProperty().addListener((obs, wasFocused, isNow) -> {
            if (!isNow) {
                try {
                    int v = Integer.parseInt(tf.getText().trim());
                    // For hour fields (max 12 when 12h) or minute fields (max 59) — just clamp
                    if (tf.getPrefWidth() == 52) { // all our time fields are 52 wide
                        // will be validated properly at confirm time
                        tf.setText(String.format("%02d", Math.max(0, Math.min(v, 59))));
                    }
                } catch (NumberFormatException ex) { /* leave it for confirm-time error */ }
            }
        });
        // Allow only digits, max 2 chars
        tf.textProperty().addListener((obs, ov, nv) -> {
            if (!nv.matches("\\d{0,2}")) tf.setText(ov);
        });
        return tf;
    }

    /** AM / PM toggle button. Selected = PM. */
    private static ToggleButton buildAmPmToggle() {
        ToggleButton tb = new ToggleButton("AM");
        tb.setPrefWidth(56); tb.setPrefHeight(40);
        tb.setStyle(
                "-fx-background-color:" + UIUtils.ACCENT_BLUE + "22;-fx-text-fill:" + UIUtils.ACCENT_BLUE + ";"
                        + "-fx-font-weight:bold;-fx-font-size:13px;-fx-background-radius:8;"
                        + "-fx-border-color:" + UIUtils.ACCENT_BLUE + ";-fx-border-radius:8;-fx-cursor:hand;"
        );
        tb.selectedProperty().addListener((obs, wasOn, isOn) -> {
            tb.setText(isOn ? "PM" : "AM");
            if (isOn) {
                tb.setStyle("-fx-background-color:" + UIUtils.ACCENT_PURP + ";-fx-text-fill:white;"
                        + "-fx-font-weight:bold;-fx-font-size:13px;-fx-background-radius:8;"
                        + "-fx-border-color:" + UIUtils.ACCENT_PURP + ";-fx-border-radius:8;-fx-cursor:hand;");
            } else {
                tb.setStyle("-fx-background-color:" + UIUtils.ACCENT_BLUE + "22;-fx-text-fill:" + UIUtils.ACCENT_BLUE + ";"
                        + "-fx-font-weight:bold;-fx-font-size:13px;-fx-background-radius:8;"
                        + "-fx-border-color:" + UIUtils.ACCENT_BLUE + ";-fx-border-radius:8;-fx-cursor:hand;");
            }
        });
        return tb;
    }

    /** Assemble DatePicker + HH : MM + AM/PM into a neat HBox. */
    private static HBox buildTimeInputRow(DatePicker dp, TextField hh, TextField mm, ToggleButton amPm) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        Label colon = new Label(":");
        colon.setStyle("-fx-font-size:20px;-fx-font-weight:bold;-fx-text-fill:" + UIUtils.textDark() + ";");
        row.getChildren().addAll(dp, hh, colon, mm, amPm);
        return row;
    }

    /** Section label for schedule popup. */
    private static Label schedSectionLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:" + UIUtils.textMid() + ";");
        return l;
    }

    /** Build LocalDateTime from DatePicker + text HH, MM fields + AM/PM toggle. */
    private static java.time.LocalDateTime buildLDT(
            DatePicker dp, TextField hhF, TextField mmF, ToggleButton amPm) {
        int h = Integer.parseInt(hhF.getText().trim());
        int m = Integer.parseInt(mmF.getText().trim());
        if (h < 1 || h > 12) throw new IllegalArgumentException("Hour out of range");
        if (m < 0 || m > 59) throw new IllegalArgumentException("Minute out of range");
        boolean isPm = amPm.isSelected();
        if (isPm && h != 12) h += 12;
        if (!isPm && h == 12) h = 0;
        return java.time.LocalDateTime.of(dp.getValue(), java.time.LocalTime.of(h, m));
    }

    /** Build a LocalDateTime from a DatePicker + hour/min/ampm combos (legacy, kept for safety). */
    private static java.time.LocalDateTime buildLocalDateTime(
            DatePicker dp, ComboBox<String> hour, ComboBox<String> min, ComboBox<String> amPm) {
        int h = Integer.parseInt(hour.getValue());
        int m = Integer.parseInt(min.getValue());
        boolean isPm = "PM".equals(amPm.getValue());
        if (isPm && h != 12) h += 12;
        if (!isPm && h == 12) h = 0;
        return java.time.LocalDateTime.of(dp.getValue(), java.time.LocalTime.of(h, m));
    }
    static class ExamEditor {
        // Persistent state across sub-nav
        private static String  sSub  = null;
        private static Integer sGrd  = null;
        private static String  sMrk  = "";
        private static String  sDur  = "";
        private static String  sTitle = "";
        private static String  sDesc  = "";
        private static double  selTotal = 0;
        private static Label   markLbl;
        private static VBox    qList;
        private static HashMap<Question, Double> sel = new HashMap<>();
        static Exam    editing = null;

        static void show(Pane ca, HelloApplication app) {
            ca.getChildren().clear();

            ScrollPane sp = new ScrollPane();
            sp.prefWidthProperty().bind(ca.widthProperty());
            sp.prefHeightProperty().bind(ca.heightProperty());
            sp.setStyle("-fx-background:transparent;-fx-background-color:transparent;");
            sp.setFitToWidth(true);

            VBox page = new VBox(18);
            page.setPadding(new Insets(28, 30, 30, 30));

            Label title = UIUtils.heading(editing == null ? "📝  Create New Exam" : "✏  Edit Exam");
            Label sub2  = UIUtils.subheading("Fill in exam details and select questions from the question bank");

            // ── Title & Description ───────────────────────────
            Label tlbl = sectionLabel("Exam Title");
            TextField fTitle = UIUtils.styledField("e.g. Mid-Term Physics Test");
            fTitle.setText(sTitle);
            fTitle.setOnKeyReleased(e -> { sTitle = fTitle.getText(); clearFieldError(fTitle); });

            Label dlbl = sectionLabel("Description (optional)");
            TextArea fDesc = new TextArea(sDesc);
            fDesc.setPromptText("Brief description of this exam...");
            fDesc.setPrefHeight(64); fDesc.setWrapText(true);
            fDesc.setStyle("-fx-font-size:13px;-fx-background-radius:8;-fx-border-color:" + UIUtils.border() + ";-fx-background-color:" + UIUtils.bgSurface() + ";-fx-text-fill:" + UIUtils.textDark() + ";-fx-control-inner-background:" + UIUtils.bgSurface() + ";");
            fDesc.setOnKeyReleased(e -> sDesc = fDesc.getText());

            // ── Config row ────────────────────────────────────
            Label cfglbl = sectionLabel("Exam Configuration");
            HBox cfg = new HBox(14); cfg.setAlignment(Pos.CENTER_LEFT);

            ComboBox<String>  cbSub = new ComboBox<>();
            cbSub.getItems().addAll("Physics", "Chemistry", "Math", "Biology", "English");
            cbSub.setPromptText("📚 Subject"); cbSub.setStyle("-fx-font-size:14px;-fx-pref-height:42px;");
            if (sSub != null) cbSub.setValue(sSub);

            ComboBox<Integer> cbGrd = new ComboBox<>();
            for (int i = 6; i <= 12; i++) cbGrd.getItems().add(i);
            cbGrd.setPromptText("🎓 Class"); cbGrd.setStyle("-fx-font-size:14px;-fx-pref-height:42px;");
            if (sGrd != null) cbGrd.setValue(sGrd);

            TextField fMark = UIUtils.styledField("Total Marks"); fMark.setPrefWidth(120);
            if (!sMrk.isEmpty()) fMark.setText(sMrk);
            TextField fDur  = UIUtils.styledField("Duration (mins)"); fDur.setPrefWidth(140);
            if (!sDur.isEmpty()) fDur.setText(sDur);

            // Inline validation: digits only for marks + duration
            fMark.setOnKeyReleased(e -> { sMrk = fMark.getText(); updateMark(); clearFieldError(fMark); });
            fDur.setOnKeyReleased(e  -> { sDur = fDur.getText(); clearFieldError(fDur); });

            cfg.getChildren().addAll(cbSub, cbGrd, fMark, fDur);

            // ── Mark tracker ──────────────────────────────────
            markLbl = new Label("Selected: 0 / 0 Marks");
            markLbl.setStyle("-fx-font-weight:bold;-fx-text-fill:" + UIUtils.ACCENT_ORG + ";-fx-font-size:13px;");

            // ── Question list ─────────────────────────────────
            Label qHdr = sectionLabel("📋  Available Questions");
            qList = new VBox(10);

            if (sSub != null && sGrd != null) refreshList(sSub, sGrd, app);

            cbSub.setOnAction(e -> { sSub = cbSub.getValue(); clearComboError(cbSub); sel.clear(); refreshList(sSub, sGrd, app); });
            cbGrd.setOnAction(e -> { sGrd = cbGrd.getValue(); clearComboError(cbGrd); sel.clear(); refreshList(sSub, sGrd, app); });

            // ── Action buttons ────────────────────────────────
            HBox actions = new HBox(14);
            Button btnMore   = UIUtils.ghostBtn("➕", "Add More Questions", UIUtils.ACCENT_BLUE);
            Button btnCreate = UIUtils.primaryBtn("✅", editing == null ? "Create Exam" : "Save Changes", UIUtils.ACCENT_GREEN);
            Button btnCancel = UIUtils.ghostBtn("✕", "Cancel", UIUtils.TEXT_MID);

            btnMore.setOnAction(e -> QuestionEditor.show(ca, app, cbSub.getValue(), cbGrd.getValue()));
            btnCreate.setOnAction(e -> handleCreate(app, ca, cbSub, cbGrd, fMark, fDur));
            btnCancel.setOnAction(e -> { clearState(); editing = null; renderDashboardHome(ca, app); });
            actions.getChildren().addAll(btnMore, btnCreate, btnCancel);

            page.getChildren().addAll(
                    title, sub2, UIUtils.divider(),
                    tlbl, fTitle,
                    dlbl, fDesc,
                    cfglbl, cfg,
                    UIUtils.divider(),
                    markLbl, qHdr, qList,
                    UIUtils.divider(), actions
            );
            sp.setContent(page);
            ca.getChildren().add(sp);
            if (ca instanceof javafx.scene.layout.AnchorPane ap) {
                javafx.scene.layout.AnchorPane.setTopAnchor(sp, 0.0);
                javafx.scene.layout.AnchorPane.setBottomAnchor(sp, 0.0);
                javafx.scene.layout.AnchorPane.setLeftAnchor(sp, 0.0);
                javafx.scene.layout.AnchorPane.setRightAnchor(sp, 0.0);
            }
            updateMark();
            UIUtils.slideIn(page, true);
        }

        static void loadForEditing(Exam exam, Pane ca, HelloApplication app) {
            editing = exam;
            sSub   = exam.getSubject();
            sGrd   = exam.getGrade();
            sMrk   = String.valueOf(exam.getTotalMarks());
            sDur   = exam.getDuration();
            sTitle = exam.getTitle() != null ? exam.getTitle() : "";
            sDesc  = exam.getDescription() != null ? exam.getDescription() : "";
            sel.clear();
            sel.putAll(exam.getQuestionsMap());
            show(ca, app);
        }

        private static Label sectionLabel(String text) {
            Label l = new Label(text);
            l.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:" + UIUtils.textMid() + ";");
            return l;
        }

        private static void refreshList(String sub, Integer cls, HelloApplication app) {
            if (qList == null) return;
            qList.getChildren().clear();
            if (sub == null || cls == null) return;

            for (Question q : QuestionBank.allQuestions) {
                if (!q.getSubject().equals(sub) || q.getGrade() != cls) continue;

                VBox card = UIUtils.card(720);
                card.setPadding(new Insets(14));

                HBox top = new HBox(12); top.setAlignment(Pos.CENTER_LEFT);
                CheckBox cb = new CheckBox();
                Label qText = new Label(q.getQuestionText());
                qText.setStyle("-fx-font-size:14px;-fx-font-weight:bold;-fx-text-fill:" + UIUtils.textDark() + ";");
                qText.setWrapText(true); qText.setMaxWidth(400);

                TextField fMk = UIUtils.styledField("Marks"); fMk.setPrefWidth(80);

                if (sel.containsKey(q)) { cb.setSelected(true); fMk.setText(String.valueOf(sel.get(q))); }
                else if (q == QuestionEditor.lastAdded) {
                    cb.setSelected(true); sel.put(q, 0.0); QuestionEditor.lastAdded = null;
                } else fMk.setDisable(true);

                String type = (q instanceof MCQ) ? "MCQ" : (q instanceof TextQuestion) ? "Text" : "Range";
                String tColor = (q instanceof MCQ) ? UIUtils.ACCENT_BLUE : (q instanceof TextQuestion) ? UIUtils.ACCENT_GREEN : UIUtils.ACCENT_PURP;
                Label badge = UIUtils.badge(type, tColor);

                MenuButton qMenu = new MenuButton("⋯");
                qMenu.setStyle(
                        "-fx-background-color:transparent;-fx-text-fill:" + UIUtils.textMid() + ";" +
                                "-fx-font-size:15px;-fx-font-weight:bold;-fx-background-radius:8;" +
                                "-fx-border-color:" + UIUtils.border() + ";-fx-border-radius:8;" +
                                "-fx-border-width:1;-fx-padding:3 8;-fx-cursor:hand;"
                );
                qMenu.setMinWidth(MenuButton.USE_PREF_SIZE);
                qMenu.setOnMouseEntered(ev -> qMenu.setStyle(qMenu.getStyle()
                        .replace("-fx-background-color:transparent;", "-fx-background-color:" + UIUtils.bgContent() + ";")));
                qMenu.setOnMouseExited(ev -> qMenu.setStyle(qMenu.getStyle()
                        .replace("-fx-background-color:" + UIUtils.bgContent() + ";", "-fx-background-color:transparent;")));
                MenuItem miEdit = new MenuItem("✏   Edit");
                miEdit.setStyle("-fx-font-size:13px;");
                miEdit.setOnAction(e -> showEditPopup(q, app));
                MenuItem miDel = new MenuItem("🗑   Delete");
                miDel.setStyle("-fx-font-size:13px;-fx-text-fill:" + UIUtils.ACCENT_RED + ";");
                miDel.setOnAction(e -> showDeleteConfirm(q, app, sub, cls));
                qMenu.getItems().addAll(miEdit, new SeparatorMenuItem(), miDel);
                top.getChildren().addAll(cb, badge, qText, fMk, qMenu);

                VBox ansBox = new VBox(4); ansBox.setPadding(new Insets(6, 0, 0, 26));
                renderAnswerPreview(q, ansBox);
                card.getChildren().addAll(top, ansBox);
                qList.getChildren().add(card);

                cb.setOnAction(e -> {
                    fMk.setDisable(!cb.isSelected());
                    if (!cb.isSelected()) sel.remove(q);
                    else sel.putIfAbsent(q, 0.0);
                    updateMark();
                });
                fMk.setOnKeyReleased(e -> {
                    try { sel.put(q, fMk.getText().isEmpty() ? 0 : Double.parseDouble(fMk.getText())); }
                    catch (Exception ex) { sel.put(q, 0.0); }
                    updateMark();
                });
            }
            updateMark();
        }

        private static void renderAnswerPreview(Question q, VBox c) {
            c.getChildren().clear();
            if (q instanceof MCQ) {
                MCQ m = (MCQ) q;
                for (int i = 0; i < 4; i++) {
                    Label l = new Label((i+1) + ". " + m.getOptions()[i]);
                    l.setStyle(i == m.getCorrectIndex()
                            ? "-fx-text-fill:#16a34a;-fx-font-weight:bold;-fx-font-size:12px;"
                            : "-fx-text-fill:" + UIUtils.textMid() + ";-fx-font-size:12px;");
                    c.getChildren().add(l);
                }
            } else if (q instanceof TextQuestion) {
                c.getChildren().add(new Label("✅ Answer: " + ((TextQuestion)q).getAnswer()) {{
                    setStyle("-fx-text-fill:#16a34a;-fx-font-size:12px;-fx-font-weight:bold;"); }});
            } else if (q instanceof RangeQuestion) {
                RangeQuestion r = (RangeQuestion) q;
                c.getChildren().add(new Label("✅ Range: " + r.getMin() + " – " + r.getMax()) {{
                    setStyle("-fx-text-fill:#16a34a;-fx-font-size:12px;-fx-font-weight:bold;"); }});
            }
        }

        private static void showEditPopup(Question q, HelloApplication app) {
            Stage st = new Stage(); st.initModality(Modality.APPLICATION_MODAL); st.setTitle("Edit Question");
            VBox layout = new VBox(14); layout.setPadding(new Insets(24)); layout.setStyle("-fx-background-color:" + UIUtils.bgCard() + ";");
            Label lbl = new Label("✏  Edit Question");
            lbl.setStyle("-fx-font-size:18px;-fx-font-weight:bold;-fx-text-fill:" + UIUtils.textDark() + ";");
            TextArea txt = new TextArea(q.getQuestionText());
            txt.setWrapText(true); txt.setPrefHeight(80);
            txt.setStyle("-fx-font-size:14px;-fx-background-radius:8;-fx-border-color:" + UIUtils.border() + ";-fx-background-color:" + UIUtils.bgSurface() + ";-fx-text-fill:" + UIUtils.textDark() + ";-fx-control-inner-background:" + UIUtils.bgSurface() + ";");

            TextField[] mcqF = new TextField[4]; ToggleGroup tg = new ToggleGroup(); RadioButton[] rbs = new RadioButton[4];
            TextField tAns = UIUtils.styledField("Answer"); tAns.setPrefWidth(200);
            TextField tMin = UIUtils.styledField("Min"); tMin.setPrefWidth(100);
            TextField tMax = UIUtils.styledField("Max"); tMax.setPrefWidth(100);
            layout.getChildren().addAll(lbl, new Label("Question:") {{ setStyle("-fx-font-weight:bold;"); }}, txt);

            if (q instanceof MCQ) {
                MCQ m = (MCQ) q;
                layout.getChildren().add(new Label("Options (select correct):") {{ setStyle("-fx-font-weight:bold;"); }});
                for (int i = 0; i < 4; i++) {
                    HBox r = new HBox(10); r.setAlignment(Pos.CENTER_LEFT);
                    rbs[i] = new RadioButton(); rbs[i].setToggleGroup(tg);
                    mcqF[i] = UIUtils.styledField("Option " + (i+1)); mcqF[i].setPrefWidth(250);
                    mcqF[i].setText(m.getOptions()[i]);
                    if (i == m.getCorrectIndex()) rbs[i].setSelected(true);
                    r.getChildren().addAll(rbs[i], mcqF[i]);
                    layout.getChildren().add(r);
                }
            } else if (q instanceof TextQuestion) {
                tAns.setText(String.valueOf(((TextQuestion)q).getAnswer()));
                layout.getChildren().addAll(new Label("Answer:") {{ setStyle("-fx-font-weight:bold;"); }}, tAns);
            } else if (q instanceof RangeQuestion) {
                RangeQuestion rq = (RangeQuestion) q;
                tMin.setText(String.valueOf(rq.getMin())); tMax.setText(String.valueOf(rq.getMax()));
                layout.getChildren().addAll(new Label("Min:") {{ setStyle("-fx-font-weight:bold;"); }}, tMin,
                        new Label("Max:") {{ setStyle("-fx-font-weight:bold;"); }}, tMax);
            }

            Button btnSave = UIUtils.primaryBtn("✅", "Apply Changes", UIUtils.ACCENT_GREEN);
            btnSave.setOnAction(e -> {
                try {
                    q.setQuestionText(txt.getText());
                    if (q instanceof MCQ) {
                        MCQ m = (MCQ)q; String[] opts = new String[4];
                        for (int i = 0; i < 4; i++) opts[i] = mcqF[i].getText();
                        m.setOptions(opts); m.setCorrectIndex(tg.getToggles().indexOf(tg.getSelectedToggle()));
                    } else if (q instanceof TextQuestion)
                        ((TextQuestion)q).setAnswer(Double.parseDouble(tAns.getText()));
                    else if (q instanceof RangeQuestion) {
                        double mn = Double.parseDouble(tMin.getText());
                        double mx = Double.parseDouble(tMax.getText());
                        if (mn >= mx) { app.showError("Invalid Range", "Min must be strictly less than Max."); return; }
                        ((RangeQuestion)q).setMin(mn);
                        ((RangeQuestion)q).setMax(mx);
                    }
                    st.close(); refreshList(sSub, sGrd, app);
                } catch (Exception ex) { app.showError("Error", "Invalid values."); }
            });
            layout.getChildren().add(btnSave);
            st.setScene(new Scene(new ScrollPane(layout) {{ setFitToWidth(true); setStyle("-fx-background:white;"); }}, 420, 500));
            st.showAndWait();
        }

        /** Public entry point so QuestionBankBrowser can call edit with an optional onSave callback. */
        static void showEditPopupPublic(Question q, HelloApplication app, Runnable onSave) {
            Stage st = new Stage(); st.initModality(Modality.APPLICATION_MODAL); st.setTitle("Edit Question");
            VBox layout = new VBox(14); layout.setPadding(new Insets(24)); layout.setStyle("-fx-background-color:" + UIUtils.bgCard() + ";");
            Label lbl = new Label("✏  Edit Question");
            lbl.setStyle("-fx-font-size:18px;-fx-font-weight:bold;-fx-text-fill:" + UIUtils.textDark() + ";");
            TextArea txt = new TextArea(q.getQuestionText());
            txt.setWrapText(true); txt.setPrefHeight(80);
            txt.setStyle("-fx-font-size:14px;-fx-background-radius:8;-fx-border-color:" + UIUtils.border() + ";-fx-background-color:" + UIUtils.bgSurface() + ";-fx-text-fill:" + UIUtils.textDark() + ";-fx-control-inner-background:" + UIUtils.bgSurface() + ";");

            TextField[] mcqF = new TextField[4]; ToggleGroup tg = new ToggleGroup(); RadioButton[] rbs = new RadioButton[4];
            TextField tAns = UIUtils.styledField("Answer"); tAns.setPrefWidth(200);
            TextField tMin = UIUtils.styledField("Min"); tMin.setPrefWidth(100);
            TextField tMax = UIUtils.styledField("Max"); tMax.setPrefWidth(100);
            layout.getChildren().addAll(lbl, new Label("Question:") {{ setStyle("-fx-font-weight:bold;"); }}, txt);

            if (q instanceof MCQ) {
                MCQ m = (MCQ) q;
                layout.getChildren().add(new Label("Options (select correct):") {{ setStyle("-fx-font-weight:bold;"); }});
                for (int i = 0; i < 4; i++) {
                    HBox r = new HBox(10); r.setAlignment(Pos.CENTER_LEFT);
                    rbs[i] = new RadioButton(); rbs[i].setToggleGroup(tg);
                    mcqF[i] = UIUtils.styledField("Option " + (i+1)); mcqF[i].setPrefWidth(250);
                    mcqF[i].setText(m.getOptions()[i]);
                    if (i == m.getCorrectIndex()) rbs[i].setSelected(true);
                    r.getChildren().addAll(rbs[i], mcqF[i]);
                    layout.getChildren().add(r);
                }
            } else if (q instanceof TextQuestion) {
                tAns.setText(String.valueOf(((TextQuestion)q).getAnswer()));
                layout.getChildren().addAll(new Label("Answer:") {{ setStyle("-fx-font-weight:bold;"); }}, tAns);
            } else if (q instanceof RangeQuestion) {
                RangeQuestion rq = (RangeQuestion) q;
                tMin.setText(String.valueOf(rq.getMin())); tMax.setText(String.valueOf(rq.getMax()));
                layout.getChildren().addAll(new Label("Min:") {{ setStyle("-fx-font-weight:bold;"); }}, tMin,
                        new Label("Max:") {{ setStyle("-fx-font-weight:bold;"); }}, tMax);
            }

            Button btnSave2 = UIUtils.primaryBtn("✅", "Apply Changes", UIUtils.ACCENT_GREEN);
            btnSave2.setOnAction(e -> {
                try {
                    q.setQuestionText(txt.getText().trim());
                    if (q instanceof MCQ) {
                        MCQ m = (MCQ)q; String[] opts = new String[4];
                        for (int i = 0; i < 4; i++) opts[i] = mcqF[i].getText();
                        m.setOptions(opts); m.setCorrectIndex(tg.getToggles().indexOf(tg.getSelectedToggle()));
                    } else if (q instanceof TextQuestion)
                        ((TextQuestion)q).setAnswer(Double.parseDouble(tAns.getText()));
                    else if (q instanceof RangeQuestion) {
                        double mn = Double.parseDouble(tMin.getText());
                        double mx = Double.parseDouble(tMax.getText());
                        if (mn >= mx) { app.showError("Invalid Range", "Min must be strictly less than Max."); return; }
                        ((RangeQuestion)q).setMin(mn);
                        ((RangeQuestion)q).setMax(mx);
                    }
                    st.close();
                    if (onSave != null) onSave.run();
                } catch (Exception ex) { app.showError("Error", "Invalid values."); }
            });
            layout.getChildren().add(btnSave2);
            st.setScene(new Scene(new ScrollPane(layout) {{ setFitToWidth(true); setStyle("-fx-background:white;"); }}, 420, 500));
            st.showAndWait();
        }

        private static void showDeleteConfirm(Question q, HelloApplication app, String sub, Integer cls) {
            Stage st = new Stage(); st.initModality(Modality.APPLICATION_MODAL); st.setTitle("Confirm Delete");
            VBox layout = new VBox(16); layout.setPadding(new Insets(28)); layout.setAlignment(Pos.CENTER);
            layout.setStyle("-fx-background-color:" + UIUtils.bgCard() + ";");
            Label warn = new Label("🗑  Delete this question?");
            warn.setStyle("-fx-font-size:17px;-fx-font-weight:bold;-fx-text-fill:" + UIUtils.ACCENT_RED + ";");
            Label prev = new Label("\"" + q.getQuestionText() + "\"");
            prev.setWrapText(true); prev.setMaxWidth(300);
            prev.setStyle("-fx-font-style:italic;-fx-text-fill:" + UIUtils.textMid() + ";-fx-font-size:13px;");
            HBox btns = new HBox(14); btns.setAlignment(Pos.CENTER);
            Button yes = UIUtils.primaryBtn("🗑", "Delete", UIUtils.ACCENT_RED);
            Button no  = UIUtils.ghostBtn("←", "Cancel", UIUtils.TEXT_MID);
            yes.setOnAction(e -> { QuestionBank.allQuestions.remove(q); sel.remove(q); st.close(); refreshList(sub, cls, app); });
            no.setOnAction(e  -> st.close());
            btns.getChildren().addAll(yes, no);
            layout.getChildren().addAll(warn, prev, btns);
            st.setScene(new Scene(layout, 360, 200)); st.showAndWait();
        }

        // ── Inline validation helpers (#13) ──────────────────
        private static void markFieldError(TextField f) {
            f.setStyle(f.getStyle().replace("-fx-border-color:" + UIUtils.BORDER, "-fx-border-color:#ef4444")
                    + "-fx-border-color:#ef4444;-fx-border-width:1.5;");
        }
        private static void clearFieldError(TextField f) {
            String s = f.getStyle()
                    .replace("-fx-border-color:#ef4444;-fx-border-width:1.5;", "")
                    .replace("-fx-border-color:#ef4444", "-fx-border-color:" + UIUtils.BORDER);
            f.setStyle(s);
        }
        private static void markComboError(ComboBox<?> c) {
            c.setStyle(c.getStyle() + "-fx-border-color:#ef4444;-fx-border-width:1.5;-fx-border-radius:8;");
        }
        private static void clearComboError(ComboBox<?> c) {
            c.setStyle(c.getStyle()
                    .replace("-fx-border-color:#ef4444;-fx-border-width:1.5;-fx-border-radius:8;", ""));
        }

        private static void handleCreate(HelloApplication app, Pane ca,
                                         ComboBox<String> cbSub, ComboBox<Integer> cbGrd,
                                         TextField fMark, TextField fDur) {
            boolean valid = true;
            if (sSub == null) { markComboError(cbSub); valid = false; }
            if (sGrd == null) { markComboError(cbGrd); valid = false; }
            if (sMrk.isEmpty()) { markFieldError(fMark); valid = false; }
            if (sDur.isEmpty()) { markFieldError(fDur);  valid = false; }
            if (!valid) {
                Toast.error(ca, "Please fill in all required fields highlighted in red");
                return;
            }
            if (sel.isEmpty()) {
                Toast.error(ca, "Select at least one question before saving");
                return;
            }
            try {
                double target = Double.parseDouble(sMrk);
                if (Math.abs(selTotal - target) > 0.01) {
                    markFieldError(fMark);
                    Toast.error(ca, "Assigned marks (" + selTotal + ") ≠ total marks (" + target + ")");
                    return;
                }
                Exam updated = new Exam(sSub, sGrd, target, sDur, new HashMap<>(sel));
                updated.setTitle(sTitle);
                updated.setDescription(sDesc);
                updated.setScheduleDetails("Scheduled");

                if (editing != null) {
                    updated.setLive(editing.isLive());
                    updated.setScheduleDetails(editing.getScheduleDetails());
                    updated.setLiveWindow(editing.getLiveWindow());
                    updated.setLiveEndMillis(editing.getLiveEndMillis());
                    updated.setExamCode(editing.getExamCode());
                    int idx = ExamBank.allExams.indexOf(editing);
                    if (idx != -1) ExamBank.allExams.set(idx, updated);
                    editing = null;
                    clearState();
                    renderDashboardHome(ca, app);
                    Toast.success(ca, "Exam updated successfully!");
                } else {
                    ExamBank.allExams.add(0, updated);
                    clearState();
                    renderDashboardHome(ca, app);
                    Toast.success(ca, "Exam created and added to Scheduled Exams!");
                }
            } catch (Exception ex) {
                markFieldError(fMark);
                Toast.error(ca, "Check marks format — must be a valid number");
            }
        }

        private static void handleCreate(HelloApplication app, Pane ca) {
            // Legacy overload (kept for safety; ExamEditor.show now calls 4-arg version)
            if (sSub == null || sGrd == null || sMrk.isEmpty() || sDur.isEmpty()) {
                app.showError("Missing Fields", "Please fill in Subject, Class, Marks, and Duration."); return;
            }
            handleCreate(app, ca, null, null, null, null);
        }

        private static void updateMark() {
            selTotal = sel.values().stream().mapToDouble(Double::doubleValue).sum();
            if (markLbl != null)
                markLbl.setText("Selected: " + selTotal + " / " + (sMrk.isEmpty() ? "?" : sMrk) + " Marks");
        }

        static void clearState() {
            sSub = null; sGrd = null; sMrk = ""; sDur = ""; sTitle = ""; sDesc = "";
            sel.clear(); selTotal = 0;
        }
    }

    // ╔══════════════════════════════════════════════════════╗
    //  6. QUESTION EDITOR
    // ╚══════════════════════════════════════════════════════╝
    static class QuestionEditor {
        static Question lastAdded = null;
        private static TextArea    qArea;
        private static TextField[] mcqOpts = new TextField[4];
        private static ToggleGroup mcqTG;
        private static TextField   exactF, minF, maxF;
        private static ComboBox<String> cbAnsType;
        private static boolean returnToExam = false;

        static void show(Pane ca, HelloApplication app, String initSub, Integer initGrd) {
            ca.getChildren().clear();
            returnToExam = (initSub != null);

            ScrollPane sp = new ScrollPane();
            sp.prefWidthProperty().bind(ca.widthProperty());
            sp.prefHeightProperty().bind(ca.heightProperty());
            sp.setStyle("-fx-background:transparent;-fx-background-color:transparent;");
            sp.setFitToWidth(true);

            VBox page = new VBox(20);
            page.setPadding(new Insets(28, 30, 30, 30));

            Label title = UIUtils.heading("➕  Add New Question");
            Label sub2  = UIUtils.subheading("Add MCQ, text, or range questions to the question bank");

            HBox cfg = new HBox(14); cfg.setAlignment(Pos.CENTER_LEFT);
            ComboBox<String>  cbSub = new ComboBox<>();
            cbSub.getItems().addAll("Physics", "Chemistry", "Math", "Biology", "English");
            cbSub.setPromptText("📚 Subject"); cbSub.setStyle("-fx-font-size:14px;-fx-pref-height:42px;");
            ComboBox<Integer> cbGrd = new ComboBox<>();
            for (int i = 6; i <= 12; i++) cbGrd.getItems().add(i);
            cbGrd.setPromptText("🎓 Class"); cbGrd.setStyle("-fx-font-size:14px;-fx-pref-height:42px;");
            if (initSub != null) { cbSub.setValue(initSub); cbGrd.setValue(initGrd); }

            ToggleGroup tg = new ToggleGroup();
            RadioButton rbMcq  = new RadioButton("📊  MCQ");  rbMcq.setToggleGroup(tg);  rbMcq.setSelected(true);
            rbMcq.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:white;"
                    + "-fx-background-color:" + UIUtils.ACCENT_BLUE + ";-fx-background-radius:20;"
                    + "-fx-padding:6 18;-fx-cursor:hand;");
            RadioButton rbText = new RadioButton("✍  Text/Numeric"); rbText.setToggleGroup(tg);
            rbText.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:" + UIUtils.ACCENT_PURP + ";"
                    + "-fx-background-color:" + (UIUtils.darkMode ? "#3b1f6e" : "#f3e8ff") + ";-fx-background-radius:20;"
                    + "-fx-padding:6 18;-fx-cursor:hand;");
            // Toggle pill appearance on selection
            tg.selectedToggleProperty().addListener((obs, oldT, newT) -> {
                rbMcq.setStyle("-fx-font-size:13px;-fx-font-weight:bold;"
                        + (newT == rbMcq
                        ? "-fx-text-fill:white;-fx-background-color:" + UIUtils.ACCENT_BLUE + ";"
                        : "-fx-text-fill:" + UIUtils.ACCENT_BLUE + ";-fx-background-color:" + (UIUtils.darkMode ? "#1e3a5f" : "#eff6ff") + ";"  + "")
                        + "-fx-background-radius:20;-fx-padding:6 18;-fx-cursor:hand;");
                rbText.setStyle("-fx-font-size:13px;-fx-font-weight:bold;"
                        + (newT == rbText
                        ? "-fx-text-fill:white;-fx-background-color:" + UIUtils.ACCENT_PURP + ";"
                        : "-fx-text-fill:" + UIUtils.ACCENT_PURP + ";-fx-background-color:#f3e8ff;")
                        + "-fx-background-radius:20;-fx-padding:6 18;-fx-cursor:hand;");
            });
            HBox typeRow = new HBox(12,
                    new Label("Question Type:") {{ setStyle("-fx-font-weight:bold;-fx-font-size:14px;-fx-text-fill:" + UIUtils.textMid() + ";"); }},
                    rbMcq, rbText);
            typeRow.setAlignment(Pos.CENTER_LEFT);
            typeRow.setPadding(new Insets(4, 0, 4, 0));

            cfg.getChildren().addAll(cbSub, cbGrd);

            VBox dynForm = new VBox();
            renderMcqForm(dynForm);
            rbMcq.setOnAction(e  -> renderMcqForm(dynForm));
            rbText.setOnAction(e -> renderTextForm(dynForm));

            Button btnSave = UIUtils.primaryBtn("💾", "Save to Question Bank", UIUtils.ACCENT_GREEN);
            btnSave.setPrefHeight(46);
            btnSave.setOnAction(e -> {
                boolean valid = true;
                if (cbSub.getValue() == null) { cbSub.setStyle(cbSub.getStyle() + "-fx-border-color:#ef4444;-fx-border-radius:8;"); valid = false; }
                if (cbGrd.getValue() == null) { cbGrd.setStyle(cbGrd.getStyle() + "-fx-border-color:#ef4444;-fx-border-radius:8;"); valid = false; }
                if (qArea.getText().trim().isEmpty()) {
                    qArea.setStyle("-fx-font-size:14px;-fx-background-radius:8;-fx-border-color:#ef4444;-fx-border-width:1.5;");
                    valid = false;
                }
                if (!valid) { Toast.error(ca, "Please fill in all required fields"); return; }
                if (rbMcq.isSelected() && mcqTG.getSelectedToggle() == null) {
                    Toast.error(ca, "Select the correct MCQ answer before saving"); return;
                }
                try {
                    String s = cbSub.getValue(); int g = cbGrd.getValue(); String txt = qArea.getText().trim();
                    Question nq;
                    if (rbMcq.isSelected()) {
                        String[] opts = new String[4];
                        for (int i = 0; i < 4; i++) opts[i] = mcqOpts[i].getText();
                        nq = new MCQ(s, g, txt, opts, mcqTG.getToggles().indexOf(mcqTG.getSelectedToggle()));
                    } else {
                        if ("Exact Answer".equals(cbAnsType.getValue())) {
                            nq = new TextQuestion(s, g, txt, Double.parseDouble(exactF.getText()));
                        } else {
                            double mn = Double.parseDouble(minF.getText());
                            double mx = Double.parseDouble(maxF.getText());
                            if (mn >= mx) {
                                Toast.error(ca, "Min value must be strictly less than Max value"); return;
                            }
                            nq = new RangeQuestion(s, g, txt, mn, mx);
                        }
                    }
                    QuestionBank.allQuestions.addFirst(nq);
                    QuestionDAO.save(nq);
                    lastAdded = nq;
                    Toast.success(ca, "Question saved to the bank!");
                    if (returnToExam) ExamEditor.show(ca, app);
                    else qArea.clear();
                } catch (Exception ex) { Toast.error(ca, "Enter valid numeric values for answer/range"); }
            });

            page.getChildren().addAll(title, sub2, UIUtils.divider(), cfg, typeRow, dynForm, btnSave);
            sp.setContent(page);
            ca.getChildren().add(sp);
            if (ca instanceof javafx.scene.layout.AnchorPane ap2) {
                javafx.scene.layout.AnchorPane.setTopAnchor(sp, 0.0);
                javafx.scene.layout.AnchorPane.setBottomAnchor(sp, 0.0);
                javafx.scene.layout.AnchorPane.setLeftAnchor(sp, 0.0);
                javafx.scene.layout.AnchorPane.setRightAnchor(sp, 0.0);
            }
            UIUtils.slideIn(page, true);
        }

        private static void renderMcqForm(Pane c) {
            c.getChildren().clear();
            // Replace brittle absolute-position layout with a responsive VBox
            VBox form = new VBox(12);
            form.prefWidthProperty().bind(c.widthProperty());

            qArea = new TextArea();
            qArea.setPromptText("Type your MCQ question here...");
            qArea.setPrefHeight(80); qArea.setWrapText(true);
            qArea.setStyle("-fx-font-size:14px;-fx-background-radius:8;-fx-border-color:" + UIUtils.border() + ";-fx-background-color:" + UIUtils.bgSurface() + ";-fx-text-fill:" + UIUtils.textDark() + ";-fx-control-inner-background:" + UIUtils.bgSurface() + ";");

            Label optHdr = new Label("OPTIONS — select the correct answer:");
            optHdr.setStyle("-fx-font-size:11px;-fx-font-weight:bold;-fx-text-fill:" + UIUtils.TEXT_MID
                    + ";-fx-letter-spacing:0.5px;");

            mcqTG = new ToggleGroup();
            // 2-column grid: options 1+2 on row 1, options 3+4 on row 2
            GridPane grid = new GridPane();
            grid.setHgap(12); grid.setVgap(10);
            for (int i = 0; i < 4; i++) {
                mcqOpts[i] = UIUtils.styledField("Option " + (i + 1));
                mcqOpts[i].setPrefWidth(280);
                RadioButton rb = new RadioButton(); rb.setToggleGroup(mcqTG);
                HBox cell = new HBox(8, rb, mcqOpts[i]);
                cell.setAlignment(Pos.CENTER_LEFT);
                grid.add(cell, i % 2, i / 2);   // col = i%2, row = i/2
            }

            form.getChildren().addAll(qArea, optHdr, grid);
            c.getChildren().add(form);
        }

        private static void renderTextForm(Pane c) {
            c.getChildren().clear();
            VBox form = new VBox(12);
            form.prefWidthProperty().bind(c.widthProperty());

            qArea = new TextArea();
            qArea.setPromptText("Type your text/numeric question here...");
            qArea.setPrefHeight(80); qArea.setWrapText(true);
            qArea.setStyle("-fx-font-size:14px;-fx-background-radius:8;-fx-border-color:" + UIUtils.border() + ";-fx-background-color:" + UIUtils.bgSurface() + ";-fx-text-fill:" + UIUtils.textDark() + ";-fx-control-inner-background:" + UIUtils.bgSurface() + ";");

            Label ansHdr = new Label("ANSWER TYPE:");
            ansHdr.setStyle("-fx-font-size:11px;-fx-font-weight:bold;-fx-text-fill:" + UIUtils.TEXT_MID
                    + ";-fx-letter-spacing:0.5px;");

            cbAnsType = new ComboBox<>();
            cbAnsType.getItems().addAll("Exact Answer", "Allow Deviate (Range)");
            cbAnsType.setValue("Exact Answer");
            cbAnsType.setStyle("-fx-font-size:13px;-fx-pref-height:38px;");

            exactF = UIUtils.styledField("Exact numeric answer"); exactF.setPrefWidth(220);
            minF   = UIUtils.styledField("Min value"); minF.setPrefWidth(160);
            maxF   = UIUtils.styledField("Max value"); maxF.setPrefWidth(160);

            HBox rangeRow = new HBox(12, minF, maxF); rangeRow.setAlignment(Pos.CENTER_LEFT);

            // Swap answer field based on type selection
            VBox ansSlot = new VBox(exactF);
            cbAnsType.setOnAction(e -> {
                ansSlot.getChildren().clear();
                if ("Exact Answer".equals(cbAnsType.getValue())) ansSlot.getChildren().add(exactF);
                else ansSlot.getChildren().add(rangeRow);
            });

            form.getChildren().addAll(qArea, ansHdr, cbAnsType, ansSlot);
            c.getChildren().add(form);
        }
    }

    // ╔══════════════════════════════════════════════════════╗
    //  7. PAST EXAMS
    // ╚══════════════════════════════════════════════════════╝
    static class PastExams {
        static void render(Pane ca, HelloApplication app) {
            ca.getChildren().clear();
            ScrollPane sp = new ScrollPane();
            sp.prefWidthProperty().bind(ca.widthProperty());
            sp.prefHeightProperty().bind(ca.heightProperty());
            sp.setStyle("-fx-background:transparent;-fx-background-color:transparent;");
            sp.setFitToWidth(true);

            VBox page = new VBox(16);
            page.setPadding(new Insets(28, 30, 30, 30));

            Label title = UIUtils.heading("Past Exam History");

            // Collect past exams newest-first
            List<Exam> pastList = ExamBank.allExams.stream()
                    .filter(e -> !e.isLive() && e.getScheduleDetails() != null
                            && e.getScheduleDetails().startsWith("Ended"))
                    .collect(java.util.stream.Collectors.toList());
            java.util.Collections.reverse(pastList); // newest-first

            // Sub-header with count
            HBox subRow = new HBox(10); subRow.setAlignment(Pos.CENTER_LEFT);
            Label countBadge = UIUtils.badge(pastList.size() + " exam" + (pastList.size() == 1 ? "" : "s"),
                    UIUtils.ACCENT_ORG);
            Label sortNote = new Label("Most recent first");
            sortNote.setStyle("-fx-font-size:12px;-fx-text-fill:" + UIUtils.textMid() + ";");
            subRow.getChildren().addAll(countBadge, sortNote);

            page.getChildren().addAll(title, subRow, UIUtils.divider());

            if (pastList.isEmpty()) {
                Label empty = new Label("No past exams yet. Exams appear here after they end.");
                empty.setStyle("-fx-text-fill:" + UIUtils.textMid() + ";-fx-font-size:14px;-fx-padding:20;");
                page.getChildren().add(empty);
            } else {
                for (Exam e : pastList) page.getChildren().add(buildPastRow(e, ca, app));
            }

            sp.setContent(page);
            ca.getChildren().add(sp);
            if (ca instanceof javafx.scene.layout.AnchorPane ap3) {
                javafx.scene.layout.AnchorPane.setTopAnchor(sp, 0.0);
                javafx.scene.layout.AnchorPane.setBottomAnchor(sp, 0.0);
                javafx.scene.layout.AnchorPane.setLeftAnchor(sp, 0.0);
                javafx.scene.layout.AnchorPane.setRightAnchor(sp, 0.0);
            }
            UIUtils.slideIn(page, true);
        }

        private static HBox buildPastRow(Exam e, Pane ca, HelloApplication app) {
            HBox row = new HBox(14);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(14, 18, 14, 18));
            row.setStyle("-fx-background-color:" + UIUtils.bgCard() + ";-fx-background-radius:12;"
                    + "-fx-border-color:" + UIUtils.border() + ";-fx-border-radius:12;");
            DropShadow ds = new DropShadow(); ds.setColor(Color.color(0,0,0,0.05)); ds.setRadius(8); ds.setOffsetY(2);
            row.setEffect(ds);

            VBox info = new VBox(3);
            String displayTitle = (e.getTitle() != null && !e.getTitle().isEmpty()) ? e.getTitle() : e.getSubject();
            Label subL = new Label("📂 " + displayTitle);
            subL.setStyle("-fx-font-size:15px;-fx-font-weight:bold;-fx-text-fill:" + UIUtils.textDark() + ";");
            Label metaL = new Label(e.getSubject() + " · Grade " + e.getGrade() + " · " + e.getScheduleDetails());
            metaL.setStyle("-fx-font-size:12px;-fx-text-fill:" + UIUtils.textMid() + ";");
            info.getChildren().addAll(subL, metaL);

            Label gradeB = UIUtils.badge("Grade " + e.getGrade(), UIUtils.ACCENT_BLUE);
            Label marksB = UIUtils.badge(e.getTotalMarks() + " Marks", UIUtils.ACCENT_ORG);

            Region sp2 = new Region(); HBox.setHgrow(sp2, Priority.ALWAYS);

            Button btnView = UIUtils.primaryBtn("📋", "View Questions", UIUtils.ACCENT_BLUE);
            btnView.setOnAction(ev -> showPastExamDetails(e, ca, app));

            Button btnDel = UIUtils.ghostBtn("🗑", "Delete", UIUtils.ACCENT_RED);
            btnDel.setOnAction(ev -> {
                Alert a = new Alert(Alert.AlertType.CONFIRMATION, "Delete this exam record?", ButtonType.YES, ButtonType.NO);
                a.showAndWait().ifPresent(r -> { if (r == ButtonType.YES) { ExamBank.allExams.remove(e); render(ca, app); } });
            });

            row.getChildren().addAll(info, gradeB, marksB, sp2, btnView, btnDel);
            return row;
        }

        private static void showPastExamDetails(Exam e, Pane ca, HelloApplication app) {
            Stage st = new Stage();
            st.initModality(Modality.APPLICATION_MODAL);
            st.setTitle("Past Exam Details");

            VBox root = new VBox(16);
            root.setPadding(new Insets(28));
            root.setStyle("-fx-background-color:" + UIUtils.bgCard() + ";");

            String displayTitle = (e.getTitle() != null && !e.getTitle().isEmpty()) ? e.getTitle() : e.getSubject();
            Label titleL = new Label("📂  " + displayTitle);
            titleL.setStyle("-fx-font-size:20px;-fx-font-weight:bold;-fx-text-fill:" + UIUtils.textDark() + ";");

            // Meta grid
            GridPane grid = new GridPane();
            grid.setHgap(16); grid.setVgap(8);
            grid.setPadding(new Insets(12));
            grid.setStyle("-fx-background-color:" + UIUtils.bgContent() + ";-fx-background-radius:10;-fx-border-color:" + UIUtils.border() + ";-fx-border-radius:10;");
            addGridRow2(grid, 0, "Subject:", e.getSubject());
            addGridRow2(grid, 1, "Grade:", "Grade " + e.getGrade());
            addGridRow2(grid, 2, "Total Marks:", String.valueOf(e.getTotalMarks()));
            addGridRow2(grid, 3, "Duration:", e.getDuration() + " min");
            addGridRow2(grid, 4, "Ended:", e.getScheduleDetails());
            addGridRow2(grid, 5, "Live Window:", e.getLiveWindow().isEmpty() ? "—" : e.getLiveWindow());
            addGridRow2(grid, 6, "Exam Code:", e.getExamCode().isEmpty() ? "—" : e.getExamCode());

            root.getChildren().addAll(titleL, grid);

            // Description
            if (e.getDescription() != null && !e.getDescription().isEmpty()) {
                Label dH = new Label("Description:");
                dH.setStyle("-fx-font-weight:bold;-fx-text-fill:" + UIUtils.textMid() + ";");
                TextArea dA = new TextArea(e.getDescription());
                dA.setEditable(false); dA.setWrapText(true); dA.setPrefHeight(60);
                dA.setStyle("-fx-background-color:" + UIUtils.bgContent() + ";-fx-font-size:13px;-fx-background-radius:8;");
                root.getChildren().addAll(dH, dA);
            }

            // Questions list
            Label qlH = new Label("Questions (" + (e.getQuestionsMap() != null ? e.getQuestionsMap().size() : 0) + "):");
            qlH.setStyle("-fx-font-size:15px;-fx-font-weight:bold;-fx-text-fill:" + UIUtils.textDark() + ";");
            root.getChildren().add(qlH);

            if (e.getQuestionsMap() != null && !e.getQuestionsMap().isEmpty()) {
                int idx = 1;
                for (Map.Entry<Question, Double> entry : e.getQuestionsMap().entrySet()) {
                    Question q = entry.getKey();
                    Double pts = entry.getValue();
                    VBox card = UIUtils.card(460);
                    card.setPadding(new Insets(12));

                    String type = (q instanceof MCQ) ? "MCQ" : (q instanceof TextQuestion) ? "Text" : "Range";
                    String tc   = (q instanceof MCQ) ? UIUtils.ACCENT_BLUE : (q instanceof TextQuestion) ? UIUtils.ACCENT_GREEN : UIUtils.ACCENT_PURP;
                    HBox topRow = new HBox(10);
                    topRow.setAlignment(Pos.CENTER_LEFT);
                    Label numL = new Label(idx++ + ".");
                    numL.setStyle("-fx-font-weight:bold;-fx-font-size:14px;-fx-text-fill:" + UIUtils.textMid() + ";");
                    Label qTextL = new Label(q.getQuestionText());
                    qTextL.setStyle("-fx-font-size:14px;-fx-font-weight:bold;-fx-text-fill:" + UIUtils.textDark() + ";");
                    qTextL.setWrapText(true); qTextL.setMaxWidth(320);
                    Label typB = UIUtils.badge(type, tc);
                    Label ptsB = UIUtils.badge(pts + " pts", UIUtils.ACCENT_ORG);
                    topRow.getChildren().addAll(numL, qTextL, typB, ptsB);
                    card.getChildren().add(topRow);

                    // Answer preview
                    if (q instanceof MCQ) {
                        MCQ m = (MCQ)q;
                        VBox opts = new VBox(4); opts.setPadding(new Insets(6, 0, 0, 16));
                        for (int i = 0; i < 4; i++) {
                            Label ol = new Label((i+1) + ". " + m.getOptions()[i]);
                            ol.setStyle(i == m.getCorrectIndex()
                                    ? "-fx-text-fill:#16a34a;-fx-font-weight:bold;-fx-font-size:12px;"
                                    : "-fx-text-fill:" + UIUtils.textMid() + ";-fx-font-size:12px;");
                            if (i == m.getCorrectIndex()) ol.setText(ol.getText() + " ✅");
                            opts.getChildren().add(ol);
                        }
                        card.getChildren().add(opts);
                    } else if (q instanceof TextQuestion) {
                        Label al = new Label("✅ Answer: " + ((TextQuestion)q).getAnswer());
                        al.setStyle("-fx-text-fill:#16a34a;-fx-font-size:12px;-fx-font-weight:bold;-fx-padding:4 0 0 16;");
                        card.getChildren().add(al);
                    } else if (q instanceof RangeQuestion) {
                        RangeQuestion rq = (RangeQuestion)q;
                        Label al = new Label("✅ Range: " + rq.getMin() + " – " + rq.getMax());
                        al.setStyle("-fx-text-fill:#16a34a;-fx-font-size:12px;-fx-font-weight:bold;-fx-padding:4 0 0 16;");
                        card.getChildren().add(al);
                    }
                    root.getChildren().add(card);
                }
            } else {
                Label noQ = new Label("No questions on record.");
                noQ.setStyle("-fx-text-fill:" + UIUtils.textMid() + ";");
                root.getChildren().add(noQ);
            }

            Button btnClose = UIUtils.primaryBtn("✕", "Close", UIUtils.ACCENT_RED);
            btnClose.setOnAction(ev -> st.close());
            root.getChildren().add(btnClose);

            ScrollPane scrollPop = new ScrollPane(root);
            scrollPop.setFitToWidth(true);
            scrollPop.setStyle("-fx-background:white;");
            st.setScene(new Scene(scrollPop, 540, 620));
            st.show();
        }

        private static void addGridRow2(GridPane g, int row, String lbl, String val) {
            Label l = new Label(lbl); l.setStyle("-fx-font-weight:bold;-fx-text-fill:" + UIUtils.textMid() + ";-fx-font-size:13px;");
            Label v = new Label(val); v.setStyle("-fx-text-fill:" + UIUtils.textDark() + ";-fx-font-size:13px;");
            g.add(l, 0, row); g.add(v, 1, row);
        }
    }

    // ╔══════════════════════════════════════════════════════╗
    //  8. QUESTION BANK BROWSER  (#6)
    //  Filterable view of all questions with search + type/
    //  subject/grade filters. Delete individual questions.
    // ╚══════════════════════════════════════════════════════╝
    static class QuestionBankBrowser {

        static void render(Pane ca, HelloApplication app) {
            ca.getChildren().clear();

            ScrollPane sp = new ScrollPane();
            sp.prefWidthProperty().bind(ca.widthProperty());
            sp.prefHeightProperty().bind(ca.heightProperty());
            sp.setStyle("-fx-background:transparent;-fx-background-color:transparent;");
            sp.setFitToWidth(true);

            VBox page = new VBox(18);
            page.setPadding(new Insets(28, 30, 30, 30));

            Label title = UIUtils.heading("📚  Question Bank");
            Label sub   = UIUtils.subheading("Browse, search, and manage all saved questions");

            // ── Filter / search bar ───────────────────────────
            HBox filterRow = new HBox(12);
            filterRow.setAlignment(Pos.CENTER_LEFT);
            filterRow.setPadding(new Insets(14));
            filterRow.setStyle("-fx-background-color:" + UIUtils.bgCard() + ";-fx-background-radius:12;"
                    + "-fx-border-color:" + UIUtils.border() + ";-fx-border-radius:12;");

            TextField searchField = UIUtils.styledField("🔍  Search questions…");
            searchField.setPrefWidth(260);

            ComboBox<String> cbSubFilter = new ComboBox<>();
            cbSubFilter.getItems().add("All Subjects");
            cbSubFilter.getItems().addAll("Physics", "Chemistry", "Math", "Biology", "English");
            cbSubFilter.setValue("All Subjects");
            cbSubFilter.setStyle("-fx-font-size:13px;-fx-pref-height:38px;");

            ComboBox<String> cbGrdFilter = new ComboBox<>();
            cbGrdFilter.getItems().add("All Grades");
            for (int i = 6; i <= 12; i++) cbGrdFilter.getItems().add("Grade " + i);
            cbGrdFilter.setValue("All Grades");
            cbGrdFilter.setStyle("-fx-font-size:13px;-fx-pref-height:38px;");

            ComboBox<String> cbTypeFilter = new ComboBox<>();
            cbTypeFilter.getItems().addAll("All Types", "MCQ", "Text", "Range");
            cbTypeFilter.setValue("All Types");
            cbTypeFilter.setStyle("-fx-font-size:13px;-fx-pref-height:38px;");

            Label totalLbl = new Label();
            totalLbl.setStyle("-fx-font-size:12px;-fx-text-fill:" + UIUtils.textMid() + ";");

            Region fsp = new Region(); HBox.setHgrow(fsp, Priority.ALWAYS);
            filterRow.getChildren().addAll(searchField, cbSubFilter, cbGrdFilter, cbTypeFilter, fsp, totalLbl);

            // ── Results container ─────────────────────────────
            VBox results = new VBox(10);

            // Wire filters to rebuild
            Runnable refresh = () -> rebuildResults(results, ca, app,
                    searchField.getText(), cbSubFilter.getValue(),
                    cbGrdFilter.getValue(), cbTypeFilter.getValue(), totalLbl);

            searchField.setOnKeyReleased(e -> refresh.run());
            cbSubFilter.setOnAction(e  -> refresh.run());
            cbGrdFilter.setOnAction(e  -> refresh.run());
            cbTypeFilter.setOnAction(e -> refresh.run());

            // Initial render
            refresh.run();

            page.getChildren().addAll(title, sub, UIUtils.divider(), filterRow, results);
            sp.setContent(page);
            ca.getChildren().add(sp);
            if (ca instanceof javafx.scene.layout.AnchorPane ap) {
                javafx.scene.layout.AnchorPane.setTopAnchor(sp, 0.0);
                javafx.scene.layout.AnchorPane.setBottomAnchor(sp, 0.0);
                javafx.scene.layout.AnchorPane.setLeftAnchor(sp, 0.0);
                javafx.scene.layout.AnchorPane.setRightAnchor(sp, 0.0);
            }
            UIUtils.slideIn(page, true);
        }

        private static void rebuildResults(VBox results, Pane ca, HelloApplication app,
                                           String search, String subFilter, String grdFilter, String typeFilter, Label totalLbl) {
            results.getChildren().clear();

            String searchLower = search == null ? "" : search.toLowerCase().trim();

            List<Question> filtered = QuestionBank.allQuestions.stream().filter(q -> {
                // Subject filter
                if (!"All Subjects".equals(subFilter) && !q.getSubject().equals(subFilter)) return false;
                // Grade filter
                if (!"All Grades".equals(grdFilter)) {
                    int g = Integer.parseInt(grdFilter.replace("Grade ", ""));
                    if (q.getGrade() != g) return false;
                }
                // Type filter
                if (!"All Types".equals(typeFilter)) {
                    String qt = (q instanceof MCQ) ? "MCQ" : (q instanceof TextQuestion) ? "Text" : "Range";
                    if (!qt.equals(typeFilter)) return false;
                }
                // Search text
                if (!searchLower.isEmpty() && !q.getQuestionText().toLowerCase().contains(searchLower)) return false;
                return true;
            }).collect(java.util.stream.Collectors.toList());

            totalLbl.setText(filtered.size() + " question" + (filtered.size() == 1 ? "" : "s"));

            if (filtered.isEmpty()) {
                VBox empty = new VBox(10);
                empty.setAlignment(Pos.CENTER);
                empty.setPadding(new Insets(40));
                Label ico = new Label("🔍");
                ico.setStyle("-fx-font-size:36px;-fx-text-fill:" + UIUtils.textDark() + ";");
                Label msg = new Label("No questions match your filters");
                msg.setStyle("-fx-font-size:15px;-fx-font-weight:bold;-fx-text-fill:" + UIUtils.textMid() + ";");
                empty.getChildren().addAll(ico, msg);
                results.getChildren().add(empty);
                return;
            }

            for (Question q : filtered) {
                String type   = (q instanceof MCQ) ? "MCQ" : (q instanceof TextQuestion) ? "Text" : "Range";
                String tColor = (q instanceof MCQ) ? UIUtils.ACCENT_BLUE : (q instanceof TextQuestion) ? UIUtils.ACCENT_GREEN : UIUtils.ACCENT_PURP;

                HBox row = new HBox(12);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(14, 16, 14, 16));
                row.setStyle("-fx-background-color:" + UIUtils.bgCard() + ";-fx-background-radius:12;"
                        + "-fx-border-color:" + UIUtils.border() + ";-fx-border-radius:12;");
                DropShadow ds = new DropShadow(); ds.setColor(Color.color(0,0,0,0.04)); ds.setRadius(6); ds.setOffsetY(1);
                row.setEffect(ds);

                // Type badge
                Label typeBadge = UIUtils.badge(type, tColor);

                // Subject + grade chip
                Label metaBadge = UIUtils.badge(q.getSubject() + " · G" + q.getGrade(), UIUtils.ACCENT_BLUE);

                // Question text
                Label qText = new Label(q.getQuestionText());
                qText.setStyle("-fx-font-size:14px;-fx-font-weight:bold;-fx-text-fill:" + UIUtils.textDark() + ";");
                qText.setWrapText(true);
                qText.setMaxWidth(380);

                Region rowSp = new Region(); HBox.setHgrow(rowSp, Priority.ALWAYS);

                // Answer preview label
                String ansPreview = "";
                if (q instanceof MCQ)          ansPreview = "✅ " + ((MCQ) q).getOptions()[((MCQ) q).getCorrectIndex()];
                else if (q instanceof TextQuestion) ansPreview = "✅ " + ((TextQuestion) q).getAnswer();
                else if (q instanceof RangeQuestion) ansPreview = "✅ " + ((RangeQuestion) q).getMin() + "–" + ((RangeQuestion) q).getMax();
                Label ansL = new Label(ansPreview);
                ansL.setStyle("-fx-font-size:12px;-fx-text-fill:#16a34a;-fx-font-weight:bold;");
                ansL.setMaxWidth(160);
                ansL.setWrapText(true);

                // ⋯ Actions menu — replaces two separate Edit/Delete buttons
                // Saves ~120px per row so question text and answer preview have room to breathe
                MenuButton actionsMenu = new MenuButton("⋯");
                actionsMenu.setStyle(
                        "-fx-background-color:transparent;" +
                                "-fx-text-fill:" + UIUtils.textMid() + ";" +
                                "-fx-font-size:16px;-fx-font-weight:bold;" +
                                "-fx-background-radius:8;" +
                                "-fx-border-color:" + UIUtils.border() + ";" +
                                "-fx-border-radius:8;" +
                                "-fx-border-width:1;" +
                                "-fx-padding:4 10;" +
                                "-fx-cursor:hand;"
                );
                actionsMenu.setMinWidth(MenuButton.USE_PREF_SIZE);
                actionsMenu.setOnMouseEntered(ev -> actionsMenu.setStyle(actionsMenu.getStyle()
                        .replace("-fx-background-color:transparent;", "-fx-background-color:" + UIUtils.bgContent() + ";")));
                actionsMenu.setOnMouseExited(ev -> actionsMenu.setStyle(actionsMenu.getStyle()
                        .replace("-fx-background-color:" + UIUtils.bgContent() + ";", "-fx-background-color:transparent;")));

                MenuItem miEdit = new MenuItem("✏   Edit Question");
                miEdit.setStyle("-fx-font-size:13px;");
                miEdit.setOnAction(ev -> ExamEditor.showEditPopupPublic(q, app, () ->
                        rebuildResults(results, ca, app, search, subFilter, grdFilter, typeFilter, totalLbl)));

                MenuItem miDel = new MenuItem("🗑   Delete");
                miDel.setStyle("-fx-font-size:13px;-fx-text-fill:" + UIUtils.ACCENT_RED + ";");
                miDel.setOnAction(ev -> {
                    Alert a = new Alert(Alert.AlertType.CONFIRMATION,
                            "Delete this question permanently?", ButtonType.YES, ButtonType.NO);
                    a.showAndWait().ifPresent(r -> {
                        if (r == ButtonType.YES) {
                            QuestionBank.allQuestions.remove(q);
                            QuestionDAO.delete(q);
                            results.getChildren().remove(row);
                            Toast.success(ca, "Question deleted");
                            totalLbl.setText((Integer.parseInt(totalLbl.getText().split(" ")[0]) - 1)
                                    + " question" + (QuestionBank.allQuestions.size() == 1 ? "" : "s"));
                        }
                    });
                });

                actionsMenu.getItems().addAll(miEdit, new SeparatorMenuItem(), miDel);

                row.getChildren().addAll(typeBadge, metaBadge, qText, rowSp, ansL, actionsMenu);
                results.getChildren().add(row);
            }
        }
    }
}