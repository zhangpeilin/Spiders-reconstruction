package cn.zpl.thirdParty;

import cn.zpl.thread.CommonThread;
import cn.zpl.util.CruxIdGenerator;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collection;

@Slf4j
public class ReadImgTypeAndRename {

    /**
     * 本地获取
     */
    public static void main(String[] args) {
        extracted();
    }

    private static void extracted() {
        boolean reName = false;
        String[] extensions = new String[]{};
//        File picture = new File("C:\\Users\\zpl\\Pictures");
        File base = new File("C:\\视频爬虫\\nas2");
        File toSave = new File("C:\\视频爬虫\\nas");
        Collection<File> list = FileUtils.listFiles(base, extensions.length == 0 ? null : extensions, true);
//        File[] files = base.listFiles((FilenameFilter) FileFilterUtils.fileFileFilter());
        for (File file : list) {
            String id = reName ? String.valueOf(CruxIdGenerator.generate()) : file.getName().contains(".") ? file.getName().substring(0, file.getName().indexOf(".")) : file.getName();
//        System.out.println(files.length);
//        BufferedImage sourceImg = ImageIO.read(Files.newInputStream(file.toPath()));
//        String[] propertyNames = sourceImg.getPropertyNames();
//        System.out.println(Arrays.toString(propertyNames));
//        System.out.printf("%.1f%n", picture.length() / 1024.0);// 源图大小
//        System.out.println(sourceImg.getWidth()); // 源图宽度
//        System.out.println(sourceImg.getHeight()); // 源图高度
            Metadata metadata = null;
            try {
                metadata = ImageMetadataReader.readMetadata(file);
            } catch (ImageProcessingException | IOException e) {
                log.debug("格式化失败，文件名：{}", file);
                continue;
            }
//        Metadata metadata = ImageMetadataReader.readMetadata(file);
            String createDate = null;
            String lat = null;
            String lon = null;
            for (Directory directory : metadata.getDirectories()) {
                for (Tag tag : directory.getTags()) {
                    String tagName = tag.getTagName();  //标签名
                    String desc = tag.getDescription(); //标签信息
//                System.out.println(tagName);
//                System.out.println(desc);
                    if (tagName.equalsIgnoreCase("Detected File Type Name")) {
                        if (Arrays.stream(extensions).anyMatch(s -> file.getName().contains(s)) || !file.getName().contains(".") || file.getName().endsWith(".tmp")) {
                            switch (desc) {
                                case "JPEG":
                                    file.renameTo(new File(toSave, id + ".jpg"));
                                    break;
                                case "PNG":
                                    file.renameTo(new File(toSave, id + ".png"));
                                    break;
                                case "GIF":
                                    file.renameTo(new File(toSave, id + ".gif"));
                                    break;
                                case "MP4":
                                    file.renameTo(new File(toSave, id + ".mp4"));
                                default:
                                    file.renameTo(new File(toSave, id + "." + desc));
                            }
                        }

                    }
//                switch (tagName) {
//                    case "Date/Time Original":
//                        createDate = desc.split(" ")[0].replace(":", "-");
//                        break;
//                    case "GPS Latitude":
//                        lat = desc;
//                        break;
//                    case "GPS Longitude":
//                        lon = desc;
//                        break;
//                }
                }
            }
        }
    }
}

class CopyThread extends CommonThread {

    private File file;

    public CopyThread(File file) {
        this.file = file;
    }

    @SneakyThrows
    @Override
    public void domain() {
        BufferedImage sourceImg = ImageIO.read(Files.newInputStream(file.toPath()));
        System.out.printf("%.1f%n", file.length() / 1024.0);// 源图大小
        if (sourceImg.getWidth() < 500 || sourceImg.getHeight() < 500) {
            return;
        }
        FileUtils.copyFileToDirectory(file, new File("C:\\big"));
    }

    public static void main(String[] args) {
        String str = "abcd.mp4";
        System.out.println(str.substring(0, str.indexOf(".")));
    }
}
