package com.zhonghang.baidu.netdisk.service;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baidubce.http.HttpMethodName;
import com.baidubce.internal.InternalRequest;
import com.zhonghang.baidu.netdisk.config.BaiduConfig;
import com.zhonghang.baidu.netdisk.dto.*;
import com.zhonghang.baidu.netdisk.http.StsRequest;
import com.zhonghang.baidu.netdisk.response.DLinkResponse;
import com.zhonghang.baidu.netdisk.response.FileInfoResponse;
import com.zhonghang.baidu.netdisk.response.ListFileResponse;
import com.zhonghang.baidu.netdisk.util.BeanUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * Created by zhonghang  2022/1/4.
 */
@Slf4j
public class FileService {
    private BaiduConfig baiduConfig;
    private StsService stsService;
    private StsRequest requestUtil;

    public FileService(BaiduConfig baiduConfig, StsService stsService , StsRequest requestUtil) {
        this.baiduConfig = baiduConfig;
        this.stsService = stsService;
        this.requestUtil = requestUtil;
    }

    /**
     * 获取扫码授权地址
     * @return 扫码授权地址
     */
    public String getGrantUrl(){
        return "https://openapi.baidu.com/oauth/2.0/authorize?response_type=code&client_id="+baiduConfig.getAppKey()
                +"&redirect_uri="+baiduConfig.getRedirectUri()+"&scope=basic,netdisk&display=tv&force_login=1&qrcode=1";
    }

    /**
     * 获取文件列表
     *
     * @param listFile 列表请求对象
     * @return 文件列表
     */
    public ListFileResponse listFile(ListFileRequest listFile) {
        StsInfo stsInfo = stsService.getStsInfo();
        StringBuilder sb = new StringBuilder();
        sb.append("method=list").append("&sts_token=").append(stsInfo.getSessionToken());
        sb.append(BeanUtil.appendString(listFile));

        InternalRequest request = new InternalRequest(HttpMethodName.GET, URI.create("https://pan.baidu.com/eopen/api/list?"+sb));
        Map<String, String> param = HttpUtil.decodeParamMap(request.getUri().toString(),"utf-8");
        JSONObject response = requestUtil.request(param, request,stsInfo.getAccessKeyId(),stsInfo.getSecretAccessKey());
        return response.toJavaObject(ListFileResponse.class);
    }

    /**
     * 管理文件，移动，重命名，删除
     * @param managerFileRequest 请求对象
     */
    public void managerFile(ManagerFileRequest managerFileRequest){
        StsInfo stsInfo = stsService.getStsInfo();
        String requestBody = "filelist="+managerFileRequest.getFilelist().toJSONString();
        if(managerFileRequest.getAsync() != null){
            requestBody +="&async="+managerFileRequest.getAsync();
        }

        if(managerFileRequest.getOndup() != null){
            requestBody +="&ondup="+managerFileRequest.getOndup();
        }

        InternalRequest request = new InternalRequest(HttpMethodName.POST, URI.create("https://pan.baidu.com/eopen/api/filemanager?sts_token="+stsInfo.getSessionToken()+"&opera="+ managerFileRequest.getOpera()));
        Map<String, String> param = HttpUtil.decodeParamMap(request.getUri().toString(),"utf-8");
        requestUtil.requestBody(param , requestBody, request);
    }

    /**
     * 下载文件
     * @param saveFilePath 下载存储地址
     * @param fid 文件fid
     */
    public void download(String saveFilePath ,String... fid){
        StsInfo stsInfo = stsService.getStsInfo();
        //获取文件信息的dlink
        List<FileInfoResponse> fileInfo = fileInfo(fid);
        for (int i = 0; i < fileInfo.size(); i++) {

            String dlink = fileInfo.get(i).getDlink()+"&sts_token="+stsInfo.getSessionToken();

            InternalRequest request = new InternalRequest(HttpMethodName.GET, URI.create(dlink));
            Map<String, String> param =  HttpUtil.decodeParamMap(dlink,"utf-8");
            requestUtil.requestDownload(param,request ,saveFilePath + "//"+ fileInfo.get(i).getServer_filename());
        }

    }

    public List<FileInfoResponse> fileInfo(String... fid){
        return fileInfo(FileInfoRequest.builder().dlink(1).needmedia(1).fsids(JSONArray.parseArray(JSONArray.toJSONString(fid))).build());
    }

    /**
     * 获取文件信息
     * @param fileInfoRequest 文件请求
     * @return 多个文件返回
     */
    public List<FileInfoResponse> fileInfo(FileInfoRequest fileInfoRequest){
        StsInfo stsInfo = stsService.getStsInfo();

        InternalRequest request = new InternalRequest(HttpMethodName.GET, URI.create( "https://pan.baidu.com/eopen/api/filemetas"));
        Map<String, String> param = BeanUtil.covBean(fileInfoRequest);
        param.put("method", "filemetas");
        param.put("sts_token", stsInfo.getSessionToken());

        JSONObject result = requestUtil.request(param,request ,stsInfo.getAccessKeyId(),stsInfo.getSecretAccessKey(),true);
        return result.getJSONArray("info").toJavaList(FileInfoResponse.class);
    }

    /**
     * 文件的预签名链接
     * @param dLinkDto 下载请求参数
     * @return 下载返回信息
     */
    public List<DLinkResponse> dLink(DLinkDto dLinkDto){
        InternalRequest request = new InternalRequest(HttpMethodName.POST, URI.create( "https://pan.baidu.com/eopen/api/exdownload?sts_token="+ stsService.getStsInfo().getSessionToken()));
        Map<String, String> param = HttpUtil.decodeParamMap(request.getUri().toString(),"utf-8");

        String body = "filelist="+JSONArray.toJSONString(dLinkDto.getFilelist());

        JSONObject result = requestUtil.requestBody(param,body,request);
        return result.getJSONArray("data").toJavaList(DLinkResponse.class);
    }

}
