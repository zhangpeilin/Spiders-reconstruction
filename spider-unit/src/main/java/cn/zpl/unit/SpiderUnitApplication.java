package cn.zpl.unit;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;
import org.springframework.context.annotation.ComponentScan;

@EnableConfigServer
@EnableEurekaServer
@MapperScan("cn.zpl.dao.mapper")
@ComponentScan("cn.zpl")
@SpringBootApplication
public class SpiderUnitApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpiderUnitApplication.class, args);
    }
}
