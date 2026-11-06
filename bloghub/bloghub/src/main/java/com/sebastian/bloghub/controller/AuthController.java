package com.sebastian.bloghub.controller;

import com.sebastian.bloghub.common.Result;
import com.sebastian.bloghub.dto.UserLoginDTO;
import com.sebastian.bloghub.entity.User;
import com.sebastian.bloghub.service.UserService;
import com.sebastian.bloghub.util.JwtUtil;
import com.sebastian.bloghub.vo.UserLoginVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 登录入口：接收账户密码，委托用户业务层核验后签发 JWT，并返回凭证与有效秒数。
 * {@code @RestController} 将 Result 序列化为 JSON；该路径未纳入文章 JWT 拦截范围，允许尚未登录者访问。
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * JSON 请求体绑定为登录 DTO，规范化与约束校验在 UserService 内完成。
     * 只有账号密码通过后才生成 JWT；返回 VO 不包含数据库中的密码哈希。成功时 MVC 默认返回 200。
     */
    @PostMapping("/login")
    public Result<UserLoginVO> login(@RequestBody UserLoginDTO userLoginDTO){
        User user = userService.login(userLoginDTO);
        Integer userId = user.getId();
        // JWT 的身份取自查询到的用户，而非客户端自行宣称的用户 ID。
        String token = jwtUtil.generateToken(userId);
        UserLoginVO userLoginVO = new UserLoginVO();
        userLoginVO.setToken(token);
        // 有效秒数帮助客户端安排续登；服务端仍以 Token 内的过期时间和验签结果为准。
        userLoginVO.setExpiresIn(jwtUtil.getExpirationSeconds());
        return Result.success("登录成功", userLoginVO);
    }

}
