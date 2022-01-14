package com.zhonghang.baidu.netdisk.exception;

import lombok.Data;

/**
 * Created by zhonghang  2022/1/4.
 */
@Data
public class NetDiskException extends RuntimeException{


    private String errMsg;
    public NetDiskException( String errMsg) {
        super(errMsg);
        this.errMsg = errMsg;
    }
}
