package com.zhonghang.baidu.netdisk.cp;

import com.zhonghang.baidu.netdisk.cp.config.BaiduConfig;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Created by zhonghang  2022/1/17.
 */
@ConfigurationProperties("baidu.netdisk.cp.net.disk")
@Data
public class BdNetdiskCpProperties extends BaiduConfig {
    //默认使用内存存储
    private String storageRule = "com.zhonghang.baidu.netdisk.cp.storage.impl.MemoryStorageDao";
}
