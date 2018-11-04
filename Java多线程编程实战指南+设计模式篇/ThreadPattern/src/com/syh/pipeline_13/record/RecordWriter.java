package com.syh.pipeline_13.record;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 18-11-3
 * Time: 下午8:18
 * To change this template use File | Settings | File Templates.
 */
public class RecordWriter {
    public static RecordWriter getInstance() {
        return new RecordWriter();
    }

    public File finishRecords(String recordDay, int targetFileIndex){
        return null;
    }

    public File write(Record[] records, int targetFileIndex) {
        return null;  //To change body of created methods use File | Settings | File Templates.
    }
}
