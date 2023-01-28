package cn.zpl.spider.on.ehentai;

import cn.zpl.spider.on.ehentai.thread.DownLoadArchiveThread;
import cn.zpl.util.DownloadTools;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class TestDownloadOne {

    @Test
    public void doBusiness(){

        String url =

                        "VM37:3 https://exhentai.org/g/1817355/dc4811ef10/\n" +
                        "VM37:3 https://exhentai.org/g/1319705/4878594d11/\n" +
                        "VM37:3 https://exhentai.org/g/1161387/d30f9fe66f/\n" +
                        "VM37:3 https://exhentai.org/g/1088577/fd67032616/\n" +
                        "VM37:3 https://exhentai.org/g/980671/dbded7a27c/\n" +
                        "VM37:3 https://exhentai.org/g/804144/41088159f9/\n" +
                        "VM37:3 https://exhentai.org/g/733825/cd403f3d14/\n" +
                        "VM37:3 https://exhentai.org/g/615820/a53271fc2c/\n" +
                        "VM37:3 https://exhentai.org/g/611366/1d8a1987a3/\n" +
                        "VM37:3 https://exhentai.org/g/603304/cac9f74555/\n" +
                        "VM37:3 https://exhentai.org/g/602562/f470c1c019/\n" +
                        "VM37:3 https://exhentai.org/g/493931/e041425855/\n" +
                        "VM37:3 https://exhentai.org/g/39398/7a0c12631a/\n" +
                        "VM37:3 https://exhentai.org/g/286162/a5d2e5527d/\n" +
                        "VM37:3 https://exhentai.org/g/622202/17494c705c/\n" +
                        "VM37:3 https://exhentai.org/g/634524/512c5a8f44/\n" +
                        "VM37:3 https://exhentai.org/g/634608/8bae0f7a9d/\n" +
                        "VM37:3 https://exhentai.org/g/2448659/90a67a5ebf/\n" +
                        "VM37:3 https://exhentai.org/g/2448657/c7109a4388/\n" +
                        "VM37:3 https://exhentai.org/g/2448656/6b323334dc/\n" +
                        "VM37:3 https://exhentai.org/g/2448634/4d2852785b/\n" +
                        "VM37:3 https://exhentai.org/g/2448592/8372f24eeb/\n" +
                        "VM37:3 https://exhentai.org/g/2448580/8f29a61a8d/\n" +
                        "VM37:3 https://exhentai.org/g/2448506/4fbbbc2ece/\n" +
                        "VM37:3 https://exhentai.org/g/2448324/892aab6636/\n" +
                        "VM37:3 https://exhentai.org/g/2447921/5930ba22ed/\n" +
                        "VM37:3 https://exhentai.org/g/2447843/b1c8e809fe/\n" +
                        "VM37:3 https://exhentai.org/g/2447717/b6daee58b4/\n" +
                        "VM37:3 https://exhentai.org/g/2447667/977e59850f/\n" +
                        "VM37:3 https://exhentai.org/g/2446899/c68cc81084/\n" +
                        "VM37:3 https://exhentai.org/g/2273228/4a4cc3ed61/\n" +
                        "VM37:3 https://exhentai.org/g/2432935/6ba3dbcd9b/\n" +
                        "VM37:3 https://exhentai.org/g/2295443/b517e5d2b7/\n" +
                        "VM37:3 https://exhentai.org/g/2445168/df2076e277/\n" +
                        "VM37:3 https://exhentai.org/g/2436037/30fee7f2aa/\n" +
                        "VM37:3 https://exhentai.org/g/536954/2f61021446/";
        url = url.replaceAll("VM.+ ", "");
        String[] urls = url.split("\n");
        DownloadTools tools = DownloadTools.getInstance(3);
        for (String s : urls) {
            tools.ThreadExecutorAdd(new DownLoadArchiveThread(s));
        }
        tools.shutdown();
    }
}
