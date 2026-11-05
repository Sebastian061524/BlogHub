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


import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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

    @Test
    @Transactional
    public void testCreateCommentsWithoutUserId() {
        UserContext.removeCurrentUserId();
        CommentCreateDTO commentCreateDTO = new CommentCreateDTO();
        commentCreateDTO.setText("This is a test comment.");
        BusinessException exception = assertThrows(BusinessException.class, () -> commentService.create(7, commentCreateDTO));
        assertEquals(401, exception.getHttpStatus());
    }

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

}
