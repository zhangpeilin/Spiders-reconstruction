package cn.zpl;

import cn.zpl.tencent.thread.PAThread;

import java.io.File;

public class TestOneFile {
    public static void main(String[] args) {
        PAThread paThread = new PAThread(new File("C:\\Users\\zhang\\OneDrive\\图片\\83BD2570-B4C0-4A4E-A150-277C0E07570E.bmp"));
        paThread.run();
    }
}
