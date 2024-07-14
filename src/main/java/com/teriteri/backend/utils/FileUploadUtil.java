package com.teriteri.backend.utils;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.*;
import com.teriteri.backend.enums.FilePathEnum;
import com.teriteri.backend.pojo.dto.UploadFileDto;
import com.teriteri.backend.strategy.context.UploadStrategyContext;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;


@Slf4j
@Component
public class FileUploadUtil {
    @Value("${directory.chunk}")
    private String CHUNK_DIRECTORY;   // 分片存储目录
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private UploadStrategyContext uploadStrategyContext;

    /**
     * 往阿里云对象存储上传单张图片
     *
     * @param file 图片文件
     * @param type 图片分类，如 cover、carousel、other等，不允许空字符串，这里没有做判断了，自己注意就好
     * @return 图片的URL地址
     * @throws IOException
     */
    public String uploadImage(@NonNull MultipartFile file, @NonNull String type) throws IOException {
        // 生成文件名
        String originalFilename = file.getOriginalFilename();   // 获取原文件名
        String ext = "." + FilenameUtils.getExtension(originalFilename);    // 获取文件后缀
        String uuid = System.currentTimeMillis() + UUID.randomUUID().toString().replace("-", "");
        String fileName = uuid + ext;
        // 完整路径名
//        String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date()).replace("-", "");
//        String filePathName = date + "/img/" + type + "/" + fileName;
        String folderName = FilePathEnum.PHOTO.getFilePath() + "/picture/" + type + "/";
        UploadFileDto uploadInfo = new UploadFileDto();
        uploadInfo.setUploadPath(folderName);
        uploadInfo.setFileName(fileName);
        uploadInfo.setNeedSlice(0);
//        uploadInfo.setFileMd5();
        uploadInfo.setFile(file);
        uploadInfo.setFileInputStream(file.getInputStream());
        String url = uploadStrategyContext.executeUploadStrategy(uploadInfo);

/*        if (uploadStrategyConfig.equals("minio")) {
            try {
                //路径

                OssFile ossFile = ossTemplate.upLoadFile(folderName, fileName, file);
                log.info(String.valueOf(ossFile));
            } catch (OSSException oe) {
                log.error("OSS出错了:" + oe.getErrorMessage());
                throw oe;
            } catch (ClientException ce) {
                log.error("OSS连接出错了:" + ce.getMessage());
                throw ce;
            }
            //前端可访问的资源路径
            String url = minioEndpoint + "/" + minioBucketName  + "/" +  filePathName;
            log.info("前端可访问的资源路径,{}",url);
            return url;
        }
        if (uploadStrategyConfig.equals("oss")) {
            try {
                ossClient.putObject(
                        OSS_BUCKET, // 仓库名
                        filePathName,   // 文件名（含路径）
                        file.getInputStream()   // 数据流
                );
            } catch (OSSException oe) {
                log.error("OSS出错了:" + oe.getErrorMessage());
                throw oe;
            } catch (ClientException ce) {
                log.error("OSS连接出错了:" + ce.getMessage());
                throw ce;
            }
            return OSS_BUCKET_URL + filePathName;
        }*/
        return url;
    }

    /**
     * 往阿里云对象存储上传单个视频，简单上传
     *
     * @param file 视频文件
     * @return 视频的URL地址
     * @throws IOException
     */
   /* public String uploadVideo(@NonNull MultipartFile file) throws IOException {
        // 生成文件名
        String originalFilename = file.getOriginalFilename();   // 获取原文件名
        String ext = "." + FilenameUtils.getExtension(originalFilename);    // 获取文件后缀
        String uuid = System.currentTimeMillis() + UUID.randomUUID().toString().replace("-", "");
        String fileName = uuid + ext;
        // 完整路径名
        String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date()).replace("-", "");
        String filePathName = date + "/video/" + fileName;
        try {
            ossClient.putObject(
                    OSS_BUCKET, // 仓库名
                    filePathName,   // 文件名（含路径）
                    file.getInputStream()   // 数据流
            );
        } catch (OSSException oe) {
            log.error("OSS出错了:" + oe.getErrorMessage());
            throw oe;
        } catch (ClientException ce) {
            log.error("OSS连接出错了:" + ce.getMessage());
            throw ce;
        }
        return OSS_BUCKET_URL + filePathName;
    }*/

    /**
     * 将本地的视频分片文件追加合并上传到OSS
     *
     * @param hash 视频的hash值，用于检索对应分片
     * @return 视频在OSS的URL地址
     * @throws IOException
     */
    public String appendUploadVideo(@NonNull String hash) throws IOException {
        // 生成文件名
//        String uuid = System.currentTimeMillis() + UUID.randomUUID().toString().replace("-", "");
        String fileName = hash + ".mp4";
//        String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date()).replace("-", "");
//        String filePathName = date + "/video/" + fileName;
        String folderName = FilePathEnum.VIDEO.getFilePath() + "/video/";
        int totalSlice = 0;//总分片数量
        while (true) {
            File chunkFile = new File(CHUNK_DIRECTORY + hash + "-" + totalSlice);
            if (!chunkFile.exists()) {
                if (totalSlice == 0) {
                    log.error("没找到任何相关分片文件");
                    return null;
                }
                break;
            }
            totalSlice++;
        }
        int chunkIndex = 0;
        Long position = 0L; // 追加位置
        String url = "";
        while (true) {
            Object s = redisUtil.getValue(folderName + fileName);
            if (null == s || -1 == Long.parseLong(s.toString())) {
                redisUtil.setExValue(folderName + fileName, position.toString(), 5 * 60, TimeUnit.SECONDS);
            }
            File chunkFile = new File(CHUNK_DIRECTORY + hash + "-" + chunkIndex);
            if (!chunkFile.exists()) {
                if (chunkIndex == 0) {
                    log.error("没找到任何相关分片文件");
                    return null;
                }
                break;
            }
            FileInputStream input = new FileInputStream(chunkFile);
            MultipartFile multipartFile = new MockMultipartFile("file", chunkFile.getName(), "application/octet" + "-stream", input);
            // 上传分片文件
            UploadFileDto uploadInfo = new UploadFileDto();
            uploadInfo.setUploadPath(folderName);
            uploadInfo.setFileName(fileName);
            uploadInfo.setNeedSlice(1);
            uploadInfo.setIndexSlice(chunkIndex);
            uploadInfo.setTotalSlice(totalSlice);
            uploadInfo.setPosition(position);
            uploadInfo.setFileMd5(hash);
            uploadInfo.setLocalFile(chunkFile);
            uploadInfo.setFile(multipartFile);
            uploadInfo.setFileInputStream(multipartFile.getInputStream());
            url = uploadStrategyContext.executeUploadStrategy(uploadInfo);
            chunkFile.delete(); // 上传完后删除分片
            chunkIndex++;
        }
        //阿里云专用，需要传每次的位置去合并（下一次合并依赖上一次的位置）
        redisUtil.setExValue(folderName + fileName, -1L, 5 * 60, TimeUnit.SECONDS);
        return url;
    }

/*
    private String appendUploadVideoAliYunOss(String hash) throws IOException {
        // 生成文件名
        String uuid = System.currentTimeMillis() + UUID.randomUUID().toString().replace("-", "");
        String fileName = uuid + ".mp4";
        // 完整路径名
        String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date()).replace("-", "");
        String filePathName = date + "/video/" + fileName;
        ObjectMetadata meta = new ObjectMetadata();
        // 设置内容类型为MP4视频
        meta.setContentType("video/mp4");
        int chunkIndex = 0;
        long position = 0; // 追加位置
        while (true) {
            File chunkFile = new File(CHUNK_DIRECTORY + hash + "-" + chunkIndex);
            if (!chunkFile.exists()) {
                if (chunkIndex == 0) {
                    log.error("没找到任何相关分片文件");
                    return null;
                }
                break;
            }
            // 读取分片数据
            FileInputStream fis = new FileInputStream(chunkFile);
            byte[] buffer = new byte[(int) chunkFile.length()];
            fis.read(buffer);
            fis.close();
            // 追加上传分片数据
            try {
                AppendObjectRequest appendObjectRequest = new AppendObjectRequest(OSS_BUCKET, filePathName, new ByteArrayInputStream(buffer), meta);
                appendObjectRequest.setPosition(position);
                AppendObjectResult appendObjectResult = ossClient.appendObject(appendObjectRequest);
                position = appendObjectResult.getNextPosition();
            } catch (OSSException oe) {
                log.error("OSS出错了:" + oe.getErrorMessage());
                throw oe;
            } catch (ClientException ce) {
                log.error("OSS连接出错了:" + ce.getMessage());
                throw ce;
            }
            chunkFile.delete(); // 上传完后删除分片
            chunkIndex++;
        }
        return OSS_BUCKET_URL + filePathName;
    }
*/

/*
    private String appendUploadVideoMinioOss(String hash)throws IOException {
        // 生成文件名
        String uuid = System.currentTimeMillis() + UUID.randomUUID().toString().replace("-", "");
        String fileName = uuid + ".mp4";
        // 完整路径名
        String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date()).replace("-", "");
        String filePathName = date + "/video/" + fileName;
        ObjectMetadata meta = new ObjectMetadata();
        // 设置内容类型为MP4视频
        meta.setContentType("video/mp4");
        int chunkIndex = 0;
        long position = 0; // 追加位置
        while (true) {
            File chunkFile = new File(CHUNK_DIRECTORY + hash + "-" + chunkIndex);
            if (!chunkFile.exists()) {
                if (chunkIndex == 0) {
                    log.error("没找到任何相关分片文件");
                    return null;
                }
                break;
            }
            String folderName = date + "/video/";
            FileInputStream input = new FileInputStream(chunkFile);
            MultipartFile multipartFile = new MockMultipartFile("file",
                    chunkFile.getName(), "application/octet" + "-stream", input);
             // 上传分片文件
            Boolean isUpload = ossTemplate.uploadFileNew("teriteri", hash, chunkIndex, multipartFile);

      // 读取分片数据
            InputStream fis = new FileInputStream(chunkFile);
            byte[] buffer = new byte[(int) chunkFile.length()];
            fis.read(buffer);
            fis.close();
            // 追加上传分片数据
            try {
                InputStream byteArrayInputStream = new ByteArrayInputStream(buffer);
                //路径
                String folderName = date + "/video/";
                OssFile ossFile = ossTemplate.upLoadFile(folderName, fileName, null, byteArrayInputStream);
                log.error("视频上传MINIO{}", ossFile);
            } catch (Exception oe) {
                log.error("视频上传MINIO出错了:" + oe);
                throw oe;
            }

            chunkFile.delete(); // 上传完后删除分片
            chunkIndex++;
        }
        //合并
        List<ComposeSource> sourceObjectList = Stream.iterate(0, i -> ++i)
                .limit(chunkIndex)
                .map(i -> ComposeSource.builder()
                        .bucket("teriteri")
                        .object(hash.concat("/").concat(Integer.toString(i)))
                        .build())
                .collect(Collectors.toList());
        ossTemplate.composeFileNew("teriteri", filePathName, sourceObjectList);
        //删除临时分片

        return minioEndpoint + "/" + minioBucketName + "/" + filePathName;
    }

*/

    /**
     * 往阿里云对象存储上传单个视频分片文件，在阿里云上设置了3天的生命周期，超过三天未合并使用的分片将被自动删除
     * 建议只在分布式系统使用，单体系统还是存在本地更好存取以及节省流量
     *
     * @param file 分片文件
     * @param name 分片名，以 hash-index 命名
     * @return 是否上传成功，一般返回false是因为分片已存在
     * @throws IOException
     */
    /*public boolean uploadChunk(@NonNull MultipartFile file, @NonNull String name) throws IOException {
        String fileName = "chunk/" + name;  // 分片文件在OSS的存储路径名
        boolean success = false;
        try {
            // 判断文件是否存在
            boolean found = ossClient.doesObjectExist(OSS_BUCKET, fileName);
            if (!found) {
                // 不存在才上传
                ossClient.putObject(OSS_BUCKET, fileName, file.getInputStream());
                success = true; // 上传成功
            }
        } catch (OSSException oe) {
            log.error("OSS出错了:" + oe.getErrorMessage());
            throw oe;
        } catch (ClientException ce) {
            log.error("OSS连接出错了:" + ce.getMessage());
            throw ce;
        }
        return success;
    }*/

    /**
     * 查询指定目录下，指定前缀的文件数量，阿里云限制了单次查询最多100条
     *
     * @param prefix 要筛选的文件名前缀，包括目录路径，如果为空字符串，则查询全部文件
     * @return 指定目录下，指定前缀的文件数量
     */
    public int countFiles(@NonNull String prefix) {
        int count = 0;
       /* try {
            ListObjectsRequest listObjectsRequest = new ListObjectsRequest(OSS_BUCKET);
            listObjectsRequest.setPrefix(prefix);
            ObjectListing objectListing = ossClient.listObjects(listObjectsRequest);
            List<OSSObjectSummary> objectSummaries = objectListing.getObjectSummaries();
            count = objectSummaries.size();
        } catch (OSSException oe) {
            log.error("OSS出错了:" + oe.getErrorMessage());
            throw oe;
        } catch (ClientException ce) {
            log.error("OSS连接出错了:" + ce.getMessage());
            throw ce;
        }*/
        return count;
    }

    /**
     * 删除指定目录下指定前缀的所有文件，不允许目录和前缀都是空字符串
     *
     * @param prefix 要筛选的文件名前缀，包括目录路径，不允许为空字符串
     */
    public void deleteFiles(@NonNull String prefix) {
      /*  if (prefix.equals("")) {
            log.warn("你正试图删除整个bucket，已拒绝该危险操作");
            return;
        }
        try {
            // 列举所有包含指定前缀的文件并删除。
            String nextMarker = null;
            ObjectListing objectListing;
            do {
                ListObjectsRequest listObjectsRequest = new ListObjectsRequest(OSS_BUCKET).withPrefix(prefix).withMarker(nextMarker);
                objectListing = ossClient.listObjects(listObjectsRequest);
                if (objectListing.getObjectSummaries().size() > 0) {
                    List<String> keys = new ArrayList<>();
                    for (OSSObjectSummary s : objectListing.getObjectSummaries()) {
//                        System.out.println("key name: " + s.getKey());
                        keys.add(s.getKey());
                    }
                    DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(OSS_BUCKET).withKeys(keys).withEncodingType("url");
                    DeleteObjectsResult deleteObjectsResult = ossClient.deleteObjects(deleteObjectsRequest);
                    List<String> deletedObjects = deleteObjectsResult.getDeletedObjects();
                    try {
                        for (String obj : deletedObjects) {
                            String deleteObj = URLDecoder.decode(obj, "UTF-8");
//                            log.info("删除文件：" + deleteObj);
                        }
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
                nextMarker = objectListing.getNextMarker();
            } while (objectListing.isTruncated());
        } catch (OSSException oe) {
            log.error("OSS出错了:" + oe.getErrorMessage());
        } catch (ClientException ce) {
            log.error("OSS连接出错了:" + ce.getMessage());
        }*/
    }
}
