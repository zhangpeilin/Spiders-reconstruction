package cn.zpl.util.m3u8.thread;

import cn.zpl.common.bean.VideoInfo;
import cn.zpl.pojo.DownloadDTO;
import cn.zpl.thread.CommonThread;
import cn.zpl.thread.OneFileOneThread;
import cn.zpl.util.CommonIOUtils;
import cn.zpl.util.FFMEPGToolsPatch;
import cn.zpl.util.m3u8.M3U8;
import cn.zpl.util.m3u8.M3U8Ts;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class M3u8FileDownloadThread extends CommonThread {

    String path;
    public M3u8FileDownloadThread(String path) {
        this.path = path;
    }


    /**
     * 正式文件存储地址(合并之后的文件)
     */
    private String tofile = "E:\\m3u8\\";

    private String directory;

    private String fileName;

    private String host;

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    private ExecutorService executor = Executors.newFixedThreadPool(10);

    public String getTofile() {
        return tofile;
    }

    public void setTofile(String tofile) {
        this.tofile = tofile;
    }

    public String getFolderpath() {
        return folderpath;
    }

    public void setFolderpath(String folderpath) {
        this.folderpath = folderpath;
    }

    public String getFoldername() {
        return foldername;
    }

    public void setFoldername(String foldername) {
        this.foldername = foldername;
    }

    /**
     * 临时文件存储地址(M3U8视频段)
     */
    private String folderpath = "E:\\m3u8\\temp";
    /**
     * 下载完的文件所放的文件夹名字
     */
    private String foldername = UUID.randomUUID().toString().replaceAll("-", "");
    public void downloadCore(String url) throws IOException, InterruptedException {
        //通过剪切板获取复制的url
        if (url != null && (url.toLowerCase().startsWith("http://") || url.toLowerCase().startsWith("https://"))) {
            /* m3u8地址 */
            System.out.println("获取到链接地址：" + url);
        } else {
            System.out.println("不合法链接地址！");
            return;
        }
        //设置好初始路径
        folderpath += File.separator + foldername;

        File dir = new File(folderpath);
        //防止文件夹里有其他文件，做好分类
        if (dir.exists()) {
            System.out.println("文件夹：" + folderpath + "已存在！");
            return;
        }
        //获取到地址里面的m3u8文件名称
        final String m3u8name = url.substring(url.lastIndexOf("/"), url.toLowerCase().indexOf(".m3u8", url.lastIndexOf("/"))) + ".m3u8";

        //先将m3u8文件保存到本地，以便不用合成也能播放对应的视频
        saveM3u8File(folderpath, url, m3u8name);
        //解析M3U8地址为对象
        M3U8 m3u8 = parseIndex(folderpath, m3u8name, url);
        m3u8.setFilePath(directory);
        m3u8.setFileName(fileName);
        if (host != null && !"".equals(host)) {
            m3u8.setBasepath(host);
        }
        //根据M3U8对象获取时长
        float duration = getDuration(m3u8);
        System.out.println("时长: " + ((int) duration / 60) + "分" + (int) duration % 60 + "秒");


//        for (M3U8Ts m3U8Ts : m3u8.getTsList()) {
//            String newName = m3U8Ts.getFile().substring(m3U8Ts.getFile().lastIndexOf("tipsid") + 6);
//            newName = (Integer.parseInt(newName.substring(0, newName.lastIndexOf("."))) + 1) + ".ts";
//            m3U8Ts.setFile(newName);
//        }
        //根据M3U8对象下载视频段
        VideoInfo info = new VideoInfo();
        info.setSavedLocalName(new File(folderpath, CommonIOUtils.filterFileName2(m3u8name)).getPath());
        info.setTimeLength(String.valueOf(duration * 1000));
        FFMEPGToolsPatch.mergeXDFTs(info);

        delAllFile(m3u8.getFpath());
        System.out.println("下载完成，文件在: " + folderpath);
    }

    public void domain() throws Exception{
        downloadByM3U8(path);
    }

    public void downloadByM3U8(String path) throws IOException {
        //设置好初始路径
//        folderpath += File.separator + foldername;

        File m3u8File = new File(path);
        File dir = new File(folderpath + foldername);
        //防止文件夹里有其他文件，做好分类
        if (dir.exists()) {
            System.out.println("文件夹：" + folderpath + "已存在！");
            return;
        }
        //获取到地址里面的m3u8文件名称
        final String m3u8name = m3u8File.getName();
        //解析M3U8地址为对象
        M3U8 m3u8 = parseIndex(m3u8File.getParent(), m3u8name, "http://test.com");

        //根据M3U8对象获取时长
        float duration = getDuration(m3u8);
        System.out.println("时长: " + ((int) duration / 60) + "分" + (int) duration % 60 + "秒");
        //重命名文件名
        //根据M3U8对象下载视频段
        VideoInfo info = new VideoInfo();
        info.setSavedLocalName(new File(folderpath, CommonIOUtils.filterFileName2(m3u8name)).getPath());
        info.setTimeLength(String.valueOf(duration * 1000));
        FFMEPGToolsPatch.mergeXDFTs(info);

        System.out.println("下载完成，文件在: " + folderpath);

        //关闭线程池

    }

    /**
     * 获取剪切板里面的文字数据
     *
     * @return
     * @Title: getSysClipboardText
     * @version v1.0.0
     * @author guojin
     * @date 2019年2月15日上午11:24:34
     */
    public String getSysClipboardText() {
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

    /**
     * @param
     * @return Modification History:
     * Date         Author          Version            Description
     * ---------------------------------------------------------*
     * 2019年2月19日     guojin           v1.0.0               修改原因
     * @ClassName: M3U8Downloader.java
     * @Description: 该类的功能描述
     * @Title:saveM3u8File
     * @version: v1.0.0
     * @author: guojin
     * @date: 2019年2月19日 下午12:54:19
     */
    private static void saveM3u8File(String folderpath, String url, String m3u8name) throws MalformedURLException, IOException {

        DownloadDTO downloadDTO = new DownloadDTO();
        downloadDTO.setUrl(url);
        downloadDTO.setProxy(true);
        downloadDTO.setSavePath(new File(folderpath, CommonIOUtils.filterFileName2(m3u8name)).getPath());
        new OneFileOneThread(downloadDTO).run();
        System.out.println("下载m3u8文件完成");
//        m3u8name = CommonIOUtils.filterFileName2(m3u8name);
//        InputStream ireader = new URL(url).openStream();
//
//        final File dir = new File(folderpath);
//
//        if (!dir.exists()) {
//            dir.mkdirs();
//        }
//
//        FileOutputStream writer = new FileOutputStream(new File(dir, m3u8name));
//
//        IOUtils.copyLarge(ireader, writer);
//
//        ireader.close();
//        writer.close();

    }

    /**
     * 根据M3U8对象获取时长
     *
     * @param m3u8
     * @return
     */
    private static float getDuration(M3U8 m3u8) {
        float duration = 0;
        for (M3U8Ts ts : m3u8.getTsList()) {
            duration += ts.getSeconds();
        }
        return duration;
    }

    /**
     * 合并文件
     *
     * @param m3u8
     * @param tofile
     * @throws IOException
     */
    public static void merge(M3U8 m3u8, String tofile) throws IOException {

        for (M3U8 m : m3u8.getM3u8List()) {
            m.setFilePath(m3u8.getFilePath());
            m.setFileName(m3u8.getFileName());
            merge(m, tofile);
        }

        if (m3u8.getTsList().size() == 0) {
            return;
        }
        File file = new File(m3u8.getFilePath(), m3u8.getFileName());
        FileOutputStream fos = new FileOutputStream(file);

        for (M3U8Ts ts : m3u8.getTsList()) {
            File fileTemp = new File(m3u8.getFpath(), ts.getFile());
            if (fileTemp.exists()) {
                IOUtils.copyLarge(new FileInputStream(fileTemp), fos);
            } else {
                throw new RuntimeException(ts.getFile() + "文件不存在，合成失败！");
            }
        }

        fos.close();

        System.out.println("已合成：" + file);
    }

    /**
     * 根据M3U8对象下载视频段
     *
     * @param m3u8
     */
    public void download(final M3U8 m3u8) {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        final File dir = new File(m3u8.getFpath());
        if (!dir.exists()) {
            dir.mkdirs();
        }

        for (final M3U8 m : m3u8.getM3u8List()) {
            //下载对应的m3u8
            download(m);
        }

        for (final M3U8Ts ts : m3u8.getTsList()) {
            executor.execute(() -> downloadThread(ts, m3u8, dir));
        }
        executor.shutdown();
        System.out.println("等待下载中...");
        while (!executor.isTerminated()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void downloadThread(@NotNull M3U8Ts ts, M3U8 m3u8, File dir) {
        DownloadDTO dto = new DownloadDTO();
        dto.setProxy(true);
        dto.setAlwaysRetry();
        dto.setUrl(ts.getSubUrl().startsWith("http") ? ts.getSubUrl() : m3u8.getBasepath() + ts.getSubUrl());
        dto.setSavePath(new File(dir, ts.getFile()).getPath());
        new OneFileOneThread(dto).run();

//            FileOutputStream writer = new FileOutputStream(new File(dir, ts.getFile()));
//            IOUtils.copyLarge(new URL(ts.getSubUrl().startsWith("http") ? ts.getSubUrl() : m3u8.getBasepath() + ts.getSubUrl()).openStream(), writer);
//            writer.close();
        System.out.println("视频段: " + ts + "下载完成");
    }

    /**
     * 删除文件夹里面的所有数据
     *
     * @param folderPath
     * @return void
     * @Title: delFolder
     * @version v1.0.0
     * @author guojin
     * @date 2019年2月27日上午11:57:25
     */
    public static void delFolder(String folderPath) {
        try {
            delAllFile(folderPath); //删除完里面所有内容
            String filePath = folderPath;
            filePath = filePath.toString();
            File myFilePath = new File(filePath);
            myFilePath.delete(); //删除空文件夹
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //删除指定文件夹下所有文件
    //param path 文件夹完整绝对路径
    public static void delAllFile(String path) {
        try {
            System.gc();
            FileUtils.forceDeleteOnExit(new File(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 解析M3U8地址为对象
     *
     * @param
     * @return Modification History:
     * Date         Author          Version            Description
     * ---------------------------------------------------------*
     * 2019年2月20日     guojin           v1.0.0               修改原因
     * @ClassName: M3U8Downloader.java
     * @Description: 该类的功能描述
     * @Title:parseIndex
     * @version: v1.0.0
     * @author: guojin
     * @date: 2019年2月20日 下午3:43:49
     */
    @NotNull
    static M3U8 parseIndex(String folderpath, String m3u8name, @NotNull String url) throws IOException {

        m3u8name = CommonIOUtils.filterFileName2(m3u8name);
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(folderpath, m3u8name))));
        //解析请求的相关路径
        String basepath;

        basepath = url.substring(0, url.lastIndexOf("/") + 1);
        M3U8 ret = new M3U8();
        Pattern pattern = Pattern.compile("\\D*(\\d+)p.*.m3u8");
        Matcher matcher = pattern.matcher(m3u8name);
        if (matcher.find()) {
            try {
                ret.setQuality(Integer.parseInt(matcher.group(1)));
            } catch (Exception e) {
                ret.setQuality(0);
            }
        }
        //基本url路径
        ret.setBasepath(basepath);
        //基本存放文件夹地址
        ret.setFpath(folderpath);

        String line;
        int num = 0;
        float seconds = 0;
        while ((line = reader.readLine()) != null && !"".equalsIgnoreCase(line)) {
            if (line.startsWith("#")) {
                if (line.startsWith("#EXTINF:")) {
                    line = line.substring(8);
                    if (line.endsWith(",")) {
                        line = line.substring(0, line.length() - 1);
                    }
                    if (line.contains(",")) {
                        line = line.substring(0, line.indexOf(","));
                    }
                    //解析每个分段的长度
                    seconds = Float.parseFloat(line);
                }
                continue;
            }
            //文件包含另一个m3u8文件
            if (line.contains("m3u8")) {
                if (line.toLowerCase().startsWith("http://") || line.toLowerCase().startsWith("https://")) {
                    String linetag = line.substring(line.lastIndexOf("/"), line.toLowerCase().indexOf(".m3u8", line.lastIndexOf("/")));
                    String linename = linetag + ".m3u8";
                    int tp = basepath.indexOf(line);
                    String nfpath = folderpath;
                    if (tp != -1) {
                        if ((line.lastIndexOf("/") + 1) > (tp + basepath.length())) {//判断路径是否重复
                            line.substring(tp + basepath.length(), line.lastIndexOf("/") + 1);
                        } else {
                            nfpath = folderpath;
                        }
                    } else {
                        nfpath = folderpath + linetag + File.separator;
                    }
                    //获取远程文件
                    saveM3u8File(nfpath, line, linename);
                    //进行递归获取数据
                    ret.addM3u8(parseIndex(nfpath, linename, line));
                } else {//不是使用http协议的 TODO
                    String nurl = basepath + line;
                    //传入新的文件夹地址
                    String nfpath = folderpath;
                    if (line.lastIndexOf("/") != -1) {
                        nfpath += line.substring(0, line.lastIndexOf("/") + 1);
                    }
                    //获取远程文件
                    saveM3u8File(nfpath, nurl, line);
                    ret.addM3u8(parseIndex(nfpath, line, nurl));
                }
            } else {
                M3U8Ts ts = new M3U8Ts(line, seconds);
                ts.setFile(num + ".ts");
                num++;
                ret.addTs(ts);
                seconds = 0;
            }
        }
        reader.close();

        return ret;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }
}
