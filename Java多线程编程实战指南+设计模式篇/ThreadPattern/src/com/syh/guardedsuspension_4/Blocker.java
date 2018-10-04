package com.syh.guardedsuspension_4;

import java.util.concurrent.Callable;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 18-9-24
 * Time: 下午9:18
 * To change this template use File | Settings | File Templates.
 */
public interface Blocker {

    /**
     * 在保护条件成立时执行目标动作，否则阻塞当前线程，直到保护条件成立；
     * @param guardedAction
     * @param <V>
     * @return
     * @throws Exception
     */
    <V> V callWithGuard(GuardedActionCallable<V> guardedAction) throws Exception;

    /**
     * 执行 stateOperations 所指定的操作后，决定是否唤醒本 Blocker
     * 所暂挂的所有线程中的一个线程
     *
     * @param stateOperation
     * @throws Exception
     */
    void signalAfter(Callable<Boolean> stateOperation) throws Exception;

    void signal() throws InterruptedException;

    /**
     * 执行 stateOperations 所指定的操作后，决定是否唤醒本 Blocker
     * 所暂挂的所有线程中的一个线程
     *
     * @param stateOperation
     * @throws Exception
     */
    void broadcastAfter(Callable<Boolean> stateOperation) throws Exception;
}
