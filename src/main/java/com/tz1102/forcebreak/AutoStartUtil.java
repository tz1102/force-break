package com.tz1102.forcebreak;

import java.io.File;

public class AutoStartUtil {

    private static final String REG_KEY = "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Run";
    private static final String APP_NAME = "ForceBreak";

    // 检查当前是否已经开启了自启动
    public static boolean isAutoStartEnabled() {
        try {
            // 查询注册表里有没有我们的软件名字
            Process process = Runtime.getRuntime().exec("reg query \"" + REG_KEY + "\" /v \"" + APP_NAME + "\"");
            process.waitFor();
            // exitValue 为 0 说明找到了键值，即已开启自启
            return process.exitValue() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    // 设置开启或关闭自启动
    public static void setAutoStart(boolean enable) {
        try {
            if (enable) {
                // 获取当前 EXE 所在的绝对路径
                String currentDir = System.getProperty("user.dir");
                String exePath = currentDir + File.separator + APP_NAME + ".exe";

                // 写入注册表 (使用 /f 强制覆盖)
                // 命令格式: reg add "HKCU\Software\..." /v "ForceBreak" /t REG_SZ /d "\"D:\...\ForceBreak.exe\"" /f
                String command = String.format("reg add \"%s\" /v \"%s\" /t REG_SZ /d \"\\\"%s\\\"\" /f", REG_KEY, APP_NAME, exePath);
                Runtime.getRuntime().exec(command);
                System.out.println("开机自启动已开启 -> " + exePath);
            } else {
                // 删除注册表项
                String command = String.format("reg delete \"%s\" /v \"%s\" /f", REG_KEY, APP_NAME);
                Runtime.getRuntime().exec(command);
                System.out.println("开机自启动已关闭");
            }
        } catch (Exception e) {
            System.out.println("修改开机启动项失败：" + e.getMessage());
        }
    }
}