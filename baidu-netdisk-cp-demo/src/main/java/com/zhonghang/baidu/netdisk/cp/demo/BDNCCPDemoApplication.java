package com.zhonghang.baidu.netdisk.cp.demo;

import com.zhonghang.baidu.netdisk.cp.BaiduNetDisk;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Created by zhonghang  2022/1/17.
 */
@SpringBootApplication
public class BDNCCPDemoApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(BDNCCPDemoApplication.class, args);
        BaiduNetDisk baiduNetDisk = applicationContext.getBean(BaiduNetDisk.class);
        System.out.println(baiduNetDisk);
    }


}
