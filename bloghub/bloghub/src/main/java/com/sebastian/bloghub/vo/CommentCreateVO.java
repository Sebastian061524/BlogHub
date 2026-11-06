package com.sebastian.bloghub.vo;

import lombok.Data;

/**
 * 评论创建成功的响应VO，返回新评论编号。
 * Lombok的@Data生成访问器、equals、hashCode和toString等样板方法。
 */
@Data
public class CommentCreateVO {
    // 数据库插入后由MyBatis回填的评论主键。
    private Integer commentId;
}
