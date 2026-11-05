package com.sebastian.bloghub.mapper;

import com.sebastian.bloghub.entity.Comment;
import com.sebastian.bloghub.vo.CommentListItemVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CommentMapper {

    int deleteByArticleId(Integer articleId);

    void createComment(Comment comment);

    List<CommentListItemVO> findByArticleId(Integer articleId);
}
