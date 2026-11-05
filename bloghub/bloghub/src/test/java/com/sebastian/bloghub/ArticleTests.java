package com.sebastian.bloghub;

import com.sebastian.bloghub.entity.Article;
import com.sebastian.bloghub.exception.BusinessException;
import com.sebastian.bloghub.mapper.ArticleMapper;
import com.sebastian.bloghub.mapper.CategoryMapper;
import com.sebastian.bloghub.service.ArticleService;
import com.sebastian.bloghub.vo.ArticleListItemVO;
import com.sebastian.bloghub.vo.ArticlePageVO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ArticleTests {

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ArticleService articleService;

    @Autowired
    private CategoryMapper categoryMapper;

    @Test
    @Transactional
    public void testCountByCategory(){
        Long before = articleMapper.countByCategory(null);
        Article article = new Article();
        article.setUserId(4);
        article.setTitle("title");
        article.setBody("body");
        article.setCategoryId(1);
        articleMapper.insert(article);
        Long after = articleMapper.countByCategory(null);
        assertEquals(before + 1, after);
    }

    @Test
    @Transactional
    public void testFindPage(){
        Article article = new Article();
        article.setUserId(4);
        article.setTitle("title");
        article.setBody("body");
        article.setCategoryId(1);
        articleMapper.insert(article);
        assertNotNull(article.getId());
        int articleId = article.getId();

        // 让测试文章比当前所有文章晚一秒，确保它进入第一页。
        Timestamp latestCreateTime = jdbcTemplate.queryForObject(
                "SELECT MAX(create_time) FROM blog_article", Timestamp.class);
        assertNotNull(latestCreateTime);
        Timestamp testCreateTime = Timestamp.valueOf(latestCreateTime.toLocalDateTime().plusSeconds(1));
        assertEquals(1, jdbcTemplate.update(
                "UPDATE blog_article SET create_time = ? WHERE id = ?", testCreateTime, articleId));

        List<ArticleListItemVO> page = articleMapper.findPage(null, 0L, 10);
        assertNotNull(page);
        assertTrue(page.size() >= 1 && page.size() <= 10);
        boolean found = false;
        for(ArticleListItemVO articleListItemVO : page){
            if(articleListItemVO.getArticleId() == articleId){
                found = true;
                assertEquals("title", articleListItemVO.getTitle());
                assertEquals("body", articleListItemVO.getSummary());
                assertNotNull(articleListItemVO.getAuthor());
                assertEquals(4, articleListItemVO.getAuthor().getUserId());
                assertNotNull(articleListItemVO.getCreateTime());
            }
        }
        assertTrue(found, "第一页应包含刚插入的测试文章");
    }

    @Test
    @Transactional
    void testCountByCategoryWithCategoryId() {
        Long count1 = articleMapper.countByCategory(1);
        Long count2 = articleMapper.countByCategory(2);

        Article article1 = new Article();
        article1.setUserId(4);
        article1.setTitle("title");
        article1.setBody("body");
        article1.setCategoryId(1);
        articleMapper.insert(article1);
        Integer id1 = article1.getId();
        assertNotNull(id1);

        Article article2 = new Article();
        article2.setUserId(4);
        article2.setTitle("other title");
        article2.setBody("other body");
        article2.setCategoryId(2);
        articleMapper.insert(article2);
        Integer id2 = article2.getId();
        assertNotNull(id2);
        assertNotEquals(id1, id2);

        // 两篇文章都排在已有文章前面，避免分页掩盖分类筛选错误。
        Timestamp latestCreateTime = jdbcTemplate.queryForObject(
                "SELECT MAX(create_time) FROM blog_article", Timestamp.class);
        assertNotNull(latestCreateTime);
        Timestamp testCreateTime = Timestamp.valueOf(latestCreateTime.toLocalDateTime().plusSeconds(1));
        assertEquals(2, jdbcTemplate.update(
                "UPDATE blog_article SET create_time = ? WHERE id IN (?, ?)", testCreateTime, id1, id2));

        assertEquals(count1 + 1L, articleMapper.countByCategory(1));
        assertEquals(count2 + 1L, articleMapper.countByCategory(2));
        List<ArticleListItemVO> page = articleMapper.findPage(1, 0L, 10);
        assertNotNull(page);
        assertTrue(page.size() >= 1 && page.size() <= 10);
        boolean found = false;
        for (ArticleListItemVO articleListItemVO : page) {
            assertNotEquals(id2, articleListItemVO.getArticleId(),
                    "分类1的列表不应包含分类2的文章");
            if (articleListItemVO.getArticleId().equals(id1)) {
                found = true;
                assertEquals("title", articleListItemVO.getTitle());
                assertEquals("body", articleListItemVO.getSummary());
                assertNotNull(articleListItemVO.getAuthor());
                assertEquals(4, articleListItemVO.getAuthor().getUserId());
                assertNotNull(articleListItemVO.getCreateTime());
            }
        }
        assertTrue(found, "分类1的第一页应包含刚插入的分类1文章");
    }

    @Test
    void testFindPageBeyondLastPage(){
        Long total = articleMapper.countByCategory(null);
        List<ArticleListItemVO> page = articleMapper.findPage(null, total, 10);
        assertNotNull(page);
        assertTrue(page.isEmpty());
    }

    @Test
    void testGetPage(){
        ArticlePageVO page = articleService.getPage(1, null);
        assertNotNull(page);
        assertEquals(10, page.getPageSize());
        assertEquals(1, page.getPage());
        Long res = jdbcTemplate.queryForObject("select count(*) from blog_article", Long.class);
        assertEquals(res, page.getTotal());
        assertNotNull(page.getItems());
        assertTrue(page.getItems().size() <= 10);
    }

    @Test
    void testGetPageBeyondLastPage(){
        Long total = jdbcTemplate.queryForObject("select count(*) from blog_article", Long.class);
        Integer pageNumber = (int)(total / 10 + 2);
        ArticlePageVO page = articleService.getPage(pageNumber, null);
        assertNotNull(page);
        assertEquals(total, page.getTotal());
        assertNotNull(page.getItems());
        assertTrue(page.getItems().isEmpty());
        assertEquals(pageNumber, page.getPage());
        assertEquals(10, page.getPageSize());
    }

    @Test
    void testGetPageWithWrong(){
        BusinessException ex = assertThrows(BusinessException.class, () -> articleService.getPage(null, null));
        assertEquals(400, ex.getHttpStatus());
        BusinessException ex2 = assertThrows(BusinessException.class, () -> articleService.getPage(1, -1));
        assertEquals(400, ex2.getHttpStatus());
        assertEquals(0, categoryMapper.countById(4));
        BusinessException ex3 = assertThrows(BusinessException.class, () -> articleService.getPage(1, 4));
        assertEquals(404, ex3.getHttpStatus());
        assertThrows(BusinessException.class, () -> articleService.getPage(-1, null));
        assertThrows(BusinessException.class, () -> articleService.getPage(1, 0));
        assertThrows(BusinessException.class, () -> articleService.getPage(0, null));
    }


}
