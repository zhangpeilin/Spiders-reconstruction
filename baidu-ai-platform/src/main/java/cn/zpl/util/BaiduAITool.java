package cn.zpl.util;

import cn.zpl.common.BaiduAIParams;
import cn.zpl.common.bean.PictureAnalyze;
import com.baidu.aip.imageclassify.AipImageClassify;
import lombok.SneakyThrows;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.ObjectOutputStream;
import java.util.HashMap;

public class BaiduAITool {
    //设置APPID/AK/SK
    @SneakyThrows
    public static void dobusiness(File file){
        {
            CrudTools<PictureAnalyze> tools = new CrudTools<>();
            // 初始化一个AipImageClassify
            AipImageClassify client = new AipImageClassify(BaiduAIParams.APP_ID, BaiduAIParams.API_KEY, BaiduAIParams.SECRET_KEY);

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
            tools.commonSave(pictureAnalyze);
        }
    }
}
