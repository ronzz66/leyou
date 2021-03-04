package com.leyou;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication //
@EnableEurekaServer //启用eureka 注册中心服务端
public class LeyouResgistryApplication {

    public static void main(String[] args) {
        SpringApplication.run(LeyouResgistryApplication.class);
    }
}
