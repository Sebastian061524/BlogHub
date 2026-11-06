package com.sebastian.bloghub.service.impl;

import com.sebastian.bloghub.context.UserContext;
import com.sebastian.bloghub.dto.ArticleCreateDTO;
import com.sebastian.bloghub.dto.ArticleUpdateDTO;
import com.sebastian.bloghub.entity.Article;
import com.sebastian.bloghub.exception.BusinessException;
import com.sebastian.bloghub.mapper.ArticleMapper;
import com.sebastian.bloghub.mapper.CategoryMapper;
import com.sebastian.bloghub.mapper.CommentMapper;
import com.sebastian.bloghub.service.ArticleService;
import com.sebastian.bloghub.vo.ArticleDetailVO;
import com.sebastian.bloghub.vo.ArticleListItemVO;
import com.sebastian.bloghub.vo.ArticlePageVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文章业务实现：协调请求身份、分类与文章查询、作者权限、分页和删除事务。
 * {@code @Service} 让 Spring 创建业务组件；通过 Mapper 完成持久化，不在控制器内直接编写数据库流程。
 */
@Service
public class ArticleServiceImpl implements ArticleService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private CommentMapper commentMapper;

    /**
     * 接收控制器已通过 @Valid 校验的发布 DTO，确认当前用户与分类后创建文章，返回生成的文章 ID。
     * 字段格式约束由 DTO 校验负责，登录态和分类是否真实存在属于业务规则，在这里检查。
     */
    @Override
    public Integer create(ArticleCreateDTO articleCreateDTO) {
        Integer userId = UserContext.getCurrentUserId();
        if(userId == null || userId <= 0){
            throw new BusinessException(401, "请先登录");
        }
        int count = categoryMapper.countById(articleCreateDTO.getCategoryId());
        if(count == 0){
            throw new BusinessException(404, "分类不存在");
        }
        // 作者来自可信上下文；DTO 只包含标题、正文和分类，copyProperties 不会覆盖 userId。
        Article article = new Article();
        article.setUserId(userId);
        BeanUtils.copyProperties(articleCreateDTO, article);
        // MyBatis 的 useGeneratedKeys 会将数据库生成的主键回填到实体，随后可直接返回 article.id。
        articleMapper.insert(article);
        return article.getId();
    }

    /**
     * 接收路径中的文章 ID，拒绝 null 或非正数（400），查询不到文章则报告 404。
     * Mapper 已将文章、作者和分类组装成详情 VO，这里无需向客户端暴露数据库实体。
     */
    @Override
    public ArticleDetailVO getDetail(Integer articleId) {
        if(articleId == null || articleId <= 0){
            throw new BusinessException(400, "文章标识必须为正整数");
        }
        ArticleDetailVO articleDetailVO = articleMapper.findDetailById(articleId);
        if(articleDetailVO == null){
            throw new BusinessException(404, "文章不存在");
        }
        return articleDetailVO;
    }

    /**
     * 接收路径文章 ID 和已通过字段校验的更新 DTO，按标识、身份、文章、作者、分类的顺序检查。
     * 成功后更新文章内容及更新时间，无返回数据；受影响行数不为 1 时报告服务端处理失败。
     */
    @Override
    public void update(Integer articleId, ArticleUpdateDTO articleUpdateDTO) {
        if(articleId == null || articleId <= 0){
            throw new BusinessException(400, "文章标识必须为正整数");
        }
        Integer userId = UserContext.getCurrentUserId();
        if(userId == null || userId <= 0){
            throw new BusinessException(401, "请先登录");
        }
        ArticleDetailVO articleDetailVO = articleMapper.findDetailById(articleId);
        if(articleDetailVO == null){
            throw new BusinessException(404, "文章不存在");
        }
        // 401 说明没有有效身份；到这里已登录但不是作者，所以用 403 拒绝修改。
        if(!articleDetailVO.getAuthor().getUserId().equals(userId)){
            throw new BusinessException(403, "无权限");
        }
        int count = categoryMapper.countById(articleUpdateDTO.getCategoryId());
        if(count == 0){
            throw new BusinessException(404, "分类不存在");
        }
        Article article = new Article();
        BeanUtils.copyProperties(articleUpdateDTO, article);
        article.setUserId(userId);
        // 文章 ID 取自路径，作者 ID 取自上下文；更新 SQL 同时约束二者，再次限定更新范围。
        article.setId(articleId);
        article.setUpdateTime(LocalDateTime.now());
        // 只有恰好更新一行才符合本次操作预期，防止并发删除等情况被当成更新成功。
        int update = articleMapper.update(article);
        if(update != 1){
            throw new BusinessException(500, "更新失败");
        }
    }

    /**
     * 接收文章 ID，确认当前用户是作者后，先删除评论，再删除文章，保证关联数据整体删除。
     * {@code @Transactional} 经 Spring 代理调用时开启事务；默认对 RuntimeException 和 Error 回滚，
     * 下方删除行数异常抛出的 BusinessException 也是运行时异常，因此先删掉的评论会一同回滚。
     */
    @Override
    @Transactional
    public void delete(Integer articleId) {
        Integer userId = UserContext.getCurrentUserId();
        if(userId == null || userId <= 0){
            throw new BusinessException(401, "请先登录");
        }
        if(articleId == null || articleId <= 0){
            throw new BusinessException(400, "文章标识必须为正整数");
        }
        ArticleDetailVO articleDetailVO = articleMapper.findDetailById(articleId);
        if(articleDetailVO == null){
            throw new BusinessException(404, "文章不存在");
        }
        if(!articleDetailVO.getAuthor().getUserId().equals(userId)){
            throw new BusinessException(403, "无权限");
        }
        // 先删从属评论再删文章，避免遗留无所属文章的评论，也符合常见的外键依赖顺序。
        commentMapper.deleteByArticleId(articleId);
        // 删除语句同时限制文章与作者；如果并发变化导致未删到一行，用异常触发整个事务回滚。
        int delete = articleMapper.deleteByIdAndUserId(articleId, userId);
        if(delete != 1){
            throw new BusinessException(500, "删除失败");
        }
    }

    /**
     * 接收页码和可选分类，检查标识及分类存在性后，以固定每页 10 条查询文章并组装分页 VO。
     * 页码超出末页时可以返回空 items；total 仍表示符合筛选条件的全部记录数。
     */
    @Override
    public ArticlePageVO getPage(Integer page, Integer categoryId) {
        if(page == null || page <= 0){
            throw new BusinessException(400, "页码必须为正整数");
        }
        if(categoryId != null && categoryId <= 0){
            throw new BusinessException(400, "分类标识必须为正整数");
        }
        // null 表示不按分类筛选；传入合法的正数后仍需检查分类存在，缺失资源用 404 表达。
        if(categoryId != null){
            int count = categoryMapper.countById(categoryId);
            if(count == 0){
                throw new BusinessException(404, "分类不存在");
            }
        }
        // 页码从 1 起，SQL offset 从 0 起；1L 让减法和乘法按 long 运算，避免 int 乘法溢出。
        long offset = (page - 1L) * 10;
        List<ArticleListItemVO> articles = articleMapper.findPage(categoryId, offset, 10);
        ArticlePageVO articlePageVO = new ArticlePageVO();
        articlePageVO.setPage(page);
        articlePageVO.setPageSize(10);
        // total 不是当前页 items.size()；总数查询与列表查询使用相同分类条件，便于前端计算页数。
        articlePageVO.setTotal(articleMapper.countByCategory(categoryId));
        articlePageVO.setItems(articles);
        return articlePageVO;
    }
}
