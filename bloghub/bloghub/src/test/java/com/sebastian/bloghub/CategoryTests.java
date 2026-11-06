package com.sebastian.bloghub;

import com.sebastian.bloghub.mapper.CategoryMapper;
import com.sebastian.bloghub.vo.CategoryVO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 分类Mapper的数据库集成测试，验证findAll的数量、排序与字段映射。
 * 前提是测试库恰有三个预置分类，固定顺序断言用于发现SQL排序或映射变化。
 */
@SpringBootTest
public class CategoryTests {

    @Autowired
    private CategoryMapper categoryMapper;

    /**
     * 读取全部分类，断言非null、恰好3项，按位置检查ID与名称，验证固定种子数据下的排序和字段映射。
     */
    @Test
    void testFindAll(){
        List<CategoryVO> all = categoryMapper.findAll();
        assertNotNull(all);
        assertEquals(3, all.size());
        assertEquals(1, all.get(0).getCategoryId());
        assertEquals("Java", all.get(0).getCategoryName());
        assertEquals(2, all.get(1).getCategoryId());
        assertEquals("数据库", all.get(1).getCategoryName());
        assertEquals(3, all.get(2).getCategoryId());
        assertEquals("学习记录", all.get(2).getCategoryName());
    }
}
