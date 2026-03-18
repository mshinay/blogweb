package com.boot.blogserver.service.impl;

import com.blog.constant.CommentStatusConstant;
import com.blog.context.BaseContext;
import com.blog.dto.CommentUpdateDTO;
import com.blog.entry.Comment;
import com.blog.exception.ForbiddenException;
import com.boot.blogserver.mapper.CommentMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTests {

    @Mock
    private CommentMapper commentMapper;

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
        when(commentMapper.getById(1L)).thenReturn(comment);

        commentService.deleteComment(1L);

        ArgumentCaptor<Comment> commentCaptor = ArgumentCaptor.forClass(Comment.class);
        verify(commentMapper).update(commentCaptor.capture());
        Comment updated = commentCaptor.getValue();
        assertEquals(CommentStatusConstant.STATUS_DELETED, updated.getStatus());
        assertNotNull(updated.getUpdatedTime());
    }
}
