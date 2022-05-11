# 百度企业网盘sdk

### 前言
- 支持多个企业网盘同时管理

## 一、使用例子

- 例子1：Spring boot项目【推荐】
- 例子2：spring项目

### 1-1 Spring boot项目
1-1-1 引入spring boot自动装配依赖

```xml
<dependency>
    <groupId>io.github.zhonghang1993</groupId>
    <artifactId>baidu-netdisk-cp-spring-boot-starter</artifactId>
    <version>1.7.2</version>
</dependency>
```

1-1-2 配置文件
```properties
# appId
baidu.netdisk.cp.net.disk.app-id=
# appName
baidu.netdisk.cp.net.disk.app-name=
# appKey
baidu.netdisk.cp.net.disk.app-key=
# secretKey
baidu.netdisk.cp.net.disk.secret-key=
# signKey
baidu.netdisk.cp.net.disk.sign-key=
# 授权回调地址
baidu.netdisk.cp.net.disk.redirect-uri=
#【重点】如果不配置，则默认存储在内存中。配置实现StorageDaoI接口的类全量路径名；查看下文《元数据存储扩展性》
baidu.netdisk.cp.net.disk.storage-rule=com.XX.XX.XX.service.impl.RedisStorage

# 上传文件的路径前缀
baidu.netdisk.cp.net.disk.file-prefix=/bbs
# 分片大小（单位M），最大限制：普通用户4M，普通会员16M，超级会员32M
baidu.netdisk.cp.net.disk.unit=32

```

#### 1-1-3 使用

```java
@Autowired
private BaiduNetDisk baiduNetDisk;
```

### 1-2 Spring 项目(ssm、ssh)

#### 1-2-1 引入依赖

```xml
<dependency>
    <groupId>io.github.zhonghang1993</groupId>
    <artifactId>baidu-netdisk</artifactId>
    <version>1.7.2</version>
</dependency>
```

#### 1-2-2 构建

```java
BaiduConfig baiduConfig = new BaiduConfig(appId,appName,appKey,secretKey,singKey,redirectUri,filePrefix,unit);
//【拿到操作所有接口的类】没有传自定义实现StorageDaoI，则使用默认的存储规则
BaiduNetDisk baiduNetDisk = new BaiduNetDisk(baiduConfig);
//BaiduNetDisk baiduNetDisk = new BaiduNetDisk(baiduConfig,storageDaoI);
```

## 二、 调用说明

### 2-1 service说明

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

### 2-2 单网盘管理，多网盘管理说明

- 管理单个用户网盘，使用default开头的
  - baiduNetDisk.getAccessTokenService().default***();
  - baiduNetDisk.getFileService().default***();
- 管理多个用户的网盘，则使用
  - baiduNetDisk.getFileService().\***(\*** ,Long cid);

<font style="color:red">**cid是什么？cid每个授权用户的网盘唯一ID，在获取公司信息中有。**</font>所以每次操作传cid即可


## 三、元数据存储扩展性
- 目前SDK给的默认规则是存储在内存中的，所以不建议生产使用
- 想把token存储在内存、存储在文件、存储在redis、存储数据库......？

` 实现抽象类 StorageDaoI 即可`

### 3-1 示例：使用redis存储

[请点击看模块中demo示例](demo.md)

- 分布式系统、或者集群建议存储在redis中

## 四、常见问题


### 4-1 【解绑】如何清空授权信息？

```java
//清除指定企业网盘
storageDaoI.clear(Long cid);
```
如果你觉得直接设置null无法处理你的业务场景，可以重写clear方法。

### 4-2 怎么显示打印日志？

在你的日志配置文件，扫描路径加上`com.zhonghang.baidu`就可以了

例子：如你使用的是logback.xml配置的：
```xml
<logger name="com.zhonghang.baidu" level="DEBUG"/>
```

## 五、加群

- 点击链接加入群聊【百度企业网盘SDK】：https://jq.qq.com/?_wv=1027&k=r6RmOwvn
- 群号：897493498
- 扫码入群：

![avatar](group.png)

## 四、支持作者
- 如果节省了你的时间，对你有帮助，请给我点支持。^_^ 一分不嫌少，一块不嫌多

![avatar](code.jpg)
