package com.sebastian.bloghub.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ArticleListItemVO {
    private Integer articleId;
    private String title;
    private AuthorVO author;
    private String summary;
    private LocalDateTime createTime;
}
