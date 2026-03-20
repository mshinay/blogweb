package com.boot.blogserver.service.impl;

import com.blog.constant.ArticleConstant;
import com.blog.constant.CategoryStatusConstant;
import com.blog.constant.CommentStatusConstant;
import com.blog.constant.TagStatusConstant;
import com.blog.constant.RoleConstant;
import com.blog.context.BaseContext;
import com.blog.dto.ArticleAdminListDTO;
import com.blog.dto.ArticleEditDTO;
import com.blog.dto.ArticleUploadDTO;
import com.blog.entry.Article;
import com.blog.entry.ArticleTag;
import com.blog.entry.ArticleStats;
import com.blog.entry.Category;
import com.blog.entry.Comment;
import com.blog.entry.Tag;
import com.blog.entry.User;
import com.blog.exception.BusinessException;
import com.blog.exception.ForbiddenException;
import com.blog.result.PageResult;
import com.blog.vo.CommentPreviewVO;
import com.blog.vo.CommentTreeVO;
import com.boot.blogserver.mapper.ArticleMapper;
import com.boot.blogserver.mapper.ArticleStatsMapper;
import com.boot.blogserver.mapper.ArticleTagMapper;
import com.boot.blogserver.mapper.CategoryMapper;
import com.boot.blogserver.mapper.CommentMapper;
import com.boot.blogserver.mapper.TagMapper;
import com.boot.blogserver.mapper.UserMapper;
import com.boot.blogserver.service.CommentService;
import com.github.pagehelper.Page;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
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
    private UserMapper userMapper;
    @Mock
    private CommentService commentService;
    @Mock
    private ArticleTagMapper articleTagMapper;
    @Mock
    private CategoryMapper categoryMapper;
    @Mock
    private ArticleStatsMapper articleStatsMapper;
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

    @Test
    void getArticleDetailShouldAggregateCoreData() {
        Article article = new Article();
        article.setId(1L);
        article.setTitle("title");
        article.setSlug("slug");
        article.setSummary("summary");
        article.setContent("content");
        article.setContentType("markdown");
        article.setAuthorId(11L);
        article.setCategoryId(21L);
        article.setStatus(ArticleConstant.STATUS_PUBLISHED);
        article.setAllowComment(1);
        article.setWordCount(123);
        when(articleMapper.getPublishedById(1L)).thenReturn(article);

        User author = new User();
        author.setId(11L);
        author.setUsername("author");
        author.setNickname("作者");
        author.setAvatarUrl("/author.png");

        when(userMapper.getById(11L)).thenReturn(author);

        Category category = new Category();
        category.setId(21L);
        category.setName("Java");
        category.setSlug("java");
        when(categoryMapper.getById(21L)).thenReturn(category);

        Tag tag = new Tag();
        tag.setId(31L);
        tag.setName("Spring");
        tag.setSlug("spring");
        when(articleTagMapper.listByArticleIds(eq(java.util.Set.of(1L))))
                .thenReturn(java.util.List.of(ArticleTag.builder().articleId(1L).tagId(31L).build()));
        when(tagMapper.getByIds(eq(java.util.Set.of(31L)))).thenReturn(java.util.List.of(tag));

        ArticleStats stats = ArticleStats.builder()
                .articleId(1L)
                .viewCount(100L)
                .likeCount(8L)
                .commentCount(2L)
                .favoriteCount(3L)
                .build();
        when(articleStatsMapper.getByArticleId(1L)).thenReturn(stats);

        Comment rootComment = Comment.builder()
                .id(101L)
                .articleId(1L)
                .userId(11L)
                .parentId(0L)
                .rootId(0L)
                .content("root")
                .status(CommentStatusConstant.STATUS_NORMAL)
                .build();
        when(commentMapper.listPublishedRootByArticleId(1L)).thenReturn(java.util.List.of(rootComment));

        CommentPreviewVO rootPreview = new CommentPreviewVO();
        rootPreview.setId(101L);
        rootPreview.setContent("root");
        CommentPreviewVO replyPreview = new CommentPreviewVO();
        replyPreview.setId(102L);
        replyPreview.setContent("reply");
        replyPreview.setReplyUserName("reply-user");
        CommentTreeVO commentTreeVO = new CommentTreeVO();
        commentTreeVO.setComment(rootPreview);
        commentTreeVO.setReplies(java.util.List.of(replyPreview));
        when(commentService.buildCommentTreeVOs(eq(java.util.List.of(rootComment))))
                .thenReturn(java.util.List.of(commentTreeVO));

        com.blog.vo.ArticleDetailVO detail = articleService.getArticleDetail(1L);

        assertEquals("title", detail.getTitle());
        assertNotNull(detail.getAuthor());
        assertEquals("author", detail.getAuthor().getUsername());
        assertNotNull(detail.getCategory());
        assertEquals("java", detail.getCategory().getSlug());
        assertEquals(1, detail.getTags().size());
        assertEquals("Spring", detail.getTags().get(0).getName());
        assertEquals(100L, detail.getStats().getViewCount());
        assertEquals(1, detail.getComments().size());
        assertEquals("root", detail.getComments().get(0).getComment().getContent());
        assertEquals(1, detail.getComments().get(0).getReplies().size());
        assertEquals("reply-user", detail.getComments().get(0).getReplies().get(0).getReplyUserName());
    }

    @Test
    void getArticleDetailShouldReturnZeroStatsWhenStatsMissing() {
        Article article = new Article();
        article.setId(1L);
        article.setAuthorId(11L);
        article.setStatus(ArticleConstant.STATUS_PUBLISHED);
        when(articleMapper.getPublishedById(1L)).thenReturn(article);

        java.util.List<Comment> rootComments = java.util.Collections.emptyList();
        when(commentMapper.listPublishedRootByArticleId(1L)).thenReturn(rootComments);
        when(commentService.buildCommentTreeVOs(eq(rootComments))).thenReturn(java.util.Collections.emptyList());

        com.blog.vo.ArticleDetailVO detail = articleService.getArticleDetail(1L);

        assertNotNull(detail.getStats());
        assertEquals(0L, detail.getStats().getViewCount());
        assertEquals(0L, detail.getStats().getLikeCount());
        assertEquals(0L, detail.getStats().getCommentCount());
        assertEquals(0L, detail.getStats().getFavoriteCount());
        assertEquals(java.util.Collections.emptyList(), detail.getComments());
    }

    @Test
    void getArticleDetailShouldThrowNotFoundWhenArticleMissing() {
        when(articleMapper.getPublishedById(1L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> articleService.getArticleDetail(1L));

        assertEquals("该文章不存在", exception.getMessage());
        verify(commentMapper, never()).listPublishedRootByArticleId(any());
        verify(commentService, never()).buildCommentTreeVOs(any());
    }

    @Test
    void getArticleDetailShouldRejectUnpublishedArticleWhenMapperReturnsIt() {
        Article article = new Article();
        article.setId(1L);
        article.setStatus(ArticleConstant.STATUS_DRAFT);
        when(articleMapper.getPublishedById(1L)).thenReturn(article);

        ForbiddenException exception = assertThrows(ForbiddenException.class, () -> articleService.getArticleDetail(1L));

        assertEquals("该文章无法访问", exception.getMessage());
        verify(commentMapper, never()).listPublishedRootByArticleId(any());
        verify(commentService, never()).buildCommentTreeVOs(any());
    }

    @Test
    void articleAdminListShouldIncludeTagList() {
        BaseContext.setCurrentRole(RoleConstant.ADMIN);

        Article article = new Article();
        article.setId(1L);
        article.setTitle("title");
        article.setAuthorId(11L);
        article.setCategoryId(21L);
        article.setStatus(ArticleConstant.STATUS_PUBLISHED);

        Page<Article> page = new Page<>();
        page.setTotal(1);
        page.add(article);

        when(articleMapper.pageQueryAdmin(any(ArticleAdminListDTO.class), isNull())).thenReturn(page);

        User author = new User();
        author.setId(11L);
        author.setUsername("admin-author");
        when(userMapper.getUsersByIds(eq(java.util.Set.of(11L)))).thenReturn(java.util.List.of(author));

        Category category = new Category();
        category.setId(21L);
        category.setName("Java");
        when(categoryMapper.getByIds(eq(java.util.Set.of(21L)))).thenReturn(java.util.List.of(category));

        ArticleStats stats = ArticleStats.builder()
                .articleId(1L)
                .viewCount(20L)
                .commentCount(5L)
                .build();
        when(articleStatsMapper.getByArticleIds(eq(java.util.Set.of(1L)))).thenReturn(java.util.List.of(stats));

        when(articleTagMapper.listByArticleIds(eq(java.util.Set.of(1L))))
                .thenReturn(java.util.List.of(ArticleTag.builder().articleId(1L).tagId(31L).build()));
        Tag tag = new Tag();
        tag.setId(31L);
        tag.setName("Spring");
        tag.setSlug("spring");
        when(tagMapper.getByIds(eq(java.util.Set.of(31L)))).thenReturn(java.util.List.of(tag));

        ArticleAdminListDTO dto = new ArticleAdminListDTO();
        dto.setPage(1);
        dto.setPageSize(10);

        PageResult pageResult = articleService.articleAdminList(dto);

        assertEquals(1L, pageResult.getTotal());
        assertEquals(1, pageResult.getRecords().size());
        com.blog.vo.ArticleAdminListVO record = (com.blog.vo.ArticleAdminListVO) pageResult.getRecords().get(0);
        assertEquals("admin-author", record.getAuthorName());
        assertEquals("Java", record.getCategoryName());
        assertEquals(1, record.getTagList().size());
        assertEquals("Spring", record.getTagList().get(0).getName());
        assertEquals(20L, record.getViewCount());
        assertEquals(5L, record.getCommentCount());
    }
}
