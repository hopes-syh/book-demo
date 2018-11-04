package com.syh.halfsynchalfasync_14;

import com.syh.guardedsuspension_4.AlarmInfo;
import com.syh.pipeline_13.AbstractTerminatableThread;
import com.syh.pipeline_13.decorator.TerminationToken;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 警告发送线程
 * 模式角色：HalfSync/HalfAsync.AsyncTask
 */
public class AlarmSendingThread extends AbstractTerminatableThread{

    private final AlarmAgent alarmAgent = new AlarmAgent();

    /**
     * 告警队列
     * 模式角色：HalfSync/HalfAsync.Queue
     */
    private final BlockingQueue<AlarmInfo> alarmQueue;
    private final ConcurrentMap<String, AtomicInteger> submittedAlarmRegistry;

    public AlarmSendingThread() {
        super(new TerminationToken());
    }

    @Override
    protected void doRun() throws Exception {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
