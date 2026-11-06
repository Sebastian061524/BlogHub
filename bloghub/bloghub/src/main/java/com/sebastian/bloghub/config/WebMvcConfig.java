package com.sebastian.bloghub.config;

import com.sebastian.bloghub.interceptor.JwtAuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 配置 Spring MVC 拦截范围，将文章及其子资源的请求交给 JWT 拦截器。
 * 这里决定哪些路径进入拦截器；哪些 HTTP 方法可以匿名访问由拦截器内部决定。
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    // 注入容器管理的拦截器实例，实例内再使用 JwtUtil 完成凭证校验。
    @Autowired
    private JwtAuthInterceptor jwtAuthInterceptor;

    /**
     * MVC 初始化时传入拦截器注册表；同时覆盖 /articles 本身和 /articles/** 子路径。
     * 这些路径下的 GET 会在拦截器中直接放行；其他方法需要凭证。当前配置不拦截其他路径。
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtAuthInterceptor)
                .addPathPatterns("/articles")
                .addPathPatterns("/articles/**");
    }
}
