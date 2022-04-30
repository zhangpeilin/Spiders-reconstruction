package cn.zpl.commondaocenter;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("cn.zpl.commondaocenter.mapper")
public class CommonDaoCenterApplication {

    public static void main(String[] args) {
        SpringApplication.run(CommonDaoCenterApplication.class, args);
    }

}
