package com.zhonghang.baidu.netdisk.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONArray;
import com.baidubce.http.HttpMethodName;
import com.baidubce.internal.InternalRequest;
import com.zhonghang.baidu.netdisk.config.BaiduConfig;
import com.zhonghang.baidu.netdisk.dto.PreUploadDto;
import com.zhonghang.baidu.netdisk.dto.SliceCreateDto;
import com.zhonghang.baidu.netdisk.dto.SliceUploadDto;
import com.zhonghang.baidu.netdisk.dto.StsInfo;
import com.zhonghang.baidu.netdisk.http.StsRequest;
import com.zhonghang.baidu.netdisk.response.PreUploadResponse;
import com.zhonghang.baidu.netdisk.response.SliceCreateResponse;
import com.zhonghang.baidu.netdisk.util.FileSeparateUtil;
import com.zhonghang.baidu.netdisk.util.Md5Util;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.net.URI;
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

    /**
     * @param localFilePath 本地上传的文件路径
     * @param saveFilePath 云端文件路径
     * @return
     */
    public SliceCreateResponse upload(String localFilePath , String saveFilePath) {

        String cloudPath = URLUtil.encode(baiduConfig.getFilePrefix() + saveFilePath);

        //文件分片并获取md5值
        File file = new File(localFilePath);
        File[] separate = FileSeparateUtil.separate(localFilePath, baiduConfig.getUnit());

        JSONArray md5Array = new JSONArray();
        if (separate.length == 1) {
            md5Array.add(Md5Util.getMD5(separate[0]));

        }
        if (separate.length > 1) {
            for (int i = 0; i < separate.length; i++) {
                md5Array.add(Md5Util.getMD5(separate[i]));
                log.debug("正在分片,{}{}", separate[i].toString(), i);
            }
        }

        //预上传
        PreUploadResponse preUploadResponse = preUpload(PreUploadDto.builder()
                .path(cloudPath)
                .size(file.length())
                .isDir(0)
                .blockList(md5Array)
                .build());

        log.debug("预上传{}", preUploadResponse.getUploadid());

        //分片上传
        upload(SliceUploadDto.builder()
                .path(cloudPath)
                .uploadid(preUploadResponse.getUploadid())
                .build() , separate);

        //创建文件
        SliceCreateResponse sliceCreateVo = create(SliceCreateDto.builder()
                .path(cloudPath)
                .size(file.length())
                .isDir(0)
                .uploadId(preUploadResponse.getUploadid())
                .blockList(md5Array)
                .build());
        log.debug("创建文件{}", sliceCreateVo.getPath());

        //获取下载地址
//        String downUrl = getDownUrl(fileName);
//        log.info("获取下载地址{}", downUrl);
//        return downUrl;
        return sliceCreateVo;
    }


    /**
     * @Description: TODO 预上传
     * @param: cloudPath 云端路径
     * @param: size 文件大小 字节
     * @param: isDir 0文件 1目录（设置为目录是 size要设置为0）
     * @param: blockList （文件的md5值） 可以把文件分为多个，然后分批上传
     * @return: java.lang.String
     */
    public PreUploadResponse preUpload(PreUploadDto preUploadDto) {
        StsInfo stsInfo = stsService.getStsInfo();
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

        InternalRequest request = new InternalRequest(HttpMethodName.POST, URI.create( "https://pan.baidu.com/eopen/api/precreate?sts_token="+stsInfo.getSessionToken()));
        Map<String, String> param = HttpUtil.decodeParamMap(request.getUri().toString(),"utf-8");

        return requestUtil.requestBody(param,requestBody.toString(),request).toJavaObject(PreUploadResponse.class);
    }

    /**
     * @Description: TODO 分片上传
     * @param: path 上传到百度网盘的地址
     * @param: uploadid 上传的id
     * @param: filePath 本地文件的地址
     * @return: java.lang.String
     */
    private void upload(SliceUploadDto uploadDto , File[] files) {
        for (int i = 0; i < files.length; i++) {
            StringBuilder url = new StringBuilder();
            url.append("https://d.pcs.baidu.com/rest/2.0/pcs/superfile2?method=upload")
                    .append("&access_token=").append(accessTokenService.getAccessToken().getAccessToken())
                    .append("&type=").append(uploadDto.getType())
                    .append("&partseq=" ).append( i )
                    .append("&path=" ).append(uploadDto.getPath())
                    .append("&uploadid=").append( uploadDto.getUploadid());
            InternalRequest request = new InternalRequest(HttpMethodName.POST, URI.create(url.toString()));
            Map<String, String> param = HttpUtil.decodeParamMap(request.getUri().toString(),"utf-8");
            requestUtil.requestFile(param,files[i],request);
            log.debug("正在上传分片文件{}",  i);
        }
    }

    public SliceCreateResponse create(SliceCreateDto sliceCreateDto ) {
        String url =  "https://pan.baidu.com/eopen/api/create?sts_token="+stsService.getStsInfo().getSessionToken();

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
        return requestUtil.requestBody(param,requestBody.toString(),request).toJavaObject(SliceCreateResponse.class);

    }

}
