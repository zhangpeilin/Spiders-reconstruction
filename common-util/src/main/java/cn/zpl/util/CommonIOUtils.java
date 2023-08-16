package cn.zpl.util;

import cn.zpl.config.CommonParams;
import cn.zpl.pojo.Data;
import cn.zpl.pojo.DoRetry;
import cn.zpl.pojo.DownloadDTO;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonNull;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.AbstractResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.reflections.Reflections;
import org.springframework.cglib.beans.BeanMap;
import org.springframework.util.Assert;
import org.springframework.util.DigestUtils;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.Vector;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public class CommonIOUtils {


    private static final ThreadLocal<DoRetry> retry = new ThreadLocal<>();
    private static final ThreadLocal<Integer> count = new ThreadLocal<>();
    public static Set<Class<? extends Serializable>> entityList = new HashSet<>();
    public static CloseableHttpClient closeableHttpClient;
    public static ResponseHandler<byte[]> byteHandler;
    public static ResponseHandler<String> strHandler;
    public static PoolingHttpClientConnectionManager clientConnectionManager;
    static int SOCKET_TIMEOUT = 60000;
    public static String splitMarkExp1 = "\\{[^{}]+\\}";
    public static String removeStr1 = "[\\{\\}]";
    public static String removeStr2 = "[\\[\\]]";
    public static String removeStr3 = "[']";

    public static void preBusiness() {
        byteHandler = new AbstractResponseHandler<byte[]>() {
            @Override
            public byte[] handleEntity(HttpEntity entity) throws IOException {
                return EntityUtils.toByteArray(entity);
            }
        };
        strHandler = new AbstractResponseHandler<String>() {
            @Override
            public String handleEntity(HttpEntity entity) throws IOException {
                return EntityUtils.toString(entity, "utf-8");
            }
        };
        ConnectionKeepAliveStrategy myStrategy = (response, context) -> {
            HeaderElementIterator iterator = new BasicHeaderElementIterator(response.headerIterator(HTTP.CONN_KEEP_ALIVE));
            while (iterator.hasNext()) {
                HeaderElement headerElement = iterator.nextElement();
                String param = headerElement.getName();
                String value = headerElement.getValue();
                if (value != null && param.equalsIgnoreCase("timeout")) {
                    return Long.parseLong(value) * 1000;
                }
            }
            return 60 * 1000;
        };
        clientConnectionManager = new PoolingHttpClientConnectionManager();
        clientConnectionManager.setMaxTotal(500);
        clientConnectionManager.setDefaultMaxPerRoute(10);
        closeableHttpClient = HttpClients.custom().setConnectionManager(clientConnectionManager).setKeepAliveStrategy(myStrategy).setRetryHandler((exception, executionCount, context) -> {
            if (executionCount > 5) {
                return false;
            }
            log.error("执行出错：\n", exception);
            log.error("重新请求，当前重试次数：{}", executionCount);
            if (exception != null) {
                return true;
            }
            HttpClientContext clientContext = HttpClientContext.adapt(context);
            HttpRequest request = clientContext.getRequest();
            // 如果请求是幂等的，就再次尝试
            return !(request instanceof HttpEntityEnclosingRequest);
        }).build();
    }

    public static void test1() {
        retry.set(new DoRetry());
        System.out.println(retry.get());
    }

    public static void test2() {
        System.out.println(retry.get());
    }

    @NotNull
    public static String makeFilePath(@NotNull List<String> path, String fileName) {
        StringBuffer res = new StringBuffer();
        path.forEach(name -> res.append(name.trim()).append(File.separator));
        return fileName != null && !"".equals(fileName) ? res.append(fileName).toString() : res.toString();
    }

    @Deprecated
    public static List<String> formatM3U8(String txt) {
        List<String> mediaAddr = new ArrayList<>();
        txt = txt.substring(txt.indexOf("#EXTINF"), txt.indexOf("#EXT-X-ENDLIST"));
        Pattern pattern = Pattern.compile("");
        Matcher matcher = pattern.matcher(txt);
        if (matcher.find()) {
            int count = matcher.groupCount();
            while (count > 0) {
                if (!matcher.group(count).contains("#EXTINF")) {
                    mediaAddr.add(matcher.group(count));
                }
                count--;
            }
        }
        return mediaAddr;
    }

    public static BigDecimal getTimeFromM3U8(String txt) {
        txt = txt.substring(txt.indexOf("#EXTINF"), txt.indexOf("#EXT-X-ENDLIST"));
        BigDecimal timeLength = new BigDecimal(0);
        String[] lines = txt.split("(#EXTINF:)+|(,)+");
        for (String line :
                lines) {
            if (line.matches("\\d+(\\.*\\d+)?")) {
                timeLength = timeLength.add(new BigDecimal(line));
            }
        }
        return timeLength;
    }

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

    public static String getPageStr(String url, String headers) {
        UrlContainer container = new UrlContainer(url, headers);
        return getPageStr(container);
    }

    public static String getPageStr(UrlContainer container) {

        URLConnection conn = container.isHttps() ? URLConnectionTool.getHttpsURLConnection(container) :
                URLConnectionTool.getHttpURLConnection(container);
        InputStream is = null;
        String str;
        try {
            conn.connect();
            is = conn.getInputStream();
            str = CommonIOUtils.toString(is);
        } catch (IOException e) {
            log.error("准备重试\n", e);
            ((HttpURLConnection) conn).disconnect();
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            return getPageStr(container);
        }
        return str;
    }

    public static StringBuffer readTxt(String path, String code) throws IOException {
        BufferedReader br = null;
        StringBuffer str = new StringBuffer();
        try {
            if (code == null || "".equals(code)) {
                code = "gbk";
            }
            br = new BufferedReader(new InputStreamReader(Files.newInputStream(new File(path).toPath()), code));
            String tmp;
            while ((tmp = br.readLine()) != null) {
                str.append(tmp).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
            try {
                assert br != null;
                br.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            return str;
        }
        br.close();
        return str;
    }

    public static boolean readTxtAndFilter(String path, String key, String code) {
        BufferedReader br = null;
        try {
            if (code == null || "".equals(code)) {
                code = "gbk";
            }
            br = new BufferedReader(new InputStreamReader(Files.newInputStream(new File(path).toPath()), code));
            String tmp;
            while ((tmp = br.readLine()) != null) {
                if (tmp.contains(key)) {
                    br.close();
                    return true;
                }
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
            try {
                Objects.requireNonNull(br).close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            return false;
        }
        return false;
    }

    public static JsonElement paraseJsonFromURL(String url, String headers) {
        UrlContainer container = new UrlContainer(url, headers);
        return paraseJsonFromURL(container);
    }

    public static JsonElement paraseJsonFromURL(@NotNull UrlContainer container) {
        URLConnection urlcon = container.isHttps() ? URLConnectionTool.getHttpsURLConnection(container) :
                URLConnectionTool.getHttpURLConnection(container);
        try {
            urlcon.connect();
            return JsonParser.parseReader(new InputStreamReader(((HttpURLConnection) urlcon).getResponseCode() != 404 ?
                    urlcon.getInputStream() : ((HttpURLConnection) urlcon).getErrorStream(), StandardCharsets.UTF_8));
        } catch (Exception e) {
            ((HttpURLConnection) urlcon).disconnect();
            log.error("根据url地址读取json失败，url地址为：" + container.getUrl() + "\n异常信息如下：\n", e);
            log.error("当前重试次数为：" + container.getRetryCount());
            if (container.canDoRetry()) {
                container.doRetry();
                log.error("5秒后重试读取该url" + container.getUrl());
                try {
                    Thread.sleep(container.getSleepMills() != 0 ? container.getSleepMills() : 5000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                return paraseJsonFromURL(container);
            } else {
                return JsonNull.INSTANCE;
            }
        } finally {
            ((HttpURLConnection) urlcon).disconnect();
        }
    }

    public static JsonElement paraseJsonElementFromURL(URLConnection urlcon) throws JsonIOException,
            JsonSyntaxException,
            IOException {
        urlcon.connect();
        return JsonParser.parseReader(new InputStreamReader(urlcon.getInputStream()));
    }

    public static String filterFileName(String title) {
        Pattern pattern = Pattern.compile("[\\\\/:*?<>\"|❤.\n\t]");
        Matcher matcher = pattern.matcher(title);
        return removeDotEnd(matcher.replaceAll(" ").trim());
    }

    public static String filterFileName2(String title) {
        Pattern pattern = Pattern.compile("[\\\\/:*?<>\"|❤\n\t]");
        Matcher matcher = pattern.matcher(title);
        return removeDotEnd(matcher.replaceAll(" ").trim());
    }

    /**
     * 移除字符串后面的省略号，windows在创建文件夹的时候返回true但是去掉了省略号。类似【63.第63话白光之中...】
     *
     * @param str str
     * @return result
     */
    private static String removeDotEnd(String str) {
        Matcher matcher = Pattern.compile("\\.+$").matcher(str);
        return matcher.replaceAll("");
    }

    public static String paraseSystemTime10(String milliseconds) {
        SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd");
        return formater.format(new Date(Long.parseLong(milliseconds + "000")));
    }

    public static String paraseSystemTime13(String milliseconds, String pattern) {
        SimpleDateFormat formater = new SimpleDateFormat(pattern);
        return formater.format(new Date(Long.parseLong(milliseconds)));
    }
    public static String paraseSystemTime13(long milliseconds, String pattern) {
        SimpleDateFormat formater = new SimpleDateFormat(pattern);
        return formater.format(new Date(milliseconds));
    }

    public static void saveString2Local(String fileName, String str) {
        File file = new File("e:" + File.separator + fileName + "升级脚本.sql");
        try {
            if (file.exists()) {
                FileUtils.delete(file);
            }
            try (FileWriter fw = new FileWriter(file)) {
                fw.write(str);
                fw.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static JsonElement getFromJson2(JsonElement json, String path) {
        String[] paths = path.split("-");
        JsonElement current = json;
        for (String string : paths) {
            if (current == null) {
                return JsonNull.INSTANCE;
            }
            current = current.getAsJsonObject().get(string);
        }
        return current == null ? JsonNull.INSTANCE : current;
    }

    public static JsonElement[] getFromJson3(String str, @NotNull String path) {
        JsonElement json = JsonParser.parseString(str);
        return getFromJson3(json, path);
    }

    /**
     * 应对获取路径中包含Array的情况，如果有Array，路径格式为a-b[key]，返回结果是JsonElement数组
     *
     * @param json json
     * @param path path
     * @return return
     */
    public static JsonElement[] getFromJson3(JsonElement json, @NotNull String path) {
        String[] paths = path.split("-");
        JsonElement[] array = new JsonElement[0];
        JsonElement current = json;
        for (String string : paths) {
            if (current == null) {
                return array;
            }
            if (current.isJsonArray()) {
                if (current.getAsJsonArray().size() == 1) {
                    return new JsonElement[]{current.getAsJsonArray().get(0).getAsJsonObject().get(string)};
                }
                array = new JsonElement[current.getAsJsonArray().size()];
                for (String s : paths) {
                    if (!s.equals(string)) {
                        continue;
                    }
                    JsonArray newArray = new JsonArray();
                    current.getAsJsonArray().forEach(jsonElement -> newArray.add(jsonElement.getAsJsonObject().get(s)));
                    current = newArray;
                }
                AtomicInteger num = new AtomicInteger();
                JsonElement[] finalArray = array;
                current.getAsJsonArray().forEach(jsonElement -> {
                    finalArray[num.get()] = jsonElement;
                    num.getAndIncrement();
                });
                return finalArray;
            }
            current = current.getAsJsonObject().get(string);
        }
        return current == null ? new JsonElement[0] : array;
    }

    public static JsonElement getFromJson2(String str, @NotNull String path) {

        if (str == null || "".equalsIgnoreCase(str)) {
            return JsonNull.INSTANCE;
        }
        String[] paths = path.split("-");
        JsonElement current = CommonIOUtils.paraseJsonFromStr(str);
        for (String string : paths) {
            if (current == null) {
                return JsonNull.INSTANCE;
            }
            current = current.getAsJsonObject().get(string);
        }
        return current == null ? JsonNull.INSTANCE : current;
    }

    public static String getFromJson2Str(JsonElement json, String path) {
        JsonElement result = getFromJson2(json, path);
        return result.isJsonNull() ? "" : (result.isJsonObject() || result.isJsonArray()) ? result.toString() : result.getAsString();
    }

    public static int getFromJson2Integer(JsonElement json, String path) {
        JsonElement result = getFromJson2(json, path);
        return result.isJsonNull() ? 0 : result.getAsInt();
    }

    public static double getFromJson2Double(JsonElement json, String path) {
        JsonElement result = getFromJson2(json, path);
        return result.isJsonNull() ? 0 : result.getAsDouble();
    }

    public static boolean getFromJson2Boolean(JsonElement json, String path) {
        JsonElement result = getFromJson2(json, path);
        return !result.isJsonNull() && result.getAsBoolean();
    }

    public static Elements getElementsFromStr(String page, String cssQuery) {
        if (page == null || "".equals(page)) {
            return null;
        }
        Document document = Jsoup.parse(page);
        return document.select(cssQuery);
    }

    public static Element getElementFromStr(String page, String cssQuery) {
        if (page == null || "".equals(page)) {
            return null;
        }
        Document document = Jsoup.parse(page);
        return document.selectFirst(cssQuery);
    }

    public static Element getElementFromStr(Document document, String cssQuery) {
        return document.selectFirst(cssQuery);
    }

    public static JsonElement paraseJsonFromStr(String str) {
        if (str == null || "".equalsIgnoreCase(str)) {
            return JsonNull.INSTANCE;
        }
        try {
            return JsonParser.parseString(str);

        } catch (Exception e) {
            log.error(e.getMessage());
            log.error("解析json失败");
            log.error(str);
        }
        return JsonNull.INSTANCE;
    }

    public static JSONObject parseJson(String str){
        return JSONObject.parseObject(str);
    }

    /**
     * unicode编码转换为汉字
     *
     * @param unicodeStr 待转化的编码
     * @return 返回转化后的汉子
     */
    public static String UnicodeToCN(String unicodeStr) {
        Pattern pattern;
        if (!unicodeStr.contains("\\")) {
            pattern = Pattern.compile("(u(\\p{XDigit}{4}))");
        } else {
            pattern = Pattern.compile("(\\\\u(\\p{XDigit}{4}))");
        }
        Matcher matcher = pattern.matcher(unicodeStr);
        char ch;
        while (matcher.find()) {
            //group
            String group = matcher.group(2);
            //ch:'李四'
            ch = (char) Integer.parseInt(group, 16);
            //group1
            String group1 = matcher.group(1);
            unicodeStr = unicodeStr.replace(group1, ch + "");
        }

        return unicodeStr.replace("\\", "").trim();
    }

    /**
     * 获取剪切板里面的文字数据
     */
    public static String getSysClipboardText() {
        String ret = "";
        Clipboard sysClip = Toolkit.getDefaultToolkit().getSystemClipboard();
        // 获取剪切板中的内容
        Transferable clipTf = sysClip.getContents(null);

        if (clipTf != null) {
            // 检查内容是否是文本类型
            if (clipTf.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                try {
                    ret = (String) clipTf
                            .getTransferData(DataFlavor.stringFlavor);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return ret;
    }

    public static void close(RandomAccessFile file, InputStream is, HttpURLConnection conn) {
        try {
            if (file != null) {
                file.close();
            }
            if (is != null) {
                is.close();
            }
            if (conn != null) {
                conn.disconnect();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getValue(String txt, String key) {
        //则移除key，只返回值
        return txt.substring(txt.indexOf(":", txt.indexOf(key)) + 1, getIndex(txt, txt.indexOf(key)));
    }

    public static String getValue2(String txt, String key) {
        //则移除key，只返回值
        //如果逗号在括号之前检测到，那么说明value中不包含复杂结构，直接返回冒号后面的value
        String tmp = txt.substring(txt.indexOf(key));
        Pattern p_ = Pattern.compile("[({\\[]");
        Matcher matcher = p_.matcher(tmp);
        if (matcher.find() && tmp.indexOf(",") < matcher.start()) {
            return tmp.substring(tmp.indexOf(":") + 1, tmp.indexOf(",")).trim().replaceAll("\"", "");
        }
        return txt.substring(txt.indexOf(":", txt.indexOf(key)) + 1, getIndex(txt, txt.indexOf(key)));
    }


    public static int getIndex(String str, int index) {

        if (count.get() == null) {
            count.set(0);
        }
        Pattern p_ = Pattern.compile("[({\\[]");
        Matcher matcher_ = p_.matcher(str.substring(index));
        Pattern _p = Pattern.compile("[}\\])]");
        Matcher _matcher = _p.matcher(str.substring(index));
        int front = 0, after = 0;
        if (matcher_.find()) {
            front = matcher_.end();
        }
        if (_matcher.find()) {
            after = _matcher.end();
        }
        if (front < after) {
            count.set(count.get() + 1);
            index += front;
        } else {
            index += after;
            count.set(count.get() - 1);
        }
        if (count.get() == 0) {
            return index;
        }
        return getIndex(str, index);
    }

    @SneakyThrows
    public static boolean reNameDirectory(String source, String desc) {
        File des = new File(desc);
        if (!des.getParentFile().exists()) {
            FileUtils.forceMkdir(des.getParentFile());
        }
        boolean flag = new File(source).renameTo(new File(desc));
        if (!flag) {
            System.out.println("移动失败");
        }
        return flag;
    }

    public static String[] splitStrUseRegularExpression(String string, String exp, String removeExp) {
        ArrayList<String> list = new ArrayList<>();
        Pattern p = Pattern.compile(exp);
        Matcher matcher = p.matcher(string);
        while (matcher.find()) {
            if (removeExp != null) {
                list.add(Pattern.compile(removeExp).matcher(string.substring(matcher.start(), matcher.end())).replaceAll(""));
            } else {
                list.add(string.substring(matcher.start(), matcher.end()));
            }
            System.out.println(string.substring(matcher.start(), matcher.end()));
        }
        return list.toArray(new String[list.size()]);
    }

    public static String[] splitStrUseRegularExpression(String string, String exp) {
        ArrayList<String> list = new ArrayList<>();
        Pattern p = Pattern.compile(exp);
        Matcher matcher = p.matcher(string);
        while (matcher.find()) {
            list.add(string.substring(matcher.start(), matcher.end()));
            System.out.println(string.substring(matcher.start(), matcher.end()));
        }
        return list.toArray(new String[list.size()]);
    }

    public static int getIntegerFromJson(JsonElement json, String key) {
        String str = getFromJson2Str(json, key);
        return Integer.parseInt(str != null && !"".equals(str) ? str : "0");
    }


    public static void withTimer(Data data) {
        if (data == null || data.getUrl() == null || "".equals(data.getUrl())) {
            return;
        }
        AtomicBoolean stop = new AtomicBoolean(false);
        buildClientAndGet(data);
        data.setStoped(stop);
        long begin = System.currentTimeMillis();
        Thread timer = new Thread(() -> {
            //                log.debug("开始计时");
            //如果是调试模式则不计时
            Map<String, String> map = System.getenv();
            if (map.get("isDebug") != null && map.get("isDebug").equalsIgnoreCase("1")) {
                return;
            }
            boolean isTimeout = waitSeconds(data.getWaitSeconds());
            data.getStoped().set(true);
            if (isTimeout) {
                log.info("已用时：" + data.getWaitSeconds() + "秒，超时终止请求");
            }
        });
        timer.start();
        Thread execute = new Thread(() -> executeResponse(data));
        execute.start();
        while (!data.getStoped().get()) {
            try {
                TimeUnit.MILLISECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        //执行到这，不管请求线程有没有执行完成，都要执行clear方法，清空计时器
        long end = System.currentTimeMillis();
        if (data.isNeedLog()) {
            log.debug("请求耗时：" + transformMills2Date2(end - begin));
        }
        timer.interrupt();
        if (execute.isAlive() && data.getResult() == null) {
            log.info("线程超时，终止请求：" + data.getUrl());
            data.getGet().abort();
        }
        data.clear();
        if (data.getResult() == null || data.getResult().equalsIgnoreCase("")) {
            //json为空说明执行没有获得结果，重新执行本方法
            log.error("重新解析" + data.getUrl());
            data.getGet().releaseConnection();
            //判断如果可以重试，则递归调用
            if (data.doRetry()) {
                withTimer(data);
            }
        }
    }

    private static void executeResponse(Data data) {

        try {
            //执行HttpGet请求实例，也就是发起GET请求，响应结果保存到httpResponse变量中
            if (data.isNeedLog()) {
                log.debug("开始请求：" + data.getUrl());
            }
//            data.setResponse(data.getClient().execute(data.getGet()));
//            data.setResult(EntityUtils.toString(data.getResponse().getEntity()));
//            data.setStatusCode(data.getResponse().getStatusLine().getStatusCode());
//            EntityUtils.consume(data.getResponse().getEntity());
//            data.getResponse().close();
            if (data.getType() != null) {
                if (data.getType().equalsIgnoreCase("json")) {
                    data.setResult(data.getClient().execute(data.getGet(), strHandler));
                }
                if (data.getType().equalsIgnoreCase("byte")) {
                    data.setResult(data.getClient().execute(data.getGet(), byteHandler));
                }
            }
            if (data.isNeedLog()) {
                log.debug("请求结束" + data.getUrl());
            }
            data.clear();
            data.setStoped(new AtomicBoolean(true));
        } catch (Exception e) {
            if (e instanceof HttpResponseException) {
                data.setStatusCode(((HttpResponseException) e).getStatusCode());
                data.setResult(((HttpResponseException) e).getReasonPhrase());
            }
            if (data.isNeedLog()) {
                log.error("解析出错，异常信息：\n", e);
            }
            data.setStoped(new AtomicBoolean(true));
            data.clear();
        }
    }

    public static void downloadImg(Data data) {
        try {
            //执行HttpGet请求实例，也就是发起GET请求，响应结果保存到httpResponse变量中
            log.debug("开始请求");
            data.setResponse(data.getClient().execute(data.getGet()));
            log.debug("请求结束");

            data.setStatusCode(data.getResponse().getStatusLine().getStatusCode());
            data.setResult(data.getClient().execute(data.getGet(), byteHandler));
            data.clear();
            data.setStoped(new AtomicBoolean(true));
            log.debug("请求执行成功，返回json");
        } catch (Exception e) {
            log.error("解析出错，异常信息：\n", e);
            data.setStoped(new AtomicBoolean(true));
            data.clear();
        }
    }

    private static void buildClientAndGet(@NotNull Data result) {

        preBusiness();
        String headers = null;
        if (result.getHeader() != null && !result.getHeader().equals("")) {
            headers = result.getHeader() + "\n";
        }

//        CloseableHttpClient closeableHttpClient = HttpClients.custom().setRetryHandler((exception, executionCount, context) -> false).build();
        HttpGet httpGet = new HttpGet(result.getUrl());
        HttpHost proxy = result.isSpecialProxyConfig() ? new HttpHost(result.getProxyIP(), result.getProxyPort()) : new HttpHost(CommonParams.hostName, CommonParams.proxyPort);
        RequestConfig requestconfig = result.isProxy() ?
                RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD_STRICT).setProxy(proxy).setConnectTimeout(SOCKET_TIMEOUT).setSocketTimeout(SOCKET_TIMEOUT).setConnectionRequestTimeout(SOCKET_TIMEOUT).build() : RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD_STRICT).setConnectTimeout(SOCKET_TIMEOUT).setSocketTimeout(SOCKET_TIMEOUT).setConnectionRequestTimeout(SOCKET_TIMEOUT).build();
        if (headers != null) {

            String[] cookieArray = headers.trim().split("\n");
            for (String pair :
                    cookieArray) {
                httpGet.addHeader(pair.split(":")[0].trim(), pair.split(":")[1].trim());
            }
        }
        if (result.getCookie() != null && !"".equals(result.getCookie())) {
            httpGet.addHeader("cookie", result.getCookie());
        }

        if (result.getReferer() != null) {
            httpGet.addHeader("Referer", result.getReferer());
        }
        httpGet.setConfig(requestconfig);
        result.setClient(closeableHttpClient);
        result.setGet(httpGet);
    }

    /**
     * 判断字符串中是否含有表情
     *
     * @param source 传入字符串
     */
    public static boolean containsEmoji(String source) {
        int len = source.length();
        for (int i = 0; i < len; i++) {
            char hs = source.charAt(i);
            if (0xd800 <= hs && hs <= 0xdbff) {
                if (source.length() > 1) {
                    char ls = source.charAt(i + 1);
                    int uc = ((hs - 0xd800) * 0x400) + (ls - 0xdc00) + 0x10000;
                    if (0x1d000 <= uc && uc <= 0x1f77f) {
                        return true;
                    }
                }
            } else {
                // non surrogate
                if (0x2100 <= hs && hs <= 0x27ff && hs != 0x263b) {
                    return true;
                } else if (0x2B05 <= hs && hs <= 0x2b07) {
                    return true;
                } else if (0x2934 <= hs && hs <= 0x2935) {
                    return true;
                } else if (0x3297 <= hs && hs <= 0x3299) {
                    return true;
                } else if (hs == 0xa9 || hs == 0xae || hs == 0x303d
                        || hs == 0x3030 || hs == 0x2b55 || hs == 0x2b1c
                        || hs == 0x2b1b || hs == 0x2b50 || hs == 0x231a) {
                    return true;
                }
                if (source.length() > 1 && i < source.length() - 1) {
                    char ls = source.charAt(i + 1);
                    if (ls == 0x20e3) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 判断某个字符是不是表情
     *
     * @param codePoint 传入字符
     */
    private static boolean isEmojiCharacter(char codePoint) {
        return (codePoint == 0x0) || (codePoint == 0x9) || (codePoint == 0xA)
                || (codePoint == 0xD)
                || ((codePoint >= 0x20) && (codePoint <= 0xD7FF))
                || ((codePoint >= 0xE000) && (codePoint <= 0xFFFD));
    }

    /**
     * 过滤掉字符串中的表情
     *
     * @param source
     * @return
     */
    public static String filterEmoji(String source) {
        if (StringUtils.isBlank(source)) {
            return source;
        }
        StringBuilder buf = null;
        int len = source.length();
        for (int i = 0; i < len; i++) {
            char codePoint = source.charAt(i);
            if (isEmojiCharacter(codePoint)) {
                if (buf == null) {
                    buf = new StringBuilder(source.length());
                }
                buf.append(codePoint);
            }
        }
        if (buf == null) {
            return source;
        } else {
            if (buf.length() == len) {
                buf = null;
                return source;
            } else {
                return buf.toString();
            }
        }
    }

    public static String jsonString(String s) {
        char[] temp = s.toCharArray();
        int n = temp.length;
        for (int i = 0; i < n; i++) {
            if (temp[i] == ':' && temp[i + 1] == '"') {
                for (int j = i + 2; j < n; j++) {
                    if (temp[j] == '"') {
                        if (temp[j + 1] != ',' && temp[j + 1] != '}') {
                            temp[j] = '”';
                        } else if (temp[j + 1] == ',' || temp[j + 1] == '}') {
                            break;
                        }
                    }
                }
            }
        }
        return new String(temp);
    }

    public static String getScriptContainTheStr(Document document, String str) {
        Elements script = document.select("script");
        for (Element element : script) {
            if (element.data().contains(str)) {
                return element.data();
            }
        }
        return null;
    }

    public static Document getDocumentFromUrl(String url) {
        Data data = new Data();
        data.setUrl(url);
        data.setWaitSeconds(5);
        CommonIOUtils.withTimer(data);
        Assert.notNull(data.getResult(), "页面返回结果为空");
        Document document = Jsoup.parse(data.getResult());
        document.setBaseUri(url);
        return document;
    }

    public static Document getDocumentFromUrlProxy(String url) {
        Data data = new Data();
        data.setProxy(true);
        data.setUrl(url);
        data.setWaitSeconds(5);
        CommonIOUtils.withTimer(data);
        Assert.notNull(data.getResult(), "页面返回结果为空");
        Document document = Jsoup.parse(data.getResult());
        document.setBaseUri(url);
        return document;
    }

    public static void setRequestProperty(URLConnection connection, String header) {
        if (header != null && !"".equals(header)) {
            String[] cookieArray = header.trim().split("\n");
            for (String pair :
                    cookieArray) {
                connection.setRequestProperty(pair.split(":")[0].trim(), pair.split(":")[1].trim());
            }
        }
    }

    public static String getFileId(String fileName) {
        String id = null;
        Matcher matcher = Pattern.compile("^\\(\\w+\\)").matcher(fileName);
        if (matcher.find()) {
            id = Pattern.compile("[()]").matcher(fileName.substring(matcher.start(), matcher.end())).replaceAll("");
        }
        return id;
    }

    public static String getFileNumId(String fileName) {
        String id = null;
        Matcher matcher = Pattern.compile("\\(\\d+\\)").matcher(fileName);
        if (matcher.find()) {
            id = Pattern.compile("[()]").matcher(fileName.substring(matcher.start(), matcher.end())).replaceAll("");
        }
        return id;
    }

    public static String getFileId(File file) {
        String fileName = file.getName();
        Matcher matcher = Pattern.compile("(?=(\\d+\\.{4})?)\\(\\w+\\)").matcher(fileName);
        if (matcher.find()) {
            return getFileId2(fileName.substring(matcher.start(), matcher.end()));
//            id = Pattern.compile("(\\d+\\.{4})?|[()]").matcher(fileName.substring(matcher.start(), matcher.end())).replaceAll("");
        }
        return null;
    }

    public static String getFileId2(String fileName) {
        String id = null;
        Matcher matcher = Pattern.compile("\\((\\w+)\\)").matcher(fileName);
        if (matcher.find()) {
            id = matcher.group(1);
        }
        return id;
    }

    public static String getFileId2(File file) {
        String fileName = file.getName();
        String id = null;
        Matcher matcher = Pattern.compile("\\(\\w+\\)").matcher(fileName);
        if (matcher.find()) {
            id = Pattern.compile("[(av)]").matcher(fileName.substring(matcher.start(), matcher.end())).replaceAll("");
        }
        return id;
    }

    public static String getJSParams(String str, String param) {
        //匹配正则表达式，var+1个及以上空格+param+0个及以上空格+等号+0个及以上空格+若干非空字符+分号
        //var\s+comic_name\s*=\s*.+;
        Pattern pattern = Pattern.compile("var\\s+" + param + "\\s*=\\s*.+;");
        Matcher matcher = pattern.matcher(str);
        if (matcher.find()) {
            return str.substring(matcher.start(), matcher.end());
        }
        return "";
    }

    public static void binPath(File file, List<String> binList, String exp) {
        if (file.isDirectory()) {
            for (File listFile : Objects.requireNonNull(file.listFiles())) {
                binPath(listFile, binList, exp);
            }
        } else {
            if (Pattern.compile(exp).matcher(file.getName()).find()) {
                binList.add(file.getPath());
            }
        }
    }

    public static String generateComicFolderName(String title, Object comic_id) {
        return "(" + comic_id + ")" + title;
    }

    public static String generateChapterName(Object title, Object chapter_id) {
        return chapter_id + "." + title;
    }

    @NotNull
    @Contract(pure = true)
    public static String transformMills2Date(long seconds) {
        long hour = seconds / 3600;
        long minute = (seconds - hour * 3600) / 60;
        long second = (seconds - hour * 3600 - minute * 60);
        return hour + "小时" + minute + "分" + second + "秒";
    }

    @NotNull
    @Contract(pure = true)
    public static String transformMills2Date2(long mills) {
        long seconds = mills / 1000;
        long hour = seconds / 3600;
        long minute = (seconds - hour * 3600) / 60;
        long second = (seconds - hour * 3600 - minute * 60);
        long rest = mills - seconds * 1000;
        return hour + "小时" + minute + "分" + second + "秒" + rest + "毫秒";
    }

    public static boolean waitSeconds(int second) {
        try {
            TimeUnit.SECONDS.sleep(second);
            return true;
        } catch (InterruptedException e) {
//            e.printStackTrace();
            return false;
        }
    }

    public static void writeToFile(String string, String path) {
        File file = new File(path);
        try {
            Writer writer = new OutputStreamWriter(Files.newOutputStream(file.toPath()), StandardCharsets.UTF_8);
            writer.write(string);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @NotNull
    public static String getDrivePath(@NotNull String path) {
        return path.substring(0, path.indexOf("\\"));
    }

    @NotNull
    public static String getJsonStrFromMap(@NotNull Map<String, Object> params) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{");
        params.forEach((key, value) -> stringBuilder.append("\"").append(key).append("\"").append(":\"").append(value).append("\","));
        return stringBuilder.deleteCharAt(stringBuilder.length() - 1).append("}").toString();
    }

    public static String getJsonArrayStrFromMap(@NotNull Map<String, Object> params) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[");
        params.forEach((key, value) -> stringBuilder.append("\"").append(value).append("\","));
        return stringBuilder.deleteCharAt(stringBuilder.length() - 1).append("]").toString();
    }

    public static String getJsonStrFromMap2(@NotNull Map<String, Object> params) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{");
        params.forEach((key, value) -> stringBuilder.append(key).append(":").append(value).append(","));
        return stringBuilder.deleteCharAt(stringBuilder.length() - 1).append("}").toString();
    }

    public static boolean checkAllDone(@NotNull Vector<DownloadDTO> list) {
        long exist_count = list.stream().filter(downloadDTO -> new File(downloadDTO.getSavePath()).exists()).count();
        return list.size() == exist_count;
    }

    public static String getUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    @NotNull
    public static <T> T bean2Map(Object bean, Class<T> clazz) {
        T result = null;
        Method put = null;
        try {
            result = clazz.newInstance();
            put = clazz.getDeclaredMethod("put", Object.class, Object.class);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        BeanMap beanMap = BeanMap.create(bean);
        for (Object key : beanMap.keySet()) {
            try {
                Objects.requireNonNull(put).invoke(result, key, beanMap.get(key));
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return Objects.requireNonNull(result);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Serializable> T clone(T obj) {
        Object clonedObj = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
            oos.close();
            ByteArrayInputStream bais = new ByteArrayInputStream(bos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            clonedObj = ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return (T) clonedObj;
    }

    /**
     * 服务端接收@RequestBody
     *
     * @param data 请求封装对象
     * @return 请求结果
     */
    public static String postUrl(@NotNull Data data) {
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpHost proxy = data.isSpecialProxyConfig() ? new HttpHost(data.getProxyIP(), data.getProxyPort()) : new HttpHost(CommonParams.hostName, CommonParams.proxyPort);
            RequestConfig requestconfig = data.isProxy() ?
                    RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD_STRICT).setProxy(proxy).setConnectTimeout(SOCKET_TIMEOUT).setSocketTimeout(SOCKET_TIMEOUT).setConnectionRequestTimeout(SOCKET_TIMEOUT).build() : RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD_STRICT).setConnectTimeout(SOCKET_TIMEOUT).setSocketTimeout(SOCKET_TIMEOUT).setConnectionRequestTimeout(SOCKET_TIMEOUT).build();
            HttpPost post = new HttpPost(data.getUrl());
            String[] cookieArray = data.getHeader().trim().split("\n");
            for (String pair :
                    cookieArray) {
                post.addHeader(pair.split(":")[0].trim(), pair.split(":")[1].trim());
            }
            post.setConfig(requestconfig);
            StringEntity postingString = new StringEntity(data.getParams(), "utf-8");
            post.setEntity(postingString);
            HttpResponse response = httpClient.execute(post);
            data.setResult(EntityUtils.toString(response.getEntity()));
            data.setStatusCode(response.getStatusLine().getStatusCode());
            return data.getResult();
        } catch (Exception e) {
            return postUrl(data);
        }
    }

    public static long transformB2MB(long size) {
        BigDecimal ori = new BigDecimal(size);
        return ori.divide(new BigDecimal(1024), 2, RoundingMode.HALF_UP).divide(new BigDecimal(1024), 0, RoundingMode.HALF_UP).longValue();
    }

    public static boolean isSameFile(File file1, File file2) {
        try {
            FileInputStream f1 = new FileInputStream(file1);
            FileInputStream f2 = new FileInputStream(file2);
            String firstFileMd5 = DigestUtils.md5DigestAsHex(f1);
            String secondFileMd5 = DigestUtils.md5DigestAsHex(f2);
            f1.close();
            f2.close();
            return firstFileMd5.equalsIgnoreCase(secondFileMd5);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static Map<String, String> transform2Map(String str) {
        HashMap<String, String> map = new HashMap<>();
        Arrays.stream(str.split(",")).map(s -> s.replaceAll("[\"\\s]", "")).forEach(s -> map.put(s.substring(0, s.indexOf(":")), s.substring(s.indexOf(":") + 1)));
        return map;
    }

    public static Map<String, String> getMapFromJson(JsonElement json, String key) {
        Map<String, String> map = new HashMap<>();
        map.put(key, getFromJson2Str(json, key));
        return map;
    }

    public static String getValue3(String str, String key) {
        String[] split = str.split("\n");
        for (String s : split) {
            if (s.contains("img_data")) {
                return s.substring(s.indexOf("=") + 1).trim().replace("\"", "");
            }
        }
        return null;
    }

    public static String urlEncodeChinese(String url) {
        try {
            Matcher matcher = Pattern.compile("[\\u4e00-\\u9fa5]").matcher(url);
            String tmp;
            while (matcher.find()) {
                tmp = matcher.group();
                url = url.replaceAll(tmp, URLEncoder.encode(tmp, "UTF-8"));
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return url.replace(" ", "%20");
    }

    public static byte[] Object2Bytes(Object obj) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(obj);
        return byteArrayOutputStream.toByteArray();
    }

    @NotNull
    public static Object bytes2Object(byte[] bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
        return objectInputStream.readObject();
//        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
//
//        objectOutputStream.writeObject(obj);
    }

    /**
     * 服务端接收@RequestParam("query")，并且发送方发送的参数是map("query","value")形式
     *
     * @param data 请求封装对象
     * @return 请求结果
     */
    public static String postRestful(@NotNull Data data) {
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpHost proxy = data.isSpecialProxyConfig() ? new HttpHost(data.getProxyIP(), data.getProxyPort()) : new HttpHost(CommonParams.hostName, CommonParams.proxyPort);
            RequestConfig requestconfig = data.isProxy() ?
                    RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD_STRICT).setProxy(proxy).setConnectTimeout(SOCKET_TIMEOUT).setSocketTimeout(SOCKET_TIMEOUT).setConnectionRequestTimeout(SOCKET_TIMEOUT).build() : RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD_STRICT).setConnectTimeout(SOCKET_TIMEOUT).setSocketTimeout(SOCKET_TIMEOUT).setConnectionRequestTimeout(SOCKET_TIMEOUT).build();
            HttpPost post = new HttpPost(data.getUrl());
            String[] cookieArray = data.getHeader().trim().split("\n");
            for (String pair :
                    cookieArray) {
                post.addHeader(pair.split(":")[0].trim(), pair.split(":")[1].trim());
            }
            post.setConfig(requestconfig);
//            StringEntity postingString = new StringEntity(data.getParams(), "utf-8");
            List<NameValuePair> valuePairs = new ArrayList<>();
            if (data.getValuePairs() != null) {
                data.getValuePairs().forEach((s, s2) -> valuePairs.add(new BasicNameValuePair(s, s2)));
            }
//            post.setEntity(postingString);
            post.setEntity(new UrlEncodedFormEntity(valuePairs, "utf-8"));
            HttpResponse response = httpClient.execute(post);
            return EntityUtils.toString(response.getEntity());
        } catch (Exception e) {
//            return postUrl(data);
            e.printStackTrace();
            return null;
        }
    }

    public static Object toObject(ScriptObjectMirror mirror) {
        if (mirror.isEmpty()) {
            return null;
        }
        if (mirror.isArray()) {
            List<Object> list = new ArrayList<>();
            for (Map.Entry<String, Object> entry : mirror.entrySet()) {
                Object result = entry.getValue();
                if (result instanceof ScriptObjectMirror) {
                    list.add(toObject((ScriptObjectMirror) result));
                } else {
                    list.add(result);
                }
            }
            return list;
        }

        Map<String, Object> map = new HashMap<>();
        for (Map.Entry<String, Object> entry : mirror.entrySet()) {
            Object result = entry.getValue();
            if (result instanceof ScriptObjectMirror) {
                map.put(entry.getKey(), toObject((ScriptObjectMirror) result));
            } else {
                map.put(entry.getKey(), result);
            }
        }
        return map;
    }

    public static String removeCallbackStr(String string) {
        int begin = string.indexOf("(");
        int end = string.lastIndexOf(")");
        if (begin != -1 && end != -1) {
            return string.substring(begin + 1, end);
        }
        return string;
    }

    public static String replaceParam(String str, String key, String value) {
        System.out.println(str);
        int begin = str.indexOf(key + "=") + key.length() + 1;
        int end = str.indexOf("&", begin);
        return str.replace(key + "=" + str.substring(begin, end), key + "=" + value);
    }

    public static String format(String str, Object... args) {
        for (Object arg : args) {
            str = str.replaceFirst("\\{}", String.valueOf(arg));
        }
        return str;
    }

    public static void testLog(){
        log.debug("debug日志");
        log.info("info日志");
    }

    public void domain() {
        System.out.println(replaceParam("https://user.qzone.qq.com/proxy/domain/photo.qzone.qq.com/fcgi-bin/cgi_floatview_photo_list_v2?g_tk=690943553&callback=viewer_Callback&t=733176778&topicId=V138wU941273CH&picKey=NRMAVjR0M2g1OVVMU1k0bHZhSXNFWQcAcGhvdG9jcQ!!&shootTime=&cmtOrder=1&fupdate=1&plat=qzone&source=qzone&cmtNum=10&likeNum=5&inCharset=utf-8&outCharset=utf-8&callbackFun=viewer&offset=0&number=15&uin=512239520&hostUin=1350377182&appid=4&isFirst=1&sortOrder=1&showMode=1&need_private_comment=1&prevNum=9&postNum=18&_=1621493069305", "picKey", "新的值"));
    }

    public void test() {
        System.out.println(LZString.decompressFromBase64(getValue3("            let img_data = \"JYWw5g9AzAjFAcAGCBWFA2KBOGEYDoArABzABpRJYFk1McJA+HUFcMgk8y6OJVDbXFgCYipIA===\"\n" +
                "            let page = \"1\"\n" +
                "            let manga_url = \"https://www.manhuacat.com/manga/32105.html\";\n" +
                "            let cur_url = \"https://www.manhuacat.com/manga/32105/521761.html\"", "img_data")));
    }

    public static List<String> getDirChapter(File file) {
        File[] files = file.listFiles();
        assert files != null;
        Set<String> chapterSet = Arrays.stream(files).filter(File::isDirectory).map(File::getName).collect(Collectors.toSet());
        return new ArrayList<String>(){
            {
                addAll(chapterSet);
            }
        };
    }
    public static Class<?> getEntityExists(String entity) {
        Class<?> aClass;
        if (entityList.isEmpty()) {
            Reflections reflections = new Reflections("cn.zpl.common.bean");
            entityList.addAll(reflections.getSubTypesOf(Serializable.class));
            reflections = new Reflections("BOOT-INF.classes.cn.zpl.commondaocenter.bean");
//            ConfigurationBuilder builder = new ConfigurationBuilder();
//            builder.addClassLoaders(this.getClass().getClassLoader());
//            builder.forPackage("cn.zpl.commondaocenter.bean", this.getClass().getClassLoader());
//            Reflections reflections1 = new Reflections(builder);
//            Set<Class<? extends Serializable>> subTypesOf = reflections1.getSubTypesOf(Serializable.class);
//            log.debug("找到的类：{}", subTypesOf);

//            subTypesOf.forEach(System.out::println);
            entityList.addAll(reflections.getSubTypesOf(Serializable.class));
        }
        Optional<Class<? extends Serializable>> first = entityList.stream().filter(clazz -> clazz.getSimpleName().equalsIgnoreCase(entity)).findFirst();
//        log.debug(CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, "PictureAnalyze"));
        return first.orElse(null);
    }
}
