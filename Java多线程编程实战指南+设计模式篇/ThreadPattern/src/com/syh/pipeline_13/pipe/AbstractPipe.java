package com.syh.pipeline_13.pipe;

import com.syh.pipeline_13.PipeContext;

import java.util.concurrent.TimeUnit;

/**
 * Pipe的抽象实现类。
 * 该类会调用其子类实现 doProcess 方法对输入元素进行处理，并将相应的输出作为下一个Pipe实例的输入。
 *
 * @param <IN>
 *     输入类型
 * @param <OUT>
 *     输出类型
 */
public abstract class AbstractPipe<IN, OUT> implements Pipe<IN, OUT> {
    protected volatile Pipe<?, ?> nextPipe = null;
    protected volatile PipeContext pipeCtx;

    @Override
    public void setNextPipe(Pipe<?, ?> nextPipe) {
        this.nextPipe = nextPipe;
    }

    @Override
    public void init(PipeContext pipeCtx) {
        this.pipeCtx = pipeCtx;
    }

    @Override
    public void shutdown(long timeout, TimeUnit timeUnit) {
        // do nothing
    }

    @Override
    public void process(IN input) throws InterruptedException {
        try{
            OUT out = doProcess(input);

            if(null != nextPipe){
                if(null != nextPipe && null != out){
                    ((Pipe<OUT, ?>) nextPipe).process(out);
                }
            }
        } catch (InterruptedException e){
            Thread.currentThread().interrupt();
        }
    }

    protected abstract OUT doProcess(IN input);
}
