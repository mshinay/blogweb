package com.boot.blogserver.mapper;

import com.blog.dto.ArticleAdminListDTO;
import com.blog.dto.ArticleListDTO;
import com.blog.entry.Article;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Set;

@Mapper
public interface ArticleMapper {

    @Insert("insert into article(title,slug,summary,cover_url,content,content_plain,content_type,author_id,category_id,status,is_top,allow_comment,publish_time,word_count,created_time,updated_time) " +
            "values(#{title},#{slug},#{summary},#{coverUrl},#{content},#{contentPlain},COALESCE(#{contentType}, 'markdown'),#{authorId},#{categoryId},COALESCE(#{status}, 0),COALESCE(#{isTop}, 0),COALESCE(#{allowComment}, 1),#{publishTime},#{wordCount},#{createdTime},#{updatedTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void save(Article article);

    Page<Article> pageQueryPublished(@Param("query") ArticleListDTO articleListDTO,
                                     @Param("articleIds") Set<Long> articleIds);

    Page<Article> pageQueryAdmin(@Param("query") ArticleAdminListDTO articleAdminListDTO,
                                 @Param("articleIds") Set<Long> articleIds);

    @Select("select * from article where id=#{id}")
    Article getById(Long id);

    @Select("select * from article where id = #{id} and status = 1")
    Article getPublishedById(Long id);

    @Select("select * from article where slug = #{slug}")
    Article getBySlug(String slug);

    int update(Article article);

    List<Article> getArticleByIds(@Param("articleIds") Set<Long> articleIds);
}
