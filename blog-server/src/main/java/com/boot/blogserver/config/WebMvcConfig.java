package com.boot.blogserver.config;

import com.blog.json.JacksonObjectMapper;
import com.boot.blogserver.interceptor.JwtTokenUserInterceptor;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * 配置类，注册web层相关组件
 */
@Configuration
@Slf4j
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private JwtTokenUserInterceptor jwtTokenUserInterceptor;

    /**
     * 配置拦截器
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtTokenUserInterceptor)
                .addPathPatterns("/users/**")
                .addPathPatterns("/articles")
                .addPathPatterns("/articles/**")
                .addPathPatterns("/comments")
                .addPathPatterns("/comments/**")
                .addPathPatterns("/admin/**")
                .excludePathPatterns("/users/login", "/users/register"); // 放行登录、注册
    }

    /**
     * 跨域配置
     * @param registry
     */   
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")  // 允许所有路径跨域
                .allowedOrigins("http://localhost:5173")  // 前端开发阶段允许所有域
                .allowedMethods("GET", "POST", "PUT", "DELETE","PATCH")
                .allowCredentials(true);
    }
//5500 5173  http://127.0.0.1:5500/ http://localhost:5173

    /**
     * 扩展MVC框架的消息转换器
     * @param converters
     */
    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        log.info("扩展消息转换器");
        //创建消息转换器对象
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        //为消息转换器设置对象转换器，将java对象序列化为json数据
        converter.setObjectMapper(new JacksonObjectMapper());
        //将消息转换器设置到MVC框架的消息转换器里
        converters.add(0,converter);
    }
}
