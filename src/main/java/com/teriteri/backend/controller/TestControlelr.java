package com.teriteri.backend.controller;

import com.teriteri.backend.pojo.SiteConfig;
import com.teriteri.backend.service.comment.SiteConfigService;
import com.teriteri.backend.utils.FileUploadUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

/**
 * @author 码头薯条Pro
 * @date 2024/7/13 19:36
 * <p></p
 */
@RestController
public class TestControlelr {

    @Autowired
    private FileUploadUtil fileUploadUtil;
    @Autowired
    private SiteConfigService siteConfigService;

    @PostMapping("/test/testUpload")
    public Object testController(@RequestBody Map<String, String> map) throws IOException {
        String hash = map.get("hash");
        // 合并到OSS，并返回URL地址
//        String url = fileUploadUtil.appendUploadVideo(hash);

        String  sysConfig = siteConfigService.getFileServiceDomain();

        return sysConfig;
    }


}
