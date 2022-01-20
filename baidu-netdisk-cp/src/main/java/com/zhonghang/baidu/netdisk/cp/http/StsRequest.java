package com.zhonghang.baidu.netdisk.cp.http;

import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSONObject;
import com.baidubce.internal.InternalRequest;
import com.zhonghang.baidu.netdisk.cp.dto.RequestDto;
import com.zhonghang.baidu.netdisk.cp.dto.RequestInfo;
import com.zhonghang.baidu.netdisk.cp.dto.StsInfo;
import com.zhonghang.baidu.netdisk.cp.exception.NetDiskException;
import com.zhonghang.baidu.netdisk.cp.service.StsService;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.Map;

/**
 * Created by zhonghang  2022/1/4.
 */
@Slf4j
public class StsRequest {
    private StsService stsService;
    public StsRequest(StsService stsService){
        this.stsService = stsService;
    }

    //处理请求
    public  JSONObject request(Map<String, String> param, InternalRequest request,StsInfo stsInfo){
        return RequestUtil.request(param,null,request, stsInfo.getAccessKeyId(), stsInfo.getSecretAccessKey() ,false);
    }
    public  JSONObject requestBody(Map<String, String> param , String body, InternalRequest request,StsInfo stsInfo){
        return RequestUtil.request(param,body,request, stsInfo.getAccessKeyId(), stsInfo.getSecretAccessKey() ,true);
    }

    public RequestInfo getRequestInfo(Map<String, String> param , InternalRequest request,StsInfo stsInfo){
        RequestDto requestDto = RequestUtil.addSign(param, request ,stsInfo.getAccessKeyId(), stsInfo.getSecretAccessKey() );
        RequestInfo requestInfo = RequestInfo.builder().url(request.getUri().toString()).header(requestDto.getHeader()).build();
        requestInfo.getHeader().put("Host","d.pcs.baidu.com");
        requestInfo.setForm(param);
        requestInfo.setMethod("POST");
        return requestInfo;
    }

    public  JSONObject requestFile(Map<String, String> param , File file, InternalRequest request,StsInfo stsInfo){
        RequestDto requestDto = RequestUtil.addSign(param, request ,stsInfo.getAccessKeyId(), stsInfo.getSecretAccessKey() );
        HttpRequest httpRequest =  HttpRequest.post(request.getUri().toString())
                .addHeaders(requestDto.getHeader())
                .header("Host","d.pcs.baidu.com")
                .form("file" , file)
                .timeout(20000);//超时，毫秒
        String response = httpRequest.execute().body();
        JSONObject result = JSONObject.parseObject(response);
        if(result.getInteger("error_code") != null && result.getInteger("error_code") !=0){
            log.error("[第二步]分片上传失败：{}" , response);
            throw new NetDiskException("[第二步]分片上传失败："+result.getString("error_msg"));
        }else{
            log.debug("文件上传成功返回：{}" , response);
        }
        return  JSONObject.parseObject(response);
    }

    public  JSONObject request(Map<String, String> param, InternalRequest request, String accessKeyId, String secretAccessKey){
        return RequestUtil.request(param,null,request, accessKeyId, secretAccessKey,false);
    }
    public  JSONObject request(Map<String, String> param, InternalRequest request, String accessKeyId, String secretAccessKey , boolean getForm){
        return RequestUtil.request(param,null,request, accessKeyId, secretAccessKey,false ,getForm);
    }

    public  void requestDownload(Map<String, String> param, InternalRequest request , String saveFilePath,StsInfo stsInfo) {
        RequestDto requestDto = RequestUtil.addSign(param, request ,stsInfo.getAccessKeyId(),stsInfo.getSecretAccessKey());
        requestDto.getHeader().put("User-Agent","pan.baidu.com");
        RequestUtil.download(request.getUri().toString() ,requestDto.getHeader() , saveFilePath);
    }

    public String requestDownloadRealPath(Map<String, String> param, InternalRequest request ,StsInfo stsInfo) {
        RequestDto requestDto = RequestUtil.addSign(param, request ,stsInfo.getAccessKeyId(),stsInfo.getSecretAccessKey());
        requestDto.getHeader().put("User-Agent","pan.baidu.com");
        return RequestUtil.downloadRealPath(request.getUri().toString() ,requestDto.getHeader());
    }



}
