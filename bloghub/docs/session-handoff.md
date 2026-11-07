# BlogHub 学习会话交接文档

更新时间：2026-11-07（Asia/Shanghai）。第 1 周 Day 6 核心训练、全量回归和原始复盘已完成，准备开始 Day 7。

## 1. 给新会话的首要说明

你是我的 Java 后端开发导师，请继续当前训练，不要从 Day 1 重新开始。

先阅读本文件，以及仓库根目录的 `java_backend_8_week_codex_plan.md` 和 `java_backend_interview_plan.md`。随后按需阅读 README、需求、设计、API 和测试记录，并核对当前代码；本文件是交接快照，不能覆盖后续用户指令与实际变更。

学习者是一名 2028 届本科生，学过苍穹外卖，能基本看懂代码，正在训练独立开发能力，目标是准备 2027 年广州 Java 后端寒假实习。实习时间约一个月，不把练习项目描述为真实企业实习。

教学约定：

- 用中文交流，每次推进一个小步骤。先给需求、验收标准和提示，让学习者自己写，再审查和纠错。
- 默认不要直接给完整实现，不要未经请求批量改代码。明确说“帮我改”“直接帮我写”才授权相应范围；完成后回到指导模式。
- 审查先讲阻塞问题，再区分必须修复、建议优化和以后再做，解释原因。
- 不反复要求确认已确定的技术选型，不为了凑技术栈引入 Redis、MQ 等。
- 区分用户反馈、截图证据与实际执行的测试，不把未测场景写成通过。
- 保留学习者原始复盘；导师观察另外记录，不能用导师判断替换其自评。
- 保留不同算法做法。**DebugController 明确保留，不删除。**
- 用户通常分别提出提交和推送；新修改完成后，不因之前的推送授权自动提交或推送新内容。

## 2. 工作区与 Git 状态

| 项目 | 位置／状态 |
| --- | --- |
| 工作区及 Git 根目录 | `D:\Java-Backend-Learning` |
| Maven 工程 | `D:\Java-Backend-Learning\bloghub\bloghub` |
| 项目文档 | `D:\Java-Backend-Learning\bloghub\docs` |
| 算法 | `D:\Java-Backend-Learning\algorithm` |
| 仓库 | https://github.com/Sebastian061524/BlogHub |
| 当前分支 | `main` |
| Day 4 提交 | `30bab43e59d2dd06757130d9c30bc2b2ae7c5012` |
| 提交说明 | `feat: complete BlogHub Day 4 pagination categories and comments` |

上表为 Day 4 历史提交，已推送。Day 5 测试和后续代码注释也已推送。Day 6 的测试、排序变更、复刷代码和文档随本次交接收尾纳入版本控制。最新 SHA 和本地、远端状态以实际 Git 查询为准。

以下未跟踪的学习资料保留在本地：

```text
homework/
java_backend_8_week_codex_plan.md
java_backend_interview_plan.md
```

本交接文件随 Day 5 文档收尾纳入版本控制。根目录两个学习计划仍未纳入 Git；新会话应打开同一本地工作区，不要以为只克隆远端就能获得这两份计划。

工程存在两层 bloghub 目录，按现有布局继续，不擅自搬迁。构建产物 target、class 和 IDE 配置不纳入提交。

## 3. 当前学习进度

| 天数 | 项目 | Java／算法 |
| --- | --- | --- |
| Day 1 | 需求、数据库与接口设计 | JDK/JRE/JVM、装箱拆箱；两数之和 |
| Day 2 | 初始化、数据库连接、统一响应、异常处理、注册登录和 JWT 基础 | equals/hashCode；有效的字母异位词 |
| Day 3 | 文章 CRUD、鉴权、作者权限、删除事务 | String/StringBuilder/常量池；存在重复元素 |
| Day 4 | 文章分页及分类筛选、分类列表、评论创建和查询 | ArrayList/LinkedList/扩容；合并两个有序数组 |
| Day 5 | JWT 篡改／过期和认证头负例、字段校验失败后的上下文清理；修复畸形 JSON 400 与不支持 Content-Type 415 | HashMap 结构／冲突／扩容；最长连续序列 |
| Day 6 | 成功请求的上下文清理、越权修改拒绝后数据保持原样、评论降序、跨页排序和摘要边界 | JWT 复述、异常体系、泛型、接口与抽象类；独立重写最长连续序列 |

Day 6 原始复盘和导师观察分别记录在 [day6-review.md](day6-review.md)。学习者希望 Day 7 先独立复习常见异常及其分类。项目从零初始化和交付验收安排在 Day 7。

Day 5 原始复盘：

> 今天我亲自完成了：token Usercontext和全局异常处理器的测试
>
> 我最容易混淆的是：没有
>
> 明天需要独立重做或复习的是：token的签发和解析

导师观察单独记录于 day5-review.md：Token 签发／验签／解析、HashMap 相同 key 与同桶冲突、算法整体复杂度经过提示后能串联解释；仍需脱离提示复述。不要用导师观察替换学习者“没有”的自评。

Day 4 历史原始复盘：

> 今天我亲自完成了：文章的分页查询 类别的查询和评论的创建和查询
>
> 我最容易混淆的是：没有
>
> 明天需要独立重做或复习的是：arrayList和linkedlist的区别

导师观察：最初曾把 ArrayList 底层回答为链表，已通过例子纠正；还需巩固“按下标定位节点”和“已定位节点后插入”的复杂度区别。不要把这些观察改写成学习者说自己混淆。

## 4. 开发环境与运行注意事项

| 组件 | 当前版本／路径 |
| --- | --- |
| Java | JDK 17.0.12；项目 java.version 为 17 |
| JDK 17 | `C:\Program Files\Java\jdk-17` |
| Maven | 3.9.14 |
| Maven 命令 | `D:\maven\apache-maven-3.9.14-bin\apache-maven-3.9.14\bin\mvn.cmd` |
| Maven 本地仓库 | `D:\maven\apache-maven-3.9.14-bin\apache-maven-3.9.14\mvn_repo` |
| Spring Boot | 4.0.9 |
| MyBatis Starter | 4.0.0，使用原生 MyBatis，SQL 主要放 XML |
| JJWT | 0.13.0 |
| MySQL | 8.0.45，本地 localhost:3306/bloghub |

使用 application.yaml，环境变量有两个：`BLOGHUB_DB_PASSWORD` 和 `BLOGHUB_JWT_SECRET`。后者是 Base64 编码的 JWT 密钥，程序先解码，再构造 HMAC SecretKey。不将密钥、数据库密码、完整 Token 写入交接文档、日志或 Git。

系统另装了 JDK 25，IDE 新建测试运行配置可能默认用它；需要检查测试 JRE 是否为 17。IDE 中新建单方法／组合测试配置也可能没有继承环境变量。以前 CommentTests 整类配置有环境变量，新生成的两方法组合配置没有；排错应看实际日志，不能仅凭断言代码推测。

Day 5 再次确认 IDEA Maven `bloghub [test]` 配置与终端环境相互独立：配置中填入变量，不会自动传给终端 `mvn test`。本机 Maven 运行配置位于工程目录 `.idea/workspace.xml`，指定的两个环境变量可以只读入测试子进程；不能输出整个配置或凭据。IDEA 配置界面中 Java 选项的环境变量需配置到实际启动的运行项。

本机 Maven 完整回归在工程目录运行 `mvn -o clean test`，需先为进程配置上述两个环境变量并使用 JDK 17。可在本机读取已有 IDE 运行配置，将指定变量仅注入测试进程，不能输出完整 IDE 配置或秘密值。离线命令依赖当前本地缓存，新机器应按需联网下载依赖。

Mockito／ByteBuddy 动态代理、bootstrap classpath sharing 的 JVM 警告不等于测试失败。不要为了消除警告擅自修改构建；先看测试结果和实际异常。

## 5. 已确定的业务与接口契约

统一 JSON：`{"status":1,"message":"...","data":...}`；失败 status 为 0。JSON 中的业务标志与真实 HTTP 状态码分别控制。JSON 使用 camelCase，数据库字段使用 snake_case。

| 方法与路径 | 行为／成功状态 |
| --- | --- |
| POST /users | 注册，201，data 为 null |
| POST /auth/login | 登录，200，data 包含 token、expiresIn |
| POST /articles | 发布文章，201，data.articleId |
| GET /articles/{articleId} | 公开详情，200 |
| PUT /articles/{articleId} | 作者修改，200，data 为 null |
| DELETE /articles/{articleId} | 作者删除，200，data 为 null |
| GET /articles | 公开分页／分类筛选，200 |
| GET /categories | 公开分类列表，200，data.items |
| POST /articles/{articleId}/comments | 登录后发表评论，201，data.commentId |
| GET /articles/{articleId}/comments | 公开评论列表，200，data.items |

关键约定：

- 用户名区分大小写，先 strip 去除首尾空白，再校验长度 3–20；内部不允许空白。注册和登录采用相同规范化策略。
- 密码长度 6–64，只允许英文可打印非空白 ASCII（正则 `[!-~]+`），不做 strip。BCrypt 保存哈希，用 matches 校验；不能比较两次 encode 的字符串。
- 注册重名返回 409；用户名不存在、密码错误统一返回 401、“用户名或密码错误”。
- JWT 有效期 7200 秒，到期重新登录；subject 存数据库用户 ID。
- 发布、修改文章 title 非空白且最多 50 字符，body 非空白且最多 1000 字符，categoryId 必须为正整数且分类存在。
- 文章 ID 来自路径，作者／评论者 ID 来自 JWT 对应的 UserContext，不接受客户端指定身份。
- 文章详情返回完整正文、嵌套 author/category、createTime/updateTime；时间 Java 类型为 LocalDateTime，JSON 例如 `2026-11-04T20:06:37`，没有偏移量，按项目本地时区理解。
- 列表 page 从 1 开始，每页固定 10 条。未传 page 默认 1，未传 categoryId 查全部；显式空值返回 400，不能与未传混同。
- 分页 offset 为 `(page - 1L) * 10`；按 create_time DESC、id DESC；摘要 LEFT(body,100)。total 是同筛选条件下总条数，不是本页长度。
- 超出最后一页返回 items 空数组并保留实际 total；存在分类但无文章返回空数组、total 0；分类不存在返回 404。
- 列表条目为 articleId、title、author、summary、createTime；当前契约没有 categoryId，不因筛选参数存在就随意加响应字段。
- 分类按 id ASC 返回 categoryId/categoryName。
- 评论请求体字段为 text，非空白，最多 500 字符。任何登录用户均可评论公开文章，不要求评论者是文章作者。
- 评论按 create_time DESC、id DESC 排序，最新的评论在前。文章不存在返回 404，文章存在且无评论时返回 200、items 空数组。
- 非法参数 400、未认证 401、非作者修改／删除 403、资源不存在 404；未知系统异常 500，不能向客户端泄漏内部异常细节。

以 `api.md` 中完整设计为准；更早对话出现过的单数路径、标题 20 字等草案已被当前实现和文档替代。

## 6. 数据库与实现结构

数据库 bloghub，InnoDB，utf8mb4，排序规则 utf8mb4_0900_as_cs。ID 为有符号 INT 自增主键。

| 表 | 关键字段 |
| --- | --- |
| blog_user | id、user_name VARCHAR(20) UNIQUE、password_hash VARCHAR(255) |
| blog_category | id、category_name VARCHAR(20) UNIQUE |
| blog_article | id、user_id、title VARCHAR(50)、body VARCHAR(1000)、category_id、create_time、update_time |
| blog_comment | id、article_id、user_id、text VARCHAR(500)、create_time |

外键引用对应用户／分类／文章主键，删除和更新为 RESTRICT。文章两个时间默认 CURRENT_TIMESTAMP；当前修改文章由 Service 显式设置 updateTime，不依赖 ON UPDATE。

文章联合索引 `(create_time,id)`、`(category_id,create_time,id)`；评论联合索引 `(article_id,create_time,id)`。不要将这些索引误建到分类表。

基础包：`com.sebastian.bloghub`。Controller → Service 接口及 impl → Mapper；DTO 接收请求、Entity 对应数据、VO 组织响应。Lombok @Data 可用。MyBatis 配置 mapper XML 扫描与下划线转驼峰，嵌套 author/category 用 resultMap + association。

重要实现位置及行为：

- `UserInputValidator` 在 Service 内规范化并校验注册／登录 DTO；DTO 已不使用自定义 strip setter。别重新移回 setter，避免构造器和直接 Service 调用绕过。
- `UserServiceImpl` 注册预查重并捕获 DuplicateKeyException；登录返回内部 User 后由 Controller 生成 Token，响应不暴露 passwordHash。
- `JwtUtil` 提供 generateToken、parseToken、getUserId；无效生成参数抛 IllegalArgumentException。当前 getUserId 解析 subject 为 Integer。
- `UserContext` 为静态 ThreadLocal<Integer>，提供 set/get/remove；不依赖 @Component。
- `JwtAuthInterceptor` 匹配 /articles 和 /articles/**，当前 GET 直接放行，写请求检查 Authorization: Bearer Token，校验后写 UserContext；afterCompletion 清理。当前针对同步 MVC，未实现异步上下文传播。
- 文章修改先查作者再校验分类，UPDATE 的 WHERE 同时限制 id/user_id；当前 Connector/J 配置下重复提交相同内容已有成功测试，不凭泛化的 affected rows 印象直接判错。
- 删除 Service 使用 @Transactional：校验身份与作者后，先删评论、再按 id/user_id 删文章；文章删除非 1 行抛运行时业务异常，事务回滚。删除 0 条评论是正常情况。
- 评论的插入／查询 SQL 在 CommentMapper，评论路由目前在 ArticleController；嵌套 URL 不意味着 SQL 都放 ArticleMapper。
- `GlobalExceptionHandler` 已处理业务异常、Bean Validation、参数类型转换异常和兜底 Exception；Day 5 新增 HttpMessageNotReadableException → 400、“请求参数不合法”，HttpMediaTypeNotSupportedException → 415、“不支持的请求内容类型”。两项先实测为 500，再修复并通过 MockMvc 测试；未知异常仍为 500。
- `JwtUtil` 方法逻辑未因 Day 5 修改；按用户要求添加中文注释。generateToken 将 subject、iat、exp 放入载荷并以服务端密钥签名；parseToken 验签并检查过期时间后返回 Claims；getUserId 再读取 subject 转为整数。Base64URL 不是加密，Token 不包含服务端密钥。

## 7. 测试证据、数据依赖与保护事项

Day 6 完整回归于 **2026-11-07 16:59（Asia/Shanghai）** 完成。JDK 17 执行 `mvn -o test`，**61 项通过，失败 0、错误 0、跳过 0**。在 Day 5 基础上，ArticleTests 增至 9 项，CommentTests 增至 7 项，UserContextTests 增至 5 项。日志为工程目录 `target/day6-full-tests.log`，五条新增用例详见 [测试记录](test-cases.md#12-day-6-接口集成与边界回归)。

Day 5 提交前完整回归发生于 **2026-11-06 17:16**，JDK 17 执行 `mvn -o test`：**56 项，失败 0、错误 0、跳过 0，BUILD SUCCESS**，约 37 秒。此前 Day 4 的 46 项结果保留为历史，不累计成额外用例。

| 测试类 | 数量 |
| --- | --- |
| BloghubApplicationTests | 22 |
| UserInputValidationTests | 8 |
| ArticleDeleteTransactionTests | 2 |
| ArticleTests | 7 |
| CategoryTests | 1 |
| CommentTests | 6 |
| JwtTests | 7 |
| UserContextTests | 3 |

日志相对工程目录：`target/day5-precommit-tests.log`（之前完整回归另存 `target/day5-full-tests.log`），报告 `target/surefire-reports`。这些构建产物在 clean 或新机器上可能不存在，不当作已提交文件。测试未变时不必仅为进入新会话重复全量回归。

JwtTests 的 7 项覆盖篡改工具／HTTP、缺认证头、空 Token、缺 Bearer 前缀、过期工具／HTTP。工具层精确断言 JJWT SignatureException／ExpiredJwtException，过期 HTTP 使用应用的同一配置密钥，避免错密钥造成假阳性。UserContextTests 的 3 项为字段校验失败后的清理断言、畸形 JSON 400、不支持 Content-Type 415；后两项只断言响应，finally 的测试自行清理不等于已验证这两条路径自动清理。用户 ID 3 在 JWT 负例中仅用于生成凭证，工具不查库，不能据此假定用户 3 存在。

删除回滚的两个测试刻意不使用测试外层 @Transactional，通过真实 Service 事务代理执行，仅模拟文章删除抛异常／返回 0；失败前确认评论确实已删除，失败后确认文章评论都恢复。曾临时绕过事务代理作反向检查，两项按预期失败，再恢复并完整测试通过。不要把这类回滚测试改成只靠测试事务回滚。

现有测试并非任意空库都可运行，仍依赖本地数据：

- 分类 1 Java、2 数据库、3 学习记录；分类 4 当前不存在。
- 用户 1、4 存在；Day2User01 为用户 4。Alice/alice 用于区分大小写测试；早期手工种子的占位密码不是可登录的 BCrypt 哈希。
- 文章 7 存在，作者 4；已被用户修改为 updated title／updated body、分类 2。不要为了旧断言恢复旧内容。
- 文章 999 当前不存在；缺失 ID 测试应先确认前提。不要假设用户 3 存在。
- 评论 25 是手动业务数据，文章 7、用户 4、内容“这篇文章很有帮助”。不要删除它来清理测试。
- 自增 ID 因回滚产生空洞是正常现象，不重置序列。

尽量创建测试自己的临时记录并回滚，ThreadLocal 写入测试用 finally 清理。验证列表时必须断言找到目标记录，不能仅在可能不进入的循环分支内断言。读取刚插入的评论按回填 commentId 查询，不能取文章评论列表第一条当作新记录。

`sql/bloghub.sql` 含课堂用户名大小写实验及故意重复插入，不能直接宣称整文件可重复初始化成功；从零初始化的脚本整理留待验收阶段。不要对真实本地库执行破坏性重建。

手动 Apifox 已验证正常及多种错误响应，详见 test-cases.md 第 10 节与 day4-review.md。部分边界只有用户“都通过”的确认；201／400 等曾因 Apifox 默认预期 200 显示红色，人工响应正确不等于其自动断言通过。

## 8. 算法与基础复习

算法源码：TwoSums.java、isAnagram.java、containsDuplicate.java、mergeTwoArrayList.java、longestConsecutive.java。保留用户命名，不为风格随意改名。

Day 4 合并数组由学习者在提示下完成：i/j 指向两个有效区末尾，k 指向目标数组末尾，较大值从后往前放，最后补 nums2 剩余。时间 O(m+n)，额外空间 O(1)。

收尾实际使用 JDK 17 编译上述四个源码，并执行合并数组 8 个用例，全部通过：普通合并、nums1 无有效元素、nums2 为空、nums2 全部较小、两个空数组、重复值、负数、nums2 全部较大。临时验证程序未改算法实现，日志为工程目录 target/day4-algorithm-verification.log；不把这 8 项计入后端 46 项。

Day 5 在提示下完成最长连续序列：HashSet 去重，遍历集合，仅在 num-1 不存在时从 num 向后扩展，每段结束更新最大长度。平均时间 O(n)、额外空间 O(n)；不能只用 contains 平均 O(1) 来解释整体复杂度，还要说明各段只展开一次。约定数值范围 -10⁹～10⁹，不把全部 int 溢出边界当作已覆盖。

Codex 实际用 JDK 17 编译新增算法源码，执行 10 个针对性用例和 200 个固定随机种子、与排序参考实现对照的用例，210 项全部通过。临时验证程序与 class 只在 target，日志为 `target/day5-algorithm-verification.log`；不计入后端 56 项，不作性能数据结论。

Day 6 已复述 Token 签发和解析，并独立重写最长连续序列。学习者起初把单次查询的平均 O(1) 当成整体复杂度，随后能用连续数组解释外层检查所有数字、只有起点向后扩展。本次复刷以课堂代码审查和推演为证据。

## 9. 新会话接下来怎么带

1. Day 7 先复习常见异常分类。给一个例子让学习者判断是否为受检异常，再根据回答讲解编译器是否要求捕获或声明。
2. 把 BlogHub 初始化所需 SQL 与课堂上的大小写、重复插入实验分开。用独立数据库验证从零初始化、应用启动和测试所需数据，保护已有本地学习库。
3. 核对 README、API 文档和测试记录，并做最终交付审查。越权删除被拒后的数据状态和无效 JWT 被拒后无新增文章仍属待验证项。
4. 安排约 20 分钟 Java 基础模拟面试，让学习者口述登录、作者权限与删除事务。保留原始复盘，按当次授权处理 Git 收尾。

## 10. 新会话可直接发送的开场文字

```text
请阅读 D:\Java-Backend-Learning\bloghub\docs\session-handoff.md，
以及根目录 java_backend_8_week_codex_plan.md 和 java_backend_interview_plan.md，
继续担任我的 Java 后端导师。

第 1 周 BlogHub Day 6 已完成，准备开始 Day 7，请先核对最新 Git 状态。
先带我复习常见异常及受检／非受检分类，再整理并验证项目从零初始化和启动流程。
默认让我自己写代码，每次一个小步骤；只有我明确要求时才直接实现或修改。
请先核对当前工作区，不要重新开始 Day 1，也不要删除 DebugController。
```

相关文档：根目录 README.md；本目录 requirements.md、design.md、api.md、test-cases.md、day2-review.md、day3-review.md、day4-review.md、day5-review.md、day6-review.md。复盘中旧的“尚未推送”是当时历史状态，最新提交与推送状态以实际 Git 核对为准。
