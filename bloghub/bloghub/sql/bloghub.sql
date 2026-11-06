-- 博客课堂建表与实验脚本：用户、分类、文章、评论共四张表。
-- IF NOT EXISTS仅用于建库和建表；后续建索引与插入样例数据并非可重复初始化操作。
-- utf8mb4支持完整Unicode字符，0900_as_cs排序规则区分重音和大小写。
create database if not exists bloghub
    default charset utf8mb4
    default collate utf8mb4_0900_as_cs;
use bloghub;
-- 用户表：用户名唯一，password_hash供应用保存编码后的密码哈希。
create table if not exists blog_user(
    id int auto_increment primary key,
    user_name varchar(20) not null unique,
    password_hash varchar(255) not null
)engine=InnoDB
 default charset utf8mb4
 default collate utf8mb4_0900_as_cs;

-- 分类表：分类名称唯一，一条分类可被多篇文章引用。
create table if not exists blog_category(
    id int auto_increment primary key,
    category_name varchar(20) not null unique
)engine=InnoDB
 default charset utf8mb4
 default collate utf8mb4_0900_as_cs;

-- 文章表：user_id关联作者，category_id关联分类，外键保障引用存在。
-- RESTRICT要求删除或修改被引用的用户、分类前先处理依赖记录。
create table if not exists blog_article(
    id int auto_increment primary key,
    user_id int not null, 
    constraint fk_article_user 
        foreign key (user_id) references blog_user(id)
        on delete restrict on update restrict,
    title varchar(50) not null,
    body varchar(1000) not null,
    category_id int not null, 
    constraint fk_article_category
        foreign key (category_id) references blog_category(id)
        on delete restrict on update restrict,
    -- 时间仅有插入默认值；没有ON UPDATE，修改时间由Service显式设置。
    create_time datetime not null default current_timestamp, 
    update_time datetime not null default current_timestamp 
)engine=InnoDB
 default charset utf8mb4
 default collate utf8mb4_0900_as_cs;

-- 评论表：article_id关联文章，user_id关联评论者。
-- RESTRICT不自动级联删除，当前Service在事务中先删文章评论，再删文章。
create table if not exists blog_comment(
    id int auto_increment primary key,
    article_id int not null,
    constraint fk_comment_article
        foreign key (article_id) references blog_article(id)
        on delete restrict on update restrict,
    user_id int not null,
    constraint fk_comment_user
        foreign key (user_id) references blog_user(id)
        on delete restrict on update restrict,
    text varchar(500) not null,
    create_time datetime not null default current_timestamp
)engine=InnoDB
 default charset utf8mb4
 default collate utf8mb4_0900_as_cs;

-- 文章索引服务于按创建时间和编号排序；复合索引支持分类筛选后的同样排序。
create index idx_article_create_time_id on blog_article(create_time, id);
create index idx_article_category_create_time_id on blog_article(category_id, create_time, id);

-- 评论索引以文章筛选列开头，再按创建时间和编号排列。
create index idx_comment_article_create_time_id on blog_comment(article_id, create_time, id);

-- 预置分类样例，重复执行会触发分类名称唯一约束。
insert into blog_category(category_name) values('Java'),('数据库'),('学习记录');

-- 以下alice和Alice用于课堂验证大小写敏感的用户名唯一约束。
-- 123456是实验占位值，并非应用BCrypt编码后的密码哈希，不是正常注册数据。
INSERT INTO blog_user(user_name, password_hash) VALUES ("alice", "123456");

-- 区分大小写时Alice与alice可并存；紧接着再次插入Alice是故意重复，预期触发唯一约束。
INSERT INTO blog_user(user_name, password_hash) VALUES ("Alice", "123456");

INSERT INTO blog_user(user_name, password_hash) VALUES ("Alice", "123456");

-- 观察实际写入的用户名；重复实验报错后是否继续执行取决于SQL客户端。
SELECT id, user_name FROM blog_user;