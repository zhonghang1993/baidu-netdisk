package com.zhonghang.baidu.netdisk.storage;

import com.zhonghang.baidu.netdisk.dto.StsInfo;
import com.zhonghang.baidu.netdisk.response.AccessTokenVo;
import com.zhonghang.baidu.netdisk.response.OrganizationInfo;

/**
 * Created by zhonghang  2022/1/5.
 * 持久化存储扩展
 */
public abstract class StorageDaoI {
    public abstract void saveAccessToken(AccessTokenVo accessTokenVo);
    public abstract AccessTokenVo getAccessToken();

    public abstract void saveOrganizationInfo(OrganizationInfo organizationInfo);
    public abstract OrganizationInfo getOrganizationInfo();

    public abstract void saveStsInfo(StsInfo stsInfo);
    public abstract StsInfo getStsInfo();

    /**
     * 解绑，清空AccessToken、stsInfo、organizationInfo信息
     */
    public void clear(){
        saveAccessToken(null);
        saveStsInfo(null);
        saveOrganizationInfo(null);
    }
}
