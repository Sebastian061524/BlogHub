package com.sebastian.bloghub.vo;

import lombok.Data;

import java.util.List;

/**
 * 分类列表响应VO，以items统一承载分类记录。
 * Lombok的@Data生成访问器、equals、hashCode和toString等样板方法。
 */
@Data
public class CategoryListVO {
    // 分类列表，当前SQL按分类编号升序返回。
    private List<CategoryVO> items;
}
