package com.boot.blogserver.controller;

import com.blog.dto.ArticleAdminListDTO;
import com.blog.dto.ArticleListDTO;
import com.blog.dto.ArticleUploadDTO;
import com.blog.dto.CommentAdminListDTO;
import com.blog.dto.CommentListDTO;
import com.blog.dto.CommentUploadDTO;
import com.blog.dto.UserUpdateDTO;
import com.blog.entry.User;
import com.blog.properties.JwtProperties;
import com.blog.result.PageResult;
import com.blog.vo.CategoryVO;
import com.blog.vo.CommentPreviewVO;
import com.blog.vo.CommentTreeVO;
import com.blog.vo.TagVO;
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
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    @BeforeEach
    void setUp() {
        UserController userController = new UserController();
        ArticleController articleController = new ArticleController();
        CommentController commentController = new CommentController();
        categoryService = mock(CategoryService.class);
        tagService = mock(TagService.class);
        CategoryController categoryController = new CategoryController(categoryService);
        TagController tagController = new TagController(tagService);

        userService = mock(UserService.class);
        articleService = mock(ArticleService.class);
        commentService = mock(CommentService.class);
        UserMapper userMapper = mock(UserMapper.class);

        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setUserSecretKey("01234567890123456789012345678901");
        jwtProperties.setUserTtl(3600000L);
        jwtProperties.setUserTokenName("token");

        ReflectionTestUtils.setField(userController, "userService", userService);
        ReflectionTestUtils.setField(userController, "jwtProperties", jwtProperties);
        ReflectionTestUtils.setField(articleController, "articleService", articleService);
        ReflectionTestUtils.setField(commentController, "commentService", commentService);

        JwtTokenUserInterceptor jwtTokenUserInterceptor = new JwtTokenUserInterceptor();
        ReflectionTestUtils.setField(jwtTokenUserInterceptor, "jwtProperties", jwtProperties);
        ReflectionTestUtils.setField(jwtTokenUserInterceptor, "userMapper", userMapper);

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(
                        userController,
                        articleController,
                        commentController,
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
    void standardPathsShouldBeReachableAndLegacyAliasesStillWork() throws Exception {
        when(articleService.articleList(any(ArticleListDTO.class))).thenReturn(new PageResult());
        when(articleService.getArticleDetail(1L)).thenReturn(new com.blog.vo.ArticleDetailVO());
        when(articleService.uploadArticle(any(ArticleUploadDTO.class))).thenReturn(1L);
        when(articleService.articleAdminList(any(ArticleAdminListDTO.class))).thenReturn(new PageResult());
        when(commentService.commentList(any(CommentListDTO.class))).thenReturn(new PageResult());
        when(commentService.commentAdminList(any(CommentAdminListDTO.class))).thenReturn(new PageResult());

        mockMvc.perform(get("/articles").param("page", "1").param("pageSize", "10"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/articles/detail/1"))
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
                .andExpect(status().isOk());
        mockMvc.perform(get("/comments").param("articleId", "1").param("page", "1").param("pageSize", "10"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/comments/list").param("articleId", "1").param("page", "1").param("pageSize", "10"))
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
        mockMvc.perform(get("/admin/articles").param("page", "1").param("pageSize", "10"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/articles/admin/list").param("page", "1").param("pageSize", "10"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/admin/comments").param("page", "1").param("pageSize", "10"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/comments/admin/list").param("page", "1").param("pageSize", "10"))
                .andExpect(status().isOk());

        verify(articleService, times(2)).getArticleDetail(1L);
        verify(articleService, times(2)).uploadArticle(any(ArticleUploadDTO.class));
        verify(commentService).uploadComment(any(CommentUploadDTO.class));
        verify(articleService, times(2)).articleAdminList(any(ArticleAdminListDTO.class));
        verify(commentService, times(2)).commentAdminList(any(CommentAdminListDTO.class));
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
    void publicLegacyAliasesShouldRemainAccessibleWithoutToken() throws Exception {
        when(articleService.getArticleDetail(1L)).thenReturn(new com.blog.vo.ArticleDetailVO());
        when(commentService.commentList(any(CommentListDTO.class))).thenReturn(new PageResult());

        publicRouteMockMvc.perform(get("/articles/detail/1"))
                .andExpect(status().isOk());
        publicRouteMockMvc.perform(get("/comments/list").param("articleId", "1").param("page", "1").param("pageSize", "10"))
                .andExpect(status().isOk());

        verify(articleService).getArticleDetail(1L);
        verify(commentService).commentList(any(CommentListDTO.class));
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
}
