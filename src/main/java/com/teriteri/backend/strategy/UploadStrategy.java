package com.teriteri.backend.strategy;

import com.teriteri.backend.pojo.dto.UploadFileDto;

/**
 * @author 码头薯条Pro
 * @date 2024/7/14 10:22
 * <p>上传策略</p>
 */
public interface UploadStrategy {
    /**
     * 上传文件
     *
     * @param uploadInfo 需要上传文件的详情
     * @return {@link String} 文件地址
     */
    String uploadFile(UploadFileDto uploadInfo);
}
