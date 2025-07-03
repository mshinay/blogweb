package com.boot.blogserver.controller;

import com.blog.result.Result;
import com.blog.utils.AliOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/common")
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
    @PostMapping("/upload")
    public Result<String> upload(MultipartFile file)  {
        log.info("上传文件");

        //获取原始文件名
        String filename = file.getOriginalFilename();
        //获取文件扩展名
        String extension = filename.substring(filename.lastIndexOf("."));
        //通过UUID生成唯一标识码，防止重名
        String objectName = UUID.randomUUID().toString() + extension;

        //
        String returnstr;
        try {
         returnstr = ossUtil.upload(file.getBytes(), objectName);
        } catch (IOException e) {
            returnstr = "上传失败";
        }
        return Result.success(returnstr);
    }
}
