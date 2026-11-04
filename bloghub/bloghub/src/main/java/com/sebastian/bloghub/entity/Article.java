package com.sebastian.bloghub.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Article {

    private Integer id;
    private Integer userId;
    private String title;
    private String body;
    private Integer categoryId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
