package com.leyou.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration//文件上传跳过网关需要重写解决跨越
public class LeyouCorsConfiguration {

    @Bean
    public CorsFilter corsFilter(){ //特殊请求 跨域
        //初始化cors配置对象
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOrigin("http://manage.leyou.com");//允许跨越的域名,如果要携带cookie 就不能写*
        configuration.setAllowCredentials(true);//是否允许携带cookie
        configuration.addAllowedMethod("*");//所有请求方法
        configuration.addAllowedHeader("*");//允许携带任何头信息

        //初始化cors配置源对象
        UrlBasedCorsConfigurationSource corsConfigurationSource = new UrlBasedCorsConfigurationSource();
        corsConfigurationSource.registerCorsConfiguration("/**",configuration);
        //CorsFilter实例,参数为cors配置源对象
        return new CorsFilter(corsConfigurationSource);
    }
}
