package com.boot.blogserver.controller;

import com.blog.dto.*;
import com.blog.result.PageResult;
import com.blog.result.Result;
import com.boot.blogserver.service.CommentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/comment")
@Slf4j
public class CommentController {
    @Autowired
    private CommentService commentService;

    /**
     * 上传评论
     *
     * @param commentUploadDTO
     */
    @PostMapping("/upload")
    public Result uploadComment(@RequestBody CommentUploadDTO commentUploadDTO) {
        log.info("评论上传{}", commentUploadDTO);
        commentService.uploadComment(commentUploadDTO);
        return Result.success();
    }

    /**
     * 列表查询
     * @param commentListDTO
     * @return
     */
    @GetMapping("/list")
    public Result<PageResult> listComment(CommentListDTO commentListDTO) {
        log.info("分页查询{}", commentListDTO);
        PageResult results = commentService.commentList(commentListDTO);
        return Result.success(results);
    }

    /**
     * 用户评论查询
     * @param commentListDTO
     * @return
     */
    @GetMapping("/user")
    public Result<PageResult> userComment(CommentListDTO commentListDTO) {
        log.info("用户评论查询{}", commentListDTO);
        PageResult results = commentService.commentList(commentListDTO);
        return Result.success(results);
    }

    /**
     * 列表查询
     * @param commentListDTO
     * @return
     */
    @GetMapping("/user/search")
    public Result<PageResult> userSearchComment(CommentListDTO commentListDTO) {
        log.info("用户评论搜索{}", commentListDTO);
        PageResult results = commentService.commentList(commentListDTO);
        return Result.success(results);
    }

    /**
     * 用户删除发表的评论
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    public Result deleteComment(@PathVariable Long id) {
        log.info("用户删除评论{}", id);
        commentService.deleteComment(id);
        return Result.success();
    }

    @PutMapping("/update")
    public Result updateComment(@RequestBody CommentUpdateDTO commentUpdateDTO) {
        log.info("用户评论更新{}", commentUpdateDTO);
        commentService.updateComment(commentUpdateDTO);
        return Result.success();
    }

    /**
     * 管理员评论查询
     * @param commentListDTO
     * @return
     */
    @GetMapping("/admin/search")
    public Result<PageResult> adminSearchManageComment(CommentListDTO commentListDTO) {
        log.info("管理员评论聚合查询{}", commentListDTO);
        PageResult results = commentService.commentAdminManageList(commentListDTO);
        return Result.success(results);
    }

    @GetMapping("/admin/list")
    public Result<PageResult> adminListManageComment(CommentListDTO commentListDTO) {
        log.info("管理员评论聚合列表{}", commentListDTO);
        PageResult results = commentService.commentAdminManageList(commentListDTO);
        return Result.success(results);
    }

    @PatchMapping("/admin/status/{id}")
    public Result adminEditStatus(@PathVariable Long id) {
        log.info("管理员修改评论状况{}", id);
        commentService.editStatus(id);
        return Result.success();
    }


}
