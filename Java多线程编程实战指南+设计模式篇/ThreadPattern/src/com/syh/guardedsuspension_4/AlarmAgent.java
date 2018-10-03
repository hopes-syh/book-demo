package com.syh.guardedsuspension_4;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 负责与告警服务器进行对接
 * <br/> 如果和“生产者-消费者”模式结合，则由“消费者”与告警服务器发送消息，在消费者端，则可以使用该模式；
 * <br/> 如果是多个线程在某个条件下才能进行计算的话，也适合使用这种模式；
 *
 * User: Administrator
 * Date: 18-9-24
 * Time: 下午8:44
 * To change this template use File | Settings | File Templates.
 */
public class AlarmAgent {
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

    private final Timer heartbeatTimer = new Timer(true);

    /**
     * 负责通过网络连接，将告警信息发送给告警服务器
     * <br/> 如果网络连接没建立，sendAlarm需要等待连接建立好再发送；
     * <br/> 如果网络中间断开了，sendAlarm需要在心跳将连接建立好了，再发送
     *
     * @param alarm
     * @throws Exception
     */
    public void sendAlarm(final AlarmInfo alarm) throws Exception {
        GuardedActionCallable<Void> guardedActionCallable = new GuardedActionCallable<Void>(agentConnected) {
            @Override
            public Void call() throws Exception {
                doSendAlarm(alarm);
                return null;
            }
        };

        blocker.callWithGuard(guardedActionCallable);
    }

    public void sendAlarmPool(final AlarmInfo alarm) throws Exception {
        GuardedActionCallable<Void> guardedActionCallable = new GuardedActionCallable<Void>(agentConnected) {
            @Override
            public Void call() throws Exception {
                doSendAlarm(alarm);
                return null;
            }
        };

        blocker.callWithGuardPool(guardedActionCallable);
    }

    // 通过网络连接将警告信息发送给服务器
    private void doSendAlarm(AlarmInfo alarm) {
        System.out.println("sending alarm " + alarm);

        // 模拟发送
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void init(){
        // 省略其他代码

        // 告警连接线程
        Thread connectingThread = new Thread(new ConnectionTask());
        connectingThread.start();

        heartbeatTimer.schedule(new HeartbeatTask(), 60000, 2000);
    }

    protected void onConnected(){
        try {
            blocker.signalAfter(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    connectedToServer = true;
                    System.out.println("connect to server");
                    return Boolean.TRUE;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onDisconnected() {
        connectedToServer = false;
    }

    /**
     * 负责与告警服务器建立连接
     */
    private class ConnectionTask implements Runnable {

        @Override
        public void run() {
            // 省略其他代码

            // 模拟连接操作
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            onConnected();
        }
    }

    private class HeartbeatTask extends TimerTask {
        @Override
        public void run() {
            if(!testConnection()){
                onDisconnected();
                reconnect();
            }
        }

        private void reconnect() {
            ConnectionTask connectionThread = new ConnectionTask();

            // 直接在心跳定时器线程中执行
            connectionThread.run();
        }

        private boolean testConnection() {
            return true;
        }
    }
}
