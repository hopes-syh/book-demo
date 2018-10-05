package com.syh.activeobject_8;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 18-10-5
 * Time: 上午10:35
 * To change this template use File | Settings | File Templates.
 */
// 模式角色：ActiveObject.Proxy
public class AsyncRequestPersistence implements RequestPersistence{

    private final AtomicLong taskTimeConsumedPerInterval = new AtomicLong(0);
    private final AtomicInteger requestSubmittedPerInterval = new AtomicInteger(0);

    // 模式角色：ActiveObject.Servant
    private final RequestPersistence delegate = new DiskbasedRequestPersistence();

    // 模式角色：ActiveObject.Scheduler
    private final ThreadPoolExecutor scheduler;

    // 用于保存 AsyncRequestPersistenced 的唯一实例
    private static class InstanceHolder{
        final static RequestPersistence INSTANCE = new AsyncRequestPersistence();
    }

    private AsyncRequestPersistence() {

        // 模式角色：ActiveObject.ActivationQueue
        BlockingQueue<Runnable> blockingQueue = new ArrayBlockingQueue<Runnable>(200);

        this.scheduler = new ThreadPoolExecutor(1, 3, 60*60, TimeUnit.SECONDS, blockingQueue, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "AsyncRequestPersistence");
            }
        });
        scheduler.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // 启动队列监控定时任务
        Timer monitorTimer = new Timer(true);
        monitorTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                taskTimeConsumedPerInterval.set(0);
                requestSubmittedPerInterval.set(0);
            }
        }, 0, 60 * 1000);
    }

    public static RequestPersistence getInstance() {
        return InstanceHolder.INSTANCE;
    }

    @Override
    public void store(final Recipient shortNumberRecipient) {
        // 模式角色：ActiveObject.MethodRequest;
        Callable<Boolean> methodRequest = new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                long start = System.currentTimeMillis();
                try{
                    delegate.store(shortNumberRecipient);
                } finally {
                    taskTimeConsumedPerInterval.addAndGet(System.currentTimeMillis() - start);
                }
                return Boolean.TRUE;
            }
        };

        this.scheduler.submit(methodRequest);
    }
}
