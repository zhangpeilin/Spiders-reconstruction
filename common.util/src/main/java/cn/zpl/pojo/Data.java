package cn.zpl.pojo;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class Data {
    private CloseableHttpClient client;
    private HttpGet get;
    private HttpPost post;
    private CloseableHttpResponse response;
    private AtomicBoolean stoped;
    private String url;
    private DoRetry doRetry;
    private Object result;
    private boolean proxy;
    private int statusCode;
    private int waitSeconds;
    private String fileName;
    private int proxyPort;
    private String proxyIP;
    private boolean specialProxyConfig;
    private String referer;
    private String type;
    private String params;
    private Map<String,String> valuePairs;
    private String baseUrl;
    private String cookie;

    public void setAlwaysRetry(){
        this.doRetry.setAlwaysRetry(true);
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    private String header;

    public boolean isProxy() {
        return proxy;
    }

    public void setProxy(boolean proxy) {
        this.proxy = proxy;
    }

    public String getResult() {
        return result== null ? "" : result.toString();
    }

    public Object getResObject() {
        return result;
    }

    @Nullable
    public String getString(){
        return result.toString();
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public Data() {
        this.doRetry = new DoRetry();
        this.proxy = false;
        this.specialProxyConfig = false;
        this.waitSeconds = 6;
        this.type = "json";
        this.header = "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/76.0.3809.132 Safari/537.36\n" +
                "Accept-Language: zh-CN,zh;q=0.9,en;q=0.8,ja;q=0.7,zh-TW;q=0.6\n";
    }

    public static Data getInstance(String url) {
        Data data = new Data();
        data.setUrl(url);
        return data;
    }

    public Data(int retryMaxCount) {
        this.doRetry = new DoRetry();
        this.proxy = false;
        this.doRetry.setRetryMaxCount(retryMaxCount);
        this.specialProxyConfig = false;
        this.waitSeconds = 6;
        this.type = "json";
        this.header = "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/76.0.3809.132 Safari/537.36\n" +
                "Accept-Language: zh-CN,zh;q=0.9,en;q=0.8,ja;q=0.7,zh-TW;q=0.6\n";
    }

    public DoRetry getDoRetry() {
        return doRetry;
    }

    public void setDoRetry(DoRetry doRetry) {
        this.doRetry = doRetry;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(@NotNull String url) {
        this.url = url;
        if (url.startsWith("http"))
        this.baseUrl = url.substring(0, url.indexOf("/", 8));
    }

    public void clear() {
        try {
            if (response != null) {
                response.close();
            }
//            if (client != null) {
//                client.close();
//            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public AtomicBoolean getStoped() {
        return stoped;
    }

    public void setStoped(AtomicBoolean stoped) {
        this.stoped = stoped;
    }

    public CloseableHttpResponse getResponse() {
        return response;
    }

    public void setResponse(CloseableHttpResponse response) {
        this.response = response;
    }

    public CloseableHttpClient getClient() {
        return client;
    }

    public void setClient(CloseableHttpClient client) {
        this.client = client;
    }

    public HttpGet getGet() {
        return get;
    }

    public void setGet(HttpGet get) {
        this.get = get;
    }

    public HttpPost getPost() {
        return post;
    }

    public void setPost(HttpPost post) {
        this.post = post;
    }

    public void stop() {
        this.stoped = new AtomicBoolean(true);
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public int getWaitSeconds() {
        return waitSeconds;
    }

    public void setWaitSeconds(int waitSeconds) {
        this.waitSeconds = waitSeconds;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void sleep(){
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public String getProxyIP() {
        return proxyIP;
    }

    public void setProxyIP(String proxyIP) {
        this.proxyIP = proxyIP;
    }

    public int getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
    }

    public boolean isSpecialProxyConfig() {
        return specialProxyConfig;
    }

    public void setSpecialProxyConfig(boolean specialProxyConfig) {
        this.specialProxyConfig = specialProxyConfig;
    }

    public String getReferer() {
        return referer;
    }

    public void setReferer(String referer) {
        this.referer = referer;
    }

    public String getType() {
        if (type == null) {
            throw new RuntimeException("请求类型不能为空，请检查！");
        }
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    @Override
    public String toString() {
        return "Data{" +
                "client=" + client +
                ", get=" + get +
                ", post=" + post +
                ", response=" + response +
                ", stoped=" + stoped +
                ", url='" + url + '\'' +
                ", doRetry=" + doRetry +
                ", result=" + result +
                ", proxy=" + proxy +
                ", statusCode=" + statusCode +
                ", waitSeconds=" + waitSeconds +
                ", fileName='" + fileName + '\'' +
                ", proxyPort=" + proxyPort +
                ", proxyIP='" + proxyIP + '\'' +
                ", specialProxyConfig=" + specialProxyConfig +
                ", referer='" + referer + '\'' +
                ", type='" + type + '\'' +
                ", params='" + params + '\'' +
                ", header='" + header + '\'' +
                '}';
    }

    public Map<String, String> getValuePairs() {
        return valuePairs;
    }

    public void setValuePairs(Map<String, String> valuePairs) {
        this.valuePairs = valuePairs;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getCookie() {
        return cookie;
    }

    public void setCookie(String cookie) {
        this.cookie = cookie;
    }
}
