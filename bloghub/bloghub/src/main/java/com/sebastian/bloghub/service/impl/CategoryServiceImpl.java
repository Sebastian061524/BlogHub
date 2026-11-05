package com.sebastian.bloghub.service.impl;

import com.sebastian.bloghub.mapper.CategoryMapper;
import com.sebastian.bloghub.service.CategoryService;
import com.sebastian.bloghub.vo.CategoryListVO;
import com.sebastian.bloghub.vo.CategoryVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public CategoryListVO getList() {
        List<CategoryVO> all = categoryMapper.findAll();
        CategoryListVO categoryListVO = new CategoryListVO();
        categoryListVO.setItems(all);
        return categoryListVO;
    }
}
