package com.svgtopng.utility;

import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.FastByteArrayOutputStream;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class MinIoUtil {
    @Value("${minio.bucketName}")
    private String bucketName;
    @Autowired
    private MinioClient minioClient;

    /**
     * 查看存储bucket是否存在
     * 
     * @param bucketName 存储bucket
     * @return boolean
     */
    public Boolean bucketExists(String bucketName) {
        Boolean found;
        try {
            found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return found;
    }

    /**
     * 创建存储bucket
     * 
     * @param bucketName 存储bucket名称
     * @return Boolean
     */
    public Boolean makeBucket(String bucketName) {
        try {
            minioClient.makeBucket(MakeBucketArgs.builder()
                    .bucket(bucketName)
                    .build());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 删除存储bucket
     * 
     * @param bucketName 存储bucket名称
     * @return Boolean
     */
    public Boolean removeBucket(String bucketName) {
        try {
            minioClient.removeBucket(RemoveBucketArgs.builder()
                    .bucket(bucketName)
                    .build());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 文件上传
     * 
     * @param file 文件
     * @return Map<String, String>
     */
    public Map<String, String> upload(MultipartFile file) {
        String objName = "";
        String res = null;
        Map<String, String> map = new HashMap<>();
        try {
            String[] nameArray = file.getOriginalFilename().split("\\.");
            String suffix = nameArray[nameArray.length - 1];
            String newFileName = UUID.randomUUID() + "." + suffix;
            objName = Utility.getYear() + "/" + Utility.getMonth() + "/"
                    + Utility.getDay()
                    + "/" + newFileName;
            PutObjectArgs objectArgs = PutObjectArgs.builder().bucket(
                    bucketName).object(
                            objName)
                    .stream(file.getInputStream(), file.getSize(), -1).contentType(file.getContentType()).build();
            // 文件名称相同会覆盖
            minioClient.putObject(objectArgs);
            res = minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .bucket(bucketName)
                    .object(objName)
                    .method(Method.GET)
                    .expiry(60, TimeUnit.SECONDS)
                    .build());
            map.put("filename", newFileName);
            map.put("path", res.substring(0, res.indexOf("?")));
        } catch (Exception e) {
            e.printStackTrace();
            return map;
        }
        return map;
    }

    public String getPreviewFileUrl(String fileName) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder().bucket(bucketName).object(fileName).build());
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 文件下载
     * 
     * @param path     文件路径
     * @param fileName 文件名称
     * @param res      response
     * @return
     */
    public void download(String path, String fileName, HttpServletResponse res) {
        GetObjectArgs objectArgs = GetObjectArgs.builder().bucket(bucketName)
                .object(path + fileName).build();
        try (GetObjectResponse response = minioClient.getObject(objectArgs)) {
            byte[] buf = new byte[1024];
            int len;
            try (FastByteArrayOutputStream os = new FastByteArrayOutputStream()) {
                while ((len = response.read(buf)) != -1) {
                    os.write(buf, 0, len);
                }
                os.flush();
                byte[] bytes = os.toByteArray();
                res.setCharacterEncoding("utf-8");
                // 设置强制下载不打开
                res.setContentType("application/force-download");
                res.addHeader("Content-Disposition", "attachment;fileName=" + fileName);
                try (ServletOutputStream stream = res.getOutputStream()) {
                    stream.write(bytes);
                    stream.flush();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 批量删除文件对象
     * 
     * @param bucketName 存储bucket名称
     * @param objects    对象名称集合
     */
    public Iterable<Result<DeleteError>> removeObjects(String bucketName, List<String> objects) {
        List<DeleteObject> dos = objects.stream().map(e -> new DeleteObject(e)).collect(Collectors.toList());
        Iterable<Result<DeleteError>> results = minioClient
                .removeObjects(RemoveObjectsArgs.builder().bucket(bucketName).objects(dos).build());
        return results;
    }
}
