package com.atguigu.srb.oss.controller.api;

import com.atguigu.srb.common.exception.BusinessException;
import com.atguigu.srb.common.result.R;
import com.atguigu.srb.common.result.ResponseEnum;
import com.atguigu.srb.oss.service.FileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;

@Api(tags = "阿里云文件管理")
@CrossOrigin //跨域
@RestController
@RequestMapping("/api/oss/file")
public class FileController {
    @Resource
    private FileService fileService;

    @ApiOperation("文件上传")
    @PostMapping("/upload")
    public R upload(
            @ApiParam("上传文件")
            @RequestParam("file") MultipartFile file,
            @ApiParam("模块")
            @RequestParam("module") String module){
        try {
            InputStream inputStream = file.getInputStream();
            String filename = file.getOriginalFilename();
            String uploadUrl = fileService.upload(inputStream, module, filename);
            return R.ok().message("文件上传成功").data("url",uploadUrl);
        } catch (IOException e) {
            throw new BusinessException(ResponseEnum.UPLOAD_ERROR,e);
        }


    }

    @ApiOperation("删除oss文件")
    @DeleteMapping("/remove")
    public R remove(
            @ApiParam("要删除文件得路径")
            @RequestParam("url") String url){
        fileService.removeFile(url);
        return R.ok().message("删除成功!");
    }

}
