package com.zhonghang.baidu.netdisk.cp.http;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baidubce.auth.DefaultBceCredentials;
import com.baidubce.auth.SignOptions;
import com.baidubce.auth.Signer;
import com.baidubce.http.HttpMethodName;
import com.baidubce.internal.InternalRequest;
import com.baidubce.util.DateUtils;
import com.zhonghang.baidu.netdisk.cp.dto.RequestDto;
import com.zhonghang.baidu.netdisk.cp.exception.NetDiskException;
import com.zhonghang.baidu.netdisk.cp.service.DownLoadCallbackI;
import com.zhonghang.baidu.netdisk.cp.signer.AdmsBceV1Signer;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by zhonghang  2022/1/5.
 */
@Slf4j
public class RequestUtil {

    public static JSONObject request(Map<String, String> param, InternalRequest request, String accessKeyId, String secretAccessKey){
        return request(param,null,request, accessKeyId, secretAccessKey,false);
    }

    public static JSONObject request(Map<String, Object> param,  InternalRequest request) {
        return request(param , null , null,request,false,false);
    }

    public static JSONObject request(Map<String, Object> param,Map<String,String> header, String requestBody , InternalRequest request, boolean body,boolean getForm) {
        String response ="";
        if(request.getHttpMethod() == HttpMethodName.GET){
            HttpRequest httpRequest =  HttpRequest.get(request.getUri().toString())
                    .addHeaders(header)
                    .form(getForm ? param : null)
                    .timeout(20000);//超时，毫秒

            if(body){
                httpRequest.body(body ? requestBody : null);
            }
            response = httpRequest.execute().body();
        }else if(request.getHttpMethod() == HttpMethodName.POST){

            HttpRequest httpRequest =  HttpRequest.post(request.getUri().toString())
                    .addHeaders(header)
                    .form(body == false ? param : null)
                    .timeout(20000);//超时，毫秒
            if(body){
                httpRequest.body(body ? requestBody : null);
            }
            response = httpRequest.execute().body();
        }

        JSONObject result = formatResult(response);
        return result;
    }
    public static JSONObject request(Map<String, String> param, String requestBody , InternalRequest request, String accessKeyId, String secretAccessKey , boolean body) {
        //计算签名
        RequestDto requestDto = addSign(param, request ,accessKeyId,secretAccessKey);
        return request(requestDto.getParam(),requestDto.getHeader() , requestBody , request , body ,false);
    }

    public static JSONObject request(Map<String, String> param, String requestBody , InternalRequest request, String accessKeyId, String secretAccessKey , boolean body ,boolean getForm) {
        //计算签名
        RequestDto requestDto = addSign(param, request ,accessKeyId,secretAccessKey);
        return request(requestDto.getParam(),requestDto.getHeader() , requestBody , request , body ,getForm);
    }

    public static void download(String url,Map<String,String> header ,String saveFilePath ){
        download(url, header, saveFilePath, (realFilePath, saveFilePath1) -> {
            Map<String,String> header1 = new HashMap<>();
            header1.put("User-Agent","pan.baidu.com"); //必须加头部，否则50M以上的文件不能下载
            download(realFilePath , header1, saveFilePath1);
        });
    }

    public static void download(String url, Map<String,String> header , String saveFilePath, DownLoadCallbackI downLoadCallbackI){
        HttpRequest httpRequest = HttpRequest.get(url)
                .addHeaders(header)
                .timeout(20000);//超时，毫秒
        HttpResponse response =  httpRequest.execute().sync();
        if (response.isOk()) {
            File outFile = response.completeFileNameFromHeader(new File(saveFilePath));
            response.writeBody(outFile, null);
        }else if(response.getStatus() == 302){
            log.debug("302重定向：{}" , response.header("Location"));
            downLoadCallbackI.callback(response.header("Location"), saveFilePath);
        }else{
            log.error("下载出错；状态码：{}，错误信息：{}" ,response.getStatus(), JSONArray.parseObject(response.body()));
            throw new NetDiskException("文件下载出错：状态码："+response.getStatus()+"，错误信息："+response.body());
        }
    }

    public static String downloadRealPath(String url,Map<String,String> header){
        AtomicReference<String> result = new AtomicReference<>();
        download(url, header, null, (realFilePath, saveFilePath1) -> {
            result.set(realFilePath);
        });
        return result.get();
    }

    private static JSONObject formatResult(String response){
        JSONObject result = JSONObject.parseObject(response);
        if(result.getInteger("errno") ==0){
            log.debug("请求成功，返回信息：{}",response);
            return result;
        }else{
            String errMsg = result.getString("err_msg") == null ? result.getString("show_msg"):result.getString("err_msg");
            log.error("请求出错，错误码：{}；错误原因：{}；\n\r全量错误：{}",result.getInteger("errno") ,errMsg,response);
            throw new NetDiskException(result.getString("err_msg"));
        }
    }

    public static RequestDto addSign(Map<String, String> param, InternalRequest request,String accessKeyId,String secretAccessKey) {
        Set<String> headersToSign = new TreeSet<>();
        headersToSign.add("x-bce-date");
        return addSign(param, request, accessKeyId, secretAccessKey,headersToSign);
    }
    //增加签名
    public static RequestDto addSign(Map<String, String> param, InternalRequest request, String accessKeyId, String secretAccessKey , Set<String> headersToSign) {
        //todo 生产成签名
        Date now = new Date();
        String time = DateUtil.format(now, DatePattern.UTC_WITH_ZONE_OFFSET_PATTERN);
        Map<String, String> requestHeader = new HashMap<>();
//        requestHeader.put("Host" , "pan.baidu.com");
        requestHeader.put("x-bce-date", time);
        String t = DateUtils.formatAlternateIso8601Date(now);
//        System.out.println(t);
        request.setHeaders(requestHeader);
        request.setParameters(param);


        SignOptions signOptions = SignOptions.DEFAULT;
        signOptions.setHeadersToSign(headersToSign);
        signOptions.setTimestamp(now);
        request.setSignOptions(signOptions);
//        Signer bceV1Signer = new BceV1Signer();
        Signer bceV1Signer = new AdmsBceV1Signer();

        bceV1Signer.sign(request, new DefaultBceCredentials(accessKeyId, secretAccessKey));

        //todo : 封装请求值
        Map<String, Object> paramO = new HashMap<>();
        for (Map.Entry<String, String> stringStringEntry : param.entrySet()) {
            paramO.put(stringStringEntry.getKey(), stringStringEntry.getValue());
        }

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", request.getHeaders().get("Authorization"));
        headers.put("x-bce-date", request.getHeaders().get("x-bce-date"));
        headers.put("Host", request.getHeaders().get("Host"));

        return RequestDto.builder().header(headers).param(paramO).build();
    }
}
