package cn.zpl.util;

import cn.zpl.common.bean.VideoInfo;
import cn.zpl.pojo.DownloadDTO;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

@Slf4j
public class SaveLog {



	public static boolean isCompelete(@NotNull VideoInfo data){
		File saveDir = new File(data.getFileList());
		File list_txt = new File(saveDir.getParent() + "\\list.txt");
		if (!list_txt.exists()) {
			delAndLog(saveDir);
			return false;
		}
		if (data.getLength() == null) {
			return isImgCompelete(data);
		}
		if (saveDir.length() != Long.parseLong(data.getLength())) {
			log.debug("删除不匹配片段" + saveDir.getName());
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
			//reader需要关闭，否则有可能删除临时目录是会留存
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			try {
				assert br != null;
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		log.debug("删除不匹配片段" + saveDir.getName());
		delAndLog(saveDir);
		return false;
	}
	public static boolean isCompeleteMultiple(DownloadDTO data) {
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

	public static boolean isImgCompelete(VideoInfo data){
		File saveDir = new File(data.getFileList());
		File list_txt = new File(saveDir.getParent() + "\\list.txt");
		//日志文件list.txt不存在，可能是没有完成下载，所以直接删除data指向的文件
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
		}finally {
			try {
				assert br != null;
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println("删除未完成分段" + saveDir.getName());
		delAndLog(saveDir);
		return false;
	}

	private static void delAndLog(@NotNull File saveDir) {
		if (saveDir.delete()) {
			log.debug(saveDir.getPath() + "删除成功");
		} else {
			log.warn(saveDir.getPath() + "删除失败，可能引发错误");
		}
	}

	public static boolean isChapterCompelete(String chapterPath){
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
		}
		finally {
			try {
				assert br != null;
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	public static void saveLog(String path){
		if (path != null && !"".equals(path)) {
			saveLog(new File(path));
		}
	}

	public static void saveLog(File saveDir){
		synchronized (SaveLog.class) {
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
}