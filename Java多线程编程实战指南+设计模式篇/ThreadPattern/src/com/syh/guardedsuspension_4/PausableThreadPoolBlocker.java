package com.syh.guardedsuspension_4;

import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 18-10-4
 * Time: 下午9:42
 * To change this template use File | Settings | File Templates.
 */
public class PausableThreadPoolBlocker implements Blocker {

    /**
     * 这里不需要锁，原因有两个：
     * 1. GuardedActionCallable.guard.evaluate() 是 volatile，内存可见；
     * 2. PausableThreadPool 线程池中，暂停、启动都已经加锁了
     */
    // private final Lock lock;
    private final PausableThreadPool pool = new PausableThreadPool(2, 10, 10L, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>(10));

    public PausableThreadPoolBlocker(Lock lock) {
        // this.lock = lock;
    }

    public PausableThreadPoolBlocker() {
        // this.lock = new ReentrantLock();
    }

    @Override
    public <V> V callWithGuard(GuardedActionCallable<V> guardedAction) throws Exception {
        // lock.lockInterruptibly();
        V result;
        try{
            final Predicate guard = guardedAction.guard;
            while (!guard.evaluate()){
                pool.pause();
            }

            Future<V> future = pool.submit(guardedAction);
            result = future.get();
            return result;
        } finally {
            // lock.unlock();
        }
    }

    @Override
    public void signalAfter(Callable<Boolean> stateOperation) throws Exception {
        // lock.lockInterruptibly();
        try{
            if (stateOperation.call()){
                pool.resume();
            }
        } finally {
            // lock.unlock();
        }
    }

    @Override
    public void signal() throws InterruptedException {
        // lock.lockInterruptibly();
        try{
            pool.resumeAll();
        } finally {
            // lock.unlock();
        }
    }

    @Override
    public void broadcastAfter(Callable<Boolean> stateOperation) throws Exception {
        // lock.lockInterruptibly();
        try{
            if (stateOperation.call()){
                pool.resumeAll();
            }
        } finally {
            // lock.unlock();
        }
    }
}
