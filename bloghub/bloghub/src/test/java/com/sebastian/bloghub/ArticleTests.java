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

/**
 * 文章统计、分页查询与分页参数校验的集成测试，验证Mapper SQL和Service返回结构。
 * 依赖用户4、分类1和2，并约定分类4不存在；写入测试由@Transactional在结束后回滚。
 * 采用插入前后数量差和专用文章ID验证查询，避免把已有数据误当成本次数据。
 * 这里只覆盖各方法中实际断言的场景，不代表分页及所有异常分支均已穷尽。
 */
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

    /**
     * 准备用户4、分类1的一篇文章；以未筛分类的插入前后数量差为1作断言，验证总数SQL包含新记录。
     */
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

    /**
     * 插入专用文章并把创建时间调至当前最大值后一秒，再查第一页；断言分页大小及该文章的摘要、作者和时间。
     * 主动控制排序位置可避免被已有数据挤出第一页，使必须找到测试文章的断言可靠。
     */
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

    /**
     * 在分类1和2各插入一篇文章，并将两者排到已有文章前面，再按分类1查页。
     * 断言分类计数各增1、分类1文章可见而分类2文章不可见，避免分页恰好隐藏错误分类导致假通过。
     */
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

    /**
     * 读取总数并用作offset跳过全部记录；断言非null空列表，验证Mapper越界分页的返回约定。
     */
    @Test
    void testFindPageBeyondLastPage(){
        Long total = articleMapper.countByCategory(null);
        List<ArticleListItemVO> page = articleMapper.findPage(null, total, 10);
        assertNotNull(page);
        assertTrue(page.isEmpty());
    }

    /**
     * 调用Service第一页并独立查库得到总数；断言页码1、每页10条、总数一致及列表最多10项。
     */
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

    /**
     * 根据真实总数构造必定超出末页的页码；断言保留总数和请求页码，但返回空列表。
     */
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

    /**
     * 用空或非正页码、非法分类值及不存在的分类4调用Service。
     * 部分场景断言400或404，其余仅断言业务异常；未断言的错误码不在本测试保证范围内。
     */
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
