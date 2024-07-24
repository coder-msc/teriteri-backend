package com.teriteri.backend.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 码头薯条Pro
 * @date 2024/7/24 23:35
 * <p></p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SiteConfig {
    @TableId(type = IdType.AUTO)
    private Integer id; // 唯一标识
    private String minio;//minio
}
