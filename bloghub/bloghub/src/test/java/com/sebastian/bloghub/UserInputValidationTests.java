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

/**
 * 用户输入规范化与校验的集成测试，兼顾直接调用Service及MockMvc的JSON请求入口。
 * 类级事务回滚注册产生的数据库记录，登录用例依赖预置测试用户。
 * Service测试检查异常及DTO状态，HTTP测试检查绑定、校验和响应，两者验证的层次不同。
 */
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

    /**
     * 每项测试前基于应用Web上下文构建MockMvc，供JSON绑定及校验入口测试使用。
     */
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext).build();
    }

    /**
     * 用空用户名与合法测试密码准备DTO，直接调用Service并断言400，验证服务层同样拒绝空用户名。
     */
    @Test
    void registerRejectsNullUsernameAsBadRequest() {
        UserRegisterDTO dto = registration(null, "BlogHub!2026");

        assertBadRequest(() -> userService.register(dto));
    }

    /**
     * 准备内部含空格的用户名并注册；断言400，区分可裁去的首尾空白与不允许的内部空白。
     */
    @Test
    void registerRejectsInternalUsernameWhitespace() {
        UserRegisterDTO dto = registration("Invalid Name", "BlogHub!2026");

        assertBadRequest(() -> userService.register(dto));
    }

    /**
     * 给测试密码加首尾空格后注册；断言400、DTO仍保留空格且没有新增用户，验证拒绝而非静默修改密码。
     */
    @Test
    void registerRejectsPasswordWhitespaceWithoutRemovingIt() {
        UserRegisterDTO dto = registration("InvalidPasswordTest", " BlogHub!2026 ");

        assertBadRequest(() -> userService.register(dto));
        assertEquals(" BlogHub!2026 ", dto.getPassword());
        assertNull(userMapper.findByUserName("InvalidPasswordTest"));
    }

    /**
     * 用全参构造器准备首尾带空格的用户名，再直接登录；断言返回用户及DTO名称均规范化，验证不依赖JSON setter路径。
     */
    @Test
    void loginNormalizesUsernameCreatedThroughAllArgumentsConstructor() {
        UserLoginDTO dto = new UserLoginDTO("  Day2User01  ", "BlogHub!2026");

        User user = assertDoesNotThrow(() -> userService.login(dto));

        assertNotNull(user);
        assertEquals("Day2User01", user.getUserName());
        assertEquals("Day2User01", dto.getUserName());
    }

    /**
     * 构造空用户名登录DTO并调用Service；断言400，验证参数非法与凭证错误的401有所区分。
     */
    @Test
    void loginRejectsNullUsernameAsBadRequest() {
        UserLoginDTO dto = new UserLoginDTO(null, "BlogHub!2026");

        assertBadRequest(() -> userService.login(dto));
    }

    /**
     * 用首尾带空格的密码登录；断言400且DTO密码原样保留，验证密码不自动trim。
     */
    @Test
    void loginRejectsPasswordWhitespaceAsBadRequest() {
        UserLoginDTO dto = new UserLoginDTO("Day2User01", " BlogHub!2026 ");

        assertBadRequest(() -> userService.login(dto));
        assertEquals(" BlogHub!2026 ", dto.getPassword());
    }

    /**
     * 生成20字符随机用户名，在JSON中加首尾空格后请求注册；断言201及完整成功响应。
     * 回查规范化名称和密码哈希匹配，验证先裁去用户名首尾空格再校验长度且确实保存用户；测试事务回滚该记录。
     */
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

    /**
     * 向调试校验端点发送首尾带空格的名称；断言200与规范化后的data，验证此入口仍经过DTO处理。
     */
    @Test
    void debugValidationStillReturnsNormalizedUsername() throws Exception {
        mockMvc.perform(post("/debug/validate-register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userName\":\"  Alice  \",\"password\":\"BlogHub!2026\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("Alice"));
    }

    /**
     * 以无参构造器和setter准备注册DTO，供各边界用例复用；只构造数据，不注册或修改密码。
     */
    private UserRegisterDTO registration(String userName, String password) {
        UserRegisterDTO dto = new UserRegisterDTO();
        dto.setUserName(userName);
        dto.setPassword(password);
        return dto;
    }

    /**
     * 执行给定操作，断言BusinessException及400状态；不额外保证异常消息内容。
     */
    private void assertBadRequest(Executable operation) {
        BusinessException exception = assertThrows(BusinessException.class, operation);
        assertEquals(400, exception.getHttpStatus());
    }
}
