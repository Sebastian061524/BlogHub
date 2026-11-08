# BlogHub

Java 后端独立项目练习。目前推进至 Day 5：已实现注册登录、JWT 请求认证、文章 CRUD 与作者权限、文章分页和分类筛选、分类列表、评论发布与查询，以及删除文章与评论的事务处理。已补充 JWT 篡改／过期拒绝、认证头错误、用户上下文清理专项测试，并修复畸形 JSON 和不支持的 Content-Type 被错误返回为 500 的问题；后续进入接口联调与业务边界补测。

## 当前内容

- [需求说明](bloghub/docs/requirements.md)：MVP 范围、权限、校验和验收目标。
- [数据库设计](bloghub/docs/design.md)：用户、文章、分类、评论四表及关系、索引、删除事务方案。
- [接口设计](bloghub/docs/api.md)：注册登录、文章 CRUD、分类列表、评论发布与查询，共 10 个接口。
- [测试记录](bloghub/docs/test-cases.md)：自动化测试与 Apifox 手动验证结果，以及尚未覆盖的场景。
- [Day 2 复盘](bloghub/docs/day2-review.md)：当天学习成果、协作情况与次日复习目标。
- [Day 3 复盘](bloghub/docs/day3-review.md)：文章 CRUD、Java 基础、算法练习及次日复习安排。
- [Day 4 复盘](bloghub/docs/day4-review.md)：分页、分类和评论功能、集合基础、合并数组与验证记录。
- [Day 5 复盘](bloghub/docs/day5-review.md)：JWT、上下文清理和异常分类测试、HashMap、最长连续序列及原始复盘。
- [会话交接](bloghub/docs/session-handoff.md)：当前学习进度、教学约定、环境和下一步任务。
- [两数之和练习](algorithm/TwoSums.java)：保留暴力法和 HashMap 法，读取一份输入后分别输出两种结果。
- [有效的字母异位词练习](algorithm/isAnagram.java)：保留 HashMap 和固定长度数组两种做法，输入限定为小写英文字母。
- [存在重复元素练习](algorithm/containsDuplicate.java)：使用 HashSet 判断重复，平均时间 O(n)，最坏额外空间 O(n)。
- [合并两个有序数组练习](algorithm/mergeTwoArrayList.java)：从后向前三指针合并，时间 O(m + n)，额外空间 O(1)。
- [最长连续序列练习](algorithm/longestConsecutive.java)：HashSet 去重，仅从连续段起点向后扫描，平均时间 O(n)，额外空间 O(n)。

## 开发环境与目录

| 工具 | 当前使用版本 |
| --- | --- |
| JDK | 17.0.12，项目编译目标为 Java 17 |
| Maven | 3.9.14 |
| Spring Boot | 4.0.9 |
| MySQL | 8.0.45 |
| MyBatis Starter | 4.0.0 |
| JJWT | 0.13.0 |

仓库根目录保存本 README 和算法练习，设计文档位于 `bloghub/docs`。**Maven 工程位于 `bloghub/bloghub`**，其中包含 `pom.xml`、`src` 和数据库初始化脚本 `sql/init.sql`。

先检查 `java -version` 和 `mvn -version`，确认 Maven 使用 JDK 17。IDEA 的项目 SDK、模块 SDK、Maven 运行 JDK 也使用 17。

本机 Maven 依赖仓库配置在 D 盘。需要复用此设置时，在 Maven 的 `conf/settings.xml` 中设置 `localRepository`，并让 IDEA 的 Maven 设置使用同一配置。Maven 安装目录与依赖仓库目录是两个不同位置。

## 数据库初始化

1. 在 MySQL 中新建并选中一个空库。使用默认应用配置时，库名为 `bloghub`。字符集选 `utf8mb4`，排序规则选 `utf8mb4_0900_as_cs`，以保留用户名的大小写区别。
2. 在该库的查询窗口中运行 `SELECT DATABASE();`，确认选中了目标库。然后执行整份 [`init.sql`](bloghub/bloghub/sql/init.sql)，创建四张表、三个联合索引并写入初始分类。
3. 用 `SHOW TABLES;` 确认有 `blog_user`、`blog_category`、`blog_article`、`blog_comment` 四张表。执行 `SELECT id, category_name FROM blog_category ORDER BY id;`，确认依次返回 `Java`、`数据库`、`学习记录`。

`init.sql` 需要在空库中手动执行一次。原 [`bloghub.sql`](bloghub/bloghub/sql/bloghub.sql) 保留为课堂记录，包含用户名大小写实验和故意触发唯一约束的重复插入。

应用默认用 `root` 连接 `jdbc:mysql://localhost:3306/bloghub`。在独立库验证启动时，可以在应用运行配置中设置 `SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/bloghub_day7` 和 `SERVER_PORT=8081`。如果使用其他 MySQL 账号，可以通过 `SPRING_DATASOURCE_USERNAME` 指定。

初始化只准备应用表结构和初始分类。全量 Maven 测试还需要额外的用户和文章等测试数据，准备方式见[运行后端测试](#运行后端测试)。

## 环境变量

应用及完整测试都需要以下两个环境变量：

| 名称 | 含义 |
| --- | --- |
| `BLOGHUB_DB_PASSWORD` | 本机 MySQL 账号的密码 |
| `BLOGHUB_JWT_SECRET` | Base64 编码的随机签名密钥，解码后至少 32 字节 |

JWT 使用 HS256，有效期由 `jwt.expiration-seconds` 配置，当前为 `7200` 秒。不要将真实密码、密钥或登录 Token 写入仓库。

使用 IDEA 时，在应用、JUnit 和 Maven 各自使用的运行配置中分别设置这两个环境变量。Maven 的 `bloghub [test]` 配置在“Java 选项 → 环境变量”中设置。仅给某个测试方法配置变量，不代表其他运行配置也已设置；IDEA 运行配置中的变量也不会自动传给终端中执行的 `mvn test`。

使用 PowerShell 时，可以在准备运行 Maven 的同一终端中输入现有凭据；以下命令不会回显输入值：

```powershell
$env:BLOGHUB_DB_PASSWORD = [System.Net.NetworkCredential]::new('', (Read-Host 'MySQL 密码' -AsSecureString)).Password
$env:BLOGHUB_JWT_SECRET = [System.Net.NetworkCredential]::new('', (Read-Host 'JWT Base64 密钥' -AsSecureString)).Password
```

首次设置 JWT 密钥时，也可以用以下命令直接生成并设置当前终端的变量，无需在控制台打印密钥。再次启动时如需保持原有 Token 有效，应复用同一密钥。

```powershell
$bloghubKeyBytes = New-Object byte[] 32
$bloghubKeyGenerator = [System.Security.Cryptography.RandomNumberGenerator]::Create()
try {
    $bloghubKeyGenerator.GetBytes($bloghubKeyBytes)
    $env:BLOGHUB_JWT_SECRET = [Convert]::ToBase64String($bloghubKeyBytes)
} finally {
    $bloghubKeyGenerator.Dispose()
    [Array]::Clear($bloghubKeyBytes, 0, $bloghubKeyBytes.Length)
}
```

PowerShell 中设置的变量仅供该终端及其后续启动的进程使用，不会自动同步到已经打开的 IDEA。

## 启动与接口验证

在仓库根目录打开已配置环境变量的 PowerShell，执行：

```powershell
Set-Location .\bloghub\bloghub
mvn spring-boot:run
```

也可以在 IDEA 中运行 `com.sebastian.bloghub.BloghubApplication`。默认地址为 `http://localhost:8080`。

在 Apifox 中验证以下已实现接口；有请求体的接口选择 JSON，并设置 `Content-Type: application/json`。GET 和 DELETE 不需要请求体。

| 接口 | 成功结果 | 已验证的异常结果 |
| --- | --- | --- |
| `POST /users` | 201，注册成功，`data: null` | 400 参数错误、409 用户名已存在 |
| `POST /auth/login` | 200，返回 `token` 与 `expiresIn` | 400 参数错误、401 用户名或密码错误 |
| `POST /articles` | 201，返回 `articleId` | 400 参数错误、401 未登录、404 分类不存在 |
| `GET /articles/{articleId}` | 200，返回包含作者、分类的完整详情 | 400 非法 ID、404 文章不存在 |
| `PUT /articles/{articleId}` | 200，修改成功，`data: null` | 400 参数错误、401 未登录、403 非作者、404 文章或分类不存在 |
| `DELETE /articles/{articleId}` | 200，删除成功，`data: null` | 400 非法 ID、401 未登录、403 非作者、404 文章不存在 |
| `GET /articles` | 200，返回 page、pageSize、total、items | 400 非法页码／分类参数、404 分类不存在 |
| `GET /categories` | 200，返回全部分类的 items，按分类 ID 升序 | 无业务请求参数 |
| `POST /articles/{articleId}/comments` | 201，返回 commentId | 400 评论参数错误、401 未登录、404 文章不存在 |
| `GET /articles/{articleId}/comments` | 200，返回评论 items，包含嵌套 author | 400 非法文章 ID、404 文章不存在 |

注册、登录的 JSON 字段均为 `userName`、`password`。用户名先去除首尾空白再校验，区分大小写；密码不做裁剪。文章新增、修改均提交 `title`、`body`、`categoryId`，具体校验规则见接口文档。

文章详情、文章列表、分类列表和评论列表公开访问。文章写接口和发表评论通过 `Authorization: Bearer <token>` 携带登录返回的凭证；拦截器验证后，将用户 ID 存入 UserContext 的 ThreadLocal，Service 读取身份，请求结束时清理。修改、删除只允许作者操作，SQL 也包含作者条件；删除 Service 在同一个事务中先删评论，再删文章。登录用户可以评论其他用户的文章，请求体只提交 text，文章 ID 来自路径、评论者 ID 来自登录上下文。

文章列表支持可选查询参数 page（默认 1）和 categoryId（不传查询所有分类），每页固定 10 条，按创建时间和文章 ID 降序；摘要取正文前 100 字符。传入空参数与省略参数不同，空值返回 400。评论列表不分页，最新发布的评论在前，同一时间按评论 ID 降序。已有文章没有评论时，items 返回空数组。

详情中的 `author`、`category` 为嵌套对象，由 MyBatis XML 的 resultMap／association 映射。Java 时间字段使用 LocalDateTime，JSON 格式约定为 `yyyy-MM-dd'T'HH:mm:ss`，不附加偏移量，按 Asia/Shanghai 本地时间解释。

响应统一为 `status`、`message`、`data`，其中 `status: 1` 表示成功、`status: 0` 表示失败，与 HTTP 状态码分别检查。Apifox 的异常用例也应配置对应的预期 HTTP 状态码。

请求体为畸形 JSON 时返回 400，不支持的请求 Content-Type 返回 415，不存在的路径返回 404。这些错误响应的 `status` 为 0。未知系统异常由 500 兜底，客户端收到通用错误消息。JWT 被篡改或过期时返回 401、“登录凭证无效或已过期”；缺少认证头、空 Token 或缺少 Bearer 前缀返回 401、“请先登录”。

按学习约定保留 `DebugController`：`GET /debug/success`、`GET /debug/business-error`、`GET /debug/system-error` 和 `POST /debug/validate-register`，它们不计入正式业务接口。

## 运行后端测试

当前测试依赖本机 MySQL 和已有测试数据，尚未实现自动准备全部测试数据。在本地学习库中准备：

- 恰好三个预置分类，以及大小写不同的 `Alice`、`alice` 两个用户。用户名映射测试可参考课堂脚本中各用户名首次插入的语句。课堂脚本的密码占位值无法用于 BCrypt 登录。
- 通过注册接口创建 `Day2User01`，密码使用测试源码中约定的公开练习值 `BlogHub!2026`，由应用生成 BCrypt 哈希。此账号仅用于本地测试。
- 部分课堂测试还使用用户 ID 1、4（当前库中 Day2User01 的 ID 为 4）及分类 ID 1、2；换库时需要核对或调整测试数据。文章详情和修改用例已自行创建临时文章；评论测试仍依赖文章 7 存在，不依赖它的标题或正文。
- 分类 ID 4、文章 ID 999 在当前异常测试中应不存在；这些依赖尚未全部替换为自动准备的测试数据。
- 测试中的不存在用户 `NotExistUser`、`112233` 及被拒绝注册的 `InvalidPasswordTest` 未被其他操作创建。

先启动应用完成测试账号注册，再在另一个已设置上述两个环境变量的终端中，从仓库根目录执行：

```powershell
Set-Location .\bloghub\bloghub
mvn test
```

依赖已缓存时可使用 `mvn -o test`；需要清除旧编译产物时运行 `mvn -o clean test`。报告位于工程目录的 `target/surefire-reports`。注册和文章写入等测试使用事务回滚；删除回滚测试自身不开启事务，会提交临时数据并在 finally 中清理。测试会访问真实数据库，应使用本地学习库。

Day 7 于 2026-11-08 使用 JDK 17 执行 `mvn -o test`，62 项自动化测试全部通过，失败 0、错误 0、跳过 0。独立空库的启动和业务流程验证单独记录。覆盖范围与结果见 [测试记录](bloghub/docs/test-cases.md#13-day-7-空库启动与交付回归)。

## 运行算法练习

在仓库根目录执行（需要 JDK）：

```shell
javac -encoding UTF-8 algorithm/TwoSums.java
java -cp algorithm TwoSums
```

输入示例，依次为数组长度、数组元素、目标值：

```text
4
2 7 11 15
9
```

预期输出：

```text
暴力法索引：[0, 1]
HashMap 法索引：[0, 1]
```

## 验证与后续工作

两数之和的两种做法均已验证普通样例、重复值、负数、零和题目数值边界。异位词练习可使用 `javac -encoding UTF-8 algorithm/isAnagram.java` 编译，再执行 `java -cp algorithm isAnagram`；当前 main 演示 HashMap 版本，数组版本保留在 `isAnagram2` 中。

存在重复元素练习可使用 `javac -encoding UTF-8 algorithm/containsDuplicate.java` 编译，再执行 `java -cp algorithm containsDuplicate`；当前 main 输出示例 `[1, 2, 3, 1]` 的结果 `true`。

合并数组练习可使用 `javac -encoding UTF-8 algorithm/mergeTwoArrayList.java` 编译，再执行 `java -cp algorithm mergeTwoArrayList`；当前 main 展示合并 `[1,2,3]` 与 `[2,5,6]`。Day 4 收尾另用临时检查程序验证了 8 个用例，包括空数组、重复值和负数，全部通过。

最长连续序列可使用 `javac -encoding UTF-8 algorithm/longestConsecutive.java` 编译，再执行 `java -cp algorithm longestConsecutive`；当前 main 样例输出 4。Day 5 使用 JDK 17 实际执行 10 个针对性用例和 200 个固定随机种子的排序方案对照，210 项全部通过。数值范围按 -10⁹～10⁹，算法验证单独记录。
