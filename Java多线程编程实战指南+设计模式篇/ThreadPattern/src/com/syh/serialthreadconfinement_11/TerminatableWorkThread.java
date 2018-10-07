package com.syh.serialthreadconfinement_11;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 18-10-7
 * Time: 下午6:38
 * To change this template use File | Settings | File Templates.
 */
public class TerminatableWorkThread<T, V> extends AbstractTerminatableThread {
    private final BlockingQueue<Runnable> workQueue;

    private final TaskProcessor<T, V> taskProcessor;

    public TerminatableWorkThread(BlockingQueue<Runnable> workQueue, TaskProcessor<T, V> taskProcessor) {
        this.workQueue = workQueue;
        this.taskProcessor = taskProcessor;
    }

    public Future<V> submit(final T task) throws InterruptedException {
        Callable<V> callable = new Callable<V>() {
            @Override
            public V call() throws Exception {
                return taskProcessor.doProcess(task);
            }
        };

        FutureTask<V> ft = new FutureTask<V>(callable);
        workQueue.put(ft);

        return ft;
    }

    @Override
    protected void doRun() throws Exception {
        Runnable ft = workQueue.take();
        ft.run();
    }

    @Override
    protected void doCleanUp() {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
