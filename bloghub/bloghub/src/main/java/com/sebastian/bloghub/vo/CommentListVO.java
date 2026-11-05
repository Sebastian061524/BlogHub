package com.sebastian.bloghub.vo;

import lombok.Data;

import java.util.List;

@Data
public class CommentListVO {
    private List<CommentListItemVO> items;
}
