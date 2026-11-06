package com.sebastian.bloghub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 注册请求DTO，接收用户名和待编码的明文密码。
 * Lombok的@Data生成访问器、equals、hashCode和toString等样板方法。
 */
@Data
public class UserRegisterDTO {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度在3到20个字符之间")
    @Pattern(regexp = "(?U)\\S+", message = "用户名不能包含空白字符")
    // 用户名长度为3到20个字符，正则禁止包含Unicode空白字符。
    private String userName;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 64, message = "密码长度在6到64个字符之间")
    @Pattern(regexp = "[!-~]+", message = "密码仅允许英文字母、数字和英文半角标点")
    // 密码长度为6到64个字符，仅允许ASCII可见非空白字符；编码后才入库。
    private String password;

}
