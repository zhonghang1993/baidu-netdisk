package com.zhonghang.baidu.netdisk.util;

import lombok.extern.slf4j.Slf4j;

import java.io.*;

/**
 * Created by zhonghang  2022/1/4.
 */
@Slf4j
public class FileSeparateUtil {

    /**
     * 文件切割
     * @param obj 目录，文件等
     * @param unit 分片大小
     * @return File[]
     */
    public static File[] separate(Object obj, Integer unit) {

        try {

            InputStream bis = null;//输入流用于读取文件数据
            OutputStream bos = null;//输出流用于输出分片文件至磁盘
            File file = null;
            if (obj instanceof String) {
                file = new File((String) obj);
            }
            if (obj instanceof File) {
                file = (File) obj;
            }

            String filePath = file.getAbsolutePath();
            File newFile = new File(filePath.substring(0, filePath.lastIndexOf("\\") + 1));
            String directoryPath = newFile.getAbsolutePath();
            long splitSize = unit * 1024 * 1024;//单片文件大小,MB
            if (file.length() < splitSize) {
                log.info("文件小于单个分片大小，无需分片{}", file.length());
                return new File[]{file};
            }


            //分片二
            //RandomAccessFile in=null;
            //RandomAccessFile out =null;
            //long length=file.length();//文件大小
            //long count=length%splitSize==0?(length/splitSize):(length/splitSize+1);//文件分片数
            //byte[] bt=new byte[1024];
            //in=new RandomAccessFile(file, "r");
            //for (int i = 1; i <= count; i++) {
            //    out = new RandomAccessFile(new File(filePath+"."+i), "rw");//定义一个可读可写且后缀名为.part的二进制分片文件
            //    long begin = (i-1)*splitSize;
            //    long end = i* splitSize;
            //    int len=0;
            //    in.seek(begin);
            //    while (in.getFilePointer()<end&&-1!=(len=in.read(bt))) {
            //        out.write(bt, 0, len);
            //    }
            //    out.close();
            //}


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
                    bos = new BufferedOutputStream(new FileOutputStream(filePath + "." + (writeByte / splitSize + 1) + ".part"));
                }
                writeByte += len;
                bos.write(bt, 0, len);
            }


            log.info("文件分片成功！");

            //排除被分片的文件
            if (newFile.isDirectory()) {
                File[] files = newFile.listFiles();
                File[] resultFiles = new File[files.length - 1];
                int j = 0;
                for (int i = 0; i < files.length; i++) {
                    if (!files[i].equals(file)) {
                        resultFiles[j] = files[i];
                        j++;
                    }
                }
                return resultFiles;
            }

            bos.flush();
            bos.close();
            bis.close();
            return new File[0];
        } catch (Exception e) {
            log.info("文件分片失败！");
            e.printStackTrace();
        }
        return null;
    }
}
