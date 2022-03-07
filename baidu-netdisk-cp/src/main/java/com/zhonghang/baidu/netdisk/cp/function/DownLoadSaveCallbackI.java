package com.zhonghang.baidu.netdisk.cp.function;

import cn.hutool.http.HttpResponse;

/**
 * Created by zhonghang  2022/3/7.
 */
@FunctionalInterface
public interface DownLoadSaveCallbackI {
    void save(HttpResponse response);
}
