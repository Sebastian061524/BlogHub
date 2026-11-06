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

/**
 * 文章及评论 HTTP 入口：接收路径、查询参数和 JSON 请求体，调用业务层，再包装响应。
 * {@code @RestController} 使返回对象作为响应体序列化；@RequestMapping 统一设置 /articles 路径。
 * 作者权限、资源存在性和数据库操作交由 Service，写请求的身份由 JWT 拦截器提供。
 */
@RestController
@RequestMapping("/articles")
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    @Autowired
    private CommentService commentService;

    /**
     * 将 JSON 转为发布 DTO；@Valid 在进入方法前检查 DTO 的 Bean Validation 约束。
     * 业务层创建文章并返回数据库生成的 ID，这里只组装创建结果并用 HTTP 201 表示资源已创建。
     */
    @PostMapping
    public ResponseEntity<Result<ArticleCreateVO>> create(@Valid @RequestBody ArticleCreateDTO articleCreateDTO){
        Integer articleId = articleService.create(articleCreateDTO);
        ArticleCreateVO vo = new ArticleCreateVO();
        vo.setArticleId(articleId);
        return ResponseEntity.status(201)
                .body(Result.success("文章发布成功", vo));
    }

    /**
     * 从 URL 取文章 ID，调用业务层获取包含作者和分类的详情 VO；查询成功返回 HTTP 200。
     */
    @GetMapping("/{articleId}")
    public ResponseEntity<Result<ArticleDetailVO>> getDetail(@PathVariable Integer articleId){
        ArticleDetailVO articleDetailVO = articleService.getDetail(articleId);
        return ResponseEntity.status(200)
                .body(Result.success("查询成功", articleDetailVO));
    }

    /**
     * 路径提供待修改文章 ID，JSON 提供新内容；@Valid 校验字段，Service 校验作者并执行更新。
     * 成功结果的 data 为 null，HTTP 状态仍为 200。
     */
    @PutMapping("/{articleId}")
    public ResponseEntity<Result<Void>> update(@PathVariable Integer articleId, @Valid @RequestBody ArticleUpdateDTO articleUpdateDTO){
        articleService.update(articleId, articleUpdateDTO);
        return ResponseEntity.status(200)
                .body(Result.success("修改成功", null));
    }

    /**
     * 把路径中的文章 ID 交给业务层，业务层负责鉴权及事务内删除评论和文章；成功返回 200。
     */
    @DeleteMapping("/{articleId}")
    public ResponseEntity<Result<Void>> delete(@PathVariable Integer articleId){
        articleService.delete(articleId);
        return ResponseEntity.status(200)
                .body(Result.success("删除成功", null));
    }

    /**
     * 接收可选页码与分类过滤条件并返回分页 VO；只有完全未提供 page 时才使用第 1 页。
     * 保留原始 request 是为了区分“没有参数”和“提供了空值后被绑定为 null”。
     */
    @GetMapping
    public ResponseEntity<Result<ArticlePageVO>> getPage(@RequestParam(required = false) Integer page, @RequestParam(required = false) Integer categoryId, HttpServletRequest request){
        // 缺省参数可使用默认值；显式提交 page= 不在这里补默认值，随后由 Service 判为 400。
        if(page == null && request.getParameter("page") == null){
            page = 1;
        }
        // categoryId= 是无效筛选值，不能把它当成“查询所有分类”；非整数由 MVC 类型转换报错。
        if(categoryId == null && request.getParameter("categoryId") != null){
            throw new BusinessException(400, "分类ID必须为正整数");
        }
        ArticlePageVO articlePageVO = articleService.getPage(page, categoryId);
        return ResponseEntity.status(200)
                .body(Result.success("查询成功", articlePageVO));
    }

    /**
     * 路径指定评论所属文章，经过 @Valid 的 JSON 指定评论文本；评论者身份从请求上下文获取。
     * Service 返回新评论 ID 后，组装创建 VO 并返回 HTTP 201。
     */
    @PostMapping("/{articleId}/comments")
    public ResponseEntity<Result<CommentCreateVO>> createComment(@PathVariable Integer articleId, @Valid @RequestBody CommentCreateDTO commentCreateDTO){
        Integer commentId = commentService.create(articleId, commentCreateDTO);
        CommentCreateVO commentCreateVO = new CommentCreateVO();
        commentCreateVO.setCommentId(commentId);
        return ResponseEntity.status(201)
                .body(Result.success("评论成功", commentCreateVO));
    }

    /**
     * 按路径文章 ID 查询评论列表；Service 区分文章不存在与文章存在但评论为空的情况。
     */
    @GetMapping("/{articleId}/comments")
    public ResponseEntity<Result<CommentListVO>> getComments(@PathVariable Integer articleId){
        CommentListVO commentListVO = commentService.getList(articleId);
        return ResponseEntity.status(200)
                .body(Result.success("查询成功", commentListVO));
    }


}
