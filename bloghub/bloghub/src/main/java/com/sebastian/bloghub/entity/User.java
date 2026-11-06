package com.sebastian.bloghub.entity;

import lombok.Data;

/**
 * 用户数据库实体，对应blog_user表，用于注册写入和登录查询。
 * Lombok的@Data生成访问器、equals、hashCode和toString等样板方法。
 */
@Data
public class User {
    // 用户自增主键，也是JWT身份和文章、评论作者的关联编号。
    private Integer id;
    // 用户名，对应user_name列；数据库唯一约束保障唯一性。
    private String userName;
    // 编码后的密码哈希，对应password_hash列，不应直接作为接口响应返回。
    private String passwordHash;
}
