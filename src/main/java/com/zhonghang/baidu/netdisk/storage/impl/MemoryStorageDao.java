package com.zhonghang.baidu.netdisk.storage.impl;

import com.zhonghang.baidu.netdisk.dto.StsInfo;
import com.zhonghang.baidu.netdisk.response.AccessTokenVo;
import com.zhonghang.baidu.netdisk.response.OrganizationInfo;
import com.zhonghang.baidu.netdisk.storage.StorageDaoI;

/**
 * Created by zhonghang  2022/1/12.
 */
public class MemoryStorageDao extends StorageDaoI {
    private AccessTokenVo accessTokenVo ;
    private OrganizationInfo organizationInfo;
    private StsInfo stsInfo;
    @Override
    public void saveAccessToken(AccessTokenVo accessTokenVo) {
        this.accessTokenVo = accessTokenVo;
    }

    @Override
    public AccessTokenVo getAccessToken() {
        return accessTokenVo;
    }

    @Override
    public void saveOrganizationInfo(OrganizationInfo organizationInfo) {
        this.organizationInfo = organizationInfo;
    }

    @Override
    public OrganizationInfo getOrganizationInfo() {
        return organizationInfo;
    }

    @Override
    public void saveStsInfo(StsInfo stsInfo) {
        this.stsInfo = stsInfo;
    }

    @Override
    public StsInfo getStsInfo() {
        return stsInfo;
    }
}
