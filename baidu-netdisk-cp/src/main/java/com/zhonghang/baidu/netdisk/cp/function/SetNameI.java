package com.zhonghang.baidu.netdisk.cp.function;

import com.zhonghang.baidu.netdisk.cp.response.FileInfoResponse;

/**
 * Created by zhonghang  2022/3/7.
 */
@FunctionalInterface
public interface SetNameI {
    void setName(FileInfoResponse fileInfo);
}
