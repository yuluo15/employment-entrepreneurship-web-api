package com.gxcj.utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.gxcj.exception.BusinessException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@Component
public class AliOssUtil implements InitializingBean {

    // 读取配置文件
    @Value("${storage.alioss.endpoint}")
    private String endpoint;
    @Value("${storage.alioss.accessKeyId}")
    private String accessKeyId;
    @Value("${storage.alioss.accessKeySecret}")
    private String accessKeySecret;
    @Value("${storage.alioss.bucketName}")
    private String bucketName;

    // 定义公开的静态变量
    public static String ENDPOINT;
    public static String ACCESS_KEY_ID;
    public static String ACCESS_KEY_SECRET;
    public static String BUCKET_NAME;

    // Spring 加载完属性后，赋值给静态变量
    @Override
    public void afterPropertiesSet() throws Exception {
        ENDPOINT = endpoint;
        ACCESS_KEY_ID = accessKeyId;
        ACCESS_KEY_SECRET = accessKeySecret;
        BUCKET_NAME = bucketName;
    }

    /**
     * 上传文件
     */
    public static String uploadFile(MultipartFile file) {
        try {
            // 1. 生成文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName =  UUID.randomUUID() + extension;

            // 2. 创建 OSSClient 实例
            OSS ossClient = new OSSClientBuilder().build(ENDPOINT, ACCESS_KEY_ID, ACCESS_KEY_SECRET);

            // 3. 上传
            InputStream inputStream = file.getInputStream();
            ossClient.putObject(BUCKET_NAME, fileName, inputStream);

            // 4. 关闭
            ossClient.shutdown();

            // 5. 返回 URL
            return ENDPOINT.split("//")[0] + "//" + BUCKET_NAME + "." + ENDPOINT.split("//")[1] + "/" + fileName;

        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException("阿里云OSS上传失败");
        }
    }
}