package com.boot.blogserver.controller;

import com.blog.exception.BusinessException;
import com.blog.result.Result;
import com.blog.utils.AliOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@Slf4j
public class CommonController {

    //阿里云工具对象
    @Autowired
    private AliOssUtil ossUtil;


    /**
     * 将文件上传给阿里云
     * @param file
     * @return
     */
    @PostMapping("/uploads")
    public Result<String> upload(MultipartFile file)  {
        log.info("上传文件");
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }

        //获取原始文件名
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.contains(".")) {
            throw new BusinessException("上传文件格式非法");
        }
        //获取文件扩展名
        String extension = filename.substring(filename.lastIndexOf("."));
        //通过UUID生成唯一标识码，防止重名
        String objectName = UUID.randomUUID().toString() + extension;

        try {
            String returnstr = ossUtil.upload(file.getBytes(), objectName);
            return Result.success(returnstr);
        } catch (IOException e) {
            throw new BusinessException("上传失败");
        }
    }
}
