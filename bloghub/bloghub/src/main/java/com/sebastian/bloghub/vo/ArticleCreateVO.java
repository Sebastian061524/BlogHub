package com.sebastian.bloghub.vo;

import lombok.Data;

/**
 * 文章创建成功的响应VO，仅返回新文章编号。
 * Lombok的@Data生成访问器、equals、hashCode和toString等样板方法。
 */
@Data
public class ArticleCreateVO {
    // 数据库插入后回填的主键，供客户端后续查询详情。
    private Integer articleId;
}
