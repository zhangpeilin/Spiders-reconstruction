package cn.zpl.util;

import cn.zpl.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Properties;

@Slf4j
public class InitConfigFactory {


    public void test() {
        loadPropertiesByLoader(getClass().getClassLoader(), "config.properties");
    }

    public static void loadPropertiesByLoader(ClassLoader loader, String propertyName) {
        try {
            Enumeration<URL> urls = loader.getResources(propertyName);
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                UrlResource resource = new UrlResource(url);
                Properties properties = PropertiesLoaderUtils.loadProperties(resource);
                properties.forEach((o, o2) -> {
                    System.out.println(o);
                    System.out.println(o2);
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 只读取本module下的配置文件，不包含其依赖模块中的config.properties
     *
     * @param loader 类加载器，传入的加载器不同，读取不同位置的配置文件
     */
    public static void loadPropertyByLoader(ClassLoader loader, String propertyName) {
        InputStream in = loader.getResourceAsStream(propertyName);
        Properties properties = new Properties();
        try {
            properties.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        properties.forEach((o, o2) -> {
            System.out.println(o);
            System.out.println(o2);
        });
    }

    /**
     * 首先读取当前模块下的config.properties文件，如果没有，则读取命令执行处目录下的config.properties
     *
     * @param clazz        需要加载参数的静态变量类
     * @param propertyName 需要加载的配置文件名，默认都是config.properties
     */
    public static void loadPropertyByClass(@NotNull Class clazz, String propertyName) {
        InputStream in = clazz.getClassLoader().getResourceAsStream(propertyName);
        InputStreamReader reader = null;
        try {
            if (in == null) {
                in = new BufferedInputStream(new FileInputStream(System.getProperty("user.dir") + File.separator + "config.properties"));
            }
            reader = new InputStreamReader(in, StandardCharsets.UTF_8);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Field[] fields = clazz.getDeclaredFields();

        Properties properties = new Properties();
        try {
            properties.load(reader);
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (Field field : fields) {
//            if (field.getName().equalsIgnoreCase("logger") && field.getType() == Logger.class) {
//                try {
//                    logger = (Logger) field.get(clazz);
//                } catch (IllegalAccessException e) {
//                    e.printStackTrace();
//                }
//            }
            if (field.isAnnotationPresent(Value.class)) {
                String filedName = field.getAnnotation(Value.class).value();
                try {
//                    logger.debug("加载属性：" + filedName + "------>值：" + properties.getProperty(filedName));
                    if (field.getType() == int.class) {
                        field.set(clazz, Integer.parseInt(properties.getProperty(filedName)));
                    } else if(field.getType() == boolean.class){
                        field.set(clazz, Boolean.valueOf(properties.getProperty(filedName)));
                    }
                    else {
                        field.set(clazz, properties.getProperty(filedName));
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
//        properties.forEach((o, o2) -> {
//            System.out.println(o);
//            System.out.println(o2);
//        });
    }
}
