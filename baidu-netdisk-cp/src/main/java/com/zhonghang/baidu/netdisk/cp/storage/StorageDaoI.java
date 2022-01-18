package com.zhonghang.baidu.netdisk.cp.storage;

import com.zhonghang.baidu.netdisk.cp.dto.StsInfo;
import com.zhonghang.baidu.netdisk.cp.response.AccessTokenVo;
import com.zhonghang.baidu.netdisk.cp.response.OrganizationInfo;

/**
 * Created by zhonghang  2022/1/5.
 * 持久化存储扩展
 */
public abstract class StorageDaoI {
    /**
     * 保存用户授权的accessToken
     * @param accessTokenVo token
     * @param cid 企业id
     */
    public abstract void saveAccessToken(AccessTokenVo accessTokenVo, Long cid);

    /**
     * 获取指定企业的授权信息
     * @param cid 企业id
     * @return AccessTokenVo
     */
    public abstract AccessTokenVo getAccessToken(Long cid);

    /**
     * 保存企业信息
     * @param organizationInfo 企业信息
     */
    public abstract void saveOrganizationInfo(OrganizationInfo organizationInfo);

    /**
     * 获取指定企业的企业信息
     * @param cid 企业id
     * @return OrganizationInfo
     */
    public abstract OrganizationInfo getOrganizationInfo(Long cid);

    /**
     * 获取默认的存储网盘，获取默认的企业信息
     * @return OrganizationInfo
     */
    public abstract OrganizationInfo getDefaultOrganizationInfo();

    /**
     * 保存sts信息
     * @param stsInfo sts信息
     * @param cid 企业id
     */
    public abstract void saveStsInfo(StsInfo stsInfo, Long cid);

    /**
     * 获取指定企业的sts信息
     * @param cid
     * @return StsInfo
     */
    public abstract StsInfo getStsInfo(Long cid);

    /**
     * 解绑，清空AccessToken、stsInfo、organizationInfo信息
     */
    public void clear(Long cid){
        saveAccessToken(null,cid);
        saveStsInfo(null,cid);
        saveOrganizationInfo(null);
    }
}
