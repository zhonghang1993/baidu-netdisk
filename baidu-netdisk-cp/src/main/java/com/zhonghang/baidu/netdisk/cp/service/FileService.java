package com.zhonghang.baidu.netdisk.cp.service;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baidubce.http.HttpMethodName;
import com.baidubce.internal.InternalRequest;
import com.zhonghang.baidu.netdisk.cp.config.BaiduConfig;
import com.zhonghang.baidu.netdisk.cp.dto.*;
import com.zhonghang.baidu.netdisk.cp.http.StsRequest;
import com.zhonghang.baidu.netdisk.cp.response.DLinkResponse;
import com.zhonghang.baidu.netdisk.cp.response.FileInfoResponse;
import com.zhonghang.baidu.netdisk.cp.response.ListFileResponse;
import com.zhonghang.baidu.netdisk.cp.util.BeanUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.util.ArrayList;
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
     * 获取文件列表 , 通过企业cid
     *
     * @param listFile 列表请求对象
     * @param cid 企业空间id
     * @return 文件列表
     */
    public ListFileResponse listFile(ListFileRequest listFile,Long cid) {
        StsInfo stsInfo = stsService.getStsInfo(cid);
        return listFile(listFile,stsInfo);
    }

    public ListFileResponse defaultListFile(ListFileRequest listFile){
        return listFile(listFile,stsService.getDefaultStsInfo());
    }

    /**
     * 通过指定的sts获取文件列表
     * @param listFile 列表请求对象
     * @param stsInfo 授权后的sts信息
     * @return ListFileResponse
     */
    public ListFileResponse listFile(ListFileRequest listFile,StsInfo stsInfo) {
        StringBuilder sb = new StringBuilder();
        sb.append("method=list").append("&sts_token=").append(stsInfo.getSessionToken());
        sb.append(BeanUtil.appendString(listFile));

        InternalRequest request = new InternalRequest(HttpMethodName.GET, URI.create("https://pan.baidu.com/eopen/api/list?"+sb));
        Map<String, String> param = HttpUtil.decodeParamMap(request.getUri().toString(),"utf-8");
        JSONObject response = requestUtil.request(param, request,stsInfo.getAccessKeyId(),stsInfo.getSecretAccessKey());
        return response.toJavaObject(ListFileResponse.class);
    }

    /**
     * 管理文件，移动，重命名，删除 - 管理默认的网盘
     * @param managerFileRequest 管理文件请求对象
     */
    public void defaultManagerFile(ManagerFileRequest managerFileRequest){
        managerFile(managerFileRequest,stsService.getDefaultStsInfo());
    }

    /**
     * 管理文件，移动，重命名，删除 - 指定的企业网盘
     * @param managerFileRequest 请求对象
     * @param cid 企业空间id
     */
    public void managerFile(ManagerFileRequest managerFileRequest,Long cid){
        managerFile(managerFileRequest,stsService.getStsInfo(cid));
    }

    public void managerFile(ManagerFileRequest managerFileRequest,StsInfo stsInfo){
        String requestBody = "filelist="+managerFileRequest.getFilelist().toJSONString();
        if(managerFileRequest.getAsync() != null){
            requestBody +="&async="+managerFileRequest.getAsync();
        }

        if(managerFileRequest.getOndup() != null){
            requestBody +="&ondup="+managerFileRequest.getOndup();
        }

        InternalRequest request = new InternalRequest(HttpMethodName.POST, URI.create("https://pan.baidu.com/eopen/api/filemanager?sts_token="+stsInfo.getSessionToken()+"&opera="+ managerFileRequest.getOpera()));
        Map<String, String> param = HttpUtil.decodeParamMap(request.getUri().toString(),"utf-8");
        requestUtil.requestBody(param , requestBody, request,stsInfo);
    }

    /**
     * 下载文件
     * @param saveFilePath 下载存储地址
     * @param cid 企业空间id
     * @param fid 文件fid
     */
    public void download(String saveFilePath ,Long cid,List<String> fid){
        download(saveFilePath,stsService.getStsInfo(cid),fid);
    }

    private void download(String saveFilePath ,StsInfo stsInfo,List<String> fid){
        //获取文件信息的dlink
        List<FileInfoResponse> fileInfo = fileInfo(stsInfo,fid);
        for (int i = 0; i < fileInfo.size(); i++) {
            String dlink = fileInfo.get(i).getDlink()+"&sts_token="+stsInfo.getSessionToken();
            InternalRequest request = new InternalRequest(HttpMethodName.GET, URI.create(dlink));
            Map<String, String> param =  HttpUtil.decodeParamMap(dlink,"utf-8");
            requestUtil.requestDownload(param,request ,saveFilePath + "//"+ fileInfo.get(i).getServer_filename(),stsInfo);
        }
    }

    public void defaultDownload(String saveFilePath ,List<String> fid){
        download(saveFilePath,stsService.getDefaultStsInfo(),fid);
    }

    public void defaultDownload(String saveFilePath ,String fid){
        List<String> fids = new ArrayList<>();
        fids.add(fid);
        download(saveFilePath,stsService.getDefaultStsInfo(),fids);
    }

    /**
     * 获取真实的下载地址，请求时需要在header设置User-Agent = pan.baidu.com
     * @param cid 网盘空间id
     * @param fid 文件fid
     * @return String
     */
    public String downloadRealPath(Long cid,String fid){
        return downloadRealPath(stsService.getStsInfo(cid),fid);
    }

    /**
     * 获取真实的下载地址，请求时需要在header设置User-Agent = pan.baidu.com
     * @param fid 文件fid
     * @return String
     */
    public String defaultDownloadRealPath(String fid){
        return downloadRealPath(stsService.getDefaultStsInfo(),fid);
    }

    private String downloadRealPath(StsInfo stsInfo,String fid){
        FileInfoResponse fileInfo = fileInfo(stsInfo,fid);
        String dlink = fileInfo.getDlink()+"&sts_token="+stsInfo.getSessionToken();
        InternalRequest request = new InternalRequest(HttpMethodName.GET, URI.create(dlink));
        Map<String, String> param =  HttpUtil.decodeParamMap(dlink,"utf-8");
        return requestUtil.requestDownloadRealPath(param,request ,stsInfo);
    }



    public List<FileInfoResponse> fileInfo(Long cid,List<String> fid){
        return fileInfo(FileInfoRequest.builder().dlink(1).needmedia(1).fsids(JSONArray.parseArray(JSONArray.toJSONString(fid))).build(),cid);
    }

    public List<FileInfoResponse> fileInfo(StsInfo stsInfo,List<String> fid){
        return fileInfo(FileInfoRequest.builder().dlink(1).needmedia(1).fsids(JSONArray.parseArray(JSONArray.toJSONString(fid))).build(),stsInfo);
    }

    public FileInfoResponse fileInfo(StsInfo stsInfo,String fid){
        List<String> fids = new ArrayList<>();
        fids.add(fid);
        return fileInfo(stsInfo,fids).get(0);
    }

    public List<FileInfoResponse> defaultFileInfo(List<String> fid){
        return defaultFileInfo(FileInfoRequest.builder().dlink(1).needmedia(1).fsids(JSONArray.parseArray(JSONArray.toJSONString(fid))).build());
    }

    /**
     * 获取文件信息
     * @param fileInfoRequest 文件请求
     * @param cid 网盘空间id
     * @return 多个文件返回
     */
    public List<FileInfoResponse> fileInfo(FileInfoRequest fileInfoRequest,Long cid){
        return fileInfo(fileInfoRequest,stsService.getStsInfo(cid));
    }

    public List<FileInfoResponse> fileInfo(FileInfoRequest fileInfoRequest,StsInfo stsInfo){
        InternalRequest request = new InternalRequest(HttpMethodName.GET, URI.create( "https://pan.baidu.com/eopen/api/filemetas"));
        Map<String, String> param = BeanUtil.covBean(fileInfoRequest);
        param.put("method", "filemetas");
        param.put("sts_token", stsInfo.getSessionToken());

        JSONObject result = requestUtil.request(param,request ,stsInfo.getAccessKeyId(),stsInfo.getSecretAccessKey(),true);
        return result.getJSONArray("info").toJavaList(FileInfoResponse.class);
    }

    public List<FileInfoResponse> defaultFileInfo(FileInfoRequest fileInfoRequest){
        return fileInfo(fileInfoRequest,stsService.getDefaultStsInfo());
    }

    /**
     * 文件的预签名链接
     * @param dLinkDto 下载请求参数
     * @param cid 网盘空间id
     * @return 下载返回信息
     */
    public List<DLinkResponse> dLink(DLinkDto dLinkDto,Long cid){
        return dLink(dLinkDto,stsService.getStsInfo(cid));
    }

    public List<DLinkResponse> defaultDLink(DLinkDto dLinkDto){
        return dLink(dLinkDto,stsService.getDefaultStsInfo());
    }

    public List<DLinkResponse> dLink(DLinkDto dLinkDto,StsInfo stsInfo){
        InternalRequest request = new InternalRequest(HttpMethodName.POST, URI.create( "https://pan.baidu.com/eopen/api/exdownload?sts_token="+ stsInfo.getSessionToken()));
        Map<String, String> param = HttpUtil.decodeParamMap(request.getUri().toString(),"utf-8");
        String body = "filelist="+JSONArray.toJSONString(dLinkDto.getFilelist());
        JSONObject result = requestUtil.requestBody(param,body,request,stsInfo);
        return result.getJSONArray("data").toJavaList(DLinkResponse.class);
    }

}
