package com.sebastian.bloghub.mapper;

import com.sebastian.bloghub.entity.Article;

import com.sebastian.bloghub.vo.ArticleDetailVO;
import com.sebastian.bloghub.vo.ArticleListItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * 文章持久层接口，方法名与ArticleMapper.xml中的语句id对应。
 * MyBatis的{@code @Mapper}使框架扫描接口并创建代理，调用方法时执行对应SQL。
 */
@Mapper
public interface ArticleMapper {


    /** 写入文章；XML生成主键配置将自增主键回填article.id，方法本身不返回主键。 */
    void insert(Article article);

    /** 按编号查询文章及作者、分类，XML组装嵌套VO；未找到时返回null。 */
    ArticleDetailVO findDetailById(Integer articleId);

    /** 按文章编号和作者编号共同更新，返回影响行数；更新时间取自Service设置的updateTime。 */
    int update(Article article);

    /** 按文章编号和作者编号共同删除，返回影响行数；业务层须先清理关联评论。 */
    int deleteByIdAndUserId(Integer articleId, Integer userId);

    /** 统计符合可选分类条件的全部文章数，作为分页total；null表示不筛选分类。 */
    Long countByCategory(Integer categoryId);

    /** 查询一页文章摘要；@Param明确XML参数名，offset为跳过条数，pageSize为本页上限。 */
    List<ArticleListItemVO> findPage(@Param("categoryId") Integer categoryId, @Param("offset") long offset, @Param("pageSize") int pageSize);
}
