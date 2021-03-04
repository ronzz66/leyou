package com.leyou.cart.interceptor;

import com.leyou.cart.config.JwtProperties;
import com.leyou.common.pojo.UserInfo;
import com.leyou.common.utils.CookieUtils;
import com.leyou.common.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//拦截器:作用把登录用户和当前线程绑定 ;只有在登录状态下才会使用这个拦截器
@Component
@EnableConfigurationProperties(JwtProperties.class)
public class LoginInterceptor extends HandlerInterceptorAdapter {//重写指定方法可以继承这个类
    private static final ThreadLocal<UserInfo> THREAD_LOCAL = new ThreadLocal<>();//当前线程和当前对象绑定

    @Autowired
    private JwtProperties jwtProperties;
    //重写前置方法
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //获取从coolie中获取token
        String token = CookieUtils.getCookieValue(request, jwtProperties.getCookieName());
        //解析token,获取用户对象
        UserInfo userInfo = JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey());

        if (userInfo==null){
            return false;
        }
        //把userInfo放入线程局部遍量
        THREAD_LOCAL.set(userInfo);
        return true;
    }


    //获取UserInfo方法
    public static UserInfo getUserInfo(){
        return THREAD_LOCAL.get();
    }

    @Override//完成方法
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //清空线程的局部变量,使用线程池必须要清空
        THREAD_LOCAL.remove();
    }
}
