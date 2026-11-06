package com.sebastian.bloghub.vo;

import lombok.Data;

import java.util.List;

/**
 * 文章分页响应VO，返回分页参数、筛选总数和当前页记录。
 * Lombok的@Data生成访问器、equals、hashCode和toString等样板方法。
 */
@Data
public class ArticlePageVO {
    // 从1开始的当前页码，由Service校验。
    private Integer page;
    // 每页记录数，当前业务层固定为10。
    private Integer pageSize;
    // 符合分类条件的全部记录数，并非当前页条数。
    private Long total; // 符合筛选条件的文章总数
    // 当前页文章摘要列表；超过末页时可以为空列表。
    private List<ArticleListItemVO> items;
}
