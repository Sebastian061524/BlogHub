package com.sebastian.bloghub.vo;

import lombok.Data;

import java.util.List;

/**
 * 文章评论列表响应VO，用items包装指定文章的评论。
 * Lombok的@Data生成访问器、equals、hashCode和toString等样板方法。
 */
@Data
public class CommentListVO {
    // 评论列表，按创建时间及编号从早到晚返回。
    private List<CommentListItemVO> items;
}
