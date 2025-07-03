package com.boot.blogserver.mapper;

import com.blog.dto.ArticleListDTO;
import com.blog.entry.Article;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Mapper
public interface ArticleMapper {

    @Insert("insert into article(title,content,author_id,status,create_time,update_time) " +
            "values(#{title},#{content},#{authorId},#{status},#{createTime},#{updateTime}) ")
    void save(Article article);

    Page<Article> pageQuery(ArticleListDTO articleListDTO);

    @Select("select * from article where id=#{id}")
    Article getById(Long id);

    int update(Article article);

    List<Article> getArticleByIds(Set<Long> articleIds);
}
