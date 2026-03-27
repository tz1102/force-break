package com.tz1102.forcebreak;

import java.io.*;
import java.util.Properties;

public class ConfigManager {
    // 配置文件路径：C:\Users\你的用户名\.forcebreak\config.properties
    private static final String CONFIG_FILE = System.getProperty("user.home") + File.separator + ".forcebreak" + File.separator + "config.properties";
    private final Properties props = new Properties();

    public ConfigManager() {
        loadConfig();
    }

    private void loadConfig() {
        File file = new File(CONFIG_FILE);
        if (file.exists()) {
            try (FileInputStream fis = new FileInputStream(file)) {
                props.load(fis);
            } catch (IOException e) {
                System.out.println("读取配置文件失败: " + e.getMessage());
            }
        }
    }

    public void saveConfig(int workMinutes, String message, String imagePath) {
        File file = new File(CONFIG_FILE);
        // 确保父目录存在
        file.getParentFile().mkdirs();

        props.setProperty("workMinutes", String.valueOf(workMinutes));
        props.setProperty("message", message != null ? message : "");
        props.setProperty("imagePath", imagePath != null ? imagePath : "");

        try (FileOutputStream fos = new FileOutputStream(file)) {
            props.store(fos, "ForceBreak Configuration");
        } catch (IOException e) {
            System.out.println("保存配置文件失败: " + e.getMessage());
        }
    }

    // 获取配置，带默认值
    public int getWorkMinutes() {
        return Integer.parseInt(props.getProperty("workMinutes", "60"));
    }

    public String getMessage() {
        return props.getProperty("message", "休息时间到！去摸摸微笑，放松一下眼睛吧~");
    }

    public String getImagePath() {
        return props.getProperty("imagePath", "");
    }
}