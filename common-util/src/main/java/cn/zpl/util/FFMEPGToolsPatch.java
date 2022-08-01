package cn.zpl.util;

import cn.zpl.common.bean.VideoInfo;
import cn.zpl.config.CommonParams;
import cn.zpl.pojo.VideoData;
import it.sauronsoftware.jave.Encoder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


@Slf4j
@Component
@EnableConfigurationProperties(CommonProperties.class)
public class FFMEPGToolsPatch {



    public static String ffmpegPath;
    /**
     * 判断是否检查已存在，false时表示不检查，已存在时也会被覆盖；true表示检查，存在时会跳过
     */
    public static boolean checkExist = true;

    public static boolean check = true;


    @Bean
    public CommonProperties fillNumber (CommonProperties properties) {
        ffmpegPath = properties.getFfmpeg();
        return properties;
    }

    public static boolean isExists(@NotNull VideoData video) {
        if (!checkExist) {
            return false;
        }
        if (new File(video.getDesSavePath()).exists()) {
            //不在判断长度，因为这个移动到这里的一定是经过判断的，FFMEPG参数中有特殊字符的时候会报错
            System.out.println("该视频已经移动到指定位置");
            deleteTS(video.GetTmpSaveDirectory());
            return true;
        }
//		if (video.getTmpSavePath().exists()) {
//			//不在判断长度，因为这个移动到这里的一定是经过判断的，FFMEPG参数中有特殊字符的时候会报错
//			System.out.println("该目录已经合并，现在进行移动");
//			//移动文件到目标文件夹内
//			if (video.getTmpSavePath().renameTo(new File(video.getDesSavePath()))) {
//				System.out.println("删除缓存");
//				deleteTS(video.GetTmpSaveDirectory());
//				return true;
//			}
//			return false;
//		}
        return false;
    }

    /**
     * 用来合并1p多段的flv视频
     *
     * @param videoData 视频信息
     * @return 返回合并结果
     */
    public static boolean mergeBilibiliVideo(@NotNull VideoData videoData) {

        File desFile = new File(videoData.getDesSavePath());
        File tmp_des_file = new File(videoData.GetTmpSaveDirectory(), videoData.getDesSaveName());
        videoData.setTmpSavePath(tmp_des_file);
        List<String> file_list = videoData.getPartList();
        if (!desFile.getParentFile().exists()) {
            if (!desFile.getParentFile().mkdirs()) {
                log.error("创建目录失败");
            }
        }
        //判断保存合并文件的磁盘空间是否大于缓存的分段占用空间
        if (FileUtils.sizeOfDirectory(new File(file_list.get(0)).getParentFile()) > new File(desFile.getParent()).getFreeSpace()) {
            System.out.println("磁盘空间不足，无法合并，停止执行");
            System.exit(0);
        }
        if (file_list.size() == 1) {
            //如果只有一个，直接拷贝
            return moveFile(file_list.get(0), videoData.getDesSavePath(), videoData);
        }
        if (isExists(videoData)) {
            //不在判断长度，因为这个移动到这里的一定是经过判断的，FFMEPG参数中有特殊字符的时候会报错
            return true;
        }
        file_list.sort(new BilibiliComparator(true));

        //创建filelist.txt
        String filelist =
                new File(file_list.get(0)).getParentFile() + "\\" + CommonIOUtils.filterFileName(videoData.getDesSaveName()) + ".txt";
        File list = new File(filelist);
        if (list.exists()) {
            try {
                FileUtils.forceDelete(list);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            list.createNewFile();
            FileWriter fw = new FileWriter(list);
            for (String string : file_list) {
                fw.write("file '" + new File(string).getPath() + "'\n");
            }
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<String> command = new ArrayList<>();
        String FFMEPGPATH = "ffmpeg";
        command.add(FFMEPGPATH);
        command.add("-f");
        command.add("concat");
        command.add("-safe");
        command.add("0");
        command.add("-y");
        command.add("-i");
        command.add(list.getPath());
        command.add("-c");
        command.add("copy");
        //合并后的文件存放在temp文件夹内，然后移动到目标文件夹
        command.add(videoData.getWebSite().equals("egame") ? videoData.getDesSavePath() : tmp_des_file.getPath());
        //可以去除最后有没有分隔符的干扰，getpath返回的字符串不带最后的斜杠
        if (process(command, "")) {
            if (checkMP4(videoData, videoData.getWebSite().equals("egame") ? videoData.getDesSavePath() : tmp_des_file.getPath())) {
                videoData.setLength((new File(videoData.getWebSite().equals("egame") ? videoData.getDesSavePath() : tmp_des_file.getPath())).length());
                //移动文件到目标文件夹内
                if (videoData.getWebSite().equals("egame")) {
                    deleteTS(videoData.GetTmpSaveDirectory());
                    return true;
                }
                return moveFile(tmp_des_file.getPath(), videoData.getDesSavePath(), videoData);
            }
            return false;
        } else {
            System.out.println("合并失败");
            System.exit(0);
            return false;
        }
    }

    /**
     * 此方法用来合并m4s文件的，没有1p多段的情况
     *
     * @param videoData 视频信息
     * @return 返回真假
     */
    public static boolean mergeBilibiliVideo2(@NotNull VideoData videoData) {

        File desFile = new File(videoData.getDesSavePath());
        File tmp_des_file = new File(videoData.GetTmpSaveDirectory(), videoData.getDesSaveName());
        if (!desFile.getParentFile().exists()) {
            if (!desFile.getParentFile().mkdirs()) {
                log.error(desFile + "创建目录失败");
            }
        }
        //判断保存合并文件的磁盘空间是否大于缓存的分段占用空间
        if (FileUtils.sizeOfDirectory(tmp_des_file.getParentFile()) > new File(desFile.getParent()).getFreeSpace()) {
            System.out.println("磁盘空间不足，无法合并，停止执行");
            System.exit(0);
        }
        if (isExists(videoData)) {
            //不在判断长度，因为这个移动到这里的一定是经过判断的，FFMEPG参数中有特殊字符的时候会报错
            return true;
        }
        List<String> command = new ArrayList<String>();
        command.add(ffmpegPath);
        command.add("-i");
        command.add("\"" + videoData.getVideo().getSavePath() + "\"");
        command.add("-i");
        command.add("\"" + videoData.getAudio().getSavePath() + "\"");
        command.add("-codec");
        command.add("copy");
        command.add("-y");
        command.add("\"" + desFile.getPath() + "\"");

        //可以去除最后有没有分隔符的干扰，getpath返回的字符串不带最后的斜杠
        if (process(command, "")) {
            if (checkMP4(videoData, videoData.getDesSavePath())) {
                videoData.setLength((new File(videoData.getDesSavePath())).length());
                try {
                    FileUtils.deleteDirectory(videoData.GetTmpSaveDirectory());
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
                return true;
            }
            return false;
        } else {
            System.exit(0);
            return false;
        }
    }

    private static boolean moveFile(String tmp_des_file, String des_file, VideoData videoData) {
        try {
            FileUtils.copyFile(new File(tmp_des_file), new File(des_file));
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (new File(des_file).exists() && new File(tmp_des_file).length() == new File(des_file).length()) {
            try {
                FileUtils.deleteDirectory(videoData.GetTmpSaveDirectory());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }
        log.error("移动文件失败" + des_file);
        return false;
    }

    /**
     * @param @param parent_path
     * @param @param list
     * @return void
     * @throws
     * @Title: deleteTS
     * @Description: TODO
     */
    private static void deleteTS(@NotNull File ts_dir) {
        if (!ts_dir.exists()) {
            return;
        }
        File[] detail = ts_dir.listFiles();
        if (detail == null) {
            return;
        }
        for (File file : detail) {
            file.delete();
        }
        ts_dir.delete();
    }

    /**
     * @param @param video
     * @param @param parent_path
     * @return void
     * @throws
     * @Title: checkMP4
     * @Description: TODO
     */
    private static boolean checkMP4(VideoData video, String des_file) {
        Encoder encoder = new Encoder();
        File desFile = new File(des_file);
        try {
            if (!desFile.exists()) {
                return false;
            }
            long total = 0;
            try {
                //如果报错，那么重命名成时间戳的格式然后再获取文件信息，获取后将文件名再改回去
                total = encoder.getInfo(desFile).getDuration();
            } catch (Exception e) {
                long timestamp = System.currentTimeMillis();
                File tmp = new File(CommonIOUtils.getDrivePath(desFile.getParent()), String.valueOf(timestamp) + desFile.getName().substring(desFile.getName().lastIndexOf(".")));
                if (desFile.renameTo(tmp)) {
                    total = encoder.getInfo(tmp).getDuration();
                    while (!tmp.renameTo(desFile)) {
                        log.error("临时改名出错，请核对");
                        CommonIOUtils.waitSeconds(5);
                    }
                }

            }
            //理论时长-实际文件时长不大于1000毫秒即认为符合要求
            if (new BigDecimal(video.getTimeLength()).subtract(new BigDecimal(total))
                    .compareTo(new BigDecimal(1000)) > 0) {
                if (new BigDecimal(video.getTimeLength()).subtract(new BigDecimal(total))
                        .compareTo(new BigDecimal(1000)) < 0) {
                    video.setHold1("差值为：" + new BigDecimal(video.getTimeLength()).subtract(new BigDecimal(total)));
                    return true;
                }
                System.out.println(total);
                System.out.println(new BigDecimal(video.getLength()));
                System.out.println("合并失败，生成文件时长小于下载列表中的记录");
                return false;
            } else {
                video.setHold1("实际视频时长比理论值多出：" + new BigDecimal(total).subtract(new BigDecimal(video.getTimeLength())));
                System.out.println(video.getHold1());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean process(List<String> command, String workDirectory) {
        try {
            if (null == command || command.size() == 0) {
                return false;
            }
            for (Object object : command) {
                System.out.print(object + " ");
            }
            ProcessBuilder builder = new ProcessBuilder(command).redirectErrorStream(true);
            if (workDirectory != null && !workDirectory.equals("")) {
                builder.directory(new File(workDirectory));
            }
            Process videoProcess = builder.start();
            new PrintWithStream(videoProcess.getErrorStream()).start();
            new PrintWithStream(videoProcess.getInputStream()).start();
            int exitCode = videoProcess.waitFor();
            if (exitCode == 1) {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean mergeXDFTs(VideoInfo video) {
        List<String> command = new ArrayList<>();
        command.add("E:\\ffmpeg-master-latest-win64-gpl-shared\\bin\\ffmpeg");
        command.add("-allowed_extensions");
        command.add("ALL");
        command.add("-y");
        command.add("-i");
        command.add(video.getSavedLocalName());
        command.add("-c");
        command.add("copy");
        //合并后的文件存放在temp文件夹内，然后移动到目标文件夹
        command.add(video.getSavedLocalName().replace(".m3u8", ".ts"));
        if (process(command, "")) {
            if (!check) {
                return true;
            }
            if (checkMP4(video, command.get(command.size() - 1))) {
                video.setSavedLocalName(command.get(command.size() - 1));
                video.setLength(String.valueOf((new File(command.get(command.size() - 1))).length()));
                //移动文件到目标文件夹内
//				Arrays.stream(video.getFileList().split("\\|")).forEach(s -> deleteTS(s));
                return true;
            }
        } else {
            //第一次合并失败，尝试重新合并
//			RetryCombine(list.getParentFile());
            System.out.println("合并失败");
//			System.exit(0);
        }
        return false;
    }

    /**
     * @param @param video
     * @param @param parent_path
     * @return void
     * @throws
     * @Title: checkMP4
     * @Description: TODO
     */
    public static boolean checkMP4(VideoInfo video, String des_file) {
        Encoder encoder = new Encoder();
        try {
            if (!new File(des_file).exists()) {
                return false;
            }
            long total = encoder.getInfo(new File(des_file)).getDuration();
            //理论时长-实际文件时长不大于1000毫秒即认为符合要求
            if (new BigDecimal(video.getTimeLength()).subtract(new BigDecimal(total))
                    .compareTo(new BigDecimal(1000)) == 1) {
//				if (new BigDecimal(video.getTimeLength()).subtract(new BigDecimal(total))
//					.compareTo(new BigDecimal(1000)) == -1) {
//					video.setHold1("差值为：" + new BigDecimal(video.getTimeLength()).subtract(new BigDecimal(total)));
//					return true;
//				}
                System.out.println(total);
                System.out.println(new BigDecimal(video.getLength()));
                System.out.println("合并失败，生成文件时长小于下载列表中的记录");
                return false;
            } else {
                video.setHold1("实际视频时长比理论值多出：" + new BigDecimal(total).subtract(new BigDecimal(video.getTimeLength())));
                System.out.println(video.getHold1());
                video.setTimeLength(String.valueOf(total));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean mergeXDFMp4(VideoInfo video) {

        if (checkMP4(video, video.getLocalPath())) {
            System.out.println("视频已存在跳过合并");
            return true;
        }
        List<String> file_list = new ArrayList<>();
        file_list.addAll(Arrays.asList(video.getFileList().split("\\|")));

        Collections.sort(file_list, new XDFComparator(true));
        //创建fileList.txt
        File list = new File(new File(video.getSavedLocalName()).getParent(), "list.txt");
        if (list.exists()) {
            list.delete();
        }
        try {
            list.createNewFile();
            FileWriter fw = new FileWriter(list);
            for (String string : file_list) {
                fw.write("file '" + new File(string).getPath() + "'\n");
            }
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


        List<String> command = new ArrayList<>();
        command.add("ffmpeg");
        command.add("-f");
        command.add("concat");
        command.add("-safe");
        command.add("0");
        command.add("-y");
        command.add("-i");
        command.add(list.getPath());
        command.add("-c");
        command.add("copy");
        //判断目标文件夹是否存在，不存在需要创建目录
        File dest = new File(video.getLocalPath()).getParentFile();
        if (!dest.exists()) {
            if (!dest.mkdirs()) {
                System.out.println("创建目标文件夹失败！请检查：" + video.getLocalPath());
                System.exit(0);
            }
        }
        command.add(video.getLocalPath());
        if (process(command, "")) {
            if (checkMP4(video, command.get(command.size() - 1))) {
                video.setSavedLocalName(command.get(command.size() - 1));
                video.setLength(String.valueOf((new File(command.get(command.size() - 1))).length()));
                //移动文件到目标文件夹内
//				Arrays.stream(video.getFileList().split("\\|")).forEach(s -> deleteTS(s));
                return true;
            }
            return false;
        } else {
            //第一次合并失败，尝试重新合并
//			RetryCombine(list.getParentFile());
            System.out.println("合并失败");
//			System.exit(0);
            return false;
        }
    }
}
