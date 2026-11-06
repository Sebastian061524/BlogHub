package com.sebastian.bloghub.vo;

import lombok.Data;

/**
 * 作者公开信息响应VO，供文章详情、文章列表和评论列表复用。
 * Lombok的@Data生成访问器、equals、hashCode和toString等样板方法。
 */
@Data
public class AuthorVO {
    // 作者的用户编号。
    private Integer userId;
    // 作者用户名，不包含密码哈希等登录校验信息。
    private String userName;
}
