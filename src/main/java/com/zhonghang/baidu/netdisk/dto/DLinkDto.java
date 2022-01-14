package com.zhonghang.baidu.netdisk.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhonghang  2022/1/6.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DLinkDto {
    private List<FileExpire> filelist = new ArrayList<>();

    public DLinkDto addFile(String fsId , Integer expireHour){
        filelist.add(new FileExpire(fsId,expireHour));
        return this;
    }

    @Data
    @AllArgsConstructor
    public static class FileExpire{
        private String fsid;
        private Integer expire_hour;
    }
}
