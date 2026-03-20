package com.boot.blogserver.mapper;

import com.blog.dto.CommentAdminListDTO;
import com.blog.dto.CommentListDTO;
import com.blog.entry.Comment;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Set;

@Mapper
public interface CommentMapper {
    @Insert("insert into comment(article_id,parent_id,root_id,reply_user_id,reply_to_comment_id,content,user_id,status,created_time,updated_time) " +
            "values(#{articleId},#{parentId},#{rootId},#{replyUserId},#{replyToCommentId},#{content},#{userId},#{status},#{createdTime},#{updatedTime}) ")
    void save(Comment comment);

    void commentBatchUpsert(List<Comment> commentList);

    Page<Comment> pageQueryPublished(CommentListDTO commentListDTO);

    Page<Comment> pageQueryAdmin(CommentAdminListDTO commentAdminListDTO);

    List<Comment> listByArticleId(Long articleId);

    List<Comment> listPublishedRootByArticleId(Long articleId);

    List<Comment> statusListByRootIds(List<Long> rootIds,Integer status);

    List<Comment> listPublishedByRootIds(@Param("rootIds") List<Long> rootIds);

    List<Comment> listByReplyToCommentIds(List<Long> replyToCommentIds);

    @Select("select * from comment where root_id = #{rootId}")
    List<Comment> listByRootId(Long rootId);

    @Select("select * from comment where reply_to_comment_id = #{replyToCommentId}")
    List<Comment> listByReplyToCommentIds(Long replyToCommentId);

    List<Comment> listByArticleIds(@Param("articleIds") Set<Long> articleIds);

    @Update("update comment set status = #{status} where article_id = #{articleId}")
    void updateStatus(@Param("articleId") Long articleId, @Param("status") Integer status);

    @Select("select * from comment where id = #{id}")
    Comment getById(Long id);

    void update(Comment comment);
}
