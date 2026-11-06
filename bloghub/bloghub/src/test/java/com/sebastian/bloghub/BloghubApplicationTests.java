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

/**
 * 应用基础集成测试：覆盖数据库查询、密码匹配、登录、JWT及文章增删改查的指定场景。
 * 依赖预置用户、三个分类及其对应关系；固定数据断言不能直接套用到任意数据库。
 * 测试注解@Transactional标记的方法回滚测试写入；UserContext是线程状态，需要在finally中另行清理。
 * 各项断言只保证已检查的结果，不能据此声称全部业务分支或真实网络链路都被覆盖。
 */
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


    /**
     * 直接查询分类总数并断言为3，完成连接及预置数据的最小检查，不代表全部表结构验证。
     */
    @Test
    void testJdbc(){
        Integer categoryCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM blog_category", Integer.class);
        assertEquals(3, categoryCount);
    }

    /**
     * 按预置名称查用户，断言对象、密码哈希及用户名映射存在，检查Mapper基本查询。
     */
    @Test
    void testUserMapper(){
        User user = userMapper.findByUserName("Alice");
        assertNotNull(user);
        assertNotNull(user.getPasswordHash());
        assertEquals("Alice", user.getUserName());
    }

    /**
     * 查询仅大小写不同的两个预置名称，断言ID不同，验证当前数据库与查询能区分这两个账户。
     */
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

    /**
     * 以约定不存在的名称查询，断言返回null，验证Mapper无记录时的返回约定。
     */
    @Test
    void testNotExistUser(){
        User user = userMapper.findByUserName("NotExistUser");
        assertNull(user);
    }

    /**
     * 读取预置测试账户的哈希；断言其不同于测试明文，且正确密码匹配、错误密码不匹配，验证编码器与种子哈希兼容。
     */
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

    /**
     * 用预置测试账户准备登录DTO并调用Service；断言返回用户及其名称正确，验证成功登录路径。
     */
    @Test
    void testLogin(){
        UserLoginDTO dto = new UserLoginDTO("Day2User01", "BlogHub!2026");
        User user = userService.login(dto);
        assertNotNull(user);
        assertEquals("Day2User01", user.getUserName());
    }

    /**
     * 准备存在的用户名和错误测试密码；断言401与统一错误消息，验证密码不匹配的拒绝路径。
     */
    @Test
    void testWrongPasswordLogin(){
        UserLoginDTO dto = new UserLoginDTO("Day2User01", "123456");
        BusinessException ex = assertThrows(BusinessException.class, () -> userService.login(dto));
        assertEquals(401, ex.getHttpStatus());
        assertEquals("用户名或密码错误", ex.getMessage());
    }

    /**
     * 准备不存在的用户名；断言同样的401与错误消息，验证该场景使用统一登录失败反馈。
     */
    @Test
    void testNotExistUserLogin(){
        UserLoginDTO dto = new UserLoginDTO("112233", "123456");
        BusinessException ex = assertThrows(BusinessException.class, () -> userService.login(dto));
        assertEquals(401, ex.getHttpStatus());
        assertEquals("用户名或密码错误", ex.getMessage());
    }
    
    /**
     * 为正数用户ID签发后解析JWT；断言subject和时间字段，检查过期时间减签发时间为配置的两小时。
     */
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
    
    /**
     * 分别以null、0和负数签发令牌，断言参数异常，验证用户ID基本前置校验。
     */
    @Test
    void testGenerateTokenWithInvalidUserIds(){
        assertThrows(IllegalArgumentException.class, () -> jwtUtil.generateToken(null));
        assertThrows(IllegalArgumentException.class, () -> jwtUtil.generateToken(0));
        assertThrows(IllegalArgumentException.class, () -> jwtUtil.generateToken(-1));
    }

    /**
     * 为指定用户ID签发令牌后读取ID，断言还原为同一整数，验证subject转换的成功路径。
     */
    @Test
    void testGetUserId(){
        String token = jwtUtil.generateToken(123);
        Integer userId = jwtUtil.getUserId(token);
        assertEquals(123, userId);
    }

    /**
     * 直接插入专用文章，再用独立SQL读取字段；断言正数主键回填及作者、分类、标题正确，结束后由测试事务回滚。
     */
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

    /**
     * 对预置分类1与不存在的分类4计数，断言分别为1和0，验证存在性SQL的两种返回。
     */
    @Test
    void testCountById(){
        int count = categoryMapper.countById(1);
        assertEquals(1, count);
        count = categoryMapper.countById(4);
        assertEquals(0, count);
    }

    /**
     * 设置线程身份为用户1，以合法DTO创建文章；断言正数ID并查库确认作者来自当前身份，最后清理线程状态。
     */
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

    /**
     * 先移除线程身份，再用合法DTO创建；断言401及登录提示，排除字段错误对认证场景的干扰。
     */
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

    /**
     * 设置登录身份，使用不存在的分类4创建；断言404与分类不存在提示，定位分类校验失败。
     */
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

    /**
     * 插入用户4、分类1的专用文章后查详情；逐项检查文章字段、关联作者和分类及时间映射，验证联表组装。
     */
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

    /**
     * 先插入取得真实ID，再显式删除该行；按相同ID查询应为null，避免猜测某个ID不存在。
     */
    @Test
    @Transactional
    void testQueryArticleDetailWithNotExistsId() {
        Article article = createTestArticle(4);
        assertEquals(1, jdbcTemplate.update("DELETE FROM blog_article WHERE id = ?", article.getId()));
        assertNull(articleMapper.findDetailById(article.getId()));
    }

    /**
     * 以文章作者身份更新标题、正文和分类；回查断言新字段生效、创建时间与作者保持原值。
     */
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

    /**
     * 创建用户4的文章，以用户1请求更新；断言403并逐字段比较前后相同，验证拒绝后没有可见改动。
     */
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

    /**
     * 作者先更新一次，再重复相同更新；断言第二次不抛异常且字段正确，验证重复提交相同内容可接受。
     */
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

    /**
     * 新建专用文章及一条评论，先断言两者存在，再由作者调用删除；断言文章查不到且文章、评论计数均为0。
     * 本用例验证成功删除结果，外层事务用于隔离数据；失败回滚由不带测试外层事务的专门测试验证。
     */
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

    /**
     * 创建指定作者、分类1的专用文章，断言主键回填并返回实体；调用方用测试事务隔离数据，助手只负责准备。
     */
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
