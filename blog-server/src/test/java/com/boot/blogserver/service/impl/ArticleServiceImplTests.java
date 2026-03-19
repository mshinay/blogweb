package com.boot.blogserver.service.impl;

import com.blog.constant.ArticleConstant;
import com.blog.constant.CategoryStatusConstant;
import com.blog.constant.CommentStatusConstant;
import com.blog.constant.TagStatusConstant;
import com.blog.context.BaseContext;
import com.blog.dto.ArticleEditDTO;
import com.blog.dto.ArticleUploadDTO;
import com.blog.entry.Article;
import com.blog.entry.ArticleTag;
import com.blog.entry.Category;
import com.blog.entry.Tag;
import com.blog.exception.BusinessException;
import com.blog.exception.ForbiddenException;
import com.boot.blogserver.mapper.ArticleMapper;
import com.boot.blogserver.mapper.ArticleTagMapper;
import com.boot.blogserver.mapper.CategoryMapper;
import com.boot.blogserver.mapper.CommentMapper;
import com.boot.blogserver.mapper.TagMapper;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ArticleServiceImplTests {

    @Mock
    private ArticleMapper articleMapper;
    @Mock
    private CommentMapper commentMapper;
    @Mock
    private ArticleTagMapper articleTagMapper;
    @Mock
    private CategoryMapper categoryMapper;
    @Mock
    private TagMapper tagMapper;

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

    @Test
    void uploadArticleShouldSaveArticleTags() {
        BaseContext.setCurrentId(1L);

        Category category = new Category();
        category.setId(2L);
        category.setStatus(CategoryStatusConstant.STATUS_ENABLED);
        when(categoryMapper.getById(2L)).thenReturn(category);

        Tag tag1 = new Tag();
        tag1.setId(10L);
        tag1.setStatus(TagStatusConstant.STATUS_ENABLED);
        Tag tag2 = new Tag();
        tag2.setId(20L);
        tag2.setStatus(TagStatusConstant.STATUS_ENABLED);
        when(tagMapper.getByIds(eq(new java.util.LinkedHashSet<>(java.util.List.of(10L, 20L)))))
                .thenReturn(java.util.List.of(tag1, tag2));

        ArticleUploadDTO dto = new ArticleUploadDTO();
        dto.setTitle("title");
        dto.setSlug("slug");
        dto.setContent("content");
        dto.setStatus(ArticleConstant.STATUS_DRAFT);
        dto.setAllowComment(1);
        dto.setCategoryId(2L);
        dto.setTagIds(java.util.List.of(10L, 20L));

        org.mockito.Mockito.doAnswer(invocation -> {
            Article article = invocation.getArgument(0);
            article.setId(99L);
            return null;
        }).when(articleMapper).save(any(Article.class));

        Long articleId = articleService.uploadArticle(dto);

        assertEquals(99L, articleId);
        verify(articleTagMapper).deleteByArticleId(99L);

        ArgumentCaptor<java.util.List<ArticleTag>> articleTagCaptor = ArgumentCaptor.forClass(java.util.List.class);
        verify(articleTagMapper).saveBatch(articleTagCaptor.capture());
        assertEquals(2, articleTagCaptor.getValue().size());
        assertEquals(99L, articleTagCaptor.getValue().get(0).getArticleId());
        assertEquals(10L, articleTagCaptor.getValue().get(0).getTagId());
        assertEquals(20L, articleTagCaptor.getValue().get(1).getTagId());
    }

    @Test
    void editArticleShouldRefreshArticleTags() {
        BaseContext.setCurrentId(1L);

        Article existingArticle = new Article();
        existingArticle.setId(1L);
        existingArticle.setAuthorId(1L);
        existingArticle.setStatus(ArticleConstant.STATUS_DRAFT);
        when(articleMapper.getById(1L)).thenReturn(existingArticle);

        Category category = new Category();
        category.setId(2L);
        category.setStatus(CategoryStatusConstant.STATUS_ENABLED);
        when(categoryMapper.getById(2L)).thenReturn(category);

        Tag tag = new Tag();
        tag.setId(30L);
        tag.setStatus(TagStatusConstant.STATUS_ENABLED);
        when(tagMapper.getByIds(eq(new java.util.LinkedHashSet<>(java.util.List.of(30L)))))
                .thenReturn(java.util.List.of(tag));

        ArticleEditDTO dto = new ArticleEditDTO();
        dto.setId(1L);
        dto.setTitle("title");
        dto.setSlug("slug");
        dto.setContent("content");
        dto.setCategoryId(2L);
        dto.setTagIds(java.util.List.of(30L));

        articleService.editArticle(dto);

        verify(articleMapper).update(any(Article.class));
        verify(articleTagMapper).deleteByArticleId(1L);
        ArgumentCaptor<java.util.List<ArticleTag>> articleTagCaptor = ArgumentCaptor.forClass(java.util.List.class);
        verify(articleTagMapper).saveBatch(articleTagCaptor.capture());
        assertEquals(1, articleTagCaptor.getValue().size());
        assertEquals(30L, articleTagCaptor.getValue().get(0).getTagId());
    }

    @Test
    void uploadArticleShouldRejectDisabledCategory() {
        BaseContext.setCurrentId(1L);

        Category category = new Category();
        category.setId(2L);
        category.setStatus(CategoryStatusConstant.STATUS_DISABLED);
        when(categoryMapper.getById(2L)).thenReturn(category);

        ArticleUploadDTO dto = new ArticleUploadDTO();
        dto.setTitle("title");
        dto.setSlug("slug");
        dto.setContent("content");
        dto.setStatus(ArticleConstant.STATUS_DRAFT);
        dto.setAllowComment(1);
        dto.setCategoryId(2L);

        BusinessException exception = assertThrows(BusinessException.class, () -> articleService.uploadArticle(dto));

        assertEquals("分类已禁用，无法绑定", exception.getMessage());
        verify(articleMapper, never()).save(any(Article.class));
        verify(articleTagMapper, never()).saveBatch(any());
    }

    @Test
    void uploadArticleShouldRejectDisabledTag() {
        BaseContext.setCurrentId(1L);

        Tag tag = new Tag();
        tag.setId(10L);
        tag.setStatus(TagStatusConstant.STATUS_DISABLED);
        when(tagMapper.getByIds(eq(new java.util.LinkedHashSet<>(java.util.List.of(10L)))))
                .thenReturn(java.util.List.of(tag));

        ArticleUploadDTO dto = new ArticleUploadDTO();
        dto.setTitle("title");
        dto.setSlug("slug");
        dto.setContent("content");
        dto.setStatus(ArticleConstant.STATUS_DRAFT);
        dto.setAllowComment(1);
        dto.setTagIds(java.util.List.of(10L));

        BusinessException exception = assertThrows(BusinessException.class, () -> articleService.uploadArticle(dto));

        assertEquals("标签已禁用，无法绑定", exception.getMessage());
        verify(articleMapper, never()).save(any(Article.class));
        verify(articleTagMapper, never()).saveBatch(any());
    }
}
