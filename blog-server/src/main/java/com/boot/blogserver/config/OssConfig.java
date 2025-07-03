package com.boot.blogserver.config;

import com.blog.properties.AliOssProperties;
import com.blog.utils.AliOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class OssConfig {

    @Bean
    @ConditionalOnMissingBean
    public AliOssUtil aliyOssUtil(AliOssProperties aliOssProperties) {
        log.info("创建阿里云文件上传对象", aliOssProperties);
        return new AliOssUtil(aliOssProperties.getEndpoint()
        , aliOssProperties.getAccessKeyId()
        , aliOssProperties.getAccessKeySecret()
        ,aliOssProperties.getBucketName());

    }
}
