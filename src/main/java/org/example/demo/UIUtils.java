package org.example.demo;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public class UIUtils {

    private static final String CSS_PATH = "/org/example/demo/style.css";

    public static void applyStyle(Scene scene) {
        if (UIUtils.class.getResource(CSS_PATH) != null) {
            scene.getStylesheets().add(UIUtils.class.getResource(CSS_PATH).toExternalForm());
        }
    }

    public static void applyButtonEffects(Button btn, String hoverColor) {
        DropShadow ds = new DropShadow();
        ds.setOffsetY(3.0);
        ds.setColor(Color.color(0, 0, 0, 0.4));
        btn.setEffect(ds);

        String baseStyle = "-fx-font-weight: bold; -fx-font-size: 14px; -fx-background-radius: 8;";
        String defaultStyle = "-fx-background-color: #ecf0f1; -fx-text-fill: #34495e; " + baseStyle;
        String hoverStyle = "-fx-background-color: " + hoverColor + "; -fx-text-fill: white; " + baseStyle;

        btn.setStyle(defaultStyle);

        btn.setOnMouseEntered(e -> {
            btn.setStyle(hoverStyle);
            btn.setScaleX(1.1);
            btn.setScaleY(1.1);
            ds.setRadius(15);
            ds.setColor(Color.color(0.2, 0.6, 1.0, 0.8)); // Blue Glow
        });

        btn.setOnMouseExited(e -> {
            btn.setStyle(defaultStyle);
            btn.setScaleX(1.0);
            btn.setScaleY(1.0);
            ds.setRadius(10);
            ds.setColor(Color.color(0, 0, 0, 0.4));
        });
    }

    public static void applyLinkEffects(Hyperlink link) {
        link.setStyle("-fx-text-fill: #2980b9; -fx-font-size: 13px; -fx-underline: false; -fx-font-weight: normal;");

        link.setOnMouseEntered(e -> {
            link.setScaleX(1.05);
            link.setScaleY(1.05);
            link.setStyle("-fx-text-fill: #3498db; -fx-underline: true; -fx-font-weight: bold;");
        });

        link.setOnMouseExited(e -> {
            link.setScaleX(1.0);
            link.setScaleY(1.0);
            link.setStyle("-fx-text-fill: #2980b9; -fx-underline: false; -fx-font-weight: normal;");
        });
    }

    public static void playTransition(Pane root, boolean movingForward) {
        double startX = movingForward ? 100 : -100;
        root.setTranslateX(startX);
        TranslateTransition slide = new TranslateTransition(Duration.millis(800), root);
        slide.setFromX(startX);
        slide.setToX(0);
        FadeTransition fade = new FadeTransition(Duration.millis(800), root);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        slide.play();
        fade.play();
    }
}

