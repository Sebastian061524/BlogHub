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

@Service
public class ArticleServiceImpl implements ArticleService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private CommentMapper commentMapper;

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
        Article article = new Article();
        article.setUserId(userId);
        BeanUtils.copyProperties(articleCreateDTO, article);
        articleMapper.insert(article);
        return article.getId();
    }

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
        article.setId(articleId);
        article.setUpdateTime(LocalDateTime.now());
        int update = articleMapper.update(article);
        if(update != 1){
            throw new BusinessException(500, "更新失败");
        }
    }

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
        commentMapper.deleteByArticleId(articleId);
        int delete = articleMapper.deleteByIdAndUserId(articleId, userId);
        if(delete != 1){
            throw new BusinessException(500, "删除失败");
        }
    }

    @Override
    public ArticlePageVO getPage(Integer page, Integer categoryId) {
        if(page == null || page <= 0){
            throw new BusinessException(400, "页码必须为正整数");
        }
        if(categoryId != null && categoryId <= 0){
            throw new BusinessException(400, "分类标识必须为正整数");
        }
        if(categoryId != null){
            int count = categoryMapper.countById(categoryId);
            if(count == 0){
                throw new BusinessException(404, "分类不存在");
            }
        }
        long offset = (page - 1L) * 10;
        List<ArticleListItemVO> articles = articleMapper.findPage(categoryId, offset, 10);
        ArticlePageVO articlePageVO = new ArticlePageVO();
        articlePageVO.setPage(page);
        articlePageVO.setPageSize(10);
        articlePageVO.setTotal(articleMapper.countByCategory(categoryId));
        articlePageVO.setItems(articles);
        return articlePageVO;
    }
}
