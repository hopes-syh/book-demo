package com.syh.halfsynchalfasync_14;

import com.sun.corba.se.impl.orbutil.fsm.GuardedAction;
import com.syh.guardedsuspension_4.*;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;

/**
 * 负责连接告警服务器，并发送告警信息至告警服务器
 */
public class AlarmAgent {

    // 用于记录AlarmAgent是否连接上告警服务器
    private volatile boolean connectedToServer = false;

    // 模式角色：GuardedSuspension.Predicate
    private final Predicate agentConnected = new Predicate() {
        @Override
        public boolean evaluate() {
            return connectedToServer;
        }
    };

    // 模式角色：GuardedSuspension.Blocker
    private final Blocker blocker = new ConditionVarBlocker();

    // 心跳定时器
    private final Timer heartbeatTimer = new Timer(true);

    /**
     * 发送告警信息
     */
    public void sendAlarm(final AlarmInfo alarm) throws Exception{
        // 可能需要等待，知道AlarmAgent连接上告警服务器
        // 模式角色：GuardedSuspension.GuardedAction
        GuardedActionCallable<Void> guardedAction = new GuardedActionCallable<Void>(agentConnected){
            @Override
            public Void call() throws Exception {
                doSendAlarm(alarm);
                return null;
            }
        };

        blocker.callWithGuard(guardedAction);
    }

    private void doSendAlarm(AlarmInfo alarm) {
        // 发送到告警服务器
    }

    public void init(){
        // 告警连接线程
        Thread connectingThread = new Thread(new ConnectingTask());

        connectingThread.start();

        heartbeatTimer.schedule(new HeartbeatTask(), 60000, 2000);
    }

    private class HeartbeatTask extends TimerTask {
        @Override
        public void run() {
            if(!testConnection()){
                onDisconnection();
                reconnect();
            }
        }

        private void reconnect() {
            ConnectingTask connectingTask = new ConnectingTask();
            connectingTask.run();
        }

        private boolean testConnection() {
            return true;
        }
    }

    private void onDisconnection() {
        connectedToServer = false;
    }

    private void onConnected() {
        try {
            blocker.signalAfter(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    connectedToServer = true;
                    System.out.println("connected to server");
                    return Boolean.TRUE;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private class ConnectingTask implements Runnable {
        @Override
        public void run() {
            onConnected();
        }
    }
}
