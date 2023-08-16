import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;

public class FileScanner1 {
    public static void main(String[] args) throws IOException {
        String sourceDirectoryPath = "D:\\OneDrive\\文档\\Tencent Files\\512239520\\Image\\Group2";
        String destinationDirectoryPath = "D:\\测试qq图片";
        LocalDate targetDate = LocalDate.of(2023, 8, 15); // 设置目标日期

        Path sourceDirectory = Paths.get(sourceDirectoryPath);
        Path destinationDirectory = Paths.get(destinationDirectoryPath);

        Files.walkFileTree(sourceDirectory, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                LocalDate creationDate = attrs.creationTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                if (creationDate.isEqual(targetDate)) {
                    Path destinationFile = destinationDirectory.resolve(file.getFileName());
                    Files.copy(file, destinationFile, StandardCopyOption.REPLACE_EXISTING);
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }
}