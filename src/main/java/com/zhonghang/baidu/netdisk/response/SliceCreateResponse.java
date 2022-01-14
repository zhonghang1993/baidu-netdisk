package com.zhonghang.baidu.netdisk.response;

import lombok.Data;

/**
 * Created by zhonghang  2022/1/5.
 */
@Data
public class SliceCreateResponse {
    private String fs_id;
    private String md5;
    private String server_filename;
    private String category;
    private String path;
    private String size;
    private String ctime;
    private String mtime;
    private String isdir;
}
