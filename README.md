# BookkeepingApp — 全栈记账应用

> **Kotlin + Jetpack Compose** 前端（AI Agent 辅助生成） + **Java Spring Boot** 后端（手动搭建）。覆盖用户认证、JWT 鉴权、API 设计等完整工程实践。

---

## 项目亮点

-   **AI Agent 辅助开发** — 前端代码由 AI Agent 生成，后端由我主导从零搭建，前后端联调中沉淀了人机协作流程
-   **全栈闭环** — Android 端（Kotlin / Room / SQLite） + 服务端（Spring Boot / JPA / MySQL）
-   **安全体系** — BCrypt + Salt 加盐加密、JWT 双 Token 鉴权（Access + Refresh）、无状态认证、全局异常处理

---

## 技术栈

| 层次 | 技术 |
|------|------|
| **前端** | Kotlin、Jetpack Compose、Material 3、Room（AI Agent 辅助生成） |
| **后端** | Java 17、Spring Boot 3.5、Spring Security、Spring Data JPA |
| **数据库** | MySQL 8 + H2（开发测试） |
| **鉴权** | JWT（jjwt 0.12）、BCryptPasswordEncoder、Salt 盐值 |
| **构建** | Gradle（Kotlin DSL）、Gradle Wrapper |

---

## 后端架构

```
请求 → JwtAuthenticationFilter（验签）→ Controller → Service → Repository → MySQL
                                            ↑                          ↑
                                     @RestControllerAdvice      JPA 自动建表
                                       全局异常处理
```

### 核心接口

| 接口 | 说明 |
|------|------|
| `POST /api/auth/register` | 注册（BCrypt + Salt 加密） |
| `POST /api/auth/login` | 登录（返回 Access + Refresh Token） |
| `POST /api/auth/refresh` | 刷新 Token |
| `POST /api/records` | 新增记账记录（需 JWT） |
| `GET /api/records` | 查询当前用户所有记录 |
| `GET /api/records/range` | 按时间范围查询 |
| `PUT /api/records/{id}` | 更新记录 |
| `DELETE /api/records/{id}` | 删除记录 |

---

## 项目结构

```
BookkeepingApp/
├── app/                     # Android 前端（Kotlin + AI Agent 生成）
│   └── src/main/java/
│       ├── data/
│       │   ├── local/       # Room 数据库、DAO
│       │   ├── model/       # 数据模型
│       │   └── repository/  # 数据仓库
│       └── ui/
│           ├── screens/     # 界面（记账、日历、统计、账户管理）
│           └── main/        # ViewModel + StateFlow
│
├── server/                  # Java Spring Boot 后端（手动搭建）
│   └── src/main/java/
│       ├── config/          # SecurityConfig
│       ├── controller/      # AuthController、RecordController
│       ├── dto/             # 请求/响应 DTO
│       ├── entity/          # User、Record（JPA 实体）
│       ├── repository/      # JPA Repository
│       ├── security/        # JwtUtil、JwtAuthenticationFilter
│       ├── service/         # AuthService、RecordService
│       └── exception/       # 全局异常处理
```

---

## 关于开发方式

本项目前端（`app/`）由 **AI Agent 辅助生成代码**，后端（`server/`）由我手动编写。`server_md/` 下的教学文档记录了完整的设计思路和实现步骤，方便他人学习和复现。

---

## 快速开始

### 后端

```bash
cd server
../gradlew.bat :server:bootRun
# 服务启动在 http://localhost:7070
```

### 前端

```bash
./gradlew.bat assembleDebug
# APK 输出：app/build/outputs/apk/debug/app-debug.apk
```
