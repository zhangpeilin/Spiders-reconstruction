package cn.zpl.tencent;

import cn.zpl.tencent.common.TencentParams;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.ims.v20201229.ImsClient;
import com.tencentcloudapi.ims.v20201229.models.*;

public class ImageModeration
{
    public static void main(String [] args) {
        try{
            // 实例化一个认证对象，入参需要传入腾讯云账户secretId，secretKey,此处还需注意密钥对的保密
            // 密钥可前往https://console.cloud.tencent.com/cam/capi网站进行获取
            Credential cred = new Credential(TencentParams.SecretId, TencentParams.SecretKey);
            // 实例化一个http选项，可选的，没有特殊需求可以跳过
            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setEndpoint("ims.tencentcloudapi.com");
            // 实例化一个client选项，可选的，没有特殊需求可以跳过
            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(httpProfile);
            // 实例化要请求产品的client对象,clientProfile是可选的
            ImsClient client = new ImsClient(cred, "ap-beijing", clientProfile);
            // 实例化一个请求对象,每个接口都会对应一个request对象
            ImageModerationRequest req = new ImageModerationRequest();
            req.setFileContent(TencentAITool.getImgBase64Str("C:\\图片\\006l0mbogy1fidzlj2u6bj30mb0w4x4v.jpg"));
            // 返回的resp是一个ImageModerationResponse的实例，与请求对象对应
            ImageModerationResponse resp = client.ImageModeration(req);
            // 输出json格式的字符串回包
            System.out.println(ImageModerationResponse.toJsonString(resp));
        } catch (TencentCloudSDKException e) {
            System.out.println(e.toString());
        }
    }
}