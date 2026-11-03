package com.sebastian.bloghub;

import com.sebastian.bloghub.dto.UserLoginDTO;
import com.sebastian.bloghub.dto.UserRegisterDTO;
import com.sebastian.bloghub.entity.User;
import com.sebastian.bloghub.exception.BusinessException;
import com.sebastian.bloghub.mapper.UserMapper;
import com.sebastian.bloghub.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
class UserInputValidationTests {

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private WebApplicationContext applicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext).build();
    }

    @Test
    void registerRejectsNullUsernameAsBadRequest() {
        UserRegisterDTO dto = registration(null, "BlogHub!2026");

        assertBadRequest(() -> userService.register(dto));
    }

    @Test
    void registerRejectsInternalUsernameWhitespace() {
        UserRegisterDTO dto = registration("Invalid Name", "BlogHub!2026");

        assertBadRequest(() -> userService.register(dto));
    }

    @Test
    void registerRejectsPasswordWhitespaceWithoutRemovingIt() {
        UserRegisterDTO dto = registration("InvalidPasswordTest", " BlogHub!2026 ");

        assertBadRequest(() -> userService.register(dto));
        assertEquals(" BlogHub!2026 ", dto.getPassword());
        assertNull(userMapper.findByUserName("InvalidPasswordTest"));
    }

    @Test
    void loginNormalizesUsernameCreatedThroughAllArgumentsConstructor() {
        UserLoginDTO dto = new UserLoginDTO("  Day2User01  ", "BlogHub!2026");

        User user = assertDoesNotThrow(() -> userService.login(dto));

        assertNotNull(user);
        assertEquals("Day2User01", user.getUserName());
        assertEquals("Day2User01", dto.getUserName());
    }

    @Test
    void loginRejectsNullUsernameAsBadRequest() {
        UserLoginDTO dto = new UserLoginDTO(null, "BlogHub!2026");

        assertBadRequest(() -> userService.login(dto));
    }

    @Test
    void loginRejectsPasswordWhitespaceAsBadRequest() {
        UserLoginDTO dto = new UserLoginDTO("Day2User01", " BlogHub!2026 ");

        assertBadRequest(() -> userService.login(dto));
        assertEquals(" BlogHub!2026 ", dto.getPassword());
    }

    @Test
    void registerHttpNormalizesBeforeCheckingTwentyCharacterLimit() throws Exception {
        String userName = UUID.randomUUID().toString().replace("-", "").substring(0, 20);
        String body = "{\"userName\":\"  " + userName
                + "  \",\"password\":\"BlogHub!2026\"}";

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(1))
                .andExpect(content().json("{\"status\":1,\"message\":\"注册成功\",\"data\":null}"));

        User savedUser = userMapper.findByUserName(userName);
        assertNotNull(savedUser);
        assertEquals(userName, savedUser.getUserName());
        assertTrue(passwordEncoder.matches("BlogHub!2026", savedUser.getPasswordHash()));
    }

    @Test
    void debugValidationStillReturnsNormalizedUsername() throws Exception {
        mockMvc.perform(post("/debug/validate-register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userName\":\"  Alice  \",\"password\":\"BlogHub!2026\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("Alice"));
    }

    private UserRegisterDTO registration(String userName, String password) {
        UserRegisterDTO dto = new UserRegisterDTO();
        dto.setUserName(userName);
        dto.setPassword(password);
        return dto;
    }

    private void assertBadRequest(Executable operation) {
        BusinessException exception = assertThrows(BusinessException.class, operation);
        assertEquals(400, exception.getHttpStatus());
    }
}
