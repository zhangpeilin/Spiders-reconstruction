package cn.zpl.util;

import cn.zpl.common.BaiduAIProperties;
import cn.zpl.common.bean.PictureAnalyze;
import cn.zpl.config.SpringContext;
import com.baidu.aip.contentcensor.AipContentCensor;
import com.baidu.aip.contentcensor.EImgType;
import com.baidu.aip.imageclassify.AipImageClassify;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.ObjectOutputStream;
import java.util.HashMap;

@Component
@Slf4j
@EnableConfigurationProperties(BaiduAIProperties.class)
public class BaiduAITool {

    @Resource
    BaiduAIProperties properties;
    //设置APPID/AK/SK
    @SneakyThrows
    public void doBusiness(File file){
        {
            CrudTools tools = SpringContext.getBeanWithGenerics(CrudTools.class);
            // 初始化一个AipImageClassify
            AipImageClassify client = new AipImageClassify(properties.getApp_id(), properties.getApi_key(), properties.getSecret_key());

            // 可选：设置网络连接参数
            client.setConnectionTimeoutInMillis(2000);
            client.setSocketTimeoutInMillis(60000);

            // 可选：设置代理服务器地址, http和socket二选一，或者均不设置
//        client.setHttpProxy("proxy_host", proxy_port);  // 设置http代理
//        client.setSocketProxy("proxy_host", proxy_port);  // 设置socket代理

            // 调用接口
//            String path = "C:\\图片\\006XFi4cly1ge783r9qxtj30xb1jwe46.jpg";
            JSONObject res = client.advancedGeneral(file.getPath(), new HashMap<>());
            System.out.println(res.toString(2));
            PictureAnalyze pictureAnalyze = new PictureAnalyze();
            pictureAnalyze.setPath(file.getPath());
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(res.toString());
            pictureAnalyze.setBaiduResult(byteArrayOutputStream.toByteArray());
            pictureAnalyze.setBaiduJsonResult(res.toString());
            tools.commonApiSave(pictureAnalyze);
        }
    }

    public String ApiCensor(String file) {
        AipContentCensor client = new AipContentCensor(properties.getApp_id(), properties.getApi_key(), properties.getSecret_key());
        // 可选：设置网络连接参数
        client.setConnectionTimeoutInMillis(2000);
        client.setSocketTimeoutInMillis(60000);

        // 可选：设置代理服务器地址, http和socket二选一，或者均不设置
//        client.setHttpProxy("proxy_host", proxy_port);  // 设置http代理
//        client.setSocketProxy("proxy_host", proxy_port);  // 设置socket代理

        // 可选：设置log4j日志输出格式，若不设置，则使用默认配置
        // 也可以直接通过jvm启动参数设置此环境变量
//        System.setProperty("aip.log4j.conf", "path/to/your/log4j.properties");

        // 调用接口
        JSONObject res = client.imageCensorUserDefined(file, EImgType.FILE, null);
        log.debug("分析结果：{}", res);
        return res.toString();
//        System.out.println(res.toString(2));
    }
}
