package org.example.demo;

import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.stage.*;
import java.util.ArrayList;

// ═══════════════════════════════════════════════════════════
//  StudentPortal.java
//  Merges: StudentLoginManager, StudentDashboard
// ═══════════════════════════════════════════════════════════
public class StudentPortal {

    // ╔══════════════════════════════════════════════════════╗
    //  1. LOGIN
    // ╚══════════════════════════════════════════════════════╝
    public static Scene createLoginScene(Stage stage, ArrayList<Student> list, HelloApplication app) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color:" + UIUtils.BG_LIGHT + ";");

        // ── Left accent panel ────────────────────────────────
        VBox accent = new VBox(20);
        accent.setPrefWidth(340);
        accent.setAlignment(Pos.CENTER);
        accent.setPadding(new Insets(50));
        accent.setStyle("-fx-background-color:#052e16;"); // Deep green for students
        accent.getChildren().addAll(
                new Label("🎓") {{ setStyle("-fx-font-size:64px;-fx-text-fill:white;"); }},
                new Label("EduExam") {{ setStyle("-fx-font-size:32px;-fx-font-weight:bold;-fx-text-fill:white;"); }},
                new Label("Student Assessment Portal") {{ setStyle("-fx-font-size:14px;-fx-text-fill:#6ee7b7;"); }}
        );
        root.setLeft(accent);

        // ── Right form ────────────────────────────────────────
        VBox form = new VBox(18);
        form.setAlignment(Pos.CENTER);
        form.setPadding(new Insets(60, 70, 60, 70));
        form.setMaxWidth(400);

        Label title = new Label("Student Login");
        title.setStyle("-fx-font-size:28px;-fx-font-weight:bold;-fx-text-fill:" + UIUtils.TEXT_DARK + ";");
        Label sub = new Label("Enter your credentials to continue 📚");
        sub.setStyle("-fx-font-size:14px;-fx-text-fill:" + UIUtils.TEXT_MID + ";");

        TextField     txtID   = UIUtils.styledField("Student ID or Email");
        PasswordField txtPass = UIUtils.styledPassword("Password");

        Button btnLogin = UIUtils.primaryBtn("🔑", "Sign In", UIUtils.ACCENT_GREEN);
        btnLogin.setPrefWidth(Double.MAX_VALUE); btnLogin.setPrefHeight(46);

        Hyperlink linkSignup = new Hyperlink("New here? Create an account");
        UIUtils.applyLinkEffects(linkSignup);
        Button btnBack = UIUtils.ghostBtn("←", "Back", UIUtils.TEXT_MID);

        btnLogin.setOnAction(e -> {
            String in = txtID.getText(), pw = txtPass.getText();
            Student found = list.stream()
                    .filter(s -> (s.getID().equals(in) || s.getEmail().equals(in)) && s.getPassword().equals(pw))
                    .findFirst().orElse(null);
            if (found != null) stage.setScene(createDashboardScene(stage, found, app));
            else app.showError("Login Failed", "Invalid Student ID/Email or Password.");
        });
        linkSignup.setOnAction(e -> stage.setScene(createSignupScene(stage, list, app)));
        btnBack.setOnAction(e -> stage.setScene(app.createMainScene(stage)));

        String lblStyle = "-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:" + UIUtils.TEXT_MID + ";";
        form.getChildren().addAll(
                title, sub, UIUtils.divider(),
                new Label("Student ID / Email") {{ setStyle(lblStyle); }}, txtID,
                new Label("Password")           {{ setStyle(lblStyle); }}, txtPass,
                btnLogin, linkSignup, btnBack);

        ScrollPane sp = new ScrollPane(form);
        sp.setFitToWidth(true); sp.setStyle("-fx-background:transparent;-fx-background-color:transparent;");
        root.setCenter(sp);

        Scene scene = new Scene(root, 1000, 600);
        UIUtils.applyStyle(scene);
        UIUtils.slideIn(form, true);
        return scene;
    }

    // ╔══════════════════════════════════════════════════════╗
    //  2. SIGN-UP
    // ╚══════════════════════════════════════════════════════╝
    public static Scene createSignupScene(Stage stage, ArrayList<Student> list, HelloApplication app) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color:" + UIUtils.BG_LIGHT + ";");

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

        TextField     txtID      = UIUtils.styledField("Student ID (numbers only)");
        TextField     txtName    = UIUtils.styledField("Full Name");
        TextField     txtEmail   = UIUtils.styledField("Email Address");
        PasswordField txtPass    = UIUtils.styledPassword("Create Password");
        PasswordField txtConfirm = UIUtils.styledPassword("Confirm Password");

        // Restrict Student ID to digits only — block non-numeric input live
        txtID.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) txtID.setText(newVal.replaceAll("[^\\d]", ""));
        });

        Button btnReg  = UIUtils.primaryBtn("✅", "Create Account", UIUtils.ACCENT_GREEN);
        btnReg.setPrefWidth(Double.MAX_VALUE); btnReg.setPrefHeight(46);
        Button btnBack = UIUtils.ghostBtn("←", "Back to Login", UIUtils.TEXT_MID);

        btnReg.setOnAction(e -> {
            if (!txtPass.getText().equals(txtConfirm.getText())) {
                app.showError("Mismatch", "Passwords do not match!"); return;
            }
            if (txtID.getText().isEmpty() || txtName.getText().isEmpty()) {
                app.showError("Missing Info", "Please fill in all fields."); return;
            }
            if (!txtID.getText().matches("\\d+")) {
                app.showError("Invalid ID", "Student ID must contain numbers only."); return;
            }
            list.add(new Student(txtID.getText(), txtName.getText(), txtEmail.getText(), txtPass.getText()));
            app.showInfo("✅ Registered!", "Account created. You can now log in.");
            stage.setScene(createLoginScene(stage, list, app));
        });
        btnBack.setOnAction(e -> stage.setScene(createLoginScene(stage, list, app)));

        String lblStyle = "-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:" + UIUtils.TEXT_MID + ";";
        form.getChildren().addAll(
                new Label("Student Registration") {{ setStyle("-fx-font-size:26px;-fx-font-weight:bold;-fx-text-fill:"+UIUtils.TEXT_DARK+";"); }},
                UIUtils.divider(),
                new Label("Student ID")  {{ setStyle(lblStyle); }}, txtID,
                new Label("Full Name")   {{ setStyle(lblStyle); }}, txtName,
                new Label("Email")       {{ setStyle(lblStyle); }}, txtEmail,
                new Label("Password")    {{ setStyle(lblStyle); }}, txtPass,
                new Label("Confirm")     {{ setStyle(lblStyle); }}, txtConfirm,
                btnReg, btnBack);

        ScrollPane sp = new ScrollPane(form);
        sp.setFitToWidth(true); sp.setStyle("-fx-background:transparent;-fx-background-color:transparent;");
        root.setCenter(sp);

        Scene scene = new Scene(root, 1000, 600);
        UIUtils.applyStyle(scene);
        UIUtils.slideIn(form, true);
        return scene;
    }

    // ╔══════════════════════════════════════════════════════╗
    //  3. STUDENT DASHBOARD
    // ╚══════════════════════════════════════════════════════╝
    public static Scene createDashboardScene(Stage stage, Student student, HelloApplication app) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color:" + UIUtils.BG_LIGHT + ";");

        // ── Dark sidebar ─────────────────────────────────────
        VBox sidebar = new VBox(18);
        sidebar.setPrefWidth(210);
        sidebar.setPadding(new Insets(0, 10, 20, 10));
        sidebar.setStyle("-fx-background-color:#052e16;");

        VBox avatarBox = new VBox(8);
        avatarBox.setAlignment(Pos.CENTER);
        avatarBox.setPadding(new Insets(30, 10, 20, 10));
        Circle av = new Circle(36);
        av.setFill(Color.web(UIUtils.ACCENT_GREEN));
        Label initials = new Label(student.getName().substring(0,1).toUpperCase());
        initials.setStyle("-fx-font-size:22px;-fx-font-weight:bold;-fx-text-fill:white;");
        StackPane avStack = new StackPane(av, initials);
        Label nameL = new Label(student.getName());
        nameL.setStyle("-fx-font-size:14px;-fx-font-weight:bold;-fx-text-fill:white;");
        Label idL = new Label("ID: " + student.getID());
        idL.setStyle("-fx-font-size:11px;-fx-text-fill:#6ee7b7;");
        avatarBox.getChildren().addAll(avStack, nameL, idL);

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color:#064e3b;");

        // Placeholder nav items (for future expansion)
        Button btnExam    = UIUtils.sidebarBtn("🏠", "Join Exam",    UIUtils.ACCENT_GREEN);
        Button btnHistory = UIUtils.sidebarBtn("📋", "My Results",   UIUtils.ACCENT_BLUE);

        UIUtils.setSidebarBtnActive(btnExam, UIUtils.ACCENT_GREEN);

        Region spacer = new Region(); VBox.setVgrow(spacer, Priority.ALWAYS);
        Button btnLogout = UIUtils.primaryBtn("🚪", "Log Out", UIUtils.ACCENT_RED);
        btnLogout.setPrefWidth(190);
        btnLogout.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Log Out");
            confirm.setHeaderText("Are you sure you want to log out?");
            confirm.setContentText("You will be returned to the home screen.");
            confirm.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
            confirm.showAndWait().ifPresent(r -> {
                if (r == ButtonType.YES) stage.setScene(app.createMainScene(stage));
            });
        });

        sidebar.getChildren().addAll(avatarBox, sep, btnExam, btnHistory, spacer, btnLogout);

        // ── Main content ──────────────────────────────────────
        ScrollPane sp = new ScrollPane();
        sp.setPrefSize(790, 600);
        sp.setStyle("-fx-background:transparent;-fx-background-color:"+UIUtils.BG_LIGHT+";");
        sp.setFitToWidth(true);

        VBox page = new VBox(28);
        page.setPadding(new Insets(36, 36, 36, 36));

        // Welcome header
        Label welcome = new Label("Welcome back, " + student.getName() + " 👋");
        welcome.setStyle("-fx-font-size:28px;-fx-font-weight:bold;-fx-text-fill:"+UIUtils.TEXT_DARK+";");
        Label sub2 = UIUtils.subheading("Enter your exam code below to join a live exam");

        // ── Join exam card ────────────────────────────────────
        VBox joinCard = UIUtils.card(500);
        joinCard.setMaxWidth(500);
        joinCard.setPadding(new Insets(30));

        Label cardTitle = new Label("🎯  Join Live Exam");
        cardTitle.setStyle("-fx-font-size:20px;-fx-font-weight:bold;-fx-text-fill:"+UIUtils.TEXT_DARK+";");
        Label cardSub = UIUtils.subheading("Get your 6-character code from your teacher");

        TextField codeField = new TextField();
        codeField.setPromptText("e.g.  A1B2C3");
        codeField.setStyle("-fx-font-family:Monospaced;-fx-font-size:26px;-fx-alignment:center;"
                + "-fx-background-color:#f8fafc;-fx-border-color:" + UIUtils.BORDER + ";"
                + "-fx-border-radius:10;-fx-background-radius:10;-fx-padding:12;");
        codeField.setPrefHeight(64);

        Button btnJoin = UIUtils.primaryBtn("🚀", "Join Exam", UIUtils.ACCENT_GREEN);
        btnJoin.setPrefWidth(Double.MAX_VALUE); btnJoin.setPrefHeight(50);

        btnJoin.setOnAction(e -> {
            String code = codeField.getText().trim().toUpperCase();
            Exam found = ExamBank.allExams.stream()
                    .filter(ex -> ex.isLive() && ex.getExamCode().equals(code))
                    .findFirst().orElse(null);
            if (found != null) {
                app.showInfo("✅ Joining...",
                        "Entering " + found.getSubject() + " exam.\n"
                                + "Duration: " + found.getDuration() + " minutes.\n"
                                + "Total Marks: " + found.getTotalMarks());
                // Future: stage.setScene(StudentExamView.create(stage, student, found, app));
            } else {
                app.showError("❌ Not Found", "No live exam found with that code.\nDouble-check with your teacher!");
            }
        });

        joinCard.getChildren().addAll(cardTitle, cardSub, UIUtils.divider(), codeField, btnJoin);

        // ── Live exams info panel ─────────────────────────────
        VBox infoCard = UIUtils.card(500);
        infoCard.setMaxWidth(500);
        infoCard.setPadding(new Insets(24));
        Label infoTitle = new Label("📡  Currently Live Exams");
        infoTitle.setStyle("-fx-font-size:16px;-fx-font-weight:bold;-fx-text-fill:"+UIUtils.TEXT_DARK+";");
        infoCard.getChildren().add(infoTitle);

        long liveCount = ExamBank.getLiveExams().size();
        if (liveCount == 0) {
            Label noLive = UIUtils.subheading("No exams are live right now.");
            infoCard.getChildren().add(noLive);
        } else {
            for (Exam ex : ExamBank.getLiveExams()) {
                HBox row = new HBox(10); row.setAlignment(Pos.CENTER_LEFT);
                Label dot = new Label("🟢"); dot.setStyle("-fx-font-size:14px;-fx-text-fill:" + UIUtils.ACCENT_GREEN + ";");
                Label subj = new Label(ex.getSubject() + " — Grade " + ex.getGrade());
                subj.setStyle("-fx-font-size:14px;-fx-font-weight:bold;-fx-text-fill:"+UIUtils.TEXT_DARK+";");
                Label dur = UIUtils.badge(ex.getDuration() + " min", UIUtils.ACCENT_ORG);
                row.getChildren().addAll(dot, subj, dur);
                infoCard.getChildren().add(row);
            }
        }

        page.getChildren().addAll(welcome, sub2, UIUtils.divider(), joinCard, infoCard);
        sp.setContent(page);

        root.setLeft(sidebar);
        root.setCenter(sp);

        Scene scene = new Scene(root, 1000, 600);
        UIUtils.applyStyle(scene);
        UIUtils.slideIn(page, true);
        return scene;
    }
}