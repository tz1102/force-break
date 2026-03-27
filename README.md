# ForceBreak ⏳

> 这是一个强制休息的桌面工具。旨在通过自定义倒计时和全屏锁屏遮罩，强迫你离开电脑去休息、放空大脑，保护视力与颈椎。

![License](https://img.shields.io/badge/license-MIT-blue.svg)
![JDK](https://img.shields.io/badge/JDK-21-green.svg)
![GraalVM](https://img.shields.io/badge/GraalVM-Native_Image-red.svg)
![JavaFX](https://img.shields.io/badge/JavaFX-21-orange.svg)
![Platform](https://img.shields.io/badge/platform-Windows-lightgrey.svg)

## ✨ 核心功能

* **⏳ 自定义倒计时**：默认 60 分钟，支持根据个人习惯自定义工作时长。
* **⚠️ 人性化预警**：倒计时剩余 30 秒时，右下角平滑弹出通知弹窗。
* **☕ 灵活延时机制**：预警弹窗支持“延时 15 分钟”或“忽略”，应对突发的紧急工作状态。
* **🔒 强制锁屏遮罩**：时间一到，自动弹出全屏置顶的无边框遮罩，并调用 Windows 底层 API 关闭显示器，彻底打断工作流。
* **🎨 个性化锁屏界面**：支持自定义锁屏展示的图片和文案。

## 🛠️ 技术栈

* **开发语言:** Java (JDK 21)
* **GUI 框架:** JavaFX 21
* **底层交互:** JNA (Java Native Access) 用于调用 Windows API
* **构建与打包:** Maven + GraalVM (结合 GluonFX 编译原生 Native EXE)

## 🚀 快速开始

### 1. 下载体验
前往 [Releases](https://github.com/tz1102/force-break/releases) 页面，下载最新版本的 `ForceBreak.exe`，双击即可运行，无需安装 Java 环境。

### 2. 本地开发指南
如果你想二次开发或自行编译代码，请确保本地已安装 **JDK 21**、**GraalVM** 以及 **Maven**。

```bash
# 1. 克隆仓库
git clone [https://github.com/tz1102/force-break.git](https://github.com/tz1102/force-break.git)

# 2. 进入项目目录
cd force-break

# 3. 运行测试项目
mvn clean javafx:run
```

### 3. 编译原生 EXE (Native Image)
本项目利用 GraalVM 和 GluonFX 插件将 Java 应用程序编译为 Windows 原生可执行文件。在确保环境配置正确（如 MSVC 编译工具链）的情况下，执行以下命令：

```bash
mvn gluonfx:build
```

编译成功后，可执行文件将生成在 target/gluonfx/x86_64-windows/ 目录下。

## ⚙️ 配置与自定义
软件支持通过本地配置文件（如 config.json）进行高度自定义，打造属于你自己的休息提醒：
```json
{
  "workDurationMinutes": 60,
  "delayDurationMinutes": 15,
  "lockScreenImagePath": "C:/images/smile.jpg",
  "lockScreenMessage": "工作辛苦了，该让你的颈椎休息一会儿啦！",
  "autoStart": true
}
```

## 📮 关注与交流
欢迎在各类社交平台（如抖音、小红书等）关注我的账号获取最新动态：

- 社交账号: [微信:ztxp1102]

- 视频平台: [在此处替换为你的账号名称/链接]

## 🤝 参与贡献
欢迎提交 Issue 或 Pull Request！

1. Fork 本仓库

2. 创建你的特性分支 (git checkout -b feature/AmazingFeature)

3. 提交你的更改 (git commit -m 'Add some AmazingFeature')

4. 推送到分支 (git push origin feature/AmazingFeature)

5. 开启一个 Pull Request

## 📄 开源协议
本项目采用 MIT License 协议开源。欢迎自由使用、修改和分发。
