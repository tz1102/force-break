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
import java.util.Objects;

public class App extends Application {

    private CountdownManager countdownManager;
    private WarningPopup warningPopup;
    private LockScreenStage lockScreenStage;
    private ConfigManager configManager;

    private Label timeDisplayLabel;
    private TextField workDurationInput;
    private TextField lockMessageInput;
    private Label selectedImageLabel;
    private CheckBox autoStartCheckbox;

    // 把托盘图标提升为全局变量，方便后续每秒更新
    private TrayIcon trayIcon;

    private String customImagePath = "";
    private int workMinutes = 60;

    @Override
    public void start(Stage primaryStage) {
        // ================= 核心：阻止 JavaFX 窗口隐藏后自动退出 =================
        Platform.setImplicitExit(false);

        configManager = new ConfigManager();
        workMinutes = configManager.getWorkMinutes();
        customImagePath = configManager.getImagePath();
        String savedMessage = configManager.getMessage();

        // 这里我默认你已经在 CountdownManager.java 里修复了 workMinutes * 60 的乘法！
        countdownManager = new CountdownManager(this, this::updateTimeDisplay);

        warningPopup = new WarningPopup(
                () -> countdownManager.addDelay(15),
                () -> System.out.println("用户忽略了弹窗")
        );

        lockScreenStage = new LockScreenStage(() -> countdownManager.startCountdown(workMinutes));

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

        // --- 修复开始：定义图片选择部分的组件，并限制标签宽度 ---
        Button selectImageBtn = new Button("选择图片");
        selectImageBtn.setStyle("-fx-background-color: #f0f0f0; -fx-text-fill: #333; -fx-padding: 8px 15px; -fx-background-radius: 6px; -fx-cursor: hand;");

        String imageLabelText = customImagePath.isEmpty() ? "未选择文件" : new File(customImagePath).getName();
        selectedImageLabel = new Label(imageLabelText);
        // 【关键修复1】：限制最大宽度，与上面的输入框prefWidth一致 (200px)
        selectedImageLabel.setMaxWidth(200);
        // 【关键修复2】：设置截断模式为省略号
        selectedImageLabel.setTextOverrun(javafx.scene.control.OverrunStyle.ELLIPSIS);
        // 可以在 style 中明确一下，防止 Grid 的干扰导致它无法自动缩小
        selectedImageLabel.setStyle("-fx-text-fill: #999; -fx-font-size: 12px; -fx-min-width: 0;");

        HBox imageBox = new HBox(10, selectImageBtn, selectedImageLabel);
        imageBox.setAlignment(Pos.CENTER_LEFT);
        // 确保它不会在Grid中水平增长，干扰其他列
        GridPane.setHgrow(imageBox, Priority.NEVER);
        // --- 修复结束 ---

        selectImageBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("选择锁屏显示的图片");
            fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("图片文件", "*.png", "*.jpg", "*.jpeg", "*.gif"));
            File selectedFile = fileChooser.showOpenDialog(primaryStage);
            if (selectedFile != null) {
                customImagePath = selectedFile.getAbsolutePath();
                // 这里更新setText后，JavaFX会根据我们设置的OverrunStyle自动应用省略号
                selectedImageLabel.setText(selectedFile.getName());
            }
        });

        // --- 新增：开机自启复选框 ---
        autoStartCheckbox = new CheckBox("随 Windows 开机自动运行");
        // 从注册表读取真实状态，回显到 UI 上
        autoStartCheckbox.setSelected(AutoStartUtil.isAutoStartEnabled());
        autoStartCheckbox.setStyle("-fx-font-size: 14px; -fx-text-fill: #333; -fx-cursor: hand;");

        // 将之前的所有组件和新复选框一起放入 Grid (注意行号 index 变化)
        settingsGrid.add(new Label("专注时长 (分钟):"), 0, 0); settingsGrid.add(workDurationInput, 1, 0);
        settingsGrid.add(new Label("锁屏文案:"), 0, 1); settingsGrid.add(lockMessageInput, 1, 1);
        settingsGrid.add(new Label("锁屏配图:"), 0, 2); settingsGrid.add(imageBox, 1, 2);
        // 放在第 4 行
        settingsGrid.add(new Label("开机启动:"), 0, 3); settingsGrid.add(autoStartCheckbox, 1, 3);

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

        // ================= 拦截关闭按钮：改为隐藏窗口 =================
        primaryStage.setOnCloseRequest(event -> {
            event.consume();
            primaryStage.hide(); // 注意：这里改成了 hide()，彻底从任务栏消失，只留托盘
        });

        // ================= 设置窗口图标 =================
        try {
            // 加载 resources 目录下的 icon.png
            javafx.scene.image.Image appIcon = new javafx.scene.image.Image(Objects.requireNonNull(getClass().getResourceAsStream("/icon.png")));
            primaryStage.getIcons().add(appIcon);
        } catch (Exception e) {
            System.out.println("图标加载失败，请检查 resources 目录下是否存在 icon.png");
        }

        // ================= 初始化系统托盘 =================
        initSystemTray(primaryStage);

        primaryStage.show();

        // 初次启动立刻更新一次界面
        updateTimeDisplay(workMinutes * 60);
        countdownManager.startCountdown(workMinutes);
    }

    private void initSystemTray(Stage primaryStage) {
        if (!SystemTray.isSupported()) {
            System.out.println("当前系统不支持系统托盘");
            return;
        }

        SystemTray tray = SystemTray.getSystemTray();

        // 初始占位图标
        BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics2D g = image.createGraphics();
        g.setColor(java.awt.Color.decode("#007AFF")); // 苹果蓝
        g.fillRect(0, 0, 16, 16);
        g.dispose();

        // 这里初始化只给个占位符，马上就会被 updateTimeDisplay 覆盖
        trayIcon = new TrayIcon(image, "ForceBreak - Calculating...");
        trayIcon.setImageAutoSize(true);

        trayIcon.addActionListener(e -> Platform.runLater(() -> {
            primaryStage.show();
            primaryStage.toFront();
        }));

        // 使用纯英文菜单，彻底避开底层 AWT GBK 乱码的坑
        java.awt.PopupMenu popup = new java.awt.PopupMenu();

        java.awt.MenuItem showItem = new java.awt.MenuItem("Show Panel");
        showItem.addActionListener(e -> Platform.runLater(() -> {
            primaryStage.show();
            primaryStage.toFront();
        }));

        java.awt.MenuItem exitItem = new java.awt.MenuItem("Exit");
        exitItem.addActionListener(e -> {
            tray.remove(trayIcon); // 从托盘移除图标
            realExit();            // 彻底结束程序
        });

        popup.add(showItem);
        popup.addSeparator(); // 加一条分割线
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
        // --- 新增：保存时应用开机自启状态 ---
        AutoStartUtil.setAutoStart(autoStartCheckbox.isSelected());

        System.out.println("设置已保存并生效！");

        // 立刻手动刷新界面和托盘的时间显示，消除 1 秒的视觉延迟
        updateTimeDisplay(workMinutes * 60);

        countdownManager.startCountdown(workMinutes);

        // 保存后立刻隐藏主窗口
        Stage stage = (Stage) workDurationInput.getScene().getWindow();
        if (stage != null) {
            stage.hide();
        }
    }

    private void realExit() {
        if (countdownManager != null) {
            countdownManager.stopCountdown();
        }
        Platform.exit(); // 真正结束 JavaFX 线程
        System.exit(0);
    }

    // 更新时间与动态图标的黑科技方法
    public void updateTimeDisplay(int totalSeconds) {
        // 计算时、分、秒用于主 UI
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;

        String timeStr;
        if (hours > 0) {
            timeStr = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            timeStr = String.format("%02d:%02d", minutes, seconds);
        }

        // 1. 更新主界面大字
        if (Platform.isFxApplicationThread()) {
            timeDisplayLabel.setText(timeStr);
        } else {
            Platform.runLater(() -> timeDisplayLabel.setText(timeStr));
        }

        // 2. 更新系统托盘动态图标
        if (trayIcon != null) {
            java.awt.EventQueue.invokeLater(() -> {
                // 更新悬停文字
                trayIcon.setToolTip("Time Left to Break: " + timeStr);

                // 黑科技：动态重绘图标
                String displayStr;
                java.awt.Color bgColor;

                if (totalSeconds >= 60) {
                    // 大于1分钟，显示向上取整的分钟数（比如 1分01秒 显示 2）
                    int displayMins = (int) Math.ceil(totalSeconds / 60.0);

                    // 为了防止 1 小时显示 60 太宽挤不下，我们需要对超大数字优化一下显示
                    displayStr = displayMins > 99 ? "99+" : String.valueOf(displayMins);
                    bgColor = java.awt.Color.decode("#007AFF"); // 蓝色背景
                } else {
                    // 最后 1 分钟，直接显示秒数倒计时，背景变橙色警告！
                    displayStr = String.valueOf(totalSeconds);
                    bgColor = java.awt.Color.decode("#FF9500"); // 橙色背景
                }

                BufferedImage dynamicImage = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);
                java.awt.Graphics2D g = dynamicImage.createGraphics();

                // 开启抗锯齿，让画出来的数字更平滑清晰
                g.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                // 画背景色
                g.setColor(bgColor);
                g.fillRect(0, 0, 16, 16);

                // 画白色数字
                g.setColor(java.awt.Color.WHITE);
                g.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 10));

                // 计算文字宽度以实现居中
                int stringWidth = g.getFontMetrics().stringWidth(displayStr);
                int x = (16 - stringWidth) / 2;
                int y = 12; // 文字基线

                g.drawString(displayStr, x, y);
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
    public void stop() {
        realExit();
    }

    public static void main(String[] args) {
        launch(args);
    }
}