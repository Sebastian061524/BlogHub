package com.sebastian.bloghub.service.impl;

import com.sebastian.bloghub.context.UserContext;
import com.sebastian.bloghub.dto.CommentCreateDTO;
import com.sebastian.bloghub.entity.Comment;
import com.sebastian.bloghub.exception.BusinessException;
import com.sebastian.bloghub.mapper.ArticleMapper;
import com.sebastian.bloghub.mapper.CategoryMapper;
import com.sebastian.bloghub.mapper.CommentMapper;
import com.sebastian.bloghub.service.CommentService;
import com.sebastian.bloghub.vo.ArticleDetailVO;
import com.sebastian.bloghub.vo.CommentListItemVO;
import com.sebastian.bloghub.vo.CommentListVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 评论业务实现：协调当前用户身份、文章存在性检查与评论数据访问。
 * 评论所属文章来自路径，评论作者来自鉴权上下文，客户端只提交评论文本。
 */
@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private ArticleMapper articleMapper;

    /**
     * 接收文章 ID 和评论 DTO，先确认已登录、标识合法及文章存在，再保存评论并返回生成的 ID。
     * DTO 字段约束由控制器的 @Valid 检查，业务层在这里补充跨数据表和身份相关的校验。
     */
    @Override
    public Integer create(Integer articleId, CommentCreateDTO dto) {
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
        Comment comment = new Comment();
        comment.setArticleId(articleId);
        // 使用已通过 JWT 校验的身份，防止调用方通过请求体冒充其他评论作者。
        comment.setUserId(userId);
        comment.setText(dto.getText());
        // MyBatis 插入配置会把数据库生成的主键回填到 comment.id，供创建响应使用。
        commentMapper.createComment(comment);
        return comment.getId();
    }

    /**
     * 接收路径文章 ID，校验后查询评论 VO 集合并装入列表 VO。
     * 先查文章是为了区分“文章不存在”（404）与“存在但没有评论”（成功且 items 为空）。
     */
    @Override
    public CommentListVO getList(Integer articleId) {
        if(articleId == null || articleId <= 0){
            throw new BusinessException(400, "文章标识必须为正整数");
        }
        ArticleDetailVO articleDetailVO = articleMapper.findDetailById(articleId);
        if(articleDetailVO == null){
            throw new BusinessException(404, "文章不存在");
        }
        // Mapper 同时读取评论作者信息，并按创建时间和评论 ID 降序排列。
        List<CommentListItemVO> comments = commentMapper.findByArticleId(articleId);
        CommentListVO commentListVO = new CommentListVO();
        commentListVO.setItems(comments);
        return commentListVO;
    }
}
