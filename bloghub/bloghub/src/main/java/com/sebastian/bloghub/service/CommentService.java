package com.sebastian.bloghub.service;

import com.sebastian.bloghub.dto.CommentCreateDTO;
import com.sebastian.bloghub.vo.CommentListVO;

/**
 * 评论业务契约，向文章控制器提供发表评论和公开查询评论的能力。
 * 实现类负责文章存在性、当前用户身份以及评论 Mapper 的调用。
 */
public interface CommentService {

    /**
     * 接收路径文章 ID 与已校验的评论文本 DTO；以当前用户为作者，返回数据库生成的评论 ID。
     */
    Integer create(Integer articleId, CommentCreateDTO dto);

    /**
     * 按路径文章 ID 查询评论列表 VO；文章存在但无评论时集合为空，文章不存在时报 404。
     */
    CommentListVO getList(Integer articleId);
}
