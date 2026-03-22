package com.boot.blogserver.interceptor;

import com.blog.constant.RoleConstant;
import com.blog.constant.UserStatusConstant;
import com.blog.context.BaseContext;
import com.blog.dto.ArticleAdminListDTO;
import com.blog.dto.CommentUserHistoryQueryDTO;
import com.blog.exception.UnauthorizedException;
import com.blog.entry.User;
import com.blog.exception.ForbiddenException;
import com.blog.properties.JwtProperties;
import com.blog.utils.JwtUtil;
import com.boot.blogserver.controller.ArticleController;
import com.boot.blogserver.controller.CommentController;
import com.boot.blogserver.mapper.UserMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.method.HandlerMethod;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtTokenUserInterceptorTests {

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private JwtTokenUserInterceptor interceptor;

    @AfterEach
    void tearDown() {
        BaseContext.clear();
    }

    @Test
    void preHandleShouldRejectNonAdminForAdminPath() throws Exception {
        JwtProperties jwtProperties = buildJwtProperties();
        ReflectionTestUtils.setField(interceptor, "jwtProperties", jwtProperties);

        User user = new User();
        user.setId(1L);
        user.setRole(RoleConstant.USER);
        user.setStatus(UserStatusConstant.STATUS_NORMAL);
        when(userMapper.getById(1L)).thenReturn(user);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/admin/articles");
        request.addHeader(jwtProperties.getUserTokenName(), createToken(jwtProperties, 1L));
        MockHttpServletResponse response = new MockHttpServletResponse();
        HandlerMethod handlerMethod = new HandlerMethod(
                new ArticleController(),
                ArticleController.class.getMethod("adminListArticles", ArticleAdminListDTO.class)
        );

        ForbiddenException exception = assertThrows(
                ForbiddenException.class,
                () -> interceptor.preHandle(request, response, handlerMethod)
        );

        assertEquals("无管理员权限", exception.getMessage());
    }

    @Test
    void preHandleShouldRejectDisabledUser() throws Exception {
        JwtProperties jwtProperties = buildJwtProperties();
        ReflectionTestUtils.setField(interceptor, "jwtProperties", jwtProperties);

        User user = new User();
        user.setId(1L);
        user.setRole(RoleConstant.USER);
        user.setStatus(UserStatusConstant.STATUS_DISABLED);
        when(userMapper.getById(1L)).thenReturn(user);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/articles");
        request.setMethod("POST");
        request.addHeader(jwtProperties.getUserTokenName(), createToken(jwtProperties, 1L));
        MockHttpServletResponse response = new MockHttpServletResponse();
        HandlerMethod handlerMethod = new HandlerMethod(
                new ArticleController(),
                ArticleController.class.getMethod("uploadArticle", com.blog.dto.ArticleUploadDTO.class)
        );

        ForbiddenException exception = assertThrows(
                ForbiddenException.class,
                () -> interceptor.preHandle(request, response, handlerMethod)
        );

        assertEquals("当前账号已被禁用", exception.getMessage());
    }

    @Test
    void preHandleShouldAllowAdminPathForAdminAndStoreContext() throws Exception {
        JwtProperties jwtProperties = buildJwtProperties();
        ReflectionTestUtils.setField(interceptor, "jwtProperties", jwtProperties);

        User user = new User();
        user.setId(1L);
        user.setRole(RoleConstant.ADMIN);
        user.setStatus(UserStatusConstant.STATUS_NORMAL);
        when(userMapper.getById(1L)).thenReturn(user);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/admin/articles");
        request.addHeader(jwtProperties.getUserTokenName(), createToken(jwtProperties, 1L));
        MockHttpServletResponse response = new MockHttpServletResponse();
        HandlerMethod handlerMethod = new HandlerMethod(
                new ArticleController(),
                ArticleController.class.getMethod("adminListArticles", ArticleAdminListDTO.class)
        );

        boolean passed = interceptor.preHandle(request, response, handlerMethod);

        assertTrue(passed);
        assertEquals(1L, BaseContext.getCurrentId());
        assertEquals(RoleConstant.ADMIN, BaseContext.getCurrentRole());
        assertEquals(UserStatusConstant.STATUS_NORMAL, BaseContext.getCurrentStatus());
    }

    @Test
    void preHandleShouldAllowPublicGetArticleListWithoutToken() throws Exception {
        JwtProperties jwtProperties = buildJwtProperties();
        ReflectionTestUtils.setField(interceptor, "jwtProperties", jwtProperties);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/articles");
        MockHttpServletResponse response = new MockHttpServletResponse();
        HandlerMethod handlerMethod = new HandlerMethod(
                new ArticleController(),
                ArticleController.class.getMethod("listArticles", com.blog.dto.ArticleListDTO.class)
        );

        boolean passed = interceptor.preHandle(request, response, handlerMethod);

        assertTrue(passed);
    }

    @Test
    void preHandleShouldAllowStandardPublicDetailWithoutToken() throws Exception {
        JwtProperties jwtProperties = buildJwtProperties();
        ReflectionTestUtils.setField(interceptor, "jwtProperties", jwtProperties);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/users/1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        HandlerMethod handlerMethod = new HandlerMethod(
                new com.boot.blogserver.controller.UserController(),
                com.boot.blogserver.controller.UserController.class.getMethod("userInfo", Long.class)
        );

        boolean passed = interceptor.preHandle(request, response, handlerMethod);

        assertTrue(passed);
    }

    @Test
    void preHandleShouldRejectUsersMeCommentsWithoutToken() throws Exception {
        JwtProperties jwtProperties = buildJwtProperties();
        ReflectionTestUtils.setField(interceptor, "jwtProperties", jwtProperties);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/users/me/comments");
        MockHttpServletResponse response = new MockHttpServletResponse();
        HandlerMethod handlerMethod = new HandlerMethod(
                new CommentController(),
                CommentController.class.getMethod("currentUserCommentHistory", CommentUserHistoryQueryDTO.class)
        );

        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> interceptor.preHandle(request, response, handlerMethod)
        );

        assertEquals("登录状态无效或已过期", exception.getMessage());
    }

    @Test
    void preHandleShouldRejectArticleStatusPatchWithoutToken() throws Exception {
        JwtProperties jwtProperties = buildJwtProperties();
        ReflectionTestUtils.setField(interceptor, "jwtProperties", jwtProperties);

        MockHttpServletRequest request = new MockHttpServletRequest("PATCH", "/articles/1/status");
        MockHttpServletResponse response = new MockHttpServletResponse();
        HandlerMethod handlerMethod = new HandlerMethod(
                new ArticleController(),
                ArticleController.class.getMethod("editStatus", Long.class)
        );

        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> interceptor.preHandle(request, response, handlerMethod)
        );

        assertEquals("登录状态无效或已过期", exception.getMessage());
    }

    @Test
    void preHandleShouldRejectRemovedLegacyPublicPathWithoutToken() throws Exception {
        JwtProperties jwtProperties = buildJwtProperties();
        ReflectionTestUtils.setField(interceptor, "jwtProperties", jwtProperties);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/articles/detail/1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        HandlerMethod handlerMethod = new HandlerMethod(
                new ArticleController(),
                ArticleController.class.getMethod("showArticle", Long.class)
        );

        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> interceptor.preHandle(request, response, handlerMethod)
        );

        assertEquals("登录状态无效或已过期", exception.getMessage());
    }

    private JwtProperties buildJwtProperties() {
        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setUserSecretKey("01234567890123456789012345678901");
        jwtProperties.setUserTokenName("token");
        jwtProperties.setUserTtl(3600000L);
        return jwtProperties;
    }

    private String createToken(JwtProperties jwtProperties, Long userId) {
        java.util.Map<String, Object> claims = new java.util.HashMap<>();
        claims.put("userId", userId);
        return JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), claims);
    }
}
