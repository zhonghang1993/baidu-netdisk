package com.zhonghang.baidu.netdisk.cp.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zhonghang.baidu.netdisk.cp.config.BaiduConfig;
import com.zhonghang.baidu.netdisk.cp.exception.AccessTokenException;
import com.zhonghang.baidu.netdisk.cp.exception.NetDiskException;
import com.zhonghang.baidu.netdisk.cp.response.AccessTokenVo;
import com.zhonghang.baidu.netdisk.cp.response.OrganizationInfo;
import com.zhonghang.baidu.netdisk.cp.storage.StorageDaoI;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhonghang  2022/1/4.
 *
 * 通过accessToken可以获取到stsToken
 */
@Slf4j
public class AccessTokenService {

    private BaiduConfig baiduConfig;

    private StorageDaoI storageDaoI ;

    private AccessTokenService(){}

    public AccessTokenService(StorageDaoI storageDaoI,BaiduConfig baiduConfig){
        this.baiduConfig = baiduConfig;
        this.storageDaoI = storageDaoI;
    }

    /**
     * 获取accessToken
     * @param code 回调的code
     * @return token信息
     * @throws AccessTokenException 验证异常
     */
    public synchronized AccessTokenVo generateAccessToken(String code) throws AccessTokenException {
        if(StrUtil.isBlank(code)){
            throw new AccessTokenException("code为空，处理失败");
        }
        //grant_type=authorization_code&code=CODE&client_id=YOUR_CLIENT_ID&client_secret=YOUR_CLIENT_SECRET&redirect_uri=YOUR_REGISTERED_REDIRECT_URI
        Map<String,Object> param = new HashMap<>();
        param.put("grant_type" , "authorization_code");
        param.put("code" , code);
        param.put("client_id" , baiduConfig.getAppKey());
        param.put("client_secret" , baiduConfig.getSecretKey());
        param.put("redirect_uri" , baiduConfig.getRedirectUri());

        JSONObject result = JSONObject.parseObject(HttpUtil.get(" https://openapi.baidu.com/oauth/2.0/token", param));
        if(StrUtil.isNotBlank(result.getString("error"))){
            log.error("授权获取accessToken失败,error：{}，error_description：{}", result.getString("error"),result.getString("error_description"));
            throw new AccessTokenException("处理失败："+result.getString("error") +"；错误详情：" + result.getString("error_description"));
        }
        AccessTokenVo accessTokenVo = result.toJavaObject(AccessTokenVo.class);
        //提前60秒刷新
        accessTokenVo.setExpiresSecond(DateUtil.currentSeconds() + accessTokenVo.getExpiresIn()-60);
        OrganizationInfo organizationInfo = OrganizationInfoService.getOrganizationInfoByAccessToken(accessTokenVo);
        storageDaoI.saveAccessToken(accessTokenVo, organizationInfo.getCid());
        storageDaoI.saveOrganizationInfo(organizationInfo);
        return accessTokenVo;
    }

    public AccessTokenVo getDefaultAccessToken(){
        return getAccessToken(storageDaoI.getDefaultOrganizationInfo().getCid());
    }

    public AccessTokenVo getAccessToken(Long cid){
        AccessTokenVo at = storageDaoI.getAccessToken(cid);
        if(at == null){
            throw new NetDiskException("请先扫码授权后，再调用其他接口。");
        }
        //验证token是否失效，如果失效则刷新
        if(at.getExpiresSecond() - DateUtil.currentSeconds() <= 0){
            at = refreshToken(at.getRefreshToken(),cid);
        }
        return at;
    }

    private AccessTokenVo refreshToken(String refreshToken,Long cid){
        Map<String,Object> param = new HashMap<>();
        param.put("grant_type" , "refresh_token");
        param.put("refresh_token" , refreshToken);
        param.put("client_id" , baiduConfig.getAppId());
        param.put("client_secret" , baiduConfig.getSecretKey());
        String resultStr = HttpUtil.get(" https://openapi.baidu.com/oauth/2.0/token", param);
        //?处理异常
        AccessTokenVo accessTokenVo = JSON.parseObject(resultStr, AccessTokenVo.class);
        log.debug("token失效，刷新token：{}" , accessTokenVo.getAccessToken());
        //提前60秒过期
        accessTokenVo.setExpiresSecond(DateUtil.currentSeconds() + accessTokenVo.getExpiresIn()-60);
        storageDaoI.saveAccessToken(accessTokenVo ,cid);
        return accessTokenVo;
    }
}
