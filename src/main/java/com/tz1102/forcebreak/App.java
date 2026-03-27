package com.tz1102.forcebreak;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.AWTException;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.image.BufferedImage;
import java.io.File;

public class App extends Application {

    private CountdownManager countdownManager;
    private WarningPopup warningPopup;
    private LockScreenStage lockScreenStage;
    private ConfigManager configManager;

    private Label timeDisplayLabel;
    private TextField workDurationInput;
    private TextField lockMessageInput;
    private Label selectedImageLabel;

    // 把托盘图标提升为全局变量，方便后续每秒更新
    private TrayIcon trayIcon;

    private String customImagePath = "";
    private int workMinutes = 60;

    @Override
    public void start(Stage primaryStage) {
        // 阻止 JavaFX 窗口隐藏后自动退出
        Platform.setImplicitExit(false);

        configManager = new ConfigManager();
        workMinutes = configManager.getWorkMinutes();
        customImagePath = configManager.getImagePath();
        String savedMessage = configManager.getMessage();

        countdownManager = new CountdownManager(this, this::updateTimeDisplay);

        warningPopup = new WarningPopup(
                () -> countdownManager.addDelay(15),
                () -> System.out.println("用户忽略了弹窗")
        );

        lockScreenStage = new LockScreenStage(() -> {
            countdownManager.startCountdown(workMinutes);
        });

        // ================= UI 初始化 =================
        timeDisplayLabel = new Label("00:00");
        timeDisplayLabel.setStyle("-fx-font-size: 80px; -fx-font-family: 'Arial'; -fx-font-weight: bold; -fx-text-fill: #333333;");
        VBox topBox = new VBox(timeDisplayLabel);
        topBox.setAlignment(Pos.CENTER);
        topBox.setPadding(new Insets(30, 0, 20, 0));

        GridPane settingsGrid = new GridPane();
        settingsGrid.setHgap(15);
        settingsGrid.setVgap(20);
        settingsGrid.setAlignment(Pos.CENTER);
        settingsGrid.setStyle("-fx-background-color: white; -fx-background-radius: 12px; -fx-padding: 30px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 15, 0, 0, 4);");

        workDurationInput = new TextField(String.valueOf(workMinutes));
        workDurationInput.setStyle("-fx-font-size: 14px; -fx-pref-width: 200px; -fx-padding: 8px; -fx-background-radius: 6px; -fx-border-color: #ddd; -fx-border-radius: 6px;");

        lockMessageInput = new TextField(savedMessage);
        lockMessageInput.setStyle("-fx-font-size: 14px; -fx-pref-width: 200px; -fx-padding: 8px; -fx-background-radius: 6px; -fx-border-color: #ddd; -fx-border-radius: 6px;");

        Button selectImageBtn = new Button("选择图片");
        selectImageBtn.setStyle("-fx-background-color: #f0f0f0; -fx-text-fill: #333; -fx-padding: 8px 15px; -fx-background-radius: 6px; -fx-cursor: hand;");

        String imageLabelText = customImagePath.isEmpty() ? "未选择文件" : new File(customImagePath).getName();
        selectedImageLabel = new Label(imageLabelText);
        selectedImageLabel.setStyle("-fx-text-fill: #999; -fx-font-size: 12px;");

        selectImageBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("选择锁屏显示的图片");
            fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("图片文件", "*.png", "*.jpg", "*.jpeg", "*.gif"));
            File selectedFile = fileChooser.showOpenDialog(primaryStage);
            if (selectedFile != null) {
                customImagePath = selectedFile.getAbsolutePath();
                selectedImageLabel.setText(selectedFile.getName());
            }
        });

        settingsGrid.add(new Label("专注时长 (分钟):"), 0, 0); settingsGrid.add(workDurationInput, 1, 0);
        settingsGrid.add(new Label("锁屏文案:"), 0, 1); settingsGrid.add(lockMessageInput, 1, 1);
        settingsGrid.add(new Label("锁屏配图:"), 0, 2); settingsGrid.add(new HBox(10, selectImageBtn, selectedImageLabel), 1, 2);

        Button saveBtn = new Button("保存并重新计时");
        saveBtn.setStyle("-fx-background-color: #007AFF; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 12px 40px; -fx-background-radius: 8px; -fx-cursor: hand;");
        saveBtn.setOnAction(e -> applySettings());

        VBox bottomBox = new VBox(15, saveBtn);
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.setPadding(new Insets(30, 0, 0, 0));

        VBox root = new VBox(topBox, settingsGrid, bottomBox);
        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(20, 40, 40, 40));
        root.setStyle("-fx-background-color: #f7f8fa;");

        Scene scene = new Scene(root, 480, 500);
        primaryStage.setTitle("ForceBreak - 保护颈椎与视力");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);

        // 拦截关闭按钮：改为隐藏窗口
        primaryStage.setOnCloseRequest(event -> {
            event.consume();
            primaryStage.hide();
        });

        // 初始化系统托盘
        initSystemTray(primaryStage);

        primaryStage.show();

        updateTimeDisplay(workMinutes * 60);
        countdownManager.startCountdown(workMinutes);
    }

    // ================= 初始化系统托盘 =================
    private void initSystemTray(Stage primaryStage) {
        if (!SystemTray.isSupported()) {
            System.out.println("当前系统不支持系统托盘");
            return;
        }

        SystemTray tray = SystemTray.getSystemTray();

        // 初始占位图标
        BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics2D g = image.createGraphics();
        g.setColor(java.awt.Color.decode("#007AFF"));
        g.fillRect(0, 0, 16, 16);
        g.dispose();

        trayIcon = new TrayIcon(image, "ForceBreak - 正在计算时间...");
        trayIcon.setImageAutoSize(true);

        trayIcon.addActionListener(e -> Platform.runLater(() -> {
            primaryStage.show();
            primaryStage.toFront();
        }));

        // 解决菜单乱码：显式设置微软雅黑字体
        java.awt.Font chineseFont = new java.awt.Font("Microsoft YaHei", java.awt.Font.PLAIN, 12);
        java.awt.PopupMenu popup = new java.awt.PopupMenu();

        java.awt.MenuItem showItem = new java.awt.MenuItem("显示主界面 (Show)");
        showItem.setFont(chineseFont); // 应用字体
        showItem.addActionListener(e -> Platform.runLater(() -> {
            primaryStage.show();
            primaryStage.toFront();
        }));

        java.awt.MenuItem exitItem = new java.awt.MenuItem("完全退出 (Exit)");
        exitItem.setFont(chineseFont); // 应用字体
        exitItem.addActionListener(e -> {
            tray.remove(trayIcon);
            realExit();
        });

        popup.add(showItem);
        popup.addSeparator();
        popup.add(exitItem);

        trayIcon.setPopupMenu(popup);

        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            System.out.println("无法将图标添加到系统托盘");
        }
    }

    private void applySettings() {
        try {
            workMinutes = Integer.parseInt(workDurationInput.getText());
        } catch (NumberFormatException ex) {
            workMinutes = 60;
        }
        configManager.saveConfig(workMinutes, lockMessageInput.getText(), customImagePath);
        System.out.println("设置已保存并生效！");
        countdownManager.startCountdown(workMinutes);
    }

    private void realExit() {
        if (countdownManager != null) {
            countdownManager.stopCountdown();
        }
        Platform.exit();
        System.exit(0);
    }

    // ================= 更新时间与动态图标 =================
    public void updateTimeDisplay(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        String timeStr = String.format("%02d:%02d", minutes, seconds);

        // 1. 更新主界面大字
        if (Platform.isFxApplicationThread()) {
            timeDisplayLabel.setText(timeStr);
        } else {
            Platform.runLater(() -> timeDisplayLabel.setText(timeStr));
        }

        // 2. 更新系统托盘
        if (trayIcon != null) {
            java.awt.EventQueue.invokeLater(() -> {
                // 更新悬停文字（需要鼠标重新移入才会刷新可见）
                trayIcon.setToolTip("距离休息还有: " + timeStr);

                // 黑科技：动态重绘图标，把剩余分钟数直接画在图标上！
                BufferedImage dynamicImage = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);
                java.awt.Graphics2D g = dynamicImage.createGraphics();

                // 画蓝色背景
                g.setColor(java.awt.Color.decode("#007AFF"));
                g.fillRect(0, 0, 16, 16);

                // 画白色文字 (剩余分钟数)
                g.setColor(java.awt.Color.WHITE);
                g.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 10));

                // 居中绘制数字
                String minStr = String.valueOf(minutes);
                int stringWidth = g.getFontMetrics().stringWidth(minStr);
                int x = (16 - stringWidth) / 2;
                g.drawString(minStr, x, 12);
                g.dispose();

                // 实时替换托盘图标
                trayIcon.setImage(dynamicImage);
            });
        }
    }

    public void showWarningPopup() {
        if (!warningPopup.isShowing()) warningPopup.showAtBottomRight();
    }

    public void updateWarningPopupTime(int seconds) {
        if (warningPopup.isShowing()) warningPopup.updateSeconds(seconds);
    }

    public void triggerLockScreen() {
        if (warningPopup != null && warningPopup.isShowing()) warningPopup.hide();
        lockScreenStage.updateContent(lockMessageInput.getText(), customImagePath);
        if (!lockScreenStage.isShowing()) lockScreenStage.showLockScreen();
    }

    @Override
    public void stop() throws Exception {
        realExit();
    }

    public static void main(String[] args) {
        launch(args);
    }
}