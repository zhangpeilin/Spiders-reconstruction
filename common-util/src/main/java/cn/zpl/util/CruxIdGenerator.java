package cn.zpl.util;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class CruxIdGenerator {
    private static AtomicInteger seq = new AtomicInteger(0);

    private static long workId = new Random(System.nanoTime()).nextInt(2048);

    public static long generate() {
        return System.currentTimeMillis() << 21 | workId << 10 | seq.getAndUpdate(operand -> ++operand % 1024);
    }


}