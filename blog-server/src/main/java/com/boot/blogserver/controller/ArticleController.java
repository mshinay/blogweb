package com.boot.blogserver.controller;

import com.blog.dto.ArticleEditDTO;
import com.blog.dto.ArticleAdminListDTO;
import com.blog.dto.ArticleListDTO;
import com.blog.dto.ArticleUploadDTO;
import com.blog.result.PageResult;
import com.blog.result.Result;
import com.blog.vo.ArticleDetailVO;
import com.boot.blogserver.service.ArticleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/article")
@Slf4j
public class ArticleController {

    @Autowired
    private ArticleService articleService;


    /**
     * 上传文章
     * @param articleUploadDTO
     * @return
     */
    @PostMapping("/upload")
    public Result uploadArticle(@RequestBody ArticleUploadDTO articleUploadDTO) {
        log.info("文章上传{}", articleUploadDTO);
        Long id = articleService.uploadArticle(articleUploadDTO);
        return Result.success(id);
    }

    /**
     * 列表查询
     * @param articleListDTO
     * @return
     */
    @GetMapping("/list")
    public Result<PageResult> listArticles(ArticleListDTO articleListDTO) {
        log.info("分页查询{}", articleListDTO);
        PageResult results = articleService.articleList(articleListDTO);
        return Result.success(results);
    }

    /**
     * 获取文章详细
     * @param articleId
     * @return
     */
    @GetMapping("/detail/{articleId}")
    public Result<ArticleDetailVO> showArticle(@PathVariable Long articleId) {
        log.info("查询文章{}", articleId);
        ArticleDetailVO articleDetailVO = articleService.getArticleDetail(articleId);
       return Result.success(articleDetailVO);
    }

    /**
     * 文章查询
     * @param articleListDTO
     * @return
     */
   @GetMapping("/search")
    public Result<PageResult> searchArticles(ArticleListDTO articleListDTO) {
       log.info("search查询{}", articleListDTO);
       PageResult results = articleService.articleList(articleListDTO);
       return Result.success(results);
   }

    /**
     * 用户文章查询
     * @param articleListDTO
     * @return
     */
   @GetMapping("/user")
   public Result<PageResult> userArticles(ArticleListDTO articleListDTO) {
        log.info("用户查询{}", articleListDTO);
       PageResult results = articleService.articleList(articleListDTO);
       return Result.success(results);
   }

    /**
     * 用户文章搜索查询
     * @param articleListDTO
     * @return
     */
    @GetMapping("/user/search")
    public Result<PageResult> userSearchArticles(ArticleListDTO articleListDTO) {
        log.info("用户搜索{}", articleListDTO);
        PageResult results = articleService.articleList(articleListDTO);
        return Result.success(results);
    }

    /**
     * 修改文章
     * @param articleEditDTO
     * @return
     */
    @PostMapping("/edit")
    public Result uploadArticle(@RequestBody ArticleEditDTO articleEditDTO) {
        log.info("文章更新{}", articleEditDTO);
        articleService.editArticle(articleEditDTO);
        return Result.success();
    }



    @DeleteMapping("/{articleId}")
    public Result deleteArticle(@PathVariable Long articleId) {
        log.info("用户删除{}", articleId);
        articleService.deleteArticle(articleId);
        return Result.success();
    }

    @GetMapping("/admin/list")
    public Result<PageResult> adminListArticles(ArticleAdminListDTO articleAdminListDTO) {
        log.info("管理员文章查询{}", articleAdminListDTO);
        PageResult results = articleService.articleAdminList(articleAdminListDTO);
        return Result.success(results);
    }

    /**
     * 管理员删除或恢复文章
     * @param id
     * @return
     */
    @PatchMapping("/admin/status/{id}")
    public Result adminEditStatus(@PathVariable Long id) {
        log.info("管理员修改文章状况{}", id);
        articleService.editStatus(id);
        return Result.success();
    }

    /**
     * 管理员文章查询
     * @param articleListDTO
     * @return
     */
    @GetMapping("/admin/search")
    public Result<PageResult> adminSearchArticles(ArticleAdminListDTO articleAdminListDTO) {
        log.info("管理员查询{}", articleAdminListDTO);
        PageResult results = articleService.articleAdminList(articleAdminListDTO);
        return Result.success(results);
    }

}
