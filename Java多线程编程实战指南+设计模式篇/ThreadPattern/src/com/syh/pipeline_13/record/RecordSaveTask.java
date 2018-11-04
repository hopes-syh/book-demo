package com.syh.pipeline_13.record;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 18-11-3
 * Time: 下午8:15
 * To change this template use File | Settings | File Templates.
 */
public class RecordSaveTask {
    public Record[] records;
    public String recordDay;
    public int targetFileIndex;

    public RecordSaveTask(Record[] records, int targetFileIndex) {
        //To change body of created methods use File | Settings | File Templates.
    }

    public RecordSaveTask(String lastRecordDay, int targetFileIndex) {
        //To change body of created methods use File | Settings | File Templates.
    }
}
