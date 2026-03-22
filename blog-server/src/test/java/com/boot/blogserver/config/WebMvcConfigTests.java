package com.boot.blogserver.config;

import com.boot.blogserver.interceptor.JwtTokenUserInterceptor;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class WebMvcConfigTests {

    @Test
    void addInterceptorsShouldOnlyExcludeExistingPublicPaths() {
        WebMvcConfig config = new WebMvcConfig();
        ReflectionTestUtils.setField(config, "jwtTokenUserInterceptor", mock(JwtTokenUserInterceptor.class));

        InterceptorRegistry registry = new InterceptorRegistry();

        config.addInterceptors(registry);

        @SuppressWarnings("unchecked")
        List<InterceptorRegistration> registrations =
                (List<InterceptorRegistration>) ReflectionTestUtils.getField(registry, "registrations");

        assertEquals(1, registrations.size());

        InterceptorRegistration registration = registrations.get(0);
        @SuppressWarnings("unchecked")
        List<String> includePatterns =
                (List<String>) ReflectionTestUtils.getField(registration, "includePatterns");
        @SuppressWarnings("unchecked")
        List<String> excludePatterns =
                (List<String>) ReflectionTestUtils.getField(registration, "excludePatterns");

        assertTrue(includePatterns.contains("/users/**"));
        assertTrue(includePatterns.contains("/articles"));
        assertTrue(includePatterns.contains("/articles/**"));
        assertTrue(includePatterns.contains("/comments"));
        assertTrue(includePatterns.contains("/comments/**"));
        assertTrue(includePatterns.contains("/admin/**"));
        assertTrue(excludePatterns.contains("/users/login"));
        assertTrue(excludePatterns.contains("/users/register"));
    }
}
