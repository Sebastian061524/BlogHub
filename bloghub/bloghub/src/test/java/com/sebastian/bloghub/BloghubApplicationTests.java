package com.sebastian.bloghub;

import com.sebastian.bloghub.dto.UserLoginDTO;
import com.sebastian.bloghub.entity.User;
import com.sebastian.bloghub.exception.BusinessException;
import com.sebastian.bloghub.mapper.UserMapper;
import com.sebastian.bloghub.service.UserService;
import com.sebastian.bloghub.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BloghubApplicationTests {

    @Autowired
	private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtUtil jwtUtil;

    @Test
    void testJdbc(){
        Integer categoryCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM blog_category", Integer.class);
        assertEquals(3, categoryCount);
    }

    @Test
    void testUserMapper(){
        User user = userMapper.findByUserName("Alice");
        assertNotNull(user);
        assertNotNull(user.getPasswordHash());
        assertEquals("Alice", user.getUserName());
    }

    @Test
    void testDifferentUser(){
        User user1 = userMapper.findByUserName("Alice");
        User user2 = userMapper.findByUserName("alice");
        assertNotNull(user1);
        assertNotNull(user2);
        assertNotNull(user1.getId());
        assertNotNull(user2.getId());
        assertNotEquals(user1.getId(), user2.getId());
    }

    @Test
    void testNotExistUser(){
        User user = userMapper.findByUserName("NotExistUser");
        assertNull(user);
    }

    @Test
    void testPasswordEncoder(){
        User user = userMapper.findByUserName("Day2User01");
        assertNotNull(user);
        assertEquals("Day2User01", user.getUserName());
        assertNotNull(user.getPasswordHash());
        assertNotEquals("BlogHub!2026", user.getPasswordHash());
        assertTrue(passwordEncoder.matches("BlogHub!2026", user.getPasswordHash()));
        assertFalse(passwordEncoder.matches("123456", user.getPasswordHash()));
    }

    @Test
    void testLogin(){
        UserLoginDTO dto = new UserLoginDTO("Day2User01", "BlogHub!2026");
        User user = userService.login(dto);
        assertNotNull(user);
        assertEquals("Day2User01", user.getUserName());
    }

    @Test
    void testWrongPasswordLogin(){
        UserLoginDTO dto = new UserLoginDTO("Day2User01", "123456");
        BusinessException ex = assertThrows(BusinessException.class, () -> userService.login(dto));
        assertEquals(401, ex.getHttpStatus());
        assertEquals("用户名或密码错误", ex.getMessage());
    }

    @Test
    void testNotExistUserLogin(){
        UserLoginDTO dto = new UserLoginDTO("112233", "123456");
        BusinessException ex = assertThrows(BusinessException.class, () -> userService.login(dto));
        assertEquals(401, ex.getHttpStatus());
        assertEquals("用户名或密码错误", ex.getMessage());
    }
    
    @Test
    void testGenerateToken(){
        String token = jwtUtil.generateToken(123);
        assertNotNull(token);

        Claims claims = jwtUtil.parseToken(token);
        assertEquals("123", claims.getSubject());
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
        assertEquals(7_200_000L,
                claims.getExpiration().getTime() - claims.getIssuedAt().getTime());
    }
    
    @Test
    void testGenerateTokenWithInvalidUserIds(){
        assertThrows(IllegalArgumentException.class, () -> jwtUtil.generateToken(null));
        assertThrows(IllegalArgumentException.class, () -> jwtUtil.generateToken(0));
        assertThrows(IllegalArgumentException.class, () -> jwtUtil.generateToken(-1));
    }

}
