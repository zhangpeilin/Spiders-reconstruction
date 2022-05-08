package cn.zpl.util;


import cn.zpl.pojo.DownloadDTO;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Objects;
@Slf4j
public class SaveLogForImages {


//    public static void saveLog(File saveDir) {
//        try {
//            File file = new File(saveDir.getParent() + "\\list.txt");
//            if (!file.exists()) {
//                file.getParentFile().mkdirs();
//                file.createNewFile();
//            }
//            FileWriter fw = new FileWriter(file, true);
//            fw.write(saveDir.getName() + "\n");
//            fw.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    public static void saveLog(File saveDir) {
        synchronized (SaveLogForImages.class) {
            log.debug("写入日志SaveLog");
            try {
                File file = new File(saveDir.getParent() + "\\list.txt");
                if (!file.exists()) {
                    if (file.getParentFile().mkdirs()) {
                        log.warn(file.getParentFile().getPath() + "日志路径创建失败，可能有风险");
                    }
                    if (file.createNewFile()) {
                        log.warn(file.getPath() + "日志文件创建失败，可能有风险");
                    }
                }
                OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file, true), "gbk");
                writer.write(saveDir.getName() + "\n");
                writer.close();
            } catch (Exception e) {
                log.error("写入日志失败：\n" + e.getMessage());
            }
        }
    }

    public static void saveLog(DownloadDTO data) {
        synchronized (SaveLogForImages.class) {
            File saveDir = new File(data.getSavePath());
            log.debug("写入日志SaveLog");
            try {
                File file = new File(saveDir.getParent() + "\\list.txt");
                if (!file.exists()) {
                    if (file.getParentFile().mkdirs()) {
                        log.warn(file.getParentFile().getPath() + "日志路径创建失败，可能有风险");
                    }
                    if (file.createNewFile()) {
                        log.warn(file.getPath() + "日志文件创建失败，可能有风险");
                    }
                }
                OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file, true), data.getCharsetName());
                writer.write(saveDir.getName() + "\n");
                writer.close();
                data.setComplete();

            } catch (Exception e) {
                log.error("写入日志失败：\n" + e.getMessage());
            }
        }
    }

    public static void saveLogInFolder(File saveDir, Object context) {
        synchronized (SaveLogForImages.class) {
            try {
                File file = new File(saveDir, "list.txt");
                if (!file.exists()) {
                    file.getParentFile().mkdirs();
                    file.createNewFile();
                }
                FileWriter fw = new FileWriter(file, true);
                fw.write(context + "\n");
                fw.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean checkStatusById(File saveDir, Object context) {
        File list_txt = new File(saveDir + "\\list.txt");
        if (!list_txt.exists()) {
            saveDir.delete();
        }
        try {
            BufferedReader br = new BufferedReader(new FileReader(list_txt));
            String tmp;
            while ((tmp = br.readLine()) != null) {
                if (tmp.equals(saveDir.getName())) {
                    br.close();
                    return true;
                }
            }
            //reader需要关闭，否则有可能删除临时目录是会留存
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isCompelete(DownloadDTO data) {
        if (data.getSavePath() == null || "".equalsIgnoreCase(data.getSavePath())) {
            return false;
        }
        File saveDir = new File(data.getSavePath());
        File list_txt = new File(saveDir.getParent() + "\\list.txt");
        if (!list_txt.exists()) {
            delAndLog(saveDir);
            return false;
        }
        if (data.getFileLength() == 0) {
            return isImgCompelete(data);
        }
        if (saveDir.length() != data.getFileLength()) {
            log.error("删除不匹配片段" + saveDir.getName());
            delAndLog(saveDir);
            return false;
        }
        try {

            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(list_txt), data.getCharsetName()));
            String tmp;
            while ((tmp = br.readLine()) != null) {
                if (tmp.equals(saveDir.getName())) {
                    br.close();
                    return true;
                }
            }
            //reader需要关闭，否则有可能删除临时目录是会留存
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.error("删除未完成分段" + saveDir.getName());
        delAndLog(saveDir);
        return false;
    }

    public static boolean isCompeleteMultiple(@NotNull DownloadDTO data) {
        File saveDir = new File(data.getSavePath());
        File list_txt = new File(saveDir.getParent() + "\\list.txt");
        if (!list_txt.exists()) {
            return false;
        }
        if (saveDir.length() != data.getFileLength()) {
            return false;
        }
        try {
            BufferedReader br = new BufferedReader(new FileReader(list_txt));
            String tmp;
            while ((tmp = br.readLine()) != null) {
                if (tmp.equals(saveDir.getName())) {
                    br.close();
                    return true;
                }
            }
            //reader需要关闭，否则有可能删除临时目录是会留存
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isImgCompelete(String path) {
        DownloadDTO data = new DownloadDTO();
        data.setSavePath(path);
        return isImgCompelete(data);
    }

    public static boolean isImgCompelete(DownloadDTO data) {
        File saveDir = new File(data.getSavePath());
        File list_txt = new File(saveDir.getParent() + "\\list.txt");
        if (!list_txt.exists()) {
            delAndLog(saveDir);
            return false;
        }
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(list_txt), data.getCharsetName()));
            String tmp;
            while ((tmp = br.readLine()) != null) {
                if (tmp.equals(saveDir.getName())) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                Objects.requireNonNull(br).close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        log.error("删除未完成分段" + saveDir);
        saveDir.delete();
        return false;
    }

    public static boolean isChapterCompelete(String chapterPath) {
        File saveDir = new File(chapterPath);
        File list_txt = new File(saveDir.getParent() + "\\list.txt");
        if (!list_txt.exists()) {
            delAndLog(saveDir);
            return false;
        }
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(list_txt), "gbk"));
            String tmp;
            while ((tmp = br.readLine()) != null) {
                if (tmp.equals(saveDir.getName())) {
                    return true;
                }
            }
        } catch (IOException e) {
            log.error("查询日志失败：\n" + e.getMessage());
        } finally {
            try {
                assert br != null;
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private static void delAndLog(@NotNull File saveDir) {
        if (saveDir.delete()) {
            log.debug(saveDir.getPath() + "删除成功");
        } else {
            log.warn(saveDir.getPath() + "删除失败，可能引发错误");
        }
    }
}
