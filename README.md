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
