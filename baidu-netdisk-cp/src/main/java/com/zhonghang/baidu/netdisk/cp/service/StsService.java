package com.zhonghang.baidu.netdisk.cp.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.http.HttpUtil;
import com.baidubce.http.HttpMethodName;
import com.baidubce.internal.InternalRequest;
import com.zhonghang.baidu.netdisk.cp.config.BaiduConfig;
import com.zhonghang.baidu.netdisk.cp.dto.StsInfo;
import com.zhonghang.baidu.netdisk.cp.http.RequestUtil;
import com.zhonghang.baidu.netdisk.cp.response.OrganizationInfo;
import com.zhonghang.baidu.netdisk.cp.storage.StorageDaoI;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.util.Map;

/**
 * Created by zhonghang  2022/1/4.
 */
@Slf4j
public class StsService {
    private BaiduConfig baiduConfig;
    private OrganizationInfoService organizationInfoService;
    private StorageDaoI storageDaoI;

    public StsService(StorageDaoI storageDaoI,OrganizationInfoService organizationInfoService ,BaiduConfig baiduConfig){
        this.organizationInfoService = organizationInfoService;
        this.storageDaoI = storageDaoI;
        this.baiduConfig = baiduConfig;
    }

    public StsInfo getDefaultStsInfo(){
        return getStsInfo(storageDaoI.getDefaultOrganizationInfo().getCid());
    }

    public StsInfo getStsInfo(Long cid){
        StsInfo stsInfo = storageDaoI.getStsInfo(cid);
        //可能会过期？
        if(stsInfo == null || (stsInfo.getExpirationSecond() - DateUtil.currentSeconds()) < 0){
            stsInfo = stsToken(cid);
            storageDaoI.saveStsInfo(stsInfo,cid);
        }

        return stsInfo;
    }

    /**
     * 获取stsToken
     * @return
     */
    private synchronized StsInfo stsToken(Long cid) {
        OrganizationInfo organizationInfo = organizationInfoService.getOrganizationInfo(cid);

        StringBuilder paramStr = new StringBuilder();
        paramStr.append("durationSeconds=129600")// 3天
                .append("&appid=").append(baiduConfig.getAppId())
                .append("&uk=").append( organizationInfo.getOrgInfo().getUk()+"")
                .append("&cid=").append( organizationInfo.getCid()+"")
                .append("&permission=READ,WRITE,LIST");
        return baiduStsToken(paramStr.toString());
    }

    private StsInfo baiduStsToken(String paramStr) {
        log.debug("请求获取stsToken：{}",baiduConfig.getAppKey());
        InternalRequest request = new InternalRequest(HttpMethodName.GET, URI.create("https://pan.baidu.com/eopen/api/sts/sessiontoken?"+paramStr));
        Map<String, String> param =  HttpUtil.decodeParamMap(request.getUri().toString(),"utf-8");
        StsInfo stsInfo = RequestUtil.request(param, request , baiduConfig.getAppKey(), baiduConfig.getSecretKey()).getJSONObject("data").toJavaObject(StsInfo.class);
        stsInfo.setExpirationSecond(DateUtil.parse(stsInfo.getExpiration()).second() - 60);//提前60秒过期
        return stsInfo;
    }


}
