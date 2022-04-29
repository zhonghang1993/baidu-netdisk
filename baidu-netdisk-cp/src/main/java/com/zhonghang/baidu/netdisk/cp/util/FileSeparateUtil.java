package com.zhonghang.baidu.netdisk.cp.util;

import com.zhonghang.baidu.netdisk.cp.exception.NetDiskException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhonghang  2022/1/4.
 */
@Slf4j
public class FileSeparateUtil {


    public static List<File> separate(String filePath, Integer unit){
        return separate(new File(filePath) ,unit);
    }

    /**
     * 文件切割
     * @param file 文件
     * @param unit 分片大小
     * @return File[]
     */
    public static List<File> separate(File file, Integer unit) {
        List<File> result = new ArrayList<>();
        if(file.isDirectory()){
            throw new NetDiskException("暂不支持文件夹的上传");
        }
        InputStream bis = null;//输入流用于读取文件数据
        OutputStream bos = null;//输出流用于输出分片文件至磁盘
        try {

            String filePath = file.getAbsolutePath();
            long splitSize = unit * 1024 * 1024;//单片文件大小,MB
            if (file.length() < splitSize) {
                log.debug("文件小于单个分片大小，无需分片{}", file.length());
                result.add(file);
                return result;
            }


            //分片一
            bis = new BufferedInputStream(new FileInputStream(file));
            long writeByte = 0;//已读取的字节数
            int len = 0;
            byte[] bt = new byte[1024];
            while (-1 != (len = bis.read(bt))) {
                if (writeByte % splitSize == 0) {
                    if (bos != null) {
                        bos.flush();
                        bos.close();
                    }
                    File split = new File(filePath + "." + (writeByte / splitSize + 1) + ".part");
                    bos = new BufferedOutputStream(new FileOutputStream(split));
                    result.add(split);
                }
                writeByte += len;
                bos.write(bt, 0, len);
            }

            log.debug("文件分片成功！");

            bos.flush();

            return result;
        } catch (Exception e) {
            log.info("文件分片失败，{}" , ExceptionUtils.getFullStackTrace(e));
            throw new NetDiskException("分片失败："+ e.getMessage());
        }finally {
             {
                try {
                    if(bos != null) bos.close();
                    if(bis != null) bis.close();
                } catch (IOException e) {
                    log.info("关闭流失败，{}" , ExceptionUtils.getFullStackTrace(e));
                }
            }

        }
    }
}
