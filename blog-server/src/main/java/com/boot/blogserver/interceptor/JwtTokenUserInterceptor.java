package com.boot.blogserver.interceptor;

import com.blog.context.BaseContext;
import com.blog.constant.RoleConstant;
import com.blog.constant.UserStatusConstant;
import com.blog.entry.User;
import com.blog.exception.ForbiddenException;
import com.blog.exception.UnauthorizedException;
import com.blog.properties.JwtProperties;
import com.blog.utils.JwtUtil;
import com.boot.blogserver.mapper.UserMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@Slf4j
public class JwtTokenUserInterceptor implements HandlerInterceptor {

    @Resource
    private JwtProperties jwtProperties;
    @Resource
    private UserMapper userMapper;

    /**
     * 校验jwt
     *
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //判断当前拦截到的是Controller的方法还是其他资源
        if (!(handler instanceof HandlerMethod)) {
            //当前拦截到的不是动态方法，直接放行
            return true;
        }
        if (isPublicRequest(request)) {
            return true;
        }

        //1、从请求头中获取令牌
        String token = request.getHeader(jwtProperties.getUserTokenName());

        //2、校验令牌
        try {
            log.info("jwt校验:{}", token);
            Claims claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), token);
            Long userId = Long.valueOf(claims.get("userId").toString());
            User currentUser = userMapper.getById(userId);
            if (currentUser == null) {
                throw new UnauthorizedException("登录状态无效或已过期");
            }
            if (UserStatusConstant.STATUS_DISABLED.equals(currentUser.getStatus())) {
                throw new ForbiddenException("当前账号已被禁用");
            }

            BaseContext.setCurrentId(currentUser.getId());
            BaseContext.setCurrentRole(currentUser.getRole());
            BaseContext.setCurrentStatus(currentUser.getStatus());
            log.info("jwt校验-当前用户id：{}", BaseContext.getCurrentId());

            if (request.getRequestURI().contains("/admin/")
                    && !RoleConstant.ADMIN.equals(currentUser.getRole())) {
                throw new ForbiddenException("无管理员权限");
            }
            return true;
        } catch (Exception ex) {
            log.warn("jwt校验失败", ex);
            BaseContext.clear();
            if (ex instanceof UnauthorizedException unauthorizedException) {
                throw unauthorizedException;
            }
            if (ex instanceof ForbiddenException forbiddenException) {
                throw forbiddenException;
            }
            throw new UnauthorizedException("登录状态无效或已过期");
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        BaseContext.clear();
    }

    private boolean isPublicRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String method = request.getMethod();

        if ("GET".equalsIgnoreCase(method)) {
            if ("/articles".equals(uri)
                    || "/comments".equals(uri)
                    || "/categories".equals(uri)
                    || "/tags".equals(uri)) {
                return true;
            }
            if (uri.matches("^/articles/\\d+$")
                    || uri.matches("^/articles/detail/\\d+$")
                    || uri.matches("^/comments/list$")
                    || uri.matches("^/users/\\d+$")
                    || uri.matches("^/users/public/\\d+$")) {
                return true;
            }
        }
        return "/users/login".equals(uri) || "/users/register".equals(uri);
    }
}

