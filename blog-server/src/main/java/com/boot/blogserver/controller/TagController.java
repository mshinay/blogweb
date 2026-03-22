package com.boot.blogserver.controller;

import com.blog.result.PageResult;
import com.blog.result.Result;
import com.boot.blogserver.service.TagService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping("/tags")
    public Result<PageResult> listEnabled() {
        log.info("查询启用标签列表");
        return Result.success(tagService.listEnabled());
    }
}
