package com.sebastian.bloghub.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评论列表单条响应VO，包含评论内容、作者和创建时间。
 * Lombok的@Data生成访问器、equals、hashCode和toString等样板方法。
 */
@Data
public class CommentListItemVO {
    // 由查询结果id映射的评论编号。
    private Integer commentId;
    // 评论者公开信息，由关联用户表的查询结果组装。
    private AuthorVO author;
    // 评论正文。
    private String text;
    // 评论创建时间；列表按此时间和编号升序排列。
    private LocalDateTime createTime;
}
