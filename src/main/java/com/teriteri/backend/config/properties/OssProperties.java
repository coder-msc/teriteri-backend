package com.teriteri.backend.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author 码头薯条Pro
 * @date 2024/7/14 11:00
 * <p>oss配置属性</p>
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "upload.oss")
public class OssProperties {
    /**
     * oss域名
     */
    private String url;

    /**
     * 终点
     */
    private String endpoint;

    /**
     * 访问密钥id
     */
    private String accessKeyId;

    /**
     * 访问密钥密码
     */
    private String accessKeySecret;

    /**
     * bucket名称
     */
    private String bucketName;
}
