package com.sebastian.bloghub;

import com.sebastian.bloghub.context.UserContext;
import com.sebastian.bloghub.dto.ArticleCreateDTO;
import com.sebastian.bloghub.exception.BusinessException;
import com.sebastian.bloghub.mapper.ArticleMapper;
import com.sebastian.bloghub.service.ArticleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

@SpringBootTest
class ArticleDeleteTransactionTests {

    @Autowired
    private ArticleService articleService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoSpyBean
    private ArticleMapper articleMapper;

    @Test
    void restoresCommentsWhenArticleDeletionThrows() {
        verifyRollbackAfterArticleDeletionFailure(false);
    }

    @Test
    void restoresCommentsWhenArticleDeletionAffectsNoRows() {
        verifyRollbackAfterArticleDeletionFailure(true);
    }

    private void verifyRollbackAfterArticleDeletionFailure(boolean returnZero) {
        // 测试本身没有事务，准备的数据在调用删除 Service 前就已提交。
        assertFalse(TransactionSynchronizationManager.isActualTransactionActive());
        Integer articleId = null;
        UserContext.removeCurrentUserId();
        try {
            Integer userId = jdbcTemplate.queryForObject(
                    "SELECT id FROM blog_user WHERE user_name = ?", Integer.class, "Day2User01");
            assertNotNull(userId);
            UserContext.setCurrentUserId(userId);

            ArticleCreateDTO dto = new ArticleCreateDTO();
            dto.setTitle("Transaction rollback test");
            dto.setBody("Only this temporary article is used by the rollback test.");
            dto.setCategoryId(1);
            articleId = articleService.create(dto);
            assertNotNull(articleId);
            Integer testArticleId = articleId;
            assertEquals(1, jdbcTemplate.update(
                    "INSERT INTO blog_comment (article_id, user_id, text) VALUES (?, ?, ?)",
                    testArticleId, userId, "This comment must survive a failed deletion."));
            assertEquals(1, countComments(testArticleId));

            IllegalStateException simulatedFailure = new IllegalStateException("模拟删除文章失败");
            doAnswer(invocation -> {
                // 此时仍处于 Service 的调用中，确认评论确实已经执行了删除。
                assertEquals(0, countComments(testArticleId));
                if (returnZero) {
                    return 0;
                }
                throw simulatedFailure;
            }).when(articleMapper).deleteByIdAndUserId(testArticleId, userId);

            if (returnZero) {
                BusinessException ex = assertThrows(BusinessException.class,
                        () -> articleService.delete(testArticleId));
                assertEquals(500, ex.getHttpStatus());
                assertEquals("删除失败", ex.getMessage());
            } else {
                IllegalStateException ex = assertThrows(IllegalStateException.class,
                        () -> articleService.delete(testArticleId));
                assertSame(simulatedFailure, ex);
            }

            verify(articleMapper).deleteByIdAndUserId(testArticleId, userId);
            // Service 已退出，使用真实数据库查询验证它自己的事务完成了回滚。
            assertFalse(TransactionSynchronizationManager.isActualTransactionActive());
            assertEquals(1, jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM blog_article WHERE id = ?", Integer.class, testArticleId));
            assertEquals(1, countComments(testArticleId));
        } finally {
            try {
                if (articleId != null) {
                    jdbcTemplate.update("DELETE FROM blog_comment WHERE article_id = ?", articleId);
                    jdbcTemplate.update("DELETE FROM blog_article WHERE id = ?", articleId);
                }
            } finally {
                UserContext.removeCurrentUserId();
            }
        }
    }

    private int countComments(Integer articleId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM blog_comment WHERE article_id = ?", Integer.class, articleId);
        assertNotNull(count);
        return count;
    }
}
