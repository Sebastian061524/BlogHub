package com.sebastian.bloghub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ArticleCreateDTO {

    @NotBlank(message = "标题不能为空")
    @Size(min = 1, max = 50, message = "标题长度在1到50个字符之间")
    private String title;
    @NotBlank(message = "内容不能为空")
    @Size(min = 1, max = 1000, message = "内容长度在1到1000个字符之间")
    private String body;
    @NotNull(message = "分类不能为空")
    @Positive(message = "分类必须是正整数")
    private Integer categoryId;
}
