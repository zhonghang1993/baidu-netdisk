package com.zhonghang.baidu.netdisk.cp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by zhonghang  2022/1/4.
 * 文件列表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListFileRequest{

    private String dir = "/"; //需要list的目录，以/开头的绝对路径, 默认为/
    /**
     * 排序字段：默认为name
     * time表示先按文件类型排序，后按修改时间排序
     * name表示先按文件类型排序，后按文件名称排序
     * size表示先按文件类型排序， 后按文件大小排序
     */
    private String order;
    private String desc; //该KEY存在为降序，否则为升序 （注：排序的对象是当前目录下所有文件，不是当前分页下的文件）
    private Integer start; //起始位置，从0开始
    private Integer limit; //每页条目数，默认为1000，最大值为10000
    private String web; //值为web时， 返回dir_empty属性 和 缩略图数据
    private Integer folder; //是否只返回文件夹，0 返回所有，1 只返回目录条目，且属性只返回path字段。
    private Integer showempty; //是否返回 dir_empty 属性，0 不返回，1 返回
}
