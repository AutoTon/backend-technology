package com.technology.feign;

import feign.Headers;
import feign.Param;
import feign.RequestLine;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * 上传文件接口样例
 */
@Component
@FeignClient(
        value = "bfs",
        url = "",
        configuration = FeignMultipartSupportConfig.class
)
public interface FileStorageFeignClient {

    /**
     * 上传文件
     * @param bucket 桶
     * @param file 文件
     * @param token 认证
     * @return 结果
     */
    @RequestLine("POST /file/new?bucket={bucket}")
    @Headers("Content-Type: multipart/form-data")
    UploadResult upload(@Param(value = "bucket") String bucket, @Param("file") MultipartFile file, @Param("Authorization") String token);

}
