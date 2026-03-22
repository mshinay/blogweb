package com.boot.blogserver.controller;

import com.blog.result.PageResult;
import com.blog.result.Result;
import com.boot.blogserver.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/categories")
    public Result<PageResult> listEnabled() {
        log.info("查询启用分类列表");
        return Result.success(categoryService.listEnabled());
    }
}
