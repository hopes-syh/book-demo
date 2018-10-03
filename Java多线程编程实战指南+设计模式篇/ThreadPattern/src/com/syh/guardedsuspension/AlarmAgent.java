package com.syh.guardedsuspension;

import java.util.Timer;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 18-9-24
 * Time: 下午8:44
 * To change this template use File | Settings | File Templates.
 */
public class AlarmAgent {
    private volatile boolean connectedToServer = false;

    private final Predicate agentConnected = new Predicate() {
        @Override
        public boolean evaluate() {
            return connectedToServer;
        }
    };

    private final Blocker blocker = new ConditionVarBlocker();

    private final Timer heartbeatTimer = new Timer(true);

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

    // 通过网络连接将警告信息发送给服务器
    private void doSendAlarm(AlarmInfo alarm) {
        //To change body of created methods use File | Settings | File Templates.
    }

}
