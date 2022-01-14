package com.zhonghang.baidu.netdisk.dto;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
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
public class SliceCreateDto {
    private String path; //上传后使用的文件绝对路径
    private Long size; //文件或目录的大小，必须要和文件真实大小保持一致
    private Integer isDir; //是否目录，0 文件、1 目录
    /**
     * 文件命名策略，默认1
     * 0 为不重命名，返回冲突
     * 1 为只要path冲突即重命名
     * 2 为path冲突且block_list不同才重命名
     * 3 为覆盖
     */
    private Integer rType = 3;
    private String uploadId; //uploadid， 非空表示通过superfile2上传

    private JSONArray blockList;//文件各分片MD5的json串 ; MD5对应superfile2返回的md5，且要按照序号顺序排列
    private String localCtime; //客户端创建时间(精确到秒)，默认为当前时间戳
    private String localMtime; //客户端修改时间(精确到秒)，默认为当前时间戳
    private Integer zipQuality; //图片压缩程度，有效值50、70、100
    private String zipSign; //未压缩原始图片文件真实md5
    private String isRevision; //是否需要多版本支持 ; 1为支持，0为不支持， 默认为0 (带此参数会忽略重命名策略)
    /**
     * 上传方式
     * 1 手动、2 批量上传、3 文件自动备份
     * 4 相册自动备份、5 视频自动备份
     */
    private Integer mode;
    private JSONObject exifInfo; //json字符串，orientation、width、height、recovery为必传字段，其他字段如果没有可以不传

}
