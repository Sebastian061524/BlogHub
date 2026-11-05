package com.sebastian.bloghub.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Comment {
    private Integer id;
    private Integer articleId;
    private Integer userId;
    private String text;
    private LocalDateTime createTime;
}
