package com.boot.blogserver.controller;

import com.blog.dto.ArticleAdminListDTO;
import com.blog.dto.ArticleEditDTO;
import com.blog.dto.ArticleListDTO;
import com.blog.dto.ArticleUploadDTO;
import com.blog.dto.CommentAdminListDTO;
import com.blog.dto.CommentListDTO;
import com.blog.dto.CommentUpdateDTO;
import com.blog.dto.CommentUploadDTO;
import com.blog.dto.CommentUserHistoryQueryDTO;
import com.blog.dto.UserUpdateDTO;
import com.blog.entry.User;
import com.blog.properties.JwtProperties;
import com.blog.result.PageResult;
import com.blog.utils.AliOssUtil;
import com.blog.vo.ArticleDetailVO;
import com.blog.vo.CategoryVO;
import com.blog.vo.CommentPreviewVO;
import com.blog.vo.CommentTreeVO;
import com.blog.vo.TagVO;
import com.blog.vo.UserCommentHistoryVO;
import com.boot.blogserver.handler.GlobalExceptionHandler;
import com.boot.blogserver.interceptor.JwtTokenUserInterceptor;
import com.boot.blogserver.mapper.UserMapper;
import com.boot.blogserver.service.ArticleService;
import com.boot.blogserver.service.CategoryService;
import com.boot.blogserver.service.CommentService;
import com.boot.blogserver.service.TagService;
import com.boot.blogserver.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ApiStandardizationControllerTests {

    private MockMvc mockMvc;
    private MockMvc publicRouteMockMvc;
    private UserService userService;
    private ArticleService articleService;
    private CommentService commentService;
    private CategoryService categoryService;
    private TagService tagService;
    private AliOssUtil aliOssUtil;
    private JwtProperties jwtProperties;
    private UserMapper userMapper;

    @BeforeEach
    void setUp() {
        UserController userController = new UserController();
        ArticleController articleController = new ArticleController();
        CommentController commentController = new CommentController();
        CommonController commonController = new CommonController();
        categoryService = mock(CategoryService.class);
        tagService = mock(TagService.class);
        CategoryController categoryController = new CategoryController(categoryService);
        TagController tagController = new TagController(tagService);

        userService = mock(UserService.class);
        articleService = mock(ArticleService.class);
        commentService = mock(CommentService.class);
        aliOssUtil = mock(AliOssUtil.class);
        userMapper = mock(UserMapper.class);

        jwtProperties = new JwtProperties();
        jwtProperties.setUserSecretKey("01234567890123456789012345678901");
        jwtProperties.setUserTtl(3600000L);
        jwtProperties.setUserTokenName("token");

        ReflectionTestUtils.setField(userController, "userService", userService);
        ReflectionTestUtils.setField(userController, "jwtProperties", jwtProperties);
        ReflectionTestUtils.setField(articleController, "articleService", articleService);
        ReflectionTestUtils.setField(commentController, "commentService", commentService);
        ReflectionTestUtils.setField(commonController, "ossUtil", aliOssUtil);

        JwtTokenUserInterceptor jwtTokenUserInterceptor = new JwtTokenUserInterceptor();
        ReflectionTestUtils.setField(jwtTokenUserInterceptor, "jwtProperties", jwtProperties);
        ReflectionTestUtils.setField(jwtTokenUserInterceptor, "userMapper", userMapper);

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(
                        userController,
                        articleController,
                        commentController,
                        commonController,
                        categoryController,
                        tagController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .build();

        publicRouteMockMvc = MockMvcBuilders.standaloneSetup(userController, articleController, commentController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .addInterceptors(jwtTokenUserInterceptor)
                .build();
    }

    @Test
    void standardPathsShouldBeReachable() throws Exception {
        when(articleService.articleList(any(ArticleListDTO.class))).thenReturn(new PageResult());
        when(articleService.getArticleDetail(1L)).thenReturn(new ArticleDetailVO());
        when(articleService.uploadArticle(any(ArticleUploadDTO.class))).thenReturn(1L);
        when(articleService.articleAdminList(any(ArticleAdminListDTO.class))).thenReturn(new PageResult());
        when(commentService.commentList(any(CommentListDTO.class))).thenReturn(new PageResult());
        when(commentService.currentUserCommentHistory(any(CommentUserHistoryQueryDTO.class))).thenReturn(new PageResult());
        when(commentService.commentAdminList(any(CommentAdminListDTO.class))).thenReturn(new PageResult());
        when(aliOssUtil.upload(any(byte[].class), any(String.class))).thenReturn("https://cdn.example.com/file.png");

        mockMvc.perform(get("/articles").param("page", "1").param("pageSize", "10"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/articles/1"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "title",
                                  "slug": "slug",
                                  "content": "content",
                                  "allowComment": 1,
                                  "status": 1
                                }
                                """))
                .andExpect(status().isOk());
        mockMvc.perform(put("/articles/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "title",
                                  "slug": "slug",
                                  "content": "content"
                                }
                                """))
                .andExpect(status().isOk());
        mockMvc.perform(patch("/articles/1/status"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/admin/articles").param("page", "1").param("pageSize", "10"))
                .andExpect(status().isOk());
        mockMvc.perform(patch("/admin/articles/1/status"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/comments").param("articleId", "1").param("page", "1").param("pageSize", "10"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/users/me/comments").param("page", "1").param("pageSize", "10"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "articleId": 1,
                                  "parentId": 0,
                                  "rootId": 0,
                                  "replyUserId": 0,
                                  "replyToCommentId": 0,
                                  "content": "content"
                                }
                                """))
                .andExpect(status().isOk());
        mockMvc.perform(put("/comments/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "content": "updated"
                                }
                                """))
                .andExpect(status().isOk());
        mockMvc.perform(get("/admin/comments").param("page", "1").param("pageSize", "10"))
                .andExpect(status().isOk());
        mockMvc.perform(patch("/admin/comments/3/status"))
                .andExpect(status().isOk());
        mockMvc.perform(multipart("/uploads")
                        .file(new MockMultipartFile("file", "cover.png", MediaType.IMAGE_PNG_VALUE, "png".getBytes())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("https://cdn.example.com/file.png"));

        verify(articleService).getArticleDetail(1L);
        verify(articleService).uploadArticle(any(ArticleUploadDTO.class));
        verify(articleService).editArticle(any(ArticleEditDTO.class));
        verify(articleService).editStatus(1L);
        verify(articleService).adminEditStatus(1L);
        verify(articleService).articleAdminList(any(ArticleAdminListDTO.class));
        verify(commentService).uploadComment(any(CommentUploadDTO.class));
        verify(commentService).currentUserCommentHistory(any(CommentUserHistoryQueryDTO.class));
        verify(commentService).updateComment(any(CommentUpdateDTO.class));
        verify(commentService).editStatus(3L);
        verify(commentService).commentAdminList(any(CommentAdminListDTO.class));
    }

    @Test
    void legacyPathsShouldReturnNotFound() throws Exception {
        mockMvc.perform(post("/users/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": 1,
                                  "nickname": "name"
                                }
                                """))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/users/public/1"))
                .andExpect(status().isNotFound());
        mockMvc.perform(post("/articles/upload")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "title",
                                  "slug": "slug",
                                  "content": "content",
                                  "allowComment": 1,
                                  "status": 1
                                }
                                """))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/articles/detail/1"))
                .andExpect(status().isNotFound());
        mockMvc.perform(post("/articles/edit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": 1,
                                  "title": "title",
                                  "slug": "slug",
                                  "content": "content"
                                }
                                """))
                .andExpect(status().isNotFound());
        mockMvc.perform(patch("/articles/1"))
                .andExpect(status().isMethodNotAllowed());
        mockMvc.perform(get("/articles/admin/list").param("page", "1").param("pageSize", "10"))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/articles/admin/search").param("page", "1").param("pageSize", "10"))
                .andExpect(status().isNotFound());
        mockMvc.perform(post("/comments/upload")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "articleId": 1,
                                  "parentId": 0,
                                  "rootId": 0,
                                  "replyUserId": 0,
                                  "replyToCommentId": 0,
                                  "content": "content"
                                }
                                """))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/comments/list").param("articleId", "1").param("page", "1").param("pageSize", "10"))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/comments/user").param("page", "1").param("pageSize", "10"))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/comments/user/search").param("page", "1").param("pageSize", "10"))
                .andExpect(status().isNotFound());
        mockMvc.perform(put("/comments/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": 2,
                                  "content": "updated"
                                }
                                """))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/comments/admin/list").param("page", "1").param("pageSize", "10"))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/comments/admin/search").param("page", "1").param("pageSize", "10"))
                .andExpect(status().isNotFound());
        mockMvc.perform(patch("/comments/admin/status/3"))
                .andExpect(status().isNotFound());
        mockMvc.perform(multipart("/common/upload")
                        .file(new MockMultipartFile("file", "cover.png", MediaType.IMAGE_PNG_VALUE, "png".getBytes())))
                .andExpect(status().isNotFound());

        verify(articleService, never()).uploadArticle(any(ArticleUploadDTO.class));
        verify(articleService, never()).getArticleDetail(any(Long.class));
        verify(articleService, never()).editArticle(any(ArticleEditDTO.class));
        verify(articleService, never()).editStatus(any(Long.class));
        verify(articleService, never()).articleAdminList(any(ArticleAdminListDTO.class));
        verify(commentService, never()).uploadComment(any(CommentUploadDTO.class));
        verify(commentService, never()).commentList(any(CommentListDTO.class));
        verify(commentService, never()).currentUserCommentHistory(any(CommentUserHistoryQueryDTO.class));
        verify(commentService, never()).updateComment(any(CommentUpdateDTO.class));
        verify(commentService, never()).commentAdminList(any(CommentAdminListDTO.class));
        verify(commentService, never()).editStatus(any(Long.class));
        verify(userService, never()).updte(any(UserUpdateDTO.class));
    }

    @Test
    void standardContractShouldExposeMessageChildrenAndPublicResources() throws Exception {
        User user = User.builder()
                .id(1L)
                .username("zhangsan")
                .nickname("张三")
                .avatarUrl("/avatar.png")
                .bio("bio")
                .build();
        when(userService.userInfo(1L)).thenReturn(user);

        CommentPreviewVO child = new CommentPreviewVO();
        child.setId(2L);
        CommentTreeVO treeVO = new CommentTreeVO();
        treeVO.setComment(new CommentPreviewVO());
        treeVO.setChildren(List.of(child));
        when(commentService.commentList(any(CommentListDTO.class))).thenReturn(new PageResult(1, List.of(treeVO)));

        CategoryVO categoryVO = new CategoryVO();
        categoryVO.setId(10L);
        when(categoryService.listEnabled()).thenReturn(new PageResult(1, List.of(categoryVO)));
        TagVO tagVO = new TagVO();
        tagVO.setId(20L);
        when(tagService.listEnabled()).thenReturn(new PageResult(1, List.of(tagVO)));

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.bio").value("bio"));
        mockMvc.perform(get("/comments").param("articleId", "1").param("page", "1").param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[0].children[0].id").value(2))
                .andExpect(jsonPath("$.data.records[0].replies").doesNotExist());
        mockMvc.perform(get("/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1));
        mockMvc.perform(get("/tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1));
        mockMvc.perform(post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "",
                                  "password": "123456"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("用户名不能为空"));
    }

    @Test
    void publicStandardPathsShouldRemainAccessibleWithoutToken() throws Exception {
        when(articleService.getArticleDetail(1L)).thenReturn(new ArticleDetailVO());
        when(commentService.commentList(any(CommentListDTO.class))).thenReturn(new PageResult());
        when(userService.userInfo(1L)).thenReturn(User.builder().id(1L).username("zhangsan").build());

        publicRouteMockMvc.perform(get("/articles/1"))
                .andExpect(status().isOk());
        publicRouteMockMvc.perform(get("/comments").param("articleId", "1").param("page", "1").param("pageSize", "10"))
                .andExpect(status().isOk());
        publicRouteMockMvc.perform(get("/users/1"))
                .andExpect(status().isOk());

        verify(articleService).getArticleDetail(1L);
        verify(commentService).commentList(any(CommentListDTO.class));
        verify(userService).userInfo(1L);
    }

    @Test
    void userCommentHistoryShouldRequireTokenAndKeepLegacyPathOffline() throws Exception {
        User currentUser = User.builder()
                .id(1L)
                .username("zhangsan")
                .build();
        when(userMapper.getById(1L)).thenReturn(currentUser);

        UserCommentHistoryVO record = new UserCommentHistoryVO();
        record.setCommentId(9L);
        record.setArticleId(101L);
        record.setArticleTitle("article-title");
        when(commentService.currentUserCommentHistory(any(CommentUserHistoryQueryDTO.class)))
                .thenReturn(new PageResult(1, List.of(record)));

        publicRouteMockMvc.perform(get("/users/me/comments").param("page", "1").param("pageSize", "10"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("登录状态无效或已过期"));

        publicRouteMockMvc.perform(get("/users/me/comments")
                        .header(jwtProperties.getUserTokenName(), createToken(1L))
                        .param("page", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].commentId").value(9))
                .andExpect(jsonPath("$.data.records[0].articleTitle").value("article-title"));

        publicRouteMockMvc.perform(get("/comments/user").header(jwtProperties.getUserTokenName(), createToken(1L)))
                .andExpect(status().isNotFound());
    }

    @Test
    void putUsersByIdShouldRejectMismatchedBodyId() throws Exception {
        mockMvc.perform(put("/users/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": 1,
                                  "nickname": "name"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("路径用户ID与请求体用户ID不一致"));

        verify(userService, never()).updte(any());
    }

    @Test
    void putUsersByIdShouldAllowBodyWithoutId() throws Exception {
        User updatedUser = User.builder()
                .id(2L)
                .username("lisi")
                .nickname("name")
                .avatarUrl("/avatar.png")
                .build();
        when(userService.updte(any(UserUpdateDTO.class))).thenReturn(updatedUser);

        mockMvc.perform(put("/users/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nickname": "name"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(2))
                .andExpect(jsonPath("$.message").doesNotExist());

        verify(userService).updte(any(UserUpdateDTO.class));
    }

    @Test
    void mediumPriorityResourcePathsShouldRejectMismatchedBodyId() throws Exception {
        mockMvc.perform(put("/articles/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": 1,
                                  "title": "title",
                                  "slug": "slug",
                                  "content": "content"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("路径文章ID与请求体文章ID不一致"));
        mockMvc.perform(put("/comments/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": 1,
                                  "content": "updated"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("路径评论ID与请求体评论ID不一致"));

        verify(articleService, never()).editArticle(any());
        verify(commentService, never()).updateComment(any());
    }

    private String createToken(Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        return com.blog.utils.JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), claims);
    }
}
