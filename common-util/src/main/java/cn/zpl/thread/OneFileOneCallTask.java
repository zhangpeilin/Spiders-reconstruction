package cn.zpl.thread;

import cn.zpl.frame.MyJFrame;
import cn.zpl.pojo.DownloadDTO;
import cn.zpl.util.CommonIOUtils;
import cn.zpl.util.SaveLogForImages;
import cn.zpl.util.URLConnectionTool;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

@Slf4j
public class OneFileOneCallTask extends CommonThreadv2<Boolean> {

    private final String url;
    private final DownloadDTO data;
    private long length;
    private final Object lock;
    private MyJFrame frame;

    private boolean checkExist = true;

    public OneFileOneCallTask(@NotNull DownloadDTO data) {
        this.data = data;
        this.url = data.getUrl();
        this.lock = data.getSynchronizeLock();
    }
    public OneFileOneCallTask(@NotNull DownloadDTO data, boolean checkExist) {
        this.data = data;
        this.url = data.getUrl();
        this.lock = data.getSynchronizeLock();
        this.checkExist = checkExist;
    }

    public DownloadDTO getData() {
        return this.data;
    }

    @Override
    public Boolean call() {
        run();
        return true;
    }

    public void run() {

        length = 0;
        File saveDir = new File(data.getSavePath());
        if (!saveDir.getParentFile().exists()) {
            boolean mkdirs = saveDir.getParentFile().mkdirs();
            if (!mkdirs) {
                log.warn(saveDir.getParentFile() + "目录创建结果false，或许有隐患");
            }
        }
        if (126816256 > new File(data.getSavePath()).getParentFile().getFreeSpace()) {
            log.error("磁盘空间不足120MB，停止下载，程序退出" + data.getSavePath());
            System.exit(0);
        }
        if (checkExist && new File(data.getSavePath()).exists() && SaveLogForImages.isCompelete(data)) {
            data.setComplete(true);
            log.debug(data.getSavePath() + "已下载，跳过");
            return;
        }
        HttpURLConnection conn = URLConnectionTool.getHttpURLConnection(data.isProxy(), url);
        BufferedOutputStream outputStream = null;
        Thread progressing = null;
        try {
            if (data.getReferer() != null && !"".equals(data.getReferer())) {
                conn.setRequestProperty("Referer", data.getReferer());
            }
            if (data.getHeader() != null && !"".equals(data.getHeader())) {
                CommonIOUtils.setRequestProperty(conn, data.getHeader());
            }
            String length_ = conn.getHeaderField("content-length");

            //如果isStrict为true，表示需要严格校验，默认为false
            if (data.isStrict()) {
                if (length_ == null) {
                    log.info(data.getUrl());
                    throw new RuntimeException("请求未返回文件长度，重新开始下载");
                }
                String Content_Type = conn.getHeaderField("Content-Type");
                if (Content_Type == null) {
                    throw new RuntimeException("返回格式为空，重新下载");
                }
                if (data.isImage() && !(Content_Type.contains("png") || Content_Type.contains("jpg") || Content_Type.contains("jpeg") || Content_Type.contains("gif"))) {
                    log.info(data.getUrl());
                    log.error(CommonIOUtils.toString(conn.getInputStream()));
                    log.error("格式：" + Content_Type);
                    throw new RuntimeException("请求未返回" + data.getType() + "格式，重新开始下载");
                }
            }
            long size = Long.parseLong(length_ == null ? "0" : length_);
            if (data.getFileLength() == 0 && size != 0) {
                data.setFileLength(size);
            }
            InputStream is = conn.getInputStream();
            if (String.valueOf(conn.getResponseCode()).startsWith("30")) {
                String redirect = URLConnectionTool.getRedirectLocation(data);
                if (!StringUtils.isEmpty(redirect)) {
                    data.setUrl(redirect);
                    run();
                    return;
                }
            }
            if (conn.getResponseCode() == 200) {
                outputStream = new BufferedOutputStream(Files.newOutputStream(saveDir.toPath()));
                //对于图片来说再长的数组都是浪费，只用前面一两千个
                byte[] getData = data.isImage() ? new byte[20000] : new byte[7000000];
                int len;
                while ((len = is.read(getData)) != -1) {
                    if (frame != null && size != 0 && progressing == null) {
                        progressing = new Thread(() -> {
                            while (length != size && !Thread.interrupted()) {
                                BigDecimal rate = new BigDecimal(length).divide(new BigDecimal(size), 4, RoundingMode.HALF_UP).multiply(new BigDecimal(100)).setScale(0, RoundingMode.HALF_UP);
                                StringBuilder stringBuilder;
                                stringBuilder = new StringBuilder();
                                stringBuilder.append("<p>压缩包大小：").append(CommonIOUtils.transformB2MB(size)).append("MB<br>").append("当前下载进度：").append(rate).append("%<br>");
                                frame.updateInfo(Thread.currentThread().getName(), stringBuilder.toString());
//                                frame.getScreen().setText(stringBuilder.toString());
                            }
                            frame.removeInfo(Thread.currentThread().getName());
                        });
                        progressing.start();
                    }
                    outputStream.write(getData, 0, len);
                    length += len;
                }
                is.close();
                //必须先关闭文件，否则下次执行的时候无法删除不正常文件
                outputStream.close();
                if (progressing != null) {
                    progressing.interrupt();
                }
                conn.disconnect();
                //如果服务器返回chunked，那么只需要比较data中的长度和下载到文件的长度即可
                if (conn.getHeaderField("Transfer-Encoding") != null && conn.getHeaderField("Transfer-Encoding").equalsIgnoreCase("chunked") && data.getFileLength() != 0 && data.getFileLength() == saveDir.length()) {
                    log.debug("服务端返回chunked，不再校验文件大小一致性");
                } else if ((data.getFileLength() != 0) && (saveDir.length() != size || length != size)) {
//                    System.out.println("下载到的文件实际大小与返回头标记大小不一致");
                    log.error("下载到的文件实际大小与返回头标记大小不一致");
                    run();
                    return;
                }
                if (data.getFileLength() == 0) {
                    data.setFileLength(saveDir.length());
                }
                //当时图片类型信息时，如果标志位不需要记录日志，则跳过
                if (data.isNeedLog()) {
                    synchronized (lock) {
                        SaveLogForImages.saveLog(data);
                        data.setComplete(true);
                    }
                }
            }
        } catch (Exception e) {
            log.error("数据内容：{}", data);
            if (progressing != null) {
                progressing.interrupt();
            }
            if (!data.doRetry()) {
                return;
            }
            log.error("报错", e);
            if (126816256 > new File(data.getSavePath()).getParentFile().getFreeSpace()) {
                log.error("磁盘空间不足120MB，停止下载，程序退出");
                System.exit(0);
            }
            if (e.getClass().getName().contains("FileNotFound")) {
                log.error("文件不存在，五秒后重试");
                //判断保存合并文件的磁盘空间是否大于缓存的分段占用空间
                log.error(data.getSavePath());
            }
            //如果是404错误，则直接返回不再重试，否则休眠5秒后重试
            try {
                if (conn.getResponseCode() == 404) {
                    data.stopRetry();
                } else {
                    TimeUnit.SECONDS.sleep(5);
                }
            } catch (InterruptedException | IOException e1) {
                e1.printStackTrace();
            }
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
                conn.disconnect();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            if (!data.doRetry()) {
                return;
            }
            if (data.getFileLength() != 0) {
                try {
                    log.debug("视频下载，休眠5秒");
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
            run();
        }
    }

    @Override
    public void domain() throws Exception {

    }

    public MyJFrame getFrame() {
        return frame;
    }

    public void setFrame(MyJFrame frame) {
        this.frame = frame;
    }
}