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

/**
 * 文章删除的真实事务回滚集成测试，使用Spring容器、真实数据库和ArticleMapper局部spy。
 * 刻意不加测试外层事务：准备数据先提交，再观察Service自己的事务能否恢复已删除评论。
 * 只替换文章删除调用，评论删除及查询仍执行真实SQL，不能把测试事务的回滚当作业务事务已回滚。
 * 前提是测试库已有指定用户和分类；finally只清理本次临时数据与线程上下文。
 */
@SpringBootTest
class ArticleDeleteTransactionTests {

    @Autowired
    private ArticleService articleService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoSpyBean
    private ArticleMapper articleMapper;

    /**
     * 准备已提交文章及评论，再让文章删除抛异常；断言原异常向外传播且两条记录仍存在，验证异常触发回滚。
     */
    @Test
    void restoresCommentsWhenArticleDeletionThrows() {
        verifyRollbackAfterArticleDeletionFailure(false);
    }

    /**
     * 准备已提交数据，将文章删除结果模拟为0行；断言Service抛500业务异常并恢复评论，验证主动失败也触发回滚。
     */
    @Test
    void restoresCommentsWhenArticleDeletionAffectsNoRows() {
        verifyRollbackAfterArticleDeletionFailure(true);
    }

    /**
     * 先断言测试不处于事务，再创建文章及评论；spy在评论已真实删除后注入文章删除失败。
     * Service退出后重新查库，断言文章保留且评论恢复；finally再清理临时数据，不参与回滚成功断言。
     */
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

    /**
     * 按文章ID执行真实COUNT查询并确认结果非null，既用于事务内观察删除，也用于事务外观察恢复。
     */
    private int countComments(Integer articleId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM blog_comment WHERE article_id = ?", Integer.class, articleId);
        assertNotNull(count);
        return count;
    }
}
