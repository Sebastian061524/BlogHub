package com.sebastian.bloghub.controller;

import com.sebastian.bloghub.common.Result;
import com.sebastian.bloghub.dto.ArticleCreateDTO;
import com.sebastian.bloghub.dto.ArticleUpdateDTO;
import com.sebastian.bloghub.dto.CommentCreateDTO;
import com.sebastian.bloghub.exception.BusinessException;
import com.sebastian.bloghub.service.ArticleService;
import com.sebastian.bloghub.service.CommentService;
import com.sebastian.bloghub.vo.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/articles")
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    @Autowired
    private CommentService commentService;

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

    @GetMapping
    public ResponseEntity<Result<ArticlePageVO>> getPage(@RequestParam(required = false) Integer page, @RequestParam(required = false) Integer categoryId, HttpServletRequest request){
        if(page == null && request.getParameter("page") == null){
            page = 1;
        }
        if(categoryId == null && request.getParameter("categoryId") != null){
            throw new BusinessException(400, "分类ID必须为正整数");
        }
        ArticlePageVO articlePageVO = articleService.getPage(page, categoryId);
        return ResponseEntity.status(200)
                .body(Result.success("查询成功", articlePageVO));
    }

    @PostMapping("/{articleId}/comments")
    public ResponseEntity<Result<CommentCreateVO>> createComment(@PathVariable Integer articleId, @Valid @RequestBody CommentCreateDTO commentCreateDTO){
        Integer commentId = commentService.create(articleId, commentCreateDTO);
        CommentCreateVO commentCreateVO = new CommentCreateVO();
        commentCreateVO.setCommentId(commentId);
        return ResponseEntity.status(201)
                .body(Result.success("评论成功", commentCreateVO));
    }

    @GetMapping("/{articleId}/comments")
    public ResponseEntity<Result<CommentListVO>> getComments(@PathVariable Integer articleId){
        CommentListVO commentListVO = commentService.getList(articleId);
        return ResponseEntity.status(200)
                .body(Result.success("查询成功", commentListVO));
    }


}
