package com.boot.blogserver.service.impl;

import com.blog.constant.CommentStatusConstant;
import com.blog.context.BaseContext;
import com.blog.dto.CommentListDTO;
import com.blog.dto.CommentUpdateDTO;
import com.blog.dto.CommentUploadDTO;
import com.blog.entry.Article;
import com.blog.entry.Comment;
import com.blog.entry.User;
import com.blog.result.PageResult;
import com.blog.utils.ArticleUtil;
import com.blog.vo.AdminCommentManageVO;
import com.blog.vo.ArticlePreviewVO;
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
import java.util.*;
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
        comment.setCreatedTime(LocalDateTime.now());
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
        Page<Comment> pages = commentMapper.pageQuery(commentListDTO);
        List<CommentPreviewVO> previewVOS = new ArrayList<>();
        pages.getResult().forEach(comment -> {
            CommentPreviewVO commentPreviewVO = new CommentPreviewVO();
            BeanUtils.copyProperties(comment, commentPreviewVO);
            commentPreviewVO.setUserName(userMapper.getNameById(comment.getUserId()));
            if(commentPreviewVO.getStatus()==CommentStatusConstant.STATUS_NORMAL) {
                previewVOS.add(commentPreviewVO);
            }
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
    public PageResult commentAdminList(CommentListDTO commentListDTO) {
        // 分页查询评论
        PageHelper.startPage(commentListDTO.getPage(), commentListDTO.getPageSize());
        Page<Comment> pages = commentMapper.pageQuery(commentListDTO);

        List<Comment> comments = pages.getResult();
        if (comments.isEmpty()) {
            return new PageResult(0L, Collections.emptyList());
        }

        // 提取所有 userId 和 articleId
        Set<Long> userIds = comments.stream().map(Comment::getUserId).collect(Collectors.toSet());
        Set<Long> articleIds = comments.stream().map(Comment::getArticleId).collect(Collectors.toSet());


        // 批量查询用户昵称和文章信息（需你自行实现这两个方法）
        List<User> users = userMapper.getUsersByIds(userIds);           // Map<userId, userName>
        List<Article> articles = articleMapper.getArticleByIds(articleIds); // Map<articleId, title>
        Map<Long,String> userNameMap = users.stream()
                .collect(Collectors.toMap(User::getId, User::getUsername));
        Map<Long,String> articleTitleMap = articles.stream()
                .collect(Collectors.toMap(Article::getId, Article::getTitle));
        // 构建按文章分组的结果
        Map<Long, AdminCommentManageVO> groupedMap = new LinkedHashMap<>();

        for (Comment comment : comments) {
            // 组装评论视图对象
            CommentPreviewVO commentVO = new CommentPreviewVO();
            BeanUtils.copyProperties(comment, commentVO);
            commentVO.setUserName(userNameMap.get(comment.getUserId()));
            commentVO.setUserName("哈哈");


            // 分组逻辑，computeIfAbsent 避免 if 判断
            AdminCommentManageVO group = groupedMap.computeIfAbsent(comment.getArticleId(), id -> {
                AdminCommentManageVO vo = new AdminCommentManageVO();
                vo.setArticleId(id);
                //log.info("ArticleId:{}",id);
                vo.setArticleTitle(articleTitleMap.getOrDefault(id, "无标题"));
                //log.info("<UNK>{}",articleTitleMap);
                //vo.setArticleTitle("null");
                vo.setComments(new ArrayList<>());
                //log.info("VO:{}",vo);
                return vo;
            });

            group.getComments().add(commentVO);
        }

        return new PageResult(pages.getTotal(), new ArrayList<>(groupedMap.values()));
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
