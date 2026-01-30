//package com.gxcj.utils;
//
//import io.minio.MinioClient;
//import io.minio.PutObjectArgs;
//import org.springframework.beans.factory.InitializingBean;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.UUID;
//
//@Component
//public class MinioUtil implements InitializingBean {
//
//    @Value("${storage.minio.endpoint}")
//    private String endpoint;
//    @Value("${storage.minio.accessKey}")
//    private String accessKey;
//    @Value("${storage.minio.secretKey}")
//    private String secretKey;
//    @Value("${storage.minio.bucketName}")
//    private String bucketName;
//
//    public static String ENDPOINT;
//    public static String ACCESS_KEY;
//    public static String SECRET_KEY;
//    public static String BUCKET_NAME;
//
//    @Override
//    public void afterPropertiesSet() throws Exception {
//        ENDPOINT = endpoint;
//        ACCESS_KEY = accessKey;
//        SECRET_KEY = secretKey;
//        BUCKET_NAME = bucketName;
//    }
//
//    /**
//     * 上传文件
//     */
//    public static String uploadFile(MultipartFile file) {
//        try {
//            // 1. 创建 MinioClient
//            MinioClient minioClient = MinioClient.builder()
//                    .endpoint(ENDPOINT)
//                    .credentials(ACCESS_KEY, SECRET_KEY)
//                    .build();
//
//            // 2. 生成文件名
//            String originalFilename = file.getOriginalFilename();
//            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
//            String fileName = new SimpleDateFormat("yyyy/MM/dd").format(new Date()) + "/" + UUID.randomUUID() + extension;
//
//            // 3. 上传
//            minioClient.putObject(
//                    PutObjectArgs.builder()
//                            .bucket(BUCKET_NAME)
//                            .object(fileName)
//                            .stream(file.getInputStream(), file.getSize(), -1)
//                            .contentType(file.getContentType())
//                            .build()
//            );
//
//            // 4. 返回 URL
//            return ENDPOINT + "/" + BUCKET_NAME + "/" + fileName;
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            throw new RuntimeException("MinIO上传失败");
//        }
//    }
//}