package com.syh.pipeline_13;

import java.sql.ResultSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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





        return null;  //To change body of created methods use File | Settings | File Templates.
    }
}
