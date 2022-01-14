package com.zhonghang.baidu.netdisk.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by zhonghang  2022/1/4.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StsInfo {
    private String sessionToken;
    private String accessKeyId;
    private String secretAccessKey;

    private String expiration;
    private String createTime;
    //失效时间
    private long expirationSecond;
}