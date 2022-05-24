package cn.zpl.thirdParty;

import cn.zpl.thread.CommonThread;
import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Files;

public class ReadImgTypeAndRename {

    /**
     * 本地获取
     */
    @SneakyThrows
    public static void main(String[] args) {
//        File picture = new File("C:\\Users\\zpl\\Pictures");
        File base = new File("C:\\Users\\zpl\\OneDrive\\QQ收藏中的图片");
        File[] files = base.listFiles((FilenameFilter) FileFilterUtils.fileFileFilter());
        for (File file : files) {

//        System.out.println(files.length);
//        BufferedImage sourceImg = ImageIO.read(Files.newInputStream(file.toPath()));
//        String[] propertyNames = sourceImg.getPropertyNames();
//        System.out.println(Arrays.toString(propertyNames));
//        System.out.printf("%.1f%n", picture.length() / 1024.0);// 源图大小
//        System.out.println(sourceImg.getWidth()); // 源图宽度
//        System.out.println(sourceImg.getHeight()); // 源图高度
        Metadata metadata = ImageMetadataReader.readMetadata(file);
//        System.out.println(metadata);


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
                    System.out.println(desc);
                    if (!file.getName().contains(".")) {
                        switch (desc) {
                            case "JPEG":
                                file.renameTo(new File(file.getParent(), file.getName() + ".jpg"));
                                break;
                            case "PNG":
                                file.renameTo(new File(file.getParent(), file.getName() + ".png"));
                                break;
                            case "GIF":
                                file.renameTo(new File(file.getParent(), file.getName() + ".gif"));
                                break;
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
}
