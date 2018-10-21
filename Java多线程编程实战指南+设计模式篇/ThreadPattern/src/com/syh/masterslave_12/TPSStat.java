package com.syh.masterslave_12;

import java.io.*;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 18-10-14
 * Time: 下午5:10
 * To change this template use File | Settings | File Templates.
 */
public class TPSStat {


    public static void main(String[] args) throws Exception {
        // 接口日志文件所在目录
        String logBaseDir = args[0];

        // 忽略的操作名列表
        String excludedOperationNames = "";

        // 指定要统计在内的操作名列表
        String includedOperationNames = "*";

        // 指定要统计在内的目标设备名
        String destinationSysName = "*";

        int argc = args.length;
        if(argc > 2)
            excludedOperationNames = args[1];
        if(argc > 3)
            includedOperationNames = args[2];
        if(argc > 4)
            destinationSysName = args[3];

        Master processor = new Master(logBaseDir, excludedOperationNames, includedOperationNames, destinationSysName);

        BufferedReader fileNamesReader = new BufferedReader(new InputStreamReader(System.in));

        ConcurrentMap<String, AtomicInteger> result = processor.calculate(fileNamesReader);

        for (String timeRange : result.keySet()){
            System.out.println(timeRange +","+ result.get(timeRange));
        }
    }

    private static class Master {
        private final String logBaseDir;
        private final String excludedOperationNames;
        private final String includedOperationNames;
        private final String destinationSysName;

        // 每次派发给某个 Slave 线程的文件数
        private static final int NUMBER_OF_FILES_FOR_EACH_DISPATCH = 5;
        private static final int WORKER_COUNT = Runtime.getRuntime().availableProcessors();

        public Master(String logBaseDir, String excludedOperationNames, String includedOperationNames, String destinationSysName) {
            this.logBaseDir = logBaseDir;
            this.excludedOperationNames = excludedOperationNames;

            this.includedOperationNames = includedOperationNames;
            this.destinationSysName = destinationSysName;
        }

        public ConcurrentMap<String, AtomicInteger> calculate(BufferedReader fileNamesReader) throws IOException {
            ConcurrentMap<String, AtomicInteger> respository = new ConcurrentSkipListMap<String, AtomicInteger>();

            // 创建工作者线程
            Worker[] workers = createAndStartWorkers(respository);

            // 指派任务给工作者线程
            dispatchTask(fileNamesReader, workers);

            // 等待工作者线程处理结束
            for(int i=0; i<WORKER_COUNT; i++){
                workers[i].interrupt();
            }

            return respository;
        }

        private Worker[] createAndStartWorkers(ConcurrentMap<String, AtomicInteger> respository) {
            Worker[] workers = new Worker[WORKER_COUNT];
            Worker worker;
            Thread.UncaughtExceptionHandler eh = new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread t, Throwable e) {
                    e.printStackTrace();
                }
            };

            for(int i=0; i<WORKER_COUNT; i++){
                worker = new Worker(respository, excludedOperationNames, includedOperationNames, destinationSysName);
                workers[i] = worker;
                worker.setUncaughtExceptionHandler(eh);
                worker.start();
            }

            return workers;
        }

        private void dispatchTask(BufferedReader fileNamesReader, Worker[] workers) throws IOException {
            String line;
            Set<String> fileNames = new HashSet<String>();

            int fileCount = 0;
            int workerIndex = -1;
            BufferedReader logFileReader;
            while ((line = fileNamesReader.readLine()) != null){
                fileNames.add(line);
                fileCount++;

                if(0 == (fileCount % NUMBER_OF_FILES_FOR_EACH_DISPATCH)){
                    // 工作者线程间的负载均衡：采用简单的轮询选择worker
                    workerIndex = (workerIndex + 1) % WORKER_COUNT;
                    logFileReader = makeReaderFrom(fileNames);

                    System.out.println(String.format("Dispatch %s files to worker: %s", NUMBER_OF_FILES_FOR_EACH_DISPATCH,
                            workerIndex));
                    workers[workerIndex].submitWorkload(logFileReader);

                    fileNames = new HashSet<String>();
                    fileCount = 0;
                }
            }

            if(fileCount > 0){
                logFileReader = makeReaderFrom(fileNames);
                workerIndex = (workerIndex + 1) % WORKER_COUNT;

                System.out.println(String.format("Dispatch %s files to worker: %s", NUMBER_OF_FILES_FOR_EACH_DISPATCH,
                        workerIndex));
                workers[workerIndex].submitWorkload(logFileReader);
            }
        }

        private BufferedReader makeReaderFrom(final Set<String> fileNames) {
            BufferedReader logFileReader;

            SequenceInputStream in = new SequenceInputStream(new Enumeration<InputStream>() {
                private Iterator<String> iterator = fileNames.iterator();

                @Override
                public boolean hasMoreElements() {
                    return iterator.hasNext();
                }

                @Override
                public InputStream nextElement() {
                    String fileName = iterator.next();
                    InputStream in = null;
                    try{
                        in = new FileInputStream(logBaseDir + fileName);
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                    return in;
                }
            });

            logFileReader = new BufferedReader(new InputStreamReader(in));
            return logFileReader;
        }
    }

    private static class Worker extends Thread {
        private static final Pattern SPLIT_PATTERN = Pattern.compile("\\|");
        private final ConcurrentMap<String, AtomicInteger> respository;
        private final BlockingQueue<BufferedReader> workQueue;

        private final String selfDevice = "ESB";
        private final String excludedOperationNames;
        private final String includedOperationNames;
        private final String destinationSysName;

        private Worker(ConcurrentMap<String, AtomicInteger> respository, String excludedOperationNames,
                       String includedOperationNames, String destinationSysName) {
            this.respository = respository;
            this.workQueue = new ArrayBlockingQueue<BufferedReader>(100);
            this.excludedOperationNames = excludedOperationNames;
            this.includedOperationNames = includedOperationNames;
            this.destinationSysName = destinationSysName;
        }

        public void submitWorkload(BufferedReader taskWorkload){
            try {
                workQueue.put(taskWorkload);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        protected void doRun() throws Exception {
            BufferedReader logFileReader = workQueue.take();

            String interfaceLogRecord;
            String[] recordParts;
            String timeStamp;
            AtomicInteger reqCounter;
            AtomicInteger existingReqCounter;
            int i=0;

            while ((interfaceLogRecord = logFileReader.readLine()) != null){
                recordParts = SPLIT_PATTERN.split(interfaceLogRecord, 0);

                // 避免CPU占用过高
                if(0 == ((++i) % 100000)){
                    Thread.sleep(80);
                    i = 0;
                }

                // 跳过无效记录
                if(recordParts.length < 7){
                    continue;
                }

                // 只考虑发请求给selfDevice所指定的系统记录
                if(("request").equals(recordParts[2]) && (recordParts[6].startsWith(selfDevice))){
                    timeStamp = recordParts[0];
                    timeStamp = new String(timeStamp.substring(0, 19).toCharArray());

                    String operName = recordParts[4];
                    reqCounter = respository.get(timeStamp);
                    if(null == reqCounter){
                        reqCounter = new AtomicInteger(0);
                        existingReqCounter = respository.putIfAbsent(timeStamp, reqCounter);
                        if(null != existingReqCounter){
                            reqCounter = existingReqCounter;
                        }
                    }

                    if(isSrcDeviceEEligible(recordParts[5])){
                        if(excludedOperationNames.contains(operName+",")){
                            continue;
                        }

                        if("*".equals(includedOperationNames)){
                            reqCounter.incrementAndGet();
                        }
                        else {
                            if(includedOperationNames.contains(operName+",")){
                                reqCounter.incrementAndGet();
                            }
                        }
                    }
                }
            }

        }

        private boolean isSrcDeviceEEligible(String recordPart) {
            boolean result = false;

            if("*".equals(destinationSysName)){
                result = true;
            }
            else if(destinationSysName.equals(recordPart)){
                result = false;
            }

            return result;
        }
    }
}
