package com.tz1102.forcebreak;

import javafx.application.Platform;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class CountdownManager {

    private ScheduledExecutorService scheduler;
    // 【核心修复】：使用 AtomicInteger 保证多线程下的内存可见性和原子操作
    private final AtomicInteger remainingSeconds = new AtomicInteger(0);
    private final App mainApp;
    private final java.util.function.Consumer<Integer> onTick;

    public CountdownManager(App mainApp, java.util.function.Consumer<Integer> onTick) {
        this.mainApp = mainApp;
        this.onTick = onTick;
    }

    public void startCountdown(int minutes) {
        stopCountdown();

        // 设置初始秒数
        remainingSeconds.set(minutes * 60);
        scheduler = Executors.newSingleThreadScheduledExecutor();

        scheduler.scheduleAtFixedRate(() -> {
            // 安全地自减并获取当前值
            int current = remainingSeconds.decrementAndGet();

            // 1. 每秒通知主界面更新 UI
            if (onTick != null) {
                Platform.runLater(() -> onTick.accept(current));
            }

            // 2. 剩余 30 秒时，弹出右下角预警
            if (current == 30) {
                Platform.runLater(mainApp::showWarningPopup);
            }

            // 3. 最后 30 秒内，更新右下角弹窗的倒计时
            if (current <= 30 && current > 0) {
                Platform.runLater(() -> mainApp.updateWarningPopupTime(current));
            }

            // 4. 到达 0，强制锁屏
            if (current <= 0) {
                stopCountdown();
                Platform.runLater(mainApp::triggerLockScreen);
            }

        }, 1, 1, TimeUnit.SECONDS);
    }

    public void addDelay(int minutes) {
        if (scheduler != null && !scheduler.isShutdown()) {
            // 【核心修复】：线程安全地增加秒数
            int newTime = remainingSeconds.addAndGet(minutes * 60);

            // 增加后立刻通知 UI 刷新，消除视觉延迟
            if (onTick != null) {
                Platform.runLater(() -> onTick.accept(newTime));
            }
        } else {
            // 如果倒计时已经结束，直接重新开始
            startCountdown(minutes);
        }
    }

    public void stopCountdown() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }
    }
}