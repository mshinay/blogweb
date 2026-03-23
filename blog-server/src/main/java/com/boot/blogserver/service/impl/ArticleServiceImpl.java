package com.boot.blogserver.service.impl;

import com.blog.constant.ArticleConstant;
import com.blog.constant.CategoryStatusConstant;
import com.blog.constant.CommentStatusConstant;
import com.blog.constant.RoleConstant;
import com.blog.constant.TagStatusConstant;
import com.blog.context.BaseContext;
import com.blog.dto.ArticleAdminListDTO;
import com.blog.dto.ArticleEditDTO;
import com.blog.dto.ArticleListDTO;
import com.blog.dto.ArticleUploadDTO;
import com.blog.entry.Article;
import com.blog.entry.ArticleTag;
import com.blog.entry.ArticleStats;
import com.blog.entry.Category;
import com.blog.entry.Comment;
import com.blog.entry.Tag;
import com.blog.entry.User;
import com.blog.exception.BusinessException;
import com.blog.exception.ForbiddenException;
import com.blog.result.PageResult;
import com.blog.utils.ArticleUtil;
import com.blog.vo.ArticleAdminListVO;
import com.blog.vo.ArticleDetailVO;
import com.blog.vo.ArticlePreviewVO;
import com.blog.vo.ArticleStatsVO;
import com.blog.vo.CategoryVO;
import com.blog.vo.CommentPreviewVO;
import com.blog.vo.CommentTreeVO;
import com.blog.vo.TagVO;
import com.blog.vo.UserProfileVO;
import com.boot.blogserver.mapper.ArticleMapper;
import com.boot.blogserver.mapper.ArticleStatsMapper;
import com.boot.blogserver.mapper.ArticleTagMapper;
import com.boot.blogserver.mapper.CategoryMapper;
import com.boot.blogserver.mapper.CommentMapper;
import com.boot.blogserver.mapper.TagMapper;
import com.boot.blogserver.mapper.UserMapper;
import com.boot.blogserver.service.ArticleService;
import com.boot.blogserver.service.CommentService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    @Autowired
    private TagMapper tagMapper;
    @Autowired
    private CommentService commentService;

    /**
     * 上传文章
     *
     * @param articleUploadDTO
     */
    @Transactional
    @Override
    public Long uploadArticle(ArticleUploadDTO articleUploadDTO) {
        if(Objects.equals(articleUploadDTO.getStatus(), ArticleConstant.STATUS_DELETED)){
            throw new BusinessException("文章状态非法");
        }
        validateCategoryForBinding(articleUploadDTO.getCategoryId());
        List<Long> tagIds = validateTagsForBinding(articleUploadDTO.getTagIds());
        Article article = new Article();
        BeanUtils.copyProperties(articleUploadDTO, article);
        article.setAuthorId(BaseContext.getCurrentId());
        LocalDateTime now = LocalDateTime.now();
        article.setUpdatedTime(now);
        article.setCreatedTime(now);
        if(Objects.equals(article.getStatus(), ArticleConstant.STATUS_PUBLISHED)){article.setPublishTime(now);}
        articleMapper.save(article);
        ArticleStats articleStats = new ArticleStats();
        //创建文章状态
        articleStats.setArticleId(article.getId());
        articleStatsMapper.save(articleStats);
        replaceArticleTags(article.getId(), tagIds);
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
        List<ArticlePreviewVO> previewVOS = buildArticlePreviewList(
                pages.getResult().stream()
                        .filter(article -> ArticleConstant.STATUS_PUBLISHED.equals(article.getStatus()))
                        .toList()
        );
        log.info("文章数{}",pages.getTotal());
        log.info("文章集{}",previewVOS.size());
        return new PageResult(pages.getTotal(), previewVOS);
    }



    @Override
    public PageResult userArticleList(ArticleListDTO articleListDTO) {
        //通过pagehelper给mybatis自动添加查询范围
        articleListDTO.setAuthorId(BaseContext.getCurrentId());
        PageHelper.startPage(articleListDTO.getPage(), articleListDTO.getPageSize());

        //Page<>是由pagehelper封装的返回集合
        Set<Long> articleIds = resolveArticleIdsByTagId(articleListDTO.getTagId());
        Page<Article> pages = articleMapper.authorPageQuery(articleListDTO, articleIds);
        List<ArticlePreviewVO> previewVOS = buildArticlePreviewList(
                pages.getResult().stream()
                        .filter(article -> ArticleConstant.STATUS_PUBLISHED.equals(article.getStatus())
                                || ArticleConstant.STATUS_DRAFT.equals(article.getStatus()))
                        .toList()
        );

        log.info("文章数{}",pages.getTotal());

        //log.info("文章集{}",previewVOS.size());
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
        if(!article.getStatus().equals(ArticleConstant.STATUS_PUBLISHED)){
            throw new ForbiddenException("该文章无法访问");
        }
        ArticleDetailVO articleDetailVO = new ArticleDetailVO();
        BeanUtils.copyProperties(article, articleDetailVO);
        articleDetailVO.setAuthor(buildUserProfile(article.getAuthorId()));
        if (article.getCategoryId() != null) {
            Category category = categoryMapper.getById(article.getCategoryId());
            if (category != null) {
                articleDetailVO.setCategory(toCategoryVO(category));
            }
        }
        articleDetailVO.setTags(buildTagListByArticleIds(Collections.singleton(articleId)).getOrDefault(articleId, Collections.emptyList()));
        articleDetailVO.setStats(buildArticleStats(articleId));
        List<Comment> rootComments = commentMapper.listPublishedRootByArticleId(articleId);
        articleDetailVO.setComments(commentService.buildCommentTreeVOs(rootComments));
        return articleDetailVO;
    }

    /**
     * 编辑文章
     * @param articleEditDTO
     */
    @Transactional
    @Override
    public void editArticle(ArticleEditDTO articleEditDTO) {
        Article existingArticle = articleMapper.getById(articleEditDTO.getId());
        if (existingArticle == null) {
            throw BusinessException.notFound("该文章不存在");
        }
        validateArticleOwnership(existingArticle);
        validateCategoryForBinding(articleEditDTO.getCategoryId());
        List<Long> tagIds = validateTagsForBinding(articleEditDTO.getTagIds());
        Integer currentStatus = existingArticle.getStatus();
        Article article = new Article();
        BeanUtils.copyProperties(articleEditDTO, article);
        article.setAuthorId(existingArticle.getAuthorId());
        article.setUpdatedTime(LocalDateTime.now());
        log.info("{}",article);
        article.setStatus(currentStatus);
        articleMapper.update(article);
        replaceArticleTags(article.getId(), tagIds);
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
    public void adminEditStatus(Long id) {
        validateAdminAccess();
        Article article = articleMapper.getById(id);
        if (article == null) {
            throw BusinessException.notFound("该文章不存在");
        }
        Integer currentStatus = article.getStatus();
        Integer newStatus;

        if (ArticleConstant.STATUS_PUBLISHED.equals(currentStatus)) {
            newStatus = ArticleConstant.STATUS_DELETED;
        } else if (ArticleConstant.STATUS_DELETED.equals(currentStatus)) {
            newStatus = ArticleConstant.STATUS_PUBLISHED;
            if(article.getPublishTime() == null){
                article.setPublishTime(LocalDateTime.now());
            }
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

    @Transactional
    @Override
    public void editStatus(Long id) {

        Article article = articleMapper.getById(id);

        if (article == null) {
            throw BusinessException.notFound("该文章不存在");
        }
        validateArticleOwnership(article);
        Integer currentStatus = article.getStatus();
        Integer newStatus;

        if (ArticleConstant.STATUS_PUBLISHED.equals(currentStatus)) {
            newStatus = ArticleConstant.STATUS_DRAFT;
        } else if (ArticleConstant.STATUS_DRAFT.equals(currentStatus)) {
            newStatus = ArticleConstant.STATUS_PUBLISHED;
            LocalDateTime now = LocalDateTime.now();
            article.setPublishTime(now);
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
        Map<Long, List<TagVO>> articleTagMap = buildTagListByArticleIds(articleIds);

        List<ArticleAdminListVO> records = new ArrayList<>();
        for (Article article : articles) {
            ArticleAdminListVO vo = new ArticleAdminListVO();
            BeanUtils.copyProperties(article, vo);
            vo.setAuthorName(authorNameMap.get(article.getAuthorId()));
            vo.setCategoryName(categoryNameMap.get(article.getCategoryId()));
            vo.setTagList(articleTagMap.getOrDefault(article.getId(), Collections.emptyList()));

            ArticleStats stats = statsMap.get(article.getId());
            if (stats != null) {
                vo.setViewCount(stats.getViewCount());
                vo.setCommentCount(stats.getCommentCount());
            }

            records.add(vo);
        }
        return records;
    }

    private List<ArticlePreviewVO> buildArticlePreviewList(List<Article> articles) {
        if (articles.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> authorIds = articles.stream().map(Article::getAuthorId).collect(Collectors.toSet());
        Set<Long> categoryIds = articles.stream()
                .map(Article::getCategoryId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<Long> articleIds = articles.stream().map(Article::getId).collect(Collectors.toSet());

        Map<Long, String> authorNameMap = userMapper.getUsersByIds(authorIds).stream()
                .collect(Collectors.toMap(User::getId, User::getUsername));
        Map<Long, CategoryVO> categoryMap = categoryIds.isEmpty()
                ? Collections.emptyMap()
                : categoryMapper.getByIds(categoryIds).stream()
                .collect(Collectors.toMap(Category::getId, this::toCategoryVO));
        Map<Long, ArticleStats> statsMap = articleStatsMapper.getByArticleIds(articleIds).stream()
                .collect(Collectors.toMap(ArticleStats::getArticleId, Function.identity()));
        Map<Long, List<TagVO>> articleTagMap = buildTagListByArticleIds(articleIds);

        List<ArticlePreviewVO> records = new ArrayList<>();
        for (Article article : articles) {
            ArticlePreviewVO articlePreviewVO = new ArticlePreviewVO();
            BeanUtils.copyProperties(article, articlePreviewVO);
            articlePreviewVO.setAuthorName(authorNameMap.get(article.getAuthorId()));

            CategoryVO categoryVO = categoryMap.get(article.getCategoryId());
            if (categoryVO != null) {
                articlePreviewVO.setCategoryName(categoryVO.getName());
                articlePreviewVO.setCategorySlug(categoryVO.getSlug());
            }
            articlePreviewVO.setTagList(articleTagMap.getOrDefault(article.getId(), Collections.emptyList()));

            if (articlePreviewVO.getSummary() == null || articlePreviewVO.getSummary().isBlank()) {
                articlePreviewVO.setSummary(ArticleUtil.generateSummary(article.getContent()));
            }

            ArticleStats stats = statsMap.get(article.getId());
            if (stats != null) {
                articlePreviewVO.setViewCount(stats.getViewCount());
                articlePreviewVO.setCommentCount(stats.getCommentCount());
            }

            records.add(articlePreviewVO);
        }
        return records;
    }

    private Map<Long, List<TagVO>> buildTagListByArticleIds(Set<Long> articleIds) {
        if (articleIds == null || articleIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<ArticleTag> articleTags = articleTagMapper.listByArticleIds(articleIds);
        if (articleTags.isEmpty()) {
            return Collections.emptyMap();
        }

        Set<Long> tagIds = articleTags.stream().map(ArticleTag::getTagId).collect(Collectors.toSet());
        Map<Long, TagVO> tagMap = tagMapper.getByIds(tagIds).stream()
                .collect(Collectors.toMap(Tag::getId, this::toTagVO));

        Map<Long, List<TagVO>> result = new LinkedHashMap<>();
        for (ArticleTag articleTag : articleTags) {
            TagVO tagVO = tagMap.get(articleTag.getTagId());
            if (tagVO == null) {
                continue;
            }
            result.computeIfAbsent(articleTag.getArticleId(), key -> new ArrayList<>()).add(tagVO);
        }
        return result;
    }

    private void validateCategoryForBinding(Long categoryId) {
        if (categoryId == null) {
            return;
        }

        Category category = categoryMapper.getById(categoryId);
        if (category == null) {
            throw BusinessException.notFound("分类不存在");
        }
        if (!CategoryStatusConstant.STATUS_ENABLED.equals(category.getStatus())) {
            throw new BusinessException("分类已禁用，无法绑定");
        }
    }

    private List<Long> validateTagsForBinding(List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return Collections.emptyList();
        }

        LinkedHashSet<Long> uniqueTagIds = new LinkedHashSet<>(tagIds);
        Map<Long, Tag> tagMap = tagMapper.getByIds(uniqueTagIds).stream()
                .collect(Collectors.toMap(Tag::getId, Function.identity()));

        for (Long tagId : uniqueTagIds) {
            Tag tag = tagMap.get(tagId);
            if (tag == null) {
                throw BusinessException.notFound("标签不存在");
            }
            if (!TagStatusConstant.STATUS_ENABLED.equals(tag.getStatus())) {
                throw new BusinessException("标签已禁用，无法绑定");
            }
        }
        return new ArrayList<>(uniqueTagIds);
    }

    private void replaceArticleTags(Long articleId, List<Long> tagIds) {
        articleTagMapper.deleteByArticleId(articleId);
        if (tagIds == null || tagIds.isEmpty()) {
            return;
        }

        List<ArticleTag> articleTags = tagIds.stream()
                .map(tagId -> ArticleTag.builder().articleId(articleId).tagId(tagId).build())
                .toList();
        articleTagMapper.saveBatch(articleTags);
    }

    private CategoryVO toCategoryVO(Category category) {
        CategoryVO categoryVO = new CategoryVO();
        BeanUtils.copyProperties(category, categoryVO);
        return categoryVO;
    }

    private TagVO toTagVO(Tag tag) {
        TagVO tagVO = new TagVO();
        BeanUtils.copyProperties(tag, tagVO);
        return tagVO;
    }

    private UserProfileVO buildUserProfile(Long userId) {
        if (userId == null) {
            return null;
        }
        User author = userMapper.getById(userId);
        if (author == null) {
            return null;
        }
        UserProfileVO authorProfile = new UserProfileVO();
        BeanUtils.copyProperties(author, authorProfile);
        return authorProfile;
    }

    private ArticleStatsVO buildArticleStats(Long articleId) {
        ArticleStats articleStats = articleStatsMapper.getByArticleId(articleId);
        ArticleStatsVO articleStatsVO = new ArticleStatsVO();
        if (articleStats == null) {
            articleStatsVO.setViewCount(0L);
            articleStatsVO.setLikeCount(0L);
            articleStatsVO.setCommentCount(0L);
            articleStatsVO.setFavoriteCount(0L);
            return articleStatsVO;
        }
        BeanUtils.copyProperties(articleStats, articleStatsVO);
        return articleStatsVO;
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
