package com.syh.halfsynchalfasync_14;

import java.sql.Connection;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 18-11-4
 * Time: 下午4:40
 * To change this template use File | Settings | File Templates.
 */
public class SampleAlarmClient {

    private static final int ALARM_MSG_SUPRESS_THRESHOLD = 10;

    static {

    }

    public static void main(String[] args){
        SampleAlarmClient alarmClient = new SampleAlarmClient();
        Connection dbConn = null;
        try{
            dbConn = alarmClient.retrieveDBConnection();
        }
        catch (Exception e){
            final AlarmMgr alarmMgr = AlarmMgr.getInstance();

            // 告警被重复发送至告警模块的次数
            int duplicateSubmissionCount;
            String alarmId = "0000010000020";
            final String alarmExtraInfo = "Failed to get DB connnection:"+e.getMessage();

            duplicateSubmissionCount = alarmMgr.sendAlarm(AlarmType.FAULT, alarmId, alarmExtraInfo);
            if(duplicateSubmissionCount < ALARM_MSG_SUPRESS_THRESHOLD){
                System.out.println(String.format("Alarm %s raised, extraInfo: %s", alarmId, alarmExtraInfo));
            }
            else{
                if(duplicateSubmissionCount == ALARM_MSG_SUPRESS_THRESHOLD){
                    System.out.println(String.format("Alarm %s raised more than %s times, it will no longer be logged.",
                            alarmId, ALARM_MSG_SUPRESS_THRESHOLD));
                }
            }
        }
    }

    private Connection retrieveDBConnection() {
        return null;  //To change body of created methods use File | Settings | File Templates.
    }
}
