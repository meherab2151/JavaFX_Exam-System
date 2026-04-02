package org.example.demo;

import javafx.animation.*;
import javafx.application.Application;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) {

        try {
            ServerClient.get().connect();
        } catch (IOException e) {
            showStartupError(stage,
                "Cannot connect to EduExam Server at "
                + Protocol.HOST + ":" + Protocol.PORT + "\n\n"
                + "Make sure EduExamServer is running before launching the client.\n\n"
                + e.getMessage());
            return;
        }

        stage.setOnCloseRequest(e -> ServerClient.get().disconnect());
        stage.setTitle("EduExam — Online Assessment Platform");
        stage.setScene(createMainScene(stage));
        stage.setResizable(true);
        stage.show();
    }

    @Override
    public void stop() { ServerClient.get().disconnect(); }

    private void showStartupError(Stage stage, String message) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Server Unavailable");
        a.setHeaderText("EduExam Server Not Found");
        a.setContentText(message);
        a.showAndWait();
        stage.close();
    }

    public Scene createMainScene(Stage stage) {
        BorderPane root = new BorderPane();

        StackPane left = new StackPane();
        left.setPrefWidth(400);
        left.setStyle("-fx-background-color:#111722;");

        Pane gridLines = buildGridLines(400, 580);

        VBox brandBox = new VBox(0);
        brandBox.setAlignment(Pos.CENTER_LEFT);
        brandBox.setPadding(new Insets(0, 0, 0, 52));
        StackPane.setAlignment(brandBox, Pos.CENTER_LEFT);

        Region tealRule = new Region();
        tealRule.setPrefSize(36, 3);
        tealRule.setStyle("-fx-background-color:#0f7d74;-fx-background-radius:99;");
        VBox.setMargin(tealRule, new Insets(0, 0, 24, 0));

        HBox wordmark = new HBox(0);
        wordmark.setAlignment(Pos.CENTER_LEFT);
        Label wordLeft  = new Label("Edu");
        wordLeft.setStyle("-fx-font-size:44px;-fx-font-weight:700;-fx-text-fill:#e8eaf2;-fx-letter-spacing:-1.5px;");
        Label wordRight = new Label("Exam");
        wordRight.setStyle("-fx-font-size:44px;-fx-font-weight:300;-fx-text-fill:#0f7d74;-fx-letter-spacing:-1.5px;");
        wordmark.getChildren().addAll(wordLeft, wordRight);
        VBox.setMargin(wordmark, new Insets(0, 0, 12, 0));

        Label tagline = new Label("Online Assessment Platform");
        tagline.setStyle("-fx-font-size:13px;-fx-text-fill:#4a566e;-fx-font-weight:500;-fx-letter-spacing:1.4px;");

        brandBox.getChildren().addAll(tealRule, wordmark, tagline);
        left.getChildren().addAll(gridLines, brandBox);

        VBox right = new VBox(22);
        right.setAlignment(Pos.CENTER);
        right.setPadding(new Insets(60, 60, 60, 60));
        right.setStyle("-fx-background-color:#fafaf8;");

        VBox rightHeader = new VBox(6);
        Label selectTitle = new Label("Select Your Portal");
        selectTitle.setStyle("-fx-font-size:22px;-fx-font-weight:700;-fx-text-fill:#1c2333;-fx-letter-spacing:-0.3px;");
        Label selectSub = new Label("Identify your role to access your workspace");
        selectSub.setStyle("-fx-font-size:13px;-fx-text-fill:#6b7585;");
        rightHeader.getChildren().addAll(selectTitle, selectSub);

        VBox studentCard = buildPortalCard(
            buildStudentArt(),
            "Student",
            "Sit live examinations, review results and monitor academic progress",
            UIUtils.ACCENT_GREEN,
            () -> stage.setScene(StudentPortal.createLoginScene(stage, this))
        );

        VBox teacherCard = buildPortalCard(
            buildInstructorArt(),
            "Instructor",
            "Author examinations, manage the question bank and analyse outcomes",
            UIUtils.ACCENT_BLUE,
            () -> stage.setScene(TeacherPortal.createLoginScene(stage, this))
        );

        HBox footer = new HBox(8);
        footer.setAlignment(Pos.CENTER_LEFT);
        Region lockIco = UIUtils.icon(UIUtils.ICO_LOCK, "#9aa1b0", 11);
        Label footerLbl = new Label("Secured · Connected to EduExam Server");
        footerLbl.setStyle("-fx-font-size:11px;-fx-text-fill:#9aa1b0;");
        footer.getChildren().addAll(lockIco, footerLbl);

        right.getChildren().addAll(rightHeader, studentCard, teacherCard, footer);

        root.setLeft(left);
        root.setCenter(right);

        Scene scene = new Scene(root, 1000, 580);
        UIUtils.applyStyle(scene);

        right.setOpacity(0); right.setTranslateX(20);
        PauseTransition pause = new PauseTransition(Duration.millis(60));
        pause.setOnFinished(e -> {
            FadeTransition ft = new FadeTransition(Duration.millis(300), right); ft.setToValue(1);
            TranslateTransition tt = new TranslateTransition(Duration.millis(300), right);
            tt.setToX(0); tt.setInterpolator(Interpolator.EASE_OUT);
            new ParallelTransition(ft, tt).play();
        });
        pause.play();

        return scene;
    }

    private Pane buildGridLines(double w, double h) {
        Pane p = new Pane(); p.setPrefSize(w, h); p.setMouseTransparent(true);
        for (double x = 40; x < w; x += 80) {
            Line l = new Line(x, 0, x, h);
            l.setStroke(Color.web("#ffffff", 0.025)); l.setStrokeWidth(0.5); p.getChildren().add(l);
        }
        for (double y = 40; y < h; y += 80) {
            Line l = new Line(0, y, w, y);
            l.setStroke(Color.web("#ffffff", 0.025)); l.setStrokeWidth(0.5); p.getChildren().add(l);
        }
        Line br1h = new Line(32, 32, 64, 32); Line br1v = new Line(32, 32, 32, 64);
        br1h.setStroke(Color.web("#0f7d74", 0.55)); br1h.setStrokeWidth(1.5);
        br1v.setStroke(Color.web("#0f7d74", 0.55)); br1v.setStrokeWidth(1.5);
        Line br2h = new Line(w-32, h-32, w-64, h-32); Line br2v = new Line(w-32, h-32, w-32, h-64);
        br2h.setStroke(Color.web("#0f7d74", 0.35)); br2h.setStrokeWidth(1);
        br2v.setStroke(Color.web("#0f7d74", 0.35)); br2v.setStrokeWidth(1);
        p.getChildren().addAll(br1h, br1v, br2h, br2v);
        return p;
    }

    private Pane buildStudentArt() {
        Pane art = new Pane(); art.setPrefSize(52, 52);
        Circle head = new Circle(26, 12, 8);
        head.setFill(Color.web("#0e7a56", 0.85)); head.setStroke(Color.web("#0e7a56")); head.setStrokeWidth(1);
        Arc shoulders = new Arc(26, 27, 12, 7, 0, 180);
        shoulders.setType(javafx.scene.shape.ArcType.CHORD);
        shoulders.setFill(Color.web("#0e7a56", 0.70)); shoulders.setStroke(Color.web("#0e7a56")); shoulders.setStrokeWidth(1);
        Polygon leftPage  = new Polygon(10.0,42.0, 10.0,33.0, 26.0,35.0, 26.0,44.0);
        leftPage.setFill(Color.web("#d1f0e8",0.9)); leftPage.setStroke(Color.web("#0e7a56",0.7)); leftPage.setStrokeWidth(1);
        Polygon rightPage = new Polygon(26.0,44.0, 26.0,35.0, 42.0,33.0, 42.0,42.0);
        rightPage.setFill(Color.web("#b8e8d8",0.9)); rightPage.setStroke(Color.web("#0e7a56",0.7)); rightPage.setStrokeWidth(1);
        Line spine = new Line(26,35,26,44); spine.setStroke(Color.web("#0e7a56")); spine.setStrokeWidth(1.5);
        for (int i=0;i<3;i++) { Line l=new Line(13,36+i*2.5,23,36.5+i*2.5); l.setStroke(Color.web("#0e7a56",0.5)); l.setStrokeWidth(0.8); art.getChildren().add(l); }
        for (int i=0;i<3;i++) { Line l=new Line(29,36+i*2.5,39,36.5+i*2.5); l.setStroke(Color.web("#0e7a56",0.5)); l.setStrokeWidth(0.8); art.getChildren().add(l); }
        Ellipse capBoard = new Ellipse(26,5,10,3); capBoard.setFill(Color.web("#0e7a56"));
        Rectangle capBase = new Rectangle(21,5,10,4); capBase.setFill(Color.web("#0e7a56",0.8));
        Line tassel = new Line(36,5,38,10); tassel.setStroke(Color.web("#0f7d74")); tassel.setStrokeWidth(1.5);
        Circle tasselEnd = new Circle(38,11,2,Color.web("#0f7d74"));
        art.getChildren().addAll(leftPage,rightPage,spine,shoulders,head,capBase,capBoard,tassel,tasselEnd);
        return art;
    }

    private Pane buildInstructorArt() {
        Pane art = new Pane(); art.setPrefSize(52, 52);
        Rectangle board = new Rectangle(2,4,36,26);
        board.setFill(Color.web("#0f7d74",0.12)); board.setStroke(Color.web("#0f7d74",0.6)); board.setStrokeWidth(1.5);
        board.setArcWidth(3); board.setArcHeight(3);
        Line legLeft=new Line(8,30,6,36); Line legRight=new Line(30,30,32,36);
        legLeft.setStroke(Color.web("#0f7d74",0.5)); legLeft.setStrokeWidth(1.5);
        legRight.setStroke(Color.web("#0f7d74",0.5)); legRight.setStrokeWidth(1.5);
        Line writeLine1=new Line(7,12,22,12); writeLine1.setStroke(Color.web("#0f7d74",0.7)); writeLine1.setStrokeWidth(1.5);
        Line eqLeft=new Line(7,17,12,17); Line eqRight=new Line(7,19.5,12,19.5);
        eqLeft.setStroke(Color.web("#0f7d74",0.5)); eqLeft.setStrokeWidth(1);
        eqRight.setStroke(Color.web("#0f7d74",0.5)); eqRight.setStrokeWidth(1);
        Line eq1=new Line(14,16.5,18,16.5); Line eq2=new Line(14,19.0,18,19.0);
        eq1.setStroke(Color.web("#0f7d74",0.5)); eq1.setStrokeWidth(1);
        eq2.setStroke(Color.web("#0f7d74",0.5)); eq2.setStrokeWidth(1);
        Line result=new Line(20,17,28,17); result.setStroke(Color.web("#0f7d74",0.6)); result.setStrokeWidth(1.2);
        Circle head=new Circle(43,14,6);
        head.setFill(Color.web("#0f7d74",0.85)); head.setStroke(Color.web("#0f7d74")); head.setStrokeWidth(1);
        Rectangle body=new Rectangle(38,21,10,14);
        body.setFill(Color.web("#0f7d74",0.65)); body.setArcWidth(3); body.setArcHeight(3);
        Line arm=new Line(38,24,32,20); arm.setStroke(Color.web("#0f7d74",0.8)); arm.setStrokeWidth(2);
        Line pointer=new Line(32,20,26,16); pointer.setStroke(Color.web("#0f7d74")); pointer.setStrokeWidth(1.5);
        Circle pointerTip=new Circle(26,16,1.5,Color.web("#0f7d74"));
        Line legL=new Line(40,35,38,46); Line legR=new Line(44,35,46,46);
        legL.setStroke(Color.web("#0f7d74",0.7)); legL.setStrokeWidth(2);
        legR.setStroke(Color.web("#0f7d74",0.7)); legR.setStrokeWidth(2);
        Line shoeL=new Line(38,46,35,47); Line shoeR=new Line(46,46,49,47);
        shoeL.setStroke(Color.web("#0f7d74")); shoeL.setStrokeWidth(2);
        shoeR.setStroke(Color.web("#0f7d74")); shoeR.setStrokeWidth(2);
        Polygon tie=new Polygon(43.0,22.0,41.5,30.0,44.5,30.0); tie.setFill(Color.web("#0e7a56",0.5));
        art.getChildren().addAll(board,legLeft,legRight,writeLine1,eqLeft,eqRight,eq1,eq2,result,
            body,arm,pointer,pointerTip,head,tie,legL,legR,shoeL,shoeR);
        return art;
    }

    private VBox buildPortalCard(Pane artPane, String title, String desc,
                                  String accent, Runnable action) {
        VBox card = new VBox(14);
        card.setPrefWidth(360);
        card.setPadding(new Insets(20, 22, 20, 22));
        card.setStyle("-fx-background-color:#ffffff;-fx-background-radius:9;-fx-border-color:#e6e8ec;-fx-border-radius:9;-fx-border-width:1;-fx-cursor:hand;");
        DropShadow ds = new DropShadow();
        ds.setColor(Color.color(0,0,0,0.04)); ds.setRadius(8); ds.setOffsetY(2);
        card.setEffect(ds);

        StackPane artBox = new StackPane(artPane);
        artBox.setPrefSize(52, 52);
        artBox.setStyle("-fx-background-color:" + accent + "10;-fx-background-radius:10;");

        VBox textArea = new VBox(4);
        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-font-size:15px;-fx-font-weight:700;-fx-text-fill:#1c2333;-fx-letter-spacing:-0.1px;");
        Label descLbl = new Label(desc);
        descLbl.setStyle("-fx-font-size:12px;-fx-text-fill:#6b7585;-fx-wrap-text:true;");
        descLbl.setWrapText(true);
        textArea.getChildren().addAll(titleLbl, descLbl);

        HBox topRow = new HBox(14, artBox, textArea);
        topRow.setAlignment(Pos.CENTER_LEFT);

        HBox ctaRow = new HBox(6);
        ctaRow.setAlignment(Pos.CENTER_LEFT);
        Label ctaLbl = new Label("Access portal");
        ctaLbl.setStyle("-fx-font-size:12px;-fx-font-weight:600;-fx-text-fill:" + accent + ";");
        Region arrowIco = UIUtils.icon(UIUtils.ICO_CHEVRON_R, accent, 12);
        ctaRow.getChildren().addAll(ctaLbl, arrowIco);

        card.getChildren().addAll(topRow, UIUtils.divider(), ctaRow);
        card.setOnMouseClicked(e -> action.run());

        card.setOnMouseEntered(e -> {
            ds.setOffsetY(6); ds.setRadius(18); ds.setColor(Color.web(accent, 0.11));
            card.setStyle("-fx-background-color:#ffffff;-fx-background-radius:9;-fx-border-color:"+accent+"55;-fx-border-radius:9;-fx-border-width:1.5;-fx-cursor:hand;");
            card.setTranslateY(-2);
        });
        card.setOnMouseExited(e -> {
            ds.setOffsetY(2); ds.setRadius(8); ds.setColor(Color.color(0,0,0,0.04));
            card.setStyle("-fx-background-color:#ffffff;-fx-background-radius:9;-fx-border-color:#e6e8ec;-fx-border-radius:9;-fx-border-width:1;-fx-cursor:hand;");
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
