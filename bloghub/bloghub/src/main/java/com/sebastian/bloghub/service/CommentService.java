package com.sebastian.bloghub.service;

import com.sebastian.bloghub.dto.CommentCreateDTO;
import com.sebastian.bloghub.vo.CommentListVO;

public interface CommentService {

    Integer create(Integer articleId, CommentCreateDTO dto);

    CommentListVO getList(Integer articleId);
}
