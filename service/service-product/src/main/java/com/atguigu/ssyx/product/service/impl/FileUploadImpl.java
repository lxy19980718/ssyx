package com.atguigu.ssyx.product.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import com.atguigu.ssyx.product.service.FileUploadService;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Service
public class FileUploadImpl implements FileUploadService {

    @Value("${aliyun.endpoint}")
    private String endPoint;

    @Value("${aliyun.keyid}")
    private String keyId;

    @Value("${aliyun.keysecret}")
    private String keySecret;

    @Value("${aliyun.bucketname}")
    private String bucketName;

    @Override
    public String uploadFile(MultipartFile file) {

        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endPoint,keyId,keySecret);


        try {
            //上传文件输入流
            InputStream inputStream = file.getInputStream();
            //获取文件实际名称
            String objectName = file.getOriginalFilename();
            String uuid = UUID.randomUUID().toString().replace("-","");
            objectName = uuid + objectName;
            //对上传文件进行分组，根据当前年/月/日
            String currentDateTime = new DateTime().toString("yyyy/MM/dd");
            objectName = currentDateTime + "/" +objectName;


            // 创建PutObjectRequest对象。
            //param：
            //1、bucket名称
            //2、上传文件路径+名称
            //3、文件输入流
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectName, inputStream);
            // 设置该属性可以返回response。如果不设置，则返回的response为空
            putObjectRequest.setProcess("true");
            // 创建PutObject请求。
            PutObjectResult result = ossClient.putObject(putObjectRequest);

            //返回上传图片在阿里云的路径
            String uri = result.getResponse().getUri();
            //如果上传成功，则返回200

            return uri;
        }  catch (Exception e) {
            System.out.println("Error Message:" + e.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
        return null;
    }
}
