package cn.zpl.thread;

import cn.zpl.myInterface.DownloadThreadInterface;
import cn.zpl.pojo.DownloadDTO;
import cn.zpl.pojo.MultiPartInfoHolder;
import cn.zpl.util.CommonIOUtils;
import cn.zpl.util.SaveLog;
import cn.zpl.util.URLConnectionTool;
import cn.zpl.util.UrlContainer;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.SocketException;

@Slf4j
public class DownloadWithMultipleThread extends CommonThread implements Runnable, DownloadThreadInterface {

    private final long startIndex;
    private final long endIndex;
    private final String url;
    private final DownloadDTO data;

    private boolean timeOut = false;

    public void setTimeOut(boolean timeOut) {
        this.timeOut = timeOut;
    }


    public DownloadWithMultipleThread(@NotNull DownloadDTO data) {
        this.startIndex = data.getStartIndex();
        this.endIndex = data.getEndIndex();
        this.url = data.getUrl();
        this.data = data;
    }

//    @Override
//    public void run() {
//        if (new File(data.getSavePath()).exists() && SaveLog.isCompeleteMultiple(data)) {
//            log.debug(data.getSavePath() + "已下载，跳过");
//            return;
//        }
//        UrlContainer container = new UrlContainer(data);
//        HttpURLConnection conn = container.isHttps() ?
//                URLConnectionTool.getHttpsURLConnection(container) : URLConnectionTool.getHttpURLConnection(container);
//
//        InputStream is = null;
//        if (data.getReferer() != null && !"".equals(data.getReferer())) {
//            conn.setRequestProperty("Referer", data.getReferer());
//        }
//        if (data.getHeader() != null && !"".equals(data.getHeader())) {
//            CommonIOUtils.setRequestProperty(conn, data.getHeader());
//        }
//        conn.setRequestProperty("Range", "bytes=" + startIndex + "-" + endIndex);
//        RandomAccessFile randomfile = null;
//        try {
//            is = conn.getInputStream();
//            if (conn.getResponseCode() == 206) {
//                File saveDir = new File(data.getSavePath());
//                randomfile = new RandomAccessFile(saveDir, "rwd");
//                randomfile.seek(startIndex);
//                byte[] getData = new byte[7000000];
//                int len;
//                Thread timer = new checkTimeOut(this, 600);
//                timer.start();
//                //读取到内容并且标志未超时
//                while ((len = is.read(getData)) != -1 && !timeOut) {
//                    randomfile.write(getData, 0, len);
//                }
//                if (timeOut) {
//                    throw new RuntimeException("此线程下载时间过长，重新启动");
//                }
//                randomfile.close();
//                timer.interrupt();
//            }
//            log.debug("区块下载完毕：bytes=" + startIndex + "-" + endIndex);
//        } catch (RuntimeException e1) {
//            //runtime错误表示超时，切换到代理下载
//            CommonIOUtils.close(randomfile, is, conn);
//            data.setProxy(!data.isProxy());
//            log.error("下载超时：", e1);
//            new DownloadWithMultipleThread(data).run();
//        } catch (SocketException e) {
//            //SocketException错误表示超时，切换到代理下载
//            CommonIOUtils.close(randomfile, is, conn);
//            log.info("切换到代理下载");
//            data.setProxy(true);
//            new DownloadWithMultipleThread(data).run();
//        } catch (Exception e) {
//            e.printStackTrace();
//            log.error(url + "区块：bytes=" + startIndex + "-" + endIndex + "访问失败，重新解析");
//            CommonIOUtils.close(randomfile, is, conn);
//            new DownloadWithMultipleThread(data).run();
//        } finally {
//            CommonIOUtils.close(randomfile, is, conn);
//        }
//    }

    @Override
    public void domain() throws Exception {
        doBusiness();
    }
//    public void domain() throws Exception {
//        if (new File(data.getSavePath()).exists() && SaveLog.isCompeleteMultiple(data)) {
//            log.debug(data.getSavePath() + "已下载，跳过");
//            return;
//        }
//        UrlContainer container = new UrlContainer(data);
//        HttpURLConnection conn = container.isHttps() ?
//                URLConnectionTool.getHttpsURLConnection(container) : URLConnectionTool.getHttpURLConnection(container);
//
//        InputStream is = null;
//        if (data.getReferer() != null && !"".equals(data.getReferer())) {
//            conn.setRequestProperty("Referer", data.getReferer());
//        }
//        if (data.getHeader() != null && !"".equals(data.getHeader())) {
//            CommonIOUtils.setRequestProperty(conn, data.getHeader());
//        }
//        conn.setRequestProperty("Range", "bytes=" + startIndex + "-" + endIndex);
//        RandomAccessFile randomfile = null;
//        try {
//            is = conn.getInputStream();
//            if (conn.getResponseCode() == 206) {
//                File saveDir = new File(data.getSavePath());
//                randomfile = new RandomAccessFile(saveDir, "rwd");
//                randomfile.seek(startIndex);
//                byte[] getData = new byte[7000000];
//                int len;
//                Thread timer = new checkTimeOut(this, 600);
//                timer.start();
//                //读取到内容并且标志未超时
//                while ((len = is.read(getData)) != -1 && !timeOut) {
//                    randomfile.write(getData, 0, len);
//                }
//                if (timeOut) {
//                    throw new RuntimeException("此线程下载时间过长，重新启动");
//                }
//                randomfile.close();
//                timer.interrupt();
//            }
//            log.debug("区块下载完毕：bytes=" + startIndex + "-" + endIndex);
//            data.getInfoHolder().setCompleteInfo(startIndex, endIndex);
//        } catch (RuntimeException e1) {
//            //runtime错误表示超时，切换到代理下载
//            CommonIOUtils.close(randomfile, is, conn);
//            data.setProxy(!data.isProxy());
//            log.error("下载超时：", e1);
//            new DownloadWithMultipleThread(data).run();
//        } catch (SocketException e) {
//            //SocketException错误表示超时，切换到代理下载
//            CommonIOUtils.close(randomfile, is, conn);
//            log.info("切换到代理下载");
//            data.setProxy(true);
//            new DownloadWithMultipleThread(data).run();
//        } catch (Exception e) {
//            e.printStackTrace();
//            log.error(url + "区块：bytes=" + startIndex + "-" + endIndex + "访问失败，重新解析");
//            CommonIOUtils.close(randomfile, is, conn);
//            new DownloadWithMultipleThread(data).run();
//        } finally {
//            CommonIOUtils.close(randomfile, is, conn);
//        }
//    }

    public MultiPartInfoHolder doBusiness() {
        if (new File(data.getSavePath()).exists() && SaveLog.isCompeleteMultiple(data)) {
            log.debug(data.getSavePath() + "已下载，跳过");
            return data.getInfoHolder().setCompleteInfo(startIndex, endIndex);
        }
        UrlContainer container = new UrlContainer(data);
        HttpURLConnection conn = container.isHttps() ?
                URLConnectionTool.getHttpsURLConnection(container) : URLConnectionTool.getHttpURLConnection(container);

        InputStream is = null;
        if (data.getReferer() != null && !"".equals(data.getReferer())) {
            conn.setRequestProperty("Referer", data.getReferer());
        }
        if (data.getHeader() != null && !"".equals(data.getHeader())) {
            CommonIOUtils.setRequestProperty(conn, data.getHeader());
        }
        conn.setRequestProperty("Range", "bytes=" + startIndex + "-" + endIndex);
        RandomAccessFile randomfile = null;
        try {
            is = conn.getInputStream();
            if (conn.getResponseCode() == 206) {
                File saveDir = new File(data.getSavePath());
                randomfile = new RandomAccessFile(saveDir, "rwd");
                randomfile.seek(startIndex);
                byte[] getData = new byte[7000000];
                int len;
                Thread timer = new checkTimeOut(this, 600);
                timer.start();
                //读取到内容并且标志未超时
                while ((len = is.read(getData)) != -1 && !timeOut) {
                    randomfile.write(getData, 0, len);
                }
                if (timeOut) {
                    throw new RuntimeException("此线程下载时间过长，重新启动");
                }
                randomfile.close();
                timer.interrupt();
            }
            log.debug("区块下载完毕：bytes=" + startIndex + "-" + endIndex);
        } catch (RuntimeException e1) {
            //runtime错误表示超时，切换到代理下载
            CommonIOUtils.close(randomfile, is, conn);
            data.setProxy(!data.isProxy());
            log.error("下载超时：", e1);
            new DownloadWithMultipleThread(data).run();
        } catch (SocketException e) {
            //SocketException错误表示超时，切换到代理下载
            CommonIOUtils.close(randomfile, is, conn);
            log.info("切换到代理下载");
            data.setProxy(true);
            new DownloadWithMultipleThread(data).run();
        } catch (Exception e) {
            e.printStackTrace();
            log.error(url + "区块：bytes=" + startIndex + "-" + endIndex + "访问失败，重新解析");
            CommonIOUtils.close(randomfile, is, conn);
            new DownloadWithMultipleThread(data).run();
        } finally {
            CommonIOUtils.close(randomfile, is, conn);
        }
        return data.getInfoHolder().setCompleteInfo(startIndex, endIndex);
    }

    @Override
    public MultiPartInfoHolder call() {
        return doBusiness();
    }
}