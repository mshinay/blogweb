package com.boot.blogserver;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement //开启注解方式的事务管理
@EnableScheduling //开启调度基础设施，具体任务语义由独立任务类承载
@MapperScan("com.boot.blogserver.mapper")
@Slf4j
public class BlogServerApplication {

    public static void main(String[] args) {

        SpringApplication.run(BlogServerApplication.class, args);
        log.info("程序启动");
    }

}
