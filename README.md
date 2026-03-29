# BookkeepingApp

一个使用 Kotlin + Jetpack Compose 开发的 Android 记账应用示例项目，支持日常收支记录、按天统计和日历查看。

## 功能

- 记录收入和支出
- 添加分类标签和备注
- 查看记录列表
- 查看按天统计结果
- 在日历中浏览每月数据
- 点击某一天查看当日明细

## 技术栈

- Kotlin
- Jetpack Compose
- Material 3
- Android ViewModel
- Kotlin Coroutines
- Room
- KSP

## 开发环境

- Gradle Wrapper `8.7`
- Android Gradle Plugin `8.5.2`
- Kotlin `1.9.24`
- JDK `21`
- compileSdk / targetSdk `36`
- minSdk `26`

说明：

- Gradle 运行环境使用 JDK 21。
- 应用字节码编译目标为 Java 17。

## 本地运行

1. 安装 Android Studio，并确保已安装 Android SDK。
2. 在项目根目录创建 `local.properties`，写入你本机的 SDK 路径：

```properties
sdk.dir=C:\\Users\\你的用户名\\AppData\\Local\\Android\\Sdk
```

3. 使用 Android Studio 打开项目，或在命令行执行：

```powershell
.\gradlew.bat assembleDebug
```

## APK 输出

Debug APK 默认输出到：

```text
app/build/outputs/apk/debug/app-debug.apk
```

## 项目结构

```text
app/                  Android 应用模块
app/src/main/         业务代码、界面、资源文件
app/schemas/          Room schema 导出文件
gradle/wrapper/       Gradle Wrapper 配置
build.gradle.kts      根项目构建配置
settings.gradle.kts   模块声明与仓库配置
gradlew / gradlew.bat Gradle 启动脚本
```

## Git 提交建议

以下内容建议提交到仓库：

- 源代码和资源文件
- Gradle 配置
- `gradle/wrapper`
- `README.md`

以下内容不建议提交：

- `.gradle/`、`.gradle-run/`、`.gradle-user*`
- `app/build/`
- `local.properties`
- `*.apk`

项目已提供 `.gitignore` 来忽略这些本机和构建产物。

## 说明

这个项目适合作为 Android Compose + Room 练手项目，也适合作为个人作品集中的一个基础应用示例。
