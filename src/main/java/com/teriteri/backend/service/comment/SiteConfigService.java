package com.teriteri.backend.service.comment;

import com.teriteri.backend.pojo.SiteConfig;

/**
 * @author 码头薯条Pro
 * @date 2024/7/24 23:43
 * <p>公告配置service</p>
 */
public interface SiteConfigService {

    SiteConfig getSysConfig();

    String getFileServiceDomain();
}
