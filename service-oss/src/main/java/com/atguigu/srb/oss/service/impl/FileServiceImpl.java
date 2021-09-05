package com.atguigu.srb.oss.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.CannedAccessControlList;
import com.atguigu.srb.oss.service.FileService;
import com.atguigu.srb.oss.util.OssProperties;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {
    @Override
    public String upload(InputStream inputStream, String module, String fileName) {
        //创建oss客户端
        OSS ossClient = new OSSClientBuilder().build(OssProperties.ENDPOINT, OssProperties.KEY_ID, OssProperties.KEY_SECRET);
        //判断oss实例是否存在
        if(!ossClient.doesBucketExist(OssProperties.BUCKET_NAME)){
            //不存在则创建
            ossClient.createBucket(OssProperties.BUCKET_NAME);
            //设置权限,公共读
            ossClient.setBucketAcl(OssProperties.BUCKET_NAME, CannedAccessControlList.PublicRead);
        }
        //构建日期路径
        String folder=new DateTime().toString("yyyy/MM/dd");
        //文件名
        fileName= UUID.randomUUID().toString()+fileName.substring(fileName.lastIndexOf("."));
        //文件根路径
        String key = module+"/"+folder+"/"+fileName;

        //上传文件到阿里云
        ossClient.putObject(OssProperties.BUCKET_NAME,key,inputStream);
        //释放资源
        ossClient.shutdown();
        //阿里云文件绝对路径
        String imgUrl= "https://" + OssProperties.BUCKET_NAME + "." + OssProperties.ENDPOINT + "/" + key;
        return imgUrl;

    }

    @Override
    public void removeFile(String url) {
        //创建oss客户端
        OSS ossClient = new OSSClientBuilder().build(OssProperties.ENDPOINT, OssProperties.KEY_ID, OssProperties.KEY_SECRET);
        //删除文件
        String host="https://" + OssProperties.BUCKET_NAME + "." + OssProperties.ENDPOINT + "/";
        String key=url.substring(host.length());
        ossClient.deleteObject(OssProperties.BUCKET_NAME,key);
        //释放资源
        ossClient.shutdown();
    }
}
