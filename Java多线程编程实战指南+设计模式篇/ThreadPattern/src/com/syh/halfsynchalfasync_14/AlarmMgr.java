package com.syh.halfsynchalfasync_14;

/**
 * 告警功能入口
 * 模式角色：HalfSync/HalfAsync.AsyncTask
 */
public class AlarmMgr {
    private static final AlarmMgr instance = new AlarmMgr();

    private volatile boolean shutdownRequested = false;

    // 警告发送线程
    private final AlarmSendingThread alarmSendingThread;

    private AlarmMgr(){
        alarmSendingThread = new AlarmSendingThread(null, null);
    }

    public static AlarmMgr getInstance() {
        return instance;
    }

    public int sendAlarm(AlarmType fault, String alarmId, String alarmExtraInfo) {
        return 0;  //To change body of created methods use File | Settings | File Templates.
    }

}
