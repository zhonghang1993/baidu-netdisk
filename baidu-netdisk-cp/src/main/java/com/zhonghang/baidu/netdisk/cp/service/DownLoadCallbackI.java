package com.zhonghang.baidu.netdisk.cp.service;

/**
 * Created by zhonghang  2022/1/20.
 */
@FunctionalInterface
public interface DownLoadCallbackI {
    void callback(String realFilePath,String saveFilePath);
}
