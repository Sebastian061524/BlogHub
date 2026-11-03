package com.sebastian.bloghub.mapper;

import com.sebastian.bloghub.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {

    @Select("SELECT id, user_name, password_hash FROM blog_user WHERE user_name = #{userName}")
    User findByUserName(String userName);

    @Insert("INSERT INTO blog_user (user_name, password_hash) VALUES (#{userName}, #{passwordHash})")
    void insert(User user);
}
