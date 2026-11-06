package com.sebastian.bloghub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 修改文章的请求参数DTO，文章编号来自路径，作者身份来自登录上下文。
 * Lombok的@Data生成访问器、equals、hashCode和toString等样板方法。
 */
@Data
public class ArticleUpdateDTO {
    @NotBlank(message = "标题不能为空")
    @Size(min = 1, max = 50, message = "标题长度在1到50个字符之间")
    // 更新仍要求提供非空白标题，长度为1到50个字符。
    private String title;
    @NotBlank(message = "内容不能为空")
    @Size(min = 1, max = 1000, message = "内容长度在1到1000个字符之间")
    // 更新仍要求提供完整正文，长度为1到1000个字符。
    private String body;
    @NotNull(message = "分类不能为空")
    @Positive(message = "分类必须为正整数")
    // 分类编号必须非空且为正数，业务层还会检查分类存在性。
    private Integer categoryId;
}
