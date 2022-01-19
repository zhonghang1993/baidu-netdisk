## 使用redis存储token等信息

```java
@Component
public class RedisStorage extends StorageDaoI {
    @Value("{$spring.profiles.active}")
    private String profile; //多环境下存储的路径不一样，防止被覆盖，当然你也可以把它删了
    @Autowired
    private RedisTemplateUtil redisTemplateUtil; // 自己定义封装的对象，可以看下面的代码

    private static String prefix = "baidu.net.disk"; //redis的key的前缀
    private String getAccessTokenKey(Long cid){
        return profile+"."+prefix+".accessToken."+cid;
    }

    private String getAllOrganizationInfoKey(){
        return profile+"."+prefix+".orgInfo.*";
    }

    private String getOrganizationInfoKey(Long cid){
        return profile+"."+prefix+".orgInfo."+cid;
    }

    private String getStsInfoKey(Long cid){
        return profile+"."+prefix+".stsInfo."+cid;
    }

    @Override
    public void saveAccessToken(AccessTokenVo accessTokenVo, Long cid) {
        redisTemplateUtil.setValue(getAccessTokenKey(cid), JSONObject.toJSONString(accessTokenVo));
    }

    @Override
    public AccessTokenVo getAccessToken(Long cid) {
        return redisTemplateUtil.getKey(getAccessTokenKey(cid),AccessTokenVo.class);
    }

    @Override
    public void saveOrganizationInfo(OrganizationInfo organizationInfo) {
        redisTemplateUtil.setValue(getOrganizationInfoKey(organizationInfo.getCid()), JSONObject.toJSONString(organizationInfo));
    }

    @Override
    public OrganizationInfo getOrganizationInfo(Long cid) {
        return redisTemplateUtil.getKey(getOrganizationInfoKey(cid),OrganizationInfo.class);
    }

    /**
     * 设置默认存储的规则，存储到默认的网盘
     * @return
     */
    @Override
    public OrganizationInfo getDefaultOrganizationInfo() {
        Set<String> keys = redisTemplateUtil.getKeys(getAllOrganizationInfoKey());
        if(keys.size() > 1){
            throw new NetDiskException("超过了1个百度授权账号，请配置多个策略");
        }
        OrganizationInfo organizationInfo = redisTemplateUtil.getKey((String) keys.toArray()[0], OrganizationInfo.class);
        return organizationInfo;
    }

    @Override
    public void saveStsInfo(StsInfo stsInfo, Long cid) {
        redisTemplateUtil.setValue(getStsInfoKey(cid), JSONObject.toJSONString(stsInfo));
    }

    @Override
    public StsInfo getStsInfo(Long cid) {
        return redisTemplateUtil.getKey(getStsInfoKey(cid),StsInfo.class);
    }
}

```

### RedisTemplateUtil

```java
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.support.atomic.RedisAtomicInteger;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
public class RedisTemplateUtil {
    @Autowired
    private StringRedisTemplate template;
 
    /**
     * 获取值
     * @param key
     * @return
     */
    public String getKey(String key){
        return template.opsForValue().get(key);
    }

    public <T> T getKey(String key ,Class<T> tClass){
        return JSONObject.parseObject(template.opsForValue().get(key),tClass);
    }

    public void setValue(String key ,String value){
        template.opsForValue().set(key,value);
    }

    public void setValue(String key ,String value ,long timeout , TimeUnit timeUnit){
        template.opsForValue().set(key,value,timeout, timeUnit);
    }

    public void remove(String key){
        template.delete(key);
    }

    public void fuzzyRemove(String key){
        template.delete(template.keys(key));
    }

    public void expire(String key,int hours){
        template.expire(key ,hours , TimeUnit.HOURS);
    }

    /**
     * 批量获取值
     *
     * @param key
     * @return
     */
    public Set<String> getKeys(String key){
        return template.keys(key);
    }
}
```


