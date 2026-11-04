package com.sebastian.bloghub.mapper;

import com.sebastian.bloghub.entity.Article;

import com.sebastian.bloghub.vo.ArticleDetailVO;
import org.apache.ibatis.annotations.Mapper;



@Mapper
public interface ArticleMapper {


    void insert(Article article);

    ArticleDetailVO findDetailById(Integer articleId);

    int update(Article article);

    int deleteByIdAndUserId(Integer articleId, Integer userId);
}
