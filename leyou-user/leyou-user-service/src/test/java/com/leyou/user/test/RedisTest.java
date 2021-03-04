package com.leyou.user.test;

import com.leyou.LeyouUserApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = LeyouUserApplication.class)
public class RedisTest {

    @Autowired
    private StringRedisTemplate redisTemplate;//StringRedisTemplate继承redisTemplate默认序列化为string类型

    @Test
    public void testRedis() {
        // 存储数据
        this.redisTemplate.opsForValue().set("key2", "value1");
        // 获取数据
        String val = this.redisTemplate.opsForValue().get("key2").toString();
        System.out.println("val = " + val);
    }

    @Test
    public void testRedis2() {
        // 存储数据，并指定剩余生命时间,5小时
        this.redisTemplate.opsForValue().set("key11", "value2",
                20, TimeUnit.SECONDS);//设置失效时间
    }

    @Test
    public void testHash() {//redis中的hashmap 类似于java中的双层hashmap
        BoundHashOperations<String, Object, Object> hashOps =
                this.redisTemplate.boundHashOps("user");//此方法先在查找user,如果不存在这个对象使用put方法会创建对象
        // 操作hash数据
        hashOps.put("name", "jack");
        hashOps.put("age", "21");

        // 获取单个数据
        Object name = hashOps.get("name");
        System.out.println("name = " + name);

        // 获取所有数据
        Map<Object, Object> map = hashOps.entries();
        for (Map.Entry<Object, Object> me : map.entrySet()) {
            System.out.println(me.getKey() + " : " + me.getValue());
        }
    }
}