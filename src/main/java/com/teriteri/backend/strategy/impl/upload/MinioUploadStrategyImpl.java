package com.teriteri.backend.strategy.impl.upload;

import com.aliyun.oss.OSS;
import com.teriteri.backend.config.properties.MinioProperties;
import com.teriteri.backend.pojo.dto.UploadFileDto;
import io.minio.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author 码头薯条Pro
 * @date 2024/7/14 10:48
 * <p>Minio上传策略</p>
 */
@Slf4j
@Service("minioUploadStrategyImpl")
public class MinioUploadStrategyImpl extends AbstractUploadStrategy {
    @Resource
    private MinioProperties minioProperties;

    /**
     * 获取ossClient
     *
     * @return {@link OSS} ossClient
     */
    private MinioClient getMinioClient() {
        MinioClient minioClient = MinioClient.builder()
                .endpoint(minioProperties.getEndpoint())
                .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                .build();
        return minioClient;
    }

    @Override
    public Boolean exists(String filePath) {
        return null;
    }

    @Override
    public void upload(String path, String fileName, InputStream stream) {
        MinioClient minioClient = getMinioClient();
        try {
            // 写入文件
            minioClient.putObject(PutObjectArgs.builder().bucket(minioProperties.getBucketName())
                    .object(path + fileName)
                    .stream(stream, stream.available(), -1).contentType("application/octet" + "-stream").build());
            log.info("上传到minio文件|uploadFile|参数：bucketName：{}，路径：{} "
                    , minioProperties.getBucketName(), path + fileName);
        } catch (Exception e) {
            log.error("文件上传到Minio异常|参数：bucketName:{},路径:{},|异常:{}", minioProperties.getBucketName(), path + fileName, e);
        }
    }

    @Override
    public void uploadAndComposeFile(UploadFileDto uploadInfo) {
        MinioClient minioClient = getMinioClient();
        try {
            String tempPath = uploadInfo.getFileMd5().concat("/").concat(Integer.toString(uploadInfo.getIndexSlice()));
            // 写入临时文件夹
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioProperties.getBucketName())
                    .object(tempPath)
                    .stream(uploadInfo.getFile().getInputStream(), uploadInfo.getFile().getSize(), -1)
                    .contentType(uploadInfo.getFile().getContentType())
                    .build());
            log.info("上传分片到minio文件|路径：{}，分片下标：{}", tempPath, uploadInfo.getIndexSlice());
            //如果分片是最后一片 那就合并
            if ((uploadInfo.getTotalSlice() - 1) == uploadInfo.getIndexSlice()) {
                log.info("合并Minio文件开始：path：{}，分片下标：{}", tempPath, uploadInfo.getIndexSlice());
                List<ComposeSource> sourceObjectList = Stream.iterate(0, i -> ++i)
                        .limit(uploadInfo.getTotalSlice())
                        .map(i -> ComposeSource.builder()
                                .bucket(minioProperties.getBucketName())
                                .object(uploadInfo.getFileMd5().concat("/").concat(Integer.toString(i))) // 临时文件们的路径
                                .build())
                        .collect(Collectors.toList());
                ObjectWriteResponse response = minioClient.composeObject(
                        ComposeObjectArgs.builder()
                                .bucket(minioProperties.getBucketName())
                                .object(uploadInfo.getUploadPath() + uploadInfo.getFileName()) //新指定的路径
                                .sources(sourceObjectList)
                                .build());
                log.info("合并Minio文件结束：path：{}，分片下标：{}", uploadInfo.getUploadPath() + uploadInfo.getFileName(), uploadInfo.getIndexSlice());
                //删除临时分片
                List<String> fileNameList = new ArrayList<>();
                for (int i = 0; i < uploadInfo.getTotalSlice(); i++) {
                    fileNameList.add(uploadInfo.getFileMd5().concat("/").concat(Integer.toString(i)));
                }
                removeFiles(fileNameList);
            }
        } catch (Exception e) {
            log.error("上传分片到minio文件异常|路径：{}，分片下标：{} 异常:{}", uploadInfo.getUploadPath() + uploadInfo.getFileName(), uploadInfo.getIndexSlice(), e);
        }
    }

    @Override
    public void removeFiles(List<String> fileNameList) {
        MinioClient minioClient = getMinioClient();
        try {
            //拼接需要删除的文件入参
            List<DeleteObject> deleteObjectList = new ArrayList<>();
            for (String fileName : fileNameList) {
                DeleteObject temp = new DeleteObject(fileName);
                deleteObjectList.add(temp);
            }
            //调用删除
            Iterable<Result<DeleteError>> results =
                    minioClient.removeObjects(
                            RemoveObjectsArgs.builder().bucket(minioProperties.getBucketName()).objects(deleteObjectList).build());
            log.info("Minio多个文件删除完成!|参数：fileNameList:{}", fileNameList);
        } catch (Exception e) {
            log.error("Minio多个文件删除异常!|参数：fileNameList:{}|异常:{}", fileNameList, e);
        }
    }

    @Override
    public String getFileAccessUrl(String filePath) {
        //获取文件的访问路径URL
//        String url = minioProperties.getEndpoint() + "/" + minioProperties.getBucketName() + "/" + filePath;
        String url = minioProperties.getBucketName() + "/" + filePath;
        log.info("上传到minio文件|访问路径：{} ", url);
        return url;
    }
}
