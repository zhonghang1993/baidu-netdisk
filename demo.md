## 使用redis存储token等信息

```java
@Component
public class RedisStorage extends StorageDaoI {
    @Value("{spring.profiles.active}")
    private String profile; //多环境下存储的路径不一样，防止被覆盖，当然你也可以把它删了
    @Autowired
    private RedisTemplateUtil redisTemplateUtil; // 自己定义封装的对象，可以看下面的代码

    private static String prefix = "baidu.net.disk"; //redis的key的前缀
    private String getAccessTokenKey(){
        return profile+"."+prefix+".accessToken"; //拼接得到key
    }

    private String getOrganizationInfoKey(){ //拼接得到key
        return profile+"."+prefix+".orgInfo";
    }

    private String getStsInfoKey(){ //拼接得到key
        return profile+"."+prefix+".stsInfo";
    }
    
    //实现存储和获取值的方法
    @Override
    public void saveAccessToken(AccessTokenVo accessTokenVo) {
        redisTemplateUtil.setValue(getAccessTokenKey(), JSONObject.toJSONString(accessTokenVo));
    }

    @Override
    public AccessTokenVo getAccessToken() {
        return redisTemplateUtil.getKey(getAccessTokenKey(),AccessTokenVo.class);
    }

    @Override
    public void saveOrganizationInfo(OrganizationInfo organizationInfo) {
        redisTemplateUtil.setValue(getOrganizationInfoKey(), JSONObject.toJSONString(organizationInfo));
    }

    @Override
    public OrganizationInfo getOrganizationInfo() {
        return redisTemplateUtil.getKey(getOrganizationInfoKey(),OrganizationInfo.class);
    }

    @Override
    public void saveStsInfo(StsInfo stsInfo) {
        redisTemplateUtil.setValue(getStsInfoKey(), JSONObject.toJSONString(stsInfo));
    }

    @Override
    public StsInfo getStsInfo() {
        return redisTemplateUtil.getKey(getStsInfoKey(),StsInfo.class);
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


