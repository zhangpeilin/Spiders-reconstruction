package cn.zpl.util;

import cn.zpl.config.CommonParams;
import cn.zpl.connection.TrustAnyHostnameVerifier;
import cn.zpl.connection.TrustAnyTrustManager;
import cn.zpl.pojo.DownloadDTO;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.openjsse.net.ssl.OpenJSSE;
import org.springframework.util.ObjectUtils;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
public class URLConnectionTool {


    private static InetSocketAddress addr = new InetSocketAddress(CommonParams.hostName, CommonParams.proxyPort);
    private static Proxy proxy = new Proxy(Proxy.Type.HTTP, addr);


    public static HttpsURLConnection getHttpsURLConnection(String url, String headers) {
        UrlContainer container = new UrlContainer(url, headers);
        return getHttpsURLConnection(container);
    }
    public static HttpsURLConnection getHttpsURLConnection(@NotNull UrlContainer container) {
        HttpsURLConnection urlcon = null;
        SSLContext sc;
        try {
            Security.addProvider(new OpenJSSE());
            sc = SSLContext.getInstance("TLSv1.2");
            sc.init(null, new TrustManager[]{new TrustAnyTrustManager()}, new java.security.SecureRandom());
            URL cover_url = new URL(container.getUrl());
            urlcon = container.isProxy() ? (HttpsURLConnection) cover_url.openConnection(proxy) :
                    (HttpsURLConnection) cover_url.openConnection();
            urlcon.setConnectTimeout(20000);
            urlcon.setSSLSocketFactory(sc.getSocketFactory());
            urlcon.setHostnameVerifier(new TrustAnyHostnameVerifier());
            urlcon.setDoOutput(true);
            urlcon.setReadTimeout(20000);
            urlcon.setRequestMethod("GET");
            urlcon.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/76.0.3809.132 Safari/537.36");

            if (container.getHeaders() != null) {
                String[] headers = container.getHeaders().trim().split("\n");
                for (String pair :
                        headers) {
                    urlcon.setRequestProperty(pair.split(":")[0].trim(), pair.split(":")[1].trim());
                }
            }
            if (!ObjectUtils.isEmpty(container.getCookies())) {
                urlcon.setRequestProperty("cookie", container.getCookies());
            }
            if (container.getReferer() != null) {
                urlcon.setRequestProperty("Referer", container.getReferer());
            }
            return urlcon;
        } catch (NoSuchAlgorithmException | KeyManagementException | IOException e) {
            e.printStackTrace();
        }
        return urlcon;
    }


    public static HttpURLConnection getHttpURLConnection(boolean isProxy, String url) {
        HttpURLConnection urlcon = null;
        try {
            URL cover_url = new URL(url);
            urlcon = isProxy ? (HttpURLConnection) cover_url.openConnection(proxy) : (HttpURLConnection) cover_url.openConnection();
            urlcon.setConnectTimeout(20000);
            urlcon.setDoOutput(true);
            urlcon.setReadTimeout(20000);
            urlcon.setRequestMethod("GET");
            urlcon.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36");
            return urlcon;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return urlcon;
    }

    public static HttpURLConnection getHttpURLConnection(@NotNull UrlContainer container) {
        HttpURLConnection urlcon = null;
        try {
            URL cover_url = new URL(container.getUrl());
            urlcon = container.isProxy() ? (HttpURLConnection) cover_url.openConnection(proxy) :
                    (HttpURLConnection) cover_url.openConnection();
            urlcon.setConnectTimeout(20000);
            urlcon.setDoOutput(true);
            urlcon.setReadTimeout(20000);
            urlcon.setRequestMethod("GET");
            urlcon.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/76.0.3809.132 Safari/537.36");
            return urlcon;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return urlcon;
    }


    public void sendPost(String url, String params, Map<String, String> property) {
        PrintWriter out = null;
        UrlContainer container = new UrlContainer(url);
        try {
            HttpsURLConnection urlconn = URLConnectionTool.getHttpsURLConnection(container);
            for (Map.Entry<String, String> entry : property.entrySet()) {
                urlconn.setRequestProperty(entry.getKey(), entry.getValue());
            }
//			urlconn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
//			urlconn.setRequestProperty("connection", "Keep-Alive");
            urlconn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.87 Safari/537.36");
            urlconn.setRequestMethod("POST");
            urlconn.setDoOutput(true);
            urlconn.setDoInput(true);

            out = new PrintWriter(urlconn.getOutputStream());
            out.print(params);
            out.flush();
            log.debug(toString(urlconn.getInputStream()));
        } catch (Exception e) {
            // TODO: handle exception
            System.out.println("发送请求异常");
            e.printStackTrace();
        } finally {
            if (out != null)
                out.close();
        }
    }

    @SneakyThrows
    public static Long getDataLength(@NotNull DownloadDTO data) {
//        if (!data.getUrl().toLowerCase().startsWith("https")) {
//            data.setUrl("https" + data.getUrl().substring(4));
//        }
        UrlContainer container = new UrlContainer(data.getUrl());
        container.setProxy(true);
        if (data.getWebSite().equals("bilibili")) {
            container.setProxy(false);
//            container.setWebSite(data.getWebSite());
        }
        HttpURLConnection httpconn = container.isHttps() ?
                URLConnectionTool.getHttpsURLConnection(container) : URLConnectionTool.getHttpURLConnection(container);
        long length;
        try {
            if (data.getReferer() != null && !"".equals(data.getReferer())) {
                httpconn.setRequestProperty("Referer", data.getReferer());
            }
            if (data.getHeader() != null && !"".equals(data.getHeader())) {
                String[] cookieArray = data.getHeader().trim().split("\n");
                for (String pair : cookieArray) {
                    httpconn.setRequestProperty(pair.split(":")[0].trim(), pair.split(":")[1].trim());
                }
            }
            httpconn.connect();
            if (httpconn.getResponseCode() != 200) {
                throw new RuntimeException("访问失败：" + httpconn.getResponseCode());
            }
            String length_ = httpconn.getHeaderField("content-length");
            length = Long.parseLong(length_ == null ? "0" : length_);
            if (length == -1 || length == 0) {
                for (int i = 0; ; i++) {
                    String mine = httpconn.getHeaderFieldKey(i);
                    if (mine == null) {
                        break;
                    }
                    if (mine.equals("Content-Length")) {
                        length = Long.parseLong(httpconn.getHeaderField(i));
                        break;
                    }
                }
            }
        } catch (IOException e) {
            log.error("获取文件大小异常：", e);
            TimeUnit.SECONDS.sleep(5);
            return getDataLength(data);
        }
        httpconn.disconnect();
        System.out.println("文件大小：" + length);
        data.setFileLength(length);
        return length;
    }

    public static String getRedirectLocation(@NotNull DownloadDTO data) {
        HttpURLConnection conn = URLConnectionTool.getHttpURLConnection(data.isProxy(), data.getUrl());
        conn.setInstanceFollowRedirects(false);
        try {
            if (data.getReferer() != null && !"".equals(data.getReferer())) {
                conn.setRequestProperty("Referer", data.getReferer());
            }
            if (conn.getResponseCode() == 301 || conn.getResponseCode() == 302) {
                return conn.getHeaderField("Location");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String postUrl(String path, String params, String headers) {
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(10000)
                    .setConnectTimeout(10000).setConnectionRequestTimeout(3000).build();
            HttpPost post = new HttpPost(path);
            addHeader(post, headers);
            post.setConfig(requestConfig);
            StringEntity postingString = new StringEntity(params, "utf-8");
            post.setEntity(postingString);
            HttpResponse response = httpClient.execute(post);
            return EntityUtils.toString(response.getEntity());
        } catch (IOException e) {
            log.error("POST请求出错：\n", e);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            return postUrl(path, params, headers);
        }
    }

    public static HttpResponse postUrlOnlyResponse(String path, String params, String headers) {
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(10000)
                    .setConnectTimeout(10000).setConnectionRequestTimeout(3000).build();
            HttpPost post = new HttpPost(path);
            addHeader(post, headers);
            post.setConfig(requestConfig);
            StringEntity postingString = new StringEntity(params, "utf-8");
            post.setEntity(postingString);
            return httpClient.execute(post);
        } catch (IOException e) {
            log.error("POST请求出错：\n", e);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            return postUrlOnlyResponse(path, params, headers);
        }
    }

    private static void addHeader(HttpRequestBase http, String headers) {
        if (headers == null || "".equalsIgnoreCase(headers)) {
            return;
        }
        String[] cookieArray = headers.trim().split("\n");
        for (String pair :
                cookieArray) {
            http.addHeader(pair.split(":")[0].trim(), pair.split(":")[1].trim());
        }
    }

    @Nullable
    public static byte[] getMethod(@NotNull String url, String headers) {
        if ("".equals(url)) {
            throw new RuntimeException("地址为空，请检查\n");
        }
        log.debug("请求地址是：\n" + url);
        log.debug("请求头header：\n" + headers);
        CloseableHttpClient httpClient = HttpClients.createDefault();
        RequestConfig requestConfig =
                RequestConfig.custom().setSocketTimeout(10000).setConnectTimeout(10000).setConnectionRequestTimeout(3000).build();
        HttpGet get = new HttpGet(url);
        addHeader(get, headers);
        get.setConfig(requestConfig);
        try {
            HttpResponse response = httpClient.execute(get);
            return EntityUtils.toByteArray(response.getEntity());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Nullable
    public static String getMethod2Str(@NotNull String url, @NotNull String headers) {
        if ("".equals(url)) {
            throw new RuntimeException("地址为空，请检查\n");
        }
        CloseableHttpClient httpClient = HttpClients.createDefault();
        RequestConfig requestConfig =
                RequestConfig.custom().setSocketTimeout(10000).setConnectTimeout(10000).setConnectionRequestTimeout(3000).build();
        HttpGet get = new HttpGet(url);
        addHeader(get, headers);
        get.setConfig(requestConfig);
        try {
            HttpResponse response = httpClient.execute(get);
            return EntityUtils.toString(response.getEntity());
        } catch (IOException e) {
            log.error("请求出错：\n", e);
        }
        return null;
    }

    @NotNull
    public static String toString(InputStream in) throws IOException {
        if (in == null)
            return "";
        BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        String tmp;
        StringBuilder str = new StringBuilder();
        while ((tmp = br.readLine()) != null) {
            str.append(tmp);
        }
        in.close();
        return str.toString();
    }

    public static String getRedirect(HttpURLConnection connection) {
        return connection.getHeaderField("Location");
    }
}
