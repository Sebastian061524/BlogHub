package com.sebastian.bloghub;

import com.sebastian.bloghub.context.UserContext;
import com.sebastian.bloghub.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/**
 * 请求异常场景的MVC测试，检查字段校验、JSON格式及媒体类型错误的响应。
 * 只有显式assertNull的用例直接证明请求结束后UserContext为空。
 * finally中的手动清理是测试隔离措施，不能证明拦截器已自动清理；这里也未验证线程间隔离。
 */
@SpringBootTest
public class UserContextTests {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private WebApplicationContext applicationContext;

    private MockMvc mockMvc;
    /**
     * 每次基于应用Web上下文构建MockMvc，用于执行拦截器和MVC异常处理流程，无需启动网络服务器。
     */
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext).build();
    }

    /**
     * 先清空上下文，以合法令牌提交空标题；断言400后立即assertNull线程用户，验证校验失败后的自动清理。
     * finally再次清理只为避免失败污染后续用例，不能替代请求后的assertNull。
     */
    @Test
    public void testUserContextClearedAfterValidationFailure() throws Exception{
        try {
            UserContext.removeCurrentUserId();
            String token = jwtUtil.generateToken(3);
            mockMvc.perform(MockMvcRequestBuilders.post("/articles")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                            """
                            {
                              "title": "",
                              "body": "测试正文",
                              "categoryId": 1
                            }
                            """
                    ))
                    .andExpect(status().isBadRequest());
            assertNull(UserContext.getCurrentUserId());
        } finally {
            UserContext.removeCurrentUserId();
        }
    }

    /**
     * 以合法令牌提交缺少闭合括号的JSON；这里只断言400和失败状态。
     * 没有请求后的上下文断言，finally手工清理不能证明系统已自动清理用户上下文。
     */
    @Test
    public void testCreateArticleWithMalformedJson() throws Exception {
        try {
            UserContext.removeCurrentUserId();
            String token = jwtUtil.generateToken(3);
            mockMvc.perform(MockMvcRequestBuilders.post("/articles")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(
                                    """
                                    {
                                      "title": "测试标题",
                                      "body": "测试正文",
                                      "categoryId": 1

                                    """
                            ))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(0));
        } finally {
            UserContext.removeCurrentUserId();
        }
    }

    /**
     * 以合法令牌提交text/plain格式的文章内容；这里只断言415及失败状态，验证不支持媒体类型的响应。
     * 没有断言请求后上下文为空，finally只承担测试隔离清理。
     */
    @Test
    public void testCreateArticleWithUnsupportedContentType() throws Exception {
        try {
            UserContext.removeCurrentUserId();
            String token = jwtUtil.generateToken(3);
            mockMvc.perform(MockMvcRequestBuilders.post("/articles")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.TEXT_PLAIN)
                            .content(
                                    """
                                    {
                                      "title": "测试标题",
                                      "body": "测试正文",
                                      "categoryId": 1
                                    }
                                    """
                            ))
                    .andExpect(status().isUnsupportedMediaType())
                    .andExpect(jsonPath("$.status").value(0));
        } finally {
            UserContext.removeCurrentUserId();
        }
    }

}
