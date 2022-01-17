package com.zhonghang.baidu.netdisk.cp.dto;

import com.alibaba.fastjson.JSONArray;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by zhonghang  2022/1/5.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PreUploadDto {
    private String path; //上传后使用的文件绝对路径，需要urlencode

    private Long size; //文件或目录的大小，单位B，目录的话大小为0
    private Integer isDir; //	是否目录，0 文件、1 目录
    private final Integer autoInit = 1; //固定值1
    /**
     * 文件命名策略，默认0
     * 0 为不重命名，返回冲突
     * 1 为只要path冲突即重命名
     * 2 为path冲突且block_list不同才重命名
     * 3 为覆盖
     */
    private Integer rType = 3;

    private String uploadId; //上传id
    private JSONArray blockList; //文件各分片MD5数组的json串
    private String contentMd5; //文件MD5
    private String sliceMd5; //文件校验段的MD5，校验段对应文件前256KB
    private String localCtime; //客户端创建时间， 默认为当前时间戳
    private String localMtime; //客户端修改时间，默认为当前时间戳






}
