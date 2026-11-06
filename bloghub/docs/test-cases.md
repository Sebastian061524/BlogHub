# BlogHub 测试记录

## 1. Day 2 验证范围与环境

- 验证日期：2026-11-03。
- Java：17.0.12；Spring Boot：4.0.9；Maven：3.9.14；MySQL：8.0.45。
- 应用地址：`http://localhost:8080`。
- 工程目录：`bloghub/bloghub`，SQL 文件：`bloghub/bloghub/sql/bloghub.sql`（均相对于仓库根目录）。
- HTTP 请求使用 Apifox 手动验证；数据库连通性使用 JUnit 与 JdbcTemplate 验证。
- 第 1–4 节依据用户提供的日志、查询结果、Apifox 截图及测试通过确认整理，当时未重新执行测试；第 5、6 节记录实际执行 Maven 测试的结果；第 7 节记录用户提供的正式登录接口截图；第 8 节为 Day 3 的自动化回归结果，第 9 节补充文章接口手动验证与提交前检查。
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

## 8. Day 3 删除事务与全量自动化回归

2026-11-04 22:28（Asia/Shanghai）在工程目录执行 `mvn -o clean test`，耗时约 40 秒，结果为 BUILD SUCCESS：

| 测试类 | 用例数量 | 结果 |
| --- | --- | --- |
| BloghubApplicationTests | 22 | 全部通过 |
| UserInputValidationTests | 8 | 全部通过 |
| ArticleDeleteTransactionTests | 2 | 全部通过 |
| 合计 | 32 | 失败 0，错误 0，跳过 0 |

本轮包含原有注册登录、输入校验和 JWT 正常解析，以及文章新增、详情嵌套映射、不存在结果、修改成功、越权修改拒绝、相同内容重复提交和带评论文章删除成功等测试。报告位于工程目录的 `target/surefire-reports`，最终运行日志为 `target/day3-full-tests-final.log`。

### 删除失败时的事务回滚

新增 `ArticleDeleteTransactionTests`，测试类和方法均不添加 `@Transactional`。临时文章与评论在调用删除 Service 之前已提交；调用真实 Service，由 `@MockitoSpyBean` 仅替换文章删除操作，评论删除仍执行真实 SQL。

| 场景 | 核心断言 |
| --- | --- |
| 评论删除后，文章删除抛出运行时异常 | 在模拟失败处确认评论数量已为 0；Service 抛出该异常后，再查询确认文章与评论数量均恢复为 1 |
| 评论删除后，文章删除返回 0 行 | 在模拟失败处确认评论数量已为 0；Service 抛出 500、“删除失败”的业务异常后，再查询确认文章与评论仍存在 |

测试在调用 Service 前后检查当前线程没有外层事务，避免测试事务掩盖 Service 事务失效。两项用例均在 finally 中清理各自创建的记录和用户上下文。

另外执行了反向验证：只在测试中临时绕过 Service 的事务代理，两项用例均按预期因评论数量“预期 1、实际 0”而失败。随后恢复正常代理调用，重新执行上述全量测试通过。业务代码未因本轮测试而修改。反向验证日志为 `target/delete-transaction-negative-control.log`。

### 测试数据与编译产物修正

初次全量运行发现，手动修改文章 7 后，详情测试仍断言旧分类而失败。已将详情、缺失文章查询及修改测试改为事务内创建临时文章，不再依赖文章 7 的可变内容，也未恢复或修改用户手动编辑的数据。

同时将测试类名从 `xBloghubApplicationTests` 对齐为文件名 `BloghubApplicationTests`，使用 clean 清理旧 class，避免旧类和改名后的类重复执行。

此处 32 项为自动化测试数量，不包含课堂 Apifox 手动请求。本轮执行时尚未接入 DELETE Controller；后续删除接口等手动验证见第 9 节。删除越权等其他失败分支尚未补成专门自动化用例，Token 篡改／过期请求也未因本次结果而视为验收通过。

## 9. Day 3 文章接口手动验证与提交前检查

证据来自 2026-11-04 用户提供的 Apifox、Navicat 截图及用户确认，未重新发送这些 HTTP 写请求。以下手动请求不计入自动化用例数量，文档不保存完整 Token。

| 范围 | 已确认的请求与结果 | 证据说明 |
| --- | --- | --- |
| 发布文章 | 合法请求 201，返回 articleId: 7；无有效凭证 401；空字符串与纯空白标题 400；不存在分类 404 | Apifox 截图；Navicat 确认文章落库，联表结果显示作者为 Day2User01（用户 4）、分类为 Java（分类 1） |
| 文章详情 | 文章 7 返回 200，包含嵌套 author／category、完整正文与不带偏移量的两个时间；文章 9 不存在返回 404；0、负数返回 400 | Apifox 截图，查询请求未手动配置 Token |
| 路径类型错误 | 初次 GET 的 abc 和超范围数字返回 500；新增类型转换异常处理后，GET /articles/2147483648 返回 400；后续 DELETE /articles/abc 也返回 400 | 初次失败及修复后截图；未收到修复后的 GET /articles/abc 单独截图，不将其单独记为已验证 |
| 修改文章 | 作者修改 200；非作者修改 403；无有效凭证 401；纯空白标题 400；分类不存在 404；文章不存在 404 | 六张 Apifox 截图确认响应；保存内容和不变字段另由第 8 节 Service 自动化测试检查，未采集每个手动请求前后的数据库快照 |
| 删除文章 | 非作者删除文章 39 返回 403；无有效凭证 401；ID 为 0、abc 返回 400；重复删除与 GET 查询文章 39 均返回 404 | 六张 Apifox 截图确认上述响应；作者成功删除的 HTTP 200、status: 1、“删除成功”由用户口头确认，未提供该成功响应截图 |

部分异常请求截图中的 Apifox 预期仍为“成功 (200)”而报红，不能将人工核对响应等同于 Apifox 自动断言通过。HTTP 删除时未采集关联评论数量的截图；带评论删除成功及失败回滚已有第 8 节真实数据库自动化测试覆盖。

### 提交前回归

- DELETE Controller 接入后，于 2026-11-04 22:52（Asia/Shanghai）重新执行 `mvn -o test`，32 项全部通过，失败 0、错误 0、跳过 0，耗时约 30 秒。
- 对应三个测试类数量仍为 22、8、2；日志位于工程目录的 `target/day3-precommit-test.log`。
- 使用 JDK 17 实际编译算法代码。containsDuplicate 通过 8 项检查，包含单元素、负数、零、重复值、边界值及长度 100000 的无重复／有重复数组；isAnagram、isAnagram2 各通过 9 项检查。
- 算法检查代码与 class 仅保存在 `target/day3-algorithm-verification`，不提交构建产物，不作性能或吞吐量结论。
- 文章列表分页、分类筛选、评论接口和 Token 篡改／过期等专项场景留待后续任务，当前验证不代表全部 MVP 验收通过。

## 10. Day 4 全量回归与手动验证

2026-11-05 22:54（Asia/Shanghai）使用 JDK 17 执行 `mvn -o clean test`，耗时约 40 秒，BUILD SUCCESS：

| 测试类 | 用例数量 | 结果 |
| --- | --- | --- |
| BloghubApplicationTests | 22 | 全部通过 |
| UserInputValidationTests | 8 | 全部通过 |
| ArticleDeleteTransactionTests | 2 | 全部通过 |
| ArticleTests | 7 | 全部通过 |
| CategoryTests | 1 | 全部通过 |
| CommentTests | 6 | 全部通过 |
| 合计 | 46 | 失败 0，错误 0，跳过 0 |

新增测试覆盖文章总数增量、列表字段与作者映射、分类筛选排除其他分类文章、超范围 offset 返回空列表、Service 默认页查询与总数、超出最后一页保留总数，以及非法页码／分类等场景。分类测试检查三个预置分类的 ID、名称及顺序。评论测试覆盖主键回填与保存字段、其他用户发表评论、未登录和文章不存在的拒绝、评论者嵌套映射，以及匿名查询新文章时返回空评论列表。

列表测试在事务内将测试文章的创建时间设置为现有最大时间再加一秒，确保测试文章进入第一页；必须找到指定记录才通过，并明确排除另一分类文章。评论写入和关联新文章均随测试事务回滚，UserContext 写入测试通过 finally 清理；原有删除回滚测试依旧不使用测试外层事务。

当前测试仍依赖本地预置分类、用户 1／4、文章 7，以及不存在的分类 4／文章 999，不能据此声称可在任意空库直接运行。评论 25 等手动数据没有被本次测试删除。

最终日志为工程目录 `target/day4-final-tests.log`，六个类的报告位于 `target/surefire-reports`。构建产物不提交。此前阶段性 35 项全量回归和 2 项评论异常测试是历史批次，不累计为本轮用例数量。

### 手动 HTTP 验证

截图与用户确认的明细见 [Day 4 复盘](day4-review.md)。截图确认文章列表及分类筛选 200、合法最大页码空列表且保留 total、非法列表参数 400、分类不存在 404、分类列表 200、发表评论 201、无 Token 401、评论文章不存在 404、空评论 400、评论列表 200 及非法文章 ID 400。部分其他边界由用户回复“都通过”确认，未逐项采集截图。HTTP 手动请求不计入 46 项自动化测试。

### 算法与后续验证

使用 JDK 17 编译四个算法源文件，对合并有序数组执行 8 个用例：普通合并、nums1 无有效元素、nums2 为空、nums2 全部较小、两个空数组、重复值、负数、nums2 全部较大，全部通过。临时检查程序未改动算法源码，结果日志为 `target/day4-algorithm-verification.log`。

后续继续补充多条评论同时间排序、超过一页的文章数据与摘要 100 字符边界、HTTP 自动化用例、JWT 篡改／过期及上下文清理专项测试。当前结果证明已有测试通过，不代表上述未覆盖场景已经完成验收。

## 11. Day 5 JWT、上下文清理与异常分类

2026-11-06 使用 JDK 17.0.12 执行 `mvn -o test`，完整回归 56 项、失败 0、错误 0、跳过 0，BUILD SUCCESS。提交前再次完整运行，于 17:16（Asia/Shanghai）结束，结果相同，耗时约 37 秒。日志分别为工程目录 `target/day5-full-tests.log`、`target/day5-precommit-tests.log`，报告在 `target/surefire-reports`；均为本机构建产物，不纳入 Git。

| 测试类 | 数量 | 结果 |
| --- | --- | --- |
| BloghubApplicationTests | 22 | 全部通过 |
| UserInputValidationTests | 8 | 全部通过 |
| ArticleDeleteTransactionTests | 2 | 全部通过 |
| ArticleTests | 7 | 全部通过 |
| CategoryTests | 1 | 全部通过 |
| CommentTests | 6 | 全部通过 |
| JwtTests | 7 | 全部通过 |
| UserContextTests | 3 | 全部通过 |
| 合计 | 56 | 失败 0，错误 0，跳过 0 |

### 新增 10 项自动化用例

HTTP 用例使用 MockMvc，经真实 Spring MVC 拦截器与全局异常处理链路执行；没有模拟掉 JwtUtil。它们不属于新增 Apifox 手动验证。

| 测试 | 核心断言 |
| --- | --- |
| 工具层篡改 Token | 将合法 Token 的 subject 从 123 改为 456，重新 Base64URL 编码 Payload，保留 Header 和原签名；parseToken 抛出 JJWT 的 SignatureException |
| HTTP 篡改 Token | 携带上述篡改凭证发布文章，401、status 0、“登录凭证无效或已过期” |
| 缺少认证头 | 合法文章请求不携带 Authorization，401、status 0、“请先登录” |
| 空 Token | Authorization 为 `Bearer `，401、status 0、“请先登录” |
| 缺少 Bearer 前缀 | 认证头直接放有效 Token，401、status 0、“请先登录” |
| 工具层过期 Token | 测试专用密钥、有效期 -60 秒生成并由同一 JwtUtil 解析，明确抛出 ExpiredJwtException |
| HTTP 过期 Token | 使用应用配置的同一签名密钥生成 -60 秒 Token，401、status 0、“登录凭证无效或已过期” |
| 字段校验失败后清理上下文 | 正常 Token、空标题返回 400，随后在测试 finally 清理之前断言 UserContext.getCurrentUserId() 为 null |
| 畸形 JSON | 正常 Token、JSON 缺少结束花括号，400、status 0 |
| 不支持的 Content-Type | 正常 Token、完整合法 JSON，但 Content-Type 为 text/plain，415、status 0 |

上下文测试类的三个用例均在 finally 清理测试线程；只有字段校验失败用例额外断言请求结束后的上下文为空。不能将另外两个用例中的测试自行清理，表述为已验证对应路径的自动清理。

### 先失败再修复与环境排查

- 学习者先运行畸形 JSON 测试，实际为 500，日志显示 Jackson UnexpectedEndOfInputException。新增 Spring HttpMessageNotReadableException 专门处理后返回 400，当前消息为“请求参数不合法”。
- text/plain 用例先实际返回 500，并确认 HttpMediaTypeNotSupportedException；新增对应分支后返回 415、“不支持的请求内容类型”。未知异常保留 500 兜底。
- 单方法／IDEA Maven 测试可通过，终端完整运行却曾出现 56 项 Errors。Codex 读取报告确认缺少 BLOGHUB_JWT_SECRET 引发上下文启动失败。IDEA 的应用、JUnit、Maven 配置及终端环境变量相互独立；按实际入口配置两个必要变量后，Codex 的完整 Maven 回归通过，学习者也确认 IDEA Maven 配置运行可通过。未将环境错误当作业务断言失败。

### 算法验证与尚未覆盖事项

Codex 使用 JDK 17 实际编译 algorithm/longestConsecutive.java，运行 10 个针对性用例和 200 个固定随机种子、以排序参考实现对照的用例，210 项全部通过。输入整数范围为 -10⁹～10⁹；日志为 `target/day5-algorithm-verification.log`。这 210 项不计入后端 56 项，不据此报告性能或全部 int 边界验证。

Day 6 继续覆盖成功请求后的上下文清理、越权拒绝后数据库内容不变、多条评论同时间排序、跨页文章排序和摘要 100 字符边界。JWT 负例当前未单独断言数据库无新增记录，56 项通过不代表这些尚未编写的断言已经通过，也不代表任意空库可直接运行测试。
