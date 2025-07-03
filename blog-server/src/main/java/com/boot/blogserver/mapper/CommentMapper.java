package com.boot.blogserver.mapper;

import com.blog.dto.CommentListDTO;
import com.blog.entry.Article;
import com.blog.entry.Comment;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface CommentMapper {
    @Insert("insert into comment(article_id,content,user_id,status,create_time) " +
            "values(#{articleId},#{content},#{userId},#{status},#{createTime}) ")
    void save(Comment comment);

    Page<Comment> pageQuery(CommentListDTO commentListDTO);

    @Update("update comment set status = #{status} where article_id = #{articleId}")
    void updateStatus(Long articleId, Integer status);

    @Select("select * from comment where id = #{id}")
    Comment getById(Long id);

    void update(Comment comment);
}
