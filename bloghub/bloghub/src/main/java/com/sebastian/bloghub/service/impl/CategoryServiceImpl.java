package com.sebastian.bloghub.service.impl;

import com.sebastian.bloghub.mapper.CategoryMapper;
import com.sebastian.bloghub.service.CategoryService;
import com.sebastian.bloghub.vo.CategoryListVO;
import com.sebastian.bloghub.vo.CategoryVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 分类业务实现：调用分类 Mapper 读取数据，再封装为分类列表 VO。
 * {@code @Service} 将实现注册为 Spring 组件，控制器可通过 CategoryService 接口注入。
 */
@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    /**
     * 无输入参数；Mapper 按分类 ID 升序查询，返回集合后放入 items，交给控制器序列化。
     */
    @Override
    public CategoryListVO getList() {
        List<CategoryVO> all = categoryMapper.findAll();
        CategoryListVO categoryListVO = new CategoryListVO();
        categoryListVO.setItems(all);
        return categoryListVO;
    }
}
