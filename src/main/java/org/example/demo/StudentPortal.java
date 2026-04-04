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
import java.util.stream.Collectors;

public class StudentPortal {

    static int activeNavIndex = 0;
    private static Timeline notificationTimeline;
    private static int lastUnreadMessageCount = -1;
    private static int lastUnreadAnnouncementCount = -1;

    private static VBox buildAuthPanel(String title, String sub) {
        VBox v = new VBox(0);
        v.setPrefWidth(320);
        v.setAlignment(Pos.CENTER_LEFT);
        v.setPadding(new Insets(0, 0, 0, 44));
        v.setStyle("-fx-background-color:#111722;");

        Region rule = new Region(); rule.setPrefSize(32, 3);
        rule.setStyle("-fx-background-color:#0f7d74;-fx-background-radius:99;");
        VBox.setMargin(rule, new Insets(0, 0, 20, 0));

        StackPane iconBox = new StackPane(UIUtils.icon(UIUtils.ICO_STUDENT, "#0f7d74", 24));
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

    public static Scene createLoginScene(Stage stage, HelloApplication app) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color:" + UIUtils.bgContent() + ";");
        root.setLeft(buildAuthPanel("Student Portal", "Sign in to access live examinations and your academic record."));

        VBox form = new VBox(16);
        form.setAlignment(Pos.CENTER);
        form.setPadding(new Insets(60, 68, 60, 68));
        form.setMaxWidth(420);

        Label title = new Label("Sign In");
        title.setStyle("-fx-font-size:24px;-fx-font-weight:700;-fx-text-fill:" + UIUtils.textDark() + ";-fx-letter-spacing:-0.3px;");
        Label sub = new Label("Enter your credentials to continue");
        sub.setStyle("-fx-font-size:13px;-fx-text-fill:" + UIUtils.textMid() + ";");

        TextField     txtID = UIUtils.styledField("Student ID or Email");
        PasswordField txtPw = UIUtils.styledPassword("Password");

        Button btnLogin = UIUtils.primaryBtn("", "Sign In", UIUtils.ACCENT_GREEN);
        btnLogin.setPrefWidth(Double.MAX_VALUE); btnLogin.setPrefHeight(42);

        Hyperlink linkSignup = new Hyperlink("New student? Create an account");
        UIUtils.applyLinkEffects(linkSignup);
        Button btnBack = UIUtils.ghostBtn("", "Back", UIUtils.TEXT_MID);

        btnLogin.setOnAction(e -> {
            String in = txtID.getText().trim(), pw = txtPw.getText();
            if (in.isEmpty() || pw.isEmpty()) { app.showError("Missing Fields", "Please enter your ID/email and password."); return; }
            Student found = ServerClient.get().studentLogin(in, pw);
            if (found != null) {
                stage.setScene(createDashboardScene(stage, found, app));
            } else { app.showError("Login Failed", "Invalid credentials. Please try again."); }
        });
        linkSignup.setOnAction(e -> stage.setScene(createSignupScene(stage, app)));
        btnBack.setOnAction(e -> stage.setScene(app.createMainScene(stage)));
        txtPw.setOnAction(e -> btnLogin.fire());

        String lblS = "-fx-font-size:10px;-fx-font-weight:700;-fx-text-fill:" + UIUtils.textSubtle() + ";-fx-letter-spacing:1.2px;";
        form.getChildren().addAll(
                title, sub, UIUtils.divider(),
                new Label("STUDENT ID / EMAIL") {{ setStyle(lblS); }}, txtID,
                new Label("PASSWORD") {{ setStyle(lblS); }}, txtPw,
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

    public static Scene createSignupScene(Stage stage, HelloApplication app) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color:" + UIUtils.bgContent() + ";");
        root.setLeft(buildAuthPanel("Create Account", "Register to join EduExam and sit your first examination."));

        VBox form = new VBox(13);
        form.setAlignment(Pos.CENTER);
        form.setPadding(new Insets(48, 68, 48, 68));
        form.setMaxWidth(420);

        Label title = new Label("Student Registration");
        title.setStyle("-fx-font-size:22px;-fx-font-weight:700;-fx-text-fill:" + UIUtils.textDark() + ";-fx-letter-spacing:-0.3px;");

        TextField     txtID      = UIUtils.styledField("Student ID (numbers only)");
        TextField     txtName    = UIUtils.styledField("Full Name");
        TextField     txtEmail   = UIUtils.styledField("Email Address");
        PasswordField txtPass    = UIUtils.styledPassword("Create Password");
        PasswordField txtConfirm = UIUtils.styledPassword("Confirm Password");

        txtID.textProperty().addListener((obs, o, n) -> {
            if (!n.matches("\\d*")) txtID.setText(n.replaceAll("[^\\d]", ""));
        });

        Button btnReg  = UIUtils.primaryBtn("", "Create Account", UIUtils.ACCENT_GREEN);
        btnReg.setPrefWidth(Double.MAX_VALUE); btnReg.setPrefHeight(42);
        Button btnBack = UIUtils.ghostBtn("", "Back to Sign In", UIUtils.TEXT_MID);

        btnReg.setOnAction(e -> {
            String id = txtID.getText().trim(), name = txtName.getText().trim();
            String email = txtEmail.getText().trim(), pass = txtPass.getText(), confirm = txtConfirm.getText();
            if (id.isEmpty() || name.isEmpty() || email.isEmpty() || pass.isEmpty()) { app.showError("Missing Info", "Please fill in all fields."); return; }
            if (!id.matches("\\d+")) { app.showError("Invalid ID", "Student ID must contain digits only."); return; }
            if (!pass.equals(confirm)) { app.showError("Mismatch", "Passwords do not match."); return; }
            if (ServerClient.get().studentIdExists(id)) { app.showError("ID Taken", "A student with ID " + id + " already exists."); return; }
            if (ServerClient.get().studentRegister(id, name, email, pass)) {
                app.showInfo("Account Created", "You may now sign in with your credentials.");
                stage.setScene(createLoginScene(stage, app));
            } else { app.showError("Error", "Registration failed. Please try again."); }
        });
        btnBack.setOnAction(e -> stage.setScene(createLoginScene(stage, app)));

        String lblS = "-fx-font-size:10px;-fx-font-weight:700;-fx-text-fill:" + UIUtils.textSubtle() + ";-fx-letter-spacing:1.2px;";
        form.getChildren().addAll(
                title, UIUtils.divider(),
                new Label("STUDENT ID")  {{ setStyle(lblS); }}, txtID,
                new Label("FULL NAME")   {{ setStyle(lblS); }}, txtName,
                new Label("EMAIL")       {{ setStyle(lblS); }}, txtEmail,
                new Label("PASSWORD")    {{ setStyle(lblS); }}, txtPass,
                new Label("CONFIRM")     {{ setStyle(lblS); }}, txtConfirm,
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

    public static Scene createDashboardScene(Stage stage, Student student, HelloApplication app) {
        return createDashboardScene(stage, student, app, activeNavIndex);
    }

    public static Scene createDashboardScene(Stage stage, Student student, HelloApplication app, int startPage) {
        stopNotificationTimeline();
        BorderPane root = new BorderPane();

        VBox sidebar = new VBox(0);
        sidebar.setPrefWidth(210);
        sidebar.setStyle("-fx-background-color:#111722;");

        StackPane themeSwitch = UIUtils.themeToggleSwitch(() ->
                stage.setScene(createDashboardScene(stage, student, app, activeNavIndex))
        );
        HBox switchRow = new HBox(themeSwitch);
        switchRow.setAlignment(Pos.CENTER_LEFT);
        switchRow.setPadding(new Insets(14, 0, 0, 14));

        VBox avatarBlock = new VBox(6);
        avatarBlock.setAlignment(Pos.CENTER_LEFT);
        avatarBlock.setPadding(new Insets(18, 14, 16, 16));

        String initials = student.getName().substring(0,1).toUpperCase();
        StackPane av = new StackPane();
        Circle avCircle = new Circle(22, Color.web("#0f7d74", 0.18));
        avCircle.setStroke(Color.web("#0f7d74", 0.4)); avCircle.setStrokeWidth(1.5);
        Label avLbl = new Label(initials);
        avLbl.setStyle("-fx-font-size:16px;-fx-font-weight:700;-fx-text-fill:#0f7d74;");
        av.getChildren().addAll(avCircle, avLbl); av.setPrefSize(44, 44);

        Label nameL = new Label(student.getName());
        nameL.setStyle("-fx-font-size:13px;-fx-font-weight:700;-fx-text-fill:#bdc6d6;");
        Label idBadge = new Label("ID " + student.getID());
        idBadge.setStyle("-fx-background-color:rgba(15,125,116,0.16);-fx-text-fill:#0f7d74;-fx-font-size:10px;-fx-font-weight:600;-fx-padding:2 7;-fx-background-radius:4;");

        HBox avRow = new HBox(10, av, new VBox(3, nameL, idBadge) {{ setAlignment(Pos.CENTER_LEFT); }});
        avRow.setAlignment(Pos.CENTER_LEFT);
        avatarBlock.getChildren().add(avRow);

        Region topSep = new Region(); topSep.setPrefHeight(1);
        topSep.setStyle("-fx-background-color:#1e2a3a;");
        VBox.setMargin(topSep, new Insets(0, 14, 8, 14));

        int unreadCount = ServerClient.get().countUnreadConversationMessagesForStudent(student.getID());
        int unreadAnnouncementCount = ServerClient.get().countUnreadAnnouncementsForStudent(student.getID());
        String inboxLabel = navLabel("Messages", unreadCount);
        String announcementLabel = navLabel("Announcements", unreadAnnouncementCount);

        String[][] navDefs = {
                {UIUtils.ICO_DASHBOARD, "Dashboard",      UIUtils.ACCENT_TEAL},
                {UIUtils.ICO_HISTORY,   "My Results",     UIUtils.ACCENT_BLUE},
                {UIUtils.ICO_ANALYTICS, "Analytics",      UIUtils.ACCENT_YELL},
                {UIUtils.ICO_TROPHY,    "Leaderboard",    "#b45309"},
                {UIUtils.ICO_SEND,      inboxLabel,       UIUtils.ACCENT_GREEN},
                {UIUtils.ICO_ANNOUNCE,  announcementLabel, UIUtils.ACCENT_PURP},
        };

        StackPane[] navBtns = new StackPane[navDefs.length];
        VBox navBox = new VBox(5);
        navBox.setPadding(new Insets(0, 10, 8, 10));

        javafx.scene.layout.AnchorPane contentArea = new javafx.scene.layout.AnchorPane();
        contentArea.setPrefSize(890, 660);
        contentArea.setStyle("-fx-background-color:" + UIUtils.bgContent() + ";");

        for (int i = 0; i < navDefs.length; i++) {
            final int idx = i;
            navBtns[i] = UIUtils.modernSidebarBtn(navDefs[i][0], navDefs[i][1], navDefs[i][2]);
            navBtns[i].setOnMouseClicked(e -> {
                activeNavIndex = idx;
                for (StackPane nb : navBtns) UIUtils.modernSidebarSetInactive(nb);
                UIUtils.modernSidebarSetActive(navBtns[idx]);
                dispatchPage(idx, contentArea, stage, student, app);
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
                if (r == ButtonType.YES) {
                    ServerClient.get().setExamEventListener(null); // clear push listener
                    stage.setScene(app.createMainScene(stage));
                }
            });
        });
        logoutBox.getChildren().add(btnLogout);

        sidebar.getChildren().addAll(switchRow, avatarBlock, topSep, navScroll, logoutBox);
        sidebar.setPrefHeight(Double.MAX_VALUE);
        root.setLeft(sidebar);
        root.setCenter(contentArea);

        ServerClient.get().setExamEventListener(updatedExam ->
                javafx.application.Platform.runLater(() -> {
                    if (activeNavIndex == 0)
                        renderDashboardPage(contentArea, stage, student, app);
                })
        );

        UIUtils.modernSidebarSetActive(navBtns[startPage]);
        dispatchPage(startPage, contentArea, stage, student, app);
        startNotificationPolling(student, contentArea, navBtns);

        Scene scene = new Scene(root, 1100, 660);
        UIUtils.applyStyle(scene);
        javafx.application.Platform.runLater(() -> { stage.setWidth(1100); stage.setHeight(660); stage.centerOnScreen(); });
        return scene;
    }

    private static void startNotificationPolling(Student student, javafx.scene.layout.AnchorPane contentArea, StackPane[] navBtns) {
        lastUnreadMessageCount = ServerClient.get().countUnreadConversationMessagesForStudent(student.getID());
        lastUnreadAnnouncementCount = ServerClient.get().countUnreadAnnouncementsForStudent(student.getID());
        updateSidebarLabel(navBtns[4], navLabel("Messages", lastUnreadMessageCount));
        updateSidebarLabel(navBtns[5], navLabel("Announcements", lastUnreadAnnouncementCount));

        notificationTimeline = new Timeline(new KeyFrame(Duration.seconds(4), e -> {
            int msgCount = ServerClient.get().countUnreadConversationMessagesForStudent(student.getID());
            int annCount = ServerClient.get().countUnreadAnnouncementsForStudent(student.getID());
            if (msgCount > lastUnreadMessageCount) {
                UIUtils.Toast.info(contentArea, (msgCount - lastUnreadMessageCount) + " new message" + (msgCount - lastUnreadMessageCount == 1 ? "" : "s"));
            }
            if (annCount > lastUnreadAnnouncementCount) {
                UIUtils.Toast.info(contentArea, (annCount - lastUnreadAnnouncementCount) + " new announcement" + (annCount - lastUnreadAnnouncementCount == 1 ? "" : "s"));
            }
            lastUnreadMessageCount = msgCount;
            lastUnreadAnnouncementCount = annCount;
            updateSidebarLabel(navBtns[4], navLabel("Messages", msgCount));
            updateSidebarLabel(navBtns[5], navLabel("Announcements", annCount));
        }));
        notificationTimeline.setCycleCount(Animation.INDEFINITE);
        notificationTimeline.play();
    }

    private static void stopNotificationTimeline() {
        if (notificationTimeline != null) {
            notificationTimeline.stop();
            notificationTimeline = null;
        }
    }

    private static String navLabel(String base, int count) {
        return count > 0 ? base + " (" + count + ")" : base;
    }

    private static void updateSidebarLabel(StackPane btn, String text) {
        HBox row = (HBox) btn.getChildren().get(1);
        Label label = (Label) row.getChildren().get(2);
        label.setText(text);
    }

    private static void dispatchPage(int idx, javafx.scene.layout.AnchorPane area,
                                     Stage stage, Student student, HelloApplication app) {
        switch (idx) {
            case 0 -> renderDashboardPage(area, stage, student, app);
            case 1 -> renderMyResultsPage(area, stage, student, app);
            case 2 -> renderAnalyticsPage(area, stage, student, app);
            case 3 -> renderLeaderboardPage(area, stage, student, app);
            case 4 -> renderInboxPage(area, stage, student, app);
            case 5 -> renderAnnouncementsPage(area, stage, student, app);
        }
    }

    private static Button backBtn(javafx.scene.layout.AnchorPane area, Stage stage, Student student, HelloApplication app) {
        Button btn = new Button();
        HBox inner = new HBox(6, UIUtils.icon(UIUtils.ICO_BACK, UIUtils.ACCENT_TEAL, 13), new Label("Back") {{ setStyle("-fx-font-size:12.5px;-fx-font-weight:600;-fx-text-fill:" + UIUtils.ACCENT_TEAL + ";"); }});
        inner.setAlignment(Pos.CENTER_LEFT);
        btn.setGraphic(inner);
        btn.setStyle("-fx-background-color:" + UIUtils.ACCENT_TEAL + "14;-fx-background-radius:6;-fx-border-color:" + UIUtils.ACCENT_TEAL + "40;-fx-border-radius:6;-fx-border-width:1;-fx-padding:6 14;-fx-cursor:hand;");
        btn.setOnAction(e -> {
            activeNavIndex = 0;
            renderDashboardPage(area, stage, student, app);
        });
        return btn;
    }

    private static void renderDashboardPage(javafx.scene.layout.AnchorPane area,
                                            Stage stage, Student student, HelloApplication app) {
        area.getChildren().clear();
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background:transparent;-fx-background-color:transparent;");

        VBox page = new VBox(0);
        page.setStyle("-fx-background-color:" + UIUtils.bgContent() + ";");

        VBox row1 = new VBox(14);
        row1.setPadding(new Insets(28, 36, 20, 36));

        int hour = java.time.LocalTime.now().getHour();
        String greet = hour < 12 ? "Good morning" : hour < 17 ? "Good afternoon" : "Good evening";
        String dateStr = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy"));
        Label greetL = new Label(greet + ", " + student.getName().split(" ")[0]);
        greetL.setStyle("-fx-font-size:20px;-fx-font-weight:700;-fx-text-fill:" + UIUtils.textDark() + ";-fx-letter-spacing:-0.3px;");
        Label dateL  = new Label(dateStr);
        dateL.setStyle("-fx-font-size:12.5px;-fx-text-fill:" + UIUtils.textSubtle() + ";");

        List<ExamResult> myResults = ServerClient.get().loadResultsForStudent(student.getID());
        List<int[]> myCodes = ServerClient.get().loadStudentExamCodes(student.getID());
        long liveCount  = myCodes.stream().filter(row -> {
            Exam ex = ServerClient.get().loadAllExams().stream().filter(e -> e.getDbId()==row[0]).findFirst().orElse(null);
            return ex != null && ex.isLive();
        }).count();
        long upcomingCount = myCodes.stream().filter(row -> {
            Exam ex = ServerClient.get().loadAllExams().stream().filter(e -> e.getDbId()==row[0]).findFirst().orElse(null);
            return ex != null && ex.isScheduled();
        }).count();
        long completedCount = myResults.size();
        double avgPct = myResults.isEmpty() ? 0 : myResults.stream().mapToDouble(ExamResult::pct).average().orElse(0);

        HBox stats = new HBox(10);
        stats.getChildren().addAll(
                UIUtils.statCard(UIUtils.ICO_LIVE,      String.valueOf(liveCount),     "Live Now",    UIUtils.ACCENT_GREEN),
                UIUtils.statCard(UIUtils.ICO_SCHEDULE,  String.valueOf(upcomingCount), "Upcoming",    UIUtils.ACCENT_PURP),
                UIUtils.statCard(UIUtils.ICO_CHECK,     String.valueOf(completedCount),"Completed",   UIUtils.ACCENT_TEAL),
                UIUtils.statCard(UIUtils.ICO_ANALYTICS, String.format("%.1f%%",avgPct),"Avg Score",   UIUtils.ACCENT_ORG)
        );

        row1.getChildren().addAll(new VBox(2, greetL, dateL), stats);

        // ── Announcements banner (shows latest non-expired announcement) ────
        List<Announcement> activeAnnouncements = ServerClient.get().loadAnnouncements()
                .stream().filter(a -> !a.isExpired()).collect(java.util.stream.Collectors.toList());
        VBox announceBanner = new VBox(6);
        if (!activeAnnouncements.isEmpty()) {
            announceBanner.setPadding(new Insets(0, 36, 0, 36));
            for (int ai = 0; ai < Math.min(2, activeAnnouncements.size()); ai++) {
                Announcement ann = activeAnnouncements.get(ai);
                String ac = (ann.color != null && !ann.color.isBlank()) ? ann.color : "#2563eb";
                HBox strip = new HBox(10); strip.setAlignment(Pos.CENTER_LEFT);
                strip.setPadding(new Insets(10, 14, 10, 14));
                strip.setMaxWidth(Double.MAX_VALUE);
                strip.setStyle("-fx-background-color:" + ac + "12;-fx-background-radius:7;" +
                        "-fx-border-color:" + ac + "45;-fx-border-radius:7;-fx-border-width:1;");
                StackPane sIco = new StackPane(UIUtils.icon(UIUtils.ICO_ANNOUNCE, ac, 12));
                sIco.setPrefSize(26, 26);
                sIco.setStyle("-fx-background-color:" + ac + "20;-fx-background-radius:6;");
                Label sTitle = new Label(ann.title + "  —  " + ann.body);
                sTitle.setStyle("-fx-font-size:12.5px;-fx-font-weight:600;-fx-text-fill:" + UIUtils.textDark() + ";");
                sTitle.setMaxWidth(580); sTitle.setEllipsisString("…");
                Region sSp = new Region(); HBox.setHgrow(sSp, Priority.ALWAYS);
                Label sDate = new Label(ann.dateStr());
                sDate.setStyle("-fx-font-size:10px;-fx-text-fill:" + UIUtils.textSubtle() + ";");
                strip.getChildren().addAll(sIco, sTitle, sSp, sDate);
                strip.setOnMouseClicked(ev -> {
                    activeNavIndex = 4;
                    renderAnnouncementsPage(area, stage, student, app);
                });
                strip.setCursor(javafx.scene.Cursor.HAND);
                announceBanner.getChildren().add(strip);
            }
        }

        VBox row2 = new VBox(14);
        row2.setPadding(new Insets(20, 36, 20, 36));
        row2.setStyle("-fx-background-color:" + UIUtils.bgContent() + ";-fx-border-color:" + UIUtils.border() + ";-fx-border-width:1 0 0 0;");

        HBox joinCaption = buildSectionHeader("Join an Examination", "#0f7d74");
        Label joinSub = new Label("Enter the 6-character code given by your instructor");
        joinSub.setStyle("-fx-font-size:12px;-fx-text-fill:" + UIUtils.textSubtle() + ";");

        TextField codeField = new TextField();
        codeField.setPromptText("A B C 1 2 3");
        codeField.setPrefWidth(280);
        codeField.setPrefHeight(52);
        codeField.setStyle(
                "-fx-font-family:Monospaced;-fx-font-size:22px;-fx-font-weight:700;" +
                        "-fx-alignment:center;" +
                        "-fx-text-fill:" + UIUtils.textDark() + ";" +
                        "-fx-background-color:" + UIUtils.bgInput() + ";" +
                        "-fx-border-color:#0f7d74;" +
                        "-fx-border-radius:8;-fx-background-radius:8;" +
                        "-fx-border-width:2;-fx-padding:10;" +
                        "-fx-letter-spacing:8px;"
        );

        codeField.textProperty().addListener((obs, o, n) -> {
            String up = n.toUpperCase().replaceAll("[^A-Z0-9]", "");
            if (up.length() > 6) up = up.substring(0, 6);
            if (!n.equals(up)) {
                String fin = up;
                Platform.runLater(() -> { codeField.setText(fin); codeField.positionCaret(fin.length()); });
            }
        });

        Button btnSearch = UIUtils.primaryBtn("", "Search Examination", UIUtils.ACCENT_TEAL);
        btnSearch.setPrefWidth(280); btnSearch.setPrefHeight(42);

        VBox codeStack = new VBox(10, codeField, btnSearch);
        codeStack.setAlignment(Pos.CENTER_LEFT);
        HBox codeRow = new HBox(codeStack);
        codeRow.setAlignment(Pos.CENTER_LEFT);

        VBox liveSection = new VBox(8);
        VBox upcomingSection = new VBox(8);

        Runnable[] refreshRef = { null };

        Runnable doSearch = () -> {
            String code = codeField.getText().trim();
            if (code.length() != 6) {
                TranslateTransition tt = new TranslateTransition(Duration.millis(50), codeField);
                tt.setFromX(0); tt.setByX(9); tt.setCycleCount(6); tt.setAutoReverse(true);
                tt.setOnFinished(ev -> codeField.setTranslateX(0)); tt.play();
                return;
            }

            btnSearch.setText("Searching..."); btnSearch.setDisable(true);
            PauseTransition delay = new PauseTransition(Duration.millis(380));
            delay.setOnFinished(ev -> {
                btnSearch.setText("Search"); btnSearch.setDisable(false);

                Exam found = ServerClient.get().loadAllExams().stream()
                        .filter(ex -> ex.getExamCode() != null
                                && ex.getExamCode().equalsIgnoreCase(code)
                                && (ex.isLive() || ex.isScheduled()))
                        .findFirst().orElse(null);

                if (found == null) {
                    UIUtils.Toast.error(area, "No active examination found with that code");
                    return;
                }

                if (ServerClient.get().hasResult(student.getID(), found.getDbId(), found.getExamCode())) {
                    ExamResult prev = ServerClient.get().loadSingleResult(student.getID(), found.getDbId());
                    showAlreadySubmittedPopup(stage, prev);
                    return;
                }

                ServerClient.get().clearInProgress(student.getID(), found.getDbId());

                List<int[]> existing = ServerClient.get().loadStudentExamCodes(student.getID());
                boolean alreadyUnlocked = existing.stream().anyMatch(row -> row[0] == found.getDbId());

                if (found.isLive()) {
                    ServerClient.get().saveStudentExamCode(student.getID(), found.getDbId(), "live");
                    codeField.clear();
                    showLiveExamPopup(found, student, stage, app, area);
                } else {
                    if (!alreadyUnlocked) {
                        ServerClient.get().saveStudentExamCode(student.getID(), found.getDbId(), "scheduled");
                        UIUtils.Toast.success(area, "Examination added to your upcoming list");
                    } else {
                        UIUtils.Toast.info(area, "This examination is already in your upcoming list");
                    }
                    codeField.clear();
                    if (refreshRef[0] != null) refreshRef[0].run();
                }
            });
            delay.play();
        };

        codeField.setOnAction(e -> doSearch.run());
        btnSearch.setOnAction(e -> doSearch.run());

        row2.getChildren().addAll(joinCaption, joinSub, codeRow);

        VBox row3 = new VBox(10);
        row3.setPadding(new Insets(18, 36, 18, 36));
        row3.setStyle("-fx-background-color:" + UIUtils.bgContent() + ";-fx-border-color:" + UIUtils.border() + ";-fx-border-width:1 0 0 0;");
        row3.getChildren().addAll(buildSectionHeaderWithDot("Live Examinations", "#0e7a56", true), liveSection);

        VBox row4 = new VBox(10);
        row4.setPadding(new Insets(18, 36, 28, 36));
        row4.setStyle("-fx-background-color:" + UIUtils.bgContent() + ";-fx-border-color:" + UIUtils.border() + ";-fx-border-width:1 0 0 0;");
        row4.getChildren().addAll(buildSectionHeaderWithDot("Upcoming Examinations", UIUtils.ACCENT_PURP, false), upcomingSection);

        Runnable refreshSections = buildDashboardRefresh(liveSection, upcomingSection, student, stage, app, area);
        refreshRef[0] = refreshSections;
        refreshSections.run();

        Timeline ticker = new Timeline(new KeyFrame(Duration.seconds(30), ev -> refreshSections.run()));
        ticker.setCycleCount(Animation.INDEFINITE); ticker.play();
        page.sceneProperty().addListener((obs, o, n) -> { if (n == null) ticker.stop(); });

        page.getChildren().addAll(row1, announceBanner, row2, row3, row4);
        scroll.setContent(page);

        javafx.scene.layout.AnchorPane.setTopAnchor(scroll, 0.0);
        javafx.scene.layout.AnchorPane.setBottomAnchor(scroll, 0.0);
        javafx.scene.layout.AnchorPane.setLeftAnchor(scroll, 0.0);
        javafx.scene.layout.AnchorPane.setRightAnchor(scroll, 0.0);
        area.getChildren().add(scroll);
        UIUtils.slideIn(page, true);
    }

    private static HBox buildSectionHeaderWithDot(String text, String color, boolean animated) {
        HBox hdr = new HBox(8); hdr.setAlignment(Pos.CENTER_LEFT);
        if (animated) {
            Circle dot    = new Circle(4, Color.web(color));
            Circle ripple = new Circle(4, Color.web(color)); ripple.setOpacity(0);
            ripple.setMouseTransparent(true);
            Timeline sonar = new Timeline(
                    new KeyFrame(Duration.ZERO,
                            new KeyValue(ripple.radiusProperty(), 4.0),
                            new KeyValue(ripple.opacityProperty(), 0.55)),
                    new KeyFrame(Duration.millis(900),
                            new KeyValue(ripple.radiusProperty(), 10.0),
                            new KeyValue(ripple.opacityProperty(), 0.0))
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
            hdr.getChildren().add(new StackPane(dot) {{
                setPrefSize(16, 16); setMinSize(16, 16); setMaxSize(16, 16);
            }});
        }
        Label lbl = new Label(text);
        lbl.setStyle("-fx-font-size:14px;-fx-font-weight:700;-fx-text-fill:" + UIUtils.textDark() + ";");
        hdr.getChildren().add(lbl);
        return hdr;
    }

    private static HBox buildSectionHeader(String text, String color) {
        HBox hdr = new HBox(8); hdr.setAlignment(Pos.CENTER_LEFT);
        Circle dot = new Circle(4, Color.web(color));
        hdr.getChildren().add(new StackPane(dot) {{ setPrefSize(16, 16); }});
        Label lbl = new Label(text);
        lbl.setStyle("-fx-font-size:14px;-fx-font-weight:700;-fx-text-fill:" + UIUtils.textDark() + ";");
        hdr.getChildren().add(lbl);
        return hdr;
    }

    private static Runnable buildDashboardRefresh(VBox liveSection, VBox upcomingSection,
                                                  Student student, Stage stage,
                                                  HelloApplication app,
                                                  javafx.scene.layout.AnchorPane area) {
        return () -> {
            liveSection.getChildren().clear();
            upcomingSection.getChildren().clear();

            List<int[]> codes = ServerClient.get().loadStudentExamCodes(student.getID());
            boolean hasLive = false, hasSched = false;

            for (int[] row : codes) {
                int examId = row[0];
                Exam ex = ServerClient.get().loadAllExams().stream()
                        .filter(e -> e.getDbId() == examId).findFirst().orElse(null);
                if (ex == null) {
                    ServerClient.get().removeStudentExamCode(student.getID(), examId);
                    continue;
                }

                if (!ex.isLive() && !ex.isScheduled()) {
                    ServerClient.get().removeStudentExamCode(student.getID(), examId);
                    continue;
                }

                if (ex.getExamCode() != null && !ex.getExamCode().isEmpty()
                        && ServerClient.get().hasResult(student.getID(), examId, ex.getExamCode())) {
                    ServerClient.get().removeStudentExamCode(student.getID(), examId);
                    continue;
                }

                if (ex.isLive()) {
                    ServerClient.get().saveStudentExamCode(student.getID(), examId, "live");
                    liveSection.getChildren().add(buildStudentLiveRow(ex, student, stage, app, area));
                    hasLive = true;
                } else if (ex.isScheduled()) {
                    upcomingSection.getChildren().add(buildStudentSchedRow(ex, student, stage, app, area, () ->
                            buildDashboardRefresh(liveSection, upcomingSection, student, stage, app, area).run()
                    ));
                    hasSched = true;
                }
            }

            for (Exam ex : ServerClient.get().loadAllExams().stream().filter(Exam::isLive).collect(java.util.stream.Collectors.toList())) {
                boolean alreadySubmitted = ex.getExamCode() != null && !ex.getExamCode().isEmpty()
                        && ServerClient.get().hasResult(student.getID(), ex.getDbId(), ex.getExamCode());
                if (ServerClient.get().hasInProgress(student.getID(), ex.getDbId()) && !alreadySubmitted) {
                    boolean alreadyShown = codes.stream().anyMatch(row -> row[0] == ex.getDbId());
                    if (!alreadyShown) {
                        ServerClient.get().saveStudentExamCode(student.getID(), ex.getDbId(), "live");
                        liveSection.getChildren().add(buildStudentLiveRow(ex, student, stage, app, area));
                        hasLive = true;
                    }
                }
            }

            if (!hasLive) {
                Label none = new Label("No live examinations. Enter a code above to join one.");
                none.setStyle("-fx-font-size:12.5px;-fx-text-fill:" + UIUtils.textSubtle() + ";-fx-padding:10 0;");
                liveSection.getChildren().add(none);
            }
            if (!hasSched) {
                Label none = new Label("No upcoming examinations. Enter a scheduled exam code to see it here.");
                none.setStyle("-fx-font-size:12.5px;-fx-text-fill:" + UIUtils.textSubtle() + ";-fx-padding:10 0;");
                upcomingSection.getChildren().add(none);
            }
        };
    }

    private static HBox buildStudentLiveRow(Exam ex, Student student, Stage stage, HelloApplication app,
                                            javafx.scene.layout.AnchorPane area) {
        HBox row = new HBox(12); row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(13, 18, 13, 18));
        row.setMaxWidth(Double.MAX_VALUE);
        row.setStyle(
                "-fx-background-color:" + UIUtils.bgCard() + ";" +
                        "-fx-background-radius:9;" +
                        "-fx-border-color:rgba(14,122,86,0.35);" +
                        "-fx-border-radius:9;-fx-border-width:1.5;"
        );
        DropShadow ds = new DropShadow(); ds.setColor(Color.color(0,0,0,0.05)); ds.setRadius(8); ds.setOffsetY(2); row.setEffect(ds);

        Circle dot = new Circle(4, Color.web("#0e7a56"));
        Circle ripple = new Circle(4, Color.web("#0e7a56")); ripple.setOpacity(0);
        ripple.setMouseTransparent(true);
        Timeline sonar = new Timeline(
                new KeyFrame(Duration.ZERO,     new KeyValue(ripple.radiusProperty(), 4.0),  new KeyValue(ripple.opacityProperty(), 0.55)),
                new KeyFrame(Duration.millis(850), new KeyValue(ripple.radiusProperty(), 11.0), new KeyValue(ripple.opacityProperty(), 0.0))
        );
        sonar.setCycleCount(Timeline.INDEFINITE); sonar.play();
        StackPane dotStack = new StackPane(ripple, dot);
        dotStack.setPrefSize(22, 22); dotStack.setMinSize(22, 22); dotStack.setMaxSize(22, 22);
        dotStack.setClip(new Circle(11, 11, 11));
        dotStack.setMouseTransparent(true);

        Label liveBadge = new Label("LIVE");
        liveBadge.setStyle("-fx-font-size:9px;-fx-font-weight:700;-fx-text-fill:#0e7a56;-fx-background-color:#d1f0e8;-fx-padding:2 8;-fx-background-radius:4;-fx-letter-spacing:1px;");

        String et = (ex.getTitle() != null && !ex.getTitle().isBlank()) ? ex.getTitle() : ex.getSubject();
        VBox info = new VBox(3);
        Label titleLbl = new Label(et);
        titleLbl.setStyle("-fx-font-size:14px;-fx-font-weight:700;-fx-text-fill:" + UIUtils.textDark() + ";");
        Label metaLbl = new Label(ex.getSubject() + "  ·  Grade " + ex.getGrade()
                + "  ·  " + ex.getDuration() + " min  ·  " + (int)ex.getTotalMarks() + " marks");
        metaLbl.setStyle("-fx-font-size:11px;-fx-text-fill:" + UIUtils.textSubtle() + ";");

        if (ServerClient.get().hasInProgress(student.getID(), ex.getDbId())) {
            Map<Integer, String> saved = ServerClient.get().loadInProgressAnswers(student.getID(), ex.getDbId());
            int answeredCount = saved == null ? 0 : saved.size();
            Label resumeLbl = new Label("Resume: " + answeredCount + " answer" + (answeredCount == 1 ? "" : "s") + " saved");
            resumeLbl.setStyle("-fx-font-size:10.5px;-fx-font-weight:700;-fx-text-fill:#5046a0;-fx-background-color:rgba(80,70,160,0.10);-fx-padding:2 8;-fx-background-radius:4;");
            info.getChildren().add(resumeLbl);
        }

        info.getChildren().addAll(titleLbl, metaLbl);

        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);

        Label remLbl = new Label(ex.getRemainingFormatted());
        remLbl.setStyle("-fx-font-family:Monospaced;-fx-font-size:13px;-fx-font-weight:700;-fx-text-fill:#0e7a56;-fx-background-color:rgba(14,122,86,0.10);-fx-background-radius:5;-fx-padding:4 10;");
        Timeline remTl = new Timeline(new KeyFrame(Duration.seconds(1), e -> remLbl.setText(ex.getRemainingFormatted())));
        remTl.setCycleCount(Animation.INDEFINITE); remTl.play();
        row.sceneProperty().addListener((o, ov, nv) -> { if (nv == null) { remTl.stop(); sonar.stop(); }});

        Button btnEnter = UIUtils.primaryBtn("", ServerClient.get().hasInProgress(student.getID(), ex.getDbId()) ? "Resume" : "Enter", UIUtils.ACCENT_TEAL);
        btnEnter.setPrefHeight(36);
        btnEnter.setOnAction(e -> showLiveExamPopup(ex, student, stage, app, area));

        row.getChildren().addAll(new HBox(6, dotStack, liveBadge) {{ setAlignment(Pos.CENTER_LEFT); }},
                info, sp, remLbl, btnEnter);
        return row;
    }

    private static HBox buildStudentLiveLockedRow(Exam ex, javafx.scene.layout.AnchorPane area) {
        HBox row = new HBox(12); row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(13, 18, 13, 18));
        row.setMaxWidth(Double.MAX_VALUE);
        row.setStyle(
                "-fx-background-color:" + UIUtils.bgCard() + ";" +
                        "-fx-background-radius:9;" +
                        "-fx-border-color:rgba(14,122,86,0.35);" +
                        "-fx-border-radius:9;-fx-border-width:1.5;"
        );
        DropShadow ds = new DropShadow(); ds.setColor(Color.color(0,0,0,0.05)); ds.setRadius(8); ds.setOffsetY(2); row.setEffect(ds);

        Circle dot = new Circle(4, Color.web("#0e7a56"));
        Label liveBadge = new Label("LIVE");
        liveBadge.setStyle("-fx-font-size:9px;-fx-font-weight:700;-fx-text-fill:#0e7a56;-fx-background-color:#d1f0e8;-fx-padding:2 8;-fx-background-radius:4;-fx-letter-spacing:1px;");

        String et = (ex.getTitle() != null && !ex.getTitle().isBlank()) ? ex.getTitle() : ex.getSubject();
        VBox info = new VBox(4);
        Label titleLbl = new Label(et);
        titleLbl.setStyle("-fx-font-size:14px;-fx-font-weight:700;-fx-text-fill:" + UIUtils.textDark() + ";");
        Label metaLbl = new Label(ex.getSubject() + "  ·  Grade " + ex.getGrade()
                + "  ·  " + ex.getDuration() + " min  ·  " + (int)ex.getTotalMarks() + " marks");
        metaLbl.setStyle("-fx-font-size:11px;-fx-text-fill:" + UIUtils.textSubtle() + ";");
        Label lockLbl = new Label("Code Required");
        lockLbl.setStyle("-fx-font-size:10.5px;-fx-font-weight:700;-fx-text-fill:#b45309;-fx-background-color:#fef3c7;-fx-padding:2 8;-fx-background-radius:4;");
        info.getChildren().addAll(titleLbl, metaLbl, lockLbl);

        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);

        Label remLbl = new Label(ex.getRemainingFormatted());
        remLbl.setStyle("-fx-font-family:Monospaced;-fx-font-size:13px;-fx-font-weight:700;-fx-text-fill:#0e7a56;-fx-background-color:rgba(14,122,86,0.10);-fx-background-radius:5;-fx-padding:4 10;");
        Timeline remTl = new Timeline(new KeyFrame(Duration.seconds(1), e -> remLbl.setText(ex.getRemainingFormatted())));
        remTl.setCycleCount(Animation.INDEFINITE); remTl.play();
        row.sceneProperty().addListener((o, ov, nv) -> { if (nv == null) remTl.stop(); });

        Button btnCode = UIUtils.ghostBtn("", "Enter Code", UIUtils.ACCENT_ORG);
        btnCode.setPrefHeight(36);
        btnCode.setOnAction(e -> UIUtils.Toast.info(area, "Enter the exam code above to unlock this live examination."));

        row.getChildren().addAll(new HBox(6, dot, liveBadge) {{ setAlignment(Pos.CENTER_LEFT); }},
                info, sp, remLbl, btnCode);
        return row;
    }

    private static VBox buildStudentSchedRow(Exam ex, Student student, Stage stage,
                                             HelloApplication app,
                                             javafx.scene.layout.AnchorPane area,
                                             Runnable onGoLive) {
        VBox wrapper = new VBox();
        wrapper.setUserData(ex.getDbId());
        HBox row = new HBox(12); row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(13, 18, 13, 18));
        row.setMaxWidth(Double.MAX_VALUE);
        row.setStyle("-fx-background-color:" + UIUtils.bgCard() + ";-fx-background-radius:9;" +
                "-fx-border-color:" + UIUtils.border() + ";-fx-border-radius:9;-fx-border-width:1;");
        DropShadow ds = new DropShadow(); ds.setColor(Color.color(0,0,0,0.04)); ds.setRadius(6); ds.setOffsetY(1); row.setEffect(ds);

        String et = (ex.getTitle() != null && !ex.getTitle().isBlank()) ? ex.getTitle() : ex.getSubject();
        VBox info = new VBox(3);
        Label titleLbl = new Label(et);
        titleLbl.setStyle("-fx-font-size:14px;-fx-font-weight:700;-fx-text-fill:" + UIUtils.textDark() + ";");
        Label metaLbl = new Label(ex.getSubject() + "  ·  Grade " + ex.getGrade()
                + "  ·  " + ex.getDuration() + " min  ·  " + (int)ex.getTotalMarks() + " marks");
        metaLbl.setStyle("-fx-font-size:11px;-fx-text-fill:" + UIUtils.textSubtle() + ";");
        info.getChildren().addAll(titleLbl, metaLbl);
        if (ex.getScheduledStartMillis() > 0 && ex.getScheduledStartMillis() > System.currentTimeMillis()) {
            java.time.LocalDateTime ldt = java.time.Instant.ofEpochMilli(ex.getScheduledStartMillis())
                    .atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();
            Label dateLbl = new Label(ldt.format(java.time.format.DateTimeFormatter.ofPattern("EEEE, d MMMM  ·  HH:mm")));
            dateLbl.setStyle("-fx-font-size:11px;-fx-font-weight:600;-fx-text-fill:" + UIUtils.ACCENT_PURP + ";");
            info.getChildren().add(dateLbl);
        }

        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);

        Label countdown = new Label(ex.getScheduledStartMillis() > 0 ? "Starts in  " + ex.getStartCountdownFormatted() : "Awaiting schedule");
        countdown.setStyle("-fx-font-family:Monospaced;-fx-font-size:12px;-fx-font-weight:700;-fx-text-fill:#5046a0;-fx-background-color:rgba(80,70,160,0.10);-fx-background-radius:4;-fx-padding:3 9;");

        Timeline tl = new Timeline(new KeyFrame(Duration.seconds(1), ev -> {
            if (ex.isLive()) {
                ServerClient.get().saveStudentExamCode(student.getID(), ex.getDbId(), "live");
                UIUtils.Toast.success(area, et + " is now live - tap Enter to join");
                onGoLive.run();
            } else if (ex.getScheduledStartMillis() > 0) {
                countdown.setText("Starts in  " + ex.getStartCountdownFormatted());
            }
        }));
        tl.setCycleCount(Animation.INDEFINITE); tl.play();
        row.sceneProperty().addListener((o, ov, nv) -> { if (nv == null) tl.stop(); });

        row.getChildren().addAll(info, sp, countdown);
        wrapper.getChildren().add(row);
        return wrapper;
    }

    private static void showLiveExamPopup(Exam exam, Student student, Stage stage,
                                          HelloApplication app, javafx.scene.layout.AnchorPane area) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.initOwner(stage);
        popup.initStyle(StageStyle.UNDECORATED);

        VBox box = new VBox(16);
        box.setPadding(new Insets(28, 34, 26, 34));
        box.setStyle(
                "-fx-background-color:" + UIUtils.bgCard() + ";" +
                        "-fx-background-radius:14;" +
                        "-fx-border-color:rgba(14,122,86,0.3);" +
                        "-fx-border-radius:14;-fx-border-width:1.5;"
        );
        box.setEffect(new DropShadow(36, Color.color(0,0,0,0.18)));

        HBox hdr = new HBox(12); hdr.setAlignment(Pos.CENTER_LEFT);

        Circle dot = new Circle(5, Color.web("#0e7a56"));
        Circle ripple2 = new Circle(5, Color.web("#0e7a56")); ripple2.setOpacity(0);
        ripple2.setMouseTransparent(true);
        Timeline sonar2 = new Timeline(
                new KeyFrame(Duration.ZERO,     new KeyValue(ripple2.radiusProperty(), 5.0),  new KeyValue(ripple2.opacityProperty(), 0.55)),
                new KeyFrame(Duration.millis(900), new KeyValue(ripple2.radiusProperty(), 13.0), new KeyValue(ripple2.opacityProperty(), 0.0))
        );
        sonar2.setCycleCount(Timeline.INDEFINITE); sonar2.play();
        StackPane dotStack2 = new StackPane(ripple2, dot);
        dotStack2.setPrefSize(22, 22); dotStack2.setMinSize(22, 22); dotStack2.setMaxSize(22, 22);
        dotStack2.setClip(new Circle(11, 11, 11));
        dotStack2.setMouseTransparent(true);

        VBox hdrText = new VBox(3);
        String et = (exam.getTitle()!=null&&!exam.getTitle().isBlank()) ? exam.getTitle() : exam.getSubject() + " Examination";
        Label hdrTitle = new Label(et);
        hdrTitle.setStyle("-fx-font-size:17px;-fx-font-weight:700;-fx-text-fill:" + UIUtils.textDark() + ";-fx-letter-spacing:-0.2px;");
        Label livePill = new Label("  LIVE  ");
        livePill.setStyle("-fx-font-size:9.5px;-fx-font-weight:700;-fx-text-fill:#0e7a56;-fx-background-color:#d1f0e8;-fx-padding:2 10;-fx-background-radius:4;-fx-letter-spacing:1.2px;");
        hdrText.getChildren().addAll(hdrTitle, livePill);
        hdr.getChildren().addAll(dotStack2, hdrText);

        Label timerLabel = new Label(exam.getRemainingFormatted());
        timerLabel.setStyle("-fx-font-family:Monospaced;-fx-font-size:28px;-fx-font-weight:700;-fx-text-fill:#0e7a56;-fx-background-color:rgba(14,122,86,0.10);-fx-background-radius:8;-fx-padding:8 20;");
        Label timerCaption = new Label("Time remaining");
        timerCaption.setStyle("-fx-font-size:10.5px;-fx-text-fill:" + UIUtils.textSubtle() + ";-fx-letter-spacing:0.8px;");
        Timeline timerTl = new Timeline(new KeyFrame(Duration.seconds(1), e -> timerLabel.setText(exam.getRemainingFormatted())));
        timerTl.setCycleCount(Animation.INDEFINITE); timerTl.play();
        VBox timerBox = new VBox(5, timerCaption, timerLabel); timerBox.setAlignment(Pos.CENTER_LEFT);

        GridPane grid = new GridPane(); grid.setHgap(20); grid.setVgap(9);
        grid.setPadding(new Insets(12)); grid.setStyle("-fx-background-color:" + UIUtils.bgMuted() + ";-fx-background-radius:8;");
        addDetailRow(grid, 0, "Subject",    exam.getSubject());
        addDetailRow(grid, 1, "Class",      "Grade " + exam.getGrade());
        addDetailRow(grid, 2, "Duration",   exam.getDuration() + " minutes");
        addDetailRow(grid, 3, "Questions",  exam.getQuestionsMap().size() + " items");
        addDetailRow(grid, 4, "Total",      String.valueOf((int) exam.getTotalMarks()) + " marks");

        boolean hasProgress = ServerClient.get().hasInProgress(student.getID(), exam.getDbId());
        if (hasProgress) {
            Map<Integer, String> saved = ServerClient.get().loadInProgressAnswers(student.getID(), exam.getDbId());
            int savedCount = saved == null ? 0 : saved.size();
            HBox resumeNotice = new HBox(8); resumeNotice.setAlignment(Pos.CENTER_LEFT);
            resumeNotice.setPadding(new Insets(9, 12, 9, 12));
            resumeNotice.setStyle("-fx-background-color:rgba(80,70,160,0.09);-fx-background-radius:7;-fx-border-color:rgba(80,70,160,0.25);-fx-border-radius:7;-fx-border-width:1;");
            resumeNotice.getChildren().addAll(
                    UIUtils.icon(UIUtils.ICO_INFO, UIUtils.ACCENT_PURP, 13),
                    new Label(savedCount + " answer" + (savedCount==1?"":"s") + " saved — you will resume from where you left off.") {{
                        setStyle("-fx-font-size:12px;-fx-font-weight:600;-fx-text-fill:#5046a0;");
                        setWrapText(true);
                    }}
            );
            box.getChildren().add(resumeNotice);
        }

        HBox btnRow = new HBox(12); btnRow.setAlignment(Pos.CENTER_LEFT);
        String btnLabel = hasProgress ? "Resume Examination" : "Begin Examination";
        Button btnJoin   = UIUtils.primaryBtn("", btnLabel, UIUtils.ACCENT_TEAL);
        btnJoin.setPrefWidth(200); btnJoin.setPrefHeight(44);
        Button btnCancel = UIUtils.ghostBtn("", "Not now", UIUtils.ACCENT_RED);
        btnCancel.setPrefHeight(44);

        btnJoin.setOnAction(e -> {
            timerTl.stop(); sonar2.stop();
            popup.close();
            Exam latestExam = ServerClient.get().loadAllExams().stream()
                    .filter(ex -> ex.getDbId() == exam.getDbId())
                    .findFirst()
                    .orElse(exam);
            stage.setScene(buildExamScene(latestExam, student, stage, app));
        });
        btnCancel.setOnAction(e -> { timerTl.stop(); sonar2.stop(); popup.close(); });
        btnRow.getChildren().addAll(btnJoin, btnCancel);

        box.getChildren().addAll(0, List.of(hdr, timerBox, UIUtils.divider(), grid));
        box.getChildren().addAll(UIUtils.divider(), btnRow);

        Scene sc = new Scene(new StackPane(box), 460, hasProgress ? 520 : 490);
        sc.setFill(Color.TRANSPARENT);
        UIUtils.applyStyle(sc);
        popup.setScene(sc);

        box.setScaleX(0.92); box.setScaleY(0.92); box.setOpacity(0);
        ScaleTransition st = new ScaleTransition(Duration.millis(220), box); st.setToX(1); st.setToY(1); st.setInterpolator(Interpolator.EASE_OUT);
        FadeTransition ft = new FadeTransition(Duration.millis(220), box); ft.setToValue(1);
        new ParallelTransition(st, ft).play();
        popup.show();
    }

    private static void showScheduledExamPopup(Stage stage, Exam exam) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.initOwner(stage);
        popup.initStyle(StageStyle.UNDECORATED);

        VBox box = new VBox(16);
        box.setPadding(new Insets(28, 34, 26, 34));
        box.setStyle(
                "-fx-background-color:" + UIUtils.bgCard() + ";" +
                        "-fx-background-radius:14;" +
                        "-fx-border-color:rgba(80,70,160,0.3);" +
                        "-fx-border-radius:14;-fx-border-width:1.5;"
        );
        box.setEffect(new DropShadow(36, Color.color(0,0,0,0.16)));

        HBox hdr = new HBox(12); hdr.setAlignment(Pos.CENTER_LEFT);
        StackPane ico = new StackPane(UIUtils.icon(UIUtils.ICO_SCHEDULE, "#5046a0", 20));
        ico.setPrefSize(44, 44); ico.setStyle("-fx-background-color:rgba(80,70,160,0.12);-fx-background-radius:9;");
        VBox hdrText = new VBox(3);
        String et = (exam.getTitle()!=null&&!exam.getTitle().isBlank()) ? exam.getTitle() : exam.getSubject() + " Examination";
        Label hdrTitle = new Label(et);
        hdrTitle.setStyle("-fx-font-size:17px;-fx-font-weight:700;-fx-text-fill:" + UIUtils.textDark() + ";");
        Label schedLabel = new Label("Scheduled Examination — Added to Your Upcoming List");
        schedLabel.setStyle("-fx-font-size:11px;-fx-text-fill:#5046a0;-fx-font-weight:600;");
        hdrText.getChildren().addAll(hdrTitle, schedLabel);
        hdr.getChildren().addAll(ico, hdrText);

        GridPane grid = new GridPane(); grid.setHgap(20); grid.setVgap(9);
        grid.setPadding(new Insets(12)); grid.setStyle("-fx-background-color:" + UIUtils.bgMuted() + ";-fx-background-radius:8;");
        addDetailRow(grid, 0, "Subject",   exam.getSubject());
        addDetailRow(grid, 1, "Class",     "Grade " + exam.getGrade());
        addDetailRow(grid, 2, "Duration",  exam.getDuration() + " minutes");
        addDetailRow(grid, 3, "Questions", exam.getQuestionsMap().size() + " items");
        addDetailRow(grid, 4, "Total",     (int)exam.getTotalMarks() + " marks");

        if (exam.getScheduledStartMillis() > 0) {
            java.time.LocalDateTime ldt = java.time.Instant.ofEpochMilli(exam.getScheduledStartMillis())
                    .atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();
            addDetailRow(grid, 5, "Starts",
                    ldt.format(java.time.format.DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy  ·  HH:mm")));
        }

        if (exam.getDescription() != null && !exam.getDescription().isBlank()) {
            addDetailRow(grid, 6, "Notes", exam.getDescription());
        }

        Button btnOk = UIUtils.primaryBtn("", "Got it", UIUtils.ACCENT_PURP);
        btnOk.setPrefWidth(160); btnOk.setPrefHeight(42);
        btnOk.setOnAction(e -> popup.close());

        box.getChildren().addAll(hdr, UIUtils.divider(), grid, UIUtils.divider(), btnOk);

        Scene sc = new Scene(new StackPane(box), 460, 400);
        sc.setFill(Color.TRANSPARENT);
        UIUtils.applyStyle(sc);
        popup.setScene(sc);

        box.setScaleX(0.92); box.setScaleY(0.92); box.setOpacity(0);
        ScaleTransition st = new ScaleTransition(Duration.millis(220), box); st.setToX(1); st.setToY(1); st.setInterpolator(Interpolator.EASE_OUT);
        FadeTransition ft = new FadeTransition(Duration.millis(220), box); ft.setToValue(1);
        new ParallelTransition(st, ft).play();
        popup.show();
    }

    private static void showAlreadySubmittedPopup(Stage stage, ExamResult prev) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.initOwner(stage);
        popup.initStyle(StageStyle.UNDECORATED);

        VBox box = new VBox(16);
        box.setPadding(new Insets(28, 34, 26, 34));
        box.setStyle("-fx-background-color:" + UIUtils.bgCard() + ";-fx-background-radius:14;-fx-border-color:" + UIUtils.border() + ";-fx-border-radius:14;-fx-border-width:1;");
        box.setEffect(new DropShadow(28, Color.color(0,0,0,0.14)));
        box.setAlignment(Pos.CENTER);

        StackPane ico = new StackPane(UIUtils.icon(UIUtils.ICO_CHECK, UIUtils.ACCENT_GREEN, 22));
        ico.setPrefSize(52, 52); ico.setStyle("-fx-background-color:" + UIUtils.ACCENT_GREEN + "18;-fx-background-radius:99;");

        Label hdr = new Label("Already Submitted");
        hdr.setStyle("-fx-font-size:17px;-fx-font-weight:700;-fx-text-fill:" + UIUtils.textDark() + ";");

        if (prev != null) {
            String gc = prev.pct()>=65?UIUtils.ACCENT_GREEN:prev.pct()>=50?UIUtils.ACCENT_TEAL:UIUtils.ACCENT_RED;
            Label scoreL = new Label(String.format("%.0f / %.0f", prev.score, prev.totalMarks));
            scoreL.setStyle("-fx-font-size:32px;-fx-font-weight:700;-fx-text-fill:" + gc + ";");
            Label pctL = new Label(String.format("%.1f%%  ·  Grade %s", prev.pct(), prev.grade()));
            pctL.setStyle("-fx-font-size:14px;-fx-font-weight:600;-fx-text-fill:" + UIUtils.textMid() + ";");
            Label dateL = new Label("Submitted " + prev.dateStr());
            dateL.setStyle("-fx-font-size:11.5px;-fx-text-fill:" + UIUtils.textSubtle() + ";");
            Label note = new Label("Each examination can only be taken once.");
            note.setStyle("-fx-font-size:12px;-fx-text-fill:" + UIUtils.textSubtle() + ";");

            box.getChildren().addAll(ico, hdr, scoreL, pctL, dateL, UIUtils.divider(), note);
        } else {
            Label note = new Label("You have already submitted this examination. Each exam can only be taken once.");
            note.setStyle("-fx-font-size:13px;-fx-text-fill:" + UIUtils.textMid() + ";"); note.setWrapText(true); note.setMaxWidth(340);
            box.getChildren().addAll(ico, hdr, note);
        }

        Button btnOk = UIUtils.primaryBtn("", "Close", UIUtils.ACCENT_TEAL);
        btnOk.setPrefWidth(140); btnOk.setPrefHeight(40);
        btnOk.setOnAction(e -> popup.close());
        box.getChildren().add(btnOk);

        Scene sc = new Scene(new StackPane(box), 400, prev!=null?380:280);
        sc.setFill(Color.TRANSPARENT);
        UIUtils.applyStyle(sc);
        popup.setScene(sc);

        box.setScaleX(0.92); box.setScaleY(0.92); box.setOpacity(0);
        ScaleTransition st = new ScaleTransition(Duration.millis(200), box); st.setToX(1); st.setToY(1); st.setInterpolator(Interpolator.EASE_OUT);
        FadeTransition ft = new FadeTransition(Duration.millis(200), box); ft.setToValue(1);
        new ParallelTransition(st, ft).play();
        popup.show();
    }

    private static void addDetailRow(GridPane grid, int row, String key, String val) {
        Label k = new Label(key.toUpperCase());
        k.setStyle("-fx-font-size:9.5px;-fx-font-weight:700;-fx-text-fill:#9aa1b0;-fx-letter-spacing:1px;");
        k.setPrefWidth(90);
        Label v = new Label(val);
        v.setStyle("-fx-font-size:13.5px;-fx-font-weight:600;-fx-text-fill:" + UIUtils.textDark() + ";");
        v.setWrapText(true);
        grid.add(k, 0, row); grid.add(v, 1, row);
    }

    private static Scene buildExamScene(Exam exam, Student student, Stage stage, HelloApplication app) {
        List<Question> questions = new ArrayList<>(exam.getQuestionsMap().keySet());
        int totalQ = questions.size();
        if (totalQ == 0) { app.showError("No Questions", "This examination has no questions loaded."); return createDashboardScene(stage, student, app); }

        Map<Integer, String> savedAnswers = ServerClient.get().loadInProgressAnswers(student.getID(), exam.getDbId());
        Map<Integer, String> answers = savedAnswers != null ? new java.util.HashMap<>(savedAnswers) : new java.util.HashMap<>();
        Set<Integer> flagged = ServerClient.get().loadInProgressFlagged(student.getID(), exam.getDbId());
        boolean[] submitted = { false };

        int durationSecs;
        try { durationSecs = Integer.parseInt(exam.getDuration().replaceAll("[^0-9]", "")) * 60; }
        catch (Exception ex2) { durationSecs = 30 * 60; }

        long examRemaining = exam.getRemainingMillis();
        final int TOTAL_SECS = durationSecs;
        long[] remaining = { examRemaining == Long.MAX_VALUE ? TOTAL_SECS : Math.min(examRemaining / 1000, TOTAL_SECS) };

        String bg      = UIUtils.bgContent();
        String card    = UIUtils.bgCard();
        String bdr     = UIUtils.border();
        String txtD    = UIUtils.textDark();
        String txtM    = UIUtils.textMid();
        String txtS    = UIUtils.textSubtle();
        String barBg   = UIUtils.darkMode ? "#1a2234" : "#f4f5f7";
        String barBdr  = UIUtils.darkMode ? "#29334a" : "#e6e8ec";
        String optBg   = UIUtils.darkMode ? "#1c2333" : "#ffffff";
        String optBdr  = UIUtils.darkMode ? "#29334a" : "#e6e8ec";
        String optSel  = "#0f7d74";
        String optSelBg= UIUtils.darkMode ? "rgba(15,125,116,0.16)" : "rgba(15,125,116,0.08)";
        String optHov  = UIUtils.darkMode ? "#222a3c" : "#f0f7f6";
        String navBg   = UIUtils.darkMode ? "#171d2b" : "#f7f8fb";

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color:" + bg + ";");

        HBox topBar = new HBox(20); topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(0, 24, 0, 24)); topBar.setPrefHeight(58);
        topBar.setStyle("-fx-background-color:" + barBg + ";-fx-border-color:" + barBdr + ";-fx-border-width:0 0 1 0;");

        VBox examIdBox = new VBox(2);
        Label subjectTag = new Label(exam.getSubject().toUpperCase());
        subjectTag.setStyle("-fx-font-size:9.5px;-fx-font-weight:700;-fx-text-fill:" + txtS + ";-fx-letter-spacing:1.8px;");
        String et = (exam.getTitle() != null && !exam.getTitle().isBlank()) ? exam.getTitle() : exam.getSubject() + " Examination";
        Label examNameLbl = new Label(et);
        examNameLbl.setStyle("-fx-font-size:14px;-fx-font-weight:700;-fx-text-fill:" + txtD + ";");
        examIdBox.getChildren().addAll(subjectTag, examNameLbl);

        Region topSpacer = new Region(); HBox.setHgrow(topSpacer, Priority.ALWAYS);

        VBox timerBox = new VBox(2); timerBox.setAlignment(Pos.CENTER);
        Label timerCaption = new Label("TIME REMAINING");
        timerCaption.setStyle("-fx-font-size:9px;-fx-font-weight:700;-fx-text-fill:" + txtS + ";-fx-letter-spacing:1.4px;");
        Label timerLbl = new Label(formatTime((int)remaining[0]));
        timerLbl.setStyle("-fx-font-size:26px;-fx-font-weight:700;-fx-font-family:Monospaced;-fx-text-fill:#0f7d74;");
        ProgressBar timeProg = new ProgressBar((double)remaining[0] / TOTAL_SECS);
        timeProg.setPrefWidth(150); timeProg.setPrefHeight(3);
        timeProg.setStyle("-fx-accent:#0f7d74;-fx-background-color:" + barBdr + ";-fx-background-radius:99;");
        timerBox.getChildren().addAll(timerCaption, timerLbl, timeProg);

        Label studentLbl = new Label(student.getName());
        studentLbl.setStyle("-fx-font-size:12px;-fx-text-fill:" + txtM + ";");

        topBar.getChildren().addAll(examIdBox, topSpacer, timerBox, studentLbl);
        root.setTop(topBar);

        VBox navPanel = new VBox(10); navPanel.setPrefWidth(172);
        navPanel.setPadding(new Insets(18, 12, 18, 12));
        navPanel.setStyle("-fx-background-color:" + navBg + ";-fx-border-color:" + barBdr + ";-fx-border-width:0 0 0 1;");

        Label navCaption = UIUtils.sectionLabel("Questions");
        TilePane navGrid = new TilePane(); navGrid.setHgap(4); navGrid.setVgap(4); navGrid.setPrefColumns(5);

        Label answeredCount = new Label(answers.size() + " / " + totalQ);
        answeredCount.setStyle("-fx-font-size:13px;-fx-font-weight:700;-fx-text-fill:" + txtD + ";");
        Label answeredLabel = new Label("answered");
        answeredLabel.setStyle("-fx-font-size:10px;-fx-text-fill:" + txtM + ";");

        VBox legend = new VBox(5);
        for (String[] leg : new String[][]{{"#0f7d74","Answered"},{"#b45309","Flagged"}}) {
            HBox lr = new HBox(6); lr.setAlignment(Pos.CENTER_LEFT);
            Circle dotL = new Circle(4, Color.web(leg[0]));
            Label lt = new Label(leg[1]); lt.setStyle("-fx-font-size:11px;-fx-text-fill:" + txtM + ";");
            lr.getChildren().addAll(dotL, lt);
            legend.getChildren().add(lr);
        }
        navPanel.getChildren().addAll(navCaption, navGrid, UIUtils.divider(), answeredCount, answeredLabel, UIUtils.divider(), legend);
        root.setRight(navPanel);

        ScrollPane centerScroll = new ScrollPane();
        centerScroll.setFitToWidth(true);
        centerScroll.setStyle("-fx-background:" + bg + ";-fx-background-color:" + bg + ";");

        VBox allQuestionsPage = new VBox(16);
        allQuestionsPage.setPadding(new Insets(26, 34, 26, 34));
        allQuestionsPage.setStyle("-fx-background-color:" + bg + ";");

        VBox[] qCardBoxes = new VBox[totalQ];
        Runnable[] refreshNav = { null };

        Button[] navBtns2 = new Button[totalQ];
        for (int i = 0; i < totalQ; i++) {
            final int fi = i;
            Button nb = new Button(String.valueOf(i+1));
            nb.setPrefSize(26,26); nb.setCursor(javafx.scene.Cursor.HAND);
            nb.setStyle(qNavStyle(barBdr, UIUtils.darkMode));
            nb.setOnAction(e -> {
                if (qCardBoxes[fi] != null) {
                    double totalH = allQuestionsPage.getBoundsInLocal().getHeight();
                    double cardY  = qCardBoxes[fi].getBoundsInParent().getMinY();
                    centerScroll.setVvalue(totalH > 0 ? cardY / totalH : 0);
                }
            });
            navBtns2[fi] = nb;
            navGrid.getChildren().add(nb);
            if (answers.containsKey(i)) nb.setStyle(qNavStyleActive("#0f7d74", UIUtils.darkMode));
            if (flagged.contains(i))    nb.setStyle(qNavStyleActive("#b45309", UIUtils.darkMode));
        }

        Runnable autoSave = () -> ServerClient.get().saveInProgress(student.getID(), exam.getDbId(), answers, flagged);

        for (int qi = 0; qi < totalQ; qi++) {
            final int idx = qi;
            Question q = questions.get(qi);

            VBox qCard = new VBox(14); qCard.setPadding(new Insets(20, 24, 20, 24));
            qCard.setStyle(
                    "-fx-background-color:" + card + ";" +
                            "-fx-border-color:" + bdr + ";" +
                            "-fx-border-radius:9;-fx-background-radius:9;-fx-border-width:1;"
            );
            DropShadow ds2 = new DropShadow();
            ds2.setColor(Color.color(0,0,0, UIUtils.darkMode?0.22:0.04));
            ds2.setOffsetY(1); ds2.setRadius(7);
            qCard.setEffect(ds2);
            qCardBoxes[idx] = qCard;

            HBox badgeRow = new HBox(8); badgeRow.setAlignment(Pos.CENTER_LEFT);
            Label qNumBadge = new Label("Q" + (idx+1));
            qNumBadge.setStyle("-fx-background-color:rgba(15,125,116,0.12);-fx-text-fill:#0f7d74;-fx-font-weight:700;-fx-font-size:11px;-fx-padding:2 9;-fx-background-radius:4;");
            double marksVal = exam.getQuestionsMap().get(q);
            Label marksBadge = UIUtils.badge((int)marksVal + " mark" + (marksVal>1?"s":""), UIUtils.ACCENT_ORG);
            badgeRow.getChildren().addAll(qNumBadge, marksBadge);

            Button flagBtn = new Button(flagged.contains(idx) ? "Flagged" : "Flag");
            flagBtn.setStyle(flagBtnStyle(flagged.contains(idx), UIUtils.darkMode));
            flagBtn.setCursor(javafx.scene.Cursor.HAND);
            Region br = new Region(); HBox.setHgrow(br, Priority.ALWAYS);
            HBox topRow = new HBox(8, badgeRow, br, flagBtn);
            topRow.setAlignment(Pos.CENTER_LEFT);

            Label qText = new Label(q.getQuestionText());
            qText.setStyle("-fx-font-size:14.5px;-fx-font-weight:600;-fx-text-fill:" + txtD + ";");
            qText.setWrapText(true);

            qCard.getChildren().addAll(topRow, qText);

            if (q instanceof MCQ mcq) {
                String[] opts = mcq.getOptions();
                VBox optionsBox = new VBox(7);

                Runnable[] rebuild = { null };
                rebuild[0] = () -> {
                    optionsBox.getChildren().clear();
                    String savedAns = answers.get(idx);
                    for (int oi = 0; oi < opts.length; oi++) {
                        final String optChar = String.valueOf((char)('A'+oi));
                        boolean sel = optChar.equals(savedAns);

                        HBox optRow = new HBox(12); optRow.setAlignment(Pos.CENTER_LEFT);
                        optRow.setPadding(new Insets(10, 16, 10, 14));
                        optRow.setCursor(javafx.scene.Cursor.HAND);
                        optRow.setStyle(
                                "-fx-background-color:" + (sel ? optSelBg : optBg) + ";" +
                                        "-fx-border-color:" + (sel ? optSel : optBdr) + ";" +
                                        "-fx-border-width:" + (sel ? "1.5" : "1") + ";" +
                                        "-fx-border-radius:7;-fx-background-radius:7;"
                        );

                        Label letter = new Label(optChar);
                        letter.setStyle(
                                "-fx-font-size:11.5px;-fx-font-weight:700;" +
                                        "-fx-min-width:26;-fx-min-height:26;-fx-alignment:center;" +
                                        "-fx-background-radius:5;" +
                                        "-fx-background-color:" + (sel ? optSel : (UIUtils.darkMode?"#29334a":"#f0f1f4")) + ";" +
                                        "-fx-text-fill:" + (sel ? "white" : txtS) + ";"
                        );

                        Label optText = new Label(opts[oi]);
                        optText.setStyle("-fx-font-size:13.5px;-fx-text-fill:" + (sel ? txtD : txtM) + ";");
                        optText.setWrapText(true);
                        optRow.getChildren().addAll(letter, optText);

                        final Runnable[] rebRef = rebuild;
                        optRow.setOnMouseClicked(me -> {
                            answers.put(idx, optChar);
                            rebRef[0].run();
                            if (refreshNav[0]!=null) refreshNav[0].run();
                            autoSave.run(); // auto-save on answer
                        });
                        optRow.setOnMouseEntered(me -> { if (!optChar.equals(answers.getOrDefault(idx,null))) optRow.setStyle("-fx-background-color:"+optHov+";-fx-border-color:"+bdr+";-fx-border-width:1;-fx-border-radius:7;-fx-background-radius:7;"); });
                        optRow.setOnMouseExited(me  -> { if (!optChar.equals(answers.getOrDefault(idx,null))) optRow.setStyle("-fx-background-color:"+optBg+";-fx-border-color:"+optBdr+";-fx-border-width:1;-fx-border-radius:7;-fx-background-radius:7;"); });
                        optionsBox.getChildren().add(optRow);
                    }
                    boolean isFlagged = flagged.contains(idx);
                    flagBtn.setText(isFlagged ? "Flagged" : "Flag");
                    flagBtn.setStyle(flagBtnStyle(isFlagged, UIUtils.darkMode));
                    if (navBtns2[idx]!=null)
                        navBtns2[idx].setStyle(flagged.contains(idx) ? qNavStyleActive("#b45309",UIUtils.darkMode) : answers.containsKey(idx) ? qNavStyleActive("#0f7d74",UIUtils.darkMode) : qNavStyle(barBdr,UIUtils.darkMode));
                    answeredCount.setText(answers.size() + " / " + totalQ);
                };
                rebuild[0].run();
                flagBtn.setOnAction(e -> {
                    if (flagged.contains(idx)) flagged.remove(idx); else flagged.add(idx);
                    rebuild[0].run();
                    autoSave.run();
                });
                qCard.getChildren().add(optionsBox);

            } else {
                TextField ansField = new TextField(answers.getOrDefault(idx, ""));
                ansField.setPromptText("Enter your answer...");
                ansField.setStyle(
                        "-fx-background-color:" + optBg + ";-fx-border-color:" + optBdr + ";" +
                                "-fx-border-radius:7;-fx-background-radius:7;" +
                                "-fx-font-size:14px;-fx-text-fill:" + txtD + ";-fx-padding:11;" +
                                "-fx-prompt-text-fill:" + txtS + ";"
                );
                ansField.textProperty().addListener((obs, o, n) -> {
                    if (n.isBlank()) answers.remove(idx); else answers.put(idx, n);
                    if (navBtns2[idx]!=null) navBtns2[idx].setStyle(flagged.contains(idx) ? qNavStyleActive("#b45309",UIUtils.darkMode) : !n.isBlank() ? qNavStyleActive("#0f7d74",UIUtils.darkMode) : qNavStyle(barBdr,UIUtils.darkMode));
                    answeredCount.setText(answers.size() + " / " + totalQ);
                    autoSave.run(); // auto-save on keystroke
                });
                flagBtn.setOnAction(e -> {
                    if (flagged.contains(idx)) flagged.remove(idx); else flagged.add(idx);
                    boolean fl = flagged.contains(idx);
                    flagBtn.setText(fl ? "Flagged" : "Flag");
                    flagBtn.setStyle(flagBtnStyle(fl, UIUtils.darkMode));
                    if (navBtns2[idx]!=null) navBtns2[idx].setStyle(fl ? qNavStyleActive("#b45309",UIUtils.darkMode) : answers.containsKey(idx) ? qNavStyleActive("#0f7d74",UIUtils.darkMode) : qNavStyle(barBdr,UIUtils.darkMode));
                    autoSave.run();
                });

                Button btnSaveAns = UIUtils.primaryBtn("", "Save Answer", UIUtils.ACCENT_GREEN);
                btnSaveAns.setPrefHeight(36);
                btnSaveAns.setOnAction(e -> {
                    String cur = ansField.getText().trim();
                    if (cur.isBlank()) {
                        btnSaveAns.setText("Enter an answer first");
                        PauseTransition resetEmpty = new PauseTransition(Duration.seconds(1.5));
                        resetEmpty.setOnFinished(ev -> btnSaveAns.setText("Save Answer"));
                        resetEmpty.play();
                        return;
                    }
                    answers.put(idx, cur);
                    if (navBtns2[idx]!=null) navBtns2[idx].setStyle(
                            flagged.contains(idx) ? qNavStyleActive("#b45309", UIUtils.darkMode)
                                    : qNavStyleActive("#0f7d74", UIUtils.darkMode));
                    answeredCount.setText(answers.size() + " / " + totalQ);
                    autoSave.run();
                    btnSaveAns.setText("Answer Saved");
                    btnSaveAns.setStyle(
                            "-fx-background-color:#0e7a56;-fx-text-fill:white;-fx-font-weight:700;" +
                                    "-fx-font-size:12px;-fx-background-radius:6;-fx-padding:9 18;-fx-cursor:hand;"
                    );
                    String origStyle = "-fx-background-color:" + UIUtils.ACCENT_GREEN + ";-fx-text-fill:white;" +
                            "-fx-font-weight:600;-fx-font-size:13px;-fx-background-radius:6;" +
                            "-fx-padding:9 18;-fx-border-color:transparent;-fx-cursor:hand;";
                    PauseTransition resetSaved = new PauseTransition(Duration.seconds(1.8));
                    resetSaved.setOnFinished(ev -> {
                        btnSaveAns.setText("Save Answer");
                        btnSaveAns.setStyle(origStyle);
                    });
                    resetSaved.play();
                });

                HBox ansRow = new HBox(10, ansField, btnSaveAns);
                ansRow.setAlignment(Pos.CENTER_LEFT);
                HBox.setHgrow(ansField, Priority.ALWAYS);
                qCard.getChildren().add(ansRow);
            }

            allQuestionsPage.getChildren().add(qCard);
        }

        refreshNav[0] = () -> {
            for (int i = 0; i < totalQ; i++) {
                if (navBtns2[i]==null) continue;
                navBtns2[i].setStyle(flagged.contains(i) ? qNavStyleActive("#b45309",UIUtils.darkMode) : answers.containsKey(i) ? qNavStyleActive("#0f7d74",UIUtils.darkMode) : qNavStyle(barBdr,UIUtils.darkMode));
            }
            answeredCount.setText(answers.size() + " / " + totalQ);
        };

        centerScroll.setContent(allQuestionsPage);
        root.setCenter(centerScroll);

        HBox bottomBar = new HBox(16); bottomBar.setAlignment(Pos.CENTER);
        bottomBar.setPadding(new Insets(11, 28, 11, 28));
        bottomBar.setStyle("-fx-background-color:" + barBg + ";-fx-border-color:" + barBdr + ";-fx-border-width:1 0 0 0;");

        Label qMeta = new Label(totalQ + " questions  ·  scroll to answer  ·  answers auto-saved");
        qMeta.setStyle("-fx-font-size:12px;-fx-text-fill:" + txtM + ";");
        Region botSpacer = new Region(); HBox.setHgrow(botSpacer, Priority.ALWAYS);
        Button btnSubmit = UIUtils.primaryBtn("", "Submit Examination", UIUtils.ACCENT_TEAL);
        btnSubmit.setPrefHeight(42); btnSubmit.setPrefWidth(190);
        bottomBar.getChildren().addAll(qMeta, botSpacer, btnSubmit);
        root.setBottom(bottomBar);

        Runnable doSubmit = () -> {
            if (submitted[0]) return;
            submitted[0] = true;
            int correct = 0; double score = 0;
            for (int i = 0; i < totalQ; i++) {
                Question q = questions.get(i);
                String ans = answers.get(i);
                if (ans == null || ans.isBlank()) continue;
                if (q instanceof MCQ mcq) {
                    if (ans.charAt(0)-'A' == mcq.getCorrectIndex()) { correct++; score += exam.getQuestionsMap().get(q); }
                } else if (q instanceof TextQuestion tq) {
                    try { if (Math.abs(Double.parseDouble(ans.trim())-tq.getAnswer())<1e-9) { correct++; score += exam.getQuestionsMap().get(q); } } catch (NumberFormatException ignored) {}
                } else if (q instanceof RangeQuestion rq) {
                    try { double v=Double.parseDouble(ans.trim()); if(v>=rq.getMin()&&v<=rq.getMax()) { correct++; score+=exam.getQuestionsMap().get(q); } } catch (NumberFormatException ignored) {}
                }
            }
            final int fc=correct; final double fs=score;
            final Map<Integer,String> ansSnap = new java.util.HashMap<>(answers);
            final Set<Integer> flagSnap = new java.util.HashSet<>(flagged);

            ExamResult result = new ExamResult();
            result.studentId   = student.getID();
            result.examId      = exam.getDbId();
            result.examCode    = exam.getExamCode(); // store which code this attempt used
            result.examTitle   = exam.getTitle()!=null?exam.getTitle():exam.getSubject();
            result.examSubject = exam.getSubject();
            result.examGrade   = exam.getGrade();
            result.score       = fs;
            result.totalMarks  = exam.getTotalMarks();
            result.correct     = fc;
            result.totalQ      = totalQ;
            result.takenAt     = System.currentTimeMillis();
            ServerClient.get().resultSave(result); // also clears in-progress
            ServerClient.get().removeStudentExamCode(student.getID(), exam.getDbId());

            Platform.runLater(() -> showResultScene(stage, exam, student, app, fc, totalQ, fs, questions, ansSnap, flagSnap));
        };

        btnSubmit.setOnAction(e -> {
            long unanswered = 0;
            for (int i = 0; i < totalQ; i++) { String a=answers.get(i); if(a==null||a.isBlank()) unanswered++; }
            if (unanswered > 0) {
                Alert c = new Alert(Alert.AlertType.CONFIRMATION);
                c.setTitle("Submit Examination"); c.setHeaderText(null);
                c.setContentText(unanswered + " question" + (unanswered>1?"s":"") + " unanswered. Submit anyway?");
                c.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
                c.showAndWait().ifPresent(r -> { if (r==ButtonType.YES) doSubmit.run(); });
            } else doSubmit.run();
        });

        ScaleTransition pulse = new ScaleTransition(Duration.millis(500), timerLbl);
        pulse.setFromX(1); pulse.setToX(1.08); pulse.setFromY(1); pulse.setToY(1.08);
        pulse.setAutoReverse(true); pulse.setCycleCount(Animation.INDEFINITE);

        Timeline autoSaveTicker = new Timeline(new KeyFrame(Duration.seconds(15), e -> autoSave.run()));
        autoSaveTicker.setCycleCount(Animation.INDEFINITE); autoSaveTicker.play();

        Timeline timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            if (submitted[0]) return;
            remaining[0]--;
            timerLbl.setText(formatTime((int)remaining[0]));
            timeProg.setProgress((double)remaining[0]/TOTAL_SECS);
            if (remaining[0]<=300 && remaining[0]>60) {
                timerLbl.setStyle("-fx-font-size:26px;-fx-font-weight:700;-fx-font-family:Monospaced;-fx-text-fill:#b45309;");
                timeProg.setStyle("-fx-accent:#b45309;-fx-background-color:"+barBdr+";-fx-background-radius:99;");
                if (!pulse.getStatus().equals(Animation.Status.RUNNING)) pulse.play();
            } else if (remaining[0]<=60) {
                timerLbl.setStyle("-fx-font-size:26px;-fx-font-weight:700;-fx-font-family:Monospaced;-fx-text-fill:#c0392b;");
                timeProg.setStyle("-fx-accent:#c0392b;-fx-background-color:"+barBdr+";-fx-background-radius:99;");
            }
            if (remaining[0]<=0) { autoSaveTicker.stop(); doSubmit.run(); }
        }));
        timer.setCycleCount(Animation.INDEFINITE); timer.play();

        Scene scene = new Scene(root, 1100, 700);
        UIUtils.applyStyle(scene);
        return scene;
    }

    private static void showResultScene(Stage stage, Exam exam, Student student,
                                        HelloApplication app, int correct, int total,
                                        double score, List<Question> questions,
                                        Map<Integer, String> answers, Set<Integer> flagged) {
        double pct   = total > 0 ? (score / exam.getTotalMarks()) * 100 : 0;
        String grade = pct>=80?"A":pct>=65?"B":pct>=50?"C":pct>=35?"D":"F";
        String gcol  = pct>=65 ? UIUtils.ACCENT_GREEN : pct>=50 ? UIUtils.ACCENT_BLUE : UIUtils.ACCENT_RED;

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

        VBox center = new VBox(20);
        center.setPadding(new Insets(32, 48, 36, 48));
        center.setStyle("-fx-background-color:" + bg + ";");

        HBox resultHeader = new HBox(16); resultHeader.setAlignment(Pos.CENTER_LEFT);

        StackPane scoreBadge = new StackPane();
        Circle scBg = new Circle(44);
        scBg.setFill(Color.web(gcol, 0.10));
        scBg.setStroke(Color.web(gcol)); scBg.setStrokeWidth(2);
        VBox scInner = new VBox(1); scInner.setAlignment(Pos.CENTER);
        Label scVal = new Label(String.format("%.0f", score));
        scVal.setStyle("-fx-font-size:20px;-fx-font-weight:700;-fx-text-fill:" + gcol + ";");
        Label scOf = new Label("/" + (int)exam.getTotalMarks());
        scOf.setStyle("-fx-font-size:10px;-fx-text-fill:" + txtM + ";");
        scInner.getChildren().addAll(scVal, scOf);
        scoreBadge.getChildren().addAll(scBg, scInner);

        VBox headerText = new VBox(5);
        Label submitTitle = new Label("Examination Complete");
        submitTitle.setStyle("-fx-font-size:20px;-fx-font-weight:700;-fx-text-fill:" + txtD + ";");
        String examTitle2 = (exam.getTitle()!=null&&!exam.getTitle().isBlank()) ? exam.getTitle() : exam.getSubject();
        Label examSub2 = new Label(examTitle2 + "  ·  " + exam.getSubject());
        examSub2.setStyle("-fx-font-size:12.5px;-fx-text-fill:" + txtM + ";");

        HBox statChips = new HBox(8); statChips.setAlignment(Pos.CENTER_LEFT);
        Label gradeLbl = new Label("Grade  " + grade);
        gradeLbl.setStyle("-fx-font-size:13px;-fx-font-weight:700;-fx-text-fill:" + gcol + ";-fx-background-color:" + gcol + "18;-fx-padding:3 12;-fx-background-radius:4;");
        Label pctLbl = new Label(String.format("%.1f%%", pct));
        pctLbl.setStyle("-fx-font-size:13px;-fx-font-weight:600;-fx-text-fill:" + txtD + ";-fx-background-color:" + UIUtils.bgMuted() + ";-fx-padding:3 10;-fx-background-radius:4;");
        Label corrLbl = new Label(correct + " / " + total + " correct");
        corrLbl.setStyle("-fx-font-size:12px;-fx-text-fill:" + txtM + ";-fx-padding:3 0;");
        statChips.getChildren().addAll(gradeLbl, pctLbl, corrLbl);
        headerText.getChildren().addAll(submitTitle, examSub2, statChips);
        resultHeader.getChildren().addAll(scoreBadge, headerText);

        Label reviewHdr = new Label("Question Review");
        reviewHdr.setStyle("-fx-font-size:15px;-fx-font-weight:700;-fx-text-fill:" + txtD + ";");

        VBox reviewBox = new VBox(11);
        for (int i = 0; i < total; i++) {
            Question q = questions.get(i);
            String studentAns = answers.get(i);
            double qMark = exam.getQuestionsMap().get(q);
            boolean wasFlagged = flagged.contains(i);
            boolean unanswered = studentAns == null || studentAns.isBlank();

            boolean isCorrect = false; String correctDisplay = "";
            if (q instanceof MCQ mcq) {
                int ci = mcq.getCorrectIndex();
                correctDisplay = (char)('A'+ci) + " — " + mcq.getOptions()[ci];
                if (!unanswered && studentAns.charAt(0)-'A'==ci) isCorrect = true;
            } else if (q instanceof TextQuestion tq) {
                correctDisplay = String.valueOf(tq.getAnswer());
                if (!unanswered) try { if(Math.abs(Double.parseDouble(studentAns.trim())-tq.getAnswer())<1e-9) isCorrect=true; } catch (NumberFormatException ign) {}
            } else if (q instanceof RangeQuestion rq) {
                correctDisplay = rq.getMin() + " – " + rq.getMax();
                if (!unanswered) try { double v=Double.parseDouble(studentAns.trim()); if(v>=rq.getMin()&&v<=rq.getMax()) isCorrect=true; } catch (NumberFormatException ign) {}
            }

            String leftAccent = isCorrect ? "#0e7a56" : (unanswered ? "#c4ccd8" : "#c0392b");
            String cardBorder = isCorrect ? "#0e7a5640" : (unanswered ? bdr : "#c0392b40");

            HBox cardWrap = new HBox(0);
            Region accentBar = new Region();
            accentBar.setPrefWidth(4); accentBar.setMinWidth(4);
            accentBar.setStyle("-fx-background-color:" + leftAccent + ";-fx-background-radius:8 0 0 8;");

            VBox cardBody = new VBox(8); cardBody.setPadding(new Insets(12, 16, 12, 14));
            cardBody.setStyle(
                    "-fx-background-color:" + card + ";" +
                            "-fx-background-radius:0 8 8 0;" +
                            "-fx-border-color:" + cardBorder + ";" +
                            "-fx-border-width:1 1 1 0;-fx-border-radius:0 8 8 0;"
            );
            HBox.setHgrow(cardBody, Priority.ALWAYS);

            HBox topRow = new HBox(7); topRow.setAlignment(Pos.CENTER_LEFT);
            Label qNumLbl = new Label("Q" + (i+1));
            qNumLbl.setStyle("-fx-font-size:10.5px;-fx-font-weight:700;-fx-text-fill:#0f7d74;-fx-background-color:rgba(15,125,116,0.10);-fx-padding:2 8;-fx-background-radius:4;");
            Label markChip = new Label((int)qMark + " mark" + (qMark>1?"s":""));
            markChip.setStyle("-fx-font-size:10.5px;-fx-text-fill:" + txtS + ";-fx-background-color:" + UIUtils.bgMuted() + ";-fx-padding:2 8;-fx-background-radius:4;");
            Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
            if (wasFlagged) {
                Label flg = new Label("Flagged");
                flg.setStyle("-fx-font-size:10px;-fx-font-weight:700;-fx-text-fill:#b45309;-fx-background-color:#fef3c7;-fx-padding:2 7;-fx-background-radius:4;");
                topRow.getChildren().add(flg);
            }
            double earned = isCorrect ? qMark : 0;
            Label earnedChip = new Label(isCorrect ? "+" + (int)earned : (unanswered ? "No answer" : "0"));
            earnedChip.setStyle("-fx-font-size:10.5px;-fx-font-weight:700;" +
                    "-fx-text-fill:" + (isCorrect?"#0e7a56":(unanswered?txtS:"#c0392b")) + ";" +
                    "-fx-background-color:" + (isCorrect?"#d1f0e8":(unanswered?UIUtils.bgMuted():"#fde8e8")) + ";" +
                    "-fx-padding:2 8;-fx-background-radius:4;");
            topRow.getChildren().addAll(0, java.util.List.of(qNumLbl, markChip, sp));
            topRow.getChildren().add(earnedChip);

            Label qTxtLbl = new Label(q.getQuestionText());
            qTxtLbl.setStyle("-fx-font-size:13.5px;-fx-font-weight:600;-fx-text-fill:" + txtD + ";");
            qTxtLbl.setWrapText(true);

            VBox answerBlock = new VBox(4);
            if (q instanceof MCQ mcq) {
                String[] opts = mcq.getOptions();
                for (int oi = 0; oi < opts.length; oi++) {
                    String oc = String.valueOf((char)('A'+oi));
                    boolean isStudentPick = oc.equals(studentAns);
                    boolean isCorrectOpt  = oi==mcq.getCorrectIndex();

                    String rBg, rBorder, lBg, lTxt, oTxt;
                    if (isCorrectOpt && isStudentPick) {
                        rBg=UIUtils.darkMode?"rgba(14,122,86,0.18)":"#d1f0e8"; rBorder="#0e7a56";
                        lBg="#0e7a56"; lTxt="white"; oTxt="#0e7a56";
                    } else if (isCorrectOpt) {
                        rBg=UIUtils.darkMode?"rgba(14,122,86,0.08)":"#f0fdf8"; rBorder="#0e7a5660";
                        lBg="#0e7a5688"; lTxt="white"; oTxt=txtD;
                    } else if (isStudentPick) {
                        rBg=UIUtils.darkMode?"rgba(192,57,43,0.16)":"#fde8e8"; rBorder="#c0392b";
                        lBg="#c0392b"; lTxt="white"; oTxt="#c0392b";
                    } else {
                        rBg=card; rBorder=bdr;
                        lBg=UIUtils.darkMode?"#29334a":"#f0f1f4"; lTxt=txtS; oTxt=txtM;
                    }

                    HBox optRow = new HBox(10); optRow.setAlignment(Pos.CENTER_LEFT);
                    optRow.setPadding(new Insets(7, 12, 7, 12));
                    optRow.setStyle("-fx-background-color:"+rBg+";-fx-border-color:"+rBorder+";-fx-border-width:1;-fx-border-radius:6;-fx-background-radius:6;");
                    Label letter = new Label(oc);
                    letter.setStyle("-fx-font-size:10.5px;-fx-font-weight:700;-fx-min-width:24;-fx-min-height:24;-fx-alignment:center;-fx-background-radius:4;-fx-background-color:"+lBg+";-fx-text-fill:"+lTxt+";");
                    Label optLbl = new Label(opts[oi]); optLbl.setStyle("-fx-font-size:13px;-fx-text-fill:"+oTxt+";"); optLbl.setWrapText(true); HBox.setHgrow(optLbl, Priority.ALWAYS);
                    optRow.getChildren().addAll(letter, optLbl);
                    if (isCorrectOpt) optRow.getChildren().add(UIUtils.icon(UIUtils.ICO_CHECK, "#0e7a56", 12));
                    else if (isStudentPick) optRow.getChildren().add(UIUtils.icon(UIUtils.ICO_CLOSE, "#c0392b", 12));
                    answerBlock.getChildren().add(optRow);
                }
            } else {
                HBox ansRow = new HBox(16); ansRow.setAlignment(Pos.CENTER_LEFT);
                VBox yourAns = new VBox(3);
                Label yaCaption = UIUtils.sectionLabel("Your Answer");
                Label yaVal = new Label(unanswered ? "—" : studentAns);
                yaVal.setStyle("-fx-font-size:14px;-fx-font-weight:700;-fx-text-fill:" + (isCorrect?"#0e7a56":(unanswered?txtS:"#c0392b")) + ";");
                yourAns.getChildren().addAll(yaCaption, yaVal);
                VBox corrAns = new VBox(3);
                Label caCaption = UIUtils.sectionLabel(q instanceof RangeQuestion ? "Accepted Range" : "Correct Answer");
                Label caVal = new Label(correctDisplay); caVal.setStyle("-fx-font-size:14px;-fx-font-weight:700;-fx-text-fill:#0e7a56;");
                corrAns.getChildren().addAll(caCaption, caVal);
                Label arrow = new Label("→"); arrow.setStyle("-fx-font-size:16px;-fx-text-fill:"+txtS+";");
                ansRow.getChildren().addAll(yourAns, arrow, corrAns);
                answerBlock.getChildren().add(ansRow);
            }

            cardBody.getChildren().addAll(topRow, qTxtLbl, answerBlock);
            cardWrap.getChildren().addAll(accentBar, cardBody);
            DropShadow cds = new DropShadow(); cds.setColor(Color.color(0,0,0, UIUtils.darkMode?0.18:0.04)); cds.setRadius(6); cds.setOffsetY(1); cardWrap.setEffect(cds);
            reviewBox.getChildren().add(cardWrap);
        }

        Button btnBack2 = UIUtils.primaryBtn("", "Return to Dashboard", UIUtils.ACCENT_TEAL);
        btnBack2.setPrefWidth(220); btnBack2.setPrefHeight(42);
        btnBack2.setOnAction(e -> { activeNavIndex = 0; stage.setScene(createDashboardScene(stage, student, app)); });

        center.getChildren().addAll(resultHeader, UIUtils.divider(), reviewHdr, reviewBox, UIUtils.divider(), btnBack2);

        center.setOpacity(0); center.setTranslateY(16);
        scroll.setContent(center);
        root.setCenter(scroll);

        Scene scene = new Scene(root, 1100, 700);
        UIUtils.applyStyle(scene);
        stage.setScene(scene);

        FadeTransition ft2 = new FadeTransition(Duration.millis(340), center); ft2.setToValue(1);
        TranslateTransition tt2 = new TranslateTransition(Duration.millis(340), center);
        tt2.setToY(0); tt2.setInterpolator(Interpolator.EASE_OUT);
        new ParallelTransition(ft2, tt2).play();
    }


    private static void renderMyResultsPage(javafx.scene.layout.AnchorPane area, Stage stage, Student student, HelloApplication app) {
        area.getChildren().clear();
        VBox page = new VBox(18); page.setPadding(new Insets(30, 38, 30, 38));
        page.setStyle("-fx-background-color:" + UIUtils.bgContent() + ";");

        HBox titleRow = new HBox(14); titleRow.setAlignment(Pos.CENTER_LEFT);
        titleRow.getChildren().addAll(backBtn(area, stage, student, app), UIUtils.heading("Academic Record"));
        page.getChildren().addAll(titleRow, UIUtils.subheading("Your examination results"), UIUtils.divider());

        List<ExamResult> results = ServerClient.get().loadResultsForStudent(student.getID());
        if (results.isEmpty()) {
            page.getChildren().add(emptyState(UIUtils.ICO_HISTORY, "No Results Yet", "Complete an examination to see your record here."));
        } else {
            for (ExamResult r : results) {
                String gc = r.pct()>=65 ? UIUtils.ACCENT_GREEN : r.pct()>=50 ? UIUtils.ACCENT_BLUE : UIUtils.ACCENT_RED;
                VBox card = UIUtils.card(700); card.setMaxWidth(Double.MAX_VALUE);
                card.setPadding(new Insets(16, 20, 16, 20)); card.setSpacing(0);

                HBox row = new HBox(14); row.setAlignment(Pos.CENTER_LEFT);
                StackPane sc = new StackPane();
                Circle cBg = new Circle(30); cBg.setFill(Color.web(gc, 0.10)); cBg.setStroke(Color.web(gc)); cBg.setStrokeWidth(1.5);
                VBox scIn = new VBox(0); scIn.setAlignment(Pos.CENTER);
                Label scVal = new Label(String.format("%.0f", r.score));
                scVal.setStyle("-fx-font-size:13px;-fx-font-weight:700;-fx-text-fill:" + gc + ";");
                Label scOf = new Label("/"+((int)r.totalMarks));
                scOf.setStyle("-fx-font-size:9.5px;-fx-text-fill:"+UIUtils.textMid()+";");
                scIn.getChildren().addAll(scVal, scOf);
                sc.getChildren().addAll(cBg, scIn);

                VBox info = new VBox(4);
                String title = (r.examTitle!=null&&!r.examTitle.isBlank())?r.examTitle:r.examSubject;
                Label nameLbl = new Label(title); nameLbl.setStyle("-fx-font-size:14px;-fx-font-weight:700;-fx-text-fill:"+UIUtils.textDark()+";");
                Label meta = new Label(r.examSubject + "  ·  Grade " + r.examGrade + "  ·  " + r.correct+"/"+r.totalQ+" correct  ·  "+r.dateStr());
                meta.setStyle("-fx-font-size:11px;-fx-text-fill:"+UIUtils.textSubtle()+";");
                info.getChildren().addAll(nameLbl, meta);

                Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);

                VBox barBox = new VBox(3); barBox.setMinWidth(90);
                Label pctLbl = new Label(String.format("%.1f%%", r.pct()));
                pctLbl.setStyle("-fx-font-size:11px;-fx-text-fill:"+UIUtils.textMid()+";");
                javafx.scene.control.ProgressBar pb = new javafx.scene.control.ProgressBar(r.pct()/100);
                pb.setPrefWidth(90); pb.setPrefHeight(5);
                pb.setStyle("-fx-accent:"+gc+";-fx-background-color:"+UIUtils.border()+";-fx-background-radius:99;");
                barBox.getChildren().addAll(pctLbl, pb);

                Label gradeBadge = new Label("Grade " + r.grade());
                gradeBadge.setStyle("-fx-font-size:12px;-fx-font-weight:700;-fx-text-fill:"+gc+";-fx-background-color:"+gc+"18;-fx-padding:3 10;-fx-background-radius:4;");

                row.getChildren().addAll(sc, info, spacer, barBox, gradeBadge);
                card.getChildren().add(row);
                page.getChildren().add(card);
            }
        }
        wrapInScroll(area, page);
    }

    private static void renderAnalyticsPage(javafx.scene.layout.AnchorPane area, Stage stage, Student student, HelloApplication app) {
        area.getChildren().clear();
        VBox page = new VBox(22); page.setPadding(new Insets(30, 38, 30, 38));
        page.setStyle("-fx-background-color:" + UIUtils.bgContent() + ";");

        HBox titleRow = new HBox(14); titleRow.setAlignment(Pos.CENTER_LEFT);
        titleRow.getChildren().addAll(backBtn(area, stage, student, app), UIUtils.heading("Performance Analytics"));
        page.getChildren().addAll(titleRow, UIUtils.subheading("Score history and subject breakdown"), UIUtils.divider());

        List<ExamResult> results = ServerClient.get().loadResultsForStudent(student.getID());
        if (results.isEmpty()) {
            page.getChildren().add(emptyState(UIUtils.ICO_ANALYTICS, "No Data Yet", "Complete some examinations to view your analytics."));
            wrapInScroll(area, page);
            return;
        }

        double avg  = results.stream().mapToDouble(ExamResult::pct).average().orElse(0);
        double best = results.stream().mapToDouble(ExamResult::pct).max().orElse(0);
        long passed = results.stream().filter(r->r.pct()>=50).count();

        HBox statRow = new HBox(12); statRow.setAlignment(Pos.CENTER_LEFT);
        statRow.getChildren().addAll(
                UIUtils.statCard(UIUtils.ICO_ANALYTICS, String.format("%.1f%%",avg), "Average Score",  UIUtils.ACCENT_TEAL),
                UIUtils.statCard(UIUtils.ICO_TROPHY,    String.format("%.1f%%",best),"Best Score",     UIUtils.ACCENT_GREEN),
                UIUtils.statCard(UIUtils.ICO_CHECK,     String.valueOf(passed),       "Passed",         UIUtils.ACCENT_PURP),
                UIUtils.statCard(UIUtils.ICO_EXAM,      String.valueOf(results.size()),"Total Taken",   UIUtils.ACCENT_ORG)
        );

        VBox chartCard = UIUtils.card(700); chartCard.setMaxWidth(Double.MAX_VALUE);
        chartCard.setPadding(new Insets(18)); chartCard.setSpacing(10);
        Label chartHdr = new Label("Score History");
        chartHdr.setStyle("-fx-font-size:13px;-fx-font-weight:700;-fx-text-fill:"+UIUtils.textDark()+";");
        chartCard.getChildren().add(chartHdr);

        List<ExamResult> recent = results.subList(0, Math.min(8, results.size()));
        double BAR_MAX = 380;

        for (int i = recent.size()-1; i >= 0; i--) {
            ExamResult r = recent.get(i);
            String gc = r.pct()>=65?UIUtils.ACCENT_GREEN:r.pct()>=50?UIUtils.ACCENT_BLUE:UIUtils.ACCENT_RED;
            String lb = (r.examTitle!=null&&!r.examTitle.isBlank()?r.examTitle:r.examSubject);
            if (lb.length()>22) lb=lb.substring(0,20)+"…";
            Label nameLbl = new Label(lb); nameLbl.setStyle("-fx-font-size:11px;-fx-text-fill:"+UIUtils.textMid()+";"); nameLbl.setMinWidth(148);
            double barW = BAR_MAX*(r.pct()/100);
            Region bar = new Region(); bar.setPrefWidth(Math.max(barW,4)); bar.setPrefHeight(18);
            bar.setStyle("-fx-background-color:"+gc+";-fx-background-radius:3;");
            Label valLbl = new Label(String.format("%.0f%%",r.pct())); valLbl.setStyle("-fx-font-size:11px;-fx-font-weight:700;-fx-text-fill:"+gc+";");
            HBox barRow = new HBox(8, nameLbl, bar, valLbl); barRow.setAlignment(Pos.CENTER_LEFT);
            chartCard.getChildren().add(barRow);
        }

        VBox subCard = UIUtils.card(700); subCard.setMaxWidth(Double.MAX_VALUE);
        subCard.setPadding(new Insets(18)); subCard.setSpacing(9);
        Label subHdr = new Label("By Subject");
        subHdr.setStyle("-fx-font-size:13px;-fx-font-weight:700;-fx-text-fill:"+UIUtils.textDark()+";");
        subCard.getChildren().add(subHdr);

        Map<String,List<ExamResult>> bySub = new java.util.LinkedHashMap<>();
        for (ExamResult r : results) bySub.computeIfAbsent(r.examSubject, k->new ArrayList<>()).add(r);
        String[] subColors = {UIUtils.ACCENT_TEAL,UIUtils.ACCENT_GREEN,UIUtils.ACCENT_PURP,UIUtils.ACCENT_ORG,UIUtils.ACCENT_YELL};
        int ci = 0;
        for (Map.Entry<String,List<ExamResult>> e : bySub.entrySet()) {
            String c = subColors[ci++ % subColors.length];
            double avg2 = e.getValue().stream().mapToDouble(ExamResult::pct).average().orElse(0);
            HBox sr = new HBox(10); sr.setAlignment(Pos.CENTER_LEFT);
            Circle dot = new Circle(5, Color.web(c));
            Label subLbl = new Label(e.getKey()); subLbl.setMinWidth(138);
            subLbl.setStyle("-fx-font-size:12px;-fx-font-weight:700;-fx-text-fill:"+UIUtils.textDark()+";");
            javafx.scene.control.ProgressBar pb = new javafx.scene.control.ProgressBar(avg2/100);
            pb.setPrefWidth(180); pb.setPrefHeight(7);
            pb.setStyle("-fx-accent:"+c+";-fx-background-color:"+UIUtils.border()+";-fx-background-radius:99;");
            Label avgLbl = new Label(String.format("%.1f%% avg  (%d)",avg2,e.getValue().size()));
            avgLbl.setStyle("-fx-font-size:11px;-fx-text-fill:"+UIUtils.textSubtle()+";");
            sr.getChildren().addAll(dot, subLbl, pb, avgLbl);
            subCard.getChildren().add(sr);
        }

        page.getChildren().addAll(statRow, chartCard, subCard);
        wrapInScroll(area, page);
    }

    private static void renderLeaderboardPage(javafx.scene.layout.AnchorPane area, Stage stage, Student student, HelloApplication app) {
        area.getChildren().clear();
        VBox page = new VBox(18); page.setPadding(new Insets(30, 38, 30, 38));
        page.setStyle("-fx-background-color:" + UIUtils.bgContent() + ";");

        HBox titleRow = new HBox(14); titleRow.setAlignment(Pos.CENTER_LEFT);
        titleRow.getChildren().addAll(backBtn(area, stage, student, app), UIUtils.heading("Leaderboard"));
        page.getChildren().addAll(titleRow, UIUtils.subheading("Top performances per examination"), UIUtils.divider());

        List<ExamResult> all = ServerClient.get().loadAllResults();
        if (all.isEmpty()) {
            page.getChildren().add(emptyState(UIUtils.ICO_TROPHY, "No Results Yet", "Be the first to complete an examination."));
            wrapInScroll(area, page);
            return;
        }

        Map<Integer,List<ExamResult>> byExam = new java.util.LinkedHashMap<>();
        for (ExamResult r : all) byExam.computeIfAbsent(r.examId, k->new ArrayList<>()).add(r);

        for (Map.Entry<Integer,List<ExamResult>> entry : byExam.entrySet()) {
            List<ExamResult> top = entry.getValue().stream()
                    .sorted((a,b)->Double.compare(b.score,a.score)).limit(10)
                    .collect(java.util.stream.Collectors.toList());

            ExamResult first = top.get(0);
            String examLabel = (first.examTitle!=null&&!first.examTitle.isBlank())?first.examTitle:first.examSubject;

            VBox card = UIUtils.card(700); card.setMaxWidth(Double.MAX_VALUE);
            card.setPadding(new Insets(16, 20, 16, 20)); card.setSpacing(7);

            HBox hdr = new HBox(10); hdr.setAlignment(Pos.CENTER_LEFT);
            Region trophyIco = UIUtils.icon(UIUtils.ICO_TROPHY, UIUtils.ACCENT_ORG, 14);
            Label examHdr = new Label(examLabel + "  ·  " + first.examSubject + "  Grade " + first.examGrade);
            examHdr.setStyle("-fx-font-size:14px;-fx-font-weight:700;-fx-text-fill:"+UIUtils.textDark()+";");
            hdr.getChildren().addAll(trophyIco, examHdr);
            card.getChildren().addAll(hdr, UIUtils.divider());

            String[] medals = {"1st","2nd","3rd"};
            for (int rank = 0; rank < top.size(); rank++) {
                ExamResult r = top.get(rank);
                String gc = r.pct()>=65?UIUtils.ACCENT_GREEN:r.pct()>=50?UIUtils.ACCENT_BLUE:UIUtils.ACCENT_RED;
                boolean isMe = r.studentId.equals(student.getID());

                HBox row = new HBox(12); row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(6, 10, 6, 10));
                if (isMe) row.setStyle("-fx-background-color:rgba(15,125,116,0.08);-fx-background-radius:6;");
                else if (rank%2==0) row.setStyle("-fx-background-color:"+UIUtils.bgMuted()+";-fx-background-radius:6;");

                Label rankLbl = new Label(rank<3 ? medals[rank] : "#"+(rank+1));
                rankLbl.setStyle("-fx-font-size:11.5px;-fx-font-weight:700;-fx-text-fill:" + (rank==0?UIUtils.ACCENT_ORG:rank==1?UIUtils.textMid():UIUtils.textSubtle()) + ";");
                rankLbl.setMinWidth(36);

                Label idLbl = new Label(isMe ? "You  ("+r.studentId+")" : r.studentId);
                idLbl.setStyle("-fx-font-size:13px;-fx-font-weight:"+(isMe?"700":"500")+";-fx-text-fill:"+(isMe?"#0f7d74":UIUtils.textDark())+";");
                idLbl.setMinWidth(160);

                Region sp2 = new Region(); HBox.setHgrow(sp2, Priority.ALWAYS);
                javafx.scene.control.ProgressBar pb = new javafx.scene.control.ProgressBar(r.pct()/100);
                pb.setPrefWidth(120); pb.setPrefHeight(6);
                pb.setStyle("-fx-accent:"+gc+";-fx-background-color:"+UIUtils.border()+";-fx-background-radius:99;");
                Label scoreLbl = new Label(String.format("%.0f / %.0f  (%.1f%%)",r.score,r.totalMarks,r.pct()));
                scoreLbl.setStyle("-fx-font-size:12px;-fx-font-weight:600;-fx-text-fill:"+gc+";");
                scoreLbl.setMinWidth(148);
                row.getChildren().addAll(rankLbl, idLbl, sp2, pb, scoreLbl);
                card.getChildren().add(row);
            }
            page.getChildren().add(card);
        }
        wrapInScroll(area, page);
    }

    private static void renderAnnouncementsPage(javafx.scene.layout.AnchorPane area,
                                                Stage stage, Student student, HelloApplication app) {
        area.getChildren().clear();

        VBox page = new VBox(0);
        page.setStyle("-fx-background-color:" + UIUtils.bgContent() + ";");

        // ── Header ───────────────────────────────────────────────────────────
        VBox headerSection = new VBox(4);
        headerSection.setPadding(new Insets(28, 38, 20, 38));

        HBox titleRow = new HBox(14); titleRow.setAlignment(Pos.CENTER_LEFT);
        titleRow.getChildren().addAll(backBtn(area, stage, student, app), UIUtils.heading("Announcements"));
        Label subL = UIUtils.subheading("Notices and reminders posted by your instructors");
        headerSection.getChildren().addAll(titleRow, subL);

        // ── Live announcement strip (top banner for newest non-expired) ──────
        VBox bannerBox = new VBox(0);
        bannerBox.setPadding(new Insets(0, 38, 0, 38));

        // ── List ─────────────────────────────────────────────────────────────
        VBox listSection = new VBox(10);
        listSection.setPadding(new Insets(18, 38, 32, 38));

        ServerClient.get().purgeExpiredAnnouncements();
        java.util.List<Announcement> announcements = ServerClient.get().loadAnnouncements()
                .stream().filter(a -> !a.isExpired()).collect(java.util.stream.Collectors.toList());
        for (Announcement a : announcements) ServerClient.get().markAnnouncementRead(student.getID(), a.id);

        if (announcements.isEmpty()) {
            VBox empty = new VBox(12); empty.setAlignment(Pos.CENTER);
            empty.setPadding(new Insets(52));
            empty.setStyle("-fx-background-color:" + UIUtils.bgCard() + ";-fx-background-radius:9;" +
                    "-fx-border-color:" + UIUtils.border() + ";-fx-border-radius:9;-fx-border-width:1;" +
                    "-fx-border-style:dashed;");
            StackPane ico = new StackPane(UIUtils.icon(UIUtils.ICO_ANNOUNCE, UIUtils.textSubtle(), 22));
            ico.setPrefSize(52, 52);
            ico.setStyle("-fx-background-color:" + UIUtils.bgMuted() + ";-fx-background-radius:99;");
            Label noTitle = new Label("No Announcements");
            noTitle.setStyle("-fx-font-size:15px;-fx-font-weight:700;-fx-text-fill:" + UIUtils.textDark() + ";");
            Label noSub = new Label("Your instructors haven't posted anything yet. Check back later.");
            noSub.setStyle("-fx-font-size:12px;-fx-text-fill:" + UIUtils.textMid() + ";");
            noSub.setWrapText(true); noSub.setMaxWidth(340);
            empty.getChildren().addAll(ico, noTitle, noSub);
            listSection.getChildren().add(empty);
        } else {
            // Pinned / newest banner
            Announcement newest = announcements.get(0);
            String bannerColor = (newest.color != null && !newest.color.isBlank()) ? newest.color : "#2563eb";
            HBox banner = new HBox(12); banner.setAlignment(Pos.CENTER_LEFT);
            banner.setPadding(new Insets(13, 18, 13, 18));
            banner.setMaxWidth(Double.MAX_VALUE);
            banner.setStyle(
                    "-fx-background-color:" + bannerColor + "14;" +
                            "-fx-background-radius:9;" +
                            "-fx-border-color:" + bannerColor + "55;" +
                            "-fx-border-radius:9;-fx-border-width:1.5;"
            );
            StackPane bannerIco = new StackPane(UIUtils.icon(UIUtils.ICO_ANNOUNCE, bannerColor, 15));
            bannerIco.setPrefSize(34, 34);
            bannerIco.setStyle("-fx-background-color:" + bannerColor + "22;-fx-background-radius:8;");

            VBox bannerText = new VBox(3);
            Label latestTag = new Label("LATEST");
            latestTag.setStyle("-fx-font-size:9px;-fx-font-weight:700;-fx-text-fill:" + bannerColor +
                    ";-fx-letter-spacing:1.5px;");
            Label bannerTitle = new Label(newest.title);
            bannerTitle.setStyle("-fx-font-size:13.5px;-fx-font-weight:700;-fx-text-fill:" + UIUtils.textDark() + ";");
            Label bannerBody = new Label(newest.body);
            bannerBody.setStyle("-fx-font-size:12px;-fx-text-fill:" + UIUtils.textMid() + ";");
            bannerBody.setWrapText(true);
            bannerText.getChildren().addAll(latestTag, bannerTitle, bannerBody);
            HBox.setHgrow(bannerText, Priority.ALWAYS);

            Label bannerDate = new Label(newest.dateStr());
            bannerDate.setStyle("-fx-font-size:10.5px;-fx-text-fill:" + UIUtils.textSubtle() + ";");

            banner.getChildren().addAll(bannerIco, bannerText, bannerDate);
            bannerBox.getChildren().add(banner);
            bannerBox.setPadding(new Insets(0, 38, 14, 38));

            // All announcements as cards
            for (Announcement a : announcements) {
                listSection.getChildren().add(buildStudentAnnouncementCard(a, student, area, stage, app));
            }
        }

        page.getChildren().addAll(headerSection, UIUtils.divider(), bannerBox, listSection);
        wrapInScroll(area, page);
    }

    private static VBox buildStudentAnnouncementCard(Announcement a, Student student,
                                                     javafx.scene.layout.AnchorPane area,
                                                     Stage stage, HelloApplication app) {
        String color = (a.color != null && !a.color.isBlank()) ? a.color : "#2563eb";

        HBox row = new HBox(0);
        row.setAlignment(Pos.TOP_LEFT);
        row.setMaxWidth(Double.MAX_VALUE);
        row.setStyle(
                "-fx-background-color:" + UIUtils.bgCard() + ";" +
                        "-fx-background-radius:9;" +
                        "-fx-border-color:" + UIUtils.border() + ";" +
                        "-fx-border-radius:9;-fx-border-width:1;"
        );
        javafx.scene.effect.DropShadow ds = new javafx.scene.effect.DropShadow();
        ds.setColor(Color.color(0,0,0, UIUtils.darkMode ? 0.16 : 0.04));
        ds.setRadius(6); ds.setOffsetY(1); row.setEffect(ds);

        // Left accent bar
        Region bar = new Region();
        bar.setPrefWidth(4); bar.setMinWidth(4);
        bar.setStyle("-fx-background-color:" + color + ";-fx-background-radius:8 0 0 8;");

        VBox body = new VBox(8); body.setPadding(new Insets(14, 18, 14, 16));
        HBox.setHgrow(body, Priority.ALWAYS);

        // Top row: icon + title + date + expiry
        HBox topRow = new HBox(10); topRow.setAlignment(Pos.CENTER_LEFT);

        StackPane icoBox = new StackPane(UIUtils.icon(UIUtils.ICO_ANNOUNCE, color, 13));
        icoBox.setPrefSize(28, 28);
        icoBox.setStyle("-fx-background-color:" + color + "18;-fx-background-radius:6;");

        Label titleLbl = new Label(a.title);
        titleLbl.setStyle("-fx-font-size:14px;-fx-font-weight:700;-fx-text-fill:" + UIUtils.textDark() + ";");

        Label dateLbl = new Label(a.dateStr());
        dateLbl.setStyle("-fx-font-size:10.5px;-fx-text-fill:" + UIUtils.textSubtle() + ";" +
                "-fx-background-color:" + UIUtils.bgMuted() + ";-fx-padding:2 8;-fx-background-radius:4;");

        String expStr = a.expireStr();
        if (!expStr.equals("Never")) {
            Label expLbl = new Label("Expires " + expStr);
            expLbl.setStyle("-fx-font-size:10px;-fx-font-weight:600;-fx-text-fill:#b45309;" +
                    "-fx-background-color:#fef3c7;-fx-padding:2 7;-fx-background-radius:4;");
            topRow.getChildren().addAll(icoBox, titleLbl, dateLbl, expLbl);
        } else {
            topRow.getChildren().addAll(icoBox, titleLbl, dateLbl);
        }

        // Message text
        Label bodyLbl = new Label(a.body);
        bodyLbl.setStyle("-fx-font-size:13px;-fx-text-fill:" + UIUtils.textMid() + ";-fx-line-spacing:2;");
        bodyLbl.setWrapText(true);

        VBox qaBox = buildAnnouncementQaSection(a, student, area, stage, app, false);
        Button qaBtn = UIUtils.ghostBtn("", "Q&A", UIUtils.ACCENT_PURP);
        qaBtn.setOnAction(e -> {
            boolean show = !qaBox.isVisible();
            qaBox.setVisible(show);
            qaBox.setManaged(show);
            qaBtn.setText(show ? "Hide Q&A" : "Q&A");
        });
        body.getChildren().addAll(topRow, bodyLbl, qaBtn, qaBox);
        row.getChildren().addAll(bar, body);

        VBox wrapper = new VBox(row);
        wrapper.setMaxWidth(Double.MAX_VALUE);
        return wrapper;
    }

    private static void renderInboxPage(javafx.scene.layout.AnchorPane area,
                                        Stage stage, Student student, HelloApplication app) {
        area.getChildren().clear();
        BorderPane page = new BorderPane();
        page.setStyle("-fx-background-color:" + UIUtils.bgContent() + ";");

        VBox header = new VBox(4);
        header.setPadding(new Insets(28, 38, 18, 38));
        HBox titleRow = new HBox(14);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        titleRow.getChildren().addAll(backBtn(area, stage, student, app), UIUtils.heading("Messages"));
        header.getChildren().addAll(titleRow, UIUtils.subheading("Search instructors and chat in a single messaging window"));

        TextField searchField = UIUtils.styledField("Search teacher by name or email...");
        searchField.setPrefHeight(38);

        List<Teacher> teachers = ServerClient.get().loadAllTeachers();
        List<DirectMessage> previews = ServerClient.get().loadConversationPreviewsForStudent(student.getID());
        String preferredTeacher = previews.isEmpty() ? "" : previews.get(0).teacherEmail;

        VBox contactList = new VBox(8);
        contactList.setPadding(new Insets(16));
        contactList.setPrefWidth(300);
        contactList.setStyle("-fx-background-color:" + UIUtils.bgCard() + ";-fx-border-color:" + UIUtils.border() + ";-fx-border-width:0 1 0 0;");

        VBox conversationHost = new VBox();
        conversationHost.setPadding(new Insets(18));
        conversationHost.setStyle("-fx-background-color:" + UIUtils.bgContent() + ";");

        final String[] selectedTeacher = {preferredTeacher};
        Runnable[] refreshContacts = {null};
        refreshContacts[0] = () -> rebuildStudentContacts(contactList, teachers, previews, searchField.getText(), selectedTeacher[0], picked -> {
            selectedTeacher[0] = picked;
            refreshContacts[0].run();
            renderStudentConversation(conversationHost, area, stage, student, app, picked);
            ServerClient.get().markConversationReadForStudent(picked, student.getID());
        });

        searchField.textProperty().addListener((obs, ov, nv) -> refreshContacts[0].run());
        refreshContacts[0].run();
        if (!selectedTeacher[0].isBlank()) {
            ServerClient.get().markConversationReadForStudent(selectedTeacher[0], student.getID());
            renderStudentConversation(conversationHost, area, stage, student, app, selectedTeacher[0]);
        } else {
            conversationHost.getChildren().add(emptyState(UIUtils.ICO_SEND, "No Teachers Found", "Once a teacher is available, you can start chatting here."));
        }

        VBox leftPane = new VBox(12, searchField, contactList);
        leftPane.setPadding(new Insets(0, 0, 0, 38));
        leftPane.setPrefWidth(338);

        HBox body = new HBox(0, leftPane, conversationHost);
        HBox.setHgrow(conversationHost, Priority.ALWAYS);
        page.setTop(header);
        page.setCenter(body);

        ScrollPane sp = new ScrollPane(page);
        sp.setFitToWidth(true);
        sp.setFitToHeight(true);
        sp.setStyle("-fx-background:transparent;-fx-background-color:transparent;");
        area.getChildren().add(sp);
        javafx.scene.layout.AnchorPane.setTopAnchor(sp, 0.0);
        javafx.scene.layout.AnchorPane.setBottomAnchor(sp, 0.0);
        javafx.scene.layout.AnchorPane.setLeftAnchor(sp, 0.0);
        javafx.scene.layout.AnchorPane.setRightAnchor(sp, 0.0);
        UIUtils.slideIn(page, true);
    }

    private static VBox buildAnnouncementQaSection(Announcement a, Student student,
                                                   javafx.scene.layout.AnchorPane area,
                                                   Stage stage, HelloApplication app,
                                                   boolean teacherView) {
        VBox qaWrap = new VBox(10);
        qaWrap.setPadding(new Insets(12, 0, 0, 0));
        qaWrap.setVisible(false);
        qaWrap.setManaged(false);

        HBox hdr = new HBox(8);
        hdr.setAlignment(Pos.CENTER_LEFT);
        hdr.getChildren().addAll(UIUtils.icon(UIUtils.ICO_INFO, UIUtils.ACCENT_PURP, 11),
                UIUtils.sectionLabel("Questions & Answers"));
        qaWrap.getChildren().add(hdr);

        List<AnnouncementQuestion> questions = ServerClient.get().loadAnnouncementQuestions(a.id);
        if (questions.isEmpty()) {
            Label none = new Label("No questions yet.");
            none.setStyle("-fx-font-size:12px;-fx-text-fill:" + UIUtils.textSubtle() + ";");
            qaWrap.getChildren().add(none);
        } else {
            for (AnnouncementQuestion q : questions) {
                qaWrap.getChildren().add(buildAnnouncementQuestionCardProfessional(q));
            }
        }

        if (!teacherView) {
            TextArea askArea = UIUtils.styledTextArea("Ask a question about this announcement...", 76);
            Button askBtn = UIUtils.primaryBtn("", "Post Question", UIUtils.ACCENT_PURP);
            askBtn.setOnAction(e -> {
                String txt = askArea.getText().trim();
                if (txt.isEmpty()) {
                    UIUtils.Toast.error(area, "Question cannot be empty");
                    return;
                }
                AnnouncementQuestion q = new AnnouncementQuestion();
                q.announcementId = a.id;
                q.studentId = student.getID();
                q.studentName = student.getName();
                q.question = txt;
                q.createdAt = System.currentTimeMillis();
                ServerClient.get().saveAnnouncementQuestion(q);
                stage.setScene(createDashboardScene(stage, student, app, 5));
                UIUtils.Toast.success(area, "Question posted");
            });
            qaWrap.getChildren().addAll(askArea, askBtn);
        }
        return qaWrap;
    }

    private static VBox buildAnnouncementQuestionCard(AnnouncementQuestion q) {
        VBox box = new VBox(6);
        box.setPadding(new Insets(12));
        box.setStyle("-fx-background-color:" + UIUtils.bgMuted() + ";-fx-background-radius:8;-fx-border-color:" + UIUtils.border() + ";-fx-border-radius:8;");

        Label askMeta = new Label(q.studentName + " • " + q.createdStr());
        askMeta.setStyle("-fx-font-size:10.5px;-fx-text-fill:" + UIUtils.textSubtle() + ";");
        Label question = new Label(q.question);
        question.setWrapText(true);
        question.setStyle("-fx-font-size:12.5px;-fx-font-weight:600;-fx-text-fill:" + UIUtils.textDark() + ";");
        box.getChildren().addAll(askMeta, question);

        if (q.isAnswered()) {
            Label ansMeta = new Label("Teacher reply • " + q.answeredStr());
            ansMeta.setStyle("-fx-font-size:10.5px;-fx-text-fill:" + UIUtils.ACCENT_GREEN + ";");
            Label answer = new Label(q.teacherAnswer);
            answer.setWrapText(true);
            answer.setStyle("-fx-font-size:12.5px;-fx-text-fill:" + UIUtils.textMid() + ";");
            box.getChildren().addAll(ansMeta, answer);
        } else {
            Label pending = UIUtils.badge("Awaiting reply", UIUtils.ACCENT_ORG);
            box.getChildren().add(pending);
        }
        return box;
    }

    /* private static VBox buildReplyBubble(MessageReply r) {
        VBox box = new VBox(4);
        box.setPadding(new Insets(10, 12, 10, 12));
        String accent = "teacher".equalsIgnoreCase(r.senderRole) ? UIUtils.ACCENT_BLUE : UIUtils.ACCENT_GREEN;
        box.setStyle("-fx-background-color:" + accent + "12;-fx-background-radius:8;-fx-border-color:" + accent + "35;-fx-border-radius:8;");
        Label meta = new Label(r.senderName + " • " + r.dateTimeStr());
        meta.setStyle("-fx-font-size:10.5px;-fx-text-fill:" + accent + ";");
        Label body = new Label(r.body);
        body.setWrapText(true);
        body.setStyle("-fx-font-size:12.5px;-fx-text-fill:" + UIUtils.textDark() + ";");
        box.getChildren().addAll(meta, body);
        return box;
    }

    */
    private static VBox buildAnnouncementQuestionCardProfessional(AnnouncementQuestion q) {
        VBox box = new VBox(6);
        box.setPadding(new Insets(12));
        box.setStyle("-fx-background-color:" + UIUtils.bgMuted() + ";-fx-background-radius:8;-fx-border-color:" + UIUtils.border() + ";-fx-border-radius:8;");

        Label askMeta = new Label(q.studentName + " • " + q.createdStr());
        askMeta.setStyle("-fx-font-size:10.5px;-fx-text-fill:" + UIUtils.textSubtle() + ";");
        Label question = new Label(q.question);
        question.setWrapText(true);
        question.setStyle("-fx-font-size:12.5px;-fx-font-weight:600;-fx-text-fill:" + UIUtils.textDark() + ";");
        box.getChildren().addAll(askMeta, question);

        if (q.isAnswered()) {
            String teacherLabel = q.answerTeacherName != null && !q.answerTeacherName.isBlank() ? q.answerTeacherName : "Teacher";
            Label ansMeta = new Label(teacherLabel + " replied • " + q.answeredStr());
            ansMeta.setStyle("-fx-font-size:10.5px;-fx-text-fill:" + UIUtils.ACCENT_GREEN + ";");
            Label answer = new Label(q.teacherAnswer);
            answer.setWrapText(true);
            answer.setStyle("-fx-font-size:12.5px;-fx-text-fill:" + UIUtils.textMid() + ";");
            box.getChildren().addAll(ansMeta, answer);
        } else {
            box.getChildren().add(UIUtils.badge("Awaiting reply", UIUtils.ACCENT_ORG));
        }
        return box;
    }

    private static void rebuildStudentContacts(VBox host, List<Teacher> teachers, List<DirectMessage> previews,
                                               String search, String selectedTeacherEmail,
                                               java.util.function.Consumer<String> onPick) {
        host.getChildren().clear();
        String q = search == null ? "" : search.toLowerCase().trim();
        Map<String, DirectMessage> previewMap = previews.stream().collect(java.util.stream.Collectors.toMap(p -> p.teacherEmail, p -> p, (a, b) -> a, LinkedHashMap::new));
        boolean searching = !q.isBlank();

        List<Teacher> filtered = teachers.stream()
                .filter(t -> searching
                        ? t.getUser().toLowerCase().contains(q) || t.getEmail().toLowerCase().contains(q)
                        : previewMap.containsKey(t.getEmail()) || t.getEmail().equals(selectedTeacherEmail))
                .collect(Collectors.toList());

        for (Teacher t : filtered) {
            DirectMessage preview = previewMap.get(t.getEmail());
            VBox row = UIUtils.card(260);
            row.setMaxWidth(Double.MAX_VALUE);
            row.setPadding(new Insets(12));
            row.setSpacing(6);
            if (t.getEmail().equals(selectedTeacherEmail)) {
                row.setStyle("-fx-background-color:" + UIUtils.ACCENT_GREEN + "10;-fx-background-radius:8;-fx-border-color:" + UIUtils.ACCENT_GREEN + "55;-fx-border-radius:8;-fx-border-width:1;");
            }
            Label name = new Label(t.getUser());
            name.setStyle("-fx-font-size:13px;-fx-font-weight:700;-fx-text-fill:" + UIUtils.textDark() + ";");
            Label meta = new Label(t.getEmail());
            meta.setStyle("-fx-font-size:10.5px;-fx-text-fill:" + UIUtils.textSubtle() + ";");
            Label snippet = new Label(preview == null ? "No messages yet" : preview.body);
            snippet.setWrapText(true);
            snippet.setMaxWidth(220);
            snippet.setStyle("-fx-font-size:11.5px;-fx-text-fill:" + UIUtils.textMid() + ";");
            row.getChildren().addAll(name, meta, snippet);
            row.setOnMouseClicked(e -> onPick.accept(t.getEmail()));
            host.getChildren().add(row);
        }

        if (filtered.isEmpty()) {
            Label none = new Label("No teachers match your search.");
            none.setStyle("-fx-font-size:12px;-fx-text-fill:" + UIUtils.textSubtle() + ";");
            host.getChildren().add(none);
        }
    }

    private static void renderStudentConversation(VBox host, javafx.scene.layout.AnchorPane area,
                                                  Stage stage, Student student, HelloApplication app, String teacherEmail) {
        host.getChildren().clear();
        Teacher teacher = ServerClient.get().loadAllTeachers().stream()
                .filter(t -> t.getEmail().equals(teacherEmail))
                .findFirst().orElse(null);
        if (teacher == null) {
            host.getChildren().add(emptyState(UIUtils.ICO_SEND, "Conversation Unavailable", "Teacher record could not be found."));
            return;
        }

        List<DirectMessage> messages = ServerClient.get().loadConversation(teacherEmail, student.getID());
        VBox frame = new VBox(14);
        frame.setMaxWidth(Double.MAX_VALUE);

        HBox top = new HBox(10);
        top.setAlignment(Pos.CENTER_LEFT);
        StackPane icon = new StackPane(UIUtils.icon(UIUtils.ICO_SEND, UIUtils.ACCENT_GREEN, 14));
        icon.setPrefSize(34, 34);
        icon.setStyle("-fx-background-color:" + UIUtils.ACCENT_GREEN + "18;-fx-background-radius:9;");
        VBox meta = new VBox(3);
        Label name = new Label(teacher.getUser());
        name.setStyle("-fx-font-size:15px;-fx-font-weight:700;-fx-text-fill:" + UIUtils.textDark() + ";");
        Label sub = new Label(teacher.getEmail());
        sub.setStyle("-fx-font-size:11px;-fx-text-fill:" + UIUtils.textSubtle() + ";");
        meta.getChildren().addAll(name, sub);
        top.getChildren().addAll(icon, meta);

        VBox stream = new VBox(8);
        stream.setPadding(new Insets(16));
        stream.setStyle("-fx-background-color:" + UIUtils.bgCard() + ";-fx-background-radius:10;-fx-border-color:" + UIUtils.border() + ";-fx-border-radius:10;");
        if (messages.isEmpty()) {
            Label none = new Label("Start the conversation with your teacher.");
            none.setStyle("-fx-font-size:12px;-fx-text-fill:" + UIUtils.textSubtle() + ";");
            stream.getChildren().add(none);
        } else {
            for (DirectMessage m : messages) stream.getChildren().add(buildDirectMessageBubble(m, () -> {
                ServerClient.get().deleteDirectMessage(m.id);
                renderInboxPage(area, stage, student, app);
            }));
        }

        ScrollPane streamScroll = new ScrollPane(stream);
        streamScroll.setFitToWidth(true);
        streamScroll.setStyle("-fx-background:transparent;-fx-background-color:transparent;");
        streamScroll.setPrefHeight(380);

        TextArea composer = UIUtils.styledTextArea("Type your message here...", 90);
        Button sendBtn = UIUtils.primaryBtn("", "Send", UIUtils.ACCENT_GREEN);
        sendBtn.setOnAction(e -> {
            String txt = composer.getText().trim();
            if (txt.isEmpty()) return;
            DirectMessage m = new DirectMessage();
            m.teacherEmail = teacher.getEmail();
            m.teacherName = teacher.getUser();
            m.studentId = student.getID();
            m.studentName = student.getName();
            m.senderRole = "student";
            m.senderName = student.getName();
            m.body = txt;
            m.createdAt = System.currentTimeMillis();
            ServerClient.get().saveDirectMessage(m);
            stage.setScene(createDashboardScene(stage, student, app, 4));
            UIUtils.Toast.success(host, "Message sent");
        });

        frame.getChildren().addAll(top, streamScroll, composer, sendBtn);
        host.getChildren().add(frame);
    }

    private static HBox buildDirectMessageBubble(DirectMessage m, Runnable onDelete) {
        boolean mine = "student".equalsIgnoreCase(m.senderRole);
        HBox wrap = new HBox();
        wrap.setAlignment(mine ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        VBox bubble = new VBox(5);
        bubble.setMaxWidth(380);
        bubble.setPadding(new Insets(10, 12, 10, 12));
        String accent = mine ? UIUtils.ACCENT_GREEN : UIUtils.ACCENT_BLUE;
        bubble.setStyle("-fx-background-color:" + accent + (mine ? "18" : "12") + ";-fx-background-radius:10;-fx-border-color:" + accent + "38;-fx-border-radius:10;");
        Label meta = new Label(m.senderName + " • " + m.dateTimeStr());
        meta.setStyle("-fx-font-size:10.5px;-fx-text-fill:" + accent + ";");
        Label body = new Label(m.body);
        body.setWrapText(true);
        body.setStyle("-fx-font-size:12.5px;-fx-text-fill:" + UIUtils.textDark() + ";");
        bubble.getChildren().addAll(meta, body);
        if (mine) {
            Button del = UIUtils.ghostBtn("", "Delete", UIUtils.ACCENT_RED);
            del.setPrefWidth(86);
            del.setOnAction(e -> onDelete.run());
            bubble.getChildren().add(del);
        }
        wrap.getChildren().add(bubble);
        return wrap;
    }

    private static String chooseTeacherContact(Stage owner, List<Teacher> teachers, String currentEmail) {
        if (teachers.isEmpty()) return null;
        String initial = currentEmail != null && !currentEmail.isBlank() ? currentEmail + " — " +
                teachers.stream().filter(t -> t.getEmail().equals(currentEmail)).findFirst().map(Teacher::getUser).orElse(currentEmail)
                : teachers.get(0).getEmail() + " — " + teachers.get(0).getUser();
        ChoiceDialog<String> dialog = new ChoiceDialog<>(initial,
                teachers.stream().map(t -> t.getEmail() + " — " + t.getUser()).toList());
        dialog.initOwner(owner);
        dialog.setTitle("New Chat");
        dialog.setHeaderText("Start a conversation with a teacher");
        dialog.setContentText("Teacher:");
        return dialog.showAndWait().map(v -> v.split(" — ")[0]).orElse(null);
    }

    private static void wrapInScroll(javafx.scene.layout.AnchorPane area, VBox page) {
        ScrollPane sp = new ScrollPane(page);
        sp.setFitToWidth(true); sp.setStyle("-fx-background:transparent;-fx-background-color:transparent;");
        javafx.scene.layout.AnchorPane.setTopAnchor(sp,0.0); javafx.scene.layout.AnchorPane.setBottomAnchor(sp,0.0);
        javafx.scene.layout.AnchorPane.setLeftAnchor(sp,0.0); javafx.scene.layout.AnchorPane.setRightAnchor(sp,0.0);
        area.getChildren().add(sp);
        UIUtils.slideIn(page, true);
    }

    private static VBox emptyState(String svgIcon, String title, String sub) {
        VBox c = UIUtils.card(480); c.setMaxWidth(480); c.setAlignment(Pos.CENTER);
        c.setPadding(new Insets(40));
        StackPane ico = new StackPane(UIUtils.icon(svgIcon, UIUtils.textSubtle(), 24));
        ico.setPrefSize(52, 52);
        ico.setStyle("-fx-background-color:" + UIUtils.bgMuted() + ";-fx-background-radius:10;");
        Label t = new Label(title); t.setStyle("-fx-font-size:15px;-fx-font-weight:700;-fx-text-fill:"+UIUtils.textDark()+";");
        Label s = new Label(sub); s.setStyle("-fx-font-size:12.5px;-fx-text-fill:"+UIUtils.textMid()+";"); s.setWrapText(true);
        c.getChildren().addAll(ico, t, s);
        return c;
    }

    private static String qNavStyle(String borderCol, boolean dark) {
        String bg = dark ? "#1c2333" : "#f0f1f4";
        String txt = dark ? "#6b7b96" : "#7a8699";
        return "-fx-background-color:" + bg + ";-fx-text-fill:" + txt + ";-fx-font-size:10.5px;-fx-font-weight:500;-fx-background-radius:5;-fx-cursor:hand;-fx-border-color:transparent;";
    }
    private static String qNavStyleActive(String color, boolean dark) {
        return "-fx-background-color:" + color + ";-fx-text-fill:white;-fx-font-size:10.5px;-fx-font-weight:700;-fx-background-radius:5;-fx-cursor:hand;";
    }
    private static String flagBtnStyle(boolean active, boolean dark) {
        if (active) return "-fx-background-color:#fef3c7;-fx-text-fill:#b45309;-fx-font-weight:700;-fx-font-size:11.5px;-fx-background-radius:5;-fx-padding:4 11;-fx-cursor:hand;-fx-border-color:#f59e0b;-fx-border-width:1;-fx-border-radius:5;";
        String base = dark ? "#222a3c" : "#f0f1f4";
        String txt  = dark ? "#6b7b96" : "#7a8699";
        return "-fx-background-color:" + base + ";-fx-text-fill:" + txt + ";-fx-font-size:11.5px;-fx-background-radius:5;-fx-padding:4 11;-fx-cursor:hand;-fx-border-color:transparent;";
    }
    private static String formatTime(int s) {
        int h=s/3600, m=(s%3600)/60, sec=s%60;
        return h>0 ? String.format("%02d:%02d:%02d",h,m,sec) : String.format("%02d:%02d",m,sec);
    }
}
