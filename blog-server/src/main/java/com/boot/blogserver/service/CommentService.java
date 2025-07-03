package com.boot.blogserver.service;

import com.blog.dto.CommentListDTO;
import com.blog.dto.CommentUpdateDTO;
import com.blog.dto.CommentUploadDTO;
import com.blog.result.PageResult;

public interface CommentService {


    /**
     * 上传评论
     * @param commentUploadDTO
     */
    void uploadComment(CommentUploadDTO commentUploadDTO);

    /**
     * 列表查询
     * @param commentListDTO
     * @return
     */
    PageResult commentList(CommentListDTO commentListDTO);

    /**
     * 用户删除发表评论
     * @param id
     */
    void deleteComment(Long id);

    /**
     * 用户更新评论
     * @param commentUpdateDTO
     */
    void updateComment(CommentUpdateDTO commentUpdateDTO);

    /**
     * 管理员列表查询
     * @param commentListDTO
     * @return
     */
    PageResult commentAdminList(CommentListDTO commentListDTO);

    /**
     * 管理员删除或恢复评论
     * @param id
     */
    void editStatus(Long id);
}
