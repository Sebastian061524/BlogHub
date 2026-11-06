package com.sebastian.bloghub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * BlogHub 的启动入口：创建 Spring 容器并启动内嵌 Web 服务。
 * {@code @SpringBootApplication} 组合自动配置、配置类声明和组件扫描；默认扫描当前包及子包，
 * 使 Controller、Service、配置类和拦截器等组件可以被容器发现并注入。
 */
@SpringBootApplication
public class BloghubApplication {

    /**
     * 接收 JVM 传入的命令行参数并交给 Spring Boot，完成应用启动；不直接承载业务处理。
     */
    public static void main(String[] args) {
        SpringApplication.run(BloghubApplication.class, args);
    }

}
