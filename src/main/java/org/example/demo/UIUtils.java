package org.example.demo;

import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class UIUtils {

    // ── Theme state — light by default ───────────────────────
    public static boolean darkMode = false;

    // Dynamic color getters
    public static String bgSidebar()  { return "#0f172a"; }                        // sidebar always dark
    public static String bgContent()  { return darkMode ? "#0d1117" : "#f8fafc"; }
    public static String bgSurface()  { return darkMode ? "#161b22" : "#ffffff"; }
    public static String bgCard()     { return darkMode ? "#1c2333" : "#ffffff"; }
    public static String bgHover()    { return darkMode ? "#21262d" : "#f1f5f9"; }
    public static String border()     { return darkMode ? "#30363d" : "#e2e8f0"; }
    public static String textDark()   { return darkMode ? "#e6edf3" : "#0f172a"; }
    public static String textMid()    { return darkMode ? "#8b949e" : "#475569"; }
    public static String textSubtle() { return darkMode ? "#6e7681" : "#94a3b8"; }

    // Static compat aliases
    public static final String BG_DARK      = "#0f172a";
    public static final String BG_DARK2     = "#1e293b";
    public static final String BG_LIGHT     = "#f8fafc";
    public static final String BG_SURFACE   = "#ffffff";
    public static final String CARD_BG      = BG_SURFACE;
    public static final String BORDER       = "#e2e8f0";
    public static final String BORDER_FOCUS = "#2563eb";
    public static final String TEXT_DARK    = "#0f172a";
    public static final String TEXT_MID     = "#475569";
    public static final String TEXT_SUBTLE  = "#94a3b8";
    public static final String TEXT_LIGHT   = "#f8fafc";

    // Accents — same in both themes
    public static final String ACCENT_BLUE  = "#2563eb";
    public static final String ACCENT_GREEN = "#16a34a";
    public static final String ACCENT_PURP  = "#7c3aed";
    public static final String ACCENT_ORG   = "#ea580c";
    public static final String ACCENT_RED   = "#ef4444";
    public static final String ACCENT_YELL  = "#d97706";

    // ── iOS-style theme toggle switch ─────────────────────────
    // Track: pill shape. Thumb: circle with emoji. Animates on click.
    // Left (☀) = Light,  Right (🌙) = Dark
    public static StackPane themeToggleSwitch(Runnable onToggle) {
        final double TRACK_W = 56, TRACK_H = 28, THUMB_R = 11;
        final double THUMB_OFF = 6;   // margin from edge
        final double X_LIGHT = THUMB_OFF + THUMB_R;          // thumb centre x when light
        final double X_DARK  = TRACK_W - THUMB_OFF - THUMB_R; // thumb centre x when dark

        // Track (pill background)
        Rectangle track = new Rectangle(TRACK_W, TRACK_H);
        track.setArcWidth(TRACK_H); track.setArcHeight(TRACK_H);
        track.setFill(darkMode ? Color.web("#334155") : Color.web("#cbd5e1"));

        // Thumb (circle)
        Circle thumb = new Circle(THUMB_R);
        thumb.setFill(Color.WHITE);
        DropShadow ts = new DropShadow(4, Color.color(0,0,0,0.25));
        thumb.setEffect(ts);

        // Emoji label inside thumb — colored ☀ amber, 🌙 indigo
        Label emoji = new Label(darkMode ? "🌙" : "☀");
        emoji.setStyle("-fx-font-size:12px;-fx-text-fill:" + (darkMode ? "#818cf8" : "#f59e0b") + ";");

        // Stack: track → thumb + emoji together
        StackPane thumbStack = new StackPane(thumb, emoji);
        thumbStack.setPrefSize(THUMB_R * 2, THUMB_R * 2);

        StackPane switchNode = new StackPane(track, thumbStack);
        switchNode.setPrefSize(TRACK_W, TRACK_H);
        switchNode.setCursor(javafx.scene.Cursor.HAND);

        // Position thumb to correct side initially
        double initX = darkMode ? (X_DARK - TRACK_W / 2) : (X_LIGHT - TRACK_W / 2);
        thumbStack.setTranslateX(initX);

        switchNode.setOnMouseClicked(e -> {
            darkMode = !darkMode;

            // Animate thumb slide
            double toX = darkMode ? (X_DARK - TRACK_W / 2) : (X_LIGHT - TRACK_W / 2);
            TranslateTransition anim = new TranslateTransition(Duration.millis(200), thumbStack);
            anim.setToX(toX);
            anim.setInterpolator(Interpolator.EASE_BOTH);
            anim.play();

            // Swap track colour
            FillTransition ft = new FillTransition(Duration.millis(200), track);
            ft.setToValue(darkMode ? Color.web("#334155") : Color.web("#cbd5e1"));
            ft.play();

            // Swap emoji + color
            emoji.setText(darkMode ? "🌙" : "☀");
            emoji.setStyle("-fx-font-size:12px;-fx-text-fill:" + (darkMode ? "#818cf8" : "#f59e0b") + ";");

            onToggle.run();
        });

        return switchNode;
    }

    // ── Typography ────────────────────────────────────────────
    public static Label heading(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size:22px;-fx-font-weight:bold;-fx-text-fill:" + textDark() + ";-fx-letter-spacing:-0.5px;");
        return l;
    }

    public static Label subheading(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size:13px;-fx-text-fill:" + textMid() + ";-fx-font-weight:normal;");
        return l;
    }

    public static Label sectionLabel(String text) {
        Label l = new Label(text.toUpperCase());
        l.setStyle("-fx-font-size:11px;-fx-font-weight:bold;-fx-text-fill:" + textSubtle() + ";-fx-letter-spacing:0.8px;");
        return l;
    }

    public static Label badge(String text, String color) {
        Label l = new Label(text);
        l.setStyle(
                "-fx-background-color:" + color + "22;" +
                        "-fx-text-fill:" + color + ";" +
                        "-fx-font-weight:bold;-fx-font-size:11px;" +
                        "-fx-padding:3 9 3 9;-fx-background-radius:6;"
        );
        return l;
    }

    // ── Card ──────────────────────────────────────────────────
    public static VBox card(double width) {
        VBox c = new VBox(12);
        c.setPadding(new Insets(20));
        c.setPrefWidth(width);
        c.setStyle(
                "-fx-background-color:" + bgCard() + ";" +
                        "-fx-background-radius:12;" +
                        "-fx-border-color:" + border() + ";" +
                        "-fx-border-radius:12;-fx-border-width:1;"
        );
        DropShadow ds = new DropShadow();
        ds.setColor(Color.color(0, 0, 0, darkMode ? 0.3 : 0.06));
        ds.setOffsetY(2); ds.setRadius(12);
        c.setEffect(ds);
        c.setOnMouseEntered(e -> { ds.setOffsetY(6); ds.setRadius(20); ds.setColor(Color.color(0,0,0, darkMode ? 0.45 : 0.10)); c.setTranslateY(-2); });
        c.setOnMouseExited(e  -> { ds.setOffsetY(2); ds.setRadius(12); ds.setColor(Color.color(0,0,0, darkMode ? 0.30 : 0.06)); c.setTranslateY(0); });
        return c;
    }

    // ── Stat card ─────────────────────────────────────────────
    public static VBox statCard(String icon, String value, String label, String accent) {
        VBox c = new VBox(4);
        c.setPadding(new Insets(0));
        c.setPrefWidth(175);
        c.setStyle(
                "-fx-background-color:" + bgCard() + ";" +
                        "-fx-background-radius:12;" +
                        "-fx-border-color:" + border() + ";" +
                        "-fx-border-radius:12;-fx-border-width:1;"
        );
        DropShadow ds = new DropShadow();
        ds.setColor(Color.color(0, 0, 0, darkMode ? 0.3 : 0.05));
        ds.setOffsetY(2); ds.setRadius(10);
        c.setEffect(ds);

        Region strip = new Region();
        strip.setPrefHeight(4);
        strip.setStyle("-fx-background-color:" + accent + ";-fx-background-radius:12 12 0 0;");

        VBox body = new VBox(4);
        body.setPadding(new Insets(16, 18, 16, 18));
        body.setAlignment(Pos.CENTER_LEFT);
        Label ico = new Label(icon); ico.setStyle("-fx-font-size:22px;");
        Label val = new Label(value); val.setStyle("-fx-font-size:26px;-fx-font-weight:bold;-fx-text-fill:" + textDark() + ";");
        Label lbl = new Label(label); lbl.setStyle("-fx-font-size:12px;-fx-text-fill:" + textMid() + ";");
        body.getChildren().addAll(ico, val, lbl);
        c.getChildren().addAll(strip, body);
        c.setOnMouseEntered(e -> { ds.setOffsetY(5); ds.setRadius(18); c.setTranslateY(-2); });
        c.setOnMouseExited(e  -> { ds.setOffsetY(2); ds.setRadius(10); c.setTranslateY(0);  });
        return c;
    }

    // ── Styled fields ─────────────────────────────────────────
    public static TextField styledField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setStyle(
                "-fx-background-color:" + bgSurface() + ";" +
                        "-fx-border-color:" + border() + ";" +
                        "-fx-border-radius:8;-fx-background-radius:8;" +
                        "-fx-font-size:14px;-fx-padding:10 14;" +
                        "-fx-text-fill:" + textDark() + ";" +
                        "-fx-prompt-text-fill:" + textSubtle() + ";"
        );
        tf.setPrefHeight(42);
        return tf;
    }

    public static PasswordField styledPassword(String prompt) {
        PasswordField pf = new PasswordField();
        pf.setPromptText(prompt);
        pf.setStyle(
                "-fx-background-color:" + bgSurface() + ";" +
                        "-fx-border-color:" + border() + ";" +
                        "-fx-border-radius:8;-fx-background-radius:8;" +
                        "-fx-font-size:14px;-fx-padding:10 14;" +
                        "-fx-text-fill:" + textDark() + ";" +
                        "-fx-prompt-text-fill:" + textSubtle() + ";"
        );
        pf.setPrefHeight(42);
        return pf;
    }

    // ── Styled TextArea ───────────────────────────────────────
    public static TextArea styledTextArea(String prompt, double prefHeight) {
        TextArea ta = new TextArea();
        ta.setPromptText(prompt);
        ta.setWrapText(true);
        ta.setPrefHeight(prefHeight);
        ta.setStyle(
                "-fx-background-color:" + bgSurface() + ";" +
                        "-fx-border-color:" + border() + ";" +
                        "-fx-border-radius:8;-fx-background-radius:8;" +
                        "-fx-font-size:13px;" +
                        "-fx-text-fill:" + textDark() + ";" +
                        "-fx-prompt-text-fill:" + textSubtle() + ";" +
                        "-fx-control-inner-background:" + bgSurface() + ";"
        );
        return ta;
    }

    // ── PRIMARY button ────────────────────────────────────────
    public static Button primaryBtn(String icon, String text, String color) {
        String label = (icon == null || icon.isEmpty()) ? text : icon + "  " + text;
        Button b = new Button(label);
        b.setStyle(
                "-fx-background-color:" + color + ";-fx-text-fill:white;" +
                        "-fx-font-weight:bold;-fx-font-size:13px;" +
                        "-fx-background-radius:8;-fx-padding:9 18;" +
                        "-fx-cursor:hand;-fx-effect:null;"
        );
        DropShadow ds = new DropShadow();
        ds.setColor(Color.color(0,0,0,0.12)); ds.setOffsetY(2); ds.setRadius(6);
        b.setEffect(ds);
        b.setOnMouseEntered(e -> { b.setTranslateY(-1); ds.setOffsetY(4); ds.setRadius(10); ds.setColor(Color.color(0,0,0,0.18)); });
        b.setOnMouseExited(e  -> { b.setTranslateY(0);  ds.setOffsetY(2); ds.setRadius(6);  ds.setColor(Color.color(0,0,0,0.12)); });
        b.setOnMousePressed(e  -> b.setTranslateY(1));
        b.setOnMouseReleased(e -> b.setTranslateY(-1));
        return b;
    }

    // ── GHOST button ──────────────────────────────────────────
    public static Button ghostBtn(String icon, String text, String color) {
        String label = (icon == null || icon.isEmpty()) ? text : icon + "  " + text;
        Button b = new Button(label);
        String normal =
                "-fx-background-color:transparent;-fx-text-fill:" + color + ";" +
                        "-fx-font-weight:bold;-fx-font-size:13px;" +
                        "-fx-border-color:" + color + ";-fx-border-radius:8;" +
                        "-fx-background-radius:8;-fx-padding:8 16;" +
                        "-fx-cursor:hand;-fx-border-width:1;";
        String hover =
                "-fx-background-color:" + color + "0f;-fx-text-fill:" + color + ";" +
                        "-fx-font-weight:bold;-fx-font-size:13px;" +
                        "-fx-border-color:" + color + ";-fx-border-radius:8;" +
                        "-fx-background-radius:8;-fx-padding:8 16;" +
                        "-fx-cursor:hand;-fx-border-width:1;";
        b.setStyle(normal);
        b.setOnMouseEntered(e -> b.setStyle(hover));
        b.setOnMouseExited(e  -> b.setStyle(normal));
        b.setOnMousePressed(e  -> b.setTranslateY(1));
        b.setOnMouseReleased(e -> b.setTranslateY(0));
        return b;
    }

    // ── SIDEBAR buttons ───────────────────────────────────────
    public static void sidebarBtn(Button b, String icon, String label, String color) {
        b.setText(icon + "   " + label);
        String inactive =
                "-fx-background-color:transparent;-fx-text-fill:" + TEXT_SUBTLE + ";" +
                        "-fx-font-size:13px;-fx-font-weight:normal;" +
                        "-fx-background-radius:8;-fx-padding:10 12 10 16;" +
                        "-fx-cursor:hand;-fx-alignment:center-left;";
        String hov =
                "-fx-background-color:" + color + "14;-fx-text-fill:" + color + ";" +
                        "-fx-font-size:13px;-fx-font-weight:bold;" +
                        "-fx-background-radius:8;-fx-padding:10 12 10 16;" +
                        "-fx-cursor:hand;-fx-alignment:center-left;";
        b.setStyle(inactive);
        b.setOnMouseEntered(e -> b.setStyle(hov));
        b.setOnMouseExited(e  -> b.setStyle(inactive));
    }

    public static Button sidebarBtn(String icon, String label, String color) {
        Button b = new Button(icon + "   " + label);
        b.setPrefWidth(190); b.setPrefHeight(42);
        b.setAlignment(Pos.CENTER_LEFT);
        String inactive =
                "-fx-background-color:transparent;-fx-text-fill:" + TEXT_SUBTLE + ";" +
                        "-fx-font-size:13px;-fx-font-weight:normal;" +
                        "-fx-background-radius:8;-fx-padding:10 12 10 16;" +
                        "-fx-cursor:hand;-fx-alignment:center-left;";
        String hov =
                "-fx-background-color:" + color + "14;-fx-text-fill:" + color + ";" +
                        "-fx-font-size:13px;-fx-font-weight:bold;" +
                        "-fx-background-radius:8;-fx-padding:10 12 10 16;" +
                        "-fx-cursor:hand;-fx-alignment:center-left;";
        b.setStyle(inactive);
        b.setOnMouseEntered(e -> b.setStyle(hov));
        b.setOnMouseExited(e  -> b.setStyle(inactive));
        return b;
    }

    public static void setSidebarBtnActive(Button b, String color) {
        b.setStyle(
                "-fx-background-color:" + color + "1a;-fx-text-fill:" + color + ";" +
                        "-fx-font-size:13px;-fx-font-weight:bold;" +
                        "-fx-background-radius:8;-fx-padding:10 12 10 16;" +
                        "-fx-cursor:hand;-fx-alignment:center-left;" +
                        "-fx-border-color:" + color + ";-fx-border-width:0 0 0 2.5;-fx-border-radius:0;"
        );
    }

    // ── Divider ───────────────────────────────────────────────
    public static Separator divider() {
        Separator s = new Separator();
        s.setStyle("-fx-background-color:" + border() + ";-fx-opacity:0.7;");
        return s;
    }

    // ── Slide + fade ──────────────────────────────────────────
    public static void slideIn(Node node, boolean fromRight) {
        double from = fromRight ? 40 : -40;
        node.setTranslateX(from); node.setOpacity(0);
        TranslateTransition tt = new TranslateTransition(Duration.millis(300), node);
        tt.setFromX(from); tt.setToX(0); tt.setInterpolator(Interpolator.EASE_OUT);
        FadeTransition ft = new FadeTransition(Duration.millis(300), node);
        ft.setFromValue(0); ft.setToValue(1);
        new ParallelTransition(tt, ft).play();
    }

    public static void playTransition(Pane root, boolean fwd) { slideIn(root, fwd); }

    // ── Legacy helpers ────────────────────────────────────────
    public static void applyButtonEffects(Button btn, String hoverColor) {
        String base  = "-fx-background-color:" + bgSurface() + ";-fx-text-fill:" + textDark() + ";-fx-font-weight:bold;-fx-font-size:13px;-fx-background-radius:8;";
        String hover = "-fx-background-color:" + hoverColor + ";-fx-text-fill:white;-fx-font-weight:bold;-fx-font-size:13px;-fx-background-radius:8;";
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e  -> btn.setStyle(base));
    }

    public static void applyLinkEffects(Hyperlink link) {
        link.setStyle("-fx-text-fill:" + ACCENT_BLUE + ";-fx-font-size:13px;-fx-underline:false;-fx-font-weight:bold;");
        link.setOnMouseEntered(e -> link.setStyle("-fx-text-fill:" + ACCENT_BLUE + ";-fx-underline:true;-fx-font-size:13px;-fx-font-weight:bold;"));
        link.setOnMouseExited(e  -> link.setStyle("-fx-text-fill:" + ACCENT_BLUE + ";-fx-underline:false;-fx-font-size:13px;-fx-font-weight:bold;"));
    }

    // ── CSS ───────────────────────────────────────────────────
    private static final String CSS = "/org/example/demo/style.css";
    public static void applyStyle(Scene scene) {
        if (UIUtils.class.getResource(CSS) != null)
            scene.getStylesheets().add(UIUtils.class.getResource(CSS).toExternalForm());
    }

    // ── Toast ─────────────────────────────────────────────────
    public enum ToastType { SUCCESS, ERROR, INFO }

    public static void toast(Pane parent, String msg, ToastType type) {
        String bg, icon;
        switch (type) {
            case SUCCESS -> { bg = ACCENT_GREEN; icon = "✅"; }
            case ERROR   -> { bg = ACCENT_RED;   icon = "❌"; }
            default      -> { bg = ACCENT_BLUE;  icon = "ℹ"; }
        }
        Label t = new Label(icon + "  " + msg);
        t.setStyle(
                "-fx-background-color:" + bg + ";-fx-text-fill:white;" +
                        "-fx-font-size:13px;-fx-font-weight:bold;" +
                        "-fx-padding:10 20;-fx-background-radius:10;"
        );
        DropShadow ds = new DropShadow();
        ds.setColor(Color.color(0,0,0,0.25)); ds.setRadius(12); ds.setOffsetY(4);
        t.setEffect(ds);
        t.setLayoutX(20); t.setLayoutY(20); t.setOpacity(0);
        parent.getChildren().add(t);
        FadeTransition fi = new FadeTransition(Duration.millis(250), t); fi.setToValue(1); fi.play();
        new Timeline(new KeyFrame(Duration.seconds(2.5), e -> {
            FadeTransition fo = new FadeTransition(Duration.millis(350), t);
            fo.setToValue(0);
            fo.setOnFinished(ev -> parent.getChildren().remove(t));
            fo.play();
        })).play();
    }

    public static class Toast {
        public static void success(Pane p, String msg) { toast(p, msg, ToastType.SUCCESS); }
        public static void error(Pane p, String msg)   { toast(p, msg, ToastType.ERROR);   }
        public static void info(Pane p, String msg)    { toast(p, msg, ToastType.INFO);    }
    }
}