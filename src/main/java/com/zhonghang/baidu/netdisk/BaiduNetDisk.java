package com.zhonghang.baidu.netdisk;

import com.zhonghang.baidu.netdisk.config.BaiduConfig;
import com.zhonghang.baidu.netdisk.http.StsRequest;
import com.zhonghang.baidu.netdisk.service.*;
import com.zhonghang.baidu.netdisk.storage.StorageDaoI;
import com.zhonghang.baidu.netdisk.storage.impl.MemoryStorageDao;
import lombok.Getter;

/**
 * Created by zhonghang  2022/1/5.
 */
@Getter
public class BaiduNetDisk {

    private AccessTokenService accessTokenService;
    private FileService fileService;
    private SuperFileService superFileService;
    private StsService stsService;
    private OrganizationInfoService organizationInfoService;
    private StsRequest requestUtil ;

    public BaiduNetDisk(BaiduConfig baiduConfig){
        //默认使用内存存储
        StorageDaoI storageDaoI = new MemoryStorageDao();
        accessTokenService = new AccessTokenService(storageDaoI,baiduConfig);
        organizationInfoService = new OrganizationInfoService(storageDaoI,accessTokenService);
        stsService = new StsService(storageDaoI,organizationInfoService,baiduConfig);
        requestUtil = new StsRequest(stsService);
        fileService = new FileService(baiduConfig,stsService,requestUtil);
        superFileService = new SuperFileService(baiduConfig,stsService,requestUtil,accessTokenService);
    }

    public BaiduNetDisk(BaiduConfig baiduConfig, StorageDaoI storageDaoI){
        accessTokenService = new AccessTokenService(storageDaoI,baiduConfig);
        organizationInfoService = new OrganizationInfoService(storageDaoI,accessTokenService);
        stsService = new StsService(storageDaoI,organizationInfoService,baiduConfig);
        requestUtil = new StsRequest(stsService);
        fileService = new FileService(baiduConfig,stsService,requestUtil);
        superFileService = new SuperFileService(baiduConfig,stsService,requestUtil,accessTokenService);
    }

}
