package cn.zpl.tencent;

import cn.zpl.common.bean.PictureAnalyze;
import cn.zpl.tencent.common.TencentParams;
import cn.zpl.util.CrudTools;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.tiia.v20190529.models.AssessQualityRequest;
import com.tencentcloudapi.tiia.v20190529.models.AssessQualityResponse;
import com.tencentcloudapi.tiia.v20190529.models.DetectLabelRequest;
import com.tencentcloudapi.tiia.v20190529.models.DetectLabelResponse;
import com.tencentcloudapi.tiia.v20190529.models.DetectMisbehaviorRequest;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

@Slf4j
public class TencentAITool {

    static final Credential credential = new Credential(TencentParams.SecretId, TencentParams.SecretKey);
    @SneakyThrows
    public static void dobusiness(File file) {
        TiiaClient tiiaClient = new TiiaClient(credential, TencentParams.region);
        DetectLabelRequest detectLabelRequest = new DetectLabelRequest();
        detectLabelRequest.setImageBase64(getImgBase64Str(file.getPath()));
        DetectMisbehaviorRequest detectMisbehaviorRequest = new DetectMisbehaviorRequest();
        detectMisbehaviorRequest.setImageBase64(getImgBase64Str(file.getPath()));

        DetectLabelResponse detectLabelResponse = tiiaClient.DetectLabel(detectLabelRequest);
//        DetectMisbehaviorResponse detectMisbehaviorResponse = tiiaClient.DetectMisbehavior(detectMisbehaviorRequest);
//        for (DetectLabelItem label : detectLabelResponse.getLabels()) {
//            log.info("可信度：{}", label.getConfidence());
//            log.info("物体名称：{}", label.getName());
//            log.info("标签一级分类：{}", label.getFirstCategory());
//            log.info("标签二级分类：{}", label.getSecondCategory());
//        }
        String labelResult = JSON.toJSONString(detectLabelResponse);
//        String behaviorResult = JSON.toJSONString(detectMisbehaviorResponse);
//        log.info("可信度：{}", detectMisbehaviorResponse.getConfidence());
//        log.info("不良行为：{}", detectMisbehaviorResponse.getType());
        PictureAnalyze pictureAnalyze = new PictureAnalyze();
        pictureAnalyze.setPath(file.getPath());
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("labelResult", detectLabelResponse);
//        jsonObject.put("behaviorResult", detectMisbehaviorResponse);
        pictureAnalyze.setTencentJsonResult(jsonObject.toJSONString());
        CrudTools.savePA(pictureAnalyze);
    }

    @SneakyThrows
    public static void pictureQuality(PictureAnalyze pictureAnalyze) {
        TiiaClient tiiaClient = new TiiaClient(credential, TencentParams.region);
        AssessQualityRequest assessQualityRequest = new AssessQualityRequest();
        assessQualityRequest.setImageBase64(getImgBase64Str(pictureAnalyze.getPath()));
        AssessQualityResponse assessQualityResponse = tiiaClient.AssessQuality(assessQualityRequest);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("AssessQualityResult", assessQualityResponse);
        pictureAnalyze.setQualityResult(jsonObject.toJSONString());
        CrudTools.savePA(pictureAnalyze);

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

    public static void main(String[] args) {

        pictureQuality(CrudTools.getPAById("1527733848444903425").getObject(PictureAnalyze.class));

    }
}
