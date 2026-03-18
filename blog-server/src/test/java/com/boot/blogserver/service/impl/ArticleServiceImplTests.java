package com.boot.blogserver.service.impl;

import com.blog.constant.ArticleConstant;
import com.blog.constant.CommentStatusConstant;
import com.blog.context.BaseContext;
import com.blog.dto.ArticleEditDTO;
import com.blog.entry.Article;
import com.blog.exception.ForbiddenException;
import com.boot.blogserver.mapper.ArticleMapper;
import com.boot.blogserver.mapper.CommentMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ArticleServiceImplTests {

    @Mock
    private ArticleMapper articleMapper;
    @Mock
    private CommentMapper commentMapper;

    @InjectMocks
    private ArticleServiceImpl articleService;

    @AfterEach
    void tearDown() {
        BaseContext.clear();
    }

    @Test
    void editArticleShouldRejectEditingOthersArticle() {
        BaseContext.setCurrentId(2L);

        Article article = new Article();
        article.setId(1L);
        article.setAuthorId(1L);
        when(articleMapper.getById(1L)).thenReturn(article);

        ArticleEditDTO dto = new ArticleEditDTO();
        dto.setId(1L);
        dto.setTitle("title");
        dto.setSlug("slug");
        dto.setContent("content");

        ForbiddenException exception = assertThrows(ForbiddenException.class, () -> articleService.editArticle(dto));

        assertEquals("无权操作他人的文章", exception.getMessage());
        verify(articleMapper, never()).update(any(Article.class));
    }

    @Test
    void deleteArticleShouldRejectDeletingOthersArticle() {
        BaseContext.setCurrentId(2L);

        Article article = new Article();
        article.setId(1L);
        article.setAuthorId(1L);
        when(articleMapper.getById(1L)).thenReturn(article);

        ForbiddenException exception = assertThrows(ForbiddenException.class, () -> articleService.deleteArticle(1L));

        assertEquals("无权操作他人的文章", exception.getMessage());
        verify(articleMapper, never()).update(any(Article.class));
    }

    @Test
    void deleteArticleShouldAllowOwner() {
        BaseContext.setCurrentId(1L);

        Article article = new Article();
        article.setId(1L);
        article.setAuthorId(1L);
        article.setStatus(ArticleConstant.STATUS_PUBLISHED);
        when(articleMapper.getById(1L)).thenReturn(article);
        when(articleMapper.update(any(Article.class))).thenReturn(1);

        articleService.deleteArticle(1L);

        ArgumentCaptor<Article> articleCaptor = ArgumentCaptor.forClass(Article.class);
        verify(articleMapper).update(articleCaptor.capture());
        assertEquals(ArticleConstant.STATUS_DELETED, articleCaptor.getValue().getStatus());
        verify(commentMapper).updateStatus(1L, CommentStatusConstant.STATUS_DELETED);
    }
}
