# 记账本应用 Java 后端开发指南

> 本指南面向已掌握 Java 基础（接口、反射、动态代理）但尚未系统学习 Spring Boot 的开发者。
> 文中会用你已经理解的概念来解释 Spring Boot 的工作机制，而不是甩一堆新名词让你死记。

---

## 目录

1. [我们要做什么](#1-我们要做什么)
2. [前置知识：用你已经会的 Java 概念理解 Spring Boot](#2-前置知识用你已经会的-java-概念理解-spring-boot)
3. [环境准备](#3-环境准备)
4. [第一步：创建 Spring Boot 项目骨架](#4-第一步创建-spring-boot-项目骨架)
5. [第二步：设计数据库用户表（职责 1 —— BCrypt + Salt）](#5-第二步设计数据库用户表职责-1----bcrypt--salt)
6. [第三步：实现 JWT 令牌鉴权（职责 2）](#6-第三步实现-jwt-令牌鉴权职责-2)
7. [第四步：编写 RESTful 记账接口（职责 3 前半段）](#7-第四步编写-restful-记账接口职责-3-前半段)
8. [第五步：实现 Token 过期刷新（职责 3 后半段）](#8-第五步实现-token-过期刷新职责-3-后半段)
9. [第六步：封装全局异常处理（职责 4）](#9-第六步封装全局异常处理职责-4)
10. [第七步：运行与 Postman 测试](#10-第七步运行与-postman-测试)
11. [附录 A：完整项目文件清单](#11-附录-a完整项目文件清单)
12. [附录 B：常见问题排查](#12-附录-b常见问题排查)

---

## 1. 我们要做什么

### 1.1 当前项目的真实情况

你现在手里的 `BookkeepingApp` 是一个**纯 Android 前端应用**：

```
用户 ─── 直接操作手机 App ─── 本地 SQLite 数据库 (Room)
                                 ↑
                            所有数据都在手机里
                            没有任何网络请求
                            没有"登录"这个概念
```

它用的是 Kotlin + Jetpack Compose + Room（SQLite），**没有任何后端代码，也不是 Spring 生态**。所有记账数据存在手机本地，没有"用户账号"这个概念——谁打开 App 都能看到所有数据。

### 1.2 改造目标

新增加一个 Java Spring Boot 后端服务，架构变成：

```
用户 ─── Android App（前端） ─── HTTP请求 ─── Java Spring Boot 后端 ─── 数据库 (MySQL/H2)
                                                   │
                                        ┌──────────┼──────────┐
                                   用户注册/登录    JWT 鉴权    记账 CRUD
                                  (BCrypt加密)   (Token拦截)  (RESTful API)
```

### 1.3 你将学会什么

按本指南做完，你将亲手实现：

| 步骤 | 对应简历职责 | 涉及的核心技术 |
|------|-------------|---------------|
| 用户注册与登录 | 职责 1 | BCrypt 加密、数据库操作、salt 盐值 |
| JWT 拦截校验 | 职责 2 | JWT 令牌生成/解析、Spring 拦截器 |
| RESTful 记账接口 | 职责 3 | @RestController、分层架构、DTO |
| Token 过期刷新 | 职责 3 | refreshToken 机制、双 Token 设计 |
| 全局异常处理 | 职责 4 | @RestControllerAdvice、HTTP 状态码 |

---

## 2. 前置知识：用你已经会的 Java 概念理解 Spring Boot

你学过 Java 的**反射**、**动态代理**和**接口**，这三个东西恰恰是 Spring 框架的底层基石。在往下写代码之前，先用你的已有知识把 Spring Boot 的几个核心概念"翻译"一遍。

### 2.1 Spring 容器（IoC）≈ 一个自动帮你 `new` 对象的工厂

**你平时写 Java：**

```java
// 你需要一个 UserService，得自己 new
UserService userService = new UserService();
userService.register("zhangsan", "123456");
```

**Spring 的做法：**

你告诉 Spring"我以后可能需要一个 `UserService`"，Spring 会替你 new 好并保管起来。当你真的需要用它时，Spring 把这对象"注入"给你。这个过程叫**依赖注入（DI）**，存放这些对象的容器叫 **Spring 容器（IoC 容器）**。

```java
// 你不需要写 new UserService()，Spring 替你做了
@Autowired  // "Spring 大哥，帮我注入一个 UserService"
private UserService userService;

userService.register("zhangsan", "123456");
```

**类比理解：** 把 Spring 容器想象成一个超级 HashMap，key 是类名，value 是该类的实例。Spring Boot 启动时扫描你写了 `@Component` / `@Service` / `@Repository` 的类，自动 `new` 好放进这个 Map。

### 2.2 Spring AOP ≈ 你学过的动态代理 + 注解

你学 Java 动态代理时应该写过类似这样的代码：

```java
// 你学过的动态代理
InvocationHandler handler = new InvocationHandler() {
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println("执行前...");       // 前置增强
        Object result = method.invoke(target, args);  // 调用原始方法
        System.out.println("执行后...");       // 后置增强
        return result;
    }
};
```

Spring 的 AOP 本质上就是**把这个代理过程自动化了**——你只需要用注解标记"我要在这里加什么逻辑"，Spring 会在底层帮你生成代理对象。

比如本项目中，`@Transactional` 注解就是一个 AOP 应用：你标注后，Spring 自动在方法前后加上"开启事务"和"提交/回滚事务"的逻辑。

```java
// 你写的代码
@Transactional
public void transfer(Long fromId, Long toId, BigDecimal amount) {
    accountDao.decrease(fromId, amount);
    accountDao.increase(toId, amount);
}

// Spring AOP 底层帮你生成的"代理"相当于：
// public void transfer(...) {
//     connection.setAutoCommit(false);   ← Spring 帮你加的
//     try {
//         accountDao.decrease(...);       ← 你写的
//         accountDao.increase(...);       ← 你写的
//         connection.commit();            ← Spring 帮你加的
//     } catch (Exception e) {
//         connection.rollback();          ← Spring 帮你加的
//     }
// }
```

**本项目里你会遇到的 AOP 应用：**
- 拦截器（Interceptor）：拦截 HTTP 请求，校验 JWT Token — 和你在动态代理里做的"调用前检查"一模一样
- 全局异常处理（`@RestControllerAdvice`）：所有 Controller 抛出的异常统一在这里处理 — 相当于把 try-catch 从每个方法里抽出来集中管理

### 2.3 @RestController ≈ 标注"这个类负责处理 HTTP 请求"

```java
@RestController  // 告诉 Spring："我是一个 HTTP 接口处理器"
public class UserController {

    @PostMapping("/api/auth/login")   // 处理 POST 请求，路径是 /api/auth/login
    public ApiResponse login(@RequestBody LoginRequest req) {
        // @RequestBody 表示从 HTTP 请求的 body 里把 JSON 自动转成 LoginRequest 对象
        // 返回值 ApiResponse 会被 Spring 自动转成 JSON 写回 HTTP 响应
        return userService.login(req);
    }
}
```

`@PostMapping` / `@GetMapping` / `@PutMapping` / `@DeleteMapping` 对应 HTTP 的四种方法，分别用于增/查/改/删（RESTful 风格）。

### 2.4 你不需要理解底层也能用的 Spring Boot 特性

Spring Boot 的核心哲学是 **"约定大于配置"**。你引入一个 starter 依赖（比如 `spring-boot-starter-web`），它自动帮你配好 Tomcat 服务器、JSON 序列化、请求分发——你不需要写任何 XML 配置文件。

**一个类比：** 你写普通 Java 程序，需要自己 `javac` 编译、自己 `java` 启动、自己管理 classpath。用 IDE（比如 IDEA），点一下 Run 按钮就行——IDE 帮你处理了所有底层细节。**Spring Boot 就是后端开发的"IDE"**，它帮你处理了 Tomcat 配置、数据库连接池、JSON 转换等底层细节，让你专注于写业务逻辑。

---

## 3. 环境准备

### 3.1 必须安装的软件

| 软件 | 最低版本 | 用途 |
|------|---------|------|
| JDK | 17 或 21 | 编译运行 Java 代码 |
| IntelliJ IDEA | 社区版即可 | IDE（写代码） |
| Postman | 最新版 | 测试 HTTP 接口 |
| Gradle | 8.x（或直接用 IDEA 自带的 Gradle Wrapper） | 构建工具 |

### 3.2 确认 JDK 版本

打开终端/命令行，运行：

```bash
java -version
```

确保输出是 17 或以上。如果你用的是 17，本指南中的代码完全兼容。

### 3.3 本指南用到的关键 Maven 坐标（Gradle 写法）

在后续步骤中你会在 `build.gradle` 里看到这些依赖，先列出来供参考：

```groovy
// Spring Boot 核心 starter（包含 Tomcat、Spring MVC、JSON 序列化）
implementation 'org.springframework.boot:spring-boot-starter-web'

// Spring Security（提供 BCrypt 加密器，我们不开启默认登录页）
implementation 'org.springframework.boot:spring-boot-starter-security'

// JPA（操作数据库的 ORM 框架，底层是 Hibernate）
implementation 'org.springframework.boot:spring-boot-starter-data-jpa'

// JWT 库（生成和解析 JWT Token）
implementation 'io.jsonwebtoken:jjwt-api:0.12.5'
runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.12.5'
runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.12.5'

// MySQL 驱动（如果你用 MySQL 的话）
runtimeOnly 'com.mysql:mysql-connector-j'

// H2 数据库（如果你用内嵌数据库做开发测试的话）
runtimeOnly 'com.h2database:h2'

// Lombok（减少样板代码，getter/setter/构造器自动生成）
compileOnly 'org.projectlombok:lombok'
annotationProcessor 'org.projectlombok:lombok'
```

> **注意：** 上面的 Groovy 语法对应 Gradle Groovy DSL。如果你用 Gradle Kotlin DSL（`build.gradle.kts`），写法略有不同，本指南会在具体步骤给出 Kotlin DSL 写法。

### 3.4 Lombok 是什么？

你在本指南里会频繁看到 `@Data`、`@AllArgsConstructor`、`@NoArgsConstructor` 这些注解。它们来自一个叫 **Lombok** 的库，作用是在编译时自动生成 getter/setter/toString/构造器。

**没有 Lombok：**

```java
public class LoginRequest {
    private String username;
    private String password;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    // ...还要写 toString、equals、hashCode、构造器
}
```

**有 Lombok：**

```java
@Data
public class LoginRequest {
    private String username;
    private String password;
}
// 一行 @Data 等价于自动生成了上面所有样板代码
```

---

## 4. 第一步：创建 Spring Boot 项目骨架

### 4.1 理解项目结构

在开始之前，先理解一个标准 Spring Boot 项目的目录结构长什么样。以下是本项目的完整结构（你不需要一次性创建所有文件，按步骤来）：

```
server/                                    ← 后端模块根目录
├── build.gradle.kts                       ← Gradle 构建配置（依赖管理）
├── settings.gradle.kts                    ← Gradle 设置（项目名）
└── src/
    └── main/
        ├── java/com/example/bookkeeping/server/
        │   ├── BookkeepingServerApplication.java    ← Spring Boot 启动入口
        │   │
        │   ├── config/
        │   │   └── SecurityConfig.java              ← 安全配置（BCrypt Bean、拦截器注册）
        │   │
        │   ├── security/
        │   │   ├── JwtUtil.java                     ← JWT 工具类（生成/解析 Token）
        │   │   ├── JwtAuthenticationFilter.java     ← JWT 拦截过滤器（每个请求都经过它）
        │   │   └── UserDetailsServiceImpl.java      ← 从数据库查用户（Spring Security 需要）
        │   │
        │   ├── controller/
        │   │   ├── AuthController.java              ← 登录/注册/刷新 Token 接口
        │   │   └── RecordController.java            ← 记账 CRUD 接口
        │   │
        │   ├── service/
        │   │   ├── AuthService.java                 ← 登录/注册业务逻辑
        │   │   └── RecordService.java               ← 记账业务逻辑
        │   │
        │   ├── repository/
        │   │   ├── UserRepository.java              ← 用户数据访问（JPA）
        │   │   └── RecordRepository.java            ← 记账数据访问（JPA）
        │   │
        │   ├── entity/
        │   │   ├── User.java                        ← 用户表实体类
        │   │   └── Record.java                      ← 记账记录表实体类
        │   │
        │   ├── dto/
        │   │   ├── LoginRequest.java                ← 登录请求体
        │   │   ├── RegisterRequest.java             ← 注册请求体
        │   │   ├── TokenResponse.java               ← 登录/刷新后返回的 Token
        │   │   ├── RecordRequest.java               ← 新增/修改记录请求体
        │   │   └── ApiResponse.java                 ← 统一响应体（所有接口都用它返回）
        │   │
        │   └── exception/
        │       ├── BusinessException.java           ← 自定义业务异常
        │       ├── UnauthorizedException.java       ← 未登录异常
        │       └── GlobalExceptionHandler.java      ← 全局异常处理器
        │
        └── resources/
            ├── application.yml                      ← Spring Boot 核心配置文件
            └── schema.sql                           ← （可选）初始化数据库表结构
```

### 4.2 创建 Gradle 构建文件

#### 4.2.1 `server/settings.gradle.kts`

```kotlin
rootProject.name = "bookkeeping-server"
```

#### 4.2.2 `server/build.gradle.kts`

```kotlin
plugins {
    id("java")
    id("org.springframework.boot") version "3.3.2"
    id("io.spring.dependency-management") version "1.1.6"
}

group = "com.example.bookkeeping"
version = "1.0.0"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)  // 或 21
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot Web：内置 Tomcat，提供 REST API 能力
    implementation("org.springframework.boot:spring-boot-starter-web")

    // Spring Security：提供 BCrypt 加密器，我们手动配置，不开启默认表单登录
    implementation("org.springframework.boot:spring-boot-starter-security")

    // Spring Data JPA：ORM 框架，通过注解操作数据库，底层是 Hibernate
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    // H2 内嵌数据库（开发测试用，不需要安装 MySQL）
    runtimeOnly("com.h2database:h2")

    // JWT：生成和解析 JSON Web Token
    implementation("io.jsonwebtoken:jjwt-api:0.12.5")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.5")

    // Lombok：自动生成 getter/setter/构造器，减少样板代码
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // 测试
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
```

### 4.3 创建 Spring Boot 启动类

创建文件 `server/src/main/java/com/example/bookkeeping/server/BookkeepingServerApplication.java`：

```java
package com.example.bookkeeping.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BookkeepingServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(BookkeepingServerApplication.class, args);
    }
}
```

**这段代码做什么？**

`@SpringBootApplication` 是一个"组合注解"，等价于同时写了三个注解：
- `@Configuration`：告诉 Spring 这个类里可能有需要管理的 Bean
- `@EnableAutoConfiguration`：启动 Spring Boot 的自动配置（帮你配好 Tomcat、JSON 等）
- `@ComponentScan`：从这个类所在包开始，扫描所有 `@Component`/`@Service`/`@Controller`，自动放入 Spring 容器

`SpringApplication.run(...)` 启动整个 Spring Boot 应用，底层会启动内嵌 Tomcat 服务器。

### 4.4 配置文件

创建 `server/src/main/resources/application.yml`：

```yaml
server:
  port: 8080

spring:
  application:
    name: bookkeeping-server

  # H2 内嵌数据库配置（开发测试用）
  datasource:
    url: jdbc:h2:mem:bookkeeping           # 内存数据库，重启后数据丢失
    driver-class-name: org.h2.Driver
    username: sa
    password:
  h2:
    console:
      enabled: true                          # 开启 H2 网页控制台，访问 /h2-console
      path: /h2-console

  # JPA 配置
  jpa:
    hibernate:
      ddl-auto: update                       # 自动根据 Entity 类创建/更新表结构
    show-sql: true                           # 控制台输出 SQL 语句（调试用）
    properties:
      hibernate:
        format_sql: true                     # SQL 语句格式化输出

# JWT 自定义配置
jwt:
  secret: "YourSuperSecretKeyForJWT_MustBeAtLeast256BitsLong_ChangeThisInProduction"
  access-token-expiration: 1800000          # accessToken 有效期：30 分钟（毫秒）
  refresh-token-expiration: 604800000       # refreshToken 有效期：7 天（毫秒）
```

**配置说明：**

- `spring.datasource.url: jdbc:h2:mem:bookkeeping` — 使用 H2 内存数据库，数据在 JVM 关闭后消失。这意味着每次重启应用，所有注册用户和记账数据都会丢失。这是开发阶段故意为之的，方便你调试。后续如果真想持久化，换成 MySQL 的连接地址即可，代码不需要改。
- `spring.jpa.hibernate.ddl-auto: update` — Hibernate 启动时自动检查 `@Entity` 类和数据库表是否匹配，不匹配就自动修改表结构。生产环境应该改成 `validate`。
- `jwt.secret` — 用于签名 JWT 的密钥。实际部署一定要换成足够长的随机字符串。

### 4.5 验证骨架是否跑通

在 IDEA 中右键 `BookkeepingServerApplication.java` → Run。

控制台如果看到类似输出就表示成功了：

```
Started BookkeepingServerApplication in 2.5 seconds
```

如果没有报错，项目骨架就搭好了。

---

## 5. 第二步：设计数据库用户表（职责 1 —— BCrypt + Salt）

这一节实现简历中的第 1 条职责。

### 5.1 先搞清楚：为什么密码不能明文存储？

假设你的数据库被攻击者拿到了（SQL 注入、服务器被黑、内部人员泄密……）：

**如果存明文：**
```
users 表
┌────┬──────────┬──────────┐
│ id │ username │ password │
├────┼──────────┼──────────┤
│ 1  │ zhangsan │ 123456   │  ← 攻击者直接看到密码！
└────┴──────────┴──────────┘
```

**如果存 BCrypt 哈希：**
```
users 表
┌────┬──────────┬──────────────────────────────────────────────┬──────────────┐
│ id │ username │ password_hash                                 │ salt         │
├────┼──────────┼──────────────────────────────────────────────┼──────────────┤
│ 1  │ zhangsan │ $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p...   │ random_salt  │
└────┴──────────┴──────────────────────────────────────────────┴──────────────┘
```

攻击者拿到的是 `$2a$10$...` 这串东西，这是 BCrypt 的单向哈希，无法反推出原始密码。即使攻击者用彩虹表（预先算好的常见密码对应哈希表）来撞库，由于每个用户有独立的 salt（盐值），同一个密码在不同用户那里的哈希值也是不同的。

### 5.2 BCrypt 的原理（用你已经会的知识解释）

你可能会想："hash 不就是 `SHA256(密码)` 吗？为什么要 BCrypt？"

SHA256 是**快哈希**——一个 GPU 每秒能算几十亿次。攻击者可以用暴力枚举法快速尝试所有常见密码。

BCrypt 是**慢哈希**——它内部会循环迭代多次（次数由 `$2a$10$` 里的 `10` 控制，2^10 = 1024 次），而且这个迭代次数可以随着硬件进步而调大。攻击者暴力破解的速度被极大拖慢了。

**salt（盐值）的作用：** 两个用户设置相同密码 "123456"，如果不用 salt，他们的哈希值是一样的。攻击者破解了一个，就拿到了所有用这个密码的用户。加了 salt 后，即使密码相同，各自混入不同的 salt 后哈希值完全不同。

> **注意：** BCrypt 算法内部已经自带了 salt 生成——你调用 `BCrypt.hashpw(password, BCrypt.gensalt())` 时，`gensalt()` 会自动生成一个随机盐值。所以有些项目会省略单独的 `salt` 字段。**但你的简历里写了"salt 盐值字段"，所以我们保留它**——你可以把生成的随机 salt 单独存一列，或者直接用 BCrypt 的 salt 机制然后解释"BCrypt 内部已包含盐值"。

本指南采用后一种做法（BCrypt 自带 salt），同时保留 salt 列（用 UUID 作为用户唯一标识盐），这样简历描述完全对得上。

### 5.3 创建 User 实体类

创建 `server/src/main/java/com/example/bookkeeping/server/entity/User.java`：

```java
package com.example.bookkeeping.server.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    /**
     * 密码哈希值（BCrypt 加密后的结果）
     * 注意：这里存的是哈希，不是明文密码
     */
    @Column(name = "password_hash", nullable = false, length = 200)
    private String passwordHash;

    /**
     * 盐值（salt）—— 增强密码安全性
     * 每个用户拥有唯一的随机盐值，即使两个用户设置相同的明文密码，
     * 由于盐值不同，最终存储的哈希值也完全不同
     */
    @Column(nullable = false, length = 100)
    private String salt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // @PrePersist 是 JPA 的生命周期回调，在 insert 之前自动执行
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
```

**逐行解释：**

| 注解 | 作用 | 类比你学过的 |
|------|------|-------------|
| `@Entity` | 告诉 JPA："这个类对应数据库的一张表" | 就是一个标记，类似于 `Serializable` |
| `@Table(name = "users")` | 指定表名（不写则默认用类名小写） | — |
| `@Data` | Lombok 自动生成 getter/setter/toString/equals/hashCode | 省去手写样板代码 |
| `@NoArgsConstructor` | 生成无参构造器（JPA 要求必须有） | — |
| `@AllArgsConstructor` | 生成全参构造器 | — |
| `@Builder` | 生成 Builder 模式的建造器 | `User.builder().username("zhangsan").build()` |
| `@Id` | 主键 | — |
| `@GeneratedValue` | 主键自增（交给数据库生成） | — |
| `@Column(...)` | 配置列的属性（非空、唯一、长度等） | — |
| `@PrePersist` | 在 insert 到数据库之前自动调用的方法 | 类似于事件监听，触发时机在 save 之前 |

### 5.4 创建 UserRepository

创建 `server/src/main/java/com/example/bookkeeping/server/repository/UserRepository.java`：

```java
package com.example.bookkeeping.server.repository;

import com.example.bookkeeping.server.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 根据用户名查找用户
     * Spring Data JPA 会根据方法名自动生成 SQL：
     * SELECT * FROM users WHERE username = ?
     */
    Optional<User> findByUsername(String username);

    /**
     * 判断用户名是否已存在
     * 自动生成 SQL：SELECT COUNT(*) > 0 FROM users WHERE username = ?
     */
    boolean existsByUsername(String username);
}
```

**重要：你只需要声明接口，不需要写实现类！**

这是 Spring Data JPA 最强大的特性。`JpaRepository<User, Long>` 已经自带了 `save()`、`findById()`、`delete()`、`findAll()` 等基本方法。而你声明的 `findByUsername(String username)` —— Spring 在运行时会**动态生成实现类**（底层用的就是 Java 动态代理！），根据方法名自动推断 SQL。

方法名规则：`findBy` + `字段名（首字母大写）`，Spring 解析为 `WHERE 字段名 = ?`。

### 5.5 实现加密工具：SecurityConfig（提供 BCrypt Bean）

创建 `server/src/main/java/com/example/bookkeeping/server/config/SecurityConfig.java`：

```java
package com.example.bookkeeping.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityConfig {

    /**
     * 把 BCryptPasswordEncoder 注册为一个 Spring Bean
     *
     * "@Bean" 注解的含义：
     * "把这个方法的返回值放进 Spring 容器（那个超级 HashMap），
     *  以后别人用 @Autowired 注入 PasswordEncoder 时，拿到的就是这个对象。"
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCryptPasswordEncoder 的构造参数 "strength" 默认是 10
        // 表示 2^10 = 1024 次哈希迭代，越高越安全但越慢
        return new BCryptPasswordEncoder();
    }
}
```

一会儿在 `AuthService` 里，你会这样用它：

```java
@Autowired
private PasswordEncoder passwordEncoder;

// 注册时：把明文密码加密后存库
String hashedPassword = passwordEncoder.encode("123456");
// 输出类似：$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy

// 登录时：比对用户输入的明文和数据库的哈希是否匹配
boolean match = passwordEncoder.matches("123456", hashedPassword);  // true
```

### 5.6 创建 AuthService（用户注册和登录）

创建 `server/src/main/java/com/example/bookkeeping/server/service/AuthService.java`：

```java
package com.example.bookkeeping.server.service;

import com.example.bookkeeping.server.entity.User;
import com.example.bookkeeping.server.repository.UserRepository;
import com.example.bookkeeping.server.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service  // 告诉 Spring："这是一个业务逻辑类，请帮我管理它"
@RequiredArgsConstructor  // Lombok：自动生成"包含所有 final 字段"的构造器
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;  // 下一步会创建

    /**
     * 用户注册
     *
     * @param username 用户名
     * @param rawPassword 明文密码（用户输入的）
     */
    public void register(String username, String rawPassword) {
        // 1. 检查用户名是否已存在
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("用户名已被注册");
        }

        // 2. 生成唯一的随机盐值（使用 UUID）
        String salt = UUID.randomUUID().toString();

        // 3. 将 salt 混入密码后做 BCrypt 加密
        //    这样即使两个用户设置了完全相同的密码，由于 salt 不同，
        //    最终存入数据库的哈希值也完全不同
        String saltedPassword = rawPassword + salt;
        String passwordHash = passwordEncoder.encode(saltedPassword);

        // 4. 保存用户到数据库
        User user = User.builder()
                .username(username)
                .passwordHash(passwordHash)
                .salt(salt)
                .build();
        userRepository.save(user);
    }

    /**
     * 用户登录
     *
     * @param username 用户名
     * @param rawPassword 明文密码
     * @return TokenResponse 包含 accessToken 和 refreshToken
     */
    public String[] login(String username, String rawPassword) {
        // 1. 根据用户名查找用户
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户名或密码错误"));

        // 2. 用相同的 salt + 用户输入的密码，做 BCrypt 比对
        String saltedPassword = rawPassword + user.getSalt();
        if (!passwordEncoder.matches(saltedPassword, user.getPasswordHash())) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 3. 比对通过，生成 JWT 令牌
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getUsername());

        return new String[]{accessToken, refreshToken};
    }

    /**
     * 刷新 Token
     * 用户用旧的 refreshToken 换新的 accessToken + refreshToken
     */
    public String[] refreshToken(String refreshToken) {
        // 校验 refreshToken 是否合法、是否过期
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new RuntimeException("refreshToken 无效或已过期，请重新登录");
        }

        // 从 token 中提取用户信息
        Long userId = jwtUtil.getUserIdFromToken(refreshToken);
        String username = jwtUtil.getUsernameFromToken(refreshToken);

        // 签发新的令牌对
        String newAccessToken = jwtUtil.generateAccessToken(userId, username);
        String newRefreshToken = jwtUtil.generateRefreshToken(userId, username);

        return new String[]{newAccessToken, newRefreshToken};
    }
}
```

**`@Service` 和 `@RequiredArgsConstructor` 解析：**

`@Service` 只是语义化的 `@Component`，和 `@Component` 效果完全一样，只是名字上告诉读代码的人"这是一个业务逻辑层"。

`@RequiredArgsConstructor` 是 Lombok 注解，它会看你的类里有哪些 `final` 字段，然后**自动生成一个包含所有这些字段的构造器**。Spring 容器在创建 `AuthService` 时，发现它的构造器需要 `UserRepository`、`PasswordEncoder`、`JwtUtil` 三个参数，就会自动从容器里找到这三个 Bean 传进去——这就是**构造器注入**，是目前推荐的最佳实践。

等价的手写代码：

```java
// @RequiredArgsConstructor 帮你生成了这个构造器
public AuthService(UserRepository userRepository,
                   PasswordEncoder passwordEncoder,
                   JwtUtil jwtUtil) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtUtil = jwtUtil;
}
```

---

## 6. 第三步：实现 JWT 令牌鉴权（职责 2）

这一节实现简历中的第 2 条职责。

### 6.1 JWT 是什么？为什么用它？

**传统 Session 模式的问题：**

```
用户登录 → 服务器创建 Session → 返回 SessionId（Cookie）→ 浏览器每次请求自动带上 Cookie
```

问题在于：手机 App 不适用 Cookie。

**JWT（JSON Web Token）模式：**

```
用户登录 → 服务器签发一个加密的 Token 字符串
       → 前端把 Token 存起来（Android 用 SharedPreferences）
       → 每次请求时手动把 Token 放到 HTTP Header 里
       → 服务器解析 Token，验证签名，提取用户信息
```

JWT 的结构是 `Header.Payload.Signature`，三个 Base64 字符串用 `.` 连接：

```
eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjF9.4q4q-3q3q3q3q3q3q3q3q3q3q
     ↑                    ↑                  ↑
   Header              Payload           Signature
 (算法信息)          (用户数据)          (防篡改签名)
```

**关键安全特性：** 如果攻击者修改了 Payload（比如把 userId 从 1 改成 2），Signature 就对不上了——服务器解密时发现签名不匹配，直接拒绝请求。

### 6.2 创建 JwtUtil（JWT 工具类）

创建 `server/src/main/java/com/example/bookkeeping/server/security/JwtUtil.java`：

```java
package com.example.bookkeeping.server.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    // @Value 注解：从 application.yml 里读取配置注入到这里
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;  // 30分钟

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;  // 7天

    /**
     * 把配置文件里的密钥字符串转成加密算法需要的 SecretKey 对象
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 生成 accessToken（短期，用于访问业务接口）
     */
    public String generateAccessToken(Long userId, String username) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + accessTokenExpiration);

        return Jwts.builder()
                .subject(String.valueOf(userId))   // 主题 = 用户ID
                .claim("username", username)       // 自定义载荷：存用户名
                .issuedAt(now)                     // 签发时间
                .expiration(expiration)            // 过期时间
                .signWith(getSigningKey())         // 用密钥签名
                .compact();                        // 合成 Token 字符串
    }

    /**
     * 生成 refreshToken（长期，仅用于刷新 accessToken）
     */
    public String generateRefreshToken(Long userId, String username) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + refreshTokenExpiration);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .claim("type", "refresh")          // 标记这是 refreshToken
                .issuedAt(now)
                .expiration(expiration)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 从 Token 中提取所有 Claims（载荷信息）
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 从 Token 中获取用户 ID
     */
    public Long getUserIdFromToken(String token) {
        String subject = extractAllClaims(token).getSubject();
        return Long.parseLong(subject);
    }

    /**
     * 从 Token 中获取用户名
     */
    public String getUsernameFromToken(String token) {
        return extractAllClaims(token).get("username", String.class);
    }

    /**
     * 校验 Token 是否合法（签名是否正确、是否过期）
     *
     * @return true 表示 Token 有效，false 表示无效
     */
    public boolean validateToken(String token) {
        try {
            extractAllClaims(token);  // 如果签名不对或过期，会抛异常
            return true;
        } catch (ExpiredJwtException e) {
            // Token 过期 —— 不是非法篡改，只是时间到了
            System.err.println("JWT 已过期: " + e.getMessage());
            return false;
        } catch (JwtException e) {
            // 签名不对、格式错误等
            System.err.println("JWT 无效: " + e.getMessage());
            return false;
        }
    }
}
```

**逐段解释：**

| 代码段 | 解释 |
|--------|------|
| `@Value("${jwt.secret}")` | Spring 启动时从 `application.yml` 读取 `jwt.secret` 的值，注入到这个字段 |
| `Keys.hmacShaKeyFor(keyBytes)` | HMAC-SHA256 是一种对称签名算法：签发和验证用同一把密钥 |
| `Jwts.builder().subject(...)` | 构建 JWT 的 Payload（载荷）部分，subject 通常存用户ID |
| `.claim("username", username)` | 在载荷里添加自定义字段 |
| `.signWith(getSigningKey())` | 用密钥对 Header+Payload 做签名，生成 Signature 部分 |
| `Jwts.parser().verifyWith(...)` | 验证签名 + 解析 Payload，签名不对或 Token 过期会抛异常 |

### 6.3 创建 JWT 拦截过滤器

这个过滤器的作用：**每个 HTTP 请求到达 Controller 之前，先检查有没有合法的 JWT Token。有就放行，没有就返回 401。**

创建 `server/src/main/java/com/example/bookkeeping/server/security/JwtAuthenticationFilter.java`：

```java
package com.example.bookkeeping.server.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    /**
     * 这个方法会在每个 HTTP 请求到达 Controller 之前被调用
     *
     * OncePerRequestFilter：保证每个请求只被这个过滤器处理一次
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        // 1. 从请求头中取出 Authorization 字段
        String authHeader = request.getHeader("Authorization");

        // 2. 如果没有 Authorization 头，或者不是 "Bearer " 开头，直接放行
        //    （放行后由 Spring Security 配置决定是否需要登录）
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        // 3. 去掉 "Bearer " 前缀，得到纯 Token
        String token = authHeader.substring(7);

        // 4. 校验 Token 是否有效
        if (token.isEmpty() || !jwtUtil.validateToken(token)) {
            chain.doFilter(request, response);
            return;
        }

        // 5. Token 有效，从 Token 中提取用户信息
        Long userId = jwtUtil.getUserIdFromToken(token);
        String username = jwtUtil.getUsernameFromToken(token);

        // 6. 把用户信息"注入"到 Spring Security 的上下文中
        //    这样后续代码可以通过 SecurityContextHolder 拿到当前登录用户
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userId,      // principal（主体）
                        null,        // credentials（凭证，JWT 模式下不需要）
                        Collections.emptyList()  // authorities（权限列表）
                );
        authentication.setDetails(username);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 7. 放行，请求继续向下传递到 Controller
        chain.doFilter(request, response);
    }
}
```

**这段代码的流程：**

```
HTTP 请求到达 Tomcat
    │
    ▼
┌──────────────────────────┐
│ JwtAuthenticationFilter  │ ← 你写的过滤器
│ 检查 Authorization 头    │
│ 有合法 Token？            │
│   是 → 把用户信息放入     │
│        SecurityContext    │
│   否 → 不做处理          │
└──────────────────────────┘
    │
    ▼
┌──────────────────────────┐
│ Controller 处理请求       │
│ (这时候可以拿到当前用户)  │
└──────────────────────────┘
```

> **类比你学过的：** 这个 Filter 就是你学过的**动态代理模式**中的代理逻辑——在所有 Controller 方法的"前面"插入一段公共逻辑（Token 校验），而不是在每个方法里重复写校验代码。

### 6.4 更新 SecurityConfig（安全配置）

现在需要回到 `SecurityConfig.java`，加上两个重要配置：

1. 关闭 Spring Security 的默认 Session 机制（改为无状态 JWT 模式）
2. 把 `JwtAuthenticationFilter` 注册到过滤器链中
3. 配置哪些接口公开访问、哪些需要认证

更新 `server/src/main/java/com/example/bookkeeping/server/config/SecurityConfig.java`：

```java
package com.example.bookkeeping.server.config;

import com.example.bookkeeping.server.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * SecurityFilterChain 是 Spring Security 的核心配置
     * 这个方法定义了：哪些请求需要认证、哪些不需要、使用什么过滤器
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 关闭 CSRF 保护（前后端分离的 REST API 不需要 CSRF）
            .csrf(csrf -> csrf.disable())

            // 设置为无状态模式 —— 不创建 Session，每次请求都通过 JWT 独立验证
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // 配置接口访问权限
            .authorizeHttpRequests(auth -> auth
                // 以下路径不需要认证（公开访问）
                .requestMatchers("/api/auth/register").permitAll()
                .requestMatchers("/api/auth/login").permitAll()
                .requestMatchers("/api/auth/refresh").permitAll()
                .requestMatchers("/h2-console/**").permitAll()  // H2 数据库控制台
                .requestMatchers("/error").permitAll()           // Spring 默认错误页

                // 其他所有接口都需要认证（头里必须带合法的 JWT Token）
                .anyRequest().authenticated()
            )

            // 允许 H2 控制台使用 iframe（默认被 Spring Security 阻止）
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))

            // 把我们写的 JWT 过滤器插入到 Spring Security 过滤器链中
            // 位置：在 UsernamePasswordAuthenticationFilter 之前执行
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
```

**关键概念：**

- `SessionCreationPolicy.STATELESS`：告诉 Spring Security "不要创建 HTTP Session"。传统的 Web 应用用 Session 存用户信息，JWT 模式下每次请求自带凭证，不需要 Session。
- `.requestMatchers("/api/auth/**").permitAll()`：这三个接口（注册、登录、刷新 Token）不需要认证——用户还没登录呢，当然不能要求 Token。
- `.anyRequest().authenticated()`：除此之外的所有接口（比如记账 CRUD）都必须携带合法 Token。
- `.addFilterBefore(jwtAuthenticationFilter, ...)`：把你写的 JWT 过滤器插到 Spring Security 的认证过滤器之前。执行顺序：你的过滤器 → Spring Security 的权限校验 → Controller。

---

## 7. 第四步：编写 RESTful 记账接口（职责 3 前半段）

### 7.1 创建记账记录实体

创建 `server/src/main/java/com/example/bookkeeping/server/entity/Record.java`：

```java
package com.example.bookkeeping.server.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "records")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Record {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 所属用户ID（关联 users 表） */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** 收支类型：INCOME（收入）/ EXPENSE（支出） */
    @Column(nullable = false, length = 10)
    private String type;

    /** 金额 */
    @Column(nullable = false)
    private Double amount;

    /** 标签（如：餐饮、交通、工资） */
    @Column(nullable = false, length = 50)
    private String tag;

    /** 备注 */
    @Column(length = 200)
    private String note;

    /** 记账时间戳（毫秒） */
    @Column(nullable = false)
    private Long timestamp;

    /** 账户ID（对应原 App 的账户：现金/银行卡/支付宝/微信） */
    @Column(name = "account_id")
    private Long accountId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
```

### 7.2 创建 RecordRepository

创建 `server/src/main/java/com/example/bookkeeping/server/repository/RecordRepository.java`：

```java
package com.example.bookkeeping.server.repository;

import com.example.bookkeeping.server.entity.Record;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface RecordRepository extends JpaRepository<Record, Long> {

    /**
     * 查询某个用户在某个时间范围内的所有记录
     *
     * 方法名太长？用 @Query 直接写 JPQL（JPA 的 SQL 方言）
     * JPQL 用的是实体类名和字段名，不是数据库表名和列名
     */
    @Query("SELECT r FROM Record r WHERE r.userId = :userId " +
           "AND r.timestamp >= :startMillis AND r.timestamp <= :endMillis " +
           "ORDER BY r.timestamp DESC")
    List<Record> findByUserIdAndTimeRange(@Param("userId") Long userId,
                                          @Param("startMillis") Long startMillis,
                                          @Param("endMillis") Long endMillis);

    /** 查询某个用户的所有记录（按时间倒序） */
    List<Record> findByUserIdOrderByTimestampDesc(Long userId);

    /** 删除某条记录（只能删自己的） */
    void deleteByIdAndUserId(Long id, Long userId);
}
```

### 7.3 创建 RecordService

创建 `server/src/main/java/com/example/bookkeeping/server/service/RecordService.java`：

```java
package com.example.bookkeeping.server.service;

import com.example.bookkeeping.server.entity.Record;
import com.example.bookkeeping.server.exception.BusinessException;
import com.example.bookkeeping.server.repository.RecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecordService {

    private final RecordRepository recordRepository;

    /** 新增一条记录 */
    @Transactional  // 声明式事务：这个方法里的数据库操作在一个事务里执行
    public Record createRecord(Record record) {
        return recordRepository.save(record);
    }

    /** 查询用户的所有记录 */
    public List<Record> getUserRecords(Long userId) {
        return recordRepository.findByUserIdOrderByTimestampDesc(userId);
    }

    /** 按时间范围查询记录 */
    public List<Record> getUserRecordsByTimeRange(Long userId, Long startMillis, Long endMillis) {
        return recordRepository.findByUserIdAndTimeRange(userId, startMillis, endMillis);
    }

    /** 更新记录 */
    @Transactional
    public Record updateRecord(Long recordId, Long userId, Record updated) {
        Record existing = recordRepository.findById(recordId)
                .orElseThrow(() -> new BusinessException("记录不存在"));

        // 只能修改自己的记录
        if (!existing.getUserId().equals(userId)) {
            throw new BusinessException("无权修改此记录");
        }

        // 更新字段
        existing.setType(updated.getType());
        existing.setAmount(updated.getAmount());
        existing.setTag(updated.getTag());
        existing.setNote(updated.getNote());
        existing.setTimestamp(updated.getTimestamp());
        existing.setAccountId(updated.getAccountId());

        return recordRepository.save(existing);
    }

    /** 删除记录 */
    @Transactional
    public void deleteRecord(Long recordId, Long userId) {
        Record existing = recordRepository.findById(recordId)
                .orElseThrow(() -> new BusinessException("记录不存在"));

        if (!existing.getUserId().equals(userId)) {
            throw new BusinessException("无权删除此记录");
        }

        recordRepository.delete(existing);
    }
}
```

### 7.4 创建 DTO 类（数据传输对象）

DTO（Data Transfer Object）的作用：**定义前后端之间传递的数据格式**。你不应该把数据库实体（Entity）直接暴露给前端——那样会把数据库表结构泄露出去，而且 Entity 通常包含敏感字段（比如密码哈希）。

#### 7.4.1 统一响应体

创建 `server/src/main/java/com/example/bookkeeping/server/dto/ApiResponse.java`：

```java
package com.example.bookkeeping.server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 所有接口的统一响应格式
 *
 * 前端拿到响应后，先看 code 判断成功与否，再看 data 取数据
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private int code;       // 状态码（200 成功，401 未登录，400 参数错误...）
    private String message; // 提示信息
    private T data;         // 响应数据（泛型，可以是任何类型）

    // 快捷静态方法：成功
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "ok", data);
    }

    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(200, "ok", null);
    }

    // 快捷静态方法：失败
    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }
}
```

#### 7.4.2 登录/注册请求体

创建 `server/src/main/java/com/example/bookkeeping/server/dto/LoginRequest.java`：

```java
package com.example.bookkeeping.server.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String password;
}
```

创建 `server/src/main/java/com/example/bookkeeping/server/dto/RegisterRequest.java`：

```java
package com.example.bookkeeping.server.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String password;
}
```

#### 7.4.3 Token 响应体

创建 `server/src/main/java/com/example/bookkeeping/server/dto/TokenResponse.java`：

```java
package com.example.bookkeeping.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenResponse {
    private String accessToken;   // 短期令牌（30分钟），用于业务接口
    private String refreshToken;  // 长期令牌（7天），用于刷新 accessToken
    private String tokenType;     // 固定值 "Bearer"
    private long expiresIn;       // accessToken 有效秒数

    public static TokenResponse of(String accessToken, String refreshToken, long expiresInSeconds) {
        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(expiresInSeconds)
                .build();
    }
}
```

#### 7.4.4 记账记录请求体

创建 `server/src/main/java/com/example/bookkeeping/server/dto/RecordRequest.java`：

```java
package com.example.bookkeeping.server.dto;

import lombok.Data;

@Data
public class RecordRequest {
    private String type;        // INCOME 或 EXPENSE
    private Double amount;
    private String tag;
    private String note;
    private Long timestamp;
    private Long accountId;
}
```

### 7.5 创建 Controller 层

#### 7.5.1 AuthController（注册/登录/刷新 Token）

创建 `server/src/main/java/com/example/bookkeeping/server/controller/AuthController.java`：

```java
package com.example.bookkeeping.server.controller;

import com.example.bookkeeping.server.dto.ApiResponse;
import com.example.bookkeeping.server.dto.LoginRequest;
import com.example.bookkeeping.server.dto.RegisterRequest;
import com.example.bookkeeping.server.dto.TokenResponse;
import com.example.bookkeeping.server.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 注册接口
     *
     * 用 Postman 测试：POST http://localhost:8080/api/auth/register
     * Body (JSON): { "username": "zhangsan", "password": "123456" }
     */
    @PostMapping("/register")
    public ApiResponse<Void> register(@RequestBody RegisterRequest request) {
        authService.register(request.getUsername(), request.getPassword());
        return ApiResponse.success();
    }

    /**
     * 登录接口
     *
     * 用 Postman 测试：POST http://localhost:8080/api/auth/login
     * Body (JSON): { "username": "zhangsan", "password": "123456" }
     *
     * 返回 accessToken 和 refreshToken
     */
    @PostMapping("/login")
    public ApiResponse<TokenResponse> login(@RequestBody LoginRequest request) {
        String[] tokens = authService.login(request.getUsername(), request.getPassword());
        String accessToken = tokens[0];
        String refreshToken = tokens[1];

        // accessToken 有效期 30 分钟 = 1800 秒
        TokenResponse tokenResponse = TokenResponse.of(accessToken, refreshToken, 1800);
        return ApiResponse.success(tokenResponse);
    }

    /**
     * 刷新 Token 接口
     *
     * 用 Postman 测试：POST http://localhost:8080/api/auth/refresh
     * Body (JSON): { "refreshToken": "eyJhbGciOi..." }
     *
     * 注意：这里直接用 Map 接收，因为 refreshToken 不是 LoginRequest 的字段
     */
    @PostMapping("/refresh")
    public ApiResponse<TokenResponse> refreshToken(@RequestBody java.util.Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        String[] tokens = authService.refreshToken(refreshToken);
        String newAccess = tokens[0];
        String newRefresh = tokens[1];

        TokenResponse tokenResponse = TokenResponse.of(newAccess, newRefresh, 1800);
        return ApiResponse.success(tokenResponse);
    }
}
```

#### 7.5.2 RecordController（记账 CRUD）

创建 `server/src/main/java/com/example/bookkeeping/server/controller/RecordController.java`：

```java
package com.example.bookkeeping.server.controller;

import com.example.bookkeeping.server.dto.ApiResponse;
import com.example.bookkeeping.server.dto.RecordRequest;
import com.example.bookkeeping.server.entity.Record;
import com.example.bookkeeping.server.service.RecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/records")
@RequiredArgsConstructor
public class RecordController {

    private final RecordService recordService;

    /**
     * 通用方法：从 JWT 中获取当前登录用户的 ID
     * 这个 ID 是在 JwtAuthenticationFilter 里放进去的
     */
    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (Long) auth.getPrincipal();
    }

    /**
     * 新增记账记录
     *
     * 用 Postman 测试：POST http://localhost:8080/api/records
     * Header: Authorization: Bearer <accessToken>
     * Body (JSON): { "type": "EXPENSE", "amount": 35.5, "tag": "餐饮", "note": "午餐", ... }
     */
    @PostMapping
    public ApiResponse<Record> createRecord(@RequestBody RecordRequest request) {
        Long userId = getCurrentUserId();

        Record record = Record.builder()
                .userId(userId)
                .type(request.getType())
                .amount(request.getAmount())
                .tag(request.getTag())
                .note(request.getNote())
                .timestamp(request.getTimestamp())
                .accountId(request.getAccountId())
                .build();

        Record saved = recordService.createRecord(record);
        return ApiResponse.success(saved);
    }

    /**
     * 查询当前用户的所有记录
     */
    @GetMapping
    public ApiResponse<List<Record>> getRecords() {
        Long userId = getCurrentUserId();
        List<Record> records = recordService.getUserRecords(userId);
        return ApiResponse.success(records);
    }

    /**
     * 按时间范围查询记录
     */
    @GetMapping("/range")
    public ApiResponse<List<Record>> getRecordsByTime(
            @RequestParam Long start,
            @RequestParam Long end) {
        Long userId = getCurrentUserId();
        List<Record> records = recordService.getUserRecordsByTimeRange(userId, start, end);
        return ApiResponse.success(records);
    }

    /**
     * 更新记录
     */
    @PutMapping("/{id}")
    public ApiResponse<Record> updateRecord(@PathVariable Long id,
                                            @RequestBody RecordRequest request) {
        Long userId = getCurrentUserId();

        Record updated = Record.builder()
                .type(request.getType())
                .amount(request.getAmount())
                .tag(request.getTag())
                .note(request.getNote())
                .timestamp(request.getTimestamp())
                .accountId(request.getAccountId())
                .build();

        Record result = recordService.updateRecord(id, userId, updated);
        return ApiResponse.success(result);
    }

    /**
     * 删除记录
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteRecord(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        recordService.deleteRecord(id, userId);
        return ApiResponse.success();
    }
}
```

---

## 8. 第五步：实现 Token 过期刷新（职责 3 后半段）

这一步的代码其实已经在上面写完了，这里做一个**设计说明**，帮你理解为什么要这样设计。

### 8.1 双 Token 机制

```
accessToken（短期 30 分钟）
  ├── 用途：访问所有业务接口（记账、查询...）
  ├── 每次请求都携带在 Authorization 头里
  └── 过期后不能用，需要用 refreshToken 换新的

refreshToken（长期 7 天）
  ├── 用途：仅在 accessToken 过期后，换取新的令牌对
  ├── 只在 /api/auth/refresh 接口使用
  └── 过期后用户需要重新登录
```

### 8.2 为什么不用一个长有效期的 Token？

如果只用一个有效期 7 天的 Token，Token 一旦泄露（比如被中间人截获），攻击者在 7 天内都可以冒充你。双 Token 机制下：

- accessToken 30 分钟过期 — 即使被截获，攻击窗口只有 30 分钟
- refreshToken 只有 1 个接口使用 — 暴露面极小，且可以在服务端做额外校验（比如记录设备指纹）

### 8.3 前端（Android 端）使用流程

前端代码不在本指南范围，但理解其流程有助于你在面试时讲清楚：

```
1. 用户打开 App → 输入用户名密码 → POST /api/auth/login
2. 收到 accessToken + refreshToken → 存入 SharedPreferences
3. 每次请求都带上 Header: Authorization: Bearer <accessToken>
4. 如果后端返回 401（Token 过期）：
   → 用 refreshToken 调 POST /api/auth/refresh
   → 拿到新的 accessToken + refreshToken
   → 用新 accessToken 重试刚才失败的请求
5. 如果 refreshToken 也过期了（后端返回 401）：
   → 跳转到登录页，让用户重新登录
```

---

## 9. 第六步：封装全局异常处理（职责 4）

### 9.1 为什么不直接在 Controller 里 try-catch？

你可以这么写：

```java
@PostMapping("/login")
public ApiResponse<?> login(@RequestBody LoginRequest request) {
    try {
        String[] tokens = authService.login(request.getUsername(), request.getPassword());
        return ApiResponse.success(tokens);
    } catch (RuntimeException e) {
        return ApiResponse.error(401, e.getMessage());
    }
}
```

但如果每个接口都这么写一遍，代码大量重复。而且 HTTP 状态码需要统一管理（401 代表未认证，400 代表参数错误，404 代表资源不存在……）。

**更好的做法：** 让异常"飞"到统一的处理中心 — 这就是 `@RestControllerAdvice` 做的事。它本质上是 AOP（你学过的动态代理思想）：Spring 在每个 Controller 方法外面包一层代理，如果方法抛出了异常，代理捕获后转交给 `@RestControllerAdvice` 标注的类来处理。

### 9.2 创建自定义异常类

创建 `server/src/main/java/com/example/bookkeeping/server/exception/UnauthorizedException.java`：

```java
package com.example.bookkeeping.server.exception;

/**
 * 未登录/Token无效异常 — 统一返回 401
 */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
```

创建 `server/src/main/java/com/example/bookkeeping/server/exception/BusinessException.java`：

```java
package com.example.bookkeeping.server.exception;

/**
 * 业务异常 — 统一返回 400
 * 比如：用户名已存在、记录不存在、无权操作等
 */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
```

### 9.3 创建全局异常处理器

创建 `server/src/main/java/com/example/bookkeeping/server/exception/GlobalExceptionHandler.java`：

```java
package com.example.bookkeeping.server.exception;

import com.example.bookkeeping.server.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 *
 * @RestControllerAdvice = @ControllerAdvice + @ResponseBody
 * 意思：拦截所有 Controller 抛出的异常，把处理结果转成 JSON 写回响应
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理未登录 / Token 无效异常 → 返回 401
     *
     * @ExceptionHandler 指定这个方法处理哪种异常
     * @ResponseStatus 指定返回的 HTTP 状态码
     */
    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)  // 401
    public ApiResponse<Void> handleUnauthorized(UnauthorizedException e) {
        return ApiResponse.error(401, e.getMessage());
    }

    /**
     * 处理业务异常 → 返回 400
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)  // 400
    public ApiResponse<Void> handleBusiness(BusinessException e) {
        return ApiResponse.error(400, e.getMessage());
    }

    /**
     * 兜底：处理所有未被上面捕获的异常 → 返回 500
     * 避免直接把异常堆栈暴露给前端
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)  // 500
    public ApiResponse<Void> handleUnknown(Exception e) {
        // 实际项目中应该把 e.printStackTrace() 换成日志框架记录
        e.printStackTrace();
        return ApiResponse.error(500, "服务器内部错误");
    }
}
```

### 9.4 改造之前的代码，使用自定义异常

现在回到前面写的代码，把 `throw new RuntimeException(...)` 替换为具体的自定义异常：

**在 `AuthService` 中：**

```java
// 原来：throw new RuntimeException("用户名已被注册");
// 改成：
throw new BusinessException("用户名已被注册");

// 原来：throw new RuntimeException("用户名或密码错误");
// 改成：
throw new BusinessException("用户名或密码错误");

// 原来：throw new RuntimeException("refreshToken 无效或已过期，请重新登录");
// 改成：
throw new UnauthorizedException("refreshToken 无效或已过期，请重新登录");
```

**在 `JwtAuthenticationFilter` 中：** 过滤器里不抛异常（抛了也没法被 `@RestControllerAdvice` 捕获，因为 Filter 在 Spring MVC 之前执行）。过滤器的处理方式是"不放行就用 response 直接写错误信息"。

我们可以改进一下 `JwtAuthenticationFilter`，当 Token 无效时直接返回 401：

```java
// 在 doFilterInternal 方法中，将原来的 chain.doFilter(request, response) 改为：

// Token 存在但无效时，直接返回 401 错误，不再继续往后走
if (authHeader != null && authHeader.startsWith("Bearer ")) {
    String token = authHeader.substring(7);
    if (!token.isEmpty() && !jwtUtil.validateToken(token)) {
        // Token 无效或已过期，直接返回 401
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":401,\"message\":\"Token已过期或无效，请重新登录\",\"data\":null}");
        return;  // 不再调用 chain.doFilter()，请求到此为止
    }
}
```

---

## 10. 第七步：运行与 Postman 测试

### 10.1 启动应用

在 IDEA 中右键 `BookkeepingServerApplication.java` → Run。

看到以下日志表示启动成功：

```
Tomcat started on port 8080
Started BookkeepingServerApplication in 3.5 seconds
```

### 10.2 Postman 测试流程

#### 测试 1：注册用户

```
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
    "username": "zhangsan",
    "password": "123456"
}
```

预期响应：
```json
{
    "code": 200,
    "message": "ok",
    "data": null
}
```

#### 测试 2：登录

```
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
    "username": "zhangsan",
    "password": "123456"
}
```

预期响应：
```json
{
    "code": 200,
    "message": "ok",
    "data": {
        "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
        "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
        "tokenType": "Bearer",
        "expiresIn": 1800
    }
}
```

**复制 `accessToken` 的值**，下一步要用。

#### 测试 3：不带 Token 访问记账接口（验证 401）

```
POST http://localhost:8080/api/records
Content-Type: application/json

{
    "type": "EXPENSE",
    "amount": 35.5,
    "tag": "餐饮",
    "note": "午餐",
    "timestamp": 1752192000000
}
```

预期响应：HTTP 状态码 **401**（因为没带 Token，被 Spring Security 拦截了）

#### 测试 4：带 Token 新增记录（验证鉴权通过）

```
POST http://localhost:8080/api/records
Content-Type: application/json
Authorization: Bearer <把刚才复制的 accessToken 粘贴到这里>

{
    "type": "EXPENSE",
    "amount": 35.5,
    "tag": "餐饮",
    "note": "午餐",
    "timestamp": 1752192000000
}
```

预期响应：
```json
{
    "code": 200,
    "message": "ok",
    "data": {
        "id": 1,
        "userId": 1,
        "type": "EXPENSE",
        "amount": 35.5,
        "tag": "餐饮",
        "note": "午餐",
        "timestamp": 1752192000000,
        ...
    }
}
```

#### 测试 5：查询所有记录

```
GET http://localhost:8080/api/records
Authorization: Bearer <accessToken>
```

#### 测试 6：刷新 Token

```
POST http://localhost:8080/api/auth/refresh
Content-Type: application/json

{
    "refreshToken": "<之前登录拿到的 refreshToken>"
}
```

预期响应：返回新的 `accessToken` 和 `refreshToken`。

### 10.3 查看数据库

启动应用后，浏览器访问 `http://localhost:8080/h2-console`：

- JDBC URL: `jdbc:h2:mem:bookkeeping`
- User Name: `sa`
- Password: （留空）

点击 Connect，可以看到 `USERS` 表（里面有 `password_hash` 和 `salt` 字段）和 `RECORDS` 表。

---

## 11. 附录 A：完整项目文件清单

按创建顺序汇总你需要在 `server/` 目录下新建的所有文件：

```
server/
├── settings.gradle.kts
├── build.gradle.kts
└── src/main/
    ├── java/com/example/bookkeeping/server/
    │   ├── BookkeepingServerApplication.java
    │   ├── config/
    │   │   └── SecurityConfig.java
    │   ├── entity/
    │   │   ├── User.java
    │   │   └── Record.java
    │   ├── repository/
    │   │   ├── UserRepository.java
    │   │   └── RecordRepository.java
    │   ├── dto/
    │   │   ├── ApiResponse.java
    │   │   ├── LoginRequest.java
    │   │   ├── RegisterRequest.java
    │   │   ├── TokenResponse.java
    │   │   └── RecordRequest.java
    │   ├── security/
    │   │   ├── JwtUtil.java
    │   │   └── JwtAuthenticationFilter.java
    │   ├── service/
    │   │   ├── AuthService.java
    │   │   └── RecordService.java
    │   ├── controller/
    │   │   ├── AuthController.java
    │   │   └── RecordController.java
    │   └── exception/
    │       ├── UnauthorizedException.java
    │       ├── BusinessException.java
    │       └── GlobalExceptionHandler.java
    └── resources/
        └── application.yml
```

**总计：19 个文件**（含 Gradle 配置）。

---

## 12. 附录 B：常见问题排查

### 12.1 端口被占用

错误信息：`Port 8080 was already in use`

解决方法：在 `application.yml` 中改端口：

```yaml
server:
  port: 8081
```

或者在启动前先杀掉占用端口的进程：

```bash
# Windows
netstat -ano | findstr :8080
taskkill /PID <进程ID> /F

# Mac/Linux
lsof -i :8080
kill -9 <PID>
```

### 12.2 Lombok 不生效

如果 IDEA 提示 `cannot find symbol: method getUsername()` 之类的错误：

1. 确认 IDEA 安装了 Lombok 插件（Settings → Plugins → 搜索 Lombok）
2. 确认 Settings → Build → Compiler → Annotation Processors → "Enable annotation processing" 已勾选

### 12.3 JWT 密钥长度不够

错误信息：`The signing key's algorithm 'HmacSHA256' requires a key with size of at least 256 bits`

解决方法：确保 `application.yml` 中 `jwt.secret` 的值足够长（至少 32 个字符）：

```yaml
jwt:
  secret: "ThisIsAVeryLongSecretKeyForJWT_AtLeast32Characters_12345678"
```

### 12.4 H2 数据库表不显示

确保 `application.yml` 中 H2 控制台已开启：

```yaml
spring:
  h2:
    console:
      enabled: true
```

并且 `SecurityConfig` 中放行了 `/h2-console/**` 路径。

### 12.5 BCrypt 加密太慢

默认 `BCryptPasswordEncoder(10)` 在你的机器上可能耗时 100-200ms。如果觉得慢，可以在 `SecurityConfig` 中降低强度：

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(6);  // 2^6 = 64 次迭代，更快但不那么安全
}
```

**开发阶段调低一点没关系**，生产环境建议不低于 10。

---

## 结语

按本指南做完，你将拥有：

1. 一个能跑起来的 Spring Boot 后端项目
2. 支持 BCrypt + Salt 加密的用户注册/登录
3. JWT 双 Token 鉴权机制
4. RESTful 风格的记账 CRUD 接口
5. 全局异常处理（401/400/500）
6. 可以通过 Postman 演示的完整接口

这些内容完美覆盖了简历里的 4 条核心职责。

编写代码的过程中，遇到任何具体问题，随时问我。

---

## 13. 附录 C：本项目所有注解速查手册

> 你说学过注解的开发原理（定义注解 → 反射读取 → 根据注解做不同处理），下面每个注解都会解释 Spring 在底层用反射对它做了什么。
> 你已经认识的 `@Override`、`@Test`、`@Data`、`@AllArgsConstructor`、`@NoArgsConstructor` 不在本节重复。

### 13.1 Spring 核心注解

---

#### `@SpringBootApplication`

| 属性 | 说明 |
|------|------|
| 贴在 | 类上（启动类） |
| 作用 | 组合注解，等价于同时写 `@Configuration` + `@EnableAutoConfiguration` + `@ComponentScan` |
| 反射原理 | `@EnableAutoConfiguration` 触发 Spring Boot 的自动配置机制——Spring Boot 启动时会读取 classpath 下的 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 文件（旧版是 `spring.factories`），里面列了 100+ 个自动配置类。Spring 用反射**批量加载这些类**，每个类上有 `@ConditionalOnClass`、`@ConditionalOnMissingBean` 等条件注解，Spring 再用反射检查条件是否满足，满足就自动创建对应的 Bean（比如你引入了 `spring-boot-starter-web`，它自动配好 Tomcat） |

类比你学过的：相当于在程序入口写了一句 "扫描我所在的包，把所有带特殊注解的类都找出来并自动 new 好"。

---

#### `@Component`

| 属性 | 说明 |
|------|------|
| 贴在 | 类上 |
| 作用 | 告诉 Spring："我是一个组件，请把我放进容器管理" |
| 反射原理 | `@ComponentScan` 扫描时，Spring 用 `Class.forName()` 加载每个类，用 `Class.isAnnotationPresent(Component.class)` 检测是否有 `@Component`（或其衍生注解 `@Service`、`@Repository`、`@Controller`）。检测到后，用反射 `Constructor.newInstance()` 创建实例，放入容器 |

**衍生注解（本质相同，只是语义不同）：**

| 注解 | 等价于 | 唯一区别 |
|------|--------|---------|
| `@Service` | `@Component` | 名字上告诉人"这是业务逻辑层" |
| `@Repository` | `@Component` | 名字上告诉人"这是数据访问层"，Spring 额外给它加了持久层异常翻译 |
| `@Controller` | `@Component` | 名字上告诉人"这是 Web 控制器" |
| `@RestController` | `@Controller` + `@ResponseBody` | 所有方法的返回值直接序列化为 JSON，不跳转页面 |

---

#### `@Configuration`

| 属性 | 说明 |
|------|------|
| 贴在 | 类上 |
| 作用 | 告诉 Spring："这个类里定义了需要管理的 Bean（用 `@Bean` 标注的方法返回值）" |
| 反射原理 | Spring 扫描到 `@Configuration` 类后，对这个类创建 CGLIB 代理（**底层就是动态代理**），保证无论你调用多少次 `@Bean` 方法，同一个 Bean 只创建一次（单例）。然后用反射调用所有带 `@Bean` 的方法，把返回值存入容器 |

---

#### `@Bean`

| 属性 | 说明 |
|------|------|
| 贴在 | 方法上（必须是 `@Configuration` 类里的方法） |
| 作用 | 声明"这个方法的返回值是一个 Bean，请放进容器管理" |
| 反射原理 | Spring 用 `Method.invoke()` 调用这个方法，拿到返回值（比如 `new BCryptPasswordEncoder()`），以方法名作为 key（或 `@Bean("name")` 指定的名字），存入容器的 HashMap |

**为什么 `SecurityConfig` 里要用 `@Bean` 而不是直接 `new`？**

如果你自己 `new BCryptPasswordEncoder()`，每个用到的地方都有一个独立实例。通过 `@Bean` 注册后，整个应用共享同一个实例（单例），而且 Spring 可以管理它的生命周期、做 AOP 代理。

---

#### `@Value`

| 属性 | 说明 |
|------|------|
| 贴在 | 字段上 |
| 作用 | 从配置文件（`application.yml` 或 `application.properties`）中读取值，注入到字段 |
| 反射原理 | Spring 创建 Bean 后，用 `Field.setAccessible(true)` 突破 private 限制，再用 `Field.set(beanInstance, 配置值)` 把值塞进去。这就是你学过的**反射绕过访问控制** |

```java
@Value("${jwt.secret}")
private String secret;  // Spring 用反射帮你从 yml 里读出来，set 进去
```

---

#### `@Autowired`（本项目暂未使用，但面试必问）

| 属性 | 说明 |
|------|------|
| 贴在 | 构造器 / 字段 / setter 上 |
| 作用 | 告诉 Spring："我需要这个类型的 Bean，请帮我注入" |
| 反射原理 | 字段注入：`Field.setAccessible(true)` → `Field.set(bean, dependency)`。构造器注入：Spring 看构造器参数类型，去容器里找匹配的 Bean，然后用 `Constructor.newInstance(dep1, dep2, ...)` 创建对象 |

**本项目用 `@RequiredArgsConstructor`（Lombok 生成含 final 字段的构造器）替代了 `@Autowired`。** Spring 发现类只有一个构造器时，会自动用这个构造器注入——这叫做"隐式构造器注入"，无需写 `@Autowired`。

---

### 13.2 Spring MVC 注解（Web 层）

---

#### `@RestController`

| 属性 | 说明 |
|------|------|
| 贴在 | 类上 |
| 作用 | 声明这是一个 REST 控制器，所有方法的返回值都自动转 JSON |
| 反射原理 | Spring MVC 初始化时扫描所有 Controller，把 `@RequestMapping` 路径和方法的映射关系注册到一张**路由表**（`HandlerMapping`）里。请求到达时，Tomcat 把 URL 交给 Spring MVC，Spring MVC 在路由表里匹配，匹配到后用 `Method.invoke(controllerInstance, args)` 调用对应方法 |

---

#### `@RequestMapping`

| 属性 | 说明 |
|------|------|
| 贴在 | 类 或 方法上 |
| 作用 | 指定 URL 路径前缀（类上）或具体路径（方法上） |
| 反射原理 | 同上——Spring 用 `Method.getAnnotation(RequestMapping.class)` 读取 value 属性，存入路由表 |

---

#### `@PostMapping` / `@GetMapping` / `@PutMapping` / `@DeleteMapping`

| 属性 | 说明 |
|------|------|
| 贴在 | 方法上 |
| 作用 | 等价于 `@RequestMapping(method = RequestMethod.POST/GET/PUT/DELETE)`，是简写形式 |
| 对应语义 | POST = 新增（Create）、GET = 查询（Read）、PUT = 更新（Update）、DELETE = 删除（Delete）——合称 CRUD |
| 反射原理 | 和 `@RequestMapping` 完全一样，只是多了 HTTP 方法的限制。匹配路由时，不仅 URL 要对上，HTTP 方法也要对上 |

```java
@GetMapping("/users")       // 等价于 @RequestMapping(value = "/users", method = RequestMethod.GET)
@PostMapping("/users")      // 等价于 @RequestMapping(value = "/users", method = RequestMethod.POST)
@PutMapping("/users/{id}")  // 等价于 @RequestMapping(value = "/users/{id}", method = RequestMethod.PUT)
@DeleteMapping("/users/{id}") // 等价于 @RequestMapping(value = "/users/{id}", method = RequestMethod.DELETE)
```

---

#### `@RequestBody`

| 属性 | 说明 |
|------|------|
| 贴在 | 方法参数上 |
| 作用 | 告诉 Spring："把 HTTP 请求的 Body（JSON 字符串）反序列化成这个参数类型的 Java 对象" |
| 反射原理 | Spring 读取 `Content-Type` 头确认是 `application/json`，然后用 Jackson 库（底层是 `ObjectMapper.readValue(jsonString, TargetClass.class)`）将 JSON 转成 Java 对象。`TargetClass.class` 是通过反射从方法参数上拿到的 `Parameter.getType()` |

```java
// 请求 Body: {"username": "zhangsan", "password": "123456"}
// Spring 帮你做了：LoginRequest req = new ObjectMapper().readValue(bodyString, LoginRequest.class);
@PostMapping("/login")
public ApiResponse login(@RequestBody LoginRequest request) { ... }
```

---

#### `@RequestParam`

| 属性 | 说明 |
|------|------|
| 贴在 | 方法参数上 |
| 作用 | 从 URL 的查询参数（`?key=value`）中取值，绑定到方法参数 |
| 反射原理 | Spring 用 `HttpServletRequest.getParameter("参数名")` 获取 URL 查询参数的值，再用类型转换器（String → Long、String → Integer 等）转成目标类型，最后传给方法 |

```java
// 请求: GET /api/records/range?start=1750000000&end=1760000000
// Spring 自动把 ?start=1750000000 提取出来，转成 Long，传给 start 参数
@GetMapping("/range")
public ApiResponse getRecordsByTime(@RequestParam Long start, @RequestParam Long end) { ... }
```

---

#### `@PathVariable`

| 属性 | 说明 |
|------|------|
| 贴在 | 方法参数上 |
| 作用 | 从 URL 路径中的占位符 `{变量名}` 提取值，绑定到方法参数 |
| 反射原理 | Spring 匹配路由时（比如 `/api/records/5` 匹配到 `/api/records/{id}`），用正则提取 `{id}` 对应的值 `5`，再转成参数类型后传入 |

```java
// 请求: PUT /api/records/5
// Spring 从 URL 路径中提取 {id} → 5，转成 Long，传给 id 参数
@PutMapping("/{id}")
public ApiResponse updateRecord(@PathVariable Long id, @RequestBody RecordRequest request) { ... }
```

---

#### `@ExceptionHandler`

| 属性 | 说明 |
|------|------|
| 贴在 | 方法上（必须在 `@RestControllerAdvice` 标注的类中） |
| 作用 | 声明"我处理这种类型的异常" |
| 反射原理 | Spring AOP 在每个 Controller 方法外包裹了代理。当 Controller 方法抛出异常时，代理**用 `instanceof` 匹配异常类型**，找到对应的 `@ExceptionHandler` 方法，用 `Method.invoke()` 调用它，拿到返回值写回 HTTP 响应 |

---

#### `@ResponseStatus`

| 属性 | 说明 |
|------|------|
| 贴在 | 方法上（配合 `@ExceptionHandler`） |
| 作用 | 指定 HTTP 响应的状态码 |
| 反射原理 | `@ExceptionHandler` 方法返回后，Spring 读取 `@ResponseStatus` 的 value，调用 `HttpServletResponse.setStatus(code)` 设置状态码 |

---

#### `@RestControllerAdvice`

| 属性 | 说明 |
|------|------|
| 贴在 | 类上 |
| 作用 | `@ControllerAdvice` + `@ResponseBody`，全局拦截所有 Controller 的异常 |
| 反射原理 | Spring 启动时扫描到这个类，把它注册为"全局异常处理器"。运行时每个 Controller 方法被 AOP 代理包裹，抛异常时代理先检查 Controller 自身有没有 `@ExceptionHandler`，没有就交给全局的 `@RestControllerAdvice` 类 |

---

### 13.3 Spring Security 注解

---

#### `@EnableWebSecurity`

| 属性 | 说明 |
|------|------|
| 贴在 | 类上（必须在 `@Configuration` 类上） |
| 作用 | 启用 Spring Security 的 Web 安全功能。这个注解会导入 Spring Security 的核心配置类 |
| 反射原理 | `@EnableWebSecurity` 上有一个 `@Import` 注解，`@Import` 会触发 Spring 用反射加载指定的配置类。同时它标记了 `@EnableGlobalAuthentication`，告诉 Spring Security"启动认证机制" |

---

### 13.4 Spring Data JPA 注解（数据库操作）

---

#### `@Entity`

| 属性 | 说明 |
|------|------|
| 贴在 | 类上 |
| 作用 | 告诉 JPA："这个类对应数据库的一张表" |
| 反射原理 | Hibernate 启动时用 `Class.isAnnotationPresent(Entity.class)` 找到所有实体类，用 `Field.getAnnotations()` 读取每个字段上的 `@Column`、`@Id` 等注解，自动生成建表 SQL（`CREATE TABLE ...`）和字段映射。这就是你学过的**注解 + 反射 → 自动化生成代码**的典型案例 |

---

#### `@Table`

| 属性 | 说明 |
|------|------|
| 贴在 | 类上 |
| 作用 | 指定对应的数据库表名。不写则默认用类名小写（`User` → `users`） |
| 反射原理 | Hibernate 用 `Class.getAnnotation(Table.class).name()` 拿表名，拼到 SQL 里 |

---

#### `@Id` / `@GeneratedValue`

| 属性 | 说明 |
|------|------|
| 贴在 | 字段上 |
| 作用 | `@Id` 标记主键；`@GeneratedValue` 标记主键由数据库自动生成 |
| 反射原理 | Hibernate insert 时：先检查 `@GeneratedValue` 的 strategy（IDENTITY = 自增、AUTO = Hibernate 选择），然后选择对应的 ID 生成策略。JPA 通过 `Field.set(entity, generatedId)` 把生成的 ID 写回对象 |

---

#### `@Column`

| 属性 | 说明 |
|------|------|
| 贴在 | 字段上 |
| 作用 | 配置列属性：列名（`name`）、是否非空（`nullable`）、最大长度（`length`）、是否唯一（`unique`）等 |
| 反射原理 | Hibernate 建表时用 `Field.getAnnotation(Column.class)` 读取这些属性，生成对应的 DDL 约束（`NOT NULL`、`VARCHAR(50)`、`UNIQUE` 等） |

---

#### `@PrePersist`

| 属性 | 说明 |
|------|------|
| 贴在 | 方法上 |
| 作用 | JPA 生命周期回调——在 `entityManager.persist()`（即 insert）执行之前自动调用 |
| 反射原理 | Hibernate 在 save 之前，用 `Method.isAnnotationPresent(PrePersist.class)` 找到标记了 `@PrePersist` 的方法，用 `Method.invoke(entity)` 执行它 |

---

#### `@Query`

| 属性 | 说明 |
|------|------|
| 贴在 | 方法上（在 Repository 接口中） |
| 作用 | 手写 JPQL（JPA 的类 SQL 方言）查询语句，替代自动方法名解析 |
| 反射原理 | Spring Data JPA 发现方法上有 `@Query` 时，不再解析方法名，而是直接拿 `@Query` 的 value 作为 JPQL 语句。运行时用 `Query.getAnnotation(Param.class).value()` 匹配 `:参数名` 占位符和方法参数上的 `@Param("参数名")`，完成参数绑定 |

---

#### `@Param`

| 属性 | 说明 |
|------|------|
| 贴在 | 方法参数上（配合 `@Query` 使用） |
| 作用 | 把方法参数绑定到 JPQL 语句中的 `:参数名` 占位符 |
| 反射原理 | Spring 用 `Parameter.getAnnotation(Param.class).value()` 获取参数名，用它去 JPQL 字符串里做 `replace(":参数名", 实际值)`，不过实际做的是参数化查询（`PreparedStatement`）防止 SQL 注入 |

---

#### `@Transactional`

| 属性 | 说明 |
|------|------|
| 贴在 | 方法 或 类上 |
| 作用 | 声明式事务管理——方法执行前自动开启事务，正常返回则提交，抛异常则回滚 |
| 反射原理 | **这是 AOP（动态代理）的典型应用。** Spring 为 `@Service` 类创建 JDK 动态代理（因为 Service 实现了接口就用 JDK 代理，没实现接口就用 CGLIB 代理）。代理逻辑相当于： |

```java
// 你写的
@Transactional
public void save(Record r) {
    repo.save(r);
}

// Spring AOP 底层帮你生成的代理逻辑（伪代码）：
public void save(Record r) {
    Transaction tx = entityManager.getTransaction();
    try {
        tx.begin();                          // ← AOP 加的前置逻辑
        repo.save(r);                       // ← 你写的
        tx.commit();                        // ← AOP 加的后置逻辑
    } catch (Exception e) {
        tx.rollback();                      // ← AOP 加的异常处理
        throw e;
    }
}
```

> 这就是你学过的 `InvocationHandler.invoke()` 在实战中的样子。

---

### 13.5 Lombok 注解（编译期注解）

> 以下注解你已认识：`@Data`、`@AllArgsConstructor`、`@NoArgsConstructor`

---

#### `@Builder`

| 属性 | 说明 |
|------|------|
| 贴在 | 类上 |
| 作用 | 自动生成 Builder 建造者模式的内部类和静态方法 |
| 与反射的关系 | Lombok 是**编译期注解**（不是运行时），在 `javac` 编译阶段直接修改 AST（抽象语法树），把 Builder 相关代码"写入"字节码。运行时已经看不到注解了，只有生成的代码 |

使用效果：

```java
// 不使用 Builder（传统写法）
Record r = new Record();
r.setUserId(1L);
r.setType("EXPENSE");
r.setAmount(35.5);
r.setTag("餐饮");
r.setNote("午餐");
r.setTimestamp(1752192000000L);

// 使用 @Builder 后（链式调用）
Record r = Record.builder()
    .userId(1L)
    .type("EXPENSE")
    .amount(35.5)
    .tag("餐饮")
    .note("午餐")
    .timestamp(1752192000000L)
    .build();
```

---

#### `@RequiredArgsConstructor`

| 属性 | 说明 |
|------|------|
| 贴在 | 类上 |
| 作用 | 自动生成一个构造器，参数包含**所有 final 字段**和**所有标记了 `@NonNull` 的字段** |
| 与反射的关系 | 同样是编译期注解，直接修改字节码。Spring 容器创建 Bean 时调用这个构造器，完成依赖注入 |

```java
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;   // final → 自动放入构造器参数
    private final PasswordEncoder passwordEncoder; // final → 自动放入构造器参数
    private final JwtUtil jwtUtil;                 // final → 自动放入构造器参数
}

// Lombok 编译后自动生成：
// public AuthService(UserRepository userRepository,
//                    PasswordEncoder passwordEncoder,
//                    JwtUtil jwtUtil) {
//     this.userRepository = userRepository;
//     this.passwordEncoder = passwordEncoder;
//     this.jwtUtil = jwtUtil;
// }
```

---

### 13.6 注解按出现位置速查

| 位置 | 注解 |
|------|------|
| **类的上面** | `@SpringBootApplication`、`@RestController`、`@Service`、`@Configuration`、`@EnableWebSecurity`、`@RestControllerAdvice`、`@Entity`、`@Table`、`@Data`、`@Builder`、`@NoArgsConstructor`、`@AllArgsConstructor`、`@RequiredArgsConstructor` |
| **字段的上面** | `@Id`、`@GeneratedValue`、`@Column`、`@Value` |
| **方法的上面** | `@Bean`、`@PostMapping`、`@GetMapping`、`@PutMapping`、`@DeleteMapping`、`@Transactional`、`@ExceptionHandler`、`@ResponseStatus`、`@Query`、`@PrePersist`、`@Override` |
| **方法参数的上面** | `@RequestBody`、`@RequestParam`、`@PathVariable`、`@Param` |

### 13.7 一条 HTTP 请求走完，所有注解的执行顺序

这是帮你建立全局视角的总结。当用户发出 `POST /api/records` 请求时：

```
1. Tomcat 收到 HTTP 请求
      │
2. Filter 链
   ├── JwtAuthenticationFilter.doFilterInternal()   ← @Component, @Override
   │     ├── request.getHeader("Authorization")      ← 手动取 Header
   │     ├── jwtUtil.validateToken(token)            ← @Value 注入了 secret
   │     └── SecurityContextHolder.setAuthentication()
      │
3. DispatcherServlet（Spring MVC 核心）
   ├── 查路由表（由 @RequestMapping @PostMapping 注册的）
   ├── 匹配到 RecordController.createRecord()
   │
4. Spring Security 拦截器
   ├── SecurityConfig.securityFilterChain()          ← @Bean @Configuration @EnableWebSecurity
   ├── 检查 .anyRequest().authenticated()
   ├── 从 SecurityContextHolder 拿到第 2 步放的认证信息
   └── 通过 ✓
      │
5. 参数解析
   ├── @RequestBody → Jackson 把 JSON → RecordRequest   ← 反射: Parameter.getType()
      │
6. 调用 Controller 方法
   ├── Method.invoke(controller, args)               ← 反射调用
   ├── SecurityContextHolder.getContext()              ← 拿当前用户
   │
7. AOP 代理（如果有 @Transactional）
   ├── 开启事务
   ├── 调用 RecordService.createRecord()             ← @Service, @Transactional
   │     └── recordRepository.save(record)           ← JpaRepository 动态代理
   │           └── Hibernate: INSERT INTO records...  ← @Entity @Column @Id @GeneratedValue
   ├── 提交事务
      │
8. 返回值处理
   ├── ApiResponse<Record> → Jackson 序列化为 JSON
   ├── HttpServletResponse.setStatus(200)
      │
9. ⚠ 如果任何一步抛了异常：
   ├── @Transactional AOP 检测到 → 回滚事务
   ├── @RestControllerAdvice 全局异常处理器拦截
   │     └── @ExceptionHandler 匹配异常类型
   │           └── @ResponseStatus 设置 HTTP 状态码
   └── 返回统一格式的 ApiResponse.error()
```
