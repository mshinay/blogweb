package com.boot.blogserver.service.impl;

import com.blog.constant.CommentStatusConstant;
import com.blog.context.BaseContext;
import com.blog.dto.CommentAdminListDTO;
import com.blog.dto.CommentListDTO;
import com.blog.dto.CommentUpdateDTO;
import com.blog.dto.CommentUploadDTO;
import com.blog.entry.Article;
import com.blog.entry.Comment;
import com.blog.entry.User;
import com.blog.result.PageResult;
import com.blog.vo.AdminCommentListVO;
import com.blog.vo.CommentPreviewVO;
import com.boot.blogserver.mapper.ArticleMapper;
import com.boot.blogserver.mapper.CommentMapper;
import com.boot.blogserver.mapper.UserMapper;
import com.boot.blogserver.service.CommentService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CommentServiceImpl implements CommentService {
    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ArticleMapper articleMapper;

    /**
     * 用户上传评论
     * @param commentUploadDTO
     */
    @Override
    public void uploadComment(CommentUploadDTO commentUploadDTO) {
        Comment comment = new Comment();
        BeanUtils.copyProperties(commentUploadDTO, comment);
        comment.setUserId(BaseContext.getCurrentId());
        log.info("{}",BaseContext.getCurrentId());
        comment.setStatus(CommentStatusConstant.STATUS_NORMAL);
        LocalDateTime now = LocalDateTime.now();
        comment.setCreatedTime(now);
        comment.setUpdatedTime(now);
        commentMapper.save(comment);
    }

    /**
     * 分页查询评论
     * @param commentListDTO
     * @return
     */
    @Override
    public PageResult commentList(CommentListDTO commentListDTO) {
        //通过pagehelper给mybatis自动添加查询范围
        PageHelper.startPage(commentListDTO.getPage(), commentListDTO.getPageSize());

        //Page<>是由pagehelper封装的返回集合
        Page<Comment> pages = commentMapper.pageQueryPublished(commentListDTO);
        List<CommentPreviewVO> previewVOS = new ArrayList<>();
        pages.getResult().forEach(comment -> {
            CommentPreviewVO commentPreviewVO = new CommentPreviewVO();
            BeanUtils.copyProperties(comment, commentPreviewVO);
            commentPreviewVO.setUserName(userMapper.getNameById(comment.getUserId()));
            previewVOS.add(commentPreviewVO);
        });
        log.info("评论数{}",pages.getTotal());
        log.info("评论集{}",previewVOS);
        return new PageResult(pages.getTotal(), previewVOS);
    }

    /**
     * 用户删除发表评论
     * @param id
     */
    @Override
    public void deleteComment(Long id) {
        Comment comment = commentMapper.getById(id);
        if(comment==null) {
            throw new RuntimeException("该评论不存在");
        }
        comment = new Comment();
        comment.setId(id);
        comment.setStatus(CommentStatusConstant.STATUS_DELETED);
        log.info("当前状态{}",comment);
        commentMapper.update(comment);
    }

    @Override
    public void updateComment(CommentUpdateDTO commentUpdateDTO) {
        Comment comment = commentMapper.getById(commentUpdateDTO.getId());
        if(comment==null) {
            throw new RuntimeException("该评论不存在");
        }
        comment = new Comment();
        comment.setId(commentUpdateDTO.getId());
        comment.setContent(commentUpdateDTO.getContent());
        log.info("当前更新状态{}",comment);
        commentMapper.update(comment);
    }

    @Override
    public PageResult commentAdminList(CommentAdminListDTO commentAdminListDTO) {
        PageHelper.startPage(commentAdminListDTO.getPage(), commentAdminListDTO.getPageSize());
        Page<Comment> pages = commentMapper.pageQueryAdmin(commentAdminListDTO);

        List<Comment> comments = pages.getResult();
        if (comments.isEmpty()) {
            return new PageResult(0L, Collections.emptyList());
        }

        Set<Long> userIds = comments.stream()
                .flatMap(comment -> java.util.stream.Stream.of(comment.getUserId(), comment.getReplyUserId()))
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
        Set<Long> articleIds = comments.stream().map(Comment::getArticleId).collect(Collectors.toSet());

        List<User> users = userMapper.getUsersByIds(userIds);
        List<Article> articles = articleMapper.getArticleByIds(articleIds);
        Map<Long, String> userNameMap = users.stream()
                .collect(Collectors.toMap(User::getId, User::getUsername));
        Map<Long, Article> articleMap = articles.stream()
                .collect(Collectors.toMap(Article::getId, article -> article));

        List<AdminCommentListVO> records = new ArrayList<>();
        for (Comment comment : comments) {
            AdminCommentListVO vo = new AdminCommentListVO();
            BeanUtils.copyProperties(comment, vo);
            vo.setCommentId(comment.getId());
            vo.setUserName(userNameMap.get(comment.getUserId()));
            vo.setReplyUserName(userNameMap.get(comment.getReplyUserId()));

            Article article = articleMap.get(comment.getArticleId());
            if (article != null) {
                vo.setArticleTitle(article.getTitle());
                vo.setArticleStatus(article.getStatus());
            }

            records.add(vo);
        }

        return new PageResult(pages.getTotal(), records);
    }

    @Override
    public void editStatus(Long id) {
        Comment comment = commentMapper.getById(id);
        if(comment==null) {
            throw new RuntimeException("该评论不存在");
        }
        Integer currentStatus = comment.getStatus();
        Integer newStatus;

        if (CommentStatusConstant.STATUS_NORMAL.equals(currentStatus)) {
            newStatus = CommentStatusConstant.STATUS_HIDDEN;
        } else if (CommentStatusConstant.STATUS_HIDDEN.equals(currentStatus)) {
            newStatus = CommentStatusConstant.STATUS_NORMAL;
        } else {
            throw new RuntimeException("评论状态非法，无法切换");
        }

        comment.setStatus(newStatus);
        commentMapper.update(comment);
    }
}
