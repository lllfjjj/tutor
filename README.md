# Tutor · 前后端学习助手

一个用 Java 25、Spring Boot 和 Thymeleaf 构建的中文学习项目。

## Windows 本地开发

安装 [Eclipse Temurin JDK 25](https://adoptium.net/temurin/releases/?version=25)。在项目目录打开 PowerShell，设置当前终端的 JDK；不必修改系统级环境变量。

```powershell
$env:JAVA_HOME = "$env:LOCALAPPDATA\TutorDev\jdk25"  # 换成实际 JDK 25 根目录
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
.\mvnw.cmd -v
.\mvnw.cmd verify
.\mvnw.cmd spring-boot:run
```

首次运行 Maven Wrapper 需要联网获取 Maven 和依赖。无需另装 Maven、Node.js 或数据库。

## 协作

从最新 `main` 创建 `codex/*` 分支，关联 GitHub Issue 发起 PR。
Windows 和 Ubuntu 上的 Maven 验证均通过且差异检查完成后，使用 squash 合并。
不要提交本地数据库、凭据或本机 IDE 配置。
