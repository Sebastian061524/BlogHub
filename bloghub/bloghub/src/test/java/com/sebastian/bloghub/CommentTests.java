package com.sebastian.bloghub;

import com.sebastian.bloghub.context.UserContext;
import com.sebastian.bloghub.dto.CommentCreateDTO;
import com.sebastian.bloghub.entity.Article;
import com.sebastian.bloghub.entity.Comment;
import com.sebastian.bloghub.entity.User;
import com.sebastian.bloghub.exception.BusinessException;
import com.sebastian.bloghub.mapper.ArticleMapper;
import com.sebastian.bloghub.mapper.CommentMapper;
import com.sebastian.bloghub.service.CommentService;
import com.sebastian.bloghub.vo.ArticleDetailVO;
import com.sebastian.bloghub.vo.CommentListItemVO;
import com.sebastian.bloghub.vo.CommentListVO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 评论新增与列表查询的集成测试，分别检查Mapper落库和Service的登录、文章存在性要求。
 * 依赖文章7、用户1和4及分类1；写入均处于测试事务内，结束后回滚。
 * 直接调用Service时手动设置线程用户模拟认证身份，此处不等同于测试HTTP拦截器认证。
 */
@SpringBootTest
public class CommentTests {

    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private CommentService commentService;
    @Autowired
    private ArticleMapper articleMapper;

    /**
     * 准备属于文章7、用户4的评论并调用Mapper；按回填ID独立查库，断言正文、关联ID和创建时间落库正确。
     */
    @Test
    @Transactional
    public void testCreateComment() {
        Comment comment = new Comment();
        comment.setArticleId(7);
        comment.setUserId(4);
        comment.setText("This is a test comment.");
        commentMapper.createComment(comment);
        Integer commentId    = comment.getId();
        assertNotNull(commentId);
        Comment c = jdbcTemplate.queryForObject(
                "SELECT id, article_id, user_id, text, create_time FROM blog_comment WHERE id = ?",
                new BeanPropertyRowMapper<>(Comment.class),
                commentId);
        assertNotNull(c);
        assertEquals(comment.getText(), c.getText());
        assertEquals(comment.getArticleId(), c.getArticleId());
        assertEquals(comment.getUserId(), c.getUserId());
        assertNotNull(c.getCreateTime());
    }

    /**
     * 设置线程用户1，对文章7调用Service创建；按返回ID查库，断言正文、文章ID与当前作者身份正确。
     */
    @Test
    @Transactional
    public void testCreateCommentsByArticleId() {
        try {
            UserContext.setCurrentUserId(1);
            CommentCreateDTO dto = new CommentCreateDTO();
            dto.setText("This is a test comment.");
            Integer commentId = commentService.create(7, dto);
            assertNotNull(commentId);
            assertTrue(commentId > 0);
            Comment comment = jdbcTemplate.queryForObject(
                    "SELECT id, article_id, user_id, text, create_time FROM blog_comment WHERE id = ?",
                    new BeanPropertyRowMapper<>(Comment.class),
                    commentId);
            assertNotNull(comment);
            assertEquals(dto.getText(), comment.getText());
            assertEquals(7, comment.getArticleId());
            assertEquals(1, comment.getUserId());
            assertNotNull(comment.getCreateTime());
        } finally {
            UserContext.removeCurrentUserId();
        }
    }

    /**
     * 先移除线程身份，再提交合法评论；断言401业务异常，验证未登录用户不能创建评论。
     */
    @Test
    @Transactional
    public void testCreateCommentsWithoutUserId() {
        UserContext.removeCurrentUserId();
        CommentCreateDTO commentCreateDTO = new CommentCreateDTO();
        commentCreateDTO.setText("This is a test comment.");
        BusinessException exception = assertThrows(BusinessException.class, () -> commentService.create(7, commentCreateDTO));
        assertEquals(401, exception.getHttpStatus());
    }

    /**
     * 先断言文章999确实不存在，再以已登录用户创建评论；断言404，避免把认证失败误当文章不存在。
     */
    @Test
    @Transactional
    public void testCreateCommentsWithWrongArticleId() {
        try {
            ArticleDetailVO articleDetailVO = articleMapper.findDetailById(999);
            assertNull(articleDetailVO);
            UserContext.setCurrentUserId(1);
            CommentCreateDTO commentCreateDTO = new CommentCreateDTO();
            commentCreateDTO.setText("This is a test comment.");
            BusinessException exception = assertThrows(BusinessException.class, () -> commentService.create(999, commentCreateDTO));
            assertEquals(404, exception.getHttpStatus());
        } finally {
            UserContext.removeCurrentUserId();
        }
    }

    /**
     * 为文章7插入专用评论，再查列表；按新主键定位并检查正文、作者与时间，最后要求找到该评论，防止循环未命中而假通过。
     */
    @Test
    @Transactional
    public void testCommentList(){
        Comment comment = new Comment();
        comment.setArticleId(7);
        comment.setUserId(4);
        comment.setText("This is a test comment.");
        commentMapper.createComment(comment);
        Integer commentId = comment.getId();
        assertNotNull(commentId);
        boolean found = false;
        List<CommentListItemVO> comments = commentMapper.findByArticleId(7);
        assertNotNull(comments);
        for (CommentListItemVO c : comments) {
            if(c.getCommentId().equals(commentId)){
                assertEquals(comment.getText(), c.getText());
                assertNotNull(c.getAuthor());
                assertEquals(comment.getUserId(), c.getAuthor().getUserId());
                assertEquals("Day2User01", c.getAuthor().getUserName());
                assertNotNull(c.getCreateTime());
                found = true;
            }
        }
        assertTrue(found, "列表应包含刚插入的评论");
    }

    /**
     * 新建无评论文章并移除登录身份，再由Service查询评论；断言非null空列表，验证匿名读取空列表路径。
     */
    @Test
    @Transactional
    void testCommentListWithNewArticle() {
        Article article = new Article();
        article.setTitle("Test Article");
        article.setBody("This is a test article.");
        article.setCategoryId(1);
        article.setUserId(4);
        articleMapper.insert(article);
        Integer articleId = article.getId();
        assertNotNull(articleId);
        UserContext.removeCurrentUserId();
        CommentListVO list = commentService.getList(articleId);
        assertNotNull(list);
        assertNotNull(list.getItems());
        assertTrue(list.getItems().isEmpty());
    }

    @Test
    @Transactional
    void testCommentListOrdersByCreateTimeThenId() {
        Article article = new Article();
        article.setUserId(1);
        article.setTitle("Test Article");
        article.setBody("This is a test article.");
        article.setCategoryId(1);
        articleMapper.insert(article);
        Integer articleId = article.getId();
        assertNotNull(articleId);
        Comment comment1 = new Comment();
        comment1.setArticleId(articleId);
        comment1.setUserId(1);
        comment1.setText("This is the first comment.");
        commentMapper.createComment(comment1);
        Integer comment1Id = comment1.getId();
        assertNotNull(comment1Id);
        Comment comment2 = new Comment();
        comment2.setArticleId(articleId);
        comment2.setUserId(1);
        comment2.setText("This is the second comment.");
        commentMapper.createComment(comment2);
        Integer comment2Id = comment2.getId();
        assertNotNull(comment2Id);
        Comment comment3 = new Comment();
        comment3.setArticleId(articleId);
        comment3.setUserId(1);
        comment3.setText("This is the third comment.");
        commentMapper.createComment(comment3);
        Integer comment3Id = comment3.getId();
        assertNotNull(comment3Id);
        LocalDateTime time1 = LocalDateTime.of(2026, 11, 7, 10, 0, 0);
        LocalDateTime time2 = LocalDateTime.of(2026, 11, 7, 10, 1, 0);
        jdbcTemplate.update("UPDATE blog_comment SET create_time = ? WHERE id = ?", time2, comment1Id);
        jdbcTemplate.update("UPDATE blog_comment SET create_time = ? WHERE id = ?", time1, comment2Id);
        jdbcTemplate.update("UPDATE blog_comment SET create_time = ? WHERE id = ?", time1, comment3Id);
        List<CommentListItemVO> comments = commentMapper.findByArticleId(articleId);
        assertNotNull(comments);
        assertEquals(3, comments.size());
        assertEquals(comment1Id, comments.get(0).getCommentId());
        assertEquals(comment3Id, comments.get(1).getCommentId());
        assertEquals(comment2Id, comments.get(2).getCommentId());
    }

}
