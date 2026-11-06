package com.sebastian.bloghub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建文章的请求参数DTO，接收标题、正文和分类。
 * Lombok的@Data生成访问器、equals、hashCode和toString等样板方法。
 */
@Data
public class ArticleCreateDTO {

    @NotBlank(message = "标题不能为空")
    @Size(min = 1, max = 50, message = "标题长度在1到50个字符之间")
    // 标题必须非空白且长度为1到50个字符，校验由Controller触发。
    private String title;
    @NotBlank(message = "内容不能为空")
    @Size(min = 1, max = 1000, message = "内容长度在1到1000个字符之间")
    // 正文必须非空白且长度为1到1000个字符。
    private String body;
    @NotNull(message = "分类不能为空")
    @Positive(message = "分类必须是正整数")
    // 分类编号必须非空且为正数；是否存在由业务层查询确认。
    private Integer categoryId;
}
