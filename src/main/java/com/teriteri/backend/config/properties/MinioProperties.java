package com.teriteri.backend.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @author 码头薯条Pro
 * @date 2024/7/14 11:01
 * <p></p>
 */
@Data
@Configuration
@ConfigurationProperties(prefix = MinioProperties.PREFIX)
public class MinioProperties {
    /**
     * 配置前缀
     */
    public static final String PREFIX = "upload.minio";

    /**
     * 对象存储服务的URL
     */
    private String endpoint;

    /**
     * Access key 账户ID
     */
    private String accessKey;

    /**
     * Secret key 密码
     */
    private String secretKey;

    /**
     * 默认的存储桶名称
     */
    private String bucketName;
    /**
     * 分片存储的临时桶
     */
    private String bucketNameSlice;
    /**
     * 可上传的文件后缀名
     */
    private List<String> fileExt;
}
