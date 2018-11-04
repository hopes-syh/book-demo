package com.syh.pipeline_13.decorator;

import com.syh.pipeline_13.AbstractTerminatableThread;
import com.syh.pipeline_13.pipe.Pipe;
import com.syh.pipeline_13.pipe.PipeContext;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 18-10-21
 * Time: 下午7:53
 * To change this template use File | Settings | File Templates.
 */
public class WorkerThreadPipeDecorator<IN, OUT> implements Pipe<IN, OUT> {
    protected final BlockingQueue<IN> workQueue;

    private final Set<AbstractTerminatableThread> workerThreads = new HashSet<AbstractTerminatableThread>();

    private final TerminationToken terminationToken = new TerminationToken();

    private final Pipe<IN, OUT> delegate;

    public WorkerThreadPipeDecorator(BlockingQueue<IN> workQueue, Pipe<IN, OUT> delegate, int workerCount) {
        if(workerCount <= 0){
            throw new IllegalArgumentException("workerCount should be positive!");
        }

        this.workQueue = workQueue;
        this.delegate = delegate;
        for(int i=0; i<workerCount; i++){
            workerThreads.add(new AbstractTerminatableThread(terminationToken){
                @Override
                protected void doRun() throws Exception {
                    try{
                        dispatch();
                    } finally {
                        terminationToken.reservations.decrementAndGet();
                    }
                }
            });
        }
    }

    public WorkerThreadPipeDecorator(Pipe<IN, OUT> delegate, int workerCount) {
        this(new SynchronousQueue<IN>(), delegate, workerCount);
    }

    private void dispatch() throws InterruptedException {
        IN input = workQueue.take();
        delegate.process(input);
    }

    @Override
    public void setNextPipe(Pipe<?, ?> nextPipe) {
        delegate.setNextPipe(nextPipe);
    }

    @Override
    public void init(PipeContext pipeCtx) {
        delegate.init(pipeCtx);
        for(AbstractTerminatableThread thread : workerThreads){
            thread.start();
        }
    }

    @Override
    public void shutdown(long timeout, TimeUnit timeUnit) {
        for(AbstractTerminatableThread thread : workerThreads){
            thread.terminate();
        }
        delegate.shutdown(timeout, timeUnit);
    }

    @Override
    public void process(IN input) throws InterruptedException {
        workQueue.put(input);
        terminationToken.reservations.incrementAndGet();
    }
}
