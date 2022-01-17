package com.zhonghang.baidu.netdisk.cp.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * Created by zhonghang  2021/12/3.
 */
@Data
@Builder
public class RequestDto {
    private Map<String,Object> param;
    private Map<String, String> header;
}
