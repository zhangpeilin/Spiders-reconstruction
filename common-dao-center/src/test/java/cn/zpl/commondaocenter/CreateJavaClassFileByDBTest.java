package cn.zpl.commondaocenter;

import cn.zpl.commondaocenter.config.MyProperties;
import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;
import com.baomidou.mybatisplus.generator.util.FileUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.Collections;

@SpringBootTest
public class CreateJavaClassFileByDBTest {

    @Value("${spring.datasource.url}")
    String url;
    @Value("${spring.datasource.username}")
    String userName;
    @Value("${spring.datasource.password}")
    String password;
    String tableName = "cron";

    @Resource
    MyProperties myProperties;
    @Test
    void test() throws IOException {
        String outPutDir = myProperties.getCreateTablePath() + tableName;

        if (!new File(outPutDir).exists()) {
            FileUtils.forceMkdir(new File(outPutDir));
        }
        FastAutoGenerator.create(url, userName, password)
                .globalConfig(builder -> {
                    builder.author("zpl") // 设置作者
                            .enableSwagger() // 开启 swagger 模式
                            .fileOverride() // 覆盖已生成文件
                            .outputDir(outPutDir); // 指定输出目录
                })
                .packageConfig(builder -> {
                    builder.parent("cn.zpl") // 设置父包名
                            .moduleName("commondaocenter") // 设置父包模块名
                            .pathInfo(Collections.singletonMap(OutputFile.mapper, outPutDir)); // 设置mapperXml生成路径
                })
                .strategyConfig(builder -> {
                    builder.addInclude(tableName) // 设置需要生成的表名
                            .addTablePrefix("t_", "c_"); // 设置过滤表前缀
                })
                .templateEngine(new FreemarkerTemplateEngine()) // 使用Freemarker引擎模板，默认的是Velocity引擎模板
                .execute();

    }
}
