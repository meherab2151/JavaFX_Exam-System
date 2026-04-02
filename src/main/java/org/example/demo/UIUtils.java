package org.example.demo;

import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.util.Duration;

public class UIUtils {

    public static boolean darkMode = false;

    public static String bgContent()  { return darkMode ? "#111722" : "#fafaf8"; }
    public static String bgSurface()  { return darkMode ? "#171d2b" : "#ffffff"; }
    public static String bgCard()     { return darkMode ? "#1c2333" : "#ffffff"; }
    public static String bgSidebar()  { return "#111722"; }             // always dark
    public static String bgHover()    { return darkMode ? "#222a3c" : "#f0f7f6"; }
    public static String bgInput()    { return darkMode ? "#1c2333" : "#ffffff"; }
    public static String bgMuted()    { return darkMode ? "#1a2030" : "#f5f6f7"; }
    public static String border()     { return darkMode ? "#29334a" : "#e6e8ec"; }
    public static String borderFocus(){ return "#0f7d74"; }
    public static String textDark()   { return darkMode ? "#e8eaf2" : "#1c2333"; }
    public static String textMid()    { return darkMode ? "#7e8aa0" : "#4a5568"; }
    public static String textSubtle() { return darkMode ? "#505869" : "#9aa1b0"; }

    public static final String BG_DARK      = "#111722";
    public static final String BG_DARK2     = "#1c2333";
    public static final String BG_LIGHT     = "#fafaf8";
    public static final String BORDER       = "#e6e8ec";
    public static final String TEXT_DARK    = "#1c2333";
    public static final String TEXT_MID     = "#4a5568";
    public static final String TEXT_SUBTLE  = "#9aa1b0";
    public static final String TEXT_LIGHT   = "#fafaf8";
    public static final String BG_SURFACE   = "#ffffff";

    public static final String ACCENT_BLUE  = "#0f7d74";   // teal — primary
    public static final String ACCENT_GREEN = "#0e7a56";   // forest green — success
    public static final String ACCENT_PURP  = "#5046a0";   // slate indigo — secondary
    public static final String ACCENT_ORG   = "#b45309";   // amber — warning
    public static final String ACCENT_RED   = "#c0392b";   // crimson — danger
    public static final String ACCENT_YELL  = "#8a6504";   // warm ochre
    public static final String ACCENT_TEAL  = "#0f7d74";   // teal — info (same as primary)

    private static String hex(Color c) {
        return String.format("#%02x%02x%02x",
                (int)(c.getRed()*255),(int)(c.getGreen()*255),(int)(c.getBlue()*255));
    }


    public static final String ICO_DASHBOARD = "M2 2h5v5H2zm7 0h5v5H9zM2 9h5v5H2zm7 0h5v5H9z";
    public static final String ICO_EXAM      = "M4 2h8a1 1 0 011 1v10a1 1 0 01-1 1H4a1 1 0 01-1-1V3a1 1 0 011-1zm1 3h6M5 8h4M5 11h3";
    public static final String ICO_QUESTION  = "M6 5.5A2.5 2.5 0 0111 7c0 1.5-1.5 2-2.5 2.5V11M8.5 13v.5";
    public static final String ICO_BANK      = "M2 6l6-4 6 4v1H2zm1 2h10v6H3zm3 0v6m4-6v6";
    public static final String ICO_HISTORY   = "M12 8A4 4 0 114 8a4 4 0 018 0zM8 6v2l1.5 1.5";
    public static final String ICO_TROPHY    = "M5 2h6l-1 4H6zm-2 0h2M11 2h2M4 6c0 2.5 1.5 4 4 4s4-1.5 4-4M6 10v2M10 10v2M4 12h8";
    public static final String ICO_ANNOUNCE  = "M3 7h2v4H3zM5 5l6-3v10L5 9V5zm8 1v4";
    public static final String ICO_LOGOUT    = "M10 3h3a1 1 0 011 1v8a1 1 0 01-1 1h-3M7 10l3-2-3-2M3 8h7";
    public static final String ICO_SETTINGS  = "M8 5a3 3 0 100 6 3 3 0 000-6zM8 2v1M8 13v1M3.22 3.22l.7.7M12.07 12.07l.7.7M2 8h1M13 8h1M3.22 12.78l.7-.7M12.07 3.93l.7-.7";
    public static final String ICO_STUDENT   = "M8 2a3 3 0 100 6 3 3 0 000-6zM3 14c0-2.8 2.2-5 5-5s5 2.2 5 5";
    public static final String ICO_TEACHER   = "M8 2a3 3 0 100 6 3 3 0 000-6zM3 14c0-2.8 2.2-5 5-5s5 2.2 5 5M10 7l2 2-4 4";
    public static final String ICO_BACK      = "M10 4L6 8l4 4";
    public static final String ICO_FORWARD   = "M6 4l4 4-4 4";
    public static final String ICO_CLOSE     = "M4 4l8 8M12 4l-8 8";
    public static final String ICO_CHECK     = "M3 8l4 4 6-7";
    public static final String ICO_PLUS      = "M8 3v10M3 8h10";
    public static final String ICO_EDIT      = "M11 3l2 2-8 8H3v-2zm1-1l1-1 2 2-1 1";
    public static final String ICO_DELETE    = "M4 5h8M6 5V3h4v2M5 5v8a1 1 0 001 1h4a1 1 0 001-1V5";
    public static final String ICO_SEARCH    = "M10.5 10.5l3 3M6.5 11a4.5 4.5 0 100-9 4.5 4.5 0 000 9z";
    public static final String ICO_LIVE      = "M4.93 4.93A6 6 0 1011.07 11.07M8 8m-1.5 0a1.5 1.5 0 113 0 1.5 1.5 0 01-3 0";
    public static final String ICO_SCHEDULE  = "M4 2v2M12 2v2M2 8h12M3 4h10a1 1 0 011 1v7a1 1 0 01-1 1H3a1 1 0 01-1-1V5a1 1 0 011-1zm4 6h2M9 10l1.5 1.5";
    public static final String ICO_CLOCK     = "M8 2a6 6 0 100 12A6 6 0 008 2zM8 5v3l2 1.5";
    public static final String ICO_CHART     = "M2 12l4-5 3 3 3-6 2 2";
    public static final String ICO_COPY      = "M5 4H3a1 1 0 00-1 1v8a1 1 0 001 1h8a1 1 0 001-1v-2M6 2h6a1 1 0 011 1v8a1 1 0 01-1 1H6a1 1 0 01-1-1V3a1 1 0 011-1z";
    public static final String ICO_FLAG      = "M4 2v12M4 2h8l-2 4 2 4H4";
    public static final String ICO_SEND      = "M2 12L14 8 2 4v3.5l8 .5-8 .5z";
    public static final String ICO_KEY       = "M5.5 9.5a4 4 0 110-7 4 4 0 010 7zm5.5-4l3 3-1.5 1.5-1-1L11 8M9.5 6.5l1 1";
    public static final String ICO_LOCK      = "M5 8V6a3 3 0 016 0v2M3 8h10a1 1 0 011 1v4a1 1 0 01-1 1H3a1 1 0 01-1-1V9a1 1 0 011-1zm5 2v2";
    public static final String ICO_EYE       = "M1 8s3-5 7-5 7 5 7 5-3 5-7 5-7-5-7-5zm7-2a2 2 0 100 4 2 2 0 000-4z";
    public static final String ICO_FILTER    = "M2 4h12M4 8h8M6 12h4";
    public static final String ICO_SORT      = "M2 4h8M2 8h6M2 12h4M11 6l3 3-3 3";
    public static final String ICO_DOWNLOAD  = "M8 3v8M5 8l3 3 3-3M3 13h10";
    public static final String ICO_UPLOAD    = "M8 11V3M5 6l3-3 3 3M3 13h10";
    public static final String ICO_INFO      = "M8 7v4M8 5v.5M8 2a6 6 0 100 12A6 6 0 008 2z";
    public static final String ICO_WARN      = "M8 3L1 13h14zM8 8v2M8 12v.5";
    public static final String ICO_STAR      = "M8 2l1.8 3.6L14 6.2l-3 2.9.7 4.1L8 11l-3.7 2.2.7-4.1-3-2.9 4.2-.6z";
    public static final String ICO_MENU      = "M3 4h10M3 8h10M3 12h10";
    public static final String ICO_CHEVRON_D = "M4 6l4 4 4-4";
    public static final String ICO_CHEVRON_R = "M6 4l4 4-4 4";
    public static final String ICO_USERS     = "M5 7a3 3 0 100-6 3 3 0 000 6zm-4 7c0-2.2 1.8-4 4-4M11 7a3 3 0 100-6 3 3 0 000 6zm2 1a4 4 0 014 4";
    public static final String ICO_ANALYTICS = "M2 12l3-5 2.5 3L11 5l3 7";
    public static final String ICO_BADGE     = "M8 2a3 3 0 100 6 3 3 0 000-6zM5 10h6l1 4H4z";
    public static final String ICO_DOT_FILL  = "M8 4a4 4 0 100 8 4 4 0 000-8z";
    public static final String ICO_STOP      = "M4 4h8v8H4z";
    public static final String ICO_GRID      = "M2 2h4v4H2zM9 2h5v4H9zM2 8h5v6H2zM9 8h5v2H9zm0 4h5v2H9z";
    public static final String ICO_CODE      = "M5 7l-3 1 3 1M11 7l3 1-3 1M9 5l-2 6";

    public static Region icon(String svgPath, String colorHex, double size) {
        Region r = new Region();
        r.setPrefSize(size, size);
        r.setMinSize(size, size);
        r.setMaxSize(size, size);
        r.setStyle(
            "-fx-background-color:" + colorHex + ";" +
            "-fx-shape:\"" + svgPath + "\";" +
            "-fx-scale-shape:true;" +
            "-fx-background-size:cover;"
        );
        return r;
    }

    public static HBox iconLabel(String svgPath, String colorHex, double iconSize,
                                  String text, String textStyle) {
        Region ic = icon(svgPath, colorHex, iconSize);
        Label  lb = new Label(text);
        lb.setStyle(textStyle);
        HBox row = new HBox(8, ic, lb);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    public static StackPane themeToggleSwitch(Runnable onToggle) {
        final double TW = 48, TH = 24, TR = 9;
        final double OFF = 3.5;
        final double X_L = OFF + TR;
        final double X_D = TW - OFF - TR;

        Rectangle track = new Rectangle(TW, TH);
        track.setArcWidth(TH); track.setArcHeight(TH);
        track.setFill(darkMode ? Color.web("#2a3450") : Color.web("#d1d9e0"));

        Circle thumb = new Circle(TR);
        thumb.setFill(darkMode ? Color.web("#0f7d74") : Color.WHITE);
        DropShadow ts = new DropShadow(4, Color.color(0,0,0,0.20));
        thumb.setEffect(ts);

        Region modeIcon = icon(
            darkMode ? "M8 2a6 6 0 000 12 5 5 0 010-12z" : "M8 4a4 4 0 100 8 4 4 0 000-8z",
            darkMode ? "#ffffff" : "#b45309",
            8
        );

        StackPane thumbStack = new StackPane(thumb, modeIcon);
        thumbStack.setPrefSize(TR*2, TR*2);

        StackPane sw = new StackPane(track, thumbStack);
        sw.setPrefSize(TW, TH);
        sw.setCursor(javafx.scene.Cursor.HAND);

        thumbStack.setTranslateX(darkMode ? (X_D - TW/2) : (X_L - TW/2));

        sw.setOnMouseClicked(e -> {
            darkMode = !darkMode;
            double toX = darkMode ? (X_D - TW/2) : (X_L - TW/2);
            TranslateTransition tt = new TranslateTransition(Duration.millis(180), thumbStack);
            tt.setToX(toX); tt.setInterpolator(Interpolator.EASE_BOTH); tt.play();
            FillTransition ft = new FillTransition(Duration.millis(180), track);
            ft.setToValue(darkMode ? Color.web("#2a3450") : Color.web("#d1d9e0")); ft.play();
            thumb.setFill(darkMode ? Color.web("#0f7d74") : Color.WHITE);
            modeIcon.setStyle(
                "-fx-background-color:" + (darkMode ? "#ffffff" : "#b45309") + ";" +
                "-fx-shape:\"" + (darkMode ?
                    "M8 2a6 6 0 000 12 5 5 0 010-12z" :
                    "M8 4a4 4 0 100 8 4 4 0 000-8z") + "\";" +
                "-fx-scale-shape:true;"
            );
            onToggle.run();
        });
        return sw;
    }

    public static Label heading(String text) {
        Label l = new Label(text);
        l.setStyle(
            "-fx-font-size:19px;" +
            "-fx-font-weight:700;" +
            "-fx-text-fill:" + textDark() + ";" +
            "-fx-letter-spacing:-0.2px;"
        );
        return l;
    }

    public static Label subheading(String text) {
        Label l = new Label(text);
        l.setStyle(
            "-fx-font-size:13px;" +
            "-fx-text-fill:" + textMid() + ";" +
            "-fx-font-weight:400;"
        );
        return l;
    }

    public static Label sectionLabel(String text) {
        Label l = new Label(text.toUpperCase());
        l.setStyle(
            "-fx-font-size:10px;" +
            "-fx-font-weight:700;" +
            "-fx-text-fill:" + textSubtle() + ";" +
            "-fx-letter-spacing:1.5px;"
        );
        return l;
    }

    public static Label badge(String text, String color) {
        Label l = new Label(text);
        l.setStyle(
            "-fx-background-color:" + color + "18;" +
            "-fx-text-fill:" + color + ";" +
            "-fx-font-weight:600;" +
            "-fx-font-size:10.5px;" +
            "-fx-padding:2 8 2 8;" +
            "-fx-background-radius:4;" +
            "-fx-letter-spacing:0.2px;"
        );
        return l;
    }

    public static HBox statusDot(String label, String color) {
        Circle dot = new Circle(4, Color.web(color));
        DropShadow glow = new DropShadow(5, Color.web(color, 0.4));
        dot.setEffect(glow);
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size:12px;-fx-font-weight:600;-fx-text-fill:" + color + ";");
        HBox row = new HBox(6, dot, lbl);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    public static VBox card(double width) {
        VBox c = new VBox(12);
        c.setPadding(new Insets(20));
        c.setPrefWidth(width);
        c.setStyle(
            "-fx-background-color:" + bgCard() + ";" +
            "-fx-background-radius:8;" +
            "-fx-border-color:" + border() + ";" +
            "-fx-border-radius:8;-fx-border-width:1;"
        );
        DropShadow ds = new DropShadow();
        ds.setColor(Color.color(0,0,0, darkMode ? 0.22 : 0.04));
        ds.setOffsetY(1); ds.setRadius(6);
        c.setEffect(ds);
        c.setOnMouseEntered(e -> {
            ds.setOffsetY(3); ds.setRadius(12);
            ds.setColor(Color.color(0,0,0, darkMode?0.35:0.07));
            c.setTranslateY(-1);
        });
        c.setOnMouseExited(e -> {
            ds.setOffsetY(1); ds.setRadius(6);
            ds.setColor(Color.color(0,0,0, darkMode?0.22:0.04));
            c.setTranslateY(0);
        });
        return c;
    }

    public static VBox statCard(String svgIcon, String value, String label, String accent) {
        VBox c = new VBox(0);
        c.setPrefWidth(158);
        c.setStyle(
            "-fx-background-color:" + bgCard() + ";" +
            "-fx-background-radius:8;" +
            "-fx-border-color:" + border() + ";" +
            "-fx-border-radius:8;-fx-border-width:1;"
        );
        DropShadow ds = new DropShadow();
        ds.setColor(Color.color(0,0,0, darkMode ? 0.20 : 0.035));
        ds.setOffsetY(1); ds.setRadius(6);
        c.setEffect(ds);

        Region strip = new Region();
        strip.setPrefHeight(3);
        strip.setStyle("-fx-background-color:" + accent + ";-fx-background-radius:8 8 0 0;");

        VBox body = new VBox(6);
        body.setPadding(new Insets(13, 16, 14, 16));
        body.setAlignment(Pos.CENTER_LEFT);

        Region ico = icon(svgIcon, accent, 16);
        Label val = new Label(value);
        val.setStyle("-fx-font-size:24px;-fx-font-weight:700;-fx-text-fill:" + textDark() + ";-fx-letter-spacing:-0.5px;");
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size:11px;-fx-text-fill:" + textSubtle() + ";-fx-font-weight:500;-fx-letter-spacing:0.4px;");
        body.getChildren().addAll(ico, val, lbl);
        c.getChildren().addAll(strip, body);

        c.setOnMouseEntered(e -> { ds.setOffsetY(4); ds.setRadius(12); c.setTranslateY(-1); });
        c.setOnMouseExited(e  -> { ds.setOffsetY(1); ds.setRadius(6);  c.setTranslateY(0);  });
        return c;
    }

    public static TextField styledField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setStyle(
            "-fx-background-color:" + bgInput() + ";" +
            "-fx-border-color:" + border() + ";" +
            "-fx-border-radius:6;-fx-background-radius:6;" +
            "-fx-font-size:13.5px;-fx-padding:10 13;" +
            "-fx-text-fill:" + textDark() + ";" +
            "-fx-prompt-text-fill:" + textSubtle() + ";"
        );
        tf.setPrefHeight(40);
        return tf;
    }

    public static PasswordField styledPassword(String prompt) {
        PasswordField pf = new PasswordField();
        pf.setPromptText(prompt);
        pf.setStyle(
            "-fx-background-color:" + bgInput() + ";" +
            "-fx-border-color:" + border() + ";" +
            "-fx-border-radius:6;-fx-background-radius:6;" +
            "-fx-font-size:13.5px;-fx-padding:10 13;" +
            "-fx-text-fill:" + textDark() + ";" +
            "-fx-prompt-text-fill:" + textSubtle() + ";"
        );
        pf.setPrefHeight(40);
        return pf;
    }

    public static TextArea styledTextArea(String prompt, double prefHeight) {
        TextArea ta = new TextArea();
        ta.setPromptText(prompt);
        ta.setWrapText(true);
        ta.setPrefHeight(prefHeight);
        ta.setStyle(
            "-fx-background-color:" + bgInput() + ";" +
            "-fx-border-color:" + border() + ";" +
            "-fx-border-radius:6;-fx-background-radius:6;" +
            "-fx-font-size:13px;" +
            "-fx-text-fill:" + textDark() + ";" +
            "-fx-prompt-text-fill:" + textSubtle() + ";" +
            "-fx-control-inner-background:" + bgInput() + ";"
        );
        return ta;
    }


    private static final Color TEAL_600  = Color.web("#0f7d74");
    private static final Color TEAL_400  = Color.web("#4aada5");
    private static final Color GRN_CHK   = Color.web("#0e7a56");
    private static final Color POPUP_BG  = Color.rgb(250, 251, 252, 0.98);
    private static final Color POPUP_BDR = Color.rgb(15, 125, 116, 0.18);
    private static final Color ROW_HOV   = Color.rgb(15, 125, 116, 0.07);
    private static final Color ROW_SEL   = Color.rgb(15, 125, 116, 0.12);
    private static final Color ROW_TXT   = Color.web("#4a5568");
    private static final Color ROW_TXT_ON= Color.web("#1c2333");
    private static final CornerRadii ROW_RX = new CornerRadii(5);

    private static void glassify(javafx.scene.control.ListView<?> lv) {
        lv.setStyle("-fx-background-color:transparent;-fx-border-color:transparent;"
                + "-fx-background-radius:8;-fx-border-radius:8;");
        lv.setBackground(new Background(new BackgroundFill(POPUP_BG, new CornerRadii(8), Insets.EMPTY)));
        lv.setBorder(new Border(new BorderStroke(POPUP_BDR, BorderStrokeStyle.SOLID, new CornerRadii(8), new BorderWidths(1))));
        DropShadow ds = new DropShadow();
        ds.setColor(Color.rgb(28,35,51,0.14)); ds.setRadius(28); ds.setOffsetY(8);
        lv.setEffect(ds);
        lv.setPadding(new Insets(4));
        lv.setOpacity(0); lv.setScaleX(0.97); lv.setScaleY(0.96);
        FadeTransition ft = new FadeTransition(Duration.millis(150), lv); ft.setToValue(1);
        ScaleTransition sx = new ScaleTransition(Duration.millis(150), lv); sx.setToX(1); sx.setInterpolator(Interpolator.EASE_OUT);
        ScaleTransition sy = new ScaleTransition(Duration.millis(150), lv); sy.setToY(1); sy.setInterpolator(Interpolator.EASE_OUT);
        new ParallelTransition(ft, sx, sy).play();
    }

    private static javafx.scene.control.ListView<?> popupLv(Skin<?> skin) {
        try {
            var m = skin.getClass().getMethod("getListView");
            return (javafx.scene.control.ListView<?>) m.invoke(skin);
        } catch (Exception ignored) {}
        Class<?> c = skin.getClass();
        while (c != null) {
            try {
                var f = c.getDeclaredField("listView");
                f.setAccessible(true);
                return (javafx.scene.control.ListView<?>) f.get(skin);
            } catch (NoSuchFieldException e) { c = c.getSuperclass(); }
            catch (Exception e) { break; }
        }
        return null;
    }

    public static <T> StackPane styledCombo(String floatLabel) {
        ComboBox<T> cb = new ComboBox<>();
        cb.setPrefHeight(44);
        cb.setMaxWidth(Double.MAX_VALUE);

        final String BASE =
            "-fx-background-color:" + bgInput() + ";" +
            "-fx-border-color:transparent transparent #0f7d74 transparent;" +
            "-fx-border-width:0 0 1.5 0;-fx-border-radius:0;-fx-background-radius:0;" +
            "-fx-font-size:13.5px;-fx-padding:10 12;-fx-cursor:hand;";
        final String SELECTED =
            "-fx-background-color:" + bgInput() + ";" +
            "-fx-border-color:transparent transparent #0f7d74 transparent;" +
            "-fx-border-width:0 0 1.5 0;-fx-border-radius:0;-fx-background-radius:0;" +
            "-fx-font-size:13.5px;-fx-padding:17 12 4 12;-fx-cursor:hand;";
        final String ERROR =
            "-fx-background-color:" + bgInput() + ";" +
            "-fx-border-color:transparent transparent #c0392b transparent;" +
            "-fx-border-width:0 0 1.5 0;-fx-border-radius:0;-fx-background-radius:0;" +
            "-fx-font-size:13.5px;-fx-padding:10 12;-fx-cursor:hand;";
        cb.setStyle(BASE);

        Label lbl = new Label(floatLabel);
        lbl.setStyle("-fx-font-size:13.5px;-fx-text-fill:" + textSubtle() + ";-fx-font-weight:400;-fx-mouse-transparent:true;");
        lbl.setMouseTransparent(true);
        lbl.setTranslateX(13);

        StackPane wrapper = new StackPane(cb, lbl);
        wrapper.setAlignment(Pos.CENTER_LEFT);
        wrapper.setUserData(cb);
        wrapper.setId(BASE + "||" + SELECTED + "||" + ERROR);

        cb.valueProperty().addListener((obs, o, n) -> {
            if (n != null) {
                TranslateTransition tt = new TranslateTransition(Duration.millis(150), lbl);
                tt.setToY(-13); tt.setInterpolator(Interpolator.EASE_OUT); tt.play();
                lbl.setStyle("-fx-font-size:9.5px;-fx-font-weight:700;-fx-text-fill:#0f7d74;-fx-mouse-transparent:true;-fx-letter-spacing:0.8px;");
                cb.setStyle(SELECTED);
            } else {
                TranslateTransition tt = new TranslateTransition(Duration.millis(150), lbl);
                tt.setToY(0); tt.setInterpolator(Interpolator.EASE_OUT); tt.play();
                lbl.setStyle("-fx-font-size:13.5px;-fx-text-fill:" + textSubtle() + ";-fx-font-weight:400;-fx-mouse-transparent:true;");
                cb.setStyle(BASE);
            }
        });

        javafx.application.Platform.runLater(() -> {
            if (cb.getValue() != null) {
                lbl.setTranslateY(-13);
                lbl.setStyle("-fx-font-size:9.5px;-fx-font-weight:700;-fx-text-fill:#0f7d74;-fx-mouse-transparent:true;-fx-letter-spacing:0.8px;");
                cb.setStyle(SELECTED);
            }
        });

        cb.setCellFactory(lv -> new javafx.scene.control.ListCell<T>() {
            private final Rectangle bar    = new Rectangle(3, 20, TEAL_600);
            private final Circle    dot    = new Circle(6, GRN_CHK);
            private final Label     chkTxt = new Label("✓");
            private final StackPane chk    = new StackPane(dot, chkTxt);
            private final StackPane slot   = new StackPane();
            private final Label     rowLbl = new Label();
            private final HBox      row;
            {
                bar.setArcWidth(3); bar.setArcHeight(3); bar.setScaleY(0); bar.setOpacity(0);
                chkTxt.setStyle("-fx-font-size:9px;-fx-font-weight:bold;-fx-text-fill:white;");
                chk.setPrefSize(14,14); chk.setMaxSize(14,14);
                slot.setPrefSize(14,14); slot.setMaxSize(14,14);
                rowLbl.setStyle("-fx-font-size:13px;-fx-text-fill:" + hex(ROW_TXT) + ";");
                Region g1 = new Region(); g1.setPrefWidth(9);
                Region g2 = new Region(); g2.setPrefWidth(7);
                row = new HBox(0, bar, g1, slot, g2, rowLbl);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(8, 12, 8, 0));
                setText(null); setGraphic(row); setPadding(Insets.EMPTY);
                setBackground(Background.EMPTY); setCursor(javafx.scene.Cursor.HAND);
            }
            private void animBar(boolean show) {
                ScaleTransition st = new ScaleTransition(Duration.millis(150), bar);
                st.setToY(show ? 1 : 0); st.setInterpolator(Interpolator.EASE_OUT);
                FadeTransition ft = new FadeTransition(Duration.millis(150), bar);
                ft.setToValue(show ? 1 : 0);
                new ParallelTransition(st, ft).play();
            }
            private void paint(boolean sel, boolean hov) {
                Color bg = sel?ROW_SEL:hov?ROW_HOV:Color.TRANSPARENT;
                setBackground(new Background(new BackgroundFill(bg, ROW_RX, Insets.EMPTY)));
                Color tc = (sel||hov)?ROW_TXT_ON:ROW_TXT;
                rowLbl.setStyle("-fx-font-size:13px;-fx-text-fill:"+hex(tc)+";-fx-font-weight:"+(sel?"600":"400")+";");
                bar.setFill(sel?TEAL_600:TEAL_400);
                slot.getChildren().setAll(sel?chk:new Region());
            }
            @Override protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(null);
                if (empty||item==null) { setGraphic(null); setBackground(Background.EMPTY); return; }
                rowLbl.setText(item.toString());
                setGraphic(row);
                paint(isSelected(), false);
                bar.setScaleY(isSelected()?1:0); bar.setOpacity(isSelected()?1:0);
            }
            { setOnMouseEntered(e->{paint(isSelected(),true); if(!isSelected()) animBar(true);});
              setOnMouseExited(e ->{paint(isSelected(),false);if(!isSelected()) animBar(false);});
              selectedProperty().addListener((ob,was,now)->{paint(now,false); animBar(now);}); }
        });

        Runnable wire = () -> {
            Skin<?> skin = cb.getSkin();
            if (skin==null) return;
            var popupList = popupLv(skin);
            if (popupList!=null) glassify(popupList);
        };
        cb.skinProperty().addListener((obs,o,n)-> { if(n!=null) javafx.application.Platform.runLater(wire); });
        cb.setOnShowing(e -> javafx.application.Platform.runLater(wire));
        return wrapper;
    }

    public static <T> StackPane styledCombo(String floatLabel, String ignored) {
        return styledCombo(floatLabel);
    }

    @SuppressWarnings("unchecked")
    public static <T> ComboBox<T> getCombo(StackPane wrapper) {
        return (ComboBox<T>) wrapper.getUserData();
    }

    public static void comboError(StackPane wrapper) {
        ComboBox<?> cb = getCombo(wrapper);
        String[] styles = wrapper.getId().split("\\|\\|");
        cb.setStyle(styles[2]);
        Label lbl = (Label) wrapper.getChildren().get(1);
        if (cb.getValue()==null)
            lbl.setStyle("-fx-font-size:13.5px;-fx-text-fill:#c0392b;-fx-font-weight:400;-fx-mouse-transparent:true;");
    }

    public static void comboClear(StackPane wrapper) {
        ComboBox<?> cb = getCombo(wrapper);
        String[] styles = wrapper.getId().split("\\|\\|");
        Label lbl = (Label) wrapper.getChildren().get(1);
        if (cb.getValue()!=null) {
            cb.setStyle(styles[1]);
            lbl.setStyle("-fx-font-size:9.5px;-fx-font-weight:700;-fx-text-fill:#0f7d74;-fx-mouse-transparent:true;-fx-letter-spacing:0.8px;");
        } else {
            cb.setStyle(styles[0]);
            lbl.setStyle("-fx-font-size:13.5px;-fx-text-fill:" + textSubtle() + ";-fx-font-weight:400;-fx-mouse-transparent:true;");
        }
    }

    public static MenuItem modernMenuItem(String svgPath, String label, String chipColor, boolean isDanger) {
        Color accent = Color.web(chipColor);
        int cr=(int)(accent.getRed()*255), cg=(int)(accent.getGreen()*255), cb2=(int)(accent.getBlue()*255);

        if (svgPath.length() <= 2 || svgPath.codePointCount(0, svgPath.length()) <= 2)
            svgPath = emojiToSvg(svgPath);

        Rectangle bar = new Rectangle(3, 18);
        bar.setArcWidth(3); bar.setArcHeight(3);
        bar.setFill(Color.web(chipColor));
        bar.setScaleY(0); bar.setOpacity(0); bar.setVisible(false);
        StackPane barWrap = new StackPane(bar); barWrap.setPrefWidth(4); barWrap.setPrefHeight(24);

        Region chipIco = icon(svgPath, chipColor, 13);
        StackPane chip = new StackPane(chipIco);
        chip.setPrefSize(24,24); chip.setMinSize(24,24); chip.setMaxSize(24,24);
        chip.setStyle(String.format("-fx-background-radius:5;-fx-background-color:rgba(%d,%d,%d,0.10);",cr,cg,cb2));

        Label textLbl = new Label(label);
        final String tRest = isDanger
            ? "-fx-font-size:13px;-fx-font-weight:400;-fx-text-fill:#c0392b;"
            : "-fx-font-size:13px;-fx-font-weight:400;-fx-text-fill:#2d3a50;";
        final String tHov = isDanger
            ? "-fx-font-size:13px;-fx-font-weight:600;-fx-text-fill:#c0392b;"
            : "-fx-font-size:13px;-fx-font-weight:600;-fx-text-fill:" + chipColor + ";";
        textLbl.setStyle(tRest);

        Region spacer = new Region(); spacer.setPrefWidth(7);
        HBox row = new HBox(0, barWrap, spacer, chip, new Region(){{setPrefWidth(9);}}, textLbl);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setMaxWidth(Double.MAX_VALUE);
        row.setPadding(new Insets(7, 12, 7, 0));

        final CornerRadii RX = new CornerRadii(5);
        final Background BG_REST  = Background.EMPTY;
        final Background BG_HOVER = isDanger
            ? new Background(new BackgroundFill(Color.rgb(192,57,43,0.07), RX, Insets.EMPTY))
            : new Background(new BackgroundFill(Color.rgb(28,35,51,0.04), RX, Insets.EMPTY));

        DropShadow glow = new DropShadow();
        glow.setColor(Color.web(chipColor, 0.0)); glow.setRadius(7);
        chip.setEffect(glow);

        final String finalSvgPath = svgPath;
        row.setOnMouseEntered(e -> {
            row.setBackground(BG_HOVER);
            chip.setStyle(String.format("-fx-background-radius:5;-fx-background-color:rgba(%d,%d,%d,0.18);",cr,cg,cb2));
            textLbl.setStyle(tHov);
            bar.setVisible(true); bar.setOpacity(1);
            ScaleTransition stIn = new ScaleTransition(Duration.millis(150), bar);
            stIn.setToY(1); stIn.setInterpolator(Interpolator.EASE_OUT); stIn.play();
            TranslateTransition ttIn = new TranslateTransition(Duration.millis(130), textLbl);
            ttIn.setToX(2); ttIn.play();
            new Timeline(new KeyFrame(Duration.millis(150), new KeyValue(glow.colorProperty(), Color.web(chipColor,0.38)))).play();
        });
        row.setOnMouseExited(e -> {
            row.setBackground(BG_REST);
            chip.setStyle(String.format("-fx-background-radius:5;-fx-background-color:rgba(%d,%d,%d,0.10);",cr,cg,cb2));
            textLbl.setStyle(tRest);
            ScaleTransition sg = new ScaleTransition(Duration.millis(120), bar); sg.setToY(0); sg.setInterpolator(Interpolator.EASE_IN);
            sg.setOnFinished(ev->{bar.setOpacity(0); bar.setVisible(false);}); sg.play();
            TranslateTransition ttOut = new TranslateTransition(Duration.millis(110), textLbl);
            ttOut.setToX(0); ttOut.play();
            new Timeline(new KeyFrame(Duration.millis(120), new KeyValue(glow.colorProperty(), Color.web(chipColor,0.0)))).play();
        });

        CustomMenuItem item = new CustomMenuItem(row, true);
        item.setStyle("-fx-padding:0;");
        if (isDanger) item.getStyleClass().add("danger");
        return item;
    }

    private static String emojiToSvg(String emoji) {
        return switch (emoji.trim()) {
            case "✏️","✏" -> ICO_EDIT;
            case "🗑️","🗑" -> ICO_DELETE;
            case "📋"      -> ICO_EXAM;
            case "📡"      -> ICO_LIVE;
            case "⏹️","⏹" -> ICO_STOP;
            case "📅"      -> ICO_SCHEDULE;
            default        -> ICO_INFO;
        };
    }


    public static Button primaryBtn(String icon, String text, String color) {
        Button b = new Button(text);
        final String NORMAL =
            "-fx-background-color:" + color + ";" +
            "-fx-text-fill:white;" +
            "-fx-font-weight:600;-fx-font-size:13px;" +
            "-fx-background-radius:6;-fx-padding:9 18;" +
            "-fx-border-color:transparent;-fx-cursor:hand;" +
            "-fx-letter-spacing:0.1px;";
        final String HOVER =
            "-fx-background-color:derive(" + color + ",-8%);" +
            "-fx-text-fill:white;" +
            "-fx-font-weight:600;-fx-font-size:13px;" +
            "-fx-background-radius:6;-fx-padding:9 18;" +
            "-fx-border-color:transparent;-fx-cursor:hand;";
        b.setStyle(NORMAL);
        DropShadow ds = new DropShadow();
        ds.setColor(Color.web(color, 0.25)); ds.setOffsetY(2); ds.setRadius(8);
        b.setEffect(ds);
        b.setOnMouseEntered(e  -> { b.setStyle(HOVER);  b.setTranslateY(-1); ds.setOffsetY(4); ds.setRadius(12); });
        b.setOnMouseExited(e   -> { b.setStyle(NORMAL); b.setTranslateY(0);  ds.setOffsetY(2); ds.setRadius(8); });
        b.setOnMousePressed(e  -> { b.setTranslateY(1); ds.setOffsetY(1); });
        b.setOnMouseReleased(e -> { b.setTranslateY(-1); ds.setOffsetY(4); });
        return b;
    }

    public static Button ghostBtn(String icon, String text, String color) {
        Button b = new Button(text);
        final String NORMAL =
            "-fx-background-color:transparent;" +
            "-fx-text-fill:" + color + ";" +
            "-fx-font-weight:500;-fx-font-size:13px;" +
            "-fx-border-color:derive(" + color + ",+20%);" +
            "-fx-border-radius:6;-fx-background-radius:6;" +
            "-fx-padding:9 18;-fx-cursor:hand;-fx-border-width:1;";
        final String HOVER =
            "-fx-background-color:derive(" + color + ",+82%);" +
            "-fx-text-fill:" + color + ";" +
            "-fx-font-weight:500;-fx-font-size:13px;" +
            "-fx-border-color:derive(" + color + ",+10%);" +
            "-fx-border-radius:6;-fx-background-radius:6;" +
            "-fx-padding:9 18;-fx-cursor:hand;-fx-border-width:1;";
        b.setStyle(NORMAL);
        b.setOnMouseEntered(e  -> b.setStyle(HOVER));
        b.setOnMouseExited(e   -> b.setStyle(NORMAL));
        b.setOnMousePressed(e  -> b.setTranslateY(1));
        b.setOnMouseReleased(e -> b.setTranslateY(0));
        return b;
    }

    public static StackPane modernSidebarBtn(String svgOrEmoji, String text, String accentColor) {
        String svgPath = (svgOrEmoji.length() <= 2 || svgOrEmoji.codePointCount(0, svgOrEmoji.length()) <= 2)
            ? emojiToSvgNav(svgOrEmoji) : svgOrEmoji;

        Rectangle glass = new Rectangle(210, 40);
        glass.setArcWidth(7); glass.setArcHeight(7);
        glass.setFill(Color.web("#ffffff", 0.06)); glass.setOpacity(0);

        Rectangle pill = new Rectangle(3, 20);
        pill.setArcWidth(3); pill.setArcHeight(3);
        pill.setFill(Color.web(accentColor)); pill.setOpacity(0);
        StackPane pillWrap = new StackPane(pill); pillWrap.setPrefSize(3,40); pillWrap.setAlignment(Pos.CENTER);

        Rectangle iconBg = new Rectangle(26, 26);
        iconBg.setArcWidth(6); iconBg.setArcHeight(6);
        iconBg.setFill(Color.web("#ffffff", 0.05));
        Region iconNode = icon(svgPath, "#6b7b96", 13);
        StackPane iconBox = new StackPane(iconBg, iconNode);
        iconBox.setPrefSize(26,26);

        Label textLbl = new Label(text);
        textLbl.setStyle("-fx-font-size:13px;-fx-font-weight:500;-fx-text-fill:#6b7b96;-fx-letter-spacing:0.1px;");

        HBox row = new HBox(9, pillWrap, iconBox, textLbl);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(0, 10, 0, 6));

        StackPane wrapper = new StackPane(glass, row);
        wrapper.setPrefSize(210, 40);
        wrapper.setAlignment(Pos.CENTER_LEFT);
        wrapper.setCursor(javafx.scene.Cursor.HAND);
        wrapper.setId(accentColor);

        wrapper.setOnMouseEntered(e -> {
            if (glass.getOpacity() < 0.5) {
                FadeTransition ft = new FadeTransition(Duration.millis(130), glass); ft.setToValue(0.38); ft.play();
                textLbl.setStyle("-fx-font-size:13px;-fx-font-weight:500;-fx-text-fill:#b8c2d4;");
            }
        });
        wrapper.setOnMouseExited(e -> {
            if (glass.getOpacity() < 0.8) {
                FadeTransition ft = new FadeTransition(Duration.millis(130), glass); ft.setToValue(0); ft.play();
                textLbl.setStyle("-fx-font-size:13px;-fx-font-weight:500;-fx-text-fill:#6b7b96;");
            }
        });
        wrapper.setOnMousePressed(e  -> wrapper.setScaleX(0.97));
        wrapper.setOnMouseReleased(e -> wrapper.setScaleX(1.0));
        return wrapper;
    }

    private static String emojiToSvgNav(String emoji) {
        return switch (emoji.trim()) {
            case "🏠","🏡" -> ICO_DASHBOARD;
            case "📝"      -> ICO_EXAM;
            case "➕"      -> ICO_PLUS;
            case "📚"      -> ICO_BANK;
            case "📂"      -> ICO_HISTORY;
            case "🏆"      -> ICO_TROPHY;
            case "📢"      -> ICO_ANNOUNCE;
            case "🚪"      -> ICO_LOGOUT;
            case "🎯"      -> "M8 2a6 6 0 100 12A6 6 0 008 2zm0 3a3 3 0 100 6 3 3 0 000-6zm0 2a1 1 0 100 2 1 1 0 000-2z";
            case "📊"      -> ICO_ANALYTICS;
            case "📡"      -> ICO_LIVE;
            case "📅"      -> ICO_SCHEDULE;
            case "📈"      -> ICO_CHART;
            case "🔑"      -> ICO_KEY;
            default        -> ICO_MENU;
        };
    }

    public static void modernSidebarSetActive(StackPane wrapper) {
        String color = wrapper.getId();
        Rectangle glass  = (Rectangle) wrapper.getChildren().get(0);
        HBox      row    = (HBox)      wrapper.getChildren().get(1);
        StackPane pillW  = (StackPane) row.getChildren().get(0);
        StackPane iconBox= (StackPane) row.getChildren().get(1);
        Label     textLbl= (Label)     row.getChildren().get(2);
        Rectangle pill   = (Rectangle) pillW.getChildren().get(0);
        Rectangle iconBg = (Rectangle) iconBox.getChildren().get(0);
        Region    iconN  = (Region)    iconBox.getChildren().get(1);

        FadeTransition glassIn = new FadeTransition(Duration.millis(180), glass); glassIn.setToValue(1); glassIn.play();
        pill.setScaleY(0);
        FadeTransition pillFadeIn = new FadeTransition(Duration.millis(180), pill); pillFadeIn.setToValue(1); pillFadeIn.play();
        ScaleTransition pillScaleIn = new ScaleTransition(Duration.millis(180), pill);
        pillScaleIn.setToY(1); pillScaleIn.setInterpolator(Interpolator.EASE_OUT); pillScaleIn.play();
        iconBg.setFill(Color.web(color, 0.16));
        String cur = iconN.getStyle();
        iconN.setStyle(cur.replaceAll("-fx-background-color:[^;]+;", "-fx-background-color:" + color + ";"));
        textLbl.setStyle("-fx-font-size:13px;-fx-font-weight:700;-fx-text-fill:" + color + ";-fx-letter-spacing:0.1px;");

        wrapper.setOnMouseEntered(null);
        wrapper.setOnMouseExited(null);
    }

    public static void modernSidebarSetInactive(StackPane wrapper) {
        String color = wrapper.getId();
        Rectangle glass  = (Rectangle) wrapper.getChildren().get(0);
        HBox      row    = (HBox)      wrapper.getChildren().get(1);
        StackPane pillW  = (StackPane) row.getChildren().get(0);
        StackPane iconBox= (StackPane) row.getChildren().get(1);
        Label     textLbl= (Label)     row.getChildren().get(2);
        Rectangle pill   = (Rectangle) pillW.getChildren().get(0);
        Rectangle iconBg = (Rectangle) iconBox.getChildren().get(0);
        Region    iconN  = (Region)    iconBox.getChildren().get(1);

        FadeTransition glassOut = new FadeTransition(Duration.millis(140), glass); glassOut.setToValue(0); glassOut.play();
        FadeTransition pillOut  = new FadeTransition(Duration.millis(140), pill);  pillOut.setToValue(0);  pillOut.play();
        iconBg.setFill(Color.web("#ffffff", 0.05));
        iconN.setStyle(iconN.getStyle().replaceAll("-fx-background-color:[^;]+;", "-fx-background-color:#6b7b96;"));
        textLbl.setStyle("-fx-font-size:13px;-fx-font-weight:500;-fx-text-fill:#6b7b96;-fx-letter-spacing:0.1px;");

        wrapper.setOnMouseEntered(e -> {
            if (glass.getOpacity() < 0.5) {
                FadeTransition ft = new FadeTransition(Duration.millis(130), glass); ft.setToValue(0.38); ft.play();
                textLbl.setStyle("-fx-font-size:13px;-fx-font-weight:500;-fx-text-fill:#b8c2d4;");
            }
        });
        wrapper.setOnMouseExited(e -> {
            if (glass.getOpacity() < 0.8) {
                FadeTransition ft = new FadeTransition(Duration.millis(130), glass); ft.setToValue(0); ft.play();
                textLbl.setStyle("-fx-font-size:13px;-fx-font-weight:500;-fx-text-fill:#6b7b96;");
            }
        });
    }

    public static void sidebarBtn(Button b, String icon, String label, String color) {
        b.setText(label);
        String inactive = "-fx-background-color:transparent;-fx-text-fill:#6b7b96;-fx-font-size:13px;-fx-font-weight:500;-fx-background-radius:6;-fx-padding:9 12 9 14;-fx-cursor:hand;-fx-alignment:center-left;";
        String hov = "-fx-background-color:" + color + "18;-fx-text-fill:" + color + ";-fx-font-size:13px;-fx-font-weight:600;-fx-background-radius:6;-fx-padding:9 12 9 14;-fx-cursor:hand;-fx-alignment:center-left;";
        b.setStyle(inactive);
        b.setOnMouseEntered(e -> b.setStyle(hov));
        b.setOnMouseExited(e  -> b.setStyle(inactive));
    }

    public static Button sidebarBtn(String icon, String label, String color) {
        Button b = new Button(label);
        b.setMaxWidth(Double.MAX_VALUE);
        b.setAlignment(Pos.CENTER_LEFT);
        sidebarBtn(b, icon, label, color);
        return b;
    }

    public static Separator divider() {
        Separator s = new Separator();
        s.setStyle("-fx-background-color:" + border() + ";-fx-opacity:0.75;");
        return s;
    }

    public static void slideIn(Node node, boolean fromRight) {
        double from = fromRight ? 28 : -28;
        node.setTranslateX(from); node.setOpacity(0);
        TranslateTransition tt = new TranslateTransition(Duration.millis(260), node);
        tt.setFromX(from); tt.setToX(0); tt.setInterpolator(Interpolator.EASE_OUT);
        FadeTransition ft = new FadeTransition(Duration.millis(260), node);
        ft.setFromValue(0); ft.setToValue(1);
        new ParallelTransition(tt, ft).play();
    }

    public static void playTransition(Pane root, boolean fwd) { slideIn(root, fwd); }

    public static void applyLinkEffects(Hyperlink link) {
        link.setStyle("-fx-text-fill:#0f7d74;-fx-font-size:13px;-fx-underline:false;-fx-font-weight:500;-fx-border-color:transparent;");
        link.setOnMouseEntered(e -> link.setStyle("-fx-text-fill:#0a5f58;-fx-underline:true;-fx-font-size:13px;-fx-font-weight:500;-fx-border-color:transparent;"));
        link.setOnMouseExited(e  -> link.setStyle("-fx-text-fill:#0f7d74;-fx-underline:false;-fx-font-size:13px;-fx-font-weight:500;-fx-border-color:transparent;"));
    }

    private static final String CSS = "/org/example/demo/style.css";
    public static void applyStyle(Scene scene) {
        if (UIUtils.class.getResource(CSS) != null)
            scene.getStylesheets().add(UIUtils.class.getResource(CSS).toExternalForm());
    }

    public enum ToastType { SUCCESS, ERROR, INFO, WARN }

    public static void toast(Pane parent, String msg, ToastType type) {
        javafx.application.Platform.runLater(() -> {
            String chipBg, chipColor, borderCol, svgPath;
            switch (type) {
                case SUCCESS -> { chipBg="#d1f0e8"; chipColor="#0e7a56"; borderCol="rgba(14,122,86,0.28)";  svgPath=ICO_CHECK; }
                case ERROR   -> { chipBg="#fde8e8"; chipColor="#c0392b"; borderCol="rgba(192,57,43,0.28)";  svgPath=ICO_CLOSE; }
                case WARN    -> { chipBg="#fef3c7"; chipColor="#b45309"; borderCol="rgba(180,83,9,0.28)";   svgPath=ICO_WARN;  }
                default      -> { chipBg="#d5eeed"; chipColor="#0f7d74"; borderCol="rgba(15,125,116,0.28)"; svgPath=ICO_INFO;  }
            }

            Region iconR = icon(svgPath, chipColor, 12);
            StackPane iconChip = new StackPane(iconR);
            iconChip.setPrefSize(22,22);
            iconChip.setStyle("-fx-background-color:" + chipBg + ";-fx-background-radius:99;");

            Label msgL = new Label(msg);
            msgL.setStyle("-fx-font-size:13px;-fx-font-weight:600;-fx-text-fill:#1c2333;-fx-letter-spacing:0.1px;");

            HBox pill = new HBox(9, iconChip, msgL);
            pill.setAlignment(Pos.CENTER_LEFT);
            pill.setPadding(new Insets(9,16,9,12));
            pill.setStyle(
                "-fx-background-color:rgba(255,255,255,0.97);" +
                "-fx-background-radius:99;" +
                "-fx-border-color:" + borderCol + ";" +
                "-fx-border-radius:99;-fx-border-width:1;"
            );
            DropShadow ds = new DropShadow();
            ds.setColor(Color.rgb(28,35,51,0.13)); ds.setRadius(18); ds.setOffsetY(5);
            pill.setEffect(ds);
            pill.setId("toast"); pill.setOpacity(0); pill.setTranslateY(-32);

            long existing = parent.getChildren().stream()
                .filter(n -> n instanceof HBox && "toast".equals(n.getId())).count();
            parent.getChildren().add(pill);

            Runnable center = () -> {
                double pw = pill.getWidth() > 0 ? pill.getWidth() : 280;
                pill.setLayoutX((parent.getWidth() - pw) / 2.0);
                pill.setLayoutY(14 + existing * 50);
            };
            javafx.application.Platform.runLater(center);
            parent.widthProperty().addListener((ob,ov,nv)->center.run());
            pill.widthProperty().addListener((ob,ov,nv)->center.run());

            FadeTransition fi = new FadeTransition(Duration.millis(190), pill); fi.setToValue(1);
            TranslateTransition ti = new TranslateTransition(Duration.millis(190), pill);
            ti.setToY(0); ti.setInterpolator(Interpolator.EASE_OUT);
            new ParallelTransition(fi,ti).play();

            PauseTransition pause = new PauseTransition(Duration.seconds(3));
            pause.setOnFinished(ev -> {
                FadeTransition fo = new FadeTransition(Duration.millis(200), pill); fo.setToValue(0);
                TranslateTransition to2 = new TranslateTransition(Duration.millis(200), pill);
                to2.setToY(-24); to2.setInterpolator(Interpolator.EASE_IN);
                ParallelTransition out = new ParallelTransition(fo,to2);
                out.setOnFinished(e2 -> parent.getChildren().remove(pill));
                out.play();
            });
            pause.play();
        });
    }

    public static class Toast {
        public static void success(Pane p, String msg) { toast(p, msg, ToastType.SUCCESS); }
        public static void error(Pane p, String msg)   { toast(p, msg, ToastType.ERROR);   }
        public static void info(Pane p, String msg)    { toast(p, msg, ToastType.INFO);    }
        public static void warn(Pane p, String msg)    { toast(p, msg, ToastType.WARN);    }
    }

    public static void bottomToast(Pane parent, String msg) {
        javafx.application.Platform.runLater(() -> {
            Region iconR = icon(ICO_CHECK, "#0e7a56", 11);
            StackPane iconChip = new StackPane(iconR);
            iconChip.setPrefSize(20, 20);
            iconChip.setStyle("-fx-background-color:#d1f0e8;-fx-background-radius:99;");

            Label msgL = new Label(msg);
            msgL.setStyle("-fx-font-size:12px;-fx-font-weight:600;-fx-text-fill:#1c2333;-fx-letter-spacing:0.1px;");

            HBox pill = new HBox(8, iconChip, msgL);
            pill.setAlignment(Pos.CENTER_LEFT);
            pill.setPadding(new Insets(7, 14, 7, 10));
            pill.setStyle(
                "-fx-background-color:rgba(255,255,255,0.97);" +
                "-fx-background-radius:99;" +
                "-fx-border-color:rgba(14,122,86,0.30);" +
                "-fx-border-radius:99;-fx-border-width:1;"
            );
            DropShadow ds = new DropShadow();
            ds.setColor(Color.rgb(14, 122, 86, 0.18)); ds.setRadius(14); ds.setOffsetY(-3);
            pill.setEffect(ds);
            pill.setId("bottom-toast");
            pill.setOpacity(0);

            parent.getChildren().removeIf(n -> n instanceof HBox && "bottom-toast".equals(n.getId()));
            parent.getChildren().add(pill);

            Runnable position = () -> {
                double pw = pill.getWidth() > 0 ? pill.getWidth() : 240;
                double ph = pill.getHeight() > 0 ? pill.getHeight() : 36;
                pill.setLayoutX((parent.getWidth() - pw) / 2.0);
                pill.setLayoutY(parent.getHeight() - ph - 24);
                pill.setTranslateY(20);
            };
            javafx.application.Platform.runLater(position);
            parent.widthProperty().addListener((ob,ov,nv) -> position.run());
            parent.heightProperty().addListener((ob,ov,nv) -> position.run());
            pill.widthProperty().addListener((ob,ov,nv) -> position.run());

            FadeTransition fi = new FadeTransition(Duration.millis(180), pill); fi.setToValue(1);
            TranslateTransition ti = new TranslateTransition(Duration.millis(180), pill);
            ti.setToY(0); ti.setInterpolator(Interpolator.EASE_OUT);
            new ParallelTransition(fi, ti).play();

            PauseTransition pause = new PauseTransition(Duration.seconds(2));
            pause.setOnFinished(ev -> {
                FadeTransition fo = new FadeTransition(Duration.millis(180), pill); fo.setToValue(0);
                TranslateTransition to2 = new TranslateTransition(Duration.millis(180), pill);
                to2.setToY(16); to2.setInterpolator(Interpolator.EASE_IN);
                ParallelTransition out = new ParallelTransition(fo, to2);
                out.setOnFinished(e2 -> parent.getChildren().remove(pill));
                out.play();
            });
            pause.play();
        });
    }

    public static void applyButtonEffects(Button btn, String hoverColor) {
        String base  = "-fx-background-color:" + bgSurface() + ";-fx-text-fill:" + textDark() + ";-fx-font-weight:600;-fx-font-size:13px;-fx-background-radius:6;";
        String hover = "-fx-background-color:" + hoverColor + ";-fx-text-fill:white;-fx-font-weight:600;-fx-font-size:13px;-fx-background-radius:6;";
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e  -> btn.setStyle(base));
    }
}
