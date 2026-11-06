package com.sebastian.bloghub.service.impl;

import com.sebastian.bloghub.dto.UserLoginDTO;
import com.sebastian.bloghub.dto.UserRegisterDTO;
import com.sebastian.bloghub.entity.User;
import com.sebastian.bloghub.exception.BusinessException;
import com.sebastian.bloghub.mapper.UserMapper;
import com.sebastian.bloghub.service.UserService;
import com.sebastian.bloghub.service.validation.UserInputValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 用户业务实现：复用输入处理器规范化和校验 DTO，通过 Mapper 访问账号，通过密码编码器保护密码。
 * 注册只存 BCrypt 哈希；登录核验成功后返回服务端用户实体，后续 JWT 签发由 AuthController 负责。
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserInputValidator userInputValidator;

    /**
     * 接收注册 DTO，依次规范化用户名、校验字段、检查重名、生成密码哈希并插入用户。
     * 无返回数据；用户名冲突转为 409，其他未处理的数据库异常交由全局兜底处理。
     */
    @Override
    public void register(UserRegisterDTO dto) {
        userInputValidator.normalizeAndValidate(dto);
        // 先查重可快速给出友好错误，但查询与插入不是原子操作，不能单靠这次查询防止并发重名。
        //1. 查询用户名是否已存在
        User oldUser = userMapper.findByUserName(dto.getUserName());
        if(oldUser != null){
            throw new BusinessException(409, "用户名已存在");
        }
        //2. 创建新用户
        User user = new User();
        user.setUserName(dto.getUserName());
        //3. 生成密码哈希
        // encode 每次使用随机盐，即便密码相同，生成的哈希通常也不同；数据库只保存这个哈希。
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        //4.  保存用户到数据库
        try {
            userMapper.insert(user);
            //5. 处理并发重复写入
            // 两个请求可能同时查到用户名未占用；数据库唯一约束最终兜底，冲突在这里仍转换为 409。
        } catch (DuplicateKeyException e) {
            throw new BusinessException(409, "用户名已存在");
        }
    }

    /**
     * 接收登录 DTO，经相同用户名规范化与字段校验后查询用户，再核验密码。
     * 账号不存在和密码不匹配都返回同一 401 消息；成功返回用户实体，仅供服务端使用。
     */
    @Override
    public User login(UserLoginDTO dto) {
        userInputValidator.normalizeAndValidate(dto);
        User user = userMapper.findByUserName(dto.getUserName());
        if(user == null){
            throw new BusinessException(401, "用户名或密码错误");
        }
        // BCrypt 含随机盐，不能重新 encode 后做字符串相等比较；matches 从已存哈希提取盐和参数核验。
        if(!passwordEncoder.matches(dto.getPassword(), user.getPasswordHash())){
            throw new BusinessException(401, "用户名或密码错误");
        }
        return user;
    }

}
