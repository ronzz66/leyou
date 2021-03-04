package com.leyou.service;

import com.leyou.common.utils.NumberUtils;
import com.leyou.mapper.Usermapper;
import com.leyou.user.pojo.User;
import com.leyou.utils.CodecUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class UserService {

    @Autowired
    private Usermapper  usermapper;


    @Autowired
    private AmqpTemplate amqpTemplate;

    private static final String key_prefix = "USER:VERIFY";//保存到redis的前缀

    @Autowired
    private StringRedisTemplate redisTemplate;
    /**
     * 校验数据是否可用
     * @param data
     * @param type
     * @return
     */
    public Boolean checkUser(String data, Integer type) {
        User recod = new User();
        if (type==1){//校验用户名
            recod.setUsername(data);
        }else if (type==2){//校验手机号
            recod.setPhone(data);
        }else {
            return null;
        }
        //查询手机或者用户名是否已经注册过
        return usermapper.selectCount(recod)==0;

    }

    //发送验证码
    public void sendVerifyCode(String phone) {

        if (StringUtils.isBlank(phone)){
            return;
        }

        //生成验证码
        String code = NumberUtils.generateCode(6);
        //发送消息到RabbitMQ
        Map<String,String> map=new HashMap();
        map.put("phone",phone);
        map.put("code",code);
        //rabbitmq发送消息，监听到消息后发送验证码
        amqpTemplate.convertAndSend("leyou.sms.exchange","verifycode.sms",map);
        //保存到redis
        redisTemplate.opsForValue().set(key_prefix+phone,code,5, TimeUnit.MINUTES);//5分钟过期
    }

    //注册验证码
    public void register(User user, String code) {
        //从redis从获取验证码
        String redisCode = redisTemplate.opsForValue().get(key_prefix + user.getPhone());

        //校验验证码
        if (!StringUtils.equals(code,redisCode)){
            return;
        }
        //生成盐
        String salt = CodecUtils.generateSalt();
        user.setSalt(salt);
        //加盐加密
        String md5Hex = CodecUtils.md5Hex(user.getPassword(), salt);
        user.setPassword(md5Hex);//设置给password
        //新增用户
        user.setId(null);
        user.setCreated(new Date());
        usermapper.insert(user);
        redisTemplate.delete(key_prefix + user.getPhone());//删除redis中的验证码
    }

    //登录校验
    public User queryUser(String username, String password) {

        User record = new User();
        record.setUsername(username);
        //先根据用户名查询出用户
        User user = usermapper.selectOne(record);
        //判断用户是否为空
        if(user==null) {//为空直接返回
            return null;
        }
        //获取盐对用户加盐加密
        String salt = user.getSalt();
        password = CodecUtils.md5Hex(password, user.getSalt());
        //和数据库中的密码进行比较
        if (StringUtils.equals(user.getPassword(),password)){
            return user;
        }
        return null;


    }
}
