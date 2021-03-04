package com.leyou.auth.controller;


import com.leyou.auth.config.JwtProperties;
import com.leyou.auth.service.AuthService;
import com.leyou.common.pojo.UserInfo;
import com.leyou.common.utils.CookieUtils;
import com.leyou.common.utils.JwtUtils;
import com.netflix.client.http.HttpRequest;
import com.netflix.client.http.HttpResponse;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@EnableConfigurationProperties(JwtProperties.class)
public class AuthController {

    @Autowired
    private AuthService authService;
    @Autowired
    private JwtProperties jwtProperties;


    @PostMapping("accredit")//校验登录  无状态登录
    public ResponseEntity<Void> accredit(@RequestParam("username")String username,
                                         @RequestParam("password")String password,
                                         HttpServletRequest request,
                                         HttpServletResponse response){
        //获取校验用户token
        String token = authService.accredit(username,password);
        if (StringUtils.isBlank(token)){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();//身份认证未通过
        }
        //把公钥token设置到用户cookie  //cookie 无效时间,单位s
        CookieUtils.setCookie(request,response,jwtProperties.getCookieName(),token,jwtProperties.getExpire()*60);
        return ResponseEntity.ok().build();

    }

    @GetMapping("verify")//获取用户名 重置cookie和token
    public ResponseEntity<UserInfo> verify(@CookieValue("LY_TOKEN")String token, HttpServletRequest request, HttpServletResponse response){
        try {
            UserInfo userInfo = JwtUtils.getInfoFromToken(token, this.jwtProperties.getPublicKey());//jwt工具类实现解析公钥
            if (userInfo==null){
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();//校验未通过
            }
            //校验成功,返回userinfo对象
            // 刷新jwt
            token = JwtUtils.generateToken(userInfo, jwtProperties.getPrivateKey(), jwtProperties.getExpire());
            //cookie的有效时间;
            CookieUtils.setCookie(request,response,jwtProperties.getCookieName(),token,this.jwtProperties.getExpire()*60);
            return ResponseEntity.ok(userInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }


}
