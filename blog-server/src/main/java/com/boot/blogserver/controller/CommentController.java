package com.boot.blogserver.controller;

import com.blog.dto.*;
import com.blog.exception.BusinessException;
import com.blog.result.PageResult;
import com.blog.result.Result;
import com.boot.blogserver.service.CommentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@Validated
public class CommentController {
    @Autowired
    private CommentService commentService;

    /**
     * 上传评论
     *
     * @param commentUploadDTO
     */
    @PostMapping({"/comments", "/comments/upload"})
    public Result uploadComment(@Valid @RequestBody CommentUploadDTO commentUploadDTO) {
        log.info("评论上传{}", commentUploadDTO);
        commentService.uploadComment(commentUploadDTO);
        return Result.success();
    }

    /**
     * 列表查询
     * @param commentListDTO
     * @return
     */
    @GetMapping({"/comments", "/comments/list"})
    public Result<PageResult> listComment(@Valid CommentListDTO commentListDTO) {
        log.info("分页查询{}", commentListDTO);
        PageResult results = commentService.commentList(commentListDTO);
        return Result.success(results);
    }

    /**
     * 用户评论查询
     * @param commentListDTO
     * @return
     */
    @GetMapping("/comments/user")
    public Result<PageResult> userComment(@Valid CommentListDTO commentListDTO) {
        log.info("用户评论查询{}", commentListDTO);
        PageResult results = commentService.commentList(commentListDTO);
        return Result.success(results);
    }

    /**
     * 列表查询
     * @param commentListDTO
     * @return
     */
    @GetMapping("/comments/user/search")
    public Result<PageResult> userSearchComment(@Valid CommentListDTO commentListDTO) {
        log.info("用户评论搜索{}", commentListDTO);
        PageResult results = commentService.commentList(commentListDTO);
        return Result.success(results);
    }

    /**
     * 用户删除发表的评论
     * @param id
     * @return
     */
    @DeleteMapping("/comments/{id}")
    public Result deleteComment(@Positive(message = "评论ID必须大于0") @PathVariable Long id) {
        log.info("用户删除评论{}", id);
        commentService.deleteComment(id);
        return Result.success();
    }

    @PutMapping("/comments/update")
    public Result updateCommentLegacy(@Valid @RequestBody CommentUpdateDTO commentUpdateDTO) {
        if (commentUpdateDTO.getId() == null) {
            throw new BusinessException(Result.VALIDATION_ERROR, 400, "评论ID不能为空");
        }
        return updateComment(commentUpdateDTO);
    }

    @PutMapping("/comments/{commentId}")
    public Result updateCommentById(@Positive(message = "评论ID必须大于0") @PathVariable Long commentId,
                                    @Valid @RequestBody CommentUpdateDTO commentUpdateDTO) {
        if (commentUpdateDTO.getId() != null && !commentId.equals(commentUpdateDTO.getId())) {
            throw new BusinessException("路径评论ID与请求体评论ID不一致");
        }
        commentUpdateDTO.setId(commentId);
        return updateComment(commentUpdateDTO);
    }

    private Result updateComment(CommentUpdateDTO commentUpdateDTO) {
        log.info("用户评论更新{}", commentUpdateDTO);
        commentService.updateComment(commentUpdateDTO);
        return Result.success();
    }

    /**
     * 管理员评论查询
     * @param commentAdminListDTO
     * @return
     */
    @GetMapping({"/admin/comments", "/comments/admin/search", "/comments/admin/list"})
    public Result<PageResult> adminListManageComment(@Valid CommentAdminListDTO commentAdminListDTO) {
        log.info("管理员评论平铺查询{}", commentAdminListDTO);
        PageResult results = commentService.commentAdminList(commentAdminListDTO);
        return Result.success(results);
    }

    @PatchMapping({"/admin/comments/{commentId}/status", "/comments/admin/status/{commentId}"})
    public Result adminEditStatus(@Positive(message = "评论ID必须大于0") @PathVariable Long commentId) {
        log.info("管理员修改评论状况{}", commentId);
        commentService.editStatus(commentId);
        return Result.success();
    }


}
