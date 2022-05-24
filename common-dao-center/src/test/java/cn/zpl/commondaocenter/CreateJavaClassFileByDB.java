package cn.zpl.commondaocenter;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;

import java.util.Collections;

public class CreateJavaClassFileByDB {

    public static void main(String[] args) {
        FastAutoGenerator.create("jdbc:mysql://192.168.3.153:4567/bilibili?useUnicode=true&serverTimezone=Asia/Shanghai&useSSL=false", "root", "zxcvbnm,./")
                .globalConfig(builder -> {
                    builder.author("zpl") // 设置作者
                            .enableSwagger() // 开启 swagger 模式
                            .fileOverride() // 覆盖已生成文件
                            .outputDir("e://picture_analyze"); // 指定输出目录
                })
                .packageConfig(builder -> {
                    builder.parent("cn.zpl") // 设置父包名
                            .moduleName("commondaocenter") // 设置父包模块名
                            .pathInfo(Collections.singletonMap(OutputFile.mapper, "e://picture_analyze")); // 设置mapperXml生成路径
                })
                .strategyConfig(builder -> {
                    builder.addInclude("picture_analyze") // 设置需要生成的表名
                            .addTablePrefix("t_", "c_"); // 设置过滤表前缀
                })
                .templateEngine(new FreemarkerTemplateEngine()) // 使用Freemarker引擎模板，默认的是Velocity引擎模板
                .execute();

    }
}
