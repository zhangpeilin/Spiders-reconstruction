package cn.zpl.spider.on.ehentai;

import cn.zpl.spider.on.ehentai.thread.DownLoadArchiveThread;
import cn.zpl.util.DownloadTools;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class TestDownloadBatch {

    @Test
    public void doBusiness(){

        String url =
                                "https://e-hentai.org/g/2442540/07bc0f7c29/\n" +
                                        "VM339:3 https://e-hentai.org/g/2420062/15239b872c/\n" +
                                        "VM339:3 https://e-hentai.org/g/2416993/37555edf36/\n" +
                                        "VM339:3 https://e-hentai.org/g/2416928/85c243e461/\n" +
                                        "VM339:3 https://e-hentai.org/g/2394654/0a6dadddb5/\n" +
                                        "VM339:3 https://e-hentai.org/g/2392469/4387730b48/\n" +
                                        "VM339:3 https://e-hentai.org/g/2392463/2faa59a3c6/\n" +
                                        "VM339:3 https://e-hentai.org/g/2392458/e1698da318/\n" +
                                        "VM339:3 https://e-hentai.org/g/2386406/fc11d41f85/\n" +
                                        "VM339:3 https://e-hentai.org/g/2386404/014985c4d5/\n" +
                                        "VM339:3 https://e-hentai.org/g/2320887/679a7b1c30/\n" +
                                        "VM339:3 https://e-hentai.org/g/2290850/44d5d5a21a/\n" +
                                        "VM339:3 https://e-hentai.org/g/2246615/667eec6836/\n" +
                                        "VM339:3 https://e-hentai.org/g/2217054/37c2d97f44/\n" +
                                        "VM339:3 https://e-hentai.org/g/2217045/ddc4e20e8e/\n" +
                                        "VM339:3 https://e-hentai.org/g/2217037/9016e9809c/\n" +
                                        "VM339:3 https://e-hentai.org/g/2186076/6060d88d70/\n" +
                                        "VM339:3 https://e-hentai.org/g/2174166/95d0e7ef2f/\n" +
                                        "VM339:3 https://e-hentai.org/g/1967038/b32a038fbc/\n" +
                                        "VM339:3 https://e-hentai.org/g/1953149/33c9726044/\n" +
                                        "VM339:3 https://e-hentai.org/g/1888733/4f27a991c2/\n" +
                                        "VM339:3 https://e-hentai.org/g/1835588/d8bab89250/\n" +
                                        "VM339:3 https://e-hentai.org/g/1829680/02ae363c3f/\n" +
                                        "VM339:3 https://e-hentai.org/g/1829647/0e11bf3d1c/\n" +
                                        "VM339:3 https://e-hentai.org/g/1807859/1d076f0a39/\n"
                + "https://e-hentai.org/g/1752268/b324c8429a/\n" +
                                        "VM416:3 https://e-hentai.org/g/1420530/c2dd271330/\n" +
                                        "VM416:3 https://e-hentai.org/g/1410822/eec15d4294/\n" +
                                        "VM416:3 https://e-hentai.org/g/1397483/e6c8d8487e/\n" +
                                        "VM416:3 https://e-hentai.org/g/1397462/5239fb8538/\n" +
                                        "VM416:3 https://e-hentai.org/g/1397164/b3ddd333a1/\n" +
                                        "VM416:3 https://e-hentai.org/g/1308067/913a2a74fb/\n" +
                                        "VM416:3 https://e-hentai.org/g/1297889/d3850c70af/";
        url = url.replaceAll("VM.+ ", "");
        String[] urls = url.split("\n");
        DownloadTools tools = DownloadTools.getInstance(3);
        for (String s : urls) {
            tools.ThreadExecutorAdd(new DownLoadArchiveThread(s));
        }
        tools.shutdown();
    }
}
