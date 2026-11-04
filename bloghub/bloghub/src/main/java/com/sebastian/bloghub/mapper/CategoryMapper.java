package com.sebastian.bloghub.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CategoryMapper {


    int countById(Integer category_id);
}
