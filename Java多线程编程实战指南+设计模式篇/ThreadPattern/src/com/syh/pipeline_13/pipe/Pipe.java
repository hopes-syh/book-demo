package com.syh.pipeline_13.pipe;


import java.util.concurrent.TimeUnit;

/**
 * 对处理现阶段的抽象
 * 负责对输入进行处理，并将输出作为下一个处理阶段的输入
 *
 * @param <IN>
 *     输入类型
 * @param <OUT>
 *     输出类型
 */
public interface Pipe<IN, OUT> {

    /**
     * 设置当前Pipe实例的下一个pipe实例
     *
     * @param nextPipe 下一个pipe实例
     */
    void setNextPipe(Pipe<?, ?> nextPipe);

    /**
     * 初始化当前pipe实例对外提供的服务
     * @param pipeCtx
     */
    void init(PipeContext pipeCtx);

    /**
     * 停止当前Pipe实例对外提供的服务
     * @param timeout
     * @param timeUnit
     */
    void shutdown(long timeout, TimeUnit timeUnit);

    /**
     * 对输入元素进行处理，并将处理结果作为下一个Pipe的输入
     * @param input
     * @throws InterruptedException
     */
    void process(IN input) throws InterruptedException;
}
