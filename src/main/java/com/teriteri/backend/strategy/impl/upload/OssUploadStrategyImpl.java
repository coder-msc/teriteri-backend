package com.teriteri.backend.strategy.impl.upload;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.AppendObjectRequest;
import com.aliyun.oss.model.AppendObjectResult;
import com.aliyun.oss.model.ObjectMetadata;
import com.teriteri.backend.config.properties.OssProperties;
import com.teriteri.backend.pojo.dto.UploadFileDto;
import com.teriteri.backend.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author 码头薯条Pro
 * @date 2024/7/14 10:47
 * <p>oss上传策略</p>
 */
@Slf4j
@Service("ossUploadStrategyImpl")
public class OssUploadStrategyImpl extends AbstractUploadStrategy {

    @Autowired
    private OssProperties ossProperties;
    @Autowired
    private RedisUtil redisUtil;
    /**
     * 获取ossClient
     *
     * @return {@link OSS} ossClient
     */
    private OSS getOssClient() {
        return new OSSClientBuilder().build(ossProperties.getEndpoint(), ossProperties.getAccessKeyId(), ossProperties.getAccessKeySecret());
    }

    @Override
    public Boolean exists(String filePath) {
        return getOssClient().doesObjectExist(ossProperties.getBucketName(), filePath);
    }

    @Override
    public void upload(String path, String fileName, InputStream inputStream) {
        OSS ossClient = getOssClient();
        try {
            // 调用oss方法上传
            ossClient.putObject(ossProperties.getBucketName(), path + fileName, inputStream);
        } catch (OSSException oe) {
            log.error("Error Message:" + oe.getErrorMessage());
            log.error("Error Code:" + oe.getErrorCode());
            log.info("Request ID:" + oe.getRequestId());
            log.info("Host ID:" + oe.getHostId());
        } catch (ClientException ce) {
            log.error("Caught an ClientException, Error Message:" + ce.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    @Override
    public void uploadAndComposeFile(UploadFileDto uploadInfo) {
        OSS ossClient = getOssClient();
        try {
            String s = redisUtil.getValue(uploadInfo.getUploadPath() + uploadInfo.getFileName()).toString();
            Long position = Long.parseLong(s);
            if (-1L == position) {
                position = 0L;
            }
            ObjectMetadata meta = new ObjectMetadata();
            meta.setContentType("application/octet" + "-stream");
            File localFile = uploadInfo.getLocalFile();
            // 读取分片数据
            FileInputStream fis = new FileInputStream(localFile);
            byte[] buffer = new byte[(int) localFile.length()];
            fis.read(buffer);
            fis.close();
            // 追加上传分片数据
            AppendObjectRequest appendObjectRequest = new AppendObjectRequest(ossProperties.getBucketName(), uploadInfo.getUploadPath() + uploadInfo.getFileName(), new ByteArrayInputStream(buffer), meta);
            appendObjectRequest.setPosition(position);
            AppendObjectResult appendObjectResult = ossClient.appendObject(appendObjectRequest);
            position = appendObjectResult.getNextPosition();
            redisUtil.setExValue(uploadInfo.getUploadPath() + uploadInfo.getFileName(), position, 5 * 60, TimeUnit.SECONDS);
        } catch (OSSException oe) {
            log.error("OSS出错了:" + oe.getErrorMessage());
            throw oe;
        } catch (ClientException ce) {
            log.error("OSS连接出错了:" + ce.getMessage());
            throw ce;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeFiles(List<String> fileNameList) {

    }

    @Override
    public String getFileAccessUrl(String filePath) {
        String url = ossProperties.getUrl() + "/" + filePath;
        log.info("上传到OSS文件|访问路径：{} ", url);
        return url;
    }


}
