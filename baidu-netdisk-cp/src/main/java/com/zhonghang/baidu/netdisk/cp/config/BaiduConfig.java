package com.zhonghang.baidu.netdisk.cp.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * Created by zhonghang  2022/1/4.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BaiduConfig {
    private String appId ;
    private String appName ;
    private String appKey ;
    private String secretKey ;
    private String singKey ;

    private String redirectUri;

    private String filePrefix; //文件上传前缀
    /**
     * 单位mb
     * 普通用户单个分片大小固定为4MB（文件大小如果小于4MB，无需切片，直接上传即可），单文件总大小上限为4G。
     * 普通会员用户单个分片大小上限为16MB，单文件总大小上限为10G。
     * 超级会员用户单个分片大小上限为32MB，单文件总大小上限为20G。
     */
    private Integer unit;// 分片大小，单位：M
}
