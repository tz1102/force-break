package com.tz1102.forcebreak;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;

public class LockScreenStage extends Stage {

    private final Runnable onUnlock;
    // 确保这些都是成员变量
    private Label messageLabel;
    private ImageView imageView;
    private VBox contentBox;

    public LockScreenStage(Runnable onUnlock) {
        this.onUnlock = onUnlock;
        initUI();
    }

    private void initUI() {
        // 1. 设置无边框和永远置顶
        this.initStyle(StageStyle.UNDECORATED);
        this.setAlwaysOnTop(true);

        // 2. 核心文本提示 (直接赋值给成员变量，不要再写 Label messageLabel = ...)
        messageLabel = new Label();
        messageLabel.setStyle("-fx-font-size: 28px; -fx-text-fill: white; -fx-font-weight: bold; -fx-effect: dropshadow(gaussian, black, 5, 0.5, 0, 0);");

        // 3. 图片展示区
        imageView = new ImageView();
        imageView.setFitWidth(500);
        imageView.setPreserveRatio(true);

        // 4. 解锁按钮
        Button unlockBtn = new Button("我已休息好，解锁并重新计时");
        unlockBtn.setStyle("-fx-font-size: 16px; -fx-padding: 10 20 10 20; -fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 5px; -fx-cursor: hand;");
        unlockBtn.setOnAction(e -> unlockAndClose());

        // 5. 布局拼装
        contentBox = new VBox(30);
        contentBox.setAlignment(Pos.CENTER);
        // 初始化时先放入文字和按钮，图片在 updateContent 中动态添加
        contentBox.getChildren().addAll(messageLabel, unlockBtn);

        // 6. 半透明毛玻璃背景
        StackPane root = new StackPane(contentBox);
        root.setStyle("-fx-background-color: rgba(30, 30, 30, 0.95);");

        // 7. 获取主屏幕铺满
        double screenWidth = Screen.getPrimary().getBounds().getWidth();
        double screenHeight = Screen.getPrimary().getBounds().getHeight();
        Scene scene = new Scene(root, screenWidth, screenHeight);

        this.setScene(scene);

        // 8. 开启全屏并禁用 ESC 退出
        this.setFullScreen(true);
        this.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
    }

    // 动态更新内容的方法
    public void updateContent(String text, String imagePath) {
        // 更新文字
        messageLabel.setText(text);

        // 先尝试从布局中移除旧图片，防止重复添加
        contentBox.getChildren().remove(imageView);

        // 更新图片
        if (imagePath != null && !imagePath.isEmpty()) {
            try {
                File file = new File(imagePath);
                if (file.exists()) {
                    // 使用 file.toURI().toString() 更安全，能完美兼容 Windows 的各种路径
                    imageView.setImage(new Image(file.toURI().toString()));
                    contentBox.getChildren().add(0, imageView); // 始终将图片添加到 VBox 的最上面 (索引 0)
                }
            } catch (Exception e) {
                System.out.println("图片加载失败: " + imagePath);
                e.printStackTrace();
            }
        } else {
            // 如果用户没选图片，清空
            imageView.setImage(null);
        }
    }

    private void unlockAndClose() {
        this.hide();
        if (onUnlock != null) {
            onUnlock.run();
        }
    }

    public void showLockScreen() {
        this.show();
        this.setAlwaysOnTop(true);
        this.setFullScreen(true);
    }
}