import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;

public class FileScanner {
    public static void main(String[] args) throws IOException {
        String directoryPath = "D:\\OneDrive\\文档\\Tencent Files\\512239520\\Image\\Group2";
        LocalDate targetDate = LocalDate.of(2023, 8, 15); // 设置目标日期

        Path directory = Paths.get(directoryPath);
        Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                LocalDate creationDate = attrs.creationTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                if (creationDate.isEqual(targetDate)) {
                    System.out.println(file);
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }
}