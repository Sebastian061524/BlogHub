package com.sebastian.bloghub.mapper;

import com.sebastian.bloghub.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 用户持久层接口，直接以MyBatis注解声明注册和登录需要的SQL。
 * MyBatis的{@code @Mapper}使框架扫描接口并创建代理，调用方法时执行对应SQL。
 */
@Mapper
public interface UserMapper {

    /** 按用户名查询用户及密码哈希；驼峰配置映射下划线列名，未找到时返回null。 */
    @Select("SELECT id, user_name, password_hash FROM blog_user WHERE user_name = #{userName}")
    User findByUserName(String userName);

    /** 写入用户名和已编码的密码哈希；未配置生成主键回填，也不返回主键。 */
    @Insert("INSERT INTO blog_user (user_name, password_hash) VALUES (#{userName}, #{passwordHash})")
    void insert(User user);
}
