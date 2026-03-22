package com.boot.blogserver.controller;

import com.blog.dto.ArticleEditDTO;
import com.blog.dto.ArticleAdminListDTO;
import com.blog.dto.ArticleListDTO;
import com.blog.dto.ArticleUploadDTO;
import com.blog.exception.BusinessException;
import com.blog.result.PageResult;
import com.blog.result.Result;
import com.blog.vo.ArticleDetailVO;
import com.boot.blogserver.service.ArticleService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@Validated
public class ArticleController {

    @Autowired
    private ArticleService articleService;


    /**
     * 上传文章
     * @param articleUploadDTO
     * @return
     */
    @PostMapping("/articles")
    public Result uploadArticle(@Valid @RequestBody ArticleUploadDTO articleUploadDTO) {
        log.info("文章上传{}", articleUploadDTO);
        Long id = articleService.uploadArticle(articleUploadDTO);
        return Result.success(id);
    }

    /**
     * 列表查询
     * @param articleListDTO
     * @return
     */
    //@GetMapping("/list")
    @GetMapping("/articles")
    public Result<PageResult> listArticles(@Valid ArticleListDTO articleListDTO) {
        log.info("分页查询{}", articleListDTO);
        PageResult results = articleService.articleList(articleListDTO);
        return Result.success(results);
    }

    /**
     * 获取文章详细
     * @param articleId
     * @return
     */
    @GetMapping("/articles/{articleId:\\d+}")
    public Result<ArticleDetailVO> showArticle(@Positive(message = "文章ID必须大于0") @PathVariable Long articleId) {
        log.info("查询文章{}", articleId);
        ArticleDetailVO articleDetailVO = articleService.getArticleDetail(articleId);
       return Result.success(articleDetailVO);
    }

    /**
     * 文章查询
     * @param articleListDTO
     * @return
     */
   @GetMapping("/articles/search")
    public Result<PageResult> searchArticles(@Valid ArticleListDTO articleListDTO) {
       log.info("search查询{}", articleListDTO);
       PageResult results = articleService.articleList(articleListDTO);
       return Result.success(results);
   }

    /**
     * 用户文章查询
     * @param articleListDTO
     * @return
     */
   @GetMapping("/articles/user")
   public Result<PageResult> userArticles(@Valid ArticleListDTO articleListDTO) {
        log.info("用户查询{}", articleListDTO);
       PageResult results = articleService.userArticleList(articleListDTO);
       return Result.success(results);
   }

    /**
     * 用户文章搜索查询
     * @param articleListDTO
     * @return
     */
    @GetMapping("/articles/user/search")
    public Result<PageResult> userSearchArticles(@Valid ArticleListDTO articleListDTO) {
        log.info("用户搜索{}", articleListDTO);
        PageResult results = articleService.userArticleList(articleListDTO);
        return Result.success(results);
    }

    @PutMapping("/articles/{articleId:\\d+}")
    public Result updateArticle(@Positive(message = "文章ID必须大于0") @PathVariable Long articleId,
                                @Valid @RequestBody ArticleEditDTO articleEditDTO) {
        if (articleEditDTO.getId() != null && !articleId.equals(articleEditDTO.getId())) {
            throw new BusinessException("路径文章ID与请求体文章ID不一致");
        }
        articleEditDTO.setId(articleId);
        return editArticle(articleEditDTO);
    }

    private Result editArticle(ArticleEditDTO articleEditDTO) {
        log.info("文章更新{}", articleEditDTO);
        articleService.editArticle(articleEditDTO);
        return Result.success();
    }



    @DeleteMapping("/articles/{articleId:\\d+}")
    public Result deleteArticle(@Positive(message = "文章ID必须大于0") @PathVariable Long articleId) {
        log.info("用户删除{}", articleId);
        articleService.deleteArticle(articleId);
        return Result.success();
    }

    @PatchMapping("/articles/{articleId:\\d+}/status")
    public Result editStatus(@Positive(message = "文章ID必须大于0") @PathVariable Long articleId) {
        log.info("用户修改文章状况{}", articleId);
        articleService.editStatus(articleId);
        return Result.success();
    }

    @GetMapping("/admin/articles")
    public Result<PageResult> adminListArticles(@Valid ArticleAdminListDTO articleAdminListDTO) {
        log.info("管理员文章查询{}", articleAdminListDTO);
        PageResult results = articleService.articleAdminList(articleAdminListDTO);
        return Result.success(results);
    }

    /**
     * 管理员删除或恢复文章
     * @param articleId
     * @return
     */
    @PatchMapping("/admin/articles/{articleId:\\d+}/status")
    public Result adminEditStatus(@Positive(message = "文章ID必须大于0") @PathVariable Long articleId) {
        log.info("管理员修改文章状况{}", articleId);
        articleService.adminEditStatus(articleId);
        return Result.success();
    }

}
