package com.sebastian.bloghub.vo;

import lombok.Data;

@Data
public class UserLoginVO {
    private String token;
    private Long expiresIn;
}
