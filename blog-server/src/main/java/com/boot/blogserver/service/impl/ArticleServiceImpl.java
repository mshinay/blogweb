package com.boot.blogserver.service.impl;

import com.blog.constant.ArticleConstant;
import com.blog.constant.CommentStatusConstant;
import com.blog.context.BaseContext;
import com.blog.dto.ArticleAdminListDTO;
import com.blog.dto.ArticleEditDTO;
import com.blog.dto.ArticleListDTO;
import com.blog.dto.ArticleUploadDTO;
import com.blog.entry.Article;
import com.blog.entry.User;
import com.blog.result.PageResult;
import com.blog.utils.ArticleUtil;
import com.blog.vo.ArticleDetailVO;
import com.blog.vo.ArticlePreviewVO;
import com.blog.vo.UserProfileVO;
import com.boot.blogserver.mapper.ArticleMapper;
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
import java.util.List;

@Service
@Slf4j
public class ArticleServiceImpl implements ArticleService {

    @Autowired
    private ArticleMapper articleMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private CommentMapper commentMapper;

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
        Page<Article> pages = articleMapper.pageQueryPublished(articleListDTO, null);
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
        Article article = articleMapper.getById(articleId);
        if (article == null) {
            throw new RuntimeException("该文章不存在");
        }
        if (!ArticleConstant.STATUS_PUBLISHED.equals(article.getStatus())) {
            throw new RuntimeException("该文章不可见");
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
        Article article = new Article();
        BeanUtils.copyProperties(articleEditDTO, article);
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
            throw new RuntimeException("该文章不存在");
        }
        article = new Article();
        article.setId(articleId);
        article.setStatus(ArticleConstant.STATUS_DELETED);

        int updated = articleMapper.update(article);
        if (updated == 0) {
            throw new RuntimeException("文章状态更新失败");
        }

        commentMapper.updateStatus(articleId, mapArticleStatusToCommentStatus(ArticleConstant.STATUS_DELETED));
    }

    @Transactional
    @Override
    public void editStatus(Long id) {
        Article article = articleMapper.getById(id);
        if (article == null) {
            throw new RuntimeException("该文章不存在");
        }
        Integer currentStatus = article.getStatus();
        Integer newStatus;

        if (ArticleConstant.STATUS_PUBLISHED.equals(currentStatus)) {
            newStatus = ArticleConstant.STATUS_DRAFT;
        } else if (ArticleConstant.STATUS_DRAFT.equals(currentStatus)) {
            newStatus = ArticleConstant.STATUS_PUBLISHED;
        } else {
            throw new RuntimeException("文章状态非法，无法切换");
        }

        article.setStatus(newStatus);

        int updated = articleMapper.update(article);
        if (updated == 0) {
            throw new RuntimeException("文章状态更新失败");
        }
        commentMapper.updateStatus(id, mapArticleStatusToCommentStatus(newStatus));
    }

    @Override
    public PageResult articleAdminList(ArticleAdminListDTO articleAdminListDTO) {
        //通过pagehelper给mybatis自动添加查询范围
        PageHelper.startPage(articleAdminListDTO.getPage(), articleAdminListDTO.getPageSize());

        //Page<>是由pagehelper封装的返回集合
        Page<Article> pages = articleMapper.pageQueryAdmin(articleAdminListDTO, null);
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
        throw new RuntimeException("文章状态非法，无法映射评论状态");
    }

}
