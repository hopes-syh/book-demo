package com.syh.serialthreadconfinement_11;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 18-10-7
 * Time: 下午6:35
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractSerializer<T, V> {
    private final TerminatableWorkThread<T, V> workThread;

    protected AbstractSerializer(BlockingQueue<Runnable> workQueue, TaskProcessor<T, V> taskProcessor) {
        workThread = new TerminatableWorkThread<T, V>(workQueue, taskProcessor);
    }

    /**
     * 用于根据指定参数生成相应的任务实例
     */
    protected abstract T makeTask(Object... params);

    /**
     * 该类对外暴露服务方法，该类的子类需要定义一个命名含义比该方法更为具体的方法（如：downloadFile）
     * @param params
     * @return
     * @throws InterruptedException
     */
    protected Future<V> service(Object... params) throws InterruptedException {
        T task = makeTask(params);
        Future<V> resultPromise = workThread.submit(task);
        return resultPromise;
    }

    public void init(){
        workThread.start();
    }

}
