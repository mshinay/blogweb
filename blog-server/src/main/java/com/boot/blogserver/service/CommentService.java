package com.boot.blogserver.service;

import com.blog.dto.CommentAdminListDTO;
import com.blog.dto.CommentListDTO;
import com.blog.dto.CommentUpdateDTO;
import com.blog.dto.CommentUploadDTO;
import com.blog.dto.CommentUserHistoryQueryDTO;
import com.blog.entry.Comment;
import com.blog.result.PageResult;
import com.blog.vo.CommentTreeVO;

import java.util.List;

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
     * 当前登录用户评论历史
     * @param commentUserHistoryQueryDTO
     * @return
     */
    PageResult currentUserCommentHistory(CommentUserHistoryQueryDTO commentUserHistoryQueryDTO);

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
     * @param commentAdminListDTO
     * @return
     */
    PageResult commentAdminList(CommentAdminListDTO commentAdminListDTO);

    /**
     * 管理员删除或恢复评论
     * @param id
     */
    void editStatus(Long id);

    public List<CommentTreeVO> buildCommentTreeVOs(List<Comment> rootComments);
}
