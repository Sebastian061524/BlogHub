package com.sebastian.bloghub;

import com.sebastian.bloghub.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import java.nio.charset.StandardCharsets;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.http.MediaType;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * JWT工具及MVC认证入口测试，分别观察解析器异常与MockMvc请求的HTTP状态和响应体。
 * MockMvc基于应用Web上下文执行Spring MVC流程，不启动真实网络服务器。
 * 篡改测试保留原签名只修改载荷；过期测试须让签发与验签用同一密钥，以免把签名失败误当过期。
 * 配置密钥仅用于构造令牌，不应在注释、日志或测试输出中打印。
 */
@SpringBootTest
public class JwtTests {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private WebApplicationContext applicationContext;

    private MockMvc mockMvc;
    /**
     * 每个用例前基于应用Web上下文重建MockMvc，请求经过应用MVC配置并在测试进程内执行。
     */
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext).build();
    }

    @Value("${jwt.secret-base64}")
    private String configuredSecretBase64;

    /**
     * 生成合法JWT，解码载荷后只修改subject并保留原签名；断言工具解析抛签名异常，验证篡改不能通过验签。
     */
    @Test
    public void testJwtUtil() {
        String token = jwtUtil.generateToken(123);
        String[] split = token.split("\\.");
        String payload = new String(
                Base64.getUrlDecoder().decode(split[1]),
                StandardCharsets.UTF_8
        );
        payload = payload.replace("\"sub\":\"123\"", "\"sub\":\"456\"");
        String tamperedToken = split[0] + "." + Base64.getUrlEncoder().withoutPadding().encodeToString(payload.getBytes(StandardCharsets.UTF_8)) + "." + split[2];
        assertThrows(SignatureException.class, () -> jwtUtil.parseToken(tamperedToken));
    }

    /**
     * 把同样篡改载荷的JWT按Bearer格式请求创建文章；断言HTTP 401及统一失败响应，验证MVC认证入口处理验签失败。
     */
    @Test
    public void testJwtUtilWithInvalidToken() throws Exception {
        String token = jwtUtil.generateToken(123);
        String[] split = token.split("\\.");
        String payload = new String(
                Base64.getUrlDecoder().decode(split[1]),
                StandardCharsets.UTF_8
        );
        payload = payload.replace("\"sub\":\"123\"", "\"sub\":\"456\"");
        String tamperedToken ="Bearer " + split[0] + "." + Base64.getUrlEncoder().withoutPadding().encodeToString(payload.getBytes(StandardCharsets.UTF_8)) + "." + split[2];
        mockMvc.perform(post("/articles")
                        .header("Authorization", tamperedToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "title": "验签测试",
                          "body": "测试正文",
                          "categoryId": 1
                        }
                        """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(0))
                .andExpect(jsonPath("$.message").value("登录凭证无效或已过期"));
    }

    /**
     * 提交合法文章JSON但省略认证头；断言401、失败状态及登录提示，令失败原因集中于缺少凭证。
     */
    @Test
    public void testCreateArticleWithoutAuthorization() throws Exception {
        mockMvc.perform(post("/articles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        """
                        {
                          "title": "测试标题",
                          "body": "测试正文",
                          "categoryId": 1
                        }
                        """
                ))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(0))
                .andExpect(jsonPath("$.message").value("请先登录"));
    }

    /**
     * 使用只有Bearer前缀的空凭证；断言401和登录提示，验证空令牌按未登录处理。
     */
    @Test
    public void testCreateArticleWithBlankToken() throws Exception {
        mockMvc.perform(post("/articles")
                .header("Authorization", "Bearer ")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        """
                        {
                          "title": "测试标题",
                          "body": "测试正文",
                          "categoryId": 1
                        }
                        """
                ))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(0))
                .andExpect(jsonPath("$.message").value("请先登录"));
    }

    /**
     * 先签发合法令牌，再故意不加Bearer前缀；断言401和登录提示，验证认证头格式要求而非签名本身。
     */
    @Test
    public void testCreateArticleWithoutBearerPrefix() throws Exception {
        String token = jwtUtil.generateToken(3);
        mockMvc.perform(post("/articles")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        """
                        {
                          "title": "测试标题",
                          "body": "测试正文",
                          "categoryId": 1
                        }
                        """
                ))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(0))
                .andExpect(jsonPath("$.message").value("请先登录"));
    }

    /**
     * 用固定测试密钥与负有效期生成已过期JWT，再由同一工具实例解析；断言过期异常，排除不同密钥引起的验签失败。
     */
    @Test
    public void testParseExpiredToken() {
        String secretBase64 = Base64.getEncoder().encodeToString(
                "0123456789abcdef0123456789abcdef"
                        .getBytes(StandardCharsets.UTF_8)
        );
        JwtUtil jwtUtil1 = new JwtUtil(secretBase64, -60);
        String token = jwtUtil1.generateToken(3);
        assertThrows(ExpiredJwtException.class, () -> jwtUtil1.parseToken(token));
    }

    /**
     * 用应用配置的同一密钥和负有效期生成已过期JWT，再发MockMvc请求；断言401和统一无效或过期消息。
     * 同密钥保证命中过期场景；此处测试MVC响应，不涉及真实HTTP网络传输。
     */
    @Test
    public void testParseExpiredTokenWithHttp() throws Exception {
        JwtUtil jwtUtil1 = new JwtUtil(configuredSecretBase64, -60);
        String token = jwtUtil1.generateToken(3);
        mockMvc.perform(post("/articles")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        """
                        {
                          "title": "测试标题",
                          "body": "测试正文",
                          "categoryId": 1
                        }
                        """
                ))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(0))
                .andExpect(jsonPath("$.message").value("登录凭证无效或已过期"));
    }

}
