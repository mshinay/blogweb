package com.boot.blogserver.service;

import com.blog.dto.ArticleEditDTO;
import com.blog.dto.ArticleAdminListDTO;
import com.blog.dto.ArticleListDTO;
import com.blog.dto.ArticleUploadDTO;
import com.blog.result.PageResult;
import com.blog.vo.ArticleDetailVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


public interface ArticleService {

    /**
     * 上传文章
     * @param articleUploadDTO
     */
    Long uploadArticle(ArticleUploadDTO articleUploadDTO);

    /**
     * 列表查询
     * @param articleListDTO
     * @return
     */
    PageResult articleList(ArticleListDTO articleListDTO);

    /**
     * 获取文章详细
     * @param articleId
     * @return
     */
    ArticleDetailVO getArticleDetail(Long articleId);

    /**
     * 修改文章
     * @param articleEditDTO
     */
    void editArticle(ArticleEditDTO articleEditDTO);

    /**
     * 删除文章
     * @param articleId
     */
    void deleteArticle(Long articleId);

    /**
     * 用户修改文章状况
     * @param articleId
     */
    void editStatus(Long articleId);


    /**
     * 管理员修改文章状况
     * @param id
     */
    void adminEditStatus(Long id);

    /**
     * 管理员列表查询
     * @param articleAdminListDTO
     * @return
     */
    PageResult articleAdminList(ArticleAdminListDTO articleAdminListDTO);

     PageResult userArticleList(ArticleListDTO articleListDTO);

     ArticleDetailVO getArticleDetailWithoutRedis(Long articleId);
}
