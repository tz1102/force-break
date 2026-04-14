package com.tz1102.forcebreak;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import java.io.File;

public class LockScreenStage extends Stage {
    private Label messageLabel;
    private Button closeButton;
    private StackPane root;

    public LockScreenStage(Runnable onUnlock) {
        // 1. 基础设置：无边框、全屏、置顶
        initStyle(StageStyle.UNDECORATED);
        setFullScreen(true);
        setFullScreenExitHint("");
        setAlwaysOnTop(true);

        root = new StackPane();
        root.setStyle("-fx-background-color: #2c3e50;");

        // 2. 中央提示文字（保留，增加阴影增强可读性）
        messageLabel = new Label("休息一下吧！");
        messageLabel.setStyle("-fx-font-size: 48px; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.8), 10, 0, 0, 0);");

        // 3. 右上角关闭按钮 (X)
        closeButton = new Button("✕");
        // 样式：透明背景，白色文字，鼠标悬停时略微显现
        closeButton.setStyle("-fx-font-size: 24px; " +
                "-fx-background-color: transparent; " +
                "-fx-text-fill: rgba(255, 255, 255, 0.5); " + // 初始半透明
                "-fx-cursor: hand; " +
                "-fx-padding: 10 20;");

        // 悬停交互：鼠标移上去时变亮
        closeButton.setOnMouseEntered(e -> closeButton.setStyle("-fx-font-size: 24px; -fx-background-color: rgba(255,255,255,0.1); -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 10 20;"));
        closeButton.setOnMouseExited(e -> closeButton.setStyle("-fx-font-size: 24px; -fx-background-color: transparent; -fx-text-fill: rgba(255, 255, 255, 0.5); -fx-padding: 10 20;"));

        // 点击动作：退出锁屏
        closeButton.setOnAction(e -> {
            hide();
            if (onUnlock != null) onUnlock.run();
        });

        // 4. 布局组合
        // 文字居中
        root.getChildren().add(messageLabel);
        StackPane.setAlignment(messageLabel, Pos.CENTER);

        // 关闭按钮放在右上角，设置 20 像素的边距
        root.getChildren().add(closeButton);
        StackPane.setAlignment(closeButton, Pos.TOP_RIGHT);
        StackPane.setMargin(closeButton, new Insets(20));

        Scene scene = new Scene(root);
        setScene(scene);

        setWidth(Screen.getPrimary().getBounds().getWidth());
        setHeight(Screen.getPrimary().getBounds().getHeight());
    }

    /**
     * 更新文案和背景图 (CSS 方案)
     */
    public void updateContent(String message, String imagePath) {
        if (message != null && !message.isEmpty()) {
            messageLabel.setText(message);
        }

        String cssStyle = "-fx-background-color: #2c3e50;";
        if (imagePath != null && !imagePath.isEmpty()) {
            try {
                File file = new File(imagePath);
                if (file.exists()) {
                    String fileUri = file.toURI().toString();
                    cssStyle += " -fx-background-image: url(\"" + fileUri + "\");"
                            + " -fx-background-size: cover;"
                            + " -fx-background-position: center;"
                            + " -fx-background-repeat: no-repeat;";
                }
            } catch (Exception e) {
                System.out.println("加载锁屏图片失败：" + e.getMessage());
            }
        }
        root.setStyle(cssStyle);
    }

    public void showLockScreen() {
        setFullScreen(true);
        show();
        toFront();
        // 删除了 startUnlockCountdown() 的调用，不再有强制等待逻辑
    }
}