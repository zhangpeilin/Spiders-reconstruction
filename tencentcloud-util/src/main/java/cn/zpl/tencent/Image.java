package cn.zpl.tencent;

import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.tiia.v20190529.models.DetectLabelItem;
import com.tencentcloudapi.tiia.v20190529.models.DetectLabelRequest;
import com.tencentcloudapi.tiia.v20190529.models.DetectLabelResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

@Slf4j
public class Image {

    @SneakyThrows
    public static void main(String[] args) {
        Credential credential = new Credential("AKIDgCerpX1FExShhRwBBvQ0RcYFgsoVx96n", "UihS0JNBk37vUnrfDWZNvEI6e191dz7z");
        TiiaClient tiiaClient = new TiiaClient(credential, "ap-beijing");
        DetectLabelRequest detectLabelRequest = new DetectLabelRequest();
        detectLabelRequest.setImageBase64(getImgBase64Str("C:\\big\\101.jpg"));
        DetectLabelResponse detectLabelResponse = tiiaClient.DetectLabel(detectLabelRequest);
        for (DetectLabelItem label : detectLabelResponse.getLabels()) {
            log.info("可信度：{}", label.getConfidence());
            log.info("物体名称：{}", label.getName());
            log.info("标签一级分类：{}", label.getFirstCategory());
            log.info("标签二级分类：{}", label.getSecondCategory());
        }
    }

    //参数imgFile：图片完整路径
    public static String getImgBase64Str(String imgFile) {
        // 将图片文件转化为字节数组字符串，并对其进行Base64编码处理
        InputStream in = null;
        byte[] data = null;
        // 读取图片字节数组
        try {
            in = Files.newInputStream(Paths.get(imgFile));
            data = new byte[in.available()];
            in.read(data);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Base64.getEncoder().encodeToString(data);
    }
}
