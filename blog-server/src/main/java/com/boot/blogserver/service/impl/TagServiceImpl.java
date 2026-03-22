package com.boot.blogserver.service.impl;

import com.blog.entry.Tag;
import com.blog.result.PageResult;
import com.blog.vo.TagVO;
import com.boot.blogserver.mapper.TagMapper;
import com.boot.blogserver.service.TagService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TagServiceImpl implements TagService {

    private final TagMapper tagMapper;

    public TagServiceImpl(TagMapper tagMapper) {
        this.tagMapper = tagMapper;
    }

    @Override
    public PageResult listEnabled() {
        List<TagVO> records = tagMapper.listEnabled().stream()
                .map(this::toTagVO)
                .toList();
        return new PageResult(records.size(), records);
    }

    private TagVO toTagVO(Tag tag) {
        TagVO tagVO = new TagVO();
        BeanUtils.copyProperties(tag, tagVO);
        return tagVO;
    }
}
