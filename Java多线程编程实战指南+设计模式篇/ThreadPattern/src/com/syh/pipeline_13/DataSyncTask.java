package com.syh.pipeline_13;

import com.syh.pipeline_13.pipe.AbstractParallelPipe;
import com.syh.pipeline_13.pipe.AbstractPipe;
import com.syh.pipeline_13.pipe.Pipe;
import com.syh.pipeline_13.pipe.SimplePipeline;
import com.syh.pipeline_13.record.Record;
import com.syh.pipeline_13.record.RecordSaveTask;
import com.syh.pipeline_13.record.RecordWriter;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 18-10-21
 * Time: 下午3:19
 * To change this template use File | Settings | File Templates.
 */
public class DataSyncTask implements Runnable {
    @Override
    public void run() {
        ResultSet rs = null;
        SimplePipeline<RecordSaveTask, String> pipeline = buildPipeline();
        pipeline.init(pipeline.newDefaultPipeContext());

        Connection dbConn = null;
        try {
            dbConn = getConnection();
            rs = queryRecords(dbConn);

            processRecords(rs, pipeline);
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private void processRecords(ResultSet rs, SimplePipeline<RecordSaveTask, String> pipeline) throws SQLException, InterruptedException {
        Record record;
        Record[] records = new Record[12];

        int targetFileIndex = 0;
        int nextTargetFileIndex = 0;
        int recordCountInTheDay = 0;
        int recordCountInTheFile = 0;
        String recordDay = null;
        String lastRecordDay = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");

        while (rs.next()){
            record = makeRecordForm(rs);
            lastRecordDay = recordDay;

            recordDay = sdf.format(record.getOperationTime());
            if(recordDay.equals(lastRecordDay)){
                records[recordCountInTheFile] = record;
                recordCountInTheDay++;
            }
            else {
                // 实际已发生的不同日期记录文件切换
                if(null != lastRecordDay){
                    if(recordCountInTheFile >= 1){
                        pipeline.process(new RecordSaveTask(Arrays.copyOf(records, recordCountInTheFile), targetFileIndex));
                    }
                    else{
                        pipeline.process(new RecordSaveTask(lastRecordDay, targetFileIndex));
                    }

                    // 在此之前，先将 records 中的内容写入文件
                    records[0] = record;
                    recordCountInTheFile = 0;
                }
                else {
                    records[0] = record;
                }
            }

            if(nextTargetFileIndex == targetFileIndex){
                recordCountInTheFile++;
                if(0==(recordCountInTheFile % 10000)){
                    pipeline.process(new RecordSaveTask(Arrays.copyOf(records, recordCountInTheFile), targetFileIndex));
                    recordCountInTheFile = 0;
                }
            }
        }

        nextTargetFileIndex = recordCountInTheDay / 10000;
        if(nextTargetFileIndex > targetFileIndex){
            // 预测到将发生同日期记录文件切换；

            if(recordCountInTheFile > 1){
                pipeline.process(new RecordSaveTask(Arrays.copyOf(records, recordCountInTheFile), targetFileIndex));
            }
            else{
                pipeline.process(new RecordSaveTask(lastRecordDay, targetFileIndex));
            }
            recordCountInTheFile = 0;
            targetFileIndex = nextTargetFileIndex;
        }
        else if(nextTargetFileIndex < targetFileIndex){
            // 实际已发生的异日期记录文件切换，recordCountInTheFile保持当前值
            targetFileIndex = nextTargetFileIndex;
        }

        if(recordCountInTheFile > 0){
            pipeline.process(new RecordSaveTask(Arrays.copyOf(records, recordCountInTheFile), targetFileIndex));
        }
    }

    private Record makeRecordForm(ResultSet rs) {
        return null;  //To change body of created methods use File | Settings | File Templates.
    }

    private ResultSet queryRecords(Connection dbConn) {
        return null;  //To change body of created methods use File | Settings | File Templates.
    }

    private SimplePipeline<RecordSaveTask, String> buildPipeline() {
        /**
         * 线程池的本质是重复利用一定数量的线程，而不是针对每个任务都有一个专门的工作者线程
         * 这里，各个pipe的初始化工作可以在上游的Pipe初始化完毕后再初始化其后继Pipe，而不必
         * 多个Pipe同时初始化。
         * 因此，这个初始化的动作可以由一个线程来处理。该线程处理完各个线程的pipe的初始化后，
         * 可以继续处理之后可能产生的任务，如错误处理。
         * 所以，上述这些先后产生的任务可以由线程池中的一个工作线程从头到尾负责执行。
         */
        final ExecutorService helperExecutor = Executors.newSingleThreadExecutor();

        final SimplePipeline<RecordSaveTask, String> pipeline =
                new SimplePipeline<RecordSaveTask, String>(helperExecutor);

        /**
         * 根据数据库记录，生成相应的数据文件
         */
        Pipe<RecordSaveTask, File> stageSaveFile = new AbstractPipe<RecordSaveTask, File>() {
            @Override
            protected File doProcess(RecordSaveTask task) {
                final RecordWriter recordWriter = RecordWriter.getInstance();
                final Record[] records = task.records;
                File file;
                if(null == records) {
                    file = recordWriter.finishRecords(task.recordDay, task.targetFileIndex);
                }
                else {
                    file = recordWriter.write(records, task.targetFileIndex);
                }
                return file;
            }
        };

        /**
         * 由于这里的几个Pipe都是处理I/O的，为了避免使用锁（以减少不必要的上下文切换）
         * 但又能保证线程安全，故每个Pipe都采用单线程处理。
         * 若各个Pipe改用线程池来处理，需要注意：1）线程安全；2）死锁；
         */
        pipeline.addAsWorkerThreadBasedPipe(stageSaveFile, 1);

        /**
         * 将生成的数据传到指定的主机上。
         */
        final ThreadPoolExecutor ftpExecutorService = new ThreadPoolExecutor(1, 10, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(10));
        Pipe<File, File> stageTransferFile = new AbstractParallelPipe<File, File, File>(new SynchronousQueue<File>(),
                ftpExecutorService) {
            @Override
            protected List<Callable<File>> buildTasks(File input) throws Exception {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            protected File combineResults(List<Future<File>> subTaskResults) throws Exception {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }
        };
        pipeline.addAsWorkerThreadBasedPipe(stageTransferFile, 1);

        /**
         * 备份已经传输的数据文件
         */
        Pipe<File, Void> stageBackupFile = new AbstractPipe<File, Void>() {
            @Override
            protected Void doProcess(File input) {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }
        };
        pipeline.addAsWorkerThreadBasedPipe(stageBackupFile, 1);

        return pipeline;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection("", "", "");
    }
}
