package com.sebastian.bloghub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserLoginDTO {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度在3到20个字符之间")
    @Pattern(regexp = "(?U)\\S+", message = "用户名不能包含空白字符")
    private String userName;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 64, message = "密码长度在6到64个字符之间")
    @Pattern(regexp = "[!-~]+", message = "密码仅允许英文字母、数字和英文半角标点")
    private String password;

}
