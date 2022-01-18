package com.zhonghang.baidu.netdisk.cp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Created by zhonghang  2022/1/12.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestInfo {
    private String method;
    private String url;
    private Map<String,String> header;
    private Map<String,String> form;
}
