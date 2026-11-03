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

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public Result<UserLoginVO> login(@RequestBody UserLoginDTO userLoginDTO){
        User user = userService.login(userLoginDTO);
        Integer userId = user.getId();
        String token = jwtUtil.generateToken(userId);
        UserLoginVO userLoginVO = new UserLoginVO();
        userLoginVO.setToken(token);
        userLoginVO.setExpiresIn(jwtUtil.getExpirationSeconds());
        return Result.success("登录成功", userLoginVO);
    }

}
