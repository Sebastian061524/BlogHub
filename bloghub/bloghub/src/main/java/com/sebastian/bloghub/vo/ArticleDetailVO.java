package com.sebastian.bloghub.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ArticleDetailVO {
    private Integer articleId;
    private String title;
    private String body;
    private AuthorVO author;
    private CategoryVO category;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
