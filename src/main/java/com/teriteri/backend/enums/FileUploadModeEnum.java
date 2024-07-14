package com.teriteri.backend.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author 码头薯条Pro
 * @date 2024/7/14 10:43
 * <p>上传模式枚举</p>
 */
@Getter
@AllArgsConstructor
public enum FileUploadModeEnum {
    /**
     * oss
     */
    OSS("oss", "ossUploadStrategyImpl"),

    /**
     * minio
     */
    MINIO("minio", "minioUploadStrategyImpl");

    /**
     * 模式
     */
    private final String mode;

    /**
     * 策略
     */
    private final String strategy;

    /**
     * 获取策略
     *
     * @param mode 模式
     * @return 搜索策略
     */
    public static String getStrategy(String mode) {
        for (FileUploadModeEnum value : FileUploadModeEnum.values()) {
            if (value.getMode().equals(mode)) {
                return value.getStrategy();
            }
        }
        return null;
    }
}
