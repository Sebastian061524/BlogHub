package com.sebastian.bloghub.controller;

import com.sebastian.bloghub.common.Result;
import com.sebastian.bloghub.dto.UserRegisterDTO;
import com.sebastian.bloghub.exception.BusinessException;
import com.sebastian.bloghub.service.validation.UserInputValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/debug")
public class DebugController {

    @Autowired
    private UserInputValidator userInputValidator;
    @GetMapping("/success")
    public Result<String> success() {
        return Result.success("测试成功", "ok");
    }

    @GetMapping("/business-error")
    public Result<String> businessError() {
        throw new BusinessException(404, "分类不存在");
    }

    @GetMapping("/system-error")
    public Result<String> systemError() {
        throw new RuntimeException("测试内部异常");
    }

    @PostMapping("/validate-register")
    public Result<String> validateRegister(@RequestBody UserRegisterDTO userRegisterDTO) {
        userInputValidator.normalizeAndValidate(userRegisterDTO);
        return Result.success("校验通过", userRegisterDTO.getUserName());
    }
}
