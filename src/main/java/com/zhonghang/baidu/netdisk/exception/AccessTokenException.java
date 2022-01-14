package com.zhonghang.baidu.netdisk.exception;

import lombok.Getter;

/**
 * Created by zhonghang  2022/1/5.
 */
@Getter
public class AccessTokenException extends Exception{
    private String errMsg;
    public AccessTokenException(String errMsg){
        super(errMsg);
        this.errMsg = errMsg;
    }
}
