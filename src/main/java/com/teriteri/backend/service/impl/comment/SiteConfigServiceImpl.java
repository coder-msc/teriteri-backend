package com.teriteri.backend.service.impl.comment;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.teriteri.backend.mapper.SiteConfigMapper;
import com.teriteri.backend.pojo.Category;
import com.teriteri.backend.pojo.SiteConfig;
import com.teriteri.backend.service.comment.SiteConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author 码头薯条Pro
 * @date 2024/7/24 23:45
 * <p>公告配置service</p>
 */
@Slf4j
@Service
public class SiteConfigServiceImpl implements SiteConfigService {
    @Autowired
    private SiteConfigMapper siteConfigMapper;
    /**
     * 上传模式
     */
    @Value("${upload.strategy}")
    private String uploadStrategy;

    @Override
    public SiteConfig getSysConfig() {
        QueryWrapper<SiteConfig> queryWrapper = new QueryWrapper<>();
        List<SiteConfig> configList = siteConfigMapper.selectList(queryWrapper);
        return configList.get(0);
    }

    @Override
    public String getFileServiceDomain() {
        if ("oss".equals(uploadStrategy)) {
            return "";
        }
        if ("minio".equals(uploadStrategy)) {
            QueryWrapper<SiteConfig> queryWrapper = new QueryWrapper<>();
            List<SiteConfig> configList = siteConfigMapper.selectList(queryWrapper);
            SiteConfig siteConfig = configList.get(0);
            return siteConfig.getMinio();
        }
        return null;
    }
}
