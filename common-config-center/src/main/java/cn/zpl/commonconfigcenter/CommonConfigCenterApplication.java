package cn.zpl.commonconfigcenter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@SpringBootApplication
@EnableConfigServer
public class CommonConfigCenterApplication {

    public static void main(String[] args) {
        SpringApplication.run(CommonConfigCenterApplication.class, args);
    }

}
