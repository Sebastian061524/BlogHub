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

@SpringBootTest
public class JwtTests {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private WebApplicationContext applicationContext;

    private MockMvc mockMvc;
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext).build();
    }

    @Value("${jwt.secret-base64}")
    private String configuredSecretBase64;

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
