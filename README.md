# BlogHub

Java 后端独立项目练习。目前完成 Day 2 的工程初始化、数据库连接、统一响应、全局异常处理、注册登录基础流程及 JWT 签发与解析。文章、评论、请求身份认证与作者权限尚待实现。

## 当前内容

- [需求说明](bloghub/docs/requirements.md)：MVP 范围、权限、校验和验收目标。
- [数据库设计](bloghub/docs/design.md)：用户、文章、分类、评论四表及关系、索引、删除事务方案。
- [接口设计](bloghub/docs/api.md)：注册登录、文章 CRUD、分类列表、评论发布与查询，共 10 个接口。
- [测试记录](bloghub/docs/test-cases.md)：自动化测试与 Apifox 手动验证结果，以及尚未覆盖的场景。
- [Day 2 复盘](bloghub/docs/day2-review.md)：当天学习成果、协作情况与次日复习目标。
- [两数之和练习](algorithm/TwoSums.java)：保留暴力法和 HashMap 法，读取一份输入后分别输出两种结果。
- [有效的字母异位词练习](algorithm/isAnagram.java)：保留 HashMap 和固定长度数组两种做法，输入限定为小写英文字母。

## 开发环境与目录

| 工具 | 当前使用版本 |
| --- | --- |
| JDK | 17.0.12，项目编译目标为 Java 17 |
| Maven | 3.9.14 |
| Spring Boot | 4.0.9 |
| MySQL | 8.0.45 |
| MyBatis Starter | 4.0.0 |
| JJWT | 0.13.0 |

仓库根目录保存本 README 和算法练习；设计文档位于 `bloghub/docs`，**Maven 工程位于 `bloghub/bloghub`**，其中包含 `pom.xml`、`src` 和 `sql/bloghub.sql`。

先检查 `java -version` 和 `mvn -version`，确认 Maven 使用 JDK 17。IDEA 的项目 SDK、模块 SDK、Maven 运行 JDK 也使用 17。

本机 Maven 依赖仓库配置在 D 盘。需要复用此设置时，在 Maven 的 `conf/settings.xml` 中设置 `localRepository`，并让 IDEA 的 Maven 设置使用同一配置。Maven 安装目录与依赖仓库目录是两个不同位置。

## 数据库初始化

1. 启动本机 MySQL，在 Navicat 中打开 `bloghub/bloghub/sql/bloghub.sql`。
2. 在新的本地学习数据库上，选中并执行文件开头的建库、四张表、三个联合索引及三个分类的插入语句，执行范围到 `insert into blog_category(...)` 为止。
3. 查询分类，确认只有 `Java`、`数据库`、`学习记录` 三条记录。数据库和表使用 `utf8mb4_0900_as_cs`，用户名区分大小写。

文件末尾的 `alice`、`Alice` 插入及查询是手动验证语句，第二次插入 `Alice` 会故意触发唯一约束错误；其中的密码是验证用占位值，不能用于 BCrypt 登录。需要运行用户名映射测试时，在新库上仅执行首次插入 `alice` 和首次插入 `Alice` 的语句。

此 SQL 目前是初始化与手动练习的合并文件：索引创建和分类插入不能重复执行，`CREATE TABLE IF NOT EXISTS` 也不会更新已有表结构。不要将整份文件作为可重复执行的迁移脚本。应用不会自动执行该 SQL。

数据库连接在 `bloghub/bloghub/src/main/resources/application.yaml` 中配置，当前为 `localhost:3306/bloghub`、用户名 `root`；使用其他连接地址或账号时修改对应配置。

## 环境变量

应用及完整测试都需要以下两个环境变量：

| 名称 | 含义 |
| --- | --- |
| `BLOGHUB_DB_PASSWORD` | 本机 MySQL 账号的密码 |
| `BLOGHUB_JWT_SECRET` | Base64 编码的随机签名密钥，解码后至少 32 字节 |

JWT 使用 HS256，有效期由 `jwt.expiration-seconds` 配置，当前为 `7200` 秒。不要将真实密码、密钥或登录 Token 写入仓库。

使用 IDEA 时，在应用运行配置和 JUnit 整个测试类的运行配置中分别设置这两个环境变量。仅给某个测试方法配置变量，不代表其他运行配置也已设置。

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

在 Apifox 中选择 JSON 请求体，设置 `Content-Type: application/json`：

| 接口 | 成功结果 | 已验证的异常结果 |
| --- | --- | --- |
| `POST /users` | 201，注册成功，`data: null` | 400 参数错误、409 用户名已存在 |
| `POST /auth/login` | 200，返回 `token` 与 `expiresIn` | 400 参数错误、401 用户名或密码错误 |

两者的 JSON 字段均为 `userName`、`password`。用户名先去除首尾空白再校验，区分大小写；密码不做裁剪。具体长度和字符要求见接口文档。

响应统一为 `status`、`message`、`data`，其中 `status: 1` 表示成功、`status: 0` 表示失败，与 HTTP 状态码分别检查。Apifox 的异常用例也应配置对应的预期 HTTP 状态码。

按学习约定保留 `DebugController`：`GET /debug/success`、`GET /debug/business-error`、`GET /debug/system-error` 和 `POST /debug/validate-register`，它们不计入正式业务接口。

## 运行后端测试

当前测试依赖本机 MySQL 和已有测试数据，尚未实现自动准备全部测试数据。在本地学习库中准备：

- 恰好三个预置分类；大小写不同的 `Alice`、`alice` 两个用户。
- 通过注册接口创建 `Day2User01`，密码使用测试源码中约定的公开练习值 `BlogHub!2026`，由应用生成 BCrypt 哈希。此账号仅用于本地测试。
- 测试中的不存在用户 `NotExistUser`、`112233` 及被拒绝注册的 `InvalidPasswordTest` 未被其他操作创建。

先启动应用完成测试账号注册，再在另一个已设置上述两个环境变量的终端中，从仓库根目录执行：

```powershell
Set-Location .\bloghub\bloghub
mvn test
```

依赖已缓存时可使用 `mvn -o test`。报告位于工程目录的 `target/surefire-reports`。新增注册写入用例使用事务回滚；现有测试会检查真实数据库，不应直接指向业务数据库。

最近一次实际运行结果及覆盖范围见测试记录。登录 HTTP 的五项 Apifox 验证单独记录，不等同于五项自动化测试。

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

下一步独立复习 JWT 生成、解析与校验，再进入文章 CRUD。后续补齐请求身份认证、作者权限、分类筛选与分页、评论、多表删除事务，以及异常分类和边界测试。当前未对受保护接口、篡改或过期 Token 的拒绝行为完成验收。
