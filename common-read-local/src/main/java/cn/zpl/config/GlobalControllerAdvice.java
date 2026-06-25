package cn.zpl.config;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    private static final String DEFAULT_SCAN_PATH;

    static {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("mac")) {
            DEFAULT_SCAN_PATH = "/Volumes/G/ehentai/archive";
        } else {
            DEFAULT_SCAN_PATH = "G:\\ehentai\\archive1111";
        }
    }

    @ModelAttribute("defaultScanPath")
    public String addDefaultScanPath(Model model) {
        return DEFAULT_SCAN_PATH;
    }
}
