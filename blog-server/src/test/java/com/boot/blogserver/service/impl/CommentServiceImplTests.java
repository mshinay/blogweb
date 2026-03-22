package com.boot.blogserver.service.impl;

import com.blog.constant.CommentStatusConstant;
import com.blog.constant.RoleConstant;
import com.blog.context.BaseContext;
import com.blog.dto.CommentAdminListDTO;
import com.blog.entry.Article;
import com.blog.dto.CommentUpdateDTO;
import com.blog.entry.Comment;
import com.blog.entry.User;
import com.blog.exception.ForbiddenException;
import com.blog.result.PageResult;
import com.boot.blogserver.mapper.ArticleMapper;
import com.boot.blogserver.mapper.CommentMapper;
import com.boot.blogserver.mapper.UserMapper;
import com.github.pagehelper.Page;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTests {

    @Mock
    private CommentMapper commentMapper;
    @Mock
    private UserMapper userMapper;
    @Mock
    private ArticleMapper articleMapper;

    @InjectMocks
    private CommentServiceImpl commentService;

    @AfterEach
    void tearDown() {
        BaseContext.clear();
    }

    @Test
    void updateCommentShouldRejectUpdatingOthersComment() {
        BaseContext.setCurrentId(2L);

        Comment comment = new Comment();
        comment.setId(1L);
        comment.setUserId(1L);
        comment.setRootId(0L);
        when(commentMapper.getById(1L)).thenReturn(comment);

        CommentUpdateDTO dto = new CommentUpdateDTO();
        dto.setId(1L);
        dto.setContent("new content");

        ForbiddenException exception = assertThrows(ForbiddenException.class, () -> commentService.updateComment(dto));

        assertEquals("无权操作他人的评论", exception.getMessage());
        verify(commentMapper, never()).update(any(Comment.class));
    }

    @Test
    void deleteCommentShouldAllowOwner() {
        BaseContext.setCurrentId(1L);

        Comment comment = new Comment();
        comment.setId(1L);
        comment.setUserId(1L);
        comment.setRootId(0L);
        comment.setParentId(0L);
        when(commentMapper.getById(1L)).thenReturn(comment);
        when(commentMapper.listByRootId(1L)).thenReturn(List.of());

        commentService.deleteComment(1L);

        ArgumentCaptor<Comment> commentCaptor = ArgumentCaptor.forClass(Comment.class);
        verify(commentMapper).update(commentCaptor.capture());
        Comment updated = commentCaptor.getValue();
        assertEquals(CommentStatusConstant.STATUS_DELETED, updated.getStatus());
        assertNotNull(updated.getUpdatedTime());
    }

    @Test
    void commentAdminListShouldKeepPageTotalWhenCurrentPageIsEmpty() {
        BaseContext.setCurrentRole(RoleConstant.ADMIN);

        Page<Comment> page = new Page<>();
        page.setTotal(7);
        when(commentMapper.pageQueryAdmin(any(CommentAdminListDTO.class))).thenReturn(page);

        CommentAdminListDTO dto = new CommentAdminListDTO();
        dto.setPage(3);
        dto.setPageSize(10);

        PageResult pageResult = commentService.commentAdminList(dto);

        assertEquals(7L, pageResult.getTotal());
        assertEquals(List.of(), pageResult.getRecords());
    }

    @Test
    void commentAdminListShouldAggregateArticleAndUserFields() {
        BaseContext.setCurrentRole(RoleConstant.ADMIN);

        Comment comment = new Comment();
        comment.setId(1L);
        comment.setArticleId(101L);
        comment.setUserId(11L);
        comment.setReplyUserId(12L);
        comment.setRootId(0L);
        comment.setParentId(0L);
        comment.setContent("content");
        comment.setStatus(CommentStatusConstant.STATUS_NORMAL);

        Page<Comment> page = new Page<>();
        page.setTotal(1);
        page.add(comment);
        when(commentMapper.pageQueryAdmin(any(CommentAdminListDTO.class))).thenReturn(page);

        User user = new User();
        user.setId(11L);
        user.setUsername("user-a");
        User replyUser = new User();
        replyUser.setId(12L);
        replyUser.setUsername("user-b");
        when(userMapper.getUsersByIds(eq(Set.of(11L, 12L)))).thenReturn(List.of(user, replyUser));

        Article article = new Article();
        article.setId(101L);
        article.setTitle("article-title");
        article.setStatus(1);
        when(articleMapper.getArticleByIds(eq(Set.of(101L)))).thenReturn(List.of(article));

        CommentAdminListDTO dto = new CommentAdminListDTO();
        dto.setPage(1);
        dto.setPageSize(10);

        PageResult pageResult = commentService.commentAdminList(dto);

        assertEquals(1L, pageResult.getTotal());
        assertEquals(1, pageResult.getRecords().size());
        com.blog.vo.AdminCommentListVO record = (com.blog.vo.AdminCommentListVO) pageResult.getRecords().get(0);
        assertEquals(1L, record.getCommentId());
        assertEquals("article-title", record.getArticleTitle());
        assertEquals(1, record.getArticleStatus());
        assertEquals("user-a", record.getUserName());
        assertEquals("user-b", record.getReplyUserName());
    }
}
