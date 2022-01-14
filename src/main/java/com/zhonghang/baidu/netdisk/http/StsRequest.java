package com.zhonghang.baidu.netdisk.http;

import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSONObject;
import com.baidubce.internal.InternalRequest;
import com.zhonghang.baidu.netdisk.dto.RequestDto;
import com.zhonghang.baidu.netdisk.exception.NetDiskException;
import com.zhonghang.baidu.netdisk.service.StsService;
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
    public  JSONObject request(Map<String, String> param, InternalRequest request){
        return RequestUtil.request(param,null,request, stsService.getStsInfo().getAccessKeyId(), stsService.getStsInfo().getSecretAccessKey() ,false);
    }
    public  JSONObject requestBody(Map<String, String> param , String body, InternalRequest request){
        return RequestUtil.request(param,body,request, stsService.getStsInfo().getAccessKeyId(), stsService.getStsInfo().getSecretAccessKey() ,true);
    }

    public  JSONObject requestFile(Map<String, String> param , File file, InternalRequest request){
        RequestDto requestDto = RequestUtil.addSign(param, request ,stsService.getStsInfo().getAccessKeyId(), stsService.getStsInfo().getSecretAccessKey() );
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

    public  void requestDownload(Map<String, String> param, InternalRequest request , String saveFilePath) {

        RequestDto requestDto = RequestUtil.addSign(param, request ,stsService.getStsInfo().getAccessKeyId(),stsService.getStsInfo().getSecretAccessKey());
        requestDto.getHeader().put("User-Agent","pan.baidu.com");

//        requestDto.getHeader().remove("Host");
//        File saveFile = new File(saveFilePath);

        System.out.println(request.getUri().toString().split("\\?")[0]);
        System.out.println(request.getUri().toString());

//        download(request.getUri().toString() ,null,requestDto.getHeader() , saveFilePath);
        RequestUtil.download(request.getUri().toString() ,requestDto.getHeader() , saveFilePath);

    }




}
