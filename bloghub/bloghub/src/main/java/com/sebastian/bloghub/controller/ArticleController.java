package com.sebastian.bloghub.controller;

import com.sebastian.bloghub.common.Result;
import com.sebastian.bloghub.dto.ArticleCreateDTO;
import com.sebastian.bloghub.dto.ArticleUpdateDTO;
import com.sebastian.bloghub.service.ArticleService;
import com.sebastian.bloghub.vo.ArticleCreateVO;
import com.sebastian.bloghub.vo.ArticleDetailVO;
import jakarta.validation.Valid;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/articles")
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    @PostMapping
    public ResponseEntity<Result<ArticleCreateVO>> create(@Valid @RequestBody ArticleCreateDTO articleCreateDTO){
        Integer articleId = articleService.create(articleCreateDTO);
        ArticleCreateVO vo = new ArticleCreateVO();
        vo.setArticleId(articleId);
        return ResponseEntity.status(201)
                .body(Result.success("文章发布成功", vo));
    }

    @GetMapping("/{articleId}")
    public ResponseEntity<Result<ArticleDetailVO>> getDetail(@PathVariable Integer articleId){
        ArticleDetailVO articleDetailVO = articleService.getDetail(articleId);
        return ResponseEntity.status(200)
                .body(Result.success("查询成功", articleDetailVO));
    }

    @PutMapping("/{articleId}")
    public ResponseEntity<Result<Void>> update(@PathVariable Integer articleId, @Valid @RequestBody ArticleUpdateDTO articleUpdateDTO){
        articleService.update(articleId, articleUpdateDTO);
        return ResponseEntity.status(200)
                .body(Result.success("修改成功", null));
    }

    @DeleteMapping("/{articleId}")
    public ResponseEntity<Result<Void>> delete(@PathVariable Integer articleId){
        articleService.delete(articleId);
        return ResponseEntity.status(200)
                .body(Result.success("删除成功", null));
    }
}
