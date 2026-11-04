package com.sebastian.bloghub;

import com.sebastian.bloghub.context.UserContext;
import com.sebastian.bloghub.dto.ArticleCreateDTO;
import com.sebastian.bloghub.dto.ArticleUpdateDTO;
import com.sebastian.bloghub.dto.UserLoginDTO;
import com.sebastian.bloghub.entity.Article;
import com.sebastian.bloghub.entity.User;
import com.sebastian.bloghub.exception.BusinessException;
import com.sebastian.bloghub.mapper.ArticleMapper;
import com.sebastian.bloghub.mapper.CategoryMapper;
import com.sebastian.bloghub.mapper.UserMapper;
import com.sebastian.bloghub.service.ArticleService;
import com.sebastian.bloghub.service.UserService;
import com.sebastian.bloghub.util.JwtUtil;
import com.sebastian.bloghub.vo.ArticleDetailVO;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private ArticleService articleService;


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

    @Test
    void testGetUserId(){
        String token = jwtUtil.generateToken(123);
        Integer userId = jwtUtil.getUserId(token);
        assertEquals(123, userId);
    }

    @Test
    @Transactional
    void testInsertArticle(){
        Article article = new Article();
        article.setTitle("Test Article");
        article.setBody("This is a test article.");
        article.setUserId(1);
        article.setCategoryId(1);
        articleMapper.insert(article);
        assertNotNull(article.getId());
        assertTrue(article.getId() > 0);
        Integer i = jdbcTemplate.queryForObject("select user_id from blog_article where id = ?", Integer.class, article.getId());
        assertEquals(1, i);
        Integer j = jdbcTemplate.queryForObject("select category_id from blog_article where id = ?", Integer.class, article.getId());
        assertEquals(1, j);
        String title = jdbcTemplate.queryForObject("select title from blog_article where id = ?", String.class, article.getId());
        assertEquals("Test Article", title);
    }

    @Test
    void testCountById(){
        int count = categoryMapper.countById(1);
        assertEquals(1, count);
        count = categoryMapper.countById(4);
        assertEquals(0, count);
    }

    @Test
    @Transactional
    void testCreateArticle1() {
        try {
            UserContext.setCurrentUserId(1);
            ArticleCreateDTO articleCreateDTO = new ArticleCreateDTO();
            articleCreateDTO.setTitle("Test Article");
            articleCreateDTO.setBody("This is a test article.");
            articleCreateDTO.setCategoryId(1);
            Integer articleId = articleService.create(articleCreateDTO);
            assertNotNull(articleId);
            assertTrue(articleId > 0);
            Integer userId = jdbcTemplate.queryForObject("select user_id from blog_article where id = ?", Integer.class, articleId);
            assertEquals(1, userId);
        } finally {
            UserContext.removeCurrentUserId();
        }
    }

    @Test
    @Transactional
    void testCreateArticleWithoutUserId() {
        UserContext.removeCurrentUserId();
        try {
            ArticleCreateDTO articleCreateDTO = new ArticleCreateDTO();
            articleCreateDTO.setTitle("Test Article");
            articleCreateDTO.setBody("This is a test article.");
            articleCreateDTO.setCategoryId(1);
            BusinessException ex = assertThrows(BusinessException.class, () -> articleService.create(articleCreateDTO));
            assertEquals(401, ex.getHttpStatus());
            assertEquals("请先登录", ex.getMessage());
        } finally {
            UserContext.removeCurrentUserId();
        }
    }

    @Test
    @Transactional
    void testCreateArticleWithWrongCategoryId() {
        UserContext.setCurrentUserId(1);
        try {
            ArticleCreateDTO articleCreateDTO = new ArticleCreateDTO();
            articleCreateDTO.setTitle("Test Article");
            articleCreateDTO.setBody("This is a test article.");
            articleCreateDTO.setCategoryId(4);
            BusinessException ex = assertThrows(BusinessException.class, () -> articleService.create(articleCreateDTO));
            assertEquals(404, ex.getHttpStatus());
            assertEquals("分类不存在", ex.getMessage());
        } finally {
            UserContext.removeCurrentUserId();
        }
    }

    @Test
    @Transactional
    void testQueryArticleDetail(){
        Article article = createTestArticle(4);
        ArticleDetailVO articleDetailVO = articleMapper.findDetailById(article.getId());
        assertNotNull(articleDetailVO);
        assertEquals(article.getId(), articleDetailVO.getArticleId());
        assertNotNull(articleDetailVO.getAuthor());
        assertEquals(4, articleDetailVO.getAuthor().getUserId());
        assertEquals("Day2User01", articleDetailVO.getAuthor().getUserName());
        assertNotNull(articleDetailVO.getCategory());
        assertEquals(1, articleDetailVO.getCategory().getCategoryId());
        assertEquals("Java", articleDetailVO.getCategory().getCategoryName());
        assertEquals(article.getTitle(), articleDetailVO.getTitle());
        assertEquals(article.getBody(), articleDetailVO.getBody());
        assertNotNull(articleDetailVO.getCreateTime());
        assertNotNull(articleDetailVO.getUpdateTime());
    }

    @Test
    @Transactional
    void testQueryArticleDetailWithNotExistsId() {
        Article article = createTestArticle(4);
        assertEquals(1, jdbcTemplate.update("DELETE FROM blog_article WHERE id = ?", article.getId()));
        assertNull(articleMapper.findDetailById(article.getId()));
    }

    @Test
    @Transactional
    void testUpdateArticle() {
        UserContext.setCurrentUserId(4);
        try {
            Integer articleId = createTestArticle(4).getId();
            LocalDateTime originalCreateTime = articleMapper.findDetailById(articleId).getCreateTime();
            ArticleUpdateDTO articleUpdateDTO = new ArticleUpdateDTO();
            articleUpdateDTO.setTitle("Updated Title");
            articleUpdateDTO.setBody("Updated Body");
            articleUpdateDTO.setCategoryId(2);
            articleService.update(articleId, articleUpdateDTO);
            ArticleDetailVO updatedArticle = articleMapper.findDetailById(articleId);
            assertNotNull(updatedArticle);
            assertEquals(originalCreateTime, updatedArticle.getCreateTime());
            assertEquals("Updated Title", updatedArticle.getTitle());
            assertEquals("Updated Body", updatedArticle.getBody());
            assertEquals(2, updatedArticle.getCategory().getCategoryId());
            assertEquals(4, updatedArticle.getAuthor().getUserId());
        } finally {
            UserContext.removeCurrentUserId();
        }
    }

    @Test
    @Transactional
    void testUpdateArticleWithWrongUserId() {
        UserContext.setCurrentUserId(1);
        try {
            Integer articleId = createTestArticle(4).getId();
            ArticleDetailVO before = articleMapper.findDetailById(articleId);
            assertNotNull(before);
            ArticleUpdateDTO articleUpdateDTO = new ArticleUpdateDTO();
            articleUpdateDTO.setTitle("Updated Title");
            articleUpdateDTO.setBody("Updated Body");
            articleUpdateDTO.setCategoryId(2);
            BusinessException ex = assertThrows(BusinessException.class, () -> articleService.update(articleId, articleUpdateDTO));
            assertEquals(403, ex.getHttpStatus());
            assertEquals("无权限", ex.getMessage());
            ArticleDetailVO after = articleMapper.findDetailById(articleId);
            assertNotNull(after);
            assertEquals(before.getCreateTime(), after.getCreateTime());
            assertEquals(before.getTitle(), after.getTitle());
            assertEquals(before.getBody(), after.getBody());
            assertEquals(before.getCategory().getCategoryId(), after.getCategory().getCategoryId());
            assertEquals(before.getAuthor().getUserId(), after.getAuthor().getUserId());
            assertEquals(before.getUpdateTime(), after.getUpdateTime());
        } finally {
            UserContext.removeCurrentUserId();
        }
    }

    @Test
    @Transactional
    void testUpdateArticleWithoutUpdate(){
        UserContext.setCurrentUserId(4);
        try {
            Integer articleId = createTestArticle(4).getId();
            ArticleUpdateDTO articleUpdateDTO = new ArticleUpdateDTO();
            articleUpdateDTO.setTitle("Updated Title");
            articleUpdateDTO.setBody("Updated Body");
            articleUpdateDTO.setCategoryId(2);
            articleService.update(articleId, articleUpdateDTO);
            assertDoesNotThrow(() -> articleService.update(articleId, articleUpdateDTO));
            ArticleDetailVO articleDetailVO = articleMapper.findDetailById(articleId);
            assertNotNull(articleDetailVO);
            assertEquals("Updated Title", articleDetailVO.getTitle());
            assertEquals("Updated Body", articleDetailVO.getBody());
            assertEquals(2, articleDetailVO.getCategory().getCategoryId());
            assertEquals(4, articleDetailVO.getAuthor().getUserId());
        } finally {
            UserContext.removeCurrentUserId();
        }
    }

    @Test
    @Transactional
    void testDeleteArticleWithComments() {
        try {
            User author = userMapper.findByUserName("Day2User01");
            assertNotNull(author);
            assertEquals(1, categoryMapper.countById(1));
            UserContext.setCurrentUserId(author.getId());

            // 创建本次测试专用文章，不修改已有文章 7。
            Article article = new Article();
            article.setUserId(author.getId());
            article.setTitle("Article for deletion test");
            article.setBody("This article and its comment should be deleted together.");
            article.setCategoryId(1);
            articleMapper.insert(article);
            assertNotNull(article.getId());
            Integer articleId = article.getId();

            int insertedComments = jdbcTemplate.update(
                    "INSERT INTO blog_comment (article_id, user_id, text) VALUES (?, ?, ?)",
                    articleId, author.getId(), "Comment for deletion test");
            assertEquals(1, insertedComments);
            assertEquals(1, jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM blog_article WHERE id = ?", Integer.class, articleId));
            assertEquals(1, jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM blog_comment WHERE article_id = ?", Integer.class, articleId));

            articleService.delete(articleId);

            assertNull(articleMapper.findDetailById(articleId));
            assertEquals(0, jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM blog_article WHERE id = ?", Integer.class, articleId));
            assertEquals(0, jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM blog_comment WHERE article_id = ?", Integer.class, articleId));
        } finally {
            UserContext.removeCurrentUserId();
        }
    }

    private Article createTestArticle(Integer userId) {
        Article article = new Article();
        article.setUserId(userId);
        article.setTitle("Test Article");
        article.setBody("This is a test article.");
        article.setCategoryId(1);
        articleMapper.insert(article);
        assertNotNull(article.getId());
        assertTrue(article.getId() > 0);
        return article;
    }
}
