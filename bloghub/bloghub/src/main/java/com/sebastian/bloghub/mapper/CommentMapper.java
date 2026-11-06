package com.sebastian.bloghub.mapper;

import com.sebastian.bloghub.entity.Comment;
import com.sebastian.bloghub.vo.CommentListItemVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 评论持久层接口，SQL和结果映射位于CommentMapper.xml。
 * MyBatis的{@code @Mapper}使框架扫描接口并创建代理，调用方法时执行对应SQL。
 */
@Mapper
public interface CommentMapper {

    /** 删除指定文章的全部评论，返回影响行数，供文章删除前清理外键依赖。 */
    int deleteByArticleId(Integer articleId);

    /** 插入评论；XML配置自增主键回填comment.id，创建时间使用数据库默认值。 */
    void createComment(Comment comment);

    /** 查询指定文章的评论和作者信息，返回按时间及编号升序排列的列表。 */
    List<CommentListItemVO> findByArticleId(Integer articleId);
}
