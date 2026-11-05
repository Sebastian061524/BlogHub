package com.sebastian.bloghub.mapper;

import com.sebastian.bloghub.entity.Article;

import com.sebastian.bloghub.vo.ArticleDetailVO;
import com.sebastian.bloghub.vo.ArticleListItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


@Mapper
public interface ArticleMapper {


    void insert(Article article);

    ArticleDetailVO findDetailById(Integer articleId);

    int update(Article article);

    int deleteByIdAndUserId(Integer articleId, Integer userId);

    Long countByCategory(Integer categoryId);

    List<ArticleListItemVO> findPage(@Param("categoryId") Integer categoryId, @Param("offset") long offset, @Param("pageSize") int pageSize);
}
