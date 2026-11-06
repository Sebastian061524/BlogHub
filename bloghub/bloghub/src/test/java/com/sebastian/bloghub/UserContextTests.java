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


@SpringBootTest
public class UserContextTests {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private WebApplicationContext applicationContext;

    private MockMvc mockMvc;
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext).build();
    }

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
