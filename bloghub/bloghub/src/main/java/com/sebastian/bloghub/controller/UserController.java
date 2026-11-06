package com.sebastian.bloghub.controller;

import com.sebastian.bloghub.common.Result;
import com.sebastian.bloghub.dto.UserRegisterDTO;
import com.sebastian.bloghub.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 用户注册 HTTP 入口：接收 JSON 注册参数，调用用户业务层后返回统一成功结果。
 * 注册所需的规范化、字段约束、用户名查重和密码哈希都由 Service 协调。
 */
@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 将请求体绑定为注册 DTO 并交给 Service；成功返回 HTTP 201，data 为 null。
     * 本方法没有 @Valid，业务层会先规范化用户名，再主动执行校验，保证校验顺序一致。
     */
    @PostMapping()
    public ResponseEntity<Result<Void>> register(@RequestBody UserRegisterDTO userRegisterDTO) {
        userService.register(userRegisterDTO);
        return ResponseEntity.status(201).body(Result.success("注册成功", null));
    }

}
