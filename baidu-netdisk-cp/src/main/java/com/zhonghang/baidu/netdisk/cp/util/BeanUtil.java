package com.zhonghang.baidu.netdisk.cp.util;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhonghang  2022/1/4.
 */
public class BeanUtil {

    public static Map<String,String> covBean(Object o){
        Map<String,String> result = new HashMap<>();
        Class<?> aClass = o.getClass();
        for (Field field : aClass.getDeclaredFields()) {
            try {
                field.setAccessible(true);
                Object value = field.get(o);
                if(value != null){
                    result.put(field.getName() ,value +"");
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

        }
        return result;
    }

    public static String appendString(Object o){
        StringBuilder result = new StringBuilder();
        Class<?> aClass = o.getClass();
        for (Field field : aClass.getDeclaredFields()) {
            try {
                field.setAccessible(true);
                Object value = field.get(o);
                if(value != null){
                    result.append("&").append(field.getName()).append("=").append(value +"");
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

        }
        return result.toString();

    }
}
