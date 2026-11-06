package com.sebastian.bloghub.service;

import com.sebastian.bloghub.dto.ArticleCreateDTO;
import com.sebastian.bloghub.dto.ArticleUpdateDTO;
import com.sebastian.bloghub.vo.ArticleDetailVO;
import com.sebastian.bloghub.vo.ArticlePageVO;

/**
 * 文章业务契约：控制器依赖此接口，具体校验、作者权限和 Mapper 协作由实现类承担。
 * DTO 表示输入内容，VO 表示面向响应的查询结果；身份通过请求上下文提供。
 */
public interface ArticleService {

    /**
     * 接收已通过入口字段校验的发布 DTO，使用当前用户创建文章并返回生成的文章 ID。
     */
    Integer create(ArticleCreateDTO articleCreateDTO);

    /**
     * 按路径传来的文章 ID 获取详情 VO；无效标识与资源不存在分别报告 400、404。
     */
    ArticleDetailVO getDetail(Integer articleId);

    /**
     * 用路径文章 ID 和请求体的新内容执行作者权限校验及更新；无返回数据，失败抛业务异常。
     */
    void update(Integer articleId, ArticleUpdateDTO articleUpdateDTO);

    /**
     * 按文章 ID 校验当前用户是否为作者，再事务性删除关联评论及文章。
     */
    void delete(Integer articleId);

    /**
     * 接收页码与可选分类 ID，返回固定每页 10 条的分页结果和同一筛选条件下的记录总数。
     */
    ArticlePageVO getPage(Integer page, Integer categoryId);
}
