package com.sebastian.bloghub.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文章数据库实体，对应blog_article表，供持久层读写文章记录。
 * Lombok的@Data生成访问器、equals、hashCode和toString等样板方法。
 */
@Data
public class Article {

    // 数据库自增主键；插入后由MyBatis生成主键配置回填。
    private Integer id;
    // 作者编号关联blog_user.id，也用于限制更新和删除的作者范围。
    private Integer userId;
    // 文章标题，对应title列。
    private String title;
    // 完整正文，对应body列；列表摘要由查询截取。
    private String body;
    // 分类编号，关联blog_category.id。
    private Integer categoryId;
    // 创建时间对应create_time；新增SQL使用数据库默认时间。
    private LocalDateTime createTime;
    // 更新时间对应update_time；修改文章时由Service设置当前时间。
    private LocalDateTime updateTime;
}
