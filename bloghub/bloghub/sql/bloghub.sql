create database if not exists bloghub
    default charset utf8mb4
    default collate utf8mb4_0900_as_cs;
use bloghub;
create table if not exists blog_user(
    id int auto_increment primary key,
    user_name varchar(20) not null unique,
    password_hash varchar(255) not null
)engine=InnoDB
 default charset utf8mb4
 default collate utf8mb4_0900_as_cs;

create table if not exists blog_category(
    id int auto_increment primary key,
    category_name varchar(20) not null unique
)engine=InnoDB
 default charset utf8mb4
 default collate utf8mb4_0900_as_cs;

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
    create_time datetime not null default current_timestamp, 
    update_time datetime not null default current_timestamp 
)engine=InnoDB
 default charset utf8mb4
 default collate utf8mb4_0900_as_cs;

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

create index idx_article_create_time_id on blog_article(create_time, id);
create index idx_article_category_create_time_id on blog_article(category_id, create_time, id);

create index idx_comment_article_create_time_id on blog_comment(article_id, create_time, id);

insert into blog_category(category_name) values('Java'),('数据库'),('学习记录');

INSERT INTO blog_user(user_name, password_hash) VALUES ("alice", "123456");

INSERT INTO blog_user(user_name, password_hash) VALUES ("Alice", "123456");

INSERT INTO blog_user(user_name, password_hash) VALUES ("Alice", "123456");

SELECT id, user_name FROM blog_user;