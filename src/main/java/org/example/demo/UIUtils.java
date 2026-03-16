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
        Label ico = new Label(icon); ico.setStyle("-fx-font-size:22px;-fx-text-fill:" + textDark() + ";");
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

    // ═══════════════════════════════════════════════════════════
    //  MODERN COMBOBOX — B×C fusion
    //  Trigger : underline-only (Style C / Material)
    //  Popup   : frosted glass card — background set via Java
    //            API (Color.rgb with alpha) because JavaFX CSS
    //            does NOT support rgba(). CSS transparent so
    //            Java Background wins unconditionally.
    //  Rows    : animated 3px left accent bar (ScaleTransition)
    //            + text scale-up on selected (Style C effects)
    // ═══════════════════════════════════════════════════════════

    // ── Popup / row colour constants ─────────────────────────
    private static final Color   IND_600    = Color.web("#6366f1");
    private static final Color   IND_400    = Color.web("#818cf8");
    private static final Color   GREEN_CHK  = Color.web("#10b981");
    private static final Color   POPUP_BG   = Color.rgb(248, 249, 255, 0.96);
    private static final Color   POPUP_BDR  = Color.rgb( 99, 102, 241, 0.22);
    private static final Color   ROW_HOV_BG = Color.rgb( 99, 102, 241, 0.08);
    private static final Color   ROW_SEL_BG = Color.rgb( 99, 102, 241, 0.13);
    private static final Color   ROW_TXT    = Color.web("#475569");
    private static final Color   ROW_TXT_ON = Color.web("#1e1b4b");
    private static final javafx.scene.layout.CornerRadii ROW_RX
            = new javafx.scene.layout.CornerRadii(9);

    private static String hex(Color c) {
        return String.format("#%02x%02x%02x",
                (int)(c.getRed()*255),(int)(c.getGreen()*255),(int)(c.getBlue()*255));
    }

    /** Apply glass card to popup ListView. Sets transparent CSS first so Java Background wins. */
    private static void glassify(javafx.scene.control.ListView<?> lv) {
        lv.setStyle("-fx-background-color:transparent;-fx-border-color:transparent;"
                + "-fx-background-radius:14;-fx-border-radius:14;");
        lv.setBackground(new Background(new BackgroundFill(
                POPUP_BG, new javafx.scene.layout.CornerRadii(14), Insets.EMPTY)));
        lv.setBorder(new javafx.scene.layout.Border(new javafx.scene.layout.BorderStroke(
                POPUP_BDR, javafx.scene.layout.BorderStrokeStyle.SOLID,
                new javafx.scene.layout.CornerRadii(14),
                new javafx.scene.layout.BorderWidths(0.8))));
        DropShadow ds = new DropShadow();
        ds.setColor(Color.rgb(80, 70, 180, 0.18));
        ds.setRadius(44); ds.setOffsetY(14); ds.setSpread(0.02);
        lv.setEffect(ds);
        lv.setPadding(new Insets(5));
        // open animation
        lv.setOpacity(0); lv.setScaleX(0.97); lv.setScaleY(0.95);
        FadeTransition ft = new FadeTransition(Duration.millis(180), lv); ft.setToValue(1);
        ScaleTransition sx = new ScaleTransition(Duration.millis(180), lv);
        sx.setToX(1); sx.setInterpolator(Interpolator.EASE_OUT);
        ScaleTransition sy = new ScaleTransition(Duration.millis(180), lv);
        sy.setToY(1); sy.setInterpolator(Interpolator.EASE_OUT);
        new ParallelTransition(ft, sx, sy).play();
    }

    /** Get popup ListView from skin — works JavaFX 17+ without cast to internal class. */
    private static javafx.scene.control.ListView<?> popupLv(Skin<?> skin) {
        try { // JavaFX 20+ public API
            var m = skin.getClass().getMethod("getListView");
            return (javafx.scene.control.ListView<?>) m.invoke(skin);
        } catch (Exception ignored) {}
        Class<?> c = skin.getClass();  // JavaFX 17-19 field reflection
        while (c != null) {
            try {
                var f = c.getDeclaredField("listView");
                f.setAccessible(true);
                return (javafx.scene.control.ListView<?>) f.get(skin);
            } catch (NoSuchFieldException e) { c = c.getSuperclass(); }
            catch (Exception e)            { break; }
        }
        return null;
    }

    public static <T> StackPane styledCombo(String floatLabel) {
        ComboBox<T> cb = new ComboBox<>();
        cb.setPrefHeight(48);
        cb.setMaxWidth(Double.MAX_VALUE);

        final String BASE_STYLE =
                "-fx-background-color:" + bgSurface() + ";" +
                        "-fx-border-color:transparent transparent #6366f1 transparent;" +
                        "-fx-border-width:0 0 2 0;-fx-border-radius:0;-fx-background-radius:0;" +
                        "-fx-font-size:14px;-fx-padding:10 12 10 12;-fx-cursor:hand;";
        final String SELECTED_STYLE =
                "-fx-background-color:" + bgSurface() + ";" +
                        "-fx-border-color:transparent transparent #6366f1 transparent;" +
                        "-fx-border-width:0 0 2 0;-fx-border-radius:0;-fx-background-radius:0;" +
                        "-fx-font-size:14px;-fx-padding:18 12 4 12;-fx-cursor:hand;";
        final String ERROR_STYLE =
                "-fx-background-color:" + bgSurface() + ";" +
                        "-fx-border-color:transparent transparent #ef4444 transparent;" +
                        "-fx-border-width:0 0 2 0;-fx-border-radius:0;-fx-background-radius:0;" +
                        "-fx-font-size:14px;-fx-padding:10 12 10 12;-fx-cursor:hand;";
        cb.setStyle(BASE_STYLE);

        Label lbl = new Label(floatLabel);
        lbl.setStyle("-fx-font-size:14px;-fx-text-fill:" + textSubtle()
                + ";-fx-font-weight:normal;-fx-mouse-transparent:true;");
        lbl.setMouseTransparent(true);
        lbl.setTranslateX(14);

        StackPane wrapper = new StackPane(cb, lbl);
        wrapper.setAlignment(Pos.CENTER_LEFT);
        wrapper.setUserData(cb);
        wrapper.setId(BASE_STYLE + "||" + SELECTED_STYLE + "||" + ERROR_STYLE);

        cb.valueProperty().addListener((obs, o, n) -> {
            if (n != null) {
                TranslateTransition tt = new TranslateTransition(Duration.millis(170), lbl);
                tt.setToY(-14); tt.setInterpolator(Interpolator.EASE_OUT); tt.play();
                lbl.setStyle("-fx-font-size:10px;-fx-font-weight:bold;"
                        + "-fx-text-fill:#6366f1;-fx-mouse-transparent:true;");
                cb.setStyle(SELECTED_STYLE);
            } else {
                TranslateTransition tt = new TranslateTransition(Duration.millis(170), lbl);
                tt.setToY(0); tt.setInterpolator(Interpolator.EASE_OUT); tt.play();
                lbl.setStyle("-fx-font-size:14px;-fx-text-fill:" + textSubtle()
                        + ";-fx-font-weight:normal;-fx-mouse-transparent:true;");
                cb.setStyle(BASE_STYLE);
            }
        });

        cb.setOnMouseEntered(e -> { if (cb.getValue()==null) cb.setStyle(BASE_STYLE.replace("#6366f1","#818cf8")); });
        cb.setOnMouseExited(e  -> { if (cb.getValue()==null) cb.setStyle(BASE_STYLE); });

        javafx.application.Platform.runLater(() -> {
            if (cb.getValue() != null) {
                lbl.setTranslateY(-14);
                lbl.setStyle("-fx-font-size:10px;-fx-font-weight:bold;"
                        + "-fx-text-fill:#6366f1;-fx-mouse-transparent:true;");
                cb.setStyle(SELECTED_STYLE);
            }
        });

        // ── CellFactory — Style C: left bar + text scale ─────
        // RULES: setText(null) always. Graphic = HBox row.
        // setBackground() via Java API — no setStyle() on cell body.
        cb.setCellFactory(lv -> new javafx.scene.control.ListCell<T>() {

            private final Rectangle bar    = new Rectangle(3, 26, IND_600);
            private final Circle    dot    = new Circle(7, GREEN_CHK);
            private final Label     chkTxt = new Label("✓");
            private final StackPane chk    = new StackPane(dot, chkTxt);
            private final StackPane slot   = new StackPane();
            private final Label     rowLbl = new Label();
            private final HBox      row;

            {
                bar.setArcWidth(3); bar.setArcHeight(3);
                bar.setScaleY(0);   bar.setOpacity(0);
                chkTxt.setStyle("-fx-font-size:9px;-fx-font-weight:bold;-fx-text-fill:white;");
                chk.setPrefSize(16,16);  chk.setMaxSize(16,16);
                slot.setPrefSize(16,16); slot.setMaxSize(16,16);
                rowLbl.setStyle("-fx-font-size:13.5px;-fx-text-fill:" + hex(ROW_TXT) + ";");
                Region g1 = new Region(); g1.setPrefWidth(10);
                Region g2 = new Region(); g2.setPrefWidth(8);
                row = new HBox(0, bar, g1, slot, g2, rowLbl);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(9, 14, 9, 0));
                setText(null);
                setGraphic(row);
                setPadding(Insets.EMPTY);
                setBackground(Background.EMPTY);
                setCursor(javafx.scene.Cursor.HAND);
            }

            private void animBar(boolean show) {
                ScaleTransition st = new ScaleTransition(Duration.millis(180), bar);
                st.setToY(show ? 1.0 : 0.0); st.setInterpolator(Interpolator.EASE_OUT);
                FadeTransition ft = new FadeTransition(Duration.millis(180), bar);
                ft.setToValue(show ? 1.0 : 0.0);
                new ParallelTransition(st, ft).play();
            }

            private void animText(boolean sel) {
                ScaleTransition st = new ScaleTransition(Duration.millis(160), rowLbl);
                st.setToX(sel ? 1.04 : 1.0); st.setToY(sel ? 1.04 : 1.0);
                st.setInterpolator(Interpolator.EASE_OUT); st.play();
            }

            private void paint(boolean sel, boolean hov) {
                Color bg = sel ? ROW_SEL_BG : hov ? ROW_HOV_BG : Color.TRANSPARENT;
                setBackground(new Background(new BackgroundFill(bg, ROW_RX, Insets.EMPTY)));
                Color tc = (sel || hov) ? ROW_TXT_ON : ROW_TXT;
                rowLbl.setStyle("-fx-font-size:13.5px;-fx-text-fill:" + hex(tc)
                        + ";-fx-font-weight:" + (sel ? "bold" : "normal") + ";");
                bar.setFill(sel ? IND_600 : IND_400);
                slot.getChildren().setAll(sel ? chk : new Region());
            }

            @Override protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(null);
                if (empty || item == null) {
                    setGraphic(null); setBackground(Background.EMPTY); return;
                }
                rowLbl.setText(item.toString());
                setGraphic(row);
                boolean sel = isSelected();
                paint(sel, false);
                bar.setScaleY(sel ? 1.0 : 0.0); bar.setOpacity(sel ? 1.0 : 0.0);
                rowLbl.setScaleX(sel ? 1.04 : 1.0); rowLbl.setScaleY(sel ? 1.04 : 1.0);
            }

            {
                setOnMouseEntered(e -> { paint(isSelected(), true);  if (!isSelected()) animBar(true);  });
                setOnMouseExited(e  -> { paint(isSelected(), false); if (!isSelected()) animBar(false); });
                selectedProperty().addListener((obs, was, now) -> {
                    paint(now, false); animBar(now); animText(now);
                });
            }
        });

        // ── Glass popup — hook via skin (lookup() won't work) ─
        Runnable wire = () -> {
            Skin<?> skin = cb.getSkin();
            if (skin == null) return;
            javafx.scene.control.ListView<?> popupList = popupLv(skin);
            if (popupList != null) glassify(popupList);
        };
        cb.skinProperty().addListener((obs, o, n) -> { if (n!=null) javafx.application.Platform.runLater(wire); });
        cb.setOnShowing(e -> javafx.application.Platform.runLater(wire));

        return wrapper;
    }

    public static <T> StackPane styledCombo(String floatLabel, String ignoredPrompt) {
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
        if (cb.getValue() == null)
            lbl.setStyle("-fx-font-size:14px;-fx-text-fill:#ef4444;-fx-font-weight:normal;-fx-mouse-transparent:true;");
    }

    public static void comboClear(StackPane wrapper) {
        ComboBox<?> cb = getCombo(wrapper);
        String[] styles = wrapper.getId().split("\\|\\|");
        Label lbl = (Label) wrapper.getChildren().get(1);
        if (cb.getValue() != null) {
            cb.setStyle(styles[1]);
            lbl.setStyle("-fx-font-size:10px;-fx-font-weight:bold;-fx-text-fill:#6366f1;-fx-mouse-transparent:true;");
        } else {
            cb.setStyle(styles[0]);
            lbl.setStyle("-fx-font-size:14px;-fx-text-fill:" + textSubtle() + ";-fx-font-weight:normal;-fx-mouse-transparent:true;");
        }
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

    // ── SIDEBAR buttons (legacy — kept for backward compat) ───
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

    // ═══════════════════════════════════════════════════════════
    //  MODERN SIDEBAR NAV  — Icon box + frosted glass + pill
    //
    //  Returns a StackPane (the full clickable row).
    //  Call modernSidebarSetActive / modernSidebarSetInactive
    //  to toggle state. Both methods are animation-safe.
    // ═══════════════════════════════════════════════════════════

    /**
     * Build a modern sidebar nav row.
     *
     * Layout:  [pill] [icon-box] [label text]   (all in a StackPane→HBox)
     *
     * The returned StackPane is the clickable unit; attach setOnMouseClicked to it.
     * Tag it with setId(color) so the helpers can read the accent back later.
     */
    public static StackPane modernSidebarBtn(String icon, String label, String color) {

        // ── Left pill indicator (hidden until active) ─────────
        Rectangle pill = new Rectangle(3, 22);
        pill.setArcWidth(3); pill.setArcHeight(3);
        pill.setFill(Color.web(color));
        pill.setOpacity(0);                     // starts invisible
        StackPane pillWrap = new StackPane(pill);
        pillWrap.setPrefWidth(6);
        pillWrap.setAlignment(Pos.CENTER_LEFT);

        // ── Icon box ──────────────────────────────────────────
        Label iconLbl = new Label(icon);
        iconLbl.setStyle("-fx-font-size:15px;");

        Rectangle iconBg = new Rectangle(32, 32);
        iconBg.setArcWidth(9); iconBg.setArcHeight(9);
        iconBg.setFill(Color.web("#ffffff", 0.06));   // subtle inactive tint

        StackPane iconBox = new StackPane(iconBg, iconLbl);
        iconBox.setPrefSize(32, 32);
        iconBox.setAlignment(Pos.CENTER);

        // ── Label ─────────────────────────────────────────────
        Label textLbl = new Label(label);
        textLbl.setStyle(
                "-fx-font-size:13px;-fx-font-weight:normal;" +
                        "-fx-text-fill:#94a3b8;"   // TEXT_SUBTLE
        );

        // ── Row ───────────────────────────────────────────────
        HBox row = new HBox(10, pillWrap, iconBox, textLbl);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(0, 8, 0, 2));

        // ── Frosted-glass background (hidden until active) ────
        //    We fake blur with a semi-transparent rounded rect
        //    layered behind the row. Opacity animated on toggle.
        Rectangle glass = new Rectangle();
        glass.setArcWidth(10); glass.setArcHeight(10);
        glass.setFill(Color.web("#ffffff", 0.09));
        glass.setStroke(Color.web("#ffffff", 0.11));
        glass.setStrokeWidth(0.8);
        glass.setOpacity(0);

        // ── Outer wrapper (StackPane) ─────────────────────────
        StackPane wrapper = new StackPane(glass, row);
        wrapper.setPrefWidth(190);
        wrapper.setPrefHeight(44);
        wrapper.setAlignment(Pos.CENTER_LEFT);
        wrapper.setCursor(javafx.scene.Cursor.HAND);
        wrapper.setId(color);   // store accent for helpers

        // Keep glass rect always same size as wrapper
        wrapper.widthProperty().addListener((obs, o, n) -> glass.setWidth(n.doubleValue() - 4));
        glass.setWidth(186); glass.setHeight(40);
        glass.heightProperty().bind(wrapper.heightProperty().subtract(4));
        wrapper.setTranslateX(2);

        // ── Hover effect (inactive state) ─────────────────────
        wrapper.setOnMouseEntered(e -> {
            if (glass.getOpacity() < 0.5) {   // not already active
                FadeTransition ft = new FadeTransition(Duration.millis(150), glass);
                ft.setToValue(0.45); ft.play();
                textLbl.setStyle(
                        "-fx-font-size:13px;-fx-font-weight:normal;" +
                                "-fx-text-fill:#cbd5e1;"
                );
            }
        });
        wrapper.setOnMouseExited(e -> {
            if (glass.getOpacity() < 0.8) {   // not active
                FadeTransition ft = new FadeTransition(Duration.millis(150), glass);
                ft.setToValue(0); ft.play();
                textLbl.setStyle(
                        "-fx-font-size:13px;-fx-font-weight:normal;" +
                                "-fx-text-fill:#94a3b8;"
                );
            }
        });

        // Click press micro-animation
        wrapper.setOnMousePressed(e  -> wrapper.setScaleX(0.97));
        wrapper.setOnMouseReleased(e -> wrapper.setScaleX(1.0));

        return wrapper;
    }

    /** Animate the nav item into its active state. */
    public static void modernSidebarSetActive(StackPane wrapper) {
        String color = wrapper.getId();

        Rectangle glass   = (Rectangle) wrapper.getChildren().get(0);
        HBox      row     = (HBox)      wrapper.getChildren().get(1);
        StackPane pillWrap = (StackPane) row.getChildren().get(0);
        StackPane iconBox  = (StackPane) row.getChildren().get(1);
        Label     textLbl  = (Label)     row.getChildren().get(2);
        Rectangle pill     = (Rectangle) pillWrap.getChildren().get(0);
        Rectangle iconBg   = (Rectangle) iconBox.getChildren().get(0);

        // Frosted glass fade-in
        FadeTransition glassFade = new FadeTransition(Duration.millis(200), glass);
        glassFade.setToValue(1); glassFade.play();

        // Pill slide-in (scale from 0 → 1 on Y)
        pill.setScaleY(0);
        FadeTransition pillFade = new FadeTransition(Duration.millis(200), pill);
        pillFade.setToValue(1);
        ScaleTransition pillScale = new ScaleTransition(Duration.millis(220), pill);
        pillScale.setToY(1);
        pillScale.setInterpolator(Interpolator.EASE_OUT);
        new ParallelTransition(pillFade, pillScale).play();

        // Icon box fill with accent color
        iconBg.setFill(Color.web(color));

        // Text bold + accent color
        textLbl.setStyle(
                "-fx-font-size:13px;-fx-font-weight:bold;" +
                        "-fx-text-fill:" + color + ";"
        );

        // Hover overrides when active — keep glass at 1
        wrapper.setOnMouseEntered(null);
        wrapper.setOnMouseExited(null);
    }

    /** Reset a nav item back to its inactive state. */
    public static void modernSidebarSetInactive(StackPane wrapper) {
        String color = wrapper.getId();

        Rectangle glass   = (Rectangle) wrapper.getChildren().get(0);
        HBox      row     = (HBox)      wrapper.getChildren().get(1);
        StackPane pillWrap = (StackPane) row.getChildren().get(0);
        StackPane iconBox  = (StackPane) row.getChildren().get(1);
        Label     textLbl  = (Label)     row.getChildren().get(2);
        Rectangle pill     = (Rectangle) pillWrap.getChildren().get(0);
        Rectangle iconBg   = (Rectangle) iconBox.getChildren().get(0);

        // Glass out
        FadeTransition glassFade = new FadeTransition(Duration.millis(150), glass);
        glassFade.setToValue(0); glassFade.play();

        // Pill out
        FadeTransition pillFade = new FadeTransition(Duration.millis(150), pill);
        pillFade.setToValue(0); pillFade.play();

        // Icon box back to subtle tint
        iconBg.setFill(Color.web("#ffffff", 0.06));

        // Text back to subtle
        textLbl.setStyle(
                "-fx-font-size:13px;-fx-font-weight:normal;" +
                        "-fx-text-fill:#94a3b8;"
        );

        // Restore hover handlers
        wrapper.setOnMouseEntered(e -> {
            if (glass.getOpacity() < 0.5) {
                FadeTransition ft = new FadeTransition(Duration.millis(150), glass);
                ft.setToValue(0.45); ft.play();
                textLbl.setStyle("-fx-font-size:13px;-fx-font-weight:normal;-fx-text-fill:#cbd5e1;");
            }
        });
        wrapper.setOnMouseExited(e -> {
            if (glass.getOpacity() < 0.8) {
                FadeTransition ft = new FadeTransition(Duration.millis(150), glass);
                ft.setToValue(0); ft.play();
                textLbl.setStyle("-fx-font-size:13px;-fx-font-weight:normal;-fx-text-fill:#94a3b8;");
            }
        });
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