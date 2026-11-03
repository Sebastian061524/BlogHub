package com.sebastian.bloghub.service;

import com.sebastian.bloghub.dto.UserLoginDTO;
import com.sebastian.bloghub.dto.UserRegisterDTO;
import com.sebastian.bloghub.entity.User;

public interface UserService {

    void register(UserRegisterDTO dto);

    User login(UserLoginDTO dto);
}
