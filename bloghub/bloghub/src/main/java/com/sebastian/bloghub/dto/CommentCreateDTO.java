package com.sebastian.bloghub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建评论的请求参数DTO，文章编号来自路径，作者来自登录上下文。
 * Lombok的@Data生成访问器、equals、hashCode和toString等样板方法。
 */
@Data
public class CommentCreateDTO {

    @NotBlank(message = "评论内容不能为空")
    @Size(min = 1, max = 500, message = "评论内容长度在1到500个字符之间")
    // 评论正文不能空白，长度为1到500个字符。
    private String text;
}
