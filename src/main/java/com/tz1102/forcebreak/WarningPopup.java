package com.tz1102.forcebreak;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class WarningPopup {
    private Popup popup;
    private Stage dummyOwner;
    private Label titleLabel;
    private Label timeLabel;

    // 弹窗的固定尺寸
    private final double POPUP_WIDTH = 260;
    private final double POPUP_HEIGHT = 130;

    public WarningPopup(Runnable onDelay, Runnable onIgnore) {
        // 【核心黑科技】：创建一个永远看不见的假 Stage 作为 Popup 的宿主。
        dummyOwner = new Stage(StageStyle.UTILITY);
        dummyOwner.setOpacity(0); // 完全透明
        dummyOwner.setWidth(1);
        dummyOwner.setHeight(1);
        dummyOwner.setX(-10000); // 藏到屏幕外面去
        dummyOwner.setY(-10000);
        dummyOwner.show(); // 启动时悄悄展示一次，之后永远潜伏在后台

        // 使用 Popup 代替 Stage，Popup 天生具备不抢夺系统焦点的特性
        popup = new Popup();

        // ================= UI 组件构建 (高度还原你的截图) =================
        titleLabel = new Label("即将锁屏休息!");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #333333; -fx-font-family: 'Microsoft YaHei';");

        timeLabel = new Label("剩余 30 秒");
        timeLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: red; -fx-font-family: 'Microsoft YaHei';");

        Button delayBtn = new Button("延时15分钟");
        // 还原截图里的蓝边白底按钮
        delayBtn.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #00A2E8; -fx-border-radius: 2; -fx-padding: 6 15; -fx-cursor: hand; -fx-font-family: 'Microsoft YaHei';");
        delayBtn.setOnAction(e -> {
            hide();
            if (onDelay != null) onDelay.run();
        });

        Button ignoreBtn = new Button("忽略");
        // 还原截图里的灰边灰底按钮
        ignoreBtn.setStyle("-fx-background-color: #e6e6e6; -fx-border-color: #cccccc; -fx-border-radius: 2; -fx-padding: 6 15; -fx-cursor: hand; -fx-font-family: 'Microsoft YaHei';");
        ignoreBtn.setOnAction(e -> {
            hide();
            if (onIgnore != null) onIgnore.run();
        });

        HBox btnBox = new HBox(15, delayBtn, ignoreBtn);
        btnBox.setAlignment(Pos.CENTER);

        VBox root = new VBox(15, titleLabel, timeLabel, btnBox);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(15, 20, 15, 20));
        root.setPrefSize(POPUP_WIDTH, POPUP_HEIGHT);

        // 设置弹窗的白底和边框阴影效果
        root.setStyle("-fx-background-color: white; -fx-border-color: #d3d3d3; -fx-border-width: 1; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 2);");

        popup.getContent().add(root);
    }

    public boolean isShowing() {
        return popup.isShowing();
    }

    public void showAtBottomRight() {
        if (isShowing()) return;

        // 获取当前主屏幕的可用区域（排除 Windows 底部任务栏）
        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();

        // 计算右下角的坐标，留出 20px 的安全边距
        double x = bounds.getMaxX() - POPUP_WIDTH - 20;
        double y = bounds.getMaxY() - POPUP_HEIGHT - 20;

        // 借助 dummyOwner 显示 Popup，实现绝对静默、不抢焦点！
        popup.show(dummyOwner, x, y);
    }

    public void updateSeconds(int seconds) {
        // 确保 UI 更新在 JavaFX 线程中执行
        Platform.runLater(() -> {
            timeLabel.setText("剩余 " + seconds + " 秒");
        });
    }

    public void hide() {
        if (popup != null && popup.isShowing()) {
            popup.hide();
        }
    }
}