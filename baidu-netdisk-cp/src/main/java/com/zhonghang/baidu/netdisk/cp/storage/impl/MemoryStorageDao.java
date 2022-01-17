package com.zhonghang.baidu.netdisk.cp.storage.impl;

import com.zhonghang.baidu.netdisk.cp.dto.StsInfo;
import com.zhonghang.baidu.netdisk.cp.response.AccessTokenVo;
import com.zhonghang.baidu.netdisk.cp.response.OrganizationInfo;
import com.zhonghang.baidu.netdisk.cp.storage.StorageDaoI;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhonghang  2022/1/12.
 */
public class MemoryStorageDao extends StorageDaoI {
    private Map<Long,AccessTokenVo> accessTokenVos = new HashMap<>();
    private Map<Long,OrganizationInfo> organizationInfos = new HashMap<>();
    private Map<Long,StsInfo> stsInfos = new HashMap<>();
    @Override
    public void saveAccessToken(AccessTokenVo accessTokenVo,Long cid) {
        this.accessTokenVos.put(cid,accessTokenVo);
    }

    @Override
    public AccessTokenVo getAccessToken(Long cid) {
        return accessTokenVos.get(cid);
    }

    @Override
    public AccessTokenVo getDefaultAccessToken() {
        //默认取第一个
        return (AccessTokenVo) accessTokenVos.values().toArray()[0];
    }

    @Override
    public void saveOrganizationInfo(OrganizationInfo organizationInfo) {
        this.organizationInfos.put(organizationInfo.getCid(), organizationInfo);
    }

    @Override
    public OrganizationInfo getOrganizationInfo(Long cid) {
        return organizationInfos.get(cid);
    }

    @Override
    public OrganizationInfo getDefaultOrganizationInfo() {
        //默认取第一个
        return (OrganizationInfo) organizationInfos.values().toArray()[0];
    }

    @Override
    public void saveStsInfo(StsInfo stsInfo,Long cid) {
        this.stsInfos.put(cid,stsInfo);
    }

    @Override
    public StsInfo getStsInfo(Long cid) {
        return stsInfos.get(cid);
    }

    @Override
    public StsInfo getDefaultStsInfo() {
        //默认取第一个
        return (StsInfo) stsInfos.values().toArray()[0];
    }
}
