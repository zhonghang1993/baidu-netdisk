package com.zhonghang.baidu.netdisk.service;

import cn.hutool.http.HttpUtil;
import com.baidubce.http.HttpMethodName;
import com.baidubce.internal.InternalRequest;
import com.zhonghang.baidu.netdisk.http.RequestUtil;
import com.zhonghang.baidu.netdisk.response.AccessTokenVo;
import com.zhonghang.baidu.netdisk.response.OrganizationInfo;
import com.zhonghang.baidu.netdisk.storage.StorageDaoI;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.util.Map;

/**
 * Created by zhonghang  2022/1/4.
 */
@Slf4j
public class OrganizationInfoService {

    private AccessTokenService accessTokenService;
    private StorageDaoI storageDaoI ;

    public OrganizationInfoService(StorageDaoI storageDaoI,AccessTokenService accessTokenService){
        this.accessTokenService = accessTokenService;
        this.storageDaoI = storageDaoI;
    }

    public OrganizationInfo getOrganizationInfo(){
        //优先从缓存中取
        OrganizationInfo cache ;
        if( (cache = storageDaoI.getOrganizationInfo()) != null){
            return cache;
        }
        OrganizationInfo organizationInfo = organizationInfo(accessTokenService.getAccessToken());
        storageDaoI.saveOrganizationInfo(organizationInfo);
        return organizationInfo;
    }

    /**
     * 获取企业信息
     * @param accessToken
     */
    private static OrganizationInfo organizationInfo(AccessTokenVo accessToken){
        InternalRequest request = new InternalRequest(HttpMethodName.GET, URI.create("https://pan.baidu.com/eopen/api/organizationinfo?access_token=" + accessToken.getAccessToken()));
        Map param = HttpUtil.decodeParamMap(request.getUri().toString(),"utf-8");
        param.put("access_token" , accessToken.getAccessToken());
        return RequestUtil.request(param ,request).getJSONArray("data").getJSONObject(0).toJavaObject(OrganizationInfo.class);
    }

}
