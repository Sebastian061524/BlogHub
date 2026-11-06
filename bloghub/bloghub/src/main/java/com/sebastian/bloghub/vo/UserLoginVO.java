package com.sebastian.bloghub.vo;

import lombok.Data;

/**
 * 登录成功响应VO，返回身份凭证与有效时长。
 * Lombok的@Data生成访问器、equals、hashCode和toString等样板方法。
 */
@Data
public class UserLoginVO {
    // 服务端签发的JWT，客户端后续请求携带它用于身份认证。
    private String token;
    // Token有效时长，单位秒；是持续时间而非到期时间戳。
    private Long expiresIn;
}
