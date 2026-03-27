package com.tz1102.forcebreak;

import javafx.application.Platform;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CountdownManager {

    private ScheduledExecutorService scheduler;
    private int remainingSeconds;
    private final App mainApp;
    // 在 CountdownManager.java 中添加一个接口回调
    private final java.util.function.Consumer<Integer> onTick;

    // 修改构造函数
    public CountdownManager(App mainApp, java.util.function.Consumer<Integer> onTick) {
        this.mainApp = mainApp;
        this.onTick = onTick;
    }

    public void startCountdown(int minutes) {
        stopCountdown();
        // 测试时可以直接把这里改成秒，比如 remainingSeconds = minutes; 方便调试
//        remainingSeconds = minutes * 60;
        remainingSeconds = minutes ;
        scheduler = Executors.newSingleThreadScheduledExecutor();

        scheduler.scheduleAtFixedRate(() -> {
            remainingSeconds--;
            // 在 scheduler.scheduleAtFixedRate 内部的 remainingSeconds--; 下面加上这行：
            if (onTick != null) {
                Platform.runLater(() -> onTick.accept(remainingSeconds));
            }
            if (remainingSeconds == 30) {
                Platform.runLater(mainApp::showWarningPopup);
            }

            if (remainingSeconds <= 30 && remainingSeconds > 0) {
                Platform.runLater(() -> mainApp.updateWarningPopupTime(remainingSeconds));
            }

            if (remainingSeconds <= 0) {
                stopCountdown();
                Platform.runLater(mainApp::triggerLockScreen);
            }

        }, 1, 1, TimeUnit.SECONDS);
    }

    public void addDelay(int minutes) {
        if (scheduler != null && !scheduler.isShutdown()) {
//            remainingSeconds += minutes * 60;
            remainingSeconds += minutes;
        } else {
            startCountdown(minutes);
        }
    }

    public void stopCountdown() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }
    }
}