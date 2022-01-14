package com.zhonghang.baidu.netdisk.util;

import java.io.File;
import java.security.MessageDigest;

/**
 * Created by zhonghang  2022/1/4.
 */
public class Md5Util {
    /**
     * @Description: TODO 获取md5值
     * String path 文件地址
     */
    private final static String[] strHex = {"0", "1", "2", "3", "4", "5",
            "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"};

    public static String getMD5(File path) {
        StringBuilder buffer = new StringBuilder();
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] b = md.digest(org.apache.commons.io.FileUtils.readFileToByteArray(path));
            for (int value : b) {
                int d = value;
                if (d < 0) {
                    d += 256;
                }
                int d1 = d / 16;
                int d2 = d % 16;
                buffer.append(strHex[d1]).append(strHex[d2]);
            }
            return buffer.toString();
        } catch (Exception e) {
            return null;
        }
    }
}
