package com.boot.blogserver.service.impl;

import com.blog.context.BaseContext;
import com.blog.dto.ArticleAdminListDTO;
import com.blog.dto.CommentAdminListDTO;
import com.blog.exception.ForbiddenException;
import com.boot.blogserver.mapper.ArticleMapper;
import com.boot.blogserver.mapper.CommentMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AdminPermissionServiceTests {

    @Mock
    private ArticleMapper articleMapper;
    @Mock
    private CommentMapper commentMapper;

    @InjectMocks
    private ArticleServiceImpl articleService;
    @InjectMocks
    private CommentServiceImpl commentService;

    @AfterEach
    void tearDown() {
        BaseContext.clear();
    }

    @Test
    void articleAdminListShouldRejectNonAdmin() {
        BaseContext.setCurrentRole(1);

        ForbiddenException exception = assertThrows(
                ForbiddenException.class,
                () -> articleService.articleAdminList(new ArticleAdminListDTO())
        );

        assertEquals("无管理员权限", exception.getMessage());
        verify(articleMapper, never()).pageQueryAdmin(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void articleEditStatusShouldRejectNonAdmin() {
        BaseContext.setCurrentRole(1);

        ForbiddenException exception = assertThrows(
                ForbiddenException.class,
                () -> articleService.editStatus(1L)
        );

        assertEquals("无管理员权限", exception.getMessage());
        verify(articleMapper, never()).getById(1L);
    }

    @Test
    void commentAdminListShouldRejectNonAdmin() {
        BaseContext.setCurrentRole(1);

        ForbiddenException exception = assertThrows(
                ForbiddenException.class,
                () -> commentService.commentAdminList(new CommentAdminListDTO())
        );

        assertEquals("无管理员权限", exception.getMessage());
        verify(commentMapper, never()).pageQueryAdmin(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void commentEditStatusShouldRejectNonAdmin() {
        BaseContext.setCurrentRole(1);

        ForbiddenException exception = assertThrows(
                ForbiddenException.class,
                () -> commentService.editStatus(1L)
        );

        assertEquals("无管理员权限", exception.getMessage());
        verify(commentMapper, never()).getById(1L);
    }
}
