package com.teriteri.backend.strategy.context;

import com.teriteri.backend.enums.FileUploadModeEnum;
import com.teriteri.backend.pojo.dto.UploadFileDto;
import com.teriteri.backend.strategy.UploadStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * @author 码头薯条Pro
 * @date 2024/7/14 10:33
 * <p>上传策略上下文</p>
 */
@Service
public class UploadStrategyContext {
    /**
     * 上传模式
     */
    @Value("${upload.strategy}")
    private String uploadStrategy;

    @Autowired
    private Map<String, UploadStrategy> uploadStrategyMap;

    /**
     * 上传文件
     *
     * @param uploadInfo 文件上传信息
     * @return {@link String} 文件地址
     */
    public String executeUploadStrategy(UploadFileDto uploadInfo) {
        return uploadStrategyMap.get(FileUploadModeEnum.getStrategy(uploadStrategy)).uploadFile(uploadInfo);
    }
}
