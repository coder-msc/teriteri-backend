package com.teriteri.backend.strategy.impl.upload;

import com.aliyun.oss.ServiceException;
import com.teriteri.backend.pojo.dto.UploadFileDto;
import com.teriteri.backend.strategy.UploadStrategy;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * @author 码头薯条Pro
 * @date 2024/7/14 10:36
 * <p>抽象上传模板</p>
 */
@Service
public abstract class AbstractUploadStrategy implements UploadStrategy {
    @Override
    public String uploadFile(UploadFileDto uploadInfo) {
        try {
            //根据信息做处理
            if (0 == uploadInfo.getNeedSlice()) {
                //不分片 上传完整文件
                upload(uploadInfo.getUploadPath(), uploadInfo.getFileName(), uploadInfo.getFileInputStream());
            } else if (1 == uploadInfo.getNeedSlice()) {
                //分片上传并合并
                uploadAndComposeFile(uploadInfo);
            }
            return getFileAccessUrl(uploadInfo.getUploadPath() + "" + uploadInfo.getFileName());
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceException("文件上传失败");
        }
    }

    /**
     * 判断文件是否存在
     *
     * @param filePath 文件路径
     * @return {@link Boolean}
     */
    public abstract Boolean exists(String filePath);

    /**
     * 上传
     *
     * @param path        路径
     * @param fileName    文件名
     * @param inputStream 输入流
     * @throws IOException io异常
     */
    public abstract void upload(String path, String fileName, InputStream inputStream);

    /**
     * 上传 然后 合并分片
     *
     * @param uploadInfo           文件上传信息
     */
    public abstract void uploadAndComposeFile(UploadFileDto uploadInfo);

    /**
     * 删除文件
     *
     * @param fileNameList 删除文件的路径
     */
    public abstract void removeFiles(List<String> fileNameList);

    /**
     * 获取文件访问url
     *
     * @param filePath 文件路径
     * @return {@link String} 文件url
     */
    public abstract String getFileAccessUrl(String filePath);
}
