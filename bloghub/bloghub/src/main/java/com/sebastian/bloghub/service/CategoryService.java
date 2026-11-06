package com.sebastian.bloghub.service;

import com.sebastian.bloghub.vo.CategoryListVO;

/**
 * 分类查询业务契约，供控制器使用，隔离 HTTP 入口与底层数据库查询实现。
 */
public interface CategoryService {

    /**
     * 无输入参数，返回封装分类集合的 VO；具体读取与排序由实现类和 Mapper 协作完成。
     */
    CategoryListVO getList();
}
