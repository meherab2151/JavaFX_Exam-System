package org.example.demo;

import javafx.application.Application;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.stage.Stage;
import java.util.ArrayList;

// ═══════════════════════════════════════════════════════════
//  HelloApplication.java
//  UPDATED: also loads exams from DB on startup.
// ═══════════════════════════════════════════════════════════
public class HelloApplication extends Application {

    private static final ArrayList<Teacher> teachers = new ArrayList<>();
    private static final ArrayList<Student> students = new ArrayList<>();

    @Override
    public void start(Stage stage) {

        // 1. Init DB (creates file + all tables if needed)
        DatabaseManager.init();

        // 2. Load persisted users
        teachers.addAll(UserDAO.loadAllTeachers());
        students.addAll(UserDAO.loadAllStudents());

        // 3. Load persisted questions
        QuestionBank.allQuestions.addAll(QuestionDAO.loadAll());
        System.out.println("[App] Loaded " + QuestionBank.allQuestions.size() + " questions from DB.");

        // 4. Load persisted exams, then clean up any that were still marked live
        //    when the app was closed (their liveEndMillis is now in the past).
        //    Without this, the dashboard ticker fires immediately and "ends" them
        //    in front of the teacher the moment they log in.
        ExamBank.allExams.addAll(ExamDAO.loadAll());
        long now = System.currentTimeMillis();
        for (Exam e : ExamBank.allExams) {
            if (e.isLive() && e.getLiveEndMillis() > 0 && now >= e.getLiveEndMillis()) {
                e.setLive(false);
                if (e.getScheduleDetails() == null || !e.getScheduleDetails().startsWith("Ended"))
                    e.setScheduleDetails("Ended: " + java.time.LocalDateTime.now()
                            .format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")));
                ExamDAO.save(e);
                System.out.println("[App] Cleaned up expired live exam id=" + e.getDbId());
            }
        }

        // 5. Close DB cleanly on exit
        stage.setOnCloseRequest(e -> DatabaseManager.close());

        stage.setTitle("EduExam – Online Assessment System");
        stage.setScene(createMainScene(stage));
        stage.setResizable(false);
        stage.show();
    }

    public Scene createMainScene(Stage stage) {
        BorderPane root = new BorderPane();

        VBox left = new VBox(24);
        left.setPrefWidth(430);
        left.setAlignment(Pos.CENTER);
        left.setPadding(new Insets(60));
        left.setStyle("-fx-background-color:" + UIUtils.BG_DARK + ";");

        Label logo  = new Label("🏫");
        logo.setStyle("-fx-font-size:72px;-fx-text-fill:white;");
        Label brand = new Label("EduExam");
        brand.setStyle("-fx-font-size:42px;-fx-font-weight:bold;-fx-text-fill:white;");
        Label tag   = new Label("The Smart Online Assessment Platform");
        tag.setStyle("-fx-font-size:15px;-fx-text-fill:#64748b;-fx-text-alignment:center;");
        tag.setWrapText(true); tag.setMaxWidth(300); tag.setAlignment(Pos.CENTER);

        HBox dots = new HBox(10);
        dots.setAlignment(Pos.CENTER);
        for (String c : new String[]{UIUtils.ACCENT_BLUE, UIUtils.ACCENT_GREEN, UIUtils.ACCENT_PURP}) {
            Circle dot = new Circle(6, Color.web(c));
            dots.getChildren().add(dot);
        }
        left.getChildren().addAll(logo, brand, tag, dots);

        VBox right = new VBox(30);
        right.setAlignment(Pos.CENTER);
        right.setPadding(new Insets(60, 70, 60, 70));
        right.setStyle("-fx-background-color:" + UIUtils.BG_LIGHT + ";");

        Label selTitle = new Label("Choose Your Portal");
        selTitle.setStyle("-fx-font-size:26px;-fx-font-weight:bold;-fx-text-fill:"+UIUtils.TEXT_DARK+";");
        Label selSub = UIUtils.subheading("Select your role to continue");

        VBox studentCard = buildPortalCard(
                "🎓", "Student",
                "Join live exams and track your scores",
                UIUtils.ACCENT_GREEN, "#052e16",
                () -> stage.setScene(StudentPortal.createLoginScene(stage, students, this))
        );
        VBox teacherCard = buildPortalCard(
                "📚", "Teacher",
                "Create exams, manage questions and results",
                UIUtils.ACCENT_BLUE, UIUtils.BG_DARK,
                () -> stage.setScene(TeacherPortal.createLoginScene(stage, teachers, this))
        );

        right.getChildren().addAll(selTitle, selSub, studentCard, teacherCard);
        root.setLeft(left);
        root.setCenter(right);

        Scene scene = new Scene(root, 1000, 600);
        UIUtils.applyStyle(scene);
        UIUtils.slideIn(right, true);
        return scene;
    }

    private VBox buildPortalCard(String icon, String title, String desc,
                                 String accent, String darkBg, Runnable action) {
        VBox card = new VBox(10);
        card.setPrefWidth(320);
        card.setPadding(new Insets(22));
        card.setStyle("-fx-background-color:white;-fx-background-radius:16;"
                + "-fx-border-color:" + UIUtils.BORDER + ";-fx-border-radius:16;-fx-cursor:hand;");
        javafx.scene.effect.DropShadow ds = new javafx.scene.effect.DropShadow();
        ds.setColor(Color.color(0,0,0,0.07)); ds.setRadius(14); ds.setOffsetY(4);
        card.setEffect(ds);

        HBox top = new HBox(14); top.setAlignment(Pos.CENTER_LEFT);
        Circle iconBg = new Circle(24, Color.web(accent + "22"));
        Label  iconL  = new Label(icon); iconL.setStyle("-fx-font-size:22px;");
        StackPane iconStack = new StackPane(iconBg, iconL);

        VBox text = new VBox(3);
        Label t = new Label(title);
        t.setStyle("-fx-font-size:18px;-fx-font-weight:bold;-fx-text-fill:"+UIUtils.TEXT_DARK+";");
        Label d = new Label(desc);
        d.setStyle("-fx-font-size:13px;-fx-text-fill:"+UIUtils.TEXT_MID+";");
        d.setWrapText(true);
        text.getChildren().addAll(t, d);
        top.getChildren().addAll(iconStack, text);

        Button btn = UIUtils.primaryBtn("→", "Enter " + title + " Portal", accent);
        btn.setPrefWidth(Double.MAX_VALUE);
        btn.setOnAction(e -> action.run());
        card.getChildren().addAll(top, btn);

        card.setOnMouseEntered(e -> {
            ds.setRadius(22); ds.setOffsetY(8); ds.setColor(Color.web(accent, 0.18));
            card.setTranslateY(-3);
        });
        card.setOnMouseExited(e -> {
            ds.setRadius(14); ds.setOffsetY(4); ds.setColor(Color.color(0,0,0,0.07));
            card.setTranslateY(0);
        });
        return card;
    }

    public void showError(String title, String message) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(message);
        a.showAndWait();
    }

    public void showInfo(String title, String message) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(message);
        a.showAndWait();
    }
}