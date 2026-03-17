package org.example.demo;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.effect.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.stage.*;
import javafx.util.Duration;

import java.util.*;
import java.util.List;
import java.util.Map;
public class StudentPortal {

    static int activeNavIndex = 0;

    // ╔══════════════════════════════════════════════════════╗
    //  1. LOGIN
    // ╚══════════════════════════════════════════════════════╝
    public static Scene createLoginScene(Stage stage, ArrayList<Student> list, HelloApplication app) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color:" + UIUtils.bgContent() + ";");

        VBox accent = new VBox(20);
        accent.setPrefWidth(340); accent.setAlignment(Pos.CENTER); accent.setPadding(new Insets(50));
        accent.setStyle("-fx-background-color:#052e16;");
        accent.getChildren().addAll(
                new Label("🎓") {{ setStyle("-fx-font-size:64px;-fx-text-fill:white;"); }},
                new Label("EduExam") {{ setStyle("-fx-font-size:32px;-fx-font-weight:bold;-fx-text-fill:white;"); }},
                new Label("Student Assessment Portal") {{ setStyle("-fx-font-size:14px;-fx-text-fill:#6ee7b7;"); }}
        );
        root.setLeft(accent);

        VBox form = new VBox(18);
        form.setAlignment(Pos.CENTER); form.setPadding(new Insets(60, 70, 60, 70)); form.setMaxWidth(400);

        Label title = new Label("Student Login");
        title.setStyle("-fx-font-size:28px;-fx-font-weight:bold;-fx-text-fill:" + UIUtils.textDark() + ";");
        Label sub = new Label("Enter your credentials to continue 📚");
        sub.setStyle("-fx-font-size:14px;-fx-text-fill:" + UIUtils.textMid() + ";");

        TextField     txtID   = UIUtils.styledField("Student ID or Email");
        PasswordField txtPass = UIUtils.styledPassword("Password");
        Button btnLogin = buildPremiumBtn("🔑   Sign In", UIUtils.ACCENT_GREEN);
        btnLogin.setPrefWidth(Double.MAX_VALUE); btnLogin.setPrefHeight(46);
        Hyperlink linkSignup = new Hyperlink("New here? Create an account");
        UIUtils.applyLinkEffects(linkSignup);
        Button btnBack = UIUtils.ghostBtn("←", "Back", UIUtils.TEXT_MID);

        btnLogin.setOnAction(e -> {
            String in = txtID.getText().trim(), pw = txtPass.getText();
            if (in.isEmpty() || pw.isEmpty()) { app.showError("Missing Fields", "Please enter your ID/email and password."); return; }
            Student found = UserDAO.loginStudent(in, pw);
            if (found != null) {
                if (list.stream().noneMatch(s -> s.getID().equals(found.getID()))) list.add(found);
                stage.setScene(createDashboardScene(stage, found, app));
            } else { app.showError("Login Failed", "Invalid Student ID/Email or Password."); }
        });
        linkSignup.setOnAction(e -> stage.setScene(createSignupScene(stage, list, app)));
        btnBack.setOnAction(e -> stage.setScene(app.createMainScene(stage)));

        String lbl = "-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:" + UIUtils.textMid() + ";";
        form.getChildren().addAll(title, sub, UIUtils.divider(),
                new Label("Student ID / Email") {{ setStyle(lbl); }}, txtID,
                new Label("Password")           {{ setStyle(lbl); }}, txtPass,
                btnLogin, linkSignup, btnBack);

        ScrollPane sp = new ScrollPane(form);
        sp.setFitToWidth(true); sp.setStyle("-fx-background:transparent;-fx-background-color:transparent;");
        root.setCenter(sp);
        Scene scene = new Scene(root, 1000, 600);
        UIUtils.applyStyle(scene); UIUtils.slideIn(form, true);
        return scene;
    }

    // ╔══════════════════════════════════════════════════════╗
    //  2. SIGN-UP
    // ╚══════════════════════════════════════════════════════╝
    public static Scene createSignupScene(Stage stage, ArrayList<Student> list, HelloApplication app) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color:" + UIUtils.bgContent() + ";");

        VBox accent = new VBox(20);
        accent.setPrefWidth(340); accent.setAlignment(Pos.CENTER); accent.setPadding(new Insets(50));
        accent.setStyle("-fx-background-color:#052e16;");
        accent.getChildren().addAll(
                new Label("📝") {{ setStyle("-fx-font-size:64px;-fx-text-fill:white;"); }},
                new Label("Join EduExam") {{ setStyle("-fx-font-size:28px;-fx-font-weight:bold;-fx-text-fill:white;"); }},
                new Label("Create your student account") {{ setStyle("-fx-font-size:14px;-fx-text-fill:#6ee7b7;"); }}
        );
        root.setLeft(accent);

        VBox form = new VBox(14);
        form.setAlignment(Pos.CENTER); form.setPadding(new Insets(40, 70, 40, 70)); form.setMaxWidth(400);

        TextField txtID = UIUtils.styledField("Student ID (numbers only)");
        TextField txtName = UIUtils.styledField("Full Name");
        TextField txtEmail = UIUtils.styledField("Email Address");
        PasswordField txtPass = UIUtils.styledPassword("Create Password");
        PasswordField txtConfirm = UIUtils.styledPassword("Confirm Password");

        txtID.textProperty().addListener((obs, o, n) -> {
            if (!n.matches("\\d*")) txtID.setText(n.replaceAll("[^\\d]", ""));
        });

        Button btnReg  = buildPremiumBtn("✅   Create Account", UIUtils.ACCENT_GREEN);
        btnReg.setPrefWidth(Double.MAX_VALUE); btnReg.setPrefHeight(46);
        Button btnBack = UIUtils.ghostBtn("←", "Back to Login", UIUtils.TEXT_MID);

        btnReg.setOnAction(e -> {
            String id = txtID.getText().trim(), name = txtName.getText().trim();
            String email = txtEmail.getText().trim(), pass = txtPass.getText(), confirm = txtConfirm.getText();
            if (id.isEmpty() || name.isEmpty() || email.isEmpty() || pass.isEmpty()) { app.showError("Missing Info", "Please fill in all fields."); return; }
            if (!id.matches("\\d+")) { app.showError("Invalid ID", "Student ID must contain numbers only."); return; }
            if (!pass.equals(confirm)) { app.showError("Mismatch", "Passwords do not match!"); return; }
            if (UserDAO.studentIdExists(id)) { app.showError("ID Taken", "A student with ID " + id + " already exists."); return; }
            if (UserDAO.registerStudent(id, name, email, pass)) {
                list.add(new Student(id, name, email, pass));
                app.showInfo("Registered!", "Account created. You can now log in.");
                stage.setScene(createLoginScene(stage, list, app));
            } else { app.showError("Error", "Registration failed. Please try again."); }
        });
        btnBack.setOnAction(e -> stage.setScene(createLoginScene(stage, list, app)));

        String lbl = "-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:" + UIUtils.textMid() + ";";
        form.getChildren().addAll(
                new Label("Student Registration") {{ setStyle("-fx-font-size:26px;-fx-font-weight:bold;-fx-text-fill:"+UIUtils.textDark()+";"); }},
                UIUtils.divider(),
                new Label("Student ID")  {{ setStyle(lbl); }}, txtID,
                new Label("Full Name")   {{ setStyle(lbl); }}, txtName,
                new Label("Email")       {{ setStyle(lbl); }}, txtEmail,
                new Label("Password")    {{ setStyle(lbl); }}, txtPass,
                new Label("Confirm")     {{ setStyle(lbl); }}, txtConfirm,
                btnReg, btnBack);

        ScrollPane sp = new ScrollPane(form);
        sp.setFitToWidth(true); sp.setStyle("-fx-background:transparent;-fx-background-color:transparent;");
        root.setCenter(sp);
        Scene scene = new Scene(root, 1000, 600);
        UIUtils.applyStyle(scene); UIUtils.slideIn(form, true);
        return scene;
    }

    // ╔══════════════════════════════════════════════════════╗
    //  3. DASHBOARD
    // ╚══════════════════════════════════════════════════════╝
    public static Scene createDashboardScene(Stage stage, Student student, HelloApplication app) {
        return createDashboardScene(stage, student, app, activeNavIndex);
    }

    public static Scene createDashboardScene(Stage stage, Student student, HelloApplication app, int startPage) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color:" + UIUtils.bgContent() + ";");

        javafx.scene.layout.AnchorPane contentArea = new javafx.scene.layout.AnchorPane();
        contentArea.setPrefSize(890, 660);
        contentArea.setStyle("-fx-background-color:" + UIUtils.bgContent() + ";");

        VBox sidebar = new VBox(0);
        sidebar.setPrefWidth(210);
        sidebar.setStyle("-fx-background-color:" + UIUtils.BG_DARK + ";");

        // Theme toggle — passes activeNavIndex so page is restored after toggle
        StackPane themeSwitch = UIUtils.themeToggleSwitch(() ->
                stage.setScene(createDashboardScene(stage, student, app, activeNavIndex))
        );
        HBox switchRow = new HBox(10, themeSwitch);
        switchRow.setAlignment(Pos.CENTER_LEFT);
        switchRow.setPadding(new Insets(14, 10, 0, 14));

        // Avatar
        VBox avatarBox = new VBox(6);
        avatarBox.setAlignment(Pos.CENTER);
        avatarBox.setPadding(new Insets(18, 10, 18, 10));
        Circle av = new Circle(34);
        av.setFill(Color.web(UIUtils.ACCENT_GREEN));
        av.setEffect(new DropShadow(14, Color.web(UIUtils.ACCENT_GREEN, 0.4)));
        Label initials = new Label(student.getName().substring(0, 1).toUpperCase());
        initials.setStyle("-fx-font-size:20px;-fx-font-weight:bold;-fx-text-fill:white;");
        StackPane avStack = new StackPane(av, initials);
        Label nameL = new Label(student.getName());
        nameL.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:white;");
        avatarBox.getChildren().addAll(avStack, nameL, UIUtils.badge("ID: " + student.getID(), UIUtils.ACCENT_GREEN));

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color:#1e293b;"); sep.setPadding(new Insets(4,0,12,0));

        String[][] nav = {
                {"🎯", "Join Exam",    UIUtils.ACCENT_GREEN},
                {"📊", "My Results",   UIUtils.ACCENT_BLUE},
                {"📡", "Ongoing",      UIUtils.ACCENT_ORG},
                {"📅", "Scheduled",    UIUtils.ACCENT_PURP},
                {"📈", "Analytics",    UIUtils.ACCENT_YELL},
                {"🏆", "Leaderboard",  "#f43f5e"},
                {"📢", "Notices",      UIUtils.ACCENT_BLUE},
        };
        StackPane[] navBtns = new StackPane[nav.length];

        // Scrollable nav area
        VBox navBox = new VBox(8);
        navBox.setPadding(new Insets(0, 10, 10, 10));
        for (int i = 0; i < nav.length; i++) {
            final int idx = i;
            navBtns[i] = UIUtils.modernSidebarBtn(nav[i][0], nav[i][1], nav[i][2]);
            navBtns[i].setOnMouseClicked(e -> {
                activeNavIndex = idx;
                for (StackPane nb : navBtns) UIUtils.modernSidebarSetInactive(nb);
                UIUtils.modernSidebarSetActive(navBtns[idx]);
                switch (idx) {
                    case 0 -> renderJoinExamPage(contentArea, stage, student, app);
                    case 1 -> renderMyResultsPage(contentArea, stage, student, app);
                    case 2 -> renderOngoingPage(contentArea, stage, student, app);
                    case 3 -> renderScheduledPage(contentArea);
                    case 4 -> renderAnalyticsPage(contentArea, student);
                    case 5 -> renderLeaderboardPage(contentArea, student);
                    case 6 -> renderAnnouncementsPage(contentArea);
                }
            });
            navBox.getChildren().add(navBtns[i]);
        }

        ScrollPane navScroll = new ScrollPane(navBox);
        navScroll.setFitToWidth(true);
        navScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        navScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        navScroll.setStyle("-fx-background:transparent;-fx-background-color:transparent;-fx-border-color:transparent;");
        VBox.setVgrow(navScroll, Priority.ALWAYS);

        // Fixed logout at bottom
        VBox logoutBox = new VBox();
        logoutBox.setPadding(new Insets(8, 10, 14, 10));
        logoutBox.setStyle("-fx-background-color:" + UIUtils.BG_DARK + ";-fx-border-color:#1e293b;-fx-border-width:1 0 0 0;");
        Button btnLogout = UIUtils.primaryBtn("🚪", "Log Out", UIUtils.ACCENT_RED);
        btnLogout.setPrefWidth(190);
        btnLogout.setOnAction(e -> {
            Alert c = new Alert(Alert.AlertType.CONFIRMATION);
            c.setTitle("Log Out"); c.setHeaderText("Are you sure you want to log out?");
            c.setContentText("You will be returned to the home screen.");
            c.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
            c.showAndWait().ifPresent(r -> { if (r == ButtonType.YES) stage.setScene(app.createMainScene(stage)); });
        });
        logoutBox.getChildren().add(btnLogout);

        sidebar.getChildren().addAll(switchRow, avatarBox, sep, navScroll, logoutBox);
        sidebar.setPrefHeight(Double.MAX_VALUE);

        root.setLeft(sidebar); root.setCenter(contentArea);

        // Restore active page and highlight (critical after theme toggle)
        UIUtils.modernSidebarSetActive(navBtns[startPage]);
        switch (startPage) {
            case 0 -> renderJoinExamPage(contentArea, stage, student, app);
            case 1 -> renderMyResultsPage(contentArea, stage, student, app);
            case 2 -> renderOngoingPage(contentArea, stage, student, app);
            case 3 -> renderScheduledPage(contentArea);
            case 4 -> renderAnalyticsPage(contentArea, student);
            case 5 -> renderLeaderboardPage(contentArea, student);
            case 6 -> renderAnnouncementsPage(contentArea);
        }

        Scene scene = new Scene(root, 1100, 660);
        UIUtils.applyStyle(scene);
        return scene;
    }

    // ╔══════════════════════════════════════════════════════╗
    //  4. JOIN EXAM PAGE
    // ╚══════════════════════════════════════════════════════╝
    private static void renderJoinExamPage(javafx.scene.layout.AnchorPane contentArea,
                                           Stage stage, Student student, HelloApplication app) {
        contentArea.getChildren().clear();
        VBox page = new VBox(28);
        page.setPadding(new Insets(36, 40, 36, 40));
        page.setStyle("-fx-background-color:" + UIUtils.bgContent() + ";");

        Label heading = UIUtils.heading("🎯  Join an Exam");
        Label sub     = UIUtils.subheading("Enter the 6-character code given by your teacher");

        VBox inputCard = UIUtils.card(580);
        inputCard.setMaxWidth(580); inputCard.setPadding(new Insets(28)); inputCard.setSpacing(16);

        Label codeLabel = new Label("EXAM CODE");
        codeLabel.setStyle("-fx-font-size:11px;-fx-font-weight:bold;-fx-letter-spacing:1.2px;-fx-text-fill:" + UIUtils.textSubtle() + ";");

        TextField codeField = new TextField();
        codeField.setPromptText("e.g.  A1B2C3");
        codeField.setStyle(
                "-fx-font-family:Monospaced;-fx-font-size:30px;-fx-font-weight:bold;-fx-alignment:center;" +
                        "-fx-text-fill:" + UIUtils.textDark() + ";-fx-background-color:" + UIUtils.bgSurface() + ";" +
                        "-fx-border-color:" + UIUtils.ACCENT_GREEN + ";-fx-border-radius:12;-fx-background-radius:12;" +
                        "-fx-border-width:2;-fx-padding:14;"
        );
        codeField.setPrefHeight(70); codeField.setMaxWidth(280);

        codeField.textProperty().addListener((obs, o, n) -> {
            String up = n.toUpperCase().replaceAll("[^A-Z0-9]", "");
            if (up.length() > 6) up = up.substring(0, 6);
            if (!n.equals(up)) {
                String fin = up;
                Platform.runLater(() -> {
                    codeField.setText(fin);
                    codeField.positionCaret(fin.length());
                });
            }
        });

        Label charCount = new Label("0 / 6");
        charCount.setStyle("-fx-font-size:12px;-fx-text-fill:" + UIUtils.textSubtle() + ";");
        codeField.textProperty().addListener((obs, o, n) -> charCount.setText(n.length() + " / 6"));

        HBox codeRow = new HBox(14, codeField, charCount);
        codeRow.setAlignment(Pos.CENTER_LEFT);

        Button btnSearch = buildPremiumBtn("🔍   Search Exam", UIUtils.ACCENT_GREEN);
        btnSearch.setPrefWidth(280); btnSearch.setPrefHeight(50);
        codeField.setOnAction(e -> btnSearch.fire());

        btnSearch.setOnAction(e -> {
            String code = codeField.getText().trim();
            if (code.length() != 6) { shakeNode(codeField); return; }
            btnSearch.setText("⏳   Searching..."); btnSearch.setDisable(true);
            PauseTransition pause = new PauseTransition(Duration.millis(500));
            pause.setOnFinished(ev -> {
                btnSearch.setText("🔍   Search Exam"); btnSearch.setDisable(false);
                Exam found = ExamBank.allExams.stream()
                        .filter(ex -> ex.isLive() && ex.getExamCode() != null && ex.getExamCode().equalsIgnoreCase(code))
                        .findFirst().orElse(null);
                if (found != null) showExamDetailPopup(found, stage, student, app);
                else               showNotFoundPopup(stage);
            });
            pause.play();
        });

        inputCard.getChildren().addAll(codeLabel, codeRow, btnSearch);

        // Live exams hint
        VBox liveCard = UIUtils.card(580);
        liveCard.setMaxWidth(580); liveCard.setPadding(new Insets(20));
        liveCard.getChildren().add(new Label("📡   Currently Live Exams") {{
            setStyle("-fx-font-size:15px;-fx-font-weight:bold;-fx-text-fill:" + UIUtils.textDark() + ";");
        }});
        List<Exam> liveExams = ExamBank.getLiveExams();
        if (liveExams.isEmpty()) {
            liveCard.getChildren().add(UIUtils.subheading("No exams are live right now."));
        } else {
            for (Exam ex : liveExams) {
                HBox row = new HBox(10); row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(9,12,9,12));
                row.setStyle("-fx-background-color:" + UIUtils.bgHover() + ";-fx-background-radius:8;");
                VBox info = new VBox(2);
                Label name = new Label(ex.getSubject() + (ex.getTitle()!=null&&!ex.getTitle().isBlank() ? "  —  "+ex.getTitle() : ""));
                name.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:" + UIUtils.textDark() + ";");
                Label meta = new Label("Grade "+ex.getGrade()+"  •  "+ex.getDuration()+" min  •  "+ex.getQuestionsMap().size()+" questions");
                meta.setStyle("-fx-font-size:11px;-fx-text-fill:" + UIUtils.textMid() + ";");
                info.getChildren().addAll(name, meta);
                Region sp2 = new Region(); HBox.setHgrow(sp2, Priority.ALWAYS);
                row.getChildren().addAll(new Label("🟢") {{ setStyle("-fx-font-size:12px;"); }}, info, sp2, UIUtils.badge(ex.getExamCode(), UIUtils.ACCENT_GREEN));
                liveCard.getChildren().add(row);
            }
        }

        page.getChildren().addAll(heading, sub, UIUtils.divider(), inputCard, liveCard);
        wrapInScroll(contentArea, page);
    }

    // ── Not found popup ──────────────────────────────────────
    private static void showNotFoundPopup(Stage owner) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL); popup.initOwner(owner); popup.initStyle(StageStyle.UNDECORATED);
        VBox box = new VBox(14); box.setAlignment(Pos.CENTER); box.setPadding(new Insets(32,40,32,40));
        box.setStyle("-fx-background-color:"+UIUtils.bgCard()+";-fx-background-radius:16;-fx-border-radius:16;-fx-border-color:"+UIUtils.ACCENT_RED+"55;-fx-border-width:2;");
        box.setEffect(new DropShadow(28, Color.color(0,0,0,0.35)));
        Label icon = new Label("❌"); icon.setStyle("-fx-font-size:40px;");
        Label msg  = new Label("Exam Not Found"); msg.setStyle("-fx-font-size:17px;-fx-font-weight:bold;-fx-text-fill:"+UIUtils.ACCENT_RED+";");
        Label hint = new Label("No live exam found with that code.\nDouble-check with your teacher.");
        hint.setStyle("-fx-font-size:12px;-fx-text-fill:"+UIUtils.textMid()+";-fx-alignment:center;");
        hint.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        Button btnOk = buildPremiumBtn("OK", UIUtils.ACCENT_RED);
        btnOk.setPrefWidth(120); btnOk.setPrefHeight(40); btnOk.setOnAction(e -> popup.close());
        box.getChildren().addAll(icon, msg, hint, btnOk);
        Scene sc = new Scene(new StackPane(box), 320, 220); sc.setFill(Color.TRANSPARENT);
        UIUtils.applyStyle(sc); popup.setScene(sc); animatePopupIn(box); popup.show();
    }

    // ╔══════════════════════════════════════════════════════╗
    //  5. EXAM DETAIL POPUP
    // ╚══════════════════════════════════════════════════════╝
    private static void showExamDetailPopup(Exam exam, Stage stage, Student student, HelloApplication app) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL); popup.initOwner(stage); popup.initStyle(StageStyle.UNDECORATED);

        VBox box = new VBox(18); box.setPadding(new Insets(32,38,28,38));
        box.setStyle("-fx-background-color:"+UIUtils.bgCard()+";-fx-background-radius:18;-fx-border-radius:18;-fx-border-color:"+UIUtils.ACCENT_GREEN+"66;-fx-border-width:2;");
        box.setEffect(new DropShadow(34, Color.color(0,0,0,0.38)));

        // Header
        HBox hdr = new HBox(12); hdr.setAlignment(Pos.CENTER_LEFT);
        StackPane ic = new StackPane(new Circle(24) {{ setFill(Color.web(UIUtils.ACCENT_GREEN,0.15)); }}, new Label("✅") {{ setStyle("-fx-font-size:18px;"); }});
        VBox tb = new VBox(3);
        String et = exam.getTitle()!=null&&!exam.getTitle().isBlank() ? exam.getTitle() : exam.getSubject()+" Examination";
        tb.getChildren().addAll(
                new Label(et) {{ setStyle("-fx-font-size:17px;-fx-font-weight:bold;-fx-text-fill:"+UIUtils.textDark()+";"); }},
                new Label("Exam found! Review details before starting.") {{ setStyle("-fx-font-size:11px;-fx-text-fill:"+UIUtils.ACCENT_GREEN+";-fx-font-weight:bold;"); }}
        );
        hdr.getChildren().addAll(ic, tb);

        // Grid
        GridPane grid = new GridPane(); grid.setHgap(18); grid.setVgap(10);
        addDetailRow(grid, 0, "📚  Subject",    exam.getSubject());
        addDetailRow(grid, 1, "🏫  Grade",      "Grade " + exam.getGrade());
        addDetailRow(grid, 2, "⏱  Duration",   exam.getDuration() + " minutes");
        addDetailRow(grid, 3, "📝  Questions", exam.getQuestionsMap().size() + " questions");
        addDetailRow(grid, 4, "🏆  Total Marks", String.valueOf((int)exam.getTotalMarks()));
        if (exam.getDescription()!=null&&!exam.getDescription().isBlank())
            addDetailRow(grid, 5, "📄  Notes", exam.getDescription());

        // Instructions
        VBox instrBox = new VBox(4); instrBox.setPadding(new Insets(10));
        instrBox.setStyle("-fx-background-color:"+UIUtils.ACCENT_BLUE+"0d;-fx-background-radius:9;");
        instrBox.getChildren().add(new Label("📋  Instructions") {{ setStyle("-fx-font-size:11px;-fx-font-weight:bold;-fx-text-fill:"+UIUtils.ACCENT_BLUE+";"); }});
        for (String tip : new String[]{"• Timer auto-submits when time is up.", "• Flag questions to revisit later.", "• Submitted answers cannot be changed."})
            instrBox.getChildren().add(new Label(tip) {{ setStyle("-fx-font-size:11.5px;-fx-text-fill:"+UIUtils.textMid()+";"); }});

        // Buttons
        HBox btnRow = new HBox(12); btnRow.setAlignment(Pos.CENTER_LEFT);
        Button btnStart  = buildPremiumBtn("🚀   Start Exam", UIUtils.ACCENT_GREEN);
        btnStart.setPrefWidth(170); btnStart.setPrefHeight(44);
        Button btnCancel = UIUtils.ghostBtn("✕", "Cancel", UIUtils.ACCENT_RED);
        btnCancel.setPrefHeight(44); btnCancel.setPrefWidth(100);
        btnStart.setOnAction(e -> { popup.close(); stage.setScene(buildExamScene(exam, student, stage, app)); });
        btnCancel.setOnAction(e -> popup.close());
        btnRow.getChildren().addAll(btnStart, btnCancel);

        box.getChildren().addAll(hdr, UIUtils.divider(), grid, instrBox, UIUtils.divider(), btnRow);
        Scene sc = new Scene(new StackPane(box), 460, 490); sc.setFill(Color.TRANSPARENT);
        UIUtils.applyStyle(sc); popup.setScene(sc); animatePopupIn(box); popup.show();
    }

    private static void addDetailRow(GridPane grid, int row, String key, String val) {
        Label k = new Label(key); k.setStyle("-fx-font-size:12px;-fx-font-weight:bold;-fx-text-fill:"+UIUtils.textMid()+";"); k.setPrefWidth(140);
        Label v = new Label(val);  v.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:"+UIUtils.textDark()+";"); v.setWrapText(true);
        grid.add(k,0,row); grid.add(v,1,row);
    }

    // ╔══════════════════════════════════════════════════════╗
    //  6. EXAM SCENE
    //  - Respects UIUtils.darkMode (not forced dark)
    //  - Scrollable question page (all questions visible)
    //  - Nav panel shows only Answered / Flagged badges
    // ╚══════════════════════════════════════════════════════╝
    private static Scene buildExamScene(Exam exam, Student student, Stage stage, HelloApplication app) {
        List<Question> questions = new ArrayList<>(exam.getQuestionsMap().keySet());
        int totalQ = questions.size();
        if (totalQ == 0) { app.showError("No Questions","This exam has no questions loaded."); return createDashboardScene(stage,student,app); }

        Map<Integer, String> answers  = new HashMap<>();
        Set<Integer>         flagged  = new HashSet<>();
        boolean[] submitted = { false };

        int durationSecs;
        try { durationSecs = Integer.parseInt(exam.getDuration().replaceAll("[^0-9]","")) * 60; }
        catch (Exception ex2) { durationSecs = 30 * 60; }
        final int TOTAL_SECS = durationSecs;
        long[] remaining = { TOTAL_SECS };

        // ── Theme-aware colours (respects user toggle) ────────
        String bg      = UIUtils.bgContent();
        String surface = UIUtils.bgSurface();
        String card    = UIUtils.bgCard();
        String bdColor = UIUtils.border();
        String txtD    = UIUtils.textDark();
        String txtM    = UIUtils.textMid();
        String txtS    = UIUtils.textSubtle();
        // For the top/bottom bars we always use a slightly elevated surface
        String barBg   = UIUtils.darkMode ? "#1c2333" : "#f1f5f9";
        String barBdr  = UIUtils.darkMode ? "#2d3748" : "#e2e8f0";
        String optBg   = UIUtils.darkMode ? "#1c2333" : "#ffffff";
        String optBdr  = UIUtils.darkMode ? "#2d3748" : "#e2e8f0";
        String optSel  = UIUtils.darkMode ? "#1d4ed8" : "#2563eb";
        String optSelBg= UIUtils.darkMode ? "#1d4ed822" : "#2563eb18";
        String optHov  = UIUtils.darkMode ? "#2a3347" : "#f8fafc";
        String navBg   = UIUtils.darkMode ? "#1a2236" : "#f8fafc";
        String navBdr  = UIUtils.darkMode ? "#2d3748" : "#e2e8f0";
        String tmrGreen= "#10b981";
        String timerTxt= UIUtils.darkMode ? "#e6edf3" : txtD;

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color:" + bg + ";");

        // ── TOP BAR ──────────────────────────────────────────
        HBox topBar = new HBox(20); topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(0,24,0,24)); topBar.setPrefHeight(62);
        topBar.setStyle("-fx-background-color:"+barBg+";-fx-border-color:"+barBdr+";-fx-border-width:0 0 1 0;");

        VBox titleVBox = new VBox(2);
        Label subjectTag = new Label(exam.getSubject().toUpperCase());
        subjectTag.setStyle("-fx-font-size:10px;-fx-font-weight:bold;-fx-text-fill:"+txtS+";-fx-letter-spacing:1.5px;");
        String et = exam.getTitle()!=null&&!exam.getTitle().isBlank() ? exam.getTitle() : exam.getSubject()+" Exam";
        Label examNameLbl = new Label(et);
        examNameLbl.setStyle("-fx-font-size:15px;-fx-font-weight:bold;-fx-text-fill:"+txtD+";");
        titleVBox.getChildren().addAll(subjectTag, examNameLbl);

        Region topSpacer = new Region(); HBox.setHgrow(topSpacer, Priority.ALWAYS);

        VBox timerBox = new VBox(2); timerBox.setAlignment(Pos.CENTER);
        Label timerCaption = new Label("TIME REMAINING");
        timerCaption.setStyle("-fx-font-size:9px;-fx-font-weight:bold;-fx-text-fill:"+txtS+";-fx-letter-spacing:1px;");
        Label timerLbl = new Label(formatTime(TOTAL_SECS));
        timerLbl.setStyle("-fx-font-size:28px;-fx-font-weight:bold;-fx-font-family:Monospaced;-fx-text-fill:"+tmrGreen+";");
        ProgressBar timeProg = new ProgressBar(1.0);
        timeProg.setPrefWidth(160); timeProg.setPrefHeight(4);
        timeProg.setStyle("-fx-accent:"+tmrGreen+";-fx-background-color:"+barBdr+";-fx-background-radius:3;");
        timerBox.getChildren().addAll(timerCaption, timerLbl, timeProg);

        Label studentLbl = new Label("👤  "+student.getName());
        studentLbl.setStyle("-fx-font-size:12px;-fx-text-fill:"+txtM+";");
        topBar.getChildren().addAll(titleVBox, topSpacer, timerBox, studentLbl);
        root.setTop(topBar);

        // ── RIGHT PANEL — only Answered + Flagged badges ──────
        VBox navPanel = new VBox(10); navPanel.setPrefWidth(180);
        navPanel.setPadding(new Insets(20,12,20,12));
        navPanel.setStyle("-fx-background-color:"+navBg+";-fx-border-color:"+navBdr+";-fx-border-width:0 0 0 1;");

        Label navTitle = new Label("QUESTIONS");
        navTitle.setStyle("-fx-font-size:10px;-fx-font-weight:bold;-fx-text-fill:"+txtS+";-fx-letter-spacing:1.2px;");

        TilePane navGrid = new TilePane(); navGrid.setHgap(5); navGrid.setVgap(5); navGrid.setPrefColumns(5);

        Label answeredCount = new Label("0 / "+totalQ);
        answeredCount.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:"+txtD+";");
        Label answeredLbl = new Label("answered");
        answeredLbl.setStyle("-fx-font-size:10px;-fx-text-fill:"+txtM+";");

        // Legend — ONLY answered and flagged, as requested
        VBox legend = new VBox(6);
        for (String[] leg : new String[][]{{"#10b981","Answered"},{"#f59e0b","Flagged"}}) {
            HBox lr = new HBox(7); lr.setAlignment(Pos.CENTER_LEFT);
            Circle dot = new Circle(5); dot.setFill(Color.web(leg[0]));
            Label lt = new Label(leg[1]); lt.setStyle("-fx-font-size:11px;-fx-text-fill:"+txtM+";");
            lr.getChildren().addAll(dot, lt); legend.getChildren().add(lr);
        }
        navPanel.getChildren().addAll(navTitle, navGrid, UIUtils.divider(), answeredCount, answeredLbl, UIUtils.divider(), legend);
        root.setRight(navPanel);

        // ── CENTER — full scrollable page of all questions ────
        ScrollPane centerScroll = new ScrollPane();
        centerScroll.setFitToWidth(true);
        centerScroll.setStyle("-fx-background:"+bg+";-fx-background-color:"+bg+";");

        VBox allQuestionsPage = new VBox(18);
        allQuestionsPage.setPadding(new Insets(30,36,30,36));
        allQuestionsPage.setStyle("-fx-background-color:"+bg+";");

        // Build a VBox[] for each question card so we can refresh it individually
        VBox[] qCardBoxes = new VBox[totalQ];

        // Helper to refresh a single question card
        Runnable[] refreshNav = { null };

        // Build nav buttons first
        Button[] navBtns = new Button[totalQ];
        for (int i = 0; i < totalQ; i++) {
            final int fi = i;
            Button nb = new Button(String.valueOf(i+1));
            nb.setPrefSize(28,28); nb.setCursor(javafx.scene.Cursor.HAND);
            nb.setStyle(qNavStyle(bdColor, false, UIUtils.darkMode));
            nb.setOnAction(e -> {
                // Scroll to that question card
                if (qCardBoxes[fi] != null) {
                    double totalH = allQuestionsPage.getBoundsInLocal().getHeight();
                    double cardY  = qCardBoxes[fi].getBoundsInParent().getMinY();
                    centerScroll.setVvalue(totalH > 0 ? cardY / totalH : 0);
                }
            });
            navBtns[fi] = nb;
            navGrid.getChildren().add(nb);
        }

        // Build all question cards
        for (int qi = 0; qi < totalQ; qi++) {
            final int idx = qi;
            Question q = questions.get(qi);

            VBox qCard = new VBox(16);
            qCard.setPadding(new Insets(24,28,24,28));
            qCard.setStyle(
                    "-fx-background-color:"+card+";" +
                            "-fx-border-color:"+bdColor+";" +
                            "-fx-border-radius:14;-fx-background-radius:14;-fx-border-width:1;"
            );
            DropShadow ds = new DropShadow(); ds.setColor(Color.color(0,0,0, UIUtils.darkMode?0.25:0.06)); ds.setOffsetY(2); ds.setRadius(10);
            qCard.setEffect(ds);
            qCardBoxes[idx] = qCard;

            // Badge row
            HBox badgeRow = new HBox(8); badgeRow.setAlignment(Pos.CENTER_LEFT);
            Label qBadge = new Label("Q"+(idx+1));
            qBadge.setStyle("-fx-background-color:#2563eb20;-fx-text-fill:#2563eb;-fx-font-weight:bold;-fx-font-size:12px;-fx-padding:3 10;-fx-background-radius:7;");
            Label marksBadge = UIUtils.badge((int)(double)exam.getQuestionsMap().get(q)+" mark"+(exam.getQuestionsMap().get(q)>1?"s":""), UIUtils.ACCENT_ORG);
            badgeRow.getChildren().addAll(qBadge, marksBadge);

            // Flag button inline
            Button flagBtn = new Button("⚑  Flag");
            flagBtn.setStyle(flagStyle(false, UIUtils.darkMode));
            flagBtn.setCursor(javafx.scene.Cursor.HAND);
            Region br = new Region(); HBox.setHgrow(br, Priority.ALWAYS);
            HBox topRow = new HBox(8, badgeRow, br, flagBtn);
            topRow.setAlignment(Pos.CENTER_LEFT);

            Label qText = new Label(q.getQuestionText());
            qText.setStyle("-fx-font-size:15px;-fx-font-weight:bold;-fx-text-fill:"+txtD+";");
            qText.setWrapText(true);

            qCard.getChildren().addAll(topRow, qText);

            // Options / text field
            if (q instanceof MCQ mcq) {
                String[] opts = mcq.getOptions();
                VBox optionsBox = new VBox(8);

                Runnable[] rebuildOpts = { null };
                rebuildOpts[0] = () -> {
                    optionsBox.getChildren().clear();
                    String savedAns = answers.getOrDefault(idx, null);
                    for (int oi = 0; oi < opts.length; oi++) {
                        final String optChar = String.valueOf((char)('A'+oi));
                        boolean selected = optChar.equals(savedAns);

                        HBox optRow = new HBox(14); optRow.setAlignment(Pos.CENTER_LEFT);
                        optRow.setPadding(new Insets(12,18,12,18));
                        optRow.setCursor(javafx.scene.Cursor.HAND);
                        optRow.setStyle(
                                "-fx-background-color:"+(selected?optSelBg:optBg)+";" +
                                        "-fx-border-color:"+(selected?optSel:optBdr)+";" +
                                        "-fx-border-width:"+(selected?"2":"1")+";" +
                                        "-fx-border-radius:10;-fx-background-radius:10;"
                        );

                        Label letter = new Label(optChar);
                        letter.setStyle(
                                "-fx-font-size:12px;-fx-font-weight:bold;" +
                                        "-fx-min-width:28;-fx-min-height:28;-fx-alignment:center;" +
                                        "-fx-background-radius:7;" +
                                        "-fx-background-color:"+(selected?optSel:(UIUtils.darkMode?"#2d3748":"#f1f5f9"))+";" +
                                        "-fx-text-fill:"+(selected?"white":(UIUtils.darkMode?"#94a3b8":"#64748b"))+";"
                        );
                        Label optText = new Label(opts[oi]);
                        optText.setStyle("-fx-font-size:13.5px;-fx-text-fill:"+(selected?txtD:(UIUtils.darkMode?"#c9d1d9":txtM))+";");
                        optText.setWrapText(true);
                        optRow.getChildren().addAll(letter, optText);

                        final Runnable[] rebuildRef = rebuildOpts;
                        optRow.setOnMouseClicked(me -> { answers.put(idx, optChar); rebuildRef[0].run(); if (refreshNav[0]!=null) refreshNav[0].run(); });
                        optRow.setOnMouseEntered(me -> { if (!optChar.equals(answers.getOrDefault(idx,null))) optRow.setStyle("-fx-background-color:"+optHov+";-fx-border-color:"+bdColor+";-fx-border-width:1;-fx-border-radius:10;-fx-background-radius:10;"); });
                        optRow.setOnMouseExited(me  -> { if (!optChar.equals(answers.getOrDefault(idx,null))) optRow.setStyle("-fx-background-color:"+optBg+";-fx-border-color:"+optBdr+";-fx-border-width:1;-fx-border-radius:10;-fx-background-radius:10;"); });
                        optionsBox.getChildren().add(optRow);
                    }
                    // Update flag button state
                    boolean isFlagged = flagged.contains(idx);
                    flagBtn.setText(isFlagged ? "🚩  Flagged" : "⚑  Flag");
                    flagBtn.setStyle(flagStyle(isFlagged, UIUtils.darkMode));
                    // Update nav button — flag always takes priority over answered
                    if (navBtns[idx]!=null) {
                        if      (flagged.contains(idx))    navBtns[idx].setStyle(qNavStyle("#f59e0b", false, UIUtils.darkMode));
                        else if (answers.containsKey(idx)) navBtns[idx].setStyle(qNavStyle("#10b981", false, UIUtils.darkMode));
                        else                               navBtns[idx].setStyle(qNavStyle(bdColor,   false, UIUtils.darkMode));
                    }
                    answeredCount.setText(answers.size()+" / "+totalQ);
                };
                rebuildOpts[0].run();

                flagBtn.setOnAction(e -> {
                    if (flagged.contains(idx)) flagged.remove(idx); else flagged.add(idx);
                    rebuildOpts[0].run();
                });
                qCard.getChildren().add(optionsBox);

            } else {
                TextField ansField = new TextField(answers.getOrDefault(idx,""));
                ansField.setPromptText("Type your answer here...");
                ansField.setStyle(
                        "-fx-background-color:"+optBg+";-fx-border-color:"+optBdr+";" +
                                "-fx-border-radius:9;-fx-background-radius:9;" +
                                "-fx-font-size:14px;-fx-text-fill:"+txtD+";-fx-padding:12;" +
                                "-fx-prompt-text-fill:"+txtS+";"
                );
                ansField.textProperty().addListener((obs, o, n) -> {
                    if (n.isBlank()) answers.remove(idx); else answers.put(idx, n);
                    // Flag takes priority over answered in nav
                    if (navBtns[idx]!=null) {
                        if      (flagged.contains(idx)) navBtns[idx].setStyle(qNavStyle("#f59e0b", false, UIUtils.darkMode));
                        else if (!n.isBlank())          navBtns[idx].setStyle(qNavStyle("#10b981", false, UIUtils.darkMode));
                        else                            navBtns[idx].setStyle(qNavStyle(bdColor,   false, UIUtils.darkMode));
                    }
                    answeredCount.setText(answers.size()+" / "+totalQ);
                });
                flagBtn.setOnAction(e -> {
                    if (flagged.contains(idx)) flagged.remove(idx); else flagged.add(idx);
                    boolean isFlagged = flagged.contains(idx);
                    flagBtn.setText(isFlagged ? "🚩  Flagged" : "⚑  Flag");
                    flagBtn.setStyle(flagStyle(isFlagged, UIUtils.darkMode));
                    if (navBtns[idx]!=null) navBtns[idx].setStyle(isFlagged ? qNavStyle("#f59e0b",false,UIUtils.darkMode) : (answers.containsKey(idx)?qNavStyle("#10b981",false,UIUtils.darkMode):qNavStyle(bdColor,false,UIUtils.darkMode)));
                });
                qCard.getChildren().add(ansField);
            }

            allQuestionsPage.getChildren().add(qCard);
        }

        refreshNav[0] = () -> {
            for (int i = 0; i < totalQ; i++) {
                if (navBtns[i]==null) continue;
                String a = answers.get(i);
                if      (flagged.contains(i))                    navBtns[i].setStyle(qNavStyle("#f59e0b", false, UIUtils.darkMode));
                else if (a != null && !a.isBlank())              navBtns[i].setStyle(qNavStyle("#10b981", false, UIUtils.darkMode));
                else                                             navBtns[i].setStyle(qNavStyle(bdColor,   false, UIUtils.darkMode));
            }
            answeredCount.setText(answers.size()+" / "+totalQ);
        };

        centerScroll.setContent(allQuestionsPage);
        root.setCenter(centerScroll);

        // ── BOTTOM BAR ────────────────────────────────────────
        HBox bottomBar = new HBox(16); bottomBar.setAlignment(Pos.CENTER);
        bottomBar.setPadding(new Insets(12,28,12,28));
        bottomBar.setStyle("-fx-background-color:"+barBg+";-fx-border-color:"+barBdr+";-fx-border-width:1 0 0 0;");

        Label qCounterLbl = new Label(totalQ+" questions  •  scroll to answer");
        qCounterLbl.setStyle("-fx-font-size:12px;-fx-text-fill:"+txtM+";");
        Region botSpacer = new Region(); HBox.setHgrow(botSpacer, Priority.ALWAYS);
        Button btnSubmit = buildPremiumBtn("✅   Submit Exam", UIUtils.ACCENT_GREEN);
        btnSubmit.setPrefHeight(44); btnSubmit.setPrefWidth(180);
        bottomBar.getChildren().addAll(qCounterLbl, botSpacer, btnSubmit);
        root.setBottom(bottomBar);

        // ── Submit ────────────────────────────────────────────
        Runnable doSubmit = () -> {
            if (submitted[0]) return;
            submitted[0] = true;
            int correct = 0; double score = 0;
            for (int i = 0; i < totalQ; i++) {
                Question q = questions.get(i);
                String ans = answers.get(i);
                if (ans == null || ans.isBlank()) continue;

                if (q instanceof MCQ mcq) {
                    if (ans.charAt(0) - 'A' == mcq.getCorrectIndex()) {
                        correct++; score += exam.getQuestionsMap().get(q);
                    }
                } else if (q instanceof TextQuestion tq) {
                    try {
                        if (Math.abs(Double.parseDouble(ans.trim()) - tq.getAnswer()) < 1e-9) {
                            correct++; score += exam.getQuestionsMap().get(q);
                        }
                    } catch (NumberFormatException ignored) {}
                } else if (q instanceof RangeQuestion rq) {
                    try {
                        double v = Double.parseDouble(ans.trim());
                        if (v >= rq.getMin() && v <= rq.getMax()) {
                            correct++; score += exam.getQuestionsMap().get(q);
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }
            final int fc = correct; final double fs = score;
            // Snapshot answers + flagged so result scene can show the review
            final Map<Integer, String> ansSnap = new HashMap<>(answers);
            final Set<Integer> flagSnap = new HashSet<>(flagged);

            // ── Persist result (best-score rule handled by ResultDAO) ──
            ExamResult result = new ExamResult();
            result.studentId   = student.getID();
            result.examId      = exam.getDbId();
            result.examTitle   = exam.getTitle()!=null ? exam.getTitle() : exam.getSubject();
            result.examSubject = exam.getSubject();
            result.examGrade   = exam.getGrade();
            result.score       = fs;
            result.totalMarks  = exam.getTotalMarks();
            result.correct     = fc;
            result.totalQ      = totalQ;
            result.takenAt     = System.currentTimeMillis();
            ResultDAO.save(result);

            Platform.runLater(() -> showResultScene(stage, exam, student, app, fc, totalQ, fs, questions, ansSnap, flagSnap));
        };

        btnSubmit.setOnAction(e -> {
            long unanswered = 0;
            for (int i = 0; i < totalQ; i++) {
                String a = answers.get(i);
                if (a == null || a.isBlank()) unanswered++;
            }
            if (unanswered > 0) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Submit Exam");
                confirm.setHeaderText(unanswered + " question" + (unanswered > 1 ? "s" : "") + " unanswered.");
                confirm.setContentText("Unanswered questions will score 0. Submit anyway?");
                confirm.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
                confirm.showAndWait().ifPresent(r -> { if (r == ButtonType.YES) doSubmit.run(); });
            } else { doSubmit.run(); }
        });

        // ── Timer ─────────────────────────────────────────────
        ScaleTransition pulse = new ScaleTransition(Duration.millis(500), timerLbl);
        pulse.setFromX(1.0); pulse.setToX(1.1); pulse.setFromY(1.0); pulse.setToY(1.1);
        pulse.setAutoReverse(true); pulse.setCycleCount(Animation.INDEFINITE);

        Timeline timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            if (submitted[0]) return;
            remaining[0]--;
            timerLbl.setText(formatTime((int)remaining[0]));
            timeProg.setProgress((double)remaining[0]/TOTAL_SECS);
            if (remaining[0]<=300 && remaining[0]>60) {
                timerLbl.setStyle("-fx-font-size:28px;-fx-font-weight:bold;-fx-font-family:Monospaced;-fx-text-fill:#f59e0b;");
                timeProg.setStyle("-fx-accent:#f59e0b;-fx-background-color:"+barBdr+";-fx-background-radius:3;");
                if (!pulse.getStatus().equals(Animation.Status.RUNNING)) pulse.play();
            } else if (remaining[0]<=60) {
                timerLbl.setStyle("-fx-font-size:28px;-fx-font-weight:bold;-fx-font-family:Monospaced;-fx-text-fill:#ef4444;");
                timeProg.setStyle("-fx-accent:#ef4444;-fx-background-color:"+barBdr+";-fx-background-radius:3;");
            }
            if (remaining[0]<=0) doSubmit.run();
        }));
        timer.setCycleCount(Animation.INDEFINITE); timer.play();

        Scene scene = new Scene(root, 1100, 700);
        UIUtils.applyStyle(scene);
        return scene;
    }

    // ╔══════════════════════════════════════════════════════╗
    //  7. RESULT SCENE  — score summary + full question review
    // ╚══════════════════════════════════════════════════════╝
    private static void showResultScene(Stage stage, Exam exam, Student student,
                                        HelloApplication app, int correct, int total,
                                        double score, List<Question> questions,
                                        Map<Integer, String> answers, Set<Integer> flagged) {
        double pct    = total > 0 ? (score / exam.getTotalMarks()) * 100 : 0;
        String grade  = pct>=80?"A":pct>=65?"B":pct>=50?"C":pct>=35?"D":"F";
        String emoji  = pct>=80?"🎉":pct>=65?"👍":pct>=50?"✅":"📚";
        String gcolor = pct>=65 ? UIUtils.ACCENT_GREEN : pct>=50 ? UIUtils.ACCENT_BLUE : UIUtils.ACCENT_RED;

        String bg   = UIUtils.bgContent();
        String card = UIUtils.bgCard();
        String txtD = UIUtils.textDark();
        String txtM = UIUtils.textMid();
        String txtS = UIUtils.textSubtle();
        String bdr  = UIUtils.border();

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color:" + bg + ";");

        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background:transparent;-fx-background-color:transparent;");

        VBox center = new VBox(22);
        center.setPadding(new Insets(36, 50, 40, 50));
        center.setStyle("-fx-background-color:" + bg + ";");

        // ── Header ────────────────────────────────────────
        HBox headerRow = new HBox(14); headerRow.setAlignment(Pos.CENTER);
        Label emojiLbl = new Label(emoji); emojiLbl.setStyle("-fx-font-size:40px;");
        VBox headerText = new VBox(3); headerText.setAlignment(Pos.CENTER_LEFT);
        Label titleLbl = new Label("Exam Submitted!");
        titleLbl.setStyle("-fx-font-size:22px;-fx-font-weight:bold;-fx-text-fill:" + txtD + ";");
        String etitle = exam.getTitle()!=null&&!exam.getTitle().isBlank() ? exam.getTitle() : exam.getSubject();
        Label subtitleLbl = new Label(etitle + "  •  " + exam.getSubject());
        subtitleLbl.setStyle("-fx-font-size:13px;-fx-text-fill:" + txtM + ";");
        headerText.getChildren().addAll(titleLbl, subtitleLbl);
        headerRow.getChildren().addAll(emojiLbl, headerText);

        // ── Score card ────────────────────────────────────
        HBox scoreRow = new HBox(24); scoreRow.setAlignment(Pos.CENTER);
        scoreRow.setPadding(new Insets(20, 28, 20, 28));
        scoreRow.setMaxWidth(560);
        scoreRow.setStyle("-fx-background-color:" + card + ";-fx-background-radius:14;" +
                "-fx-border-radius:14;-fx-border-color:" + gcolor + "44;-fx-border-width:2;");
        scoreRow.setEffect(new DropShadow(16, Color.web(gcolor, 0.18)));

        StackPane scoreCircle = new StackPane();
        Circle circleBg = new Circle(48);
        circleBg.setFill(Color.web(gcolor, 0.13));
        circleBg.setStroke(Color.web(gcolor)); circleBg.setStrokeWidth(2.5);
        VBox inner = new VBox(1); inner.setAlignment(Pos.CENTER);
        Label scoreLbl = new Label(String.format("%.0f", score));
        scoreLbl.setStyle("-fx-font-size:22px;-fx-font-weight:bold;-fx-text-fill:" + gcolor + ";");
        Label outOfLbl = new Label("/" + ((int) exam.getTotalMarks()));
        outOfLbl.setStyle("-fx-font-size:11px;-fx-text-fill:" + txtM + ";");
        inner.getChildren().addAll(scoreLbl, outOfLbl);
        scoreCircle.getChildren().addAll(circleBg, inner);

        VBox rightSide = new VBox(10); rightSide.setAlignment(Pos.CENTER_LEFT);
        Label gradeLbl = new Label("Grade  " + grade);
        gradeLbl.setStyle("-fx-font-size:20px;-fx-font-weight:bold;-fx-text-fill:" + gcolor + ";" +
                "-fx-background-color:" + gcolor + "22;-fx-padding:4 16;-fx-background-radius:8;");
        HBox stats = new HBox(20); stats.setAlignment(Pos.CENTER_LEFT);
        stats.getChildren().addAll(
                makeStatBox("✅", String.valueOf(correct),        "Correct",  UIUtils.ACCENT_GREEN),
                makeStatBox("❌", String.valueOf(total - correct),"Wrong",    UIUtils.ACCENT_RED),
                makeStatBox("📊", String.format("%.1f%%", pct),  "Score",    UIUtils.ACCENT_BLUE)
        );
        rightSide.getChildren().addAll(gradeLbl, stats);
        scoreRow.getChildren().addAll(scoreCircle, rightSide);

        // ── Divider + review header ───────────────────────
        Label reviewHdr = new Label("📋  Question Review");
        reviewHdr.setStyle("-fx-font-size:16px;-fx-font-weight:bold;-fx-text-fill:" + txtD + ";");

        // ── Question review cards ─────────────────────────
        VBox reviewBox = new VBox(14);

        for (int i = 0; i < total; i++) {
            Question q = questions.get(i);
            String studentAns = answers.get(i);
            double qMark = exam.getQuestionsMap().get(q);
            boolean wasFlagged = flagged.contains(i);

            // Determine correctness and earned marks
            boolean isCorrect = false;
            double earned = 0;
            String correctDisplay = "";

            if (q instanceof MCQ mcq) {
                int ci = mcq.getCorrectIndex();
                correctDisplay = (char)('A' + ci) + " — " + mcq.getOptions()[ci];
                if (studentAns != null && !studentAns.isBlank()
                        && studentAns.charAt(0) - 'A' == ci) {
                    isCorrect = true; earned = qMark;
                }
            } else if (q instanceof TextQuestion tq) {
                correctDisplay = String.valueOf(tq.getAnswer());
                if (studentAns != null && !studentAns.isBlank()) {
                    try {
                        if (Math.abs(Double.parseDouble(studentAns.trim()) - tq.getAnswer()) < 1e-9) {
                            isCorrect = true; earned = qMark;
                        }
                    } catch (NumberFormatException ignored) {}
                }
            } else if (q instanceof RangeQuestion rq) {
                correctDisplay = rq.getMin() + " – " + rq.getMax();
                if (studentAns != null && !studentAns.isBlank()) {
                    try {
                        double v = Double.parseDouble(studentAns.trim());
                        if (v >= rq.getMin() && v <= rq.getMax()) {
                            isCorrect = true; earned = qMark;
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }

            boolean unanswered = studentAns == null || studentAns.isBlank();
            String cardBorder = isCorrect ? "#22c55e44" : (unanswered ? bdr : "#ef444444");
            String cardLeft   = isCorrect ? "#22c55e"   : (unanswered ? "#94a3b8" : "#ef4444");

            // Card
            HBox cardWrap = new HBox(0);
            // Left accent bar
            Region accentBar = new Region();
            accentBar.setPrefWidth(4); accentBar.setMinWidth(4);
            accentBar.setStyle("-fx-background-color:" + cardLeft + ";-fx-background-radius:12 0 0 12;");

            VBox cardBody = new VBox(10);
            cardBody.setPadding(new Insets(14, 18, 14, 16));
            cardBody.setStyle("-fx-background-color:" + card + ";-fx-background-radius:0 12 12 0;" +
                    "-fx-border-color:" + cardBorder + ";-fx-border-width:1 1 1 0;-fx-border-radius:0 12 12 0;");
            HBox.setHgrow(cardBody, Priority.ALWAYS);

            // Top row: Q# badge + marks + flag chip + result chip
            HBox topRow = new HBox(8); topRow.setAlignment(Pos.CENTER_LEFT);
            Label qNum = new Label("Q" + (i + 1));
            qNum.setStyle("-fx-font-size:11px;-fx-font-weight:bold;-fx-text-fill:#2563eb;" +
                    "-fx-background-color:#2563eb18;-fx-padding:2 9;-fx-background-radius:6;");
            Label markChip = new Label((int)qMark + " mark" + (qMark > 1 ? "s" : ""));
            markChip.setStyle("-fx-font-size:11px;-fx-text-fill:" + txtS + ";" +
                    "-fx-background-color:" + (UIUtils.darkMode?"#2d3748":"#f1f5f9") + ";-fx-padding:2 9;-fx-background-radius:6;");
            Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);

            if (wasFlagged) {
                Label flagChip = new Label("🚩 Flagged");
                flagChip.setStyle("-fx-font-size:10px;-fx-font-weight:bold;-fx-text-fill:#d97706;" +
                        "-fx-background-color:#fef3c7;-fx-padding:2 8;-fx-background-radius:6;");
                topRow.getChildren().add(flagChip);
            }
            // Earned marks chip
            Label earnedChip = new Label(isCorrect ? "+" + (int)earned + " ✓" : (unanswered ? "— No answer" : "0  ✗"));
            earnedChip.setStyle("-fx-font-size:11px;-fx-font-weight:bold;" +
                    "-fx-text-fill:" + (isCorrect ? "#15803d" : (unanswered ? txtS : "#b91c1c")) + ";" +
                    "-fx-background-color:" + (isCorrect ? "#dcfce7" : (unanswered ? (UIUtils.darkMode?"#2d3748":"#f1f5f9") : "#fee2e2")) + ";" +
                    "-fx-padding:2 9;-fx-background-radius:6;");
            topRow.getChildren().addAll(0, java.util.List.of(qNum, markChip, sp));
            topRow.getChildren().add(earnedChip);

            // Question text
            Label qTextLbl = new Label(q.getQuestionText());
            qTextLbl.setStyle("-fx-font-size:13.5px;-fx-font-weight:bold;-fx-text-fill:" + txtD + ";");
            qTextLbl.setWrapText(true);

            // Answer rows
            VBox answerBlock = new VBox(5);

            if (q instanceof MCQ mcq) {
                String[] opts = mcq.getOptions();
                for (int oi = 0; oi < opts.length; oi++) {
                    String optChar = String.valueOf((char)('A' + oi));
                    boolean isStudentPick = optChar.equals(studentAns);
                    boolean isCorrectOpt  = oi == mcq.getCorrectIndex();

                    String rowBg, rowBorder, letterBg, letterTxt, optTxt;
                    if (isCorrectOpt && isStudentPick) {
                        // Correct + picked → green
                        rowBg = UIUtils.darkMode ? "#14532d44" : "#dcfce7";
                        rowBorder = "#22c55e";
                        letterBg = "#22c55e"; letterTxt = "white"; optTxt = "#15803d";
                    } else if (isCorrectOpt) {
                        // Correct but not picked → show correct in green
                        rowBg = UIUtils.darkMode ? "#14532d22" : "#f0fdf4";
                        rowBorder = "#22c55e66";
                        letterBg = "#22c55e88"; letterTxt = "white"; optTxt = txtD;
                    } else if (isStudentPick) {
                        // Student picked wrong → red
                        rowBg = UIUtils.darkMode ? "#7f1d1d44" : "#fee2e2";
                        rowBorder = "#ef4444";
                        letterBg = "#ef4444"; letterTxt = "white"; optTxt = "#b91c1c";
                    } else {
                        rowBg = card; rowBorder = bdr;
                        letterBg = UIUtils.darkMode ? "#2d3748" : "#f1f5f9";
                        letterTxt = txtS; optTxt = txtM;
                    }

                    HBox optRow = new HBox(12); optRow.setAlignment(Pos.CENTER_LEFT);
                    optRow.setPadding(new Insets(8, 14, 8, 14));
                    optRow.setStyle("-fx-background-color:" + rowBg + ";" +
                            "-fx-border-color:" + rowBorder + ";-fx-border-width:1;" +
                            "-fx-border-radius:8;-fx-background-radius:8;");

                    Label letter = new Label(optChar);
                    letter.setStyle("-fx-font-size:11px;-fx-font-weight:bold;" +
                            "-fx-min-width:24;-fx-min-height:24;-fx-alignment:center;" +
                            "-fx-background-color:" + letterBg + ";-fx-text-fill:" + letterTxt + ";" +
                            "-fx-background-radius:6;");
                    Label optLbl = new Label(opts[oi]);
                    optLbl.setStyle("-fx-font-size:13px;-fx-text-fill:" + optTxt + ";");
                    optLbl.setWrapText(true); HBox.setHgrow(optLbl, Priority.ALWAYS);

                    // Indicator icons
                    if (isCorrectOpt) {
                        Label tick = new Label("✓");
                        tick.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:#22c55e;");
                        optRow.getChildren().addAll(letter, optLbl, tick);
                    } else if (isStudentPick) {
                        Label cross = new Label("✗");
                        cross.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:#ef4444;");
                        optRow.getChildren().addAll(letter, optLbl, cross);
                    } else {
                        optRow.getChildren().addAll(letter, optLbl);
                    }
                    answerBlock.getChildren().add(optRow);
                }
            } else {
                // Text / Range: show student answer vs correct
                HBox ansRow = new HBox(14); ansRow.setAlignment(Pos.CENTER_LEFT);

                VBox yourAns = new VBox(3);
                Label yaLbl = new Label("YOUR ANSWER");
                yaLbl.setStyle("-fx-font-size:9px;-fx-font-weight:bold;-fx-text-fill:" + txtS + ";-fx-letter-spacing:0.8px;");
                Label yaVal = new Label(unanswered ? "—" : studentAns);
                yaVal.setStyle("-fx-font-size:14px;-fx-font-weight:bold;-fx-text-fill:" +
                        (isCorrect ? "#15803d" : (unanswered ? txtS : "#b91c1c")) + ";");
                yourAns.getChildren().addAll(yaLbl, yaVal);

                VBox corrAns = new VBox(3);
                Label caLbl = new Label(q instanceof RangeQuestion ? "ACCEPTED RANGE" : "CORRECT ANSWER");
                caLbl.setStyle("-fx-font-size:9px;-fx-font-weight:bold;-fx-text-fill:" + txtS + ";-fx-letter-spacing:0.8px;");
                Label caVal = new Label(correctDisplay);
                caVal.setStyle("-fx-font-size:14px;-fx-font-weight:bold;-fx-text-fill:#15803d;");
                corrAns.getChildren().addAll(caLbl, caVal);

                ansRow.getChildren().addAll(yourAns, new Label("→") {{ setStyle("-fx-font-size:16px;-fx-text-fill:"+txtS+";"); }}, corrAns);
                answerBlock.getChildren().add(ansRow);
            }

            cardBody.getChildren().addAll(topRow, qTextLbl, answerBlock);
            cardWrap.getChildren().addAll(accentBar, cardBody);
            // Card shadow
            DropShadow cds = new DropShadow();
            cds.setColor(Color.color(0,0,0, UIUtils.darkMode?0.2:0.06));
            cds.setRadius(8); cds.setOffsetY(2);
            cardWrap.setEffect(cds);
            reviewBox.getChildren().add(cardWrap);
        }

        // ── Back button ───────────────────────────────────
        Button btnBack = buildPremiumBtn("← Back to Dashboard", UIUtils.ACCENT_GREEN);
        btnBack.setPrefWidth(240); btnBack.setPrefHeight(46);
        btnBack.setOnAction(e -> { activeNavIndex = 0; stage.setScene(createDashboardScene(stage, student, app)); });

        center.getChildren().addAll(headerRow, scoreRow, UIUtils.divider(), reviewHdr, reviewBox, UIUtils.divider(), btnBack);

        center.setOpacity(0); center.setTranslateY(20);
        scroll.setContent(center);
        root.setCenter(scroll);

        Scene scene = new Scene(root, 1100, 700);
        UIUtils.applyStyle(scene);
        stage.setScene(scene);

        FadeTransition ft = new FadeTransition(Duration.millis(380), center); ft.setToValue(1);
        TranslateTransition tt = new TranslateTransition(Duration.millis(380), center);
        tt.setToY(0); tt.setInterpolator(Interpolator.EASE_OUT);
        new ParallelTransition(ft, tt).play();
    }

    // ╔══════════════════════════════════════════════════════╗
    //  PLACEHOLDER PAGES
    // ╚══════════════════════════════════════════════════════╝
    // ╔══════════════════════════════════════════════════════╗
    //  MY RESULTS PAGE — shows best score per exam
    // ╚══════════════════════════════════════════════════════╝
    private static void renderMyResultsPage(javafx.scene.layout.AnchorPane contentArea,
                                            Stage stage, Student student, HelloApplication app) {
        contentArea.getChildren().clear();
        VBox page = new VBox(22); page.setPadding(new Insets(36,40,36,40));
        page.setStyle("-fx-background-color:"+UIUtils.bgContent()+";");
        page.getChildren().addAll(UIUtils.heading("📊  My Results"),
                UIUtils.subheading("Your best score per exam"), UIUtils.divider());

        List<ExamResult> results = ResultDAO.loadForStudent(student.getID());
        if (results.isEmpty()) {
            page.getChildren().add(buildPlaceholder("📂","No results yet",
                    "Complete an exam to see your results here.", UIUtils.ACCENT_BLUE));
        } else {
            for (ExamResult r : results) {
                VBox card = UIUtils.card(700); card.setMaxWidth(700);
                card.setPadding(new Insets(18,20,18,20)); card.setSpacing(0);

                String gcolor = r.pct()>=65 ? UIUtils.ACCENT_GREEN : r.pct()>=50 ? UIUtils.ACCENT_BLUE : UIUtils.ACCENT_RED;

                HBox row = new HBox(16); row.setAlignment(Pos.CENTER_LEFT);

                // Score circle
                StackPane sc = new StackPane();
                Circle bg2 = new Circle(32); bg2.setFill(Color.web(gcolor,0.13));
                bg2.setStroke(Color.web(gcolor)); bg2.setStrokeWidth(2);
                VBox inn = new VBox(0); inn.setAlignment(Pos.CENTER);
                Label sc1 = new Label(String.format("%.0f",r.score));
                sc1.setStyle("-fx-font-size:14px;-fx-font-weight:bold;-fx-text-fill:"+gcolor+";");
                Label sc2 = new Label("/"+((int)r.totalMarks));
                sc2.setStyle("-fx-font-size:10px;-fx-text-fill:"+UIUtils.textMid()+";");
                inn.getChildren().addAll(sc1,sc2); sc.getChildren().addAll(bg2,inn);

                // Info
                VBox info = new VBox(4);
                String title = r.examTitle!=null&&!r.examTitle.isBlank() ? r.examTitle : r.examSubject;
                Label nameLbl = new Label(title);
                nameLbl.setStyle("-fx-font-size:14px;-fx-font-weight:bold;-fx-text-fill:"+UIUtils.textDark()+";");
                Label meta = new Label("📚 "+r.examSubject+"  •  Grade "+r.examGrade
                        +"  •  "+r.correct+"/"+r.totalQ+" correct  •  "+r.dateStr());
                meta.setStyle("-fx-font-size:11px;-fx-text-fill:"+UIUtils.textMid()+";");
                info.getChildren().addAll(nameLbl, meta);

                Region sp2 = new Region(); HBox.setHgrow(sp2, Priority.ALWAYS);

                // Grade badge
                Label gradeBadge = new Label("Grade "+r.grade());
                gradeBadge.setStyle("-fx-background-color:"+gcolor+"22;-fx-text-fill:"+gcolor
                        +";-fx-font-weight:bold;-fx-font-size:13px;-fx-padding:4 14;-fx-background-radius:8;");

                // Percentage bar
                VBox barBox = new VBox(2); barBox.setMinWidth(100);
                Label pctLbl = new Label(String.format("%.1f%%",r.pct()));
                pctLbl.setStyle("-fx-font-size:11px;-fx-text-fill:"+UIUtils.textMid()+";");
                javafx.scene.control.ProgressBar pb = new javafx.scene.control.ProgressBar(r.pct()/100);
                pb.setPrefWidth(100); pb.setPrefHeight(6);
                pb.setStyle("-fx-accent:"+gcolor+";-fx-background-color:"+UIUtils.border()+";-fx-background-radius:3;");
                barBox.getChildren().addAll(pctLbl, pb);

                row.getChildren().addAll(sc, info, sp2, barBox, gradeBadge);
                card.getChildren().add(row);
                page.getChildren().add(card);
            }
        }
        wrapInScroll(contentArea, page);
    }

    // ╔══════════════════════════════════════════════════════╗
    //  ONGOING EXAMS — live exams the student can join
    // ╚══════════════════════════════════════════════════════╝
    private static void renderOngoingPage(javafx.scene.layout.AnchorPane contentArea,
                                          Stage stage, Student student, HelloApplication app) {
        contentArea.getChildren().clear();
        VBox page = new VBox(22); page.setPadding(new Insets(36,40,36,40));
        page.setStyle("-fx-background-color:"+UIUtils.bgContent()+";");
        page.getChildren().addAll(UIUtils.heading("📡  Ongoing Exams"),
                UIUtils.subheading("Live exams happening right now — click Join to start"),
                UIUtils.divider());

        List<Exam> live = ExamBank.getLiveExams();
        if (live.isEmpty()) {
            page.getChildren().add(buildPlaceholder("📡","No ongoing exams",
                    "When a teacher starts an exam, it will appear here.", UIUtils.ACCENT_ORG));
        } else {
            for (Exam ex : live) {
                VBox card = UIUtils.card(700); card.setMaxWidth(700);
                card.setPadding(new Insets(18,20,18,20)); card.setSpacing(8);

                HBox topRow = new HBox(12); topRow.setAlignment(Pos.CENTER_LEFT);
                Label liveDot = new Label("🟢"); liveDot.setStyle("-fx-font-size:12px;");
                String et = ex.getTitle()!=null&&!ex.getTitle().isBlank() ? ex.getTitle() : ex.getSubject()+" Exam";
                Label titleLbl = new Label(et);
                titleLbl.setStyle("-fx-font-size:15px;-fx-font-weight:bold;-fx-text-fill:"+UIUtils.textDark()+";");
                Region sp2 = new Region(); HBox.setHgrow(sp2,Priority.ALWAYS);
                Label codeBadge = UIUtils.badge(ex.getExamCode(), UIUtils.ACCENT_GREEN);

                // Remaining time
                String remStr = ex.getLiveEndMillis()>0 ? "⏱ "+ex.getRemainingFormatted()+" left" : "⏱ No time limit";
                Label remLbl = new Label(remStr);
                remLbl.setStyle("-fx-font-size:11px;-fx-text-fill:"+UIUtils.ACCENT_ORG+";-fx-font-weight:bold;");

                topRow.getChildren().addAll(liveDot, titleLbl, sp2, remLbl, codeBadge);

                Label meta = new Label("📚 "+ex.getSubject()+"  •  Grade "+ex.getGrade()
                        +"  •  "+ex.getDuration()+" min  •  "+ex.getQuestionsMap().size()+" questions"
                        +"  •  "+((int)ex.getTotalMarks())+" marks");
                meta.setStyle("-fx-font-size:11px;-fx-text-fill:"+UIUtils.textMid()+";");

                Button btnJoin = buildPremiumBtn("🚀  Join Now", UIUtils.ACCENT_GREEN);
                btnJoin.setPrefWidth(140); btnJoin.setPrefHeight(38);
                btnJoin.setOnAction(e -> showExamDetailPopup(ex, stage, student, app));

                card.getChildren().addAll(topRow, meta, btnJoin);
                page.getChildren().add(card);
            }
        }
        wrapInScroll(contentArea, page);
    }

    // ╔══════════════════════════════════════════════════════╗
    //  SCHEDULED EXAMS — upcoming exams with countdown
    // ╚══════════════════════════════════════════════════════╝
    private static void renderScheduledPage(javafx.scene.layout.AnchorPane contentArea) {
        contentArea.getChildren().clear();
        VBox page = new VBox(22); page.setPadding(new Insets(36,40,36,40));
        page.setStyle("-fx-background-color:"+UIUtils.bgContent()+";");
        page.getChildren().addAll(UIUtils.heading("📅  Scheduled Exams"),
                UIUtils.subheading("Upcoming exams with their countdown timers"),
                UIUtils.divider());

        long now = System.currentTimeMillis();
        List<Exam> scheduled = ExamBank.allExams.stream()
                .filter(e -> !e.isLive() && e.getScheduledStartMillis() > now)
                .sorted(java.util.Comparator.comparingLong(Exam::getScheduledStartMillis))
                .collect(java.util.stream.Collectors.toList());

        if (scheduled.isEmpty()) {
            page.getChildren().add(buildPlaceholder("📅","No upcoming exams",
                    "Your teacher hasn't scheduled any exams yet.", UIUtils.ACCENT_PURP));
        } else {
            for (Exam ex : scheduled) {
                VBox card = UIUtils.card(700); card.setMaxWidth(700);
                card.setPadding(new Insets(18,20,18,20)); card.setSpacing(8);

                HBox topRow = new HBox(12); topRow.setAlignment(Pos.CENTER_LEFT);
                String et = ex.getTitle()!=null&&!ex.getTitle().isBlank() ? ex.getTitle() : ex.getSubject()+" Exam";
                Label titleLbl = new Label("📅  "+et);
                titleLbl.setStyle("-fx-font-size:15px;-fx-font-weight:bold;-fx-text-fill:"+UIUtils.textDark()+";");
                Region sp2 = new Region(); HBox.setHgrow(sp2,Priority.ALWAYS);

                // Live countdown label — updates every second
                Label countdownLbl = new Label(ex.getStartCountdownFormatted());
                countdownLbl.setStyle("-fx-font-family:Monospaced;-fx-font-size:17px;-fx-font-weight:bold;" +
                        "-fx-text-fill:"+UIUtils.ACCENT_PURP+";");
                topRow.getChildren().addAll(titleLbl, sp2,
                        new Label("Starts in ") {{ setStyle("-fx-font-size:12px;-fx-text-fill:"+UIUtils.textMid()+";"); }},
                        countdownLbl);

                Label meta = new Label("📚 "+ex.getSubject()+"  •  Grade "+ex.getGrade()
                        +"  •  "+ex.getDuration()+" min  •  "+ex.getQuestionsMap().size()+" questions");
                meta.setStyle("-fx-font-size:11px;-fx-text-fill:"+UIUtils.textMid()+";");

                // Scheduled time display
                java.time.LocalDateTime ldt = java.time.Instant.ofEpochMilli(ex.getScheduledStartMillis())
                        .atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();
                String dateStr = ldt.format(java.time.format.DateTimeFormatter.ofPattern("EEE, d MMM yyyy  •  HH:mm"));
                Label dateLbl = new Label("🗓  "+dateStr);
                dateLbl.setStyle("-fx-font-size:12px;-fx-font-weight:bold;-fx-text-fill:"+UIUtils.ACCENT_PURP+";");

                // Tick the countdown every second
                Timeline tl = new Timeline(new KeyFrame(Duration.seconds(1), ev ->
                        countdownLbl.setText(ex.getStartCountdownFormatted())));
                tl.setCycleCount(Animation.INDEFINITE); tl.play();
                // Stop ticking when card is removed from scene
                countdownLbl.sceneProperty().addListener((obs,o,n)->{ if(n==null) tl.stop(); });

                card.getChildren().addAll(topRow, dateLbl, meta);
                page.getChildren().add(card);
            }
        }
        wrapInScroll(contentArea, page);
    }

    // ╔══════════════════════════════════════════════════════╗
    //  ANALYTICS — performance chart + subject breakdown
    // ╚══════════════════════════════════════════════════════╝
    private static void renderAnalyticsPage(javafx.scene.layout.AnchorPane contentArea, Student student) {
        contentArea.getChildren().clear();
        VBox page = new VBox(24); page.setPadding(new Insets(36,40,36,40));
        page.setStyle("-fx-background-color:"+UIUtils.bgContent()+";");
        page.getChildren().addAll(UIUtils.heading("📈  Performance Analytics"),
                UIUtils.subheading("Your score trends and subject breakdown"),
                UIUtils.divider());

        List<ExamResult> results = ResultDAO.loadForStudent(student.getID());
        if (results.isEmpty()) {
            page.getChildren().add(buildPlaceholder("📈","No data yet",
                    "Complete some exams to see your analytics.", UIUtils.ACCENT_YELL));
            wrapInScroll(contentArea, page);
            return;
        }

        // ── Overall stats row ─────────────────────────────
        double avgPct = results.stream().mapToDouble(ExamResult::pct).average().orElse(0);
        double bestPct = results.stream().mapToDouble(ExamResult::pct).max().orElse(0);
        long passCount = results.stream().filter(r->r.pct()>=50).count();

        HBox statsRow = new HBox(16); statsRow.setAlignment(Pos.CENTER_LEFT);
        statsRow.getChildren().addAll(
                makeStatCard("📊", String.format("%.1f%%",avgPct),  "Average Score",  UIUtils.ACCENT_BLUE),
                makeStatCard("🏆", String.format("%.1f%%",bestPct), "Best Score",     UIUtils.ACCENT_GREEN),
                makeStatCard("✅", String.valueOf(passCount),        "Exams Passed",   UIUtils.ACCENT_PURP),
                makeStatCard("📝", String.valueOf(results.size()),   "Exams Taken",    UIUtils.ACCENT_ORG)
        );

        // ── Bar chart of recent scores ────────────────────
        VBox chartCard = UIUtils.card(700); chartCard.setMaxWidth(700);
        chartCard.setPadding(new Insets(20)); chartCard.setSpacing(12);
        chartCard.getChildren().add(new Label("📊  Score History (recent 8)") {{
            setStyle("-fx-font-size:14px;-fx-font-weight:bold;-fx-text-fill:"+UIUtils.textDark()+";");
        }});

        List<ExamResult> recent = results.subList(0, Math.min(8, results.size()));
        double maxMark = recent.stream().mapToDouble(r->r.totalMarks).max().orElse(100);

        // Simple bar chart using HBoxes
        double BAR_MAX_W = 420;
        for (int i = recent.size()-1; i >= 0; i--) {
            ExamResult r = recent.get(i);
            String gc = r.pct()>=65 ? UIUtils.ACCENT_GREEN : r.pct()>=50 ? UIUtils.ACCENT_BLUE : UIUtils.ACCENT_RED;
            String lblText = (r.examTitle!=null&&!r.examTitle.isBlank()?r.examTitle:r.examSubject);
            if (lblText.length()>22) lblText = lblText.substring(0,20)+"…";

            Label nameLbl = new Label(lblText);
            nameLbl.setStyle("-fx-font-size:11px;-fx-text-fill:"+UIUtils.textMid()+";");
            nameLbl.setMinWidth(150); nameLbl.setMaxWidth(150);

            double barW = BAR_MAX_W * (r.score / maxMark);
            Region bar = new Region();
            bar.setPrefWidth(Math.max(barW,6)); bar.setPrefHeight(22);
            bar.setStyle("-fx-background-color:"+gc+";-fx-background-radius:4;");

            Label valLbl = new Label(String.format("%.0f%%", r.pct()));
            valLbl.setStyle("-fx-font-size:11px;-fx-font-weight:bold;-fx-text-fill:"+gc+";");

            HBox barRow = new HBox(8, nameLbl, bar, valLbl);
            barRow.setAlignment(Pos.CENTER_LEFT);
            chartCard.getChildren().add(barRow);
        }

        // ── Subject breakdown ─────────────────────────────
        VBox subCard = UIUtils.card(700); subCard.setMaxWidth(700);
        subCard.setPadding(new Insets(20)); subCard.setSpacing(10);
        subCard.getChildren().add(new Label("📚  By Subject") {{
            setStyle("-fx-font-size:14px;-fx-font-weight:bold;-fx-text-fill:"+UIUtils.textDark()+";");
        }});

        Map<String,List<ExamResult>> bySub = new java.util.LinkedHashMap<>();
        for (ExamResult r : results) bySub.computeIfAbsent(r.examSubject, k->new ArrayList<>()).add(r);
        String[] subColors = {UIUtils.ACCENT_BLUE,UIUtils.ACCENT_GREEN,UIUtils.ACCENT_PURP,UIUtils.ACCENT_ORG,UIUtils.ACCENT_YELL};
        int ci = 0;
        for (Map.Entry<String,List<ExamResult>> e : bySub.entrySet()) {
            String c = subColors[ci++ % subColors.length];
            double avg = e.getValue().stream().mapToDouble(ExamResult::pct).average().orElse(0);
            HBox sr = new HBox(12); sr.setAlignment(Pos.CENTER_LEFT);
            Circle dot = new Circle(6); dot.setFill(Color.web(c));
            Label subLbl = new Label(e.getKey()); subLbl.setMinWidth(140);
            subLbl.setStyle("-fx-font-size:12px;-fx-font-weight:bold;-fx-text-fill:"+UIUtils.textDark()+";");
            javafx.scene.control.ProgressBar pb2 = new javafx.scene.control.ProgressBar(avg/100);
            pb2.setPrefWidth(200); pb2.setPrefHeight(10);
            pb2.setStyle("-fx-accent:"+c+";-fx-background-color:"+UIUtils.border()+";-fx-background-radius:5;");
            Label avgLbl = new Label(String.format("%.1f%% avg  (%d exams)",avg,e.getValue().size()));
            avgLbl.setStyle("-fx-font-size:11px;-fx-text-fill:"+UIUtils.textMid()+";");
            sr.getChildren().addAll(dot, subLbl, pb2, avgLbl);
            subCard.getChildren().add(sr);
        }

        page.getChildren().addAll(statsRow, chartCard, subCard);
        wrapInScroll(contentArea, page);
    }

    // ╔══════════════════════════════════════════════════════╗
    //  LEADERBOARD — top students per exam
    // ╚══════════════════════════════════════════════════════╝
    private static void renderLeaderboardPage(javafx.scene.layout.AnchorPane contentArea, Student student) {
        contentArea.getChildren().clear();
        VBox page = new VBox(22); page.setPadding(new Insets(36,40,36,40));
        page.setStyle("-fx-background-color:"+UIUtils.bgContent()+";");
        page.getChildren().addAll(UIUtils.heading("🏆  Leaderboard"),
                UIUtils.subheading("Top scores for each exam"), UIUtils.divider());

        // Group all results by examId
        List<ExamResult> all = ResultDAO.loadAll();
        if (all.isEmpty()) {
            page.getChildren().add(buildPlaceholder("🏆","No scores yet",
                    "Be the first to complete an exam!", "#f43f5e"));
            wrapInScroll(contentArea, page);
            return;
        }

        Map<Integer, List<ExamResult>> byExam = new java.util.LinkedHashMap<>();
        // Keep only best per student per exam
        for (ExamResult r : all) {
            byExam.computeIfAbsent(r.examId, k->new ArrayList<>()).add(r);
        }

        for (Map.Entry<Integer,List<ExamResult>> entry : byExam.entrySet()) {
            List<ExamResult> top = entry.getValue().stream()
                    .sorted((a,b)->Double.compare(b.score,a.score))
                    .limit(10)
                    .collect(java.util.stream.Collectors.toList());

            ExamResult first = top.get(0);
            String examLabel = first.examTitle!=null&&!first.examTitle.isBlank()
                    ? first.examTitle : first.examSubject;

            VBox card = UIUtils.card(700); card.setMaxWidth(700);
            card.setPadding(new Insets(18,20,18,20)); card.setSpacing(8);

            Label examHdr = new Label("🏆  "+examLabel+"  —  "+first.examSubject+" (Grade "+first.examGrade+")");
            examHdr.setStyle("-fx-font-size:14px;-fx-font-weight:bold;-fx-text-fill:"+UIUtils.textDark()+";");
            card.getChildren().add(examHdr);
            card.getChildren().add(UIUtils.divider());

            String[] medals = {"🥇","🥈","🥉"};
            for (int rank = 0; rank < top.size(); rank++) {
                ExamResult r = top.get(rank);
                String gc = r.pct()>=65?UIUtils.ACCENT_GREEN:r.pct()>=50?UIUtils.ACCENT_BLUE:UIUtils.ACCENT_RED;

                HBox row = new HBox(12); row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(6,10,6,10));

                // Highlight current student
                boolean isMe = r.studentId.equals(student.getID());
                if (isMe) row.setStyle("-fx-background-color:"+UIUtils.ACCENT_GREEN+"15;-fx-background-radius:8;");

                Label rankLbl = new Label(rank<3 ? medals[rank] : "#"+(rank+1));
                rankLbl.setStyle("-fx-font-size:16px;"); rankLbl.setMinWidth(36);

                Label idLbl = new Label(isMe ? "You ("+r.studentId+")" : r.studentId);
                idLbl.setStyle("-fx-font-size:13px;-fx-font-weight:"+(isMe?"bold":"normal")
                        +";-fx-text-fill:"+(isMe?UIUtils.ACCENT_GREEN:UIUtils.textDark())+";");
                idLbl.setMinWidth(160);

                Region sp2 = new Region(); HBox.setHgrow(sp2,Priority.ALWAYS);

                javafx.scene.control.ProgressBar pb = new javafx.scene.control.ProgressBar(r.pct()/100);
                pb.setPrefWidth(140); pb.setPrefHeight(8);
                pb.setStyle("-fx-accent:"+gc+";-fx-background-color:"+UIUtils.border()+";-fx-background-radius:4;");

                Label scoreLbl = new Label(String.format("%.0f / %.0f  (%.1f%%)", r.score, r.totalMarks, r.pct()));
                scoreLbl.setStyle("-fx-font-size:12px;-fx-font-weight:bold;-fx-text-fill:"+gc+";");
                scoreLbl.setMinWidth(150);

                row.getChildren().addAll(rankLbl, idLbl, sp2, pb, scoreLbl);
                card.getChildren().add(row);
            }
            page.getChildren().add(card);
        }
        wrapInScroll(contentArea, page);
    }

    // ╔══════════════════════════════════════════════════════╗
    //  ANNOUNCEMENTS PAGE
    // ╚══════════════════════════════════════════════════════╝
    private static void renderAnnouncementsPage(javafx.scene.layout.AnchorPane contentArea) {
        contentArea.getChildren().clear();

        // Clean expired before showing
        ResultDAO.deleteExpired();

        VBox page = new VBox(18); page.setPadding(new Insets(36,40,36,40));
        page.setStyle("-fx-background-color:"+UIUtils.bgContent()+";");
        page.getChildren().addAll(UIUtils.heading("📢  Announcements"),
                UIUtils.subheading("Notices and updates from your teachers"),
                UIUtils.divider());

        List<Announcement> list = ResultDAO.loadAnnouncements();
        if (list.isEmpty()) {
            page.getChildren().add(buildPlaceholder("📢","No announcements",
                    "Your teacher hasn't posted any notices yet.", UIUtils.ACCENT_BLUE));
        } else {
            for (Announcement a : list) {
                VBox card = new VBox(6);
                card.setPadding(new Insets(16,20,16,20));
                card.setStyle("-fx-background-color:"+UIUtils.bgCard()+";" +
                        "-fx-border-color:"+a.color+"55;-fx-border-width:0 0 0 4;" +
                        "-fx-background-radius:10;-fx-border-radius:10;");
                DropShadow ds = new DropShadow(); ds.setColor(Color.color(0,0,0,0.05));
                ds.setRadius(8); ds.setOffsetY(2); card.setEffect(ds);

                HBox hdr = new HBox(10); hdr.setAlignment(Pos.CENTER_LEFT);
                Label titleLbl = new Label(a.title);
                titleLbl.setStyle("-fx-font-size:14px;-fx-font-weight:bold;-fx-text-fill:"+UIUtils.textDark()+";");
                Region sp2 = new Region(); HBox.setHgrow(sp2,Priority.ALWAYS);

                // Expiry info for student
                if (a.expireAt > 0) {
                    Label expLbl = new Label("⏰ "+a.expireStr());
                    expLbl.setStyle("-fx-font-size:10px;-fx-text-fill:"+UIUtils.ACCENT_ORG+";" +
                            "-fx-background-color:"+UIUtils.ACCENT_ORG+"18;-fx-padding:2 8;-fx-background-radius:6;");
                    hdr.getChildren().addAll(titleLbl, sp2, expLbl);
                } else {
                    Label dateLbl = new Label(a.dateStr());
                    dateLbl.setStyle("-fx-font-size:11px;-fx-text-fill:"+UIUtils.textSubtle()+";");
                    hdr.getChildren().addAll(titleLbl, sp2, dateLbl);
                }

                Label bodyLbl = new Label(a.body);
                bodyLbl.setStyle("-fx-font-size:13px;-fx-text-fill:"+UIUtils.textMid()+";");
                bodyLbl.setWrapText(true);

                card.getChildren().addAll(hdr, bodyLbl);
                page.getChildren().add(card);
            }
        }

        // Auto-refresh every 60 s so expired notices disappear without restart
        ScrollPane sp = new ScrollPane(page);
        sp.setFitToWidth(true); sp.setStyle("-fx-background:transparent;-fx-background-color:transparent;");
        AnchorPane.setTopAnchor(sp,0.0); AnchorPane.setBottomAnchor(sp,0.0);
        AnchorPane.setLeftAnchor(sp,0.0); AnchorPane.setRightAnchor(sp,0.0);
        contentArea.getChildren().add(sp);

        Timeline autoRefresh = new Timeline(new KeyFrame(Duration.seconds(60), ev ->
                renderAnnouncementsPage(contentArea)));
        autoRefresh.setCycleCount(Animation.INDEFINITE); autoRefresh.play();
        sp.sceneProperty().addListener((obs,o,n) -> { if (n==null) autoRefresh.stop(); });

        UIUtils.slideIn(page, true);
    }

    private static void wrapInScroll(javafx.scene.layout.AnchorPane area, VBox page) {
        ScrollPane sp = new ScrollPane(page);
        sp.setFitToWidth(true); sp.setStyle("-fx-background:transparent;-fx-background-color:transparent;");
        AnchorPane.setTopAnchor(sp,0.0); AnchorPane.setBottomAnchor(sp,0.0);
        AnchorPane.setLeftAnchor(sp,0.0); AnchorPane.setRightAnchor(sp,0.0);
        area.getChildren().add(sp); UIUtils.slideIn(page,true);
    }

    private static VBox makeStatCard(String icon, String value, String label, String color) {
        VBox card = new VBox(4); card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(14,20,14,20));
        card.setStyle("-fx-background-color:"+UIUtils.bgCard()+";-fx-background-radius:12;" +
                "-fx-border-color:"+color+"33;-fx-border-radius:12;-fx-border-width:1;");
        DropShadow ds2 = new DropShadow(); ds2.setColor(Color.web(color,0.12)); ds2.setRadius(8); ds2.setOffsetY(2);
        card.setEffect(ds2);
        card.getChildren().addAll(
                new Label(icon)  {{ setStyle("-fx-font-size:20px;"); }},
                new Label(value) {{ setStyle("-fx-font-size:18px;-fx-font-weight:bold;-fx-text-fill:"+color+";"); }},
                new Label(label) {{ setStyle("-fx-font-size:10px;-fx-text-fill:"+UIUtils.textMid()+";"); }}
        );
        return card;
    }

    private static VBox buildPlaceholder(String icon, String title, String sub, String color) {
        VBox c = UIUtils.card(500); c.setMaxWidth(500); c.setAlignment(Pos.CENTER); c.setPadding(new Insets(44));
        c.getChildren().addAll(
                new Label(icon)  {{ setStyle("-fx-font-size:44px;"); }},
                new Label(title) {{ setStyle("-fx-font-size:17px;-fx-font-weight:bold;-fx-text-fill:"+UIUtils.textDark()+";"); }},
                new Label(sub)   {{ setStyle("-fx-font-size:13px;-fx-text-fill:"+UIUtils.textMid()+";"); setWrapText(true); }}
        );
        return c;
    }

    // ╔══════════════════════════════════════════════════════╗
    //  STYLE HELPERS
    // ╚══════════════════════════════════════════════════════╝
    private static Button buildPremiumBtn(String text, String color) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color:linear-gradient(to bottom right,"+color+","+darken(color)+");" +
                "-fx-text-fill:white;-fx-font-weight:bold;-fx-font-size:13px;" +
                "-fx-background-radius:10;-fx-cursor:hand;-fx-padding:10 20;");
        DropShadow ds = new DropShadow(); ds.setColor(Color.web(color,0.38)); ds.setOffsetY(4); ds.setRadius(12); b.setEffect(ds);
        b.setOnMouseEntered(e -> { b.setTranslateY(-2); ds.setOffsetY(7); ds.setRadius(18); });
        b.setOnMouseExited(e  -> { b.setTranslateY(0);  ds.setOffsetY(4); ds.setRadius(12); });
        b.setOnMousePressed(e  -> { b.setTranslateY(1);  ds.setOffsetY(2); });
        b.setOnMouseReleased(e -> { b.setTranslateY(-1); ds.setOffsetY(7); });
        return b;
    }

    // Flag button style — adapts to dark/light
    private static String flagStyle(boolean active, boolean dark) {
        if (active) return "-fx-background-color:#f59e0b22;-fx-text-fill:#f59e0b;-fx-font-weight:bold;-fx-font-size:12px;-fx-background-radius:7;-fx-padding:5 12;-fx-cursor:hand;-fx-border-color:#f59e0b;-fx-border-width:1;-fx-border-radius:7;";
        String base = dark?"#2d3748":"#f1f5f9", txt = dark?"#94a3b8":"#64748b";
        return "-fx-background-color:"+base+";-fx-text-fill:"+txt+";-fx-font-size:12px;-fx-background-radius:7;-fx-padding:5 12;-fx-cursor:hand;-fx-border-color:transparent;";
    }

    // Nav button — only green (answered) and amber (flagged), otherwise plain
    private static String qNavStyle(String color, boolean ignored, boolean dark) {
        boolean isSpecial = color.equals("#10b981") || color.equals("#f59e0b");
        String txtColor = isSpecial ? "white" : (dark ? "#8b949e" : "#64748b");
        String bg = isSpecial ? color : (dark ? "#2d3748" : "#e2e8f0");
        return "-fx-background-color:"+bg+";-fx-text-fill:"+txtColor+";-fx-font-weight:"+(isSpecial?"bold":"normal")+";-fx-font-size:11px;-fx-background-radius:6;-fx-cursor:hand;";
    }

    private static VBox makeStatBox(String icon, String value, String label, String color) {
        VBox b = new VBox(3); b.setAlignment(Pos.CENTER);
        b.getChildren().addAll(
                new Label(icon)  {{ setStyle("-fx-font-size:18px;"); }},
                new Label(value) {{ setStyle("-fx-font-size:18px;-fx-font-weight:bold;-fx-text-fill:"+color+";"); }},
                new Label(label) {{ setStyle("-fx-font-size:10px;-fx-text-fill:"+UIUtils.textMid()+";"); }}
        );
        return b;
    }

    private static String darken(String hex) {
        try { Color c=Color.web(hex); return String.format("#%02x%02x%02x",(int)(c.getRed()*195),(int)(c.getGreen()*195),(int)(c.getBlue()*195)); }
        catch (Exception e) { return hex; }
    }

    private static String formatTime(int s) {
        int h=s/3600, m=(s%3600)/60, sec=s%60;
        return h>0?String.format("%02d:%02d:%02d",h,m,sec):String.format("%02d:%02d",m,sec);
    }

    private static void shakeNode(javafx.scene.Node node) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(55), node);
        tt.setFromX(0); tt.setByX(10); tt.setCycleCount(6); tt.setAutoReverse(true);
        tt.setOnFinished(e -> node.setTranslateX(0)); tt.play();
    }

    private static void animatePopupIn(javafx.scene.Node node) {
        node.setScaleX(0.88); node.setScaleY(0.88); node.setOpacity(0);
        ScaleTransition st = new ScaleTransition(Duration.millis(260), node);
        st.setToX(1); st.setToY(1); st.setInterpolator(Interpolator.EASE_OUT);
        FadeTransition ft = new FadeTransition(Duration.millis(260), node); ft.setToValue(1);
        new ParallelTransition(st,ft).play();
    }
}