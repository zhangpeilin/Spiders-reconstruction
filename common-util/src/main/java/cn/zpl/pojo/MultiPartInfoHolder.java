package cn.zpl.pojo;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
public
class MultiPartInfoHolder {
    ConcurrentHashMap<String, Boolean> completeInfo = new ConcurrentHashMap<>();
    DownloadDTO downloadDTO;

    public MultiPartInfoHolder(DownloadDTO downloadDTO) {
        this.downloadDTO = downloadDTO;
    }

    @SneakyThrows
    public static void main(String[] args) {
        DownloadDTO downloadDTO1 = new DownloadDTO();
        downloadDTO1.setSavePath(new File("s:\\sdfasdf").getPath());
        MultiPartInfoHolder holder = new MultiPartInfoHolder(downloadDTO1);
        holder.addPartInfo(0, 1);
        holder.addPartInfo(0, 2);
        holder.addPartInfo(0, 3);
        holder.addPartInfo(0, 4);
        new Thread(() -> {
            while (true) {
                System.out.println(holder.isComplete());
                try {
                    TimeUnit.MILLISECONDS.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
        for (int i = 1; i < 5; i++) {
            holder.setCompleteInfo(0, i);
            TimeUnit.SECONDS.sleep(1);
        }
    }

    public DownloadDTO getDownloadDTO() {
        return downloadDTO;
    }

    public void setDownloadDTO(DownloadDTO downloadDTO) {
        this.downloadDTO = downloadDTO;
    }

    public void addPartInfo(long startIndex, long endIndex) {
        completeInfo.put(getKey(startIndex, endIndex), false);
    }

    public MultiPartInfoHolder setCompleteInfo(long startIndex, long endIndex) {
        Boolean result = completeInfo.get(getKey(startIndex, endIndex));
        log.debug("{}部分下载完成,startIndex->{}, endIndex->{}", downloadDTO.getSavePath(), startIndex, endIndex);
        if (result == null) {
            log.error(String.format("未找到%3$s部分初始化信息startIndex->%1$s,endIndex->%2$s", startIndex, endIndex, downloadDTO.getSavePath()));
        }
        completeInfo.put(getKey(startIndex, endIndex), true);
        return this;
    }

    public String getKey(long startIndex, long endIndex) {
        return String.format("%1$s-%2$s", startIndex, endIndex);
    }

    public boolean isComplete() {
        return completeInfo.entrySet().stream().allMatch(Map.Entry::getValue);
    }
}
