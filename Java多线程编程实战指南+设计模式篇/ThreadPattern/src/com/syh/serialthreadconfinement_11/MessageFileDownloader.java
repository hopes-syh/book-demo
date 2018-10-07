package com.syh.serialthreadconfinement_11;

/**
 * Serial Thread Confinement 适用于以下两个典型的场景：
 * <br/>
 * 1.需要使用非线程安全对象，但又不希望引入锁：任务的执行涉及非线程安全对象，如果采用锁去保证对这些对象访问的线程安全，
 * 这些锁的开销比起将任务通过队列中转涉及锁的开销更大的话，那么我们使用Serial Thread Confinement。
 * <br/>
 * 2.任务的执行涉及I/O操作，但我们不希望过多的I/O线程增加上下文切换。（个人觉得如果是自己实现的I/O处理，可以直接使用AIO，
 * 不需要锁，也可以避免线程的上下文切换）
 */

import sun.net.ftp.FtpClient;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.concurrent.*;

/**
 * 实现 FTP 文件下载
 * 模式角色：SerialThreadConfinement.Serializer
 */
public class MessageFileDownloader {

    // 模式角色：SerialThreadConfinement.WorkerThread
    private final WorkerThread workerThread;

    public MessageFileDownloader(String outputDir, final String ftpServer, final String userName, final String password) {
        this.workerThread = new WorkerThread(outputDir, ftpServer, userName, password);
    }

    public void init() {
        workerThread.start();
    }

    public void shutdown(){
        workerThread.terminate();
    }

    public void downloadFile(String file){
        workerThread.download(file);
    }

    // 模式角色：SerialThreadConfinement.WorkerThread
    private static class WorkerThread extends AbstractTerminatableThread {

        // 模式角色：SerialThreadConfinement.Queue
        private final BlockingQueue<String> workQueue;

        private final Future<FtpClient> ftpClientPromise;
        private final String outputDir;

        private WorkerThread(String outputDir, final String ftpServer, final String userName, final String password) {
            this.workQueue = new ArrayBlockingQueue<String>(100);
            this.outputDir = outputDir + "/";

            this.ftpClientPromise = new FutureTask<FtpClient>(
                    new Callable<FtpClient>() {
                        @Override
                        public FtpClient call() throws Exception {
                            FtpClient ftpClient = initFTPClient(ftpServer, userName, password);
                            return ftpClient;
                        }
                    });

            new Thread((Runnable) ftpClientPromise).start();
        }

        public void download(String file){
            try {
                workQueue.put(file);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        private FtpClient initFTPClient(String ftpServer, String userName, String password){
            return null;
        }

        @Override
        protected void doRun() throws Exception {
            String file = workQueue.take();

            OutputStream os = null;
            try{
                os = new BufferedOutputStream(new FileOutputStream(outputDir + file));
                ftpClientPromise.get().getFile(file, os);
            } finally {
                if(null != os){
                    try{
                        os.close();
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }

        @Override
        protected void doCleanUp() {
            //To change body of implemented methods use File | Settings | File Templates.
        }
    }
}
