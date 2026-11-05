package com.sebastian.bloghub;

import com.sebastian.bloghub.mapper.CategoryMapper;
import com.sebastian.bloghub.vo.CategoryVO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class CategoryTests {

    @Autowired
    private CategoryMapper categoryMapper;

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
