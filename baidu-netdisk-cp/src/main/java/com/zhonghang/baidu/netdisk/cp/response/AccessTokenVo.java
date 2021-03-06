package com.zhonghang.baidu.netdisk.cp.response;

import lombok.Data;

import java.util.Date;

/**
 * Created by zhonghang  2022/1/4.
 */
@Data
public class AccessTokenVo {
    private String accessToken;
    private Integer expiresIn; //access_token的有效期，单位：秒
    private String refreshToken; // 用于刷新access_token, 有效期为10年
    private String scope; //access_token最终的访问权限，即用户的实际授权列表

    private Date expiration;

    private long expiresSecond; //过期时间，单位秒
}
