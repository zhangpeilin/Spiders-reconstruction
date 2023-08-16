package cn.zpl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class PublicIpExample {
    public static void main(String[] args) {
        try {
            // 发起网络请求获取包含公网 IP 的响应
            String ipResponse = getPublicIp();

            // 解析响应获取公网 IP
            String publicIp = extractPublicIp(ipResponse);

            System.out.println("Public IP: " + publicIp);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getPublicIp() throws IOException {
        // 发起 GET 请求获取包含公网 IP 的响应
        URL url = new URL("https://checkip.amazonaws.com");
        BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));

        String response = br.readLine();
        br.close();

        return response;
    }

    private static String extractPublicIp(String response) {
        // 去除响应中的空格和换行符
        return response.trim();
    }
}