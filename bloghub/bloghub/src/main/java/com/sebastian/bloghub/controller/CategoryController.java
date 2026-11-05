package com.sebastian.bloghub.controller;

import com.sebastian.bloghub.common.Result;
import com.sebastian.bloghub.service.CategoryService;
import com.sebastian.bloghub.vo.CategoryListVO;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public ResponseEntity<Result<CategoryListVO>> getList() {
        CategoryListVO categoryListVO = categoryService.getList();
        return ResponseEntity.ok(Result.success("获取分类列表成功", categoryListVO));
    }
}
