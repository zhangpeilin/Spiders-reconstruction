package cn.zpl.commondaocenter;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@MapperScan("cn.zpl.commondaocenter.mapper")
@ComponentScan("cn.zpl")
public class CommonDaoCenterApplication {

    public static void main(String[] args) {
        SpringApplication.run(CommonDaoCenterApplication.class, args);
    }

}
