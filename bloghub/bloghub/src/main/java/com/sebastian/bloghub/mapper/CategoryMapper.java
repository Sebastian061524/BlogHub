package com.sebastian.bloghub.mapper;

import com.sebastian.bloghub.vo.CategoryVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 分类持久层接口，由CategoryMapper.xml执行存在性检查和列表查询。
 * MyBatis的{@code @Mapper}使框架扫描接口并创建代理，调用方法时执行对应SQL。
 */
@Mapper
public interface CategoryMapper {


    /** 统计指定主键的分类数量，通常为0或1，供Service判断分类是否存在。 */
    int countById(Integer category_id);

    /** 查询全部分类并返回CategoryVO列表，SQL按分类编号升序排列。 */
    List<CategoryVO> findAll();
}
