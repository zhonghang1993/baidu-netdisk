package com.zhonghang.baidu.netdisk.response;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import java.util.List;

/**
 * Created by zhonghang  2022/1/6.
 */
@Data
public class ListFileResponse {
    private List<ListFile> list;
    private Integer cur_permit_code;
    private List<Integer> cur_permit_op_list;
    private Integer cur_permit_source;
    @Data
    public static class ListFile{
        private String fs_id; //uint64	文件在云端的唯一标识ID
        private String path	; //	文件的绝对路径
        private String server_filename	; //	文件名称
        private Long size	; //	文件大小，单位B
        private String server_mtime	; //	文件在服务器修改时间
        private String server_ctime	; //	文件在服务器创建时间
        private Long local_mtime	; //	文件在客户端修改时间
        private Long local_ctime	; //	文件在客户端创建时间
        private Integer isdir	; //	是否目录，0 文件、1 目录
        private Integer category	; //	文件类型，1 视频、2 音频、3 图片、4 文档、5 应用、6 其他、7 种子
        private String md5	; //	文件的md5值，只有是文件类型时，该KEY才存在
        private Integer dir_empty	; //	该目录是否存在子目录， 只有请求参数带WEB且该条目为目录时，该KEY才存在， 0为存在， 1为不存在
        private JSONObject thumbs	; //	只有请求参数带WEB且该条目分类为图片时，该KEY才存在，包含三个尺寸的缩略图URL
    }
}
