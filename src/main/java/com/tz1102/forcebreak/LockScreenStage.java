package com.tz1102.forcebreak;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
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

    // 注意：构造函数现在接收一个指定的 Screen
    public LockScreenStage(Screen screen, Runnable onUnlock) {
        // 1. 无边框、置顶。多屏模式下绝对不要用原生全屏，用无边框+精准覆盖最稳！
        initStyle(StageStyle.UNDECORATED);
        setAlwaysOnTop(true);

        root = new StackPane();
        root.setStyle("-fx-background-color: #2c3e50;");

        // 2. 中央提示文字（默认隐藏）
        messageLabel = new Label();
        messageLabel.setStyle("-fx-font-size: 48px; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.8), 10, 0, 0, 0);");
        messageLabel.setVisible(false); // 初始不可见
        messageLabel.setManaged(false); // 不占据布局空间

        // 3. 右上角关闭按钮 (X)
        closeButton = new Button("✕");
        closeButton.setStyle("-fx-font-size: 24px; -fx-background-color: transparent; -fx-text-fill: rgba(255, 255, 255, 0.5); -fx-cursor: hand; -fx-padding: 10 20;");
        closeButton.setOnMouseEntered(e -> closeButton.setStyle("-fx-font-size: 24px; -fx-background-color: rgba(255,255,255,0.1); -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 10 20;"));
        closeButton.setOnMouseExited(e -> closeButton.setStyle("-fx-font-size: 24px; -fx-background-color: transparent; -fx-text-fill: rgba(255, 255, 255, 0.5); -fx-padding: 10 20;"));

        closeButton.setOnAction(e -> {
            if (onUnlock != null) onUnlock.run();
        });

        root.getChildren().add(messageLabel);
        StackPane.setAlignment(messageLabel, Pos.CENTER);

        root.getChildren().add(closeButton);
        StackPane.setAlignment(closeButton, Pos.TOP_RIGHT);
        StackPane.setMargin(closeButton, new Insets(20));

        Scene scene = new Scene(root);
        setScene(scene);

        // 4. 【核心黑科技】：获取指定屏幕的坐标和分辨率，精准覆盖！
        Rectangle2D bounds = screen.getBounds();
        setX(bounds.getMinX()); // 有些副屏的 X 坐标是负数或者很大的正数
        setY(bounds.getMinY());
        setWidth(bounds.getWidth());
        setHeight(bounds.getHeight());
    }

    public void updateContent(String message, String imagePath) {
        // 【逻辑修改】：如果没有文字，彻底隐藏 Label
        if (message == null || message.trim().isEmpty()) {
            messageLabel.setVisible(false);
            messageLabel.setManaged(false);
        } else {
            messageLabel.setText(message);
            messageLabel.setVisible(true);
            messageLabel.setManaged(true);
        }

        // 图片 CSS 覆盖逻辑（保持不变）
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
}