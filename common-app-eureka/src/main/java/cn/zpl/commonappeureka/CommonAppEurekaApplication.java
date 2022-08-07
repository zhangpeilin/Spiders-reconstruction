package cn.zpl.commonappeureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class CommonAppEurekaApplication {

    public static void main(String[] args) {
        SpringApplication.run(CommonAppEurekaApplication.class, args);
    }

}
