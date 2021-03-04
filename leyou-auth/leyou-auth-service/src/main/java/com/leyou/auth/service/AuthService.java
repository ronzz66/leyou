package com.leyou.auth.service;

import com.leyou.auth.cilent.UserClient;
import com.leyou.auth.config.JwtProperties;
import com.leyou.common.pojo.UserInfo;
import com.leyou.common.utils.JwtUtils;
import com.leyou.user.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    @Autowired
    private UserClient  userClient;
    @Autowired
    private JwtProperties jwtProperties;

    //私钥加密获取taken
    public String accredit(String username, String password) {
        //根据用户名和密码查询
        User user = userClient.queryUser(username, password);
        //判断user
        if (user==null){//user存在，登录成功
            return null;
        }
        try {
            //生成token
            UserInfo userInfo = new UserInfo();
            userInfo.setId(user.getId());
            userInfo.setUsername(user.getUsername());
            //用户信息保存在token中 私钥进行加密,返回token
            return JwtUtils.generateToken(userInfo,jwtProperties.getPrivateKey(),jwtProperties.getExpire());

        } catch (Exception e) {
            e.printStackTrace();
        }


        return null;
    }
}
