package com.sebastian.bloghub.vo;

import lombok.Data;

/**
 * 分类信息响应VO，用于分类列表和文章详情。
 * Lombok的@Data生成访问器、equals、hashCode和toString等样板方法。
 */
@Data
public class CategoryVO {
    // 分类编号，对应blog_category.id或SQL别名category_id。
    private Integer categoryId;
    // 分类名称，对应category_name列。
    private String categoryName;
}
