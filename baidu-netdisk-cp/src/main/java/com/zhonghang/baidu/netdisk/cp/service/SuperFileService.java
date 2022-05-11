package com.zhonghang.baidu.netdisk.cp.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONArray;
import com.baidubce.http.HttpMethodName;
import com.baidubce.internal.InternalRequest;
import com.zhonghang.baidu.netdisk.cp.config.BaiduConfig;
import com.zhonghang.baidu.netdisk.cp.dto.*;
import com.zhonghang.baidu.netdisk.cp.exception.NetDiskException;
import com.zhonghang.baidu.netdisk.cp.http.StsRequest;
import com.zhonghang.baidu.netdisk.cp.response.AccessTokenVo;
import com.zhonghang.baidu.netdisk.cp.response.PreUploadResponse;
import com.zhonghang.baidu.netdisk.cp.response.SliceCreateResponse;
import com.zhonghang.baidu.netdisk.cp.util.FileSeparateUtil;
import com.zhonghang.baidu.netdisk.cp.util.Md5Util;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by zhonghang  2022/1/4.
 * 分片上传
 */
@Slf4j
public class SuperFileService {

    private BaiduConfig baiduConfig;
    private StsService stsService;
    private StsRequest requestUtil;
    private AccessTokenService accessTokenService;
    public SuperFileService(BaiduConfig baiduConfig , StsService stsService, StsRequest requestUtil ,AccessTokenService accessTokenService){
        this.baiduConfig = baiduConfig;
        this.stsService = stsService;
        this.requestUtil = requestUtil;
        this.accessTokenService = accessTokenService;
    }

    private SliceCreateResponse upload(File file, String saveFilePath,StsInfo stsInfo, AccessTokenVo accessTokenVo ){
        String cloudPath = URLUtil.encode(baiduConfig.getFilePrefix() + saveFilePath);

        List<File> separate = FileSeparateUtil.separate(file, baiduConfig.getUnit());

        JSONArray md5Array = new JSONArray();
        if (separate.size() == 1) {
            md5Array.add(Md5Util.getMD5(separate.get(0)));

        }
        if (separate.size() > 1) {
            for (int i = 0; i < separate.size(); i++) {
                md5Array.add(Md5Util.getMD5(separate.get(i)));
                log.debug("正在分片,{}{}", separate.get(i).toString(), i);
            }
        }

        //预上传
        PreUploadResponse preUploadResponse = preUpload(PreUploadDto.builder()
                .path(cloudPath)
                .size(file.length())
                .isDir(0)
                .blockList(md5Array)
                .build(),stsInfo);

        log.debug("预上传{}", preUploadResponse.getUploadid());

        //分片上传
        upload(SliceUploadDto.builder()
                .path(cloudPath)
                .uploadid(preUploadResponse.getUploadid())
                .build() , separate,stsInfo,accessTokenVo);

        //创建文件
        SliceCreateResponse sliceCreateVo = create(SliceCreateDto.builder()
                .path(cloudPath)
                .size(file.length())
                .isDir(0)
                .uploadId(preUploadResponse.getUploadid())
                .blockList(md5Array)
                .build(),stsInfo);
        log.debug("创建文件{}", sliceCreateVo.getPath());

        //获取下载地址
//        String downUrl = getDownUrl(fileName);
//        log.info("获取下载地址{}", downUrl);
//        return downUrl;
        return sliceCreateVo;
    }

    private SliceCreateResponse upload(String localFilePath , String saveFilePath,StsInfo stsInfo, AccessTokenVo accessTokenVo) {
        return upload(new File(localFilePath) , saveFilePath,stsInfo,accessTokenVo);
    }

    public SliceCreateResponse defaultUpload(String localFilePath , String saveFilePath) {
        return upload(localFilePath,saveFilePath,stsService.getDefaultStsInfo(), accessTokenService.getDefaultAccessToken());
    }

    public SliceCreateResponse defaultUpload(File file , String saveFilePath) {
        return upload(file,saveFilePath,stsService.getDefaultStsInfo(), accessTokenService.getDefaultAccessToken());
    }

    /**
     * @param localFilePath 本地上传的文件路径
     * @param saveFilePath 云端文件路径
     * @param cid 企业空间id
     * @return 上传成功结果
     */
    public SliceCreateResponse upload(String localFilePath , String saveFilePath,Long cid) {
        return upload(localFilePath,saveFilePath,stsService.getStsInfo(cid),accessTokenService.getAccessToken(cid));
    }

    public PreUploadResponse preUpload(PreUploadDto preUploadDto,StsInfo stsInfo){
        StringBuilder requestBody = new StringBuilder();
        requestBody.append("path=").append( URLUtil.encode(preUploadDto.getPath()) ).append( "&size=").append( preUploadDto.getSize())
                .append("&rtype=").append(preUploadDto.getRType()).append("&autoinit=").append(preUploadDto.getAutoInit())
                .append("&block_list=").append(preUploadDto.getBlockList().toJSONString()).append("&isdir=").append(preUploadDto.getIsDir());
        if(StrUtil.isNotBlank(preUploadDto.getUploadId()))
            requestBody.append("&uploadid").append(preUploadDto.getUploadId());
        if(StrUtil.isNotBlank(preUploadDto.getContentMd5()))
            requestBody.append("&content-md5").append(preUploadDto.getContentMd5());
        if(StrUtil.isNotBlank(preUploadDto.getSliceMd5()))
            requestBody.append("&slice-md5").append(preUploadDto.getSliceMd5());
        if(StrUtil.isNotBlank(preUploadDto.getLocalCtime()))
            requestBody.append("&local_ctime").append(preUploadDto.getLocalCtime());
        if(StrUtil.isNotBlank(preUploadDto.getLocalMtime()))
            requestBody.append("&local_mtime").append(preUploadDto.getLocalMtime());
        //必须是post，文档上的get会报错
        InternalRequest request = new InternalRequest(HttpMethodName.POST, URI.create( "https://pan.baidu.com/eopen/api/precreate?sts_token="+stsInfo.getSessionToken()));
        Map<String, String> param = HttpUtil.decodeParamMap(request.getUri().toString(),"utf-8");

        return requestUtil.requestBody(param,requestBody.toString(),request,stsInfo).toJavaObject(PreUploadResponse.class);
    }

    /**
     * 预上传请求参数
     * @param preUploadDto 预上传请求参数
     * @param cid 企业空间id
     * @return PreUploadResponse
     */
    public PreUploadResponse preUpload(PreUploadDto preUploadDto,Long cid) {
        return preUpload(preUploadDto,stsService.getStsInfo(cid));
    }

    public PreUploadResponse defaultPreUpload(PreUploadDto preUploadDto) {
        return preUpload(preUploadDto,stsService.getDefaultStsInfo());
    }

    /**
     *  分片上传
     * @param uploadDto 分片上传请求参数
     * @param files 文件
     * @param cid 企业空间id
     */
    private void upload(SliceUploadDto uploadDto , List<File> files,Long cid) {
        StsInfo stsInfo = stsService.getStsInfo(cid);
        upload(uploadDto,files,stsInfo,accessTokenService.getAccessToken(cid));
    }

    private void upload(SliceUploadDto uploadDto , List<File> files, StsInfo stsInfo , AccessTokenVo accessTokenVo) {
        for (int i = 0; i < files.size(); i++) {

            InternalRequest request = new InternalRequest(HttpMethodName.POST, URI.create(getUploadUrl(uploadDto,i,accessTokenVo)));
            Map<String, String> param = HttpUtil.decodeParamMap(request.getUri().toString(),"utf-8");
            requestUtil.requestFile(param,files.get(i),request,stsInfo);
            log.debug("正在上传分片文件{}",  i);
            if(files.size() != 1){
                files.get(i).delete();
            }
        }
    }

    private String getUploadUrl(SliceUploadDto uploadDto ,int i ,AccessTokenVo accessTokenVo){
        StringBuilder url = new StringBuilder();
        try {
            url.append("https://d.pcs.baidu.com/rest/2.0/pcs/superfile2?method=upload")
                    .append("&access_token=").append(accessTokenVo.getAccessToken())
                    .append("&type=").append(uploadDto.getType())
                    .append("&partseq=" ).append( i )
                    .append("&path=" ).append(URLEncoder.encode(uploadDto.getPath(), "utf-8"))
                    .append("&uploadid=").append( uploadDto.getUploadid());
        } catch (UnsupportedEncodingException e) {
            log.error("上传路径urlEncoder失败，详情：{}" , ExceptionUtils.getFullStackTrace(e));
            throw new NetDiskException("上传路径urlEncoder失败");
        }
        return url.toString();
    }

    public List<RequestInfo> uploadRequestInfo(SliceUploadDto uploadDto, int fileNum ,Long cid) {
        return uploadRequestInfo(uploadDto,fileNum,stsService.getStsInfo(cid) , accessTokenService.getAccessToken(cid));
    }

    public List<RequestInfo> defaultUploadRequestInfo(SliceUploadDto uploadDto, int fileNum ) {
        return uploadRequestInfo(uploadDto,fileNum,stsService.getDefaultStsInfo() , accessTokenService.getDefaultAccessToken());
    }

    /**
     * 获取文件的上传地址
     * @param uploadDto 分片上传对象
     * @param fileNum 分片文件数
     * @param stsInfo sts信息
     * @param accessTokenVo ac
     * @return List<RequestInfo>
     */
    private List<RequestInfo> uploadRequestInfo(SliceUploadDto uploadDto, int fileNum ,StsInfo stsInfo, AccessTokenVo accessTokenVo) {
        List<RequestInfo> result = new ArrayList<>();
        for (int i =0 ; i< fileNum ; i++) {
            InternalRequest request = new InternalRequest(HttpMethodName.POST, URI.create(getUploadUrl(uploadDto,i , accessTokenVo)));
            Map<String, String> param = HttpUtil.decodeParamMap(request.getUri().toString(),"utf-8");
            result.add(requestUtil.getRequestInfo(param, request,stsInfo));
        }
        return result;
    }

    private void defaultUpload(SliceUploadDto uploadDto , List<File> files){
        upload(uploadDto,files,stsService.getDefaultStsInfo(),accessTokenService.getDefaultAccessToken());
    }

    public SliceCreateResponse defaultCreate(SliceCreateDto sliceCreateDto ) {
        return create(sliceCreateDto,stsService.getDefaultStsInfo());
    }

    private SliceCreateResponse create(SliceCreateDto sliceCreateDto ,StsInfo stsInfo) {
        String url =  "https://pan.baidu.com/eopen/api/create?sts_token="+stsInfo.getSessionToken();

        InternalRequest request = new InternalRequest(HttpMethodName.POST, URI.create(url));
        StringBuilder requestBody = new StringBuilder();
        requestBody.append("path=").append(sliceCreateDto.getPath()).append("&size=").append(sliceCreateDto.getSize())
                .append("&rtype=").append(sliceCreateDto.getRType()).append("&isdir=").append(sliceCreateDto.getIsDir())
                .append("&block_list=").append(sliceCreateDto.getBlockList() )
                .append("&uploadid=").append( sliceCreateDto.getUploadId());

        if(StrUtil.isNotBlank(sliceCreateDto.getLocalCtime()))
            requestBody.append("&local_ctime=").append( sliceCreateDto.getLocalCtime());
        if(StrUtil.isNotBlank(sliceCreateDto.getLocalMtime()))
            requestBody.append("&local_mtime=").append( sliceCreateDto.getLocalMtime());
        if(sliceCreateDto.getZipQuality() != null)
            requestBody.append("&zip_quality=").append( sliceCreateDto.getZipQuality());
        if(StrUtil.isNotBlank(sliceCreateDto.getZipSign()))
            requestBody.append("&zip_sign=").append( sliceCreateDto.getZipSign());
        if(StrUtil.isNotBlank(sliceCreateDto.getIsRevision()))
            requestBody.append("&is_revision=").append( sliceCreateDto.getIsRevision());
        if(sliceCreateDto.getMode() != null)
            requestBody.append("&mode=").append( sliceCreateDto.getMode());
        if(sliceCreateDto.getExifInfo() != null)
            requestBody.append("&exif_info=").append( sliceCreateDto.getExifInfo());

        Map<String, String> param = HttpUtil.decodeParamMap(request.getUri().toString(),"utf-8");
        return requestUtil.requestBody(param,requestBody.toString(),request,stsInfo).toJavaObject(SliceCreateResponse.class);
    }

    public SliceCreateResponse create(SliceCreateDto sliceCreateDto ,Long cid) {
        StsInfo stsInfo = stsService.getStsInfo(cid);
        return create(sliceCreateDto,stsInfo);
    }

}
