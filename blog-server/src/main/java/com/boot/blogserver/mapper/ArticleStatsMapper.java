package com.boot.blogserver.mapper;

import com.blog.entry.ArticleStats;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Set;

@Mapper
public interface ArticleStatsMapper {

    @Insert("insert into article_stats(article_id,view_count,like_count,comment_count,favorite_count,created_time,updated_time) " +
            "values(#{articleId},COALESCE(#{viewCount}, 0),COALESCE(#{likeCount}, 0),COALESCE(#{commentCount}, 0),COALESCE(#{favoriteCount}, 0),#{createdTime},#{updatedTime})")
    @Options(useGeneratedKeys = false)
    void save(ArticleStats articleStats);

    @Select("select * from article_stats where article_id = #{articleId}")
    ArticleStats getByArticleId(Long articleId);

    List<ArticleStats> getByArticleIds(@Param("articleIds") Set<Long> articleIds);

    int update(ArticleStats articleStats);
}
