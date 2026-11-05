package com.sebastian.bloghub.mapper;

import com.sebastian.bloghub.vo.CategoryVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CategoryMapper {


    int countById(Integer category_id);

    List<CategoryVO> findAll();
}
