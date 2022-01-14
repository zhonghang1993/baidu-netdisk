# 百度企业网盘sdk

### 前言
百度网盘开放平台接口没有任何语言的SDK，只有赤裸裸的API。
- 百度网盘文档写的不清晰，对接只能靠猜？
- 接口总是调不通，不知道问题在哪里，写了反馈，几个月了没音信？
- 某些参数文档写的明明不是必传，但是不传它就是不通？
- 接口返回的错误参数看不懂，有时直接给你来500？
- ...

作为码农是不是很难受？不要气馁，因为并不是你的问题，~~而是这个文档和接口写的真TMD的XX！！！~~

收！气了发了，活还是要干的。本人能力有限，如果在使用SDK中发现问题，或不满足你们公司某些业务场景，请在github中写`Issues` ，我会第一时间收到，并思考解决方案。带来不便敬请谅解！

## 一、使用例子

### 1-1 构建

```java
BaiduConfig baiduConfig = new BaiduConfig(appId,appName,appKey,secretKey,singKey,redirectUri,filePrefix,unit);
//【拿到操作所有接口的类】没有传自定义实现StorageDaoI，则使用默认的存储规则
BaiduNetDisk baiduNetDisk = new BaiduNetDisk(baiduConfig);
//BaiduNetDisk baiduNetDisk = new BaiduNetDisk(baiduConfig,storageDaoI);
```

### 1-2 调用说明

```java
//获取accessToken的类，获取二维码扫码地址、通过code鉴权
baiduNetDisk.getAccessTokenService();
//获取文件管理的类，文件的移动、拷贝、删除、列表搜索、文件详情、下载地址...
baiduNetDisk.getFileService();
//分片上传文件
baiduNetDisk.getSuperFileService();
//获取当前授权账号的企业信息
baiduNetDisk.getOrganizationInfoService();

//你基本上用不上，请求接口时，计算签名用的
baiduNetDisk.getStsService();
```

## 二、元数据存储扩展性
- 目前SDK给的默认规则是存储在内存中的，所以不建议生产使用
- 想把token存储在内存、存储在文件、存储在redis、存储数据库......？

` 实现抽象类 StorageDaoI 即可`

### 2-1 示例：使用redis存储

[请点击看模块中demo示例](demo.md)

- 分布式系统、或者集群建议存储在redis中

## 三、常见问题

### 3-1 如何同时管理多个用户的授权空间？

目前你就new多个BaiduNetDisk，虽然不优雅，但是可以解决问题。 ~~正在重构中~~

### 3-2 【解绑】如何清空授权信息？

```java
storageDaoI.clear();
```
如果你觉得直接设置null无法处理你的业务场景，可以重写clear方法。

### 3-3 怎么显示打印日志？

在你的日志配置文件，扫描路径加上`com.zhonghang.baidu`就可以了

例子：如你使用的是logback.xml配置的：
```xml
<logger name="com.zhonghang.baidu" level="DEBUG"/>
```

## 四、支持作者
- 如果节省了你的时间，对你有帮助，请给我点支持。^_^ 一分不嫌少，一块不嫌多


