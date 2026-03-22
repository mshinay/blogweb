package com.boot.blogserver.service.impl;

import com.blog.entry.Category;
import com.blog.result.PageResult;
import com.blog.vo.CategoryVO;
import com.boot.blogserver.mapper.CategoryMapper;
import com.boot.blogserver.service.CategoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;

    public CategoryServiceImpl(CategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    @Override
    public PageResult listEnabled() {
        List<CategoryVO> records = categoryMapper.listEnabled().stream()
                .map(this::toCategoryVO)
                .toList();
        return new PageResult(records.size(), records);
    }

    private CategoryVO toCategoryVO(Category category) {
        CategoryVO categoryVO = new CategoryVO();
        BeanUtils.copyProperties(category, categoryVO);
        return categoryVO;
    }
}
