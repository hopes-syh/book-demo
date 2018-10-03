package com.syh.guardedsuspension;

import java.util.concurrent.Callable;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 18-9-24
 * Time: 下午9:35
 * To change this template use File | Settings | File Templates.
 */
public class ConditionVarBlocker implements Blocker {
    private final Lock lock;
    private final Condition condition;

    public ConditionVarBlocker(Lock lock) {
        this.lock = lock;
        this.condition = lock.newCondition();
    }

    public ConditionVarBlocker() {
        this.lock = new ReentrantLock();
        this.condition = lock.newCondition();
    }

    @Override
    public <V> V callWithGuard(GuardedActionCallable<V> guardedAction) throws Exception {
        lock.lockInterruptibly();
        V result;
        try{
            final Predicate guard = guardedAction.guard;
            while (!guard.evaluate()){
                condition.await();
            }

            result = guardedAction.call();
            return result;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void signalAfter(Callable<Boolean> stateOperation) throws Exception {
        lock.lockInterruptibly();
        try{
            if (stateOperation.call()){
                condition.signal();
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void signal() throws InterruptedException {
        lock.lockInterruptibly();
        try{
            condition.signalAll();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void broadcastAfter(Callable<Boolean> stateOperation) throws Exception {
        lock.lockInterruptibly();
        try{
            if (stateOperation.call()){
                condition.signalAll();
            }
        } finally {
            lock.unlock();
        }
    }
}
