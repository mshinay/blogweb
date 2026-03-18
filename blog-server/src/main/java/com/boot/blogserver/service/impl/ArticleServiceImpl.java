package com.boot.blogserver.service.impl;

import com.blog.constant.ArticleConstant;
import com.blog.constant.CommentStatusConstant;
import com.blog.constant.RoleConstant;
import com.blog.context.BaseContext;
import com.blog.dto.ArticleAdminListDTO;
import com.blog.dto.ArticleEditDTO;
import com.blog.dto.ArticleListDTO;
import com.blog.dto.ArticleUploadDTO;
import com.blog.entry.Article;
import com.blog.entry.ArticleStats;
import com.blog.entry.Category;
import com.blog.entry.User;
import com.blog.exception.BusinessException;
import com.blog.exception.ForbiddenException;
import com.blog.result.PageResult;
import com.blog.utils.ArticleUtil;
import com.blog.vo.ArticleAdminListVO;
import com.blog.vo.ArticleDetailVO;
import com.blog.vo.ArticlePreviewVO;
import com.blog.vo.UserProfileVO;
import com.boot.blogserver.mapper.ArticleMapper;
import com.boot.blogserver.mapper.ArticleStatsMapper;
import com.boot.blogserver.mapper.ArticleTagMapper;
import com.boot.blogserver.mapper.CategoryMapper;
import com.boot.blogserver.mapper.CommentMapper;
import com.boot.blogserver.mapper.UserMapper;
import com.boot.blogserver.service.ArticleService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ArticleServiceImpl implements ArticleService {

    @Autowired
    private ArticleMapper articleMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private ArticleTagMapper articleTagMapper;
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private ArticleStatsMapper articleStatsMapper;

    /**
     * 上传文章
     *
     * @param articleUploadDTO
     */
    @Override
    public Long uploadArticle(ArticleUploadDTO articleUploadDTO) {
        Article article = new Article();
        BeanUtils.copyProperties(articleUploadDTO, article);
        article.setAuthorId(BaseContext.getCurrentId());
        article.setStatus(ArticleConstant.STATUS_PUBLISHED);
        LocalDateTime now = LocalDateTime.now();
        article.setCreatedTime(now);
        article.setUpdatedTime(now);

        articleMapper.save(article);
        return article.getId();
    }

    /**
     * 文章列表查询
     *
     * @param articleListDTO
     * @return
     */
    @Override
    public PageResult articleList(ArticleListDTO articleListDTO) {
        //通过pagehelper给mybatis自动添加查询范围
        PageHelper.startPage(articleListDTO.getPage(), articleListDTO.getPageSize());

        //Page<>是由pagehelper封装的返回集合
        Set<Long> articleIds = resolveArticleIdsByTagId(articleListDTO.getTagId());
        Page<Article> pages = articleMapper.pageQueryPublished(articleListDTO, articleIds);
        List<ArticlePreviewVO> previewVOS = new ArrayList<>();
        pages.getResult().forEach(article -> {
            ArticlePreviewVO articlePreviewVO = new ArticlePreviewVO();
            BeanUtils.copyProperties(article, articlePreviewVO);
            articlePreviewVO.setAuthorName(userMapper.getNameById(article.getAuthorId()));
            if (articlePreviewVO.getSummary() == null || articlePreviewVO.getSummary().isBlank()) {
                articlePreviewVO.setSummary(ArticleUtil.generateSummary(article.getContent()));
            }
            previewVOS.add(articlePreviewVO);
        });
        log.info("文章数{}",pages.getTotal());
        log.info("文章集{}",previewVOS);
        return new PageResult(pages.getTotal(), previewVOS);
    }

    /**
     * 获取文章详细
     *
     * @param articleId
     * @return
     */
    @Override
    public ArticleDetailVO getArticleDetail(Long articleId) {
        Article article = articleMapper.getPublishedById(articleId);
        if (article == null) {
            throw BusinessException.notFound("该文章不存在");
        }
        ArticleDetailVO articleDetailVO = new ArticleDetailVO();
        BeanUtils.copyProperties(article, articleDetailVO);
        User author = userMapper.getById(article.getAuthorId());
        if (author != null) {
            UserProfileVO authorProfile = new UserProfileVO();
            BeanUtils.copyProperties(author, authorProfile);
            articleDetailVO.setAuthor(authorProfile);
        }
        return articleDetailVO;
    }

    /**
     * 编辑文章
     * @param articleEditDTO
     */
    @Override
    public void editArticle(ArticleEditDTO articleEditDTO) {
        Article existingArticle = articleMapper.getById(articleEditDTO.getId());
        if (existingArticle == null) {
            throw BusinessException.notFound("该文章不存在");
        }
        validateArticleOwnership(existingArticle);

        Article article = new Article();
        BeanUtils.copyProperties(articleEditDTO, article);
        article.setAuthorId(existingArticle.getAuthorId());
        article.setUpdatedTime(LocalDateTime.now());
        log.info("{}",article);
        articleMapper.update(article);
    }

    /**
     * 用户删除文章
     * @param articleId
     */
    @Transactional
    @Override
    public void deleteArticle(Long articleId) {
        Article article = articleMapper.getById(articleId);
        if (article == null) {
            throw BusinessException.notFound("该文章不存在");
        }
        validateArticleOwnership(article);
        article = new Article();
        article.setId(articleId);
        article.setStatus(ArticleConstant.STATUS_DELETED);

        int updated = articleMapper.update(article);
        if (updated == 0) {
            throw new BusinessException("文章状态更新失败");
        }

        commentMapper.updateStatus(articleId, mapArticleStatusToCommentStatus(ArticleConstant.STATUS_DELETED));
    }

    @Transactional
    @Override
    public void editStatus(Long id) {
        validateAdminAccess();
        Article article = articleMapper.getById(id);
        if (article == null) {
            throw BusinessException.notFound("该文章不存在");
        }
        Integer currentStatus = article.getStatus();
        Integer newStatus;

        if (ArticleConstant.STATUS_PUBLISHED.equals(currentStatus)) {
            newStatus = ArticleConstant.STATUS_DRAFT;
        } else if (ArticleConstant.STATUS_DRAFT.equals(currentStatus)) {
            newStatus = ArticleConstant.STATUS_PUBLISHED;
        } else {
            throw new BusinessException("文章状态非法，无法切换");
        }

        article.setStatus(newStatus);

        int updated = articleMapper.update(article);
        if (updated == 0) {
            throw new BusinessException("文章状态更新失败");
        }
        commentMapper.updateStatus(id, mapArticleStatusToCommentStatus(newStatus));
    }

    @Override
    public PageResult articleAdminList(ArticleAdminListDTO articleAdminListDTO) {
        validateAdminAccess();
        //通过pagehelper给mybatis自动添加查询范围
        PageHelper.startPage(articleAdminListDTO.getPage(), articleAdminListDTO.getPageSize());

        //Page<>是由pagehelper封装的返回集合
        Set<Long> articleIds = resolveArticleIdsByTagId(articleAdminListDTO.getTagId());
        Page<Article> pages = articleMapper.pageQueryAdmin(articleAdminListDTO, articleIds);
        List<ArticleAdminListVO> previewVOS = buildAdminArticleList(pages.getResult());
        log.info("文章数{}",pages.getTotal());
        log.info("文章集{}",previewVOS);
        return new PageResult(pages.getTotal(), previewVOS);
    }

    private Set<Long> resolveArticleIdsByTagId(Long tagId) {
        if (tagId == null) {
            return null;
        }
        List<Long> articleIds = articleTagMapper.listArticleIdsByTagId(tagId);
        return new LinkedHashSet<>(articleIds);
    }

    private List<ArticleAdminListVO> buildAdminArticleList(List<Article> articles) {
        if (articles.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> authorIds = articles.stream().map(Article::getAuthorId).collect(Collectors.toSet());
        Set<Long> categoryIds = articles.stream()
                .map(Article::getCategoryId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
        Set<Long> articleIds = articles.stream().map(Article::getId).collect(Collectors.toSet());

        Map<Long, String> authorNameMap = userMapper.getUsersByIds(authorIds).stream()
                .collect(Collectors.toMap(User::getId, User::getUsername));
        Map<Long, String> categoryNameMap = categoryIds.isEmpty()
                ? Collections.emptyMap()
                : categoryMapper.getByIds(categoryIds).stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));
        Map<Long, ArticleStats> statsMap = articleStatsMapper.getByArticleIds(articleIds).stream()
                .collect(Collectors.toMap(ArticleStats::getArticleId, Function.identity()));

        List<ArticleAdminListVO> records = new ArrayList<>();
        for (Article article : articles) {
            ArticleAdminListVO vo = new ArticleAdminListVO();
            BeanUtils.copyProperties(article, vo);
            vo.setAuthorName(authorNameMap.get(article.getAuthorId()));
            vo.setCategoryName(categoryNameMap.get(article.getCategoryId()));

            ArticleStats stats = statsMap.get(article.getId());
            if (stats != null) {
                vo.setViewCount(stats.getViewCount());
                vo.setCommentCount(stats.getCommentCount());
            }

            records.add(vo);
        }
        return records;
    }


    private Integer mapArticleStatusToCommentStatus(Integer articleStatus) {
        if (ArticleConstant.STATUS_DELETED.equals(articleStatus)) {
            return CommentStatusConstant.STATUS_DELETED;
        }
        if (ArticleConstant.STATUS_PUBLISHED.equals(articleStatus)) {
            return CommentStatusConstant.STATUS_NORMAL;
        }
        if (ArticleConstant.STATUS_DRAFT.equals(articleStatus)) {
            return CommentStatusConstant.STATUS_HIDDEN;
        }
        throw new BusinessException("文章状态非法，无法映射评论状态");
    }

    private void validateArticleOwnership(Article article) {
        Long currentUserId = BaseContext.getCurrentId();
        if (currentUserId == null || !currentUserId.equals(article.getAuthorId())) {
            throw new ForbiddenException("无权操作他人的文章");
        }
    }

    private void validateAdminAccess() {
        if (!RoleConstant.ADMIN.equals(BaseContext.getCurrentRole())) {
            throw new ForbiddenException("无管理员权限");
        }
    }

}
