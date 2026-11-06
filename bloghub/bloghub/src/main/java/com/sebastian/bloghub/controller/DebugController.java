package com.sebastian.bloghub.controller;

import com.sebastian.bloghub.common.Result;
import com.sebastian.bloghub.dto.UserRegisterDTO;
import com.sebastian.bloghub.exception.BusinessException;
import com.sebastian.bloghub.service.validation.UserInputValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 学习与联调用的调试入口，保留用于观察统一响应、异常转换和注册参数校验的行为。
 * /debug 下的接口分别模拟成功、业务错误、系统错误，以及只校验不落库的注册输入。
 */
@RestController
@RequestMapping("/debug")
public class DebugController {

    @Autowired
    private UserInputValidator userInputValidator;
    /**
     * 不接收业务输入，返回固定成功结果，用于检查 JSON 响应体结构。
     */
    @GetMapping("/success")
    public Result<String> success() {
        return Result.success("测试成功", "ok");
    }

    /**
     * 主动抛出带 HTTP 404 的业务异常，观察全局异常处理器如何返回业务消息。
     */
    @GetMapping("/business-error")
    public Result<String> businessError() {
        throw new BusinessException(404, "分类不存在");
    }

    /**
     * 主动抛出未分类的运行时异常，观察兜底处理器如何记录日志并返回通用 HTTP 500。
     */
    @GetMapping("/system-error")
    public Result<String> systemError() {
        throw new RuntimeException("测试内部异常");
    }

    /**
     * 对请求体运行与正式注册相同的用户名规范化和 DTO 校验，返回规范化后的用户名；不创建账号。
     */
    @PostMapping("/validate-register")
    public Result<String> validateRegister(@RequestBody UserRegisterDTO userRegisterDTO) {
        userInputValidator.normalizeAndValidate(userRegisterDTO);
        return Result.success("校验通过", userRegisterDTO.getUserName());
    }
}
