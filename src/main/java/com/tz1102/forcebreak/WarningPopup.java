package com.tz1102.forcebreak;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class WarningPopup extends Stage {

    private final Runnable onDelay;
    private final Runnable onIgnore;
    private Label timeLabel;

    public WarningPopup(Runnable onDelay, Runnable onIgnore) {
        this.onDelay = onDelay;
        this.onIgnore = onIgnore;
        initUI();
    }

    private void initUI() {
        this.initStyle(StageStyle.UNDECORATED);
        this.setAlwaysOnTop(true);

        Label title = new Label("即将锁屏休息！");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        timeLabel = new Label("剩余 30 秒");
        timeLabel.setStyle("-fx-text-fill: red; -fx-font-size: 14px;");

        Button delayBtn = new Button("延时15分钟");
        delayBtn.setOnAction(e -> {
            this.hide();
            if (onDelay != null) onDelay.run();
        });

        Button ignoreBtn = new Button("忽略");
        ignoreBtn.setOnAction(e -> {
            this.hide();
            if (onIgnore != null) onIgnore.run();
        });

        HBox btnBox = new HBox(10, delayBtn, ignoreBtn);
        btnBox.setAlignment(Pos.CENTER);

        VBox root = new VBox(15, title, timeLabel, btnBox);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-border-width: 1px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 0);");

        Scene scene = new Scene(root, 300, 150);
        this.setScene(scene);
    }

    public void updateSeconds(int seconds) {
        timeLabel.setText("剩余 " + seconds + " 秒");
    }

    public void showAtBottomRight() {
        this.show();
        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        this.setX(bounds.getMaxX() - this.getWidth() - 20);
        this.setY(bounds.getMaxY() - this.getHeight() - 20);
    }
}