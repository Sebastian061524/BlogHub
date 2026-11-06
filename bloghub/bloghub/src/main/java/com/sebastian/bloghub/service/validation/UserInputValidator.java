package com.sebastian.bloghub.service.validation;

import com.sebastian.bloghub.dto.UserLoginDTO;
import com.sebastian.bloghub.dto.UserRegisterDTO;
import com.sebastian.bloghub.exception.BusinessException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 注册和登录由同一个组件处理，保证经过同一规范化规则后再检查 DTO 字段约束。
 * 这里通过 jakarta.validation.Validator 主动校验，所以这些接口无需依赖控制器上的 @Valid。
 * 用户名会原地写回 DTO；密码保持原样，不能擅自 strip 改变用户实际提交的口令。
 * Service 层共用的输入处理：先规范化用户名，再执行 DTO 注解校验。
 */
@Component
public class UserInputValidator {

    private final Validator validator;

    /**
     * 由 Spring 构造器注入 Bean Validation 校验器，供两个重载方法复用。
     */
    public UserInputValidator(Validator validator) {
        this.validator = validator;
    }

    /**
     * 接收注册 DTO，先拒绝空对象，再清理用户名前后空白并原地写回，最后执行 DTO 注解约束。
     * 先规范化再检查长度和空白规则，允许用户名前后无意输入的空白被清除。
     */
    public void normalizeAndValidate(UserRegisterDTO dto) {
        if (dto == null) {
            throw new BusinessException(400, "请求参数不合法");
        }
        dto.setUserName(normalizeUserName(dto.getUserName()));
        validate(dto);
    }

    /**
     * 接收登录 DTO，使用与注册相同的用户名规范化规则后校验，避免同一账号在两个入口行为不一致。
     * 只处理用户名，密码未经修改就交给后续 BCrypt matches。
     */
    public void normalizeAndValidate(UserLoginDTO dto) {
        if (dto == null) {
            throw new BusinessException(400, "请求参数不合法");
        }
        dto.setUserName(normalizeUserName(dto.getUserName()));
        validate(dto);
    }

    /**
     * 对非 null 用户名执行 strip，按 Character.isWhitespace 规则移除首尾空白；中间空白交给 DTO 约束拒绝。
     * null 保留给后续 @NotBlank 判定，不在这里制造空串或掩盖缺失值。
     */
    private String normalizeUserName(String userName) {
        return userName == null ? null : userName.strip();
    }

    /**
     * 读取 DTO 上的约束并执行校验；有违规时选取一条消息抛 400，全部通过才正常返回。
     * 违规集合不保证错误排序，因此同时违反多条规则时，不应依赖总是出现同一个首条消息。
     */
    private <T> void validate(T dto) {
        Set<ConstraintViolation<T>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            String message = violations.iterator().next().getMessage();
            throw new BusinessException(400, message);
        }
    }
}
