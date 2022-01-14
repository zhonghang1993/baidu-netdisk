package com.zhonghang.baidu.netdisk.response;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

/**
 * Created by zhonghang  2022/1/6.
 */
@Data
public class FileInfoResponse {
    private String dlink;
    private Integer category;
    private String docPreview;
    private String fs_id;
    private String file_key;
    private String headurl;
    private String isdir;
    private String local_ctime;
    private String local_mtime;
    private String lodocpreview;
    private String md5;
    private String path;
    private String server_filename;
    private String server_mtime;
    private Long size;
    private JSONObject thumbs;

}
