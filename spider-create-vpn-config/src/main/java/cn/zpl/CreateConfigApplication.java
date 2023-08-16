package cn.zpl;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("cn.zpl.dao.mapper")
public class CreateConfigApplication {
    public static void main(String[] args) {
        SpringApplication.run(CreateConfigApplication.class);
    }
}