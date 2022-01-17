package com.zhonghang.baidu.netdisk.cp.dto;

import com.alibaba.fastjson.JSONArray;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by zhonghang  2022/1/4.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManagerFileRequest {

    enum Opera{
        copy, move, rename, delete
    }

    private Opera opera;
    private Integer async = 1; //0:同步， 1 自适应，2异步

    /**
     * copy/move:[{"path":"/测试目录/123456.docx","dest":"/测试目录/abc","newname":"11223.docx","ondup":"fail"}]
     * rename:[{path":"/测试目录/123456.docx","newname":test.docx"}]
     * delete:["/测试目录/123456.docx"]
     */
    private JSONArray filelist; //待操作文件列表
    private String ondup; //全局ondup,遇到重复文件的处理策略, fail(默认，直接返回失败)、newcopy(重命名文件)、overwrite、skip

}
