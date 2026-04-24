package com.tz1102.forcebreak;

import javafx.stage.Screen;
import java.util.ArrayList;
import java.util.List;

public class LockScreenManager {
    // 记录当前打开的所有锁屏窗口
    private static List<LockScreenStage> activeStages = new ArrayList<>();

    public static void showAllScreens(String message, String imagePath, Runnable onUnlock) {
        // 防止重复打开，先清理旧的
        hideAll();

        // 无论你在哪个屏幕点击了右上角的 "✕"，都触发这个统一的回调
        Runnable globalUnlock = () -> {
            hideAll();
            if (onUnlock != null) onUnlock.run();
        };

        // 遍历电脑连接的所有显示器
        for (Screen screen : Screen.getScreens()) {
            // 为每一个显示器创建一个专属的遮罩
            LockScreenStage stage = new LockScreenStage(screen, globalUnlock);
            stage.updateContent(message, imagePath);
            stage.show();
            stage.toFront();
            activeStages.add(stage);
        }
    }

    public static void hideAll() {
        for (LockScreenStage stage : activeStages) {
            if (stage.isShowing()) {
                stage.hide();
            }
        }
        activeStages.clear();
    }
}