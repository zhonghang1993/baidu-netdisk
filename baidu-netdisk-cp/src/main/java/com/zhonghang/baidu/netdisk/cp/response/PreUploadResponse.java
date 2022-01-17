package com.zhonghang.baidu.netdisk.cp.response;

import com.alibaba.fastjson.JSONArray;
import lombok.Data;

/**
 * Created by zhonghang  2022/1/5.
 */
@Data
public class PreUploadResponse {
    private String path;
    private String uploadid;
    private Integer return_type;
    private JSONArray block_list;
}
