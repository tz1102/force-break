@echo off
chcp 65001 >nul
echo ==========================================
echo   ForceBreak 自动打包构建脚本 (jpackage)
echo ==========================================

echo [1/4] 清理并打包 Maven (生成 Fat Jar)...
:: 注意：这里必须用 call，否则 mvn 执行完 bat 就会直接退出
call mvn clean package -DskipTests
if %ERRORLEVEL% neq 0 (
    echo [错误] Maven 打包失败，请检查代码！
    pause
    exit /b
)

echo [2/4] 准备干净的打包目录...
if exist release rmdir /s /q release
if exist target\app-in rmdir /s /q target\app-in
mkdir target\app-in

echo [3/4] 复制胖包并准备资源...
copy target\force-break-1.0-SNAPSHOT.jar target\app-in\ >nul

echo [4/4] 正在生成专属 EXE (带有柴犬图标)...
:: 这里填入你昨天成功的终极打包命令，注意把图标路径带上
"D:\jdk21\bin\jpackage.exe" --type app-image --name ForceBreak --icon src\main\resources\icon.ico --input target\app-in --main-jar force-break-1.1.jar --main-class com.tz1102.forcebreak.Launcher --dest release --add-modules java.desktop,jdk.unsupported,java.logging

echo ==========================================
echo   打包大功告成！EXE 已生成在 release\ForceBreak 目录下
echo ==========================================
pause