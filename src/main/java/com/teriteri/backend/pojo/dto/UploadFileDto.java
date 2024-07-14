package com.teriteri.backend.pojo.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;

/**
 * @author 码头薯条Pro
 * @date 2024/7/14 10:18
 * <p>上传文件DTO</p>
 */
@Data
public class UploadFileDto implements Serializable {
    /**
     * 标识列
     */
    private String uuid;
    /**
     * 需要上传的文件
     */
    private File localFile;
    /**
     * 需要上传的文件
     */
    private MultipartFile file;
    /**
     * 需要上传的文件  inputStream
     */
    private InputStream fileInputStream;
    /**
     * 文件Md5
     */
    private String fileMd5;
    /**
     * 文件原始名称
     */
    private String originalFileName;
    /**
     * 文件存储地址 上传路径
     */
    private String uploadPath;
    /**
     * 需要上传的文件名
     */
    private String fileName;
    /**
     * 0不需要分片  1需要分片
     */
    private Integer needSlice;
    /**
     * 第几个分片
     */
    private Integer indexSlice;
    /**
     * 总分片
     */
    private Integer totalSlice;
    /**
     * 分片追加的位置
     */
    private long position;
    /**
     * 分片的大小
     */
    private Double sliceFileSize;
    /**
     * 文件大小
     */
    private Double fileSize;
    /**
     * 桶名称
     */
    private String bucketName;
    /**
     * 是否上传;0：未上传，1：已上传
     */
    private Integer isUploaded;
}
