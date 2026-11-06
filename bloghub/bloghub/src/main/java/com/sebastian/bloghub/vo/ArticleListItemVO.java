package com.sebastian.bloghub.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文章列表单条响应VO，提供列表展示所需的作者和摘要。
 * Lombok的@Data生成访问器、equals、hashCode和toString等样板方法。
 */
@Data
public class ArticleListItemVO {
    // 文章编号，用于查询详情。
    private Integer articleId;
    // 文章标题。
    private String title;
    // 作者公开信息，由列表查询关联用户表获得。
    private AuthorVO author;
    // 正文前100个字符，由SQL的LEFT函数生成。
    private String summary;
    // 文章创建时间，也是列表排序的首要字段。
    private LocalDateTime createTime;
}
