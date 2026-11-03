package com.sebastian.bloghub.service.validation;

import com.sebastian.bloghub.dto.UserLoginDTO;
import com.sebastian.bloghub.dto.UserRegisterDTO;
import com.sebastian.bloghub.exception.BusinessException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Service 层共用的输入处理：先规范化用户名，再执行 DTO 注解校验。
 */
@Component
public class UserInputValidator {

    private final Validator validator;

    public UserInputValidator(Validator validator) {
        this.validator = validator;
    }

    public void normalizeAndValidate(UserRegisterDTO dto) {
        if (dto == null) {
            throw new BusinessException(400, "请求参数不合法");
        }
        dto.setUserName(normalizeUserName(dto.getUserName()));
        validate(dto);
    }

    public void normalizeAndValidate(UserLoginDTO dto) {
        if (dto == null) {
            throw new BusinessException(400, "请求参数不合法");
        }
        dto.setUserName(normalizeUserName(dto.getUserName()));
        validate(dto);
    }

    private String normalizeUserName(String userName) {
        return userName == null ? null : userName.strip();
    }

    private <T> void validate(T dto) {
        Set<ConstraintViolation<T>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            String message = violations.iterator().next().getMessage();
            throw new BusinessException(400, message);
        }
    }
}
