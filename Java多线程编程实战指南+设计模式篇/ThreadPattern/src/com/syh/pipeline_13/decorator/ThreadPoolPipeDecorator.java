package com.syh.pipeline_13.decorator;

import com.syh.pipeline_13.PipeContext;
import com.syh.pipeline_13.pipe.Pipe;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 18-10-21
 * Time: 下午8:26
 * To change this template use File | Settings | File Templates.
 */
public class ThreadPoolPipeDecorator<IN, OUT> implements Pipe<IN, OUT> {
    private final Pipe<IN, OUT> delegate;
    private final ExecutorService executorService;

    // 线程池停止标志
    private final TerminationToken terminationToken;
    private final CountDownLatch stageProcessDoneLatch = new CountDownLatch(1);

    public ThreadPoolPipeDecorator(Pipe<IN, OUT> delegate, ExecutorService executorService) {
        this.delegate = delegate;
        this.executorService = executorService;
        this.terminationToken = TerminationToken.newInstance(executorService);
    }

    @Override
    public void setNextPipe(Pipe<?, ?> nextPipe) {
        delegate.setNextPipe(nextPipe);
    }

    @Override
    public void init(PipeContext pipeCtx) {
        delegate.init(pipeCtx);
    }

    @Override
    public void shutdown(long timeout, TimeUnit timeUnit) {
        terminationToken.setIsToShutdown();

        if(terminationToken.reservations.get() > 0){
            if(stageProcessDoneLatch.getCount() > 0){
                try {
                    stageProcessDoneLatch.await(timeout, timeUnit);
                } catch (InterruptedException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }

        delegate.shutdown(timeout, timeUnit);
    }

    @Override
    public void process(final IN input) throws InterruptedException {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                int remainingReservations = -1;

                try {
                    delegate.process(input);
                } catch (InterruptedException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } finally {
                    remainingReservations = terminationToken.reservations.decrementAndGet();
                }

                if(terminationToken.isToShutdown() && 0==remainingReservations){
                    stageProcessDoneLatch.countDown();
                }
            }
        };

        executorService.submit(task);
        terminationToken.reservations.incrementAndGet();
    }
}
