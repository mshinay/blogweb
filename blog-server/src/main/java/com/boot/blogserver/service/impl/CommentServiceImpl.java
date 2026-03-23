package com.boot.blogserver.service.impl;

import com.blog.constant.CommentStatusConstant;
import com.blog.constant.RoleConstant;
import com.blog.context.BaseContext;
import com.blog.dto.CommentAdminListDTO;
import com.blog.dto.CommentListDTO;
import com.blog.dto.CommentUpdateDTO;
import com.blog.dto.CommentUploadDTO;
import com.blog.dto.CommentUserHistoryQueryDTO;
import com.blog.entry.Article;
import com.blog.entry.ArticleStats;
import com.blog.entry.Comment;
import com.blog.entry.User;
import com.blog.exception.BusinessException;
import com.blog.exception.ForbiddenException;
import com.blog.exception.UnauthorizedException;
import com.blog.result.PageResult;
import com.blog.vo.AdminCommentListVO;
import com.blog.vo.CommentPreviewVO;
import com.blog.vo.CommentTreeVO;
import com.blog.vo.UserCommentHistoryVO;
import com.boot.blogserver.mapper.ArticleMapper;
import com.boot.blogserver.mapper.ArticleStatsMapper;
import com.boot.blogserver.mapper.CommentMapper;
import com.boot.blogserver.mapper.UserMapper;
import com.boot.blogserver.service.CommentService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class CommentServiceImpl implements CommentService {
    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ArticleMapper articleMapper;
    @Autowired
    private ArticleStatsMapper articleStatsMapper;

    /**
     * 用户上传评论
     * @param commentUploadDTO
     */
    @Transactional
    @Override
    public void uploadComment(CommentUploadDTO commentUploadDTO) {
        Comment comment = new Comment();
        BeanUtils.copyProperties(commentUploadDTO, comment);
        //如果不为根评论，则进行评论树校验
        if(comment.getRootId()>0){
            Comment replyToComment = commentMapper.getById(comment.getReplyToCommentId());
            Comment rootComment = commentMapper.getById(comment.getRootId());
            if(replyToComment==null || rootComment==null){
                throw new BusinessException("评论树结构异常");
            }
            /**
             * - rootId 是否对应某个顶级评论树
             * - replyToCommentId 是否确实是当前要回复的那条评论
             * - parentId 是否是允许挂载的位置，并且属于同一篇文章、同一棵树
             */
            if(!(rootComment.getRootId().equals(0L)
              && rootComment.getParentId().equals(0L)
              && rootComment.getArticleId().equals(comment.getArticleId())
              && replyToComment.getUserId().equals(comment.getReplyUserId())
              && replyToComment.getArticleId().equals(comment.getArticleId())
              && (replyToComment.getId().equals(comment.getRootId())||replyToComment.getRootId().equals(comment.getRootId()))
              && comment.getParentId().equals(rootComment.getId())))//当前接口策略,两层展示
            {
                throw new BusinessException("评论树结构异常");
            }

        }
        comment.setUserId(BaseContext.getCurrentId());
        log.info("{}",BaseContext.getCurrentId());
        comment.setStatus(CommentStatusConstant.STATUS_NORMAL);
        LocalDateTime now = LocalDateTime.now();
        comment.setCreatedTime(now);
        comment.setUpdatedTime(now);
        comment.setLikeCount(0);
        commentMapper.save(comment);
        ArticleStats articleStats = articleStatsMapper.getByArticleId(comment.getArticleId());
        articleStats.setCommentCount(articleStats.getCommentCount() + 1);
        articleStats.setUpdatedTime(LocalDateTime.now());
        articleStatsMapper.update(articleStats);

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
        List<CommentTreeVO> previewVOS = buildCommentTreeVOs(pages.getResult());
        log.info("评论数{}",pages.getTotal());
        log.info("评论集{}",previewVOS);

        return new PageResult(pages.getTotal(), previewVOS);
    }

    @Override
    public PageResult currentUserCommentHistory(CommentUserHistoryQueryDTO commentUserHistoryQueryDTO) {
        Long currentUserId = BaseContext.getCurrentId();
        if (currentUserId == null) {
            throw new UnauthorizedException("登录状态无效或已过期");
        }

        PageHelper.startPage(commentUserHistoryQueryDTO.getPage(), commentUserHistoryQueryDTO.getPageSize());
        Page<Comment> pages = commentMapper.pageQueryCurrentUser(commentUserHistoryQueryDTO, currentUserId);
        List<Comment> comments = pages.getResult();
        if (comments.isEmpty()) {
            return new PageResult(pages.getTotal(), Collections.emptyList());
        }

        Set<Long> userIds = comments.stream()
                .flatMap(comment -> Stream.of(comment.getUserId(), comment.getReplyUserId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<Long> articleIds = comments.stream().map(Comment::getArticleId).collect(Collectors.toSet());

        Map<Long, String> userNameMap = userMapper.getUsersByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, User::getUsername));
        Map<Long, String> articleTitleMap = articleMapper.getArticleByIds(articleIds).stream()
                .collect(Collectors.toMap(Article::getId, Article::getTitle));

        List<UserCommentHistoryVO> records = new ArrayList<>();
        for (Comment comment : comments) {
            UserCommentHistoryVO vo = new UserCommentHistoryVO();
            vo.setCommentId(comment.getId());
            vo.setArticleId(comment.getArticleId());
            vo.setArticleTitle(articleTitleMap.get(comment.getArticleId()));
            vo.setUserId(comment.getUserId());
            vo.setUserName(userNameMap.get(comment.getUserId()));
            vo.setReplyUserId(comment.getReplyUserId());
            vo.setReplyUserName(userNameMap.get(comment.getReplyUserId()));
            vo.setReplyToCommentId(comment.getReplyToCommentId());
            vo.setRootId(comment.getRootId());
            vo.setParentId(comment.getParentId());
            vo.setContent(comment.getContent());
            vo.setStatus(comment.getStatus());
            vo.setCreatedTime(comment.getCreatedTime());
            vo.setUpdatedTime(comment.getUpdatedTime());
            records.add(vo);
        }

        return new PageResult(pages.getTotal(), records);
    }

    public List<CommentTreeVO> buildCommentTreeVOs(List<Comment> rootComments) {
        if (rootComments.isEmpty()) {
            return Collections.emptyList();
        }
        List<CommentTreeVO> commentTreeVOs = new ArrayList<>();
        List<Long> rootIds = rootComments.stream()
                .map(Comment::getId)
                .collect(Collectors.toList());
        //根据根评论id列表查询所有子评论，并且过滤掉非正常状态的评论
        List<Comment> allChildComments = commentMapper.statusListByRootIds(rootIds,CommentStatusConstant.STATUS_NORMAL);
        Map<Long, List<Comment>> childCommentsMap = allChildComments.stream()
                .collect(Collectors.groupingBy(Comment::getRootId));
        //所以的comment都放在allComments里了，pages.getResult()是一级评论，childCommentsMap里是二级评论
        List<Comment> allComments = new ArrayList<>(rootComments);
        allComments.addAll(allChildComments);
        Map<Long, User> commentsUsers = userMapper.getUsersByIds(allComments.stream()
                        .flatMap(c -> Stream.of(c.getUserId(), c.getReplyUserId()))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet()))
                .stream()
                .collect(Collectors.toMap(User::getId, user -> user));
        rootComments.forEach(comment -> {
            CommentTreeVO commentTreeVO = new CommentTreeVO();
            commentTreeVO.setComment(convertToPreviewVO(comment,commentsUsers));
            List<Comment> childComments = childCommentsMap.getOrDefault(comment.getId(), Collections.emptyList());
            List<CommentPreviewVO> childPreviews = childComments.stream()
                    .map(childComment -> convertToPreviewVO(childComment,commentsUsers))
                    .collect(Collectors.toList());
            commentTreeVO.setChildren(childPreviews);


            commentTreeVOs.add(commentTreeVO);

        });

        return commentTreeVOs;
    }


    private CommentPreviewVO convertToPreviewVO(Comment comment,Map<Long, User> users) {
        CommentPreviewVO commentPreviewVO = new CommentPreviewVO();
        BeanUtils.copyProperties(comment, commentPreviewVO);
        User user = users.get(comment.getUserId());
        if (user == null) {
            commentPreviewVO.setUserName("默认");
            commentPreviewVO.setUserAvatarUrl("/images/default-avatar.png");
        }else {
            commentPreviewVO.setUserName(user.getUsername());
            commentPreviewVO.setUserAvatarUrl(user.getAvatarUrl());
        }
        if (comment.getReplyUserId() != null && comment.getReplyToCommentId() > 0){
            User replyedUser = users.get(comment.getReplyUserId());
            if(replyedUser==null){commentPreviewVO.setReplyUserName("默认");}
           else{ commentPreviewVO.setReplyUserName(replyedUser.getUsername());}
        }

        return commentPreviewVO;
    }


    /**
     * 用户删除发表评论
     * @param id
     */
    @Transactional
    @Override
    public void deleteComment(Long id) {
        Comment comment = commentMapper.getById(id);
        Long commentCount = 0L;
        if(comment==null) {
            throw BusinessException.notFound("该评论不存在");
        }
        validateCommentOwnership(comment);
        //根评论
        if(comment.getRootId()==0){
            List<Comment> childComments = commentMapper.listByRootId(comment.getId());
            for (Comment childComment : childComments) {
                childComment.setStatus(CommentStatusConstant.STATUS_DELETED);
                childComment.setUpdatedTime(LocalDateTime.now());
            }
            if(!childComments.isEmpty()){
                commentMapper.commentBatchUpsert(childComments);
                ArticleStats articleStats = articleStatsMapper.getByArticleId(comment.getArticleId());
                articleStats.setCommentCount(articleStats.getCommentCount() - (childComments.size()+1));
                articleStatsMapper.update(articleStats);
            }else{
                ArticleStats articleStats = articleStatsMapper.getByArticleId(comment.getArticleId());
                articleStats.setCommentCount(articleStats.getCommentCount() - 1);
                articleStatsMapper.update(articleStats);
            }

        }else{
            //二级评论
            List<Comment> allRootChildComments = commentMapper.listByRootId(comment.getRootId());
            List<Comment> childComments = commentMapper.listByReplyToCommentId(comment.getId());
            List<Long> currentChildIds = childComments.stream().map(Comment::getId).collect(Collectors.toList());
            List<Comment>allChildComments = new ArrayList<>(List.of());
            List<Comment> subAllChildComments;
            while(!currentChildIds.isEmpty()) {
                List<Long> finalCurrentChildIds = currentChildIds;
                subAllChildComments = allRootChildComments.stream()
                        .filter(childComment -> finalCurrentChildIds.contains(childComment.getReplyToCommentId()))
                        .collect(Collectors.toList());
                allChildComments.addAll(subAllChildComments);
                currentChildIds = subAllChildComments.stream().map(Comment::getId).collect(Collectors.toList());
            }
            allChildComments.addAll(childComments);
            for (Comment childComment : allChildComments) {
                childComment.setStatus(CommentStatusConstant.STATUS_DELETED);
                childComment.setUpdatedTime(LocalDateTime.now());
            }
            if(!childComments.isEmpty()){
                commentMapper.commentBatchUpsert(allChildComments);
                ArticleStats articleStats = articleStatsMapper.getByArticleId(comment.getArticleId());
                articleStats.setCommentCount(articleStats.getCommentCount() - (allChildComments.size()+1));
                articleStatsMapper.update(articleStats);

            }else {
                ArticleStats articleStats = articleStatsMapper.getByArticleId(comment.getArticleId());
                articleStats.setCommentCount(articleStats.getCommentCount() - 1);
                articleStatsMapper.update(articleStats);
            }

        }
        comment = new Comment();
        comment.setId(id);
        comment.setStatus(CommentStatusConstant.STATUS_DELETED);
        comment.setUpdatedTime(LocalDateTime.now());
        log.info("当前状态{}",comment);
        commentMapper.update(comment);

    }


    @Override
    public void updateComment(CommentUpdateDTO commentUpdateDTO) {
        Comment comment = commentMapper.getById(commentUpdateDTO.getId());
        if(comment==null) {
            throw BusinessException.notFound("该评论不存在");
        }
        validateCommentOwnership(comment);
        comment = new Comment();
        comment.setId(commentUpdateDTO.getId());
        comment.setContent(commentUpdateDTO.getContent());
        comment.setUpdatedTime(LocalDateTime.now());
        log.info("当前更新状态{}",comment);
        commentMapper.update(comment);
    }

    @Override
    public PageResult commentAdminList(CommentAdminListDTO commentAdminListDTO) {
        validateAdminAccess();
        PageHelper.startPage(commentAdminListDTO.getPage(), commentAdminListDTO.getPageSize());
        Page<Comment> pages = commentMapper.pageQueryAdmin(commentAdminListDTO);

        List<Comment> comments = pages.getResult();
        if (comments.isEmpty()) {
            return new PageResult(pages.getTotal(), Collections.emptyList());
        }

        Set<Long> userIds = comments.stream()
                .flatMap(comment -> Stream.of(comment.getUserId(), comment.getReplyUserId()))
                .filter(Objects::nonNull)
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
        validateAdminAccess();
        Comment comment = commentMapper.getById(id);
        if(comment==null) {
            throw BusinessException.notFound("该评论不存在");
        }
        Integer currentStatus = comment.getStatus();
        Integer newStatus;

        if (CommentStatusConstant.STATUS_NORMAL.equals(currentStatus)) {
            newStatus = CommentStatusConstant.STATUS_HIDDEN;
        } else if (CommentStatusConstant.STATUS_HIDDEN.equals(currentStatus)) {
            newStatus = CommentStatusConstant.STATUS_NORMAL;
        } else {
            throw new BusinessException("评论状态非法，无法切换");
        }

        comment.setStatus(newStatus);
        commentMapper.update(comment);
    }

    private void validateCommentOwnership(Comment comment) {
        Long currentUserId = BaseContext.getCurrentId();
        if (currentUserId == null || !currentUserId.equals(comment.getUserId())) {
            throw new ForbiddenException("无权操作他人的评论");
        }
    }

    private void validateAdminAccess() {
        if (!RoleConstant.ADMIN.equals(BaseContext.getCurrentRole())) {
            throw new ForbiddenException("无管理员权限");
        }
    }
}
