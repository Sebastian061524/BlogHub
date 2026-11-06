# BlogHub Day 5 复盘

日期：2026-11-06（Asia/Shanghai）。

## 学习者原始复盘

- 今天我亲自完成了：token Usercontext和全局异常处理器的测试
- 我最容易混淆的是：没有
- 明天需要独立重做或复习的是：token的签发和解析

## 当天成果与协作范围

- 学习者在逐步提示、代码审查和纠错下编写 JwtTests（7 项）、UserContextTests（3 项），覆盖 JWT 篡改与过期的工具层／HTTP 层拒绝、认证头缺失或格式错误、参数校验失败后的用户上下文清理、畸形 JSON 和不支持的 Content-Type。
- 测试实际暴露两类客户端请求错误被兜底为 500。学习者新增 HttpMessageNotReadableException 的 400 分支和 HttpMediaTypeNotSupportedException 的 415 分支；未知服务器异常保留 500 兜底。畸形 JSON 当前提示仍为“请求参数不合法”，更具体的提示仅作为建议，未改写为已实现。
- 根据学习者明确要求，Codex 仅在 JwtUtil 中新增生成、解析和获取用户 ID 的说明注释，未修改 JWT 方法逻辑。其余本日代码与算法由学习者编写，Codex 提供局部提示和审查。
- 学习者复盘后提醒补齐文档和 Git 推送，Codex 据此整理 README、接口约定、测试记录、本复盘及会话交接，并执行提交前检查和推送收尾。未将 homework、两份本地学习计划、IDE 配置或构建产物混入本次提交。

## 自动化验证与排错证据

- 课堂各新增用例先由学习者反馈通过；畸形 JSON 的 400 与不支持 Content-Type 的 415 测试先分别收到实际 500，确认异常类型后修复，再由学习者反馈通过。
- 首次完整回归为 56 项、失败 0、错误 56。Codex 读取 Surefire 报告确认根因为缺少 BLOGHUB_JWT_SECRET，导致 Spring 上下文启动失败，并非 56 个业务断言失败。报告同时确认 JDK 17.0.12。
- 本机 Maven `bloghub [test]` 配置已经保存两个非空环境变量。Codex 仅读取指定变量并注入测试进程，不输出秘密值；使用 JDK 17 执行 `mvn -o test`，56 项、失败 0、错误 0、跳过 0，BUILD SUCCESS，日志为工程目录 `target/day5-full-tests.log`。
- 学习者随后确认：通过 IDEA 已配置的 Maven 配置运行可以通过，终端直接执行 `mvn test` 不行。两者环境变量作用域独立，IDEA 配置不会自动写入终端环境；无需因此修改业务代码或重新生成 JWT 密钥。
- 提交前清理测试中的一行行尾空白后，于 17:16（Asia/Shanghai）再次完整回归，耗时约 37 秒：56 项、失败 0、错误 0、跳过 0，BUILD SUCCESS，日志另存工程目录 `target/day5-precommit-tests.log`；摘要同时记入 test-cases.md 第 11 节。报告和日志属于本机构建产物，不提交。

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
| 合计 | 56 |

新增 HTTP 用例通过 MockMvc 和真实 Spring MVC 拦截器／异常处理链路执行，不等同于对已启动服务发送 Apifox 请求。上下文清理专项当前覆盖认证成功后字段校验失败这一分支；认证拒绝后的数据不变、正常成功后的清理和全部路由边界未因本轮结果自动视为通过。

## Java 基础与算法

- 复习 ArrayList／LinkedList 按下标中间插入通常均为 O(n)，已通过迭代器定位的 LinkedList 插入为 O(1)。
- HashMap：相同 key 更新 value、size 不变；不同 key 可以有相同 value，落入同一桶不代表 key 相等。hashCode 相同仍需结合引用相同／equals 判断。
- JDK 17 中 HashMap 使用数组、链表及满足条件后的红黑树。通常由元素数量触发扩容时，阈值为容量乘负载因子；默认负载因子 0.75，容量 16 时阈值 12，新增到 13 条通常扩容到 32。此处阈值属于整个 Map，不是某个桶；更新已有 key 不会因元素数量增长而触发扩容。
- 学习者在提示下完成 algorithm/longestConsecutive.java：HashSet 去重，遍历集合，仅在前一个整数不存在时从该数向后扫描，每段结束更新全局最大长度。
- 算法平均时间 O(n)、额外空间 O(n)。除了 contains 平均 O(1)，还必须说明只从连续段起点展开、各段不重复完整扫描，因此内层 while 的累计工作量为 O(n)。
- Codex 使用 JDK 17 编译实际算法源码，执行 10 个针对性用例（含空数组、单元素、重复、负数、多段、最长段不从最小值开始及题目数值边界），再与独立排序参考实现对照 200 个固定随机种子用例，210 项全部通过。整数范围为 -10⁹～10⁹，不声称覆盖全部 int 溢出边界；算法用例不计入后端 56 项。
- 临时检查程序与 class 仅保存在 target，日志为 `target/day5-algorithm-verification.log`，未修改学习者的算法实现或其他已有做法。

## 导师观察与 Day 6 衔接

保留学习者“没有”的原始自评。导师另行记录：课堂曾将验签理解为比较 secretKey、将 HashMap 同 key 的两次 put 判断为两条记录、把排序方案复杂度写为 O(n+m)，以及在最长连续序列中遗漏全局最大值。经过分步讲解与纠错后，学习者能够解释 Token 的生成／解析、Map 的更新与冲突区别、仅从连续段起点扫描的原因；后续独立重做用于检验脱离提示后的掌握程度。

Day 6 先让学习者脱离提示复述 Token 签发和解析，再选择一个实际尚未覆盖的接口场景继续联调：越权拒绝后数据不变、成功请求后的上下文清理、多条评论同时间排序、超过一页文章排序或 100 字符摘要边界。每次只推进一个小步骤。Day 7 整理可从零执行的初始化脚本、README 与最终交付审查；本日完成不等同于整个 MVP 最终验收。
