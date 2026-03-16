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
        Button btnLogin = UIUtils.primaryBtn("🔑", "Sign In", UIUtils.ACCENT_GREEN);
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

        Button btnReg  = UIUtils.primaryBtn("✅", "Create Account", UIUtils.ACCENT_GREEN);
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
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color:" + UIUtils.bgContent() + ";");

        javafx.scene.layout.AnchorPane contentArea = new javafx.scene.layout.AnchorPane();
        contentArea.setPrefSize(890, 660);
        contentArea.setStyle("-fx-background-color:" + UIUtils.bgContent() + ";");

        VBox sidebar = new VBox(8);
        sidebar.setPrefWidth(210);
        sidebar.setPadding(new Insets(0, 10, 20, 10));
        sidebar.setStyle("-fx-background-color:" + UIUtils.BG_DARK + ";");

        // Theme toggle — top of sidebar, matches TeacherPortal
        StackPane themeSwitch = UIUtils.themeToggleSwitch(() ->
                stage.setScene(createDashboardScene(stage, student, app))
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

        sidebar.getChildren().addAll(switchRow, avatarBox);
        Separator sep = new Separator();
        sep.setStyle("-fx-background-color:#1e293b;"); sep.setPadding(new Insets(4,0,12,0));
        sidebar.getChildren().add(sep);

        String[][] nav = {
                {"🎯", "Join Exam",  UIUtils.ACCENT_GREEN},
                {"📊", "My Results", UIUtils.ACCENT_BLUE},
                {"📡", "Ongoing",    UIUtils.ACCENT_ORG},
        };
        StackPane[] navBtns = new StackPane[nav.length];
        for (int i = 0; i < nav.length; i++) {
            final int idx = i;
            navBtns[i] = UIUtils.modernSidebarBtn(nav[i][0], nav[i][1], nav[i][2]);
            navBtns[i].setOnMouseClicked(e -> {
                activeNavIndex = idx;
                for (StackPane nb : navBtns) UIUtils.modernSidebarSetInactive(nb);
                UIUtils.modernSidebarSetActive(navBtns[idx]);
                switch (idx) {
                    case 0 -> renderJoinExamPage(contentArea, stage, student, app);
                    case 1 -> renderMyResultsPage(contentArea);
                    case 2 -> renderOngoingPage(contentArea);
                }
            });
            sidebar.getChildren().add(navBtns[i]);
        }

        Region spacer = new Region(); VBox.setVgrow(spacer, Priority.ALWAYS);
        Button btnLogout = UIUtils.primaryBtn("🚪", "Log Out", UIUtils.ACCENT_RED);
        btnLogout.setPrefWidth(190);
        btnLogout.setOnAction(e -> {
            Alert c = new Alert(Alert.AlertType.CONFIRMATION);
            c.setTitle("Log Out"); c.setHeaderText("Are you sure you want to log out?");
            c.setContentText("You will be returned to the home screen.");
            c.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
            c.showAndWait().ifPresent(r -> { if (r == ButtonType.YES) stage.setScene(app.createMainScene(stage)); });
        });
        sidebar.getChildren().addAll(spacer, btnLogout);

        root.setLeft(sidebar); root.setCenter(contentArea);

        UIUtils.modernSidebarSetActive(navBtns[activeNavIndex]);
        switch (activeNavIndex) {
            case 0 -> renderJoinExamPage(contentArea, stage, student, app);
            case 1 -> renderMyResultsPage(contentArea);
            case 2 -> renderOngoingPage(contentArea);
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
            if (!n.equals(up)) { String fin = up; Platform.runLater(() -> codeField.setText(fin)); }
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
                    // Update nav button
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
                    answers.put(idx, n);
                    if (navBtns[idx]!=null) navBtns[idx].setStyle(n.isEmpty() ? qNavStyle(bdColor, false, UIUtils.darkMode) : qNavStyle("#10b981", false, UIUtils.darkMode));
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
                if      (flagged.contains(i))    navBtns[i].setStyle(qNavStyle("#f59e0b", false, UIUtils.darkMode));
                else if (answers.containsKey(i)) navBtns[i].setStyle(qNavStyle("#10b981", false, UIUtils.darkMode));
                else                             navBtns[i].setStyle(qNavStyle(bdColor,   false, UIUtils.darkMode));
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
                if (ans==null) continue;
                if (q instanceof MCQ mcq && ans.charAt(0)-'A'==mcq.getCorrectIndex()) {
                    correct++; score += exam.getQuestionsMap().get(q);
                }
            }
            final int fc=correct; final double fs=score;
            Platform.runLater(() -> showResultScene(stage, exam, student, app, fc, totalQ, fs));
        };

        btnSubmit.setOnAction(e -> {
            int unanswered = totalQ - answers.size();
            if (unanswered > 0) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Submit Exam");
                confirm.setHeaderText(unanswered+" question"+(unanswered>1?"s":"")+" unanswered.");
                confirm.setContentText("Unanswered questions will score 0. Submit anyway?");
                confirm.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
                confirm.showAndWait().ifPresent(r -> { if (r==ButtonType.YES) doSubmit.run(); });
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
    //  7. RESULT SCENE  — compact, scrollable, fits window
    // ╚══════════════════════════════════════════════════════╝
    private static void showResultScene(Stage stage, Exam exam, Student student,
                                        HelloApplication app, int correct, int total, double score) {
        double pct    = total>0 ? (score/exam.getTotalMarks())*100 : 0;
        String grade  = pct>=80?"A":pct>=65?"B":pct>=50?"C":pct>=35?"D":"F";
        String emoji  = pct>=80?"🎉":pct>=65?"👍":pct>=50?"✅":"📚";
        String gcolor = pct>=65?UIUtils.ACCENT_GREEN:pct>=50?UIUtils.ACCENT_BLUE:UIUtils.ACCENT_RED;

        // Outer scroll so nothing is clipped
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color:"+UIUtils.bgContent()+";");

        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background:transparent;-fx-background-color:transparent;");

        VBox center = new VBox(20); center.setAlignment(Pos.TOP_CENTER);
        center.setPadding(new Insets(40,60,40,60));
        center.setStyle("-fx-background-color:"+UIUtils.bgContent()+";");

        // Compact header row
        HBox headerRow = new HBox(14); headerRow.setAlignment(Pos.CENTER);
        Label emojiLbl = new Label(emoji); emojiLbl.setStyle("-fx-font-size:42px;");
        VBox headerText = new VBox(4);
        headerText.setAlignment(Pos.CENTER_LEFT);
        Label titleLbl = new Label("Exam Submitted!");
        titleLbl.setStyle("-fx-font-size:22px;-fx-font-weight:bold;-fx-text-fill:"+UIUtils.textDark()+";");
        String etitle = exam.getTitle()!=null&&!exam.getTitle().isBlank()?exam.getTitle():exam.getSubject();
        Label subtitleLbl = new Label(etitle+"  •  "+exam.getSubject());
        subtitleLbl.setStyle("-fx-font-size:13px;-fx-text-fill:"+UIUtils.textMid()+";");
        headerText.getChildren().addAll(titleLbl, subtitleLbl);
        headerRow.getChildren().addAll(emojiLbl, headerText);

        // Compact score card
        HBox scoreRow = new HBox(24); scoreRow.setAlignment(Pos.CENTER);
        scoreRow.setPadding(new Insets(22,32,22,32));
        scoreRow.setMaxWidth(520);
        scoreRow.setStyle("-fx-background-color:"+UIUtils.bgCard()+";-fx-background-radius:14;" +
                "-fx-border-radius:14;-fx-border-color:"+gcolor+"44;-fx-border-width:2;");
        scoreRow.setEffect(new DropShadow(16, Color.web(gcolor,0.18)));

        // Score circle — smaller
        StackPane scoreCircle = new StackPane();
        Circle circleBg = new Circle(48); circleBg.setFill(Color.web(gcolor,0.13));
        circleBg.setStroke(Color.web(gcolor)); circleBg.setStrokeWidth(2.5);
        VBox inner = new VBox(1); inner.setAlignment(Pos.CENTER);
        Label scoreLbl = new Label(String.format("%.0f",score));
        scoreLbl.setStyle("-fx-font-size:22px;-fx-font-weight:bold;-fx-text-fill:"+gcolor+";");
        Label outOfLbl = new Label("/"+((int)exam.getTotalMarks()));
        outOfLbl.setStyle("-fx-font-size:11px;-fx-text-fill:"+UIUtils.textMid()+";");
        inner.getChildren().addAll(scoreLbl, outOfLbl);
        scoreCircle.getChildren().addAll(circleBg, inner);

        // Grade + stats stacked
        VBox rightSide = new VBox(12); rightSide.setAlignment(Pos.CENTER_LEFT);
        Label gradeLbl = new Label("Grade  "+grade);
        gradeLbl.setStyle("-fx-font-size:20px;-fx-font-weight:bold;-fx-text-fill:"+gcolor+";" +
                "-fx-background-color:"+gcolor+"22;-fx-padding:4 16;-fx-background-radius:8;");

        HBox stats = new HBox(20); stats.setAlignment(Pos.CENTER_LEFT);
        stats.getChildren().addAll(
                makeStatBox("✅", String.valueOf(correct),       "Correct",  UIUtils.ACCENT_GREEN),
                makeStatBox("❌", String.valueOf(total-correct), "Wrong",    UIUtils.ACCENT_RED),
                makeStatBox("📊", String.format("%.1f%%",pct),  "Score",    UIUtils.ACCENT_BLUE)
        );
        rightSide.getChildren().addAll(gradeLbl, stats);
        scoreRow.getChildren().addAll(scoreCircle, rightSide);

        // Back button — always visible
        Button btnBack = buildPremiumBtn("← Back to Dashboard", UIUtils.ACCENT_GREEN);
        btnBack.setPrefWidth(240); btnBack.setPrefHeight(46);
        btnBack.setOnAction(e -> { activeNavIndex=0; stage.setScene(createDashboardScene(stage,student,app)); });

        center.getChildren().addAll(headerRow, scoreRow, btnBack);

        // Animate in
        center.setOpacity(0); center.setTranslateY(20);
        scroll.setContent(center);
        root.setCenter(scroll);

        Scene scene = new Scene(root, 1100, 660);
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
    private static void renderMyResultsPage(javafx.scene.layout.AnchorPane contentArea) {
        contentArea.getChildren().clear();
        VBox page = new VBox(24); page.setPadding(new Insets(36,40,36,40));
        page.setStyle("-fx-background-color:"+UIUtils.bgContent()+";");
        page.getChildren().addAll(UIUtils.heading("📊  My Results"),
                UIUtils.subheading("Your exam history and scores"), UIUtils.divider(),
                buildPlaceholder("📂","No results yet","Complete an exam to see your results here.",UIUtils.ACCENT_BLUE));
        wrapInScroll(contentArea, page);
    }

    private static void renderOngoingPage(javafx.scene.layout.AnchorPane contentArea) {
        contentArea.getChildren().clear();
        VBox page = new VBox(24); page.setPadding(new Insets(36,40,36,40));
        page.setStyle("-fx-background-color:"+UIUtils.bgContent()+";");
        page.getChildren().addAll(UIUtils.heading("📡  Ongoing Exams"),
                UIUtils.subheading("Live exams happening right now"), UIUtils.divider(),
                buildPlaceholder("📡","No ongoing exams","When a teacher starts an exam, it will appear here.",UIUtils.ACCENT_ORG));
        wrapInScroll(contentArea, page);
    }

    private static void wrapInScroll(javafx.scene.layout.AnchorPane area, VBox page) {
        ScrollPane sp = new ScrollPane(page);
        sp.setFitToWidth(true); sp.setStyle("-fx-background:transparent;-fx-background-color:transparent;");
        AnchorPane.setTopAnchor(sp,0.0); AnchorPane.setBottomAnchor(sp,0.0);
        AnchorPane.setLeftAnchor(sp,0.0); AnchorPane.setRightAnchor(sp,0.0);
        area.getChildren().add(sp); UIUtils.slideIn(page,true);
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