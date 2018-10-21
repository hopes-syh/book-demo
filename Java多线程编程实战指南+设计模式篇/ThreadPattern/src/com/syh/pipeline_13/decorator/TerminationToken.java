package com.syh.pipeline_13.decorator;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 18-10-21
 * Time: 下午8:12
 * To change this template use File | Settings | File Templates.
 */
public class TerminationToken {
    public AtomicInteger reservations = new AtomicInteger(0);

    public static TerminationToken newInstance(ExecutorService executorService) {
        return null;  //To change body of created methods use File | Settings | File Templates.
    }

    public boolean isToShutdown() {
        return false;  //To change body of created methods use File | Settings | File Templates.
    }

    public void setIsToShutdown() {
        //To change body of created methods use File | Settings | File Templates.
    }
}
