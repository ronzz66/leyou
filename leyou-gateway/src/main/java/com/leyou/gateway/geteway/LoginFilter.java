package com.leyou.gateway.geteway;

import com.leyou.common.utils.CookieUtils;
import com.leyou.common.utils.JwtUtils;
import com.leyou.gateway.config.FilterProperties;
import com.leyou.gateway.config.JwtProperties;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Component
@EnableConfigurationProperties({JwtProperties.class, FilterProperties.class})//引入jwt配置，白名单配置
public class LoginFilter  extends ZuulFilter{

    @Autowired
    private JwtProperties jwtProperties;
    @Autowired
    private FilterProperties filterProperties;

    @Override//路由类型
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 10;
    }

    @Override//是否执行run方法
    public boolean shouldFilter() {
        //1.获取白名单路径
        List<String> allowPaths = filterProperties.getAllowPaths();

        //初始化运行上下文
        RequestContext currentContext = RequestContext.getCurrentContext();
        //获取request
        HttpServletRequest request = currentContext.getRequest();
        //2.获取请求路径
        String url = request.getRequestURL().toString();

        //判断是否在白名单中
        for (String allowPath : allowPaths) {
            if (StringUtils.contains(url,allowPath)){
                return false;//包含白名单不拦截
            }
        }

        return true;//拦截
    }

    @Override//具体拦截方法
    public Object run() throws ZuulException {
        //初始化运行上下文
        RequestContext currentContext = RequestContext.getCurrentContext();
        //获取request
        HttpServletRequest request = currentContext.getRequest();
        //获取cookie
        String token = CookieUtils.getCookieValue(request, jwtProperties.getCookieName());
        if (StringUtils.isBlank(token)){//如果没有cookie说明未登录
            currentContext.setSendZuulResponse(false);//不转发
            currentContext.setResponseStatusCode(HttpStatus.UNAUTHORIZED.value());//响应校验未通过

        }
        try {
            JwtUtils.getInfoFromToken(token,jwtProperties.getPublicKey());//解析公钥
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
