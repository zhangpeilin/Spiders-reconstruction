package cn.zpl.tencent.bs;

import cn.zpl.pojo.Data;
import cn.zpl.tencent.common.TencentParams;
import cn.zpl.util.CommonIOUtils;
import cn.zpl.util.URLConnectionTool;
import cn.zpl.util.UrlContainer;
import com.google.gson.JsonElement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class TencentBusiness {
    @Resource
    TencentParams tencentParams;
    @Async
    public void saveShareFile(String share_key) {
        String getTokenUrl = "https://share.weiyun.com/" + share_key;
        HttpURLConnection httpsURLConnection = URLConnectionTool.getHttpsURLConnection(new UrlContainer(getTokenUrl));

        try {
            httpsURLConnection.connect();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String token = "";
        List<String> cookies = httpsURLConnection.getHeaderFields().get("Set-Cookie");
        if (!cookies.isEmpty()) {
            String tokenCookie = cookies.get(0);
            Matcher matcher = Pattern.compile("(wyctoken=)(\\S+);").matcher(tokenCookie);
            if (matcher.find()) {
                System.out.println(matcher.group(2));
                token = matcher.group(2);
            }
        }
        String getFileInfo = "{\"req_header\":\"{\\\"seq\\\":16649871109161720,\\\"type\\\":1,\\\"cmd\\\":12002,\\\"appid\\\":30113,\\\"version\\\":3,\\\"major_version\\\":3,\\\"minor_version\\\":3,\\\"fix_version\\\":3,\\\"wx_openid\\\":\\\"\\\",\\\"user_flag\\\":0}\",\"req_body\":\"{\\\"ReqMsg_body\\\":{\\\"ext_req_head\\\":{\\\"token_info\\\":{\\\"token_type\\\":0,\\\"login_key_type\\\":1,\\\"login_key_value\\\":\\\"@7uVk3GA7G\\\"},\\\"language_info\\\":{\\\"language_type\\\":2052}},\\\".weiyun.WeiyunShareViewMsgReq_body\\\":{\\\"share_pwd\\\":null,\\\"share_key\\\":\\\"%1$s\\\"}}}\"}";

        String getFileJson = String.format(getFileInfo, share_key);
        Data data = new Data();
        data.setHeader(String.format(tencentParams.weiyunCookie, String.format("wyctoken=%1$s\n", token)));
        data.setUrl(String.format("https://share.weiyun.com/webapp/json/weiyunShare/WeiyunShareView?refer=chrome_windows&g_tk=%1$s&r=0.6800144973027133", token));
        data.setParams(getFileJson);
        String fileInfo = CommonIOUtils.postUrl(data);
        JsonElement file_list = CommonIOUtils.getFromJson2(fileInfo, "data-rsp_body-RspMsg_body-file_list");
        if (file_list.isJsonArray()) {
            for (JsonElement jsonElement : file_list.getAsJsonArray()) {
                String file_id = CommonIOUtils.getFromJson2Str(jsonElement, "file_id");
                String file_name = CommonIOUtils.getFromJson2Str(jsonElement, "file_name");
                String file_size = CommonIOUtils.getFromJson2Str(jsonElement, "file_size");
                String pdir_key = CommonIOUtils.getFromJson2Str(jsonElement, "pdir_key");

                String json = "{\"req_header\":\"{\\\"seq\\\":16649868086504332,\\\"type\\\":1,\\\"cmd\\\":12025,\\\"appid\\\":30113,\\\"version\\\":3,\\\"major_version\\\":3,\\\"minor_version\\\":3,\\\"fix_version\\\":3,\\\"wx_openid\\\":\\\"\\\",\\\"user_flag\\\":0,\\\"device_info\\\":\\\"{\\\\\\\"browser\\\\\\\":\\\\\\\"chrome\\\\\\\"}\\\"}\",\"req_body\":\"{\\\"ReqMsg_body\\\":{\\\"ext_req_head\\\":{\\\"token_info\\\":{\\\"token_type\\\":0,\\\"login_key_type\\\":1,\\\"login_key_value\\\":\\\"@7uVk3GA7G\\\"},\\\"language_info\\\":{\\\"language_type\\\":2052}},\\\".weiyun.WeiyunSharePartSaveDataMsgReq_body\\\":{\\\"os_info\\\":\\\"windows\\\",\\\"browser\\\":\\\"chrome\\\",\\\"share_key\\\":\\\"%1$s\\\",\\\"pwd\\\":\\\"\\\",\\\"dst_ppdir_key\\\":\\\"%6$s\\\",\\\"dst_pdir_key\\\":\\\"%7$s\\\",\\\"src_pdir_key\\\":\\\"\\\",\\\"dir_list\\\":[],\\\"note_list\\\":[],\\\"file_list\\\":[{\\\"pdir_key\\\":\\\"%2$s\\\",\\\"file_id\\\":\\\"%3$s\\\",\\\"filename\\\":\\\"%4$s\\\",\\\"file_size\\\":%5$s}],\\\"is_save_all\\\":true}}}\"}";
                String saveJson = String.format(json, share_key, pdir_key, file_id, file_name, file_size, tencentParams.getDst_ppdir_key(), tencentParams.getDst_pdir_key());
                data.setUrl(String.format("https://share.weiyun.com/webapp/json/weiyunShare/WeiyunSharePartSaveData?refer=chrome_windows&g_tk=%1$s&r=0.06866114011787916", token));
                data.setParams(saveJson);
                String saveResult = CommonIOUtils.postUrl(data);
                System.out.println(saveResult);
            }
        }
    }
}
