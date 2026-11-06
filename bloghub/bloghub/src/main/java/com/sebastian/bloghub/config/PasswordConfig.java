package com.sebastian.bloghub.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 集中提供密码编码器，使注册与登录复用同一种密码哈希规则。
 * {@code @Configuration} 表示配置类，@Bean 方法的返回值交由 Spring 管理和注入。
 */
@Configuration
public class PasswordConfig {

    /**
     * 返回 BCrypt 实现的 PasswordEncoder；注册时 encode 生成含随机盐的哈希，
     * 登录时 matches 使用已存哈希中的盐和参数核验原始密码，因此无需自行保存盐或解密密码。
     */
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
