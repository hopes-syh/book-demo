package com.syh.pipeline_13.pipe;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * 各个子任务的执行结果会被合并为相应输入元素的输出结果
 * @param <IN>
 * @param <OUT>
 * @param <V>
 *     并行子任务的处理结果类型
 */
public abstract class AbstractParallelPipe<IN, OUT, V> extends AbstractPipe<IN, OUT> {
    private final ExecutorService executorService;

    public AbstractParallelPipe(BlockingQueue<IN> queue, ExecutorService executorService) {
        super();
        this.executorService = executorService;
    }

    /**
     * 用于根据指定的输入元素input构造一组子任务
     */
    protected abstract List<Callable<V>> buildTasks(IN input) throws Exception;

    /**
     * 留给子类实现。对各个子任务的处理结果进行合并，形成相应输入元素，形成相应输入元素的输出结果
     */
    protected abstract OUT combineResults(List<Future<V>> subTaskResults) throws Exception;

    protected List<Future<V>> invokeParallel(List<Callable<V>> tasks) throws Exception{
        return executorService.invokeAll(tasks);
    }

    @Override
    protected OUT doProcess(IN input) {
        OUT out = null;
        try {
            out = combineResults(invokeParallel(buildTasks(input)));
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return out;
    }
}
