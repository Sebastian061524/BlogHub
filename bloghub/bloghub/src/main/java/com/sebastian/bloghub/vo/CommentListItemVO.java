package com.sebastian.bloghub.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentListItemVO {
    private Integer commentId;
    private AuthorVO author;
    private String text;
    private LocalDateTime createTime;
}
