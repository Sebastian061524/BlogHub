# BlogHub 测试记录

## 1. Day 2 验证范围与环境

- 验证日期：2026-11-03。
- Java：17.0.12；Spring Boot：4.0.9；Maven：3.9.14；MySQL：8.0.45。
- 应用地址：`http://localhost:8080`。
- 工程目录：`bloghub/bloghub`，SQL 文件：`bloghub/bloghub/sql/bloghub.sql`（均相对于仓库根目录）。
- HTTP 请求使用 Apifox 手动验证；数据库连通性使用 JUnit 与 JdbcTemplate 验证。
- 第 1–4 节依据用户提供的日志、查询结果、Apifox 截图及测试通过确认整理，当时未重新执行测试；第 5、6 节记录实际执行 Maven 测试的结果；第 7 节记录用户提供的正式登录接口截图。
- 早期验证范围为工程启动、数据库基础验证、统一响应与两类异常处理。注册和登录 Service、JWT 工具与正式登录 HTTP 的后续验证见第 5–7 节，不代表完整博客业务已验收通过。

## 2. 工程与数据库验证

| 编号 | 验证内容 | 预期结果 | 实际结果与证据 | 结论 |
| --- | --- | --- | --- | --- |
| ENV-01 | 启动 Spring Boot 应用 | 应用使用 Java 17，Web 服务监听 8080 | 用户提供的日志包含 Java 17.0.12、`Tomcat started on port 8080` 和 `Started BloghubApplication` | 通过 |
| DB-01 | 插入大小写不同的用户名 `alice`、`Alice` | 两条记录可以同时存在 | 用户提供的查询截图显示两条记录，ID 分别为 1、2 | 通过 |
| DB-02 | 再次插入已有用户名 `Alice` | 唯一约束拒绝重复写入 | 用户提供的截图显示错误 1062，`Duplicate entry 'Alice' for key 'blog_user.user_name'` | 通过 |
| DB-03 | 检查文章表和评论表定义 | 外键目标正确，删除与更新规则为 RESTRICT，时间字段默认当前时间 | 用户提供的两份 `SHOW CREATE TABLE` 结果符合上述定义；文章修改时间未配置自动更新 | 结构检查通过 |
| DB-04 | 检查文章查询索引 | 存在 `(create_time, id)` 与 `(category_id, create_time, id)` 两个联合索引 | 用户提供的 `SHOW INDEX` 截图确认字段及顺序正确 | 结构检查通过 |
| DB-05 | 检查评论查询索引 | 存在 `(article_id, create_time, id)` 联合索引 | 用户提供的 `SHOW INDEX` 截图确认字段及顺序正确 | 结构检查通过 |
| DB-06 | 插入三个预置分类后查询 | 返回 Java、数据库、学习记录三个分类 | 用户提供的查询截图显示对应分类，ID 为 1、2、3 | 通过 |
| DB-07 | 通过 Spring Boot 查询分类数量 | `SELECT COUNT(*) FROM blog_category` 返回 3，JUnit 断言通过 | `BloghubApplicationTests.testJdbc` 使用 JdbcTemplate 查询并调用 `assertEquals(3, categoryCount)`；用户提供连接池建立连接日志，并确认测试面板绿色、1 个测试通过 | 通过 |

DB-07 的测试运行配置需提供 `BLOGHUB_DB_PASSWORD` 环境变量，使用本机数据库凭据。此记录不保存密码。该测试依赖当前测试库中恰好存在三个分类。

索引检查仅确认定义存在，尚未通过实际业务查询和 EXPLAIN 验证执行计划及性能。外键的实际拒绝行为、多表删除事务与回滚也尚未验证。

## 3. 统一响应与异常处理验证

下列 GET 请求均无需请求体，由 `DebugController` 提供。响应体中的 `status` 是成功／失败标记，与 HTTP 状态码分别检查。

### HTTP-01：成功响应

- 请求：`GET http://localhost:8080/debug/success`。
- 行为：返回 `Result.success("测试成功", "ok")`。
- 预期：HTTP 200，响应体 status 为 1，data 为字符串 `ok`。
- 实际：Apifox 截图显示 HTTP 200，响应体如下，符合预期。
- 结论：通过。

```json
{
  "status": 1,
  "message": "测试成功",
  "data": "ok"
}
```

### HTTP-02：业务异常响应

- 请求：`GET http://localhost:8080/debug/business-error`。
- 行为：抛出 `BusinessException(404, "分类不存在")`。
- 预期：HTTP 404，响应体 status 为 0，message 为“分类不存在”，data 为 null。
- 实际：Apifox 截图显示 HTTP 404，响应体如下，符合预期。
- 结论：通过。该用例模拟业务异常，不涉及真实分类查询。

```json
{
  "status": 0,
  "message": "分类不存在",
  "data": null
}
```

### HTTP-03：未知异常响应与服务端日志

- 请求：`GET http://localhost:8080/debug/system-error`。
- 行为：抛出 `RuntimeException("测试内部异常")`。
- 预期：HTTP 500，响应体 status 为 0，返回固定通用提示，data 为 null，不向客户端返回原始异常消息。
- 实际：Apifox 截图显示 HTTP 500，响应体如下，未包含“测试内部异常”。
- 日志预期：服务端记录“未处理的服务器异常”及完整异常堆栈。
- 日志实际：用户确认控制台已记录上述日志及堆栈；整理文档时未另行采集日志文件。
- 结论：响应与日志验证通过。

```json
{
  "status": 0,
  "message": "系统内部错误, 请稍后再试",
  "data": null
}
```

### Apifox 校验配置说明

HTTP-02、HTTP-03 的截图右侧显示“HTTP 状态码应当是 200”，原因是当时所选预期响应为“成功 (200)”，与本轮异常用例的预期不一致。实际 HTTP 状态码和 JSON 响应均符合本表要求。

对应预期响应应分别设置为 404、500；目前尚未收到修改后的校验结果，不将这两项记录为 Apifox 自动校验通过。

## 4. 调试接口保留约定与后续验证

- 按用户明确要求保留 `DebugController`，包含上述三个 GET 调试接口和后续新增的 `POST /debug/validate-register`，未执行删除。
- 这些接口用于验证统一响应和异常处理，不计入正式博客业务接口。
- 后续仍需完成注册登录的边界及并发测试、JWT 身份认证、作者权限、文章及评论业务，以及数据库外键行为和多表删除事务回滚验证。
- 早期验证未覆盖请求参数绑定、JSON 解析和参数校验异常的分类处理。Service 输入校验的后续验证见第 5 节；JSON 解析错误、不支持的 Content-Type 等框架异常的分类处理仍待补齐。

## 5. 注册／登录输入处理迁移到 Service

### 调整范围

- 两个 DTO 保留校验注解，移除去空白的自定义 setter，普通 setter 仅赋值。
- 注册和登录 Service 首先调用共用的 UserInputValidator，先去除用户名首尾空白，再执行注解校验。
- 用户名保留大小写，null 保持为 null；密码保持原样，非法密码直接拒绝。
- 注册 Controller 移除提前执行的 @Valid，保留 @RequestBody；调试校验接口复用同一 Service 层输入校验组件。
- 输入错误统一抛出 HTTP 400 的业务异常，用户名重复仍为 409，合法格式的错误登录凭据仍为 401。

### 自动化回归结果

2026-11-03 在工程目录实际执行 `mvn -o test`，运行前通过本机测试配置提供 BLOGHUB_DB_PASSWORD 环境变量，未将密码写入文档或命令参数。

| 测试类 | 用例数量 | 结果 |
| --- | --- | --- |
| BloghubApplicationTests | 8 | 全部通过 |
| UserInputValidationTests | 8 | 全部通过 |
| 合计 | 16 | 失败 0，错误 0，跳过 0，BUILD SUCCESS |

原有 8 项覆盖分类数量、用户名查询及大小写区分、不存在用户、已存密码哈希与正确／错误密码匹配、登录成功及两类登录失败的 401 状态和统一提示。

新增 8 项回归覆盖：

| 场景 | 预期及实际结果 |
| --- | --- |
| 直接调用注册 Service，用户名为 null | 返回 400 业务异常，而非数据库非空约束错误 |
| 直接调用注册 Service，用户名含内部空白 | 返回 400 业务异常 |
| 直接调用注册 Service，密码含首尾空白 | 返回 400，密码未被删除字符，对应用户名未写入数据库 |
| 使用全参数构造的登录 DTO，用户名含首尾空白 | 规范化后成功登录，用户名为 Day2User01 |
| 直接调用登录 Service，用户名为 null | 返回 400，而非身份验证失败的 401 |
| 直接调用登录 Service，密码含空白 | 返回 400，密码保持原样 |
| HTTP 注册：20 字符合法用户名外加首尾空白 | MockMvc 验证 HTTP 201 和统一成功响应；数据库保存去除首尾空白后的用户名和可匹配的密码哈希 |
| 调试校验接口接收带首尾空白的用户名 | MockMvc 验证 HTTP 200，data 为去除首尾空白后的用户名 |

新增写入测试使用测试事务回滚，不保留测试账号。原有查询与登录测试依赖测试库中的 Alice、alice、Day2User01 等既有测试数据。

修改前先执行了新增 8 项测试，其中 6 项因 Service 尚未规范化／校验输入而按预期失败；修改后运行全部 16 项通过。

本节记录的是当时的阶段结果，包含注册 HTTP 链路与登录 Service 验证；JWT 工具与正式登录 HTTP 的后续验证见下文。到期拒绝行为仍未验证。

## 6. JWT 工具与 Day 2 自动化回归

2026-11-03 文档收尾时，在工程目录重新执行 `mvn -o test`。运行进程复用本机测试运行配置中的 `BLOGHUB_DB_PASSWORD`、`BLOGHUB_JWT_SECRET`，未输出凭据或写入文档。

JWT 工具新增两项测试：

- 生成用户 ID 为 123 的 Token，使用配置密钥解析，确认 subject 为字符串 `123`，签发时间和过期时间非空，两者相差 7,200,000 毫秒。
- 对 null、0、负数用户 ID，均拒绝签发并抛出 `IllegalArgumentException`。这些是工具调用参数错误，不将其测试为 HTTP 业务异常。

测试同时复查已有的数据库、密码匹配、注册 HTTP、登录 Service 和输入规范化用例。本次运行于 2026-11-03 21:27（Asia/Shanghai）结束，耗时约 35 秒，结果如下：

| 测试类 | 用例数量 | 结果 |
| --- | --- | --- |
| BloghubApplicationTests | 10 | 全部通过 |
| UserInputValidationTests | 8 | 全部通过 |
| 合计 | 18 | 失败 0，错误 0，跳过 0，BUILD SUCCESS |

完整报告位于工程目录的 `target/surefire-reports`，本次控制台摘要保存在 `target/day2-doc-verification.log`（构建产物不提交）。这 18 项不包含正式登录 HTTP 的五项手动验证，也不包含 Token 过期或篡改的拒绝测试。

## 7. 正式登录接口手动验证

- 验证日期：2026-11-03。
- 请求：`POST http://localhost:8080/auth/login`。
- 请求体：JSON，字段为 `userName`、`password`。
- 证据：用户提供的五张 Apifox 请求／响应截图，未另行重发这些 HTTP 请求。不保存截图中的完整 Token。

| 编号 | 场景 | 截图中的实际结果 | 结论 |
| --- | --- | --- | --- |
| LOGIN-01 | 正确用户名与密码 | HTTP 200，status 为 1，提示“登录成功”，data 返回非空 token 和 expiresIn: 7200；未返回用户实体或密码哈希 | 手动验证通过 |
| LOGIN-02 | 已存在用户名、错误密码 | HTTP 401，status 为 0，提示“用户名或密码错误”，data 为 null | 手动验证通过 |
| LOGIN-03 | 不存在的用户名 | HTTP 401，与密码错误返回相同提示，data 为 null | 手动验证通过 |
| LOGIN-04 | 正确用户名前后添加空格 | HTTP 200，成功返回 token 与 expiresIn: 7200 | 手动验证通过 |
| LOGIN-05 | userName 为 null | HTTP 400，status 为 0，提示“用户名不能为空”，data 为 null | 手动验证通过 |

LOGIN-02、LOGIN-03、LOGIN-05 右侧仍以 200 为预期而报红，因此记录为人工对照请求和响应通过，不记为 Apifox 自动校验通过。

本轮证明基本登录链路可用，未覆盖携带 Token 访问受保护资源、Token 被篡改、Token 到期、并发注册及全部框架异常分类。JWT 拒绝篡改和过期 Token 的行为仍需补充专门测试。
