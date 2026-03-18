package com.boot.blogserver.mapper;

import com.blog.entry.ArticleTag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

@Mapper
public interface ArticleTagMapper {

    void saveBatch(@Param("articleTags") List<ArticleTag> articleTags);

    int deleteByArticleId(Long articleId);

    List<Long> listTagIdsByArticleId(Long articleId);

    List<Long> listArticleIdsByTagId(Long tagId);

    List<ArticleTag> listByArticleIds(@Param("articleIds") Set<Long> articleIds);
}
