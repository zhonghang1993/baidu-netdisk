package com.zhonghang.baidu.netdisk.cp.dto;

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
public class SliceUploadDto {
    private final String type = "tmpfile"; //固定值 tmpfile
    private String path; //上传后使用的文件绝对路径，需要urlencode
    private String uploadid; //precreate接口下发的uploadid
//    private Integer partseq; //文件分片的位置序号，从0开始，参考precreate接口返回的block_list
//    private File file; //	上传的文件内容
}
