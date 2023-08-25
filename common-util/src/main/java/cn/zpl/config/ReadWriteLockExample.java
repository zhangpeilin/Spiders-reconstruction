package cn.zpl.config;

import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ReadWriteLockExample {
    private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private static int counter = 0;

    public static void main(String[] args) {
        // 创建多个读线程和写线程
        for (int i = 0; i < 5; i++) {
            new Thread(new Reader()).start();
            new Thread(new Writer()).start();
        }
    }

    static class Reader implements Runnable {
        @Override
        public void run() {
            lock.readLock().lock();
            try {
                // 读取数据
                System.out.println("Reader " + Thread.currentThread().getId() + " is reading: " + counter);
            } finally {
                lock.readLock().unlock();
            }
        }
    }

    static class Writer implements Runnable {
        @Override
        public void run() {
            lock.writeLock().lock();
            try {
                // 修改数据
                counter++;
                System.out.println("Writer " + Thread.currentThread().getId() + " is writing: " + counter);
            } finally {
                lock.writeLock().unlock();
            }
        }
    }
}