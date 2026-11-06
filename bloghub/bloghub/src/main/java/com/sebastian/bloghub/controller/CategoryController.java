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

/**
 * 公开分类查询入口：把 /categories 的 GET 请求交给分类业务层，并使用统一 Result 返回。
 * 分类数据封装在列表 VO 中，控制器不直接访问 Mapper。
 */
@RestController
@RequestMapping("/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 无请求参数；从业务层取得分类列表，返回 HTTP 200 与包装后的分类数据。
     */
    @GetMapping
    public ResponseEntity<Result<CategoryListVO>> getList() {
        CategoryListVO categoryListVO = categoryService.getList();
        return ResponseEntity.ok(Result.success("获取分类列表成功", categoryListVO));
    }
}
