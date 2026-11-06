package com.sebastian.bloghub.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评论数据库实体，对应blog_comment表，记录所属文章、评论者和内容。
 * Lombok的@Data生成访问器、equals、hashCode和toString等样板方法。
 */
@Data
public class Comment {
    // 数据库自增主键；插入评论后回填此字段。
    private Integer id;
    // 所属文章编号，关联blog_article.id。
    private Integer articleId;
    // 评论者编号，关联blog_user.id，由登录上下文提供。
    private Integer userId;
    // 评论正文，对应text列。
    private String text;
    // 创建时间；当前新增SQL使用数据库默认当前时间。
    private LocalDateTime createTime;
}
