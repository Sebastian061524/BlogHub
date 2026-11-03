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

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserInputValidator userInputValidator;

    @Override
    public void register(UserRegisterDTO dto) {
        userInputValidator.normalizeAndValidate(dto);
        //1. 查询用户名是否已存在
        User oldUser = userMapper.findByUserName(dto.getUserName());
        if(oldUser != null){
            throw new BusinessException(409, "用户名已存在");
        }
        //2. 创建新用户
        User user = new User();
        user.setUserName(dto.getUserName());
        //3. 生成密码哈希
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        //4.  保存用户到数据库
        try {
            userMapper.insert(user);
            //5. 处理并发重复写入
        } catch (DuplicateKeyException e) {
            throw new BusinessException(409, "用户名已存在");
        }
    }

    @Override
    public User login(UserLoginDTO dto) {
        userInputValidator.normalizeAndValidate(dto);
        User user = userMapper.findByUserName(dto.getUserName());
        if(user == null){
            throw new BusinessException(401, "用户名或密码错误");
        }
        if(!passwordEncoder.matches(dto.getPassword(), user.getPasswordHash())){
            throw new BusinessException(401, "用户名或密码错误");
        }
        return user;
    }

}
