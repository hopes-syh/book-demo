package com.syh.pipeline_13.pipeline;

import com.syh.pipeline_13.PipeContext;
import com.syh.pipeline_13.decorator.ThreadPoolPipeDecorator;
import com.syh.pipeline_13.decorator.WorkerThreadPipeDecorator;
import com.syh.pipeline_13.pipe.AbstractPipe;
import com.syh.pipeline_13.pipe.Pipe;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 18-10-21
 * Time: 下午9:45
 * To change this template use File | Settings | File Templates.
 */
public class SimplePipeline<T, OUT> extends AbstractPipe<T, OUT> implements Pipeline<T, OUT> {
    private final Queue<Pipe<?, ?>> pipes = new LinkedList<Pipe<?, ?>>();

    private final ExecutorService helperExecutor;

    public SimplePipeline(){
        this(Executors.newSingleThreadExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "simplePipeline-helper");
                t.setDaemon(true);
                return t;
            }
        }));
    }

    public SimplePipeline(ExecutorService helperExecutor) {
        super();
        this.helperExecutor = helperExecutor;
    }

    public void shutdown(long timeout, TimeUnit timeUnit){
        Pipe<?, ?> pipe;
        while (null != (pipe = pipes.poll())){
            pipe.shutdown(timeout, timeUnit);
        }

        helperExecutor.shutdown();
    }

    @Override
    protected OUT doProcess(T input) {
        return null;
    }

    @Override
    public void addPipe(Pipe<?, ?> pipe) {
        pipes.add(pipe);
    }

    public <IN, OUT> void addAsWorkerThreadBasedPipe(Pipe<IN, OUT> delegate, int workerCount){
        addPipe(new WorkerThreadPipeDecorator<IN, OUT>(delegate, workerCount));
    }

    public <IN, OUT> void addAsThreadPoolBasedPipe(Pipe<IN, OUT> delegate, ExecutorService executorService){
        addPipe(new ThreadPoolPipeDecorator<IN, OUT>(delegate, executorService));
    }

    @Override
    public void process(T input) throws InterruptedException {
        Pipe<T, ?> firstPipe = (Pipe<T, ?>) pipes.peek();
        firstPipe.process(input);
    }

    public void init(final PipeContext ctx){
        LinkedList<Pipe<?, ?>> pipesList = (LinkedList<Pipe<?, ?>>) pipes;
        Pipe<?, ?> prevPipe = this;
        for(Pipe<?, ?> pipe : pipesList){
            prevPipe.setNextPipe(pipe);
            prevPipe = pipe;
        }

        Runnable task = new Runnable() {
            @Override
            public void run() {
                for(Pipe<?, ?> pipe:pipes){
                    pipe.init(ctx);
                }
            }
        };

        helperExecutor.submit(task);
    }

    public PipeContext newDefaultPipeContext(){
        return new PipeContext(){
            public void handlerError(final Exception e){
                helperExecutor.submit(new Runnable() {
                    @Override
                    public void run() {
                        e.printStackTrace();
                    }
                });
            }
        };
    }
}
