package com.sebastian.bloghub.vo;

import lombok.Data;

import java.util.List;

@Data
public class ArticlePageVO {
    private Integer page;
    private Integer pageSize;
    private Long total; // 符合筛选条件的文章总数
    private List<ArticleListItemVO> items;
}
