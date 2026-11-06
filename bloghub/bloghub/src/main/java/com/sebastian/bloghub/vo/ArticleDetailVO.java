package com.sebastian.bloghub.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文章详情响应VO，包含完整文章及嵌套的作者和分类信息。
 * Lombok的@Data生成访问器、equals、hashCode和toString等样板方法。
 */
@Data
public class ArticleDetailVO {
    // 由查询结果id映射的文章编号。
    private Integer articleId;
    // 文章标题。
    private String title;
    // 文章完整正文。
    private String body;
    // 作者公开信息，由MyBatis的association关联映射填充。
    private AuthorVO author;
    // 分类信息，由MyBatis的association关联映射填充。
    private CategoryVO category;
    // 文章创建时间。
    private LocalDateTime createTime;
    // 文章最近一次修改时间。
    private LocalDateTime updateTime;
}
