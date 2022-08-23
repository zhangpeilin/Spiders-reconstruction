package cn.zpl.tencent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("cn.zpl")
public class TencentApplication {
    public static void main(String[] args) {
        SpringApplication.run(TencentApplication.class, args);
    }
}
