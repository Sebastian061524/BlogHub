package com.sebastian.bloghub.service;

import com.sebastian.bloghub.dto.UserLoginDTO;
import com.sebastian.bloghub.dto.UserRegisterDTO;
import com.sebastian.bloghub.entity.User;

/**
 * 用户注册与登录的业务契约，集中定义控制器可调用的账户能力。
 * 实现类负责规范化、参数校验、密码哈希核验及用户 Mapper 协作，JWT 签发由登录控制器接续完成。
 */
public interface UserService {

    /**
     * 接收注册 DTO，规范化和校验输入后创建用户；成功无返回值，重复用户名通过业务异常报告。
     */
    void register(UserRegisterDTO dto);

    /**
     * 接收登录 DTO 并校验账户密码，返回仅供服务端后续签发 JWT 使用的用户实体；不能直接作为登录响应。
     */
    User login(UserLoginDTO dto);
}
