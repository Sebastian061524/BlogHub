package com.sebastian.bloghub.entity;

import lombok.Data;

@Data
public class User {
    private Integer id;
    private String userName;
    private String passwordHash;
}
