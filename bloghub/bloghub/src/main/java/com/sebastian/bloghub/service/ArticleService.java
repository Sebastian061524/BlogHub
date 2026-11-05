package com.sebastian.bloghub.service;

import com.sebastian.bloghub.dto.ArticleCreateDTO;
import com.sebastian.bloghub.dto.ArticleUpdateDTO;
import com.sebastian.bloghub.vo.ArticleDetailVO;
import com.sebastian.bloghub.vo.ArticlePageVO;

public interface ArticleService {

    Integer create(ArticleCreateDTO articleCreateDTO);

    ArticleDetailVO getDetail(Integer articleId);

    void update(Integer articleId, ArticleUpdateDTO articleUpdateDTO);

    void delete(Integer articleId);

    ArticlePageVO getPage(Integer page, Integer categoryId);
}
