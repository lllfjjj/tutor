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
启动后访问 [本地 Tutor](http://127.0.0.1:8080)。课程和题目由 Flyway 初始化，数据保存在 `data/tutor.mv.db`。

## 怎么学

- 首页选择前端基础、Java 后端或前后端联调，共 6 节课程、18 道单选题。联调课可实际发起 200 和 404 请求。
- 每节课读完示例后提交练习。结果页会给出判分和解析，Java 题还附有选题来源及技术依据。
- 错题本跟踪每题最后一次作答；重做答对后移出。任意一节课第一次完整提交后计入进度。重启应用仍能继续学习。

## 开源参考

- [Spring 官方 Thymeleaf 示例](https://github.com/spring-guides/gs-serving-web-content) 和 [Spring Petclinic](https://github.com/spring-projects/spring-petclinic)：参考 MVC 模板、静态资源与 H2 集成验证。
- [roadmap.sh](https://github.com/nilbuild/developer-roadmap)：借鉴按主题组织学习路线、再逐步拆分成可完成项目的方式。
- [QuizZz](https://github.com/oojorgeoo89/QuizZz)：参考免登录答题体验；独立实现判分与解析，不引入旧版前端或用户管理模块。
- [JavaGuide](https://github.com/Snailclimb/JavaGuide)：补充 Java 与 Spring 面试考点。只参考主题，独立编写题目和示例；公开面经不视为公司官方真题。

## 协作

从最新 `main` 创建 `codex/*` 分支，关联 GitHub Issue 发起 PR。
Windows 和 Ubuntu 上的 Maven 验证均通过且差异检查完成后，使用 squash 合并。
不要提交本地数据库、凭据或本机 IDE 配置。
已发布的题目 ID 和对应的题干、选项、答案与解析保持不变，历史作答引用原题；后续需要更正时应保留旧版本。
