package com.zhonghang.baidu.netdisk.cp;

import cn.hutool.core.util.StrUtil;
import com.zhonghang.baidu.netdisk.cp.storage.StorageDaoI;
import com.zhonghang.baidu.netdisk.cp.storage.impl.MemoryStorageDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by zhonghang  2022/1/17.
 */
@Configuration
@Slf4j
@EnableConfigurationProperties(BdNetdiskCpProperties.class)
public class BdNetdiskCpAutoConfiguration implements ApplicationContextAware {
    private ApplicationContext applicationContext;
    @Autowired
    private BdNetdiskCpProperties bdNetdiskCpProperties;

    @Bean
    public BaiduNetDisk baiduNetDisk() throws ClassNotFoundException {
        if(StrUtil.isBlank(bdNetdiskCpProperties.getAppId()) ||
                StrUtil.isBlank(bdNetdiskCpProperties.getAppKey()) ||
                StrUtil.isBlank(bdNetdiskCpProperties.getSecretKey())||
                StrUtil.isBlank(bdNetdiskCpProperties.getStorageRule())
        ){
            log.error("百度企业网盘缺少必要配置信息，请配置后再启动。否则会导致一系列未知错误。查看详细文档：https://github.com/zhonghang1993/baidu-netdisk");
//            throw new RuntimeException(">>> 百度企业网盘缺少必要配置信息，请配置后再启动，查看详细文档：https://github.com/zhonghang1993/baidu-netdisk");
            return null;
        }
        BaiduNetDisk baiduNetDisk = new BaiduNetDisk(bdNetdiskCpProperties,verifyStorage(bdNetdiskCpProperties.getStorageRule()));
        return baiduNetDisk;
    }

    private StorageDaoI verifyStorage(String storageDaoIPath) throws ClassNotFoundException {

        if(MemoryStorageDao.class.getName().equals(storageDaoIPath)){
            return new MemoryStorageDao();
        }
        //从spring容器中查找该对象并注入
         Object storageDaoI = applicationContext.getBean(Class.forName(storageDaoIPath));
        if(storageDaoI == null){
            throw new RuntimeException("根据您设定的路径，在spring容器中未找到自定义的Storage存储规则实现");
        }
        if(! (storageDaoI instanceof StorageDaoI)){
            throw new RuntimeException("自定义的存储规则必须继承并实现StorageDaoI");
        }

        return (StorageDaoI) storageDaoI;

    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
