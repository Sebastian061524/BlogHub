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

@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private ArticleMapper articleMapper;

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
        comment.setUserId(userId);
        comment.setText(dto.getText());
        commentMapper.createComment(comment);
        return comment.getId();
    }

    @Override
    public CommentListVO getList(Integer articleId) {
        if(articleId == null || articleId <= 0){
            throw new BusinessException(400, "文章标识必须为正整数");
        }
        ArticleDetailVO articleDetailVO = articleMapper.findDetailById(articleId);
        if(articleDetailVO == null){
            throw new BusinessException(404, "文章不存在");
        }
        List<CommentListItemVO> comments = commentMapper.findByArticleId(articleId);
        CommentListVO commentListVO = new CommentListVO();
        commentListVO.setItems(comments);
        return commentListVO;
    }
}
