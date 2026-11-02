# BlogHub

Java 后端独立项目练习。目前完成 Day 1 的需求分析、数据库设计草案及接口设计，尚未实现 Spring Boot 后端功能。

## 当前内容

- [需求说明](bloghub/docs/requirements.md)：MVP 范围、权限、校验和验收目标。
- [数据库设计](bloghub/docs/design.md)：用户、文章、分类、评论四表及关系、索引、删除事务方案。
- [接口设计](bloghub/docs/api.md)：注册登录、文章 CRUD、分类列表、评论发布与查询，共 10 个接口。
- [两数之和练习](algorithm/TwoSums.java)：保留暴力法和 HashMap 法，读取一份输入后分别输出两种结果。

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

两数之和的两种做法均已验证普通样例、重复值、负数、零和题目数值边界。接口文档中的 JSON 示例已检查语法，验收清单仍待后续实现后执行。

下一步初始化 Spring Boot 工程和数据库连接，再逐步实现接口。当前无应用启动命令或可执行的数据库初始化 SQL，设计文档不代表业务功能已通过测试。
